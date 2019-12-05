package com.xx.video_dev.common;

import com.xx.module.common.model.cache.DataProviderManager;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-06-14
 */
public class AppCacheProvider extends DataProviderManager.BaseCacheProvider<IAppCache> {
    @Override
    protected Class<IAppCache> getCacheClass() {
        return IAppCache.class;
    }

    public Observable<String> loadSplashADCache(Observable<String> data) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadSplashADCache(source, new DynamicKey("11"), new EvictDynamicKey(save));
            }
        });
    }
}
