package com.dadoutek.uled.scene

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.util.DiffUtil
import android.support.v7.widget.*
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.app.hubert.guide.core.Builder
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dadoutek.uled.R
import com.dadoutek.uled.intf.CallbackLinkMainActAndFragment
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbScene
import com.dadoutek.uled.model.DbModel.DbSceneActions
import com.dadoutek.uled.model.Opcode
import com.dadoutek.uled.model.SharedPreferencesHelper
import com.dadoutek.uled.othersview.BaseFragment
import com.dadoutek.uled.tellink.TelinkLightApplication
import com.dadoutek.uled.tellink.TelinkLightService
import com.dadoutek.uled.util.GuideUtils
import com.dadoutek.uled.util.LogUtils
import com.dadoutek.uled.util.SharedPreferencesUtils
import com.dadoutek.uled.util.StringUtils
import kotlinx.android.synthetic.main.fragment_scene.*
import kotlinx.android.synthetic.main.popwindow_install_deive_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener
import java.util.*

/**
 * Created by hejiajun on 2018/5/2.
 */

class SceneFragment : BaseFragment(), Toolbar.OnMenuItemClickListener, View.OnClickListener {
    private var inflater: LayoutInflater? = null
    private var adaper: SceneRecycleListAdapter? = null

    private var toolbar: Toolbar? = null
    internal var toolbarTitle: TextView? = null

    private var telinkLightApplication: TelinkLightApplication? = null
    //    private List<Scenes> scenesListData;
    private var scenesListData: MutableList<DbScene>? = null
    private var isDelete = false
    internal var builder: Builder? = null
    private var guideShowCurrentPage = false

    private var recyclerView: RecyclerView? = null

    private var no_scene: ConstraintLayout? = null

    private var isGuide = false
    private var isRgbClick = false

    private var add_scenes: Button? = null

    private var addNewScene: ConstraintLayout? = null

    private var install_device: TextView? = null
    private var create_group: TextView? = null
    private var create_scene: TextView? = null
    val CREATE_GROUP_REQUESTCODE = 3

