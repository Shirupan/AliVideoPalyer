package com.xx.app.dependendy.service;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.push.SmPushManager;
import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.CommonModule;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.util.CrashHandler;
import com.xx.module.common.router.ActivityRouter;

/**
 * 推送注册，信息回调
 */
public class MyPushMessageJob implements BackgroundJob {
    private Intent mIntent;
    private Context mContext;


    public MyPushMessageJob(Context context, Intent intent) {
        mIntent = intent;
        mContext = context;
    }

    @Override
    public void doJob() {
        if (mIntent == null) {
            stop();
            return;
        }
        try {
            String what = mIntent.getStringExtra(SmPushManager.KEY_WHAT);
            switch (what) {
                // 绑定
                case SmPushManager.WHAT_BIND_START:
                    // 获取附加参数
                    String cid = mIntent.getStringExtra(SmPushManager.KEY_USER_ID);
                    // 保存绑定信息到服务器
                    bindCidToUser(cid);
                    break;
                // 消息数
                case SmPushManager.WHAT_MESSAGE_START:
                    // 获取附加参数

                    break;
                // 通知
                case SmPushManager.WHAT_NOTIFICATION_START:
                    // 获取附加参数
                    onNotification(mIntent.getStringExtra(SmPushManager.KEY_CONTENT));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCatchInfo2File(e);
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public void stop() {

    }


    private void bindCidToUser(final String cid) {
        UserDataManager.getInstance().getUserSystem(new UserDataManager.SimpleOnGetUserDataListener() {
            @Override
            public void onSuccess(@NonNull final UserSystem user) {
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                SmLogger.i("当前帐号userid:" + user.getUid());
                //如果之前没有绑定，或者与之前绑定的cid不一样，就重新绑定
                if (!TextUtils.equals(cid, user.getXingpush())) {
                    ModuleManager.of(CommonModule.class)
                            .getModelClient()
                            .pushToken(user.getToken(), cid, new ResultUICallback<String>() {
                                @Override
                                public void onNext(String content) {
                                    super.onNext(content);
                                    user.setXingpush(cid);
                                    UserDataManager.getInstance().setUserSystem(user);
                                }
                            });
                }
            }

            @Override
            public void onFailed(Throwable e) {
                super.onFailed(e);
                stop();
            }
        });
    }


    /**
     * 处理通知
     *
     * @param customContentString
     */
    private void onNotification(String customContentString) {
        SmLogger.i("透传带有:pageGoto：" + customContentString);
        ActivityRouter.handleUri(mContext, Uri.parse(customContentString));
        stop();
    }


}
