package com.xx.module.common.imageload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.lib.db.entity.SmContextWrap;


/**
 * @author
 * @date 2018/1/17 0017
 */

public interface IImageLoader {


    void loadCircle(SmContextWrap contextWrap, String url, ImageView view, @DrawableRes int defaultResId);

    void loadCircleResource(Context context, @DrawableRes int resId, ImageView view);


    void loadCircleBound(SmContextWrap contextWrap, String url, ImageView view, @ColorRes int colorId, int widthDp, @DrawableRes int defaultResId);

    void loadCircleBoundResource(Context context, @DrawableRes int resId, ImageView view, @ColorRes int colorId, int widthDp);

    /**
     * 对比和设置View的tag
     *
     * @param view
     * @param url
     * @return
     */
    boolean checkTag(View view, String url);

    void load(SmContextWrap contextWrap, String url, @DrawableRes int defaultid, ImageView imageview);

    void load(SmContextWrap contextWrap, String url, @DrawableRes int defaultid, ImageLoaderListener<Drawable> listener);

    void loadUri(SmContextWrap contextWrap, Uri uri, ImageView imageview, @DrawableRes int defaultid);

    void loadUri(SmContextWrap contextWrap, Uri uri, @DrawableRes int defaultid, ImageLoaderListener<Drawable> listener);


    void loadGif(SmContextWrap contextWrap, String url, @DrawableRes int defaultResId, @Nullable ImageLoaderListener<Drawable> listener);

    void loadGif(SmContextWrap contextWrap, String url, @DrawableRes int defaultResId, @Nullable ImageView imageView);

    void loadGifUri(SmContextWrap contextWrap, Uri uri, ImageView imageview, @DrawableRes int defaultResId);


    void loadWithThumb(SmContextWrap contextWrap, String url, String thumb, ImageLoaderListener<Drawable> glideDrawableImageViewTarget);

    void loadBlur(SmContextWrap contextWrap, String url, int radius, @DrawableRes int placeholder, ImageView imageview);

    void loadBlur(SmContextWrap contextWrap, String url, int radius, @DrawableRes int placeholder, ImageLoaderListener<Drawable> listener);

    void loadBlur(SmContextWrap contextWrap, Bitmap bitmap, int radius, @DrawableRes int placeholder, ImageLoaderListener<Drawable> listener);


    void loadRound(SmContextWrap contextWrap, String url, ImageView imageview, float radius, @DrawableRes int placeholder);

    void loadRound(SmContextWrap contextWrap, String url, ImageView imageview, boolean admin, float radius, @DrawableRes int placeholder);


    void loadTextView(SmContextWrap contextWrap, String tag, TextView view, ImageView imageView, boolean isBlur);

    void loadMask(SmContextWrap contextWrap, String url, @DrawableRes int maskId, ImageLoaderListener<Drawable> listener);

    void pause(SmContextWrap contextWrap);


    void resume(SmContextWrap contextWrap);


    void loadThumbnail(Context context, Drawable placeholder, ImageView imageView, Uri uri, int resize);

    void loadAnimatedGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri);

    void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri);

    void loadAnimatedGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri);

    void loadResource(Context context, int resId, int w, int height,
                      @org.jetbrains.annotations.Nullable final ImageLoaderListener<Drawable> listener);

    void loadResource(Context context, @DrawableRes int resId, @org.jetbrains.annotations.Nullable ImageView imageView);

    void clear(Context context);

    Bitmap get(Context context, int width, int height, Bitmap.Config config);
}
