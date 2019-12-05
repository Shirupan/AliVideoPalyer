package com.xx.module.common.view.widget;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.mrkj.lib.common.util.ScreenUtils;
import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.common.imageload.BitmapUtil;

/**
 * @author
 * @date 2018/7/27 0027
 */
public class ViewRevealAnimatorUtils {
    public static Bitmap tansalteBitmap;

    /**
     * 获取View中心点
     *
     * @param view
     * @return
     */
    public static int[] getViewCenter(View view) {
        int left = view.getLeft();
        int right = view.getRight();
        int x1 = (right - left) / 2;
        int y1 = (view.getBottom() - view.getTop()) / 2;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x2 = location[0];
        int y2 = location[1];
        return new int[]{x2 + x1, y2 + y1};
    }

    public static Intent getRevealAnimatorIntent(Context context, View view, Class<?> clz) {
        //获得屏幕坐标
        int[] viewCenter = getViewCenter(view);
        Intent intent = new Intent(context, clz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context instanceof Activity) {
            View contentView = ((Activity) context).findViewById(android.R.id.content);
            if (contentView != null) {
                ViewRevealAnimatorUtils.tansalteBitmap = BitmapUtil.getBitmapFromView(contentView);
            }

        }
        intent.putExtra("view_reveal_x", viewCenter[0]);
        intent.putExtra("view_reveal_y", viewCenter[1]);
        return intent;
    }

    /**
     * 设置动画
     *
     * @param activity
     * @param view
     * @param intent
     */
    public static void setActivityStartAnim(final Activity activity, final View view, final Intent intent) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int mX = intent.getIntExtra("view_reveal_x", 0);
                    int mY = intent.getIntExtra("view_reveal_y", 0);
                    if (view != null && view.isAttachedToWindow()) {
                        //对控件View进行判空，防止后台时间过长activity被回收后启动
                        Animator animator = createRevealAnimator(activity, view, false, mX, mY);
                        animator.start();
                    }
                }
            }
        });
    }

    /**
     * 关闭Activity动画
     *
     * @param activity
     * @param view
     * @param intent
     */
    public static void setActivityFinishAnim(final Activity activity, final View view, final Intent intent) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int mX = intent.getIntExtra("view_reveal_x", 0);
                    int mY = intent.getIntExtra("view_reveal_y", 0);
                    if (view != null && view.isAttachedToWindow()) {
                        //对控件View进行判空，防止后台时间过长activity被回收后启动
                        Animator animator = createRevealAnimator(activity, view, true, mX, mY);
                        animator.start();
                    }
                }
            }
        });
    }

    /**
     * @param activity
     * @param view
     * @param reversed 是否反向(页面关闭)
     * @param x
     * @param y
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Animator createRevealAnimator(@Nullable final Activity activity, @NonNull final View view,
                                                 final boolean reversed, int x, int y) {
        return createRevealAnimator(activity, view, reversed, x, y, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Animator createRevealAnimator(@Nullable final Activity activity, @NonNull final View view,
                                                 final boolean reversed, int x, int y, final SimpleAnimatorListener listener) {
        int a = x;
        int b = y;
        Context context;
        if (activity != null) {
            context = activity;
        } else {
            context = view.getContext();
        }
        int screenWidth = ScreenUtils.getWidth(context);
        int screenHeight = ScreenUtils.getHeight(context);
        if (screenWidth - x > x) {
            a = screenWidth - x;
        }
        if (screenHeight - y > y) {
            b = screenHeight - y;
        }
        float hypot = (float) Math.hypot(a, b);
        float startRadius = reversed ? hypot : 0;
        float endRadius = reversed ? 0 : hypot;
        Animator animator = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (ViewRevealAnimatorUtils.tansalteBitmap != null) {
                    ViewRevealAnimatorUtils.tansalteBitmap.recycle();
                    ViewRevealAnimatorUtils.tansalteBitmap = null;
                }
                if (reversed) {
                    if (activity != null) {
                        view.setVisibility(View.INVISIBLE);
                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                    }
                }
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
                SmLogger.d("结束跳转页面");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null) {
                    listener.onAnimationCancel(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null) {
                    listener.onAnimationRepeat(animation);
                }
            }
        });
        return animator;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setViewStartAnim(final View view, final int x, final int y, final SimpleAnimatorListener listener) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (view.isAttachedToWindow()) {
                    //对控件View进行判空，防止后台时间过长activity被回收后启动
                    Animator animator = createRevealAnimator(null, view, false, x, y, listener);
                    animator.start();
                }
            }
        });
    }

    /**
     * @param view     动画效果的view
     * @param listener
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setViewCloseAnim(final View view, final int x, final int y, final SimpleAnimatorListener listener) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (view.isAttachedToWindow()) {
                    Animator animator = createRevealAnimator(null, view, true, x, y, listener);
                    animator.start();
                }
            }
        });
    }

    public static abstract class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


}
