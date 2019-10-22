package com.dadoutek.uled.group

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.app.hubert.guide.util.ScreenUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.*
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.scene.SceneEditListAdapter
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.BleUtils
import com.dadoutek.uled.util.StringUtils
import com.dadoutek.uled.widget.RecyclerSpace
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.TelinkApplication
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.light.Parameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_batch_group_four.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * 创建者     ZCL
 * 创建时间   2019/10/16 14:41
 * 描述
 *
 * 更新者     zcl$
 * 更新时间   用于冷暖灯,彩灯,窗帘控制器的批量分组$
 * 更新描述
 */
class BatchGroupFourDevice : TelinkBaseActivity() {
    private var mTelinkLightService: TelinkLightService? = null
    private var footerViewAdd: View? = null
    private lateinit var groupAdapter: SceneEditListAdapter
    private var lightAdapter: BatchFourLightAdapter? = null
    private lateinit var deviceListRelay: ArrayList<DbConnector>
    private lateinit var deviceListCurtains: ArrayList<DbCurtain>
    private lateinit var deviceListLight: ArrayList<DbLight>
    private var groupsByDeviceType: MutableList<DbGroup>? = null
    private var deviceType: Int = 100
    private var lastCheckedGroupPostion: Int = 0
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

    private var isHaveGrouped: Boolean = false


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
        toolbar.setNavigationIcon(R.drawable.icon_top_tab_back)
        toolbar.setNavigationOnClickListener { finish() }

        if (TelinkApplication.getInstance().connectDevice == null)
            image_bluetooth.setImageResource(R.drawable.bluetooth_no)
        else
            image_bluetooth.setImageResource(R.drawable.icon_bluetooth)

