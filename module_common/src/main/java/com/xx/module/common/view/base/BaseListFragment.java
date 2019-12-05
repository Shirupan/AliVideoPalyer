package com.xx.module.common.view.base;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
 * 有使用RecyclerView的Fragment的基类
 * 几个重要的方法：
 * 1.setSwipeRefreshLayout(),如果布局中有使用到SwipeRefreshLayout，请使用该方法传入控件
 * 2.initRecyclerViewOrListView()。请在向RecyclerView中装配数据前调用该方法
 * 3.loadData(int page); 加载数据，请在该方法内完成
 * 4.initAdapter();  只需实例化Adapter.常规初始化会自己完成.(item的选择监听需要自己完成)
 * 5.onGetDataListFailed().  数据加载失败情况下，请调用该方法，做回滚处理
 * 6.onLoadDataCompleted().  数据加载完成，需要主动调用该方法
 * Created by fhs on 2016-08-10.
 */
public abstract class BaseListFragment<T extends BaseListPresenter> extends SmFragment implements IBaseListView {
    private RecyclerView mRecyclerView;
    private ListViewDelegate iBaseListViewDelegate;
    private T mPresenter;

    /**
     * 默认的起始第一页
     *
     * @param pageNum
     */
    public void setStartingPageNum(int pageNum) {
        iBaseListViewDelegate.setStartingPageNum(pageNum);
    }

    public int getStartingPageNum() {
        return iBaseListViewDelegate.getStartingPageNum();
    }


    public int getNowPage() {
        return iBaseListViewDelegate.getNowPage();
    }

    public void setNowPage(int page) {
        iBaseListViewDelegate.setNowPage(page);
    }

    private ListViewDelegate.OnReadyLoadDataCallback onReadyLoadDataCallback = new ListViewDelegate.OnReadyLoadDataCallback() {
        @Override
        public void onLoadData(int page) {
            loadData(page);
            if (iBaseListViewDelegate.ptrFrameLayout != null) {
                iBaseListViewDelegate.ptrFrameLayout.setEnable(false);
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        iBaseListViewDelegate = onCreateBaseAgency();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPresenter = getPresenter();
        if (mPresenter != null) {
            mPresenter.bindView(this);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public T getPresenter() {
        if (mPresenter == null) {
            mPresenter = iBaseListViewDelegate.getPresenter(AnnotationProcessor.getPresenterAnnotation(this.getClass()));
        }
        return mPresenter;
    }

    protected ListViewDelegate onCreateBaseAgency() {
        if (iBaseListViewDelegate == null) {
            iBaseListViewDelegate = new ListViewDelegate(this);
        }
        return iBaseListViewDelegate;
    }


    public void setPtrFrameLayout(PtrFrameLayout ptrFrameLayout) {
        setPtrFrameLayout(ptrFrameLayout, 0);
    }

    public AutoLoadMoreScrollListener getLoadMoreScrollListener() {
        return iBaseListViewDelegate.getLoadMoreScrollListener();
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
        IRefreshLayout delegate = new RefreshLayoutDelegate(frameLayout, new Runnable() {
            @Override
            public void run() {
                startRefresh();
            }
        });
        delegate.setAppbarLayout(appBarLayout);
        delegate.setBackgroundColorRes(backgroundColorRes);
        iBaseListViewDelegate.setRefreshLayout(delegate);
    }

    /**
     * 外部来调用刷新。
     * 会执行{@link PtrFrameLayout#autoRefresh()}
     */
    public void refreshData() {
        refreshData(true);
    }

    public void refreshData(boolean showAnim) {
        if (!showAnim || iBaseListViewDelegate.getRefreshLayout() == null) {
            startRefresh();
            if (iBaseListViewDelegate.ptrFrameLayout != null) {
                iBaseListViewDelegate.ptrFrameLayout.setEnable(false);
            }
        } else {
            iBaseListViewDelegate.autoRefresh();
        }
    }

    private void startRefresh() {
        iBaseListViewDelegate.refreshData(onReadyLoadDataCallback);
    }

    public RvComboAdapter setupRecyclerView(ListViewDelegate.RecyclerViewConfig config) {
        if (mRecyclerView == null) {
            mRecyclerView = getRecyclerView();
        }
        return iBaseListViewDelegate.setupRecyclerView(mRecyclerView, config, onReadyLoadDataCallback);
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

    @Override
    protected void initLoadingView(ViewGroup parent) {
        if (parent == null) {
            return;
        }
        iBaseListViewDelegate.setLoadingView(parent, null);
        iBaseListViewDelegate.setDefaultButtonClickListener(onReadyLoadDataCallback);
        if (getLoadingViewManager() != null) {
            getLoadingViewManager().setOnRefreshClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRefresh();
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

    /**
     * 传入RecyclerView实例
     *
     * @return
     */
    public abstract RecyclerView getRecyclerView();


    public IBaseAdapter getAdapter() {
        return iBaseListViewDelegate.getAdapter();
    }

    /**
     * 在使用RecyclerView前，请在【初始化布局】之后，调用此初始化方法
     */
    protected void initRecyclerViewOrListView(OnCreateListAdapterListener listener) {
        if (mRecyclerView == null) {
            mRecyclerView = getRecyclerView();
        }
        iBaseListViewDelegate.initRecyclerViewOrListView(mRecyclerView, listener, onReadyLoadDataCallback);
    }

    protected abstract void loadData(int page);


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
        if (iBaseListViewDelegate.ptrFrameLayout != null) {
            iBaseListViewDelegate.ptrFrameLayout.setEnable(true);
        }
    }

    @Override
    public void onLoadDataFailed(Throwable e) {
        iBaseListViewDelegate.onLoadDataFailed(e);
        if (iBaseListViewDelegate.ptrFrameLayout != null) {
            iBaseListViewDelegate.ptrFrameLayout.setEnable(true);
        }
    }

    @Override
    public boolean onLoadCacheSuccess() {
        if (getActivity() != null && getActivity().isFinishing()) {
            return false;
        } else {
            if (iBaseListViewDelegate != null) {
                iBaseListViewDelegate.onLoadCacheSuccess();
            }
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (iBaseListViewDelegate != null) {
            iBaseListViewDelegate.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (iBaseListViewDelegate != null) {
            iBaseListViewDelegate.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.unBindView();
        }
    }

    @Override
    protected void onUserVisible() {
        if (getLoadingViewManager() != null && !getLoadingViewManager().isLoadingViewShow()) {
            getLoadingViewManager().dismiss();
        }
    }
}
