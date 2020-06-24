package com.dadoutek.uled.rgb

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbDiyGradient
import com.dadoutek.uled.model.ItemRgbGradient
import com.dadoutek.uled.othersview.MainActivity
import com.dadoutek.uled.util.SpeedDialog
import com.dadoutek.uled.widget.ActionSheetDialog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rgb_gradient.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import java.util.concurrent.TimeUnit

class RGBGradientActivity : TelinkBaseActivity(), View.OnClickListener {

    private var buildInModeList: ArrayList<ItemRgbGradient>? = null
    private var diyGradientList: MutableList<DbDiyGradient>? = null
    private var rgbGradientAdapter: RGBGradientAdapter? = null
    private var rgbDiyGradientAdapter: RGBDiyGradientAdapter? = null
    private var applyDisposable: Disposable? = null
    private var dstAddress: Int = 0
    private var firstLightAddress: Int = 0
    private var currentShowIsDiy = false
    var type = Constant.TYPE_GROUP
    var speed = 50
    var positionState = 0
    protected val FLIP_DISTANCE = 50f
    internal var mDetector: GestureDetector? = null

    private var isDiyMode = true

    private var isPresetMode = true

    private var diyPosition: Int = 0

    private var isDelete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rgb_gradient)
        initToolbar()
        initData()
        initView()
