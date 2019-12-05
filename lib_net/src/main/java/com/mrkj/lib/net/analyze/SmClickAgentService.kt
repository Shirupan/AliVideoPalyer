package com.mrkj.lib.net.analyze

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * @author
 * @date  2018/4/9 0009
 *
 */
interface SmClickAgentService {
    @GET("")
    fun clickEvent(@QueryMap param: Map<String, String>): Observable<String>


}