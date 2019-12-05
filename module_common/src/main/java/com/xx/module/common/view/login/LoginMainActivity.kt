package com.xx.module.common.view.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.ActivityManagerUtil
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
import com.xx.module.common.presenter.LoginMainPresenter
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IViewManager
import com.xx.module.common.view.dialog.SmDefaultDialog
import com.xx.module.common.view.simple.SimpleOnTextWatch
import com.umeng.socialize.bean.SHARE_MEDIA

/**
 *@author someone
 *@date 2019-05-28
 */
@Path(RouterUrl.ACTIVITY_LOGIN_MAIN)
@Presenter(LoginMainPresenter::class)
class LoginMainActivity : BaseActivity<LoginMainPresenter>(), IViewManager.IMainLoginView, View.OnClickListener {

    private val mHandler = Handler()

    private val closeBtn by bindView<ImageView>(R.id.main_login_close)
    private val nameEt by bindView<TextInputEditText>(R.id.main_login_name)
    private val passwordEt by bindView<TextInputEditText>(R.id.main_login_password)
    private val passwordEtInput by bindView<TextInputLayout>(R.id.main_login_password_input_1)
    private val loginSubmitView by bindView<TextView>(R.id.main_login_submit)
    private val registerBtn by bindView<TextView>(R.id.main_login_register)
    private val forgetBtn by bindView<TextView>(R.id.main_login_forget)
    private val codeBtn by bindView<TextView>(R.id.main_login_get_code)

    private val wxLogin by bindView<TextView>(R.id.main_login_layout_wx)
    private val qqLogin by bindView<TextView>(R.id.main_login_layout_qq)
    private val smLogin by bindView<TextView>(R.id.main_login_layout_sm)
    private val weiboLogin by bindView<TextView>(R.id.main_login_layout_weibo)

    private val privacyTv by bindView<TextView>(R.id.main_login_privacy)
    /**
     *  0 短信验证码登录  1账号密码登录
     */
    private var loginType = 0
    private var phone = ""

    private var submitBtnStatus = false
    private var lastSubmitText = ""

    override fun beforeSetContentView() {
        // setTheme(R.style.SmTheme_Night)
    }

    override fun getLayoutId(): Int = R.layout.activity_main_login


    override fun initViewsAndEvents() {
        setStatusBar(true, true)
        initEvent()
        setPrivacyText()
        setLoginType(0)
    }


