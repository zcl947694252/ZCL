package com.dadoutek.uled.rgb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dadoutek.uled.R;
import com.dadoutek.uled.communicate.Commander;
import com.dadoutek.uled.group.GroupsRecyclerViewAdapter;
import com.dadoutek.uled.intf.OnRecyclerviewItemClickListener;
import com.dadoutek.uled.intf.OnRecyclerviewItemLongClickListener;
import com.dadoutek.uled.intf.SyncCallback;
import com.dadoutek.uled.model.Constant;
import com.dadoutek.uled.model.DadouDeviceInfo;
import com.dadoutek.uled.model.dbModel.DBUtils;
import com.dadoutek.uled.model.dbModel.DbGroup;
import com.dadoutek.uled.model.dbModel.DbLight;
import com.dadoutek.uled.model.dbModel.DbUser;
import com.dadoutek.uled.model.DeviceType;
import com.dadoutek.uled.model.ItemTypeGroup;
import com.dadoutek.uled.model.Opcode;
import com.dadoutek.uled.model.SharedPreferencesHelper;
import com.dadoutek.uled.network.NetworkFactory;
import com.dadoutek.uled.othersview.SplashActivity;
import com.dadoutek.uled.tellink.TelinkLightApplication;
import com.dadoutek.uled.tellink.TelinkLightService;
import com.dadoutek.uled.tellink.TelinkMeshErrorDealActivity;
import com.dadoutek.uled.util.NetWorkUtils;
import com.dadoutek.uled.util.OtherUtils;
import com.dadoutek.uled.util.StringUtils;
import com.dadoutek.uled.util.SyncDataPutOrGetUtils;
import com.telink.TelinkApplication;
import com.telink.bluetooth.LeBluetooth;
import com.telink.bluetooth.TelinkLog;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.ErrorReportEvent;
import com.telink.bluetooth.event.LeScanEvent;
import com.telink.bluetooth.event.MeshEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.light.DeviceInfo;
import com.telink.bluetooth.light.LeAutoConnectParameters;
import com.telink.bluetooth.light.LeRefreshNotifyParameters;
import com.telink.bluetooth.light.LightAdapter;
import com.telink.bluetooth.light.Parameters;
import com.telink.util.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * 创建者     zcl
 * 创建时间   2019/8/30 17:05
 * 描述	      ${批量分组界面}$
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${
 */
public class RgbBatchGroupActivity extends TelinkMeshErrorDealActivity
        implements AdapterView.OnItemClickListener, EventListener<String>,
        Toolbar.OnMenuItemClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbarTv)
    TextView toolbarTv;
    @BindView(R.id.recycler_view_groups)
    RecyclerView recyclerViewGroups;
    @BindView(R.id.add_group_layout)
    LinearLayout addGroupLayout;
    @BindView(R.id.groups_bottom)
    LinearLayout groupsBottom;
    @BindView(R.id.tv_num_lights)
    TextView tvNumLights;
    @BindView(R.id.light_num_layout)
    LinearLayout lightNumLayout;
    @BindView(R.id.list_devices)
    GridView listDevices;
    @BindView(R.id.btn_log)
    Button btnLog;
    @BindView(R.id.btn_scan)
    Button btnScan;
    @BindView(R.id.btn_add_groups)
    Button btnAddGroups;
    @BindView(R.id.grouping_completed)
    Button groupingCompleted;
    @BindView(R.id.topView)
    ConstraintLayout topView;
    @BindView(R.id.scanPb)
    MaterialProgressBar scanPb;
    @BindView(R.id.add_group_relativeLayout)
    RelativeLayout add_relativeLayout;
    @BindView(R.id.add_group)
    RelativeLayout add_group;

    private static final int MAX_RETRY_COUNT = 4;   //update mesh failed的重试次数设置为4次
    private TelinkLightApplication mApplication;
    //防止内存泄漏
    CompositeDisposable mDisposable = new CompositeDisposable();
    //分组所含灯的缓存
    private List<DbLight> nowLightList;
    private LayoutInflater inflater;
    private boolean grouping;
    private DeviceListAdapter adapter;
    boolean isFirtst = true;
    //标记登录状态
    private boolean isLoginSuccess = false;
    private GridView deviceListView;
    private GroupsRecyclerViewAdapter groupsRecyclerViewAdapter;
    private List<DbGroup> groups;
    private Disposable mTimer;
    private int mRetryCount = 0;
    private String lightType;
    private String groupLight;
    //当前所选组index
    private int currentGroupIndex = -1;
    private List<DbLight> updateList;
    private ArrayList<Integer> indexList = new ArrayList<>();

    //对一个灯重复分组时记录上一次分组
    private int originalGroupID = -1;
    private Disposable mGroupingDisposable;
    private TextView tvStopScan;
    //灯的mesh地址
    private int dstAddress;
    private Disposable mConnectTimer;
    private SparseArray<Disposable> mBlinkDisposables = new SparseArray<>();
    private boolean isSelectAll = false;
    private boolean scanCURTAIN = false;

    private boolean initHasGroup = false;
    private boolean guideShowCurrentPage = false;
    private boolean isGuide = false;
    private LinearLayoutManager layoutmanager;
    private long allLightId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_batch_group);
        ButterKnife.bind(this);
        initData();
        initView();
        initClick();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DbLight light = this.adapter.getItem(position);
        light.selected = !light.selected;
        DeviceItemHolder holder = (DeviceItemHolder) view.getTag();
        holder.selected.setImageResource(light.selected ? R.drawable.icon_checkbox_selected :
                R.drawable.icon_checkbox_unselected);

        if (light.selected) {
            this.updateList.add(light);
            nowLightList.get(position).selected = true;

            btnAddGroups.setText(R.string.sure_group);
            if (hasGroup()) {
                startBlink(light);
            } else {
                ToastUtils.showLong(R.string.tip_add_group);
            }
        } else {
            nowLightList.get(position).selected = false;
            this.updateList.remove(light);
            stopBlink(light);
            if ((!isSelectLight()) && isAllLightsGrouped()) {
                btnAddGroups.setText(R.string.complete);
            }
        }
    }

    private void isSelectAll() {
        if (isSelectAll) {
            for (int j = 0; j < nowLightList.size(); j++) {
                this.updateList.add(nowLightList.get(j));
                nowLightList.get(j).selected = true;

                btnAddGroups.setText(R.string.sure_group);

                if (hasGroup()) {
                    startBlink(nowLightList.get(j));
                } else {
                    ToastUtils.showLong(R.string.tip_add_group);
                }
            }

            this.adapter.notifyDataSetChanged();
        } else {
            for (int j = 0; j < nowLightList.size(); j++) {
                this.updateList.remove(nowLightList.get(j));
                nowLightList.get(j).selected = false;
                stopBlink(nowLightList.get(j));
                if ((!isSelectLight()) && isAllLightsGrouped()) {
                    btnAddGroups.setText(R.string.complete);
                }
            }

            this.adapter.notifyDataSetChanged();
        }
    }

    private boolean hasGroup() {
        if (groups.size() == -1) {
            groups = new ArrayList<>();
            return false;
        } else {
            return true;
        }

    }

    /**
     * 让灯开始闪烁
     */
    private void startBlink(DbLight light) {
        //        int dstAddress = light.getMeshAddr();
        DbGroup group;
        DbGroup groupOfTheLight = DBUtils.INSTANCE.getGroupByID(light.getBelongGroupId());
        if (groupOfTheLight == null)
            group = groups.get(0);
        else
            group = groupOfTheLight;
        int groupAddress = group.getMeshAddr();
        Log.d("Saw", "startBlink groupAddresss = " + groupAddress);
        int dstAddress = light.getMeshAddr();
        byte opcode = (byte) Opcode.SET_GROUP;
        byte[] params = new byte[]{0x01, (byte) (groupAddress & 0xFF),
                (byte) (groupAddress >> 8 & 0xFF)};
        params[0] = 0x01;

        if (mBlinkDisposables.get(dstAddress) != null) {
            mBlinkDisposables.get(dstAddress).dispose();
        }

        //每隔1s发一次，就是为了让灯一直闪.
        mBlinkDisposables.put(dstAddress, Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    TelinkLightService instance = TelinkLightService.Instance();
                    if (instance != null)
                        instance.sendCommandNoResponse(opcode, dstAddress, params);
                }));
    }

    private void stopBlink(DbLight light) {
        Disposable disposable = mBlinkDisposables.get(light.getMeshAddr());
        disposable.dispose();
    }

    //扫描失败处理方法
    private void scanFail() {
        showToast(getString(R.string.scan_end));
        doFinish();
    }


    private void stopTimer() {
        if (mTimer != null && !mTimer.isDisposed()) {
            mTimer.dispose();
        }
    }


    private Disposable createConnectTimeout() {
        return Observable.timer(60, TimeUnit.SECONDS)
                 .subscribeOn(Schedulers.io())
                                 .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    hideLoadingDialog();
                    mConnectTimer = null;
                });
    }

    //处理扫描成功后
    @SuppressLint("StringFormatInvalid")
    private void scanSuccess() {
        //更新Title
        tvStopScan.setVisibility(View.GONE);
        toolbar.setTitle(getString(R.string.title_scanned_device_num,adapter.getCount()));

        //存储当前添加的灯。
        //2018-4-19-hejiajun 添加灯调整位置，防止此时点击灯造成下标越界
        if (nowLightList != null && nowLightList.size() > 0) {
            nowLightList.clear();
        }
        if (nowLightList != null)
            nowLightList.addAll(adapter.getLights());

        scanPb.setVisibility(View.GONE);

        //先连接灯。
        autoConnect();
        //倒计时10s，出问题了就超时。
        mConnectTimer = createConnectTimeout();

        btnAddGroups.setVisibility(View.VISIBLE);
        btnAddGroups.setText(R.string.start_group_bt);

        btnAddGroups.setOnClickListener(v -> {
            if (isLoginSuccess) {
                //进入分组
                startGrouping();
            } else if (mConnectTimer == null) {
                if (Constant.IS_ROUTE_MODE) return;
                autoConnect();
                mConnectTimer = createConnectTimeout();
            } else {    //正在连接中
                if (Constant.IS_ROUTE_MODE) return;
                showLoadingDialog(getResources().getString(R.string.connecting_tip));

            }
        });

    }

    private void doFinish() {
        if (updateList != null && updateList.size() > 0) {
            checkNetworkAndSync();
        }
        this.mApplication.removeEventListener(this);
        this.updateList = null;
        mDisposable.dispose();  //销毁时取消订阅.
        if (mTimer != null)
            mTimer.dispose();
        if (mGroupingDisposable != null)
            mGroupingDisposable.dispose();
        if (mConnectTimer != null)
            mConnectTimer.dispose();

        for (int i = 0; i < mBlinkDisposables.size(); i++) {
            Disposable disposable = mBlinkDisposables.get(i);
            if (disposable != null)
                disposable.dispose();
        }

        for (int i = 0; i < nowLightList.size(); i++) {
            if (nowLightList.get(i).selected) {
                nowLightList.get(i).selected = false;
            }
        }


        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnectTimer != null)
            mConnectTimer.dispose();
    }

    /**
     * 开始分组
     */
    private void startGrouping() {
        LeBluetooth.getInstance().stopScan();

        //初始化分组页面
        changeGroupView();

        //完成分组跳转
        changOtherView();

        //确定当前分组
        sureGroupingEvent();
    }

    private void sureGroupingEvent() {
        btnAddGroups.setText(R.string.sure_group);
        btnAddGroups.setOnClickListener(v -> {
            if (isAllLightsGrouped() && !isSelectLight()) {
                doFinish();
            } else {
                sureGroups();
            }
        });
    }

    private void changOtherView() {
        groupingCompleted.setOnClickListener(v -> {
            //判定是否还有灯没有分组，如果没有允许跳转到下一个页面
            if (isAllLightsGrouped()) {//所有灯都有分组可以跳转
                showToast(getString(R.string.grouping_completed));
                //页面跳转前进行分组数据保存

                TelinkLightService instance = TelinkLightService.Instance();
                if (instance != null)
                    instance.idleMode(true);
                //目前测试调到主页
                doFinish();
            } else {
                showToast(getString(R.string.have_lamp_no_group_tip));
            }
        });
    }

    /**
     * 有无被选中的用来分组的灯
     *
     * @return true: 选中了       false:没选中
     */
    private boolean isSelectLight() {
        return getCurrentSelectLights().size() > 0;

    }

    private void sureGroups() {
        if (isSelectLight()) {
            //进行分组操作
            //获取当前选择的分组
            DbGroup group = getCurrentGroup();
            if (group != null) {
                if (group.getMeshAddr() == 0xffff) {
                    ToastUtils.showLong(R.string.tip_add_gp);
                    return;
                }
                //获取当前勾选灯的列表
                List<DbLight> selectLights = getCurrentSelectLights();

                showLoadingDialog(getResources().getString(R.string.grouping_wait_tip,
                        selectLights.size() + ""));
                //将灯列表的灯循环设置分组
                setGroups(group, selectLights);
            }

        } else {
            showToast(getString(R.string.selected_lamp_tip));
        }
    }


    private void setGroupOneByOne(DbGroup dbGroup, List<DbLight> selectLights, int index) {
        DbLight dbLight = selectLights.get(index);
        int lightMeshAddr = dbLight.getMeshAddr();
        Commander.INSTANCE.addGroup(lightMeshAddr, dbGroup.getMeshAddr(), () -> {
            dbLight.setBelongGroupId(dbGroup.getId());
            updateGroupResult(dbLight, dbGroup);
            if (index + 1 > selectLights.size() - 1)
                completeGroup(selectLights);
            else
                setGroupOneByOne(dbGroup, selectLights, index + 1);
            return null;
        }, () -> {
            dbLight.setBelongGroupId(allLightId);
            ToastUtils.showLong(R.string.group_fail_tip);
            updateGroupResult(dbLight, dbGroup);
            if (TelinkLightApplication.Companion.getApp().getConnectDevice() == null) {
                ToastUtils.showLong( getString(R.string.device_disconnected));
            } else {
                if (index + 1 > selectLights.size() - 1)
                    completeGroup(selectLights);
                else
                    setGroupOneByOne(dbGroup, selectLights, index + 1);
            }
            return null;
        });

    }

    private void completeGroup(List<DbLight> selectLights) {
        //取消分组成功的勾选的灯
        for (int i = 0; i < selectLights.size(); i++) {
            DbLight light = selectLights.get(i);
            light.selected = false;
        }
        adapter.notifyDataSetChanged();
        hideLoadingDialog();
        if (isAllLightsGrouped()) {
            btnAddGroups.setText(R.string.complete);
        }
    }

    private void setGroups(DbGroup group, List<DbLight> selectLights) {
        if (group == null) {
            Toast.makeText(mApplication, R.string.select_group_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSelectAll) {
            toolbar.getMenu().findItem(R.id.menu_select_all).setTitle(getString(R.string.select_all));
            isSelectAll = false;
        }

        for (int i = 0; i < selectLights.size(); i++) {
            //让选中的灯停下来别再发闪的命令了。
            stopBlink(selectLights.get(i));
        }

        setGroupOneByOne(group, selectLights, 0);
    }

    private void updateGroupResult(DbLight light, DbGroup group) {
        for (int i = 0; i < nowLightList.size(); i++) {
            if (light.getMeshAddr() == nowLightList.get(i).getMeshAddr()) {
                if (light.getBelongGroupId() != allLightId) {
                    nowLightList.get(i).hasGroup = true;
                    nowLightList.get(i).setBelongGroupId(group.getId());
                    nowLightList.get(i).setName(light.getName());
                    DBUtils.INSTANCE.updateLight(light);
                } else {
                    nowLightList.get(i).hasGroup = false;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        //        super.onBackPressed();
        if (grouping) {
            for (int i = 0; i < getCurrentSelectLights().size(); i++) {
                //让选中的灯停下来别再发闪的命令了。
                stopBlink(getCurrentSelectLights().get(i));
            }
            doFinish();
        } else {
            new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        doFinish();
                    })
                    .setNegativeButton(R.string.btn_cancel, ((dialog, which) -> {
                    }))
                    .setMessage(R.string.exit_tips_in_scanning)
                    .show();
        }
    }

    private List<DbLight> getCurrentSelectLights() {
        ArrayList<DbLight> arrayList = new ArrayList<>();
        indexList.clear();
        for (int i = 0; i < nowLightList.size(); i++) {
            if (nowLightList.get(i).selected && !nowLightList.get(i).hasGroup) {
                arrayList.add(nowLightList.get(i));
                indexList.add(i);
            } else if (nowLightList.get(i).selected && nowLightList.get(i).hasGroup) {
                originalGroupID =
                        Integer.parseInt(String.valueOf(nowLightList.get(i).getBelongGroupId()));
                //如果所选灯已有分组，清空后再继续添加到新的分组
                //                nowLightList.get(i).belongGroups.clear();
                arrayList.add(nowLightList.get(i));
                indexList.add(i);
            }
        }
        return arrayList;
    }

    private DbGroup getCurrentGroup() {
        if (currentGroupIndex == -1) {
            if (groups.size() > 1) {
                Toast.makeText(this, R.string.please_select_group, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.tip_add_gp, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        return groups.get(currentGroupIndex);
    }

    /**
     * 是否所有灯都分了组
     *
     * @return false还有没有分组的灯 true所有灯都已经分组
     */
    private boolean isAllLightsGrouped() {
        for (int j = 0; j < nowLightList.size(); j++) {
            if (nowLightList.get(j).getBelongGroupId() == allLightId) {
                return false;
            }
        }
        return true;
    }

    //分组页面调整
    private void changeGroupView() {
        grouping = true;
        toolbar.inflateMenu(R.menu.menu_grouping_select_all);
        toolbar.setOnMenuItemClickListener(this);
        deviceListView.setOnItemClickListener(this);
        deviceListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        btnAddGroups.setVisibility(View.VISIBLE);
        groupsBottom.setVisibility(View.VISIBLE);
        layoutmanager = new LinearLayoutManager(this);
        layoutmanager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewGroups.setLayoutManager(layoutmanager);

        if (lightType.equals("cw_light")) {
            add_relativeLayout.setVisibility(View.GONE);
            add_group.setVisibility(View.GONE);
            groupsBottom.setVisibility(View.GONE);
        } else {
            groupsBottom.setVisibility(View.VISIBLE);
            if (groups.size() > 0) {
                groupsRecyclerViewAdapter = new GroupsRecyclerViewAdapter(groups,
                        onRecyclerviewItemClickListener, onRecyclerviewItemLongClickListener);
                recyclerViewGroups.setAdapter(groupsRecyclerViewAdapter);
                add_relativeLayout.setVisibility(View.GONE);
                add_group.setVisibility(View.VISIBLE);
            } else {
                add_relativeLayout.setVisibility(View.VISIBLE);
                add_group.setVisibility(View.GONE);
            }
        }

        groupsRecyclerViewAdapter = new GroupsRecyclerViewAdapter(groups,
                onRecyclerviewItemClickListener, onRecyclerviewItemLongClickListener);
        recyclerViewGroups.setAdapter(groupsRecyclerViewAdapter);
        disableEventListenerInGrouping();

        initOnLayoutListener();
    }

    OnRecyclerviewItemLongClickListener onRecyclerviewItemLongClickListener = (v, position) -> {
        showGroupForUpdateNameDialog(position);
    };

    private void showGroupForUpdateNameDialog(int position) {
        EditText textGp = new EditText(this);
        StringUtils.initEditTextFilter(textGp);
        textGp.setText(groups.get(position).getName());
        //        //设置光标默认在最后
        textGp.setSelection(textGp.getText().toString().length());
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.update_group))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textGp)
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    if (StringUtils.compileExChar(textGp.getText().toString().trim())) {
                        ToastUtils.showLong(getString(R.string.rename_tip_check));
                    } else {
                        groups.get(position).setName(textGp.getText().toString().trim());
                        DBUtils.INSTANCE.updateGroup(groups.get(position));
                        groupsRecyclerViewAdapter.notifyItemChanged(position);
                        adapter.notifyDataSetChanged();
                        //                                DBUtils.INSTANCE.getLightByGroupMesh
                        //                                (groups.get(position).getMeshAddr());
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_all:
                if (isSelectAll) {
                    isSelectAll = false;
                    item.setTitle(R.string.select_all);
                } else {
                    isSelectAll = true;
                    item.setTitle(R.string.cancel);
                }
                isSelectAll();
                break;
        }
        return false;
    }

    private void disableEventListenerInGrouping() {
        this.mApplication.removeEventListener(LeScanEvent.LE_SCAN, this);
        this.mApplication.removeEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this);
    }

    @OnClick(R.id.add_group_layout)
    public void onViewClicked() {
        isGuide = false;
        // addNewGroup();
        popMain.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void addNewGroup() {
        final EditText textGp = new EditText(this);
        textGp.setText(DBUtils.INSTANCE.getDefaultNewGroupName());
        StringUtils.initEditTextFilter(textGp);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_new_group);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(textGp);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            // 获取输入框的内容
            if (StringUtils.compileExChar(textGp.getText().toString().trim())) {
                ToastUtils.showLong(getString(R.string.rename_tip_check));
            } else {
                //往DB里添加组数据
                DBUtils.INSTANCE.addNewGroupWithType(textGp.getText().toString().trim(),
                        Constant.DEVICE_TYPE_LIGHT_RGB);
                refreshView();
                dialog.dismiss();
                InputMethodManager imm =
                        (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        if (!isGuide) {
            builder.setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> {
                dialog.dismiss();
            });
        }
        textGp.setFocusable(true);
        textGp.setFocusableInTouchMode(true);
        textGp.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager) textGp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(textGp, 0);
            }
        }, 200);
        builder.show();
    }

    private void refreshView() {
        currentGroupIndex = groups.size() - 1;
        for (int i = groups.size() - 1; i >= 0; i--) {
            if (i == groups.size() - 1) {
                groups.get(i).checked = true;
            } else {
                groups.get(i).checked = false;
            }
        }

        groupsRecyclerViewAdapter = new GroupsRecyclerViewAdapter(groups,
                onRecyclerviewItemClickListener, onRecyclerviewItemLongClickListener);
        recyclerViewGroups.setAdapter(groupsRecyclerViewAdapter);
        add_relativeLayout.setVisibility(View.GONE);
        add_group.setVisibility(View.VISIBLE);
        recyclerViewGroups.smoothScrollToPosition(groups.size() - 1);
        groupsRecyclerViewAdapter.notifyDataSetChanged();
        SharedPreferencesHelper.putInt(TelinkLightApplication.Companion.getApp(),
                Constant.DEFAULT_GROUP_ID, currentGroupIndex);
    }

    private OnRecyclerviewItemClickListener onRecyclerviewItemClickListener =
            new OnRecyclerviewItemClickListener() {
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
                    SharedPreferencesHelper.putInt(TelinkLightApplication.Companion.getApp(),
                            Constant.DEFAULT_GROUP_ID, currentGroupIndex);
                }
            };

    private void updateData(int position, boolean checkStateChange) {
        groups.get(position).checked = checkStateChange;
    }

    private boolean startConnect = false;

    /**
     * 自动重连
     * 此处用作设备登录
     */
    private void autoConnect() {
        if (TelinkLightService.Instance() != null) {
            if (TelinkLightService.Instance().getMode() != LightAdapter.MODE_AUTO_CONNECT_MESH) {
                showLoadingDialog(getResources().getString(R.string.connecting_tip));
                //                LeBluetooth.getApp().stopScan();
                TelinkLightService instance = TelinkLightService.Instance();
                if (instance != null)
                    instance.idleMode(true);

                startConnect = true;


                //自动重连参数DBUtils.lastUser?.controlMeshName
                LeAutoConnectParameters connectParams = Parameters.createAutoConnectParameters();
                DbUser lastUser = DBUtils.INSTANCE.getLastUser();
                if (lastUser == null)
                    return;
                connectParams.setMeshName(lastUser.getControlMeshName());
                connectParams.setConnectMac(bestRssiDevice.macAddress);
                connectParams.setPassword(NetworkFactory.md5(
                        NetworkFactory.md5(lastUser.getControlMeshName()) + lastUser.getControlMeshName())
                        .substring(0, 16));
                connectParams.autoEnableNotification(true);

                //连接，如断开会自动重连
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                        if (instance != null)
                            instance.autoConnect(connectParams);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                //                connectDevice(bestRssiDevice.macAddress);
            }

            //刷新Notify参数
            LeRefreshNotifyParameters refreshNotifyParams =
                    Parameters.createRefreshNotifyParameters();
            refreshNotifyParams.setRefreshRepeatCount(1);
            refreshNotifyParams.setRefreshInterval(2000);
            //开启自动刷新Notify
            TelinkLightService instance = TelinkLightService.Instance();
            if (instance != null)
                instance.autoRefreshNotify(refreshNotifyParams);
        }
    }

    private static final int TIME_OUT_CONNECT = 15;

    public void connectDevice(String mac) {
        TelinkLightService instance = TelinkLightService.Instance();
        if (instance != null)
            instance.connect(mac, TIME_OUT_CONNECT);
    }

    private void initClick() {
        this.btnScan.setOnClickListener(this.clickListener);
        this.btnLog.setOnClickListener(this.clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == btnScan) {
                doFinish();
                //stopScanAndUpdateMesh();
            } else if (v.getId() == R.id.btn_log) {
                //                startActivity(new Intent(RgbBatchGroupActivity.this,
                //                LogInfoActivity.class));
            }
        }
    };

    private void initView() {
        initToolbar();
        //        tvStopScan.setVisibility(View.GONE);
        scanPb.setVisibility(View.GONE);
        //监听事件
        mApplication.removeEventListener(this);
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN, this);
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this);
        this.mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this);
        this.mApplication.addEventListener(MeshEvent.UPDATE_COMPLETED, this);
        this.mApplication.addEventListener(ErrorReportEvent.ERROR_REPORT, this);
        this.mApplication.addEventListener(NotificationEvent.GET_GROUP, this);
        this.inflater = this.getLayoutInflater();
        List<DbLight> list = DBUtils.INSTANCE.getAllRGBLight();
        List<DbLight> no_list = new ArrayList<>();
        List<DbLight> group_list = new ArrayList<>();
        List<DbLight> all_light = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.getLightGroupName(list.get(i)).equals(TelinkLightApplication.Companion.getApp().getString(R.string.not_grouped))) {
                no_list.add(list.get(i));
            } else {
                group_list.add(list.get(i));
            }
        }

        if (no_list.size() > 0) {
            for (int i = 0; i < no_list.size(); i++) {
                all_light.add(no_list.get(i));
            }
        }

        if (group_list.size() > 0) {
            for (int i = 0; i < group_list.size(); i++) {
                all_light.add(group_list.get(i));
            }
        }
        this.adapter = new DeviceListAdapter(all_light, this);
        nowLightList.addAll(all_light);


        groupsBottom = findViewById(R.id.groups_bottom);
        recyclerViewGroups = findViewById(R.id.recycler_view_groups);
        this.btnAddGroups = findViewById(R.id.btn_add_groups);
        this.groupingCompleted = findViewById(R.id.grouping_completed);
        this.add_group = (RelativeLayout) this.findViewById(R.id.add_group);
        this.add_relativeLayout = (RelativeLayout) this.findViewById(R.id.add_group_relativeLayout);
        this.groupingCompleted.setBackgroundColor(getResources().getColor(R.color.gray));
        this.btnLog = findViewById(R.id.btn_log);
        this.btnScan = (Button) this.findViewById(R.id.btn_scan);
        this.btnScan.setEnabled(false);
        this.btnScan.setBackgroundResource(R.color.gray);
        deviceListView = this.findViewById(R.id.list_devices);
        deviceListView.setAdapter(this.adapter);
        this.updateList = new ArrayList<>();

        add_relativeLayout.setOnClickListener(v -> {
            // addNewGroup();
            popMain.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        });

        startGrouping();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
        TelinkApplication.getInstance().removeEventListener(this);
    }

    private void initToolbar() {
        //        toolbar.setTitle(R.string.batch_group);
        //        setSupportActionBar(toolbar);
        //        ActionBar actionBar = getSupportActionBar();
        //        if (actionBar != null)
        //            actionBar.setDisplayHomeAsUpEnabled(true);
        if (lightType.equals("rgb_light")) {
            toolbar.inflateMenu(R.menu.menu_grouping_select_all);
        } else {
            toolbar.setTitle(R.string.batch_group);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.menu_grouping_select_all);
        toolbar.setOnMenuItemClickListener(this);
        toolbarTv.setText(groupLight);
        toolbar.setNavigationIcon(R.drawable.icon_return);
        toolbar.setNavigationOnClickListener(v -> finish());
        Drawable moreIcon = ContextCompat.getDrawable(toolbar.getContext(), R.drawable.abc_ic_menu_overflow_material);
        if(moreIcon != null) {
            moreIcon.setColorFilter(ContextCompat.getColor(toolbar.getContext(), R.color.black), PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(moreIcon);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grouping_select_all, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {

        ArrayList<ItemTypeGroup> groups = DBUtils.INSTANCE.getgroupListWithType(this);
        LogUtils.e("zcl--" + groups.toString());

        Intent intent = getIntent();
        scanCURTAIN = intent.getBooleanExtra(Constant.IS_SCAN_CURTAIN, false);
        lightType = intent.getStringExtra("lightType");
        //冷暖灯传递的all light
        if (lightType.equals("rgb_light")) {
            groupLight = intent.getStringExtra("rgb_light_group_name");
        }

        //所有灯的meshId
        allLightId = DBUtils.INSTANCE.getGroupByMeshAddr(0xffff).getId();

        this.mApplication = (TelinkLightApplication) this.getApplication();
        nowLightList = new ArrayList<>();

        if (this.groups == null) {//初始化数据
            this.groups = new ArrayList<>();
            List<DbGroup> list = DBUtils.INSTANCE.getGroupList();//所有组
            LogUtils.e("zcl", "zcl******" + list.toString());
            if (scanCURTAIN) {//是否窗帘
                for (int i = 0; i < list.size(); i++) {
                    if (OtherUtils.isRGBGroup(list.get(i))) {
                        this.groups.add(list.get(i));
                    }
                }
            }
        }


        if (lightType.equals("rgb_light")) {
            if (this.groups.size() > 0) {
                for (int i = 0; i < this.groups.size(); i++) {
                    if (this.groups.get(i).getName().equals(groupLight)) {
                        this.groups.get(i).checked = true;
                        currentGroupIndex = i;
                        SharedPreferencesHelper.putInt(TelinkLightApplication.Companion.getApp(),
                                Constant.DEFAULT_GROUP_ID, currentGroupIndex);
                    } else {
                        this.groups.get(i).checked = false;
                    }
                }
                initHasGroup = true;
            } else {
                initHasGroup = false;
                currentGroupIndex = -1;
            }
        } else {//rgb走这里
            if (this.groups.size() > 0) {
                for (int i = 0; i < this.groups.size(); i++) {
                    if (i == 0) {
                        this.groups.get(i).checked = true;
                        currentGroupIndex = i;
                        SharedPreferencesHelper.putInt(TelinkLightApplication.Companion.getApp(),
                                Constant.DEFAULT_GROUP_ID, currentGroupIndex);
                    } else {
                        this.groups.get(i).checked = false;
                    }
                }
                initHasGroup = true;
            } else {
                initHasGroup = false;
                currentGroupIndex = -1;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //检测service是否为空，为空则重启
        if (TelinkLightService.Instance() == null)
            mApplication.startLightService(TelinkLightService.class);

    }

    // 如果没有网络，则弹出网络设置对话框
    public void checkNetworkAndSync() {
        if (NetWorkUtils.isNetworkAvalible(this)) {
            SyncDataPutOrGetUtils.Companion.syncPutDataStart(this, syncCallback);
        }
    }

    SyncCallback syncCallback = new SyncCallback() {
        @Override
        public void start() { }

        @Override
        public void complete() { }

        @Override
        public void error(String msg) {
            ToastUtils.showLong(R.string.upload_data_failed);
        }
    };


    @Override
    protected void onLocationEnable() { }

    private static class DeviceItemHolder {
        public ImageView icon;
        public TextView txtName;
        public ImageView selected;
        public TextView lightName;
    }

    final class DeviceListAdapter extends BaseAdapter {
        private List<DbLight> lights;

        public DeviceListAdapter(List<DbLight> connectors, Context mContext) {
            lights =
                    connectors;
        }

        @Override
        public int getCount() {
            return this.lights == null ? 0 : this.lights.size();
        }

        @Override
        public DbLight getItem(int position) {
            return this.lights.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceItemHolder holder;
            convertView = inflater.inflate(R.layout.template_batch_small_item, null);
            ImageView icon = convertView.findViewById(R.id.template_device_batch_icon);
            TextView txtName = convertView.findViewById(R.id.template_device_batch_title_blow);
            ImageView selected = convertView.findViewById(R.id.template_device_batch_selected);
            TextView lightName = convertView.findViewById(R.id.template_device_batch_title);
            holder = new DeviceItemHolder();
            holder.icon = icon;
            holder.txtName = txtName;
            holder.selected = selected;
            holder.lightName = lightName;
            convertView.setTag(holder);
            DbLight light = this.getItem(position);

            holder.txtName.setText(light.getName());
            if (light.getProductUUID() == DeviceType.LIGHT_RGB)
                holder.icon.setImageResource(R.drawable.icon_rgb_no_circle);
            else
                holder.icon.setImageResource(R.drawable.icon_rgb_close);


            holder.selected.setImageResource(light.selected ? R.drawable.icon_checkbox_selected :
                    R.drawable.icon_checkbox_unselected);
            holder.lightName.setText(light.getName());
            holder.txtName.setText(getDeviceName(light));
            holder.icon.setVisibility(View.VISIBLE);
            holder.selected.setVisibility(View.VISIBLE);

            return convertView;
        }

        public void add(DbLight light) {

            if (this.lights == null)
                this.lights = new ArrayList<>();
            DBUtils.INSTANCE.saveLight(light, false);
            this.lights.add(light);
        }

        public DbLight get(int meshAddress) {

            if (this.lights == null)
                return null;

            for (DbLight light : this.lights) {
                if (light.getMeshAddr() == meshAddress) {
                    return light;
                }
            }

            return null;
        }

        public List<DbLight> getLights() {
            return lights;
        }
    }

    private String getDeviceName(DbLight light) {
        if (light.getBelongGroupId() != allLightId) {
            return DBUtils.INSTANCE.getGroupNameByID(light.getBelongGroupId(),this);
        } else {
            return getString(R.string.not_grouped);
        }
    }

    /*********************************泰凌微后台数据部分*********************************************/

    /**
     * 事件处理方法
     *
     * @param event
     */

    private void onNError(final DeviceEvent event) {

        TelinkLightService instance = TelinkLightService.Instance();
        if (instance != null)
            instance.idleMode(true);
        TelinkLog.d("DeviceScanningActivity#onNError");
        onLeScanTimeout();
    }

    /**
     * 扫描不到任何设备了
     * （扫描结束）
     */
    private void onLeScanTimeout() {
        //        TelinkLightService.Instance()?.)
        LeBluetooth.getInstance().stopScan();
        TelinkLightService instance = TelinkLightService.Instance();
        if (instance != null)
            instance.idleMode(true);
        this.btnScan.setBackgroundResource(R.color.primary);
        if (adapter.getCount() > 0) {//表示目前已经搜到了至少有一个设备
            scanSuccess();
        } else {
            scanFail();
        }
    }

    private boolean checkIsCurtain(int productUUID) {
        if (productUUID == DeviceType.LIGHT_RGB) {
            return true;
        } else {
            return false;
        }
    }

    private void addDevice(DeviceInfo deviceInfo) {
        int meshAddress = deviceInfo.meshAddress & 0xFF;
        DbLight light = this.adapter.get(meshAddress);

        if (light == null) {
            light = new DbLight();
            light.setName(getString(R.string.device_name) + light.getMeshAddr());
            light.setMeshAddr(meshAddress);
            light.textColor = this.getResources().getColor(
                    R.color.black);
            light.setBelongGroupId(allLightId);
            light.setMacAddr(deviceInfo.macAddress);
            light.setProductUUID(deviceInfo.productUUID);
            light.setSelected(false);
            this.adapter.add(light);
            this.adapter.notifyDataSetChanged();
            this.listDevices.smoothScrollToPosition(adapter.getCount() - 1);
        }
    }

    private DeviceInfo bestRssiDevice = null;

    private void onDeviceStatusChanged(DeviceEvent event) {

        DeviceInfo deviceInfo = event.getArgs();

        switch (deviceInfo.status) {
            case LightAdapter.STATUS_UPDATE_MESH_COMPLETED:
                //加灯完成继续扫描,直到扫不到设备
                DadouDeviceInfo dadouDeviceInfo1 = new DadouDeviceInfo();
                dadouDeviceInfo1.deviceName = deviceInfo.deviceName;
                dadouDeviceInfo1.firmwareRevision = deviceInfo.firmwareRevision;
                dadouDeviceInfo1.longTermKey = deviceInfo.longTermKey;
                dadouDeviceInfo1.macAddress = deviceInfo.macAddress;
                TelinkLog.d("deviceInfo-Mac:" + deviceInfo.productUUID);
                dadouDeviceInfo1.meshAddress = deviceInfo.meshAddress;
                dadouDeviceInfo1.meshUUID = deviceInfo.meshUUID;
                dadouDeviceInfo1.productUUID = deviceInfo.productUUID;
                dadouDeviceInfo1.status = deviceInfo.status;
                dadouDeviceInfo1.meshName = deviceInfo.meshName;
                if (bestRssiDevice == null) {
                    bestRssiDevice = deviceInfo;
                    if (bestRssiDevice.rssi < deviceInfo.rssi) {
                        bestRssiDevice = deviceInfo;
                    }
                }
                //                Log.d(TAG, "onDeviceStatusChanged: " + dadouDeviceInfo1
                //                .macAddress + "-----" + dadouDeviceInfo1.meshAddress);

                new Thread(() -> this.mApplication.getMesh().saveOrUpdate(this)).start();

                if (scanCURTAIN) {
                    if (checkIsCurtain(dadouDeviceInfo1.productUUID) && dadouDeviceInfo1.productUUID == DeviceType.LIGHT_RGB) {
                        addDevice(deviceInfo);
                    }
                } else {
                }

                //扫描出灯就设置为非首次进入
                if (isFirtst) {
                    isFirtst = false;
                    SharedPreferencesHelper.putBoolean(this, SplashActivity.IS_FIRST_LAUNCH, false);
                }
                toolbar.setTitle(getString(R.string.title_scanned_device_num,adapter.getCount()));
                tvStopScan.setVisibility(View.VISIBLE);

                //                Log.d("ScanningTest", "update mesh success");
                mRetryCount = 0;
                //                this.startScan(0);
                break;
            case LightAdapter.STATUS_UPDATE_MESH_FAILURE:
                //加灯失败继续扫描
                if (mRetryCount < MAX_RETRY_COUNT) {
                    mRetryCount++;
                    Log.d("ScanningTest", "update mesh failed , retry count = " + mRetryCount);
                    stopTimer();
                    //                    this.startScan(0);
                } else {
                    Log.d("ScanningTest", "update mesh failed , do not retry");
                }
                break;

            case LightAdapter.STATUS_ERROR_N:
                this.onNError(event);
                break;
            case LightAdapter.STATUS_LOGIN:
                Log.d("ScanningTest", "mConnectTimer = " + mConnectTimer);
                if (mConnectTimer != null && !mConnectTimer.isDisposed()) {
                    Log.d("ScanningTest",
                            " !mConnectTimer.isDisposed() = " + !mConnectTimer.isDisposed());
                    mConnectTimer.dispose();
                    isLoginSuccess = true;
                    //进入分组
                    hideLoadingDialog();
                    startGrouping();
                }
                break;
            case LightAdapter.STATUS_LOGOUT:
                isLoginSuccess = false;
                break;
        }
    }

}
