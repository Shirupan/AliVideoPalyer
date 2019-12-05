package com.xx.module.video.model

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.mrkj.lib.net.retrofit.RetrofitManager
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.MainVideo
import com.xx.lib.db.entity.ReturnBeanJson
import com.xx.lib.db.exception.SmCacheException
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.SimpleSubscriber
import com.xx.module.common.model.net.SmHttpClient
import com.xx.module.video.model.entity.MainVideoTab
import com.xx.module.video.model.entity.VideoDetail
import com.xx.module.video.model.entity.VideoTabManagerJson
import io.reactivex.Observable

/**
 *@author someone
 *@date 2019-05-30
 */
class VideoModel : IVideoModel {


    override fun geVideoDetail(token: String, viod: Int, uiCallback: SimpleSubscriber<VideoDetail>) {
        val map = SmHttpClient.getInitParamsMap()
        RetrofitManager.createApi(IVideoModel.Service::class.java)
                .getVideoDetail(map)
                .compose(RetrofitManager.rxReturnBeanJsonTransformer<VideoDetail>(uiCallback.bindLifeObject, VideoDetail::class.java))
                .subscribe(uiCallback)
    }

    override fun getMainVideoTabs(token: String?, callback: SimpleSubscriber<List<MainVideoTab>>) {
        val map = SmHttpClient.getInitParamsMap()

        object : DataProviderManager.Builder<VideoCacheProvider>(VideoCacheProvider::class.java) {}
                .data(RetrofitManager.createApi(IVideoModel.Service::class.java).getMainVideoTabs(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { provider, net -> provider.getMainVideoTabs(net, token) }
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MainVideoTab>>(callback.bindLifeObject,
                        object : TypeToken<List<MainVideoTab>>() {}.type))
                .subscribe(callback)
    }

    override fun getUserTabs(token: String?, callback: SimpleSubscriber<VideoTabManagerJson>) {
        val map = SmHttpClient.getInitParamsMap()

        object : DataProviderManager.Builder<VideoCacheProvider>(VideoCacheProvider::class.java) {}
                .data(RetrofitManager.createApi(IVideoModel.Service::class.java).getUserTabs(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { provider, net -> provider.getMyTabList(net, token) }
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<VideoTabManagerJson>(callback.bindLifeObject,
                        object : TypeToken<VideoTabManagerJson>() {}.type))
                .subscribe(callback)
    }

    override fun sortVideoTab(token: String?, tid: Int, sort: Int, callback: SimpleSubscriber<ReturnBeanJson<*>>) {
        val map = SmHttpClient.getInitParamsMap()
        RetrofitManager.createApi(IVideoModel.Service::class.java)
                .sortVideoTab(map)
                .compose(RetrofitManager.rxReturnBeanJsonTransformer(callback.bindLifeObject))
                .subscribe(callback)
    }

    override fun delVideoTab(token: String?, hid: Int, callback: SimpleSubscriber<ReturnBeanJson<*>>) {
        val map = SmHttpClient.getInitParamsMap()

        RetrofitManager.createApi(IVideoModel.Service::class.java)
                .delVideoTab(map)
                .compose(RetrofitManager.rxReturnBeanJsonTransformer(callback.bindLifeObject))
                .subscribe(callback)
    }

    override fun getMainVideoList(page: Int, type: Int, token: String?, uiCallback: SimpleSubscriber<List<MainVideo>>) {
        val map = SmHttpClient.getInitParamsMap()

        DataProviderManager.Builder<VideoCacheProvider>(VideoCacheProvider::class.java)
                .cache { provider, net -> provider.getMainVideoList(net, page, type) }
                .data(RetrofitManager.createApi(IVideoModel.Service::class.java).getMainListVideo(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MainVideo>>(uiCallback.bindLifeObject,
                        object : TypeToken<List<MainVideo>>() {}.type))
                .subscribe(uiCallback)
    }

    override fun getMainVideoListCache(mType: Int, page: Int, uiCallback: SimpleSubscriber<List<MainVideo>>) {
        DataProviderManager.Builder<VideoCacheProvider>(VideoCacheProvider::class.java)
                .cache { provider, net -> provider.getMainVideoList(net, page, mType) }
                .data(Observable.error(SmCacheException("")))
                .useCache(true)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MainVideo>>(uiCallback.bindLifeObject,
                        object : TypeToken<List<MainVideo>>
                        () {}.type))
                .subscribe(uiCallback)
    }
}