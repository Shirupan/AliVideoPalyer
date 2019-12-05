package com.xx.module.common.view.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild2;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.tencent.smtt.sdk.WebView;

/**
 * @author someone
 * @date 2019-06-14
 */
public class NestScrollWebView extends WebView implements NestedScrollingChild2 {

    public boolean scrollEnable;

    public NestScrollWebView(Context context) {
        super(context);
    }

    public NestScrollWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public NestScrollWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!scrollEnable) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return false;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }
}
