package com.mrkj.lib.push;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.huawei.hms.support.api.push.PushReceiver;

public class SmHuaweiPushReceiver extends PushReceiver {
    @Override
    public void onEvent(Context context, Event event, Bundle bundle) {
        super.onEvent(context, event, bundle);
        //华为推送点击事件
        Log.d("huawei", "华为推送点击状态栏" + bundle.toString());
    }
}
