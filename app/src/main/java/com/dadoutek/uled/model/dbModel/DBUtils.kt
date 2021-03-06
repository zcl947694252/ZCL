package com.dadoutek.uled.model.dbModel

import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.dao.*
import com.dadoutek.uled.gateway.bean.DbGateway
import com.dadoutek.uled.gateway.bean.DbRouter
import com.dadoutek.uled.model.*
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.util.SharedPreferencesUtils

/**
 * Created by hejiajun on 2018/5/18.
 */

object DBUtils {
    private var lastClickTime: Long = 0
    val lastUser: DbUser?
        get() {
            val list = DaoSessionInstance.getInstance().dbUserDao.queryBuilder().orderDesc(DbUserDao.Properties.Id).list()
            return if (list.size == 0) {
                null
            } else list[0]
        }

    val lastRegion: DbRegion
        get() {
            val list = DaoSessionInstance.getInstance().dbRegionDao.queryBuilder().orderDesc(DbRegionDao.Properties.Id).list()
            return when {
                list.size > 0 -> list[0]
                else -> DbRegion()
            }
        }

    val allLight: List<DbLight>
        //普通等
        get() {
            val arrayList = ArrayList<DbLight>()
            arrayList.addAll(DaoSessionInstance.getInstance().dbLightDao.loadAll())
            return arrayList
        }


    val allCurtain: List<DbCurtain>
        //窗帘
        get() {
            val arrayList = ArrayList<DbCurtain>()
            arrayList.addAll(DaoSessionInstance.getInstance().dbCurtainDao.loadAll())
            return arrayList
        }


    val allRely: List<DbConnector>
        //蓝牙接收器
        get() {
            val arrayList = ArrayList<DbConnector>()
            arrayList.addAll(DaoSessionInstance.getInstance().dbConnectorDao.loadAll())
            return arrayList
        }

    val sceneList: MutableList<DbScene>
        get() {
            val qb = DaoSessionInstance.getInstance().dbSceneDao.queryBuilder()

            val list = qb.where(DbSceneDao.Properties.BelongRegionId.eq(SharedPreferencesUtils.getCurrentUseRegionId())).list()
            val arrayList = ArrayList<DbScene>()
            arrayList.addAll(list)
            return arrayList
        }

    val swtichList: MutableList<DbSwitch>
        //开关
        get() = DaoSessionInstance.getInstance().dbSwitchDao.loadAll()

    val eightSwitchList: MutableList<DbEightSwitch>
        get() = DaoSessionInstance.getInstance().dbEightSwitchDao.loadAll()


    val groupList: MutableList<DbGroup>
        get() {
            val allGIndex = -1
            val qb = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder()
            val list = qb.where(DbGroupDao.Properties.BelongRegionId.eq(SharedPreferencesUtils.getCurrentUseRegionId())).list()
            val arrayList = ArrayList<DbGroup>()
            arrayList.addAll(list)
            return arrayList
        }


