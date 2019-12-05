package com.xx.module.common.view.base;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.xx.multiadapter.BaseFooterAdapter;
import com.xx.multiadapter.BaseLoadingAdapter;
import com.xx.multiadapter.RecyclerViewAdapterFactory;
import com.xx.multiadapter.RvComboAdapter;
import com.xx.multiadapter.ToolAdapter;
import com.mrkj.lib.common.util.ScreenUtils;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.module.common.R;
import com.xx.module.common.presenter.BaseListPresenter;
import com.xx.module.common.presenter.PresenterManager;
import com.xx.module.common.view.contract.IBaseListView;
import com.xx.module.common.view.contract.OnCreateListAdapterListener;
import com.xx.module.common.view.loading.AutoLoadMoreScrollListener;
import com.xx.module.common.view.loading.BaseRVAdapter;
import com.xx.module.common.view.loading.IBaseAdapter;
import com.xx.module.common.view.loading.ILoadingView;
import com.xx.module.common.view.refresh.IRefreshLayout;

import java.net.ConnectException;

/**
 * @author
 * @Function 该类功能：各Activity或Fragment公共实现部分，统一初始化mvp基本接口
 * @Date 2017/5/17.
 */

public class ListViewDelegate extends ViewDelegate implements IBaseListView, Handler.Callback {
    private static final int NO_DATA_PAGE_ONE = 1;
    private static final int NO_DATA_PAGE_MORE = 2;
    private static final int INFO_FOR_FOOTER = 3;
    private static final int ERROR_NET_WORK = 4;
    private static final int LOADING = 5;

    private IBaseAdapter mAdapter;
    private int nowPage;
    private int startingPageNum = 0;
    private AutoLoadMoreScrollListener onScrollListener;
    private Context mContext;
    private SmActivity activity;
    private BaseListFragment fragment;
    private RecyclerView listRv;
    private Handler mHandler = new Handler(this);


    public ListViewDelegate(SmActivity activity) {
        this.activity = activity;
        this.mContext = activity;
    }

    public ListViewDelegate(BaseListFragment fragment) {
        this.fragment = fragment;
        this.mContext = fragment.getContext();
    }

    @Override
    public void setNowPage(int nowPage) {
        this.nowPage = nowPage;
    }

    /**
     * BaseListViewXXX的基本依赖对象.用于在通过{@link IBaseListView#onGetDataListFailed(Throwable)}等方法中的相关处理
     *
     * @param onScrollListener RecyclerView上拉加载更多监听
     * @param adapter          RecyclerView的Adapter
     */
    public void setListViewDependency(AutoLoadMoreScrollListener onScrollListener, IBaseAdapter adapter) {
        this.mAdapter = adapter;
        this.onScrollListener = onScrollListener;
    }

    @Override
    public void setRefreshLayout(IRefreshLayout ptrFrameLayout) {
        this.ptrFrameLayout = ptrFrameLayout;
    }


    /**
     * {@link IBaseListView#onGetDataListFailed(Throwable)}
     *
     * @param e
     */
    @Override
    public void onGetDataListFailed(Throwable e) {
        String message = ExceptionUtl.catchTheError(e);
        if (nowPage <= startingPageNum && e instanceof ReturnJsonCodeException) {
            Message msg = Message.obtain();
            msg.what = NO_DATA_PAGE_ONE;
            if (!TextUtils.isEmpty(message) && !message.contains("没有")) {
                msg.obj = message;
            }
            mHandler.sendEmptyMessage(NO_DATA_PAGE_ONE);
        } else {
            //网络错误
            if (e instanceof ConnectException) {
                mHandler.sendEmptyMessage(ERROR_NET_WORK);
            } else {
                if (!TextUtils.isEmpty(message) && message.contains("没有")) {
                    mHandler.sendEmptyMessage(NO_DATA_PAGE_MORE);
                } else {  //其他错误
                    mHandler.sendEmptyMessage(INFO_FOR_FOOTER);
                }
            }
        }

        if (nowPage > startingPageNum) {
            --nowPage;
        }
    }

    @Override
    public void showNoNetWork() {
        if (mLayoutAdapter != null) {
            mLayoutAdapter.notifyLoadingStateChanged(mContext.getString(R.string.sm_error_network), ToolAdapter.ERROR);
        }
        if (mAdapter != null) {
            mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NET_ERROR);
        }
        if (iLoadingView != null && iLoadingView.isLoadingViewShow()) {
            iLoadingView.showNoNet();
        } else if (iLoadingView != null) {
            iLoadingView.dismiss();
        }

