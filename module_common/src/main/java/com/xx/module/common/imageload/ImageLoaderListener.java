package com.xx.module.common.imageload;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * @author
 * @date 2018/1/17 0017
 */

public abstract class ImageLoaderListener<T extends Drawable> {
    ImageView view;

    public ImageLoaderListener() {
    }

    public ImageLoaderListener(ImageView view) {
        this.view = view;
    }

    public abstract void onSuccess(T data);

    public void onLoadFailed() {
    }

    /**
     * 此回调只有在加载有缩略图的时候才有值
     * {@link IImageLoader#loadWithThumb(SmContextWrap, String, String, ImageLoaderListener)}
     */
    public void onProgress(int progress) {
    }

    public ImageView getView() {
        return view;
    }
}
