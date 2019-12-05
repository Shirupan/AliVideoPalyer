package com.xx.module.video.view.shortvideo.tipsview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alivc.player.AliyunErrorCode;
import com.mrkj.lib.common.util.ScreenUtils;

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */

/**
 * 提示对话框的管理器。
 * {@link LoadingView}
 */

public class TipsView extends FrameLayout {
    //错误码
    private int mErrorCode;

    //网络请求加载提示
    private LoadingView mBufferLoadingView = null;
    //提示点击事件
    private OnTipClickListener mOnTipClickListener = null;

    private boolean isErrorViewAdded;
    private ErrorView errorView;
    //网络变化监听事件。
    private OnNetChangeClickListener onNetChangeClickListener = new OnNetChangeClickListener() {
        @Override
        public void onContinuePlay() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener.onContinuePlay();
            }
        }

        @Override
        public void onStopPlay() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener.onStopPlay();
            }
        }
    };

    /**
     * 重试的点击事件
     */
    public interface OnRetryClickListener {
        /**
         * 重试按钮点击
         */
        void onRetryClick();
    }

    /**
     * 界面中的点击事件
     */
    public interface OnNetChangeClickListener {
        /**
         * 继续播放
         */
        void onContinuePlay();

        /**
         * 停止播放
         */
        void onStopPlay();
    }


    public void setOnNetChangeClickListener(OnNetChangeClickListener onNetChangeClickListener) {
        this.onNetChangeClickListener = onNetChangeClickListener;
    }

    /**
     * 设置重试点击事件
     *
     * @param l 重试的点击事件
     */
    public void setOnRetryClickListener(OnRetryClickListener l) {
        onRetryClickListener = l;
    }

    //错误提示的重试点击事件
    private OnRetryClickListener onRetryClickListener = new OnRetryClickListener() {
        @Override
        public void onRetryClick() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener.onRetryPlay();
            }
        }
    };


    public TipsView(Context context) {
        super(context);
        int padding = ScreenUtils.dp2px(context, 10);
        setPadding(padding, padding, padding, padding);
    }

    public TipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 显示网络变化提示
     */
    public void showNetChangeTipView() {
        if (errorView != null && errorView.getVisibility() == View.VISIBLE) {
            return;
        }
        errorView = new ErrorView(getContext());
        errorView.setText("您当前使用数据流量，是否继续播放？");
        errorView.setPositiveButton("播放", true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNetChangeTipView();
                if (onNetChangeClickListener != null) {
                    onNetChangeClickListener.onContinuePlay();
                }
            }
        });
        errorView.setNegativeButton("取消", true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNetChangeTipView();
                if (onNetChangeClickListener != null) {
                    onNetChangeClickListener.onStopPlay();
                }
            }
        });
        addSubView(errorView);
        isErrorViewAdded = true;
    }

    /**
     * 显示错误提示
     *
     * @param errorCode 错误码
     * @param errorMsg  错误消息
     */
    public void showErrorTipView(int errorCode, int event, String errorMsg) {
        //出现错误了，先把网络的对话框关闭掉。防止同时显示多个对话框。
        //都出错了，还显示网络切换，没有意义
        hideNetChangeTipView();
        mErrorCode = errorCode;
        if (mErrorCode == AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE.getCode()) {
            errorMsg = "播放地址错误";
        }
        errorView = new ErrorView(getContext());
        errorView.setText(errorMsg);
        errorView.setNegativeButton("取消", true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNetChangeTipView();
                if (onNetChangeClickListener != null) {
                    onNetChangeClickListener.onStopPlay();
                }
            }
        });
        errorView.setPositiveButton("重试", true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrorTipView();
                if (onRetryClickListener != null) {
                    onRetryClickListener.onRetryClick();
                }
            }
        });
        addSubView(errorView);
        isErrorViewAdded = true;
    }


    /**
     * 显示缓冲加载view
     */
    public void showBufferLoadingTipView() {
        if (mBufferLoadingView == null) {
            mBufferLoadingView = new LoadingView(getContext());
            addSubView(mBufferLoadingView);
        }
        if (mBufferLoadingView.getVisibility() != VISIBLE) {
            mBufferLoadingView.setVisibility(VISIBLE);
        }
    }

    /**
     * 更新缓冲加载的进度
     *
     * @param percent 进度百分比
     */
    public void updateLoadingPercent(int percent) {
        showBufferLoadingTipView();
        mBufferLoadingView.updateLoadingPercent(percent);
    }


    /**
     * 把新增的view添加进来，居中添加
     *
     * @param subView 子view
     */
    private void addSubView(View subView) {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(subView, params);
    }


    /**
     * 隐藏所有的tip
     */
    public void hideAll() {
        hideNetChangeTipView();
        hideErrorTipView();
        hideBufferLoadingTipView();
    }

    /**
     * 隐藏缓冲加载的tip
     */
    public void hideBufferLoadingTipView() {
        if (mBufferLoadingView != null && mBufferLoadingView.getVisibility() == VISIBLE) {
            mBufferLoadingView.setVisibility(INVISIBLE);
        }
    }


    /**
     * 隐藏网络变化的tip
     */
    public void hideNetChangeTipView() {
        if (errorView != null) {
            removeView(errorView);
            isErrorViewAdded = false;
        }
    }

    /**
     * 隐藏错误的tip
     */
    public void hideErrorTipView() {
        if (errorView != null) {
            removeView(errorView);
            isErrorViewAdded = false;
        }
    }

    /**
     * 错误的tip是否在显示，如果在显示的话，其他的tip就不提示了。
     *
     * @return true：是
     */
    public boolean isErrorShow() {
        if (errorView != null && isErrorViewAdded) {
            return errorView.getVisibility() == View.VISIBLE;
        } else {
            return false;
        }
    }

    /**
     * 隐藏网络错误tip
     */
    public void hideNetErrorTipView() {
        if (errorView != null) {
            removeView(errorView);
        }
    }


    /**
     * 提示view中的点击操作
     */
    public interface OnTipClickListener {
        /**
         * 继续播放
         */
        void onContinuePlay();

        /**
         * 停止播放
         */
        void onStopPlay();

        /**
         * 重试播放
         */
        void onRetryPlay();

        /**
         * 重播
         */
        void onReplay();
    }

    /**
     * 设置提示view中的点击操作 监听
     *
     * @param l 监听事件
     */
    public void setOnTipClickListener(OnTipClickListener l) {
        mOnTipClickListener = l;
    }
}
