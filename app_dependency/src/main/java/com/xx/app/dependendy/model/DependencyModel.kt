package com.xx.app.dependendy.model

import com.mrkj.lib.net.retrofit.RetrofitManager
import com.xx.base.GsonSingleton
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.model.net.SmHttpClient

/**
 * @author someone
 * @date 2019-05-30
 */
class DependencyModel : IDependencyModel {

    override fun getAppStatus(callback: ResultUICallback<String>) {
        val map = SmHttpClient.getInitParamsMap()
        DataProviderManager.Builder(DependencyCacheProvider::class.java)
                .useCache(false)
                .cache { provider, net ->
                    return@cache provider.getAppStatus(net)
                }
                .data(RetrofitManager.createApi(IDependencyModel.Service::class.java)
                        .getAppStatus(map)
                        .map { return@map GsonSingleton.getInstance().toJson(it) })
                .build()
                .compose(RetrofitManager.rxTransformer<String>(null, String::class.java))
                .subscribe(callback)
    }


}
