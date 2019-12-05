package com.xx.module.common.view.login

import android.app.Activity
import android.view.View
import com.xx.module.common.R
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IBaseView

/**
 *@author someone
 *@date 2019-05-30
 */
class RegisterSuccessActivity : BaseActivity<BasePresenter<IBaseView>>() {

    override fun getLayoutId(): Int = R.layout.activity_register_success

    override fun initViewsAndEvents() {
        findViewById<View>(R.id.register_success_go).setOnClickListener {
            //进入首页
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed()
    }
}