    internal var onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
        try {
            if (position < adapter.getData().size) {
                setScene(scenesListData!![position].id!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal var onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
        if (dialog_pop.visibility == View.GONE || dialog_pop == null) {
            if (view.getId() == R.id.scene_delete) {
                scenesListData!![position].isSelected = !scenesListData!![position].isSelected
            } else
                if (view.getId() == R.id.scene_edit) {
                    //                setScene(scenesListData.get(position).getId());
                    val scene = scenesListData!![position]
                    val intent = Intent(activity, NewSceneSetAct::class.java)
                    intent.putExtra(Constant.CURRENT_SELECT_SCENE, scene)
                    intent.putExtra(Constant.IS_CHANGE_SCENE, true)
                    startActivityForResult(intent, 3)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater
        val view = inflater.inflate(R.layout.fragment_scene, null)
        recyclerView = view.findViewById(R.id.recyclerView)
        no_scene = view.findViewById(R.id.no_scene)
        add_scenes = view.findViewById(R.id.add_scenes)

        install_device = view.findViewById(R.id.install_device)
        create_group = view.findViewById(R.id.create_group)
        create_scene = view.findViewById(R.id.create_scene)
        addNewScene = view.findViewById(R.id.add_new_scene)
        install_device?.setOnClickListener(onClick)
        create_group?.setOnClickListener(onClick)
        create_scene?.setOnClickListener(onClick)

        add_scenes!!.setOnClickListener(this)
        addNewScene!!.setOnClickListener(this)

        initToolBar(view)
        initData()
        initView()
        return view
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
                        startActivityForResult(intent, 3)
                    }
                }
            }
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
                        DBUtils.addNewGroupWithType(textGp.text.toString().trim { it <= ' ' }, DBUtils.groupList, Constant.DEVICE_TYPE_DEFAULT_ALL, activity!!)
                        callbackLinkMainActAndFragment?.changeToGroup()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initToolBar(view: View) {
        setHasOptionsMenu(true)
        toolbar = view.findViewById(R.id.toolbar)
        toolbar!!.setTitle(R.string.scene_name)

        val btn_add = toolbar!!.findViewById<ImageView>(R.id.img_function1)
        val btn_delete = toolbar!!.findViewById<ImageView>(R.id.img_function2)

//        btn_add.visibility = View.VISIBLE
        btn_delete.visibility = View.VISIBLE

        btn_add.setOnClickListener(this)
        btn_delete.setOnClickListener(this)
    }

    fun lazyLoad(showTypeView: Int) {
//        step1Guide()
    }

    private fun step1Guide() {
        guideShowCurrentPage = !GuideUtils.getCurrentViewIsEnd(activity!!, GuideUtils.END_ADD_SCENE_KEY, false)
        if (guideShowCurrentPage) {
            GuideUtils.resetSceneGuide(activity!!)
            val guide1 = toolbar!!.findViewById<View>(R.id.img_function1) as ImageView
            GuideUtils.guideBuilder(this, GuideUtils.STEP7_GUIDE_ADD_SCENE)
                    .addGuidePage(GuideUtils.addGuidePage(guide1, R.layout.view_guide_simple_scene_1, getString(R.string.scene_guide_1),
                            View.OnClickListener { guide1.performClick() }, GuideUtils.END_ADD_SCENE_KEY, activity!!)).show()
        }
    }

    private fun stepEndGuide2() {
        if (activity != null) {
            val view = activity!!.window.decorView
            val viewTreeObserver = view.viewTreeObserver
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val guide2 = adaper!!.getViewByPosition(0, R.id.scene_edit) as TextView?
                    GuideUtils.guideBuilder(this@SceneFragment, GuideUtils.ADDITIONAL_GUIDE_SET_SCENE)
                            .addGuidePage(GuideUtils.addGuidePage(guide2!!, R.layout.view_guide_simple_scene_2, getString(R.string.click_update_scene),
                                    View.OnClickListener { v -> GuideUtils.changeCurrentViewIsEnd(activity!!, GuideUtils.END_ADD_SCENE_SET_KEY, true) }, GuideUtils.END_ADD_SCENE_SET_KEY, activity!!)).show()
                }
            })
        }
    }

    private fun stepEndGuide1() {
        if (activity != null && adaper!!.itemCount > 0) {
            val view = activity!!.window.decorView
            val viewTreeObserver = view.viewTreeObserver
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val guide2 = adaper!!.getViewByPosition(0, R.id.scene_name) as TextView?
                    if (guide2 != null) {
                        GuideUtils.guideBuilder(this@SceneFragment, GuideUtils.STEP14_GUIDE_APPLY_SCENE)
                                .addGuidePage(GuideUtils.addGuidePage(guide2, R.layout.view_guide_simple_scene_2, getString(R.string.apply_scene),
                                        View.OnClickListener { v -> stepEndGuide2() }, GuideUtils.END_ADD_SCENE_SET_KEY, activity!!)).show()
                    }
                }
            })
        }
    }

    private fun initOnLayoutListener(showTypeView: Int) {
        if (activity != null) {
            val view = activity!!.window.decorView
            val viewTreeObserver = view.viewTreeObserver
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    lazyLoad(showTypeView)
                }
            })
        }
        //        }
    }

    private fun initData() {
        telinkLightApplication = this.activity!!.application as TelinkLightApplication
        scenesListData = DBUtils.sceneList

        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
        toolbar!!.navigationIcon = null
        toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
        toolbar!!.setTitle(R.string.scene_name)
    }

    private fun initView() {
        if (scenesListData!!.size > 0) {
            recyclerView!!.visibility = View.VISIBLE
            no_scene!!.visibility = View.GONE
            addNewScene!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.GONE
            no_scene!!.visibility = View.VISIBLE
            addNewScene!!.visibility = View.GONE
        }
        val layoutmanager = LinearLayoutManager(activity)
        layoutmanager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = layoutmanager
        //添加分割线
        val decoration = DividerItemDecoration(activity!!,
                DividerItemDecoration
                        .VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(activity!!, R.color
                .divider)))
        recyclerView!!.addItemDecoration(decoration)
        //添加Item变化动画
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        adaper = SceneRecycleListAdapter(R.layout.item_scene, scenesListData, isDelete)
        adaper!!.setOnItemClickListener(onItemClickListener)
        adaper!!.setOnItemChildClickListener(onItemChildClickListener)
        adaper!!.onItemLongClickListener = onItemChildLongClickListener
        adaper!!.bindToRecyclerView(recyclerView)

        isDelete = false
        adaper!!.changeState(isDelete)
        for (i in scenesListData!!.indices) {
            if (scenesListData!![i].isSelected) {
                scenesListData!![i].isSelected = false
            }
        }
        adaper!!.notifyDataSetChanged()

