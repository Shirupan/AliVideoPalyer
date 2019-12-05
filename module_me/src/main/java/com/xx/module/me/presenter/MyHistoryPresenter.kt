package com.xx.module.me.presenter

import com.google.gson.reflect.TypeToken
import com.mrkj.lib.net.retrofit.RetrofitManager
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.ResultListUICallback
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BaseListPresenter
import com.xx.module.me.MeModule
import com.xx.module.me.model.MeCacheProvider
import com.xx.module.me.model.entity.MyHistoryJson
import com.xx.module.me.view.contract.IMyHistoryView

/**
 *@author someone
 *@date 2019-06-12
 */
class MyHistoryPresenter : BaseListPresenter<IMyHistoryView>() {
    fun loadCache(token: String) {
        DataProviderManager.get(MeCacheProvider::class.java)
                .loadHistory(null, token)
                .compose(RetrofitManager.rxTransformer<List<MyHistoryJson>>(null,
                        object : TypeToken<List<MyHistoryJson>>() {}.type))
                .subscribe(object : ResultUICallback<List<MyHistoryJson>>() {
                    override fun onNext(t: List<MyHistoryJson>) {
                        super.onNext(t)
                        view?.onLoadHistoryCacheResult(t)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onLoadHistoryCacheResult(null)
                    }
                })

    }

    fun loadHistory(token: String, page: Int) {
        ModuleManager.of(MeModule::class.java)
                .modelClient
                .loadHistory(token, page, object : ResultListUICallback<List<MyHistoryJson>>(view) {
                    override fun onNext(t: List<MyHistoryJson>) {
                        super.onNext(t)
                        view?.onLoadHistoryResult(t, page)
                    }
                }.unShowDefaultMessage())
    }
}