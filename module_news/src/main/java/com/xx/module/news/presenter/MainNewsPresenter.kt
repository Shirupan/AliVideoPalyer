package com.xx.module.news.presenter

import com.google.gson.reflect.TypeToken
import com.mrkj.lib.net.retrofit.RetrofitManager
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.ResultListUICallback
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BaseListPresenter
import com.xx.module.news.NewsModule
import com.xx.module.news.model.NewsCacheProvider
import com.xx.lib.db.entity.NewsJson
import com.xx.module.news.view.constract.IMainNewsView

/**
 *@author someone
 *@date 2019-06-12
 */
class MainNewsPresenter : BaseListPresenter<IMainNewsView>() {
    fun loadCache(token: String, page: Int) {
        DataProviderManager.get(NewsCacheProvider::class.java)
                .loadNewsList(null, token, page)
                .compose(RetrofitManager.rxTransformer<List<NewsJson>>(null, object : TypeToken<List<NewsJson>>() {}.type))
                .subscribe(object : ResultUICallback<List<NewsJson>>() {
                    override fun onNext(t: List<NewsJson>) {
                        super.onNext(t)
                        view?.loadNewsCacheList(t)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.loadNewsCacheList(null)
                    }
                })
    }

    fun loadNewsList(token: String, page: Int) {
        ModuleManager.of(NewsModule::class.java)
                .modelClient
                .loadNewsList(token, page, object : ResultListUICallback<List<NewsJson>>(view) {
                    override fun onNext(t: List<NewsJson>) {
                        super.onNext(t)
                        view?.onLoadNewsListResult(t, page)
                    }
                }.unShowDefaultMessage())
    }
}