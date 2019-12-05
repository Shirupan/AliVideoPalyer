package com.xx.module.news.presenter

import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.news.NewsModule
import com.xx.module.news.model.entity.NewsDetailJson
import com.xx.module.news.view.constract.INewsDetailView

/**
 *@author someone
 *@date 2019-06-12
 */
class NewsDetailPresenter : BasePresenter<INewsDetailView>() {
    fun loadNewsDetails(token: String, sid: Int) {
        ModuleManager.of(NewsModule::class.java)
                .modelClient
                .loadNewsDetails(token, sid, object : ResultUICallback<NewsDetailJson>(view) {
                    override fun onNext(t: NewsDetailJson) {
                        super.onNext(t)
                        view?.onNewsDetailResult(t)
                    }
                }.unShowDefaultMessage())

    }
}