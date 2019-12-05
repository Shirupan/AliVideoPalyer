package com.xx.module.common.imageload.photo;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.xx.module.common.imageload.ImageLoader;
import com.zhihu.matisse.engine.ImageEngine;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class TakePhotoGlideEngine implements ImageEngine {
    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        ImageLoader.getInstance().loadThumbnail(context, placeholder, imageView, uri, resize);
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        ImageLoader.getInstance().loadAnimatedGifThumbnail(context, resize, placeholder, imageView, uri);
    }


    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        ImageLoader.getInstance().loadImage(context, resizeX, resizeY, imageView, uri);
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        ImageLoader.getInstance().loadAnimatedGifImage(context, resizeX, resizeY, imageView, uri);
    }


    @Override
    public boolean supportAnimatedGif() {
        return true;
    }
}
