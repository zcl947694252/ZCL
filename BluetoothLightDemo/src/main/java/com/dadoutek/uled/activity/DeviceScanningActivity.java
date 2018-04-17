package com.dadoutek.uled.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dadoutek.uled.R;
import com.dadoutek.uled.TelinkLightApplication;
import com.dadoutek.uled.TelinkLightService;
import com.dadoutek.uled.TelinkMeshErrorDealActivity;
import com.dadoutek.uled.adapter.GroupsRecyclerViewAdapter;
import com.dadoutek.uled.intf.OnRecyclerviewItemClickListener;
import com.dadoutek.uled.model.Cmd;
import com.dadoutek.uled.model.Constant;
import com.dadoutek.uled.model.Group;
import com.dadoutek.uled.model.Groups;
import com.dadoutek.uled.model.Light;
import com.dadoutek.uled.model.Lights;
import com.dadoutek.uled.model.Mesh;
import com.dadoutek.uled.model.SharedPreferencesHelper;
import com.dadoutek.uled.util.DataCreater;
import com.dadoutek.uled.util.TimeUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.telink.bluetooth.LeBluetooth;
import com.telink.bluetooth.TelinkLog;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.LeScanEvent;
import com.telink.bluetooth.event.MeshEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.light.DeviceInfo;
import com.telink.bluetooth.light.LeAutoConnectParameters;
import com.telink.bluetooth.light.LeRefreshNotifyParameters;
import com.telink.bluetooth.light.LeScanParameters;
import com.telink.bluetooth.light.LeUpdateParameters;
import com.telink.bluetooth.light.LightAdapter;
import com.telink.bluetooth.light.NotificationInfo;
import com.telink.bluetooth.light.Parameters;
import com.telink.util.Event;
import com.telink.util.EventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public final class DeviceScanningActivity extends TelinkMeshErrorDealActivity implements AdapterView.OnItemClickListener, EventListener<String> {
    private static final String TAG = DeviceScanningActivity.class.getSimpleName();
    private static final int SCAN_TIMEOUT_SECOND = 10;
    //    @Bind(R.id.recycler_view_groups)
    android.support.v7.widget.RecyclerView recyclerViewGroups;
    //    @Bind(R.id.groups_bottom)
    LinearLayout groupsBottom;
    private ImageView backView;
    private Button btnScan;
    private Button btnLog;
    private Button btnAddGroups;
    private Button btnGroupingCompleted;

    private LayoutInflater inflater;
    private DeviceListAdapter adapter;

    private TelinkLightApplication mApplication;
    private List<DeviceInfo> updateList;

    private boolean isInit = false;
    private RxPermissions mRxPermission;
    private GridView deviceListView;
    private Handler mHandler = new Handler();

    private int preTime = 0;
    private int nextTime = 0;
    private boolean grouping;
    private boolean canStartTimer = true;
    private Timer timer;
    private Dialog loadDialog;
    private Groups groups = Groups.getInstance();
    GroupsRecyclerViewAdapter groupsRecyclerViewAdapter;
    //当前所选组index
    private int currentGroupIndex = -1;
    //灯的mesh地址
    private int dstAddress;
    //标记登录状态
    private boolean isLoginSuccess = false;
    //灯的所属分组缓存
    private List<Group> lightToGroupList = null;
    //分组所含灯的缓存
    private Lights nowLightList = Lights.getInstance();
    private ArrayList<Integer> indexList = new ArrayList<>();

    private final MyHandler handler = new MyHandler(this);

    //防止内存泄漏
    CompositeDisposable mDisposable = new CompositeDisposable();

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == backView) {
                finish();
            } else if (v == btnScan) {
                finish();
                //stopScanAndUpdateMesh();
            } else if (v.getId() == R.id.btn_log) {
                startActivity(new Intent(DeviceScanningActivity.this, LogInfoActivity.class));
            }
        }
    };

    //扫描失败处理方法
    private void scanFail() {
        btnAddGroups.setVisibility(View.VISIBLE);
        btnGroupingCompleted.setVisibility(View.GONE);
        btnAddGroups.setText("重新扫描");
        btnAddGroups.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan(0);
                btnAddGroups.setVisibility(View.GONE);
            }
        });
        if (timer != null) {
            timer.cancel();
        }
        closeDialog();
        showToast(getString(R.string.scan_end));
        finish();
        canStartTimer = false;
        nextTime = 0;
    }

    //处理扫描成功后
    private void scanSuccess() {

        //先连接灯。
        autoConnect();

        btnAddGroups.setVisibility(View.VISIBLE);
        btnAddGroups.setText("开始分组");
        if (nowLightList != null && nowLightList.size() > 0) {
            nowLightList.clear();
        }
        nowLightList.add(adapter.getLights());
        btnAddGroups.setOnClickListener(v -> {
            if (isLoginSuccess) {
                //进入分组
                startGrouping();
            } else {
                openLoadingDialog(getResources().getString(R.string.device_login_tip));
                Consumer<Boolean> loginConsumer = aBoolean -> {
                    if (aBoolean) {
                        //收到登录成功的时间后关闭dialog并自动进入分组流程。
                        closeDialog();
                        startGrouping();
                    }
                };
                mDisposable.add(Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    //循环检测isLoginSuccess
                    while (true) {
                        Thread.sleep(20);   //两次检测之间的延时是必须的
                        Log.d("Saw", "isLoginSuccess = " + isLoginSuccess);
                        //如果isLoginSuccess为 true，则发射事件并退出循环检测。
                        if (isLoginSuccess) {
                            emitter.onNext(true);
                            emitter.onComplete();
                            break;
                        }
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(loginConsumer));
            }
        });
        if (timer != null) {
            timer.cancel();
        }
        nextTime = 0;
        closeDialog();
        canStartTimer = false;
    }

    private static class MyHandler extends Handler {
        //防止内存溢出
        private final WeakReference<DeviceScanningActivity> mWeakActivity;

        private MyHandler(DeviceScanningActivity mWeakActivity) {
            this.mWeakActivity = new WeakReference<>(mWeakActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceScanningActivity activity = mWeakActivity.get();
            switch (msg.what) {
                case Cmd.SCANCOMPLET:
                    if (msg.arg1 == Cmd.SCANFAIL) {
                        activity.scanFail();
                    } else if (msg.arg1 == Cmd.SCANSUCCESS) {
                        Log.d(TAG, "Cmd.SCANSUCCESS");
                        activity.scanSuccess();
                    }
                    break;

                case Cmd.UPDATEDATA:
                    break;
            }
        }
    }

    /**
     * 自动重连
     * 此处用作设备登录
     */
    private void autoConnect() {
        if (TelinkLightService.Instance() != null) {

            if (TelinkLightService.Instance().getMode() != LightAdapter.MODE_AUTO_CONNECT_MESH) {


                if (this.mApplication.isEmptyMesh())
                    return;

//                Lights.getInstance().clear();
                this.mApplication.refreshLights();

                Mesh mesh = this.mApplication.getMesh();

                if (TextUtils.isEmpty(mesh.name) || TextUtils.isEmpty(mesh.password)) {
                    TelinkLightService.Instance().idleMode(true);
                    return;
                }

                //自动重连参数
                LeAutoConnectParameters connectParams = Parameters.createAutoConnectParameters();
                connectParams.setMeshName(mesh.name);
                connectParams.setPassword(mesh.password);
                connectParams.autoEnableNotification(true);
                //连接之前安装的第一个灯，因为第一个灯的信号一般会比较好。
                connectParams.setConnectMac(adapter.getItem(0).macAddress);

                // 之前是否有在做MeshOTA操作，是则继续
                if (mesh.isOtaProcessing()) {
                    connectParams.setConnectMac(mesh.otaDevice.mac);
                    saveLog("Action: AutoConnect:" + mesh.otaDevice.mac);
                } else {
                    saveLog("Action: AutoConnect:NULL");
                }
                //自动重连
                TelinkLightService.Instance().autoConnect(connectParams);
            }

            //刷新Notify参数
            LeRefreshNotifyParameters refreshNotifyParams = Parameters.createRefreshNotifyParameters();
            refreshNotifyParams.setRefreshRepeatCount(2);
            refreshNotifyParams.setRefreshInterval(2000);
            //开启自动刷新Notify
            TelinkLightService.Instance().autoRefreshNotify(refreshNotifyParams);
        }
    }

    /**
     * 检测是否还有没有分组的灯
     *
     * @return false还有没有分组的灯 true所有灯都已经分组
     */
    private boolean checkLightsHaveGroup() {
        for (int j = 0; j < nowLightList.size(); j++) {
            if (nowLightList.get(j).belongGroups == null || nowLightList.get(j).belongGroups.size() == 0) {
                btnGroupingCompleted.setBackgroundColor(getResources().getColor(R.color.gray));
                return false;
            }
        }
        btnGroupingCompleted.setBackgroundColor(getResources().getColor(R.color.theme_positive_color));
        return true;
    }

    private void startGrouping() {
        changeGroupView();
        //完成分组
        btnGroupingCompleted.setOnClickListener(v -> {
            //判定是否还有灯没有分组，如果没有允许跳转到下一个页面
            if (checkLightsHaveGroup()) {//所有灯都有分组可以跳转
                showToast("分组完成！");
                //页面跳转前进行分组数据保存
                DataCreater.updateGroup(groups);
                DataCreater.updateLights(nowLightList);

                TelinkLightService.Instance().idleMode(true);
                //目前测试调到主页
//                ActivityUtils.finishToActivity(MainActivity.class, true, true);
                Intent intent = new Intent(DeviceScanningActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                showToast("还有灯没有分组，请完成分组操作");
            }
        });

        //确定当前分组
        btnAddGroups.setText("确定分组");
        btnAddGroups.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sureGroups();
                checkLightsHaveGroup();
            }
        });
    }

    //分组页面调整
    private void changeGroupView() {
        grouping = true;
        deviceListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        btnAddGroups.setVisibility(View.VISIBLE);
        btnGroupingCompleted.setVisibility(View.VISIBLE);
        groupsBottom.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutmanager = new LinearLayoutManager(this);
        layoutmanager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewGroups.setLayoutManager(layoutmanager);
        groupsRecyclerViewAdapter = new GroupsRecyclerViewAdapter(groups, onRecyclerviewItemClickListener);
        recyclerViewGroups.setAdapter(groupsRecyclerViewAdapter);
    }

    private void sureGroups() {
        boolean hasBeSelected = false;//有无被勾选的用来分组的灯
        if (updateList != null && updateList.size() != 0) {
            hasBeSelected = true;
        }

        if (hasBeSelected) {
            //进行分组操作
            openLoadingDialog(getResources().getString(R.string.grouping_wait_tip));
            //获取当前选择的分组
            Group group = getCurrentGroup();
            //获取当前勾选灯的列表
            List<Light> selectLights = getCurrentSelectLights();
            //将灯列表的灯循环设置分组
            setGroups(group, selectLights);

        } else if (!hasBeSelected) {
            showToast("请至少选择一个灯！");
        }
    }

    private void setGroups(Group group, List<Light> selectLights) {
        if (group == null) {
            Toast.makeText(DeviceScanningActivity.this, "请至少选择一个分组", Toast.LENGTH_SHORT).show();
            return;
        }


        for (int i = 0; i < indexList.size(); i++) {
            nowLightList.get(indexList.get(i)).hasGroup = true;
            nowLightList.get(indexList.get(i)).belongGroups.add(group.name);
        }

        int index = 0;
        while (index < selectLights.size()) {
            //每个灯发5次分组的命令，确保灯能收到命令.
            for (int i = 0; i < 3; i++) {
                sendGroupData(selectLights.get(index), group, index);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            index++;
        }

        new Handler().postDelayed(()-> {

                closeDialog();

        },selectLights.size()*3*300);
    }

    private List<Light> getCurrentSelectLights() {
        ArrayList<Light> arrayList = new ArrayList<>();
        indexList.clear();
        for (int i = 0; i < nowLightList.size(); i++) {
            if (nowLightList.get(i).selected && !nowLightList.get(i).hasGroup) {
                arrayList.add(nowLightList.get(i));
                indexList.add(i);
            }
        }
        return arrayList;
    }

    private Group getCurrentGroup() {
        if (currentGroupIndex == -1) {
            return groups.get(0);
        }
        return groups.get(currentGroupIndex);
    }

    private OnRecyclerviewItemClickListener onRecyclerviewItemClickListener = new OnRecyclerviewItemClickListener() {
        @Override
        public void onItemClickListener(View v, int position) {
            currentGroupIndex = position;
            for (int i = groups.size() - 1; i >= 0; i--) {
                if (i != position && groups.get(i).checked) {
                    updateData(i, false);
                } else if (i == position && !groups.get(i).checked) {
                    updateData(i, true);
                } else if (i == position && groups.get(i).checked) {
                    updateData(i, true);
                }
            }

            groupsRecyclerViewAdapter.notifyDataSetChanged();
//            SharedPreferencesHelper.putObject(TelinkLightApplication.getInstance(),
//                    Constant.GROUPS_KEY, groups);
            SharedPreferencesHelper.putInt(TelinkLightApplication.getInstance(),
                    Constant.DEFAULT_GROUP_ID, currentGroupIndex);
        }
    };

    private void updateData(int position, boolean checkStateChange) {
        groups.get(position).checked = checkStateChange;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRxPermission = new RxPermissions(this);
        //设置屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_device_scanning);
        groups = DataCreater.getInitGroups();
//        checkPermission();
//        handleIfSupportBle();
        initView();
        initClick();
        startScan(0);
    }

    private void initClick() {
        this.backView.setOnClickListener(this.clickListener);
        this.btnScan.setOnClickListener(this.clickListener);
        this.btnLog.setOnClickListener(this.clickListener);
        deviceListView.setOnItemClickListener(this);
    }

    public void handleIfSupportBle() {
        //检查是否支持蓝牙设备
        if (!LeBluetooth.getInstance().isSupport(getApplicationContext())) {
            Toast.makeText(this, "ble not support", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        if (!LeBluetooth.getInstance().isEnabled()) {
            LeBluetooth.getInstance().enable(getApplicationContext());
        }
    }

    int PERMISSION_REQUEST_CODE = 0x10;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("douda", "checkPerm: " + checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) + "");
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    // 显示解释权限用途的界面，然后再继续请求权限
                } else {
                    // 没有权限，直接请求权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CODE);
                }
            }
        }

    }

    private void initView() {
        //监听事件
        this.mApplication = (TelinkLightApplication) this.getApplication();
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN, this);
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this);
        this.mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this);
        this.mApplication.addEventListener(MeshEvent.UPDATE_COMPLETED, this);
        this.mApplication.addEventListener(MeshEvent.ERROR, this);

        this.inflater = this.getLayoutInflater();
        this.adapter = new DeviceListAdapter();


        this.backView = (ImageView) this.findViewById(R.id.img_header_menu_left);
        groupsBottom = findViewById(R.id.groups_bottom);
        recyclerViewGroups = findViewById(R.id.recycler_view_groups);
        this.btnAddGroups = findViewById(R.id.btn_add_groups);
        this.btnGroupingCompleted = findViewById(R.id.grouping_completed);
        this.btnGroupingCompleted.setBackgroundColor(getResources().getColor(R.color.gray));
        this.btnLog = findViewById(R.id.btn_log);
        this.btnScan = (Button) this.findViewById(R.id.btn_scan);
        this.btnScan.setEnabled(false);
        this.btnScan.setBackgroundResource(R.color.gray);
        deviceListView = (GridView) this.findViewById(R.id.list_devices);
        deviceListView.setAdapter(this.adapter);
        this.updateList = new ArrayList<>();

