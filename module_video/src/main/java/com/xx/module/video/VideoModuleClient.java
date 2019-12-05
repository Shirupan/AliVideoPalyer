package com.xx.module.video;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.client.BaseClient;
import com.xx.module.video.model.IVideoModel;
import com.xx.module.video.model.VideoModel;
import com.xx.module.video.view.VideoDetailActivity;
import com.xx.module.video.view.VideoTabManagerActivity;
import com.xx.module.video.view.shortvideo.ShortVideoMainActivity;

import java.util.Map;

/**
 * @author someone
 * @date 2019-05-30
 */
public class VideoModuleClient extends BaseClient<IVideoModel> {
    @Override
    protected Class<? extends IVideoModel> getModelClass() {
        return VideoModel.class;
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {
        map.put(AnnotationProcessor.getActivityPath(VideoTabManagerActivity.class), VideoTabManagerActivity.class);
        map.put(AnnotationProcessor.getActivityPath(VideoDetailActivity.class), VideoDetailActivity.class);
        map.put(AnnotationProcessor.getActivityPath(ShortVideoMainActivity.class), ShortVideoMainActivity.class);
    }


    /**
     * 监听系统音频焦点变化
     *
     * @param context
     * @param focusChangeListener
     * @return
     */
    public AudioManager.OnAudioFocusChangeListener registerAudioFocus(final Context context, final OnFocusChangeListener focusChangeListener) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                SmLogger.d("OnAudioFocusChangeListener " + focusChange);
                //如果应用语音焦点丢失，则关闭监听。并停止播放音频
                if (audioManager != null && focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    if (focusChangeListener != null) {
                        focusChangeListener.onLoss();
                    }
                    unRegisterAudioFocus(context, this);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
            builder.setOnAudioFocusChangeListener(listener);
            audioManager.requestAudioFocus(builder.build());
        } else {
            audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        return listener;
    }

    public interface OnFocusChangeListener {
        void onLoss();
    }

    /**
     * 注销监听系统音频焦点变化
     *
     * @param context
     * @param l
     */
    public void unRegisterAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener l) {
        try {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
                builder.setOnAudioFocusChangeListener(l);

                audioManager.abandonAudioFocusRequest(builder.build());
            } else {
                audioManager.abandonAudioFocus(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
