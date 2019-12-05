package com.xx.multiadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;
import android.view.View.OnClickListener;

import com.xx.base.R;

import java.util.Map;

public abstract class ToolAdapter extends MultilItemAdapter<String> {
    public static final int DLE = -1;
    public static final int LOADING = 0;
    public static final int COMPLETE = 1;
    public static final int ERROR = 2;
    public static final int NO_DATA = 3;
    private OnCurrentStateChangedListener onCurrentStateChangedListener;
    private int currentState = 0;
    protected Map<Integer, MessageHolder> messages = new ArrayMap();
    private Context mContext;

    public ToolAdapter(Context context) {
        this.mContext = context;
        this.initMessages();
    }

    protected void initMessages() {
        this.setMessages(this.mContext.getString(R.string.rv_footer_loading), (Drawable)null, 0);
        this.setMessages(this.mContext.getString(R.string.rv_footer_no_more), (Drawable)null, 1);
        this.setMessages(this.mContext.getString(R.string.rv_footer_again), (Drawable)null, 2);
        this.setMessages(this.mContext.getString(R.string.rv_footer_no_data), (Drawable)null, 3);
    }

    public Context getContext() {
        return this.mContext;
    }

    public final int getItemCount() {
        return 1;
    }

    public void onBindViewHolder(RvComboAdapter.ViewHolder viewHolder, int position) {
    }

    public void setOnCurrentStateChangedListener(OnCurrentStateChangedListener onCurrentStateChangedListener) {
        this.onCurrentStateChangedListener = onCurrentStateChangedListener;
    }

    public void setCurrentState(int state) {
        if (state != this.currentState) {
            if (this.onCurrentStateChangedListener != null) {
                this.onCurrentStateChangedListener.onChanged(state, this.currentState);
            }

            this.currentState = state;
        }
    }

    public int getCurrentState() {
        return this.currentState;
    }

    public int getItemMatchType() {
        return 1;
    }

    public void setMessages(CharSequence message, Drawable drawable, int state) {
        MessageHolder holder = new MessageHolder();
        holder.message = message;
        holder.drawable = drawable;
        this.messages.put(state, holder);
    }

    public void setMessages(CharSequence message, int state) {
        MessageHolder holder = (MessageHolder)this.messages.get(state);
        if (holder == null) {
            holder = new MessageHolder();
        }

        holder.message = message;
        this.messages.put(state, holder);
    }

    public void setLoadingMessage(CharSequence loadingMessage) {
        MessageHolder holder = (MessageHolder)this.messages.get(0);
        if (holder == null) {
            holder = new MessageHolder();
            holder.message = loadingMessage;
        } else {
            holder.message = loadingMessage;
        }

        this.messages.put(0, holder);
    }

    public abstract void setRetryListener(OnClickListener var1);

    public abstract OnClickListener getRetryListener();

    public interface OnCurrentStateChangedListener {
        void onChanged(int var1, int var2);
    }

    public static class MessageHolder {
        public CharSequence message;
        public Drawable drawable;

        public MessageHolder() {
        }
    }
}
