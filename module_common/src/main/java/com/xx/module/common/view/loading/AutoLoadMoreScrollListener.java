package com.xx.module.common.view.loading;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.imageload.ImageLoader;

/**
 * 上拉自动加载更多。加载完成后调用setLoadingData(false)来关闭正在加载状态
 *
 * @author
 * @date 2016/11/10
 */

public abstract class AutoLoadMoreScrollListener extends RecyclerView.OnScrollListener {
    private boolean isLoadingData;

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private LinearLayoutManager layoutManager;
    /**
     * 分页已经全部加载
     */
    private boolean dataHasBeanLoadOver;

    public AutoLoadMoreScrollListener(@NonNull Context a) {
        mContext = a;
    }

    public AutoLoadMoreScrollListener(@NonNull Activity a) {
        activity = a;
    }

    public AutoLoadMoreScrollListener(@NonNull Fragment a) {
        fragment = a;
    }

    private void checkAndLoadMore(RecyclerView recyclerView) {
        if (layoutManager == null ) {
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
           if (manager instanceof LinearLayoutManager) {
                layoutManager = (LinearLayoutManager) manager;
            }
        }
        //正在加载中或者已经没有更多数据，就不做处理
        if (isLoadingData || dataHasBeanLoadOver) {
            return;
        }
        int lastCompletelyVisibleItem = 0;
        int totalItemCount = 0;
        if (layoutManager != null) {
            lastCompletelyVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            totalItemCount = layoutManager.getItemCount();
        }
        if (!isLoadingData && lastCompletelyVisibleItem >= totalItemCount - 2) {
            isLoadingData = true;
            loadMoreData();
        }
    }

    private boolean isImageLoadPause;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            //无滑动
            case RecyclerView.SCROLL_STATE_IDLE:
                if (activity != null && !activity.isFinishing()) {
                    ImageLoader.getInstance().resume(SmContextWrap.obtain(activity));
                    isImageLoadPause = false;
                } else if (fragment != null) {
                    ImageLoader.getInstance().resume(SmContextWrap.obtain(fragment));
                    isImageLoadPause = false;
                }
                checkAndLoadMore(recyclerView);
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (activity != null) {
                    ImageLoader.getInstance().pause(SmContextWrap.obtain(activity));
                    isImageLoadPause = true;
                } else if (fragment != null) {
                    ImageLoader.getInstance().pause(SmContextWrap.obtain(fragment));
                    isImageLoadPause = true;
                }
                break;

            default:
        }

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (Math.abs(dy) < 100 && isImageLoadPause) {
            if (activity != null) {
                isImageLoadPause = false;
                ImageLoader.getInstance().resume(SmContextWrap.obtain(activity));
            } else if (fragment != null) {
                isImageLoadPause = false;
                ImageLoader.getInstance().resume(SmContextWrap.obtain(fragment));
            }
        }
    }

    public void setLoadingData(boolean loadingData) {
        isLoadingData = loadingData;
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    public abstract void loadMoreData();

    /**
     * 有时候接口是详情+列表项一起返回的时候，需要主动判断时候已经加载完毕
     *
     * @param dataHasBeanLoadOver
     */
    public void setDataHasBeanLoadOver(boolean dataHasBeanLoadOver) {
        this.dataHasBeanLoadOver = dataHasBeanLoadOver;
    }

    public boolean isDataHasBeanLoadOver() {
        return dataHasBeanLoadOver;
    }
}
