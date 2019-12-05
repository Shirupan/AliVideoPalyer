package com.xx.module.common.presenter

import com.xx.lib.db.entity.UserSystem
import com.xx.module.common.CommonModule
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.SmErrorHandler
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.view.contract.IViewManager

/**
 *@author someone
 *@date 2019-05-29
 */
class LoginPasswordPresenter : BasePresenter<IViewManager.IPasswordView>() {


    fun clearEmpty(password: String): String {
        return password.trim { it <= ' ' }
                .replace("<br\\\\s*/?>|<p\\\\s*/?>|[\\\\s\\\\n]", "")
    }

    fun changePassword(phone: String, newPassword: String) {
        ModuleManager.of(CommonModule::class.java)
                .modelClient
                .changePassword(phone, newPassword, object : ResultUICallback<UserSystem>(view) {
                    override fun onNext(t: UserSystem) {
                        super.onNext(t)
                        view?.onChangePasswordResult(t, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onChangePasswordResult(null, SmErrorHandler.catchTheErrorSmError(t))
                    }
                }.unShowDefaultMessage())
    }

    fun register(phone: String, newPassword: String) {
        ModuleManager.of(CommonModule::class.java)
                .modelClient
                .register(phone, newPassword, object : ResultUICallback<UserSystem>(view) {
                    override fun onNext(t: UserSystem) {
                        super.onNext(t)
                        view?.onRegisterResult(t, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onRegisterResult(null, SmErrorHandler.catchTheErrorSmError(t))

                    }
                }.unShowDefaultMessage())
    }

}