package com.mrkj.lib.net;


import android.content.Context;
import android.net.http.HttpResponseCache;

import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.net.retrofit.RetrofitManager;

import java.io.File;
import java.io.IOException;

/**
 * @author
 * @date 2017/10/25
 */

public class NetLib {
    public static boolean DEBUG;
    private static Context mContext;

    public static String BASE_URL;

    public static void init(Context context, String baseUrl) {
        if (context == null) {
            return;
        }
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
        BASE_URL = baseUrl;
        RetrofitManager.init(baseUrl);
        try {
            File cacheDir = new File(AppUtil.getAppCachePath(getContext()), "svga");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            HttpResponseCache.install(cacheDir, 1024 * 1024 * 128);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Context getContext() {
        if (mContext == null) {
            throw new NullPointerException("has not init NetLib");
        }
        return mContext;
    }



}
