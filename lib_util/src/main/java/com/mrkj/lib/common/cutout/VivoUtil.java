package com.mrkj.lib.common.cutout;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mrkj.lib.common.util.SmLogger;

import java.lang.reflect.Method;

/**
 * @author
 * @date 2018/5/10 0010
 */
public class VivoUtil {
    /**
     * 是否有凹槽
     */
    public static final int FLAG_NOTCH_SUPPORT = 0x00000020;
    /**
     * 屏幕是否有圆角
     */
    public static final int FLAG_CIRCLE_SUPPORT = 0x00000008;

    /**
     * 判断屏幕是否有凹槽。其高度为状态栏高度
     *
     * @param context
     * @return
     */
    public static boolean hasNotchInScreen(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class vivo = cl.loadClass("android.util.FtFeature");
            Method get = vivo.getMethod("isFeatureSupport");
            ret = (boolean) get.invoke(vivo, FLAG_NOTCH_SUPPORT);
        } catch (ClassNotFoundException e) {
            SmLogger.e("vivo hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            SmLogger.e("vivo hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            SmLogger.e("vivo hasNotchInScreen Exception");
        } finally {
            return ret;
        }
    }
}
