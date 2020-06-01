package com.dadoutek.uled.othersview

import android.content.Context
import android.view.KeyEvent
import com.dadoutek.uled.R
import com.dadoutek.uled.base.BaseActivity
import kotlinx.android.synthetic.main.activity_instructions_for_us.*
import kotlinx.android.synthetic.main.activity_user_agreement.*
import kotlinx.android.synthetic.main.toolbar.*

class UserAgreementActivity :BaseActivity(){
    override fun initListener() {

    }

    override fun initData() {

    }

    override fun initView() {
        toolbar.title = getString(R.string.user_agreement)
        toolbar.setNavigationIcon(R.drawable.navigation_back_white)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        val webSettings = webView_user_agreement?.settings
        //设置WebView属性，能够执行Javascript脚本
        webSettings?.javaScriptEnabled = true
        //设置可以访问文件
        webSettings?.allowFileAccess = true
        //加载需要显示的网页
        //支持自动适配
        webSettings?.useWideViewPort = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        webSettings?.loadWithOverviewMode = true
        webSettings?.setSupportZoom(true)  //支持放大缩小
        webSettings?.allowFileAccess = true // 允许访问文件
        webSettings?.saveFormData = true
        webSettings?.setGeolocationEnabled(true)
        webSettings?.domStorageEnabled = true
        webView_user_agreement!!.clearCache(true)
        if(isZh(this)){
            webView!!.loadUrl("https://dev.dadoutek.com/static/README/index.html?lang=0")
        }else{
            webView!!.loadUrl("http://www.dadoutek.com/app/README/index.html?lang=1")
        }
    }

    override fun setLayoutID(): Int {
        return R.layout.activity_user_agreement
    }

    override//设置回退 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
            webView!!.goBack() //goBack()表示返回WebView的上一页面
            return true
        }
        finish()//结束退出程序
        return false
    }

    private   fun isZh(context: Context): Boolean {
        val locale = context.resources.configuration.locale
        val language = locale.language
        return language.endsWith("zh")
    }

}