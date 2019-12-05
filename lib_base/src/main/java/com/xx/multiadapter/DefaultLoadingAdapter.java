package com.xx.multiadapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.base.R;

public class DefaultLoadingAdapter extends BaseLoadingAdapter {
    private OnClickListener onErrorClickLister;

    public DefaultLoadingAdapter(Context context) {
        super(context);
    }

    public void onBindViewHolder(@NonNull RvComboAdapter.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        MessageHolder messageHolder = (MessageHolder)this.messages.get(this.getCurrentState());
        if (messageHolder != null) {
            final ImageView iv = (ImageView)viewHolder.getView(R.id.loading_failed);
            if (messageHolder.drawable != null) {
                iv.setImageDrawable(messageHolder.drawable);
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }

            ((TextView)viewHolder.getView(R.id.loading_text)).setText(messageHolder.message);
            TextView button = (TextView)viewHolder.getView(R.id.loading_btn);
            if (this.onErrorClickLister != null) {
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        DefaultLoadingAdapter.this.setCurrentState(0);
                        DefaultLoadingAdapter.this.notifyItemChanged(0);
                        DefaultLoadingAdapter.this.onErrorClickLister.onClick(v);
                    }
                });
            }

            if (this.getCurrentState() == 0) {
                iv.post(new Runnable() {
                    public void run() {
                        if (iv.getDrawable() instanceof AnimationDrawable) {
                            ((AnimationDrawable)iv.getDrawable()).start();
                        }

                    }
                });
                button.setVisibility(View.GONE);
                viewHolder.itemView.setOnClickListener((OnClickListener)null);
            } else if (this.getCurrentState() == 1 || this.getCurrentState() == 2) {
                iv.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
            }

        }
    }

    public int getItemLayoutIds(int viewType) {
        return R.layout.lib_multi_layout_loading;
    }

    public void setRetryListener(OnClickListener listener) {
        this.onErrorClickLister = listener;
    }

    public OnClickListener getRetryListener() {
        return this.onErrorClickLister;
    }

    public int getItemMatchType() {
        return 1;
    }

    public int getItemViewType(int position) {
        return 10010;
    }
}