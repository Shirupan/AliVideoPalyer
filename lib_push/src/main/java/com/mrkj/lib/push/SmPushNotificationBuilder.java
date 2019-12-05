package com.mrkj.lib.push;

import android.app.Notification;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.android.tpush.XGCustomPushNotificationBuilder;

public class SmPushNotificationBuilder extends XGCustomPushNotificationBuilder {

    public SmPushNotificationBuilder(String channelId, String channelName) {
        setDefaults(Notification.DEFAULT_ALL);
        if (!TextUtils.isEmpty(channelId)) {
            setChannelId(channelId);
        }
        if (!TextUtils.isEmpty(channelName)) {
            setChannelName(channelName);
        }
    }

    @Override
    public Notification buildNotification(Context context) {
        Log.d("Notification", "来通知，构建Notification");
        return super.buildNotification(context);
    }
}
