package com.xx.module.common.imageload.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author
 * @date 2018/1/18 0018
 */

public interface ITakePhoto {
    void onPickVideo(int limit);

    /**
     * 图片多选
     *
     * @param limit 最多选择图片张数的限制
     */
    void onPickMultiple(int limit);


    /**
     * 从相册中获取图片（不裁剪）
     */
    void onPickFromGallery();

    /**
     * 从相册中获取图片并裁剪
     *
     * @param outPutUri 图片裁剪之后保存的路径
     * @param options   裁剪配置
     */
    void onPickFromGalleryWithCrop(Uri outPutUri, CropOptions options);

    /**
     * 从相机获取图片(不裁剪)
     *
     * @param outPutUri 图片保存的路径
     */
    void onPickFromCapture(Uri outPutUri);

    /**
     * 从相机获取图片并裁剪
     *
     * @param outPutUri 图片裁剪之后保存的路径
     * @param options   裁剪配置
     */
    void onPickFromCaptureWithCrop(Uri outPutUri, CropOptions options);

    /**
     * 裁剪图片
     *
     * @param imageUri  要裁剪的图片
     * @param outPutUri 图片裁剪之后保存的路径
     * @param options   裁剪配置
     */
    void onCrop(Uri imageUri, Uri outPutUri, CropOptions options);


    void onCreate(Bundle savedInstanceState);

    void onSaveInstanceState(Bundle outState);

    /**
     * 处理拍照或从相册选择的图片或裁剪的结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * 拍照结果监听接口
     */
    interface TakeResultListener {
        void takeSuccess(PhotoResult result);

        void takeFail(PhotoResult result, String msg);

        void takeCancel();
    }
}
