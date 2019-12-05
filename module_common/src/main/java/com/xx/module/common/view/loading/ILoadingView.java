package com.xx.module.common.view.loading;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * @Function loading页面接口
 * @Author
 * @Date 2017/3/17
 */
public interface ILoadingView {
    void setEmptyMessage(String message, String btnStr);

    /**
     * 显示正在加载页面
     */
    void loading();

    /**
     * 显示网络错误页面
     */
    void showNoNet();

    /**
     * 显示加载失败
     */
    void showFailed();

    void showFailed(String msg);

    /**
     * 显示当前页数据空
     */
    void showEmpty();

    void showEmpty(String msg);

    void showEmpty(Drawable drawable, String message);

    void dismiss();

    View getLayoutView();

    /**
     * 当前是否显示正在加载
     *
     * @return
     */
    boolean isLoading();

    /**
     * 是否加载了空数据
     *
     * @return
     */
    boolean isLoadingEmpty();

    /**
     * 加载出错后点击重新加载
     *
     * @param listener
     */
    void setOnRefreshClickListener(View.OnClickListener listener);

    /**
     * 加载数据空的时候显示按钮并添加事件
     *
     * @param listener
     */
    void setOnEmptyClickListener(View.OnClickListener listener);

    /**
     * loading页面还没消失
     */
    boolean isLoadingViewShow();

    /**
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    void setMargin(int left, int top, int right, int bottom);

    void post(Runnable function);
}
