package com.xx.module.common.view.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.ViewGroup;

import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.presenter.BasePresenter;
import com.xx.module.common.view.contract.IBaseView;
import com.xx.module.common.view.loading.ILoadingView;
import com.xx.module.common.view.refresh.IRefreshLayout;
import com.xx.module.common.view.refresh.RefreshLayoutDelegate;

import in.srain.cube.views.ptr.PtrFrameLayout;


public abstract class BaseActivity<T extends BasePresenter> extends SmActivity implements IBaseView {


    protected UserSystem mUserSystem;

    private T mPresenter;

    /**
     * 基础接口统一实现类
     */
    private ViewDelegate mIBaseViewHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIBaseViewHandler = onCreateViewAgency();
        mPresenter = getPresenter();
        if (mPresenter != null) {
            mPresenter.bindView(this);
        }
        super.onCreate(savedInstanceState);
    }

    private ViewDelegate onCreateViewAgency() {
        if (mIBaseViewHandler == null) {
            mIBaseViewHandler = new ViewDelegate();
        }
        return mIBaseViewHandler;
    }


    public void setPtrFrameLayout(PtrFrameLayout frameLayout, AppBarLayout appBarLayout, int backgroundColorRes, Runnable runnable) {
        IRefreshLayout refreshLayout = new RefreshLayoutDelegate(frameLayout, runnable);
        setRefreshLayout(refreshLayout, appBarLayout, backgroundColorRes);
        mIBaseViewHandler.setRefreshLayout(refreshLayout);
    }

    public T getPresenter() {
        if (mPresenter == null) {
            mPresenter = mIBaseViewHandler.getPresenter(AnnotationProcessor.getPresenterAnnotation(this.getClass()));
        }
        return mPresenter;
    }

    @Override
    public void showNoNetWork() {
        mIBaseViewHandler.showNoNetWork();
    }

    @Override
    public void onLoadDataCompleted() {
        mIBaseViewHandler.onLoadDataCompleted();
    }

    @Override
    public void onLoadDataFailed(Throwable msg) {
        mIBaseViewHandler.onLoadDataFailed(msg);
    }

    @Override
    public boolean onLoadCacheSuccess() {
        if (isFinishing()) {
            return false;
        } else {
            mIBaseViewHandler.onLoadCacheSuccess();
            return true;
        }
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
    public void initLoadingView(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        mIBaseViewHandler.setLoadingView(viewGroup, null);
        if (mIBaseViewHandler.getLoadingViewManager() != null) {
            mIBaseViewHandler.getLoadingViewManager().loading();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserDataManager.getInstance().getUserSystem(new UserDataManager.SimpleOnGetUserDataListener() {
            @Override
            public void onSuccess(@NonNull UserSystem us) {
                super.onSuccess(us);
                mUserSystem = us;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIBaseViewHandler != null) {
            mIBaseViewHandler.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unBindView();
        }
    }


}

