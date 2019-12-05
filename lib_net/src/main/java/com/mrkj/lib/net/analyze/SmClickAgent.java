package com.mrkj.lib.net.analyze;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.mcxiaoke.packer.helper.PackerNg;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.net.base.OkHttpUtil;
import com.xx.lib.db.entity.SmContextWrap;

import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author
 * @date 2018/4/9 0009
 */
public class SmClickAgent {
    private static String DEFAULT_CHANNEL = "";
    private static String BASE_URL = "";

    /**
     * 点击事件统计
     *
     * @param baseUrl
     * @param defaultChannel
     */
    public static void init(String baseUrl, String defaultChannel) {
        BASE_URL = baseUrl;
        DEFAULT_CHANNEL = defaultChannel;
    }

    public static void init(String defaultChannel) {
        DEFAULT_CHANNEL = defaultChannel;
    }

    private static long uid = 0L;

    public static void setUserId(long uid) {
        SmClickAgent.uid = uid;
    }

    /**
     * 点击事件统计
     *
     * @param context
     * @param name
     */
    public static void onEvent(Context context, String name, String title) {
        try {
            onEvent(context, name, title, uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param contextWrap
     * @param name
     * @param addActivityClassName 将类名称添加到name前缀
     */
    public static void onEvent(SmContextWrap contextWrap, String name, String title, boolean addActivityClassName) {
        Activity activity = contextWrap.getActivity() == null ? contextWrap.getFragment().getActivity() : contextWrap.getActivity();
        String tag;
        if (addActivityClassName && activity != null) {
            tag = activity.getClass().getSimpleName() + "_" + name;
        } else {
            tag = name;
        }
        SmClickAgent.onEvent(contextWrap.getContext(), tag, title);
    }

    /**
     *
     */
    public static void onEvent(Context context, final String name, String title, Long uid) {
        Map<String, String> param = getInitParamsMap(context);
        param.put("from", name);
        if (uid == null) {
            uid = 0L;
        }
        param.put("uid", uid + "");
        param.put("name", title);
        createApi(SmClickAgentService.class)
                .clickEvent(param)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mObserver);
    }

    private static Observer<String> mObserver = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(String s) {
            Log.d(getClass().getSimpleName(), "[success]SmClickAgent onEvent");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            Log.d(getClass().getSimpleName(), "[fail]SmClickAgent onEvent");
        }

        @Override
        public void onComplete() {

        }
    };

    /**
     * @return 返回带有公共参数的请求map
     */
    private static Map<String, String> getInitParamsMap(Context context) {
        Map<String, String> map = new ArrayMap<>();
        map.put("z", "1");
        map.put("clientType", "1");
        map.put("versionCode", getVersionCode(context) + "");
        map.put("uniqueIdentifier", AppUtil.getIMEI(context));
        map.put("providername", PackerNg.getMarket(context, DEFAULT_CHANNEL));
        return map;
    }

    private static int getVersionCode(Context context) {
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int version;

            if (packInfo == null) {
                version = 0;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (packInfo.getLongVersionCode() < Integer.MAX_VALUE) {
                        return (int) packInfo.getLongVersionCode();
                    } else {
                        return Integer.MAX_VALUE;
                    }
                } else {
                    version = packInfo.versionCode;
                }
            }
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 57;
    }

    private static Retrofit retrofit;

    /**
     * @return Retrofit接口实例化
     */
    private static <T> T createApi(Class<T> type) {
        if (retrofit == null) {
            synchronized (SmClickAgent.class) {
                if (retrofit == null) {
                    initRetrofit();
                }
            }
        }
        return retrofit.create(type);
    }

    private static void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpUtil.getOkHttpClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