//        initGesture()
    }

    private fun initGesture() {
        mDetector = GestureDetector(this, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {

            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return false
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return false
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                try {
                    if (e1!!.getX() - e2!!.getX() > FLIP_DISTANCE) {
                        if (!currentShowIsDiy) {
                            changeToDiyPage()
                        }
                        Log.i("TAG", "<--- left, left, go go go");
                        return true;
                    }
                    if (e2?.getX() - e1?.getX() > FLIP_DISTANCE) {
                        if (currentShowIsDiy) {
                            changeToBuildInPage()
                        }
                        Log.i("TAG", "right, right, go go go --->");
                        return true;
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                return false
            }

            override fun onLongPress(e: MotionEvent?) {

            }
        }, null)
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return mDetector!!.onTouchEvent(event)
//    }

    override fun onPause() {
        super.onPause()
        applyDisposable?.dispose()
    }

    private fun initToolbar() {
        toolbarTv.text = getString(R.string.dynamic_gradient)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
//        btnStopGradient.visibility = View.VISIBLE
//        btnStopGradient.setOnClickListener(this)
//        normal_rgb.setOnClickListener(this)
    }

    private fun initData() {
        val intent = intent
        type = intent.getStringExtra(Constant.TYPE_VIEW)
        dstAddress = intent.getIntExtra(Constant.TYPE_VIEW_ADDRESS, 0)
        if (type == Constant.TYPE_GROUP) {
            val lightList = DBUtils.getLightByGroupMesh(dstAddress)
            if (lightList != null) {
                if (lightList.size > 0) {
                    firstLightAddress = lightList[0].meshAddr
                }
            }
        } else {
            firstLightAddress = dstAddress
        }
        buildInModeList = ArrayList()
        val presetGradientList = resources.getStringArray(R.array.preset_gradient)
        for (i in 0..10) {
            var item = ItemRgbGradient()
            item.name = presetGradientList[i]
            buildInModeList?.add(item)
        }

        diyGradientList = DBUtils.diyGradientList
    }

    private fun initView() {
//        changeToBuildInPage()
        btnAdd.setOnClickListener(this)
        mode_preset_layout.setOnClickListener(this)
        mode_diy_layout.setOnClickListener(this)
//        btnStopGradient.visibility = View.VISIBLE
        btnStopGradient.setOnClickListener(this)
    }

    private fun applyPresetView() {
//        this.sbSpeed!!.progress = speed
//        tvSpeed.text = getString(R.string.speed_text, speed.toString())
//        this.sbSpeed!!.setOnSeekBarChangeListener(this.barChangeListener)
        val layoutmanager = LinearLayoutManager(this)
        layoutmanager.orientation = LinearLayoutManager.VERTICAL
        builtInModeRecycleView!!.layoutManager = layoutmanager
        this.rgbGradientAdapter = RGBGradientAdapter(R.layout.item_gradient_mode, buildInModeList)
        builtInModeRecycleView?.itemAnimator = DefaultItemAnimator()
//        builtInModeRecycleView?.setOnTouchListener { v, event ->
////            mDetector?.onTouchEvent(event)
//            false
//        }
        val decoration = DividerItemDecoration(this,
                DividerItemDecoration
                        .VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(this, R.color
                .divider)))
        //添加分割线
        builtInModeRecycleView?.addItemDecoration(decoration)
        rgbGradientAdapter!!.onItemChildClickListener = onItemChildClickListener
        rgbGradientAdapter!!.bindToRecyclerView(builtInModeRecycleView)
    }

    private fun applyDiyView() {
        val layoutmanager = LinearLayoutManager(this)
        layoutmanager.orientation = LinearLayoutManager.VERTICAL
        builtDiyModeRecycleView!!.layoutManager = layoutmanager
        this.rgbDiyGradientAdapter = RGBDiyGradientAdapter(R.layout.activity_diy_gradient_item, diyGradientList, isDelete)
        builtDiyModeRecycleView?.itemAnimator = DefaultItemAnimator()
//        builtDiyModeRecycleView?.setOnTouchListener { v, event ->
//            mDetector?.onTouchEvent(event)
//            false
//        }
        val decoration = DividerItemDecoration(this,
                DividerItemDecoration
                        .VERTICAL)
        decoration.setDrawable(ColorDrawable(ContextCompat.getColor(this, R.color
                .black_ee)))
        //添加分割线
        builtDiyModeRecycleView?.addItemDecoration(decoration)
        rgbDiyGradientAdapter!!.onItemChildClickListener = onItemChildClickListenerDiy
        rgbDiyGradientAdapter!!.onItemLongClickListener = this.onItemChildLongClickListenerDiy
        rgbDiyGradientAdapter!!.bindToRecyclerView(builtDiyModeRecycleView)

//        rgbDiyGradientAdapter!!.onItemChildClickListener = onItemChildDiyClickListener
    }

    private val barChangeListener = object : SeekBar.OnSeekBarChangeListener {


        override fun onStopTrackingTouch(seekBar: SeekBar) {
            this.onValueChange(seekBar, seekBar.progress, true)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                       fromUser: Boolean) {
//            tvSpeed.text = getString(R.string.speed_text, progress.toString())
        }

        @SuppressLint("StringFormatInvalid")
        private fun onValueChange(view: View, progress: Int, immediate: Boolean) {
            speed = progress
            if (speed == 0) {
                speed = 1
            }
//            tvSpeed.text = getString(R.string.speed_text, speed.toString())
            if (positionState != 0) {
                stopGradient()
                Thread.sleep(200)
                Commander.applyGradient(dstAddress, positionState, speed, firstLightAddress)
            }
        }
    }

    var onItemChildLongClickListenerDiy = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
        isDelete = true
        rgbDiyGradientAdapter!!.changeState(isDelete)
        refreshData()
        return@OnItemLongClickListener true
    }

    private fun refreshData() {
        rgbDiyGradientAdapter!!.notifyDataSetChanged()
        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.GONE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.GONE
        toolbar!!.title = ""

        deleteDiyGradient()
    }

    private fun deleteDiyGradient() {
        var batchGroup = toolbar.findViewById<ImageView>(R.id.img_function2)
        batchGroup.visibility = View.GONE
        batchGroup.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.delete_model))
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                for (i in diyGradientList!!.indices) {
                    if (diyGradientList!![i].isSelected) {
                        startDeleteGradientCmd(diyGradientList!![i].id)
                        DBUtils.deleteGradient(diyGradientList!![i])
                        DBUtils.deleteColorNodeList(DBUtils.getColorNodeListByDynamicModeId(diyGradientList!![i].id!!))
                    }
                }
                diyGradientList = DBUtils.diyGradientList
                rgbDiyGradientAdapter!!.setNewData(diyGradientList)
            }
            builder.setNeutralButton(R.string.cancel) { dialog, which -> }
            builder.create().show()
        }


