package com.xx.module.common.view.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.presenter.BasePresenter;
import com.xx.module.common.view.contract.IBaseView;
import com.xx.module.common.view.loading.ILoadingView;
import com.xx.module.common.view.refresh.IRefreshLayout;
import com.xx.module.common.view.refresh.RefreshLayoutDelegate;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * 可以在布局中include loading_layout 这个布局，就可以使用基类中的showLoadingViewLoading()等加载时的显示操作
 * Created by fhs on 2016-11-25.
 */
public abstract class BaseFragment<T extends BasePresenter> extends SmFragment implements IBaseView {
    private T mPresenter;

    /**
     * 基础接口统一实现类
     */
    private ViewDelegate mIBaseViewHandler;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIBaseViewHandler = onCreateViewAgency();
        mPresenter = getPresenter();
        if (mPresenter != null) {
            mPresenter.bindView(this);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public T getPresenter() {
        if (mPresenter == null) {
            mPresenter = mIBaseViewHandler.getPresenter(AnnotationProcessor.getPresenterAnnotation(this.getClass()));
        }
        return mPresenter;
    }

    private ViewDelegate onCreateViewAgency() {
        if (mIBaseViewHandler == null) {
            mIBaseViewHandler = new ViewDelegate();
        }
        return mIBaseViewHandler;
    }

    public void setPtrFragment(PtrFrameLayout refreshLayout, AppBarLayout appbar, Runnable runnable) {
        initPtrFrameLayout(new RefreshLayoutDelegate(refreshLayout, runnable), appbar);
    }

    public void initPtrFrameLayout(IRefreshLayout frameLayout, AppBarLayout appBarLayout) {
        frameLayout.setAppbarLayout(appBarLayout);
        mIBaseViewHandler.setRefreshLayout(frameLayout);
    }

    @Override
    public void showNoNetWork() {
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.showNoNetWork();
        }
    }

    @Override
    public void onLoadDataCompleted() {
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onLoadDataCompleted();
        }
    }

    @Override
    public void onLoadDataFailed(Throwable msg) {
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onLoadDataFailed(msg);
        }
    }

    @Override
    public boolean onLoadCacheSuccess() {
        if (mIBaseViewHandler != null) {
            return mIBaseViewHandler.onLoadCacheSuccess();
        }
        return false;
    }

    @Override
    protected void initLoadingView(ViewGroup rootView) {
        if (rootView == null) {
            return;
        }
        mIBaseViewHandler.setLoadingView(rootView, null);
    }

    /**
     * @param loadingViewManager 控制器对应的布局，建议根据需求添加到相应的布局XML中，否则将会自动添加到根布局中
     */
    public void setloadingManager(ILoadingView loadingViewManager) {
        mIBaseViewHandler.setLoadingManager(rootView, loadingViewManager);
    }

    /**
     * 获得LoadingView操作对象
     *
     * @return
     */
    @Nullable
    public ILoadingView getLoadingViewManager() {
        return mIBaseViewHandler.getLoadingViewManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onStop();
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

    }
}
