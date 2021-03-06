package com.dadoutek.uled.model.routerModel

import com.blankj.utilcode.util.LogUtils
import com.dadoutek.uled.gateway.bean.DbRouter
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.Response
import com.dadoutek.uled.model.dbModel.DbSceneActions
import com.dadoutek.uled.network.*
import com.dadoutek.uled.rgb.AddGradientBean
import com.dadoutek.uled.router.DelGradientBodyBean
import com.dadoutek.uled.router.GroupBlinkBodyBean
import com.dadoutek.uled.router.SceneAddBodyBean
import com.dadoutek.uled.router.bean.*
import com.dadoutek.uled.switches.RouterListBody
import com.dadoutek.uled.switches.bean.KeyBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * 创建者     ZCL
 * 创建时间   2020/8/11 15:01
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
object RouterModel {

    /**
     * 配置路由wifi
     */
    fun routerConfigWifi(macAddr: String, wifiAccount: String, pwd: String, hour: Int, min: Int, serid: String): Observable<RouterTimeoutBean>? {
        return NetworkFactory.getApi()
                .routerConfigWifi(macAddr, wifiAccount, pwd, hour, min, serid)
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 添加路由入网
     */
    fun routerAccessInNet(macAddr: String, hour: Int, min: Int, serid: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi()
                .routerAccessIn(macAddr, hour, min, serid)
                //.compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 绑定路由设备
     */
    fun bindRouter(meshAddrsList: MutableList<Int>, meshType: Int, macAddr: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi()
                .bindRouter(MeshListTypeMacBody(meshAddrsList, meshType, macAddr)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 解绑路由设备
     */
    fun unbindRouter(meshAddrsList: MutableList<Int>, meshType: Int): Observable<Response<Any>>? {
        val groupBlinkBodyBean = GroupBlinkBodyBean(meshAddrsList, meshType)
        return NetworkFactory.getApi()
                .unbindRouter(groupBlinkBodyBean).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 删除路由
     */
    fun delete(bodyBean: MacResetBody): Observable<Long>? {
        return NetworkFactory.getApi()
                .routerReset(bodyBean)
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {}
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 更新路由相关信息
     */
    fun updateRouter(it: DbRouter): Observable<Any>? {
        return NetworkFactory.getApi()
                .updateRouter(it.id, NameBody(it.name))
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {}
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由扫描结果
     * /.//正式服暂无
     */
    fun getRouteScanningResult(): Observable<RouteScanResultBean>? {
        return NetworkFactory.getApi().scanResult
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由停止扫描
     */
    fun routeStopScan(serid: String, scanSerId: Long): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeStopScanDevcie(serid, scanSerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由停止扫描告诉服务器清除数据
     */
    fun routeScanClear(): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().tellServerClearScanning()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由批量分组
     */
    fun routeBatchGp(targetGroupMeshAddr: Int, deviceMeshAddrs: List<Int>, meshType: Int, ser_id: String): Observable<Response<RouterBatchGpBean>>? {
        return NetworkFactory.getApi().routerBatchGp(GPAddrMeshListTypeSerBody(targetGroupMeshAddr, deviceMeshAddrs, meshType, ser_id))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
    }

    fun routeBatchGpNew(bodyBean: GroupBodyBean): Observable<Response<RouterBatchGpBean>>? {
        return NetworkFactory.getApi().routerBatchGpN(bodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeBatchGpBlink(bodyBean: GroupBlinkBodyBean): Observable<Response<RouterBatchGpBean>>? {
        return NetworkFactory.getApi().routerBatchGpBlink(bodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由开始扫描
     */
    fun routerStartScan(scanType: Int, ser_id: String): Observable<Response<ScanDataBean>>? {
        return NetworkFactory.getApi().routeScanDevcie(scanType, Constant.DEFAULT_MESH_FACTORY_NAME, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由开始扫描
     */
    fun routerStatus(): Observable<RouteStasusBean>? {
        return NetworkFactory.getApi().routerStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由删除群组
     */
    fun routerDelGp(body: RouterDelGpBody): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerDeleteGroup(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由添加场景
     */
    fun routeAddScene(bodyBean: SceneAddBodyBean): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerAddScene(bodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由更新场景
     */
    fun routeUpdateScene(sceneId: Long, actions: List<DbSceneActions>, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerUpdateScene(RouterUpDateSceneBody(sceneId, actions, ser_id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由删除场景
     */
    fun routeDelScene(sceneActionId: Int): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerDelScene(SceneIdBodyBean("delScene", sceneActionId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     *获取设备版本号
     */
    fun getDevicesVersion(meshAddrs: MutableList<Int>, meshType: Int, ser_id: String): Observable<Response<RouterVersionsBean>>? {
        val meshAddressTypeBody = MeshAddressTypeBody( meshType,meshAddrs, ser_id)
        return NetworkFactory.getApi().routerGetDevicesVersion(meshAddressTypeBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     *获取设备版本号  新版 类似扫描流程  http请求-》回复收到请求-》等待上传数据
     */
    fun getDevicesVersionNew(meshAddrs: MutableList<Int>, meshType: Int, ser_id: String): Observable<Response<RouterVersionsBean>>? {
        val meshAddressTypeBody = MeshAddressTypeBody( meshType,meshAddrs, ser_id)
        return NetworkFactory.getApi().routerGetDevicesVersionNew(meshAddressTypeBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     *路由升级 //0设备类型  1单灯  2群组  3 ota Router本身
     */
    fun toDevicesOTA(meshAddrs: MutableList<Int>, meshType: Int, currentTimeMillis: Long,op: Int,ser_id: String): Observable<Response<Any>>? {
        LogUtils.v("zcl-----------收到路由设备升级--mesh--$meshAddrs---类型---$meshType---时间$currentTimeMillis---来源-$op")
        return NetworkFactory.getApi().routerToDevicesOta(MeshAddressTypeTimeBody(meshType,meshAddrs,currentTimeMillis,op,ser_id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     *获取ota升级结果
     */
    fun routerOTAResult(page: Int, size: Int, time: Long): Observable<MutableList<RouterOTAResultBean>>? {
        return NetworkFactory.getApi().routerGetOTAResult(page, size, time)
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     *停止路由ota
     */
    fun routerStopOTA(time: Long,ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerStopOTA(ser_id, time)
               // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        /**
         * 添加自定义渐变
         */
    }

    fun routerAddGradient(bean: AddGradientBean): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerAddCustomGradient(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 更新自定义渐变
     */
    fun routerUpdateGradient(bean: UpdateGradientBean): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerUpdateCustomGradient(bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    /**
     * 更新自定义渐变名字与速度
     */
    fun routerUpdateGradientNameSpeed(id:Int,bean: UpdateGradientNameSpeedBean): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerUpdateGradientNameSpeed(id,bean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 删除自定义渐变
     */
    fun routerDelGradient(customBodyBean: DelGradientBodyBean): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerDelCustomGradient(customBodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 设备&组应用自定义渐变
     */
    fun routerApplyDiyGradient(id: Int, dstAddress: Int, devicetype: Int, serId: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerApplyDiyGradient(id, dstAddress, devicetype, serId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 设备&组应用自定义渐变
     */
    fun routerApplySystemGradient(id: Int, dstAddress: Int, devicetype: Int, speed: Int, serId: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerApplySystemGradient(id, dstAddress, devicetype, speed, serId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 直连开关或传感器meshType 开关 = 99 或 0x20 或 0x22 或 0x21 或 0x28 或 0x27 或 0x25 传感器 = 98 或 0x23 或 0x24
     * op	否	int	0连接，1断开。默认0
     */
    fun routerConnectSwOrSe(id: Long, meshType: Int,op:Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerConnectSwOrSensor(id.toInt(), meshType,op, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 配置普通开关
     */
    fun configNormalSw(id: Long, groupMeshAddr: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().configNormalSw(id.toInt(), groupMeshAddr, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * 配置双组开关
     */
    fun configDoubleSw(body: RouterListBody): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().configDoubleSw(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 配置场景开关
     */
    fun configSceneSw(id: Long, groupMeshAddrs: List<Int>, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().configSceneSw(SwSceneListBody(id.toInt(), groupMeshAddrs, ser_id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 配置场景开关
     */
    fun configEightSw(id: Long, keys: List<KeyBean>, ser_id: String, configSwitchType: Int): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().configEightSw(SwEightBody(id.toInt(), keys, ser_id,configSwitchType))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 配置传感器
     */
    fun configSensor(id: Long, configuration: ConfigurationBean, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().configSensor(ConfigSensorBody(id.toInt(), configuration, ser_id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 开关灯
     */
    fun routeOpenOrClose(meshAddr: Int, meshType: Int, status: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeOpenOrClose(meshAddr, meshType, status, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 停止自定义渐变
     */
    fun routeStopDynamic(meshAddr: Int, meshType: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerStopDynamic(meshAddr, meshType, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由开关传感器
     */
    fun routeSwitchSensor(id: Int, status: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerSwitchSensor(id, status, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * 用户复位
     * @Field("meshAddr") int ,@Field("meshType") int , @Field("controlCmd") int , @Field("value") int speedValue, @Field("ser_id") String ser_id)
     */
    fun routerUserReset(ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routerUserReset(RouterDelGpBody(ser_id, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 安全锁
     *
     */
    fun routeOpenOrCloseSafeLock(status: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeOpenOrCloseSafeLock(status,ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * 控制相关开始
     * 调节亮度
     */
    fun routeConfigBrightness(meshAddr: Int, meshType: Int, brightness: Int,isEnableBright:Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        LogUtils.v("zcl-----------调节开关亮度-------$isEnableBright----$brightness")
        return NetworkFactory.getApi().routeConfigBrightness(meshAddr, meshType, brightness,isEnableBright, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 调节色温
     */
    fun routeConfigColorTemp(meshAddr: Int, meshType: Int, colorTemperature: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeConfigColorTemp(meshAddr, meshType, colorTemperature, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 调节白色
     */
    fun routeConfigWhiteNum(meshAddr: Int, meshType: Int, color: Int,isEnableWhiteBright :Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeConfigWhiteNum(meshAddr, meshType, color,isEnableWhiteBright, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 调节rgb值
     */
    fun routeConfigRGBNum(meshAddr: Int, meshType: Int, color: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeConfigRGBNum(meshAddr, meshType, color, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始  int	1开 2关 (特别注意2才是关)
     * 开关缓起缓灭
     */
    fun routeSlowUpSlowDownSw(status: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeSlowUpSlowDownSwitch(status, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 调节缓起缓灭速度
     */
    fun routeSlowUpSlowDownSpeed(speed: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeSlowUpSlowDownSpeed(speed, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 控制相关开始
     * 恢复出厂设置  meshType	是	int	meshAddr类型  普通灯 = 4彩灯 = 6 蓝牙连接器 = 5
     * 开关 = 99 或 0x20 或 0x22 或 0x21 或 0x28 或 0x27 或 0x25 传感器 = 98 或 0x23 或 0x24 组 = 97 全部 = 100 窗帘 = 0x10
     * 不支持窗帘  meshType=97&meshAddr=65535时效果与meshType=100一致
     */
    fun routeResetFactory(bodyBean: MacResetBody): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeResetFactory(bodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 路由恢复自身 物理恢复
     */
    fun routeResetFactoryBySelf(bodyBean: MacResetBody): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeResetFactoryBySelf(bodyBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 获取路由版本号
     */
    fun routeGetRouterVersion(mac: Long, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeGetVersion(mac, ser_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 获取路由版本号
     */
    fun routeOtaRouter(mac: String, start: Long): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeOtaRouter(start,mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /*--------------------------------------控制相关开始---------------------------------------------------------*/
    /**
     * 恢复出厂设置  meshType	是	int	meshAddr类型  普通灯 = 4彩灯 = 6 蓝牙连接器 = 5
     * 开关 = 99 或 0x20 或 0x22 或 0x21 或 0x28 或 0x27 或 0x25 传感器 = 98 或 0x23 或 0x24 组 = 97 全部 = 100
     * 不支持窗帘  meshType=97&meshAddr=65535时效果与meshType=100一致
     */
    fun routeUpdateLightName(id: Long, name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateLight(id, NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateRelayName(id: Long, name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateLight(id, NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateSensorName(id: Long, name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateSensor(id, NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateCurtainName(id: Long, name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateLight(id, NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateSw(id: Long, name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateSwitch(id, NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun routeApplyScene(id: Long, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeApplyScene(id, ser_id)
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeControlCurtain(meshAddr: Int, meshType: Int, controlCmd: Int, value: Int, ser_id: String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeControlCurtain(meshAddr, meshType, controlCmd, value, ser_id)
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeAddTimerScene( name:String , hour:Int, min :Int, week:Int, sid :Int,status :Int, ser_id:String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeAddTimerScene(TimerSceneBody(0,name, hour, min , week, sid , status,ser_id))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun routeUpdateTimerScene( id:Int , hour:Int, min :Int, week:Int, sid :Int, ser_id:String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeUpdateTimerScene(TimerSceneBody(id,"", hour, min , week, sid ,0, ser_id))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun routeDelTimerScene( id:Int ,  ser_id:String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeDelTimerScene(IdSerIdBody(id,ser_id))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    //1开 0关
    fun routeTimerSceneStatus( id:Int , status:Int, ser_id:String): Observable<Response<RouterTimeoutBean>>? {
        return NetworkFactory.getApi().routeTimerSceneStatus(id,status,ser_id)
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeTimerSceneList(): Observable<MutableList<RouterTimerSceneBean>>? {
        return NetworkFactory.getApi().routeTimerSceneList()
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateSceneName(id:Int,name: String,imgName:String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateSceneName(id,NameBody(name,imgName))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun routeUpdateTimerSceneName(id:Int,name: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeUpdateTimerSceneName(id,NameBody(name))
                // .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun routeDelSelf(mac: String): Observable<Response<Any>>? {
        return NetworkFactory.getApi().routeDelSelf(mac)
                 //.compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun routeUpdateRouterName(id:Long,name: String): Observable<Any>? {
        return NetworkFactory.getApi().updateRouter(id,NameBody(name))
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun appThirdParty(): Observable<MutableList<ThirdPartyBean>>? {
        return NetworkFactory.getApi().appThirdParty()
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateToThirdParty(thirdPartyId:Int): Observable<Any>? {
        return NetworkFactory.getApi().updateToThirdParty(thirdPartyId)
                .compose(NetworkTransformer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}




