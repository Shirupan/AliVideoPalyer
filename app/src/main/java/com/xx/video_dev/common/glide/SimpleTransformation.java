package com.xx.video_dev.common.glide;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * @author
 * @date 2018/1/17 0017
 */

public class SimpleTransformation extends BitmapTransformation {
    private static final String ID = SimpleTransformation.class.getName();

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return toTransform;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
