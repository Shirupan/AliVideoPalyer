package com.xx.module.video.view.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.video.R;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;

import fm.jiecao.jcvideoplayer_lib.JCUserActionStandard;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * @author someone
 * @date 2019-06-06
 */
public abstract class SmVideoControl extends FrameLayout implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        View.OnTouchListener, TextureView.SurfaceTextureListener {
    public static final String TAG = "SmVideoControl";

    public static int FULLSCREEN_ORIENTATION_W_H = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    public static int FULLSCREEN_ORIENTATION_H_W = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_PREPARING = 1;
    public static final int CURRENT_STATE_PREPARING_CHANGING_URL = 2;
    public static final int CURRENT_STATE_PLAYING = 3;
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 4;
    public static final int CURRENT_STATE_PAUSE = 5;
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    public static final int CURRENT_STATE_ERROR = 7;

    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;


    protected boolean isVideoRendingStart = false;
    /**
     * 是否已经提示过wifi环境播放
     */
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;

    public static final int FULLSCREEN_ID = 33797;
    public static final int TINY_ID = 33798;


    public static boolean ACTION_BAR_EXIST = true;
    public static boolean TOOL_BAR_EXIST = true;

    public static final int SCREEN_WINDOW_FULLSCREEN = 2;
    public static final int SCREEN_WINDOW_TINY = 3;

    protected static Timer UPDATE_PROGRESS_TIMER;
    protected static Timer UPDATE_CONTROL_TIME_TIMER;

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected Handler mHandler;

    protected boolean mTouchingProgressBar;
    protected float mDownX;
    protected float mDownY;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected boolean mChangeBrightness;
    protected int mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;
    protected int mSeekTimePosition;

    public static final int THRESHOLD = 80;
    private ProgressTimerTask mProgressTimerTask;
    SmVcResizeTextureView mTextureView;

    public SmVideoControl(Context context) {
        super(context);
        init(context);
    }

    public SmVideoControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmVideoControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public int getLayoutId() {
        return R.layout.sm_layout_video_play_control;
    }

    public ImageView startButton;
    public SeekBar progressBar;
    public ImageView fullscreenButton;
    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup textureViewContainer;
    public ViewGroup topContainer, bottomContainer;
    public ProgressBar hintProgressBar;
    public TextView clarityTv;
    public ProgressBar loadingBar;
    public ImageView coverIv;
    public TextView retryTv;

    public int currentState = -1;
    public int currentScreen = SCREEN_WINDOW_TINY;

    private void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mHandler = new Handler();

        coverIv = findViewById(R.id.sm_video_control_cover);
        clarityTv = findViewById(R.id.sm_vc_clarity);
        clarityTv.setVisibility(GONE);
        retryTv = findViewById(R.id.sm_retry_text);

