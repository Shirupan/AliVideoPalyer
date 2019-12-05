package com.xx.multiadapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RvComboAdapter extends Adapter<RvComboAdapter.ViewHolder> {
    private final List<MultilItemAdapter> mSubAdapters = new ArrayList();
    private BaseFooterAdapter mFooterAdapter;
    private BaseLoadingAdapter mLoadingAdapter;
    private AdapterDataObserver observer;

    public RvComboAdapter() {
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        MultilItemAdapter resultItem = null;
        int realViewType = -1;
        Iterator var5 = this.mSubAdapters.iterator();

        while(var5.hasNext()) {
            MultilItemAdapter item = (MultilItemAdapter)var5.next();
            if (item.containsViewType(viewType)) {
                resultItem = item;
                realViewType = this.getRealChildViewType(viewType, item.getIndex());
                break;
            }
        }

        return resultItem.onCreateViewHolder(viewGroup, realViewType);
    }

    public final int getItemViewType(int position) {
        MultilItemAdapter item = this.findMultiItemByPosition(position);
        if (!this.checkAdapter(item)) {
            return 0;
        } else {
            int viewType = item.getItemViewType(position - item.mStartPosition);
            viewType = this.transformChildViewType(viewType, item.getIndex());
            item.setChildViewType(viewType);
            return viewType;
        }
    }

    private int transformChildViewType(int viewType, int index) {
        if (viewType != index) {
            viewType += index * 100000;
        }

        return viewType;
    }

    private int getRealChildViewType(int viewType, int index) {
        return viewType == index ? viewType : viewType - index * 100000;
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MultilItemAdapter iMultiItem = this.findMultiItemByPosition(position);
        viewHolder.setAdapter(iMultiItem);
        iMultiItem.onBindViewHolder(viewHolder, position - iMultiItem.getStartPosition());
        if (iMultiItem.getItemClickListener() != null) {
            viewHolder.itemView.setOnClickListener(viewHolder.mAdapter.getItemClickListener());
        }

    }

    protected MultilItemAdapter findMultiItemByPosition(int position) {
        int count = this.mSubAdapters.size();
        if (count == 0) {
            return null;
        } else {
            MultilItemAdapter result = null;
            int small = 0;
            int height = count - 1;

            while(small <= height) {
                int middle = (small + height) / 2;
                result = (MultilItemAdapter)this.mSubAdapters.get(middle);
                int endPosition = result.getStartPosition() + result.getItemCount() - 1;
                if (position > endPosition) {
                    small = middle + 1;
                } else if (position < result.getStartPosition()) {
                    height = middle - 1;
                } else if (position >= result.getStartPosition() && position <= endPosition) {
                    break;
                }
            }

            return result;
        }
    }

    public int getItemCount() {
        return this.getAllItemCount();
    }

    public int getItemCountWithoutEmptyAndFooter() {
        int count = this.getItemCount();
        if (this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter)) {
            --count;
        }

        if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
            --count;
        }

        return count;
    }

    public List<MultilItemAdapter> getAdapters() {
        return this.mSubAdapters;
    }

    private int getAllItemCount() {
        int count = 0;

        MultilItemAdapter item;
        for(Iterator var2 = this.mSubAdapters.iterator(); var2.hasNext(); count += item.getItemCount()) {
            item = (MultilItemAdapter)var2.next();
        }

        return count;
    }

    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mAdapter != null) {
            holder.mAdapter.onViewRecycled(holder);
        }

        holder.setAdapter((MultilItemAdapter)null);
    }

    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return super.onFailedToRecycleView(holder) && holder.mAdapter != null && holder.mAdapter.onFailedToRecycleView(holder);
    }

    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.mAdapter != null) {
            holder.mAdapter.onViewAttachedToWindow(holder);
        }

    }

    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.mAdapter != null) {
            holder.mAdapter.onViewDetachedFromWindow(holder);
        }

    }

    public void notifyLoadingStateChanged(int state) {
        if (this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter)) {
            this.mFooterAdapter.setCurrentState(state);
            this.mFooterAdapter.notifyItemChanged(0);
        }

        if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
            this.mLoadingAdapter.setCurrentState(state);
            this.mLoadingAdapter.notifyItemChanged(0);
        }

    }

    public void notifyLoadingStateChanged(String message, int state) {
        if (this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter)) {
            this.mFooterAdapter.setCurrentState(state);
            this.mFooterAdapter.setMessages(message, state);
            this.mFooterAdapter.notifyItemChanged(0);
        }

        if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
            this.mLoadingAdapter.setCurrentState(state);
            this.mLoadingAdapter.setMessages(message, state);
            this.mLoadingAdapter.notifyItemChanged(0);
        }

    }

    public void notifyLoadingStateChanged(String message, Drawable drawable, int state) {
        if (this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter)) {
            this.mFooterAdapter.setCurrentState(state);
            this.mFooterAdapter.setMessages(message, drawable, state);
            this.mFooterAdapter.notifyItemChanged(0);
        }

        if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
            this.mLoadingAdapter.setCurrentState(state);
            this.mLoadingAdapter.setMessages(message, drawable, state);
            this.mLoadingAdapter.notifyItemChanged(0);
        }

    }

    public void setEmptyAdapter(BaseLoadingAdapter emptyAdapter) {
        if (emptyAdapter == null) {
            if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
                this.removeAdapter(this.mLoadingAdapter);
            }

            this.mLoadingAdapter = null;
        } else {
            this.mLoadingAdapter = emptyAdapter;
            if (!this.mSubAdapters.isEmpty()) {
                this.mSubAdapters.clear();
            }

            List<MultilItemAdapter> adapters = new ArrayList();
            adapters.add(this.mLoadingAdapter);
            this.setMultiItems(adapters);
        }
    }

    public void setFooterAdapter(BaseFooterAdapter adapter) {
        if ((this.mLoadingAdapter == null || !this.mSubAdapters.contains(this.mLoadingAdapter)) && adapter != null) {
            if (this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter)) {
                this.mSubAdapters.remove(this.mFooterAdapter);
            }

            this.mFooterAdapter = adapter;
            this.addAdapter(this.mFooterAdapter);
        } else {
            this.removeAdapter(this.mFooterAdapter);
            this.mFooterAdapter = adapter;
        }
    }

    public void removeAdapter(@Nullable MultilItemAdapter adapter) {
        if (adapter != null) {
            int index = this.mSubAdapters.indexOf(adapter);
            if (index >= 0) {
                this.mSubAdapters.remove(adapter);
                this.notifyItemRangeRemoved(adapter.mStartPosition, adapter.getItemCount());
            }

        }
    }

    public void addAdapter(@Nullable MultilItemAdapter adapter) {
        if (adapter != null) {
            int position = this.mSubAdapters.size();
            this.addAdapter(position, adapter);
        }
    }

    public void addAdapter(int position, @Nullable MultilItemAdapter adapter) {
        if (adapter != null) {
            this.addAdapters(position, Collections.singletonList(adapter));
        }
    }

    public void addAdapters(List<MultilItemAdapter> adapters) {
        if (this.mSubAdapters.isEmpty()) {
            this.setMultiItems(adapters);
        } else {
            this.addAdapters(this.mSubAdapters.size(), adapters);
        }

    }

    public void addAdapters(int position, List<MultilItemAdapter> items) {
        if (items != null) {
            if (this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter)) {
                this.setMultiItems(items);
            } else {
                int max = this.mSubAdapters.size();
                if (position < 0) {
                    position = 0;
                } else if (position > max) {
                    position = max;
                }

                List<MultilItemAdapter> newAdapters = new ArrayList(this.mSubAdapters);

                for(Iterator var5 = items.iterator(); var5.hasNext(); ++position) {
                    MultilItemAdapter adapter = (MultilItemAdapter)var5.next();
                    newAdapters.add(position, adapter);
                }

                this.setMultiItems(newAdapters);
            }

        }
    }

    public void setMultiItems(List<MultilItemAdapter> items) {
        if (items != null && !items.isEmpty()) {
            int count = 0;
            this.mSubAdapters.clear();
            if (this.observer != null) {
                this.unregisterAdapterDataObserver(this.observer);
            }

            this.observer = new AdapterDataObserver();
            this.registerAdapterDataObserver(this.observer);

            for(int i = 0; i < items.size(); ++i) {
                MultilItemAdapter adapter = (MultilItemAdapter)items.get(i);
                adapter.setMainAdapter(this);
                adapter.setIndex(i);
                adapter.setStartPosition(count);
                count += adapter.getItemCount();
                if (adapter instanceof BaseLoadingAdapter) {
                    this.mLoadingAdapter = (BaseLoadingAdapter)adapter;
                    break;
                }

                if (adapter instanceof BaseFooterAdapter) {
                    this.mFooterAdapter = (BaseFooterAdapter)adapter;
                } else {
                    this.mSubAdapters.add(adapter);
                }
            }

            if (this.mLoadingAdapter != null) {
                this.mSubAdapters.clear();
                this.mSubAdapters.add(this.mLoadingAdapter);
            } else if (this.mFooterAdapter != null && !this.mSubAdapters.contains(this.mFooterAdapter)) {
                this.mSubAdapters.add(this.mFooterAdapter);
            }

            this.notifyDataSetChanged();
        }
    }

    public int getAdaptersCount() {
        return this.mSubAdapters.size();
    }

    public void clearAdapters() {
        this.mSubAdapters.clear();
        this.notifyDataSetChanged();
    }

    public int getLoadingItemStatus() {
        return this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter) ? this.mLoadingAdapter.getCurrentState() : -1;
    }

    public boolean isLoadingSet() {
        return this.mLoadingAdapter != null && this.mSubAdapters.contains(this.mLoadingAdapter);
    }

    public int getFooterStatus() {
        return this.mFooterAdapter != null && this.mSubAdapters.contains(this.mFooterAdapter) ? this.mFooterAdapter.getCurrentState() : -1;
    }

    protected boolean checkAdapter(MultilItemAdapter pair) {
        return pair != null;
    }

    protected class AdapterDataObserver extends android.support.v7.widget.RecyclerView.AdapterDataObserver {
        AdapterDataObserver() {
        }

        private void updateLayoutHelper(int from) {
            MultilItemAdapter item = RvComboAdapter.this.findMultiItemByPosition(from);
            int fromPosition = RvComboAdapter.this.mSubAdapters.indexOf(item) - 1;
            if (fromPosition < 0) {
                fromPosition = 0;
            }

            this.updateAdaptersItemCount(fromPosition);
        }

        private void updateAdaptersItemCount(int fromPosition) {
            synchronized(RvComboAdapter.this.mSubAdapters) {
                if (!RvComboAdapter.this.mSubAdapters.isEmpty()) {
                    MultilItemAdapter pair = (MultilItemAdapter)RvComboAdapter.this.mSubAdapters.get(fromPosition);
                    if (RvComboAdapter.this.checkAdapter(pair)) {
                        int count = pair.getStartPosition();

                        for(int i = fromPosition; i < RvComboAdapter.this.mSubAdapters.size(); ++i) {
                            pair = (MultilItemAdapter)RvComboAdapter.this.mSubAdapters.get(i);
                            pair.setStartPosition(count);
                            count = pair.getStartPosition() + pair.getItemCount();
                        }

                    }
                }
            }
        }

        public void onChanged() {
            RvComboAdapter.this.notifyItemRangeChanged(0, RvComboAdapter.this.getAllItemCount());
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            this.updateLayoutHelper(positionStart);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            this.updateLayoutHelper(positionStart);
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int position = fromPosition <= toPosition ? fromPosition : toPosition;
            this.updateLayoutHelper(position);
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            this.updateLayoutHelper(positionStart);
        }
    }

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        MultilItemAdapter mAdapter;
        private SparseArray<View> views;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public <T extends View> View getView(int id) {
            if (this.views == null) {
                this.views = new SparseArray();
            }

            View view = (View)this.views.get(id);
            if (view == null) {
                view = this.itemView.findViewById(id);
                this.views.put(id, view);
            }

            return view;
        }

        public ViewHolder setText(int viewId, String value) {
            TextView view = (TextView)this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setText(value);
                return this;
            }
        }

        public ViewHolder setTextColor(int viewId, int textColor) {
            TextView view = (TextView)this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setTextColor(textColor);
                return this;
            }
        }

        public ViewHolder setImageResource(int viewId, int imageResId) {
            ImageView view = (ImageView)this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setImageResource(imageResId);
                return this;
            }
        }

        public ViewHolder setBackgroundColor(int viewId, int color) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setBackgroundColor(color);
                return this;
            }
        }

        public ViewHolder setBackgroundResource(int viewId, int backgroundRes) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setBackgroundResource(backgroundRes);
                return this;
            }
        }

        public ViewHolder setVisible(int viewId, boolean visible) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
                return this;
            }
        }

        public ViewHolder setOnClickListener(int viewId, OnClickListener listener) {
            View view = this.getView(viewId);
            if (view != null) {
                view.setOnClickListener(listener);
            }

            return this;
        }

        public ViewHolder setOnTouchListener(int viewId, OnTouchListener listener) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setOnTouchListener(listener);
                return this;
            }
        }

        public ViewHolder setOnLongClickListener(int viewId, OnLongClickListener listener) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setOnLongClickListener(listener);
                return this;
            }
        }

        public ViewHolder setTag(int viewId, Object tag) {
            View view = this.getView(viewId);
            if (!this.checkView(view)) {
                return this;
            } else {
                view.setTag(tag);
                return this;
            }
        }

        public boolean checkView(View view) {
            return view != null;
        }

        public void setAdapter(MultilItemAdapter item) {
            this.mAdapter = item;
        }
    }
}

