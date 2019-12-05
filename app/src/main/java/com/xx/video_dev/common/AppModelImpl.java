package com.xx.video_dev.common;

import android.support.annotation.Nullable;

import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.entity.SplashAdvert;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.net.SmHttpClient;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * @author someone
 * @date 2019-06-14
 */
public class AppModelImpl implements IAppModel {
    @Override
    public void loadSplashAD(ResultUICallback<SplashAdvert> callback) {
        Map<String, String> params = SmHttpClient.getInitParamsMap();
        new DataProviderManager.Builder<>(AppCacheProvider.class)
                .useCache(false)
                .data(RetrofitManager.createApi(IAppModel.Service.class)
                        .loadSplashAD(params)
                        .map(new Function<ReturnBeanJson, String>() {
                            @Override
                            public String apply(ReturnBeanJson returnBeanJson) throws Exception {
                                return GsonSingleton.getInstance().toJson(returnBeanJson);
                            }
                        }))
                .cache(new DataProviderManager.ICacheObservable<AppCacheProvider>() {
                    @Override
                    public Observable<String> onNetObservable(AppCacheProvider provider, @Nullable Observable<String> net) {
                        return provider.loadSplashADCache(net);
                    }
                })
                .build()
                .compose(RetrofitManager.<SplashAdvert>rxTransformer(callback.getBindLifeObject(), SplashAdvert.class))
                .subscribe(callback);
    }
}
