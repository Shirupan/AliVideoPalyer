package com.xx.module.common.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created with IntelliJ IDEA.
 * User: ChengLu
 * Date: 13-8-5
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */
public class MyGridView extends GridView {

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec;
        //    SmLogger.d("centerAAA", "heightMeasureSpec = " + Integer.toBinaryString(heightMeasureSpec));
        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(
                    0x3fffffff, MeasureSpec.AT_MOST);
        } else {
            heightSpec = heightMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, heightSpec);
        //   int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        //  super.onMeasure(widthMeasureSpec, expandSpec);
    }

}