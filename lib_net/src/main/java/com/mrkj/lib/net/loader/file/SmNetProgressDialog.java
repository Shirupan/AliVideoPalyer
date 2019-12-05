package com.mrkj.lib.net.loader.file;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mrkj.net.R;

import io.reactivex.disposables.Disposable;


/**
 * 等待过程的view
 *
 * @author
 * @date 2016/11/15
 */

public class SmNetProgressDialog extends Dialog {

    TextView textView;
    private Builder mBuilder;

    protected SmNetProgressDialog(Builder builder) {
        super(builder.mContext, R.style.net_loading_dialog);
        mBuilder = builder;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mBuilder.mContext).inflate(R.layout.sm_net_progress_dialog_layout, null, false);
        if (!TextUtils.isEmpty(mBuilder.message)) {
            textView = view.findViewById(R.id.id_tv_loadingmsg);
            textView.setText(mBuilder.message);
        }

        setContentView(view);
        if (!mBuilder.dimBehind && getWindow() != null) {
            //背景全亮
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        setCancelable(mBuilder.cancelable);
        //取消当前任务
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                release();
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                release();
            }
        });
    }

    public void setText(CharSequence sequence) {
        textView.setText(sequence);
    }

    private void release() {
        if (mBuilder.subscription != null && !mBuilder.subscription.isDisposed()) {
            mBuilder.subscription.dispose();
        }
        mBuilder.subscription = null;
    }

    public static class Builder {
        private String message = "请稍等...";
        private Context mContext;
        private boolean cancelable = true;
        /**
         * 当前正在进行的任务
         */
        private Disposable subscription;
        /**
         * 背景是否暗淡黑色
         */
        private boolean dimBehind = false;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setCancelable(boolean b) {
            cancelable = b;
            return this;
        }

        /**
         * 背景是否暗淡黑色.默认true
         *
         * @param dimBehind
         * @return
         */
        public Builder setDimBehind(boolean dimBehind) {
            this.dimBehind = dimBehind;
            return this;
        }

        public Builder setSubscription(Disposable subscription) {
            this.subscription = subscription;
            return this;
        }

        public Dialog build() {
            if (mContext == null) {
                return null;
            }
            return new SmNetProgressDialog(this);
        }


        @android.support.annotation.Nullable
        public Dialog show() {
            Dialog dialog = build();
            if (dialog != null) {
                dialog.show();
            }
            return dialog;
        }

    }
}
