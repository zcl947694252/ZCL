package com.dadoutek.uled.switches

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.BuildConfig
import com.dadoutek.uled.R
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.Constant.VENDOR_ID
import com.dadoutek.uled.model.DbModel.DbSwitch
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.othersview.MainActivity
import com.dadoutek.uled.tellink.TelinkBaseActivity
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.AppUtils
import com.dadoutek.uled.util.DialogUtils
import com.dd.processbutton.iml.ActionProcessButton
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.event.DeviceEvent
import com.telink.bluetooth.event.ErrorReportEvent
import com.telink.bluetooth.event.LeScanEvent
import com.telink.bluetooth.light.DeviceInfo
import com.telink.bluetooth.light.ErrorReportInfo
import com.telink.bluetooth.light.LeScanParameters
import com.telink.bluetooth.light.LightAdapter
import com.telink.util.Event
import com.telink.util.EventListener
import com.telink.util.Strings
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scanning_switch.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT = 10
private const val SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND: Long = 1
private const val SCAN_TIMEOUT_SECOND: Int = 10
private const val MAX_RETRY_CONNECT_TIME = 1


class ScanningSwitchActivity : TelinkBaseActivity(), EventListener<String> {

    private var connectDisposable: Disposable? = null

    private lateinit var mApplication: TelinkLightApplication

    private val mDisposable = CompositeDisposable()

    private var mRxPermission: RxPermissions? = null

    private var localVersion: String? = null

//    private var scanDisposable: Disposable? = null

    private var mDeviceMeshName: String = Constant.PIR_SWITCH_MESH_NAME

    private var bestRSSIDevice: DeviceInfo? = null

    private var mScanTimeoutDisposal: Disposable? = null

    private var mConnectDisposal: Disposable? = null

    private var retryConnectCount = 0

    private var isSupportInstallOldDevice=false

//    private var isOTA=true

    private var dbSwitch=DbSwitch()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning_switch)
        this.mApplication = this.application as TelinkLightApplication
        mRxPermission = RxPermissions(this)
        initView()
        initListener()
        readyConnection()
    }

    private var mIntervalCheckConnection: Disposable? = null

    @SuppressLint("CheckResult")
    private fun readyConnection() {
        val b =TelinkLightService.Instance()!=null&& !TelinkLightService.Instance()!!.isLogin
        mIntervalCheckConnection = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //如果已经断连或者检测超过10次，直接完成。
                    if (b || it > 10) {
                        onInited()
                    }
                },{})

        TelinkLightService.Instance()?.idleMode(true)
        TelinkLightService.Instance()?.disconnect()

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                doFinish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private var mIsInited: Boolean = false

    private fun initView() {
        mIsInited = false;
        content.visibility = View.INVISIBLE
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.switch_title)
    }

    private fun initListener() {
        mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this@ScanningSwitchActivity)
        progressBtn.onClick {
            retryConnectCount = 0
            isSupportInstallOldDevice=false
//            isOTA=true
            progressOldBtn.progress=0
            startScan()
        }

        progressOldBtn.onClick {
            retryConnectCount = 0
            isSupportInstallOldDevice=true
//            isOTA=true
            progressBtn.progress=0
            startScan()
        }
