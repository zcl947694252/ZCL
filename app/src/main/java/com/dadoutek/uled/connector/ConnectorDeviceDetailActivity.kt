package com.dadoutek.uled.connector

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.*
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.group.BatchGroupFourDeviceActivity
import com.dadoutek.uled.group.InstallDeviceListAdapter
import com.dadoutek.uled.light.DeviceScanningNewActivity
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbConnector
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.model.InstallDeviceModel
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.pir.ScanningSensorActivity
import com.dadoutek.uled.rgb.RGBSettingActivity
import com.dadoutek.uled.scene.NewSceneSetAct
import com.dadoutek.uled.switches.ScanningSwitchActivity
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.util.OtherUtils
import com.dadoutek.uled.util.StringUtils
import com.telink.bluetooth.light.ConnectionStatus
import com.telink.util.MeshUtils.DEVICE_ADDRESS_MAX
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.template_device_detail_list.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
/**
 * 蓝牙接收器列表
 */
class ConnectorDeviceDetailActivity : TelinkBaseActivity(), View.OnClickListener {
    private  var launch: Job? = null
    private var type: Int? = null
    private val lightsData: MutableList<DbConnector> = mutableListOf()
    private var inflater: LayoutInflater? = null
    private var adaper: DeviceDetailConnectorAdapter? = null
    private var currentLight: DbConnector? = null
    private var positionCurrent: Int = 0
    private var canBeRefresh = true
    private val REQ_LIGHT_SETTING: Int = 0x01
    private var acitivityIsAlive = true
    private var mConnectDisposal: Disposable? = null
    private var install_device: TextView? = null
    private var create_group: TextView? = null
    private var create_scene: TextView? = null
    private val SCENE_MAX_COUNT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.template_device_detail_list)
        type = this.intent.getIntExtra(Constant.DEVICE_TYPE, 0)
        inflater = this.layoutInflater
        initData()
        initView()
    }

    override fun onResume() {
        super.onResume()
        inflater = this.layoutInflater
        initData()
        initView()
    }

    private fun initView() {
        recycleView!!.layoutManager = GridLayoutManager(this, 2)
        recycleView!!.itemAnimator = DefaultItemAnimator()
        adaper = DeviceDetailConnectorAdapter(R.layout.template_device_type_item, lightsData)
        adaper!!.onItemChildClickListener = onItemChildClickListener
        adaper!!.bindToRecyclerView(recycleView)
        for (i in lightsData?.indices!!) {
            lightsData!![i].updateIcon()
        }
        install_device = findViewById(R.id.install_device)
        create_group = findViewById(R.id.create_group)
        create_scene = findViewById(R.id.create_scene)
        install_device?.setOnClickListener(onClick)
        create_group?.setOnClickListener(onClick)
        create_scene?.setOnClickListener(onClick)

        add_device_btn.setOnClickListener(this)
        toolbar.setNavigationIcon(R.drawable.icon_return)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbarTv.text = getString(R.string.relay) + " (" + lightsData.size + ")"

    }

    private val onClick = View.OnClickListener {
        when (it.id) {
            R.id.install_device -> {
                showInstallDeviceList()
            }
            R.id.create_group -> {
                dialog_relay?.visibility = View.GONE
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    ToastUtils.showLong(getString(R.string.device_not_connected))
                } else {
                   // addNewGroup()
                    popMain.showAtLocation(window.decorView, Gravity.CENTER,0,0)
                }
            }
            R.id.create_scene -> {
                dialog_relay?.visibility = View.GONE
                val nowSize = DBUtils.sceneList.size
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    ToastUtils.showLong(getString(R.string.device_not_connected))
                } else {
                    if (nowSize >= SCENE_MAX_COUNT) {
                        ToastUtils.showLong(R.string.scene_16_tip)
                    } else {
                        val intent = Intent(this, NewSceneSetAct::class.java)
                        intent.putExtra(Constant.IS_CHANGE_SCENE, false)
                        startActivity(intent)
                    }
                }
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

                .setPositiveButton(getString(android.R.string.ok)) { dialog, which ->
                    // 获取输入框的内容
                    if (StringUtils.compileExChar(textGp.text.toString().trim { it <= ' ' })) {
                        ToastUtils.showLong(getString(R.string.rename_tip_check))
                    } else {
                        //往DB里添加组数据
                        DBUtils.addNewGroupWithType(textGp.text.toString().trim { it <= ' ' }, Constant.DEVICE_TYPE_DEFAULT_ALL)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun showInstallDeviceList() {
        dialog_relay.visibility = View.GONE
        showInstallDeviceList(isGuide, isRgbClick)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_device_btn -> {
                val lastUser = DBUtils.lastUser
                lastUser?.let {
                    if (it.id.toString() != it.last_authorizer_user_id)
                        ToastUtils.showLong(getString(R.string.author_region_warm))
                    else {
                        addDevice()
                    }
                }
            }
        }
    }

    private fun addDevice() {
        intent = Intent(this, DeviceScanningNewActivity::class.java)
        intent.putExtra(Constant.DEVICE_TYPE, DeviceType.SMART_RELAY)
        startActivityForResult(intent, 0)
    }

    var onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
        currentLight = lightsData?.get(position)
        positionCurrent = position
        Opcode.LIGHT_ON_OFF
        val unit = when (view.id) {
            R.id.template_device_icon -> {
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    autoConnect()
                } else {
                    canBeRefresh = true
                    if (currentLight!!.connectionStatus == ConnectionStatus.OFF.value) {
                        if (currentLight!!.productUUID == DeviceType.SMART_CURTAIN) {
                            Commander.openOrCloseCurtain(currentLight!!.meshAddr, true, false)
                        } else {
                            Commander.openOrCloseLights(currentLight!!.meshAddr, true)
                        }

                        currentLight!!.connectionStatus = ConnectionStatus.ON.value
                    } else {
                        if (currentLight!!.productUUID == DeviceType.SMART_CURTAIN) {
                            Commander.openOrCloseCurtain(currentLight!!.meshAddr, false, false)
                        } else {
                            Commander.openOrCloseLights(currentLight!!.meshAddr, false)
                        }
                        currentLight!!.connectionStatus = ConnectionStatus.OFF.value
                    }

                    currentLight!!.updateIcon()
                    DBUtils.updateConnector(currentLight!!)
                    runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
            R.id.template_device_setting -> {
                val lastUser = DBUtils.lastUser
                lastUser?.let {
                    if (it.id.toString() != it.last_authorizer_user_id)
                        ToastUtils.showLong(getString(R.string.author_region_warm))
                    else {
                        if (TelinkLightApplication.getApp().connectDevice == null) {
                            autoConnect()
                        } else {
                            var intent = Intent(this@ConnectorDeviceDetailActivity, ConnectorSettingActivity::class.java)
                            if (currentLight?.productUUID == DeviceType.LIGHT_RGB) {
                                intent = Intent(this@ConnectorDeviceDetailActivity, RGBSettingActivity::class.java)
                                intent.putExtra(Constant.TYPE_VIEW, Constant.TYPE_LIGHT)
                            }
                            intent.putExtra(Constant.LIGHT_ARESS_KEY, currentLight)
                            intent.putExtra(Constant.GROUP_ARESS_KEY, currentLight!!.meshAddr)
                            intent.putExtra(Constant.LIGHT_REFRESH_KEY, Constant.LIGHT_REFRESH_KEY_OK)
                            startActivityForResult(intent, REQ_LIGHT_SETTING)
                        }
                    }
                }
            }
            else -> ToastUtils.showLong(R.string.reconnecting)
        }
    }

    private fun initData() {
        setScanningMode(true)
        lightsData.clear()
        val all_light_data = DBUtils.getAllRelay()
        if (all_light_data.size > 0) {
            var list_group: ArrayList<DbConnector> = ArrayList()
            var no_group: ArrayList<DbConnector> = ArrayList()
            for (i in all_light_data.indices) {
                if (StringUtils.getConnectorGroupName(all_light_data[i]) == TelinkLightApplication.getApp().getString(R.string.not_grouped)) {
                    no_group.add(all_light_data[i])
                } else {
                    list_group.add(all_light_data[i])
                }
            }

            if (no_group.size > 0) {
                for (i in no_group.indices) {
                    lightsData.add(no_group[i])
                }
            }

            if (list_group.size > 0) {
                for (i in list_group.indices) {
                    lightsData.add(list_group[i])
                }
            }
            toolbar!!.tv_function1.visibility = View.VISIBLE
            recycleView.visibility = View.VISIBLE
            no_device_relativeLayout.visibility = View.GONE
            var batchGroup = toolbar.findViewById<TextView>(R.id.tv_function1)
            toolbar!!.findViewById<TextView>(R.id.tv_function1).visibility = View.VISIBLE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
            batchGroup.setText(R.string.batch_group)
            batchGroup.visibility = View.VISIBLE
            batchGroup.setOnClickListener {
                /*    val intent = Intent(this,
                            ConnectorBatchGroupActivity::class.java)
                    intent.putExtra(Constant.IS_SCAN_RGB_LIGHT, true)
                    intent.putExtra(Constant.IS_SCAN_CURTAIN, true)
                    intent.putExtra("relayType", "all_relay")
                    startActivity(intent)*/
                if (TelinkLightApplication.getApp().connectDevice != null) {
                    val lastUser = DBUtils.lastUser
                    lastUser?.let {
                        if (it.id.toString() != it.last_authorizer_user_id)
                            ToastUtils.showLong(getString(R.string.author_region_warm))
                        else {
                            val intent = Intent(this, BatchGroupFourDeviceActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.SMART_RELAY)
                            startActivity(intent)
                        }
                    }
                } else {
                    autoConnect()
                    ToastUtils.showLong(getString(R.string.connecting_tip))
                }
            }
        } else {
            recycleView.visibility = View.GONE
            no_device_relativeLayout.visibility = View.VISIBLE
            toolbar!!.findViewById<TextView>(R.id.tv_function1).visibility = View.GONE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).setOnClickListener {
                val lastUser = DBUtils.lastUser
                lastUser?.let {
                    if (it.id.toString() != it.last_authorizer_user_id)
                        ToastUtils.showLong(getString(R.string.author_region_warm))
                    else {
                        if (dialog_relay?.visibility == View.GONE) {
                            showPopupMenu()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        disposableConnectTimer?.dispose()
    }

    private fun showPopupMenu() {
        dialog_relay?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mConnectDisposal?.dispose()
        canBeRefresh = false
        acitivityIsAlive = false
        launch?.cancel()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        notifyData()
        launch?.cancel()
         launch = GlobalScope.launch {
            //踢灯后没有回调 状态刷新不及时 延时2秒获取最新连接状态
            delay(2500)
            if (this@ConnectorDeviceDetailActivity == null ||
                    this@ConnectorDeviceDetailActivity.isDestroyed ||
                    this@ConnectorDeviceDetailActivity.isFinishing || !acitivityIsAlive) {
            } else {
                autoConnect()
            }
        }
    }

    fun notifyData() {
        val mOldDatas: MutableList<DbConnector> = lightsData
        val mNewDatas: MutableList<DbConnector> = getNewData()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mOldDatas.get(oldItemPosition).id?.equals(mNewDatas.get
                (newItemPosition).id) ?: false
            }

            override fun getOldListSize(): Int {
                return mOldDatas.size ?: 0
            }

            override fun getNewListSize(): Int {
                return mNewDatas.size ?: 0
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val beanOld = mOldDatas[oldItemPosition]
                val beanNew = mNewDatas[newItemPosition]
                return if (beanOld.name != beanNew.name) {
                    return false//如果有内容不同，就返回false
                } else true

            }
        }, true)
        adaper?.let { diffResult.dispatchUpdatesTo(it) }

        lightsData.clear()
        lightsData.addAll(mNewDatas)

        toolbarTv.text = getString(R.string.relay) + " (" + lightsData.size + ")"
//        adaper!!.setNewData(lightsData)
        adaper?.notifyDataSetChanged()

    }

    private fun getNewData(): MutableList<DbConnector> {
        lightsData.clear()
        lightsData.addAll(DBUtils.getAllRelay())
        return lightsData
    }

    fun autoConnect() {
        val size = DBUtils.getAllCurtains().size + DBUtils.allLight.size + DBUtils.allRely.size
        if ( size < 0)
            return
        mConnectDisposal = connect()
                ?.subscribe(
                        {
                            LogUtils.d(it)
                        }
                        ,
                        {
                            LogUtils.d(it)
                        }
                )
    }


}
