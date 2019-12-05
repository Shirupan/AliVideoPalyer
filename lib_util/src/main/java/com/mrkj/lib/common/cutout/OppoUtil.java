package com.mrkj.lib.common.cutout;

import android.content.Context;

/**
 * @author
 * @date 2018/5/10 0010
 */
public class OppoUtil {
    /**
     * 判断屏幕是否有凹槽。其高度为状态栏高度
     *
     * @param context
     * @return
     */
    public static boolean hasNotchInScreen(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }
}
