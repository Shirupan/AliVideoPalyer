package com.xx.module.common.imageload.photo;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class ContextWrap {
    private Activity activity;
    private Context mContext;
    private Fragment fragment;

    public static ContextWrap of(Activity activity) {
        return new ContextWrap(activity);
    }

    public static ContextWrap of(Fragment fragment) {
        return new ContextWrap(fragment);
    }

    private ContextWrap(Activity activity) {
        this.mContext = activity;
        this.activity = activity;
    }

    private ContextWrap(Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        this.mContext = fragment.getContext();
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public Context getContext() {
        return mContext;
    }
}
