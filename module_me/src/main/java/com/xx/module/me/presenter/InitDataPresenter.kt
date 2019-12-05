package com.xx.module.me.presenter

import com.xx.lib.db.entity.UserSystem
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.SmErrorHandler
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.me.MeModule
import com.xx.module.me.view.contract.IInitDataView


/**
 * @author
 * @date  2017/7/10
 *
 */
class InitDataPresenter : BasePresenter<IInitDataView>() {


    fun editUserNickName(us: UserSystem) {
        ModuleManager.of(MeModule::class.java)
                .modelClient
                .postUserNickName(us.token, us.nickname, object : ResultUICallback<UserSystem>(view, true, false) {
                    override fun onNext(t: UserSystem) {
                        super.onNext(t)
                        view?.onSaveUserResult(t, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onSaveUserResult(null, SmErrorHandler.catchTheErrorSmError(t))

                    }
                }.unShowDefaultMessage())
    }
}