//        otaBtn.onClick {
////            retryConnectCount = 0
//            isOTA=false
//            otaBtn.progress=0
//            startScan()
//        }
    }

    private fun getScanFilters(): MutableList<ScanFilter> {
        val scanFilters = ArrayList<ScanFilter>()

        scanFilters.add(ScanFilter.Builder()
                .setManufacturerData(VENDOR_ID,
                        byteArrayOf(0, 0, 0, 0, 0, 0, DeviceType.NORMAL_SWITCH.toByte()),
                        byteArrayOf(0, 0, 0, 0, 0, 0, 0xFF.toByte()))
                .build())

        scanFilters.add(ScanFilter.Builder()
                .setManufacturerData(VENDOR_ID,
                        byteArrayOf(0, 0, 0, 0, 0, 0, DeviceType.SCENE_SWITCH.toByte()),
                        byteArrayOf(0, 0, 0, 0, 0, 0, 0xFF.toByte()))
                .build())
        scanFilters.add(ScanFilter.Builder()
                .setManufacturerData(VENDOR_ID,
                        byteArrayOf(0, 0, 0, 0, 0, 0, DeviceType.NORMAL_SWITCH2.toByte()),
                        byteArrayOf(0, 0, 0, 0, 0, 0, 0xFF.toByte()))
                .build())
        scanFilters.add(ScanFilter.Builder()
                .setManufacturerData(VENDOR_ID,
                        byteArrayOf(0, 0, 0, 0, 0, 0, DeviceType.SMART_CURTAIN_SWITCH.toByte()),
                        byteArrayOf(0, 0, 0, 0, 0, 0, 0xFF.toByte()))
                .build())
        return scanFilters
    }

    @SuppressLint("CheckResult")
    private fun startScan() {
        LeBluetooth.getInstance().stopScan()
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN).subscribe({ granted ->
            if (granted) {

                bestRSSIDevice = null   //扫描前置空信号最好设备。
                TelinkLightService.Instance()?.idleMode(true)
                val mesh = mApplication.mesh
                //扫描参数
                val params = LeScanParameters.create()
                if (BuildConfig.DEBUG) {
                    params.setMeshName(Constant.PIR_SWITCH_MESH_NAME)
                } else {
                    params.setMeshName(mesh.factoryName)
                }

                if (!AppUtils.isExynosSoc()) {
                    params.setScanFilters(getScanFilters())
                }
                //把当前的mesh设置为out_of_mesh，这样也能扫描到已配置过的设备
                if(isSupportInstallOldDevice){
                    params.setMeshName(mesh.name)
                    params.setOutOfMeshName(mesh.name)
                }
                params.setTimeoutSeconds(SCAN_TIMEOUT_SECOND)
                params.setScanMode(false)

                this.mApplication.addEventListener(LeScanEvent.LE_SCAN, this)
                this.mApplication.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this)

                startCheckRSSITimer()
                TelinkLightService.Instance()?.startScan(params)
                if(isSupportInstallOldDevice){
                    progressOldBtn.setMode(ActionProcessButton.Mode.ENDLESS)   //设置成intermediate的进度条
                    progressOldBtn.progress = 50   //在2-99之间随便设一个值，进度条就会开始动
                }else{
                    progressBtn.setMode(ActionProcessButton.Mode.ENDLESS)   //设置成intermediate的进度条
                    progressBtn.progress = 50   //在2-99之间随便设一个值，进度条就会开始动
                }
            } else {

            }
        },{})
    }

    private fun startCheckRSSITimer() {
        mScanTimeoutDisposal?.dispose()
        val periodCount = SCAN_TIMEOUT_SECOND.toLong() - SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND
        Observable.intervalRange(1, periodCount, SCAN_BEST_RSSI_DEVICE_TIMEOUT_SECOND, 1,
                TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    override fun onComplete() {
                       //("onLeScanTimeout()")
                        onLeScanTimeout()
                    }

                    override fun onSubscribe(d: Disposable) {
                        mScanTimeoutDisposal = d
                    }

                    override fun onNext(t: Long) {
                        if (bestRSSIDevice != null) {
                            mScanTimeoutDisposal?.dispose()
                           //("connect device , mac = ${bestRSSIDevice?.macAddress}  rssi = ${bestRSSIDevice?.rssi}")
                            connect(bestRSSIDevice!!.macAddress)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("SawTest", "error = $e")

                    }
                })
    }

//    private fun retryConnect() {
//        mRetryConnectCount++
//       //("reconnect time = $mRetryConnectCount")
//        if (mRetryConnectCount > MAX_RETRY_CONNECT_TIME) {
//            showConnectFailed()
//        } else {
//            if (TelinkLightApplication.getApp().connectDevice == null)
//                connect()   //重新进行连接
//            else
//                login()   //重新进行登录
//        }
//    }

