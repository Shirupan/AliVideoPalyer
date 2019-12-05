package com.xx.lib.db.entity;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;


/**
 * @author
 * @date 2018/1/18 0018
 */

public class SmContextWrap {
    private Activity activity;
    private Fragment fragment;
    private Context mContext;

    public static SmContextWrap obtain(Activity activity) {
        return new SmContextWrap(activity);
    }

    public static SmContextWrap obtain(Fragment fragment) {
        return new SmContextWrap(fragment);
    }

    private SmContextWrap(Activity activity) {
        this.activity = activity;
    }

    private SmContextWrap(Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.getActivity();
    }

    private SmContextWrap(Context context) {
        this.mContext = context;
    }

    public static SmContextWrap obtain(Context context) {
        return new SmContextWrap(context);
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
        if (fragment != null) {
            return fragment.getContext();
        } else if (activity != null) {
            return activity;
        }
        return mContext;
    }


}
