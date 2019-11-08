package com.dadoutek.uled.group

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.app.hubert.guide.util.ScreenUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.intf.SyncCallback
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.*
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.BleUtils
import com.dadoutek.uled.util.StringUtils
import com.dadoutek.uled.util.SyncDataPutOrGetUtils
import com.dadoutek.uled.widget.RecyclerGridDecoration
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.TelinkApplication
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.event.DeviceEvent
import com.telink.bluetooth.event.ErrorReportEvent
import com.telink.bluetooth.light.LightAdapter
import com.telink.bluetooth.light.Parameters
import com.telink.util.Event
import com.telink.util.EventListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_batch_group_four.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * 创建者     ZCL
 * 创建时间   2019/10/16 14:41
 * 描述
 *
 * 更新者     zcl$
 * 更新时间   用于冷暖灯,彩灯,窗帘控制器的批量分组$
 * 更新描述
 */
class BatchGroupFourDeviceActivity : TelinkBaseActivity(), EventListener<String> {
    private var deviceCount: Int? = null
    private var lpShort: LinearLayout.LayoutParams? = null
    private var lplong: LinearLayout.LayoutParams? = null
    private var disposable: Disposable? = null
    private var currentGroup: DbGroup? = null
    private var btnAddGroups: TextView? = null
    private var emptyGroupView: View? = null
    private var mTelinkLightService: TelinkLightService? = null

    private var groupAdapter: BatchGrouopEditListAdapter? = null
    private var lightAdapter: BatchFourLightAdapter? = null
    private var relayAdapter: BatchFourRelayAdapter? = null
    private var curtainAdapter: BatchFourCurtainAdapter? = null

    private lateinit var deviceDataRelays: ArrayList<DbConnector>
    private lateinit var deviceDataCurtains: ArrayList<DbCurtain>
    private lateinit var deviceDataLights: ArrayList<DbLight>

    private var scanDeviceData: Array<Parcelable>? = null
    private var groupsByDeviceType: MutableList<DbGroup>? = null
    private var deviceType: Int = 100
    private var lastCheckedGroupPostion: Int = 1000
    private var allLightId: Long = 0//有设备等于0说明没有分组成功
    private var retryConnectCount = 0
    private var connectMeshAddress: Int = 0
    private var mApplication: TelinkLightApplication? = null

    private var noGroup: MutableList<DbLight>? = null
    private var listGroup: MutableList<DbLight>? = null
    private var deviceData: MutableList<DbLight>? = null

    private var noGroupCutain: MutableList<DbCurtain>? = null
    private var listGroupCutain: MutableList<DbCurtain>? = null
    private var deviceDataCurtain: MutableList<DbCurtain>? = null

    private var noGroupRelay: MutableList<DbConnector>? = null
    private var listGroupRelay: MutableList<DbConnector>? = null
    private var deviceDataRelay: MutableList<DbConnector>? = null

