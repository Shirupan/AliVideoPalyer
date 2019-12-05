package com.xx.multiadapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xx.base.R;

public class DefaultFooterAdapter extends BaseFooterAdapter {
    private OnClickListener onErrorClickLister;

    public DefaultFooterAdapter(Context context) {
        super(context);
    }

    public void onBindViewHolder(RvComboAdapter.ViewHolder viewHolder, int position) {
        MessageHolder messageHolder = (MessageHolder)this.messages.get(this.getCurrentState());
        if (messageHolder != null) {
            ImageView iv = (ImageView)viewHolder.getView(R.id.footer_load_result_iv);
            ProgressBar loadingBar = (ProgressBar)viewHolder.getView(R.id.footer_loading);
            if (messageHolder.drawable != null) {
                iv.setImageDrawable(messageHolder.drawable);
                loadingBar.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
            } else {
                loadingBar.setVisibility(View.VISIBLE);
                iv.setVisibility(View.GONE);
            }

            ((TextView)viewHolder.getView(R.id.footview_text)).setText(messageHolder.message);
            if (this.getCurrentState() == 0) {
                viewHolder.itemView.setOnClickListener((OnClickListener)null);
            } else if (this.getCurrentState() == 2) {
                loadingBar.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                if (this.onErrorClickLister != null) {
                    viewHolder.itemView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            DefaultFooterAdapter.this.setCurrentState(0);
                            DefaultFooterAdapter.this.notifyItemChanged(0);
                            DefaultFooterAdapter.this.onErrorClickLister.onClick(v);
                        }
                    });
                }
            } else {
                loadingBar.setVisibility(View.GONE);
            }

        }
    }

    public int getItemLayoutIds(int viewType) {
        return R.layout.lib_multi_layout_footview;
    }

    public int getItemMatchType() {
        return 1;
    }

    public int getItemViewType(int position) {
        return 10000;
    }

    public void setRetryListener(OnClickListener listener) {
        this.onErrorClickLister = listener;
    }

    public OnClickListener getRetryListener() {
        return this.onErrorClickLister;
    }
}
