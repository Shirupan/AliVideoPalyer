package com.xx.module.video.model;

import com.xx.module.common.model.cache.Cache;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-05-30
 */

public interface IVideoCache extends Cache {

    Observable<String> getMainVideoList(Observable<String> source, DynamicKey key, EvictDynamicKey evictDynamicKey);

    Observable<String> getMainVideoTabs(Observable<String> source, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

    Observable<String> getMyTabList(Observable<String> source, DynamicKey dynamicKey, EvictDynamicKey dynamicKey1);
}
