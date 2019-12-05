package com.xx.video_dev.common;

import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.entity.SplashAdvert;
import com.xx.module.common.model.callback.ResultUICallback;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-06-14
 */
public interface IAppModel {
    void loadSplashAD(ResultUICallback<SplashAdvert> callback);

    public interface Service {
        @GET("")
        Observable<ReturnBeanJson> loadSplashAD(@QueryMap Map<String, String> params);
    }
}