    /**
     * 用户协议文字和点击事件
     */
    private fun setPrivacyText() {
        val msg = getString(R.string.phone_login_agreement, SubClickSpan.TO_USER, getString(R.string.user_tip),
                SubClickSpan.TO_PRIVACY, getString(R.string.privacy_tip))
        val sp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(msg)
        }
        val urlSpan = sp.getSpans(0, sp.length, URLSpan::class.java)
        val stylesBuilder = SpannableStringBuilder(sp.toString())
        stylesBuilder.clearSpans()
        urlSpan.forEach {
            val newSp: SubClickSpan?
            when {
                it.url == SubClickSpan.TO_USER -> {
                    newSp = SubClickSpan(this@LoginMainActivity, 0)
                }
                it.url == SubClickSpan.TO_PRIVACY -> {
                    newSp = SubClickSpan(this@LoginMainActivity, 1)
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

    class SubClickSpan(val context: Context, val type: Int) : ClickableSpan() {
        companion object {
            const val TO_USER = "user"
            const val TO_PRIVACY = "privacy"
        }

        override fun onClick(widget: View) {
            if (type == 0) {
                //用户协议
                ActivityRouter.get().startWebActivity(widget.context, "file:///android_asset/user_agreement.html", "")
            } else {
                //隐私条款
                ActivityRouter.get().startWebActivity(widget.context, "file:///android_asset/privacy_agreement.html", "")
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(context, R.color.text_faaf1e)
        }
    }

    private fun setLoginType(type: Int) {
        loginType = type
        checkLoginButtonStatus()
        if (loginType == 0) {
            if (NumberCodeManager.getInstance().isCountDown) {
                loginSubmitView.text = "下一步"
                findViewById<View>(R.id.main_login_password_input_1).visibility = View.VISIBLE
                codeBtn.visibility = View.VISIBLE
            } else {
                loginSubmitView.text = "获取短信验证码"
                findViewById<View>(R.id.main_login_password_input_1).visibility = View.INVISIBLE
                codeBtn.visibility = View.INVISIBLE
            }
            findViewById<View>(R.id.main_login_get_code_tip).visibility = View.VISIBLE
            forgetBtn.visibility = View.INVISIBLE
            passwordEtInput.hint = "请输入验证码"
            passwordEt.inputType = InputType.TYPE_CLASS_NUMBER
            val d = ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.icon_login_yanzhengma)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            passwordEt.setCompoundDrawables(d, null, null, null)
            registerBtn.text = "密码登录"
        } else {
            findViewById<View>(R.id.main_login_password_input_1).visibility = View.VISIBLE
            findViewById<View>(R.id.main_login_get_code_tip).visibility = View.INVISIBLE
            forgetBtn.visibility = View.VISIBLE
            codeBtn.visibility = View.INVISIBLE
            passwordEt.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            val d = ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.icon_login_mima)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            passwordEt.setCompoundDrawables(d, null, null, null)
            passwordEtInput.hint = "请输入密码"
            loginSubmitView.text = "登录"
            registerBtn.text = "验证码登录"
        }
    }

    private fun initEvent() {
        privacyTv.setOnClickListener(this)
        closeBtn.setOnClickListener(this)
        codeBtn.setOnClickListener(this)
        wxLogin.setOnClickListener(this)
        qqLogin.setOnClickListener(this)
        smLogin.setOnClickListener(this)
        weiboLogin.setOnClickListener(this)
        registerBtn.setOnClickListener(this)
        forgetBtn.setOnClickListener(this)
        loginSubmitView.setOnClickListener(this)
        nameEt.addTextChangedListener(object : SimpleOnTextWatch() {
            override fun afterTextChanged(s: Editable?) {
                checkLoginButtonStatus()
            }
        })
        loginSubmitView.isEnabled = false
        nameEt.setText("")
    }


    override fun onClick(v: View?) {
        when (v) {
            closeBtn -> finish()
            privacyTv -> {
                //隐私协议
            }
            weiboLogin -> {
                //微博登录
                loading()
                presenter.loginByThird(this, SHARE_MEDIA.SINA)
            }
            wxLogin -> {
                //微信登录
                loading()
                presenter.loginByThird(this, SHARE_MEDIA.WEIXIN)
            }
            qqLogin -> {
                //QQ登录
                loading()
                presenter.loginByThird(this, SHARE_MEDIA.QQ)
            }
            smLogin -> {
                //知命登录

            }
            codeBtn -> {
                //获取验证码
                getNumberCode()
            }
            forgetBtn -> {
                //忘记密码
                val params = mutableMapOf<String, String>()
                params[RouterParams.LoginView.PASSWORD_TYPE] = RouterParams.LoginView.PASSWORD_TYPE_CHANGE
                phone = nameEt.text.toString().replace(" ", "")
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show()
                    return
                }
                params[RouterParams.LoginView.PHONE_NUM] = phone
                ActivityRouter.get().startActivity(this, RouterUrl.ACTIVITY_LOGIN_PASSWORD, params, false, 0)
            }
            registerBtn -> {
                //更改验证码登录还是密码登录
                setLoginType(if (loginType == 0) 1 else 0)
            }
            loginSubmitView -> {
                phone = nameEt.text.toString().replace(" ", "")
                if (loginType == 0) {
                    if (codeBtn.visibility == View.VISIBLE) {
                        val code = passwordEt.text.toString()
                        if (TextUtils.isEmpty(code) || !TextUtils.isDigitsOnly(code)) {
                            Toast.makeText(this, "验证码需纯数字", Toast.LENGTH_SHORT).show()
                            return
                        }
                        NumberCodeManager.getInstance().submitVerificationCode(this, "86", phone, code.toInt())
                    } else {
                        getNumberCode()
                    }
                } else {
                    //密码登录
                    val password = passwordEt.text.toString().replace(" ", "")
                    loading()
                    presenter.loginWithPassword(phone, password)
                }
            }
        }
    }

    private fun loading() {
        lastSubmitText = loginSubmitView.text.toString()
        submitBtnStatus = loginSubmitView.isEnabled
        qqLogin.isEnabled = false
        weiboLogin.isEnabled = false
        wxLogin.isEnabled = false

        loginSubmitView.isEnabled = false
        loginSubmitView.text = "登录中..."
    }

    private fun resetSubmitButton() {
        loginSubmitView.isEnabled = submitBtnStatus
        loginSubmitView.text = lastSubmitText

        qqLogin.isEnabled = true
        weiboLogin.isEnabled = true
        wxLogin.isEnabled = true
    }

    fun getNumberCode() {
        //获取验证码
        phone = nameEt.text.toString().replace(" ", "")
        NumberCodeManager.getInstance().getVerificationCode(this, "86", phone)
    }

    override fun onStart() {
        super.onStart()
        NumberCodeManager.getInstance().register(this, codeBtn, object : NumberCodeManager.SimpleSubmitCallback() {
            override fun onSubmitPass() {
                //验证通过
                codeBtn.isEnabled = true
                val code = NumberCodeManager.getInstance().code
                //走注册接口(撞库，手机号已经存在，则登录，未存在则注册)
                presenter.loginWithCode(phone, "$code")
            }

            override fun onSendComplete() {
                findViewById<View>(R.id.main_login_password_input_1).visibility = View.VISIBLE
                codeBtn.visibility = View.VISIBLE
                passwordEt.post {
                    passwordEt.requestFocus()
                }
                loginSubmitView.text = "下一步"
            }

            override fun onError(e: Throwable) {
                val message = if (e is ReturnJsonCodeException || e is NumberErrorException) {
                    e.message
                } else {
                    "请稍后重试"
                }
                SmToast.showToastRight(this@LoginMainActivity, message)
            }

        })
    }

    override fun onStop() {
        super.onStop()
        NumberCodeManager.getInstance().unRegister(this, codeBtn)
    }

    /**
     * 登录结果回调
     */
    override fun onLoginResult(userSystem: UserSystem?, error: SmError?) {
        if (error != null) {
            checkLoginButtonStatus()
            resetSubmitButton()
            object : SmDefaultDialog.Builder(this) {}
                    .setTitle("登录失败")
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            //登录成功
            UserDataManager.getInstance().userSystem = userSystem
            finish()
        }
    }

    /**
     * 设置当前按钮状态是否可以点击
     */
    private fun checkLoginButtonStatus() {
        //账号登录
        loginSubmitView.isEnabled = (nameEt.text?.toString()?.replace(" ", "")?.length ?: 0) > 6
    }

    /**
     * 若没有该用户，则去注册
     */
    override fun onCheckUserResult(us: UserSystem?, error: SmError?) {
        checkLoginButtonStatus()
        if (error != null) {
            when {
                error.customCode == 101 -> {
                    resetSubmitButton()
                    //用户不存在，去注册
                    val params = mutableMapOf<String, String>()
                    params[RouterParams.LoginView.PASSWORD_TYPE] = RouterParams.LoginView.PASSWORD_TYPE_REGISTER
                    params[RouterParams.LoginView.PHONE_NUM] = phone
                    ActivityRouter.get().startActivity(this, RouterUrl.ACTIVITY_LOGIN_PASSWORD, params, false, 1100)
                    passwordEt.setText("")
                }
                error.customCode == 102 -> {
                    //密码错误（其实是用验证码去撞库）
                    //验证码已经通过，走无密码登录
                    presenter.loginNoPwd(phone)
                }
                else -> {
                    resetSubmitButton()
                    object : SmDefaultDialog.Builder(this) {}
                            .setTitle("登录失败")
                            .setMessage(error.getMessage(this))
                            .cancelOutside(false)
                            .setNegativeButton("知道了", null)
                            .showPositiveButton(false)
                            .show()
                }
            }
        } else {
            //登录成功？理论上不会走这里
            finish()
        }
    }

    /**
     * 无密码登录结果返回
     */
    override fun onNoPwdLoginResult(userSystem: UserSystem?, error: SmError?) {
        if (error != null) {
            resetSubmitButton()
            object : SmDefaultDialog.Builder(this) {}
                    .setTitle("登录失败")
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            //登录成功返回
            loginSuccessBack()
        }
    }

    /**
     *
     * 第三方登录返回
     */
    override fun onThirdLoginResult(userSystem: UserSystem?, error: SmError?) {
        if (userSystem == null && error == null) {
            //取消登录
            resetSubmitButton()
            return
        }
        if (error != null) {
            resetSubmitButton()
            object : SmDefaultDialog.Builder(this) {}
                    .setTitle("登录失败")
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            //登录成功返回
            if (TextUtils.isEmpty(userSystem!!.phone)) {
                //没有绑定手机号
                val intent = ActivityRouter.get().getIntent(this, RouterUrl.ACTIVITY_PHONE_BIND)
                startActivityForResult(intent, 1000)
                // SmToast.show(this, "需要执行绑定手机号码")
            } else {
                loginSuccessBack()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1100 && loginUser != null) {
            //去注册回来
            loginSuccessBack()
        } else if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                //绑定手机号成功
                loginSuccessBack()
            } else {
                UserDataManager.getInstance().logout()
                resetSubmitButton()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(0)
    }

    fun loginSuccessBack() {
        UserDataManager.getInstance().startPushService(this)
        setResult(Activity.RESULT_OK)
        if (ActivityManagerUtil.getScreenManager().isMainActivityOpened) {
            finish()
        } else {
            ActivityRouter.get().startActivity(this, RouterUrl.ACTIVITY_MAIN)
            mHandler.postDelayed({
                finish()
            }, 1000)
        }
    }


}