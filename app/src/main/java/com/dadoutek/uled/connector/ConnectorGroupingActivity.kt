package com.dadoutek.uled.connector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.GridView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.group.DeviceGroupingAdapter
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.dbModel.DBUtils
import com.dadoutek.uled.model.dbModel.DbConnector
import com.dadoutek.uled.model.dbModel.DbGroup
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.rgb.RGBSettingActivity
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.OtherUtils
import com.dadoutek.uled.util.StringUtils
import com.telink.TelinkApplication
import com.telink.bluetooth.event.NotificationEvent
import com.telink.util.Event
import com.telink.util.EventListener
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.singleLine
import java.util.*


class ConnectorGroupingActivity : TelinkBaseActivity(), EventListener<String> {

    private var inflater: LayoutInflater? = null
    private var adapter: DeviceGroupingAdapter? = null
    private var groupsInit: MutableList<DbGroup>? = null

    private var lights: DbConnector? = null
    private var gpAdress: Int = 0

    private var listView: GridView? = null
    private var isStartGroup = false

//    private var curtain:DbCurtain?=null
//
//    private var address:Int?=null
//
//    private var productUuid:Int?=null
//
//    private var beLongId:Long?=null

    private val itemClickListener = OnItemClickListener { parent, view, position, id ->
        val group = adapter!!.getItem(position)
        if (group != null) {
            if (TelinkApplication.getInstance().connectDevice == null) {
                ToastUtils.showLong(R.string.group_fail)
            } else {
                if(!isStartGroup){
                    isStartGroup=true
                    showLoadingDialog(getString(R.string.grouping))
                    //如果修改分组成功,才改数据库之类的操作
                    allocDeviceGroup(group,{
                        Thread {
                            val sceneIds = getRelatedSceneIds(group.meshAddr)
                            for(i in 0..1){
                                deletePreGroup(lights!!.meshAddr)
                                Thread.sleep(100)
                            }

                            for(i in 0..1){
                                deleteAllSceneByLightAddr(lights!!.meshAddr)
                                Thread.sleep(100)
                            }

                            for(i in 0..1){
                                allocDeviceGroup(group)
                                Thread.sleep(100)
                            }

                            for (sceneId in sceneIds) {
                                val action = DBUtils.getActionBySceneId(sceneId, group.meshAddr)
                                if (action != null) {
                                    for(i in 0..1){
                                        Commander.addScene(sceneId, lights!!.meshAddr, action.color)
                                        Thread.sleep(100)
                                    }
                                }
                            }
                            group.deviceType= lights!!.productUUID.toLong()
                            Log.d("窗帘升级用的productUUID", "deviceType="+group.deviceType.toString()+",address="+lights!!.meshAddr+",productUUID="+lights!!.productUUID)
                            Log.d("message",lights.toString())
                            DBUtils.updateGroup(group)
                            DBUtils.updateConnector(lights!!)
                            runOnUiThread {
                                hideLoadingDialog()
                                ActivityUtils.finishActivity(RGBSettingActivity::class.java)
                                finish()
                            }
                        }.start()
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

    /**
     *  start to group
     *  设置设备分组
     */
    private fun allocDeviceGroup(group: DbGroup, successCallback: () -> Unit, failedCallback: () -> Unit) {
        Commander.addGroup(lights!!.meshAddr, group.meshAddr, {
            successCallback.invoke()
        }, {
            failedCallback.invoke()
        })
        lights!!.belongGroupId = group.id
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


    private fun getRelatedSceneIds(groupAddress: Int): List<Long> {
        val sceneIds = ArrayList<Long>()
        val dbSceneList = DBUtils.sceneList
        sceneLoop@ for (dbScene in dbSceneList) {
            val dbActions = DBUtils.getActionsBySceneId(dbScene.id)
            for (action in dbActions) {
                if (groupAddress == action.groupAddr || 0xffff == action.groupAddr) {
                    sceneIds.add(dbScene.id)
                    continue@sceneLoop
                }
            }
        }
        return sceneIds
    }


    /**
     * 删除指定灯里的所有场景
     *
     * @param lightMeshAddr 灯的mesh地址
     */
    private fun deleteAllSceneByLightAddr(lightMeshAddr: Int) {
        val opcode = Opcode.SCENE_ADD_OR_DEL
        val params: ByteArray
        params = byteArrayOf(0x00, 0xff.toByte())
        TelinkLightService.Instance()?.sendCommandNoResponse(opcode, lightMeshAddr, params)
    }

    /**
     * 删除指定灯的之前的分组
     *
     * @param lightMeshAddr 灯的mesh地址
     */
    private fun deletePreGroup(lightMeshAddr: Int) {
        if (DBUtils.getGroupByID(lights!!.belongGroupId!!) != null) {
            val groupAddress = DBUtils.getGroupByID(lights!!.belongGroupId!!)?.meshAddr
            val opcode = Opcode.SET_GROUP
            val params = byteArrayOf(0x00, (groupAddress!! and 0xFF).toByte(), //0x00表示删除组
                    (groupAddress shr 8 and 0xFF).toByte())
            TelinkLightService.Instance()?.sendCommandNoResponse(opcode, lightMeshAddr, params)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        this.mApplication = this.application as TelinkLightApplication
        this.mApplication!!.addEventListener(NotificationEvent.GET_GROUP, this)
        //        this.mApplication.addEventListener(NotificationEvent.GET_SCENE, this);

        this.setContentView(R.layout.activity_device_grouping)


        initData()
        initView()

        this.getDeviceGroup()
        //        getScene();
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbarTv?.text = getString(R.string.activity_device_grouping)
        toolbar.setNavigationIcon(R.drawable.icon_return)
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val moreIcon = ContextCompat.getDrawable(toolbar.context, R.drawable.abc_ic_menu_overflow_material)
        if (moreIcon != null) {
            moreIcon.setColorFilter(ContextCompat.getColor(toolbar.context, R.color.black), PorterDuff.Mode.SRC_ATOP)
            toolbar.overflowIcon = moreIcon
        }

        this.inflater = this.layoutInflater
        listView = this.findViewById(R.id.list_groups)
        listView!!.onItemClickListener = this.itemClickListener

        adapter = DeviceGroupingAdapter(groupsInit!!, this)
        listView!!.adapter = adapter
    }

    private fun initData() {
//        this.type=this.intent.getStringExtra(Constant.TYPE_VIEW)
//        if(type.equals(Constant.CURTAINS_KEY)){
//            this.curtain=this.intent.extras?.get("curtain")as DbCurtain
//        }else if(type.equals(Constant.LIGHT_KEY)){
//            this.light = this.intent.extras?.get("light") as DbLight
//        }

//        this.productUuid=this.intent.getIntExtra("uuid",0)
//        this.address=this.intent.getIntExtra("gpAddress",0)
//        this.beLongId=this.intent.getLongExtra("belongId",0)
        this.lights = this.intent.extras?.get("light") as DbConnector
//        Log.d("message", productUuid!!.toString()+"====>"+address!!.toString()+"--->"+beLongId!!.toString())

        groupsInit = ArrayList()
        val list = DBUtils.groupList
        filter(list)
//        groupsInit = DBUtils.groupList
    }

    private fun filter(list: MutableList<DbGroup>) {
        groupsInit?.clear()
        for (i in list.indices) {
            if (lights?.productUUID == DeviceType.SMART_RELAY){
                if (OtherUtils.isConnector(list[i])) {
                    groupsInit?.add(list[i])
                }
            }

            if(OtherUtils.isDefaultGroup(list[i])){
                groupsInit?.add(list[i])
            }
        }
    }

    private fun getScene() {
        val opcode = 0xc0.toByte()
        val dstAddress = lights!!.meshAddr
        val params = byteArrayOf(0x10, 0x00)


        if (dstAddress != null) {
            TelinkLightService.Instance()?.sendCommandNoResponse(opcode, dstAddress, params)
        }

        TelinkLightService.Instance()?.updateNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mApplication!!.removeEventListener(this)
    }

    private fun getDeviceGroup() {
        TelinkLightService.Instance()?.updateNotification()
        val opcode = 0xDD.toByte()
        val dstAddress = lights!!.meshAddr
        val params = byteArrayOf(0x08, 0x01)

        if (dstAddress != null) {
            TelinkLightService.Instance()?.sendCommandNoResponse(opcode, dstAddress, params)
        }

        TelinkLightService.Instance()?.updateNotification()

    }

    private fun allocDeviceGroup(group: DbGroup) {
        val groupAddress = group.meshAddr
        val dstAddress = lights!!.meshAddr
        val opcode = 0xD7.toByte()
        val params = byteArrayOf(0x01, (groupAddress and 0xFF).toByte(), (groupAddress shr 8 and 0xFF).toByte())
        params[0] = 0x01

        if (dstAddress != null) {
            TelinkLightService.Instance()?.sendCommandNoResponse(opcode, dstAddress, params)
        }

        lights!!.belongGroupId = group.id

    }

    override fun performed(event: Event<String>) {
        val e = event as NotificationEvent
        val info = e.args

        val srcAddress = info.src and 0xFF
        val params = info.params

        if (srcAddress != lights!!.meshAddr)
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

    private fun addNewGroup() {
        val textGp = EditText(this)
        textGp.singleLine = true
        StringUtils.initEditTextFilter(textGp)
        textGp.setText(DBUtils.getDefaultNewGroupName())
        //设置光标默认在最后
        textGp.setSelection(textGp.getText().toString().length)
        AlertDialog.Builder(this@ConnectorGroupingActivity)
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
                .setNegativeButton(getString(R.string.btn_cancel)) { dialog, which -> dialog.dismiss() }.show()
    }

    private fun refreshView() {
        val list = DBUtils.groupList
        filter(list)

        adapter = DeviceGroupingAdapter(groupsInit!!, this)
        listView!!.adapter = this.adapter
        adapter!!.notifyDataSetChanged()
    }


    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@ConnectorGroupingActivity)
        builder.setTitle(R.string.group_not_change_tip)
        builder.setPositiveButton(android.R.string.ok) { dialog, which -> finish() }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()

            R.id.menu_install -> popMain.showAtLocation(window.decorView,Gravity.CENTER,0,0)  /*addNewGroup()*/
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group_fragment, menu)
        return true
    }

    companion object {

        private val UPDATE = 1
    }
}
