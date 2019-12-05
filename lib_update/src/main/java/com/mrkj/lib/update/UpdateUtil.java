package com.mrkj.lib.update;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

/**
 * @author
 * @date 2017/7/13
 */

public class UpdateUtil {
    /**
     * @return 0没有网络，1数据流量，2wifi
     */
    public static int isNetWorkConnected(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkInfo network = connectivityManager.getActiveNetworkInfo();
            if (network == null || !network.isAvailable()) {
            } else {
                String name = network.getTypeName();
                if (network.getState() == NetworkInfo.State.CONNECTED) {
                    //更改NetworkStateService的静态变量，之后只要在Activity中进行判断就好了
                    if (TextUtils.equals(name.toLowerCase(), "WIFI".toLowerCase())) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            }
        } else {
            NetworkInfo[] networks = connectivityManager.getAllNetworkInfo();
            for (NetworkInfo info : networks) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    String name = info.getTypeName();
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        //更改NetworkStateService的静态变量，之后只要在Activity中进行判断就好了
                        if (name.equals("WIFI")) {
                            return 2;
                        } else {
                            return 1;
                        }
                    }
                }
            }
        }

        return 0;
    }

    /**
     * 获取meta-data下对应的key的值
     *
     * @param ctx
     * @param key meta-data 的名称
     * @return
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = String.valueOf(applicationInfo.metaData.get(key));
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

}
