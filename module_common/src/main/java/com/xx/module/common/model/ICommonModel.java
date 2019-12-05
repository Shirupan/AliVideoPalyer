package com.xx.module.common.model;

import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.common.model.net.LoginPost;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-05-28
 */
public interface ICommonModel {

    void loginWithPassword(String phone, String password, SimpleSubscriber<UserSystem> callback);

    void loginWithCode(String phone, String code, ResultUICallback<UserSystem> unShowDefaultMessage);

    void register(@NotNull String phone, @NotNull String newPassword, @NotNull SimpleSubscriber<UserSystem> uiCallback);

    void changePassword(@NotNull String phone, @NotNull String newPassword, @NotNull SimpleSubscriber<UserSystem> uiCallback);

    void getLoginUser(String token, SimpleSubscriber<UserSystem> callback);

    void loginNoPwd(String phone, ResultUICallback<UserSystem> callback);

    void addPraise(int vid, int type, String token, SimpleSubscriber<String> callback);

    void delPraise(int vid, int type, String token, SimpleSubscriber<String> callback);

    void delCollection(int vid, int type, String token, SimpleSubscriber<String> callback);

    void addCollection(int vid, int type, String token, SimpleSubscriber<String> uiCallback);

    void loginByThird(String openId, int registertype, String imgeurl, String nickName, SimpleSubscriber<UserSystem> callback);

    void bindPhone(@NotNull String phone, @NotNull String token, SimpleSubscriber<String> callback);

    void pushToken(String token, String cid, SimpleSubscriber<String> uiCallback);

    void fellbackShare(String token, Integer kind, int qid, int shareType, SimpleSubscriber<ReturnBeanJson> callback);


    interface Service {
        @POST("/")
        Observable<ReturnBeanJson> loginWithPassword(@Body LoginPost post);

        @POST("/")
        Observable<ReturnBeanJson> register(@Body Map<String, String> post);

        @POST("")
        Observable<ReturnBeanJson> changePassword(@Body LoginPost post);

        @GET("")
        Observable<ReturnBeanJson> getLoginUser(@QueryMap Map<String, String> map);

        @POST("")
        Observable<ReturnBeanJson> loginNoPwd(@Body LoginPost map);

        @GET(")
        Observable<ReturnBeanJson> delPraise(@QueryMap Map<String, String> map);

        @GET("/")
        Observable<ReturnBeanJson> addPraise(@QueryMap Map<String, String> map);

        @GET("/")
        Observable<ReturnBeanJson> delCollection(@QueryMap Map<String, String> map);

        @GET("/")
        Observable<ReturnBeanJson> addCollection(@QueryMap Map<String, String> map);

        @POST("/")
        Observable<ReturnBeanJson> loginByThird(@Body Map<String, String> parmas);

        @POST("/")
        Observable<ReturnBeanJson> bindPhone(@Body Map<String, String> parmas);

        @GET("/")
        Observable<ReturnBeanJson> pushToken(@QueryMap Map<String, String> parmas);

        @GET("/")
        Observable<ReturnBeanJson> fellbackShare(@QueryMap Map<String, String> map);
    }
}
