package com.dadoutek.uled.group

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.dadoutek.uled.R
import com.dadoutek.uled.windowcurtains.WindowCurtainsActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.connector.ConnectorOfGroupActivity
import com.dadoutek.uled.connector.ConnectorSettingActivity
import com.dadoutek.uled.curtain.CurtainOfGroupActivity
import com.dadoutek.uled.intf.CallbackLinkMainActAndFragment
import com.dadoutek.uled.intf.MyBaseQuickAdapterOnClickListner
import com.dadoutek.uled.light.LightsOfGroupActivity
import com.dadoutek.uled.light.NormalSettingActivity
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbGroup
import com.dadoutek.uled.model.DbModel.DbLight
import com.dadoutek.uled.model.ItemTypeGroup
import com.dadoutek.uled.model.SharedPreferencesHelper
import com.dadoutek.uled.othersview.BaseFragment
import com.dadoutek.uled.othersview.MainActivity
import com.dadoutek.uled.rgb.RGBSettingActivity
import com.dadoutek.uled.scene.NewSceneSetAct
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.util.*
import com.telink.bluetooth.light.ConnectionStatus
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_group_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class GroupListFragment : BaseFragment() {
    private var inflater: LayoutInflater? = null
    private var adapter: GroupListRecycleViewAdapter? = null
    private var mContext: Activity? = null
    private var mApplication: TelinkLightApplication? = null
    private var dataManager: DataManager? = null
    private var gpList: List<ItemTypeGroup>? = null
    private var application: TelinkLightApplication? = null
    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    internal var showList: List<ItemTypeGroup>? = null
    private var updateLightDisposal: Disposable? = null
    private val SCENE_MAX_COUNT = 16

    //19-2-20 界面调整
    private var install_device: TextView? = null
    private var create_group: TextView? = null
    private var create_scene: TextView? = null

    private var sharedPreferences: SharedPreferences? = null

    //新用户选择的初始安装选项是否是RGB灯
    private var isRgbClick = false
    //是否正在引导
    private var isGuide = false
    var firstShowGuide = true
    private var isFristUserClickCheckConnect = true
    private var guideShowCurrentPage = false

    internal var lastPosition: Int = 0

    internal var lastOffset: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mContext = this.activity
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = getView(inflater)
        this.initData()
        return view
    }

    private fun scrollToPosition() {

        sharedPreferences = mContext!!.getSharedPreferences("key", Activity.MODE_PRIVATE)

        lastOffset = sharedPreferences!!.getInt("lastOffset", 0)

        lastPosition = sharedPreferences!!.getInt("lastPosition", 0)

        if (recyclerView!!.layoutManager != null && lastPosition >= 0) {

            (recyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(lastPosition, lastOffset)

        }

    }


    override fun onResume() {
        super.onResume()
        isFristUserClickCheckConnect = true

        refreshView()
        scrollToPosition()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            val act = activity as MainActivity?
            act?.addEventListeners()
            if (Constant.isCreat) {
                refreshAndMoveBottom()
                Constant.isCreat = false
            } else {
                refreshView()
            }

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
//            this.initData()
        }
    }

    override fun onStop() {
        super.onStop()
        isFristUserClickCheckConnect = false
    }

    private fun getView(inflater: LayoutInflater): View {
        this.inflater = inflater

        val view = inflater.inflate(R.layout.fragment_group_list, null)

        toolbar = view.findViewById(R.id.toolbar)
        toolbar!!.setTitle(R.string.group_title)

        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).setOnClickListener {
            isGuide = false
            if (dialog_pop?.visibility == View.GONE) {
                showPopupMenu()
            } else {
                hidePopupMenu()
            }
        }

        setHasOptionsMenu(true)

        recyclerView = view.findViewById(R.id.list_groups)

        install_device = view.findViewById(R.id.install_device)
        create_group = view.findViewById(R.id.create_group)
        create_scene = view.findViewById(R.id.create_scene)
        install_device?.setOnClickListener(onClick)
        create_group?.setOnClickListener(onClick)
        create_scene?.setOnClickListener(onClick)

        return view
    }

    private fun initData() {
        this.mApplication = activity!!.application as TelinkLightApplication
        gpList = DBUtils.getgroupListWithType(activity!!)

        showList = ArrayList()
        showList = gpList


        val layoutmanager = LinearLayoutManager(activity)
        layoutmanager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = layoutmanager
        this.adapter = GroupListRecycleViewAdapter(R.layout.group_item, onItemChildClickListener, showList!!)

        val decoration = DividerItemDecoration(activity!!,
                DividerItemDecoration
                        .VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(activity!!, R.color
                .divider)))
        //添加分割线
        recyclerView?.addItemDecoration(decoration)
        recyclerView?.itemAnimator = DefaultItemAnimator()

//        adapter!!.addFooterView(getFooterView())
        adapter!!.bindToRecyclerView(recyclerView)

