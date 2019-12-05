package com.xx.module.common.view.login

import android.content.Intent
import android.os.Build
import android.support.design.widget.TextInputEditText
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mrkj.base.bindView
import com.mrkj.lib.common.view.SmToast
import com.mrkj.module.sms.NumberCodeManager
import com.mrkj.module.sms.NumberErrorException
import com.xx.lib.db.entity.UserSystem
import com.xx.lib.db.exception.ReturnJsonCodeException
import com.xx.module.common.R
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.presenter.LoginPasswordPresenter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IViewManager
import com.xx.module.common.view.dialog.SmDefaultDialog
import com.xx.module.common.view.simple.SimpleOnTextWatch

/**
 *@author someone
 *@date 2019-05-29
 */
@Path(RouterUrl.ACTIVITY_LOGIN_PASSWORD)
@Presenter(LoginPasswordPresenter::class)
class PasswordSettingActivity : BaseActivity<LoginPasswordPresenter>(), IViewManager.IPasswordView, View.OnClickListener {


    private val phoneEt by bindView<TextView>(R.id.login_phone_name)
    private val privacyTv by bindView<TextView>(R.id.login_phone_privacy)
    private val passwordEt by bindView<TextInputEditText>(R.id.login_phone_code_et)
    private val passworAgaindEt by bindView<TextInputEditText>(R.id.login_phone_code_et_again)

    private val getCodeBtn by bindView<TextView>(R.id.login_phone_get_code)
    private val numberEt by bindView<EditText>(R.id.login_phone_number_et)

    private val submitView by bindView<TextView>(R.id.login_phone_submit)
    private val cancelView by bindView<ImageView>(R.id.login_phone_close)

    private var phone = ""
    private var passwordType: String = ""

    private var newPassword = ""
    private var newPasswordAgain = ""

    override fun getLayoutId(): Int = R.layout.activty_login_password

    override fun initViewsAndEvents() {
        setStatusBar(true, true)
        phone = intent?.getStringExtra(RouterParams.LoginView.PHONE_NUM) ?: ""
        passwordType = intent?.getStringExtra(RouterParams.LoginView.PASSWORD_TYPE) ?: ""
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(passwordType)) {
            Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        privacyTv.visibility = if (passwordType == RouterParams.LoginView.PASSWORD_TYPE_CHANGE) View.GONE else View.VISIBLE
        phoneEt.text = phone
        if (passwordType == RouterParams.LoginView.PASSWORD_TYPE_REGISTER) {
            //注册是从登录首页进来的，不用再获取验证码了
            findViewById<View>(R.id.login_phone_number_input).visibility = View.GONE
        }

        setPrivacyText()
        cancelView.setOnClickListener(this)
        submitView.setOnClickListener(this)

        passwordEt.addTextChangedListener(object : SimpleOnTextWatch() {
            override fun afterTextChanged(s: Editable?) {
                newPassword = presenter.clearEmpty(s?.toString() ?: "")
                if (TextUtils.isEmpty(newPasswordAgain) && !TextUtils.isEmpty(newPassword)) {
                    submitView.isEnabled = true
                }
            }

        })
        passworAgaindEt.addTextChangedListener(object : SimpleOnTextWatch() {
            override fun afterTextChanged(s: Editable?) {
                newPasswordAgain = presenter.clearEmpty(s?.toString() ?: "")
                if (TextUtils.isEmpty(newPasswordAgain) && !TextUtils.isEmpty(newPassword)) {
                    submitView.isEnabled = true
                }
            }
        })



        getCodeBtn.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        if (v == cancelView) {
            onBackPressed()
        } else if (submitView == v) {
            if (!TextUtils.equals(newPassword, newPasswordAgain)) {
                SmToast.show(this, "两次输入密码不一致")
            } else {
                if (passwordType == RouterParams.LoginView.PASSWORD_TYPE_REGISTER) {
                    //注册，此处不需要重复获取验证码
                    presenter.register(phone, newPassword)
                } else {
                    //修改密码需要验证码
                    val code = numberEt.text.toString().replace(" ", "")
                    if (TextUtils.isEmpty(code) || !TextUtils.isDigitsOnly(code)) {
                        SmToast.show(this, "请输入验证码")
                        return
                    }
                    NumberCodeManager.getInstance().submitVerificationCode(this, "86", phone, code.toInt())

                }
            }
        } else if (v == getCodeBtn) {
            getNumberCode()
        }
    }

    fun getNumberCode() {
        //获取验证码
        NumberCodeManager.getInstance().getVerificationCode(this, "86", phone)
    }


    override fun onStart() {
        super.onStart()
        NumberCodeManager.getInstance().register(this, getCodeBtn, object : NumberCodeManager.SimpleSubmitCallback() {
            override fun onSubmitPass() {
                //验证通过
                getCodeBtn.isEnabled = true
                //提交密码修改或者注册
                if (passwordType == RouterParams.LoginView.PASSWORD_TYPE_CHANGE) {
                    presenter.changePassword(phone, newPassword)
                } else {
                    //注册
                    presenter.register(phone, newPassword)
                }
            }

            override fun onSendComplete() {
                findViewById<View>(R.id.main_login_password_input_1).visibility = View.VISIBLE
                getCodeBtn.visibility = View.VISIBLE
                numberEt.post {
                    numberEt.requestFocus()
                }
            }

            override fun onError(e: Throwable) {
                val message = if (e is ReturnJsonCodeException || e is NumberErrorException) {
                    e.message
                } else {
                    "请稍后重试"
                }
                SmToast.showToastRight(this@PasswordSettingActivity, message)
            }

        })
    }

    override fun onStop() {
        super.onStop()
        NumberCodeManager.getInstance().unRegister(this, getCodeBtn)
    }

    /**
     * 用户协议文字和点击事件
     */
    private fun setPrivacyText() {
        val msg = getString(R.string.phone_login_agreement, LoginMainActivity.SubClickSpan.TO_USER, getString(R.string.user_tip),
                LoginMainActivity.SubClickSpan.TO_PRIVACY, getString(R.string.privacy_tip))
        val sp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(msg)
        }
        val urlSpan = sp.getSpans(0, sp.length, URLSpan::class.java)
        val stylesBuilder = SpannableStringBuilder(sp.toString())
        stylesBuilder.clearSpans()
        urlSpan.forEach {
            val newSp: LoginMainActivity.SubClickSpan?
            when {
                it.url == LoginMainActivity.SubClickSpan.TO_USER -> {
                    newSp = LoginMainActivity.SubClickSpan(this@PasswordSettingActivity, 0)
                }
                it.url == LoginMainActivity.SubClickSpan.TO_PRIVACY -> {
                    newSp = LoginMainActivity.SubClickSpan(this@PasswordSettingActivity, 1)
                }
                else -> newSp = null
            }
            newSp?.let { sub ->
                stylesBuilder.setSpan(sub, sp.getSpanStart(it), sp.getSpanEnd(it), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        privacyTv.text = stylesBuilder
        privacyTv.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * 注册成功回调
     */
    override fun onRegisterResult(us: UserSystem?, error: SmError?) {
        if (error != null) {
            object : SmDefaultDialog.Builder(this) {}
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            //注册成功
            UserDataManager.getInstance().userSystem = us
            startActivityForResult(Intent(this, RegisterSuccessActivity::class.java), 1100)
        }
    }

    override fun onChangePasswordResult(us: UserSystem?, error: SmError?) {
        if (error != null) {
            object : SmDefaultDialog.Builder(this) {}
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            //登录成功
            UserDataManager.getInstance().userSystem = us
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1100) {
            //注册成功提示页面回来
            finish()
        }
    }
}