package com.xx.module.video.model;

import android.text.TextUtils;

import com.xx.lib.db.exception.SmCacheException;
import com.xx.module.common.model.cache.DataProviderManager;

import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-05-30
 */
public class VideoCacheProvider extends DataProviderManager.BaseCacheProvider<IVideoCache> {
    @Override
    protected Class<IVideoCache> getCacheClass() {
        return IVideoCache.class;
    }

    @Nullable
    public Observable<String> getMainVideoList(@Nullable Observable<String> net, final int page, final int type) {
        return checkNull(net, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? Observable.<String>error(new SmCacheException(DataProviderManager.NO_NET)) :
                        getCacheInterface().getMainVideoList(source, new DynamicKey(page+"$" + type), new EvictDynamicKey(save));
            }
        });
    }

    public Observable<String> getMainVideoTabs(Observable<String> data, String token) {
        final String key = TextUtils.isEmpty(token) ? "default" : token;
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? Observable.<String>error(new SmCacheException(DataProviderManager.NO_NET)) :
                        getCacheInterface().getMainVideoTabs(source, new DynamicKey(key), new EvictDynamicKey(save));
            }
        });
    }

    public Observable<String> getMyTabList(Observable<String> data, String token) {
        final String key = TextUtils.isEmpty(token) ? "default" : token;
        return checkNull(data, new DataProviderManager.IEvictDynamicHandler() {
            @Override
            public Observable<String> cache(Observable<String> source, boolean save) {
                return getCacheInterface() == null ? Observable.<String>error(new SmCacheException(DataProviderManager.NO_NET)) :
                        getCacheInterface().getMyTabList(source, new DynamicKey(key), new EvictDynamicKey(save));
            }
        });
    }
}
