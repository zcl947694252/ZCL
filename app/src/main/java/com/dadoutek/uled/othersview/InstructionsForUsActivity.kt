package com.dadoutek.uled.othersview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient

import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import kotlinx.android.synthetic.main.toolbar.*

class InstructionsForUsActivity : TelinkBaseActivity() {

    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_for_us)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setNavigationIcon(R.drawable.icon_return)
        webView = findViewById<WebView>(R.id.webView)
        val webSettings = webView!!.settings
        //设置WebView属性，能够执行Javascript脚本
        webSettings.javaScriptEnabled = true
        //设置可以访问文件
        webSettings.allowFileAccess = true
        //加载需要显示的网页
        //支持自动适配
        webSettings.useWideViewPort = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)  //支持放大缩小
        webSettings.allowFileAccess = true // 允许访问文件
        webSettings.saveFormData = true
        webSettings.setGeolocationEnabled(true)
        webSettings.domStorageEnabled = true
        webView!!.clearCache(true)
        if(isZh(this)){
            webView!!.loadUrl("https://dev.dadoutek.com/static/README2.0/index.html")
        }else{
            webView!!.loadUrl("http://www.dadoutek.com/app/README/index.html?lang=1")
        }

        //设置Web视图
//        webView!!.webViewClient = webViewClient()
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
