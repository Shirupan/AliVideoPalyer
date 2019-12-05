package com.xx.module.video.presenter

import com.xx.lib.db.entity.MainVideo
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.callback.ResultListUICallback
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BaseListPresenter
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.view.constract.IMainShortVideoListView

/**
 *@author someone
 *@date 2019-06-11
 */
class MainShortVideoListViewPresenter : BaseListPresenter<IMainShortVideoListView>() {
    fun getCache(mType: Int, page: Int) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .getMainVideoListCache(mType, page, object : ResultUICallback<List<MainVideo>>() {
                    override fun onNext(t: List<MainVideo>) {
                        super.onNext(t)
                        view?.getListCache(t)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.getListCache(null)
                    }
                })
    }

    fun loadMainVideoList(page: Int, type: Int, token: String?) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .getMainVideoList(page, type, token, object : ResultListUICallback<List<MainVideo>>(view) {
                    override fun onNext(t: List<MainVideo>) {
                        super.onNext(t)
                        view?.onMainListResult(t, page)
                    }
                }.unShowDefaultMessage())
    }
}