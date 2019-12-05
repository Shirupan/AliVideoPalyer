package com.xx.module.common.model;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.loader.file.SmNetProgressDialog;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.entity.ReturnJson;
import com.xx.lib.db.entity.SmShare;
import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.CommonModule;
import com.xx.module.common.R;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.callback.ResultUICallback;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.BaseMediaObject;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


/**
 * @Function 该类功能：第三方分享封装
 * @Author
 * @Date 2017/3/16
 */

public class ThirdShareManager {
    private static Dialog loadingDialog;
    /**
     * 网页json分享
     */
    public static final int SHARE_MODE_WEB = 4;

    public static void onResume() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public static void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Nullable
    public static SHARE_MEDIA getShareMedia(Integer type) {
        SHARE_MEDIA shareMedia = null;
        if (type == null) {
            return shareMedia;
        }
        switch (type) {
            case 1:
                shareMedia = SHARE_MEDIA.WEIXIN;
                break;
            case 2:
                shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case 3:
                shareMedia = SHARE_MEDIA.QQ;
                break;
            case 4:
                shareMedia = SHARE_MEDIA.QZONE;
                break;
            case 5:
                shareMedia = SHARE_MEDIA.SINA;
                break;
        }
        return shareMedia;
    }

    @NotNull
    public static SmShare getDefaultShare(Context context) {
        SmShare smShare = new SmShare();
        smShare.setKind(2);
        smShare.setUrl("https://android.myapp.com/myapp/detail.htm?apkName=" + context.getPackageName());
        // smShare.set(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        smShare.setTitle("好玩有趣的短视频就在这里，快来看看吧～");
        return smShare;
    }

    public interface OnGetShareInfoCallback {
        void onResult(@Nullable SmShare smShare);
    }


