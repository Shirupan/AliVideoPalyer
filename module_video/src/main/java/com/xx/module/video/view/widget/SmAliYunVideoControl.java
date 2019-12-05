package com.xx.module.video.view.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.video.R;
import com.xx.module.video.model.SmMediaCacheManager;

/**
 * @author someone
 * @date 2019-06-06
 */
public class SmAliYunVideoControl extends SmVideoControl {


    private AliyunVodPlayer mPlayer;
    protected Dialog mBrightnessDialog;
    protected ProgressBar mDialogBrightnessProgressBar;
    protected TextView mDialogBrightnessTextView;

    protected Dialog mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;
    protected TextView mDialogVolumeTextView;
    protected ImageView mDialogVolumeImageView;

    protected Dialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;

    protected Dialog mBufferDialog;
    protected TextView mBufferTotalTime;


    public SmAliYunVideoControl(Context context) {
        super(context);
    }

    public SmAliYunVideoControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void startPlayVideo(boolean reply) {
        SmVideoPlayerManager.setIsNeedPlay(true);
        if (SmVideoPlayerManager.getMediaPrepared()) {
            reallyPlay(reply);
        } else {
            mPlayer.prepareAsync(mPlaySource);
        }
    }

    @Override
    protected boolean isNeedShowWifiDialog() {
        return AppUtil.getNetworkInfoType(getContext()) != ConnectivityManager.TYPE_WIFI;
    }


    @Override
    protected void onChangeTiny(SmVideoControl control) {

    }


    @Override
    protected void onStartFullScreen(SmVideoControl videoPlayer) {
        if (videoPlayer instanceof SmAliYunVideoControl) {
            ((SmAliYunVideoControl) videoPlayer).setPlayer(mPlayer, getPlayUrl());
        }
    }

