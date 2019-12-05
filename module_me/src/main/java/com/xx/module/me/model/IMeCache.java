package com.xx.module.me.model;

import com.xx.module.common.model.cache.Cache;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;

/**
 * @author someone
 * @date 2019-05-31
 */
public interface IMeCache extends Cache {
    Observable<String> getMeToolsCache(io.reactivex.Observable<String> data, DynamicKey ky, EvictDynamicKey dynamicKey);

    Observable<String> loadHistory(Observable<String> source, DynamicKey dynamicKey, EvictDynamicKey dynamicKey1);

    Observable<String> loadPraiseHistory(Observable<String> source, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);

    Observable<String> loadCollectionHistory(Observable<String> source, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);
}
