package com.xx.multiadapter;

import android.database.Observable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class MultilItemAdapter<T> {
    private Set<Integer> subViewTypes = new ArraySet();
    private boolean mHasStableIds = false;
    protected int mStartPosition;
    private int mIndex;
    private final List<T> data = new ArrayList();
    private RvComboAdapter mMainAdapter;
    private OnClickListener itemClickListener;
    private final AdapterDataObservable mObservable = new AdapterDataObservable();
    public static final int MATCH = 1;
    public static final int WARP = 0;

    public MultilItemAdapter() {
    }

    public void setMainAdapter(RvComboAdapter mainAdapter) {
        this.mMainAdapter = mainAdapter;
    }

    public RvComboAdapter getMainAdapter() {
        return this.mMainAdapter;
    }

    public int getItemCount() {
        return this.data.size();
    }

    public abstract void onBindViewHolder(RvComboAdapter.ViewHolder var1, int var2);

    public RvComboAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.getItemLayoutIds(viewType), parent, false);
        return new RvComboAdapter.ViewHolder(view);
    }

    public abstract int getItemLayoutIds(int var1);

    public void setHasStableIds(boolean hasStableIds) {
        if (this.hasObservers()) {
            throw new IllegalStateException("Cannot change whether this adapter has stable IDs while the adapter has registered observers.");
        } else {
            this.mHasStableIds = hasStableIds;
        }
    }

    public List<T> getData() {
        return this.data;
    }

    public void addData(T data) {
        this.addDataList(Collections.singletonList(data));
    }

    public void addData(int position, T data) {
        this.addDataList(position, Collections.singletonList(data));
    }

    public long getItemId(int position) {
        return -1L;
    }

    public final boolean hasStableIds() {
        return this.mHasStableIds;
    }

    public void onViewRecycled(@NonNull android.support.v7.widget.RecyclerView.ViewHolder holder) {
    }

    public boolean onFailedToRecycleView(@NonNull android.support.v7.widget.RecyclerView.ViewHolder holder) {
        return false;
    }

    public void onViewAttachedToWindow(@NonNull android.support.v7.widget.RecyclerView.ViewHolder holder) {
    }

    public void onViewDetachedFromWindow(@NonNull android.support.v7.widget.RecyclerView.ViewHolder holder) {
    }

    public final boolean hasObservers() {
        return this.mObservable.hasObservers();
    }

    public final void notifyDataSetChanged() {
        this.mObservable.notifyChanged();
    }

    public final void notifyItemChanged(int position) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeChanged(position, 1);
            this.mMainAdapter.notifyItemChanged(this.getStartPosition() + position, 1);
        }

    }

    public final void notifyItemChanged(int position, @Nullable Object payload) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeChanged(position, 1, payload);
            this.mMainAdapter.notifyItemChanged(this.getStartPosition() + position, payload);
        }

    }

    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeChanged(positionStart, itemCount);
            this.mMainAdapter.notifyItemRangeChanged(this.getStartPosition() + positionStart, itemCount);
        }

    }

    public final void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        this.mObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
        if (this.mMainAdapter != null) {
            this.mMainAdapter.notifyItemRangeChanged(this.getStartPosition() + positionStart, itemCount, payload);
        }

    }

    public final void notifyItemInserted(int position) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeInserted(position, 1);
            this.mMainAdapter.notifyItemInserted(this.getStartPosition() + position);
        }

    }

    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeInserted(positionStart, itemCount);
            this.mMainAdapter.notifyItemRangeInserted(this.getStartPosition() + positionStart, itemCount);
        }

    }

    public final void notifyItemRemoved(int position) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeRemoved(position, 1);
            this.mMainAdapter.notifyItemRangeRemoved(this.getStartPosition() + position, 1);
        }

    }

    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        if (this.mMainAdapter != null) {
            this.mObservable.notifyItemRangeRemoved(positionStart, itemCount);
            this.mMainAdapter.notifyItemRangeRemoved(this.getStartPosition() + positionStart, itemCount);
        }

    }

    public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
        this.mObservable.registerObserver(observer);
    }

    public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
        this.mObservable.unregisterObserver(observer);
    }

    public int getItemMatchType() {
        return 0;
    }

    public int getIndex() {
        return this.mIndex;
    }

    protected void setIndex(int index) {
        this.mIndex = index;
    }

    public int getStartPosition() {
        return this.mStartPosition;
    }

    protected void setStartPosition(int startPosition) {
        this.mStartPosition = startPosition;
    }

    public void setOnItemClickListener(OnClickListener onClickListener) {
        this.itemClickListener = onClickListener;
    }

    protected OnClickListener getItemClickListener() {
        return this.itemClickListener;
    }

    public void addDataList(List<T> data) {
        int index = this.getItemCount() - 1;
        if (index < 0) {
            index = 0;
        }

        this.addDataList(index, data);
    }

    public void addDataList(int position, List<T> data) {
        if (data != null && !data.isEmpty()) {
            this.getData().addAll(data);
            this.notifyItemRangeInserted(this.getItemCount() - 1, data.size());
        }
    }

    public void clearData() {
        int count = this.getData().size();
        this.getData().clear();
        this.notifyItemChanged(this.getStartPosition(), count);
    }

    public void setDataList(List<T> data) {
        int oldCount = this.getItemCount();
        if (data != null) {
            this.getData().clear();
            this.getData().addAll(data);
            int newCount = this.getItemCount();
            this.notifyDateSet(oldCount, newCount);
        } else {
            this.notifyItemRangeRemoved(0, oldCount);
            this.getData().clear();
        }

    }

    public abstract int getItemViewType(int var1);

    void setChildViewType(int viewType) {
        this.subViewTypes.add(viewType);
    }

    boolean containsViewType(int viewType) {
        return this.subViewTypes.contains(viewType);
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void notifyDateSet(int oldCount, int newCount) {
        if (oldCount == newCount) {
            this.notifyItemRangeChanged(0, newCount);
        } else if (oldCount > newCount) {
            int removeCount = oldCount - newCount;
            this.notifyItemRangeRemoved(oldCount - removeCount, removeCount);
            this.notifyItemRangeChanged(0, newCount);
        } else {
            this.notifyItemRangeChanged(0, newCount);
            this.notifyItemRangeInserted(newCount, newCount - oldCount);
        }

    }

    protected static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public boolean hasObservers() {
            return !this.mObservers.isEmpty();
        }

        public void notifyChanged() {
            for(int i = this.mObservers.size() - 1; i >= 0; --i) {
                ((AdapterDataObserver)this.mObservers.get(i)).onChanged();
            }

        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            this.notifyItemRangeChanged(positionStart, itemCount, (Object)null);
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            for(int i = this.mObservers.size() - 1; i >= 0; --i) {
                ((AdapterDataObserver)this.mObservers.get(i)).onItemRangeChanged(positionStart, itemCount, payload);
            }

        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            for(int i = this.mObservers.size() - 1; i >= 0; --i) {
                ((AdapterDataObserver)this.mObservers.get(i)).onItemRangeInserted(positionStart, itemCount);
            }

        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            for(int i = this.mObservers.size() - 1; i >= 0; --i) {
                ((AdapterDataObserver)this.mObservers.get(i)).onItemRangeRemoved(positionStart, itemCount);
            }

        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            for(int i = this.mObservers.size() - 1; i >= 0; --i) {
                ((AdapterDataObserver)this.mObservers.get(i)).onItemRangeMoved(fromPosition, toPosition, 1);
            }

        }
    }
}