    fun getgroupListWithType(context: Context): ArrayList<ItemTypeGroup> {
        var itemTypeGroup: ItemTypeGroup? = null

        val allList = ArrayList<ItemTypeGroup>()

        val listAll = getAllGroupsOrderByIndex()
        val normalList = ArrayList<DbGroup>()
        val allLightList = ArrayList<DbGroup>()
        val rgbList = ArrayList<DbGroup>()
        val curtainList = ArrayList<DbGroup>()
        val otherList = ArrayList<DbGroup>()
        val connectorList = ArrayList<DbGroup>()

        for (group in listAll) {
            when (group.deviceType) {
                Constant.DEVICE_TYPE_DEFAULT_ALL -> {
                    otherList.add(group)
                }
                Constant.DEVICE_TYPE_LIGHT_NORMAL -> {
                    normalList.add(group)
                }
                Constant.DEVICE_TYPE_LIGHT_RGB -> {
                    rgbList.add(group)
                }
                Constant.DEVICE_TYPE_CURTAIN -> {
                    curtainList.add(group)
                }
                Constant.DEVICE_TYPE_NO -> {
                    allLightList.add(group)
                }
                Constant.DEVICE_TYPE_CONNECTOR -> {
                    connectorList.add(group)
                }
                else -> {
                    otherList.add(group)
                }
            }
        }

        if (allLightList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.allLight), allLightList, R.drawable.icon_light_on)
            allList.add(itemTypeGroup)
        }

        if (normalList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.normal_light), normalList, R.drawable.icon_light_on)
            allList.add(itemTypeGroup)
        }

        if (rgbList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.rgb_light), rgbList, R.drawable.icon_light_on)
            allList.add(itemTypeGroup)
        }

        if (curtainList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.curtain), curtainList, R.drawable.curtain_on)
            allList.add(itemTypeGroup)
        }

        if (otherList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.not_type), otherList)
            allList.add(itemTypeGroup)
        }

        if (connectorList.size > 0) {
            itemTypeGroup = ItemTypeGroup(context.getString(R.string.relay), connectorList, R.drawable.curtain_on)
            allList.add(itemTypeGroup)
        }
        return allList
    }


    val diyGradientList: MutableList<DbDiyGradient>
        get() {
            val qb = DaoSessionInstance.getInstance().dbDiyGradientDao.queryBuilder()
            return qb.list()
        }

    //未分组
    val groupNull: DbGroup?
        get() {
            val qb = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder()
            val list = qb.where(
                    DbGroupDao.Properties.BelongRegionId.eq(SharedPreferencesUtils.getCurrentUseRegionId()))
                    .list()

            for (i in list.indices) {
                if (list[i].meshAddr == 0xffff) {
                    return list[i]
                }
            }
            return null
        }

    val sceneAll: List<DbScene>
        get() = DaoSessionInstance.getInstance().dbSceneDao.loadAll()

    val regionAll: List<DbRegion>
        get() = DaoSessionInstance.getInstance().dbRegionDao.loadAll()

    val dataChangeAll: List<DbDataChange>
        get() = DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()

    fun getdataChangeAll(): List<DbDataChange> {
        return DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()
    }

    val dataChangeAllHaveAboutLight: Boolean
        get() {
            val list = DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()
            for (i in list.indices) {
                if (list[i].tableName == "DB_LIGHT") {
                    return true
                }
            }
            return false
        }

    val dataChangeAllHaveAboutCurtain: Boolean
        get() {
            val list = DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()
            for (i in list.indices) {
                if (list[i].tableName == "DB_CURTAIN") {
                    return true
                }
            }
            return false
        }

    val dataChangeAllHaveAboutRelay: Boolean
        get() {
            val list = DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()
            for (i in list.indices) {
                if (list[i].tableName == "DB_CONNECTOR") {
                    return true
                }
            }
            return false
        }

    val allGroups: MutableList<DbGroup>
        get() = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().list()

    val deleteGroups: List<DbDeleteGroup>
        get() = DaoSessionInstance.getInstance().dbDeleteGroupDao.queryBuilder().list()

    val allGroupsExceptCurtain: MutableList<DbGroup>
        get() = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().where(DbLightDao.Properties.ProductUUID.notEq(DeviceType.SMART_CURTAIN)).list()


    private//去掉所有组，避免影响判断
    val groupAdress: Int
        get() {
            val list = DBUtils.groupList
            val idList = ArrayList<Int>()
            for (i in list.indices.reversed()) {
                if (list[i].meshAddr == 0xffff) {
                    list.removeAt(i)
                }
            }

            for (i in list.indices) {
                idList.add(list[i].meshAddr)
            }

            var id = 0
            for (i in 0x8001..33023) {
                if (idList.contains(i)) {
//                    Log.d("sceneID", "getSceneId: " + "aaaaa")
                    continue
                } else {
                    id = i
//                    Log.d("sceneID", "getSceneId: bbbbb$id")
                    break
                }
            }

            if (list.size == 0) {
                id = 0x8001
            }

            return id
        }

    /********************************************查询 */

    fun getAllUser(): ArrayList<DbUser> {
        val query = DaoSessionUser.getInstance().dbUserDao.queryBuilder().build()
        var arrayList: ArrayList<DbUser> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllRGBLight(): ArrayList<DbLight> {
        val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.ProductUUID.eq(DeviceType.LIGHT_RGB)).build()
        var arrayList: ArrayList<DbLight> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllNormalLight(): ArrayList<DbLight> {
        val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder()
                .whereOr(DbLightDao.Properties.ProductUUID.eq(DeviceType.LIGHT_NORMAL_OLD), DbLightDao.Properties.ProductUUID.eq(DeviceType.LIGHT_NORMAL)).build()

        var arrayList: ArrayList<DbLight> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllRouter(): ArrayList<DbRouter> {
        val query = DaoSessionInstance.getInstance().dbRouterDao.queryBuilder()
                .where(DbRouterDao.Properties.ProductUUID.eq(DeviceType.ROUTER)).build()

        var arrayList: ArrayList<DbRouter> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllGateWay(): ArrayList<DbGateway> {
        val query = DaoSessionInstance.getInstance().dbGatewayDao.queryBuilder()
                .where(DbGatewayDao.Properties.ProductUUID.eq(DeviceType.GATE_WAY)).build()

        var arrayList: ArrayList<DbGateway> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllSwitch(): ArrayList<DbSwitch> {
        val query = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder()
                .whereOr(DbSwitchDao.Properties.ProductUUID.eq(DeviceType.NORMAL_SWITCH)
                        , DbSwitchDao.Properties.ProductUUID.eq(DeviceType.NORMAL_SWITCH2)
                        , DbSwitchDao.Properties.ProductUUID.eq(DeviceType.SCENE_SWITCH)
                        , DbSwitchDao.Properties.ProductUUID.eq(DeviceType.DOUBLE_SWITCH)
                        , DbSwitchDao.Properties.ProductUUID.eq(DeviceType.EIGHT_SWITCH)
                        , DbSwitchDao.Properties.ProductUUID.eq(DeviceType.SMART_CURTAIN_SWITCH)).build()

        var arrayList: ArrayList<DbSwitch> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllSensor(): ArrayList<DbSensor> {
        val query = DaoSessionInstance.getInstance().dbSensorDao.queryBuilder()
                .whereOr(DbSensorDao.Properties.ProductUUID.eq(DeviceType.NIGHT_LIGHT)
                        , DbSensorDao.Properties.ProductUUID.eq(DeviceType.SENSOR)).build()

        var arrayList: ArrayList<DbSensor> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllCurtains(): ArrayList<DbCurtain> {
        val query = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder()
                .where(DbCurtainDao.Properties.ProductUUID.eq(DeviceType.SMART_CURTAIN)).build()

        var arrayList: ArrayList<DbCurtain> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getAllRelay(): ArrayList<DbConnector> {
        val query = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder()
                .where(DbConnectorDao.Properties.ProductUUID.eq(DeviceType.SMART_RELAY)).build()

        var arrayList: ArrayList<DbConnector> = arrayListOf()
        arrayList.addAll(query.list())
        return arrayList
    }

    fun getActionBySwitchId(id: Long): ArrayList<DbSwitch> {
        val query = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder().where(DbSwitchDao.Properties.BelongGroupId.eq(id)).build()
        return ArrayList(query.list())
    }

    fun getActionsBySceneId(id: Long): ArrayList<DbSceneActions> {
        val eq = DbSceneActionsDao.Properties.BelongSceneId.eq(id)
        val query = DaoSessionInstance.getInstance().dbSceneActionsDao.queryBuilder().where(eq).build()
        return ArrayList(query.list())
    }

    fun getActionBySceneId(id: Long, address: Int): DbSceneActions? {
        val query = DaoSessionInstance.getInstance().dbSceneActionsDao.queryBuilder().where(DbSceneActionsDao.Properties.BelongSceneId.eq(id)).build()
        val list = ArrayList(query.list())
        for (i in list.indices) {
            if (list[i].groupAddr == address) {
                return list[i]
            }
        }
        return null
    }

    fun getColorNodeListByDynamicModeId(id: Long): ArrayList<DbColorNode> {
        val query = DaoSessionInstance.getInstance().dbColorNodeDao.queryBuilder().where(DbColorNodeDao.Properties.BelongDynamicChangeId.eq(id)).build()
        return ArrayList(query.list())
    }

    fun getGroupNameByID(id: Long?, context: Context): String {
        val group = DaoSessionInstance.getInstance().dbGroupDao.load(id)
        if (group != null) {
            return group.name
        }
        return context.getString(R.string.not_grouped)
    }


    fun getGroupByID(id: Long): DbGroup? {
        return DaoSessionInstance.getInstance().dbGroupDao.load(id)
    }

    fun getUserPhone(phone: String): DbUser? {
        val dbUser = DaoSessionUser.getInstance().dbUserDao.queryBuilder().where(DbUserDao.Properties.Phone.eq(phone)).unique()
        if (dbUser != null) {
            return dbUser
        }
        return null
    }

    fun getLightMesAddr(mesAddr: Int): DbLight? {
        return DaoSessionInstance.getInstance().dbLightDao.load(mesAddr.toLong())
    }

    fun getCurtainMesAddr(mesAddr: Int): DbCurtain? {
        return DaoSessionInstance.getInstance().dbCurtainDao.load(mesAddr.toLong())
    }

    fun getRouterByID(id: Long): DbRouter? {
        return DaoSessionInstance.getInstance().dbRouterDao.load(id)
    }

    fun getRouterByMac(macAddr: String): MutableList<DbRouter>? {
        return DaoSessionInstance.getInstance().dbRouterDao.queryBuilder()
                .where(DbRouterDao.Properties.MacAddr.eq(macAddr)).list()
    }

    fun getGatewayByID(id: Long): DbRouter? {
        return DaoSessionInstance.getInstance().dbRouterDao.load(id)
    }

    fun getConnectorByID(id: Long): DbConnector? {
        return DaoSessionInstance.getInstance().dbConnectorDao.load(id)
    }

    fun getConnectorByMac(mac: String): DbConnector? {
        val list = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder()
                .where(DbConnectorDao.Properties.MacAddr.eq(mac)).list()
        return if (list.size > 0) list[0] else null
    }

    fun getLightByID(id: Long): DbLight? {
        return DaoSessionInstance.getInstance().dbLightDao.load(id)
    }

    fun getLightByMac(mac: String): DbLight? {
        val list = DaoSessionInstance.getInstance().dbLightDao.queryBuilder()
                .where(DbLightDao.Properties.MacAddr.eq(mac)).list()
        return if (list.size > 0) list[0] else null
    }

    fun getCurtainByID(id: Long): DbCurtain? {
        return DaoSessionInstance.getInstance().dbCurtainDao.load(id)
    }

    fun getCurtainByMac(mac: String): DbCurtain? {
        val list = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder()
                .where(DbCurtainDao.Properties.MacAddr.eq(mac)).list()
        return if (list.size > 0) list[0] else null
    }

    fun getSwitchByID(id: Long): DbSwitch? {
        return DaoSessionInstance.getInstance().dbSwitchDao.load(id)
    }

    fun getSensorByID(id: Long): DbSensor? {
        return DaoSessionInstance.getInstance().dbSensorDao.load(id)
    }

    fun getRegionByID(id: Long): DbRegion {
        return DaoSessionInstance.getInstance().dbRegionDao.load(id)
    }


    fun getSceneByID(id: Long): DbScene? {
        return DaoSessionInstance.getInstance().dbSceneDao.load(id)
    }

    fun getGradientByID(id: Long): DbDiyGradient? {
        return DaoSessionInstance.getInstance().dbDiyGradientDao.load(id)
    }

    fun getSceneActionsByID(id: Long): DbSceneActions {
        return DaoSessionInstance.getInstance().dbSceneActionsDao.load(id)
    }


    fun getUserByID(id: Long): DbUser {
        return DaoSessionInstance.getInstance().dbUserDao.load(id)
    }

    fun getEightSwitchByID(id: Long): DbEightSwitch? {
        return DaoSessionInstance.getInstance().dbEightSwitchDao.load(id)
    }

    fun getGatewayByMeshAddr(meshAddr: Int): DbGateway? {
        val dbGatewayList = DaoSessionInstance.getInstance().dbGatewayDao.queryBuilder()
                .where(DbGatewayDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbGatewayList.size > 0) {
            dbGatewayList[0]
        } else null
    }

    fun getLightByMeshAddr(meshAddr: Int): DbLight? {
        val dbLightList = DaoSessionInstance.getInstance().dbLightDao.queryBuilder()
                .where(DbLightDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbLightList.size > 0) {
            dbLightList[0]
        } else null
    }

    fun getCurtainByMeshAddr(meshAddr: Int): DbCurtain? {
        val dbCurtianList = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder()
                .where(DbCurtainDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbCurtianList.size > 0) {
            //            for(int i=0;i<dbLightList.size();i++){
            ////                Log.d("DataError", "getLightByMeshAddr: "+dbLightList.get(i).getMeshAddr()+);
            //            }
            dbCurtianList[0]
        } else null
    }

    fun getConnectorByMeshAddr(meshAddr: Int): DbConnector? {
        val dbCurtianList = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder()
                .where(DbCurtainDao.Properties.MeshAddr.eq(meshAddr)).list()
        return when {
            dbCurtianList.size <= 0 -> null
            else -> dbCurtianList[0]
        }
    }


    fun getEightSwitchByMeshAddr(meshAddr: Int): DbEightSwitch? {
        val dbEightSwitchList = DaoSessionInstance.getInstance().dbEightSwitchDao.queryBuilder()
                .where(DbEightSwitchDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbEightSwitchList.size > 0) {
            dbEightSwitchList[0]
        } else null
    }


    fun getRelyByMeshAddr(meshAddr: Int): DbConnector? {
        val dbRelyList = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder()
                .where(DbConnectorDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbRelyList.size > 0) {
            dbRelyList[0]
        } else null
    }


    fun getEightSwitchByMachAddr(machAddr: String): DbEightSwitch? {
        val dbEightSwitchList = DaoSessionInstance.getInstance().dbEightSwitchDao.queryBuilder()
                .where(DbEightSwitchDao.Properties.MacAddr.eq(machAddr)).list()
        return if (dbEightSwitchList.size > 0) {
            dbEightSwitchList[0]
        } else null
    }


    fun getSwitchByMacAddr(macAddr: String): DbSwitch? {
        val dbLightList = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder()
                .where(DbSwitchDao.Properties.MacAddr.eq(macAddr)).list()
        return if (dbLightList.size > 0) {
            //            for(int i=0;i<dbLightList.size();i++){
            ////                Log.d("DataError", "getLightByMeshAddr: "+dbLightList.get(i).getMeshAddr()+);
            //            }
            dbLightList[0]
        } else null
    }


    /**
     * 判断这个mesh地址的设备是不是存在与数据库
     */
    fun isDeviceExist(meshAddr: Int): Boolean {
        return getLightByMeshAddr(meshAddr) != null || getRelyByMeshAddr(meshAddr) != null || getCurtainByMeshAddr(meshAddr) != null
                || getSwitchByMeshAddr(meshAddr) != null || getSensorByMeshAddr(meshAddr) != null || getGatewayByMeshAddr(meshAddr) != null
    }

    fun getSwitchByMeshAddr(meshAddr: Int): DbSwitch? {
        val dbLightList = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder().where(DbSwitchDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (dbLightList.size > 0) {
            dbLightList[0]
        } else null
    }

    fun getSensorByMacAddr(macAddr: String): DbSensor? {
        val dbLightList = DaoSessionInstance.getInstance().dbSensorDao.queryBuilder().where(DbSensorDao.Properties.MacAddr.eq(macAddr)).list()
        return if (dbLightList.size > 0) {
            //            for(int i=0;i<dbLightList.size();i++){
            ////                Log.d("DataError", "getLightByMeshAddr: "+dbLightList.get(i).getMeshAddr()+);
            //            }
            dbLightList[0]
        } else null
    }

    fun getLightListByMeshAddr(meshAddr: Int): MutableList<DbLight>? {
        val dbLightList = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.MeshAddr.eq(meshAddr)).list()
        return dbLightList
    }


    fun getGroupByMeshAddr(mesh: String): DbGroup {
        val dbGroup = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().where(DbGroupDao.Properties.MeshAddr.eq(mesh)).unique()
//        Log.d("datasave", "getGroupByMeshAddr: $mesh")
        return dbGroup
    }

    fun getGroupByName(name: String): DbGroup {
        val dbGroup = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().where(DbGroupDao.Properties.Name.eq(name)).unique()
//        Log.d("datasave", "getGroupByMeshAddr: $name")
        return dbGroup
    }

    fun getGroupsByDeviceType(vararg types: Int): MutableList<DbGroup> {
        val groups = mutableListOf<DbGroup>()
        for (type in types) {
            val group = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().where(DbGroupDao.Properties.DeviceType.eq(type)).list()
            groups.addAll(group)

        }
        return groups
    }

    fun getAllGroupsOrderByIndex(): MutableList<DbGroup> {
        val dbSwitches = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().orderAsc(DbGroupDao.Properties.Index).list()
        return dbSwitches
    }

    fun getSwtitchesByProductUUID(uuid: Int): MutableList<DbSwitch> {
        val dbSwitches = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder().where(DbSwitchDao.Properties.ProductUUID.eq(uuid)).list()
        return dbSwitches
    }

    fun getCurtainName(name: String): MutableList<DbCurtain> {
        val dbCurtain = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder().where(DbSwitchDao.Properties.Name.eq(name)).list()
        return dbCurtain
    }


    /**
     * 一个mesh地址对应一个组
     */
    fun getGroupByMeshAddr(mesh: Int): DbGroup {
        var dbGroup = DbGroup()
        dbGroup.id = 0L
        val dbGroupLs = DaoSessionInstance.getInstance().dbGroupDao.queryBuilder().where(DbGroupDao.Properties.MeshAddr.eq(mesh)).list()
//        Log.d("datasave", "getGroupByMeshAddr: $mesh")
        if (dbGroupLs.size > 0) {
            dbGroup = dbGroupLs[0]
        }
        return dbGroup  //如果获取不到所有灯这个组，就直接创建一个返回
    }


    fun getLightMeshAddr(meshAddr: Int): ArrayList<DbLight> {
        val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.MeshAddr.eq(meshAddr)).build()
        return ArrayList(query.list())
    }

    fun getCurtainMeshAddr(meshAddr: Int): ArrayList<DbCurtain> {
        val query = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder().where(DbCurtainDao.Properties.MeshAddr.eq(meshAddr)).build()
        return ArrayList(query.list())
    }

    //查询改组内设备数量
    fun getLightByGroupID(id: Long): ArrayList<DbLight> {
        val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.BelongGroupId.eq(id)).build()
        return ArrayList(query.list())
    }

    fun getRelayByGroupID(id: Long): ArrayList<DbConnector> {
        val query = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder().where(DbConnectorDao.Properties.BelongGroupId.eq(id)).build()
        return ArrayList(query.list())
    }

    fun getLightByGroupName(name: String): ArrayList<DbLight> {
        val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.Name.eq(name)).build()
        return ArrayList(query.list())
    }

    fun getCurtainByGroupMesh(mesh: Int): ArrayList<DbCurtain> {
        val group = getGroupByMeshAddr(mesh)
        if (group != null) {
            val query = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder().where(DbCurtainDao.Properties.BelongGroupId.eq(group.id)).build()
            return ArrayList(query.list())
        }
        return ArrayList()
    }

    fun getCurtainByGroupID(id: Long): ArrayList<DbCurtain> {
        val query = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder().where(DbCurtainDao.Properties.BelongGroupId.eq(id)).build()
        return ArrayList(query.list())
    }

    fun getLightByGroupMesh(mesh: Int): ArrayList<DbLight>? {
        val group = getGroupByMeshAddr(mesh)
        if (group != null) {
            val query = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.BelongGroupId.eq(group.id)).build()
            return ArrayList(query.list())
        }
        return ArrayList()
    }

    fun getRelayByGroupMesh(mesh: Int): ArrayList<DbConnector>? {
        val group = getGroupByMeshAddr(mesh)
        if (group != null) {
            val query = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder().where(DbConnectorDao.Properties.BelongGroupId.eq(group.id)).build()
            return ArrayList(query.list())
        }
        return ArrayList()
    }

    /********************************************保存 */


    fun saveRegion(dbRegion: DbRegion, isFromServer: Boolean) {
        if (isFromServer) {
            val dbRegionOld = DaoSessionInstance.getInstance().dbRegionDao.queryBuilder().where(DbRegionDao.Properties.ControlMesh.eq(dbRegion.controlMesh)).list()
            if (dbRegionOld.isEmpty()) {
                DaoSessionInstance.getInstance().dbRegionDao.insert(dbRegion)
            }
        } else {
            //判断原来是否保存过这个区域
            val dbRegionOld = DaoSessionInstance.getInstance().dbRegionDao.queryBuilder().where(DbRegionDao.Properties.ControlMesh.eq(dbRegion.controlMesh)).list()
            if (dbRegionOld.isEmpty()) {//直接插入
                DaoSessionInstance.getInstance().dbRegionDao.insert(dbRegion)
                //暂时用本地保存区域
                SharedPreferencesUtils.saveCurrentUseRegionID(dbRegion.id)

                recordingChange(dbRegion.id,
                        DaoSessionInstance.getInstance().dbRegionDao.tablename,
                        Constant.DB_ADD)
                //创建新区域首先创建一个所有灯的分组
                createAllLightControllerGroup()
            } else {//更新数据库
                dbRegion.id = dbRegionOld[0].id
                DaoSessionInstance.getInstance().dbRegionDao.update(dbRegion)
                //暂时用本地保存区域
                SharedPreferencesUtils.saveCurrentUseRegionID(dbRegion.id)
                recordingChange(dbRegion.id,
                        DaoSessionInstance.getInstance().dbRegionDao.tablename,
                        Constant.DB_UPDATE)
            }
        }
    }

    fun deleteRegion(dbRegion: DbRegion) {
        DaoSessionInstance.getInstance().dbRegionDao.delete(dbRegion)
        recordingChange(dbRegion.id,
                DaoSessionInstance.getInstance().dbRegionDao.tablename,
                Constant.DB_DELETE)
    }


    fun saveGroup(group: DbGroup, isFromServer: Boolean) {
        if (group.meshAddr == 0)
            return

        when {
            isFromServer -> {
                DaoSessionInstance.getInstance().dbGroupDao.insertOrReplace(group)
            }
            else -> {
                DaoSessionInstance.getInstance().dbGroupDao.insertOrReplace(group)
                recordingChange(group.id,
                        DaoSessionInstance.getInstance().dbGroupDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }


    fun saveLight(db: DbLight, isFromServer: Boolean) {
        val existList = DaoSessionInstance.getInstance().dbLightDao.queryBuilder().where(DbLightDao.Properties.MeshAddr.eq(db.meshAddr)).list()
        if (existList.size > 0) {
            //如果该mesh地址的数据已经存在，就直接修改
            db.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbLightDao.insertOrReplace(db)

        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbLightDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbLightDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }


    fun saveSensor(sensor: DbSensor, isFromServer: Boolean) {
        val existList = DaoSessionInstance.getInstance().dbSensorDao.queryBuilder().where(DbSensorDao.Properties.MeshAddr.eq(sensor.meshAddr)).list()
        if (existList.size > 0 /*&& existList[0].macAddr == sensor.macAddr*/) {
            //如果该mesh地址的数据已经存在，就直接修改 mes一致则判断mac
            sensor.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbSensorDao.insertOrReplace(sensor)

        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(sensor.id,
                        DaoSessionInstance.getInstance().dbSensorDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(sensor.id,
                        DaoSessionInstance.getInstance().dbSensorDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }


    fun saveSwitch(db: DbSwitch, isFromServer: Boolean, type: Int = 3, keys: String = "[]") {
        val existList = DaoSessionInstance.getInstance().dbSwitchDao.queryBuilder().where(DbSwitchDao.Properties.MeshAddr.eq(db.meshAddr)).list()
        if (existList.size > 0 && existList[0].macAddr == db.macAddr) {//
            //如果该mesh地址的数据已经存在，就直接修改
            db.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbSwitchDao.insertOrReplace(db)

        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id, DaoSessionInstance.getInstance().dbSwitchDao.tablename, Constant.DB_UPDATE, type, keys)
            } else {
                recordingChange(db.id, DaoSessionInstance.getInstance().dbSwitchDao.tablename, Constant.DB_ADD, type, keys)
            }
        }
    }

    fun saveGateWay(db: DbGateway, isFromServer: Boolean) {//添加mesh以后要改成mes地址
        val existList = DaoSessionInstance.getInstance().dbGatewayDao.queryBuilder().where(DbGatewayDao.Properties.MeshAddr.eq(db.meshAddr)).list()

        //如果该mesh地址的数据已经存在，就直接修改
        if (existList.size > 0 && existList[0].macAddr == db.macAddr)
            db.id = existList[0].id

        DaoSessionInstance.getInstance().dbGatewayDao.insertOrReplace(db)
        LogUtils.v("zcl-----------保存网关-------${getAllGateWay()}")
        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbGatewayDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbGatewayDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }

    fun saveRouter(db: DbRouter, isFromServer: Boolean) {
        val existList = DaoSessionInstance.getInstance().dbRouterDao.queryBuilder().where(DbRouterDao.Properties.Id.eq(db.id)).list()
        if (existList.size > 0 && existList[0].macAddr == db.macAddr) {//
            //如果该mesh地址的数据已经存在，就直接修改
            db.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbRouterDao.insertOrReplace(db)
        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            when {
                existList.size > 0 -> recordingChange(db.id, DaoSessionInstance.getInstance().dbRouterDao.tablename, Constant.DB_UPDATE)
                else -> recordingChange(db.id, DaoSessionInstance.getInstance().dbRouterDao.tablename, Constant.DB_ADD)
            }
        }
    }

    fun saveEightSwitch(db: DbEightSwitch, isFromServer: Boolean) {
        val existList = DaoSessionInstance.getInstance().dbEightSwitchDao.queryBuilder().where(DbEightSwitchDao.Properties.MeshAddr.eq(db.meshAddr)).list()

        if (existList.size > 0 && existList[0].macAddr == db.macAddr) {//
            //如果该mesh地址的数据已经存在，就直接修改
            db.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbEightSwitchDao.insertOrReplace(db)

        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbEightSwitchDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbEightSwitchDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }

    fun saveCurtain(db: DbCurtain, isFromServer: Boolean) {
        LogUtils.e("zcl保存分组前curtain-------------${DBUtils.getAllCurtains().size}")
        val existList = DaoSessionInstance.getInstance().dbCurtainDao.queryBuilder().where(DbCurtainDao.Properties.MeshAddr.eq(db.meshAddr)).list()
        if (existList.size > 0) {
            //如果该mesh地址的数据已经存在，就直接修改
            db.id = existList[0].id
        }

        DaoSessionInstance.getInstance().dbCurtainDao.insertOrReplace(db)
        LogUtils.e("zcl保存分组后curtain-------------${DBUtils.getAllCurtains().size}")
        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbCurtainDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbCurtainDao.tablename,
                        Constant.DB_ADD)

            }
        }

    }

    fun saveConnector(db: DbConnector, isFromServer: Boolean = false) {

        val existList = DaoSessionInstance.getInstance().dbConnectorDao.queryBuilder().where(DbConnectorDao.Properties.MeshAddr.eq(db.meshAddr)).list()
        if (existList.size > 0) {
            //如果该mesh地址的数据已经存在，就直接修改已存在的数据
            db.id = existList[0].id
        }
        DaoSessionInstance.getInstance().dbConnectorDao.insertOrReplace(db)

        //不是从服务器下载下来的，才需要把变化写入数据变化表
        if (!isFromServer) {
            if (existList.size > 0) {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbConnectorDao.tablename,
                        Constant.DB_UPDATE)
            } else {
                recordingChange(db.id,
                        DaoSessionInstance.getInstance().dbConnectorDao.tablename,
                        Constant.DB_ADD)
            }
        }
    }

    fun oldToNewSaveLight(light: DbLight) {
        DaoSessionInstance.getInstance().dbLightDao.insertOrReplace(light)
        recordingChange(light.id,
                DaoSessionInstance.getInstance().dbLightDao.tablename,
                Constant.DB_ADD)
    }

    fun saveUser(dbUser: DbUser) {
        DaoSessionInstance.getInstance().dbUserDao.insertOrReplace(dbUser)
//        LogUtils.v("zcl-0--------------"+ getAllUser())
        recordingChange(dbUser.id,
                DaoSessionInstance.getInstance().dbUserDao.tablename,
                Constant.DB_ADD)
    }

    fun saveUserDao(dbUser: DbUser) {
        //insertOrReplace有数据就更新该数据 没有就直接插入
        DaoSessionUser.getInstance().dbUserDao.insertOrReplace(dbUser)
    }

    fun saveScene(dbScene: DbScene, isFromServer: Boolean) {

        DaoSessionInstance.getInstance().dbSceneDao.insertOrReplace(dbScene)
        if (!isFromServer)
            recordingChange(dbScene.id, DaoSessionInstance.getInstance().dbSceneDao.tablename, Constant.DB_ADD)

    }

    fun saveSceneActions(sceneActions: DbSceneActions) {
        DaoSessionInstance.getInstance().dbSceneActionsDao.insertOrReplace(sceneActions)
        recordingChange(sceneActions.id,
                DaoSessionInstance.getInstance().dbSceneActionsDao.tablename,
                Constant.DB_ADD)
    }

    fun saveGradient(dbDiyGradient: DbDiyGradient, isFromServer: Boolean) {
        /*    if (isFromServer) {
                DaoSessionInstance.getInstance().dbDiyGradientDao.insert(dbDiyGradient)
            } else {*/
        DaoSessionInstance.getInstance().dbDiyGradientDao.insertOrReplace(dbDiyGradient)
        if (!isFromServer)
            recordingChange(dbDiyGradient.id,
                    DaoSessionInstance.getInstance().dbDiyGradientDao.tablename,
                    Constant.DB_ADD)
        //}
    }


    fun saveColorNode(colorNode: DbColorNode) {
        DaoSessionInstance.getInstance().dbColorNodeDao.insertOrReplace(colorNode)
        //        recordingChange(sceneActions.getId(),
        //                DaoSessionInstance.getApp().getDbSceneActionsDao().getTablename(),
        //                Constant.DB_ADD);
    }

    fun saveSceneActions(sceneActions: DbSceneActions, sceneId: Long) {
        val actions = DbSceneActions()
        actions.belongSceneId = sceneId
        actions.brightness = sceneActions.brightness
        actions.colorTemperature = sceneActions.colorTemperature
        actions.groupAddr = sceneActions.groupAddr
        actions.color = sceneActions.color
        actions.deviceType = sceneActions.deviceType
        actions.isOn = sceneActions.isOn
        actions.isEnableBright = sceneActions.isEnableBright
        actions.isEnableWhiteBright = sceneActions.isEnableWhiteBright
        actions.rgbType = sceneActions.rgbType
        actions.gradientId = sceneActions.gradientId
        actions.gradientType = sceneActions.gradientType
        actions.gradientName = sceneActions.gradientName
        actions.gradientSpeed = sceneActions.gradientSpeed

        DaoSessionInstance.getInstance().dbSceneActionsDao.insertOrReplace(actions)
    }

    fun saveColorNodes(colorNode: DbColorNode, id: Long?, gradientId: Long) {
        val colorNodeNew = DbColorNode()
        colorNodeNew.belongDynamicChangeId = gradientId
        colorNodeNew.index = colorNode.index
        colorNodeNew.brightness = colorNode.brightness
        colorNodeNew.colorTemperature = colorNode.colorTemperature
        colorNodeNew.rgbw = colorNode.rgbw
        //getColorNodeListByIndex()
        DaoSessionInstance.getInstance().dbColorNodeDao.insertOrReplace(colorNodeNew)
    }

    private fun getColorNodeListByIndex(index: Int): ArrayList<DbColorNode> {
        val query = DaoSessionInstance.getInstance().dbColorNodeDao.queryBuilder().where(DbColorNodeDao.Properties.Index.eq(index)).build()
        return ArrayList(query.list())
    }

    /********************************************更改 */


    @Deprecated("")
    fun updateGroup(group: DbGroup) {
        saveGroup(group, false)
//        DaoSessionInstance.getInstance().dbGroupDao.update(group)
//        recordingChange(group.id,
//                DaoSessionInstance.getInstance().dbGroupDao.tablename,
//                Constant.DB_UPDATE)
    }

    fun updateGroupList(groups: MutableList<DbGroup>) {
//        DaoSessionInstance.getInstance().dbGroupDao.updateInTx(groups)
        for (group in groups) {
            saveGroup(group, false)
        }
    }

    @Deprecated("Use saveRouter()")
    fun updateRouter(router: DbRouter) {
        saveRouter(router, false)
    }

    @Deprecated("Use saveGateWay()")
    private fun updateGate(gateway: DbGateway) {
        saveGateWay(gateway, false)
    }

    @Deprecated("Use saveLight()")
    fun updateLight(light: DbLight) {
        saveLight(light, false)
    }

    @Deprecated("Use saveCurtain()")
    fun updateCurtain(curtain: DbCurtain) {
        saveCurtain(curtain, false)
    }

    @Deprecated("Use saveSwitch()")
    fun updateSwicth(switch: DbSwitch) {
        saveSwitch(switch, false)
    }

    @Deprecated("Use saveEightSwitch()")
    fun updateEightSwicth(switch: DbEightSwitch) {
        saveEightSwitch(switch, false)
    }

    @Deprecated("use saveConnector()")
    fun updateConnector(connector: DbConnector) {
        saveConnector(connector, false)
    }

    @Deprecated("")
    fun updateLightLocal(light: DbLight) {
        saveLight(light, false)
    }

    @Deprecated("")
    fun updateRelayLocal(relay: DbConnector) {
//        DaoSessionInstance.getInstance().dbConnectorDao.update(relay)
        saveConnector(relay, false)
    }


    @Deprecated("")
    fun updateScene(scene: DbScene) {
        saveScene(scene, false)
//        DaoSessionInstance.getInstance().dbSceneDao.update(scene)
//        recordingChange(scene.id,
//                DaoSessionInstance.getInstance().dbSceneDao.tablename,
//                Constant.DB_UPDATE)
    }


    @Deprecated("")
    fun updateGradient(dbDiyGradient: DbDiyGradient) {
        saveGradient(dbDiyGradient, false)
    }


    fun updateDbchange(change: DbDataChange) {
        DaoSessionInstance.getInstance().dbDataChangeDao.update(change)
    }

    /********************************************删除 */

    /**
     * 只删除分组，不动组里的灯
     * @param dbGroup
     */

    fun deleteGroupOnly(dbGroup: DbGroup) {
        //        saveDeleteGroup(dbGroup);
        DaoSessionInstance.getInstance().dbGroupDao.delete(dbGroup)
        recordingChange(dbGroup.id,
                DaoSessionInstance.getInstance().dbGroupDao.tablename,
                Constant.DB_DELETE)
    }


    fun deleteLight(dbLight: DbLight) {
        DaoSessionInstance.getInstance().dbLightDao.delete(dbLight)
        recordingChange(dbLight.id,
                DaoSessionInstance.getInstance().dbLightDao.tablename,
                Constant.DB_DELETE)
    }


    fun deleteAllRouter() {
        val gateways = getAllGateWay()
        for (g in gateways) {
            deleteGateway(g)
        }
    }

    fun deleteAllGateway() {
        val gateways = getAllGateWay()
        for (g in gateways) {
            deleteGateway(g)
        }
    }

    fun deleteAllNormalLight() {
        val lights = getAllNormalLight()
        for (light in lights) {
            deleteLight(light)
        }
    }

    fun deleteAllRGBLight() {
        val lights = getAllRGBLight()
        for (light in lights) {
            deleteLight(light)
        }
    }

    fun deleteAllConnector() {
        val connectors = getAllRelay()
        for (connector in connectors) {
            deleteConnector(connector)
        }
    }

    fun deleteAllCurtain() {
        val curtains = getAllCurtains()
        for (item in curtains) {
            deleteCurtain(item)
        }
    }


    fun deleteConnector(dbConnector: DbConnector) {
        DaoSessionInstance.getInstance().dbConnectorDao.delete(dbConnector)
        recordingChange(dbConnector.id,
                DaoSessionInstance.getInstance().dbConnectorDao.tablename,
                Constant.DB_DELETE)
    }

    fun deleteUser(dbUser: DbUser) {
        DaoSessionUser.getInstance().dbUserDao.delete(dbUser)
    }


    fun deleteScene(dbScene: DbScene) {
        DaoSessionInstance.getInstance().dbSceneDao.delete(dbScene)
        recordingChange(dbScene.id,
                DaoSessionInstance.getInstance().dbSceneDao.tablename,
                Constant.DB_DELETE)
    }

    fun deleteCurtain(dbCurtain: DbCurtain) {
        DaoSessionInstance.getInstance().dbCurtainDao.delete(dbCurtain)
        recordingChange(dbCurtain.id,
                DaoSessionInstance.getInstance().dbCurtainDao.tablename,
                Constant.DB_DELETE
        )
    }

    fun deleteSwitch(dbSwitch: DbSwitch) {
        DaoSessionInstance.getInstance().dbSwitchDao.delete(dbSwitch)
        recordingChange(dbSwitch.id,
                DaoSessionInstance.getInstance().dbSwitchDao.tablename,
                Constant.DB_DELETE
        )
    }

    fun deleteSensor(dbSensor: DbSensor) {
        DaoSessionInstance.getInstance().dbSensorDao.delete(dbSensor)
        recordingChange(dbSensor.id,
                DaoSessionInstance.getInstance().dbSensorDao.tablename,
                Constant.DB_DELETE
        )
    }

    fun deleteGateway(gateway: DbGateway) {
        DaoSessionInstance.getInstance().dbGatewayDao.delete(gateway)
        recordingChange(gateway.id,
                DaoSessionInstance.getInstance().dbGatewayDao.tablename,
                Constant.DB_DELETE
        )
    }

    fun deleteRouter(dbRouter: DbRouter) {
        DaoSessionInstance.getInstance().dbRouterDao.delete(dbRouter)
        recordingChange(dbRouter.id,
                DaoSessionInstance.getInstance().dbRouterDao.tablename,
                Constant.DB_DELETE
        )
    }


    fun deleteSceneActionsList(sceneActionslist: List<DbSceneActions>) {
        DaoSessionInstance.getInstance().dbSceneActionsDao.deleteInTx(sceneActionslist)
    }

    fun deleteGradient(dbDiyGradient: DbDiyGradient) {
        DaoSessionInstance.getInstance().dbDiyGradientDao.delete(dbDiyGradient)
        recordingChange(dbDiyGradient.id, DaoSessionInstance.getInstance().dbDiyGradientDao.tablename, Constant.DB_DELETE)
    }


    fun deleteColorNodeList(colorNodelist: List<DbColorNode>) {
        DaoSessionInstance.getInstance().dbColorNodeDao.deleteInTx(colorNodelist)
    }


    fun deleteAllData() {
        DaoSessionInstance.getInstance().dbUserDao.deleteAll()
        DaoSessionInstance.getInstance().dbSceneDao.deleteAll()
        DaoSessionInstance.getInstance().dbSceneActionsDao.deleteAll()
        DaoSessionInstance.getInstance().dbRegionDao.deleteAll()
        DaoSessionInstance.getInstance().dbGroupDao.deleteAll()
        DaoSessionInstance.getInstance().dbLightDao.deleteAll()
        DaoSessionInstance.getInstance().dbDataChangeDao.deleteAll()
        DaoSessionInstance.getInstance().dbDiyGradientDao.deleteAll()
        DaoSessionInstance.getInstance().dbColorNodeDao.deleteAll()
        DaoSessionInstance.getInstance().dbCurtainDao.deleteAll()
        DaoSessionInstance.getInstance().dbSwitchDao.deleteAll()
        DaoSessionInstance.getInstance().dbSensorDao.deleteAll()
        DaoSessionInstance.getInstance().dbConnectorDao.deleteAll()
        //DaoSessionInstance.getInstance().dbGatewayDao.deleteAll()
        DaoSessionInstance.getInstance().dbRouterDao.deleteAll()
    }

    fun deleteAllSensorAndSwitch() {
        DaoSessionInstance.getInstance().dbSwitchDao.deleteAll()
        DaoSessionInstance.getInstance().dbSensorDao.deleteAll()
    }

    fun deleteColorNodeAll() {
        DaoSessionInstance.getInstance().dbColorNodeDao.deleteAll()
    }

    fun deleteDiyGradientAll() {
        DaoSessionInstance.getInstance().dbDiyGradientDao.deleteAll()
    }

    fun deleteAllSwitch() {
        DaoSessionInstance.getInstance().dbSwitchDao.deleteAll()
    }

    fun deleteAllSensor() {
        DaoSessionInstance.getInstance().dbSensorDao.deleteAll()
    }

    fun deleteAllScene() {
        DaoSessionInstance.getInstance().dbSceneDao.deleteAll()
        DaoSessionInstance.getInstance().dbSceneActionsDao.deleteAll()
    }

    fun deleteLocalData() {
        //        DaoSessionInstance.getApp().getDbUserDao().deleteAllSensorAndSwitch();
        DaoSessionInstance.getInstance().dbSceneDao.deleteAll()
        DaoSessionInstance.getInstance().dbSceneActionsDao.deleteAll()
        DaoSessionInstance.getInstance().dbRegionDao.deleteAll()
        DaoSessionInstance.getInstance().dbGroupDao.deleteAll()
        DaoSessionInstance.getInstance().dbLightDao.deleteAll()
        DaoSessionInstance.getInstance().dbCurtainDao.deleteAll()
        DaoSessionInstance.getInstance().dbSwitchDao.deleteAll()
        DaoSessionInstance.getInstance().dbSensorDao.deleteAll()
        DaoSessionInstance.getInstance().dbDataChangeDao.deleteAll()
        DaoSessionInstance.getInstance().dbDiyGradientDao.deleteAll()
        DaoSessionInstance.getInstance().dbColorNodeDao.deleteAll()
        DaoSessionInstance.getInstance().dbConnectorDao.deleteAll()
        DaoSessionInstance.getInstance().dbGatewayDao.deleteAll()
        DaoSessionInstance.getInstance().dbRouterDao.deleteAll()
    }


    fun deleteDbDataChange(id: Long) {
        DaoSessionInstance.getInstance().dbDataChangeDao.deleteByKey(id)
    }

    /********************************************其他 */


    fun addNewGroup(name: String, groups: MutableList<DbGroup>, context: Context) {
        //        if (!checkRepeat(groups, context, name) && !checkReachedTheLimit(groups)) {
        if (!checkReachedTheLimit(groups, name)) {
            val newMeshAdress: Int
            var group = DbGroup()
            newMeshAdress = groupAdress
            group.meshAddr = newMeshAdress
            group.name = name
            group.brightness = 100
            group.colorTemperature = 100
            group.color = 0xffffff
            group.belongRegionId = SharedPreferencesUtils.getCurrentUseRegionId().toInt()//目前暂无分区 区域ID暂为0
            groups.add(group)
            //新增数据库保存
            saveGroup(group, false)

            group = DBUtils.getGroupByMeshAddr(newMeshAdress)!!
            if (group != null) {
                recordingChange(group.id,
                        DaoSessionInstance.getInstance().dbGroupDao.tablename,
                        Constant.DB_ADD)
            }

        }
    }

    fun addNewGroupWithType(name: String, type: Long): DbGroup? {
        val groups = DBUtils.groupList
        //        if (!checkRepeat(groups, context, name) && !checkReachedTheLimit(groups)) {
        if (!checkReachedTheLimit(groups, name)) {
            val newMeshAdress: Int
            var group = DbGroup()
            newMeshAdress = groupAdress
            group.meshAddr = newMeshAdress
            group.name = name
            group.brightness = 100
            group.colorTemperature = 100
            group.color = 0xffffff
            group.belongRegionId = SharedPreferencesUtils.getCurrentUseRegionId().toInt()//目前暂无分区 区域ID暂为0
            group.deviceType = type
            groups.add(group)
            //新增数据库保存
            saveGroup(group, false)

            group.index = group.id.toInt()

//            group=DBUtils.getGroupByMeshAddr(newMeshAdress)

            updateGroup(group)

            recordingChange(group.id,
                    DaoSessionInstance.getInstance().dbGroupDao.tablename,
                    Constant.DB_ADD)

            return group
        }
        return null
    }

    fun getDefaultNewGroupName(): String {
        //        if (!checkRepeat(groups, context, name) && !checkReachedTheLimit(groups)) {
        val gpList = groupList
        val gpNameList = ArrayList<String>()
        for (i in gpList.indices) {
            gpNameList.add(gpList[i].name)
        }

        var count = gpList.size - 1
        while (true) {
            count++
            val newName = TelinkLightApplication.getApp().getString(R.string.group) + count
            if (!gpNameList.contains(newName)) {
                return newName
            }
        }
    }

    fun getDefaultNewSceneName(): String {
        //        if (!checkRepeat(groups, context, name) && !checkReachedTheLimit(groups)) {
        val scList = sceneList
        val scNameList = ArrayList<String>()
        for (i in scList.indices) {
            scNameList.add(scList[i].name)
        }


        var count = scList.size
        while (true) {
            count++
            val newName = TelinkLightApplication.getApp().getString(R.string.scene_name) + count
            if (!scNameList.contains(newName)) {
                return newName
            }
        }
    }

    fun getDefaultModeName(): String {
        //        if (!checkRepeat(groups, context, name) && !checkReachedTheLimit(groups)) {
        val dyList = diyGradientList
        val scNameList = ArrayList<String>()
        for (i in dyList.indices) {
            scNameList.add(dyList[i].name)
        }

        var count = dyList.size
        while (true) {
            count++
            val newName = TelinkLightApplication.getApp().getString(R.string.mode_str) + count
            if (!scNameList.contains(newName)) {
                return newName
            }
        }
    }

    private fun checkReachedTheLimit(groups: List<DbGroup>, name: String): Boolean {
        if (groups.size > Constant.MAX_GROUP_COUNT) {
            ToastUtils.showLong(R.string.group_limit)
            return true
        }
        for (i in groups.indices) {
            if (groups[i].name == name) {
                ToastUtils.showLong(TelinkLightApplication.getApp().getString(R.string.repeat_name))
                return true
            }
        }

        return false
    }


    /**
     * 创建一个控制所有灯的分组
     *
     * @return group对象
     */

    fun createAllLightControllerGroup(): DbGroup {
        val groupAllLights = DbGroup()
        if (allGroups.size > 0)
            groupAllLights.connectionStatus = allGroups[0].connectionStatus
        groupAllLights.name = TelinkLightApplication.getApp().getString(R.string.allLight)
        groupAllLights.meshAddr = 0xFFFF
        groupAllLights.brightness = 100
        groupAllLights.colorTemperature = 100
        groupAllLights.deviceType = 1
        groupAllLights.color = TelinkLightApplication.getApp().resources.getColor(R.color.gray)
        groupAllLights.belongRegionId = SharedPreferencesUtils.getCurrentUseRegionId().toInt()
        groupAllLights.id = 1
        DaoSessionInstance.getInstance().dbGroupDao.insertOrReplace(groupAllLights)
        recordingChange(groupAllLights.id,
                DaoSessionInstance.getInstance().dbGroupDao.tablename, Constant.DB_ADD)

        return groupAllLights
    }

    /**
     * 记录本地数据库变化
     * @param changeIndex 变化的位置
     * @param changeTable 变化数据所属表
     * @param operating   所执行的操作
     */

    fun recordingChange(changeIndex: Long?, changeTable: String, operating: String, type: Int = 3, keys: String = "") {
        val dataChangeList = DaoSessionInstance.getInstance().dbDataChangeDao.loadAll()

        if (dataChangeList.size == 0) {
            saveChange(changeIndex, operating, changeTable, type, keys)
            return
        } else {
            for (i in dataChangeList.indices) {
                //首先确定是同一张表的同一条数据进行操作
                if ((dataChangeList[i].tableName == changeTable) and (dataChangeList[i].changeId == changeIndex)) {

                    if (dataChangeList[i].changeType == operating) {
                        break
                    }

                    //如果改变相同数据是删除就再记录一次，如果不是删除则不再记录
                    if (dataChangeList[i].changeType == Constant.DB_ADD && operating == Constant.DB_DELETE) {
                        deleteDbDataChange(dataChangeList[i].id)
                        break
                    } else if (dataChangeList[i].changeType == Constant.DB_UPDATE && operating == Constant.DB_DELETE) {
                        dataChangeList[i].changeType = operating
                        updateDbchange(dataChangeList[i])
                        continue
                    } else if (dataChangeList[i].changeType == Constant.DB_ADD && operating == Constant.DB_UPDATE) {
                        break
                    } else if (dataChangeList[i].changeType == Constant.DB_DELETE && operating == Constant.DB_ADD) {
                        dataChangeList[i].changeType = operating
                        updateDbchange(dataChangeList[i])
                        break
                    } else if (dataChangeList[i].changeType == operating) {
                        break
                    }
                } else if (i == dataChangeList.size - 1) {
                    saveChange(changeIndex, operating, changeTable, type, keys)
                    break
                }//如果数据表没有该数据直接添加
            }
        }
//        LogUtils.v("zcl-------添加变化表${dataChangeAll.size}-----------$dataChangeAll")
    }

    private fun saveChange(changeIndex: Long?, operating: String, changeTable: String, type: Int, keys: String) {
        val dataChange = DbDataChange()
        dataChange.changeId = changeIndex
        dataChange.changeType = operating
        dataChange.tableName = changeTable
        dataChange.type = type
        dataChange.keys = keys
        DaoSessionInstance.getInstance().dbDataChangeDao.insert(dataChange)
    }


    fun getSensorByMeshAddr(meshAddr: Int): DbSensor? {
        var sensorDbList: MutableList<DbSensor?> = DaoSessionInstance.getInstance().dbSensorDao.queryBuilder().where(DbSensorDao.Properties.MeshAddr.eq(meshAddr)).list()
        return if (sensorDbList.size > 0) {
            sensorDbList[0]
        } else null
    }

    fun timeStr(time: Int): String {
        return if (time < 10)
            "0$time"
        else
            time.toString()
    }

    fun getlastDeviceMesh(): Int {
        val lights = allLight?.map { it.meshAddr }
        val curtain = allCurtain?.map { it.meshAddr }
        val relay = allRely?.map { it.meshAddr }
        val switch = getAllSwitch().map { it.meshAddr }
        val sensor = getAllSensor().map { it.meshAddr }
        val addressList = mutableListOf<Int>()

        /*  addressList.addAll(lights)
          addressList.addAll(curtain)
          addressList.addAll(relay)
          addressList.addAll(switch)
          addressList.addAll(sensor)*/

        lights?.let {
            addressList.addAll(it)
        }
        curtain?.let {
            addressList.addAll(it)
        }
        relay?.let {
            addressList.addAll(it)
        }
        switch?.let {
            addressList.addAll(it)
        }
        sensor?.let {
            addressList.addAll(it)
        }
        addressList.sortBy { it }

        return when {
            addressList.isEmpty() -> 0
            else -> addressList.last()
        }
    }

    fun isFastDoubleClick(delay: Int = 500): Boolean {
        val time = System.currentTimeMillis()
        if (time - lastClickTime < delay)
            return true
        lastClickTime = time
        return false
    }
}
