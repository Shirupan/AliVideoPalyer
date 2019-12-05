package com.xx.module.common.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.mrkj.lib.common.util.StringUtil;
import com.mrkj.lib.common.view.SmToast;
import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.BaseConfig;
import com.xx.module.common.CommonModule;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.SmErrorHandler;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.net.ThirdLoginManager;
import com.xx.module.common.view.contract.IViewManager;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * @author someone
 * @date 2019-05-28
 */
public class LoginMainPresenter extends BasePresenter<IViewManager.IMainLoginView> {
    /**
     * 账号密码登录
     *
     * @param phone
     * @param password
     */
    public void loginWithPassword(@NotNull String phone, @NotNull String password) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .loginWithPassword(phone, password, new ResultUICallback<UserSystem>(getView()) {
                    @Override
                    public void onNext(UserSystem userSystem) {
                        super.onNext(userSystem);
                        if (getView() != null) {
                            getView().onLoginResult(userSystem, null);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (getView() != null) {
                            getView().onLoginResult(null, SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }

    /**
     * ping账号。未注册则前往注册
     *
     * @param phone
     * @param code
     */
    public void loginWithCode(@NotNull String phone, @Nullable String code) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .loginWithCode(phone, code, new ResultUICallback<UserSystem>(getView()) {
                    @Override
                    public void onNext(UserSystem userSystem) {
                        super.onNext(userSystem);
                        if (getView() != null) {
                            getView().onCheckUserResult(userSystem, null);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (getView() != null) {
                            getView().onCheckUserResult(null, SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());

    }

    public void loginByThird(Activity activity, @NotNull SHARE_MEDIA type) {
        String message = "正在跳转%s";
        String app = "";
        if (type == SHARE_MEDIA.QQ || type == SHARE_MEDIA.QZONE) {
            app = "QQ客户端";
        } else if (type == SHARE_MEDIA.WEIXIN) {
            app = "微信客户端";
        } else if (type == SHARE_MEDIA.SINA) {
            app = "微博客户端";
        }
        SmToast.show(activity, String.format(Locale.getDefault(), message, app));
        ThirdLoginManager.getInstance().loginByThird(activity, type, getCallback());
    }

    private UMAuthListener callback;

    /**
     * 拿到第三方登录后的数据
     *
     * @return
     */
    private UMAuthListener getCallback() {
        if (callback == null) {
            callback = new UMAuthListener() {
                @Override
                public void onStart(SHARE_MEDIA share_media) {

                }

                @Override
                public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                    int type;
                    String dlname = map.get("openid");
                    String userName, userHeadUrl;
                    userHeadUrl = map.get("profile_image_url");
                    userName = map.get("screen_name");
                    switch (share_media.name()) {
                        case "WEIXIN":
                            type = 1;
                            dlname = BaseConfig.LongInName_WX + dlname;
                            break;
                        case "QQ":
                            type = 2;
                            dlname = BaseConfig.LongInName_TX + dlname;
                            break;
                        case "SINA":
                            type = 3;
                            dlname = BaseConfig.LongInName_SINA + dlname;
                            break;
                        default:
                            type = 0;
                            userHeadUrl = "";
                            userName = "";
                    }
                    if (TextUtils.isEmpty(userName)) {
                        userName = StringUtil.randomString(8);
                    }
                    loginByThird(dlname, type, userHeadUrl, userName);
                }

                @Override
                public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                    if (getView() != null) {
                        getView().onThirdLoginResult(null, SmErrorHandler.catchTheErrorSmError(throwable));
                    }
                }

                @Override
                public void onCancel(SHARE_MEDIA share_media, int i) {
                    if (getView() != null) {
                        getView().onThirdLoginResult(null, null);
                    }
                }
            };
        }
        return callback;
    }


    public void loginNoPwd(@NotNull String phone) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .loginNoPwd(phone, new ResultUICallback<UserSystem>(getView()) {
                    @Override
                    public void onNext(UserSystem userSystem) {
                        super.onNext(userSystem);
                        UserDataManager.getInstance().setUserSystem(userSystem);
                        if (getView() != null) {
                            getView().onNoPwdLoginResult(userSystem, null);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (getView() != null) {
                            getView().onNoPwdLoginResult(null, SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }


    private void loginByThird(String openId, int registertype, String imgeurl, String nickName) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .loginByThird(openId, registertype, imgeurl, nickName,
                        new ResultUICallback<UserSystem>(getView()) {
                            @Override
                            public void onNext(UserSystem userSystem) {
                                super.onNext(userSystem);
                                UserDataManager.getInstance().setUserSystem(userSystem);
                                if (getView() != null) {
                                    getView().onThirdLoginResult(userSystem, null);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                super.onError(t);
                                if (getView() != null) {
                                    getView().onThirdLoginResult(null, SmErrorHandler.catchTheErrorSmError(t));
                                }
                            }
                        }.unShowDefaultMessage());
    }
}
