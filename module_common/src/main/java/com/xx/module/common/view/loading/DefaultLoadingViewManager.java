package com.xx.module.common.view.loading;


import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.common.R;

/**
 * @Function 该类功能：
 * @Author
 * @Date 2017/3/17
 */

public class DefaultLoadingViewManager implements ILoadingView {
    //加载画面的控件
    private LinearLayout loading_layout;
    private TextView loading_btn;
    private ImageView loading_iv, loading_failed;
    private TextView loading_tv;
    private boolean isloading;
    private boolean isLoadingViewShow;
    private boolean isEmpty;
    private String emptyStr;
    private String emptyBtnStr;
    private int emptyImageResource;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public DefaultLoadingViewManager(FrameLayout rootView) {
        isloading = true;
        initView(rootView);
    }

    private void initView(FrameLayout parent) {
        if (parent == null) {
            return;
        }
        View loadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading, parent, false);
        if (loadingView == null) {
            return;
        }
        parent.addView(loadingView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loading_layout = loadingView.findViewById(R.id.loading_layout);
        if (loading_layout == null) {
            return;
        }
        loading_layout.setVisibility(View.VISIBLE);
        isLoadingViewShow = true;
        loading_iv = loading_layout.findViewById(R.id.loading_img);
        loading_failed = loading_layout.findViewById(R.id.loading_failed);
        loading_tv = loading_layout.findViewById(R.id.loading_text);
        loading_btn = loading_layout.findViewById(R.id.loading_btn);
    }


    @Override
    public void setEmptyMessage(String message, String btnStr) {
        this.emptyStr = message;
        this.emptyBtnStr = btnStr;
    }

    @Override
    public void loading() {
        if (loading_layout == null) {
            SmLogger.i("no loading layout");
            return;
        }
        isloading = true;
        isLoadingViewShow = true;
        SmLogger.i("show layout loading");
        loading_layout.setVisibility(View.VISIBLE);
        loading_btn.setVisibility(View.GONE);
        loading_tv.setText("正在加载中...");
        loading_iv.setVisibility(View.VISIBLE);
        loading_failed.setVisibility(View.GONE);
        loading_iv.post(new Runnable() {
            @Override
            public void run() {
                if (loading_iv.getVisibility() == View.VISIBLE) {
                    if (loading_iv.getDrawable() instanceof AnimationDrawable) {
                        AnimationDrawable drawable = (AnimationDrawable) loading_iv.getDrawable();
                        drawable.start();
                    } else {
                        Animation animation = new RotateAnimation(0, 360, loading_iv.getMeasuredWidth() / 2, loading_iv.getMeasuredHeight() / 2);
                        animation.setRepeatCount(Animation.INFINITE);
                        animation.setRepeatMode(Animation.RESTART);
                        animation.setInterpolator(new LinearInterpolator());
                        animation.setDuration(800);
                        loading_iv.startAnimation(animation);
                    }
                }
            }
        });
    }

