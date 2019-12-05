package com.xx.module.common.view.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.design.widget.TextInputEditText
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mrkj.base.bindView
import com.mrkj.module.sms.NumberCodeManager
import com.xx.module.common.R
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.presenter.PhoneLoginPresenter
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IViewManager
import com.xx.module.common.view.dialog.SmDefaultDialog
import com.xx.module.common.view.simple.SimpleOnTextWatch

/**
 * 绑定手机号
 *@author someone
 *@date 2019-05-29
 */
@Path(RouterUrl.ACTIVITY_PHONE_BIND)
@Presenter(PhoneLoginPresenter::class)
class PhoneLoginActivity : BaseActivity<PhoneLoginPresenter>(), IViewManager.IPhoneLoginView, View.OnClickListener {


    private val phoneEt by bindView<TextInputEditText>(R.id.login_phone_name)
    private val codeEt by bindView<TextInputEditText>(R.id.login_phone_code_et)

    private val submitView by bindView<TextView>(R.id.login_phone_submit)
    private val codeView by bindView<TextView>(R.id.login_phone_code)
    private val cancelView by bindView<ImageView>(R.id.login_phone_close)
    private val privacyView by bindView<TextView>(R.id.login_phone_privacy)

    private var phone = ""


    override fun beforeSetContentView() {
        //  setTheme(R.style.SmTheme_Night)
    }

    override fun getLayoutId(): Int = R.layout.activty_login_phone

    override fun initViewsAndEvents() {
        setStatusBar(true, true)

        initEvent()

        setPrivacyText()

        if (loginUser == null) {
            Toast.makeText(this, "当前账号空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun initEvent() {
        cancelView.setOnClickListener(this)
        codeView.setOnClickListener(this)
        submitView.setOnClickListener(this)
        phoneEt.addTextChangedListener(object : SimpleOnTextWatch() {
            override fun afterTextChanged(s: Editable?) {
                codeView.isEnabled = (s?.toString()?.replace(" ", "")?.length ?: 0) > 6
            }
        })
        codeEt.addTextChangedListener(object : SimpleOnTextWatch() {
            override fun afterTextChanged(s: Editable) {
                submitView.isEnabled = !TextUtils.isEmpty(s.toString())
            }
        })
    }


    override fun onClick(v: View?) {
        when (v) {
            cancelView -> onBackPressed()
            codeView -> {
                //获取验证码
                phone = phoneEt.text.toString().replace(" ", "")
                NumberCodeManager.getInstance().getVerificationCode(this, "86", phone)
            }
            submitView -> {
                //验证验证码
                val code = codeEt.text.toString()
                if (!TextUtils.isDigitsOnly(code)) {
                    Toast.makeText(this, "验证码需纯数字", Toast.LENGTH_SHORT).show()
                    return
                }
                phone = phoneEt.text.toString().replace(" ", "")
                NumberCodeManager.getInstance().submitVerificationCode(this, "86", phone, code.toInt())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        NumberCodeManager.getInstance().register(this, codeView, object : NumberCodeManager.SimpleSubmitCallback() {
            override fun onSubmitPass() {
                //验证通过
                presenter.bindPhone(phone, loginUser?.token ?: "")
                // ActivityRouter.get().startActivity(this@PhoneLoginActivity, "")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        NumberCodeManager.getInstance().unRegister(this, codeView)
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
                    newSp = LoginMainActivity.SubClickSpan(this@PhoneLoginActivity, 0)
                }
                it.url == LoginMainActivity.SubClickSpan.TO_PRIVACY -> {
                    newSp = LoginMainActivity.SubClickSpan(this@PhoneLoginActivity, 1)
                }
                else -> newSp = null
            }
            newSp?.let { sub ->
                stylesBuilder.setSpan(sub, sp.getSpanStart(it), sp.getSpanEnd(it), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        privacyView.text = stylesBuilder
        privacyView.movementMethod=LinkMovementMethod.getInstance()
    }

    /**
     * 绑定手机号回调
     */
    override fun onBindPhoneResult(success: Boolean, error: SmError?) {
        if (error != null) {
            object : SmDefaultDialog.Builder(this) {}
                    .setTitle("手机号绑定失败")
                    .setMessage(error.getMessage(this))
                    .cancelOutside(false)
                    .setNegativeButton("知道了", null)
                    .showPositiveButton(false)
                    .show()
        } else {
            startActivityForResult(Intent(this, RegisterSuccessActivity::class.java), 1100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1100) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


}