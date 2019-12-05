package com.xx.app.dependendy.model;

import com.xx.module.common.model.cache.Cache;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictDynamicKey;

/**
 * @author someone
 * @date 2019-05-30
 */
public interface IDependencyCache extends Cache {

    Observable<String> getAppStatus(Observable<String> source, DynamicKey dynamicKey, EvictDynamicKey dynamicKey1);
}
