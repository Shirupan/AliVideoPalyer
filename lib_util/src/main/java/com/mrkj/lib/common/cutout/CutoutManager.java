package com.mrkj.lib.common.cutout;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.SmLogger;

/**
 * @author
 * @date 2018/5/10 0010
 */
public class CutoutManager {

    public static String getHandSetInfo() {
        String handSetInfo = "手机型号:" + Build.MODEL
                + "\n系统版本:" + Build.VERSION.RELEASE
                + "\n产品型号:" + Build.PRODUCT
                + "\n版本显示:" + Build.DISPLAY
                + "\n系统定制商:" + Build.BRAND
                + "\n设备参数:" + Build.DEVICE
                + "\n开发代号:" + Build.VERSION.CODENAME
                + "\nSDK版本号:" + Build.VERSION.SDK_INT
                + "\nCPU类型:" + Build.CPU_ABI
                + "\n硬件类型:" + Build.HARDWARE
                + "\n主机:" + Build.HOST
                + "\n生产ID:" + Build.ID
                + "\nROM制造商:" + Build.MANUFACTURER // 这行返回的是rom定制商的名称
                ;
        SmLogger.d(handSetInfo);
        return handSetInfo;
    }


    /**
     * 获取状态栏以及凹槽高度中的max
     *
     * @param context
     * @return
     */
    public static int getCutOutAndStatusMaxHeight(Context context) {
        String brand = Build.BRAND.toLowerCase();
        if (brand.contains("huawei")) {
            int cutoutHeight = 0;
            if (HuaweiUtil.hasNotchInScreen(context)) {
                cutoutHeight = HuaweiUtil.getNotchSize(context)[1];
            }
            int statusHeight = AppUtil.getStatuBarHeight(context);
            return Math.max(cutoutHeight, statusHeight);
        } else if (brand.contains("oppo")) {
            if (OppoUtil.hasNotchInScreen(context)) {
                //oppo文档中没有给出获取凹槽高度。默认应该是状态栏高度
            }
            return AppUtil.getStatuBarHeight(context);
        } else if (brand.contains("vivo")) {
            if (VivoUtil.hasNotchInScreen(context)) {
                //vivo文档中没有给出获取凹槽高度。默认应该是状态栏高度
            }
            return AppUtil.getStatuBarHeight(context);
        } /* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        } */ else {
            return AppUtil.getStatuBarHeight(context);
        }
    }

    /**
     * 判断屏幕是否是凹槽屏
     *
     * @param context
     * @return
     */
    public static boolean isCutoutScreen(Context context) {
        String brand = Build.BRAND.toLowerCase();
        if (brand.contains("huawei")) {
            return HuaweiUtil.hasNotchInScreen(context);
        } else if (brand.contains("oppo")) {
            return OppoUtil.hasNotchInScreen(context);
        } else if (brand.contains("vivo")) {
            return VivoUtil.hasNotchInScreen(context);
        }/* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        }*/ else {
            return false;
        }
    }

    public static void setCutoutFullScreen(Activity activity) {
        String brand = Build.BRAND.toLowerCase();
     /*   if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)) {

        } else*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (brand.contains("huawei")) {
                HuaweiUtil.setFullScreenWindowLayoutInDisplayCutout(activity.getWindow());
            } else if (brand.contains("oppo")) {
                //return OppoUtil.hasNotchInScreen(context);
            } else if (brand.contains("vivo")) {
                // return VivoUtil.hasNotchInScreen(context);
            }
        }

    }
}
