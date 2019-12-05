package com.xx.module.common.imageload.photo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.util.GetPathFromUri4kitkat;
import com.mrkj.lib.common.view.SmToast;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class PhotoUtils {
    private static final String TAG = PhotoUtils.class.getName();


    /**
     * 将Image集合转换成Uri集合
     *
     * @param images
     * @return
     */
    public static ArrayList<Uri> converPhotoImageToUri(Context context, ArrayList<Image> images) throws IllegalArgumentException {
        ArrayList<Uri> uris = new ArrayList<>();
        for (Image image : images) {
            uris.add(FileProvider.getUriForFile(context, PhotoConstant.getFileProviderName(context), new File(image.path)));
        }
        return uris;
    }

    /**
     * 将Image集合转换成PhotoImage集合
     *
     * @param images
     * @return
     */
    public static ArrayList<PhotoImage> getPhotoImagesWithImages(ArrayList<Image> images, PhotoImage.FromType fromType) {
        ArrayList<PhotoImage> PhotoImages = new ArrayList<>();
        for (Image image : images) {
            PhotoImages.add(PhotoImage.obtain(image.path, fromType));
        }
        return PhotoImages;
    }

    /**
     * 将Uri集合转换成PhotoImage集合
     *
     * @param uris
     * @return
     */
    public static ArrayList<PhotoImage> getPhotoImagesWithUris(ArrayList<Uri> uris, PhotoImage.FromType fromType) {
        ArrayList<PhotoImage> PhotoImages = new ArrayList<>();
        for (Uri uri : uris) {
            PhotoImages.add(PhotoImage.obtain(uri, fromType));
        }
        return PhotoImages;
    }

    /**
     * @param context
     * @param uris     content:// 等等此类的uri
     * @param fromType
     * @return
     */
    public static ArrayList<PhotoImage> getPhotoImagesWithContentUris(Context context, ArrayList<Uri> uris, PhotoImage.FromType fromType) {
        ArrayList<PhotoImage> PhotoImages = new ArrayList<>();
        for (Uri uri : uris) {
            Uri uri1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uri1 = Uri.parse(GetPathFromUri4kitkat.getPath(context, uri));
            } else {
                uri1 = uri;
            }
            PhotoImages.add(PhotoImage.obtain(uri1, fromType));
        }
        return PhotoImages;
    }

    /**
     * @param contextWrap
     * @param intentWap
     */
    public static void startActivityForResult(ContextWrap contextWrap, IntentWap intentWap) {
        if (contextWrap.getFragment() != null) {
            contextWrap.getFragment().startActivityForResult(intentWap.getIntent(), intentWap.getRequestCode());
        } else {
            contextWrap.getActivity().startActivityForResult(intentWap.getIntent(), intentWap.getRequestCode());
        }
    }

    /**
     * 安全地发送Intent
     *
     * @param contextWrap
     * @param intentWapList 要发送的Intent以及候选Intent
     * @param defaultIndex  默认发送的Intent
     * @param isCrop        是否为裁切照片的Intent
     */
    public static void sendIntentBySafely(ContextWrap contextWrap, List<IntentWap> intentWapList, int defaultIndex, boolean isCrop) {
        if (defaultIndex + 1 > intentWapList.size()) {
            SmToast.show(contextWrap.getActivity(), "打开错误");
            return;
        }
        IntentWap intentWap = intentWapList.get(defaultIndex);
        List result = queryIntentActivities(contextWrap, intentWap.getIntent());
        if (result.isEmpty()) {
            sendIntentBySafely(contextWrap, intentWapList, ++defaultIndex, isCrop);
        } else {
            startActivityForResult(contextWrap, intentWap);
        }
    }

    /**
     * 拍照前检查是否有相机
     **/
    public static void captureBySafely(final ContextWrap contextWrap, final IntentWap intentWap) {
        List result = queryIntentActivities(contextWrap, intentWap.getIntent());
        if (result.isEmpty()) {
            SmToast.show(contextWrap.getActivity(), "没有相机");
        } else {
            if (contextWrap.getActivity() == null || PermissionUtil.hasPermissions(contextWrap.getContext(), Manifest.permission.CAMERA)) {
                startActivityForResult(contextWrap, intentWap);
            } else {
                PermissionUtil.checkAndRequestPermissions(contextWrap.getActivity(), new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        startActivityForResult(contextWrap, intentWap);
                    }
                }, Manifest.permission.CAMERA);
            }
        }
    }

    private static List<ResolveInfo> queryIntentActivities(ContextWrap contextWrap, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return contextWrap.getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_ALL);
        } else {
            return contextWrap.getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        }
    }

    /**
     * 通过第三方工具裁切照片，当没有第三方裁切工具时，会自动使用自带裁切工具进行裁切
     *
     * @param contextWrap
     * @param imageUri
     * @param outPutUri
     * @param options
     */
    public static void cropWithOtherAppBySafely(ContextWrap contextWrap, Uri imageUri, Uri outPutUri, CropOptions options) {
        Intent nativeCropIntent = IntentUtils.getCropIntentWithOtherApp(imageUri, outPutUri, options);
        List result = queryIntentActivities(contextWrap, nativeCropIntent);
        if (result.isEmpty()) {
            cropWithOwnApp(contextWrap, imageUri, outPutUri, options);
        } else {
            startActivityForResult(contextWrap, new IntentWap(IntentUtils.getCropIntentWithOtherApp(imageUri, outPutUri, options), PhotoConstant.RC_CROP));
        }
    }

    /**
     * 通过TakePhoto自带的裁切工具裁切图片
     *
     * @param contextWrap
     * @param imageUri
     * @param outPutUri
     * @param options
     */
    public static void cropWithOwnApp(ContextWrap contextWrap, Uri imageUri, Uri outPutUri, CropOptions options) {
        UCrop uCrop = UCrop.of(imageUri, outPutUri);
        if (options.getAspectX() * options.getAspectY() > 0) {
            uCrop.withAspectRatio(options.getAspectX(), options.getAspectY());
        }
        if (options.getOutputX() * options.getOutputY() > 0) {
            uCrop.withMaxResultSize(options.getOutputX(), options.getOutputY());
        }
        uCrop.start(contextWrap.getActivity());
    }

    /**
     * 是否裁剪之后返回数据
     **/
    public static boolean isReturnData() {
        String release = Build.VERSION.RELEASE;
        int sdk = Build.VERSION.SDK_INT;
        Log.i(TAG, "release:" + release + "sdk:" + sdk);
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.toLowerCase().contains("lenovo")) {//对于联想的手机返回数据
                return true;
            }
        }
//        if (sdk>=21){//5.0或以上版本要求返回数据
//            return  true;
//        }
        return false;
    }


}
