package com.mrkj.module.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SMSModuleRegisterReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "com.mrkj.sm.action.inject.jump".equals(intent.getAction())) {

        }
    }
}