//        groups= DataCreater.getGroups();
//        backView.setVisibility(View.GONE);
        btnScan.setVisibility(View.GONE);
        btnLog.setVisibility(View.GONE);
        btnAddGroups.setVisibility(View.GONE);
        btnGroupingCompleted.setVisibility(View.GONE);

        currentGroupIndex = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.updateList = null;
        if (timer != null)
            timer.cancel();
        canStartTimer = false;
        nextTime = 0;
        this.mApplication.removeEventListener(this);
        this.mHandler.removeCallbacksAndMessages(null);
        mDisposable.dispose();  //销毁时取消订阅.
    }

    @Override
    protected void onLocationEnable() {
//        startScan(50);
    }

    /**
     * 开始扫描
     */
    private void startScan(final int delay) {
        //添加进disposable，防止内存溢出.
        mDisposable.add(
                mRxPermission.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN).subscribe(granted -> {
                    if (granted) {
                        handleIfSupportBle();
                        TelinkLightService.Instance().idleMode(true);
                        mHandler.postDelayed(() -> {
                            if (mApplication.isEmptyMesh())
                                return;
                            Mesh mesh = mApplication.getMesh();
                            //扫描参数
                            LeScanParameters params = LeScanParameters.create();
                            params.setMeshName(mesh.factoryName);
                            params.setOutOfMeshName(Constant.OUT_OF_MESH_NAME);
                            params.setTimeoutSeconds(SCAN_TIMEOUT_SECOND);
                            params.setScanMode(true);
                            //                params.setScanMac("FF:FF:7A:68:6B:7F");
                            TelinkLightService.Instance().startScan(params);
                            openLoadingDialog(getString(R.string.loading));
                        }, delay);
                    } else {
                        // TODO: 2018/3/26 弹框提示为何需要此权限，点击确定后再次申请权限，点击取消退出.
                        AlertDialog.Builder dialog = new AlertDialog.Builder(DeviceScanningActivity.this);
                        dialog.setMessage(getResources().getString(R.string.scan_tip));
                        dialog.setPositiveButton(R.string.btn_ok, (dialog1, which) -> startScan(0));
                        dialog.setNegativeButton(R.string.btn_cancel, (dialog12, which) -> System.exit(0));
                    }
                }));

    }

    /**
     * 处理扫描事件
     *
     * @param event
     */
    private void onLeScan(final LeScanEvent event) {

        final Mesh mesh = this.mApplication.getMesh();
        final int meshAddress = mesh.getDeviceAddress();

        if (meshAddress == -1) {
            this.showToast("哎呦，网络里的灯泡太多了！目前可以有256灯");
            this.finish();
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //更新参数
                LeUpdateParameters params = Parameters.createUpdateParameters();
                params.setOldMeshName(mesh.factoryName);
                params.setOldPassword(mesh.factoryPassword);
                params.setNewMeshName(mesh.name);
                params.setNewPassword(mesh.password);

                DeviceInfo deviceInfo = event.getArgs();
                deviceInfo.meshAddress = meshAddress;
                params.setUpdateDeviceList(deviceInfo);
                TelinkLightService.Instance().updateMesh(params);
            }
        }, 200);


    }

    /**
     * 扫描不到任何设备了
     * （扫描结束）
     *
     * @param event
     */
    private void onLeScanTimeout(LeScanEvent event) {
//        this.btnScan.setEnabled(true);
        this.btnScan.setBackgroundResource(R.color.theme_positive_color);
        if (preTime != 0) {//表示目前已经搜到了至少有一个设备
            creatMessage(Cmd.SCANCOMPLET, Cmd.SCANSUCCESS);
        } else {
            creatMessage(Cmd.SCANCOMPLET, Cmd.SCANFAIL);
        }
    }

    private void creatMessage(int what, int arg) {
        Message message = new Message();
        message.what = what;
        message.arg1 = arg;
        handler.sendMessage(message);
    }

    private void onDeviceStatusChanged(DeviceEvent event) {

        DeviceInfo deviceInfo = event.getArgs();

        switch (deviceInfo.status) {
            case LightAdapter.STATUS_UPDATE_MESH_COMPLETED:
                //加灯完成继续扫描,直到扫不到设备
                com.dadoutek.uled.model.DeviceInfo deviceInfo1 = new com.dadoutek.uled.model.DeviceInfo();
                deviceInfo1.deviceName = deviceInfo.deviceName;
                deviceInfo1.firmwareRevision = deviceInfo.firmwareRevision;
                deviceInfo1.longTermKey = deviceInfo.longTermKey;
                deviceInfo1.macAddress = deviceInfo.macAddress;
                TelinkLog.d("deviceInfo-Mac:" + deviceInfo.productUUID);
                deviceInfo1.meshAddress = deviceInfo.meshAddress;
                deviceInfo1.meshUUID = deviceInfo.meshUUID;
                deviceInfo1.productUUID = deviceInfo.productUUID;
                deviceInfo1.status = deviceInfo.status;
                deviceInfo1.meshName = deviceInfo.meshName;
                this.mApplication.getMesh().devices.add(deviceInfo1);
                this.mApplication.getMesh().saveOrUpdate(this);
                int meshAddress = deviceInfo.meshAddress & 0xFF;
                Light light = this.adapter.get(meshAddress);

                if (light == null) {
                    light = new Light();
                    light.name = deviceInfo.meshName;
                    light.meshAddress = meshAddress;
                    light.textColor = this.getResources().getColorStateList(
                            R.color.black);
                    light.selected = false;
                    light.raw = deviceInfo;
                    this.adapter.add(light);
                    this.adapter.notifyDataSetChanged();
                }

                if (canStartTimer) {
//                    startTimer();
                    canStartTimer = false;
                }

                preTime = TimeUtil.getNowSeconds();
//                this.startScan(1000);
                this.startScan(200);
                break;
            case LightAdapter.STATUS_UPDATE_MESH_FAILURE:
                //加灯失败继续扫描
                this.startScan(200);
                break;

            case LightAdapter.STATUS_ERROR_N:
                this.onNError(event);
                break;
            case LightAdapter.STATUS_LOGIN:
                isLoginSuccess = true;
//                btnAddGroups.doneLoadingAnimation(R.color.black,
//                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp));
                break;
            case LightAdapter.STATUS_LOGOUT:
                isLoginSuccess = false;
                break;
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(task, 1000, 1000);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            nextTime = TimeUtil.getNowSeconds();
            Log.d("DeviceScanning", "timer: " + "nextTime=" + nextTime + ";preTime=" + preTime);
            if (preTime > 0 && nextTime - preTime >= SCAN_TIMEOUT_SECOND) {
                creatMessage(Cmd.SCANCOMPLET, Cmd.SCANSUCCESS);
            }
        }
    };

    private void onNError(final DeviceEvent event) {

        TelinkLightService.Instance().idleMode(true);
        TelinkLog.d("DeviceScanningActivity#onNError");

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceScanningActivity.this);
        builder.setMessage("当前环境:Android7.0!加灯时连接重试: 3次失败!");
        builder.setNegativeButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();

    }

    private void onMeshEvent(MeshEvent event) {
        new AlertDialog.Builder(this).setMessage("重启蓝牙,更好地体验智能灯!").show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Light light = this.adapter.getItem(position);
        light.selected = !light.selected;
        DeviceItemHolder holder = (DeviceItemHolder) view.getTag();
        holder.selected.setChecked(light.selected);

        if (light.selected) {
            this.updateList.add(light.raw);
            nowLightList.get(position).selected = true;
//            getDeviceGroup(light);
            checkSelectLamp(light);
        } else {
            nowLightList.get(position).selected = false;
            this.updateList.remove(light.raw);
        }
    }

    private void getDeviceGroup(Light light) {
        byte opcode = (byte) 0xDD;
        int dstAddress = light.meshAddress;
        byte[] params = new byte[]{0x08, 0x01};

        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddress, params);
        TelinkLightService.Instance().updateNotification();
    }

    private void checkSelectLamp(Light light) {

        Group group = groups.get(groups.size() - 1);
        Log.d("ScanGroup", "checkSelectLamp: " + groups.size());

        int groupAddress = group.meshAddress;
        int dstAddress = light.meshAddress;
        byte opcode = (byte) 0xD7;
        byte[] params = new byte[]{0x01, (byte) (groupAddress & 0xFF),
                (byte) (groupAddress >> 8 & 0xFF)};

        Log.d("Scanner", "checkSelectLamp: " + "opcode:" + opcode + ";  dstAddress:" +
                dstAddress + ";  params:" + params.toString() + "------>" + light.status);
        if (!group.checked) {
            params[0] = 0x01;
            TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddress, params);

        } else {
            params[0] = 0x00;
            TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddress, params);
        }
    }

    private void sendGroupData(Light light, Group group, int index) {
        int groupAddress = group.meshAddress;
        dstAddress = light.meshAddress;
        byte opcode = (byte) 0xD7;          //0xD7 代表添加组的指令
        byte[] params = new byte[]{0x01, (byte) (groupAddress & 0xFF),      //0x01 代表添加组
                (byte) (groupAddress >> 8 & 0xFF)};

//        Log.d("Scanner", "checkSelectLamp: " + "opcode:" + opcode + ";  dstAddress:" + dstAddress + ";  params:" + params.toString());
//        Log.d("groupingCC", "sendGroupData: "+"----dstAddress:"+dstAddress+";  group:name=="+group.name+";  group:name=="+group.meshAddress+";  lighthas"+light.hasGroup);

        if (group.checked) {
            params[0] = 0x01;
            TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddress, params);

        }
