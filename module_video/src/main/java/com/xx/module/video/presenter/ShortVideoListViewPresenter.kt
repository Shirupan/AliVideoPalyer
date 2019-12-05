package com.xx.module.video.presenter

import com.xx.lib.db.entity.MainVideo
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.callback.ResultListUICallback
import com.xx.module.common.presenter.BaseListPresenter
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.view.constract.IShortVideoListView


class ShortVideoListViewPresenter : BaseListPresenter<IShortVideoListView>() {
    fun watchVideo(userId: Long, vid: Long?) {

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