package com.xx.module.common.imageload.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.view.SmToast;
import com.xx.module.common.R;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;


/**
 * @author
 * @date 2018/1/18 0018
 */

public class TakePhotoImpl implements ITakePhoto {
    private static final String TAG = IntentUtils.class.getName();
    private ContextWrap contextWrap;
    private ITakePhoto.TakeResultListener listener;
    private Uri outPutUri;
    private Uri tempUri;
    private CropOptions cropOptions;
    // private TakePhotoOptions takePhotoOptions;

    private PhotoImage.FromType fromType; //CAMERA图片来源相机，OTHER图片来源其他
    /**
     * 是否显示压缩对话框
     */
    private boolean showCompressDialog;

    public TakePhotoImpl(Activity activity, ITakePhoto.TakeResultListener listener) {
        contextWrap = ContextWrap.of(activity);
        this.listener = listener;
    }

    public TakePhotoImpl(Fragment fragment, ITakePhoto.TakeResultListener listener) {
        contextWrap = ContextWrap.of(fragment);
        this.listener = listener;
    }

    private Matisse getMatisse() {
        if (contextWrap.getFragment() != null) {
            return Matisse.from(contextWrap.getFragment());
        } else {
            return Matisse.from(contextWrap.getActivity());
        }
    }

    @Override
    public void onPickVideo(int limit) {
        getMatisse().choose(MimeType.of(MimeType.MP4, MimeType.QUICKTIME))
                .countable(true)
                .theme(R.style.Matisse_SM)
                .maxSelectable(limit)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new TakePhotoGlideEngine())
                .forResult(PhotoConstant.RC_VIDEO);
    }

    @Override
    public void onPickMultiple(int limit) {
        getMatisse().choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .theme(R.style.Matisse_SM)
                .maxSelectable(limit)
                // .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                // .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new TakePhotoGlideEngine())
                .forResult(PhotoConstant.RC_PICK_MULTIPLE);
    }


    @Override
    public void onPickFromGallery() {
        PermissionUtil.checkAndRequestPermissions(contextWrap.getActivity(),
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        selectPicture(false);

                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(contextWrap.getContext(), "未获得读取手机文件内容权限", Toast.LENGTH_SHORT).show();
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onPickFromGalleryWithCrop(Uri outPutUri, CropOptions options) {
        this.cropOptions = options;
        this.outPutUri = outPutUri;
        PermissionUtil.checkAndRequestPermissions(contextWrap.getActivity(),
                new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        selectPicture(true);
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(contextWrap.getContext(), "未获得读取手机文件内容权限", Toast.LENGTH_SHORT).show();
                    }
                }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onPickFromCapture(Uri outPutUri) {
        this.fromType = PhotoImage.FromType.CAMERA;
        if (Build.VERSION.SDK_INT >= 23) {
            this.outPutUri = PhotoUriParse.convertFileUriToFileProviderUri(contextWrap.getActivity(), outPutUri);
        } else {
            this.outPutUri = outPutUri;
        }
        PhotoUtils.captureBySafely(contextWrap, new IntentWap(IntentUtils.getCaptureIntent(this.outPutUri),
                PhotoConstant.RC_PICK_PICTURE_FROM_CAPTURE));
    }

    @Override
    public void onPickFromCaptureWithCrop(Uri outPutUri, CropOptions options) {
        this.fromType = PhotoImage.FromType.CAMERA;
        this.cropOptions = options;
        this.outPutUri = outPutUri;
        if (Build.VERSION.SDK_INT >= 23) {
            this.tempUri = PhotoUriParse.getTempUri(contextWrap.getActivity());
        } else {
            this.tempUri = outPutUri;
        }
        PhotoUtils.captureBySafely(contextWrap, new IntentWap(IntentUtils.getCaptureIntent(this.tempUri),
                PhotoConstant.RC_PICK_PICTURE_FROM_CAPTURE_CROP));
    }

    @Override
    public void onCrop(Uri imageUri, Uri outPutUri, CropOptions options) throws NullPointerException {
        this.outPutUri = outPutUri;
        if (!PhotoImageFiles.checkMimeType(contextWrap.getActivity(), PhotoImageFiles.getMimeType(contextWrap.getActivity(), imageUri))) {
            SmToast.show(contextWrap.getActivity(), "类型错误");
            throw new NullPointerException("onCrop no image");
        }
        cropWithNonException(imageUri, outPutUri, options);
    }

    private void cropWithNonException(Uri imageUri, Uri outPutUri, CropOptions options) {
        this.outPutUri = outPutUri;
        if (options.isWithOwnCrop()) {
            PhotoUtils.cropWithOwnApp(contextWrap, imageUri, outPutUri, options);
        } else {
            PhotoUtils.cropWithOtherAppBySafely(contextWrap, imageUri, outPutUri, options);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            cropOptions = (CropOptions) savedInstanceState.getSerializable("cropOptions");
            showCompressDialog = savedInstanceState.getBoolean("showCompressDialog");
            outPutUri = savedInstanceState.getParcelable("outPutUri");
            tempUri = savedInstanceState.getParcelable("tempUri");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("cropOptions", cropOptions);
        outState.putBoolean("showCompressDialog", showCompressDialog);
        outState.putParcelable("outPutUri", outPutUri);
        outState.putParcelable("tempUri", tempUri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoConstant.RC_PICK_PICTURE_FROM_GALLERY_CROP:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //从相册选择照片并裁剪
                    if (outPutUri == null) {
                        SmToast.show(contextWrap.getContext(), "图片处理错误");
                        return;
                    }
                    try {
                        onCrop(data.getData(), outPutUri, cropOptions);
                    } catch (NullPointerException e) {
                        takeResult(PhotoResult.of(PhotoImage.obtain(outPutUri, fromType)), e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_PICTURE_FROM_GALLERY_ORIGINAL:
                //从相册选择照片不裁剪
                if (resultCode == Activity.RESULT_OK) {
                    takeResult(PhotoResult.of(PhotoImage.obtain(PhotoUriParse.getFilePathWithUri(data.getData(), contextWrap.getActivity()), fromType)));
                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_PICTURE_FROM_DOCUMENTS_ORIGINAL:
                //从文件选择照片不裁剪
                if (resultCode == Activity.RESULT_OK) {
                    takeResult(PhotoResult.of(PhotoImage.obtain(PhotoUriParse.getFilePathWithDocumentsUri(data.getData(), contextWrap.getActivity()), fromType)));
                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_PICTURE_FROM_DOCUMENTS_CROP:
                //从文件选择照片，并裁剪
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        onCrop(data.getData(), outPutUri, cropOptions);
                    } catch (NullPointerException e) {
                        takeResult(PhotoResult.of(PhotoImage.obtain(outPutUri, fromType)), e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_PICTURE_FROM_CAPTURE_CROP:
                //拍取照片,并裁剪
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        onCrop(tempUri, Uri.fromFile(new File(PhotoUriParse.parseOwnUri(contextWrap.getActivity(), outPutUri))), cropOptions);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        takeResult(PhotoResult.of(PhotoImage.obtain(outPutUri, fromType)), e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_PICTURE_FROM_CAPTURE:
                //拍取照片
                if (resultCode == Activity.RESULT_OK) {
                    takeResult(PhotoResult.of(PhotoImage.obtain(PhotoUriParse.getFilePathWithUri(outPutUri, contextWrap.getActivity()), fromType)));
                } else {
                    listener.takeCancel();
                }
                break;
            case UCrop.REQUEST_CROP:
                //裁剪照片返回结果
                if (resultCode == Activity.RESULT_OK) {
                    PhotoImage image = PhotoImage.obtain(PhotoUriParse.getFilePathWithUri(outPutUri, contextWrap.getActivity()), fromType);
                    image.setCropped(true);
                    takeResult(PhotoResult.of(image));
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //裁剪的照片没有保存
                    if (data != null) {
                        Bitmap bitmap = data.getParcelableExtra("data");
                        //获取裁剪的结果数据
                        PhotoImageFiles.writeToFile(bitmap, outPutUri);
                        //将裁剪的结果写入到文件
                        PhotoImage image = PhotoImage.obtain(outPutUri.getPath(), fromType);
                        image.setCropped(true);
                        takeResult(PhotoResult.of(image));
                    } else {
                        listener.takeCancel();
                    }

                } else {
                    listener.takeCancel();
                }
                break;
            case PhotoConstant.RC_PICK_MULTIPLE:
                //多选图片返回结果
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Uri> images = (ArrayList<Uri>) Matisse.obtainResult(data);
                    if (images != null && !images.isEmpty()) {
                        takeResult(PhotoResult.of(PhotoUtils.getPhotoImagesWithContentUris(contextWrap.getActivity(), images, fromType)));
                    } else {
                        listener.takeFail(PhotoResult.of(PhotoUtils.getPhotoImagesWithImages(new ArrayList<Image>(), fromType)), "错误");
                    }

                } else {
                    listener.takeCancel();
                }
                break;
            default:
                break;
        }
    }

    private void takeResult(final PhotoResult result, final String... message) {
        handleTakeCallBack(result, message);
    }

    private void handleTakeCallBack(final PhotoResult result, String... message) {
        if (message.length > 0) {
            listener.takeFail(result, message[0]);
        } else {
            listener.takeSuccess(result);
        }
        clearParams();
    }

    private void selectPicture(boolean isCrop) {
        this.fromType = PhotoImage.FromType.OTHER;
        ArrayList<IntentWap> intentWapList = new ArrayList<>();
        intentWapList.add(new IntentWap(IntentUtils.getPickIntentWithDocuments(), isCrop ? PhotoConstant.RC_PICK_PICTURE_FROM_DOCUMENTS_CROP : PhotoConstant.RC_PICK_PICTURE_FROM_DOCUMENTS_ORIGINAL));
        intentWapList.add(new IntentWap(IntentUtils.getPickIntentWithGallery(), isCrop ? PhotoConstant.RC_PICK_PICTURE_FROM_GALLERY_CROP : PhotoConstant.RC_PICK_PICTURE_FROM_GALLERY_ORIGINAL));
        PhotoUtils.sendIntentBySafely(contextWrap, intentWapList, 1, isCrop);
    }

    private void clearParams() {
        cropOptions = null;
    }
}
