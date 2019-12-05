package com.xx.module.common.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mrkj.lib.common.util.ScreenUtils;

/**
 * @author
 * @date 2018/8/23 0023
 */
public class SmDownloadProgressView extends View {

    private int minSize;
    private int mProgress;
    private int mBorderSize;
    private RectF mRectF = new RectF();
    private int mProgressColor = Color.parseColor("#ffffff");
    private Paint mCirclePaint;
    private Paint mBorderPaint;

    public SmDownloadProgressView(Context context) {
        super(context);
    }

    public SmDownloadProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SmDownloadProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w >= 0 && h > 0) {
            minSize = Math.min(w, h);
        } else {
            minSize = w < 0 ? h : w;
            if (minSize < 0) {
                minSize = ScreenUtils.dp2px(getContext(), 50);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initIfNeed();
        mRectF.bottom = getMeasuredHeight() / 2 + minSize / 2 - mBorderSize * 2;
        mRectF.top = getMeasuredHeight() / 2 - minSize / 2 + mBorderSize * 2;
        mRectF.left = getMeasuredWidth() / 2 - minSize / 2 + mBorderSize * 2;
        mRectF.right = getMeasuredWidth() / 2 + minSize / 2 - mBorderSize * 2;
        int cx = getMeasuredWidth() / 2;
        int cy = getMeasuredHeight() / 2;
        canvas.drawCircle(cx, cy, minSize / 2 - mBorderSize, mBorderPaint);
        float progressAngle = mProgress * 360 / 100;
        canvas.drawArc(mRectF, -90, progressAngle, true, mCirclePaint);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int mProgress) {
        this.mProgress = mProgress;
        postInvalidate();
    }


    public void setBorderSize(int mBorderSize) {
        this.mBorderSize = mBorderSize;
    }

    public void setProgressColor(int mProgressColor) {
        this.mProgressColor = mProgressColor;
        initIfNeed();
    }

    private void initIfNeed() {
        if (mCirclePaint == null) {
            mCirclePaint = new Paint();
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setStyle(Paint.Style.FILL);
            mCirclePaint.setColor(mProgressColor);
            mCirclePaint.setAlpha(200);
        }
        if (mBorderPaint == null) {
            mBorderPaint = new Paint();
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setAntiAlias(true);
            if (mBorderSize == 0) {
                mBorderSize = ScreenUtils.dip2px(getContext(), 3);
            }
            mBorderPaint.setStrokeWidth(mBorderSize);
            mBorderPaint.setColor(mProgressColor);
            mBorderPaint.setAlpha(200);
        }
    }
}