        if (onScrollListener != null) {
            onScrollListener.setDataHasBeanLoadOver(false);
        }
    }

    @Override
    public void onLoadDataCompleted() {
        if (ptrFrameLayout != null && ptrFrameLayout.isRefreshing()) {
            ptrFrameLayout.refreshComplete();
        }

        if (onScrollListener != null) {
            onScrollListener.setLoadingData(false);
        }
        if ((iLoadingView != null && iLoadingView.isLoadingViewShow()) && isAdapterHasData()) {
            iLoadingView.dismiss();
        }
        setupListFooter();
        if (nowPage == getStartingPageNum() && listRv != null && listRv.getAdapter() != null && listRv.getLayoutManager() instanceof LinearLayoutManager) {
            if (listRv.getAdapter() instanceof RvComboAdapter && ((RvComboAdapter) listRv.getAdapter()).getLoadingItemStatus() == ToolAdapter.LOADING) {
                checkAndLoadMore();
            } else if (listRv.getAdapter() instanceof BaseRVAdapter && ((BaseRVAdapter) listRv.getAdapter()).getLoadingStatus() == IBaseAdapter.FOOTER_LOADING) {
                checkAndLoadMore();
            }
        }
    }

    private void checkAndLoadMore() {
        listRv.post(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) listRv.getLayoutManager();
                int lastPosition = layoutManager.findLastVisibleItemPosition();
                if (lastPosition >= 0) {
                    View view = layoutManager.findViewByPosition(lastPosition);
                    if (view != null && view.getBottom() < listRv.getBottom()) {
                        onScrollListener.loadMoreData();
                    }
                }
            }
        });
    }


    /**
     * {@link com.xx.module.common.view.contract.IBaseView#onLoadDataFailed(Throwable)}
     *
     * @param msg
     */
    @Override

    public void onLoadDataFailed(Throwable msg) {
        if (onScrollListener != null) {
            if (msg instanceof ReturnJsonCodeException) {
                //不能直接通过下拉加载更多需要手动点击加载更多
                onScrollListener.setDataHasBeanLoadOver(true);
            }
        }
        super.onLoadDataFailed(msg);
    }


    private boolean isAdapterHasData() {
        boolean hasData;
        if (mAdapter == null && mLayoutAdapter == null) {
            return false;
        }
        if (mAdapter != null) {
            IBaseAdapter vLayoutAdapter = mAdapter.getMainDataAdapter();
            hasData = vLayoutAdapter != null && vLayoutAdapter.getData() != null && !vLayoutAdapter.getData().isEmpty();
            return hasData;
        } else {
            return mLayoutAdapter.getItemCount() > 0;
        }
    }


    private BaseListPresenter mPresenter;

    /**
     * @param clazz
     * @param <T>   返回指定类型的Presenter实例
     * @return
     */
    @Override
    public <T> T getPresenter(Class<?> clazz) {
        if (mPresenter == null && clazz != null) {
            mPresenter = PresenterManager.getInstance().getListPresenter(clazz);
        }
        return (T) mPresenter;
    }


    /**
     * 填充statusBar Padding
     *
     * @param view
     */
    @Override
    public void setStatusBarPadding(View view) {
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setPadding(0, ScreenUtils.getStatusBarHeight(view.getContext()), 0, 0);
            }
        }
    }

    @Override
    public void setStatusBarPadding(View rootView, int id) {
        if (rootView == null) {
            return;
        }
        View view = rootView.findViewById(id);
        setStatusBarPadding(view);
    }

    @Override
    public void setLoadingManager(View rootView, ILoadingView loadingViewManager) {
        this.iLoadingView = loadingViewManager;
        if (loadingViewManager != null) {
            View view = loadingViewManager.getLayoutView();
            if (view == null) {
                return;
            }
            ViewGroup group = (ViewGroup) rootView;
            int count = group.getChildCount();
            boolean iscontains = false;  //是否布局中已经包含了loadingview布局
            for (int i = 0; i < count; i++) {
                if (view == group.getChildAt(i)) {
                    iscontains = true;
                    break;
                }
            }
            if (!iscontains) {  //如果没有添加，就主动添加到根布局中
                ((ViewGroup) rootView.getRootView()).addView(view);
            }
        }
    }

    @Override
    public ILoadingView getLoadingViewManager() {
        return iLoadingView;
    }


    public void setStartingPageNum(int defaultPageOne) {
        this.startingPageNum = defaultPageOne;
        nowPage = startingPageNum;
    }

    /**
     * 返回是否是view自动刷新
     *
     * @return
     */
    public boolean isRefreshing() {
        if (ptrFrameLayout != null) {
            return ptrFrameLayout.isRefreshing();
        } else {
            return false;
        }
    }


    public void refreshData(OnReadyLoadDataCallback callback) {
        nowPage = startingPageNum;
        if (onScrollListener != null) {
            onScrollListener.setDataHasBeanLoadOver(false);
        }

        mHandler.sendEmptyMessage(LOADING);

        if (listRv == null) {
            if (getLoadingViewManager() != null) {
                getLoadingViewManager().loading();
            }
            callback.onLoadData(nowPage);
        } else {
            //如果loading页面还在，禁用下拉刷新
            if (getLoadingViewManager() != null && getLoadingViewManager().isLoading()) {
                getLoadingViewManager().loading();
            }
            callback.onLoadData(nowPage);
        }
        if (onScrollListener != null) {
            onScrollListener.setLoadingData(true);
        }
    }

    public int getNowPage() {
        return nowPage;
    }

    public int getStartingPageNum() {
        return startingPageNum;
    }

    public AutoLoadMoreScrollListener getLoadMoreScrollListener() {
        return onScrollListener;
    }


    public void setDefaultButtonClickListener(final OnReadyLoadDataCallback callback) {
        setOnLoadingViewButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData(callback);
                if (getLoadingViewManager() != null) {
                    getLoadingViewManager().loading();
                }
            }
        });
    }

    private RvComboAdapter mLayoutAdapter;

    public boolean isLoadDateComplete() {
        return onScrollListener != null && onScrollListener.isDataHasBeanLoadOver();
    }

    @Override
    public boolean handleMessage(Message msg) {
        int what = msg.what;
        final String message = msg.obj instanceof String ? (String) msg.obj : "";
        switch (what) {
            //第一页没有数据 刷新ui
            case NO_DATA_PAGE_ONE:
                if (getLoadingViewManager() != null && getLoadingViewManager().isLoadingViewShow()) {
                    getLoadingViewManager().showEmpty();
                }
                if (mAdapter != null) {
                    mAdapter.clearData();
                    if (TextUtils.isEmpty(message)) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA, false, null);
                    } else {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA, false, null, message);
                    }
                }
                if (mLayoutAdapter != null) {
                    mLayoutAdapter.clearAdapters();
                    if (TextUtils.isEmpty(message)) {
                        mLayoutAdapter.notifyLoadingStateChanged(ToolAdapter.NO_DATA);
                    } else {
                        mLayoutAdapter.notifyLoadingStateChanged(message, ToolAdapter.NO_DATA);
                    }
                }
                if (ptrFrameLayout != null && getLoadingViewManager() != null) {
                    ptrFrameLayout.setEnable(false);
                }
                break;
            //更多页没有数据，刷新
            case NO_DATA_PAGE_MORE:
                //服务器返回分页没有数据
                if (mAdapter != null && mAdapter.getLoadingStatus() == IBaseAdapter.FOOTER_LOADING) {
                    if (!mAdapter.getMainDataAdapter().getData().isEmpty()) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOAD_COMPLETE);
                    } else {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA);
                    }
                }
                if (mLayoutAdapter != null && mLayoutAdapter.getFooterStatus() == ToolAdapter.LOADING) {
                    mLayoutAdapter.notifyLoadingStateChanged(ToolAdapter.COMPLETE);
                }
                if (onScrollListener != null) {
                    onScrollListener.setDataHasBeanLoadOver(true);
                }
                break;
            //错误信息显示
            case INFO_FOR_FOOTER:
                if (mAdapter != null) {
                    if (!TextUtils.isEmpty(message)) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(message, true);
                    } else {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOAD_ERROR);
                    }
                }
                if (mLayoutAdapter != null) {
                    mLayoutAdapter.notifyLoadingStateChanged(message, ToolAdapter.ERROR);
                }
                break;
            //网络错误
            case ERROR_NET_WORK:
                showNoNetWork();
                break;
            case LOADING:
                if (getLoadingViewManager() != null) {
                    getLoadingViewManager().loading();
                }
                if (mAdapter != null) {
                    mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOADING);
                }
                if (mLayoutAdapter != null) {
                    mLayoutAdapter.notifyLoadingStateChanged(ToolAdapter.LOADING);
                }
                break;
        }

        return false;
    }

    public static class RecyclerViewConfig {
        public boolean closeAnim = true;
        public BaseFooterAdapter footerAdapter;
        public String loadingMessage;
        public BaseLoadingAdapter emptyAdapter;
        public RecyclerView.LayoutManager layoutManager;
        public RecyclerViewAdapterFactory.OnCreateAdaptersListener listener;

        private RecyclerViewConfig() {
        }

        public static RecyclerViewConfig obtain(Context context) {
            RecyclerViewConfig config = new RecyclerViewConfig();
            config.closeAnim = true;
            config.footerAdapter = RecyclerViewAdapterFactory.getFooterAdapterFromTemple(context);
            config.emptyAdapter = RecyclerViewAdapterFactory.getEmptyAdapterFromTemple(context);
            return config;
        }
    }

    public RvComboAdapter setupRecyclerView(RecyclerView view, RecyclerViewConfig config,
                                            final OnReadyLoadDataCallback onReadyLoadDataCallback) {
        listRv = view;
        if (onScrollListener == null) {
            if (activity != null) {
                onScrollListener = new AutoLoadMoreScrollListener(activity) {
                    @Override
                    public void loadMoreData() {
                        nowPage++;
                        loading(onReadyLoadDataCallback, nowPage);
                    }
                };
            } else if (fragment != null) {
                onScrollListener = new AutoLoadMoreScrollListener(fragment) {
                    @Override
                    public void loadMoreData() {
                        nowPage++;
                        loading(onReadyLoadDataCallback, nowPage);
                    }
                };
            }
            if (onScrollListener != null) {
                listRv.addOnScrollListener(onScrollListener);
            }
        }
        if (mLayoutAdapter == null || mLayoutAdapter != view.getAdapter()) {
            if (config == null) {
                config = RecyclerViewConfig.obtain(mContext);
            }
            if (TextUtils.isEmpty(config.loadingMessage)) {
                config.loadingMessage = view.getContext().getString(R.string.rv_footer_loading);
            }
            if (config.emptyAdapter != null) {
                config.emptyAdapter.setLoadingMessage(config.loadingMessage);
                if (config.emptyAdapter.getRetryListener() == null) {
                    config.emptyAdapter.setRetryListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mLayoutAdapter.notifyLoadingStateChanged("", ToolAdapter.LOADING);
                            nowPage = getStartingPageNum();
                            loading(onReadyLoadDataCallback, nowPage);
                        }
                    });
                }
            }
            if (config.footerAdapter != null) {
                config.footerAdapter.setLoadingMessage(config.loadingMessage);
                config.footerAdapter.setRetryListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLayoutAdapter.notifyLoadingStateChanged("", ToolAdapter.LOADING);
                        nowPage++;
                        loading(onReadyLoadDataCallback, nowPage);
                    }
                });
            }
            RecyclerViewAdapterFactory.Builder builder = new RecyclerViewAdapterFactory.Builder(view.getContext())
                    .attachToRecyclerView(view)
                    .setEmptyLoadingAdapter(config.emptyAdapter)
                    .setFooterAdapter(config.footerAdapter)
                    .setLayoutManager(config.layoutManager);
            if (config.closeAnim) {
                builder.closeAllAnim();
            }
            if (config.layoutManager == null) {
                builder.setLayoutManager(new LinearLayoutManager(view.getContext()));
            }
            mLayoutAdapter = builder.build();
        }
        return mLayoutAdapter;
    }

    private void loading(OnReadyLoadDataCallback callback, int page) {
        //加载更多
        mHandler.sendEmptyMessage(LOADING);
        if (onScrollListener != null) {
            onScrollListener.setDataHasBeanLoadOver(false);
            onScrollListener.setLoadingData(true);
        }
        callback.onLoadData(page);
    }


    public void initRecyclerViewOrListView(Object view, OnCreateListAdapterListener adapterListener,
                                           final OnReadyLoadDataCallback onReadyLoadDataCallback) {
        if (view instanceof RecyclerView) {
            listRv = (RecyclerView) view;
            listRv.setVisibility(View.VISIBLE);
            if (onScrollListener == null) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        //加载更多
                        listRv.post(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessage(LOADING);
                                nowPage++;
                                if (onScrollListener != null) {
                                    onScrollListener.setDataHasBeanLoadOver(false);
                                }
                                onReadyLoadDataCallback.onLoadData(nowPage);
                            }
                        });
                    }
                };
                if (activity != null) {
                    onScrollListener = new AutoLoadMoreScrollListener(activity) {
                        @Override
                        public void loadMoreData() {
                            r.run();
                        }
                    };
                } else if (fragment != null) {
                    onScrollListener = new AutoLoadMoreScrollListener(fragment) {
                        @Override
                        public void loadMoreData() {
                            r.run();
                        }
                    };
                }
            }
            if (listRv.getItemAnimator() == null) {
                ((SimpleItemAnimator) listRv.getItemAnimator()).setSupportsChangeAnimations(false);
            }
            listRv.addOnScrollListener(onScrollListener);
            listRv.setVerticalScrollBarEnabled(true);
            listRv.setScrollBarStyle(RecyclerView.SCROLLBARS_INSIDE_OVERLAY);

            IBaseAdapter impl = adapterListener.onCreateRecyclerViewAdapter();
            //普通的Adapter
            if (impl != null) {
                listRv.setLayoutManager(impl.getLayoutManager(mContext));
                mAdapter = impl;
                if (mAdapter instanceof RecyclerView.Adapter) {
                    listRv.setAdapter((RecyclerView.Adapter) mAdapter);
                    mAdapter.setLoadingItemViewClickListener(getLoadingItemClickListener(onReadyLoadDataCallback));
                }
            }
        } else if (view instanceof ListView) {

        } else {
            Log.e(this.getClass().getName(), "View is not a listView or RecyclerView");
        }

    }

    private View.OnClickListener getLoadingItemClickListener(final OnReadyLoadDataCallback callback) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.isMainLoadingViewShowingNow()) {
                    refreshData(callback);
                } else {
                    mHandler.sendEmptyMessage(LOADING);
                    nowPage++;
                    if (onScrollListener != null) {
                        onScrollListener.setDataHasBeanLoadOver(false);
                    }
                    callback.onLoadData(nowPage);
                }
            }
        };
    }

    public IBaseAdapter getAdapter() {
        return mAdapter;
    }


    void setupListFooter() {
        if (onScrollListener != null) {
            if (onScrollListener.isDataHasBeanLoadOver()) {
                //上滑已经没有更多数据
                if (mAdapter != null && mAdapter.getLoadingStatus() == IBaseAdapter.FOOTER_LOADING) {
                    if (mAdapter.getMainDataAdapter().getAdapterItemCount() > 0) {
                        if (mAdapter.getMainDataAdapter() instanceof BaseRVAdapter
                                && ((BaseRVAdapter) mAdapter.getMainDataAdapter()).getHeadViewCount() == ((BaseRVAdapter) mAdapter.getMainDataAdapter()).getItemCount()) {
                            mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA);
                        } else {
                            mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOAD_COMPLETE);
                        }
                        onScrollListener.setDataHasBeanLoadOver(true);
                    } else if (mAdapter.getMainDataAdapter().getAdapterItemCount() <= 0) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA);
                    } else {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOAD_COMPLETE);
                    }
                }
                if (mLayoutAdapter != null && mLayoutAdapter.getLoadingItemStatus() == ToolAdapter.LOADING) {
                    mLayoutAdapter.notifyLoadingStateChanged("已加载全部", ToolAdapter.COMPLETE);
                }
            } else if (listRv != null && listRv.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager manager = (LinearLayoutManager) listRv.getLayoutManager();
                int lastItem = manager.findLastCompletelyVisibleItemPosition();
                if (mAdapter != null && mAdapter.getLoadingStatus() == IBaseAdapter.FOOTER_LOADING) {
                    if (mAdapter.getMainDataAdapter().getAdapterItemCount() <= 0) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_NODATA);
                    } else if (lastItem + 1 >= mAdapter.getAdapterItemCount()) {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOAD_COMPLETE);
                    } else {
                        mAdapter.notifyLoadingViewItemViewStateChanged(IBaseAdapter.FOOTER_LOADING);
                    }
                }
            }
        }
    }

    public void releaseAdapter() {
        mAdapter = null;
    }

    public void autoRefresh() {
        if (ptrFrameLayout != null) {
            ptrFrameLayout.autoRefresh();
        }
    }

    public interface OnReadyLoadDataCallback {
        void onLoadData(int page);
    }

}
