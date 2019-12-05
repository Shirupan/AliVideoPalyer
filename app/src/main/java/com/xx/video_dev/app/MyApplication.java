package com.xx.video_dev.app;


import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.mcxiaoke.packer.helper.PackerNg;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.NetLib;
import com.mrkj.lib.push.SmPushManager;
import com.mrkj.lib.update.SmUpdateManager;

import com.xx.app.dependendy.DependencyModule;
import com.xx.module.common.BaseConfig;
import com.xx.module.common.CommonModule;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.util.CrashHandler;
import com.xx.module.common.router.RouterParams;
import com.xx.module.me.MeModule;
import com.xx.module.news.NewsModule;
import com.xx.module.video.VideoModuleClient;
import com.xx.video_dev.BuildConfig;
import com.xx.video_dev.R;
import com.xx.video_dev.common.AppModuel;
import com.xx.video_dev.common.SmImageLoaderImpl;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;

import rx_activity_result2.RxActivityResult;

/**
 * Created by Administrator on 2016-05-25.
 */
public class MyApplication extends Application {
    public static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication myApplication;
    private static Context mContext;


    public static MyApplication getInstance() {
        return myApplication;
    }

    /**
     * 得到上下文
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        mContext = getApplicationContext();
        ModuleManager.of(CommonModule.class).init(this);
        NetLib.DEBUG = BuildConfig.DEBUG;
        NetLib.init(this, BaseConfig.GET_URL_NEW);
        ImageLoader.init(SmImageLoaderImpl.getInstance());
        SmLogger.setBebug(BuildConfig.DEBUG);
        RxActivityResult.register(this);

        ModuleManager.of(AppModuel.class).init(this);
        ModuleManager.of(DependencyModule.class).init(this);
        ModuleManager.of(MeModule.class).init(this);
        ModuleManager.of(NewsModule.class).init(this);
        ModuleManager.of(VideoModuleClient.class).init(this);
        ModuleManager.of(CommonModule.class).init(this);
        DataProviderManager.init(this);

        CrashHandler.getInstance().init(this);

        SmUpdateManager.BuglyCrashReportAndUpgradeInit(this, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
        SmPushManager.bindPushMessageBroadCastReceiver(RouterParams.GlobalBroadCast.PUSH_MESSAGE);


        BaseConfig.QQ_APP_ID = AppUtil.getAppMetaData(this, "QQ_APPID");
        BaseConfig.QQ_APP_SECRET = AppUtil.getAppMetaData(this, "QQ_KEY");

        BaseConfig.WEIBO_KEY = AppUtil.getAppMetaData(this, "WB_APPID");
        BaseConfig.WEIBO_SECRET = AppUtil.getAppMetaData(this, "WB_KEY");

        BaseConfig.WX_APP_ID = AppUtil.getAppMetaData(this, "WX_APPID");
        BaseConfig.WX_APP_SECRET = AppUtil.getAppMetaData(this, "WX_KEY");

        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        UMConfigure.init(this,
                AppUtil.getAppMetaData(this, "UMENG_APPKEY"),
                PackerNg.getMarket(this, BaseConfig.DEFAULT_CHANNEL),
                UMConfigure.DEVICE_TYPE_PHONE, "");
        initSocialModule();
    }

    /**
     * 第三方，分享组件初始化
     */
    private void initSocialModule() {
        //友盟社交化分享
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(false);
        config.isOpenShareEditActivity(true);
        if (AppUtil.isAppInstalled(this, AppUtil.PACKAGE_NAME_WEIBO)) {
            config.setSinaAuthType(UMShareConfig.AUTH_TYPE_SSO);
        } else {
            config.setSinaAuthType(UMShareConfig.AUTH_TYPE_WEBVIEW);
        }
        //以下這句会导致分享到QQ好友被定向到分享到空间
        // config.setShareToQQFriendQzoneItemHide(false);
        PlatformConfig.setSinaWeibo(BaseConfig.WEIBO_KEY, BaseConfig.WEIBO_SECRET, BaseConfig.WEIBO_URL);
        PlatformConfig.setQQZone(BaseConfig.QQ_APP_ID, BaseConfig.QQ_APP_SECRET);
        PlatformConfig.setWeixin(BaseConfig.WX_APP_ID, BaseConfig.WX_APP_SECRET);
        UMShareAPI.get(this).setShareConfig(config);
    }


}
