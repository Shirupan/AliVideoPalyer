package com.xx.module.me.model;

import android.text.TextUtils;

import com.xx.module.common.model.cache.DataProviderManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;

/**
 * @author someone
 * @date 2019-05-31
 */
public class MeCacheProvider extends DataProviderManager.BaseCacheProvider<IMeCache> {
    @Override
    protected Class<IMeCache> getCacheClass() {
        return IMeCache.class;
    }

    @Nullable
    public Observable<String> getMeTools(@Nullable Observable<String> data) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().getMeToolsCache(source, new DynamicKey("1111"), new EvictDynamicKey(save));
            }
        });
    }

    public Observable<String> loadHistory(@Nullable Observable<String> data, @NotNull final String token) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadHistory(source, new DynamicKey(TextUtils.isEmpty(token) ? "default" : token), new EvictDynamicKey(save));
            }
        });
    }

    public Observable<String> loadPraiseHistory(Observable<String> data, final String token, final int type) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadPraiseHistory(source,
                                new DynamicKeyGroup(TextUtils.isEmpty(token) ? "default" : token, type + ""),
                                new EvictDynamicKeyGroup(save));
            }
        });
    }


    public Observable<String> loadCollectionHistory(@Nullable Observable<String> data,
                                                    @NotNull final String token, final int type) {
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? source :
                        getCacheInterface().loadCollectionHistory(source,
                                new DynamicKeyGroup(TextUtils.isEmpty(token) ? "default" : token, type + ""),
                                new EvictDynamicKeyGroup(save));
            }
        });
    }
}