    @Override
    protected void dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
    }

    @Override
    protected void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    @Override
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void showBrightnessDialog(int brightnessPercent) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.sm_vc_dialog_brightness, null);
            mDialogBrightnessTextView = ((TextView) localView.findViewById(R.id.tv_brightness));
            mDialogBrightnessProgressBar = ((ProgressBar) localView.findViewById(R.id.brightness_progressbar));
            mBrightnessDialog = createDialogWithView(localView);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        mDialogBrightnessTextView.setText(brightnessPercent + "%");
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
        // onCLickUiToggleToClear();
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.sm_vc_dialog_volume, null);
            mDialogVolumeImageView = ((ImageView) localView.findViewById(R.id.volume_image_tip));
            mDialogVolumeTextView = ((TextView) localView.findViewById(R.id.tv_volume));
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mVolumeDialog = createDialogWithView(localView);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }
        if (volumePercent <= 0) {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.jc_close_volume);
        } else {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.jc_add_volume);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        mDialogVolumeTextView.setText(volumePercent + "%");
        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.sm_vc_dialog_progress, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = createDialogWithView(localView);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : (seekTimePosition * 100 / totalTimeDuration));
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.jc_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.jc_backward_icon);
        }
    }

    public Dialog createDialogWithView(View localView) {
        Dialog dialog = new Dialog(getContext(), R.style.jc_style_dialog_progress);
        dialog.setContentView(localView);
        Window window = dialog.getWindow();
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(localLayoutParams);
        return dialog;
    }

    @Override
    public void resume() {
        super.resume();
        if (currentScreen == CURRENT_STATE_PLAYING) {
            if (SmVideoPlayerManager.getPlayerCallback() != null) {
                SmVideoPlayerManager.getPlayerCallback().onStart();
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (SmVideoPlayerManager.getPlayerCallback() != null) {
            SmVideoPlayerManager.getPlayerCallback().onPause();
        }
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (SmVideoPlayerManager.getPlayerCallback() != null) {
            SmVideoPlayerManager.getPlayerCallback().onStop();
        }
        if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void release() {
        super.release();
        if (currentScreen != SCREEN_WINDOW_FULLSCREEN) {
            try {
                mPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayer = null;
        }
    }


    @Override
    protected int getDuration() {
        return (int) mPlayer.getDuration();
    }

    @Override
    public int getCurrentPositionWhenPlaying() {
        return mPlayer == null ? 0 : (int) mPlayer.getCurrentPosition();
    }

    private int mSeekPosition;

    @Override

    protected void onProgressSeekTo(int time) {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.seekTo(time);
                mPlayer.start();
            } else {
                mSeekPosition = time;
                SmVideoPlayerManager.setIsNeedPlay(true);
                mPlayer.prepareAsync(mPlaySource);
            }
        }
    }

    @Override
    protected Dialog showBufferProgressDialog(int progress) {
        if (mBufferDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.sm_vc_dialog_buffer, null);
            mBufferTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mBufferDialog = createDialogWithView(localView);
        }
        if (!mBufferDialog.isShowing()) {
            mBufferDialog.show();
        }
        mDialogTotalTime.setText(progress + "%");
        return mBufferDialog;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureAvailable(surface, width, height);
        if (mPlayer != null) {
            mPlayer.setSurface(mSurface);
            if (getCurrentState() == -1) {
                setCurrentState(CURRENT_STATE_NORMAL);
            }
        }
    }


    @Override
    protected String getPlayUrl() {
        return mPlayer.getMediaInfo().getVideoId();
    }

    private AliyunLocalSource mPlaySource;

    public void setPlayer(AliyunVodPlayer player, String url) {
        // url = "http://qiniu.ddznzj.com/media/sm/181219/181219210123503.mp4";
        // url = "http://qiniu.ddznzj.com/20190518181945344874.mp4";
        mPlayer = player;
        if (mPlayer != null && !mPlayer.isPlaying()) {
            //缓存目录
            SmMediaCacheManager.setupAliyunVideoCachePath(getContext(), player);
        }
        AliyunLocalSource.AliyunLocalSourceBuilder buidler = new AliyunLocalSource.AliyunLocalSourceBuilder();
        buidler.setSource(url);
        mPlaySource = buidler.build();
        initPlayer();
        if (!TextUtils.isEmpty(url) && mPlayer.getMediaInfo() != null && mPlayer.getMediaInfo().getVideoId().equals(url) && mPlayer.isPlaying()) {
            startProgressTimer();
            return;
        }
        if (mSurface != null) {
            setCurrentState(CURRENT_STATE_NORMAL);
        }
        if (mTextureView == null) {
            setupTextureView();
        }
        SmVideoPlayerManager.setMediaPrepared(false);
        SmVideoPlayerManager.setIsNeedPlay(false);
        mPlayer.prepareAsync(mPlaySource);
    }


    private void initPlayer() {
        mPlayer.setAutoPlay(false);
        mPlayer.setCirclePlay(false);
        if (mSurface != null) {
            mPlayer.setSurface(mSurface);
        }
        if (currentScreen == SCREEN_WINDOW_TINY) {
            SmVideoPlayerManager.setFirstFloor(this);
        } else if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            SmVideoPlayerManager.setSecondFloor(this);
        }
        mPlayer.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mPlayer.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                SmVideoPlayerManager.setMediaPrepared(true);
                SmVideoPlayerManager.setVideoSize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
                totalTimeTextView.setText(SmVcUtil.stringForTime(getDuration()));
                if (SmVideoPlayerManager.getPlayerCallback() != null) {
                    SmVideoPlayerManager.getPlayerCallback().onPrepared();
                }
                if (SmVideoPlayerManager.getIsNeedPlay()) {
                    reallyPlay(true);
                }
            }
        });
        mPlayer.setOnBufferingUpdateListener(new IAliyunVodPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(int i) {
                Log.d(TAG, "player onBufferingUpdate: " + i);
                setBufferingUpdate(i);
            }
        });
        mPlayer.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener() {
            @Override
            public void onFirstFrameStart() {
                if (SmVideoPlayerManager.getPlayerCallback() != null) {
                    SmVideoPlayerManager.getPlayerCallback().onStart();
                }
                SmVideoPlayerManager.setCurrentState(CURRENT_STATE_PLAYING);
            }
        });
        mPlayer.setOnLoadingListener(new IAliyunVodPlayer.OnLoadingListener() {
            @Override
            public void onLoadStart() {
                SmLogger.i("mPlayer is onLoadStart");
                setLoadingSecondaryProgress(0);
            }

            @Override
            public void onLoadEnd() {
                SmLogger.i("mPlayer is onLoadEnd");
                setLoadingSecondaryProgress(100);
            }

            @Override
            public void onLoadProgress(int i) {
                SmLogger.i("mPlayer is onLoadProgress :" + i);
                setLoadingSecondaryProgress(i);
            }
        });
        mPlayer.setOnSeekCompleteListener(new IAliyunVodPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                startProgressTimer();
            }
        });
        mPlayer.setOnErrorListener(new IAliyunVodPlayer.OnErrorListener() {
            @Override
            public void onError(int what, int extra, String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                SmVideoPlayerManager.setCurrentState(CURRENT_STATE_ERROR);
                SmVideoPlayerManager.setMediaPrepared(false);
                if (SmVideoPlayerManager.getPlayerCallback() != null) {
                    SmVideoPlayerManager.getPlayerCallback().onError(what, extra, msg);
                }
            }
        });
        mPlayer.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                SmVideoPlayerManager.setCurrentState(CURRENT_STATE_AUTO_COMPLETE);
                SmVideoPlayerManager.setMediaPrepared(false);
                if (SmVideoPlayerManager.getPlayerCallback() != null) {
                    SmVideoPlayerManager.getPlayerCallback().onCompleted();
                }
            }
        });
    }


    private void reallyPlay(boolean reply) {
        Log.d(TAG, "player onPrepared");
        mPlayer.start();
        if (reply) {
            SmVideoPlayerManager.setCurrentState(CURRENT_STATE_PLAYING_BUFFERING_START);
           /* if (mSeekPosition != 0) {
                mPlayer.seekTo(mSeekPosition);
            } else {
                int position = SmVcUtil.getSavedProgress(getContext(), getPlayUrl());
                mPlayer.seekTo(position);
            }*/
        } else {
            SmVideoPlayerManager.setCurrentState(CURRENT_STATE_PLAYING);
        }
        SmVideoPlayerManager.setVideoSize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
    }

    public void setPlayerCallback(OnPlayCallback callback) {
        SmVideoPlayerManager.setPlayerCallback(callback);
    }

    public interface OnPlayCallback {
        void onPrepared();

        void onStart();

        /**
         * {@link com.alivc.player.AliyunErrorCode}
         *
         * @param what
         * @param extra
         * @param message
         */
        void onError(int what, int extra, String message);

        void onCompleted();

        void onPause();

        void onStop();
    }
}