//        batchGroup.setOnClickListener(View.OnClickListener {
//            for(i in diyGradientList!!.indices){
//                if(diyGradientList!![i].isSelected){
//                    startDeleteGradientCmd(diyGradientList!![i].id)
//                    DBUtils.deleteGradient(diyGradientList!![i])
//                    DBUtils.deleteColorNodeList(DBUtils.getColorNodeListByDynamicModeId(diyGradientList!![i].id!!))
//                    rgbDiyGradientAdapter!!.notifyDataSetChanged()
//                }
//            }
//        })
    }


    private var onItemChildClickListenerDiy = OnItemChildClickListener { adapter, view, position ->

        when (view!!.getId()) {
            R.id.diy_mode_on -> {
                //应用自定义渐变
                Thread {
                    stopGradient()
                    Thread.sleep(200)
                    Commander.applyDiyGradient(dstAddress, diyGradientList!![position].id.toInt(),
                            diyGradientList!![position].speed, firstLightAddress)

                }.start()
                diyPosition = position

                diyGradientList!![position].select = true

                for (i in diyGradientList!!.indices) {
                    if (i != position) {
                        if (diyGradientList!![i].select) {
                            diyGradientList!![i].select = false
                            DBUtils.updateGradient(diyGradientList!![i])
                        }
                    }
                }
                rgbDiyGradientAdapter!!.notifyDataSetChanged()
            }

            R.id.diy_mode_off -> {

                Commander.closeGradient(dstAddress, diyGradientList!![position].id.toInt(), diyGradientList!![position].speed)
                diyGradientList!![position].select = false
                rgbDiyGradientAdapter!!.notifyItemChanged(position)
                DBUtils.updateGradient(diyGradientList!![position])

            }

            R.id.diy_mode_set -> {
                val intent = Intent(this, SetDiyColorAct::class.java)
                intent.putExtra(Constant.IS_CHANGE_COLOR, true)
                intent.putExtra(Constant.GRADIENT_KEY, diyGradientList!![position])
                intent.putExtra(Constant.TYPE_VIEW_ADDRESS, dstAddress)
                startActivityForResult(intent, 0)
            }

            R.id.diy_selected -> {
                diyGradientList!![position].isSelected = !diyGradientList!![position].isSelected
            }
        }

    }

