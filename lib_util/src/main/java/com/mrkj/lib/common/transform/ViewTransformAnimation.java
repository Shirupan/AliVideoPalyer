package com.mrkj.lib.common.transform;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author someone
 * @date 2019/3/27 0027
 */
public class ViewTransformAnimation {
    public static final String VIEW_TRANSFORM = "view_transform";

    public static void startActivityForResult(Activity a, View view, Intent intent, int requestCode) {
        int[] positions = new int[2];
        ViewTransform transform = new ViewTransform();
        transform.setPosition(positions);
        transform.setWidth(view.getMeasuredWidth());
        transform.setHeight(view.getMeasuredHeight());
        intent.putExtra(VIEW_TRANSFORM, transform);
        a.startActivityForResult(intent, requestCode);
        a.overridePendingTransition(0, 0);
    }

    @Nullable
    public static ViewTransform getViewTransform(Intent intent) {
        return intent.getParcelableExtra(VIEW_TRANSFORM);
    }

    public static Animator attachToView(Activity activity, View view, boolean hidden, Runnable callback) {
        ViewTransform transform = activity.getIntent().getParcelableExtra(VIEW_TRANSFORM);
        if (transform == null) {
            callback.run();
            return ValueAnimator.ofInt(0, 0);
        }
        if (hidden) {
            view.setVisibility(View.INVISIBLE);
            activity.findViewById(android.R.id.content).setVisibility(View.INVISIBLE);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        view.post(new Runnable() {
            @Override
            public void run() {
                //ValueAnimator animatorX=ValueAnimator.ofFloat()
            }
        });
        return animatorSet;
    }
}
