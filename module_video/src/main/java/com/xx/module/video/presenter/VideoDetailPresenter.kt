package com.xx.module.video.presenter

import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.model.entity.VideoDetail
import com.xx.module.video.view.constract.IVideoDetailView

/**
 *@author someone
 *@date 2019-06-04
 */

class VideoDetailPresenter : BasePresenter<IVideoDetailView>() {
    fun loadDetail(token: String, viod: Int) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .geVideoDetail(token, viod, object : ResultUICallback<VideoDetail>(view) {
                    override fun onNext(t: VideoDetail) {
                        super.onNext(t)
                        view?.onVideoDetailResult(t)
                    }
                }.unShowDefaultMessage())
    }
}