package com.xx.video_dev.common.glide;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * @author
 * @date 2018/1/16 0016
 */

public class GlideRoundTransform extends BitmapTransformation {
    private static final String ID = GlideRoundTransform.class.getName();
    float roundRadius;

    public GlideRoundTransform() {
        this(0);
    }

    public GlideRoundTransform(float radius) {
        roundRadius = radius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap result = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        if (!toTransform.isRecycled()) {
            paint.setShader(new BitmapShader(toTransform, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, outWidth, outHeight);
            canvas.drawRoundRect(rectF, roundRadius, roundRadius, paint);
        }
        return result;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
            //使用 ByteBuffer 来包含基本参数到你的 updateDiskCacheKey 实现中
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            byteBuffer.put(0, (byte) roundRadius);
            byte[] radiusData = byteBuffer.array();
            messageDigest.update(radiusData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
