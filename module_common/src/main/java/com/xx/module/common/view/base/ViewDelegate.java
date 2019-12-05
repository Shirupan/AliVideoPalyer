package com.xx.module.common.view.base;


import android.os.Build;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mrkj.lib.common.util.ScreenUtils;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.lib.db.exception.SmCacheException;
import com.xx.module.common.R;
import com.xx.module.common.presenter.BasePresenter;
import com.xx.module.common.presenter.PresenterManager;
import com.xx.module.common.view.contract.IBaseView;
import com.xx.module.common.view.loading.DefaultLoadingViewManager;
import com.xx.module.common.view.loading.ILoadingView;
import com.xx.module.common.view.refresh.IRefreshLayout;

/**
 * @author
 * @Function 该类功能：各Activity或Fragment公共实现部分，统一初始化mvp基本接口
 * @Date 2017/5/17.
 */

public class ViewDelegate implements IBaseView {
    protected ILoadingView iLoadingView;
    protected int nowPage;
    protected IRefreshLayout ptrFrameLayout;


    public ViewDelegate() {
    }

    public void setNowPage(int nowPage) {
        this.nowPage = nowPage;
    }


    public void setRefreshLayout(IRefreshLayout ptrFrameLayout) {
        this.ptrFrameLayout = ptrFrameLayout;
    }

    public IRefreshLayout getRefreshLayout() {
        return ptrFrameLayout;
    }

    public void onStart() {
        if (ptrFrameLayout != null) {
            ptrFrameLayout.onBind();
        }
    }

    public void onStop() {
        if (ptrFrameLayout != null) {
            ptrFrameLayout.unBind();
        }
    }

    @Override
    public void showNoNetWork() {
        //  NotNetworkReceiver.sendBroadCast(SmBaseApplication.getBaseContext());
        if (iLoadingView != null && iLoadingView.isLoadingViewShow()) {
            iLoadingView.showNoNet();
        }
    }

    @Override
    public void onLoadDataCompleted() {
        if (ptrFrameLayout != null && ptrFrameLayout.isRefreshing()) {
            ptrFrameLayout.refreshComplete();
        }
    }


    /**
     * {@link  IBaseView#onLoadDataFailed(Throwable)}
     *
     * @param msg
     */

    @Override
    public void onLoadDataFailed(Throwable msg) {
        if (msg instanceof SmCacheException) {
            return;
        }
        if (iLoadingView != null && iLoadingView.isLoadingViewShow()) {
            String message = ExceptionUtl.catchTheError(msg);
            if (!TextUtils.isEmpty(message) && message.contains("没有")) {
                iLoadingView.showEmpty();
            } else {
                iLoadingView.showFailed(message);
            }
        }
        if (ptrFrameLayout != null && ptrFrameLayout.isRefreshing()) {
            ptrFrameLayout.refreshComplete();
        }
    }


    @Override
    public boolean onLoadCacheSuccess() {
        if (iLoadingView != null) {
            iLoadingView.dismiss();
            return true;
        } else {
            return false;
        }
    }

    public void setOnLoadingViewButtonClickListener(View.OnClickListener listener) {
        if (getLoadingViewManager() != null) {
            getLoadingViewManager().setOnRefreshClickListener(listener);
        }
    }

    private BasePresenter mPresenter;

    /**
     * @param clazz
     * @param <T>   返回指定类型的Presenter实例
     * @return
     */
    public <T> T getPresenter(Class<?> clazz) {
        if (mPresenter == null) {
            mPresenter = instancePresenter(clazz);
        }
        return (T) mPresenter;
    }

    private <T> T instancePresenter(Class<?> clazz) {
        if (clazz != null) {
            return (T) PresenterManager.getInstance().getPresenter(clazz);
        } else {
            SmLogger.i("Not a presenter annotation is set here");
            return null;
        }
    }

    /**
     * 填充statusBar Padding
     *
     * @param view
     */
    public void setStatusBarPadding(View view) {
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setPadding(0, ScreenUtils.getStatusBarHeight(view.getContext()), 0, 0);
            }
        }
    }

    public void setStatusBarPadding(View rootView, int id) {
        if (rootView == null) {
            return;
        }
        View view = rootView.findViewById(id);
        setStatusBarPadding(view);
    }

    public void setLoadingManager(View rootView, ILoadingView loadingViewManager) {
        this.iLoadingView = loadingViewManager;
        if (loadingViewManager != null) {
            View view = loadingViewManager.getLayoutView();
            if (view == null) {
                return;
            }
            ViewGroup group = (ViewGroup) rootView;
            int count = group.getChildCount();
            //是否布局中已经包含了loadingview布局
            boolean iscontains = false;
            for (int i = 0; i < count; i++) {
                if (view == group.getChildAt(i)) {
                    iscontains = true;
                    break;
                }
            }
            if (!iscontains) {
                //如果没有添加，就主动添加到根布局中
                ((ViewGroup) rootView.getRootView()).addView(view);
            }
        }
    }

    @Nullable
    public ILoadingView getLoadingViewManager() {
        return iLoadingView;
    }

    /**
     * @param parent   指定的Loading的View
     * @param listener 刷新按钮的动作
     */
    public void setLoadingView(ViewGroup parent, View.OnClickListener listener) {
        FrameLayout loadingRoot;
        if (parent instanceof FrameLayout) {
            loadingRoot = (FrameLayout) parent;
        } else {
            loadingRoot = new FrameLayout(parent.getContext());
            if (parent instanceof RelativeLayout) {
                loadingRoot.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                parent.addView(loadingRoot);
            } else if (parent instanceof LinearLayout) {
                ViewGroup viewGroup = (ViewGroup) parent.getParent();
                if (viewGroup == null) {
                    throw new NullPointerException("Loading view can not attach to LinearLayout without parent.");
                }
                loadingRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                int index = viewGroup.indexOfChild(parent);
                viewGroup.removeView(parent);
                loadingRoot.addView(parent);
                viewGroup.addView(loadingRoot, index);
            } else if (parent instanceof ConstraintLayout) {
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, 0);
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
                loadingRoot.setLayoutParams(lp);
                parent.addView(loadingRoot);
            } else if (parent instanceof CoordinatorLayout) {
                CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                loadingRoot.setLayoutParams(lp);
                parent.addView(loadingRoot);
            }
        }
        iLoadingView = new DefaultLoadingViewManager(loadingRoot);
        if (iLoadingView.getLayoutView() == null) {
            SmLogger.d("LoadingLayout: loadLayout is null. ");
            return;
        }
        SmLogger.d("LoadingLayout: loadLayoutManager is Builed. ");
        final View toolbar = parent.findViewById(R.id.sm_toolbar_layout);
        if (toolbar != null && getLoadingViewManager() != null) {
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    getLoadingViewManager().setMargin(0, toolbar.getBottom(), 0, 0);
                }
            });
        }
        iLoadingView.setOnRefreshClickListener(listener);
    }
}
