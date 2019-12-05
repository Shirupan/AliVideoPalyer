package com.xx.module.common.view.widget;

import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.View;

/**
 * 版本兼容
 *
 * @author someone
 * @date 2019-06-03
 */
public class SmCompat {

    /**
     * 5.0之前的版本在xml-drawable中使用自定义属性来配置主题会报错
     *
     * @param view
     * @param resource
     * @param compat
     */
    public static void setBackgound(View view, @DrawableRes int resource, @DrawableRes int compat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackgroundResource(resource);
        } else {
            view.setBackgroundResource(compat);
        }
    }
}
