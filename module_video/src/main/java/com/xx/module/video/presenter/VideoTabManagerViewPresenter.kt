package com.xx.module.video.presenter

import com.mrkj.lib.common.util.SmLogger
import com.xx.lib.db.entity.ReturnBeanJson
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.SmErrorHandler
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.model.entity.MainVideoTab
import com.xx.module.video.model.entity.VideoTabManagerJson
import com.xx.module.video.view.constract.IVideoTabManagerView

/**
 *@author someone
 *@date 2019-06-04
 */
class VideoTabManagerViewPresenter : BasePresenter<IVideoTabManagerView>() {

    fun loadMyTabs(token: String?) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .getUserTabs(token, object : ResultUICallback<VideoTabManagerJson>(view) {
                    override fun onNext(t: VideoTabManagerJson) {
                        super.onNext(t)
                        view?.onMyTabsResult(t, null)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onMyTabsResult(null, SmErrorHandler.catchTheErrorSmError(t))
                    }
                }.unShowDefaultMessage())
    }

    fun deleteMyTab(token: String?, data: MainVideoTab) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .delVideoTab(token, data.id, object : ResultUICallback<ReturnBeanJson<*>>(view, true, false) {
                    override fun onNext(t: ReturnBeanJson<*>) {
                        super.onNext(t)
                        view?.onDeleteSuccessResult(data)
                    }
                })
    }

    fun addTab(token: String?, data: MainVideoTab) {
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .sortVideoTab(token, data.tid, data.srot, object : ResultUICallback<ReturnBeanJson<*>>(view) {
                    override fun onNext(t: ReturnBeanJson<*>) {
                        super.onNext(t)
                        view?.onAddSuccessResult(data)
                    }
                })
    }

    fun changeTabPosition(token: String?, from: MainVideoTab, to: MainVideoTab, toPosition: Int) {
        SmLogger.d("将tid=${from.tid}（原位置${from.srot}）的tab更换到${toPosition}位置上")
        ModuleManager.of(VideoModuleClient::class.java)
                .modelClient
                .sortVideoTab(token, from.tid, toPosition, object : ResultUICallback<ReturnBeanJson<*>>(view) {
                    override fun onNext(t: ReturnBeanJson<*>) {
                        super.onNext(t)
                        view?.onChangeTabResult(from, to, true)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        view?.onChangeTabResult(from, to, false)
                    }
                })
    }
}