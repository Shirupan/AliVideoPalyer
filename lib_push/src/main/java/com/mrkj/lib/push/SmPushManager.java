package com.mrkj.lib.push;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.getui.sm.BuildConfig;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.HuaweiPushApiImp;
import com.tencent.android.tpush.XGCustomPushNotificationBuilder;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import java.util.Map;
import java.util.Set;

public class SmPushManager {

    public static final String KEY_WHAT = "what";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CONTENT = "customContentString";
    public static final String KEY_MESSAGE = "message";

    /**
     * 绑定服务
     */
    public static final String WHAT_BIND_START = "1";
    /**
     * 查询消息数
     */
    public static final String WHAT_MESSAGE_START = "2";
    /**
     * 处理通知栏点击
     */
    public static final String WHAT_NOTIFICATION_START = "3";


    /**
     * 推送消息透传处理
     */
    private static String pushMessageAction;

    public static void bindPushMessageBroadCastReceiver(String action) {
        pushMessageAction = action;
    }

    /**
     * 判断通知是否被打开，并且默认会有提示去打开通知
     */
    public static void checkNotificationIsOpen(Context context, NotificationCheckCallback checkCallback) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (manager.areNotificationsEnabled()) {
            if (checkCallback != null) {
                checkCallback.onOpened();
            }
            return;
        }

        if (checkCallback != null && checkCallback.onLimit()) {
            return;
        }



    }

    public interface NotificationCheckCallback {
        boolean onLimit();

        boolean onOpened();
    }


    /**
     * 腾讯信鸽初始化
     *
     * @param context
     * @param uid
     */
    public static void startService(Context context, String uid, String notificationChannelId, String notificationChannelName) {
        if (BuildConfig.DEBUG) {
            XGPushConfig.enableDebug(context.getApplicationContext(), true);
        }
        XGPushConfig.enableDebug(context.getApplicationContext(), true);
        SmPushNotificationBuilder builder = new SmPushNotificationBuilder(notificationChannelId, notificationChannelName);
        XGPushManager.setDefaultNotificationBuilder(context.getApplicationContext(), builder);
        //华为厂商日志记录
        XGPushConfig.setHuaweiDebug(true);
        //小米厂商渠道
        XGPushConfig.setMiPushAppId(context.getApplicationContext(),
                getAppMetaData(context, "XIAOMI_PUSH_APPID_KEY"));
        XGPushConfig.setMiPushAppKey(context.getApplicationContext(),
                getAppMetaData(context, "XIAOMI_PUSH_APPKEY_KEY"));
        //魅族厂商渠道
        XGPushConfig.setMzPushAppId(context.getApplicationContext(),
                getAppMetaData(context, "MEIZU_PUSH_APPID_KEY"));
        XGPushConfig.setMzPushAppKey(context.getApplicationContext(),
                getAppMetaData(context, "MEIZU_PUSH_APPKEY_KEY"));

        XGPushConfig.enableOtherPush(context.getApplicationContext(), true);
        if (TextUtils.isEmpty(uid)) {
            XGPushManager.registerPush(context.getApplicationContext());
        } else {
            //绑定用户体系
            XGPushManager.bindAccount(context.getApplicationContext(), uid);
        }
    }

    /**
     * 推送解除绑定账号
     *
     * @param context
     * @param uid
     */
    public static void delAccount(Context context, String uid) {
        if (!TextUtils.isEmpty(uid)) {
            XGPushManager.delAccount(context, uid, new XGIOperateCallback() {
                @Override
                public void onSuccess(Object data, int flag) {
                    //token在设备卸载重装的时候有可能会变
                    Log.d("TPush", "解除绑定账号成功，设备token为：" + data);
                }

                @Override
                public void onFail(Object data, int errCode, String msg) {
                    Log.d("TPush", "解除绑定账号失败，错误码：" + errCode + ",错误信息：" + msg);
                }
            });
        }
    }

    /**
     * 关闭推送
     *
     * @param context
     */
    public static void unregisterPush(final Context context) {
        XGPushManager.unregisterPush(context, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                //token在设备卸载重装的时候有可能会变
                Log.d("TPush", "注销推送成功");
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注销失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
    }

    /**
     * 处理推送消息
     *
     * @param context
     * @param params
     */
    public static void pushMessage(Context context, Map<String, String> params) {
        if (pushMessageAction == null) {
            return;
        }
        Intent startIntent = new Intent(pushMessageAction);
        startIntent.setPackage(context.getPackageName());
        if (params != null) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = params.get(key);
                if (!TextUtils.isEmpty(value)) {
                    startIntent.putExtra(key, value);
                }
            }
        }
        try {
            context.sendBroadcast(startIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return "";
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        Object value = applicationInfo.metaData.get(key);
                        if (value instanceof String) {
                            resultData = ((String) value).replace("value_", "");
                        } else {
                            resultData = String.valueOf(value);
                        }
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }
}
