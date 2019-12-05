package com.xx.module.common.view.loading;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 统一管理ViewHolder
 * Created by fhs on 2016-07-23.
 */
public class SparseArrayViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> views;

    public SparseArrayViewHolder(View itemView) {
        super(itemView);
    }

    public <T extends View> T getView(int id) {
        if (views == null) {
            views = new SparseArray<>();
        }
        View view = views.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            views.put(id, view);
        }
        return (T) view;
    }

    public SparseArrayViewHolder setText(int viewId, String value) {
        TextView view = getView(viewId);
        view.setText(value);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setTextColor(int viewId, int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setImageResource(int viewId, int imageResId) {
        ImageView view = getView(viewId);
        view.setImageResource(imageResId);
        return SparseArrayViewHolder.this;
    }


    public SparseArrayViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setBackgroundResource(int viewId, int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return SparseArrayViewHolder.this;
    }

    public SparseArrayViewHolder setTag(int viewId, Object tag) {
        View view = getView(viewId);
        view.setTag(tag);
        return SparseArrayViewHolder.this;
    }
}
