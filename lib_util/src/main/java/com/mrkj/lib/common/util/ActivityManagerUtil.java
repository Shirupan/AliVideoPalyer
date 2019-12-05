package com.mrkj.lib.common.util;

import android.app.Activity;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Liufan on 2016/3/24.
 */
public class ActivityManagerUtil {
    private static Stack<Activity> activityStack = new Stack<>();
    private static ActivityManagerUtil instance;
    private static Map<Activity, Boolean> activityInfos = new android.support.v4.util.ArrayMap<>();

    private ActivityManagerUtil() {
    }

    public static ActivityManagerUtil getScreenManager() {
        if (instance == null) {
            instance = new ActivityManagerUtil();
        }
        return instance;
    }

    //退出Activity
    public void popActivity(Activity activity) {
        if (activity != null) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
            if (activityStack.contains(activity)) {
                activityStack.remove(activity);
                activityInfos.remove(activity);
            }
        }
    }

    //获得当前栈顶Activity
    @Nullable
    public Activity currentActivity() {
        if (activityStack.empty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    //将当前Activity推入栈中
    public void pushActivity(Activity activity) {
        pushActivity(activity, false);
    }

    /**
     * 主要是用于首页入栈时候有个标识
     *
     * @param activity
     * @param isMainActivity
     */
    public void pushActivity(Activity activity, boolean isMainActivity) {
        activityInfos.put(activity, isMainActivity);
        if (!activityStack.contains(activity)) {
            activityStack.add(activity);
        }
    }

    //退出栈中所有Activity
    public void popAllActivityExceptOne(Class cls) {
        List<Activity> activities = new ArrayList<>();
        for (Activity activity : activityStack) {
            if (activity != null && !activity.getClass().equals(cls)) {
                activities.add(activity);
            }
        }
        for (Activity activity : activities) {
            popActivity(activity);
        }
    }


    public void popAllActivity() {
        while (activityStack != null && !activityStack.isEmpty()) {
            Activity activity = activityStack.lastElement();
            activityStack.remove(activity);
            activityInfos.remove(activity);
            activity.finish();
        }
    }

    public boolean isMainActivity(Activity activity) {
        Boolean b = activityInfos.get(activity);
        return b == null ? false : b;
    }

    /**
     * 首页会有个标识
     *
     * @return
     */
    public boolean isMainActivityOpened() {
        if (activityStack.isEmpty()) {
            return false;
        }
        for (Activity activity : activityStack) {
            if (isMainActivity(activity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当前Activity的上一个Activity
     *
     * @return
     */
    public Activity lastActivity() {
        if (activityStack.empty()) {
            return null;
        }
        //最后一个Activity
        Activity activity = activityStack.pop();
        if (activityStack.empty()) {
            return null;
        }
        Activity last = activityStack.lastElement();
        activityStack.add(activity);
        return last;
    }

    public boolean isWelcomeView(Activity activity) {
        return activity.getClass().getName().contains("WelcomeActivity");
    }
}
