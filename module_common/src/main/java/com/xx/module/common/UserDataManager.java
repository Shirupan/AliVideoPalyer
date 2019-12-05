package com.xx.module.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.mrkj.lib.push.SmPushManager;
import com.xx.lib.db.entity.UserSetting;
import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.dao.AppDatabase;
import com.xx.lib.db.dao.UserSystemDao;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.SmErrorHandler;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.model.callback.SimpleFlowableSubscriber;
import com.xx.module.common.model.entity.SmError;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.view.SmNotificationManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author someone
 * @date 2019-05-28
 */
public class UserDataManager {
    private static UserSystem mUser;

    /**
     * 直接获取内存中存放的用户数据
     *
     * @return
     */
    @Nullable
    public UserSystem getUserSystem() {
        return mUser;
    }


    /**
     * 异步从数据库获取当前登陆用户的信息
     *
     * @param listener
     */
    public void getUserSystem(final OnGetUserDataListener listener) {
        if (mUser != null) {
            if (listener != null) {
                listener.onSuccess(mUser);
            }
            return;
        }
        UserSystemDao dao = AppDatabase.getInstance(ModuleManager.of(CommonModule.class).getContext()).getUserDao();
        dao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<List<UserSystem>>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        SmLogger.i("getUser from db");
                        s.request(1);
                    }

