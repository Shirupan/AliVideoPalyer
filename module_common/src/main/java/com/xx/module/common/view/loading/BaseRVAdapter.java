package com.xx.module.common.view.loading;


import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.module.common.R;
import com.xx.module.common.imageload.ImageLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 封装带有脚布局的RecyclerView.Adapter。可以通过showFooter...等方法来显示不同状态
 * 可以调用addHeaderView()来添加头布局。并在BindHeaderView方法中绑定数据
 * Created by fhs on 2016-07-06.
 */
public abstract class BaseRVAdapter<T> extends RvAdapter<T, SparseArrayViewHolder> {

    /**
     * 当内容空的时候显示loadingView
     */
    private boolean showLoadingView = false;

    protected SparseArrayViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutIds(viewType), parent, false);
        return new SparseArrayViewHolder(view);
    }

    public int getItemLayoutIds(int viewType) {
        return 0;
    }

    @Override
    public SparseArrayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_LOADING) {
            FrameLayout view = new FrameLayout(parent.getContext());
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            loadingViewManager = new DefaultLoadingViewManager(view);
            return new SparseArrayViewHolder(view);
        }
        //尾布局
        if (viewType == ITEM_TYPE_FOOTER) {
            return onCreateFooterView(parent.getContext(), parent);
        }

        //没有头布局
        if (getHeadViewCount() == 0) {
            return onCreateItemViewHolder(parent, viewType);
        } else if (viewType < getHeadViewCount()) {
            //逐一加载头布局
            for (int i = 0; i < getHeadViewCount(); i++) {
                int layout = headerLayouts.get(i);
                Class<?> viewHolderClass = headViewHolderClasses.get(i);
                View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                try {
                    Constructor c = viewHolderClass.getConstructor(View.class);
                    return (SparseArrayViewHolder) c.newInstance(view);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return new SparseArrayViewHolder(view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        //加载item
        return onCreateItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final SparseArrayViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_TYPE_LOADING) {
            isMainLoadingViewShowingNow = true;
            bindLoadingViewHolder(holder);
            return;
        }
        isMainLoadingViewShowingNow = false;

        int offset = 1;
        if (isShowFooter) {
            offset = 2;
        }

        if (position < getHeadViewCount()) {
            //header
            onBindHeadViewHolder(holder, position);
        } else if (position >= getHeadViewCount() && position <= getItemCount() - offset) {
            //item
            final int dataPosition = position - getHeadViewCount();
            int viewType = getItemViewType(position);
            onBindItemViewHolder(holder, dataPosition, viewType);
            //item点击事件
            boolean setClick;
            if (isShowFooter && getData().size() >= position) {
                setClick = true;
            } else {
                setClick = !isShowFooter && getData().size() - 1 >= position;
            }
            if (setClick && itemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClickListener.onClick(holder, dataPosition);
                    }
                });
            }
        } else if (isShowFooter && position == getItemCount() - 1) {
            //footer
            //如果当前数据不为空，且当页的数据量是满的
            onBindFooterViewHolder(holder, position, mLoadingStatus);
        }
    }


    private int mLoadingStatus = FOOTER_LOADING;
    /**
     * loading显示的信息组
     */
    private String[] mLoadingViewMessage;
    private boolean mLoadingViewCanClick = true;
    private View.OnClickListener mLoadingViewEmptyClickListener;
    private View.OnClickListener mLoadingViewRetryClickListener;
    private DefaultLoadingViewManager loadingViewManager = new DefaultLoadingViewManager(null);

    private boolean isMainLoadingViewShowingNow;

    @Override
    public boolean isMainLoadingViewShowingNow() {
        return isMainLoadingViewShowingNow;
    }

    private void bindLoadingViewHolder(SparseArrayViewHolder holder) {
        if (loadingViewManager == null) {
            return;
        }
        if (mLoadingStatus == FOOTER_LOADING) {
            loadingViewManager.loading();
        } else if (mLoadingStatus == FOOTER_LOAD_ERROR) {
            if (mLoadingViewCanClick) {
                loadingViewManager.setOnRefreshClickListener(mLoadingViewRetryClickListener);
            } else {
                loadingViewManager.setOnRefreshClickListener(null);
            }
            if (mLoadingViewMessage != null && mLoadingViewMessage.length >= 1) {
                loadingViewManager.showFailed(mLoadingViewMessage[0]);
            }
        } else if (mLoadingStatus == FOOTER_NET_ERROR) {
            if (mLoadingViewCanClick) {
                loadingViewManager.setOnRefreshClickListener(mLoadingViewRetryClickListener);
            } else {
                loadingViewManager.setOnRefreshClickListener(null);
            }
            loadingViewManager.showNoNet();
        } else if (mLoadingStatus == FOOTER_NODATA) {
            loadingViewManager.setOnEmptyClickListener(mLoadingViewEmptyClickListener);
            if (mLoadingViewMessage != null) {
                String message1 = "", message2 = "";
                if (mLoadingViewMessage.length >= 1) {
                    message1 = mLoadingViewMessage[0];
                }
                if (mLoadingViewMessage.length >= 2) {
                    message2 = mLoadingViewMessage[1];
                }
                if (TextUtils.isEmpty(message1)) {
                    message1 = noDataMsg;
                }
                if (TextUtils.isEmpty(message2)) {
                    message2 = noDataBtnMsg;
                }
                loadingViewManager.setEmptyMessage(message1, message2);
                loadingViewManager.showEmpty();
            }
        } else if (mLoadingStatus == FOOTER_LOAD_COMPLETE) {
            loadingViewManager.dismiss();
        } else if (mLoadingStatus == FOOTER_LOAD_CUSTOM) {
            if (mLoadingViewCanClick) {
                loadingViewManager.setOnRefreshClickListener(mLoadingViewRetryClickListener);
            } else {
                loadingViewManager.setOnRefreshClickListener(null);
            }
            if (mLoadingViewMessage != null && mLoadingViewMessage.length >= 1) {
                loadingViewManager.showFailed(mLoadingViewMessage[0]);
            }
        }
    }

    public boolean isLoadingViewShowing() {
        return getData().isEmpty() && showLoadingView;
    }

    /**
     * 该RecyclerView是否在空数据时候显示【加载信息页面】
     *
     * @param showLoadingView
     */
    public void setShowLoadingView(boolean showLoadingView) {
        this.showLoadingView = showLoadingView;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingViewShowing()) {
            return ITEM_TYPE_LOADING;
        }

        if (isShowFooter && position == getItemCount() - 1) {
            return ITEM_TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onViewRecycled(@NonNull SparseArrayViewHolder holder) {
        super.onViewRecycled(holder);

    }

    @Override
    public int getItemCount() {
        int size = super.getItemCount();
        if (size == 0 && showLoadingView) {
            isMainLoadingViewShowingNow = true;
            return 1;
        }
        isMainLoadingViewShowingNow = false;
        if (isShowFooter) {
            size += 1;
        }
        return size;
    }

    @Override
    public void addDataList(List<T> data) {
        if (data == null || data.isEmpty()) {
            if (isShowFooter) {
                if (this.getData().isEmpty()) {
                    notifyLoadingViewItemViewStateChanged(FOOTER_NODATA);
                } else {
                    notifyLoadingViewItemViewStateChanged(FOOTER_LOAD_COMPLETE);
                }
            }
            return;
        }
        int size = this.getData().size();
        this.getData().addAll(data);
        if (size == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(getHeadViewCount() + size, data.size());
        }
    }

    public void notifyHeaderDataChanged() {
        notifyItemRangeChanged(0, getHeadViewCount());
    }

    /**
     * 如果需要自定义footer,请重写该方法，返回true会抛弃所有默认设置
     *
     * @return
     */
    protected void onBindFooterViewHolder(final SparseArrayViewHolder holder, int position, int state) {
        final BaseRVAdapter.DefaultFooterHolder foot;
        if (holder instanceof DefaultFooterHolder) {
            Context context = holder.itemView.getContext();
            foot = (DefaultFooterHolder) holder;
            foot.itemView.setOnClickListener(null);
            foot.itemView.setEnabled(true);
            if (state == FOOTER_LOADING) {
                //正在加载中
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.VISIBLE);
                String s = context.getString(R.string.rv_footer_loading);
                foot.load_more_text.setText(s);
                ImageLoader.getInstance().loadResource(holder.itemView.getContext(), R.drawable.common_loading_icon, foot.footer_loading);
            } else if (state == FOOTER_LOAD_ERROR) {
                //加载错误
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.GONE);
                foot.load_more_text.setText(R.string.rv_footer_again);
                foot.itemView.setOnClickListener(mLoadingViewRetryClickListener);
            } else if (state == FOOTER_NET_ERROR) {
                //网络错误
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.GONE);
                foot.load_more_text.setText(R.string.rv_footer_no_network);
                foot.itemView.setOnClickListener(mLoadingViewRetryClickListener);
            } else if (state == FOOTER_NODATA) {
                //没有数据
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.GONE);
                String s = noDataMsg == null ? context.getString(R.string.rv_footer_no_data) : noDataMsg;
                foot.load_more_text.setText(s);
                foot.itemView.setOnClickListener(mLoadingViewEmptyClickListener);
            } else if (state == FOOTER_LOAD_COMPLETE) {
                //加载完成
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.GONE);
                String s = context.getString(R.string.rv_footer_no_more);
                foot.load_more_text.setText(s);
                foot.itemView.setOnClickListener(null);
            } else if (state == FOOTER_LOAD_CUSTOM) {
                //自定义文字
                foot.itemView.setVisibility(View.VISIBLE);
                foot.footer_loading.setVisibility(View.GONE);
                String s = mLoadingViewMessage == null || mLoadingViewMessage.length <= 0 ?
                        context.getString(R.string.rv_footer_again) : mLoadingViewMessage[0];
                foot.load_more_text.setText(s);
                if (mLoadingViewCanClick) {
                    foot.itemView.setOnClickListener(mLoadingViewRetryClickListener);
                } else {
                    foot.itemView.setOnClickListener(null);
                }
            }
        }
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }


    private String noDataMsg;
    private String noDataBtnMsg;
    /**
     * 默认开启footer
     */
    protected boolean isShowFooter = true;


    @Override
    public int getLoadingStatus() {
        return mLoadingStatus;
    }

    public void showFooterView(boolean b) {
        isShowFooter = b;
    }

    @Override
    public View.OnClickListener getLoadingItemViewClickListener() {
        return mLoadingViewRetryClickListener;
    }

    @Override
    public void setLoadingItemViewClickListener(View.OnClickListener listener) {
        mLoadingViewRetryClickListener = listener;
    }

    @Override
    public boolean isShowFooter() {
        return isShowFooter;
    }

    @Override
    public void notifyLoadingViewItemViewStateChanged(String message, boolean customFooterClick) {
        notifyLoadingViewItemViewStateChanged(FOOTER_LOAD_CUSTOM, customFooterClick, mLoadingViewRetryClickListener, message);
    }

    @Override
    public void notifyLoadingViewItemViewStateChanged(int status) {
        boolean canClick = false;
        if (status == IBaseAdapter.FOOTER_LOAD_ERROR) {
            canClick = true;
        }
        notifyLoadingViewItemViewStateChanged(status, canClick, mLoadingViewRetryClickListener);
    }

    @Override
    public void notifyLoadingViewItemViewStateChanged(int statues, boolean canClick, View.OnClickListener newListener, String... message) {
        mLoadingStatus = statues;
        mLoadingViewMessage = message;
        mLoadingViewCanClick = canClick;
        if (newListener != null) {
            if (statues == FOOTER_NODATA) {
                this.mLoadingViewEmptyClickListener = newListener;
            } else {
                this.mLoadingViewRetryClickListener = newListener;
            }
        }
        if (isLoadingViewShowing()) {
            notifyItemChanged(0);
        } else if (isShowFooter && getItemCount() > 0) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void setLoadingViewEmptyClickListener(View.OnClickListener clickListener) {
        this.mLoadingViewEmptyClickListener = clickListener;
        loadingViewManager.setOnEmptyClickListener(clickListener);
    }

    /**
     * 不使用自带footer
     */
    public void unShowFooterView() {
        isShowFooter = false;
    }

    protected SparseArrayViewHolder onCreateFooterView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.ablistview_footview, parent, false);
        return new DefaultFooterHolder(view);
    }


    public void setNoDataMsg(String noDataMsg, String btnMsg) {
        this.noDataMsg = noDataMsg;
        noDataBtnMsg = btnMsg;
        loadingViewManager.setEmptyMessage(noDataMsg, btnMsg);
    }


    /**
     * 暂时不显示footer
     */
    public void hideFooter() {
        if (isShowFooter && getItemCount() > 0) {
            notifyItemRemoved(getItemCount() - 1);
        }
        isShowFooter = false;
    }

    public void setEmptyImage(@DrawableRes int resource) {
        loadingViewManager.setEmptyImage(resource);
    }

    public static class DefaultFooterHolder extends SparseArrayViewHolder {
        public TextView load_more_text;
        public ImageView footer_loading;

        DefaultFooterHolder(View view) {
            super(view);
            this.load_more_text = itemView.findViewById(R.id.footview_text);
            this.footer_loading = itemView.findViewById(R.id.footer_loading);
        }
    }


}