    @Override
    public void showNoNet() {
        if (loading_layout == null) {
            SmLogger.e("no loading layout");
            return;
        }
        isloading = false;
        isLoadingViewShow = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                loading_layout.setVisibility(View.VISIBLE);
                loading_btn.setVisibility(View.VISIBLE);
                loading_btn.setText("点击重新刷新");
                loading_tv.setText("网络连接失败");
                loading_failed.setVisibility(View.VISIBLE);
                loading_failed.setImageResource(R.drawable.icon_empty);
                Animation animation = loading_iv.getAnimation();
                if (animation != null) {
                    animation.cancel();
                }
                loading_iv.setVisibility(View.GONE);
                if (loading_btn != null) {
                    loading_btn.setOnClickListener(refreshListener);
                }
                if (loading_tv != null) {
                    loading_tv.setOnClickListener(refreshListener);
                }
            }
        });
    }

    @Override
    public void showFailed() {
        showFailed("数据加载失败");
    }

    @Override
    public void showFailed(final String msg) {
        if (loading_layout == null) {
            SmLogger.e("no loading layout");
            return;
        }
        isloading = false;
        isLoadingViewShow = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SmLogger.i("show layout failed");
                loading_layout.setVisibility(View.VISIBLE);
                loading_btn.setVisibility(View.VISIBLE);
                loading_btn.setText("点击重新刷新");
                loading_tv.setText(msg);
                Animation animation = loading_iv.getAnimation();
                if (animation != null) {
                    animation.cancel();
                }
                loading_iv.setVisibility(View.GONE);
                loading_failed.setVisibility(View.VISIBLE);
                loading_failed.setImageResource(R.drawable.icon_empty);
                if (loading_btn != null) {
                    loading_btn.setOnClickListener(refreshListener);
                }
            }
        });

       /* if (loading_tv != null) {
            loading_tv.setOnClickListener(refreshListener);
        }*/
    }

    @Override
    public void showEmpty() {
        showEmpty(null, TextUtils.isEmpty(emptyStr) ? "没有数据" : emptyStr);
    }

    @Override
    public void showEmpty(String msg) {
        showEmpty(null, msg);
    }

    @Override
    public void showEmpty(final Drawable drawable, final String message) {
        if (loading_layout == null) {
            SmLogger.e("no loading layout");
            return;
        }
        isloading = false;
        isLoadingViewShow = true;
        isEmpty = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SmLogger.i("show layout empty");
                loading_layout.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(emptyBtnStr)) {
                    loading_btn.setText(emptyBtnStr);
                    loading_btn.setVisibility(View.VISIBLE);
                }
                loading_tv.setText(message != null ? message : emptyStr);
                Animation animation = loading_iv.getAnimation();
                if (animation != null) {
                    animation.cancel();
                }
                loading_iv.setVisibility(View.GONE);
                loading_failed.setVisibility(View.VISIBLE);
                if (drawable != null) {
                    loading_failed.setImageDrawable(drawable);
                } else if (emptyImageResource != 0) {
                    loading_failed.setImageResource(emptyImageResource);
                } else {
                    loading_failed.setImageResource(R.drawable.icon_empty);
                }

                View.OnClickListener listener;
                listener = emptyClickListner;
                if (loading_btn != null) {
                    if (listener == null) {
                        loading_btn.setVisibility(View.GONE);
                    } else {
                        loading_btn.setOnClickListener(listener);
                        loading_btn.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void dismiss() {
        if (loading_layout == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                loading_btn.setVisibility(View.GONE);
                loading_tv.setVisibility(View.GONE);
                if (!isLoadingViewShow) {
                    isloading = false;
                    return;
                }
                isloading = false;
                isLoadingViewShow = false;

                if (loading_layout.getVisibility() == View.GONE) {
                    return;
                }
                removeLoadingView();
            }
        });
    }

    private void removeLoadingView() {
        loading_layout.setVisibility(View.GONE);
        if (loading_layout != null) {
            ViewGroup viewGroup = (ViewGroup) loading_layout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(loading_layout);
                loading_layout = null;
            }
        }
    }

    @Override
    public View getLayoutView() {
        return loading_layout;
    }

    @Override
    public boolean isLoading() {
        return isloading;
    }

    @Override
    public boolean isLoadingEmpty() {
        return isEmpty;
    }


    /**
     * @return loading异常后的按钮事件
     */
    @Override
    public void setOnRefreshClickListener(View.OnClickListener l) {
        refreshListener = l;
    }

    private View.OnClickListener refreshListener, emptyClickListner;

    @Override
    public void setOnEmptyClickListener(View.OnClickListener listener) {
        emptyClickListner = listener;
    }

    @Override
    public boolean isLoadingViewShow() {
        return isLoadingViewShow;
    }

    @Override
    public void setMargin(int left, int top, int right, int bottom) {
        if (loading_layout != null) {
            if (loading_layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) loading_layout.getLayoutParams();
                lp.topMargin = top;
                lp.leftMargin = left;
                lp.rightMargin = right;
                lp.bottomMargin = bottom;
                loading_layout.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void post(Runnable function) {
        if (loading_layout != null) {
            loading_layout.post(function);
        }
    }


    public void init(FrameLayout itemView) {
        initView(itemView);
    }

    public void setEmptyImage(@DrawableRes int resource) {
        emptyImageResource = resource;
    }
}
