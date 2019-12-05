package com.xx.module.news.model;

import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.common.model.net.SmHttpClient;
import com.xx.module.news.model.entity.NewsDetailJson;
import com.xx.lib.db.entity.NewsJson;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-06-12
 */
public class NewsModelImpl implements INewsModel {
    @Override
    public void loadNewsList(@NotNull final String token, final int page, @NotNull SimpleSubscriber<List<NewsJson>> uiCallback) {
        Map<String, String> params = SmHttpClient.getInitParamsMap();

        new DataProviderManager.Builder<>(NewsCacheProvider.class)
                .data(RetrofitManager.createApi(NewsModelImpl.IService.class)
                        .loadNewsList(params)
                        .map(new Function<ReturnBeanJson, String>() {
                            @Override
                            public String apply(ReturnBeanJson returnBeanJson) throws Exception {
                                return GsonSingleton.getInstance().toJson(returnBeanJson);
                            }
                        }))
                .cache(new DataProviderManager.ICacheObservable<NewsCacheProvider>() {
                    @Override
                    public Observable<String> onNetObservable(NewsCacheProvider provider, @Nullable Observable<String> net) {
                        return provider.loadNewsList(net, token, page);
                    }
                })
                .useCache(false)
                .build()
                .compose(RetrofitManager.<List<NewsJson>>rxTransformer(uiCallback.getBindLifeObject(),
                        new TypeToken<List<NewsJson>>() {
                        }.getType()))
                .subscribe(uiCallback);
    }

    @Override
    public void loadNewsDetails(@NotNull final String token, int sid, @NotNull SimpleSubscriber<NewsDetailJson> uiCallback) {
        Map<String, String> params = SmHttpClient.getInitParamsMap();

        new DataProviderManager.Builder<>(NewsCacheProvider.class)
                .data(RetrofitManager.createApi(NewsModelImpl.IService.class)
                        .loadNewsDetails(params)
                        .map(new Function<ReturnBeanJson, String>() {
                            @Override
                            public String apply(ReturnBeanJson returnBeanJson) throws Exception {
                                return GsonSingleton.getInstance().toJson(returnBeanJson);
                            }
                        }))
                .cache(new DataProviderManager.ICacheObservable<NewsCacheProvider>() {
                    @Override
                    public Observable<String> onNetObservable(NewsCacheProvider provider, @Nullable Observable<String> net) {
                        return provider.loadNewsDetails(net, token);
                    }
                })
                .useCache(false)
                .build()
                .compose(RetrofitManager.<NewsDetailJson>rxTransformer(uiCallback.getBindLifeObject(),
                        new TypeToken<NewsDetailJson>() {
                        }.getType()))
                .subscribe(uiCallback);
    }

    public interface IService {
        @GET("")
        Observable<ReturnBeanJson> loadNewsList(@QueryMap Map<String, String> params);

        @GET("")
        Observable<ReturnBeanJson> loadNewsDetails(@QueryMap Map<String, String> params);
    }
}