        loadingBar = findViewById(R.id.sm_vc_loading);
        loadingBar.setVisibility(GONE);
        startButton = findViewById(R.id.sm_vc_start);
        fullscreenButton = findViewById(R.id.sm_vc_fullscreen);
        fullscreenButton.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.sm_vc_bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.sm_vc_current);
        totalTimeTextView = findViewById(R.id.sm_vc_total);
        bottomContainer = findViewById(R.id.sm_vc_layout_bottom);
        textureViewContainer = findViewById(R.id.sm_video_control_surface_container);
        topContainer = findViewById(R.id.sm_vc_layout_top);
        hintProgressBar = findViewById(R.id.sm_video_control_progress);
        hintProgressBar.setVisibility(GONE);
        hintProgressBar.setProgress(0);

        retryTv.setOnClickListener(this);
        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);
        findViewById(R.id.sm_vc_back).setOnClickListener(this);

        try {
            if (isCurrentControl()) {
                NORMAL_ORIENTATION = ((AppCompatActivity) context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isCurrentControl() {//虽然看这个函数很不爽，但是干不掉
        return SmVideoPlayerManager.getCurrentControl() != null
                && SmVideoPlayerManager.getCurrentControl() == this;
    }

    public void setTopBarPaddingTop(int size) {
        topContainer.setPadding(0, size, 0, 0);
    }

    public void setCoverImage(@Nullable String coverurl) {
        ImageLoader.getInstance().load(SmContextWrap.obtain(getContext()), coverurl, R.drawable.icon_default_vertical, coverIv);
    }

    private boolean isStartBtnClickByUser;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sm_vc_start || id == R.id.sm_retry_text) {
            isStartBtnClickByUser = true;
            Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
            if (currentState == CURRENT_STATE_NORMAL || currentState == CURRENT_STATE_ERROR) {
                if (isNeedShowWifiDialog()) {
                    showWifiDialog(JCUserActionStandard.ON_CLICK_START_ICON);
                    return;
                }
                startControlBannelTimer();
                setCurrentState(CURRENT_STATE_PREPARING);
                startPlayVideo(true);
            } else if (currentState == CURRENT_STATE_PLAYING) {
                setCurrentState(CURRENT_STATE_PAUSE);
                pause();
            } else if (currentState == CURRENT_STATE_PAUSE) {
                setCurrentState(CURRENT_STATE_PLAYING);
                startPlayVideo(false);
            } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                setCurrentState(CURRENT_STATE_PREPARING);
                startPlayVideo(true);
            }
        } else if (id == R.id.sm_vc_fullscreen) {
            Log.i(TAG, "onClick fullscreen [" + this.hashCode() + "] ");
            if (currentState == CURRENT_STATE_AUTO_COMPLETE) return;
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                //quit fullscreen
                backPress();
            } else {
                Log.d(TAG, "toFullscreenActivity [" + this.hashCode() + "] ");
                startWindowFullscreen();
            }
        } else if (id == R.id.sm_video_control_surface_container && currentState == CURRENT_STATE_ERROR) {
            Log.i(TAG, "onClick surfaceContainer State=Error [" + this.hashCode() + "] ");
            startPlayVideo(true);
        } else if (id == R.id.sm_vc_back) {
            if (getContext() instanceof Activity) {
                ((Activity) getContext()).onBackPressed();
            }
        }
    }


    protected void setVideoSize(int videoWidth, int videoHeight) {
        mTextureView.setVideoSize(new Point(videoWidth, videoHeight));
    }

    /**
     * 设置当前播放状态，并更新按钮状态
     *
     * @param state
     */
    protected void setCurrentState(int state) {
        retryTv.setVisibility(GONE);
        loadingBar.setVisibility(GONE);
        startButton.setImageResource(R.drawable.icon_video_click_play_selector);
        currentState = state;
        if (currentState == CURRENT_STATE_PREPARING || currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            //正在准备
            loadingBar.setVisibility(VISIBLE);
            startButton.setVisibility(GONE);
        } else if (currentState == CURRENT_STATE_PLAYING) {
            requestAudioFocus();
            //正在播放
            startProgressTimer();
            fullscreenButton.setVisibility(VISIBLE);
            coverIv.setVisibility(GONE);
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.icon_video_click_pause_selector);
        } else if (currentState == CURRENT_STATE_ERROR) {
            //播放错误
            if (getCurrentPositionWhenPlaying() == 0) {
                coverIv.setVisibility(VISIBLE);
            } else {
                coverIv.setVisibility(GONE);
            }
            retryTv.setVisibility(VISIBLE);
        } else if (currentState == CURRENT_STATE_PAUSE) {
            //暂停
            cancelProgressTimer();
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            //自动完成
            onCompletion();
            progressBar.setProgress(100);
            hintProgressBar.setProgress(100);
            retryTv.setVisibility(VISIBLE);
            startButton.setVisibility(VISIBLE);
            coverIv.setVisibility(VISIBLE);
            showOrHideContainer(true, false);
        } else if (currentState == CURRENT_STATE_NORMAL) {
            showOrHideContainer(true, false);
        }
    }

    /**
     * 向系统申请音频通道
     */
    private void requestAudioFocus() {
        SmVideoPlayerManager.requestAudioFocus(getContext(), mHandler);
    }

    /**
     * 取消音频通道监听
     */
    private void abandonAudioFocusRequest() {
        SmVideoPlayerManager.abandonAudioFocusRequest(getContext(), mHandler);
    }

    protected int getCurrentState() {
        return currentState;
    }

    protected abstract void startPlayVideo(boolean reply);

    private void showWifiDialog(int viewID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayVideo(true);
                WIFI_TIP_DIALOG_SHOWED = true;
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                    dialog.dismiss();
                    clearFullscreenLayout();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                    dialog.dismiss();
                    clearFullscreenLayout();
                }
            }
        });
        builder.create().show();
    }

    /**
     * 退出全屏播放
     */
    public void clearFullscreenLayout() {
        ViewGroup vp = SmVcUtil.scanForActivity(getContext()).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        View oldT = vp.findViewById(TINY_ID);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        if (oldT != null) {
            vp.removeView(oldT);
        }
        showSupportActionBar(getContext());
        showNavigation(getContext());
    }

    public static void showSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST) {
            ActionBar ab = SmVcUtil.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.show();
            }
        }
        if (TOOL_BAR_EXIST) {
            SmVcUtil.getAppCompActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static void hideSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST) {
            ActionBar ab = SmVcUtil.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.hide();
            }
        }
        if (TOOL_BAR_EXIST) {
            SmVcUtil.getAppCompActivity(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private static int lastSystemUiVisibility;

    private static void hideNavigation(Context context) {
        lastSystemUiVisibility = SmVcUtil.getAppCompActivity(context).getWindow().getDecorView().getSystemUiVisibility();
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN //hide statusBar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; //hide navigationBar
        SmVcUtil.getAppCompActivity(context).getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    private static void showNavigation(Context context) {
        SmVcUtil.getAppCompActivity(context).getWindow().getDecorView().setSystemUiVisibility(lastSystemUiVisibility);
    }

    /**
     * 判断当前文件是否是网络文件，并且如果网络不是在wifi环境下，应该返回true
     *
     * @return
     */
    protected abstract boolean isNeedShowWifiDialog();


    public boolean backPress() {
        Log.i(TAG, "backPress");
        clearTimer();
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY)
            return false;
        if (SmVideoPlayerManager.getSecondFloor() != null) {
            startTinyScreen();
            return true;
        } else {
            //小屏情况按返回键，则退出播放
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            currentState = CURRENT_STATE_NORMAL;
            stop();
            SmVideoPlayerManager.setFirstFloor(null);
        }
        return false;
    }


    protected abstract void onChangeTiny(SmVideoControl control);

    protected void clearTimer() {
        stopControlBannelTimer();
        cancelProgressTimer();
    }

    /**
     * 回退到小屏
     */
    private void startTinyScreen() {
        SmVideoControl control = SmVideoPlayerManager.getSecondFloor();
        //当前是全屏控制器
        //则需要销毁，并将mTextureView交给小屏渲染画面
        control.textureViewContainer.removeView(mTextureView);
        SmVideoControl smallControl = SmVideoPlayerManager.getFirstFloor();
        smallControl.setCurrentState(currentState);
        smallControl.setTextureView(mTextureView);
        smallControl.startControlBannelTimer();
        if (control.currentState == CURRENT_STATE_AUTO_COMPLETE) {
            control.coverIv.setVisibility(VISIBLE);
        }
        control.onChangeTiny(smallControl);
        clearFullscreenLayout();
        SmVcUtil.getAppCompActivity(getContext()).setRequestedOrientation(NORMAL_ORIENTATION);
        SmVideoPlayerManager.setSecondFloor(null);
    }

    /**
     * 开启全屏
     */
    public void startWindowFullscreen() {
        SmVideoPlayerManager.setFirstFloor(this);
        Log.i(TAG, "startWindowFullscreen " + " [" + this.hashCode() + "] ");
        hideSupportActionBar(getContext());
        hideNavigation(getContext());

        if (mTextureView != null && mTextureView.getWidth() > mTextureView.getHeight()) {
            SmVcUtil.getAppCompActivity(getContext()).setRequestedOrientation(FULLSCREEN_ORIENTATION_W_H);
        } else {
            SmVcUtil.getAppCompActivity(getContext()).setRequestedOrientation(FULLSCREEN_ORIENTATION_H_W);
        }
        ViewGroup vp = SmVcUtil.scanForActivity(getContext()).findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(FULLSCREEN_ID);
        if (old != null) {
            vp.removeView(old);
        }
        if (mTextureView != null) {
            textureViewContainer.removeView(mTextureView);
        }
        try {
            //构造一个新的control，添加到Activity根布局最顶层
            Constructor<SmVideoControl> constructor = (Constructor<SmVideoControl>) SmVideoControl.this.getClass().getConstructor(Context.class);
            SmVideoControl videoPlayer = constructor.newInstance(getContext());
            videoPlayer.setId(FULLSCREEN_ID);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(videoPlayer, lp);
            videoPlayer.currentScreen = JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN;
            videoPlayer.fullscreenButton.setVisibility(View.VISIBLE);
            videoPlayer.setTextureView(mTextureView);
            videoPlayer.setCurrentState(currentState);
            onStartFullScreen(videoPlayer);
            videoPlayer.startControlBannelTimer();
            SmVideoPlayerManager.setSecondFloor(videoPlayer);
            // onStateNormal();
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected abstract void onStartFullScreen(SmVideoControl videoPlayer);

    public void setupTextureView() {
        mTextureView = new SmVcResizeTextureView(getContext());
        mTextureView.setSurfaceTextureListener(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        textureViewContainer.addView(mTextureView, lp);
    }


    private void setTextureView(SmVcResizeTextureView view) {
        mTextureView = view;
        if (mTextureView == null) {
            return;
        }
        //将mTextureView附着到全屏到control中
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        textureViewContainer.addView(mTextureView, layoutParams);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int id = v.getId();
        if (id == R.id.sm_video_control_surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
                    mTouchingProgressBar = true;
                    mDownX = x;
                    mDownY = y;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mChangeBrightness = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                                cancelProgressTimer();
                                stopControlBannelTimer();
                                if (absDeltaX >= THRESHOLD) {
                                    // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                                    // 否则会因为mediaplayer的状态非法导致App Crash
                                    if (currentState != CURRENT_STATE_ERROR) {
                                        mChangePosition = true;
                                        mGestureDownPosition = getCurrentPositionWhenPlaying();
                                    }
                                } else {
                                    //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                                    if (mDownX < mScreenWidth * 0.5f) {//左侧改变亮度
                                        mChangeBrightness = true;
                                        WindowManager.LayoutParams lp = SmVcUtil.getAppCompActivity(getContext()).getWindow().getAttributes();
                                        if (lp.screenBrightness < 0) {
                                            try {
                                                mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                                Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                            } catch (Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            mGestureDownBrightness = lp.screenBrightness * 255;
                                            Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                                        }
                                    } else {//右侧改变声音
                                        mChangeVolume = true;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    }
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        int totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        String seekTime = SmVcUtil.stringForTime(mSeekTimePosition);
                        String totalTime = SmVcUtil.stringForTime(totalTimeDuration);
                        showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                    if (mChangeVolume) {
                        deltaY = -deltaY;
                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                        //dialog中显示百分比
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                        showVolumeDialog(-deltaY, volumePercent);
                    }

                    if (mChangeBrightness) {
                        deltaY = -deltaY;
                        int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                        WindowManager.LayoutParams params = SmVcUtil.getAppCompActivity(getContext()).getWindow().getAttributes();
                        if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                            params.screenBrightness = 1;
                        } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                            params.screenBrightness = 0.01f;
                        } else {
                            params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                        }
                        SmVcUtil.getAppCompActivity(getContext()).getWindow().setAttributes(params);
                        //dialog中显示百分比
                        int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                        showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
                    mTouchingProgressBar = false;
                    dismissProgressDialog();
                    dismissVolumeDialog();
                    dismissBrightnessDialog();
                    if (mChangePosition) {
                        onProgressSeekTo(mSeekTimePosition);
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        progressBar.setProgress(progress);
                    }
                    if (mChangeVolume) {

                    }
                    //打开上下控制面板
                    showOrHideContainer(!isControlShow, currentState == CURRENT_STATE_PLAYING);
                    break;
            }
        }
        return false;
    }

    private AnimatorSet containerAnim;
    private TimerTask controlBannelTask;
    private boolean isControlShow;

    /**
     * 开启或者关闭控制面板
     */
    private void showOrHideContainer(boolean show, boolean timeToHide) {
        if (containerAnim != null && containerAnim.isRunning()) {
            return;
        }
        isControlShow = show;
        int upStart, upEnd;
        int bottomStart, bottomEnd;
        stopControlBannelTimer();
        if (show) {
            //显示动画
            startButton.setVisibility(VISIBLE);
            hintProgressBar.setVisibility(GONE);
            upStart = -topContainer.getMeasuredHeight();
            upEnd = 0;
            bottomStart = bottomContainer.getMeasuredHeight();
            bottomEnd = 0;
            if (timeToHide) {
                startControlBannelTimer();
            }
        } else {
            //隐藏动画
            startButton.setVisibility(GONE);
            hintProgressBar.setVisibility(VISIBLE);
            upStart = 0;
            upEnd = -topContainer.getMeasuredHeight();
            bottomStart = 0;
            bottomEnd = bottomContainer.getMeasuredHeight();
        }

        ValueAnimator upAnim = ValueAnimator.ofInt(upStart, upEnd);
        upAnim.setDuration(500);
        upAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                topContainer.setTranslationY(value);
            }
        });

        ValueAnimator bottomAnim = ValueAnimator.ofInt(bottomStart, bottomEnd);
        bottomAnim.setDuration(500);
        bottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                bottomContainer.setTranslationY(value);
            }
        });
        containerAnim = new AnimatorSet();
        containerAnim.play(upAnim).with(bottomAnim);
        containerAnim.start();
    }

    private void startControlBannelTimer() {
        stopControlBannelTimer();
        controlBannelTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showOrHideContainer(false, false);
                    }
                });
            }
        };
        UPDATE_CONTROL_TIME_TIMER = new Timer();
        UPDATE_CONTROL_TIME_TIMER.schedule(controlBannelTask, 3000);
    }

    private void stopControlBannelTimer() {
        if (controlBannelTask != null) {
            controlBannelTask.cancel();
            controlBannelTask = null;
        }
        if (UPDATE_CONTROL_TIME_TIMER != null) {
            UPDATE_CONTROL_TIME_TIMER.cancel();
            UPDATE_CONTROL_TIME_TIMER = null;
        }
    }

    protected abstract void dismissBrightnessDialog();

    protected abstract void dismissVolumeDialog();

    protected abstract void dismissProgressDialog();

    /**
     * 显示亮度变化提示
     *
     * @param brightnessPercent
     */
    protected abstract void showBrightnessDialog(int brightnessPercent);

    /**
     * 显示音频变化提示
     *
     * @param deltaY
     * @param volumePercent
     */
    protected abstract void showVolumeDialog(float deltaY, int volumePercent);

    /**
     * 显示进度变化提示
     *
     * @param deltaX            x轴滑动距离
     * @param seekTime
     * @param seekTImePosition
     * @param totalTime
     * @param totalTimeDuration
     */
    protected abstract void showProgressDialog(float deltaX, String seekTime, int seekTImePosition, String totalTime, int totalTimeDuration);

    public void startProgressTimer() {
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 400);
    }


    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void stop() {
        cancelProgressTimer();
        stopControlBannelTimer();
        abandonAudioFocusRequest();
    }

    public void release() {
        stop();
    }

    public void pause() {
        if (currentState == CURRENT_STATE_PLAYING && !isStartBtnClickByUser) {
            setCurrentState(CURRENT_STATE_PAUSE);
        }
        isStartBtnClickByUser = false;
    }

    public void resume() {
        if (currentState == CURRENT_STATE_PAUSE && !isStartBtnClickByUser) {
            setCurrentState(CURRENT_STATE_PLAYING);
        }
        isStartBtnClickByUser = false;
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE || currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int position = getCurrentPositionWhenPlaying();
                        int duration = getDuration();
                        int progress = position * 100 / (duration == 0 ? 1 : duration);
                        setProgressAndText(progress, position, duration);
                    }
                });
            }
        }
    }

    /**
     * 获取视频的总长度
     *
     * @return
     */
    protected abstract int getDuration();

    /**
     * 获取当前视频播放的位置
     *
     * @return
     */
    public abstract int getCurrentPositionWhenPlaying();

    public void setProgressAndText(int progress, int position, int duration) {
        Log.d(TAG, "video progress:" + progress + ", position:" + position + ", duration:" + duration);
        if (!mTouchingProgressBar) {
            if (progress != 0) progressBar.setProgress(progress);
            hintProgressBar.setProgress(progress);
        }
        if (position != 0) currentTimeTextView.setText(SmVcUtil.stringForTime(position));
        totalTimeTextView.setText(SmVcUtil.stringForTime(duration));
    }

    private int lastSeekProgress;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            lastSeekProgress = progress;
            int time = lastSeekProgress * getDuration() / 100;
            currentTimeTextView.setText(SmVcUtil.stringForTime(time));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        stopControlBannelTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            //防止与父层级滑动冲突
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) return;
        int time = lastSeekProgress * getDuration() / 100;
        onProgressSeekTo(time);
        lastSeekProgress = 0;
        Log.i(TAG, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    protected abstract void onProgressSeekTo(int time);

    /**
     * 缓存加载进度(还在播放)
     *
     * @param progress
     */
    protected void setLoadingSecondaryProgress(int progress) {
        hintProgressBar.setSecondaryProgress(progress);
        progressBar.setSecondaryProgress(progress);
    }

    private Dialog bufferDialog;

    /**
     * 缓存已不够播放，暂停中加载数据
     *
     * @param progress
     */
    protected void setBufferingUpdate(int progress) {
        if (bufferDialog != null) {
            bufferDialog.dismiss();
        }
        bufferDialog = showBufferProgressDialog(progress);
        if (bufferDialog != null && !bufferDialog.isShowing()) {
            bufferDialog.show();
        }
    }

    /**
     * 缓存加载进度
     *
     * @param progress
     */
    protected abstract Dialog showBufferProgressDialog(int progress);

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public Surface mSurface;

    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = getCurrentPositionWhenPlaying();
            SmVcUtil.saveProgress(getContext(), getPlayUrl(), position);
        }
        onStateNormal();
        isVideoRendingStart = false;
        stop();
    }

    public void onStateNormal() {
        currentState = CURRENT_STATE_NORMAL;
        cancelProgressTimer();
    }


    protected abstract String getPlayUrl();
}
