package com.xx.module.common.imageload;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.view.SmToast;
import com.xx.module.common.R;
import com.xx.module.common.imageload.photo.CropOptions;
import com.xx.module.common.imageload.photo.ITakePhoto;
import com.xx.module.common.imageload.photo.PhotoImage;
import com.xx.module.common.imageload.photo.PhotoResult;
import com.xx.module.common.view.ImagePageActivity;
import com.xx.module.common.view.ImageShowerActivity;
import com.xx.module.common.view.dialog.SmDefaultDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 功能：打开相册，打开相机，选择多图，打开大图显示
 *
 * @author
 * @date 2016-10-24
 */

public class TakePhotoUtil {
    public static final int ACTIVITY_IMAGEPAGE_REQUEST = 1010;
    public static final int ACTIVITY_IMAGEPAGE_RESULT = 101;
    public static final String VIEW_NAME_HEADER_IMAGE = "image_view";

    /**
     * 获取视频
     *
     * @param activity
     * @param iTakePhoto
     * @param limit
     */
    public static void pickVideo(final Activity activity, final ITakePhoto iTakePhoto, final int limit) {
        //权限检查
        PermissionUtil.checkAndRequestPermissions(activity,
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        iTakePhoto.onPickVideo(limit);
                    }

                    @Override
                    public void onFailed() {
                        SmToast.show(activity, "应用没有获得访问文件的权限");
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * @param activity
     * @param iTakePhoto
     * @param limit      获取的图片个数
     */
    public static void pickImages(final Activity activity, final ITakePhoto iTakePhoto, final int limit) {
        //权限检查
        PermissionUtil.checkAndRequestPermissions(activity,
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        iTakePhoto.onPickMultiple(limit);
                    }

                    @Override
                    public void onFailed() {
                        SmToast.show(activity, "应用没有获得访问文件的权限");
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void pickImageAndCrop(Activity activity, final ITakePhoto iTakePhoto) {
        pickImageAndCrop(activity, iTakePhoto, null);
    }

    /**
     * 从系统相册中读取单张图片并裁剪
     *
     * @param activity
     * @param iTakePhoto
     * @param option
     */
    public static void pickImageAndCrop(final Activity activity, final ITakePhoto iTakePhoto, final CropOptions option) {
        //权限检查
        PermissionUtil.checkAndRequestPermissions(activity,
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
                        dateFormat.applyPattern("yyyy-MM-dd_HH_mm_ss");
                        String url = dateFormat.format(new Date(System.currentTimeMillis()));
                        File outFile = new File(AppUtil.getCacheDir(activity) + "/images", url + "_sm_pic.jpg");
                        if (!outFile.getParentFile().exists()) {
                            outFile.getParentFile().mkdirs();
                        }
                        if (!outFile.exists()) {
                            try {
                                outFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Uri outputUri = Uri.fromFile(outFile);
                        CropOptions options;
                        if (option == null) {
                            options = new CropOptions.Builder()
                                    .setOutputX(500)
                                    .setOutputY(500)
                                    .setWithOwnCrop(true)
                                    .create();
                        } else {
                            options = option;
                        }
                        iTakePhoto.onPickFromGalleryWithCrop(outputUri, options);
                    }

                    @Override
                    public void onFailed() {
                        SmToast.show(activity, "应用没有获得读写内存的权限");
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * 选择文件
     *
     * @param activity
     * @param requestCode
     */
    public static void pickFileFromSystemChooser(final Activity activity, final int requestCode) {
        PermissionUtil.checkAndRequestPermissions(activity, new PermissionUtil.SimpleOnPermissionRequestCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    activity.startActivityForResult(intent, requestCode);
                } catch (android.content.ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    SmToast.show(activity, "请安装文件管理器");
                }
            }

            @Override
            public void onFailed() {
                new SmDefaultDialog.Builder(activity)
                        .setMessage("应用没有获得读取文件权限。请前往设置开启文件读写权限。")
                        .setPositiveButton("前往", new SmDefaultDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialog, int resId) {
                                dialog.dismiss();
                                PermissionUtil.openPermissionSettingActivity(activity);
                            }
                        }).show();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void takePhoto(final Activity activity, final ITakePhoto iTakePhoto) {
        //权限检查
        PermissionUtil.checkAndRequestPermissions(activity,
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        File file = getTakePhotoImageOutFile();
                        final Uri outputUri = Uri.fromFile(file);
                        iTakePhoto.onPickFromCapture(outputUri);
                    }

                    @Override
                    public void onFailed() {
                        SmToast.show(activity, "照相机权限未获取");
                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);

    }

    /**
     * 指定照相机拍照输出文件(写到相机目录下)
     *
     * @return
     */
    @NonNull
    private static File getTakePhotoImageOutFile() {
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        dateFormat.applyPattern("yyyy-MM-dd_HH_mm_ss");
        String url = dateFormat.format(new Date(System.currentTimeMillis()));
        String path;
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        if (TextUtils.isEmpty(path)) {
            path = Environment.getDataDirectory().getPath();
        }
        File file = new File(path + "/Camera", url + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void takePhotoAndCrop(Activity activity, final ITakePhoto iTakePhoto) {
        takePhotoAndCrop(activity, iTakePhoto, null);
    }

    public static void takePhotoAndCrop(final Activity activity, final ITakePhoto iTakePhoto, final CropOptions option) {
        //权限检查
        PermissionUtil.checkAndRequestPermissions(activity, new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        File outFile = getTakePhotoImageOutFile();
                        final Uri outputUri = Uri.fromFile(outFile);
                        CropOptions options;
                        if (option == null) {
                            options = new CropOptions.Builder()
                                    .setAspectX(1)
                                    .setAspectY(1)
                                    .setOutputX(500)
                                    .setOutputY(500)
                                    .setWithOwnCrop(true)
                                    .create();
                        } else {
                            options = option;
                        }
                        iTakePhoto.onPickFromCaptureWithCrop(outputUri, options);
                    }

                    @Override
                    public void onFailed() {
                        SmToast.show(activity, "照相机权限未获取");
                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA);

    }

    /**
     * 打开图片浏览器，选择删除。<p>
     * 请在Activity的{@link Activity#onActivityResult(int, int, Intent)} 中，调用{@link TakePhotoUtil#onImagePageActivityResult(int, int, Intent, OnImageResultCallBack)}获取回调信息
     *
     * @param activity
     * @param imgUrls  图片url们
     * @param position 立即打开这组图片中的第几个
     */
    public static void openImageSelectPage(Activity activity, final ArrayList<String> imgUrls, int position) {
        Intent intent = new Intent(activity, ImagePageActivity.class);
        intent.putExtra(Extra.IMAGE_POSITION, position);
        intent.putStringArrayListExtra(Extra.IMAGES, imgUrls);
        activity.startActivityForResult(intent, ACTIVITY_IMAGEPAGE_REQUEST);
        activity.overridePendingTransition(R.anim.photo_enter_anim, 0);
    }


    public static class Extra {
        public static final String IMAGES = "com.mrkj.sm.IMAGES";
        public static final String IMAGE_POSITION = "com.mrkj.sm.IMAGE_POSITION";
    }

    /**
     * 打开图片浏览器，选择删除。<p>
     * 请在Activity的{@link Activity#onActivityResult(int, int, Intent)}中，调用{@link TakePhotoUtil#onImagePageActivityResult(int, int, Intent, OnImageResultCallBack)}获取回调信息
     *
     * @param fragment
     * @param imgUrls  图片url们
     * @param position 立即打开这组图片中的第几个
     */
    public static void openImageSelectPage(Fragment fragment, ArrayList<String> imgUrls, int position) {
        Intent intent = new Intent(fragment.getContext(), ImagePageActivity.class);
        intent.putExtra(Extra.IMAGE_POSITION, position);
        intent.putStringArrayListExtra(Extra.IMAGES, imgUrls);
        fragment.startActivityForResult(intent, ACTIVITY_IMAGEPAGE_REQUEST);
        if (fragment.getActivity() != null) {
            fragment.getActivity().overridePendingTransition(R.anim.photo_enter_anim, 0);
        }
    }


    /**
     * 图片查看并删除，结果返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param onImageResultCallBack
     */
    public static void onImagePageActivityResult(int requestCode, int resultCode, Intent data,
                                                 OnImageResultCallBack onImageResultCallBack) {
        if (requestCode == ACTIVITY_IMAGEPAGE_REQUEST && resultCode == ACTIVITY_IMAGEPAGE_RESULT) {
            if (data != null && data.hasExtra("bundle2")) {
                Bundle bundle = data.getBundleExtra("bundle2");
                if (onImageResultCallBack != null) {
                    onImageResultCallBack.onResult((ArrayList<String>) bundle.getSerializable("list"));
                }
            }
        }
    }


    /**
     * 打开图片浏览器,仅查看大图。<p>
     *
     * @param activity
     * @param imgUrls  图片url们
     * @param position 立即打开这组图片中的第几个
     */
    public static void openImagesShower(Activity activity, final String[] imgUrls, int position) {
        if (position >= imgUrls.length) {
            position = 0;
        }

        Intent intent = new Intent(activity, ImageShowerActivity.class);
        intent.putExtra("urls", imgUrls);
        intent.putExtra("index", position);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.photo_enter_anim, 0);
    }


    public static void openImagesShower(Activity activity, int initPosition, final String[] imgUrls) {
        Intent intent = new Intent(activity, ImageShowerActivity.class);
        intent.putExtra("urls", imgUrls);
        intent.putExtra("index", initPosition);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.photo_enter_anim, 0);
    }

    /**
     * 打开图片浏览器,仅查看大图。<p>
     *
     * @param fragment
     * @param imgUrls  图片url们
     * @param position 立即打开这组图片中的第几个
     */
    public static void openImagesShower(Fragment fragment, final String[] imgUrls, int position) {
        Intent intent = new Intent(fragment.getContext(), ImageShowerActivity.class);
        intent.putExtra("urls", imgUrls);
        intent.putExtra("index", position);
        fragment.startActivity(intent);
        fragment.getActivity().overridePendingTransition(R.anim.photo_enter_anim, R.anim.photo_exit_anim);
    }


    /**
     * 从ITakePhoto中拿到照片结果，转成路径集合
     *
     * @param result
     * @return
     */
    public static List<String> dealWithTResult(PhotoResult result) {
        List<String> images = new ArrayList<>();
        List<PhotoImage> tImages = result.getImages();
        for (PhotoImage t : tImages) {
            String path;
            path = t.getCompressPath();
            if (TextUtils.isEmpty(path)) {
                path = t.getOriginalPath();
            }
            String realPath;
            //android 7.0之后通过FileProvider获取路径可能头部有其他路径
            ///sm/storage/emulated/0/sm/images/2017-04-12_20_01_57.jpg
            if (path.contains("/storage/emulated/0/")) {
                String temp[] = path.split("/storage/emulated/0/");
                if (temp.length == 1) {
                    realPath = path;
                } else {
                    realPath = "/storage/emulated/0/" + temp[1];
                }
            } else {
                realPath = path;
            }
            images.add(realPath.endsWith(".gif") ? realPath : BitmapUtil.bitmapScaled(realPath, 1024, 1024));
        }
        return images;
    }

    public interface OnImageResultCallBack {
        void onResult(ArrayList<String> images);
    }


}
