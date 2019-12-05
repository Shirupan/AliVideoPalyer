package com.xx.module.common.presenter

import com.xx.module.common.CommonModule
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.SmErrorHandler
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.view.contract.IViewManager

/**
 *@author someone
 *@date 2019-05-29
 */
class PhoneLoginPresenter : BasePresenter<IViewManager.IPhoneLoginView>() {
    fun bindPhone(phone: String, token: String) {
        ModuleManager.of(CommonModule::class.java)
                .modelClient
                .bindPhone(phone, token, object : ResultUICallback<String>(view, true, false) {
                    override fun onNext(t: String) {
                        super.onNext(t)
                        view?.onBindPhoneResult(true, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onBindPhoneResult(false, SmErrorHandler.catchTheErrorSmError(t))
                    }
                }.unShowDefaultMessage())

    }

}