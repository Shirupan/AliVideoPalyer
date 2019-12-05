package com.xx.module.me.presenter

import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.SmErrorHandler
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.me.MeModule
import com.xx.module.me.model.entity.MeMainInfo
import com.xx.module.me.view.contract.IMeView

/**
 *@author someone
 *@date 2019-05-31
 */

class MeViewPresenter : BasePresenter<IMeView>() {
    fun getTools(cache: Boolean) {
        ModuleManager.of(MeModule::class.java)
                .modelClient
                .getMeTools(cache, object : ResultUICallback<MeMainInfo>(view) {
                    override fun onNext(t: MeMainInfo) {
                        super.onNext(t)
                        view?.onToolResult(t, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onToolResult(null, SmErrorHandler.catchTheErrorSmError(t))
                    }
                }.unShowDefaultMessage())
    }
}