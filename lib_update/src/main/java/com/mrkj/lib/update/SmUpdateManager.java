package com.mrkj.lib.update;

import android.content.Context;

import com.mcxiaoke.packer.helper.PackerNg;
import com.mrkj.lib.common.util.AppUtil;
import com.sm.lib_update.R;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.xx.module.common.BaseConfig;
import com.xx.module.common.BuildConfig;

/**
 * @author
 * @date 2017/7/13
 */

public class SmUpdateManager {
    private static SmUpdateManager updateManager;

    private SmUpdateManager() {
        updateManager = this;
    }

    public static SmUpdateManager getInstance() {
        if (updateManager == null) {
            synchronized (SmUpdateManager.class) {
                if (updateManager == null) {
                    new SmUpdateManager();
                }
            }
        }
        return updateManager;
    }


    /**
     * 主动上报异常
     */
    public void reportCrash(Throwable throwable) {
        CrashReport.postCatchedException(throwable, new Thread());
    }


    public interface OnPassUpdateCheckListener {
        void onPass();

        void onUpdating();
    }

    public static class SimpleOnPassUpdateCheckListener implements OnPassUpdateCheckListener {
        @Override
        public void onPass() {

        }

        @Override
        public void onUpdating() {

        }


    }


    /**
     * Bugly报错初始化以及灰度更新初始化
     *
     * @param context
     * @param largeIconId
     * @param smallIconId
     */
    public static void BuglyCrashReportAndUpgradeInit(Context context, int largeIconId, int smallIconId) {
        //自动检查更新(关闭)
        Beta.autoCheckUpgrade = false;
        Beta.enableNotification = true;
        //升级检查周期设置
        Beta.upgradeCheckPeriod = 60 * 1000;
        Beta.largeIconId = largeIconId;
        Beta.smallIconId = smallIconId;
        //设置显示弹窗的Activity
        //Beta.canShowUpgradeActs.add(MainActivity.class);
        //关闭热更新
        Beta.enableHotfix = false;
        //自定义升级弹窗样式
        Beta.upgradeDialogLayoutId = R.layout.sm_bulgy_dialog_upgrade;
        //仅在主进程上报错误
        if (AppUtil.checkIsMainProcess(context)) {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setUploadProcess(true);
            // strategy.setAppChannel("xiaomi");
            strategy.setAppChannel(PackerNg.getMarket(context, BaseConfig.DEFAULT_CHANNEL));
            Bugly.init(context, AppUtil.getAppMetaData(context, "META_BUYLG_APPID"), BuildConfig.DEBUG, strategy);
        }
    }

    /**
     * 灰度更新检查
     *
     * @param context
     */
    public static void buglyCheckUpgrade(Context context) {
        Beta.checkUpgrade(false, false);
    }

}
