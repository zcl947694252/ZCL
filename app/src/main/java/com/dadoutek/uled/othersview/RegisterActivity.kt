package com.dadoutek.uled.othersview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.dadoutek.uled.R
import com.dadoutek.uled.base.TelinkBaseActivity
import com.dadoutek.uled.intf.SyncCallback
import com.dadoutek.uled.model.Constant
import com.dadoutek.uled.model.dbModel.DbUser
import com.dadoutek.uled.model.httpModel.UpdateModel
import com.dadoutek.uled.user.EnterConfirmationCodeActivity
import com.dadoutek.uled.util.NetWorkUtils
import com.dadoutek.uled.util.StringUtils
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.btn_send_verification
import kotlinx.android.synthetic.main.activity_register.edit_user_phone
import kotlinx.android.synthetic.main.activity_register.register_completed
import kotlinx.android.synthetic.main.activity_register.return_image
import org.json.JSONObject

/**
 * Created by hejiajun on 2018/5/16.
 */

class RegisterActivity : TelinkBaseActivity(), View.OnClickListener, TextWatcher {
    private var userName: String? = null
    private var countryCode: String = "86"
    private var isChangePwd = false
    private var dbUser: DbUser? = null
    private var isPassword = false
    private var isPasswordAgain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_register)
        initView()
        initListener()
        SMSSDK.registerEventHandler(eventHandler)
    }

    private fun initListener() {
        register_completed.setOnClickListener(this)
        btn_send_verification.setOnClickListener(this)
        image_password_btn.setOnClickListener(this)
        image_again_password_btn.setOnClickListener(this)
        return_image.setOnClickListener(this)
        regist_country_code_arrow.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> if (resultCode == Activity.RESULT_OK) {
                val bundle = data?.extras
                val countryName = bundle?.getString("countryName")
                val countryNumber = bundle?.getString("countryNumber")

                val toString = countryNumber?.replace("+", "").toString()
                LogUtils.v("zcl------------------countryCode接手前$countryCode")
                if (TextUtils.isEmpty(countryCode))
                    return
                LogUtils.v("zcl------------------countryCode接收后$countryCode")
                countryCode = toString
                regist_ccp_tv.text = countryName + countryNumber
                LogUtils.v("zcl------------------countryCode$countryCode")
            }
            0 -> {
                if (resultCode == Activity.RESULT_FIRST_USER) {
                    setResult(Activity.RESULT_FIRST_USER)
                    finish()
                }
            }
            else -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SMSSDK.unregisterEventHandler(eventHandler)
    }

    private fun initView() {
        val changeKey = intent.getStringExtra("fromLogin")
        isChangePwd = changeKey != "register"
        StringUtils.initEditTextFilterForRegister(edit_user_phone)
        StringUtils.initEditTextFilterForRegister(edit_user_password)
        StringUtils.initEditTextFilterForRegister(again_password)

        edit_user_phone.addTextChangedListener(this)

        if (isChangePwd) {
            dbUser = DbUser()
            register_completed.setText(R.string.confirm)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.regist_country_code_arrow -> {
                val intent = Intent()
                intent.setClass(this@RegisterActivity, CountryActivity::class.java)
                startActivityForResult(intent, 10)
            }
            R.id.register_completed -> {
                if (NetWorkUtils.isNetworkAvalible(this)) {
                    userName = edit_user_phone!!.text.toString().trim { it <= ' ' }
                    if (compileExChar(userName!!)) {
                        ToastUtils.showLong(R.string.phone_input_error)
                        return
                    }
                    if (com.blankj.utilcode.util.StringUtils.isEmpty(userName)) {
                        ToastUtils.showLong(R.string.phone_cannot_be_empty)
                        return
                    }

                    UpdateModel.isRegister(userName!!)?.subscribe({
                        if (!it) {
                            regist_frist_progress.visibility = View.GONE
                            SMSSDK.getVerificationCode(countryCode, userName)

                        } else {
                            ToastUtils.showLong(getString(R.string.account_exist))
                        }
                    }, {})

                } else {
                    ToastUtils.showLong(getString(R.string.network_unavailable))
                }
            }
            R.id.btn_send_verification ->
                if (NetWorkUtils.isNetworkAvalible(this)) {
                    sendVerification()
                } else {
                    ToastUtils.showLong(getString(R.string.network_unavailable))
                }

            R.id.image_password_btn -> eyePassword()
            R.id.image_again_password_btn -> eyePasswordAgain()
            R.id.return_image -> {
                SMSSDK.unregisterEventHandler(eventHandler)
                finish()
            }

        }
    }

    private val eventHandler = object : EventHandler() {
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

                regist_frist_progress.visibility = View.GONE
                register_completed.isClickable = true

                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                        ToastUtils.showLong(R.string.send_message_success)
                        goSkipActivity()
                    } else {
                        if (result == SMSSDK.RESULT_ERROR) {
                            try {
                                val a = (data as Throwable)
                                val jsonObject = JSONObject(a.localizedMessage)
                                val message = jsonObject.opt("detail").toString()
                                when{
                                    message.contains("请填写正确的")->ToastUtils.showShort(getString(R.string.right_phone_num))
                                    message.contains("提交校验的验证")->ToastUtils.showShort(getString(R.string.submit_error_code))
                                    message.contains("验证码失效")->ToastUtils.showShort(getString(R.string.verification_code_invalid))
                                    else-> ToastUtils.showLong(message)
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            val a = (data as Throwable)
                            a.printStackTrace()
                            ToastUtils.showShort(getString(R.string.send_message_fail))
                            //ToastUtils.showLong(a.message)
                        }
                    }
                }
                false
            }).sendMessage(msg)
        }
    }

    private fun goSkipActivity() {
        register_completed.isClickable = false
        var intent = Intent(this@RegisterActivity, EnterConfirmationCodeActivity::class.java)
        intent.putExtra(Constant.TYPE_USER, Constant.TYPE_REGISTER)
        intent.putExtra("country_code", countryCode)
        intent.putExtra("phone", edit_user_phone!!.text.toString().trim { it <= ' ' }.replace(" ".toRegex(), ""))

        startActivityForResult(intent, 0)
    }

    private fun eyePasswordAgain() {
        if (isPasswordAgain) {
            image_again_password_btn.setImageResource(R.drawable.icon_turn)
            isPasswordAgain = false
            again_password.transformationMethod = PasswordTransformationMethod.getInstance()
            again_password.setSelection(again_password.text.length)
        } else {
            isPasswordAgain = true
            image_again_password_btn.setImageResource(R.drawable.icon_open_eye)
            again_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            again_password.setSelection(again_password.text.length)
        }
    }

    private fun eyePassword() {
        if (isPassword) {
            image_password_btn.setImageResource(R.drawable.icon_turn)
            isPassword = false
            edit_user_password.transformationMethod = PasswordTransformationMethod.getInstance()
            edit_user_password.setSelection(edit_user_password.text.length)
        } else {
            isPassword = true
            image_password_btn.setImageResource(R.drawable.icon_open_eye)
            edit_user_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            edit_user_password.setSelection(edit_user_password.text.length)
        }
    }

    private fun sendVerification() {
        val phoneNum = edit_user_phone.text.toString().trim { it <= ' ' }
        if (com.blankj.utilcode.util.StringUtils.isEmpty(phoneNum)) {
            ToastUtils.showLong(R.string.phone_cannot_be_empty)
        } else {
            SMSSDK.getVerificationCode(countryCode, phoneNum)
        }
    }

    internal var syncCallback: SyncCallback = object : SyncCallback {

        override fun start() {
            showLoadingDialog(getString(R.string.tip_start_sync))
        }

        override fun complete() {
            hideLoadingDialog()
            syncComplet()
        }

        override fun error(msg: String) {
            hideLoadingDialog()
            ToastUtils.showLong(msg)
        }
    }

    private fun syncComplet() {
        hideLoadingDialog()
        transformView()
    }

    private fun transformView() {
        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
        SMSSDK.unregisterEventHandler(eventHandler)
        finish()
    }

    override fun afterTextChanged(p0: Editable?) {}

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (TextUtils.isEmpty(p0.toString())) {
            register_completed.background = getDrawable(R.drawable.btn_rec_black_bt)
            register_phone_line_b.background = getDrawable(R.drawable.line_gray)
        } else {
            register_completed.background = getDrawable(R.drawable.btn_rec_blue_bt)
            register_phone_line_b.background = getDrawable(R.drawable.line_blue)
        }
    }
}

