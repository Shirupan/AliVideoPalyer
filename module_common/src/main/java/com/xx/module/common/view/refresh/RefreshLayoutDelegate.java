package com.xx.module.common.view.refresh;

import android.support.annotation.ColorRes;
import android.support.design.widget.AppBarLayout;
import android.view.View;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;


/**
 * 下拉刷新统一代理
 *
 * @author Administrator
 */
public class RefreshLayoutDelegate implements IRefreshLayout {
    private PtrFrameLayout mRefreshLayout;
    private AppBarLayout mAppBarLayout;
    private Runnable refresh;

    public RefreshLayoutDelegate(PtrFrameLayout layout, Runnable refresh) {
        mRefreshLayout = layout;
        this.refresh = refresh;
        init();
    }

    private void init() {
        if (mRefreshLayout == null) {
            return;
        }
        //防止内部有自动滑动的部件时候，会自动滑动到对应的控件
        mRefreshLayout.requestFocus();
        PtrUIHandler handler = null;
        View header = mRefreshLayout.getHeaderView();
        if (header == null) {
            //设置头布局，以及头部动画
            header = new PtrSmLoadingHeader(mRefreshLayout.getContext());
            mRefreshLayout.setHeaderView(header);

        }
        if (header instanceof PtrUIHandler) {
            handler = (PtrUIHandler) header;
        }
        mRefreshLayout.addPtrUIHandler(handler);

        //阻尼系数
        mRefreshLayout.setResistance(3.0f);
        //触发刷新时移动的位置比例
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(1.2f);
        mRefreshLayout.setDurationToClose(200);
        mRefreshLayout.setDurationToCloseHeader(600);
        // default is false
        mRefreshLayout.setPullToRefresh(false);
        // default is true
        mRefreshLayout.setKeepHeaderWhenRefresh(true);
        //下拉刷新事件
        //具体下拉刷新事件控制在这里
        mRefreshLayout.setPtrHandler(new SmPtrHandler(refresh));
        mRefreshLayout.disableWhenHorizontalMove(true);

    }

    @Override
    public void setBackgroundColorRes(@ColorRes int backgroundColorRes) {
        if (backgroundColorRes != 0) {
            mRefreshLayout.setBackgroundResource(backgroundColorRes);
            mRefreshLayout.getHeaderView().setBackgroundResource(backgroundColorRes);
        }
    }

    @Override
    public void autoRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.autoRefresh();
        }
    }

    @Override
    public void setEnable(boolean enable) {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(enable);
        }
    }

    @Override
    public void setAppbarLayout(AppBarLayout appbarLayout) {
        if (mAppBarLayout != null && onOffsetChangedListener != null) {
            mAppBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener);
            onOffsetChangedListener = null;
        }
        if (appbarLayout != null) {
            mAppBarLayout = appbarLayout;
            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        //页面手势上划
                        mRefreshLayout.setEnabled(verticalOffset >= 0);
                    }
                };
            }
            mAppBarLayout.addOnOffsetChangedListener(onOffsetChangedListener);
        }
    }

    @Override
    public void unBind() {
        if (mAppBarLayout != null && onOffsetChangedListener != null) {
            mAppBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener);
            onOffsetChangedListener = null;
        }
    }

    @Override
    public void onBind() {
        setAppbarLayout(mAppBarLayout);
    }

    private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;

    @Override
    public boolean isRefreshing() {
        if (mRefreshLayout != null) {
            return mRefreshLayout.isRefreshing();
        }
        return false;
    }

    @Override
    public void refreshComplete() {
        if (mRefreshLayout != null) {
            mRefreshLayout.refreshComplete();
        }
    }
}