//        else {
//            params[0] = 0x00;
//            TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddress, params);
//        }

        //已分组灯不显示
//        nowLightList.get(index).hasGroup=true;
//        Log.d("groupingCC", "sendGroupData: " + "----dstAddress:" + dstAddress + ";  group:name==" + group.name + "; " +
//                " group:name==" + group.meshAddress + ";  lighthas" + light.hasGroup);
        //灯和分组互相绑定
//            Lights.getInstance().get(index).belongGroups.add(group.name);

        if (group.containsLightList == null) {
            group.containsLightList = new ArrayList<>();
        }
        group.containsLightList.add(light.meshAddress);
        adapter.notifyDataSetChanged();
    }

    /**
     * 事件处理方法
     *
     * @param event
     */
    @Override
    public void performed(Event<String> event) {

        switch (event.getType()) {
            case LeScanEvent.LE_SCAN:
                this.onLeScan((LeScanEvent) event);
                break;
            case LeScanEvent.LE_SCAN_TIMEOUT:
                this.onLeScanTimeout((LeScanEvent) event);
                break;
            case DeviceEvent.STATUS_CHANGED:
                this.onDeviceStatusChanged((DeviceEvent) event);
                break;
            case NotificationEvent.GET_GROUP:
                this.onGetGroupEvent((NotificationEvent) event);
                break;
            case MeshEvent.ERROR:
                this.onMeshEvent((MeshEvent) event);
                break;
        }
    }

    private void onGetGroupEvent(NotificationEvent event) {
        if (event.getType() == NotificationEvent.GET_GROUP) {
            NotificationEvent e = (NotificationEvent) event;
            NotificationInfo info = e.getArgs();

            int srcAddress = info.src & 0xFF;
            byte[] params = info.params;

            if (srcAddress != this.dstAddress)
                return;

            int count = groups.size();

            Group group;

            for (int i = 0; i < count; i++) {
                group = this.groups.get(currentGroupIndex);

                if (group != null) {
                }
//                    group.checked = false;
            }

            int groupAddress;
            int len = params.length;

            for (int j = 0; j < len; j++) {

                groupAddress = params[j];

                if (groupAddress == 0x00 || groupAddress == 0xFF)
                    break;

                groupAddress = groupAddress | 0x8000;

                group = this.groupsRecyclerViewAdapter.get(groupAddress);

                if (group != null) {
//                    group.checked = true;
                }
            }

//            mHandler.obtainMessage(UPDATE).sendToTarget();
        }
    }

    private static class DeviceItemHolder {
        public ImageView icon;
        public TextView txtName;
        public CheckBox selected;
    }

    final class DeviceListAdapter extends BaseAdapter {

        private List<Light> lights;

        public DeviceListAdapter() {

        }

        @Override
        public int getCount() {
            return this.lights == null ? 0 : this.lights.size();
        }

        @Override
        public Light getItem(int position) {
            return this.lights.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DeviceItemHolder holder;

            if (convertView == null) {

                convertView = inflater.inflate(R.layout.device_item, null);
                ImageView icon = (ImageView) convertView
                        .findViewById(R.id.img_icon);
                TextView txtName = (TextView) convertView
                        .findViewById(R.id.txt_name);
                CheckBox selected = (CheckBox) convertView.findViewById(R.id.selected);

                holder = new DeviceItemHolder();


                holder.icon = icon;
                holder.txtName = txtName;
                holder.selected = selected;

                convertView.setTag(holder);
            } else {
                holder = (DeviceItemHolder) convertView.getTag();
            }

            Light light = this.getItem(position);

            holder.txtName.setText(light.name);
            holder.icon.setImageResource(R.drawable.icon_light_on);
            holder.selected.setChecked(light.selected);

            if (light.hasGroup) {
                holder.txtName.setVisibility(View.GONE);
                holder.icon.setVisibility(View.GONE);
                holder.selected.setVisibility(View.GONE);
            } else {
                holder.txtName.setVisibility(View.VISIBLE);
                holder.icon.setVisibility(View.VISIBLE);
                if (grouping) {
                    holder.selected.setVisibility(View.VISIBLE);
                } else {
                    holder.selected.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        public void add(Light light) {

            if (this.lights == null)
                this.lights = new ArrayList<>();

            this.lights.add(light);
        }

        public Light get(int meshAddress) {

            if (this.lights == null)
                return null;

            for (Light light : this.lights) {
                if (light.meshAddress == meshAddress) {
                    return light;
                }
            }

            return null;
        }

        public List<Light> getLights() {
            return lights;
        }
    }

    public void openLoadingDialog(String content) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialogview, null);

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
        TextView tvContent = (TextView) v.findViewById(R.id.tvContent);
        tvContent.setText(content);

        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);

        @SuppressLint("ResourceType") Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this,
                R.animator.load_animation);

        spaceshipImage.startAnimation(hyperspaceJumpAnimation);

        if (loadDialog == null) {
            loadDialog = new Dialog(this,
                    R.style.FullHeightDialog);
        }
        //loadDialog没显示才把它显示出来
        if (!loadDialog.isShowing()) {
            loadDialog.setCancelable(true);
            loadDialog.setCanceledOnTouchOutside(false);
            loadDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            loadDialog.show();
        }
    }

    public void closeDialog() {
        if (loadDialog != null) {
            loadDialog.dismiss();
        }
    }
}
