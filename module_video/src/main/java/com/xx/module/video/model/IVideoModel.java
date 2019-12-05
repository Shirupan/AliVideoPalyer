package com.xx.module.video.model;

import com.xx.lib.db.entity.MainVideo;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.video.model.entity.MainVideoTab;
import com.xx.module.video.model.entity.VideoDetail;
import com.xx.module.video.model.entity.VideoTabManagerJson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-05-30
 */
public interface IVideoModel {
    /**
     * 首页缓存数据
     *
     * @param mType
     * @param uiCallback
     */
    void getMainVideoListCache(int mType, int page, @NotNull SimpleSubscriber<List<MainVideo>> uiCallback);

    /**
     * 视频列表
     *
     * @param page
     * @param type
     * @param token
     * @param uiCallback
     */
    void getMainVideoList(int page, int type, @Nullable String token, @NotNull SimpleSubscriber<List<MainVideo>> uiCallback);

    /**
     * 首页全部的类型
     *
     * @param token    token为空，则会返回默认的集合
     * @param callback
     */
    void getMainVideoTabs(String token, SimpleSubscriber<List<MainVideoTab>> callback);

    /**
     * 用户偏好设置的类型
     *
     * @param token
     * @param callback
     */
    void getUserTabs(String token, SimpleSubscriber<VideoTabManagerJson> callback);

    /**
     * 视频类型排序
     *
     * @param token
     * @param tid
     * @param sort
     * @param callback
     */
    void sortVideoTab(String token, int tid, int sort, SimpleSubscriber<ReturnBeanJson> callback);

    /**
     * 删除视频类型
     *
     * @param token
     * @param hid
     * @param callback
     */
    void delVideoTab(String token, int hid, SimpleSubscriber<ReturnBeanJson> callback);

    void geVideoDetail(@NotNull String token, int viod, @NotNull SimpleSubscriber<VideoDetail> uiCallback);

    public interface Service {

        @GET("")
        Observable<ReturnBeanJson> getMainListVideo(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> getMainVideoTabs(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> getUserTabs(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> sortVideoTab(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> delVideoTab(@QueryMap Map<String, String> map);

        @GET("")
        Observable<ReturnBeanJson> getVideoDetail(@QueryMap Map<String, String> map);
    }
}