        footerViewAdd = LayoutInflater.from(this).inflate(R.layout.add_group, null)
        autoConnect()
    }


    private fun initData() {
        deviceType = intent.getIntExtra(Constant.DEVICE_TYPE, 100)
        setDevicesData(deviceType, isHaveGrouped)
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

        batch_four_group_recycle.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false)
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.divider)))
        batch_four_group_recycle.addItemDecoration(decoration)

        groupAdapter = SceneEditListAdapter(R.layout.scene_group_edit_item, groupsByDeviceType!!)
        groupAdapter.bindToRecyclerView(batch_four_group_recycle)
    }

    /**
     * 设置设备数据
     */
    @SuppressLint("StringFormatInvalid")
    private fun setDevicesData(deviceType: Int, isHaveGrouped: Boolean) {
        when (deviceType) {
            DeviceType.LIGHT_NORMAL -> {
                noGroup = mutableListOf()
                listGroup = mutableListOf()
                deviceListLight = DBUtils.getAllNormalLight()
            }
            DeviceType.LIGHT_RGB -> {
                noGroup = mutableListOf()
                listGroup = mutableListOf()
                deviceListLight = DBUtils.getAllRGBLight()
            }
            DeviceType.SMART_CURTAIN -> {
                noGroupCutain = mutableListOf()
                listGroupCutain = mutableListOf()
                deviceListCurtains = DBUtils.getAllCurtains()
            }
            DeviceType.SMART_RELAY -> {
                noGroupRelay = mutableListOf()
                listGroupRelay = mutableListOf()
                deviceListRelay = DBUtils.getAllRelay()
            }
        }
        batch_four_device_recycle.layoutManager = GridLayoutManager(this, 2)
        batch_four_device_recycle.addItemDecoration(RecyclerSpace(1,getColor(R.color.gray)))

        when (deviceType) {
            DeviceType.LIGHT_RGB, DeviceType.LIGHT_NORMAL -> {
                groupingLight(deviceListLight, noGroup, listGroup)
                deviceData = if (                                      !isHaveGrouped) {
                    noGroup
                } else {
                    listGroup
                }
                if (deviceData!!.size > 8)
                    batch_four_device_recycle.layoutParams.height = ScreenUtils.dp2px(this, 250)

                LogUtils.e("zcl---批量分组4----灯设备信息$deviceData")

                lightAdapter = BatchFourLightAdapter(R.layout.batch_device_item, deviceData!!)

                lightAdapter?.bindToRecyclerView(batch_four_device_recycle)
            }
            DeviceType.SMART_CURTAIN -> {
                deviceListCurtains = DBUtils.getAllCurtains()
            }
            DeviceType.SMART_RELAY -> {
                deviceListRelay = DBUtils.getAllRelay()
            }
        }
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
                RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                            if (!TelinkLightService.Instance().isLogin) {
                                ToastUtils.showLong(R.string.connecting_please_wait)

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


    @SuppressLint("StringFormatMatches")
    private fun groupingLight(deviceListLight: ArrayList<DbLight>, noGroup: MutableList<DbLight>?, listGroup: MutableList<DbLight>?) {
        for (i in deviceListLight.indices) {//获取组名 将分组与未分组的拆分
            if (StringUtils.getLightGroupName(deviceListLight[i]) == TelinkLightApplication.getApp().getString(R.string.not_grouped)) {
                deviceListLight[i].groupName = ""
                noGroup!!.add(deviceListLight[i])
            } else {
                deviceListLight[i].groupName = StringUtils.getLightGroupName(deviceListLight[i])
                listGroup!!.add(deviceListLight[i])
            }
        }

        batch_four_no_group.text = getString(R.string.no_group_device_num, noGroup?.size)
        batch_four_grouped.text = getString(R.string.grouped_num, listGroup?.size)
    }

    private fun initListener() {
        batch_four_group_rg.setOnCheckedChangeListener { _, checkedId ->
            isHaveGrouped = checkedId != R.id.batch_four_grouped
            if (checkedId == R.id.batch_four_no_group) {
                batch_four_no_group_line.visibility = View.VISIBLE
                batch_four_grouped_line.visibility = View.INVISIBLE
            } else {
                batch_four_no_group_line.visibility = View.INVISIBLE
                batch_four_grouped_line.visibility = View.VISIBLE
            }
            setChangeDevice()
        }
        groupAdapter.setOnItemClickListener { _, _, position ->
            //如果点中的不是上次选中的组则恢复未选中状态
            if (lastCheckedGroupPostion != position && groupsByDeviceType?.get(lastCheckedGroupPostion) != null)
                groupsByDeviceType?.get(lastCheckedGroupPostion)!!.checked = false

            lastCheckedGroupPostion = position
            if (groupsByDeviceType?.get(position) != null)
                groupsByDeviceType?.get(position)!!.checked = !groupsByDeviceType?.get(position)!!.checked
            groupAdapter.notifyDataSetChanged()
        }

        lightAdapter?.setOnItemChildClickListener { _, _, position ->
            deviceData?.get(position)!!.selected = !deviceData?.get(position)!!.selected
            lightAdapter?.notifyDataSetChanged()
        }



        grouping_completed.setOnClickListener {
            //判定是否还有灯没有分组，如果没有允许跳转到下一个页面
            if (isAllLightsGrouped()) {
                showToast(getString(R.string.group_completed))
                TelinkLightService.Instance()?.idleMode(true)
                //目前测试调到主页
                doFinish()
            } else {
                showToast(getString(R.string.have_lamp_no_group_tip))
            }
        }
    }

    private fun setChangeDevice() {
        when (deviceType) {
            DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_RGB -> {
                deviceData = if (isHaveGrouped)
                    listGroup
                else
                    noGroup
                lightAdapter?.notifyDataSetChanged()
            }
            DeviceType.SMART_CURTAIN -> {
                noGroupCutain = mutableListOf()
                listGroupCutain = mutableListOf()
                deviceListCurtains = DBUtils.getAllCurtains()
            }
            DeviceType.SMART_RELAY -> {
                noGroupRelay = mutableListOf()
                listGroupRelay = mutableListOf()
                deviceListRelay = DBUtils.getAllRelay()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        enableConnectionStatusListener()
        //检测service是否为空，为空则重启
        if (TelinkLightService.Instance() == null)
            mApplication?.startLightService(TelinkLightService::class.java)
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        registerReceiver(mReceiver, filter)
    }


    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    BluetoothAdapter.STATE_ON -> {
                        retryConnectCount = 0
                        autoConnect()
                    }
                    BluetoothAdapter.STATE_OFF -> {
                    }
                }
            }
        }
    }

    /**
     * 是否所有灯都分了组
     *
     * @return false还有没有分组的灯 true所有灯都已经分组
     */
    private fun isAllLightsGrouped(): Boolean {
        for (j in deviceListLight.indices) {
            if (deviceListLight[j].belongGroupId == allLightId) {
                return false
            }
        }
        return true
    }

    private fun doFinish() {
        //页面跳转前进行分组数据保存
        if (deviceListLight != null && deviceListLight.size > 0) {
            checkNetworkAndSync(this)
        }
        TelinkLightService.Instance()?.idleMode(true)
        finish()
    }


}