//    private fun retryLogin() {
//        mRetryConnectCount++
//       //("reconnect time = $mRetryConnectCount")
//        if (mRetryConnectCount > MAX_RETRY_CONNECT_TIME) {
//            showConnectFailed()
//        } else {
//            login()   //重新进行连接
//        }
//    }

    override fun onResume() {
        super.onResume()
        progressBtn.progress = 0
        progressOldBtn.progress=0
    }

    override fun onPause() {
        super.onPause()
        mScanTimeoutDisposal?.dispose()
        connectDisposable?.dispose()
//        scanDisposable?.dispose()
        mConnectDisposal?.dispose()
        this.mApplication.removeEventListener(this)
        stopConnectTimer()
    }

    override fun performed(event: Event<String>?) {
        when (event?.type) {
            LeScanEvent.LE_SCAN -> this.onLeScan(event as LeScanEvent)
//            LeScanEvent.LE_SCAN_TIMEOUT -> this.onLeScanTimeout()
            DeviceEvent.STATUS_CHANGED -> this.onDeviceStatusChanged(event as DeviceEvent)
//            LeScanEvent.LE_SCAN_COMPLETED -> this.onLeScanTimeout()
            ErrorReportEvent.ERROR_REPORT -> {
                val info = (event as ErrorReportEvent).args
                onErrorReport(info)
            }

//            NotificationEvent.GET_GROUP -> this.onGetGroupEvent(event as NotificationEvent)
//            MeshEvent.ERROR -> this.onMeshEvent(event as MeshEvent)
        }
    }

    private fun onErrorReport(info: ErrorReportInfo) {
        when (info.stateCode) {
            ErrorReportEvent.STATE_SCAN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_SCAN_BLE_DISABLE -> {
                       //("蓝牙未开启")
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_ADV -> {
                       //("无法收到广播包以及响应包")
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_TARGET -> {
                       //("未扫到目标设备")
                    }
                }

            }
            ErrorReportEvent.STATE_CONNECT -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_CONNECT_ATT -> {
                       //("未读到att表")
                    }
                    ErrorReportEvent.ERROR_CONNECT_COMMON -> {
                       //("未建立物理连接")

                    }
                }
                retryConnect()

            }
            ErrorReportEvent.STATE_LOGIN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_LOGIN_VALUE_CHECK -> {
                       //("value check失败： 密码错误")
                    }
                    ErrorReportEvent.ERROR_LOGIN_READ_DATA -> {
                       //("read login data 没有收到response")
                    }
                    ErrorReportEvent.ERROR_LOGIN_WRITE_DATA -> {
                       //("write login data 没有收到response")
                    }
                }
               //("onError login")
                retryConnect()

            }
        }
    }

    private fun onLeScanTimeout() {
       //("onLeScanTimeout")
        GlobalScope.launch(Dispatchers.Main) {
            retryConnectCount = 0
//            if(isOTA){
                if(isSupportInstallOldDevice){
                    progressOldBtn.progress = -1   //控件显示Error状态
                    progressOldBtn.text = getString(R.string.not_found_switch)
                }else{
                    progressBtn.progress = -1   //控件显示Error状态
                    progressBtn.text = getString(R.string.not_found_switch)
                }
//            }
//            else{
//                otaBtn.progress = -1   //控件显示Error状态
//                otaBtn.text = getString(R.string.not_found_switch)
//            }

        }
    }


    private fun onDeviceStatusChanged(deviceEvent: DeviceEvent) {
        val deviceInfo = deviceEvent.args
        mDeviceMeshName = deviceInfo.meshName
        when (deviceInfo.status) {
            LightAdapter.STATUS_LOGIN -> {
                onLogin()
                stopConnectTimer()
               //("connected22")
            }
            LightAdapter.STATUS_LOGOUT -> {
//                onLoginFailed()

                GlobalScope.launch(Dispatchers.Main) {
                    if (!mIsInited){
                        onInited()
                    }
                }
            }

            LightAdapter.STATUS_CONNECTED -> {
                connectDisposable?.dispose()

                login()
               //("connected11")
            }
        }

    }

    private fun onInited() {
        mIntervalCheckConnection?.dispose()
        mIsInited = true
        content.visibility = View.VISIBLE
        pb.visibility = View.GONE

    }

    private fun login() {
        val mesh = TelinkLightApplication.getApp().mesh
        val pwd: String

        if (mDeviceMeshName == Constant.PIR_SWITCH_MESH_NAME) {
            pwd = mesh.factoryPassword.toString()
        } else {
            pwd = NetworkFactory.md5(NetworkFactory.md5(mDeviceMeshName) + mDeviceMeshName)
                    .substring(0, 16)
        }
        TelinkLightService.Instance()?.login(Strings.stringToBytes(mDeviceMeshName, 16)
                , Strings.stringToBytes(pwd, 16))

    }

    private fun onLogin() {
        TelinkLightService.Instance()?.enableNotification()
        mApplication.removeEventListener(this)
        connectDisposable?.dispose()
        mScanTimeoutDisposal?.dispose()

            if(isSupportInstallOldDevice){
                progressOldBtn.progress = 100  //进度控件显示成完成状态
            }else{
                progressBtn.progress = 100  //进度控件显示成完成状态
            }


        if(isSupportInstallOldDevice){
            if (bestRSSIDevice?.productUUID == DeviceType.NORMAL_SWITCH ||
                    bestRSSIDevice?.productUUID == DeviceType.NORMAL_SWITCH2) {
                startActivity<ConfigNormalSwitchActivity>("deviceInfo" to bestRSSIDevice!!, "group" to "false")
            } else if (bestRSSIDevice?.productUUID == DeviceType.SCENE_SWITCH) {
                startActivity<ConfigSceneSwitchActivity>("deviceInfo" to bestRSSIDevice!!, "group" to "false")
            } else if (bestRSSIDevice?.productUUID == DeviceType.SMART_CURTAIN_SWITCH) {
                startActivity<ConfigCurtainSwitchActivity>("deviceInfo" to bestRSSIDevice!!, "group" to "false")
            }
        }else{
            if (bestRSSIDevice?.productUUID == DeviceType.NORMAL_SWITCH ||
                    bestRSSIDevice?.productUUID == DeviceType.NORMAL_SWITCH2) {
                startActivity<ConfigNormalSwitchActivity>("deviceInfo" to bestRSSIDevice!!)
            } else if (bestRSSIDevice?.productUUID == DeviceType.SCENE_SWITCH) {
                startActivity<ConfigSceneSwitchActivity>("deviceInfo" to bestRSSIDevice!!)
            }else if (bestRSSIDevice?.productUUID == DeviceType.SMART_CURTAIN_SWITCH) {
                startActivity<ConfigCurtainSwitchActivity>("deviceInfo" to bestRSSIDevice!!)
            }
        }
    }


    private fun showConnectFailed() {
        mApplication.removeEventListener(this)
        TelinkLightService.Instance()?.idleMode(true)
            if(isSupportInstallOldDevice){
                progressOldBtn.progress = -1    //控件显示Error状态
                progressOldBtn.text = getString(R.string.connect_failed)
            }else{
                progressBtn.progress = -1    //控件显示Error状态
                progressBtn.text = getString(R.string.connect_failed)
            }
    }




    @SuppressLint("CheckResult")
    private fun connect(mac: String) {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe {
                    if (it) {
                        //授予了权限
                        if (TelinkLightService.Instance() != null) {
//                            progressBar?.visibility = View.VISIBLE
                            mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this@ScanningSwitchActivity)
                            mApplication.addEventListener(ErrorReportEvent.ERROR_REPORT, this@ScanningSwitchActivity)
                            TelinkLightService.Instance()?.connect(mac, CONNECT_TIMEOUT)
                            startConnectTimer()

//                            if(isOTA){
                                if(isSupportInstallOldDevice){
                                    progressOldBtn.text = getString(R.string.connecting)
                                }else{
                                    progressBtn.text = getString(R.string.connecting)
                                }
//                            }
//                            else{
//                                otaBtn.text = getString(R.string.connecting)
//                            }
                        }
                    } else {
                        //没有授予权限
                        DialogUtils.showNoBlePermissionDialog(this, { connect(mac) }, { finish() })
                    }
                }
    }

    private fun stopConnectTimer() {
        mConnectDisposal?.dispose()
    }

    private fun startConnectTimer() {
        mConnectDisposal?.dispose()
        mConnectDisposal = Observable.timer(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ retryConnect() },{})
    }

    /**
     * 重试
     * 先扫描到信号比较好的，再连接。
     */
    private fun retryConnect() {
        if (retryConnectCount < MAX_RETRY_CONNECT_TIME) {
            retryConnectCount++
            if (TelinkLightService.Instance()?.adapter!!.mLightCtrl.currentLight?.isConnected != true)
                startScan()
            else
                login()
        } else {
            retryConnectCount = 0
            TelinkLightService.Instance()?.idleMode(true)
            showConnectFailed()
        }
    }

    private fun doFinish() {
        this.mApplication.removeEventListener(this)
        TelinkLightService.Instance()?.idleMode(true)
        ActivityUtils.finishToActivity(MainActivity::class.java, false, true)
    }


    override fun onBackPressed() {
        this.doFinish()
    }

    private fun onLeScan(leScanEvent: LeScanEvent) {
        val meshAddress = Constant.SWITCH_PIR_ADDRESS
        val deviceInfo: DeviceInfo = leScanEvent.args
        if (meshAddress == -1) {
            this.doFinish()
            return
        }
        when (leScanEvent.args.productUUID) {
            DeviceType.SCENE_SWITCH, DeviceType.NORMAL_SWITCH, DeviceType.NORMAL_SWITCH2,DeviceType.SMART_CURTAIN_SWITCH -> {
                val MAX_RSSI = 81
                if (leScanEvent.args.rssi < MAX_RSSI) {

                    if (bestRSSIDevice != null) {
                        //扫到的灯的信号更好并且没有连接失败过就把要连接的灯替换为当前扫到的这个。
                        if (deviceInfo.rssi > bestRSSIDevice?.rssi ?: 0) {
                           LogUtils.e("changeToScene to device with better RSSI  new meshAddr = ${deviceInfo.meshAddress} rssi = ${deviceInfo.rssi}")
                            bestRSSIDevice = deviceInfo
                        }
                    } else {
                        LogUtils.e("RSSI  meshAddr = ${deviceInfo.meshAddress} rssi = ${deviceInfo.rssi}")
                        bestRSSIDevice = deviceInfo
                    }
                } else {
                    ToastUtils.showLong(getString(R.string.rssi_low))
                }
            }
        }
    }
}
