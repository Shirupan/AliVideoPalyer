package com.xx.video_dev.common.glide;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.xx.module.common.imageload.glide.FastBlur;
import com.xx.module.common.imageload.glide.RSBlur;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * @author
 * @date 2018/1/16 0016
 */

public class GlideBlurTransformation extends BitmapTransformation {
    private static final String ID = GlideBlurTransformation.class.getName();

    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;

    private Context mContext;
    private boolean isFromTextView;

    private int mRadius;
    private int mSampling;

    public GlideBlurTransformation(Context context) {
        this(context, MAX_RADIUS, DEFAULT_DOWN_SAMPLING, false);
    }

    public GlideBlurTransformation(Context context, int radius, boolean isFromTextView) {
        this(context, radius, DEFAULT_DOWN_SAMPLING, isFromTextView);
    }


    public GlideBlurTransformation(Context context, int radius, int sampling, boolean isFromTextView) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        mRadius = radius;
        mSampling = sampling;
        this.isFromTextView = isFromTextView;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;
        Bitmap bitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        if (!toTransform.isRecycled()) {
            canvas.drawBitmap(toTransform, 0, 0, paint);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (isFromTextView) {
                    if (mContext == null) {
                        return null;
                    }
                    bitmap = RSBlur.blur(mContext, bitmap, mRadius);
                } else {
                    bitmap = FastBlur.blur(bitmap, mRadius, false);
                }
            } else {
                bitmap = FastBlur.blur(bitmap, mRadius, false);
            }

        }
        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
            //使用 ByteBuffer 来包含基本参数到你的 updateDiskCacheKey 实现中
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.put(0, (byte) mRadius);
            byteBuffer.put(1, (byte) mSampling);
            byte[] radiusData = byteBuffer.array();
            messageDigest.update(radiusData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
