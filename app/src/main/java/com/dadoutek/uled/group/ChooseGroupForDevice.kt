package com.dadoutek.uled.group

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.GridView
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbGroup
import com.dadoutek.uled.model.DbModel.DbLight
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.util.OtherUtils
import com.dadoutek.uled.util.StringUtils
import com.telink.TelinkApplication
import com.telink.bluetooth.event.NotificationEvent
import com.telink.util.Event
import com.telink.util.EventListener
import kotlinx.android.synthetic.main.activity_device_grouping.*
import kotlinx.coroutines.GlobalScope


/**
 * 修改设备分组的页面（灯，接收器之类的）
 */
class ChooseGroupForDevice : TelinkBaseActivity(), EventListener<String> {
    companion object {
        private val UPDATE = 1
    }

    private var inflater: LayoutInflater? = null
    private var adapter: DeviceGroupingAdapter? = null
    private var mGroupList: MutableList<DbGroup> = mutableListOf()

    private lateinit var mLight: DbLight
    private var type: String? = null


    private val itemClickListener = OnItemClickListener { _, _, position, _ ->
        val group = adapter!!.getItem(position)
        group?.deviceType = mLight.productUUID.toLong()
        if (group != null) {
            if (TelinkApplication.getInstance().connectDevice == null) {
                ToastUtils.showLong(R.string.group_fail)
            } else {
                showLoadingDialog(getString(R.string.grouping))

                GlobalScope.let {
                    allocDeviceGroup(group, {
                        mLight.belongGroupId = group.id
                        group.deviceType = mLight.productUUID.toLong()
                        updateGroupResult(mLight, group)
                        runOnUiThread {
                            hideLoadingDialog()
                            finish()
                            ToastUtils.showLong(getString(R.string.grouping_success_tip))
                        }
                    }, {
                        runOnUiThread {
                            hideLoadingDialog()
                            ToastUtils.showLong(R.string.group_failed)
                        }
                    })
                }
            }
        }
    }

    private fun updateGroupResult(light: DbLight, group: DbGroup) {
        light.hasGroup = true
        light.belongGroupId = group.id
        light.name = light.name
        DBUtils.updateLight(light)
        if (group != null)
            DBUtils.updateGroup(group!!)//更新组类型
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE -> adapter!!.notifyDataSetChanged()
            }
        }
    }

    private var mApplication: TelinkLightApplication? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        this.mApplication = this.application as TelinkLightApplication
        this.setContentView(R.layout.activity_device_grouping)

        initData()
        initView()
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.activity_device_grouping)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        this.inflater = this.layoutInflater
        list_groups!!.onItemClickListener = this.itemClickListener

        adapter = DeviceGroupingAdapter(mGroupList!!,this)
        list_groups!!.adapter = adapter
    }

    private fun initData() {
        this.type = this.intent.getStringExtra(Constant.TYPE_VIEW)
        this.mLight = this.intent.extras?.get("light") as DbLight
        mGroupList.clear()
        mGroupList.addAll(DBUtils.getGroupsByDeviceType(mLight.productUUID))
        mGroupList.addAll(DBUtils.getGroupsByDeviceType(0))

        setGroupChecked()
    }

    private fun filter(list: MutableList<DbGroup>) {
        mGroupList?.clear()
        for (i in list.indices) {
            if (mLight!!.productUUID == DeviceType.LIGHT_NORMAL ||
                    mLight!!.productUUID == DeviceType.LIGHT_NORMAL_OLD ||
                    mLight!!.productUUID == 0x00) {
                if (OtherUtils.isNormalGroup(list[i])) {
                    mGroupList?.add(list[i])
                }
            } else if (mLight!!.productUUID == DeviceType.LIGHT_RGB) {
                if (OtherUtils.isRGBGroup(list[i])) {
                    mGroupList?.add(list[i])
                }
            }

            if (OtherUtils.isDefaultGroup(list[i])) {
                mGroupList?.add(list[i])
            }
        }
    }

    /**
     * 把所在的组，设置成checked，用来标识当前设备处于哪个分组
     */
    private fun setGroupChecked() {
        for (group in mGroupList) {
            group.checked = group.id == mLight.belongGroupId
        }
    }


    /**
     *  start to group
     *  设置设备分组
     */
    private fun allocDeviceGroup(group: DbGroup, successCallback: () -> Unit, failedCallback: () -> Unit) {
        Commander.addGroup(mLight.meshAddr, group.meshAddr, {
            successCallback.invoke()
        }, {
            failedCallback.invoke()
        })
        mLight.belongGroupId = group.id
    }

    override fun performed(event: Event<String>) {
        val e = event as NotificationEvent
        val info = e.args

        val srcAddress = info.src and 0xFF
        val params = info.params

        if (srcAddress != mLight!!.meshAddr)
            return

        val count = this.adapter!!.count

        var group: DbGroup?

        for (i in 0 until count) {
            group = this.adapter!!.getItem(i)

            if (group != null)
                group.checked = false
        }

        var groupAddress: Int
        val len = params.size

        for (j in 0 until len) {

            groupAddress = params[j].toInt()

            if (groupAddress == 0x00 || groupAddress == 0xFF)
                break

            groupAddress = groupAddress or 0x8000

            group = this.adapter!![groupAddress]

            if (group != null) {
                group.checked = true
            }
        }
        mHandler.obtainMessage(UPDATE).sendToTarget()
    }

    /**
     * 添加新的分组进列表
     */
    private fun addNewGroup() {
        val textGp = EditText(this)
        StringUtils.initEditTextFilter(textGp)
        textGp.setText(DBUtils.getDefaultNewGroupName())
        //设置光标默认在最后
        textGp.setSelection(textGp.text.toString().length)
        AlertDialog.Builder(this@ChooseGroupForDevice)
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
                        refreshView()
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    /**
     * 刷新UI
     */
    private fun refreshView() {
        val list = DBUtils.groupList
        filter(list)

        adapter = DeviceGroupingAdapter(mGroupList!!, this)
        list_groups!!.adapter = this.adapter
        adapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@ChooseGroupForDevice)
        builder.setTitle(R.string.group_not_change_tip)
        builder.setPositiveButton(android.R.string.ok) { _, _ -> finish() }
        builder.setNegativeButton(R.string.btn_cancel) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()

            R.id.menu_install -> addNewGroup()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group_fragment, menu)
        return true
    }

}
