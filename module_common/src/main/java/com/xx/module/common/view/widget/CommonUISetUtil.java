package com.xx.module.common.view.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

import com.xx.module.common.R;

import org.jetbrains.annotations.NotNull;

/**
 * @author someone
 * @date 2019-06-11
 */
public class CommonUISetUtil {


    public static void initSwipeRefreshLayout(@NotNull SwipeRefreshLayout refreshLayout, final Runnable function) {
        Context context = refreshLayout.getContext();
        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.app_main_color_pressed),
                ContextCompat.getColor(context, R.color.app_main_color),
                ContextCompat.getColor(context, R.color.app_main_color));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                function.run();
            }
        });
    }


    /**
     * 打开默认局部刷新动画
     */
    public static void openDefaultAnimator(RecyclerView rv) {
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator != null) {
            animator.setAddDuration(120);
            animator.setChangeDuration(250);
            animator.setMoveDuration(250);
            animator.setRemoveDuration(120);
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(true);
            }
        }
    }

    /**
     * 关闭默认局部刷新动画
     */
    public static void closeDefaultAnimator(RecyclerView rv) {
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator != null) {
            animator.setAddDuration(0);
            animator.setChangeDuration(0);
            animator.setMoveDuration(0);
            animator.setRemoveDuration(0);
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
        }
    }
}