                    @Override
                    public void onNext(List<UserSystem> userSystems) {
                        if (!userSystems.isEmpty()) {
                            mUser = userSystems.get(0);
                        }
                        if (mUser != null) {
                            if (listener != null) {
                                listener.onSuccess(mUser);
                            }
                        } else {
                            onError(new ReturnJsonCodeException("没有登录用户"));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (listener != null) {
                            listener.onFailed(t);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 更新内存以及数据库中的用户数据
     *
     * @param userSystem
     */
    public void setUserSystem(UserSystem userSystem) {
        mUser = userSystem;
        if (mUser != null) {
            new RxAsyncHandler<UserSystem>() {
                @Override
                public UserSystem doSomethingBackground() {
                    UserSystemDao dao = AppDatabase.getInstance(ModuleManager.of(CommonModule.class).getContext()).getUserDao();
                    List<UserSystem> list = dao.getAllList();
                    List<UserSystem> removeList = new ArrayList<>();
                    for (UserSystem us : list) {
                        if (us.getUid() == mUser.getUid()) {
                            removeList.add(us);
                        }
                    }
                    if (!removeList.isEmpty()) {
                        dao.delete(removeList.toArray(new UserSystem[0]));
                    }
                    if (mUser.getId() == null) {
                        dao.insert(mUser);
                    } else {
                        dao.update(mUser);
                    }
                    return mUser;
                }

                @Override
                public void onNext(UserSystem data) {
                    SmLogger.i("更新用户数据完成");
                    //更新数据库操作，并发送用户信息变化广播
                    BaseConfig.sendUserInfoChangeBroadcast(ModuleManager.of(CommonModule.class).getContext());
                }
            }.execute();

        }
    }

    /**
     * 退出登录
     */
    public void logout() {
        new RxAsyncHandler<Boolean>() {
            @Override
            public Boolean doSomethingBackground() {
                if (mUser != null) {
                    UserSystemDao dao = AppDatabase.getInstance(ModuleManager.of(CommonModule.class).getContext()).getUserDao();
                    List<UserSystem> list = dao.getUserByUid(mUser.getUid());
                    if (!list.isEmpty()) {
                        UserSystem[] array = new UserSystem[list.size()];
                        dao.delete(list.toArray(array));
                    }
                }
                mUser = null;
                return true;
            }

            @Override
            public void onNext(Boolean data) {

            }
        }.execute();
    }

    /**
     * 根据用户设置来开启推送
     *
     * @param context
     */
    public void startPushService(final Context context) {
        if (mUser != null) {
            AppDatabase.getInstance(context).getUserSettingDao().getSettingByToken(mUser.getToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleFlowableSubscriber<List<UserSetting>>() {
                        @Override
                        public void onNext(List<UserSetting> userSettings) {
                            if (!userSettings.isEmpty() && userSettings.get(0).getNotify() != 1) {
                                SmPushManager.startService(context.getApplicationContext(), mUser.getUid() + "",
                                        SmNotificationManager.Companion.getCHANNEL_SYSTEM_ID(),
                                        SmNotificationManager.Companion.getCHANNEL_SYSTEM_NAME());
                            }
                        }
                    });
        } else {
            SmPushManager.startService(context.getApplicationContext(), "-10",
                    SmNotificationManager.Companion.getCHANNEL_SYSTEM_ID(),
                    SmNotificationManager.Companion.getCHANNEL_SYSTEM_NAME());
        }
    }

    /**
     * 关闭推送
     *
     * @param context
     */
    public void stopPushService(Context context) {
        SmPushManager.unregisterPush(context);
    }


    public interface OnGetUserDataListener {
        void onSuccess(UserSystem us);

        void onFailed(Throwable e);
    }


    public static class SimpleOnGetUserDataListener implements OnGetUserDataListener {
        @Override
        public void onSuccess(@NonNull UserSystem us) {

        }

        @Override
        public void onFailed(Throwable e) {

        }
    }

    /**
     * 网络请求最新的用户数据
     *
     * @param listener
     */
    public void getUserInfoFromNet(final OnGetUserDataListener listener) {
        ModuleManager.of(CommonModule.class).getModelClient()
                .getLoginUser(mUser.getToken(), new ResultUICallback<UserSystem>() {
                    @Override
                    public void onNext(UserSystem userSystem) {
                        super.onNext(userSystem);
                        if (listener != null) {
                            listener.onSuccess(userSystem);
                        }
                        setUserSystem(userSystem);
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (listener != null) {
                            listener.onFailed(t);
                        }
                    }
                }.unShowDefaultMessage());
    }

    /**
     * 点赞功能
     *
     * @param vid  视频或者趣闻的id
     * @param type 0视频  1趣闻
     */
    public void addPraise(Activity activity, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(activity);
            return;
        }
        executeAddPraise(activity, vid, type, callback);
    }

    /**
     * @param fragment
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     */
    public void addPraise(Fragment fragment, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(fragment);
            return;
        }
        executeAddPraise(fragment, vid, type, callback);
    }

    private void executeAddPraise(Object lifecylce, int vid, int type, final PraiseZanCallback callback) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .addPraise(vid, type, mUser.getToken(), new ResultUICallback<String>(lifecylce) {
                    @Override
                    public void onNext(String returnBeanJson) {
                        super.onNext(returnBeanJson);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (callback != null) {
                            callback.onError(SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }


    /**
     * 取消点赞
     *
     * @param vid  视频或者趣闻的id
     * @param type 0视频  1趣闻
     */
    public void delPraise(Activity activity, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(activity);
            return;
        }
        executeDelPraise(activity, vid, type, callback);
    }

    /**
     * 取消点赞
     *
     * @param fragment
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     */
    public void delPraise(Fragment fragment, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(fragment);
            return;
        }
        executeDelPraise(fragment, vid, type, callback);
    }

    private void executeDelPraise(Object lifecylce, int vid, int type, final PraiseZanCallback callback) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .delPraise(vid, type, mUser.getToken(), new ResultUICallback<String>(lifecylce) {
                    @Override
                    public void onNext(String returnBeanJson) {
                        super.onNext(returnBeanJson);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (callback != null) {
                            callback.onError(SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }

    /**
     * 点赞
     *
     * @param fragment
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     * @param callback
     */
    public void addCollection(@NotNull Fragment fragment, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(fragment);
            return;
        }
        executeAddCollection(fragment, vid, type, callback);
    }

    /**
     * 点赞
     *
     * @param activity
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     * @param callback
     */
    public void addCollection(@NotNull Activity activity, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(activity);
            return;
        }
        executeAddCollection(activity, vid, type, callback);
    }

    private void executeAddCollection(Object lifecycle, int vid, int type, final PraiseZanCallback callback) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .addCollection(vid, type, mUser.getToken(), new ResultUICallback<String>(lifecycle) {
                    @Override
                    public void onNext(String returnBeanJson) {
                        super.onNext(returnBeanJson);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (callback != null) {
                            callback.onError(SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }

    /**
     * 取消点赞
     *
     * @param activity
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     * @param callback
     */
    public void delCollection(@NotNull Activity activity, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(activity);
            return;
        }
        executeDelCollection(activity, vid, type, callback);
    }

    /**
     * 取消点赞
     *
     * @param fragment
     * @param vid      视频或者趣闻的id
     * @param type     0视频  1趣闻
     * @param callback
     */
    public void delCollection(@NotNull Fragment fragment, int vid, int type, PraiseZanCallback callback) {
        if (mUser == null) {
            ActivityRouter.get().goToLoginActivity(fragment);
            return;
        }
        executeDelCollection(fragment, vid, type, callback);
    }

    private void executeDelCollection(Object lifecycle, int vid, int type, final PraiseZanCallback callback) {
        ModuleManager.of(CommonModule.class)
                .getModelClient()
                .delCollection(vid, type, mUser.getToken(), new ResultUICallback<String>(lifecycle) {
                    @Override
                    public void onNext(String returnBeanJson) {
                        super.onNext(returnBeanJson);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (callback != null) {
                            callback.onError(SmErrorHandler.catchTheErrorSmError(t));
                        }
                    }
                }.unShowDefaultMessage());
    }

    public interface PraiseZanCallback {
        void onSuccess();

        void onError(SmError e);
    }


    public static UserDataManager getInstance() {
        return Singleton.instance;
    }


    static class Singleton {
        static UserDataManager instance = new UserDataManager();
    }
}
