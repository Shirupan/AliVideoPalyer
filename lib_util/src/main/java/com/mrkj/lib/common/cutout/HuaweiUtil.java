package com.mrkj.lib.common.cutout;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Window;
import android.view.WindowManager;

import com.mrkj.lib.common.util.SmLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author
 * @date 2018/5/10 0010
 */
public class HuaweiUtil {
    /**
     * 华为机型判断是否是凹面屏
     *
     * @param context
     * @return
     */
    public static boolean hasNotchInScreen(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            SmLogger.e("huawei hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            SmLogger.e("huawei hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            SmLogger.e("huawei hasNotchInScreen Exception");
        } finally {
            return ret;
        }
    }

    /**
     * 凹面高度
     *
     * @param context
     * @return
     */
    public static int[] getNotchSize(Context context) {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            SmLogger.e("huawei getNotchSize ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            SmLogger.e("huawei getNotchSize NoSuchMethodException");
        } catch (Exception e) {
            SmLogger.e("huawei getNotchSize Exception");
        } finally {
            return ret;
        }
    }

    public static final int FLAG_NOTCH_SUPPORT = 0x00010000;

    /**
     * 设置凹面屏全屏页面模式 （默认全屏会用黑色填充凹面高度。设置该项，则将页面内容显示到全屏）
     *
     * @param window
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("addHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            SmLogger.e("huawei hw notch screen flag api error");
        } catch (Exception e) {
            SmLogger.e("huawei other Exception");
        }
    }
}
