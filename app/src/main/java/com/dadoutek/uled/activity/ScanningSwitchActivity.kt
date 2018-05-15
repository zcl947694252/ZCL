package com.dadoutek.uled.activity

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.dadoutek.uled.R
import com.dadoutek.uled.TelinkLightApplication
import com.dadoutek.uled.TelinkLightService
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DeviceType
import com.dd.processbutton.iml.ActionProcessButton
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.TelinkLog
import com.telink.bluetooth.event.DeviceEvent
import com.telink.bluetooth.event.ErrorReportEvent
import com.telink.bluetooth.event.LeScanEvent
import com.telink.bluetooth.light.DeviceInfo
import com.telink.bluetooth.light.LeScanParameters
import com.telink.bluetooth.light.LightAdapter
import com.telink.bluetooth.light.Parameters
import com.telink.util.Event
import com.telink.util.EventListener
import com.telink.util.Strings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scanning_switch.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class ScanningSwitchActivity : AppCompatActivity(), EventListener<String> {
    private val SCAN_TIMEOUT_SECOND: Int = 10

    private lateinit var mApplication: TelinkLightApplication
    private var mRetryLoginCount: Int = 0

    lateinit var mDeviceInfo: DeviceInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning_switch)

        this.mApplication = this.application as TelinkLightApplication

        initView()
        initListener()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.install_switch)
    }

    private fun initListener() {
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN, this)
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this)
        this.mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this)

        progressBtn.onClick {
            if (progressBtn.progress <= 0) {
                mRetryLoginCount = 0
                startScan()
            }
        }

    }

    private fun handleIfSupportBle() {
        //检查是否支持蓝牙设备
        if (!LeBluetooth.getInstance().isSupport(applicationContext)) {
            Toast.makeText(this, "ble not support", Toast.LENGTH_SHORT).show()
            this.finish()
            return
        }

        if (!LeBluetooth.getInstance().isEnabled) {
            LeBluetooth.getInstance().enable(applicationContext)
        }
    }


    private fun startScan() {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN).subscribe({ granted ->
            if (granted) {
                handleIfSupportBle()
                TelinkLightService.Instance().idleMode(true)
                val mesh = mApplication.mesh
                //扫描参数
                val params = LeScanParameters.create()
                params.setMeshName(mesh.factoryName)
                params.setOutOfMeshName(Constant.OUT_OF_MESH_NAME)
                params.setTimeoutSeconds(SCAN_TIMEOUT_SECOND)
                params.setScanMode(false)
                TelinkLightService.Instance().startScan(params)

                progressBtn.setMode(ActionProcessButton.Mode.ENDLESS)   //设置成intermediate的进度条
                progressBtn.progress = 50   //在2-99之间随便设一个值，进度条就会开始动
            } else {

            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        TelinkLightService.Instance().disconnect()
        this.mApplication.removeEventListener(LeScanEvent.LE_SCAN, this)
        this.mApplication.removeEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this)
        this.mApplication.removeEventListener(DeviceEvent.STATUS_CHANGED, this)
        this.mApplication.removeEventListener(ErrorReportEvent.ERROR_REPORT, this)
    }

    override fun performed(event: Event<String>?) {
        when (event?.getType()) {
            LeScanEvent.LE_SCAN -> this.onLeScan(event as LeScanEvent)
            LeScanEvent.LE_SCAN_TIMEOUT -> this.onLeScanTimeout()
            DeviceEvent.STATUS_CHANGED -> this.onDeviceStatusChanged(event as DeviceEvent)
            LeScanEvent.LE_SCAN_COMPLETED -> this.onLeScanTimeout()
            ErrorReportEvent.ERROR_REPORT -> this.onErrorReport(event as ErrorReportEvent)
//            NotificationEvent.GET_GROUP -> this.onGetGroupEvent(event as NotificationEvent)
//            MeshEvent.ERROR -> this.onMeshEvent(event as MeshEvent)
        }
    }

    private fun onErrorReport(event: ErrorReportEvent) {
        val info = (event as ErrorReportEvent).args
        Log.d("Saw", "ScanningActivity#performed#ERROR_REPORT: " + " stateCode-" + info.stateCode
                + " errorCode-" + info.errorCode
                + " deviceId-" + info.deviceId)
    }

    private fun onLeScanTimeout() {
        progressBtn.progress = -1   //控件显示Error状态
    }

    private fun login(): Boolean {
        val mesh = mApplication.mesh
        return TelinkLightService.Instance().login(Strings.stringToBytes(mesh.factoryName, 16),
                Strings.stringToBytes(mesh.factoryPassword, 16))

    }

    private fun onDeviceStatusChanged(deviceEvent: DeviceEvent) {
        val deviceInfo = deviceEvent.args

        when (deviceInfo.status) {
            LightAdapter.STATUS_CONNECTED -> {
                if (!login()) {
                    Log.d("Saw", "Login Failed")
                    //
                    TelinkLightService.Instance().adapter.mode = LightAdapter.MODE_UPDATE_MESH
                    TelinkLightService.Instance().connect(mDeviceInfo.macAddress, 15)
                }

            }
            LightAdapter.STATUS_LOGIN -> {
                progressBtn.progress = 100  //进度控件显示成完成状态

                Observable.create<Boolean> {
                    it.onNext(true)
                }
                        .delay(100, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe {
                            if (mDeviceInfo.productUUID == DeviceType.NORMAL_SWITCH) {
                                startActivity<SelectGroupForSwitchActivity>("deviceInfo" to mDeviceInfo)
                            } else {
                                startActivity<SelectSceneForSwitchActivity>("deviceInfo" to mDeviceInfo)
                            }
                        }
            }

            LightAdapter.STATUS_LOGOUT -> {
                if (mRetryLoginCount > 3) {
                    progressBtn.progress = -1    //控件显示Error状态
                    progressBtn.text = getString(R.string.connect_failed)
                } else {
                    if (!login()) {
                        Log.d("Saw", "Login Failed")
                        TelinkLightService.Instance().connect(mDeviceInfo.macAddress, 15)
                    }
                    mRetryLoginCount++
                }


            }


        }//                btnAddGroups.doneLoadingAnimation(R.color.black,
        //                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp));

    }


    private fun onLeScan(leScanEvent: LeScanEvent) {
        val mesh = this.mApplication.mesh
        val meshAddress = mesh.deviceAddress

        if (meshAddress == -1) {
//            this.showToast(getString(R.string.much_lamp_tip))
            this.finish()
            return
        }
        //更新参数
        val params = Parameters.createUpdateParameters()
        params.setOldMeshName(mesh.factoryName)
        params.setOldPassword(mesh.factoryPassword)
        params.setNewMeshName(mesh.name)
        params.setNewPassword(mesh.password)


//        mDeviceInfo.meshAddress = meshAddress


        Log.d("Saw", "onLeScan leScanEvent.args.productUUID = " + leScanEvent.args.productUUID)
        when (leScanEvent.args.productUUID) {
            DeviceType.NORMAL_SWITCH -> {
                LeBluetooth.getInstance().stopScan()
                mDeviceInfo = leScanEvent.args
                params.setUpdateDeviceList(mDeviceInfo)
                TelinkLightService.Instance().connect(mDeviceInfo.macAddress, 15)
                progressBtn.text = getString(R.string.connecting)
            }
            DeviceType.SCENE_SWITCH -> {
                LeBluetooth.getInstance().stopScan()
                mDeviceInfo = leScanEvent.args
                params.setUpdateDeviceList(mDeviceInfo)
                TelinkLightService.Instance().connect(mDeviceInfo.macAddress, 15)
                progressBtn.text = getString(R.string.connecting)
            }
            else -> {
                toast("leScanEvent.args.productUUID = ${leScanEvent.args.productUUID}")

//                //如果扫到的不是以上两种设备，就重新进行扫描
//                if (mRetryLoginCount > 3) {
//                    progressBtn.progress = -1    //控件显示Error状态
//                    progressBtn.text = getString(R.string.connect_failed)
//                } else {
//                    startScan()
//                    mRetryLoginCount++
//                }
            }
        }
    }
}
