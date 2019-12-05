package com.xx.video_dev.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.mrkj.lib.net.base.OkHttpUtil;
import com.xx.video_dev.common.glide.OkHttpUrlLoader;

import java.io.InputStream;

/**
 * Glide v4 注解生成Generated API
 *
 * @author
 * @date 2018/1/17 0017
 */

@GlideModule
public class SmGlideAppModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(OkHttpUtil.getOkHttpClient()));
    }
}
