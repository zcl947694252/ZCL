package com.dadoutek.uled.light

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.le.ScanFilter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import butterknife.ButterKnife
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dadoutek.uled.R
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbGroup
import com.dadoutek.uled.model.DbModel.DbLight
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.model.SharedPreferencesHelper
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.pir.ScanningSensorActivity
import com.dadoutek.uled.rgb.RGBDeviceSettingActivity
import com.dadoutek.uled.tellink.TelinkBaseActivity
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.TelinkApplication
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.TelinkLog
import com.telink.bluetooth.event.*
import com.telink.bluetooth.light.*
import com.telink.util.Event
import com.telink.util.EventListener
import com.telink.util.Strings
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_lights_of_group.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.design.indefiniteSnackbar
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by hejiajun on 2018/4/24.
 */

private const val MAX_RETRY_CONNECT_TIME = 5
private const val CONNECT_TIMEOUT = 10
private const val SCAN_TIMEOUT_SECOND: Int = 10
private const val SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND: Long = 1

class LightsOfGroupActivity : TelinkBaseActivity(), EventListener<String>, SearchView.OnQueryTextListener {
    private val REQ_LIGHT_SETTING: Int = 0x01

    private lateinit var group: DbGroup
    private var mDataManager: DataManager? = null
    private var mApplication: TelinkLightApplication? = null
    private lateinit var lightList: MutableList<DbLight>
    private var adapter: LightsOfGroupRecyclerViewAdapter? = null
    private var positionCurrent: Int = 0
    private var currentLight: DbLight? = null
    private var searchView: SearchView? = null
    private var canBeRefresh = true
    private var bestRSSIDevice: DeviceInfo? = null
    private var connectMeshAddress: Int = 0
    private var mTelinkLightService: TelinkLightService? = null
    private var retryConnectCount = 0
    private val connectFailedDeviceMacList: MutableList<String> = mutableListOf()
    private var mConnectDisposal: Disposable? = null
    private var mScanDisposal: Disposable? = null
    private var mScanTimeoutDisposal: Disposable? = null
    private var mNotFoundSnackBar: Snackbar? = null
    private var acitivityIsAlive = true

    override fun onStart() {
        super.onStart()
        // 监听各种事件
        LogUtils.d("____onStart")
    }

    override fun onPostResume() {
        super.onPostResume()
        addListeners()
    }

    fun addListeners() {
        addScanListeners()
        this.mApplication?.addEventListener(DeviceEvent.STATUS_CHANGED, this)
//        this.mApplication?.addEventListener(NotificationEvent.ONLINE_STATUS, this)
        this.mApplication?.addEventListener(ErrorReportEvent.ERROR_REPORT, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lights_of_group)
        ButterKnife.bind(this)
        initToolbar()
        initParameter()
        initOnLayoutListener()
    }

