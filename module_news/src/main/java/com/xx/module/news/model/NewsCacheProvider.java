package com.xx.module.news.model;

import android.text.TextUtils;

import com.xx.module.common.model.cache.DataProviderManager;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;

/**
 * @author someone
 * @date 2019-06-12
 */
public class NewsCacheProvider extends DataProviderManager.BaseCacheProvider<INewsCache> {
    @Override
    protected Class<INewsCache> getCacheClass() {
        return INewsCache.class;
    }

    public Observable<String> loadNewsList(final Observable<String> data, @NotNull final String token, final int page) {

        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadNewsList(source, new DynamicKeyGroup(page + "", TextUtils.isEmpty(token) ? "111" : token), new EvictDynamicKeyGroup(save));
            }
        });
    }

    public Observable<String> loadNewsDetails(Observable<String> data, final String token) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadNewsDetails(source, new DynamicKey(TextUtils.isEmpty(token) ? "111" : token), new EvictDynamicKey(save));
            }
        });
    }
}
