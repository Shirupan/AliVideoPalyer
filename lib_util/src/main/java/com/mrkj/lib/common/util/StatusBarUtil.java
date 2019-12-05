package com.mrkj.lib.common.util;


import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarUtil {
    public static void transparencyBar(Activity activity, boolean isTransparency) {
        transparencyBar(activity, isTransparency, false);
    }

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    public static void transparencyBar(Activity activity, boolean isTransparency, boolean isLightMode) {
        Window window = activity.getWindow();
        int uiFlags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isTransparency) {
                //(默认)状态栏背景被contentView填充，状态栏内容可见
                uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (isLightMode) {
                    uiFlags = uiFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                uiFlags = uiFlags | navigationWhite(window);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //如果设置了透明标签，再设置状态栏颜色的话，是不会生效的。
                // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                // window.setStatusBarColor(Color.TRANSPARENT);
                //以下是导航栏透明Flag
                //   window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                uiFlags = View.SYSTEM_UI_FLAG_VISIBLE;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    /**
     * 白色都导航栏背景，图标需要是深色调
     *
     * @param window
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static int navigationWhite(Window window) {
        int flags;
        flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.setNavigationBarColor(Color.WHITE);
        return flags;
    }


    /**
     * 设置状态栏颜色，并根据颜色是否是亮色调来调整显示状态栏文字
     *
     * @param activity
     * @param color
     */
    public static void setStatusBarColor(Activity activity, @ColorInt int color, boolean isLightMode) {
        if (color == 0) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            int flags = isLightMode ? View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    : View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            //导航栏一直都是白色都，所以按钮颜色只能是深色
            flags = flags | navigationWhite(window);
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //状态栏是否可见
          /*  window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);*/
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    /**
     * 是否占用statusbar的空间
     *
     * @return
     */
    public static boolean isOverStatusBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}