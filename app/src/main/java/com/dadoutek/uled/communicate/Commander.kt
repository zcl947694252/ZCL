package com.dadoutek.uled.communicate

import com.blankj.utilcode.util.LogUtils
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbLight
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.SharedPreferencesUtils
import com.telink.TelinkApplication
import com.telink.bluetooth.TelinkLog
import com.telink.bluetooth.event.DeviceEvent
import com.telink.bluetooth.event.MeshEvent
import com.telink.bluetooth.event.NotificationEvent
import com.telink.bluetooth.light.LightAdapter
import com.telink.util.Event
import com.telink.util.EventListener
import com.telink.util.Strings
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.experimental.and

object Commander : EventListener<String> {
    private var mApplication: TelinkLightApplication? = null
    private var mGroupingAddr: Int = 0
    private var mGroupAddr: Int = 0
    private var mLightAddr: Int = 0
    private var mGroupSuccess: Boolean = false
    private var mResetSuccess: Boolean = false
    private var mGetVersionSuccess: Boolean = false
    private var mUpdateMeshSuccess: Boolean = false
    private var version: String? = null

    init {
        mApplication = TelinkLightApplication.getApp()
        //监听事件
//        mApplication?.addEventListener(LeScanEvent.LE_SCAN, this)
//        mApplication?.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this)
//        mApplication?.addEventListener(DeviceEvent.STATUS_CHANGED, this)
//        mApplication?.addEventListener(MeshEvent.UPDATE_COMPLETED, this)
//        mApplication?.addEventListener(MeshEvent.ERROR, this)
    }

    fun openOrCloseLights(groupAddr: Int, isOpen: Boolean) {
        val opcode = Opcode.LIGHT_ON_OFF
        mGroupAddr = groupAddr
        val params: ByteArray

        if (isOpen) {
            //0x64代表延时100ms保证开关同步
            params = byteArrayOf(0x01, 0x64, 0x00)
        } else {
            params = byteArrayOf(0x00, 0x64, 0x00)
        }

        TelinkLightService.Instance().sendCommandNoResponse(opcode, mGroupAddr, params)
    }

    @Synchronized
    fun resetLights(lightList: List<DbLight>, successCallback: () -> Unit,
                    failedCallback: () -> Unit) {
        val sleepTime: Long = 200
        val resendCmdTime: Int = 3
        var connectDeviceIndex: Int = 0
        val lastIndex = lightList.size - 1
        val connectDeviceMeshAddr = TelinkLightApplication.getInstance()?.connectDevice?.meshAddress
                ?: 0x00

        if (lightList.isNotEmpty()) {
            Thread {
                //找到当前连接的灯的mesh地址
                for (k in lightList.indices) {
                    if (lightList[k].meshAddr == connectDeviceMeshAddr) {
                        connectDeviceIndex = k
                        break
                    }
                }
                //把当前连接的灯放到list的最后一个
                Collections.swap(lightList, lastIndex, connectDeviceIndex)
                for (light in lightList) {
                    for (k in 0..resendCmdTime) {
                        val opcode = Opcode.KICK_OUT
                        TelinkLightService.Instance().sendCommandNoResponse(opcode, light.meshAddr, null)
                        Thread.sleep(sleepTime)
                    }
                    Thread.sleep(sleepTime);
                    DBUtils.deleteLight(light);
                }
                launch(UI) {
                    successCallback.invoke()
                }
            }.start()
        } else {
            failedCallback.invoke()
        }


    }

    fun addScene(sceneId: Long, meshAddr: Int, color: Int) {
        val opcode = Opcode.SCENE_ADD_OR_DEL
        val list = DBUtils.getActionsBySceneId(sceneId)
        var params: ByteArray
        for (i in list.indices) {
            Thread.sleep(100)
            var temperature = list[i].colorTemperature.toByte()
            if (temperature > 99)
                temperature = 99
            var light = list[i].brightness.toByte()
            if (light > 99)
                light = 99

            var red = color and 0xff0000 shr 16
            var green = color and 0x00ff00 shr 8
            var blue = color and 0x0000ff

            val minVal = 0x50.toByte()
            if (green and 0xff <= minVal)
                green = 0
            if (red and 0xff <= minVal)
                red = 0
            if (blue and 0xff <= minVal)
                blue = 0
            params = byteArrayOf(0x01, sceneId.toByte(), light, red.toByte(), green.toByte(), blue.toByte(), temperature)
            TelinkLightService.Instance().sendCommandNoResponse(opcode, meshAddr, params)
        }
    }

