package com.xx.module.video.view.shortvideo.tipsview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xx.module.video.R;

public class ErrorView extends FrameLayout {
    public ErrorView(Context context) {
        super(context);
        init();
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sm_alivc_dialog_error, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);
    }


    public void setText(String message) {
        TextView tv = findViewById(R.id.dialog_sm_content);
        if (tv != null) {
            tv.setText(message);
        }
    }

    public void setPositiveButton(String msg, boolean show, OnClickListener listener) {
        TextView btn = findViewById(R.id.dialog_sm_positive);
        if (btn == null) {
            return;
        }
        if (!show) {
            btn.setVisibility(GONE);
        } else {
            btn.setVisibility(VISIBLE);
            btn.setText(msg);
            btn.setOnClickListener(listener);
        }
    }


    public void setNegativeButton(String msg, boolean show, OnClickListener listener) {
        TextView btn = findViewById(R.id.dialog_sm_negative);
        if (btn == null) {
            return;
        }
        if (!show) {
            btn.setVisibility(GONE);
        } else {
            btn.setVisibility(VISIBLE);
            btn.setText(msg);
            btn.setOnClickListener(listener);
        }
    }
}
