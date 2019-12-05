package com.xx.module.common.view.loading;

import android.support.annotation.LayoutRes;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author
 * @date 2018/2/24 0024
 */

public abstract class RvAdapter<T, H extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<H> implements IBaseAdapter<RvAdapter, T> {


    protected OnRvItemListener itemClickListener;

    private final List<T> data = new ArrayList<>();


    protected SparseArrayCompat<Integer> headerLayouts;
    protected SparseArrayCompat<Class<? extends H>> headViewHolderClasses;
    private int headViewCount = 0;

    public int getHeadViewCount() {
        return headViewCount;
    }

    public void addHeader(@LayoutRes int layout, Class<? extends H> viewholder) {
        if (layout == 0 || viewholder == null) {
            return;
        }
        if (headerLayouts == null) {
            headerLayouts = new SparseArrayCompat<>();
            headViewHolderClasses = new SparseArrayCompat<>();
        }
        headerLayouts.append(headViewCount, layout);
        headViewHolderClasses.append(headViewCount, viewholder);
        headViewCount++;
    }


    protected void onBindHeadViewHolder(H holder, int position) {

    }

    protected abstract void onBindItemViewHolder(H holder, int dataPosition, int viewType);

    @Override
    public int getItemCount() {
        return headViewCount + getData().size();
    }


    @Override
    public int getItemViewType(int position) {
        if (headViewCount > 0) {
            if (position < headViewCount) {
                //头布局0.1.2.3.4....
                return position;
            } else {
                //上面都是头布局，这里才是item
                return getRealItemType(position);
            }
        }
        //如果没有头布局，直接返回position作为item的type
        return getRealItemType(position);
    }

    /**
     * @param positionWithHeader
     * @return
     */
    public int getRealItemType(int positionWithHeader) {
        return headViewCount;
    }


    @Override
    public IBaseAdapter getRecyclerViewAdapter() {
        return this;
    }

    @Override
    public void addDataList(List<T> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int size = this.getData().size();
        this.getData().addAll(data);
        notifyItemRangeInserted(getHeadViewCount() + size, data.size());
    }

    @Override
    public void addData(T data) {
        addDataList(Collections.singletonList(data));
    }


    @Override
    public void setData(final List<T> data) {
        int size = this.getData().size();
        if (size != 0 && data != null && size == data.size()) {
            getData().clear();
            getData().addAll(data);
            notifyItemRangeChanged(getHeadViewCount(), size);
        } else {
            this.getData().clear();
            notifyItemRangeRemoved(getHeadViewCount(), size);
            addDataList(data);
        }
    }

    public void setOnItemClickListener(OnRvItemListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void clearData() {
        getData().clear();
        notifyDataSetChanged();
    }


    @Override
    public RvAdapter getMainDataAdapter() {
        return this;
    }

    @Override
    public List<T> getData() {
        return data;
    }

    @Override
    public int getAdapterItemCount() {
        return getItemCount();
    }


    public interface OnRvItemListener {
        void onClick(RecyclerView.ViewHolder holder, int dataPosition);
    }


}
