package com.xx.module.news.model

import com.xx.module.common.model.cache.Cache
import io.reactivex.Observable
import io.rx_cache2.DynamicKey
import io.rx_cache2.DynamicKeyGroup
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.EvictDynamicKeyGroup

/**
 *@author someone
 *@date 2019-06-12
 */
interface INewsCache : Cache {
    fun loadNewsList(data: Observable<String>, dynamicKey: DynamicKeyGroup, dynamicKey1: EvictDynamicKeyGroup): Observable<String>
    fun loadNewsDetails(source: Observable<String>, dynamicKey: DynamicKey, evictDynamicKey: EvictDynamicKey): Observable<String>
}