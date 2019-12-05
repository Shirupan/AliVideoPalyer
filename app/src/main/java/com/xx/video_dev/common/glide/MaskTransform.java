package com.xx.video_dev.common.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * @author someone
 * @date 2019-04-29
 */
public class MaskTransform extends BitmapTransformation {
    private static final String ID = GlideCircleTransform.class.getName();

    @DrawableRes
    private int maskRId;
    private Context mContext;

    public MaskTransform(Context context, @DrawableRes int rId) {
        maskRId = rId;
        mContext = context;
    }


    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Paint paint = new Paint();
        //两图交会，显示上层
        PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        Bitmap maskBitmap = BitmapFactory.decodeResource(mContext.getResources(), maskRId);
        //缩放至ImageView框大小
        maskBitmap = scaleImage(maskBitmap, outWidth, outHeight, false, 0);
        toTransform = scaleImage(toTransform, outWidth, outHeight, true, 0);

        int saveFlags = Canvas.ALL_SAVE_FLAG;
        Bitmap result = Glide.get(mContext).getBitmapPool().get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.saveLayer(0, 0, outWidth, outHeight, null, saveFlags);
        //画遮罩
        canvas.drawBitmap(maskBitmap, 0, 0, paint);
        //设置遮罩效果，上层显示
        paint.setXfermode(xfermode);
        canvas.drawBitmap(toTransform, 0, 0, paint);
        paint.setXfermode(null);
        canvas.restore();
        return result;
    }

    private Bitmap scaleImage(Bitmap bitmap, int w, int h, boolean equalRatio, int offset) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int new_width;
            int new_height;
            if (equalRatio) {
                if (width != height) {
                    if (width > height) {
                        new_height = h - offset;
                        new_width = width * new_height / height;
                    } else {
                        new_width = w - offset;
                        new_height = height * new_width / width;
                    }
                } else {
                    new_width = w;
                    new_height = h;
                }
            } else {
                new_width = w;
                new_height = h;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, new_width, new_height, true);
        }
        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
            //使用 ByteBuffer 来包含基本参数到你的 updateDiskCacheKey 实现中
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            byteBuffer.put(0, (byte) maskRId);
            byte[] radiusData = byteBuffer.array();
            messageDigest.update(radiusData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
