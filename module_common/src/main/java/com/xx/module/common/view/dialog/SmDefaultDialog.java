package com.xx.module.common.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.xx.module.common.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * @Function 该类功能：应用默认的提示弹窗
 * @Author
 * @Date 2017/3/27
 */

public class SmDefaultDialog extends Dialog {
    private AlertParams P;

    private SmDefaultDialog(AlertParams P) {
        super(P.contextWeak.get(), R.style.custom_dialog_style);
        this.P = P;
    }

    private void init() {
        if (P == null) {
            dismiss();
            return;
        }
        setContentView(R.layout.dialog_default_sm);
        if (!P.dimBehind && getWindow() != null) { //背景全亮
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        TextView titleTv = findViewById(R.id.dialog_sm_title);
        TextView messageTv = findViewById(R.id.dialog_sm_content);
        TextView mPositiveButton = findViewById(R.id.dialog_sm_positive);
        TextView mNegativeButton = findViewById(R.id.dialog_sm_negative);


        mPositiveButton.setVisibility(P.showPositiveButton ? View.VISIBLE : View.GONE);
        mNegativeButton.setVisibility(P.showNegativeButton ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(P.title)) {
            titleTv.setText(P.title);
        }
        messageTv.setText(P.message);
        //确定按钮
        if (!TextUtils.isEmpty(P.mPositiveButtonText)) {
            mPositiveButton.setText(P.mPositiveButtonText);
        }
        RxView.clicks(mPositiveButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        if (P.mPositiveButtonListener != null) {
                            P.mPositiveButtonListener.onClick(SmDefaultDialog.this, R.id.dialog_sm_positive);
                        }
                    }
                });
        //取消按钮
        if (!TextUtils.isEmpty(P.mNegativeButtonText)) {
            mNegativeButton.setText(P.mNegativeButtonText);
        }
        RxView.clicks(mNegativeButton)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        if (P.mNegativeButtonListener != null) {
                            P.mNegativeButtonListener.onClick(SmDefaultDialog.this, R.id.dialog_sm_negative);
                        } else {
                            SmDefaultDialog.this.dismiss();
                        }
                    }
                });
        setCanceledOnTouchOutside(P.canCancelOutside);
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        P = null;
    }

    public static class Builder {
        AlertParams P;

        public Builder(Context context) {
            P = new AlertParams(context);
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            P.mPositiveButtonListener = listener;
            P.mPositiveButtonText = text;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            P.mNegativeButtonListener = listener;
            P.mNegativeButtonText = text;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            P.title = title;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            P.message = message;
            return this;
        }

        public Builder cancelOutside(boolean cancel) {
            P.canCancelOutside = cancel;
            return this;
        }

        public Builder setDimBehind(boolean dimBehind) {
            P.dimBehind = dimBehind;
            return this;
        }

        public Builder showPositiveButton(boolean show) {
            P.showPositiveButton = show;
            return this;
        }

        public Builder showNegativeButton(boolean show) {
            P.showNegativeButton = show;
            return this;
        }

        @android.support.annotation.Nullable
        public SmDefaultDialog show() {
            SmDefaultDialog defaultDialog = create();
            if (defaultDialog != null) {
                defaultDialog.show();
            }
            return defaultDialog;
        }

        @android.support.annotation.Nullable
        public SmDefaultDialog create() {
            SmDefaultDialog defaultDialog = null;
            if (P.contextWeak != null && P.contextWeak.get() != null) {
                defaultDialog = new SmDefaultDialog(P);
                defaultDialog.init();
            }

            return defaultDialog;
        }
    }


    private static class AlertParams {
        private CharSequence mPositiveButtonText;
        OnClickListener mPositiveButtonListener;
        private CharSequence mNegativeButtonText;
        OnClickListener mNegativeButtonListener;
        private CharSequence title;
        private CharSequence message;
        private boolean canCancelOutside = true;
        private boolean showPositiveButton = true;
        private boolean showNegativeButton = true;
        private WeakReference<Context> contextWeak;
        /**
         * 是否背景暗淡
         */
        private boolean dimBehind = true;

        AlertParams(Context context) {
            contextWeak = new WeakReference<>(context);
        }
    }

    public interface OnClickListener {
        void onClick(Dialog dialog, int resId);
    }
}
