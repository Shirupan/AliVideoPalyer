package com.mrkj.lib.common.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * @author
 * @date 2018/3/5 0005
 */

public class ColorUtils {
    /**
     * 动态颜色渐变
     *
     * @param fraction   进度值
     * @param startColor 起始颜色
     * @param endColor   终点颜色
     * @return
     */
    public static int getCurrentColor(float fraction, int startColor, int endColor) {
        int redCurrent;
        int blueCurrent;
        int greenCurrent;
        int alphaCurrent;

        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaStart = Color.alpha(startColor);

        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);
        int alphaEnd = Color.alpha(endColor);

        int redDifference = redEnd - redStart;
        int blueDifference = blueEnd - blueStart;
        int greenDifference = greenEnd - greenStart;
        int alphaDifference = alphaEnd - alphaStart;

        redCurrent = (int) (redStart + fraction * redDifference);
        blueCurrent = (int) (blueStart + fraction * blueDifference);
        greenCurrent = (int) (greenStart + fraction * greenDifference);
        alphaCurrent = (int) (alphaStart + fraction * alphaDifference);

        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent);
    }

    /**
     * 颜色动态
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static int eval(float fraction, int startValue, int endValue) {
        int startA = startValue >> 24 & 255;
        int startR = startValue >> 16 & 255;
        int startG = startValue >> 8 & 255;
        int startB = startValue & 255;
        int endA = endValue >> 24 & 255;
        int endR = endValue >> 16 & 255;
        int endG = endValue >> 8 & 255;
        int endB = endValue & 255;
        int currentA = startA + (int) (fraction * (float) (endA - startA)) << 24;
        int currentR = startR + (int) (fraction * (float) (endR - startR)) << 16;
        int currentG = startG + (int) (fraction * (float) (endG - startG)) << 8;
        int currentB = startB + (int) (fraction * (float) (endB - startB));
        return currentA | currentR | currentG | currentB;
    }

    public static Drawable setTintColorRes(Context context, @DrawableRes int res, @ColorRes int normal) {
        Drawable drawable = context.getResources().getDrawable(res).mutate();
        drawable = drawable.mutate();
        //版本适配
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, normal));
        return drawable;
    }

    public static Drawable setTintVectorDrawableColorRes(Context context, @DrawableRes int res, @ColorRes int normal) {
        Drawable drawable = VectorDrawableCompat.create(context.getResources(), res, null);
        if (drawable != null) {
            drawable = drawable.mutate();
            //版本适配
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, normal));
        }
        return drawable;
    }

    public static Drawable setTintColor(Context context, @DrawableRes int res, @ColorInt int normal) {
        Drawable drawable = context.getResources().getDrawable(res).mutate();
        drawable = drawable.mutate();
        //版本适配
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, normal);
        return drawable;
    }

    public static Drawable setTintColor(Context context, Drawable drawable, @ColorRes int normal) {
        if (drawable == null) {
            return null;
        }
        drawable = drawable.mutate();
        //版本适配
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, normal));
        return drawable;
    }

    public static Drawable setTintVectorDrawableColor(Context context, @DrawableRes int res, @ColorInt int normal) {
        Drawable drawable = VectorDrawableCompat.create(context.getResources(), res, null);
        if (drawable != null) {
            drawable = drawable.mutate();
            //版本适配
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, normal);
        }
        return drawable;
    }

    /**
     * 着色
     *
     * @param context
     * @param res
     * @param normal
     * @param press
     */
    public static Drawable setTintList(Context context, @DrawableRes int res, @ColorRes int normal, @ColorRes int press) {
        Drawable drawable = context.getResources().getDrawable(res).mutate();
        drawable = drawable.mutate();
        //版本适配
        drawable = DrawableCompat.wrap(drawable);
        if (press == 0) {
            press = android.R.color.transparent;
        }
        int activeColor = ContextCompat.getColor(context, press);
        if (normal == 0) {
            normal = android.R.color.transparent;
        }
        int inActiveColor = ContextCompat.getColor(context, normal);
        DrawableCompat.setTintList(drawable, new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected}, //1
                        new int[]{-android.R.attr.state_selected}, //2
                        new int[]{}
                },
                new int[]{
                        activeColor, //1
                        inActiveColor, //2
                        inActiveColor //3
                }
        ));
        return drawable;
    }

    public static Drawable setTintVectorDrawableList(Context context, @DrawableRes int res, @ColorRes int normal, @ColorRes int press) {
        Drawable drawable = VectorDrawableCompat.create(context.getResources(), res, null);
        if (drawable != null) {
            drawable = drawable.mutate();
            //版本适配
            drawable = DrawableCompat.wrap(drawable);
            int activeColor = ContextCompat.getColor(context, press);
            int inActiveColor = ContextCompat.getColor(context, normal);
            DrawableCompat.setTintList(drawable, new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //1
                            new int[]{-android.R.attr.state_selected}, //2
                            new int[]{}
                    },
                    new int[]{
                            activeColor, //1
                            inActiveColor, //2
                            inActiveColor //3
                    }
            ));
        }
        return drawable;
    }


    public static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        // It's a light color
        return darkness < 0.5;
    }
}
