package com.dadoutek.uled.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.smssdk.SMSSDK
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dadoutek.uled.R
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.gateway.bean.GwStompBean
import com.dadoutek.uled.group.InstallDeviceListAdapter
import com.dadoutek.uled.group.TypeListAdapter
import com.dadoutek.uled.intf.SyncCallback
import com.dadoutek.uled.light.DeviceScanningNewActivity
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DeviceType
import com.dadoutek.uled.model.HttpModel.AccountModel
import com.dadoutek.uled.model.InstallDeviceModel
import com.dadoutek.uled.model.SharedPreferencesHelper
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.othersview.InstructionsForUsActivity
import com.dadoutek.uled.pir.ScanningSensorActivity
import com.dadoutek.uled.stomp.MqttBodyBean
import com.dadoutek.uled.stomp.StompManager
import com.dadoutek.uled.stomp.model.QrCodeTopicMsg
import com.dadoutek.uled.switches.ScanningSwitchActivity
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.telink.TelinkApplication
import com.telink.bluetooth.LeBluetooth
import com.telink.bluetooth.event.DeviceEvent
import com.telink.bluetooth.event.ErrorReportEvent
import com.telink.bluetooth.light.DeviceInfo
import com.telink.bluetooth.light.ErrorReportInfo
import com.telink.bluetooth.light.LightAdapter
import com.telink.bluetooth.light.Parameters
import com.telink.util.EventListener
import com.telink.util.MeshUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import org.fusesource.mqtt.client.MQTT
import org.jetbrains.anko.singleLine
import java.util.concurrent.TimeUnit

abstract class TelinkBaseActivity : AppCompatActivity() {
    private var viewInstall: View? = null
    private var installTitleTv: TextView? = null
    private var netWorkChangReceiver: NetWorkChangReceiver? = null
    private var isResume: Boolean = false
    private var mConnectDisposable: Disposable? = null
    private var changeRecevicer: ChangeRecevicer? = null
    private var mStompListener: Disposable? = null
    private var authorStompClient: Disposable? = null
    var pop: PopupWindow? = null
    var popView: View? = null
    private var codeWarmDialog: AlertDialog? = null
    private var singleLogin: AlertDialog? = null
    private var payload: String? = null
    var stompLifecycleDisposable: Disposable? = null
    var singleLoginTopicDisposable: Disposable? = null
    var codeStompClient: Disposable? = null
    private lateinit var stompRecevice: StompReceiver
    private var locationServiceDialog: android.support.v7.app.AlertDialog? = null
    private lateinit var mStompManager: StompManager
    private var loadDialog: Dialog? = null
    private var mApplication: TelinkLightApplication? = null
    private var mScanDisposal: Disposable? = null
    var disposableConnectTimer: Disposable? = null
    var isScanning = false
    private var upDateTimer: Disposable? = null
    private lateinit var dialog: Dialog
    private var adapterType: TypeListAdapter? = null
    private var list: MutableList<String>? = null
    private var groupType: Long = 0L
    private var dialogGroupName: TextView? = null
    private var dialogGroupType: TextView? = null
    open lateinit var popMain: PopupWindow
    private val SHOW_LOADING_DIALOG_DELAY: Long = 300 //ms
    var installDialog: AlertDialog? = null
    var isRgbClick = false
    var clickRgb: Boolean = false
    var installId = 0
    var stepOneText: TextView? = null
    var stepTwoText: TextView? = null
    var stepThreeText: TextView? = null
    var switchStepOne: TextView? = null
    var switchStepTwo: TextView? = null
    var swicthStepThree: TextView? = null
    private var installHelpe: TextView? = null
    val INSTALL_NORMAL_LIGHT = 0
    val INSTALL_RGB_LIGHT = 1
    val INSTALL_SWITCH = 2
    val INSTALL_SENSOR = 3
    val INSTALL_CURTAIN = 4
    val INSTALL_CONNECTOR = 5
    val INSTALL_GATEWAY = 6
    var isGuide: Boolean = false
    lateinit var hinitOne: TextView
    lateinit var popFinish: PopupWindow
    lateinit var cancelf: Button
    lateinit var confirmf: Button
    var isEdite: Boolean = false
    var type: Int? = null
    var showDialogHardDelete: AlertDialog? = null

