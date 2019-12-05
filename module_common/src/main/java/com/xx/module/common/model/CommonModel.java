package com.xx.module.common.model;

import android.support.v4.util.ArrayMap;

import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.common.model.net.LoginPost;
import com.xx.module.common.model.net.SmHttpClient;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author someone
 * @date 2019-05-28
 */
public class CommonModel implements ICommonModel {
    @Override
    public void loginWithPassword(String phone, String password, SimpleSubscriber<UserSystem> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        LoginPost post = new LoginPost();
        post.setCommon(GsonSingleton.getInstance().toJson(map));

        RetrofitManager.createApi(ICommonModel.Service.class)
                .loginWithPassword(post)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), UserSystem.class))
                .subscribe(callback);
    }

    @Override
    public void loginByThird(String openId, int registertype, String imgeurl, String nickName, SimpleSubscriber<UserSystem> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        ArrayMap<String, String> parmas = new ArrayMap<>();
        parmas.put("common", GsonSingleton.getInstance().toJson(map));

        RetrofitManager.createApi(ICommonModel.Service.class)
                .loginByThird(parmas)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), UserSystem.class))
                .subscribe(callback);
    }

    @Override
    public void bindPhone(@NotNull String phone, @NotNull String token, SimpleSubscriber<String> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        ArrayMap<String, String> parmas = new ArrayMap<>();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .bindPhone(parmas)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), String.class))
                .subscribe(callback);
    }

    @Override
    public void pushToken(String token, String cid, SimpleSubscriber<String> uiCallback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        ArrayMap<String, String> parmas = new ArrayMap<>();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .pushToken(parmas)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(uiCallback.getBindLifeObject(), String.class))
                .subscribe(uiCallback);
    }

    @Override
    public void fellbackShare(String token, Integer kind, int qid, int shareType, SimpleSubscriber<ReturnBeanJson> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();


        RetrofitManager.createApi(ICommonModel.Service.class)
                .fellbackShare(map)
                .compose(RetrofitManager.<ReturnBeanJson>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), ReturnBeanJson.class))
                .subscribe(callback);
    }

    @Override
    public void loginWithCode(String phone, String code, ResultUICallback<UserSystem> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        LoginPost post = new LoginPost();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .loginWithPassword(post)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), UserSystem.class))
                .subscribe(callback);
    }

    @Override
    public void register(@NotNull String phone, @NotNull String newPassword, @NotNull SimpleSubscriber<UserSystem> uiCallback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        ArrayMap<String, String> params = new ArrayMap<>();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .register(params)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(uiCallback.getBindLifeObject(), UserSystem.class))
                .subscribe(uiCallback);
    }

    @Override
    public void changePassword(@NotNull String phone, @NotNull String newPassword, @NotNull SimpleSubscriber<UserSystem> uiCallback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        LoginPost post = new LoginPost();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .changePassword(post)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(uiCallback.getBindLifeObject(), UserSystem.class))
                .subscribe(uiCallback);
    }

    @Override
    public void getLoginUser(String token, SimpleSubscriber<UserSystem> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        map.put("token", token);
        RetrofitManager.createApi(ICommonModel.Service.class)
                .getLoginUser(map)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), UserSystem.class))
                .subscribe(callback);
    }

    @Override
    public void loginNoPwd(String phone, ResultUICallback<UserSystem> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();
        LoginPost post = new LoginPost();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .loginNoPwd(post)
                .compose(RetrofitManager.<UserSystem>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), UserSystem.class))
                .subscribe(callback);
    }

    @Override
    public void addPraise(int vid, int type, String token, SimpleSubscriber<String> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .addPraise(map)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), String.class))
                .subscribe(callback);
    }

    @Override
    public void delPraise(int vid, int type, String token, SimpleSubscriber<String> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .delPraise(map)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), String.class))
                .subscribe(callback);
    }

    @Override
    public void delCollection(int vid, int type, String token, SimpleSubscriber<String> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .delCollection(map)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), String.class))
                .subscribe(callback);
    }

    @Override
    public void addCollection(int vid, int type, String token, SimpleSubscriber<String> callback) {
        Map<String, String> map = SmHttpClient.getInitParamsMap();

        RetrofitManager.createApi(ICommonModel.Service.class)
                .addCollection(map)
                .compose(RetrofitManager.<String>rxReturnBeanJsonTransformer(callback.getBindLifeObject(), String.class))
                .subscribe(callback);
    }


}