//        getPositionAndOffset()

        recyclerView!!.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (recyclerView!!.layoutManager != null) {
                    getPositionAndOffset()
                }
            }
        })

//        setMove()

        application = activity!!.application as TelinkLightApplication
        dataManager = DataManager(TelinkLightApplication.getInstance(),
                application!!.mesh.name, application!!.mesh.password)
    }

    private fun getPositionAndOffset() {

        val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager

        //获取可视的第一个view

        val topView = layoutManager.getChildAt(0)

        if (topView != null) {

            //获取与该view的顶部的偏移量

            lastOffset = topView.top

            //得到该View的数组位置

            lastPosition = layoutManager.getPosition(topView)

            sharedPreferences = mContext!!.getSharedPreferences("key", Activity.MODE_PRIVATE)

            val editor = sharedPreferences!!.edit()

            editor.putInt("lastOffset", lastOffset)

            editor.putInt("lastPosition", lastPosition)

            editor.commit()

        }

    }

    private fun addNewGroup() {
        val textGp = EditText(activity)
        StringUtils.initEditTextFilter(textGp)
        textGp.setText(DBUtils.getDefaultNewGroupName())
        //设置光标默认在最后
        textGp.setSelection(textGp.getText().toString().length)
        AlertDialog.Builder(activity)
                .setTitle(R.string.create_new_group)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textGp)

                .setPositiveButton(getString(android.R.string.ok)) { dialog, which ->
                    // 获取输入框的内容
                    if (StringUtils.compileExChar(textGp.text.toString().trim { it <= ' ' })) {
                        ToastUtils.showShort(getString(R.string.rename_tip_check))
                    } else {
                        //往DB里添加组数据
                        DBUtils.addNewGroupWithType(textGp.text.toString().trim { it <= ' ' }, DBUtils.groupList, Constant.DEVICE_TYPE_DEFAULT, activity!!)
                        refreshAndMoveBottom()
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { dialog, which -> dialog.dismiss() }.show()
    }

    private fun refreshAndMoveBottom() {
        refreshView()
        recyclerView?.smoothScrollToPosition(showList!!.size)
    }

    private fun refreshView() {
        if (activity != null) {
            gpList = DBUtils.getgroupListWithType(activity!!)

            showList = ArrayList()
            showList = gpList

            val layoutmanager = LinearLayoutManager(activity)
            layoutmanager.orientation = LinearLayoutManager.VERTICAL
            recyclerView?.layoutManager = layoutmanager
            this.adapter = GroupListRecycleViewAdapter(R.layout.group_item, onItemChildClickListener, showList!!)

//            val decoration = DividerItemDecoration(activity,
//                    DividerItemDecoration
//                            .VERTICAL)
//            decoration.setDrawable(ColorDrawable(ContextCompat.getColor(activity!!, R.color
//                    .divider)))
            //添加分割线
//            recyclerView?.addItemDecoration(decoration)
            recyclerView?.itemAnimator = DefaultItemAnimator()

//          adapter!!.addFooterView(getFooterView())
            adapter!!.bindToRecyclerView(recyclerView)
//            setMove()
        }
    }

    var onItemChildClickListener = object : MyBaseQuickAdapterOnClickListner {
        override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int, groupPosition: Int) {
            if (dialog_pop.visibility == View.GONE || dialog_pop == null) {
                val group = showList!![groupPosition].list[position]
                val dstAddr = group.meshAddr
                var intent: Intent

                if (TelinkLightApplication.getInstance().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                    checkConnect()
                } else {
                    when (view!!.getId()) {
                        R.id.btn_on -> {
                            Commander.openOrCloseLights(dstAddr, true)
                            updateLights(true, group)
                        }
                        R.id.btn_off -> {
                            Commander.openOrCloseLights(dstAddr, false)
                            updateLights(false, group)
                        }
                        R.id.btn_set -> {
                            intent = Intent(mContext, NormalSettingActivity::class.java)
                            if (OtherUtils.isRGBGroup(group) && group.meshAddr != 0xffff) {
                                intent = Intent(mContext, RGBSettingActivity::class.java)
                            } else if (OtherUtils.isCurtain(group) && group.meshAddr != 0xffff) {
                                intent = Intent(mContext, WindowCurtainsActivity::class.java)
                            } else if (OtherUtils.isConnector(group) && group.meshAddr != 0xfffff) {
                                intent = Intent(mContext, ConnectorSettingActivity::class.java)
                            }
                            intent.putExtra(Constant.TYPE_VIEW, Constant.TYPE_GROUP)
                            intent.putExtra("group", group)
                            startActivityForResult(intent, 2)
                        }
                        R.id.txt_name -> {
                            if (group.meshAddr != 0xffff) {
                                if (group.deviceType == Constant.DEVICE_TYPE_CURTAIN) {
                                    intent = Intent(mContext, CurtainOfGroupActivity::class.java)
                                    intent.putExtra("group", group)
                                    startActivityForResult(intent, 2)
                                } else if (group.deviceType == Constant.DEVICE_TYPE_CONNECTOR) {
                                    intent = Intent(mContext, ConnectorOfGroupActivity::class.java)
                                    intent.putExtra("group", group)
                                    startActivityForResult(intent, 2)
                                } else {
                                    intent = Intent(mContext, LightsOfGroupActivity::class.java)
                                    intent.putExtra("group", group)
                                    startActivityForResult(intent, 2)
                                }
                            } else if (group.deviceType == Constant.DEVICE_TYPE_NO) {
                                Toast.makeText(activity, R.string.device_page, Toast.LENGTH_LONG).show()
                            }

//                        ActivityUtils.startActivityForResult(intent)
                        }
//                    R.id.add_group -> {
//                        addNewGroup()
//                    }
                    }
                }
            }
        }
    }

    private val onClick = View.OnClickListener {
        var intent: Intent? = null
        //点击任何一个选项跳转页面都隐藏引导
//        val controller=guide2()
//            controller?.remove()
        hidePopupMenu()
        when (it.id) {
            R.id.install_device -> {
                showInstallDeviceList()
            }
            R.id.create_group -> {
                if (TelinkLightApplication.getInstance().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                } else {
                    addNewGroup()
                }
            }
            R.id.create_scene -> {
                val nowSize = DBUtils.sceneList.size
                if (TelinkLightApplication.getInstance().connectDevice == null) {
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
        }
    }

    val CREATE_SCENE_REQUESTCODE = 3
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        refreshData()
        if (requestCode == CREATE_SCENE_REQUESTCODE) {
            callbackLinkMainActAndFragment?.changeToScene()
        }
    }

    var callbackLinkMainActAndFragment: CallbackLinkMainActAndFragment? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CallbackLinkMainActAndFragment) {
            callbackLinkMainActAndFragment = context as CallbackLinkMainActAndFragment
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
            if (TelinkLightApplication.getInstance().connectDevice == null) {
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

    override fun endCurrentGuide() {
        super.endCurrentGuide()
    }

    private fun updateLights(isOpen: Boolean, group: DbGroup) {
        updateLightDisposal?.dispose()
        updateLightDisposal = Observable.timer(300, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe {
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

                    for (dbLight: DbLight in lightList) {
                        if (isOpen) {
                            dbLight.connectionStatus = ConnectionStatus.ON.value
                        } else {
                            dbLight.connectionStatus = ConnectionStatus.OFF.value
                        }
                        DBUtils.updateLightLocal(dbLight)
                    }
                }
    }

//    fun loadData(): List<DbGroup> {
//        var showList = mutableListOf<DbGroup>()
//
//        val dbOldGroupList = SharedPreferencesHelper.getObject(TelinkLightApplication.getInstance(), Constant.OLD_INDEX_DATA) as? ArrayList<DbGroup>
//
//        //如果有调整过顺序取本地数据，否则取数据库数据
//        if (dbOldGroupList != null && dbOldGroupList.size > 0) {
//            showList = dbOldGroupList
//        } else {
//            showList = DBUtils.groupList
//        }
//        return showList
//    }

//    fun refreshData() {
//        val mOldDatas: List<DbGroup>? = showList
//        val mNewDatas: List<DbGroup>? = loadData()
//        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return mOldDatas?.get(oldItemPosition)?.id?.equals(mNewDatas?.get
//                (newItemPosition)?.id) ?: false;
//            }
//
//            override fun getOldListSize(): Int {
//                return mOldDatas?.size ?: 0
//            }
//
//            override fun getNewListSize(): Int {
//                return mNewDatas?.size ?: 0
//            }
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                val beanOld = mOldDatas?.get(oldItemPosition)
//                val beanNew = mNewDatas?.get(newItemPosition)
//                return if (!beanOld?.name.equals(beanNew?.name)) {
//                    return false//如果有内容不同，就返回false
//                } else true
//
//            }
//        }, true)
//        showList = mNewDatas
//        adapter?.setNewData(showList)
//        adapter?.let { diffResult.dispatchUpdatesTo(it) }
//    }

//    private fun setMove() {
//        val onItemDragListener = object : OnItemDragListener {
//            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {}
//
//            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int,
//                                          target: RecyclerView.ViewHolder, to: Int) {
//            }
//
//            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
//                //                viewHolder.getItemId();
//                val list = adapter!!.data
//                SharedPreferencesHelper.putObject(activity, Constant.OLD_INDEX_DATA, list)
//            }
//        }
//
//        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
//        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
//        itemTouchHelper.attachToRecyclerView(recyclerView)
//
//        adapter!!.enableDragItem(itemTouchHelper, R.id.txt_name, true)
//        adapter!!.setOnItemDragListener(onItemDragListener)
//    }

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
}
