package com.xx.app.dependendy.model;

import com.xx.module.common.model.cache.DataProviderManager;

import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-05-30
 */
public class DependencyCacheProvider extends DataProviderManager.BaseCacheProvider<IDependencyCache> {
    @Override
    protected Class<IDependencyCache> getCacheClass() {
        return IDependencyCache.class;
    }



    public Observable<String> getAppStatus(@Nullable Observable<String> data) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().getAppStatus(source, new DynamicKey("111"), new EvictDynamicKey(save));
            }
        });
    }
}
