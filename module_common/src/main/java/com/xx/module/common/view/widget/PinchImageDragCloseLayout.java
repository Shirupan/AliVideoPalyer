package com.xx.module.common.view.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * 结合PinchImageView处理滑动冲突，用于单指下滑的时候关闭页面
 */
public class PinchImageDragCloseLayout extends FrameLayout {
    private PinchImageView pinchImageView;
    private int mTouchSlopSquare;

    private OnViewMoveListener onViewMoveListener;

    public PinchImageDragCloseLayout(Context context) {
        super(context);
    }

    public PinchImageDragCloseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinchImageDragCloseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setOnViewMoveListener(OnViewMoveListener onViewMoveListener) {
        this.onViewMoveListener = onViewMoveListener;
    }


    public void setPinchImageView(PinchImageView pinchImageView) {
        this.pinchImageView = pinchImageView;
    }

    /**
     * 这一组动作出现过多指操作
     */
    private boolean hasShownTwoFinger;
    private PointF mDownPointF = new PointF();
    private boolean hasDown;

    @Override

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchSlopSquare == 0) {
            mTouchSlopSquare = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        if (pinchImageView == null) {
            return super.onInterceptTouchEvent(ev);
        }
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (ev.getPointerCount() >= 2) {
            hasShownTwoFinger = true;
        }
        if (action == MotionEvent.ACTION_POINTER_UP) {
            //其他手指抬起
            ev.setAction(MotionEvent.ACTION_UP);
            onInterceptTouchEvent(MotionEvent.obtain(ev));
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hasShownTwoFinger = false;
        } else if (action == MotionEvent.ACTION_MOVE && checkIsOriginalMatrix() && ev.getPointerCount() <= 1) {
            //点击事件
            if (Math.abs(mDownPointF.x - ev.getX()) < mTouchSlopSquare
                    || Math.abs(mDownPointF.y - ev.getY()) < mTouchSlopSquare) {
                return super.onInterceptTouchEvent(ev);
            } else if (onViewMoveListener != null && !hasShownTwoFinger) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownPointF.set(ev.getX(), ev.getY());
            if (onViewMoveListener != null) {
                onViewMoveListener.onDown(this, ev.getX(), ev.getY());
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (!hasDown) {
                hasDown = true;
                mDownPointF.set(ev.getX(), ev.getY());
                if (onViewMoveListener != null) {
                    onViewMoveListener.onDown(this, ev.getX(), ev.getY());
                }
            }
            if (onViewMoveListener != null && ev.getPointerCount() <= 1 && !hasShownTwoFinger) {
                onViewMoveListener.onMove(this, ev.getX(), ev.getY());
                super.onTouchEvent(ev);
                return true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (ev.getPointerCount() <= 1) {
                hasDown = false;
                if (onViewMoveListener != null && Math.abs(pinchImageView.getTranslationY()) > getMeasuredHeight() / 5) {
                    onViewMoveListener.onClose(this, ev.getX(), ev.getY());
                } else {
                    pinchImageView.setTranslationY(0);
                    if (onViewMoveListener != null) {
                        onViewMoveListener.onCancel();
                    }
                }
            }
        }

        return super.onTouchEvent(ev);
    }

    private float[] currentF = new float[9];
    private float[] originalF = new float[9];

    /**
     * 判断图像是否是输入原始缩放状态
     *
     * @return
     */
    private boolean checkIsOriginalMatrix() {
        Matrix current = pinchImageView.getCurrentImageMatrix(PinchImageView.MathUtils.matrixTake());
        current.getValues(currentF);
        Matrix original = pinchImageView.getInnerMatrix(PinchImageView.MathUtils.matrixTake());
        original.getValues(originalF);
        boolean result = true;
        for (int i = 0; i < currentF.length; i++) {
            //取小数点后两位进行比较
            if ((int) (currentF[i] * 100) != (int) (originalF[i] * 100)) {
                result = false;
                break;
            }
        }
        return result;
    }


    public interface OnViewMoveListener {
        void onDown(View view, float eventX, float eventY);

        void onMove(View view, float eventX, float eventY);

        void onClose(View view, float eventX, float eventY);

        void onCancel();
    }
}