//        add_scenes!!.setOnClickListener(this)
    }

    var onItemChildLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
        isDelete = true
        adaper!!.changeState(isDelete)
        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.GONE
        toolbar!!.title = ""
        setBack()
        adaper!!.notifyDataSetChanged()
//        refreshData()
        return@OnItemLongClickListener true
    }

    private fun setBack() {
        toolbar!!.setNavigationIcon(R.drawable.navigation_back_white)
        toolbar!!.setNavigationOnClickListener {
            toolbar!!.setTitle(R.string.scene_name)
            toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
            toolbar!!.navigationIcon = null
            toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
            isDelete = false
            adaper!!.changeState(isDelete)
            for (i in scenesListData!!.indices) {
                if (scenesListData!![i].isSelected) {
                    scenesListData!![i].isSelected = false
                }
            }
            adaper!!.notifyDataSetChanged()
        }
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.sure_delete)
        builder.setPositiveButton(activity!!.getString(android.R.string.ok)) { dialog, which ->
            //            deleteScene(position)
//            adapter.notifyItemRemoved(position)
//            refreshData()
            Log.e("TAG_SIZE", scenesListData!!.size.toString())
            for (i in scenesListData!!.indices) {
                if (scenesListData!![i].isSelected) {
                    val opcode = Opcode.SCENE_ADD_OR_DEL
                    val params: ByteArray
                    if (scenesListData!!.size > 0) {
                        Thread.sleep(300)
                        val id = scenesListData!![i].id!!
                        val list = DBUtils.getActionsBySceneId(id)
                        params = byteArrayOf(0x00, id.toByte())
                        Thread { TelinkLightService.Instance().sendCommandNoResponse(opcode, 0xFFFF, params) }.start()
                        DBUtils.deleteSceneActionsList(list)
                        DBUtils.deleteScene(scenesListData!![i])
//                        scenesListData!!.removeAt(position)
                    }
//                    startDeleteGradientCmd(diyGradientList!![i].id)
//                    DBUtils.deleteGradient(diyGradientList!![i])
//                    DBUtils.deleteColorNodeList(DBUtils.getColorNodeListByDynamicModeId(diyGradientList!![i].id!!))
                }
            }
            refreshView()
//              scenesListData = DBUtils.sceneList
//              adapter.setNewData(scenesListData)
//            adaper!!.setNewData(scenesListData )
        }
        builder.setNegativeButton(activity!!.getString(R.string.cancel)) { dialog, which -> }
        val dialog = builder.show()
        dialog.show()
    }

    private fun refreshData() {
        adaper!!.notifyDataSetChanged()
        initData()
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (GuideUtils.getCurrentViewIsEnd(activity!!, GuideUtils.END_ADD_SCENE_KEY, false)) {
            stepEndGuide1()
        }
    }

    override fun onPause() {
        super.onPause()
        LogUtils.d("ss")
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 3 && resultCode == Constant.RESULT_OK) {
        initData()
        initView()
//            stepEndGuide1()
//        }
    }

    @Synchronized
    private fun deleteScene(position: Int) {
        val opcode = Opcode.SCENE_ADD_OR_DEL
        val params: ByteArray
        if (scenesListData!!.size > 0) {
            val id = scenesListData!![position].id!!
            val list = DBUtils.getActionsBySceneId(id)
            params = byteArrayOf(0x00, id.toByte())
            Thread { TelinkLightService.Instance().sendCommandNoResponse(opcode, 0xFFFF, params) }.start()
            DBUtils.deleteSceneActionsList(list)
            DBUtils.deleteScene(scenesListData!![position])
            scenesListData!!.removeAt(position)
        }
    }

    private fun setScene(id: Long) {
        val opcode = Opcode.SCENE_LOAD
        val list = DBUtils.getActionsBySceneId(id)
        Thread {
            val params: ByteArray
            params = byteArrayOf(id.toByte())
            TelinkLightService.Instance().sendCommandNoResponse(opcode, 0xFFFF, params)
        }.start()

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        builder = GuideUtils.guideBuilder(this, Constant.TAG_SceneFragment)
        if (isVisibleToUser) {
            //            initData();
            //            initView();
            //            showLoadingDialog("ss");
            refreshAllData()
            refreshView()
            initOnLayoutListener(1)
        } else {
            //                initOnLayoutListener(2);
            refreshView()
        }

    }

    private fun refreshView() {
        if (activity != null) {
            scenesListData = DBUtils.sceneList
            if (scenesListData!!.size > 0) {
                if (recyclerView != null) {
                    recyclerView!!.visibility = View.VISIBLE
                    no_scene!!.visibility = View.GONE
                    addNewScene!!.visibility = View.VISIBLE
                }
            } else {
                if(recyclerView!=null){
                    recyclerView!!.visibility = View.GONE
                    no_scene!!.visibility = View.VISIBLE
                    addNewScene!!.visibility = View.GONE
                }
            }

            if(recyclerView!=null){
                val layoutmanager = LinearLayoutManager(activity)
                layoutmanager.orientation = LinearLayoutManager.VERTICAL
                recyclerView!!.layoutManager = layoutmanager
                //添加分割线
                val decoration = DividerItemDecoration(activity!!,
                        DividerItemDecoration
                                .VERTICAL)
                decoration.setDrawable(ColorDrawable(ContextCompat.getColor(activity!!, R.color
                        .divider)))
                recyclerView!!.addItemDecoration(decoration)
                //添加Item变化动画
                recyclerView!!.itemAnimator = DefaultItemAnimator()
                adaper = SceneRecycleListAdapter(R.layout.item_scene, scenesListData, isDelete)
                adaper!!.setOnItemClickListener(onItemClickListener)
                adaper!!.setOnItemChildClickListener(onItemChildClickListener)
                adaper!!.onItemLongClickListener = onItemChildLongClickListener
                adaper!!.bindToRecyclerView(recyclerView)
            }

            toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
            toolbar!!.navigationIcon = null
            toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
            toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
            toolbar!!.setTitle(R.string.scene_name)

            isDelete = false
            adaper!!.changeState(isDelete)
            for (i in scenesListData!!.indices) {
                if (scenesListData!![i].isSelected) {
                    scenesListData!![i].isSelected = false
                }
            }
            adaper!!.notifyDataSetChanged()
        }
    }

    private fun refreshAllData() {

        val mOldDatas = scenesListData
        val mNewDatas = loadData()

        if (mOldDatas != null && mNewDatas != null) {

            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mOldDatas!!.size
                }

                override fun getNewListSize(): Int {
                    return mNewDatas.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return mOldDatas!![oldItemPosition].id == mNewDatas[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val beanOld = mOldDatas!![oldItemPosition]
                    val beanNew = mNewDatas[newItemPosition]
                    return if (beanOld.name != beanNew.name) {
                        false
                    } else
                        false
                }
            }, true)

            scenesListData = mNewDatas
            adaper!!.setNewData(scenesListData)
            diffResult.dispatchUpdatesTo(adaper!!)
        }

    }

    private fun loadData(): MutableList<DbScene> {
        var showList: List<DbScene> = ArrayList()
        showList = DBUtils.sceneList
        return showList
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.menu_delete -> {
////                if (isDelete) {
////                    isDelete = false
////                } else {
////                    isDelete = true
////                }
////
////                adaper!!.changeState(isDelete)
////                refreshData()
////            }
            R.id.menu_install -> if (!SharedPreferencesUtils.getConnectState(activity)) {
                //                    return;
            } else {
                if (scenesListData!!.size >= SCENE_MAX_COUNT) {
                    ToastUtils.showLong(R.string.scene_16_tip)
                } else {
                    val intent = Intent(activity, NewSceneSetAct::class.java)
                    intent.putExtra(Constant.IS_CHANGE_SCENE, false)
                    startActivityForResult(intent, 3)
                }
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_function2 -> {
                showDeleteDialog()
//                if (isDelete) {
//                    isDelete = false
//                } else {
//                    isDelete = true
//                }
//
//                adaper!!.changeState(isDelete)
//                refreshData()
            }
            R.id.img_function1 -> {
                isGuide = false
                if (dialog_pop?.visibility == View.GONE) {
                    showPopupMenu()
                } else {
                    hidePopupMenu()
                }
            }

            R.id.add_scenes -> {
                val nowSize = DBUtils.sceneList.size
                if (TelinkLightApplication.getInstance().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                } else {
                    if (nowSize >= SCENE_MAX_COUNT) {
                        ToastUtils.showLong(R.string.scene_16_tip)
                    } else {
                        val intent = Intent(activity, NewSceneSetAct::class.java)
                        intent.putExtra(Constant.IS_CHANGE_SCENE, false)
                        startActivityForResult(intent, 3)
                    }
                }
            }

            R.id.add_new_scene -> {
                val nowSize = DBUtils.sceneList.size
                if (TelinkLightApplication.getInstance().connectDevice == null) {
                    ToastUtils.showLong(activity!!.getString(R.string.device_not_connected))
                } else {
                    if (nowSize >= SCENE_MAX_COUNT) {
                        ToastUtils.showLong(R.string.scene_16_tip)
                    } else {
                        val intent = Intent(activity, NewSceneSetAct::class.java)
                        intent.putExtra(Constant.IS_CHANGE_SCENE, false)
                        startActivityForResult(intent, 3)
                    }
                }
            }
        }
        //                if (!SharedPreferencesUtils.getConnectState(getActivity())) {
        ////                    return;
        //                } else {
        //                    if (scenesListData.size() >= SCENE_MAX_COUNT) {
        //                        ToastUtils.showLong(R.string.scene_16_tip);
        //                    } else {
        //                        if(TelinkLightApplication.getInstance().getConnectDevice()==null){
        //                            ToastUtils.showLong(R.string.device_not_connected);
        //                        }else{
        //                            Intent intent = new Intent(getActivity(), NewSceneSetAct.class);
        //                            intent.putExtra(Constant.IS_CHANGE_SCENE, false);
        //                            startActivityForResult(intent, 0);
        //                        }
        //                    }
        //                }
    }

    private fun showPopupMenu() {
        dialog_pop?.visibility = View.VISIBLE
    }

    private fun hidePopupMenu() {
        if (!isGuide || GuideUtils.getCurrentViewIsEnd(activity!!, GuideUtils.END_GROUPLIST_KEY, false)) {
            dialog_pop?.visibility = View.GONE
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

    companion object {
        private val SCENE_MAX_COUNT = 16
    }
}
