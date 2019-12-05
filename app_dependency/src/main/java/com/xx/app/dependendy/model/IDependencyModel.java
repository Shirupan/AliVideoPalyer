package com.xx.app.dependendy.model;

import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.module.common.model.callback.ResultUICallback;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-05-30
 */
public interface IDependencyModel {


    void getAppStatus(ResultUICallback<String> callback);


    interface Service {

        @GET("/")
        Observable<ReturnBeanJson> getAppStatus(@QueryMap Map<String, String> map);
    }
}
