package com.xx.module.common.view.loading;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

/**
 * @param <M> 如果是有子Adapter.没有就填本身Adapter
 * @param <D> MainAdapter 的数据对象
 * @author
 */

public interface IBaseAdapter<M extends IBaseAdapter, D> {
    int ITEM_TYPE_LOADING = 1001;
    /**
     * 当前脚布局的状态：加载中，无数据，加载错误
     */

    int ITEM_TYPE_FOOTER = 10002;

    /**
     * 正在加载中
     */
    int FOOTER_LOADING = 101;
    /**
     * 没有数据
     */
    int FOOTER_NODATA = 102;
    /**
     * 网络错误
     */
    int FOOTER_NET_ERROR = 103;
    /**
     * 加载错误
     */
    int FOOTER_LOAD_ERROR = 104;
    /**
     * 已经全部加载
     */
    int FOOTER_LOAD_COMPLETE = 105;

    /**
     * 自定义显示结果
     */
    int FOOTER_LOAD_CUSTOM = 106;

    /**
     * 此方法不建议在Adapter内部调用，绘制过程中调用该方法会导致错误
     *
     * @param status {@link IBaseAdapter#FOOTER_LOAD_COMPLETE}
     */
    void notifyLoadingViewItemViewStateChanged(int status);

    void notifyLoadingViewItemViewStateChanged(String message, boolean customFooterClick);


    void notifyLoadingViewItemViewStateChanged(int statues, boolean canClick,
                                               View.OnClickListener newListener, String... message);

    /**
     * 清空数据
     */
    void clearData();

    /**
     * 返回Footer是否需要显示
     */
    boolean isShowFooter();

    /**
     * @return
     */
    M getMainDataAdapter();

    /**
     * 返回Adapter绑定的数据集
     *
     * @return
     */
    List<D> getData();

    void addData(D data);

    void addDataList(List<D> data);

    void setData(List<D> data);

    RecyclerView.LayoutManager getLayoutManager(Context context);

    /**
     * 总的item条目数
     *
     * @return
     */
    int getAdapterItemCount();

    /**
     * @param l
     */
    void setLoadingItemViewClickListener(View.OnClickListener l);

    /**
     * @return
     */
    View.OnClickListener getLoadingItemViewClickListener();

    /**
     * 获取当前Footer状态
     *
     * @return
     */
    int getLoadingStatus();


    IBaseAdapter getRecyclerViewAdapter();

    /**
     * 当前是否显示loading
     *
     * @return
     */
    boolean isMainLoadingViewShowingNow();

}
