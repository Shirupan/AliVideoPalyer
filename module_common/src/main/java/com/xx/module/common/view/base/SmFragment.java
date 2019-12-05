package com.xx.module.common.view.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrkj.lib.common.cutout.CutoutManager;
import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.router.ActivityRouter;
import com.trello.rxlifecycle2.components.support.RxFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 2018/1/18 0018
 */

public abstract class SmFragment extends RxFragment implements ITakePhotoView {
    protected View rootView;
    private boolean isNeedTakePhoto;
    private TakePhotoHandler takePhotoHandler;
    private boolean analyze = true;
    private boolean isNewView;
    private boolean showLoadingView;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public TakePhotoHandler getTakePhotoHandler() {
        if (takePhotoHandler == null) {
            takePhotoHandler = new TakePhotoHandler(this);
        }
        return takePhotoHandler;
    }

    public void beforeSetContentView() {

    }

    public boolean isLazyFragment() {
        return isLazyFragment;
    }

    public void setShowLoadingView(boolean show) {
        showLoadingView = show;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onCreate(savedInstanceState);
        }
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutID(), container, false);
            if (showLoadingView) {
                initLoadingView((ViewGroup) rootView);
            }
            isNewView = true;
        } else {
            isNewView = false;
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isNewView) {
            initViewsAndEvents(rootView);
        }
        if (isLazyFragment) {
            isViewCreated = true;
            lazyLoad();
        }
    }


    public abstract int getLayoutID();

    public View getRootView() {
        return rootView;
    }

    /**
     * @param rootView
     */
    protected abstract void initViewsAndEvents(View rootView);

    protected abstract void initLoadingView(ViewGroup rootView);

    /**
     * 建议在{@link SmActivity#beforeSetContentView()} 中设置
     *
     * @param need 是否需要选择图片功能
     */
    public void setNeedTakePhoto(boolean need) {
        isNeedTakePhoto = need;
    }


    /**
     * 是否开启该页面的友盟统计（默认开启）
     *
     * @param analyze
     */
    public void setAnalyze(boolean analyze) {
        this.analyze = analyze;
    }

    private UserDataManager.SimpleOnGetUserDataListener loginListener;

    /**
     * 获取失败将前往登陆
     *
     * @param listener
     */
    public void getLoginUserAndLogin(final UserDataManager.SimpleOnGetUserDataListener listener) {
        UserDataManager.getInstance().getUserSystem(new UserDataManager.SimpleOnGetUserDataListener() {
            @Override
            public void onSuccess(UserSystem us) {
                if (listener != null) {
                    listener.onSuccess(us);
                }
            }

            @Override
            public void onFailed(Throwable e) {
                loginListener = listener;
                ActivityRouter.get().goToLoginActivity(SmFragment.this);
            }
        });
    }

    public void getLoginUser(final UserDataManager.OnGetUserDataListener listener) {
        UserDataManager.getInstance().getUserSystem(new UserDataManager.SimpleOnGetUserDataListener() {
            @Override
            public void onSuccess(UserSystem us) {
                if (listener != null) {
                    listener.onSuccess(us);
                }
            }

            @Override
            public void onFailed(Throwable e) {
                if (listener != null) {
                    listener.onFailed(e);
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (analyze) {
            //统计页面跳转
            MobclickAgent.onPageStart(this.getClass().getName());
        }
        ImageLoader.getInstance().resume(SmContextWrap.obtain(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        ImageLoader.getInstance().pause(SmContextWrap.obtain(this));
        if (analyze) {
            //统计页面跳转
            MobclickAgent.onPageEnd(this.getClass().getName());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == ActivityRouter.LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && loginListener != null) {
            //登录返回
            if (UserDataManager.getInstance().getUserSystem() != null) {
                loginListener.onSuccess(UserDataManager.getInstance().getUserSystem());
            } else {
                loginListener.onFailed(new ReturnJsonCodeException("未登录"));
            }
            loginListener = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onSaveInstanceState(outState);
        }
    }


    public UserSystem getLoginUser() {
        return UserDataManager.getInstance().getUserSystem();
    }

    private long lastClickTime;

    /**
     * 防止快速双击
     *
     * @return
     */
    public boolean checkDoubleClick() {
        long time = System.currentTimeMillis();
        boolean temp = time - lastClickTime < 1000;
        lastClickTime = time;
        return temp;
    }

    /**
     * 检查当前Activity是否已空
     *
     * @return
     */
    public boolean getActivityByCheck() {
        return (getActivity() != null && !getActivity().isFinishing());
    }

    /**
     * 设置懒加载模式。建议在{@link SmFragment#beforeSetContentView()} 中设置
     *
     * @param is
     */
    public void setIsLazyFragmentMode(boolean is) {
        isLazyFragment = is;
    }


    //Fragment的View加载完毕的标记
    private boolean isViewCreated;
    /**
     * 是否是懒加载模式
     */
    private boolean isLazyFragment = false;
    private boolean isVisibleToUser;
    private boolean isFirstVisible = false;
    private boolean isFirstInvisible = false;


    private void lazyLoad() {
        if (isViewCreated && isVisibleToUser) {
            if (!isFirstVisible) {
                isFirstVisible = true;
                onFirstUserVisible();
            } else {
                onUserVisible();
            }
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isRunBeforeSetContentView) {
            beforeSetContentView();
            isRunBeforeSetContentView = true;
        }
        //这里有个问题。如果是单Fragment显示，并且Fragment启用了懒加载，
        // 则需要在父层Fragment或者Activity显示调用setUserVisibleHint(true)
        if (!isLazyFragment) {
            return;
        }
        if (isVisibleToUser) { //当前Fragment可见
            this.isVisibleToUser = true;
            lazyLoad();
        } else if (isViewCreated) { //当前Fragment不可见,并且view创建完成
            if (!isFirstInvisible) {
                isFirstInvisible = true;
                onFirstUserInvisible();
            } else {
                onUserInvisible();
            }
        }
    }

    private boolean isRunBeforeSetContentView;

    /**
     * 首次进入这个fragment
     */
    protected void onFirstUserVisible() {
    }

    /**
     * 切换fragment进入该fragment
     */
    protected void onUserVisible() {
    }


    protected void onFirstUserInvisible() {
    }

    protected void onUserInvisible() {
    }

    @Override
    public void onModifyPhoto(ArrayList<String> list) {

    }

    @Override
    public void onGetPhoto(List<String> data) {

    }

    /**
     * 获取状态栏以及凹槽屏高度中最高一项
     * android 6.0 以上支持（应用在6.0以上系统才状态栏兼容）
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public int getCutOutAndStatusMaxHeight() {
        return CutoutManager.getCutOutAndStatusMaxHeight(getContext());
    }

    public void setCutOutAndStatusMaxHeightToView(View v) {
        if (v == null) {
            return;
        }
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (v.getLayoutParams() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lp.height = getCutOutAndStatusMaxHeight();
        } else {
            lp.height = 1;
        }
        v.setLayoutParams(lp);
    }

    public boolean isCutoutScreen() {
        return CutoutManager.isCutoutScreen(getContext());
    }
}
