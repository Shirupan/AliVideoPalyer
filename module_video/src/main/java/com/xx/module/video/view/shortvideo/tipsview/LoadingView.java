package com.xx.module.video.view.shortvideo.tipsview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xx.module.video.R;

/**
 * 加载提示对话框。加载过程中，缓冲过程中会显示。
 */
public class LoadingView extends RelativeLayout {
    private static final String TAG = LoadingView.class.getSimpleName();


    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sm_alivc_dialog_loading, this, false);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);
    }

    /**
     * 更新加载进度
     *
     * @param percent 百分比
     */
    public void updateLoadingPercent(int percent) {
        // mLoadPercentView.setText(getContext().getString(R.string.alivc_loading) + percent + "%");
    }

    /**
     * 只显示loading，不显示进度提示
     */
    public void setOnlyLoading() {
        View view = findViewById(R.id.loading_layout);
        if (view != null) {
            view.setVisibility(GONE);
        }
    }

}
