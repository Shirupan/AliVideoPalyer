package com.xx.module.me.model;

import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.me.model.entity.MeMainInfo;
import com.xx.module.me.model.entity.MyHistoryJson;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-05-31
 */
public interface IMeModel {

    void getMeTools(boolean cache, @NotNull SimpleSubscriber<MeMainInfo> uiCallback);

    /**
     * 头像上传
     *
     * @param token
     * @param url      本地文件地址
     * @param callback
     */
    void postUserSavor(String token, String url, SimpleSubscriber<UserSystem> callback);

    void postUserNickName(String token, String nickname, SimpleSubscriber<UserSystem> callback);

    void loadHistory(@NotNull String token, int page, @NotNull SimpleSubscriber<List<MyHistoryJson>> uiCallback);

    void loadPraiseHistory(String token, int type, int page, SimpleSubscriber<List<MyHistoryJson>> uiCallback);


    void loadCollectionHistory(String token, int type, int page, SimpleSubscriber<List<MyHistoryJson>> uiCallback);

    void delCollection(String token, String cids, SimpleSubscriber<String> uiCallback);


    interface Service {

        @GET("")
        Observable<ReturnBeanJson> getMeTools(@QueryMap Map<String, String> map);

        @POST("")
        Observable<ReturnBeanJson> postUserSavor(@Body RequestBody body);

        @POST("")
        Observable<ReturnBeanJson> postUserNickName(@Body Map<String, String> body);

        @GET("")
        Observable<ReturnBeanJson> loadHistory(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> loadPraiseHistory(@QueryMap Map<String, String> map);


        @GET("")
        Observable<ReturnBeanJson> loadCollectionHistory(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> delCollection(@QueryMap Map<String, String> map);
    }
}
