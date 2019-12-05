package com.xx.video_dev.common;

import com.xx.module.common.model.cache.Cache;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-06-14
 */
public interface IAppCache extends Cache {
    Observable<String> loadSplashADCache(Observable<String> source, DynamicKey dynamicKey, EvictDynamicKey dynamicKey1);
}
