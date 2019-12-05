package com.xx.module.common.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * @author
 * 宽高比相等的ImageView
 * @date 2017/7/3
 */

public class SquareImageView extends AppCompatImageView {
    public enum HeightType {
        TYPE_1_1, TYPE_WARP_CONTENT
    }

    private HeightType heightType = HeightType.TYPE_1_1;


    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (heightType == HeightType.TYPE_1_1) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
        } else {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setHeightType(HeightType heightType) {
        this.heightType = heightType;
    }

    public HeightType getHeightType() {
        return heightType;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
