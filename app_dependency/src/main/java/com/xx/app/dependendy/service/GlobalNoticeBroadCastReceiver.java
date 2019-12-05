package com.xx.app.dependendy.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.common.router.RouterParams;

/**
 * 各module之间消息通知(内存资源不忧，慎用广播)
 *
 * @author
 */

public class GlobalNoticeBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SmLogger.d("[GlobalNoticeBroadCastReceiver]:" + action);
        if (TextUtils.equals(action, RouterParams.GlobalBroadCast.PUSH_MESSAGE)) {
            //推送消息处理
            MyPushMessageJob job = new MyPushMessageJob(context, intent);
            job.doJob();
        }
    }
}