//    private var onItemChildDiyClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
//        when (view.id) {
//            R.id.more -> {
//                showMoreSetDialog(adapter, position)
//            }
//        }
//    }

    private fun showMoreSetDialog(adapter: BaseQuickAdapter<Any, BaseViewHolder>, position: Int) {
        val dialog = ActionSheetDialog.ActionSheetBuilder(this@RGBGradientActivity, R.style.ActionSheetDialogBase)
                .setItems(arrayOf<CharSequence>(getString(R.string.delete), getString(R.string.edit)), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        when (which) {
                            0 -> {
                                deleteGradient(position, adapter, dialog)
                            }
                            1 -> {
                                transChangeAct(diyGradientList!![position])
                                dialog.dismiss()
                            }
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), { dialog, which -> })
                .setCancelable(true)
                .create()
        dialog.show()
    }

    fun deleteGradient(position: Int, adapter: BaseQuickAdapter<Any, BaseViewHolder>, dialog: DialogInterface) {
        startDeleteGradientCmd(diyGradientList!![position].id)
        DBUtils.deleteGradient(diyGradientList!![position])
        DBUtils.deleteColorNodeList(DBUtils.getColorNodeListByDynamicModeId(diyGradientList!![position].id!!))
        adapter.remove(position)
        dialog.dismiss()
    }

    private fun startDeleteGradientCmd(id: Long) {
        Commander.deleteGradient(dstAddress, id.toInt(), {}, {})
    }

    private var onItemChildClickListener = OnItemChildClickListener { adapter, view, position ->

        when (view!!.getId()) {
            R.id.gradient_mode_on -> {
                //应用内置渐变
                applyDisposable?.dispose()
                applyDisposable = Observable.timer(50, TimeUnit.MILLISECONDS, Schedulers.io())
                        .subscribe {
                            for (i in 0..2) {
                                stopGradient()
                                Thread.sleep(50)
                            }
                            positionState = position + 1
                            Commander.applyGradient(dstAddress, positionState, speed, firstLightAddress)
                        }
                buildInModeList!![position].select = true

                for (i in buildInModeList!!.indices) {
                    if (i != position) {
                        if (buildInModeList!![i].select) {
                            buildInModeList!![i].select = false
                        }
                    }
                }
                rgbGradientAdapter!!.notifyDataSetChanged()
            }

            R.id.gradient_mode_off -> {
                buildInModeList!![position].select = false
                rgbGradientAdapter!!.notifyItemChanged(position)
                stopGradient()
            }

            R.id.gradient_mode_set -> {
                var dialog = SpeedDialog(this, speed, R.style.Dialog, SpeedDialog.OnSpeedListener {
                    speed = it
                    stopGradient()
                    Thread.sleep(200)
                    Commander.applyGradient(dstAddress, positionState, speed, firstLightAddress)
                })
                dialog.show()
            }
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mode_diy_layout -> {
                changeToDiyPage()
            }
            R.id.mode_preset_layout -> {
                changeToBuildInPage()
            }
            R.id.btnStopGradient -> {
                stopGradient()
            }
            R.id.normal_rgb -> {
                finish()
            }
            R.id.btnAdd -> {
                transAddAct()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        diyGradientList = DBUtils.diyGradientList
        this.rgbDiyGradientAdapter = RGBDiyGradientAdapter(R.layout.activity_diy_gradient_item, diyGradientList, isDelete)
        isDelete = false
        rgbDiyGradientAdapter!!.changeState(isDelete)
        toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
        toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
        toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
        toolbar!!.title = getString(R.string.dynamic_gradient)
        rgbDiyGradientAdapter!!.notifyDataSetChanged()
        setDate()
        isDiyMode = true
        changeToDiyPage()
    }

    private fun transAddAct() {
        if (DBUtils.diyGradientList.size < 6) {
            val intent = Intent(this, SetDiyColorAct::class.java)
            intent.putExtra(Constant.IS_CHANGE_COLOR, false)
            intent.putExtra(Constant.TYPE_VIEW_ADDRESS, dstAddress)
            startActivityForResult(intent, 0)
        } else {
            ToastUtils.showLong(getString(R.string.add_gradient_limit))
        }
    }

    private fun transChangeAct(dbDiyGradient: DbDiyGradient) {
        val intent = Intent(this, SetDiyColorAct::class.java)
        intent.putExtra(Constant.IS_CHANGE_COLOR, true)
        intent.putExtra(Constant.GRADIENT_KEY, dbDiyGradient)
        intent.putExtra(Constant.TYPE_VIEW_ADDRESS, dstAddress)
        startActivityForResult(intent, 0)
    }

    private fun changeToDiyPage() {
//        currentShowIsDiy=true
//        diyButton.setBackgroundColor(resources.getColor(R.color.primary))
//        diyButton.setTextColor(resources.getColor(R.color.white))
//        buildInButton.setBackgroundColor(resources.getColor(R.color.white))
//        buildInButton.setTextColor(resources.getColor(R.color.primary))
//        layoutModeDiy.visibility = View.VISIBLE
//        layoutModePreset.visibility = View.GONE
        if (isDiyMode) {
            diyButton.setTextColor(resources.getColor(R.color.blue_background))
            diyButton_image.setImageResource(R.drawable.icon_selected_rgb)
            layoutModeDiy.visibility = View.VISIBLE
            buildInButton.setTextColor(resources.getColor(R.color.black_three))
            buildInButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            isDiyMode = false
        } else {
            diyButton.setTextColor(resources.getColor(R.color.black_three))
            diyButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            layoutModeDiy.visibility = View.GONE
            buildInButton.setTextColor(resources.getColor(R.color.black_three))
            buildInButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            isDiyMode = true
        }
        applyDiyView()
    }

    private fun changeToBuildInPage() {
//        currentShowIsDiy=false
        if (isPresetMode) {
            buildInButton.setTextColor(resources.getColor(R.color.blue_background))
            buildInButton_image.setImageResource(R.drawable.icon_selected_rgb)
            layoutModePreset.visibility = View.VISIBLE
            diyButton.setTextColor(resources.getColor(R.color.black_three))
            diyButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            isPresetMode = false
        } else {
            buildInButton.setTextColor(resources.getColor(R.color.black_three))
            buildInButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            layoutModePreset.visibility = View.GONE
            diyButton.setTextColor(resources.getColor(R.color.black_three))
            diyButton_image.setImageResource(R.drawable.icon_unselected_rgb)
            isPresetMode = true
        }
        applyPresetView()
    }

    fun stopGradient() {
        Commander.closeGradient(dstAddress, positionState, speed)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isDelete) {
                    isDelete = false
                    rgbDiyGradientAdapter!!.changeState(isDelete)
                    toolbar!!.findViewById<ImageView>(R.id.img_function2).visibility = View.GONE
                    toolbar!!.findViewById<ImageView>(R.id.image_bluetooth).visibility = View.VISIBLE
                    toolbar!!.findViewById<ImageView>(R.id.img_function1).visibility = View.VISIBLE
                    toolbar!!.title = getString(R.string.dynamic_gradient)
                    rgbDiyGradientAdapter!!.notifyDataSetChanged()
                    setDate()
                } else {
                    ActivityUtils.finishToActivity(MainActivity::class.java, false, true)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setDate() {
        for (i in diyGradientList!!.indices) {
            if (diyGradientList!![i].isSelected) {
                diyGradientList!![i].isSelected = false
            }
        }
    }

}