package com.xx.module.common.imageload;


public class ImageLoader {
    private static IImageLoader mLoader;

    private ImageLoader() {
    }


    public static void init(IImageLoader loader) {
        mLoader = loader;
    }




    public static IImageLoader getInstance() {
        return mLoader;
    }




}
