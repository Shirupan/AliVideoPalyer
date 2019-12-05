package com.xx.module.me.model

import com.google.gson.reflect.TypeToken
import com.mrkj.lib.net.retrofit.RetrofitManager
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.UserSystem
import com.xx.module.common.BaseConfig
import com.xx.module.common.imageload.BitmapUtil
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.SimpleSubscriber
import com.xx.module.common.model.net.SmHttpClient
import com.xx.module.me.model.entity.MeMainInfo
import com.xx.module.me.model.entity.MyHistoryJson
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 *@author someone
 *@date 2019-05-31
 */
class MeModelImpl : IMeModel {
    override fun delCollection(token: String?, cids: String?, uiCallback: SimpleSubscriber<String>) {
        val map = SmHttpClient.getInitParamsMap()
        RetrofitManager.createApi(IMeModel.Service::class.java).delCollection(map)
                .compose(RetrofitManager.rxReturnBeanJsonTransformer<String>(uiCallback.bindLifeObject,
                        String::class.java))
                .subscribe(uiCallback)
    }

    override fun loadCollectionHistory(token: String, type: Int, page: Int, uiCallback: SimpleSubscriber<List<MyHistoryJson>>) {
        val map = SmHttpClient.getInitParamsMap()
        DataProviderManager.Builder<MeCacheProvider>(MeCacheProvider::class.java)
                .data(RetrofitManager.createApi(IMeModel.Service::class.java).loadCollectionHistory(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { meCacheProvider: MeCacheProvider, observable: Observable<String>? ->
                    return@cache meCacheProvider.loadCollectionHistory(observable, token, type)
                }
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MyHistoryJson>>(uiCallback.bindLifeObject,
                        object : TypeToken<List<MyHistoryJson>>() {}.type))
                .subscribe(uiCallback)
    }

    override fun loadPraiseHistory(token: String, type: Int, page: Int, uiCallback: SimpleSubscriber<List<MyHistoryJson>>) {
        val map = SmHttpClient.getInitParamsMap()
        DataProviderManager.Builder<MeCacheProvider>(MeCacheProvider::class.java)
                .data(RetrofitManager.createApi(IMeModel.Service::class.java).loadPraiseHistory(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { meCacheProvider: MeCacheProvider, observable: Observable<String>? ->
                    return@cache meCacheProvider.loadPraiseHistory(observable, token, type)
                }
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MyHistoryJson>>(uiCallback.bindLifeObject,
                        object : TypeToken<List<MyHistoryJson>>() {}.type))
                .subscribe(uiCallback)
    }

    override fun loadHistory(token: String, page: Int, uiCallback: SimpleSubscriber<List<MyHistoryJson>>) {
        val map = SmHttpClient.getInitParamsMap()
        DataProviderManager.Builder<MeCacheProvider>(MeCacheProvider::class.java)
                .data(RetrofitManager.createApi(IMeModel.Service::class.java).loadHistory(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { meCacheProvider: MeCacheProvider, observable: Observable<String>? ->
                    return@cache meCacheProvider.loadHistory(observable, token)
                }
                .useCache(false)
                .build()
                .compose(RetrofitManager.rxTransformer<List<MyHistoryJson>>(uiCallback.bindLifeObject,
                        object : TypeToken<List<MyHistoryJson>>() {}.type))
                .subscribe(uiCallback)
    }


    override fun postUserNickName(token: String, nickname: String, callback: SimpleSubscriber<UserSystem>) {
        val map = mutableMapOf<String, String>()
        SmHttpClient.addCommonMap(map)
        RetrofitManager.createApi(IMeModel.Service::class.java)
                .postUserNickName(map)
                .compose(RetrofitManager.rxReturnBeanJsonTransformer<UserSystem>(callback.bindLifeObject, UserSystem::class.java))
                .subscribe(callback)
    }

    override fun postUserSavor(token: String, url: String?, callback: SimpleSubscriber<UserSystem>) {
        Observable.create<String> {
            //压缩图片
            val newUrl = BitmapUtil.compressImage(url, BaseConfig.DEFAULT_SAVOR_SIZE, BaseConfig.DEFAULT_SAVOR_SIZE, 100)
            it.onNext(newUrl)
            it.onComplete()
        }.flatMap { str ->
            val body = MultipartBody.Builder()

            //添加上传的文件
            val file = File(str)
            val fileRQ = RequestBody.create(MediaType.parse("image/*"), file)
            body.addFormDataPart("file", file.name, fileRQ)
            body.addFormDataPart("token", token)
            //公参添加
            SmHttpClient.addFormDataPart(body)
            return@flatMap RetrofitManager.createApi(IMeModel.Service::class.java)
                    .postUserSavor(body.build())
        }.compose(RetrofitManager.rxReturnBeanJsonTransformer<UserSystem>(callback.bindLifeObject, UserSystem::class.java))
                .subscribe(callback)

    }

    override fun getMeTools(cache: Boolean, uiCallback: SimpleSubscriber<MeMainInfo>) {
        val map = SmHttpClient.getInitParamsMap()
        DataProviderManager.Builder<MeCacheProvider>(MeCacheProvider::class.java)
                .data(RetrofitManager.createApi(IMeModel.Service::class.java).getMeTools(map)
                        .map { t -> GsonSingleton.getInstance().toJson(t) })
                .cache { meCacheProvider: MeCacheProvider, observable: Observable<String>? ->
                    return@cache meCacheProvider.getMeTools(observable)
                }
                .useCache(cache)
                .build()
                .compose(RetrofitManager.rxTransformer<MeMainInfo>(uiCallback.bindLifeObject, MeMainInfo::class.java))
                .subscribe(uiCallback)
    }


}