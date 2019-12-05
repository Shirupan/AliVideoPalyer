package com.xx.module.common.model.net;


import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.mcxiaoke.packer.helper.PackerNg;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.NetLib;
import com.mrkj.lib.net.base.OkHttpUtil;
import com.mrkj.lib.net.loader.file.SmNetProgressDialog;
import com.mrkj.lib.net.loader.qiniu.QiniuUolpadManager;
import com.qiniu.android.http.ResponseInfo;
import com.xx.base.GsonSingleton;
import com.xx.module.common.BaseConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author
 * @date 2017/10/25
 */

public class SmHttpClient {
    private static String ONLINE;
    private static long userId = -1;

    public static void setUserId(long uid) {
        userId = uid;
    }

    /**
     * GET方式的同步请求
     *
     * @param url ,tag也设置为url（取消请求时候用到）
     * @return
     */
    public static Response executeGET(String url) throws IOException {
        return OkHttpUtil.executeGET(addAndroidCheck(NetLib.getContext(), url));
    }

    /**
     * GET方式的异步请求
     *
     * @param url
     * @param callback OKHttp提供的回调方法并不是在主线程上。请使用UICallback回调。
     */
    public static Call executeGET(String url, Callback callback) {
        String allUrl = addAndroidCheck(NetLib.getContext(), url);
        return OkHttpUtil.executeGET(allUrl, callback);
    }

    public static Call executeGET(String url, Map<String, String> headers, boolean addParams, Callback callback) {
        String allUrl;
        if (addParams) {
            allUrl = addAndroidCheck(NetLib.getContext(), url);
        } else {
            allUrl = url;
        }
        return OkHttpUtil.executeGET(allUrl, headers, callback);
    }

    /**
     * @param url
     * @param requestBody
     * @param callback    OKHttp提供的回调方法并不是在主线程上。请使用UICallback回调。
     * @return 如果想取消请求，可以使用Call.cancel()方法
     */
    public static Call executePost(String url, RequestBody requestBody, Callback callback) {
        String allUrl = addAndroidCheck(NetLib.getContext(), url);
        return OkHttpUtil.executePost(allUrl, requestBody, callback);
    }

    public static Response executePost(String url, RequestBody requestBody) throws IOException {
        String allUrl = addAndroidCheck(NetLib.getContext(), url);
        return OkHttpUtil.executePost(allUrl, requestBody);

    }


    public static String addAndroidCheck(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        StringBuilder sBuffer = new StringBuilder(url);
        if (url.indexOf("?") > 0) {
        } else {
            sBuffer.append("?");
        }
        if (!url.contains("clientType=")) {
            sBuffer.append("&")
                    .append("clientType=1");
        }
        if (!url.contains("versionCode=")) {
            sBuffer.append("&");
            sBuffer.append("versionCode=").append(getVersionCode(context));
        }
        if (!url.contains("uniqueIdentifier=")) {
            sBuffer.append("&");
            sBuffer.append("uniqueIdentifier=").append(AppUtil.getIMEI(context));
        }
        if (!url.contains("uid_must=")) {
            if (userId != -1) {
                sBuffer.append("&");
                sBuffer.append("uid_must=").append(userId);
            }
        }
        if (!url.contains("packname=")) {
            sBuffer.append("&");
            sBuffer.append("packname=")
                    .append(getPackageName(context));
        }
        if (!url.contains("providername")) {
            sBuffer.append("&");
            sBuffer.append("providername=")
                    .append(PackerNg.getMarket(context, BaseConfig.DEFAULT_CHANNEL));
        }
        //手机机型
        sBuffer.append("&");
        sBuffer.append("mobile=")
                .append(Build.MANUFACTURER)
                .append("_")
                .append(Build.MODEL);
        return sBuffer.toString();
    }

    public static void addFormDataPart(@NotNull MultipartBody.Builder body) {
        Map<String, String> map = getInitParamsMap();
        String commom = GsonSingleton.getInstance().toJson(map);
        body.addFormDataPart("common", commom);
    }

    private static String getMetaDataValue(Context context, String metaDataName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return String.valueOf(appInfo.metaData.get(metaDataName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "dashi";
    }

    private static int getVersionCode(Context context) {
        try {
            if (context == null) {
                context = NetLib.getContext();
            }

            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 57;
    }

    private static String getPackageName(Context context) {
        try {
            if (context == null) {
                context = NetLib.getContext();
            }
            return context.getPackageName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "com.mrkj.sm";
    }

    private static int getVersionCode() {
        Context context;
        try {
            context = NetLib.getContext();
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 57;
    }

    private static String getPackageName() {
        Context context;
        context = NetLib.getContext();
        String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return "com.mrkj.sm";
        }
        return context.getPackageName();
    }

    private static String getMetaDataValue(String metaDataName) {
        try {
            Context context = NetLib.getContext();
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return String.valueOf(appInfo.metaData.get(metaDataName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "dashi";
    }

    /**
     * @return 返回带有公共参数的请求map
     */
    public static Map<String, String> getInitParamsMap() {
        Map<String, String> map = new ArrayMap<>();
        map.put("z", String.valueOf(1));
        map.put("clientType", String.valueOf(1));
        map.put("version", String.valueOf(getVersionCode()));
        map.put("uniqueIdentifier", AppUtil.getIMEI(NetLib.getContext()));
        if (userId != -1) {
            map.put("uid_must", String.valueOf(userId));
        }
        map.put("packname", getPackageName());
        map.put("channel", PackerNg.getMarket(NetLib.getContext(), BaseConfig.DEFAULT_CHANNEL));
        //设备机型
        map.put("mobile", Build.MANUFACTURER + "_" + Build.MODEL);
        return map;
    }


    public static void uploadQiniuFile(Context context, final String mLocalPath,
                                       final QiniuUolpadManager.OnUploadListener listener) {
        final File file = new File(mLocalPath);
        if (!file.exists() || !file.isFile()) {
            SmToast.show(context, "文件有误，请重新选择");
            if (listener != null) {
                listener.error(new IOException("empty file"));
            }
            return;
        }
        final Dialog dialog = new SmNetProgressDialog.Builder(context)
                .setMessage("请稍等")
                .setCancelable(false)
                .show();
        OkHttpUtil.uploadQiniuFile(mLocalPath, getInitParamsMap(), new QiniuUolpadManager.OnUploadListener() {
            @Override
            public boolean isCancelled() {
                if (dialog != null) {
                    return !dialog.isShowing();
                }
                return false;
            }

            @Override
            public void progress(String name, double percent) {
                if (dialog != null) {
                    int progress = (int) (percent * 100);
                    if (dialog instanceof SmNetProgressDialog) {
                        ((SmNetProgressDialog) dialog).setText("请稍等..." + progress + "%");
                    }
                }
                if (listener != null) {
                    listener.progress(name, percent);
                }
            }

            @Override
            public void complete(String url, ResponseInfo info, JSONObject response) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (listener != null) {
                    listener.complete(url, info, response);
                }
            }

            @Override
            public void error(Exception e) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (listener != null) {
                    listener.error(e);
                }
            }
        });
    }

    public static void addCommonMap(@NotNull Map<String, String> map) {
        map.put("common", GsonSingleton.getInstance().toJson(getInitParamsMap()));
    }


    public static abstract class SimpleOnQiniuFileUploadListener implements QiniuUolpadManager.OnUploadListener {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void progress(String name, double percent) {

        }

        @Override
        public void complete(String name, ResponseInfo info, JSONObject response) {

        }

        @Override
        public void error(Exception e) {

        }
    }


}
