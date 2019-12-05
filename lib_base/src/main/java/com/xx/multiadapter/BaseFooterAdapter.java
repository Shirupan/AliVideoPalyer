package com.xx.multiadapter;

import android.content.Context;

public abstract class BaseFooterAdapter extends ToolAdapter {
    public BaseFooterAdapter(Context context) {
        super(context);
    }

    public void onBindViewHolder(RvComboAdapter.ViewHolder viewHolder, int position) {
    }

    public int getItemMatchType() {
        return 1;
    }
}