    fun deleteGroup(lightMeshAddr: Int, successCallback: () -> Unit, failedCallback: () -> Unit) {
        mApplication?.addEventListener(NotificationEvent.GET_GROUP, this)

        mLightAddr = lightMeshAddr
        mGroupingAddr = 0xFFFF
        mGroupSuccess = false
        val opcode = Opcode.SET_GROUP          //0xD7 代表设置 组的指令
//        val params = byteArrayOf(0x01, (groupMeshAddr and 0xFF).toByte(), //0x00 代表删除组
//                (groupMeshAddr shr 8 and 0xFF).toByte())
        val params = byteArrayOf(0x00, 0xFF.toByte(), //0x00 代表删除组
                0xFF.toByte())
        TelinkLightService.Instance().sendCommandNoResponse(opcode, lightMeshAddr, params)
        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    var mDisposable: Disposable? = null
                    override fun onComplete() {
                        mDisposable?.dispose()
                        mApplication?.removeEventListener(Commander)
                    }

                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Long) {
                        LogUtils.d("mGroupSuccess = $mGroupSuccess")
                        if (t >= 10) {   //10次 * 200 = 2000, 也就是超过了2s就超时
                            onComplete()
                            failedCallback.invoke()
                        } else if (mGroupSuccess) {
                            onComplete()
                            successCallback.invoke()
                        }
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.d(e.message)
                    }
                })
    }


    fun addGroup(dstAddr: Int, groupAddr: Int, successCallback: () -> Unit, failedCallback: () -> Unit) {
        mApplication?.addEventListener(NotificationEvent.GET_GROUP, this)

        mLightAddr = dstAddr
        mGroupingAddr = groupAddr
        mGroupSuccess = false
        val opcode = Opcode.SET_GROUP          //0xD7 代表添加组的指令
        val params = byteArrayOf(0x01, (groupAddr and 0xFF).toByte(), //0x01 代表添加组
                (groupAddr shr 8 and 0xFF).toByte())
        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    var mDisposable: Disposable? = null
                    override fun onComplete() {
                        mDisposable?.dispose()
                        mApplication?.removeEventListener(Commander)
                    }

                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Long) {
                        if (t >= 10) {   //10次 * 200 = 2000, 也就是超过了2s就超时
                            onComplete()
                            failedCallback.invoke()
                        } else if (mGroupSuccess) {
                            onComplete()
                            successCallback.invoke()
                        }
                    }

                    override fun onError(e: Throwable) {
                        onComplete()
                        failedCallback.invoke()
                        LogUtils.d("addGroup error: ${e.message}")
                    }
                })
    }


    fun updateMeshName(newMeshName: String = DBUtils.lastUser!!.account, newMeshAddr: Int =
            Constant.SWITCH_PIR_ADDRESS,
                       successCallback: () -> Unit,
                       failedCallback: () -> Unit) {
        mUpdateMeshSuccess = false
        this.mApplication?.addEventListener(DeviceEvent.STATUS_CHANGED, this)
        val mesh = mApplication!!.mesh

        val password: ByteArray
        password = Strings.stringToBytes(NetworkFactory.md5(
                NetworkFactory.md5(mesh?.password) + newMeshName), 16)


        TelinkLightService.Instance().adapter.mode = LightAdapter.MODE_UPDATE_MESH
        TelinkLightService.Instance().adapter.mLightCtrl.currentLight.newMeshAddress = newMeshAddr
        TelinkLightService.Instance().adapter.mLightCtrl.reset(
                Strings.stringToBytes(newMeshName, 16), password, null)
//        TelinkLightService.Instance().enableNotification()
//        TelinkLightService.Instance().updateMesh(params)

        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    var mDisposable: Disposable? = null
                    override fun onComplete() {
                        mDisposable?.dispose()
                        mApplication?.removeEventListener(Commander)
                    }

                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Long) {
                        if (t >= 10) {   //10次 * 200 = 2000, 也就是超过了2s就超时
                            onComplete()
                            failedCallback.invoke()
                        } else if (mUpdateMeshSuccess) {
                            onComplete()
                            successCallback.invoke()
                        }
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.d("updateMeshName error: ${e.message}")
                    }
                })


    }


    override fun performed(event: Event<String>?) {
        when (event?.type) {
            NotificationEvent.GET_GROUP -> this.onGetGroupEvent(event as NotificationEvent)
            NotificationEvent.GET_DEVICE_STATE -> this.onGetLightVersion(event as NotificationEvent)
            NotificationEvent.USER_ALL_NOTIFY -> this.onKickoutEvent(event as NotificationEvent)
            MeshEvent.ERROR -> this.onMeshEvent(event as MeshEvent)
            DeviceEvent.STATUS_CHANGED -> this.onDeviceStatusChanged(event as DeviceEvent)
        }

    }

    private fun onDeviceStatusChanged(deviceEvent: DeviceEvent) {
        val deviceInfo = deviceEvent.args
        when (deviceInfo.status) {
            LightAdapter.STATUS_UPDATE_MESH_COMPLETED -> {
                mUpdateMeshSuccess = true
//                ActivityUtils.finishToActivity(MainActivity::class.java, false, true)
            }
            LightAdapter.STATUS_UPDATE_MESH_FAILURE -> {
            }
        }
    }


    private fun onMeshEvent(event: MeshEvent) {
//        ToastUtils.showShort(event.toString())
        LogUtils.d("Error ${event.toString()}")
    }

    private fun onGetGroupEvent(event: NotificationEvent) {
        val info = event.args

        val srcAddress = info.src and 0xFF
        val params = info.params


        if (srcAddress != mLightAddr) {
            return
        }

        var groupAddress: Int
        val len = params.size

        for (j in 0 until len) {

            groupAddress = params[j].toInt()
            if (mGroupingAddr != 0xFFFF) {
                groupAddress = groupAddress or 0x8000
            } else {
                groupAddress = mGroupingAddr
            }

            if (mGroupingAddr == groupAddress) {
                LogUtils.d(String.format("grouping success, groupAddr = %x groupingLight.meshAddr = %x",
                        groupAddress, mLightAddr))
                mGroupSuccess = true
            }
        }
    }

    //关闭渐变
    fun closeGradient(dstAddr: Int,id: Int,speed: Int,successCallback: (version: String?) -> Unit,
                      failedCallback: () -> Unit){
        var opcode = Opcode.APPLY_RGB_GRADIENT
        val gradientActionType= 0x03
        val params: ByteArray
        params = byteArrayOf(gradientActionType.toByte(), id.toByte(), speed.toByte())
        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
    }

    //加载渐变
    fun applyGradient(dstAddr: Int,id: Int,speed: Int,firstAddress: Int,successCallback: (version: String?) -> Unit,
                      failedCallback: () -> Unit){
        var opcode = Opcode.APPLY_RGB_GRADIENT
        val gradientActionType= 0x02
        val params: ByteArray
            params = byteArrayOf(gradientActionType.toByte(), id.toByte(), speed.toByte(), firstAddress.toByte())
        for(i in 0..2){
            TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
            Thread.sleep(50)
        }
    }

    //删除渐变
    fun deleteGradient(dstAddr: Int,id: Int,successCallback: (version: String?) -> Unit,
                       failedCallback: () -> Unit){
        var opcode = Opcode.APPLY_RGB_GRADIENT
        val gradientActionType= 0x01
        val params: ByteArray
        params = byteArrayOf(gradientActionType.toByte(), id.toByte())
        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
    }

    /**
     * 添加渐变
     * id  渐变索引号
     * node 渐变节点号。（填0: 从此颜色开始渐变，填1: 经过此颜色，填2: 以此颜色结束)
     * mode 渐变模式 (填0: RGB渐变， 填1: 双色温渐变)
     * (C没值就都先传FF)(W没值就都先传FF)
     * 双色温渐变模式：[16]: 色温
     */
    fun addGradient(dstAddr: Int,id:Int,node: Int,mode: Int,brightness: Int,
                    r:Int,g:Int,b:Int,c:Int=0xff,w:Int=0xff,successCallback: (version: String?) -> Unit,
                    failedCallback: () -> Unit){
        var opcode = Opcode.APPLY_RGB_GRADIENT
        val gradientActionType= 0x00
        val params: ByteArray
        params = byteArrayOf(gradientActionType.toByte(),id.toByte(),node.toByte(),
                mode.toByte(),brightness.toByte(),r.toByte(),g.toByte(),b.toByte(),c.toByte(),w.toByte())
        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
    }

    fun getDeviceVersion(dstAddr: Int,successCallback: (version: String?) -> Unit,
                         failedCallback: () -> Unit) {
        mApplication?.addEventListener(NotificationEvent.GET_DEVICE_STATE, this)

        mLightAddr = dstAddr
        mGetVersionSuccess = false
        var opcode = Opcode.GET_VERSION          //0xFC 代表获取灯版本的指令

        val params: ByteArray
        if (TelinkApplication.getInstance().connectDevice.meshAddress == dstAddr) {
            params = byteArrayOf(0x00, 0x00)
        } else {
            opcode = Opcode.SEND_MESSAGE_BY_MESH
            params = byteArrayOf(0x3c, TelinkApplication.getInstance().connectDevice.meshAddress.toByte())
        }

        TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr, params)
        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long?> {
                    var mDisposable: Disposable? = null
                    override fun onComplete() {
                        mDisposable?.dispose()
                        mApplication?.removeEventListener(Commander)
                    }

                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Long) {
                        if (t >= 10) {   //30次 * 200 = 6000, 也就是超过了2s就超时
                            onComplete()
                            failedCallback.invoke()
                        } else if (mGetVersionSuccess) {
                            onComplete()
                            successCallback.invoke(version)
                        }
                    }

                    override fun onError(e: Throwable) {
                        onComplete()
                        failedCallback.invoke()
                        LogUtils.d(e.message)
                    }
                })
    }

    private fun onGetLightVersion(event: NotificationEvent) {
        val data = event.args.params
        if (data[0] == (Opcode.GET_VERSION and 0x3F)) {
            version = Strings.bytesToString(Arrays.copyOfRange(data, 1, data.size - 1))
//            va
// l version = Strings.bytesToString(data)
            val meshAddress = event.args.src

//            val light = DBUtils.getLightByMeshAddr(meshAddress)
//            light.version = version

            if(version != ""){
                mGetVersionSuccess = true
            }
            SharedPreferencesUtils.saveCurrentLightVsersion(version)
            TelinkLog.i("OTAPrepareActivity#GET_DEVICE_STATE#src:$meshAddress get version success: $version")
        } else {
            version = Strings.bytesToString(data)
//            val version = Strings.bytesToString(data)
            val meshAddress = event.args.src

//            val light = DBUtils.getLightByMeshAddr(meshAddress)
//            light.version = version

            if(version != ""){
                mGetVersionSuccess = true
            }
            SharedPreferencesUtils.saveCurrentLightVsersion(version)
            TelinkLog.i("OTAPrepareActivity#GET_DEVICE_STATE#src:$meshAddress get version success: $version")
        }
    }

    private fun onKickoutEvent(notificationEvent: NotificationEvent) {
        val data = notificationEvent.args.params
        for (i in data.indices) {
            com.dadoutek.uled.util.LogUtils.d("Res----------" + data[i].toInt())
        }
        com.dadoutek.uled.util.LogUtils.d("Reset----------" + data[0].toInt() + "==" + mLightAddr)
//        if(data[0].toInt()== mLightAddr){
        mResetSuccess = true
//        }
    }
}