package com.dadoutek.uled.group

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.fragment.CWLightFragmentList
import com.dadoutek.uled.fragment.CurtainFragmentList
import com.dadoutek.uled.fragment.RGBLightFragmentList
import com.dadoutek.uled.fragment.RelayFragmentList
import com.dadoutek.uled.gateway.bean.GwStompBean
import com.dadoutek.uled.gateway.util.Base64Utils
import com.dadoutek.uled.intf.CallbackLinkMainActAndFragment
import com.dadoutek.uled.light.NormalSettingActivity
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.dbModel.DBUtils
import com.dadoutek.uled.model.dbModel.DbDeviceName
import com.dadoutek.uled.model.dbModel.DbGroup
import com.dadoutek.uled.model.dbModel.DbLight
import com.dadoutek.uled.model.httpModel.GwModel
import com.dadoutek.uled.model.ItemTypeGroup
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.network.GwGattBody
import com.dadoutek.uled.othersview.BaseFragment
import com.dadoutek.uled.othersview.MainActivity
import com.dadoutek.uled.othersview.ViewPagerAdapter
import com.dadoutek.uled.scene.NewSceneSetAct
import com.dadoutek.uled.stomp.MqttBodyBean
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.util.*
import com.telink.bluetooth.light.ConnectionStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.runOnUiThread
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class GroupListFragment : BaseFragment() {
    private var groupAllLy: ConstraintLayout? = null
    private var disposableTimer: Disposable? = null
    private lateinit var viewContent: View
    private var inflater: LayoutInflater? = null
    private var adapter: GroupListRecycleViewAdapter? = null
    private var mContext: Activity? = null
    private var mApplication: TelinkLightApplication? = null
    private var dataManager: DataManager? = null
    private var gpList: List<ItemTypeGroup>? = null
    private var application: TelinkLightApplication? = null
    private var toolbar: Toolbar? = null
    private var toolbarTv: TextView? = null
    private var showList: List<ItemTypeGroup>? = null
    private var updateLightDisposal: Disposable? = null
    private val SCENE_MAX_COUNT = 100
    private var viewPager: ViewPager? = null
    internal var deviceName: ArrayList<DbDeviceName>? = null
    private lateinit var cwLightFragment: CWLightFragmentList
    private lateinit var rgbLightFragment: RGBLightFragmentList
    private lateinit var curtianFragment: CurtainFragmentList
    private lateinit var relayFragment: RelayFragmentList

    //19-2-20 界面调整
    private var install_device: TextView? = null
    private var create_group: TextView? = null
    private var create_scene: TextView? = null

    //新用户选择的初始安装选项是否是RGB灯
    private var isRgbClick = false

    //是否正在引导
    private var isGuide = false
    private var isFristUserClickCheckConnect = true
    private var totalNum: TextView? = null
    private var btnOn: ImageView? = null
    private var btnOff: ImageView? = null
    private var btnSet: ImageView? = null
    private var allGroup: DbGroup? = null
    private var cwLightGroup: String? = null
    private var switchFragment: String? = null
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var br: BroadcastReceiver
    private var isFirst: Boolean = false
    private var allLightText: TextView? = null
    private var onText: TextView? = null
    private var offText: TextView? = null
    private var fragmentPosition = 0
    private var delete: String? = null
    private var deleteComplete: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mContext = this.activity

        setHasOptionsMenu(true)
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext!!)
        val intentFilter = IntentFilter()
        intentFilter.addAction("showPro")
        intentFilter.addAction("switch_fragment")
        intentFilter.addAction("isDelete")
        intentFilter.addAction("delete_true")

        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                cwLightGroup = intent.getStringExtra("is_delete")//用户删除模式 长按和长按
                switchFragment = intent.getStringExtra("switch_fragment")
                delete = intent.getStringExtra("isDelete")//必定将删除模式恢复掉
                deleteComplete = intent.getStringExtra("delete_true")
                if (cwLightGroup == "true") {
                    toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.GONE
                    toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
                    toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
                    toolbar!!.title = ""
                    changeEnableBackToolbar()
                } else {
                    setBluetoothAndAddVisableDeleteGone()
                }
                if (switchFragment == "true" || delete == "true") {
                    setBluetoothAndAddVisableDeleteGone()
                    sendGroupResterNormal()
                }
                if (deleteComplete == "true") {
                    hideLoadingDialog()
                }
            }
        }
        localBroadcastManager.registerReceiver(br, intentFilter)
    }

    private fun setBluetoothAndAddVisableDeleteGone() {
        toolbarTv!!.setText(R.string.group_title)
        toolbar!!.navigationIcon = null
        toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE//加号
        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE//删除
        SharedPreferencesUtils.setDelete(false)
    }

    private fun sendGroupResterNormal() {
        val intent = Intent("back")
        intent.putExtra("back", "true")
        LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
    }

    private fun changeEnableBackToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.icon_return)
        toolbar!!.setNavigationOnClickListener {
            sendGroupResterNormal()
            setBluetoothAndAddVisableDeleteGone()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = getView(inflater)
        this.initData()
        return view
    }

    override fun refreshGroupData() {
        refreshView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDeviceTypeNavigation()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            isFristUserClickCheckConnect = true
            refreshView()
        } else {
            isFristUserClickCheckConnect = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getView(inflater: LayoutInflater): View {
        this.inflater = inflater

        viewContent = inflater.inflate(R.layout.fragment_group_list, null)

        viewPager = viewContent.findViewById(R.id.list_groups)
        //groupAllLy = viewContent.findViewById(R.id.group_all_ly)

        toolbar = viewContent.findViewById(R.id.toolbar)
        toolbarTv = viewContent.findViewById(R.id.toolbarTv)
        toolbarTv!!.setText(R.string.group_title)

        // allLightText = viewContent.findViewById(R.id.textView6)
        val btnDelete = toolbar!!.findViewById<ImageView>(R.id.img_function2)

        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).setOnClickListener {
            val lastUser = DBUtils.lastUser
            lastUser?.let {
                if (it.id.toString() != it.last_authorizer_user_id)
                    ToastUtils.showLong(getString(R.string.author_region_warm))
                else {
                    isGuide = false
                    if (dialog_pop?.visibility == View.GONE) {
                        showPopupMenu()
                    } else {
                        hidePopupMenu()
                    }
                }
            }
        }

        setHasOptionsMenu(true)

        /*btnOn = viewContent.findViewById(R.id.btn_on)
        btnOff = viewContent.findViewById(R.id.btn_off)
        btnSet = viewContent.findViewById(R.id.btn_set)
        onText = viewContent.findViewById(R.id.tv_on)
        offText = viewContent.findViewById(R.id.tv_off)
        totalNum = viewContent.findViewById(R.id.total_num)
         install_device = viewContent.findViewById(R.id.install_device)
         create_group = viewContent.findViewById(R.id.create_group)
         create_scene = viewContent.findViewById(R.id.create_scene)
         install_device?.setOnClickListener(onClick)
         create_group?.setOnClickListener(onClick)
         create_scene?.setOnClickListener(onClick)
        btnOn?.setOnClickListener(onClick)
         btnOff?.setOnClickListener(onClick)
         btnSet?.setOnClickListener(onClick)
         onText?.setOnClickListener(onClick)
         offText?.setOnClickListener(onClick)*/
        btnDelete.setOnClickListener(onClick)
        // allLightText?.setOnClickListener(onClick)
        // groupAllLy?.setOnClickListener(onClick)
        return viewContent
    }

    /**
     * 是否是删除模式
     */
    fun isDelete(delete: Boolean) {
        if (delete) {
            toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.VISIBLE
        } else {
            toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        this.mApplication = activity!!.application as TelinkLightApplication
        gpList = DBUtils.getgroupListWithType(activity!!)


        val cwNum = DBUtils.getAllNormalLight().size
        val rgbNum = DBUtils.getAllRGBLight().size

        if (cwNum != 0 || rgbNum != 0) {
            totalNum?.text = getString(R.string.total) + (cwNum + rgbNum) + getString(R.string.piece)
        } else {
            totalNum?.text = getString(R.string.total) + (0) + getString(R.string.piece)
        }

        deviceName = ArrayList()
        val stringName = arrayOf(TelinkLightApplication.getApp().getString(R.string.normal_light), TelinkLightApplication.getApp().getString(R.string.rgb_light),
                TelinkLightApplication.getApp().getString(R.string.curtain), TelinkLightApplication.getApp().getString(R.string.relay))

        for (i in stringName.indices) {
            val device = DbDeviceName()
            device.name = stringName[i]
            deviceName!!.add(device)
        }

        showList = ArrayList()
        showList = gpList

        allGroup = DBUtils.getGroupByMeshAddr(0xFFFF)

        if (allGroup != null) {
            if (allGroup!!.connectionStatus == ConnectionStatus.ON.value) {
                btnOn?.setBackgroundResource(R.drawable.icon_open_group)
                btnOff?.setBackgroundResource(R.drawable.icon_down)
                onText?.setTextColor(resources.getColor(R.color.white))
                offText?.setTextColor(resources.getColor(R.color.black_nine))
            } else if (allGroup!!.connectionStatus == ConnectionStatus.OFF.value) {
                btnOn?.setBackgroundResource(R.drawable.icon_down)
                btnOff?.setBackgroundResource(R.drawable.icon_open_group)
                onText?.setTextColor(resources.getColor(R.color.black_nine))
                offText?.setTextColor(resources.getColor(R.color.white))
            }
        }


        application = activity!!.application as TelinkLightApplication
        dataManager = DataManager(TelinkLightApplication.getApp(),
                application!!.mesh.name, application!!.mesh.password)
    }

    /**
     * 初始化内部device的Navigation
     */
    private fun initDeviceTypeNavigation() {
        cwLightFragment = CWLightFragmentList()
        rgbLightFragment = RGBLightFragmentList()
        curtianFragment = CurtainFragmentList()
        relayFragment = RelayFragmentList()

        val fragments: List<Fragment> = listOf(cwLightFragment, rgbLightFragment, curtianFragment, relayFragment)
        val vpAdapter = ViewPagerAdapter(childFragmentManager, fragments)
        viewPager?.adapter = vpAdapter
        viewPager?.currentItem = fragmentPosition

        viewPager?.offscreenPageLimit = 3
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}

            override fun onPageSelected(i: Int) {
                sendGroupResterNormal()
                setBluetoothAndAddVisableDeleteGone()
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })

        bnve.setLargeTextSize(15f)
        bnve.setSmallTextSize(15f)
        bnve.setIconVisibility(false)
        bnve.enableAnimation(false)
        bnve.enableShiftingMode(false)
        bnve.enableItemShiftingMode(false)
        bnve.setupWithViewPager(viewPager)
    }


    /**
     * 添加新的组
     */
    private fun addNewGroup() {
        val textGp = EditText(activity)
        StringUtils.initEditTextFilter(textGp)
        textGp.setText(DBUtils.getDefaultNewGroupName())
        //设置光标默认在最后
        textGp.setSelection(textGp.text.toString().length)
        AlertDialog.Builder(activity)
                .setTitle(R.string.create_new_group)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textGp)
                .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
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
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val inputManager = textGp.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(textGp, 0)
            }
        }, 200)
    }

    /**
     * 刷新数据
     */
    fun refreshView() {

        if (activity != null) {
            cwLightFragment.refreshData()
            curtianFragment.refreshData()
            relayFragment.refreshData()
            rgbLightFragment.refreshData()
            gpList = DBUtils.getgroupListWithType(activity!!)

            showList = ArrayList()
            showList = gpList

            deviceName = ArrayList()

            isFirst = false

            val cwNum = DBUtils.getAllNormalLight().size
            val rgbNum = DBUtils.getAllRGBLight().size

            if (cwNum != 0 || rgbNum != 0) {
                totalNum?.text = getString(R.string.total) + (cwNum + rgbNum) + getString(R.string.piece)
            } else {
                totalNum?.text = getString(R.string.total) + (0) + getString(R.string.piece)
            }

            val stringName = arrayOf(TelinkLightApplication.getApp().getString(R.string.normal_light),
                    TelinkLightApplication.getApp().getString(R.string.rgb_light),
                    TelinkLightApplication.getApp().getString(R.string.curtain),
                    TelinkLightApplication.getApp().getString(R.string.relay))

            for (i in stringName.indices) {
                val device = DbDeviceName()
                device.name = stringName[i]
                deviceName!!.add(device)
            }

            toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
            toolbar!!.navigationIcon = null
            toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
            allGroup = DBUtils.getGroupByMeshAddr(0xFFFF)
            toolbarTv!!.setText(R.string.group_title)
            SharedPreferencesUtils.setDelete(false)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendToGw(isOpen: Boolean) {
        val gateWay = DBUtils.getAllGateWay()
        if (gateWay.size > 0)
            GwModel.getGwList()?.subscribe({
                TelinkLightApplication.getApp().offLine = true
                hideLoadingDialog()
                it.forEach { db ->
                    //网关在线状态，1表示在线，0表示离线
                    if (db.state == 1)
                        TelinkLightApplication.getApp().offLine = false
                }
                if (!TelinkLightApplication.getApp().offLine) {
                    disposableTimer?.dispose()
                    disposableTimer = Observable.timer(7000, TimeUnit.MILLISECONDS).subscribe {
                        hideLoadingDialog()
                        runOnUiThread { ToastUtils.showShort(getString(R.string.gate_way_offline)) }
                    }
                    val low = allGroup!!.meshAddr and 0xff
                    val hight = (allGroup!!.meshAddr shr 8) and 0xff
                    val gattBody = GwGattBody()
                    var gattPar: ByteArray = byteArrayOf()
                    if (isOpen) {
                        gattPar = byteArrayOf(0x11, 0x11, 0x11, 0, 0, low.toByte(), hight.toByte(), Opcode.LIGHT_ON_OFF,
                                0x11, 0x02, 0x01, 0x64, 0, 0, 0, 0, 0, 0, 0, 0)
                        gattBody.ser_id = Constant.SER_ID_GROUP_ALLON
                    } else {
                        gattPar = byteArrayOf(0x11, 0x11, 0x11, 0, 0, low.toByte(), hight.toByte(), Opcode.LIGHT_ON_OFF,
                                0x11, 0x02, 0x00, 0x64, 0, 0, 0, 0, 0, 0, 0, 0)
                        gattBody.ser_id = Constant.SER_ID_GROUP_ALLOFF
                    }

                    val s = Base64Utils.encodeToStrings(gattPar)
                    gattBody.data = s
                    gattBody.cmd = Constant.CMD_MQTT_CONTROL
                    gattBody.meshAddr = allGroup!!.meshAddr
                    sendToServer(gattBody)
                } else {
                    ToastUtils.showShort(getString(R.string.gw_not_online))
                }
            }, {
                hideLoadingDialog()
                ToastUtils.showShort(getString(R.string.gw_not_online))
            })
    }

    @SuppressLint("CheckResult")
    private fun sendToServer(gattBody: GwGattBody) {
        GwModel.sendDeviceToGatt(gattBody)?.subscribe({
            disposableTimer?.dispose()
            LogUtils.v("zcl-----------远程控制-------$it")
        }, {
            disposableTimer?.dispose()
            ToastUtils.showShort(it.message)
            LogUtils.v("zcl-----------远程控制-------${it.message}")
        })
    }


    @SuppressLint("StringFormatInvalid")
    @RequiresApi(Build.VERSION_CODES.O)
    private val onClick = View.OnClickListener {
        var intent: Intent?
        //点击任何一个选项跳转页面都隐藏引导
        hidePopupMenu()
        when (it.id) {
            /*  R.id.group_all_ly -> {
                  if (TelinkLightApplication.getApp().connectDevice != null) {
                      val intentSetting = Intent(context, NormalSettingActivity::class.java)
                      intentSetting.putExtra(Constant.TYPE_VIEW, Constant.TYPE_GROUP)
                      intentSetting.putExtra("group", DBUtils.allGroups[0])
                      startActivityForResult(intentSetting, 1)
                  } else {
                      ToastUtils.showShort(getString(R.string.device_not_connected))
                      val activity = activity as MainActivity
                      activity.autoConnect()
                  }
              }*/
            R.id.install_device -> {
                showInstallDeviceList()
            }
            R.id.create_group -> {
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                } else {
                    //addNewGroup()
                    popMain.showAtLocation(viewContent, Gravity.CENTER, 0, 0)
                }
            }
            R.id.create_scene -> {
                val nowSize = DBUtils.sceneList.size
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                } else {
                    if (nowSize >= SCENE_MAX_COUNT) {
                        ToastUtils.showLong(R.string.scene_16_tip)
                    } else {
                        val intent = Intent(activity, NewSceneSetAct::class.java)
                        intent.putExtra(Constant.IS_CHANGE_SCENE, false)
                        startActivityForResult(intent, CREATE_SCENE_REQUESTCODE)
                    }
                }
            }

            R.id.btn_on, R.id.tv_on -> {
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    // ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                    checkConnect()
                    sendToGw(true)
                } else {
                    if (allGroup != null) {
                        val dstAddr = this.allGroup!!.meshAddr
                        Commander.openOrCloseLights(dstAddr, true)
                        allGroupOpenSuccess()
                    }
                }
            }

            R.id.btn_off, R.id.tv_off -> {
                if (TelinkLightApplication.getApp().connectDevice == null) {
                    //TelinkLightService.Instance()?.isLogin 有时候会出现连接状态下返回false的情况所以不用
                    //ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                    checkConnect()
                    sendToGw(false)
                } else {
                    if (allGroup != null) {
                        val dstAddr = this.allGroup!!.meshAddr
                        Commander.openOrCloseLights(dstAddr, false)
                        allGroupOffSuccess()
                    }
                }
            }

            R.id.btn_set -> {
                val lastUser = DBUtils.lastUser
                lastUser?.let {
                    if (it.id.toString() != it.last_authorizer_user_id)
                        ToastUtils.showLong(getString(R.string.author_region_warm))
                    else {
                        if (TelinkLightApplication.getApp().connectDevice == null) {
                            ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                            checkConnect()
                        } else {
                            intent = Intent(mContext, NormalSettingActivity::class.java)
                            intent!!.putExtra(Constant.TYPE_VIEW, Constant.TYPE_GROUP)
                            intent!!.putExtra("group", allGroup)
                            startActivityForResult(intent, 2)
                        }
                    }
                }
            }

            R.id.img_function2 -> {
                var deleteList: ArrayList<DbGroup> = ArrayList()
                deleteList.addAll(cwLightFragment.getGroupDeleteList())
                deleteList.addAll(rgbLightFragment.getGroupDeleteList())
                deleteList.addAll(curtianFragment.getGroupDeleteList())
                deleteList.addAll(relayFragment.getGroupDeleteList())

                if (deleteList.size > 0) {
                    android.support.v7.app.AlertDialog.Builder(Objects.requireNonNull<FragmentActivity>(mContext as FragmentActivity?))
                            .setMessage(getString(R.string.delete_group_confirm, deleteList[0].name))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                //showLoadingDialog(getString(R.string.deleting))
                                val intent = Intent("delete")
                                intent.putExtra("delete", "true")
                                LocalBroadcastManager.getInstance(this.mContext!!).sendBroadcast(intent)
                            }
                            .setNegativeButton(R.string.btn_cancel, null)
                            .show()
                }
            }
            /*   R.id.textView6 -> {
                   Toast.makeText(activity, R.string.device_page, Toast.LENGTH_LONG).show()
               }*/
        }
    }

    private fun allGroupOpenSuccess() {
        btnOn?.setBackgroundResource(R.drawable.icon_open_group)
        btnOff?.setBackgroundResource(R.drawable.icon_down_group)
        onText?.setTextColor(resources.getColor(R.color.white))
        offText?.setTextColor(resources.getColor(R.color.black_nine))
        updateLights(true, this.allGroup!!)
        val intent = Intent("switch_here")
        intent.putExtra("switch_here", "on")
        LocalBroadcastManager.getInstance(this!!.mContext!!).sendBroadcast(intent)
    }

    private fun allGroupOffSuccess() {
        btnOn?.setBackgroundResource(R.drawable.icon_down_group)
        btnOff?.setBackgroundResource(R.drawable.icon_open_group)
        onText?.setTextColor(resources.getColor(R.color.black_nine))
        offText?.setTextColor(resources.getColor(R.color.white))
        updateLights(false, this.allGroup!!)
        val intent = Intent("switch_here")
        intent.putExtra("switch_here", "false")
        LocalBroadcastManager.getInstance(this!!.mContext!!)
                .sendBroadcast(intent)
    }

    val CREATE_SCENE_REQUESTCODE = 3
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_SCENE_REQUESTCODE) {
            callbackLinkMainActAndFragment?.changeToScene()
        } else if (requestCode == 1) {
            val dbGroup = DBUtils.allGroups[0]
            if (dbGroup.status == 1) {
                btnOn?.setBackgroundResource(R.drawable.icon_open_group)
                btnOff?.setBackgroundResource(R.drawable.icon_down_group)
                onText?.setTextColor(resources.getColor(R.color.white))
                offText?.setTextColor(resources.getColor(R.color.black_nine))
            } else {
                btnOn?.setBackgroundResource(R.drawable.icon_down_group)
                btnOff?.setBackgroundResource(R.drawable.icon_open_group)
                onText?.setTextColor(resources.getColor(R.color.black_nine))
                offText?.setTextColor(resources.getColor(R.color.white))
            }
        }
    }

    var callbackLinkMainActAndFragment: CallbackLinkMainActAndFragment? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CallbackLinkMainActAndFragment) {
            callbackLinkMainActAndFragment = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbackLinkMainActAndFragment = null
    }

    private fun showInstallDeviceList() {
        dialog_pop.visibility = View.GONE
        callbackLinkMainActAndFragment?.showDeviceListDialog(isGuide, isRgbClick)
    }

    private fun checkConnect() {
        try {
            if (TelinkLightApplication.getApp().connectDevice == null) {
                if (isFristUserClickCheckConnect) {
                    val activity = activity as MainActivity
                    activity.autoConnect()
                    isFristUserClickCheckConnect = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLights(isOpen: Boolean, group: DbGroup) {
        updateLightDisposal?.dispose()
        updateLightDisposal = Observable.timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    var lightList: MutableList<DbLight> = ArrayList()

                    if (group.meshAddr == 0xffff) {
                        //            lightList = DBUtils.getAllLight();
                        val list = DBUtils.groupList
                        for (j in list.indices) {
                            lightList.addAll(DBUtils.getLightByGroupID(list[j].id))
                        }
                    } else {
                        lightList = DBUtils.getLightByGroupID(group.id)
                    }

                    if (isOpen) {
                        group.connectionStatus = ConnectionStatus.ON.value
                        DBUtils.updateGroup(group)
                    } else {
                        group.connectionStatus = ConnectionStatus.OFF.value
                        DBUtils.updateGroup(group)
                    }

                    for (dbLight: DbLight in lightList) {
                        if (isOpen) {
                            dbLight.connectionStatus = ConnectionStatus.ON.value
                        } else {
                            dbLight.connectionStatus = ConnectionStatus.OFF.value
                        }
                        DBUtils.updateLightLocal(dbLight)
                    }
                }, {})
    }

    fun notifyDataSetChanged() {
        this.adapter!!.notifyDataSetChanged()
    }

    fun myPopViewClickPosition(x: Float, y: Float) {
        if (x < dialog_pop?.left ?: 0 || y < dialog_pop?.top ?: 0 || y > dialog_pop?.bottom ?: 0) {
            if (dialog_pop?.visibility == View.VISIBLE) {
                Thread {
                    //避免点击过快点击到下层View
                    Thread.sleep(100)
                    GlobalScope.launch(Dispatchers.Main) {
                        hidePopupMenu()
                    }
                }.start()
            } else if (dialog_pop == null) {
                hidePopupMenu()
            }
        }
    }

    private fun showPopupMenu() {
        dialog_pop?.visibility = View.VISIBLE
    }

    private fun hidePopupMenu() {
        if (!isGuide || GuideUtils.getCurrentViewIsEnd(activity!!, GuideUtils.END_GROUPLIST_KEY, false)) {
            dialog_pop?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(br)
    }

    override fun onResume() {
        super.onResume()
        sendGroupResterNormal()
    }

    override fun receviedGwCmd2500(gwStompBean: GwStompBean) {
        when (gwStompBean.ser_id.toInt()) {
            Constant.SER_ID_GROUP_ALLON -> {
                LogUtils.v("zcl-----------远程控制群组开启成功-------")
                disposableTimer?.dispose()
                hideLoadingDialog()
                allGroupOpenSuccess()
            }
            Constant.SER_ID_GROUP_ALLOFF -> {
                LogUtils.v("zcl-----------远程控制群组关闭成功-------")
                disposableTimer?.dispose()
                hideLoadingDialog()
                allGroupOffSuccess()
            }
        }
    }

    override fun receviedGwCmd2500M(gwStompBean: MqttBodyBean) {
        when (gwStompBean.ser_id.toInt()) {
            Constant.SER_ID_GROUP_ALLON -> {
                LogUtils.v("zcl-----------远程控制群组开启成功-------")
                disposableTimer?.dispose()
                hideLoadingDialog()
                allGroupOpenSuccess()
            }
            Constant.SER_ID_GROUP_ALLOFF -> {
                LogUtils.v("zcl-----------远程控制群组关闭成功-------")
                disposableTimer?.dispose()
                hideLoadingDialog()
                allGroupOffSuccess()
            }
        }
    }
}
