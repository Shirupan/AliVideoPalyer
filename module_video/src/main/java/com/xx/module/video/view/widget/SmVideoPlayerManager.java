package com.xx.module.video.view.widget;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;

import com.mrkj.lib.common.util.SmLogger;

/**
 * @author someone
 * @date 2019-06-06
 */
public class SmVideoPlayerManager {
    public static SmVideoControl FIRST_FLOOR_JCVD;
    public static SmVideoControl SECOND_FLOOR_JCVD;

    public static void setFirstFloor(SmVideoControl jcVideoPlayer) {
        FIRST_FLOOR_JCVD = jcVideoPlayer;
    }

    public static void setSecondFloor(SmVideoControl jcVideoPlayer) {
        SECOND_FLOOR_JCVD = jcVideoPlayer;
    }

    public static SmVideoControl getFirstFloor() {
        return FIRST_FLOOR_JCVD;
    }

    public static SmVideoControl getSecondFloor() {
        return SECOND_FLOOR_JCVD;
    }

    public static SmVideoControl getCurrentControl() {
        if (getSecondFloor() != null) {
            return getSecondFloor();
        }
        return getFirstFloor();
    }


    public static void setCurrentState(int state) {
        if (SmVideoPlayerManager.getFirstFloor() != null) {
            SmVideoPlayerManager.getFirstFloor().setCurrentState(state);
        }
        if (SmVideoPlayerManager.getSecondFloor() != null) {
            SmVideoPlayerManager.getSecondFloor().setCurrentState(state);
        }
    }

    public static void setVideoSize(int videoWidth, int videoHeight) {
        SmVideoControl videoControl = SmVideoPlayerManager.getFirstFloor();
        SmVideoControl fullControl = SmVideoPlayerManager.getSecondFloor();
        if (videoControl != null) {
            videoControl.setVideoSize(videoWidth, videoHeight);
        }
        if (fullControl != null) {
            fullControl.setVideoSize(videoWidth, videoHeight);
        }
    }


    private static boolean ISMEDIAPREPARED;
    private static boolean ISNEEDPLAY;


    public static void setMediaPrepared(boolean isMediaPrepared) {
        ISMEDIAPREPARED = isMediaPrepared;
    }

    public static boolean getMediaPrepared() {
        return ISMEDIAPREPARED;
    }


    public static void setIsNeedPlay(boolean isneedplay) {
        ISNEEDPLAY = isneedplay;
    }

    public static boolean getIsNeedPlay() {
        return ISNEEDPLAY;
    }


    public static void stop() {
        SmVideoControl videoControl = SmVideoPlayerManager.getFirstFloor();
        SmVideoControl fullControl = SmVideoPlayerManager.getSecondFloor();
        if (videoControl != null) {
            videoControl.stop();
        }
        if (fullControl != null) {
            fullControl.stop();
        }
    }

    public static void pause() {
        SmVideoControl videoControl = SmVideoPlayerManager.getFirstFloor();
        SmVideoControl fullControl = SmVideoPlayerManager.getSecondFloor();
        if (videoControl != null) {
            videoControl.pause();
        }
        if (fullControl != null) {
            fullControl.pause();
        }
    }

    public static void onResume() {
        SmVideoControl videoControl = SmVideoPlayerManager.getFirstFloor();
        SmVideoControl fullControl = SmVideoPlayerManager.getSecondFloor();
        if (videoControl != null) {
            videoControl.resume();
        }
        if (fullControl != null) {
            fullControl.resume();
        }
    }

    public static void release() {
        SmVideoControl videoControl = SmVideoPlayerManager.getFirstFloor();
        SmVideoControl fullControl = SmVideoPlayerManager.getSecondFloor();
        if (videoControl != null) {
            videoControl.release();
        }
        if (fullControl != null) {
            fullControl.release();
        }
        releasePlayerCallback();
    }

    private static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    private static AudioManager.OnAudioFocusChangeListener getAudioFocusChangeListener() {
        if (onAudioFocusChangeListener == null) {
            onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            pause();
                            SmLogger.d("AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            pause();
                            SmLogger.d("AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            break;
                    }
                }
            };
        }
        return onAudioFocusChangeListener;
    }


    /**
     * @param context
     * @param handler 回调线程
     */
    public static void requestAudioFocus(Context context, Handler handler) {
        if (onAudioFocusChangeListener == null) {
            onAudioFocusChangeListener = getAudioFocusChangeListener();
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setOnAudioFocusChangeListener(onAudioFocusChangeListener, handler)
                        .build();
                audioManager.requestAudioFocus(request);
            } else {
                audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        }
    }

    public static void abandonAudioFocusRequest(Context context, Handler handler) {
        if (onAudioFocusChangeListener != null) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setOnAudioFocusChangeListener(onAudioFocusChangeListener, handler)
                        .build();
                audioManager.abandonAudioFocusRequest(request);
            } else {
                audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            }
            onAudioFocusChangeListener = null;
        }
    }


    private static SmAliYunVideoControl.OnPlayCallback mPlayCallback;

    public static void setPlayerCallback(SmAliYunVideoControl.OnPlayCallback callback) {
        mPlayCallback = callback;
    }

    public static SmAliYunVideoControl.OnPlayCallback getPlayerCallback() {
        return mPlayCallback;
    }

    public static void releasePlayerCallback() {
        mPlayCallback = null;
    }
}