    private fun initOnLayoutListener() {
        val view = getWindow().getDecorView()
        val viewTreeObserver = view.getViewTreeObserver()
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                lazyLoad()
            }
        })
    }

    fun lazyLoad() {
        if (lightList.size != 0) {
            val guide2: View? = adapter!!.getViewByPosition(0, R.id.img_light)
            val guide3 = adapter!!.getViewByPosition(0, R.id.tv_setting)

            val builder = GuideUtils.guideBuilder(this@LightsOfGroupActivity, Constant.TAG_LightsOfGroupActivity)
            builder.addGuidePage(GuideUtils.addGuidePage(guide2!!, R.layout
                    .view_guide_simple_light_1, getString(R.string.light_guide_1), View
                    .OnClickListener {
                        ActivityUtils.startActivity(ScanningSensorActivity::class.java)
                    }))
            builder.addGuidePage(GuideUtils.addGuidePage(guide3!!, R.layout.view_guide_simple_light_1, getString(R.string.light_guide_2)))
            val guide = builder.show()

        }
    }

    private fun initToolbar() {
        toolbar.setTitle(R.string.group_setting_header)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchView!!.clearFocus()
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null && !newText.isEmpty()) {
            filter(newText, true)
            adapter!!.notifyDataSetChanged()
        } else {
            filter(newText, false)
            adapter!!.notifyDataSetChanged()
        }

        return false
    }


    private fun filter(groupName: String?, isSearch: Boolean) {
        val list = DBUtils.groupList
//        val nameList : ArrayList<String> = ArrayList()
        if (lightList != null && lightList.size > 0) {
            lightList.clear()
        }

//        for(i in list.indices){
//            nameList.add(list[i].name)
//        }

        if (isSearch) {
            for (i in list.indices) {
                if (groupName == list[i].name || (list[i].name).startsWith(groupName!!)) {
                    lightList.addAll(DBUtils.getLightByGroupID(list[i].id))
                }
            }

        } else {
            for (i in list.indices) {
                if (list.get(i).meshAddr == 0xffff) {
                    Collections.swap(list, 0, i)
                }
            }

            for (j in list.indices) {
                lightList.addAll(DBUtils.getLightByGroupID(list[j].id))
            }
        }
    }

    private fun initParameter() {
        this.group = this.intent.extras!!.get("group") as DbGroup
        this.mApplication = this.application as TelinkLightApplication
        mDataManager = DataManager(this, mApplication!!.mesh.name, mApplication!!.mesh.password)
    }

    override fun onResume() {
        super.onResume()
        initData()
        initView()
    }

    override fun onStop() {
        super.onStop()
        this.mApplication!!.removeEventListener(this)
        if (TelinkLightService.Instance() != null)
            TelinkLightService.Instance().disableAutoRefreshNotify()
    }

    override fun onDestroy() {
        super.onDestroy()
        //        this.mApplication.removeEventListener(this);
        canBeRefresh = false
        acitivityIsAlive = false
        mScanDisposal?.dispose()
        if (TelinkLightApplication.getInstance().connectDevice == null) {
            TelinkLightService.Instance().idleMode(true)
            LeBluetooth.getInstance().stopScan()
        }
    }

    private fun initData() {
        lightList = ArrayList()
        if (group.meshAddr == 0xffff) {
            //            lightList = DBUtils.getAllLight();
//            lightList=DBUtils.getAllLight()
            filter("", false)
        } else {
            lightList = DBUtils.getLightByGroupID(group.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (group.meshAddr == 0xffff) {
            getMenuInflater().inflate(R.menu.menu_search, menu)
            searchView = (menu!!.findItem(R.id.action_search)).actionView as SearchView
            searchView!!.setOnQueryTextListener(this)
            searchView!!.imeOptions = EditorInfo.IME_ACTION_SEARCH
            searchView!!.setQueryHint(getString(R.string.input_groupAdress))
            searchView!!.setSubmitButtonEnabled(true)
            searchView!!.backgroundColor = resources.getColor(R.color.blue)
            searchView!!.alpha = 0.3f
        }
        return true
    }

    private fun initView() {
        if (group.meshAddr == 0xffff) {
            toolbar.title = getString(R.string.allLight) + " (" + lightList.size + ")"
//            if(searchView==null){
//                toolbar.inflateMenu(R.menu.menu_search)
//                searchView = MenuItemCompat.getActionView(toolbar.menu.findItem(R.id.action_search)) as SearchView
//                searchView!!.setOnQueryTextListener(this)
//            }
        } else {
            toolbar.title = (group.name ?: "") + " (" + lightList.size + ")"
        }
        recycler_view_lights.layoutManager = GridLayoutManager(this, 3)
        adapter = LightsOfGroupRecyclerViewAdapter(R.layout.item_lights_of_group, lightList)
        adapter!!.onItemChildClickListener = onItemChildClickListener
        adapter!!.bindToRecyclerView(recycler_view_lights)
        for (i in lightList.indices) {
            lightList[i].updateIcon()
        }
    }

    var onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
        currentLight = lightList[position]
        positionCurrent = position
        val opcode = Opcode.LIGHT_ON_OFF
        if (view.id == R.id.img_light) {
            canBeRefresh = true
            if (currentLight!!.connectionStatus == ConnectionStatus.OFF.value) {
                TelinkLightService.Instance().sendCommandNoResponse(opcode, currentLight!!.meshAddr,
                        byteArrayOf(0x01, 0x00, 0x00))
                currentLight!!.connectionStatus = ConnectionStatus.ON.value
            } else {
                TelinkLightService.Instance().sendCommandNoResponse(opcode, currentLight!!.meshAddr,
                        byteArrayOf(0x00, 0x00, 0x00))
                currentLight!!.connectionStatus = ConnectionStatus.OFF.value
            }

            currentLight!!.updateIcon()
            DBUtils.updateLight(currentLight!!)
            runOnUiThread {
                adapter?.notifyDataSetChanged()
            }
        } else if (view.id == R.id.tv_setting) {
            if (scanPb.visibility != View.VISIBLE) {
                //判断是否为rgb灯
                var intent = Intent(this@LightsOfGroupActivity, NormalDeviceSettingActivity::class.java)
                if (currentLight?.productUUID == DeviceType.LIGHT_RGB) {
                    intent = Intent(this@LightsOfGroupActivity, RGBDeviceSettingActivity::class.java)
                }
                intent.putExtra(Constant.LIGHT_ARESS_KEY, currentLight)
                intent.putExtra(Constant.GROUP_ARESS_KEY, group.meshAddr)
                intent.putExtra(Constant.LIGHT_REFRESH_KEY, Constant.LIGHT_REFRESH_KEY_OK)
                startActivityForResult(intent, REQ_LIGHT_SETTING)
            } else {
                ToastUtils.showShort(R.string.reconnecting)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_LIGHT_SETTING -> {
                    initData()
                    adapter?.notifyDataSetChanged()
                    val isConnect = data?.getBooleanExtra("data", false) ?: false
                    if (isConnect) {
                        scanPb.visibility = View.VISIBLE
                    }
                }
            }

        }

        Thread {
            //踢灯后没有回调 状态刷新不及时 延时2秒获取最新连接状态
            Thread.sleep(2500)
            if (this@LightsOfGroupActivity == null ||
                    this@LightsOfGroupActivity.isDestroyed ||
                    this@LightsOfGroupActivity.isFinishing || !acitivityIsAlive) {
            } else {
                autoConnect()
            }
        }.start()
    }

    /**********************************telink part***************************************************/

    override fun performed(event: Event<String>) {
        when (event.type) {
            LeScanEvent.LE_SCAN -> onLeScan(event as LeScanEvent)
//            LeScanEvent.LE_SCAN_TIMEOUT -> onLeScanTimeout()
//            LeScanEvent.LE_SCAN_COMPLETED -> onLeScanTimeout()
//            NotificationEvent.ONLINE_STATUS -> this.onOnlineStatusNotify(event as NotificationEvent)
            NotificationEvent.GET_ALARM -> {
            }
            DeviceEvent.STATUS_CHANGED -> this.onDeviceStatusChanged(event as DeviceEvent)
//            MeshEvent.OFFLINE -> this.onMeshOffline(event as MeshEvent)
            ServiceEvent.SERVICE_CONNECTED -> this.onServiceConnected(event as ServiceEvent)
            ServiceEvent.SERVICE_DISCONNECTED -> this.onServiceDisconnected(event as ServiceEvent)
//            NotificationEvent.GET_DEVICE_STATE -> onNotificationEvent(event as NotificationEvent)
            ErrorReportEvent.ERROR_REPORT -> {
                val info = (event as ErrorReportEvent).args
                onErrorReport(info)
            }
        }
    }

    private fun onLogout() {

    }

    /**
     * 处理[NotificationEvent.ONLINE_STATUS]事件
     */
    private fun onOnlineStatusNotify(event: NotificationEvent) {

        if (canBeRefresh) {
            canBeRefresh = false
        } else {
            return
        }

        TelinkLog.i("MainActivity#onOnlineStatusNotify#Thread ID : " + Thread.currentThread().id)

        val notificationInfoList = event.parse() as List<OnlineStatusNotificationParser.DeviceNotificationInfo>

        if (notificationInfoList.isEmpty())
            return

        for (notificationInfo in notificationInfoList) {

            if (notificationInfo.meshAddress == TelinkApplication.getInstance().connectDevice.meshAddress) {
                currentLight?.textColor = ContextCompat.getColor(
                        this, R.color.primary)
            }

            for (dbLight in lightList) {
                if (notificationInfo.meshAddress == dbLight.meshAddr) {
                    dbLight.connectionStatus = notificationInfo.connectionStatus.value
                    dbLight.updateIcon()
                    DBUtils.updateLight(dbLight)
                    runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }

                }
            }
        }
    }

    fun autoConnect() {
        //检查是否支持蓝牙设备
        if (!LeBluetooth.getInstance().isSupport(applicationContext)) {
            Toast.makeText(this, "ble not support", Toast.LENGTH_SHORT).show()
            ActivityUtils.finishAllActivities()
        } else {  //如果蓝牙没开，则弹窗提示用户打开蓝牙
            if (!LeBluetooth.getInstance().isEnabled) {
                indefiniteSnackbar(root, R.string.openBluetooth, R.string.btn_ok) {
                    LeBluetooth.getInstance().enable(applicationContext)
                }
            } else {
                //如果位置服务没打开，则提示用户打开位置服务
                if (!BleUtils.isLocationEnable(this)) {
                    launch(UI) {
                        showOpenLocationServiceDialog()
                    }
                } else {
                    launch(UI) {
                        hideLocationServiceDialog()
                    }
                    mTelinkLightService = TelinkLightService.Instance()
                    if (TelinkLightApplication.getInstance().connectDevice == null) {
                        while (TelinkApplication.getInstance()?.serviceStarted == true) {
                            launch(UI) {
                                retryConnectCount = 0
                                connectFailedDeviceMacList.clear()
                                startScan()
                            }
                            break;
                        }

                    } else {
                        launch(UI) {
                            scanPb?.visibility = View.GONE
                            SharedPreferencesHelper.putBoolean(TelinkLightApplication.getInstance(), Constant.CONNECT_STATE_SUCCESS_KEY, true);
                        }
                    }

                }
            }

        }

        val deviceInfo = this.mApplication?.connectDevice

        if (deviceInfo != null) {
            this.connectMeshAddress = (this.mApplication?.connectDevice?.meshAddress
                    ?: 0x00) and 0xFF
        }
    }

    var locationServiceDialog: AlertDialog? = null
    fun showOpenLocationServiceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.open_location_service)
        builder.setNegativeButton(getString(R.string.btn_sure)) { dialog, which ->
            BleUtils.jumpLocationSetting()
        }
        locationServiceDialog = builder.create()
        locationServiceDialog?.setCancelable(false)
        locationServiceDialog?.show()
    }

    fun hideLocationServiceDialog() {
        locationServiceDialog?.hide()
    }

    @SuppressLint("CheckResult")
    private fun startScan() {
        //当App在前台时，才进行扫描。
        if (AppUtils.isAppForeground())
            if (acitivityIsAlive || !(mScanDisposal?.isDisposed ?: false)) {
                LogUtils.d("startScanLight_LightOfGroup")
                mScanDisposal = RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN)
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            if (it) {
                                TelinkLightService.Instance().idleMode(true)
                                bestRSSIDevice = null   //扫描前置空信号最好设备。
                                //扫描参数
                                val account = DBUtils.lastUser?.account

                                val scanFilters = java.util.ArrayList<ScanFilter>()
                                val scanFilter = ScanFilter.Builder()
                                        .setDeviceName(account)
                                        .build()
                                scanFilters.add(scanFilter)

                                val params = LeScanParameters.create()
                                if (!com.dadoutek.uled.util.AppUtils.isExynosSoc()) {
                                    params.setScanFilters(scanFilters)
                                }
                                params.setMeshName(account)
                                params.setOutOfMeshName(account)
                                params.setTimeoutSeconds(SCAN_TIMEOUT_SECOND)
                                params.setScanMode(false)

                                addScanListeners()
                                TelinkLightService.Instance().startScan(params)
                                startCheckRSSITimer()

                            } else {
                                //没有授予权限
                                DialogUtils.showNoBlePermissionDialog(this, {
                                    retryConnectCount = 0
                                    startScan()
                                }, { finish() })
                            }
                        }
            }
    }

    private fun startConnectTimer() {
        mConnectDisposal?.dispose()
        mConnectDisposal = Observable.timer(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    retryConnect()
                }
    }

    private fun stopConnectTimer() {
        mConnectDisposal?.dispose()
    }

    @SuppressLint("CheckResult")
    private fun connect(mac: String) {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe {
                    if (it) {
                        //授予了权限
                        if (TelinkLightService.Instance() != null) {
                            progressBar?.visibility = View.VISIBLE
                            TelinkLightService.Instance().connect(mac, CONNECT_TIMEOUT)
                            startConnectTimer()
                        }
                    } else {
                        //没有授予权限
                        DialogUtils.showNoBlePermissionDialog(this, { connect(mac) }, { finish() })
                    }
                }
    }

    /**
     * 扫描不到任何设备了
     * （扫描结束）
     */
    private fun onLeScanTimeout() {
        LogUtils.d("onErrorReport: onLeScanTimeout")
//        if (mConnectSnackBar) {
//        indefiniteSnackbar(root, R.string.not_found_light, R.string.retry) {
        TelinkLightService.Instance().idleMode(true)
        LeBluetooth.getInstance().stopScan()
        startScan()
//        }
//        } else {
//            retryConnect()
//        }

    }

    private fun startCheckRSSITimer() {
        mScanTimeoutDisposal?.dispose()
        val periodCount = SCAN_TIMEOUT_SECOND.toLong() - SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND
        Observable.intervalRange(1, periodCount, SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND, 1,
                TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    override fun onComplete() {
                        LogUtils.d("onLeScanTimeout()")
                        onLeScanTimeout()
                    }

                    override fun onSubscribe(d: Disposable) {
                        mScanTimeoutDisposal = d
                    }

                    override fun onNext(t: Long) {
                        if (bestRSSIDevice != null) {
                            mScanTimeoutDisposal?.dispose()
                            LogUtils.d("connect device , mac = ${bestRSSIDevice?.macAddress}  rssi = ${bestRSSIDevice?.rssi}")
                            connect(bestRSSIDevice!!.macAddress)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("SawTest", "error = $e")

                    }
                })
    }

    private fun addScanListeners() {
        this.mApplication?.addEventListener(LeScanEvent.LE_SCAN, this)
        this.mApplication?.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this)
        this.mApplication?.addEventListener(LeScanEvent.LE_SCAN_COMPLETED, this)
    }

    private fun login() {
        val account = DBUtils.lastUser?.account
        val pwd = NetworkFactory.md5(NetworkFactory.md5(account) + account).substring(0, 16)
        TelinkLightService.Instance().login(Strings.stringToBytes(account, 16)
                , Strings.stringToBytes(pwd, 16))
    }

    private fun onNError(event: DeviceEvent) {

//        ToastUtils.showLong(getString(R.string.connect_fail))
        SharedPreferencesHelper.putBoolean(this, Constant.CONNECT_STATE_SUCCESS_KEY, false)

        TelinkLightService.Instance().idleMode(true)
        TelinkLog.d("DeviceScanningActivity#onNError")

        val builder = AlertDialog.Builder(this)
        builder.setMessage("当前环境:Android7.0!连接重试:" + " 3次失败!")
        builder.setNegativeButton("confirm") { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(false)
        builder.show()
    }

    /**
     * 重试
     * 先扫描到信号比较好的，再连接。
     */
    private fun retryConnect() {
        if (retryConnectCount < MAX_RETRY_CONNECT_TIME) {
            retryConnectCount++
            if (TelinkLightService.Instance().adapter.mLightCtrl.currentLight?.isConnected != true)
                startScan()
            else
                login()
        } else {
            TelinkLightService.Instance().idleMode(true)
            if (!scanPb.isShown) {
                retryConnectCount = 0
                connectFailedDeviceMacList.clear()
                startScan()
            }

        }
    }

    override fun onPause() {
        super.onPause()
        mScanTimeoutDisposal?.dispose()
        mConnectDisposal?.dispose()
        mNotFoundSnackBar?.dismiss()
        //移除事件
        this.mApplication?.removeEventListener(this)
        stopConnectTimer()
    }

    private fun onDeviceStatusChanged(event: DeviceEvent) {

        val deviceInfo = event.args

        when (deviceInfo.status) {
            LightAdapter.STATUS_LOGIN -> {

                TelinkLightService.Instance().enableNotification()
                TelinkLightService.Instance().updateNotification()
                launch(UI) {
                    stopConnectTimer()
                    if (progressBar?.visibility != View.GONE)
                        progressBar?.visibility = View.GONE
                    delay(300)
                }

                val connectDevice = this.mApplication?.connectDevice
                if (connectDevice != null) {
                    this.connectMeshAddress = connectDevice.meshAddress
                }

                scanPb.visibility = View.GONE
                adapter?.notifyDataSetChanged()
                SharedPreferencesHelper.putBoolean(this, Constant.CONNECT_STATE_SUCCESS_KEY, true)
            }
            LightAdapter.STATUS_CONNECTING -> {
                Log.d("connectting", "444")
                scanPb.visibility = View.VISIBLE
            }
            LightAdapter.STATUS_CONNECTED -> {
                if (!TelinkLightService.Instance().isLogin)
                    login()
            }
            LightAdapter.STATUS_ERROR_N -> onNError(event)
        }
    }

    private fun onServiceConnected(event: ServiceEvent) {
//        LogUtils.d("onServiceConnected")
    }

    private fun onServiceDisconnected(event: ServiceEvent) {
        LogUtils.d("onServiceDisconnected")
        TelinkLightApplication.getInstance().startLightService(TelinkLightService::class.java)
    }

    /**
     * 处理扫描事件
     *
     * @param event
     */
    @Synchronized
    private fun onLeScan(event: LeScanEvent) {
        val mesh = this.mApplication?.mesh
        val meshAddress = mesh?.generateMeshAddr()
        val deviceInfo: DeviceInfo = event.args

        Thread {
            val dbLight = DBUtils.getLightByMeshAddr(deviceInfo.meshAddress)
            if (dbLight != null && dbLight.macAddr == "0") {
                dbLight.macAddr = deviceInfo.macAddress
                DBUtils.updateLight(dbLight)
            }
        }.start()

        if (!isSwitch(deviceInfo.productUUID) && !connectFailedDeviceMacList.contains(deviceInfo.macAddress)) {
//            connect(deviceInfo.macAddress)
            if (bestRSSIDevice != null) {
                //扫到的灯的信号更好并且没有连接失败过就把要连接的灯替换为当前扫到的这个。
                if (deviceInfo.rssi > bestRSSIDevice?.rssi ?: 0) {
                    LogUtils.d("change to device with better RSSI  new meshAddr = ${deviceInfo.meshAddress} rssi = ${deviceInfo.rssi}")
                    bestRSSIDevice = deviceInfo
                }
            } else {
                LogUtils.d("RSSI  meshAddr = ${deviceInfo.meshAddress} rssi = ${deviceInfo.rssi}")
                bestRSSIDevice = deviceInfo
            }

        }

    }

    private fun isSwitch(uuid: Int): Boolean {
        return when (uuid) {
            DeviceType.SCENE_SWITCH, DeviceType.NORMAL_SWITCH, DeviceType.NORMAL_SWITCH2, DeviceType.SENSOR -> {
                LogUtils.d("This is switch")
                true
            }
            else -> {
                false

            }
        }
    }

    private fun onErrorReport(info: ErrorReportInfo) {
//        LogUtils.d("onErrorReport current device mac = ${bestRSSIDevice?.macAddress}")
        if (bestRSSIDevice != null) {
            connectFailedDeviceMacList.add(bestRSSIDevice!!.macAddress)
        }
        when (info.stateCode) {
            ErrorReportEvent.STATE_SCAN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_SCAN_BLE_DISABLE -> {
                        LogUtils.d("蓝牙未开启")
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_ADV -> {
                        LogUtils.d("无法收到广播包以及响应包")
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_TARGET -> {
                        LogUtils.d("未扫到目标设备")
                    }
                }

            }
            ErrorReportEvent.STATE_CONNECT -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_CONNECT_ATT -> {
                        LogUtils.d("未读到att表")
                    }
                    ErrorReportEvent.ERROR_CONNECT_COMMON -> {
                        LogUtils.d("未建立物理连接")
                    }
                }
                retryConnect()

            }
            ErrorReportEvent.STATE_LOGIN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_LOGIN_VALUE_CHECK -> {
                        LogUtils.d("value check失败： 密码错误")
                    }
                    ErrorReportEvent.ERROR_LOGIN_READ_DATA -> {
                        LogUtils.d("read login data 没有收到response")
                    }
                    ErrorReportEvent.ERROR_LOGIN_WRITE_DATA -> {
                        LogUtils.d("write login data 没有收到response")
                    }
                }
                retryConnect()

            }
        }
    }
}

