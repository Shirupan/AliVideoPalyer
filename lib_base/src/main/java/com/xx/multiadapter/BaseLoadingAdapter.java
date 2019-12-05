package com.xx.multiadapter;

import android.content.Context;

public abstract class BaseLoadingAdapter extends ToolAdapter {
    public BaseLoadingAdapter(Context context) {
        super(context);
    }

    public void onBindViewHolder(RvComboAdapter.ViewHolder viewHolder, int position) {
    }

    public int getItemMatchType() {
        return 1;
    }
}
