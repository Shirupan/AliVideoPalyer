package com.xx.module.video.model;

import android.content.Context;

import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.mrkj.lib.common.util.AppUtil;

public class SmMediaCacheManager {
    private static SmMediaCacheManager manager;

    private SmMediaCacheManager() {
    }

    public static SmMediaCacheManager getInstance() {
        if (manager == null) {
            synchronized (SmMediaCacheManager.class) {
                if (manager == null) {
                    manager = new SmMediaCacheManager();
                }
            }
        }
        return manager;
    }

    /**
     * 视频缓存目录
     *
     * @param context
     * @return
     */
    public static String getCachePath(Context context) {
        return AppUtil.getCacheDir(context) + "/videos";
    }

    public static void setupAliyunVideoCachePath(Context context, AliyunVodPlayer player) {
        String path = getCachePath(context);
        //maxDuration 秒。maxSize单位mb
        if (player != null) {
            player.setPlayingCache(true, path, 400, 1024);
            player.disableNativeLog();
        }

    }


}
