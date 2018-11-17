package com.dadoutek.uled.rgb

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dadoutek.uled.R
import com.dadoutek.uled.communicate.Commander
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.ItemRgbGradient
import com.dadoutek.uled.tellink.TelinkBaseActivity
import kotlinx.android.synthetic.main.activity_rgb_gradient.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class RGBGradientActivity : TelinkBaseActivity(), View.OnClickListener {

    private var buildInModeList: ArrayList<ItemRgbGradient>? = null
    private var rgbGradientAdapter: RGBGradientAdapter? = null
    private var dstAddress: Int = 0
    var type = Constant.TYPE_GROUP
    var speed = 50
    var positionState = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rgb_gradient)
        initToolbar()
        initData()
        initView()
    }

    private fun initToolbar() {
        toolbar.title = getString(R.string.dynamic_gradient)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        btnStopGradient.visibility=View.VISIBLE
        btnStopGradient.setOnClickListener(this)
        normal_rgb.setOnClickListener(this)
    }

    private fun initData() {
        val intent = intent
        type = intent.getStringExtra(Constant.TYPE_VIEW)
        dstAddress = intent.getIntExtra(Constant.TYPE_VIEW_ADDRESS, 0)
        buildInModeList = ArrayList()
        val presetGradientList = resources.getStringArray(R.array.preset_gradient)
        for (i in 0..6) {
            var item = ItemRgbGradient()
            item.name = presetGradientList[i]
            buildInModeList?.add(item)
        }
    }

    private fun initView() {
        applyPresetView()
        diyButton.setOnClickListener(this)
        buildInButton.setOnClickListener(this)
        this.sbSpeed!!.progress = speed
        tvSpeed.text = getString(R.string.speed_text, speed.toString())
        this.sbSpeed!!.setOnSeekBarChangeListener(this.barChangeListener)
        btnStopGradient.visibility=View.VISIBLE
        btnStopGradient.setOnClickListener(this)
    }

    private fun applyPresetView(){
        val layoutmanager = LinearLayoutManager(this)
        layoutmanager.orientation = LinearLayoutManager.VERTICAL
        builtInModeRecycleView!!.layoutManager = layoutmanager
        this.rgbGradientAdapter = RGBGradientAdapter(R.layout.item_gradient_mode, buildInModeList)
        builtInModeRecycleView?.itemAnimator = DefaultItemAnimator()

        rgbGradientAdapter!!.onItemChildClickListener = onItemChildClickListener
        rgbGradientAdapter!!.bindToRecyclerView(builtInModeRecycleView)
    }

    private val barChangeListener = object : SeekBar.OnSeekBarChangeListener {


        override fun onStopTrackingTouch(seekBar: SeekBar) {
            this.onValueChange(seekBar, seekBar.progress, true)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                       fromUser: Boolean) {

        }

        @SuppressLint("StringFormatInvalid")
        private fun onValueChange(view: View, progress: Int, immediate: Boolean) {
            speed = progress
            tvSpeed.text = getString(R.string.speed_text, speed.toString())
            if(positionState!=0){
                Commander.applyGradient(dstAddress, positionState, speed, successCallback = {}, failedCallback = {})
            }
        }
    }

    private var onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
        positionState=position+1
        Commander.applyGradient(dstAddress, positionState, speed, successCallback = {}, failedCallback = {})
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.diyButton->{
                diyButton.setBackgroundColor(resources.getColor(R.color.mode_check_color))
                buildInButton.setBackgroundColor(resources.getColor(R.color.white))
            }
            R.id.buildInButton->{
                diyButton.setBackgroundColor(resources.getColor(R.color.white))
                buildInButton.setBackgroundColor(resources.getColor(R.color.mode_check_color))
            }
            R.id.btnStopGradient->{
                positionState = 0
               Commander.closeGradient(dstAddress,1, speed, successCallback = {}, failedCallback = {})
            }
            R.id.normal_rgb->{
               finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}