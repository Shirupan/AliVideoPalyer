package com.xx.module.common.view.widget;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;

import org.jetbrains.annotations.NotNull;

/**
 * @author someone
 * @date 2019-06-11
 */
public class SmAnimationUtil {
    /**
     * 缩放动画
     *
     * @param view
     * @param scale  与原始大小的缩放比
     * @param reaver 动画是否会回到原始大小
     */
    public static void scale(@NotNull final View view, final float scale, final boolean reaver) {
        view.post(new Runnable() {
            @Override
            public void run() {
                AnimationSet set = new AnimationSet(false);
                Animation animation = new ScaleAnimation(1.0f, scale, 1.0f, scale, view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2);
                animation.setDuration(200);
                animation.setInterpolator(new LinearInterpolator());
                set.addAnimation(animation);
                if (reaver) {
                    Animation animation1 = new ScaleAnimation(scale, 1.0f, scale, 1.0f, view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2);
                    animation1.setDuration(200);
                    animation1.setStartOffset(200);
                    animation1.setInterpolator(new LinearInterpolator());
                    set.addAnimation(animation1);

                    /*Animation animation2 = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2);
                    animation2.setDuration(200);
                    animation2.setStartOffset(500);
                    animation2.setInterpolator(new LinearInterpolator());
                    set.addAnimation(animation2);*/
                }
                view.startAnimation(set);
            }
        });
    }
}
