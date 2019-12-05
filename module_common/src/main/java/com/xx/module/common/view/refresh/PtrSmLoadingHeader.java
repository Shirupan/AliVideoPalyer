package com.xx.module.common.view.refresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.module.common.R;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * 经典下拉样式（类微博首页刷新）
 *
 * @Author
 * @Create 2017/2/22
 */
public class PtrSmLoadingHeader extends FrameLayout implements PtrUIHandler, PtrHandler {

    private ImageView mIv;
    private TextView mTv;
    private AnimationDrawable loadingDrawable;
    int backgroundColorRes;
    private View rootView;

    private Drawable mLoadingImg;
    /**
     * 刷新
     */
    private Runnable refreshCallback;

    public PtrSmLoadingHeader(Context context) {
        super(context);
        initViews();
    }

    public PtrSmLoadingHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PtrSmLoadingHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    protected void initViews() {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.layout_ptr_refresh, this);
        rootView = header;
        mIv = header.findViewById(R.id.loading_img);
        mTv = header.findViewById(R.id.loading_text);
        resetView();
    }

    private void resetView() {
        if (mLoadingImg == null) {
            mIv.setImageResource(R.drawable.common_loading_icon);
        }
        Drawable drawable = mIv.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            loadingDrawable = (AnimationDrawable) drawable;
        }
        if (loadingDrawable != null) {
            loadingDrawable.stop();
        }
        if (mIv.getAnimation() != null) {
            mIv.getAnimation().cancel();
        }
    }

    public void setBackgroundColorRes(int backgroundColorRes) {
        this.backgroundColorRes = backgroundColorRes;
        if (rootView != null) {
            rootView.setBackgroundResource(backgroundColorRes);
        }
    }

    public void setLoadingImage(Drawable drawable) {
        mLoadingImg = drawable;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void setTextColor(int color) {
        mTv.setTextColor(color);
    }

    public void setImageResource(@DrawableRes int res) {
        mIv.setImageResource(res);
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        resetView();
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        if (frame.isPullToRefresh()) {
            mTv.setText("下拉刷新");
        } else {
            mTv.setText("下拉刷新");
        }
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        resetView();
        if (loadingDrawable != null) {
            loadingDrawable.start();
        } else {
            mIv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                            0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    anim.setRepeatCount(1000);
                    anim.setDuration(500);
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setRepeatMode(Animation.RESTART);
                    anim.setFillAfter(true);
                    mIv.startAnimation(anim);
                }
            }, 50);
        }

        mTv.setText("努力加载中");
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        if (loadingDrawable != null) {
            loadingDrawable.stop();
        }
        if (mIv.getAnimation() != null) {
            mIv.getAnimation().cancel();
        }
        mTv.setText("加载完成");
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        if (status == PtrFrameLayout.PTR_STATUS_LOADING) {
            return;
        }
        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();
        final int lastPos = ptrIndicator.getLastPosY();
        //拉下来了又往上拉回去，没到刷新
        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromBottomUnderTouch(frame);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            //下拉到触发刷新，但是没有松手
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromTopUnderTouch(frame);
            }
        }

        if (mIv.getAnimation() != null) {
            mIv.getAnimation().cancel();
        }
        if (loadingDrawable == null) {
            int frameIndex = currentPos * 360 / frame.getHeaderHeight();
            mIv.setRotation(frameIndex);
        } else {
            int allIndex = loadingDrawable.getNumberOfFrames();
            if (allIndex <= 0 || frame.getHeaderHeight() == 0) {
                return;
            }
            int frameIndex = currentPos * allIndex / frame.getHeaderHeight();
            Drawable temp = loadingDrawable.getFrame(frameIndex % allIndex);
            mIv.setImageDrawable(temp);
        }
    }

    private void crossRotateLineFromTopUnderTouch(PtrFrameLayout frame) {
        if (!frame.isPullToRefresh()) {
            mTv.setVisibility(VISIBLE);
            mTv.setText("释放刷新");
        }
    }

    private void crossRotateLineFromBottomUnderTouch(PtrFrameLayout frame) {
        mTv.setVisibility(VISIBLE);
        if (frame.isPullToRefresh()) {
            mTv.setText("下拉刷新");
        } else {
            mTv.setText("下拉刷新");
        }
    }


    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
    }


    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }
}