package com.xx.video_dev.common.glide;


import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.xx.lib.db.entity.SmContextWrap;

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
     * {@link com.xx.module.common.imageload.IImageLoader#loadWithThumb(SmContextWrap, String, String, com.xx.module.common.imageload.ImageLoaderListener)}
     */
    public void onProgress(int progress) {
    }

    public ImageView getView() {
        return view;
    }
}
