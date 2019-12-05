package com.xx.video_dev.common.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.mrkj.lib.common.util.ScreenUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * @author
 * @date 2018/1/12 0012
 */

public class GlideCircleTransform extends BitmapTransformation {
    private static final String ID = GlideCircleTransform.class.getName();

    /**
     * 描边颜色
     */
    private int color;
    private int mStrokeWidth;

    public GlideCircleTransform(Context context, @ColorInt int color, int widthDp) {
        this.color = color;
        this.mStrokeWidth = ScreenUtils.dp2px(context, widthDp);
    }

    public int getColor() {
        return color;
    }

    public int getWidth() {
        return mStrokeWidth;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform, Math.min(outWidth, outHeight));
    }


    private Bitmap circleCrop(BitmapPool pool, Bitmap source, int minOut) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;

        Bitmap bitmap = pool.get(size, size, Bitmap.Config.ARGB_8888);
        while (bitmap.isRecycled()) {
            bitmap = pool.get(size, size, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        if (!source.isRecycled()) {
            BitmapShader shader = new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                // source isn't square, move viewport to center
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            canvas.save();
            if (color != 0) {
                float radius = r - mStrokeWidth / 2;
                canvas.restore();
                Paint paint1 = new Paint();
                paint1.setAntiAlias(true);
                paint1.setStyle(Paint.Style.STROKE);
                paint1.setColor(color);
                //  float stroke = width * size / minOut;
                paint1.setStrokeWidth(mStrokeWidth);
                canvas.drawCircle(r, r, radius, paint1);
            }
        }

        return bitmap;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof GlideCircleTransform) {
            GlideCircleTransform other = (GlideCircleTransform) o;
            return mStrokeWidth == other.getWidth() && color == other.getWidth();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
            //使用 ByteBuffer 来包含基本参数到你的 updateDiskCacheKey 实现中
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.put(0, (byte) color);
            byteBuffer.put(1, (byte) mStrokeWidth);
            byte[] radiusData = byteBuffer.array();
            messageDigest.update(radiusData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
