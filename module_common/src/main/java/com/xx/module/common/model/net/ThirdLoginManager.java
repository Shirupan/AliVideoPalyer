package com.xx.module.common.model.net;


import android.app.Activity;

import com.xx.module.common.BaseConfig;
import com.xx.module.common.CommonModule;
import com.xx.module.common.client.ModuleManager;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

/**
 * @author
 * @date 2016-11-02
 */

public class ThirdLoginManager {
    private static ThirdLoginManager loginManager;
    private UMShareAPI mShareAPI;

    private ThirdLoginManager() {
        loginManager = this;
        mShareAPI = UMShareAPI.get(ModuleManager.of(CommonModule.class).getContext());
    }

    public static ThirdLoginManager getInstance() {
        if (loginManager == null) {
            synchronized (ThirdLoginManager.class) {
                if (loginManager == null) {
                    loginManager = new ThirdLoginManager();
                }
            }
        }
        return loginManager;
    }

    /**
     * 第三方登录，使用Umeng
     *
     * @param activity
     * @param type
     */
    public void loginByThird(final Activity activity, SHARE_MEDIA type, UMAuthListener callback) {
        LoginUMAuthListener loginUMAuthListener;
        loginUMAuthListener = new LoginUMAuthListener(activity, mShareAPI, callback);
        mShareAPI.doOauthVerify(activity, type, loginUMAuthListener);

    }


    /**
     * 第三方登录后回调
     * 这里用于立即去获取用户信息，并且传入成功后回调
     */
    private class LoginUMAuthListener implements UMAuthListener {
        private UMShareAPI shareAPI;
        private Activity activity;
        private UMAuthListener successListener;


        /**
         * @param activity
         * @param shareAPI
         */
        public LoginUMAuthListener(Activity activity, UMShareAPI shareAPI, UMAuthListener successListener) {
            this.shareAPI = shareAPI;
            this.activity = activity;
            this.successListener = successListener;
        }

        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            shareAPI.getPlatformInfo(activity, share_media, successListener);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            //  SmToast.show(ProjectBase.getApplication(), "登陆失败：" + throwable.getLocalizedMessage());
            if (successListener != null) {
                successListener.onError(share_media, i, throwable);
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            PlatformConfig.setQQZone(BaseConfig.QQ_APP_ID, BaseConfig.QQ_APP_SECRET);
            if (successListener != null) {
                successListener.onCancel(share_media, i);
            }
        }
    }
}
