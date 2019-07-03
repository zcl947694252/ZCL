package com.dadoutek.uled.user

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Toast
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.intf.SyncCallback
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbUser
import com.dadoutek.uled.model.HttpModel.AccountModel
import com.dadoutek.uled.network.NetworkObserver
import com.dadoutek.uled.othersview.MainActivity
import com.dadoutek.uled.tellink.TelinkBaseActivity
import com.dadoutek.uled.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_enter_confirmation_code.*
import kotlinx.android.synthetic.main.activity_verification_code.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EnterConfirmationCodeActivity : TelinkBaseActivity(), View.OnClickListener {


    private var isChangePwd = false

    private val TIME_INTERVAL: Long = 60

    private val mCompositeDisposable = CompositeDisposable()

    private var countryCode: String? = null

    private var areaCode: String? = null

    private var phone: String? = null

    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_confirmation_code)
        type = this.intent.extras!!.getString(Constant.TYPE_USER)
        initViewType()
        initView()
    }

    private fun initViewType() {
        if (type == Constant.TYPE_VERIFICATION_CODE) {
            countryCode = this.intent.extras!!.getString("country_code")
            phone = this.intent.extras!!.getString("phone")
            codePhone.text = resources.getString(R.string.send_code)+"+" + countryCode + phone
        }
    }

    private fun initView() {
        verCodeInputView.setOnCompleteListener(VerCodeInputView.OnCompleteListener {
            verificationLogin()
        })
        SMSSDK.registerEventHandler(eventHandler)
        refresh_code.setOnClickListener(this)
        image_return.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.refresh_code -> verificationCode()
            R.id.return_image -> finish()
        }
    }

    val eventHandler = object : EventHandler() {
        override fun afterEvent(event: Int, result: Int, data: Any?) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            val msg = Message()
            msg.arg1 = event
            msg.arg2 = result
            msg.obj = data
            Handler(Looper.getMainLooper(), Handler.Callback { msg ->
                val event = msg.arg1
                val result = msg.arg2
                val data = msg.obj
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // TODO 处理成功得到验证码的结果
                        // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                        ToastUtils.showLong(R.string.send_message_success)
                        timing()
                    } else {
                        // TODO 处理错误的结果
                        if (result == SMSSDK.RESULT_ERROR) {
                            val a = (data as Throwable)
                            val jsonObject = JSONObject(a.localizedMessage)
                            val message = jsonObject.opt("detail").toString()
                            ToastUtils.showLong(message)
                        } else {
                            val a = (data as Throwable)
                            a.printStackTrace()
                            ToastUtils.showLong(a.message)
                        }
                    }
                    hideLoadingDialog()
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // TODO 处理验证成功的结果
                        if (isChangePwd) {

                        } else {
                            verificationLogin()
                        }
                    } else {
                        // TODO 处理错误的结果
                        if (result == SMSSDK.RESULT_ERROR) {
                            val a = (data as Throwable)
                            val jsonObject = JSONObject(a.localizedMessage)
                            val message = jsonObject.opt("detail").toString()
                            ToastUtils.showLong(message)
                            hideLoadingDialog()
                        } else {
                            val a = (data as Throwable)
                            a.printStackTrace()
                            ToastUtils.showLong(a.message)
                        }
                    }
                }
                // TODO 其他接口的返回结果也类似，根据event判断当前数据属于哪个接口
                false
            }).sendMessage(msg)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun timing() {
        mCompositeDisposable.add(Observable.intervalRange(0, TIME_INTERVAL, 0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val num = 59 - it as Long
                    if (num == 0L) {
                        reacquireCode.text = resources.getString(R.string.reget)
                        image_refresh.visibility = View.VISIBLE
                        reacquireCode.setTextColor(Color.parseColor("#18B4ED"))
                        refresh_code.isEnabled = true
                    } else {
                        reacquireCode.text = num.toString() + "s" + resources.getString(R.string.reacquire)
                        reacquireCode.setTextColor(Color.parseColor("#999999"))
                        refresh_code.isEnabled = false
                        image_refresh.visibility = View.GONE
                    }
                })
    }

    private fun verificationCode() {
        if (NetWorkUtils.isNetworkAvalible(this)) {
            send_verification()
        } else {
            ToastUtils.showLong(getString(R.string.net_work_error))
        }
    }

    private fun send_verification() {
        if (com.blankj.utilcode.util.StringUtils.isEmpty(phone)) {
            ToastUtils.showShort(R.string.phone_cannot_be_empty)
        } else {
            showLoadingDialog(getString(R.string.get_code_ing))
            SMSSDK.getVerificationCode(countryCode, phone)
        }
    }


    private fun verificationLogin() {
        if (!StringUtils.isTrimEmpty(phone)) {
            showLoadingDialog(getString(R.string.logging_tip))
            AccountModel.smsLogin(phone!!)
                    .subscribe(object : NetworkObserver<DbUser>() {
                        override fun onNext(dbUser: DbUser) {
                            DBUtils.deleteLocalData()
//                            ToastUtils.showLong(R.string.login_success)
//                            hideLoadingDialog()
                            //判断是否用户是首次在这个手机登录此账号，是则同步数据
//                            showLoadingDialog(getString(R.string.sync_now))
                            SyncDataPutOrGetUtils.syncGetDataStart(dbUser, syncCallback)
                            SharedPreferencesUtils.setUserLogin(true)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            LogUtils.d("logging: " + "登录错误" + e.message)
                            hideLoadingDialog()
                        }
                    })
        } else {
            Toast.makeText(this, getString(R.string.phone_or_password_can_not_be_empty), Toast.LENGTH_SHORT).show()
        }
    }

    internal var syncCallback: SyncCallback = object : SyncCallback {

        override fun start() {
            showLoadingDialog(getString(R.string.tip_start_sync))
        }

        override fun complete() {
            syncComplet()
        }

        override fun error(msg: String) {
            LogUtils.d("GetDataError:$msg")
        }

    }

    private fun syncComplet() {
//        ToastUtils.showLong(getString(R.string.upload_complete))
        hideLoadingDialog()
        TransformView()
    }

    private fun TransformView() {
        startActivity(Intent(this@EnterConfirmationCodeActivity, MainActivity::class.java))
        finish()
    }
}
