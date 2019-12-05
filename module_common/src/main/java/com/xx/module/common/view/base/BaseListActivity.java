package com.xx.module.common.view.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.xx.multiadapter.MultilItemAdapter;
import com.xx.multiadapter.RvComboAdapter;
import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.presenter.BaseListPresenter;
import com.xx.module.common.view.contract.IBaseListView;
import com.xx.module.common.view.contract.OnCreateListAdapterListener;
import com.xx.module.common.view.loading.AutoLoadMoreScrollListener;
import com.xx.module.common.view.loading.IBaseAdapter;
import com.xx.module.common.view.loading.ILoadingView;
import com.xx.module.common.view.refresh.IRefreshLayout;
import com.xx.module.common.view.refresh.RefreshLayoutDelegate;

import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by fhs on 2016-08-10.
 */
public abstract class BaseListActivity<T extends BaseListPresenter> extends SmActivity implements IBaseListView {

    private RecyclerView mRecyclerView;
    private ListViewDelegate iBaseListViewDelegate;
    private T mPresenter;

    /**
     * 默认的起始第一页
     *
     * @param pageOne
     */
    public void setDefaultPageOne(int pageOne) {
        iBaseListViewDelegate.setStartingPageNum(pageOne);
    }

    public int getDefaultPageOne() {
        return iBaseListViewDelegate.getStartingPageNum();
    }

    public int getNowPage() {
        return iBaseListViewDelegate.getNowPage();
    }

    public void setNowPage(int page) {
        iBaseListViewDelegate.setNowPage(page);
    }

    public AutoLoadMoreScrollListener getOnScrollListener() {
        return iBaseListViewDelegate.getLoadMoreScrollListener();
    }


    public void setPtrFrameLayout(PtrFrameLayout ptrFrameLayout) {
        setPtrFrameLayout(ptrFrameLayout, 0);
    }

    /**
     * 第三方下拉刷新
     *
     * @param frameLayout
     */
    public void setPtrFrameLayout(PtrFrameLayout frameLayout, int backgroundColorRes) {
        setPtrFrameLayout(frameLayout, null, backgroundColorRes);
    }

    /**
     * 第三方下拉刷新
     *
     * @param frameLayout
     */
    public void setPtrFrameLayout(PtrFrameLayout frameLayout, AppBarLayout appBarLayout, int backgroundColorRes) {
        IRefreshLayout refreshLayout = new RefreshLayoutDelegate(frameLayout, new Runnable() {
            @Override
            public void run() {
                startRefresh();
            }
        });
        refreshLayout.setAppbarLayout(appBarLayout);
        if (backgroundColorRes != 0) {
            refreshLayout.setBackgroundColorRes(backgroundColorRes);
        }
        iBaseListViewDelegate.setRefreshLayout(refreshLayout);
        setRefreshLayout(refreshLayout, appBarLayout, backgroundColorRes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        iBaseListViewDelegate = onCreateViewAgency();
        mPresenter = getPresenter();
        if (mPresenter != null) {
            mPresenter.bindView(this);
        }
        super.onCreate(savedInstanceState);
    }

    private ListViewDelegate onCreateViewAgency() {
        if (iBaseListViewDelegate == null) {
            iBaseListViewDelegate = new ListViewDelegate(this);
        }
        return iBaseListViewDelegate;
    }

    public T getPresenter() {
        if (mPresenter == null) {
            mPresenter = iBaseListViewDelegate.getPresenter(AnnotationProcessor.getPresenterAnnotation(this.getClass()));
        }
        return mPresenter;
    }

    private ListViewDelegate.OnReadyLoadDataCallback onReadyLoadDataCallback = new ListViewDelegate.OnReadyLoadDataCallback() {
        @Override
        public void onLoadData(int page) {
            loadData(page);
        }
    };

    protected abstract void loadData(int page);

    protected abstract RecyclerView getRecyclerView();


    public IBaseAdapter getAdapter() {
        return iBaseListViewDelegate.getAdapter();
    }

    public void setupRecyclerView(ListViewDelegate.RecyclerViewConfig config) {
        if (mRecyclerView == null) {
            mRecyclerView = getRecyclerView();
        }
        iBaseListViewDelegate.setupRecyclerView(mRecyclerView, config, onReadyLoadDataCallback);
    }

    public void setupAdapters(List<MultilItemAdapter> adapters) {
        if (mRecyclerView == null) {
            return;
        }
        RvComboAdapter mainAdapter = mRecyclerView.getAdapter() instanceof RvComboAdapter ? (RvComboAdapter) mRecyclerView.getAdapter() : null;
        if (mainAdapter == null) {
            setupRecyclerView(null);
            setupAdapters(adapters);
        } else {
            mainAdapter.setMultiItems(adapters);
        }
    }

    public boolean isLoadDateComplete() {
        return iBaseListViewDelegate.isLoadDateComplete();
    }

    /**
     * 在使用列表之前，请执行初始化操作
     *
     * @param listener
     */
    public void initRecyclerViewOrListView(OnCreateListAdapterListener listener) {
        if (mRecyclerView == null) {
            mRecyclerView = getRecyclerView();
        }
        iBaseListViewDelegate.initRecyclerViewOrListView(mRecyclerView, listener, onReadyLoadDataCallback);
    }

    public void releaseAdapter() {
        iBaseListViewDelegate.releaseAdapter();
    }

    /**
     * 外部来调用刷新。
     * 会执行{@link PtrFrameLayout#autoRefresh()}
     */
    public void refreshDataByLayout() {
        if (iBaseListViewDelegate.getRefreshLayout() != null) {
            iBaseListViewDelegate.autoRefresh();
        } else {
            startRefresh();
        }
    }

    public void refreshDataByLayout(boolean showAnim) {
        if (!showAnim) {
            startRefresh();
        } else {
            iBaseListViewDelegate.autoRefresh();
        }
    }

    public void startRefresh() {
        iBaseListViewDelegate.refreshData(onReadyLoadDataCallback);
    }


    @Override
    public void onGetDataListFailed(Throwable e) {
        iBaseListViewDelegate.onGetDataListFailed(e);
    }

    @Override
    public void showNoNetWork() {
        iBaseListViewDelegate.showNoNetWork();
    }

    @Override
    public void onLoadDataCompleted() {
        iBaseListViewDelegate.onLoadDataCompleted();
    }

    @Override
    public void onLoadDataFailed(Throwable e) {
        iBaseListViewDelegate.onLoadDataFailed(e);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (iBaseListViewDelegate != null) {
            iBaseListViewDelegate.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (iBaseListViewDelegate != null) {
            iBaseListViewDelegate.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unBindView();
        }

    }

    @Override
    public boolean onLoadCacheSuccess() {
        if (isFinishing()) {
            return false;
        } else {
            iBaseListViewDelegate.onLoadCacheSuccess();
            return true;
        }
    }


    @Override
    public void initLoadingView(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        iBaseListViewDelegate.setLoadingView(viewGroup, null);
        iBaseListViewDelegate.setDefaultButtonClickListener(onReadyLoadDataCallback);
        if (getLoadingViewManager() != null) {
            getLoadingViewManager().setOnRefreshClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshDataByLayout();
                    if (getLoadingViewManager() != null) {
                        getLoadingViewManager().loading();
                    }
                }
            });
            getLoadingViewManager().loading();
        }
    }

    @Nullable
    public ILoadingView getLoadingViewManager() {
        return iBaseListViewDelegate.getLoadingViewManager();
    }

}