    /**
     * @param activity
     * @param smShare  如果需要分享图片，请设置{@link SmShare#setShareBitmap(Bitmap)}
     * @param url      若是网页分享则传入原地址
     * @param plaform
     * @param callback
     */
    public static void share(final Activity activity, final SmShare smShare, @Nullable final String url,
                             final SHARE_MEDIA plaform, final OnShareResultListener callback) {
        final Runnable d = new Runnable() {
            @Override
            public void run() {
                if (plaform == null) {
                    return;
                }
                if (smShare == null || (TextUtils.isEmpty(smShare.getContent()) && smShare.getShareBitmap() == null)) {
                    SmToast.show(activity, "获取分享内容错误");
                    return;
                }
                if (plaform == SHARE_MEDIA.QQ || plaform == SHARE_MEDIA.QZONE) {
                    if (!UMShareAPI.get(activity).isInstall(activity, SHARE_MEDIA.QQ)) {
                        if (callback != null) {
                            callback.onFailed(new Throwable("没有手机QQ客户端"));
                        }
                    }
                } else if (plaform == SHARE_MEDIA.WEIXIN || plaform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                    if (!UMShareAPI.get(activity).isInstall(activity, SHARE_MEDIA.WEIXIN)) {
                        if (callback != null) {
                            callback.onFailed(new Throwable("没有微信客户端"));
                        }
                        return;
                    }
                }
                final BaseMediaObject shareMedia;
                UMImage umImage;
                //本地生成的图片
                if (smShare.getShareBitmap() != null) {
                    umImage = new UMImage(activity, smShare.getShareBitmap());
                    umImage.setThumb(new UMImage(activity, smShare.getShareBitmap()));
                    umImage.compressFormat = Bitmap.CompressFormat.PNG;
                    umImage.compressStyle = UMImage.CompressStyle.QUALITY;
                    if (!TextUtils.isEmpty(smShare.getTitle())) {
                        umImage.setTitle(smShare.getTitle());
                    } else {
                        umImage.setTitle(smShare.getContent());
                    }
                    umImage.setDescription(smShare.getContent());
                    shareMedia = umImage;
                } else if (TextUtils.isEmpty(smShare.getUrl()) && !TextUtils.isEmpty(smShare.getImgurl())) {
                    //网址空，分享图片
                    //图片分享
                    if (smShare.getShareBitmap() != null) { //本地截图分享优先
                        umImage = new UMImage(activity, smShare.getShareBitmap());
                        umImage.setThumb(new UMImage(activity, smShare.getShareBitmap()));

                    } else { //服务器图片分享其次
                        umImage = new UMImage(activity, smShare.getImgurl());
                        umImage.setThumb(new UMImage(activity, smShare.getImgurl()));
                    }
                    umImage.compressFormat = Bitmap.CompressFormat.PNG;
                    umImage.compressStyle = UMImage.CompressStyle.QUALITY;
                    if (!TextUtils.isEmpty(smShare.getTitle())) {
                        umImage.setTitle(smShare.getTitle());
                    } else {
                        umImage.setTitle(smShare.getContent());
                    }
                    umImage.setDescription(smShare.getContent());
                    shareMedia = umImage;
                } else { //分享网址
                    if (TextUtils.isEmpty(smShare.getUrl())) {
                        smShare.setUrl("http://ai.xx.com/");
                    }
                    String url = smShare.getUrl();
                    UMWeb umWeb = new UMWeb(url);
                    if (TextUtils.isEmpty(smShare.getImgurl())) {
                        umWeb.setThumb(new UMImage(activity, R.mipmap.ic_launcher));
                    } else {
                        umWeb.setThumb(new UMImage(activity, smShare.getImgurl()));
                    }

                    if (!TextUtils.isEmpty(smShare.getTitle())) {
                        umWeb.setTitle(smShare.getTitle());
                    } else {
                        umWeb.setTitle(smShare.getContent());
                    }
                    umWeb.setDescription(smShare.getContent());
                    shareMedia = umWeb;
                }

                //先判断第三方有没有授权
                SHARE_MEDIA authPlaform = plaform;
                if (authPlaform == SHARE_MEDIA.QZONE) {  //QQ空间没有授权功能
                    authPlaform = SHARE_MEDIA.QQ;
                }
                if (!UMShareAPI.get(activity).isAuthorize(activity, authPlaform)) {
                    UMShareAPI.get(activity)
                            .doOauthVerify(activity, authPlaform, new UMAuthListener() {

                                @Override
                                public void onStart(SHARE_MEDIA shareMedia) {
                                    if (!activity.isFinishing()) {
                                        loadingDialog = new SmNetProgressDialog.Builder(activity).setMessage("请稍等").show();
                                    }
                                }

                                @Override
                                public void onComplete(SHARE_MEDIA shareMedia, int i, Map<String, String> map) {
                                    //授权成功，重新执行分享
                                    if (loadingDialog != null) {
                                        loadingDialog.dismiss();
                                        loadingDialog = null;
                                    }
                                    share(activity, smShare, url, plaform, callback);
                                }

                                @Override
                                public void onError(SHARE_MEDIA shareMedia, int i, Throwable throwable) {
                                    if (callback != null) {
                                        callback.onFailed(throwable);
                                    } else {
                                        SmLogger.e("分享失败：" + throwable.getMessage());
                                    }
                                    if (loadingDialog != null) {
                                        loadingDialog.dismiss();
                                        loadingDialog = null;
                                    }
                                }

                                @Override
                                public void onCancel(SHARE_MEDIA shareMedia, int i) {
                                    if (loadingDialog != null) {
                                        loadingDialog.dismiss();
                                        loadingDialog = null;
                                    }
                                    if (callback != null) {
                                        callback.onFailed(new Throwable("用户取消"));
                                    }
                                }
                            });
                } else {
                    //分享成功回调
                    UMShareListener shareCallback = new UMShareListener() {
                        @Override
                        public void onStart(SHARE_MEDIA shareMedia) {
                            if (!activity.isFinishing()) {
                                loadingDialog = new SmNetProgressDialog.Builder(activity).setMessage("请稍等").show();
                            }
                        }

                        @Override
                        public void onResult(SHARE_MEDIA shareMedia) {
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                                loadingDialog = null;
                            }
                            doAfterShare(shareMedia, smShare);
                        }

                        @Override
                        public void onError(SHARE_MEDIA shareMedia, final Throwable throwable) {
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SmToast.show(activity, throwable.getMessage());
                                }
                            });
                            if (callback != null) {
                                callback.onFailed(throwable);
                            }
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA shareMedia) {
                            if (loadingDialog != null) {
                                loadingDialog.dismiss();
                            }
                            if (callback != null) {
                                callback.onFailed(new Throwable("取消分享"));
                            }
                        }

                    };
                    if (shareMedia instanceof UMImage) {  //分享图片
                        new ShareAction(activity)
                                .withText(smShare.getContent())
                                .withMedia((UMImage) shareMedia)
                                .setPlatform(plaform)
                                .setCallback(shareCallback)
                                .share();
                    } else {  //分享连接
                        new ShareAction(activity)
                                .withMedia((UMWeb) shareMedia)
                                .setPlatform(plaform)
                                .setCallback(shareCallback)
                                .share();
                    }
                }
            }


        };
        PermissionUtil.checkAndRequestPermissions(activity,
                new PermissionUtil.SimpleOnPermissionRequestCallback() {

                    @Override
                    public void onSuccess() {
                        d.run();
                    }

                    @Override
                    public void onFailed() {
                        d.run();
                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE);
    }

    /**
     * 分享后事件统计
     *
     * @param media
     * @param smShare
     */
    private static void doAfterShare(SHARE_MEDIA media, SmShare smShare) {
        UserSystem us = UserDataManager.getInstance().getUserSystem();
        int shareType = 0;
        if (media == SHARE_MEDIA.WEIXIN) {
            shareType = 0;
        } else if (media == SHARE_MEDIA.WEIXIN_CIRCLE) {
            shareType = 1;
        } else if (media == SHARE_MEDIA.SINA) {
            shareType = 3;
        } else if (media == SHARE_MEDIA.QQ) {
            shareType = 2;
        } else if (media == SHARE_MEDIA.QZONE) {
            shareType = 4;
        }
        String token = us == null ? "" : us.getToken();
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .fellbackShare(token, smShare.getKind(),
                        smShare.getQid(), shareType, new ResultUICallback<ReturnBeanJson>());
    }

    public static void onPause() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public interface OnShareResultListener {
        void onFailed(Throwable e);

        void onSuccess(SHARE_MEDIA shareMedia, @Nullable ReturnJson json);
    }


}
