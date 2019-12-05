package com.xx.module.common.imageload.photo;

import android.content.Intent;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class IntentWap {
    private Intent intent;
    private int requestCode;

    public IntentWap() {
    }

    public IntentWap(Intent intent, int requestCode) {
        this.intent = intent;
        this.requestCode = requestCode;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }
}
