package com.mrkj.lib.common.util;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import com.mrkj.lib.common.view.SmToast;

import java.util.List;

public class ScreenUtils {

    public static int getWidth(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.widthPixels;
    }

    /**
     * 获取手机显示高度（不包括不可绘制的虚拟栏）
     *
     * @param activity
     * @return
     */
    public static int getHeight(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    /**
     * 获取手机屏幕真实高度
     *
     * @param activity
     * @return
     */
    public static int getRealHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            display.getSize(size);
        }
        return size.y;
    }

    public static int getHeight(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.heightPixels;
    }

    public static float getDensity(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.density;
    }

    public static int getDensityDpi(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.densityDpi;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;

    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int dp2px(Context context, float dp) {
        if (context == null) {
            return 1;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    public static int getColorFromRes(Context context, int colorRes) {
        if (context != null) {
            return ContextCompat.getColor(context, colorRes);
        }
        return Color.WHITE;
    }

    public static Drawable getDrawable(Context context, int imageRes) {
        if (context != null) {
            return ContextCompat.getDrawable(context, imageRes);
        }
        return new ColorDrawable();
    }


    public static void startSavityActivity(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } catch (Exception e) {
                SmToast.show(context, "无法开启允许查看使用情况的应用界面");
                e.printStackTrace();
            }
        }
    }


    /**
     * 判断调用该设备中“有权查看使用权限的应用”这个选项的APP有没有打开
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static boolean isSwitch(Context context) {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, ts);
        return !(queryUsageStats == null || queryUsageStats.isEmpty());
    }
}