    var renameCancel: TextView? = null
    var renameConfirm: TextView? = null
    var renameEt: EditText? = null
    var popReNameView: View? = null
    lateinit var renameDialog: Dialog

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mApplication = this.application as TelinkLightApplication
        enableConnectionStatusListener()    //尽早注册监听
        //注册网络状态监听广播
        netWorkChangReceiver = NetWorkChangReceiver()
        var filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netWorkChangReceiver, filter)
        initOnLayoutListener()//加载view监听
        makeDialogAndPop()
        makeRenamePopuwindow()
        makeStopScanPop()
        makeDialog()
        initStompReceiver()
        initChangeRecevicer()
    }

    private fun initChangeRecevicer() {
        changeRecevicer = ChangeRecevicer()
        val filter = IntentFilter()
        filter.addAction("STATUS_CHANGED")
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 2
//        registerReceiver(changeRecevicer, filter)
    }

    private fun startTimerUpdate() {
        upDateTimer = Observable.interval(0, 5, TimeUnit.SECONDS).subscribe {
            if (netWorkCheck(this))//oom溢出
                CoroutineScope(Dispatchers.IO).launch {
                    SyncDataPutOrGetUtils.syncPutDataStart(this@TelinkBaseActivity, object : SyncCallback {
                        override fun start() {}
                        override fun complete() {
                            LogUtils.v("zcl-----------默认上传成功-------")
                        }

                        override fun error(msg: String?) {}
                    })
                }
        }
    }

    // 网络连接判断
    open fun netWorkCheck(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info: NetworkInfo? = cm.activeNetworkInfo
        return info?.isConnected ?: false
    }


    fun stopTimerUpdate() {
        upDateTimer?.dispose()
        upDateTimer = null
    }

    /**
     * 自动重连
     */
    fun autoConnectAll() {
        val deviceTypes = mutableListOf(DeviceType.LIGHT_NORMAL, DeviceType.LIGHT_NORMAL_OLD, DeviceType.LIGHT_RGB)
        val size = DBUtils.getAllCurtains().size + DBUtils.allLight.size + DBUtils.allRely.size
        if (size > 0) {
            ToastUtils.showLong(getString(R.string.connecting_tip))
            mConnectDisposable?.dispose()
            mConnectDisposable = connect(deviceTypes = deviceTypes, fastestMode = true, retryTimes = 10)
                    ?.subscribe({
                        LogUtils.d("connection success")
                    }, {
                        LogUtils.d("connect failed")
                    }
                    )
        }
    }

    private fun makeDialogAndPop() {
        singleLogin = AlertDialog.Builder(this)
                .setTitle(R.string.other_device_login)
                .setMessage(getString(R.string.single_login_warm))
                .setCancelable(false)
                .setOnDismissListener {
                    restartApplication()
                }.setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    restartApplication()
                }.create()

        popView = LayoutInflater.from(this).inflate(R.layout.code_warm, null)
        pop = PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        pop?.let {
            it.isFocusable = true // 设置PopupWindow可获得焦点
            it.isTouchable = true // 设置PopupWindow可触摸补充：
            it.isOutsideTouchable = false
        }
    }

    private fun makeDialog() {
        dialog = Dialog(this@TelinkBaseActivity)
        list = mutableListOf(getString(R.string.normal_light), getString(R.string.rgb_light), getString(R.string.curtain), getString(R.string.relay))
        adapterType = TypeListAdapter(R.layout.item_group, list!!)

        val popView = View.inflate(this@TelinkBaseActivity, R.layout.dialog_add_group, null)
        popMain = PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        popMain.isFocusable = true // 设置PopupWindow可获得焦点
        popMain.isTouchable = true // 设置PopupWindow可触摸补充：
        popMain.isOutsideTouchable = false

        val recyclerView = popView.findViewById<RecyclerView>(R.id.pop_recycle)
        recyclerView.layoutManager = LinearLayoutManager(this@TelinkBaseActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapterType

        adapterType?.bindToRecyclerView(recyclerView)

        val dialogGroupTypeArrow = popView.findViewById<ImageView>(R.id.dialog_group_type_arrow)
        val dialogGroupCancel = popView.findViewById<TextView>(R.id.dialog_group_cancel)
        val dialogGroupOk = popView.findViewById<TextView>(R.id.dialog_group_ok)
        dialogGroupType = popView.findViewById<TextView>(R.id.dialog_group_type)
        dialogGroupName = popView.findViewById<TextView>(R.id.dialog_group_name)

        dialogGroupTypeArrow.setOnClickListener {
            if (recyclerView.visibility == View.GONE)
                recyclerView.visibility = View.VISIBLE
            else
                recyclerView.visibility = View.GONE

        }
        dialogGroupType?.setOnClickListener {
            if (recyclerView.visibility == View.GONE)
                recyclerView.visibility = View.VISIBLE
            else
                recyclerView.visibility = View.GONE

        }
        dialogGroupCancel.setOnClickListener { PopUtil.dismiss(popMain) }
        dialogGroupOk.setOnClickListener {
            addNewTypeGroup()
        }

        adapterType?.setOnItemClickListener { _, _, position ->
            dialogGroupType?.text = list!![position]
            recyclerView.visibility = View.GONE
            when (position) {
                0 -> groupType = Constant.DEVICE_TYPE_LIGHT_NORMAL
                1 -> groupType = Constant.DEVICE_TYPE_LIGHT_RGB
                2 -> groupType = Constant.DEVICE_TYPE_CURTAIN
                3 -> groupType = Constant.DEVICE_TYPE_CONNECTOR
            }
        }
        popMain.setOnDismissListener {
            recyclerView.visibility = View.GONE
            dialogGroupType?.text = getString(R.string.not_type)
            dialogGroupName?.text = ""
            groupType = 0
        }
    }

    private fun addNewTypeGroup() {
        // 获取输入框的内容
        if (StringUtils.compileExChar(dialogGroupName?.text.toString().trim { it <= ' ' })) {
            ToastUtils.showLong(getString(R.string.rename_tip_check))
        } else {
            if (groupType == 0L) {
                ToastUtils.showLong(getString(R.string.select_type))
            } else {
                //往DB里添加组数据
                DBUtils.addNewGroupWithType(dialogGroupName?.text.toString().trim { it <= ' ' }, groupType)
                refreshGroupData()
                PopUtil.dismiss(popMain)
            }
        }
    }

    open fun refreshGroupData() {

    }

    //增加全局监听蓝牙开启状态
    private fun showOpenBluetoothDialog(context: Context) {
        val dialogTip = AlertDialog.Builder(context)
        dialogTip.setMessage(R.string.openBluetooth)
        dialogTip.setPositiveButton(android.R.string.ok) { _, _ ->
            LeBluetooth.getInstance().enable(applicationContext)
        }
        dialogTip.setCancelable(true)
        dialogTip.create().show()
    }

    /**
     * 改变Toolbar上的图片和状态
     * @param isConnected       是否是连接状态
     */
    fun changeDisplayImgOnToolbar(isConnected: Boolean) {
        if (isConnected) {
            if (toolbar != null) {
                toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).setImageResource(R.drawable.icon_bluetooth)
                toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).isEnabled = false
            }
        } else {
            if (toolbar != null) {
                toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).setImageResource(R.drawable.bluetooth_no)
                toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).isEnabled = true
                toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).setOnClickListener {
                    val dialog = BluetoothConnectionFailedDialog(this, R.style.Dialog)
                    dialog.show()
                }
            }
        }
    }

    //打开基类的连接状态变化监听
    private fun enableConnectionStatusListener() { //todo 传感器上传下拉  外部不退出
        this.mApplication?.addEventListener(DeviceEvent.STATUS_CHANGED, statusChangedListener)
        //LogUtils.d("enableConnectionStatusListener, current listeners = ${mApplication?.mEventBus?.mEventListeners}")
    }

    //关闭基类的连接状态变化监听
    fun disableConnectionStatusListener() {
        this.mApplication?.removeEventListener(DeviceEvent.STATUS_CHANGED, statusChangedListener)
    }

    private val statusChangedListener = EventListener<String?> { event ->
        when (event.type) {
            DeviceEvent.STATUS_CHANGED -> {
                onDeviceStatusChanged(event as DeviceEvent)
            }
        }
    }

    private fun onDeviceStatusChanged(event: DeviceEvent) {
        val deviceInfo = event.args
        when (deviceInfo.status) {
            LightAdapter.STATUS_LOGIN -> {
                LogUtils.v("zcl---baseactivity收到登入广播")
                GlobalScope.launch(Dispatchers.Main) {
                    if (!isScanning)
                        ToastUtils.showLong(getString(R.string.connect_success))
                    changeDisplayImgOnToolbar(true)
                }
                afterLogin()
                val connectDevice = this.mApplication?.connectDevice
                LogUtils.d("directly connection device meshAddr = ${connectDevice?.meshAddress}")
                //if (!isScanning)
                // RecoverMeshDeviceUtil.addDevicesToDb(deviceInfo)//  如果已连接的设备不存在数据库，则创建。 主要针对扫描的界面和会连接的界面
            }
            LightAdapter.STATUS_LOGOUT -> {
//              LogUtils.v("zcl---baseactivity收到登出广播")
                GlobalScope.launch(Dispatchers.Main) {
                    changeDisplayImgOnToolbar(false)
                    afterLoginOut()
                }
            }

            LightAdapter.STATUS_CONNECTING -> {
                if (!isScanning)
                    ToastUtils.showLong(R.string.connecting_please_wait)
            }
        }
    }


    open fun afterLoginOut() {

    }

    open fun afterLogin() {

    }

    override fun onResume() {
        super.onResume()
        if (!LeBluetooth.getInstance().enable(applicationContext))
            TmtUtils.midToastLong(this, getString(R.string.open_blutooth_tip))
        isResume = true
       if (TelinkLightApplication.getApp().mStompManager?.mStompClient?.isConnected !=true)
           TelinkLightApplication.getApp().initStompClient()
        val lastUser = DBUtils.lastUser
        lastUser?.let {
            if (it.id.toString() == it.last_authorizer_user_id)//没有上传数据或者当前区域不是自己的区域
                startTimerUpdate()
        }


        if (LeBluetooth.getInstance().isEnabled) {
            if (TelinkLightApplication.getApp().connectDevice != null/*lightService?.isLogin == true*/) {
                changeDisplayImgOnToolbar(true)
            } else {
                changeDisplayImgOnToolbar(false)
            }
        } else {
            changeDisplayImgOnToolbar(false)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disableConnectionStatusListener()
        mConnectDisposable?.dispose()
        loadDialog?.dismiss()
        unregisterReceiver(stompRecevice)
        unregisterReceiver(netWorkChangReceiver)
        SMSSDK.unregisterAllEventHandler()
        stopTimerUpdate()
    }

    open fun initOnLayoutListener() {
        var view = window.decorView
        var viewTreeObserver = view.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener {
            view.viewTreeObserver.removeOnGlobalLayoutListener {}
        }
    }

    fun showToast(s: CharSequence) {
        ToastUtils.showLong(s)
    }

    fun showLoadingDialog(content: String) {
        val inflater = LayoutInflater.from(this)
        val v = inflater.inflate(R.layout.dialogview, null)

        val layout = v.findViewById<View>(R.id.dialog_view) as LinearLayout
        val tvContent = v.findViewById<View>(R.id.tvContent) as TextView
        tvContent.text = content

        if (loadDialog == null) {
            loadDialog = Dialog(this, R.style.FullHeightDialog)
        }

        if (loadDialog!!.isShowing)
            return

        //loadDialog没显示才把它显示出来
        if (!this.isFinishing && !loadDialog!!.isShowing) {
            loadDialog!!.setCancelable(false)
            loadDialog!!.setCanceledOnTouchOutside(false)
            loadDialog!!.setContentView(layout)
            if (!this.isDestroyed) {
                loadDialog!!.show()
            }
        }
    }

    fun hideLoadingDialog() {
        if (!this@TelinkBaseActivity.isFinishing && loadDialog != null && loadDialog!!.isShowing) {
            loadDialog?.dismiss()
        }
    }

    fun compileExChar(str: String): Boolean {
        return StringUtils.compileExChar(str)
    }


    override fun onPause() {
        super.onPause()
        stopTimerUpdate()
        showDialogHardDelete?.dismiss()
        mConnectDisposable?.dispose()
        isResume = false
    }


    /**
     * 检查网络上传数据
     * 如果没有网络，则弹出网络设置对话框
     */
    protected fun checkNetworkAndSync(activity: Activity?) {
        if (!NetWorkUtils.isNetworkAvalible(activity!!)) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.network_tip_title)
                    .setMessage(R.string.net_disconnect_tip_message)
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ ->
                        // 跳转到设置界面
                        activity.startActivityForResult(Intent(Settings.ACTION_WIRELESS_SETTINGS), 0)
                    }.create().show()
        } else {
            SyncDataPutOrGetUtils.syncPutDataStart(activity, syncCallback)
        }
    }

    internal var syncCallbackGet: SyncCallback = object : SyncCallback {
        override fun start() {}
        override fun complete() {}

        @SuppressLint("CheckResult")
        override fun error(msg: String) {
        }
    }

    /**
     * 上传回调
     */
    private var syncCallback: SyncCallback = object : SyncCallback {
        override fun start() {
            showLoadingDialog(getString(R.string.tip_start_sync))
        }

        override fun complete() {
            hideLoadingDialog()
        }

        override fun error(msg: String) {
            hideLoadingDialog()
            if (msg != null && msg != "null")
                ToastUtils.showLong(msg)
        }
    }

    //重启app并杀死原进程
    open fun restartApplication() {
        TelinkApplication.getInstance().removeEventListeners()
        SharedPreferencesHelper.putBoolean(this, Constant.IS_LOGIN, false)
        TelinkLightApplication.getApp().releseStomp()
        AppUtils.relaunchApp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "StringFormatInvalid")
    private fun makeCodeDialog(type: Int, phone: Any, rid: Any, regionName: Any, lastRegionId: Int? = DBUtils.lastUser?.last_region_id?.toInt()) {
        //移交码为0授权码为1
        var title: String? = null
        var recever: String? = null

        when (type) {
            -1 -> {
                title = getString(R.string.transfer_region_success)
                recever = getString(R.string.recevicer)
            }
            0 -> {
                title = getString(R.string.author_account_receviced)
                recever = getString(R.string.recevicer)
            }
            1 -> {
                title = getString(R.string.author_region_receviced)
                recever = getString(R.string.recevicer)
            }
            2 -> {
                title = getString(R.string.author_region_unAuthorized, regionName)
                recever = getString(R.string.licensor)
            }
        }

        runOnUiThread {
            popView?.let {
                it.findViewById<TextView>(R.id.code_warm_title).text = title

                it.findViewById<TextView>(R.id.code_warm_context).text = recever + phone

                it.findViewById<TextView>(R.id.code_warm_i_see).setOnClickListener {
                    PopUtil.dismiss(pop)
//                    if (type == 0)
//                        restartApplication()
                    PopUtil.dismiss(pop)
                    when (type) {
                        -1, 0, 2 -> { //移交的区域已被接收 账号已被接收 解除了区域%1$s的授权
                            if (lastRegionId == rid || rid == 1) {//如果正在使用或者是区域一则退出
                                //DBUtils.deleteAllData()//删除数据
                                restartApplication()
                                ToastUtils.showLong(getString(R.string.cancel_authorization))
                            }
                        }
                    }
                }

                initOnLayoutListener()
                LogUtils.v("zcl---------判断tel---${!this@TelinkBaseActivity.isFinishing}----- && --${!pop!!.isShowing} ---&&-- ${true}&&---$isResume")
                try {
                    if (!this@TelinkBaseActivity.isFinishing && !pop!!.isShowing && isResume)
                        pop!!.showAtLocation(window.decorView, Gravity.CENTER, 0, 0)
                } catch (e: Exception) {
                    LogUtils.v("zcl弹框出现问题${e.localizedMessage}")
                }
                notifyWSData(type, rid)
            }
        }
    }


    open fun notifyWSData(type: Int, rid: Any) {

    }

    private fun initStompReceiver() {
        stompRecevice = StompReceiver()
        val filter = IntentFilter()
        filter.addAction(Constant.LOGIN_OUT)
       //filter.addAction(Constant.GW_COMMEND_CODE)
       //filter.addAction(Constant.CANCEL_CODE)
       //filter.addAction(Constant.PARSE_CODE)
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        registerReceiver(stompRecevice, filter)
    }

    inner class StompReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
        var  codeBean =  (intent?.getSerializableExtra(Constant.LOGIN_OUT)?:"") as MqttBodyBean
            when(codeBean.cmd){
                1->{//单点登录
                    LogUtils.e("zcl_baseMe___________收到登出消息")
                    loginOutMethod()
                }
                2->{//推送二维码扫描解析结果
                    makeCodeDialog(0, codeBean.ref_user_phone, codeBean.rid.toString(), codeBean.region_name)
                }
                 3->{//解除授权信息
                     val user = DBUtils.lastUser
                     user?.let {
                         if (it.last_authorizer_user_id == codeBean.authorizer_user_id.toString() && it.last_region_id == codeBean.rid.toString()
                                 &&it.id.toString() == it.last_authorizer_user_id) {//是自己的区域
                             user.last_region_id = 1.toString()
                             user.last_authorizer_user_id = user.id.toString()
                             DBUtils.deleteAllData()
                             AccountModel.initDatBase(it)
                             //更新last—region-id
                             DBUtils.saveUser(user)
                             SyncDataPutOrGetUtils.syncGetDataStart(user, syncCallbackGet)
                         }
                     }
                     codeBean.let { makeCodeDialog(2, it.authorizer_user_phone, codeBean.rid, codeBean.region_name, user?.last_region_id?.toInt()) }//2代表解除授权信息type
                     LogUtils.e("zcl_baseMe___________取消授权${codeBean == null}")
                }
                700 -> TelinkLightApplication.getApp().offLine = false
                701 -> TelinkLightApplication.getApp().offLine = true
                2000->{//下发标签结果
                    if (codeBean.status == 0) receviedGwCmd2000(codeBean.ser_id)
                }
                2500->{//推送下发控制指令结果
                    receviedGwCmd2500M(codeBean)
                }
            }


            when (intent?.action) {
                Constant.GW_COMMEND_CODE -> {
                    val gwStompBean = intent.getSerializableExtra(Constant.GW_COMMEND_CODE) as GwStompBean
                    LogUtils.v("zcl-----------长连接接收网关数据-------$gwStompBean")
                    when (gwStompBean.cmd) {
                        700 -> TelinkLightApplication.getApp().offLine = false
                        701 -> TelinkLightApplication.getApp().offLine = true
                        2000 -> if (gwStompBean.status == 0) receviedGwCmd2000(gwStompBean.ser_id)
                        2500 -> receviedGwCmd2500(gwStompBean)
                    }

                }
                Constant.LOGIN_OUT -> {
                    LogUtils.e("zcl_baseMe___________收到登出消息")
                    loginOutMethod()
                }
                Constant.CANCEL_CODE -> {
                    val extra = intent.getSerializableExtra(Constant.CANCEL_CODE)
                    var cancelBean: CancelAuthorMsg? = null
                    if (extra != null)
                        cancelBean = extra as CancelAuthorMsg

                    val user = DBUtils.lastUser
                    val lastRegionId = user?.last_region_id
                    user?.let {
                        if (user.last_authorizer_user_id == cancelBean?.authorizer_user_id.toString()
                                && user.last_region_id == cancelBean?.rid.toString()) {
                            user.last_region_id = 1.toString()
                            user.last_authorizer_user_id = user.id.toString()
                            DBUtils.deleteAllData()
                            AccountModel.initDatBase(it)
                            //更新last—region-id
                            DBUtils.saveUser(user)
                            Log.e("zclbaseActivity", "zcl******" + DBUtils.lastUser)
                            SyncDataPutOrGetUtils.syncGetDataStart(user, syncCallbackGet)
                        }
                    }

                    cancelBean?.let { makeCodeDialog(2, it.authorizer_user_phone, cancelBean.rid, cancelBean.region_name, lastRegionId?.toInt()) }//2代表解除授权信息type
                    LogUtils.e("zcl_baseMe___________取消授权${cancelBean == null}")
                }
                Constant.PARSE_CODE -> {
                    val codeBean: QrCodeTopicMsg = intent.getSerializableExtra(Constant.PARSE_CODE) as QrCodeTopicMsg
//                    LogUtils.e("zcl_baseMe___________收到消息***解析二维码***")
                    makeCodeDialog(codeBean.type, codeBean.ref_user_phone, codeBean.rid.toString(), codeBean.region_name)
                }
            }
        }
    }

    open fun receviedGwCmd2500M(codeBean: MqttBodyBean) {

    }

    open fun receviedGwCmd2500(gwStompBean: GwStompBean) {

    }

    open fun receviedGwCmd2000(serId: String) {

    }

    open fun loginOutMethod() {
        if (!NetWorkUtils.isNetworkAvalible(this)) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.network_tip_title)
                    .setMessage(R.string.net_disconnect_tip_message)
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ ->
                        // 跳转到设置界面
                        this.startActivityForResult(Intent(Settings.ACTION_WIRELESS_SETTINGS), 0)
                    }.create().show()
        } else {
            SyncDataPutOrGetUtils.syncPutDataStart(this, object : SyncCallback {
                override fun start() {
                    showLoadingDialog(getString(R.string.tip_start_sync))
                }

                override fun complete() {
                    hideLoadingDialog()
                    val b = this@TelinkBaseActivity.isFinishing
                    val showing = singleLogin?.isShowing
                    SharedPreferencesHelper.putBoolean(TelinkLightApplication.getApp(), Constant.IS_LOGIN, false)
                    if (!b && showing != null && !showing) {
                        singleLogin!!.show()
                    }
                }

                override fun error(msg: String) {
                    hideLoadingDialog()
                    if (msg != "null")
                        ToastUtils.showLong(msg)
                }
            })
        }
    }


    /**
     * 报错log打印
     */
    fun onErrorReportNormal(info: ErrorReportInfo) {

        when (info.stateCode) {
            ErrorReportEvent.STATE_SCAN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_SCAN_BLE_DISABLE -> {
                        LogUtils.d("蓝牙未开启")
//                        showToast(getString(R.string.close_bluetooth))
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_ADV -> {
                        LogUtils.d("无法收到广播包以及响应包")
//                        showToast("无法收到广播包以及响应包")
                    }
                    ErrorReportEvent.ERROR_SCAN_NO_TARGET -> {
                        LogUtils.d("未扫到目标设备")
//                        showToast("未扫到目标设备")
                    }
                }

            }
            ErrorReportEvent.STATE_CONNECT -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_CONNECT_ATT -> {
                        LogUtils.d("未读到att表")
//                        showToast("未读到att表")
                    }
                    ErrorReportEvent.ERROR_CONNECT_COMMON -> {
                        LogUtils.d("未建立物理连接")
//                        showToast("未建立物理连接")
                    }
                }
            }
            ErrorReportEvent.STATE_LOGIN -> {
                when (info.errorCode) {
                    ErrorReportEvent.ERROR_LOGIN_VALUE_CHECK -> {
                        LogUtils.d("value check失败： 密码错误")
//                        showToast("value check失败： 密码错误")
                    }
                    ErrorReportEvent.ERROR_LOGIN_READ_DATA -> {
                        LogUtils.d("read login data 没有收到response")
//                        showToast("read login data 没有收到response")
                    }
                    ErrorReportEvent.ERROR_LOGIN_WRITE_DATA -> {
                        LogUtils.d("write login data 没有收到response")
//                        showToast("write login data 没有收到response")
                    }
                }
            }
        }
    }


    fun showOpenLocationServiceDialog() {
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.open_location_service)
        builder.setNegativeButton(getString(android.R.string.ok)) { _, _ ->
            BleUtils.jumpLocationSetting()
        }
        locationServiceDialog = builder.create()
        locationServiceDialog?.setCancelable(false)
        locationServiceDialog?.show()
    }

    fun hideLocationServiceDialog() {
        locationServiceDialog?.hide()
    }


    fun connect(meshAddress: Int = 0, fastestMode: Boolean = false, macAddress: String? = null, meshName: String? = DBUtils.lastUser?.controlMeshName,
                meshPwd: String? = NetworkFactory.md5(NetworkFactory.md5(meshName) + meshName).substring(0, 16),
                retryTimes: Long = 1, deviceTypes: List<Int>? = null, connectTimeOutTime: Long = 20, isAutoConnect: Boolean = true): Observable<DeviceInfo>? {

        // !TelinkLightService.Instance().isLogin 代表只有没连接的时候，才会往下跑，走连接的流程。  mConnectDisposable == null 代表这是第一次执行
        return if (mConnectDisposable == null && TelinkLightService.Instance()?.isLogin == false) {
            return Commander.connect(meshAddress, fastestMode, macAddress, meshName, meshPwd, retryTimes, deviceTypes, connectTimeOutTime)
                    ?.doOnSubscribe {
                        mConnectDisposable = it
                    }
                    ?.doFinally {
                        mConnectDisposable = null
                    }
                    ?.doOnError {
                        TelinkLightService.Instance()?.idleMode(false)
                    }

        } else {
            LogUtils.d("autoConnect Commander = ${mConnectDisposable?.isDisposed}, isLogin = ${TelinkLightService.Instance()?.isLogin}")
            Observable.create {
                it.onNext(TelinkLightApplication.getApp().connectDevice)
            }
        }
    }

    /**
     * 自动重连
     */
    @SuppressLint("CheckResult")
    @Deprecated("use connect()")
    fun connectOld(macAddr: String?) {
        //如果支持蓝牙就打开蓝牙
        // if (LeBluetooth.getInstance().isSupport(applicationContext))
        //  LeBluetooth.getInstance().enable(applicationContext)    //如果没打开蓝牙，就提示用户打开

        //如果位置服务没打开，则提示用户打开位置服务，bleScan必须
        if (!BleUtils.isLocationEnable(this)) {
            showOpenLocationServiceDialog()
        } else {
            hideLocationServiceDialog()
            TelinkLightService.Instance()?.idleMode(true)
            Thread.sleep(200)
            RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (TelinkLightApplication.getApp().connectDevice == null/*!TelinkLightService.Instance().isLogin*/) {
                            showLoadingDialog(getString(R.string.please_wait))

                            val meshName = DBUtils.lastUser!!.controlMeshName

                            GlobalScope.launch {
                                //自动重连参数
                                val connectParams = Parameters.createAutoConnectParameters()
                                connectParams.setMeshName(meshName)
                                connectParams.setPassword(NetworkFactory.md5(NetworkFactory.md5(meshName) + meshName).substring(0, 16))
                                connectParams.autoEnableNotification(true)
                                connectParams.setConnectMac(macAddr)
                                LogUtils.v("autoconnect  meshName = $meshName   meshPwd = ${NetworkFactory.md5(NetworkFactory.md5(meshName) + meshName).substring(0, 16)}   macAddress = ${macAddr}")

                                //连接，如断开会自动重连
                                TelinkLightService.Instance()?.autoConnect(connectParams)
                            }
                        }
                    }, { LogUtils.d(it) })
        }
    }


    inner class ChangeRecevicer : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceInfo = intent?.getParcelableExtra("STATUS_CHANGED") as DeviceInfo
            LogUtils.e("zcl获取通知$deviceInfo")
            when (deviceInfo.status) {
                LightAdapter.STATUS_LOGIN -> {
                    if (!isScanning)
                        ToastUtils.showLong(getString(R.string.connect_success))
                    changeDisplayImgOnToolbar(true)

                }
                LightAdapter.STATUS_LOGOUT -> {
                    changeDisplayImgOnToolbar(false)
                }

            }
        }
    }

    fun setScanningMode(isScanning: Boolean) {
        this.isScanning = isScanning
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (loadDialog != null && loadDialog!!.isShowing)
            hideLoadingDialog()
        else
            finish()
    }

    class NetWorkChangReceiver : BroadcastReceiver() {
        private var isHaveNetwork: Boolean = true

        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                var connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isAvailable) {
                    if (!isHaveNetwork) {

                        val connected = TelinkLightApplication.getApp().mStompManager?.mStompClient?.isConnected
                        if (connected != true)
                            TelinkLightApplication.getApp().initStompClient()

                        LogUtils.v("zcl-----------telinbase收到监听有网状态-------$connected")
                        isHaveNetwork = true
                    }
                } else {
                    LogUtils.v("zcl-----------telinbase收到监听无网状态-------")
                    isHaveNetwork = false
                }
            } catch (e: Exception) {
                //ignore
            }
        }
    }


    fun showInstallDeviceList(isGuide: Boolean, clickRgb: Boolean) {
        this.clickRgb = clickRgb
        val view = View.inflate(this, R.layout.dialog_install_list, null)
        val closeInstallList = view.findViewById<ImageView>(R.id.close_install_list)
        val installDeviceRecyclerview = view.findViewById<RecyclerView>(R.id.install_device_recyclerView)
        closeInstallList.setOnClickListener { v -> installDialog?.dismiss() }

        val installList: ArrayList<InstallDeviceModel> = OtherUtils.getInstallDeviceList(this)

        val installDeviceListAdapter = InstallDeviceListAdapter(R.layout.item_install_device, installList)
        val layoutManager = LinearLayoutManager(this)
        installDeviceRecyclerview?.layoutManager = layoutManager
        installDeviceRecyclerview?.adapter = installDeviceListAdapter
        installDeviceListAdapter.bindToRecyclerView(installDeviceRecyclerview)
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.divider)))
        //添加分割线
        installDeviceRecyclerview?.addItemDecoration(decoration)
        installDeviceListAdapter.onItemClickListener = onItemClickListenerInstallList

        installDialog = AlertDialog.Builder(this).setView(view).create()
        installDialog?.setOnShowListener {}
        if (isGuide) installDialog?.setCancelable(false)
        installDialog?.show()
    }

    private val onItemClickListenerInstallList = BaseQuickAdapter.OnItemClickListener { _, _, position ->
        isGuide = false
        installDialog?.dismiss()
        when (position) {
            INSTALL_GATEWAY -> {
                installId = INSTALL_GATEWAY
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.Gate_way))
            }
            INSTALL_NORMAL_LIGHT -> {
                installId = INSTALL_NORMAL_LIGHT
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.normal_light))
            }
            INSTALL_RGB_LIGHT -> {
                installId = INSTALL_RGB_LIGHT
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.rgb_light))
            }
            INSTALL_CURTAIN -> {
                installId = INSTALL_CURTAIN
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.curtain))
            }
            INSTALL_SWITCH -> {
                installId = INSTALL_SWITCH
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.switch_title))
                stepOneText?.visibility = View.GONE
                stepTwoText?.visibility = View.GONE
                stepThreeText?.visibility = View.GONE
                switchStepOne?.visibility = View.VISIBLE
                switchStepTwo?.visibility = View.VISIBLE
                swicthStepThree?.visibility = View.VISIBLE
            }
            INSTALL_SENSOR -> {
                installId = INSTALL_SENSOR
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.sensor))
            }
            INSTALL_CONNECTOR -> {
                installId = INSTALL_CONNECTOR
                showInstallDeviceDetail(StringUtils.getInstallDescribe(installId, this), position, getString(R.string.relay))
            }
        }
    }

    fun showInstallDeviceDetail(describe: String, position: Int, string: String) {
        makeInstallView()
        installTitleTv?.text = string
        installDialog = AlertDialog.Builder(this).setView(viewInstall).create()
        installDialog?.setOnShowListener {}
        installDialog?.dismiss()
        installDialog?.show()
    }

    private fun makeInstallView() {
        viewInstall = View.inflate(this, R.layout.dialog_install_detail, null)
        val closeInstallList = viewInstall?.findViewById<ImageView>(R.id.close_install_list)
        val btnBack = viewInstall?.findViewById<ImageView>(R.id.btnBack)

        installTitleTv = viewInstall?.findViewById(R.id.install_title_tv)
        stepOneText = viewInstall?.findViewById(R.id.step_one)
        stepTwoText = viewInstall?.findViewById(R.id.step_two)
        stepThreeText = viewInstall?.findViewById(R.id.step_three)
        switchStepOne = viewInstall?.findViewById(R.id.switch_step_one)
        switchStepTwo = viewInstall?.findViewById(R.id.switch_step_two)
        swicthStepThree = viewInstall?.findViewById(R.id.switch_step_three)
        installHelpe = viewInstall?.findViewById(R.id.install_see_helpe)

        installHelpe?.setOnClickListener(dialogOnclick)
        val searchBar = viewInstall?.findViewById<Button>(R.id.search_bar)
        closeInstallList?.setOnClickListener(dialogOnclick)
        btnBack?.setOnClickListener(dialogOnclick)
        searchBar?.setOnClickListener(dialogOnclick)
    }

    private val dialogOnclick = View.OnClickListener {
        var medressData = 0
        var allData = DBUtils.allLight
        var sizeData = DBUtils.allLight.size
        if (sizeData != 0) {
            var lightData = allData[sizeData - 1]
            medressData = lightData.meshAddr
        }

        when (it.id) {
            R.id.close_install_list -> installDialog?.dismiss()
            R.id.install_see_helpe -> seeHelpe()

            R.id.search_bar -> {
                if (medressData <= MeshUtils.DEVICE_ADDRESS_MAX) {
                    when (installId) {
                        INSTALL_NORMAL_LIGHT -> {
                            intent = Intent(this, DeviceScanningNewActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.LIGHT_NORMAL)
                            startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                        INSTALL_RGB_LIGHT -> {
                            intent = Intent(this, DeviceScanningNewActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.LIGHT_RGB)
                            startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                        INSTALL_CURTAIN -> {
                            intent = Intent(this, DeviceScanningNewActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.SMART_CURTAIN)
                            startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()

                        }
                        INSTALL_SWITCH -> {
                            startActivity(Intent(this, ScanningSwitchActivity::class.java))
                            //intent = Intent(this, DeviceScanningNewActivity::class.java)
                            // intent.putExtra(Constant.DEVICE_TYPE, DeviceType.NORMAL_SWITCH)       //connector也叫relay
                            // startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                        INSTALL_SENSOR -> {
                            startActivity(Intent(this, ScanningSensorActivity::class.java))
                            // intent = Intent(this, DeviceScanningNewActivity::class.java)
                            //intent.putExtra(Constant.DEVICE_TYPE, DeviceType.SENSOR)       //connector也叫relay
                            // startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                        INSTALL_CONNECTOR -> {
                            intent = Intent(this, DeviceScanningNewActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.SMART_RELAY)       //connector也叫relay
                            startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                        INSTALL_GATEWAY -> {
                            intent = Intent(this, DeviceScanningNewActivity::class.java)
                            intent.putExtra(Constant.DEVICE_TYPE, DeviceType.GATE_WAY)
                            startActivityForResult(intent, 0)
                            installDialog?.show()
                            finish()
                        }
                    }
                } else ToastUtils.showLong(getString(R.string.much_lamp_tip))


            }
            R.id.btnBack -> {
                installDialog?.dismiss()
                showInstallDeviceList(isGuide, clickRgb)
            }
        }
    }

    fun seeHelpe() {
        var intent = Intent(this, InstructionsForUsActivity::class.java)
        startActivity(intent)
    }

    private fun makeStopScanPop() {
        var popView: View = LayoutInflater.from(this).inflate(R.layout.pop_warm, null)

        hinitOne = popView.findViewById(R.id.pop_warm_tv)
        cancelf = popView.findViewById(R.id.btn_cancel)
        confirmf = popView.findViewById(R.id.btn_confirm)
        hinitOne.text = getString(R.string.exit_tips_in_scanning)

        popFinish = PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        popFinish.isOutsideTouchable = false
        popFinish.isFocusable = true // 设置PopupWindow可获得焦点
        popFinish.isTouchable = true // 设置PopupWindow可触摸补充：
    }

    private fun makeRenamePopuwindow() {
        popReNameView = View.inflate(this, R.layout.pop_rename, null)
        renameEt = popReNameView?.findViewById(R.id.pop_rename_edt)
        renameCancel = popReNameView?.findViewById(R.id.pop_rename_cancel)
        renameConfirm = popReNameView?.findViewById(R.id.pop_rename_confirm)
        StringUtils.initEditTextFilter(renameEt)
        renameEt?.singleLine = true

        renameDialog = Dialog(this)
        renameDialog?.setContentView(popReNameView)
        renameDialog?.setCanceledOnTouchOutside(false)
        renameCancel?.setOnClickListener { renameDialog?.dismiss() }
        //确定回调 单独写
    }

}

