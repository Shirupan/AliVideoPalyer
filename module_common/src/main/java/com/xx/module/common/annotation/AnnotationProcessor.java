package com.xx.module.common.annotation;/**
 * Created by someone on 2017/1/12.
 */

import android.app.Activity;
import android.text.TextUtils;

/**
 * 注解处理器
 *
 * @Author
 * @Create 2017/1/12
 */
public class AnnotationProcessor {

    public static Class<?> getPresenterAnnotation(Class<?> clz) {
        if (clz.isAnnotationPresent(Presenter.class)) {
            Presenter type = clz.getAnnotation(Presenter.class);
            if (type != null) {
                return type.value();
            }
        }
        return null;
    }

    public static String getActivityPath(Class<? extends Activity> clz) {
        if (clz.isAnnotationPresent(Path.class)) {
            Path type = clz.getAnnotation(Path.class);
            if (type != null) {
                String value = type.value();
                return TextUtils.isEmpty(value) ? "0" : value;
            }
        }
        return "0";
    }
}