    private var checkedNoGrouped: Boolean = true
    //获取当前勾选灯的列表
    private var selectLights = mutableListOf<DbLight>()
    private var selectCurtains = mutableListOf<DbCurtain>()
    private var selectRelays = mutableListOf<DbConnector>()
    private val mBlinkDisposables = SparseArray<Disposable>()
    private var isAddGroupEmptyView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_group_four)
        this.mApplication = this.application as TelinkLightApplication
        initView()
        initData()
        initListener()
    }

    private fun initView() {
        toolbarTv.text = getString(R.string.batch_group)
        image_bluetooth.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.navigation_back_white)
        toolbar.setNavigationOnClickListener {
            checkNetworkAndSync(this)
            finish()
        }

        if (TelinkApplication.getInstance().connectDevice == null)
            image_bluetooth.setImageResource(R.drawable.bluetooth_no)
        else
            image_bluetooth.setImageResource(R.drawable.icon_bluetooth)

        emptyGroupView = LayoutInflater.from(this).inflate(R.layout.empty_group_view, null)
        btnAddGroups = emptyGroupView!!.findViewById<TextView>(R.id.add_groups_btn)

        batch_four_device_recycle.layoutManager = GridLayoutManager(this, 2)
        batch_four_device_recycle.addItemDecoration(RecyclerGridDecoration(this, 2))


        batch_four_group_recycle.layoutManager = GridLayoutManager(this, 2)
        batch_four_group_recycle.addItemDecoration(RecyclerGridDecoration(this, 2))

        lplong = batch_four_no_group.layoutParams as LinearLayout.LayoutParams
        lplong?.weight = 3f

        lpShort = batch_four_no_group_line_ly.layoutParams as LinearLayout.LayoutParams
        lpShort?.weight = 2f

        autoConnect()
    }

    private fun initData() {
        deviceType = intent.getIntExtra(Constant.DEVICE_TYPE, 100)
        scanDeviceData = intent.getParcelableArrayExtra("data")
        setDevicesData(deviceType)
        setGroupData(deviceType)
    }

    /**
     * 设置组数据
     */
    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    private fun setGroupData(deviceType: Int) {
        groupsByDeviceType = DBUtils.getGroupsByDeviceType(deviceType)
        if (groupsByDeviceType == null) {
            groupsByDeviceType = mutableListOf()
        } else if (groupsByDeviceType!!.size > 0) {
            for (index in groupsByDeviceType!!.indices)
                groupsByDeviceType!![index].isChecked = index == 0
        }
        batch_four_group_title.text = getString(R.string.grouped_num, groupsByDeviceType?.size)

        groupAdapter = BatchGrouopEditListAdapter(R.layout.batch_four_group_edit_item, groupsByDeviceType!!)
        if (!isAddGroupEmptyView)
            groupAdapter?.emptyView = emptyGroupView

        isAddGroupEmptyView = true
        groupAdapter?.bindToRecyclerView(batch_four_group_recycle)
        if (groupsByDeviceType!!.size > 0) {
            for (index in groupsByDeviceType!!.indices) {
                val isSelect = index == 0
                groupsByDeviceType!![index].isChecked = isSelect
                if (isSelect) {
                    lastCheckedGroupPostion = 0
                    currentGroup = groupsByDeviceType!![index]
                }
            }
        }

        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                changeGroupingCompleteState(selectLights.size)
            }
            DeviceType.SMART_CURTAIN -> {
                changeGroupingCompleteState(selectCurtains.size)
            }
            DeviceType.SMART_RELAY -> {
                changeGroupingCompleteState(selectRelays.size)
            }
        }

        groupAdapter?.setOnItemClickListener { _, _, position ->
            //如果点中的不是上次选中的组则恢复未选中状态
            changeGroupSelectView(position)
        }

    }

    private fun clearSelectors() {
        selectLights = mutableListOf()
        selectCurtains = mutableListOf()
        selectRelays = mutableListOf()
    }

    /**
     * 设置设备数据
     */
    @SuppressLint("StringFormatInvalid")
    private fun setDevicesData(deviceType: Int) {
        clearSelectors()
        when (deviceType) {
            DeviceType.LIGHT_NORMAL -> {
                noGroup = mutableListOf()
                listGroup = mutableListOf()
                deviceDataLights = DBUtils.getAllNormalLight()
            }
            DeviceType.LIGHT_RGB -> {
                noGroup = mutableListOf()
                listGroup = mutableListOf()
                deviceDataLights = DBUtils.getAllRGBLight()
            }
            DeviceType.SMART_CURTAIN -> {
                noGroupCutain = mutableListOf()
                listGroupCutain = mutableListOf()
                deviceDataCurtains = DBUtils.getAllCurtains()
            }
            DeviceType.SMART_RELAY -> {
                noGroupRelay = mutableListOf()
                listGroupRelay = mutableListOf()
                deviceDataRelays = DBUtils.getAllRelay()
            }
        }


        when (deviceType) {
            DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                groupingNoOrHaveAndchangeTitle(deviceType)
                checkedNoGrouped = noGroup?.size ?: 0 > 0  //初始化状态是否显示未分组灯 如果有就显示未分组否则一分组

                changeDeviceData()//赋值指定数据
            }
            DeviceType.SMART_CURTAIN -> {
                groupingNoOrHaveAndchangeTitle(DeviceType.SMART_CURTAIN)
                checkedNoGrouped = noGroupCutain?.size ?: 0 > 0

                changeDeviceData()
            }
            DeviceType.SMART_RELAY -> {
                groupingNoOrHaveAndchangeTitle(DeviceType.SMART_RELAY)
                checkedNoGrouped = noGroupRelay?.size ?: 0 > 0

                changeDeviceData()
            }
        }
    }

    /**
     * adapter赋值并设置不同高度
     */
    private fun setLightAdatpter() {
        lightAdapter = BatchFourLightAdapter(R.layout.batch_device_item, deviceData!!)
        setDeviceListHeightAndEmpty(deviceData?.size)
        lightAdapter?.bindToRecyclerView(batch_four_device_recycle)
        btnAddGroups?.setOnClickListener { addNewGroup() }
        lightAdapter?.setOnItemChildClickListener { _, _, position ->
            deviceData?.get(position)!!.selected = !deviceData?.get(position)!!.selected
            if (deviceData?.get(position)!!.isSelected) {
                startBlink(deviceData?.get(position)!!.belongGroupId, deviceData?.get(position)!!.meshAddr)
                selectLights.add(deviceData!![position])
            } else {
                stopBlink(deviceData?.get(position)!!.meshAddr)
                selectLights.remove(deviceData!![position])
            }
            changeGroupingCompleteState(selectLights.size)
            lightAdapter?.notifyDataSetChanged()
        }
    }

    private fun setCurtainAdatpter() {
        curtainAdapter = BatchFourCurtainAdapter(R.layout.batch_device_item, deviceDataCurtain!!)
        setDeviceListHeightAndEmpty(deviceDataCurtain?.size)
        curtainAdapter?.bindToRecyclerView(batch_four_device_recycle)
        curtainAdapter?.setOnItemChildClickListener { _, _, position ->
            deviceDataCurtain?.get(position)!!.selected = !deviceDataCurtain?.get(position)!!.selected
            if (deviceDataCurtain?.get(position)!!.isSelected) {
                startBlink(deviceDataCurtain?.get(position)!!.belongGroupId, deviceDataCurtain?.get(position)!!.meshAddr)
                selectCurtains.add(deviceDataCurtain!![position])
            } else {
                stopBlink(deviceDataCurtain?.get(position)!!.meshAddr)
                selectCurtains.remove(deviceDataCurtain!![position])
            }
            changeGroupingCompleteState(selectCurtains.size)
            curtainAdapter?.notifyDataSetChanged()
        }
    }

    private fun setRelayAdatpter() {
        relayAdapter = BatchFourRelayAdapter(R.layout.batch_device_item, deviceDataRelay!!)
        setDeviceListHeightAndEmpty(deviceDataRelay?.size)
        relayAdapter?.bindToRecyclerView(batch_four_device_recycle)
        relayAdapter?.setOnItemChildClickListener { _, _, position ->
            deviceDataRelay?.get(position)!!.selected = !deviceDataRelay?.get(position)!!.selected
            if (deviceDataRelay?.get(position)!!.isSelected) {
                startBlink(deviceDataRelay?.get(position)!!.belongGroupId, deviceDataRelay?.get(position)!!.meshAddr)
                selectRelays.add(deviceDataRelay!![position])
            } else {
                stopBlink(deviceDataRelay?.get(position)!!.meshAddr)
                selectRelays.remove(deviceDataRelay!![position])
            }
            changeGroupingCompleteState(selectRelays.size)
            relayAdapter?.notifyDataSetChanged()
        }
    }

    private fun setDeviceListHeightAndEmpty(size: Int?) {
        if (size == 0) {
            batch_four_device_recycle.visibility = View.INVISIBLE
            image_no_device.visibility = View.VISIBLE
        } else {
            batch_four_device_recycle.visibility = View.VISIBLE
            image_no_device.visibility = View.INVISIBLE
        }
        setRecycleViewH(size)
    }

    @SuppressLint("CheckResult")
    fun autoConnect() {
        //如果支持蓝牙就打开蓝牙
        if (LeBluetooth.getInstance().isSupport(applicationContext))
            LeBluetooth.getInstance().enable(applicationContext)    //如果没打开蓝牙，就提示用户打开

        //如果位置服务没打开，则提示用户打开位置服务，bleScan必须
        if (!BleUtils.isLocationEnable(this)) {
            showOpenLocationServiceDialog()
        } else {
            hideLocationServiceDialog()
            mTelinkLightService = TelinkLightService.Instance()
            while (TelinkApplication.getInstance()?.serviceStarted == true) {
                disposable?.dispose()
                if (!this.isDestroyed)
                    disposable = RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (!TelinkLightService.Instance().isLogin) {
                                    ToastUtils.showShort(getString(R.string.connecting_tip))
                                    retryConnectCount = 0
                                    val meshName = DBUtils.lastUser!!.controlMeshName

                                    GlobalScope.launch {
                                        //自动重连参数
                                        val connectParams = Parameters.createAutoConnectParameters()
                                        connectParams.setMeshName(meshName)
                                        connectParams.setPassword(NetworkFactory.md5(NetworkFactory.md5(meshName) + meshName).substring(0, 16))
                                        connectParams.autoEnableNotification(true)
                                        connectParams.setConnectDeviceType(
                                                mutableListOf(DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_NORMAL_OLD, DeviceType.LIGHT_RGB,
                                                        DeviceType.SMART_RELAY, DeviceType.SMART_CURTAIN))
                                        //连接，如断开会自动重连
                                        TelinkLightService.Instance().autoConnect(connectParams)
                                    }
                                }
                            }, { LogUtils.d(it) })
                break
            }
        }
        val deviceInfo = this.mApplication?.connectDevice
        if (deviceInfo != null) {
            this.connectMeshAddress = (this.mApplication?.connectDevice?.meshAddress
                    ?: 0x00) and 0xFF
        }
    }

    /**
     * 将已分组与未分组设备分离并设置recucleview
     */
    @SuppressLint("StringFormatMatches")
    private fun groupingNoOrHaveAndchangeTitle(deviceType: Int) {
        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                if (deviceType == DeviceType.LIGHT_NORMAL) {
                    deviceDataLights = DBUtils.getAllNormalLight()
                } else if (deviceType == DeviceType.LIGHT_RGB)
                    deviceDataLights = DBUtils.getAllRGBLight()

                for (i in deviceDataLights.indices) {//获取组名 将分组与未分组的拆分
                    if (StringUtils.getLightGroupName(deviceDataLights[i]) == TelinkLightApplication.getApp().getString(R.string.not_grouped)) {
                        deviceDataLights[i].groupName = ""
                        noGroup!!.add(deviceDataLights[i])
                    } else {
                        deviceDataLights[i].groupName = StringUtils.getLightGroupName(deviceDataLights[i])
                        listGroup!!.add(deviceDataLights[i])
                    }
                }
            }
            DeviceType.SMART_CURTAIN -> {
                deviceDataCurtains = DBUtils.getAllCurtains()

                for (i in deviceDataCurtains.indices) {//获取组名 将分组与未分组的拆分
                    if (StringUtils.getCurtainName(deviceDataCurtains[i]) == TelinkLightApplication.getApp().getString(R.string.not_grouped)) {
                        deviceDataCurtains[i].groupName = ""
                        noGroupCutain!!.add(deviceDataCurtains[i])
                    } else {
                        deviceDataCurtains[i].groupName = StringUtils.getCurtainName(deviceDataCurtains[i])
                        listGroupCutain!!.add(deviceDataCurtains[i])
                    }
                }
            }
            DeviceType.SMART_RELAY -> {
                deviceDataRelays = DBUtils.getAllRelay()

                for (i in deviceDataRelays.indices) {//获取组名 将分组与未分组的拆分
                    if (StringUtils.getConnectorName(deviceDataRelays[i]) == TelinkLightApplication.getApp().getString(R.string.not_grouped)) {
                        deviceDataRelays[i].groupName = ""
                        noGroupRelay!!.add(deviceDataRelays[i])
                    } else {
                        deviceDataRelays[i].groupName = StringUtils.getConnectorName(deviceDataRelays[i])
                        listGroupRelay!!.add(deviceDataRelays[i])
                    }
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun changeTitleChecked() {
        if (checkedNoGrouped) {//显示没有分组的情况下
            batch_four_no_group.layoutParams = lplong
            batch_four_no_group.textSize = 18f
            batch_four_no_group_line_ly.layoutParams = lplong
            batch_four_grouped.layoutParams = lpShort
            batch_four_grouped.textSize = 15f
            batch_four_grouped_line_ly.layoutParams = lpShort

            batch_four_no_group_line.visibility = View.VISIBLE
            batch_four_grouped_line.visibility = View.INVISIBLE
        } else {//显示分组的情况下
            batch_four_no_group.layoutParams = lpShort
            batch_four_no_group.textSize = 15f
            batch_four_no_group_line_ly.layoutParams = lpShort
            batch_four_grouped.layoutParams = lplong
            batch_four_grouped.textSize = 18f
            batch_four_grouped_line_ly.layoutParams = lplong

            batch_four_no_group_line.visibility = View.INVISIBLE
            batch_four_grouped_line.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        //先取消，这样可以确保不会重复添加监听
//        this.mApplication?.removeEventListener(DeviceEvent.STATUS_CHANGED, this)
        this.mApplication?.addEventListener(DeviceEvent.STATUS_CHANGED, this)

        batch_four_group_rg.setOnCheckedChangeListener { _, checkedId ->
            checkedNoGrouped = checkedId == batch_four_no_group.id
            changeDeviceData()
        }

        batch_four_device_all.setOnClickListener { changeDeviceAll() }

        batch_four_group_add_group.setOnClickListener { addNewGroup() }

        grouping_completed.setOnClickListener {
            if (TelinkLightService.Instance()?.isLogin == true)
                sureGroups()
            else
                autoConnect()
        }
    }

    private fun changeGroupingCompleteState(size: Int) {
        if (size > 0 && currentGroup != null) {
            grouping_completed.setBackgroundResource(R.drawable.btn_rec_blue_bt)
            grouping_completed.isClickable = true
        } else {
            grouping_completed.isClickable = false
            grouping_completed.setBackgroundResource(R.drawable.btn_rec_black_bt)
        }
    }

    private fun sureGroups() {
        val isSelected = isSelectDevice(deviceType)
        if (isSelected) {
            //进行分组操作
            //获取当前选择的分组
            if (currentGroup != null) {
                if (currentGroup!!.meshAddr == 0xffff) {
                    ToastUtils.showLong(R.string.tip_add_gp)
                    return
                }
                when (deviceType) {
                    DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                        showLoadingDialog(resources.getString(R.string.grouping_wait_tip,
                                selectLights.size.toString()))

                    }
                    DeviceType.SMART_CURTAIN -> {
                        showLoadingDialog(resources.getString(R.string.grouping_wait_tip,
                                selectCurtains.size.toString()))
                    }
                    DeviceType.SMART_RELAY -> {
                        showLoadingDialog(resources.getString(R.string.grouping_wait_tip,
                                selectRelays.size.toString()))

                    }
                }
                setGroup(deviceType)
                //将灯列表的灯循环设置分组
                setGroupForLights(deviceType)
            } else {
                Toast.makeText(mApplication, R.string.select_group_tip, Toast.LENGTH_SHORT).show()
            }
        } else {
            showToast(getString(R.string.selected_lamp_tip))
        }
    }

    private fun setGroup(deviceType: Int) {
        when (deviceType) {
            DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                showLoadingDialog(resources.getString(R.string.grouping_wait_tip, selectLights.size.toString()))
                for (i in selectLights.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectLights[i].meshAddr)
                }
            }
            DeviceType.SMART_CURTAIN -> {
                showLoadingDialog(resources.getString(R.string.grouping_wait_tip, selectCurtains.size.toString()))

                for (i in selectCurtains.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectCurtains[i].meshAddr)
                }
            }
            DeviceType.SMART_RELAY -> {
                showLoadingDialog(resources.getString(R.string.grouping_wait_tip, selectRelays.size.toString()))
                for (i in selectRelays.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectRelays[i].meshAddr)
                }
            }
        }
        setGroupOneByOne(currentGroup!!, deviceType, 0)
    }

    private fun completeGroup(deviceType: Int) {
        when (deviceType) {
            DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                //取消分组成功的勾选的灯
                for (i in selectLights.indices) {
                    val light = selectLights[i]
                    light.selected = false
                }
                lightAdapter?.notifyDataSetChanged()
            }
            DeviceType.SMART_CURTAIN -> {
                //取消分组成功的勾选的灯
                for (i in selectCurtains.indices) {
                    val light = selectCurtains[i]
                    light.selected = false
                }
                curtainAdapter?.notifyDataSetChanged()
            }
            DeviceType.SMART_RELAY -> {
                //取消分组成功的勾选的灯
                for (i in selectRelays.indices) {
                    val light = selectRelays[i]
                    light.selected = false
                }
                relayAdapter?.notifyDataSetChanged()
            }
        }

        hideLoadingDialog()
        clearSelectors()
        SyncDataPutOrGetUtils.syncPutDataStart(this, object : SyncCallback {
            override fun start() {
                LogUtils.v("zcl更新start")
            }

            override fun complete() {
                LogUtils.v("zcl更新结束")
                refreshData()
                initData()
                initListener()
                LogUtils.v("")
            }

            override fun error(msg: String?) {
                LogUtils.v("zcl更新错误")
            }
        })
            //grouping_completed.setText(R.string.complete)
    }

    private fun setGroupForLights(deviceType: Int) {
        when (deviceType) {
            DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                for (i in selectLights.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectLights[i].meshAddr)
                }
            }
            DeviceType.SMART_CURTAIN -> {
                for (i in selectCurtains.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectCurtains[i].meshAddr)
                }
            }
            DeviceType.SMART_RELAY -> {
                for (i in selectRelays.indices) {
                    //让选中的灯停下来别再发闪的命令了。
                    stopBlink(selectRelays[i].meshAddr)
                }
            }
        }
    }

    private fun updateGroupResultLight(light: DbLight, group: DbGroup) {
        for (i in selectLights.indices) {
            if (light.meshAddr == selectLights[i].meshAddr) {
                if (light.belongGroupId != allLightId) {
                    selectLights[i].hasGroup = true
                    selectLights[i].belongGroupId = group.id
                    selectLights[i].name = light.name
                    DBUtils.updateLight(light)
                } else {
                    selectLights[i].hasGroup = false
                }
            }
        }
    }

    private fun updateGroupResultCurtain(light: DbCurtain, group: DbGroup) {
        for (i in selectCurtains.indices) {
            if (light.meshAddr == selectCurtains[i].meshAddr) {
                if (light.belongGroupId != allLightId) {
                    selectCurtains[i].hasGroup = true
                    selectCurtains[i].belongGroupId = group.id
                    selectCurtains[i].name = light.name
                    DBUtils.updateCurtain(light)
                } else {
                    selectCurtains[i].hasGroup = false
                }
            }
        }
    }

    private fun updateGroupResultRelay(light: DbConnector, group: DbGroup) {
        for (i in selectRelays.indices) {
            if (light.meshAddr == selectRelays[i].meshAddr) {
                if (light.belongGroupId != allLightId) {
                    selectRelays[i].hasGroup = true
                    selectRelays[i].belongGroupId = group.id
                    selectRelays[i].name = light.name
                    DBUtils.updateConnector(light)
                } else {
                    selectRelays[i].hasGroup = false
                }
            }
        }
    }

    //等待解决无法加入窗帘如组
    private fun setGroupOneByOne(dbGroup: DbGroup, deviceType: Int, index: Int) {
        var deviceMeshAddr = 0
        var dbLight: DbLight? = null
        var dbCurtain: DbCurtain? = null
        var dbRelay: DbConnector? = null

        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                dbLight = selectLights[index]
                deviceMeshAddr = dbLight.meshAddr
            }
            DeviceType.SMART_CURTAIN -> {
                dbCurtain = selectCurtains[index]
                deviceMeshAddr = dbCurtain.meshAddr
            }
            DeviceType.SMART_RELAY -> {
                dbRelay = selectRelays[index]
                deviceMeshAddr = dbRelay.meshAddr
            }
        }

        val successCallback: () -> Unit = {
            ToastUtils.showShort(getString(R.string.grouping_success_tip))
            when (deviceType) {
                DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                    dbLight?.belongGroupId = dbGroup.id
                    updateGroupResultLight(dbLight!!, dbGroup)
                    if (index + 1 > selectLights.size - 1)
                        completeGroup(deviceType)
                    else
                        setGroupOneByOne(dbGroup, deviceType, index + 1)
                }
                DeviceType.SMART_CURTAIN -> {
                    dbCurtain?.belongGroupId = dbGroup.id
                    updateGroupResultCurtain(dbCurtain!!, dbGroup)
                    if (index + 1 > selectCurtains.size - 1)
                        completeGroup(deviceType)
                    else
                        setGroupOneByOne(dbGroup, deviceType, index + 1)
                }
                DeviceType.SMART_RELAY -> {
                    dbRelay?.belongGroupId = dbGroup.id
                    updateGroupResultRelay(dbRelay!!, dbGroup)
                    if (index + 1 > selectRelays.size - 1)
                        completeGroup(deviceType)
                    else
                        setGroupOneByOne(dbGroup, deviceType, index + 1)
                }
            }
        }
        val failedCallback: () -> Unit = {
            ToastUtils.showLong(R.string.group_fail_tip)
            when (deviceType) {
                DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                    dbLight?.belongGroupId = allLightId
                    updateGroupResultLight(dbLight!!, dbGroup)
                    if (TelinkLightApplication.getApp().connectDevice == null) {
                        ToastUtils.showLong(getString(R.string.connect_fail))
                    } else {
                        if (index + 1 > selectLights.size - 1)
                            completeGroup(deviceType)
                        else
                            setGroupOneByOne(dbGroup, deviceType, index + 1)
                    }
                }
                DeviceType.SMART_CURTAIN -> {
                    dbCurtain?.belongGroupId = allLightId
                    updateGroupResultCurtain(dbCurtain!!, dbGroup)
                    if (TelinkLightApplication.getApp().connectDevice == null) {
                        ToastUtils.showLong(getString(R.string.connect_fail))
                    } else {
                        if (index + 1 > selectCurtains.size - 1)
                            completeGroup(deviceType)
                        else
                            setGroupOneByOne(dbGroup, deviceType, index + 1)
                    }
                }
                DeviceType.SMART_RELAY -> {
                    dbRelay?.belongGroupId = allLightId
                    updateGroupResultRelay(dbRelay!!, dbGroup)
                    if (TelinkLightApplication.getApp().connectDevice == null) {
                        ToastUtils.showLong(getString(R.string.connect_fail))
                    } else {
                        if (index + 1 > selectRelays.size - 1)
                            completeGroup(deviceType)
                        else
                            setGroupOneByOne(dbGroup, deviceType, index + 1)
                    }
                }
            }
        }
        Commander.addGroup(deviceMeshAddr, dbGroup.meshAddr, successCallback, failedCallback)
    }

    private fun stopBlink(meshAddr: Int) {
        val disposable = mBlinkDisposables.get(meshAddr)
        disposable?.dispose()
    }

    private fun isSelectDevice(deviceType: Int): Boolean {
        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                deviceData ?: return false
                return selectLights.size > 0
            }
            DeviceType.SMART_CURTAIN -> {
                deviceDataCurtain ?: return false
                return selectCurtains.size > 0
            }
            DeviceType.SMART_RELAY -> {
                deviceDataRelay ?: return false
                return selectRelays.size > 0
            }
        }
        return false
    }

    /**
     * 分组与未分组切换逻辑
     */
    private fun changeDeviceData() {
        batch_four_device_all.text = getString(R.string.select_all)
        setAllSelect(false)

        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                deviceData = if (checkedNoGrouped) {
                    batch_four_no_group.isChecked = true
                    noGroup
                } else {
                    batch_four_grouped.isChecked = true
                    listGroup
                }

                setTitleTexts(noGroup?.size, listGroup?.size)

                setLightAdatpter()
            }
            DeviceType.SMART_CURTAIN -> {
                deviceDataCurtain = if (checkedNoGrouped) {
                    batch_four_no_group.isChecked = true
                    noGroupCutain
                } else {
                    batch_four_grouped.isChecked = true
                    listGroupCutain
                }
                setTitleTexts(noGroupCutain?.size, listGroupCutain?.size)
                setCurtainAdatpter()
            }
            DeviceType.SMART_RELAY -> {
                deviceDataRelay = if (checkedNoGrouped) {
                    batch_four_no_group.isChecked = true
                    noGroupRelay
                } else {
                    batch_four_grouped.isChecked = true
                    listGroupRelay
                }
                setTitleTexts(noGroupRelay?.size, listGroupRelay?.size)
                setRelayAdatpter()
            }
        }
        changeTitleChecked()//改变title
    }

    @SuppressLint("StringFormatMatches")
    private fun setTitleTexts(noSize: Int?, haveSize: Int?) {
        batch_four_no_group.text = getString(R.string.no_group_device_num, noSize ?: 0)
        batch_four_grouped.text = getString(R.string.grouped_num, haveSize ?: 0)
    }

    /**
     * 设置list高度
     */
    private fun setRecycleViewH(size: Int?) {
        if (size ?: 0 > 8)
            batch_four_device_recycle.layoutParams.height = ScreenUtils.dp2px(this, 220)
        else
            batch_four_device_recycle.layoutParams.height = ScreenUtils.dp2px(this, 100)
    }

    private fun changeDeviceAll() {
        val isSelectAll = getString(R.string.select_all) == batch_four_device_all.text.toString()
        if (isSelectAll)
            batch_four_device_all.text = getString(R.string.cancel)
        else
            batch_four_device_all.text = getString(R.string.select_all)
        setAllSelect(isSelectAll)
    }

    private fun changeGroupSelectView(position: Int) {
        if (lastCheckedGroupPostion != position && groupsByDeviceType?.get(lastCheckedGroupPostion) != null) {
            groupsByDeviceType?.get(lastCheckedGroupPostion)!!.checked = false
        }

        lastCheckedGroupPostion = position
        if (groupsByDeviceType?.get(position) != null)
            groupsByDeviceType?.get(position)!!.checked = !groupsByDeviceType?.get(position)!!.checked
        currentGroup = if (groupsByDeviceType?.get(position)!!.isChecked)
            groupsByDeviceType?.get(position)
        else
            null
        groupAdapter?.notifyDataSetChanged()

        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                changeGroupingCompleteState(selectLights.size)
            }
            DeviceType.SMART_CURTAIN -> {
                changeGroupingCompleteState(selectCurtains.size)
            }
            DeviceType.SMART_RELAY -> {
                changeGroupingCompleteState(selectRelays.size)
            }

        }
    }

    private fun addNewGroup() {
        val textGp = EditText(this)
        StringUtils.initEditTextFilter(textGp)
        textGp.setText(DBUtils.getDefaultNewGroupName())
        //设置光标默认在最后
        textGp.setSelection(textGp.text.toString().length)
        android.app.AlertDialog.Builder(this)
                .setTitle(R.string.create_new_group)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textGp)
                .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                    // 获取输入框的内容
                    if (StringUtils.compileExChar(textGp.text.toString().trim { it <= ' ' })) {
                        ToastUtils.showShort(getString(R.string.rename_tip_check))
                    } else {
                        //往DB里添加组数据
                        var groupType = Constant.DEVICE_TYPE_DEFAULT_ALL
                        when (deviceType) {
                            DeviceType.LIGHT_NORMAL -> groupType = Constant.DEVICE_TYPE_LIGHT_NORMAL
                            DeviceType.LIGHT_RGB -> groupType = Constant.DEVICE_TYPE_LIGHT_RGB
                            DeviceType.SMART_CURTAIN -> groupType = Constant.DEVICE_TYPE_CURTAIN
                            DeviceType.SMART_RELAY -> groupType = Constant.DEVICE_TYPE_CONNECTOR
                        }
                        DBUtils.addNewGroupWithType(textGp.text.toString().trim { it <= ' ' }, groupType)
                        setGroupData(deviceType)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    fun refreshData() {
        val groupList = DBUtils.getGroupsByDeviceType(deviceType)
        //以下是检索组里有多少设备的代码
        for (group in groupList) {
            when (group.deviceType) {
                Constant.DEVICE_TYPE_LIGHT_NORMAL -> {
                    group.deviceCount = DBUtils.getLightByGroupID(group.id).size  //查询改组内设备数量  普通灯和冷暖灯是一个方法  查询什么设备类型有grouplist内容决定
                }
                Constant.DEVICE_TYPE_LIGHT_RGB -> {
                    group.deviceCount = DBUtils.getLightByGroupID(group.id).size  //查询改组内设备数量
                }
                Constant.DEVICE_TYPE_CONNECTOR -> {
                    group.deviceCount = DBUtils.getConnectorByGroupID(group.id).size  //查询改组内设备数量
                }
                Constant.DEVICE_TYPE_CURTAIN -> {
                    group.deviceCount = DBUtils.getCurtainByGroupID(group.id).size  //查询改组内设备数量//窗帘和传感器是一个方法
                }
            }
        }
    }

    private fun setAllSelect(isSelectAll: Boolean) {
        clearSelectors()//清理所有选中
        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                deviceData ?: return
                if (isSelectAll)
                    selectLights = deviceData as MutableList<DbLight>

                for (i in deviceData!!.indices) {
                    deviceData!![i].selected = isSelectAll
                    if (isSelectAll)
                        startBlink(deviceData!![i].belongGroupId, deviceData!![i].meshAddr)
                }
                lightAdapter?.notifyDataSetChanged()
                changeGroupingCompleteState(selectLights.size)
            }
            DeviceType.SMART_CURTAIN -> {
                deviceDataCurtain ?: return

                if (isSelectAll)
                    selectCurtains = deviceDataCurtain as MutableList<DbCurtain>

                for (i in deviceDataCurtain!!.indices) {
                    deviceDataCurtain!![i].selected = isSelectAll
                    if (isSelectAll)
                        startBlink(deviceDataCurtain!![i].belongGroupId, deviceDataCurtain!![i].meshAddr)
                }
                curtainAdapter?.notifyDataSetChanged()
                changeGroupingCompleteState(selectCurtains.size)
            }
            DeviceType.SMART_RELAY -> {
                deviceDataRelay ?: return

                if (isSelectAll)
                    selectRelays = deviceDataRelay as MutableList<DbConnector>

                for (i in deviceDataRelay!!.indices) {
                    deviceDataRelay!![i].selected = isSelectAll
                    if (isSelectAll)
                        startBlink(deviceDataRelay!![i].belongGroupId, deviceDataRelay!![i].meshAddr)
                }
                relayAdapter?.notifyDataSetChanged()

                changeGroupingCompleteState(selectRelays.size)
            }
        }

    }

    /**
     * 让灯开始闪烁
     */
    private fun startBlink(belongGroupId: Long, meshAddr: Int) {
        val group: DbGroup
        if (currentGroup != null) {
            val groupOfTheLight = DBUtils.getGroupByID(belongGroupId)
            group = when (groupOfTheLight) {
                null -> currentGroup!!
                else -> groupOfTheLight
            }
            val groupAddress = group.meshAddr
            Log.d("zcl", "startBlink groupAddresss = $groupAddress")
            val opcode = Opcode.SET_GROUP
            val params = byteArrayOf(0x01, (groupAddress and 0xFF).toByte(), (groupAddress shr 8 and 0xFF).toByte())
            params[0] = 0x01

            if (mBlinkDisposables.get(meshAddr) != null) {
                mBlinkDisposables.get(meshAddr).dispose()
            }
            //每隔1s发一次，就是为了让灯一直闪.
            mBlinkDisposables.put(meshAddr, Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { TelinkLightService.Instance().sendCommandNoResponse(opcode, meshAddr, params) })
        }
    }

    override fun onResume() {
        super.onResume()
        //检测service是否为空，为空则重启
        if (TelinkLightApplication.getApp().connectDevice == null) {
            if (TelinkLightService.Instance() == null)
                mApplication?.startLightService(TelinkLightService::class.java)
            autoConnect()
        }
    }

    override fun performed(event: Event<String>?) {
        when (event?.type) {
            DeviceEvent.STATUS_CHANGED -> this.onDeviceStatusChanged(event as DeviceEvent)
            ErrorReportEvent.ERROR_REPORT -> {
                val info = (event as ErrorReportEvent).args
                onErrorReportNormal(info)
            }
        }
    }

    private fun onDeviceStatusChanged(event: DeviceEvent) {
        val deviceInfo = event.args
        when (deviceInfo.status) {
            LightAdapter.STATUS_LOGIN -> {
                ToastUtils.showLong(getString(R.string.connect_success))
                changeDisplayImgOnToolbar(true)

                val connectDevice = this.mApplication?.connectDevice
                LogUtils.d("directly connection device meshAddr = ${connectDevice?.meshAddress}")
            }
            LightAdapter.STATUS_LOGOUT -> {
                changeDisplayImgOnToolbar(false)
                hideLoadingDialog()
                autoConnect()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        this.mApplication?.removeEventListener(this)
        isAddGroupEmptyView = false
    }

}