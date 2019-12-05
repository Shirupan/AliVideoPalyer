package com.xx.module.common.view.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mrkj.lib.common.cutout.CutoutManager;
import com.mrkj.lib.common.util.ActivityManagerUtil;
import com.mrkj.lib.common.util.ColorUtils;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.util.StatusBarUtil;
import com.mrkj.lib.common.util.StringUtil;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.module.common.R;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.view.refresh.IRefreshLayout;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareAPI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date 2018/1/18 0018
 */

public abstract class SmActivity extends RxAppCompatActivity implements ITakePhotoView {
    View rootView;
    Context mContext;
    private boolean isNeedTakePhoto;

    private TakePhotoHandler takePhotoHandler;
    private boolean showLoadingView = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        beforeSetContentView();
        TypedValue typedValue = new TypedValue();
        int color;
        if (getTheme().resolveAttribute(R.attr.smToolbarBackgroundColor, typedValue, true)) {
            color = ContextCompat.getColor(this, typedValue.resourceId);
        } else {
            color = ContextCompat.getColor(this, R.color.color_toolbar_bg);
        }
        setStatusBar(false, color, ColorUtils.isLightColor(color));
        ActivityManagerUtil.getScreenManager().pushActivity(this);
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onCreate(savedInstanceState);
        }
        //contentView处理
        if (getLayoutId() != 0) {
            rootView = LayoutInflater.from(this).inflate(getLayoutId(), null, false);
            setContentView(rootView);
            setToolBarBack(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            TextView rightTv = findViewById(R.id.btn_reply);
            ImageButton rightIb = findViewById(R.id.toolbar_right_ib);
            if (rightIb != null && rightTv != null) {
                rightIb.setVisibility(View.GONE);
                rightTv.setVisibility(View.GONE);
            }
        }
        if (showLoadingView) {
            ViewGroup view = findViewById(android.R.id.content);
            initLoadingView(view);
        }
        initViewsAndEvents();
    }

    protected void beforeSetContentView() {

    }


    public abstract int getLayoutId();

    public abstract void initLoadingView(ViewGroup viewGroup);

    protected abstract void initViewsAndEvents();

    @Override
    public TakePhotoHandler getTakePhotoHandler() {
        if (takePhotoHandler == null) {
            takePhotoHandler = new TakePhotoHandler(this);
        }
        return takePhotoHandler;
    }

    public void setShowLoadingView(boolean show) {
        showLoadingView = show;
    }

    /**
     * 建议在{@link SmActivity#beforeSetContentView()} 中设置
     *
     * @param need 是否需要选择图片功能
     */
    public void setNeedTakePhoto(boolean need) {
        isNeedTakePhoto = need;
    }


    public UserSystem getLoginUser() {
        return UserDataManager.getInstance().getUserSystem();
    }


    private UserDataManager.OnGetUserDataListener loginListener;

    /**
     * 获取失败将前往登陆
     *
     * @param listener
     */
    public void getLoginUserAndLogin(final UserDataManager.OnGetUserDataListener listener) {
        UserDataManager.getInstance().getUserSystem(new UserDataManager.SimpleOnGetUserDataListener() {
            @Override
            public void onSuccess(@NonNull UserSystem us) {
                if (listener != null) {
                    listener.onSuccess(us);
                }
            }

            @Override
            public void onFailed(Throwable e) {
                loginListener = listener;
                ActivityRouter.get().goToLoginActivity(SmActivity.this);
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

    public void setRefreshLayout(IRefreshLayout ptrFrameLayout) {
        setRefreshLayout(ptrFrameLayout, 0);
    }

    /**
     * 第三方下拉刷新
     *
     * @param frameLayout
     */
    public void setRefreshLayout(IRefreshLayout frameLayout, int backgroundColorRes) {
        setRefreshLayout(frameLayout, null, backgroundColorRes);
    }

    /**
     * 第三方下拉刷新
     *
     * @param frameLayout
     */
    public void setRefreshLayout(IRefreshLayout frameLayout, AppBarLayout appBarLayout, int backgroundColorRes) {
        frameLayout.setAppbarLayout(appBarLayout);
        frameLayout.setBackgroundColorRes(backgroundColorRes);
        // CommonUISetUtil.initPtrFrameLayout(frameLayout, appBarLayout, backgroundColorRes, runnable);
    }

    /**
     * 返回键
     *
     * @param onClickListener
     */
    public void setToolBarBack(final View.OnClickListener onClickListener) {
        View view = findViewById(R.id.toolbar_left_ib);
        if (view != null) {
            view.setOnClickListener(onClickListener);
        }
    }


    public void setToolBarBackgroundColor(int color) {
        View view = findViewById(R.id.sm_toolbar_layout);
        if (view != null) {
            view.setBackgroundResource(color);
        }
    }


    public void showLeftSecondedBack(boolean show) {
        View view = findViewById(R.id.toolbar_left_ib_2);
        if (view != null) {
            if ((view.getVisibility() == View.VISIBLE && show) || (view.getVisibility() == View.GONE && !show)) {
                return;
            }
            view.setVisibility(show ? View.VISIBLE : View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    /**
     * 标题文字
     *
     * @param text
     */
    public void setToolBarTitle(CharSequence text) {
        setToolBarTitle(text, null);
    }

    /**
     * 标题文字
     *
     * @param text
     */
    public void setToolBarTitle(CharSequence text, View.OnClickListener l) {
        TextView titleTv = findViewById(R.id.tv_topbar_title);
        if (titleTv != null) {
            titleTv.setText(text);
            titleTv.setOnClickListener(l);
        }
    }

    /**
     * 标题文字
     *
     * @param text
     * @param textSize sp
     */
    public void setToolBarTitle(CharSequence text, int textSize) {
        TextView titleTv = findViewById(R.id.tv_topbar_title);
        if (titleTv != null) {
            titleTv.setText(text);
            if (textSize != 0) {
                titleTv.setTextSize(textSize);
            }
        }
    }

    public void setToolbarTitleDrawable(Drawable d, boolean isRight) {
        TextView titleTv = findViewById(R.id.tv_topbar_title);
        if (titleTv != null) {
            if (d == null) {
                titleTv.setCompoundDrawables(null, null, null, null);
            } else {
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            }
            if (isRight) {
                titleTv.setCompoundDrawables(null, null, d, null);
            } else {
                titleTv.setCompoundDrawables(d, null, null, null);
            }
        }
    }

    /**
     * 隐藏toolbar右边ImageButton
     */
    public void hideToolBarRightButton() {
        View view = findViewById(R.id.toolbar_right_ib);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * toolbar右边
     *
     * @param text
     * @param listener
     */
    public void setToolBarRight(CharSequence text, Integer textSizeDip, boolean showBG, final View.OnClickListener listener) {
        final TextView rightTv = findViewById(R.id.btn_reply);
        ImageButton rightIb = findViewById(R.id.toolbar_right_ib);
        if (rightTv != null) {
            if (!showBG) {
                rightTv.setBackground(null);
                rightTv.setTextColor(Color.WHITE);
            }
            rightTv.setText(text);
            if (textSizeDip != null) {
                rightTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDip);
            }
            rightTv.setOnClickListener(listener);
            rightTv.setVisibility(View.VISIBLE);
        }
        if (rightIb != null) {
            rightIb.setVisibility(View.GONE);
        }
    }

    /**
     * toolbar右边
     *
     * @param text
     * @param listener
     */
    public void setToolBarRight(CharSequence text, boolean showBG, final View.OnClickListener listener) {
        TextView rightTv = findViewById(R.id.btn_reply);
        ImageButton rightIb = findViewById(R.id.toolbar_right_ib);
        if (rightTv != null && TextUtils.isEmpty(text)) {
            rightTv.setVisibility(View.GONE);
        } else {
            if (rightTv != null) {
                if (!showBG) {
                    rightTv.setBackground(null);
                    rightTv.setTextColor(ContextCompat.getColor(this, R.color.text_33));
                }
                rightTv.setText(text, TextView.BufferType.SPANNABLE);
                rightTv.setOnClickListener(listener);
                rightTv.setVisibility(View.VISIBLE);
            }
            if (rightIb != null) {
                rightIb.setVisibility(View.GONE);
            }
        }
    }

    /**
     * toolbar右边
     *
     * @param resId
     * @param listener
     */
    public void setToolBarRight(@DrawableRes int resId, final View.OnClickListener listener) {
        TextView rightTv = findViewById(R.id.btn_reply);
        ImageButton rightIb = findViewById(R.id.toolbar_right_ib);

        if (rightTv != null) {
            rightTv.setVisibility(View.GONE);
        }
        if (rightIb != null) {
            if (resId == -1) {
                rightIb.setVisibility(View.GONE);
                return;
            }
            rightIb.setImageResource(resId);
            rightIb.setVisibility(View.VISIBLE);
            rightIb.setOnClickListener(listener);
        }
    }

    /**
     * 返回当前页面显示的导航栏右边按钮
     *
     * @return
     */
    public View getToolbarRightButton() {
        TextView rightTv = findViewById(R.id.btn_reply);
        if (rightTv != null && rightTv.getVisibility() == View.VISIBLE) {
            return rightTv;
        } else {
            ImageButton rightIb = findViewById(R.id.toolbar_right_ib);
            if (rightIb != null && rightIb.getVisibility() == View.VISIBLE) {
                return rightIb;
            }
            return null;
        }
    }

    /**
     * 取Intent传过来的Int值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntExtra(String key, int defaultValue) {
        int value = getIntent().getIntExtra(key, defaultValue);
        if (value == defaultValue) {
            value = StringUtil.integerValueOf(getIntent().getStringExtra(key), defaultValue);
        }
        return value;
    }

    /**
     * 取Intent传过来的Long值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLongExtra(String key, long defaultValue) {
        long value = getIntent().getLongExtra(key, defaultValue);
        if (value == defaultValue) {
            value = StringUtil.longValueOf(getIntent().getStringExtra(key), defaultValue);
        }
        return value;
    }

    public double getDoubleExtra(String key, int defaultValue) {
        double value = getIntent().getDoubleExtra(key, defaultValue);
        if (value == defaultValue) {
            value = StringUtil.doubleValueOf(getIntent().getStringExtra(key), defaultValue);
        }
        return value;
    }

    public <T> T getInstanceExtra(String key, Class<T> jsonClass) {
        String value = getIntent().getStringExtra(key);
        if (!TextUtils.isEmpty(value)) {
            return GsonSingleton.getInstance().fromJson(value, jsonClass);
        }
        return null;
    }

    /**
     * 是否统计该页
     */
    private boolean analyze = true;

    /**
     * 是否开启此页面的跳转统计
     *
     * @param analyze
     */
    public void setAnalyze(boolean analyze) {
        this.analyze = analyze;
    }


    public View getContentView() {
        return rootView;
    }

    /***************************状态栏设置****************************************************************/
    public void setStatusBar(int statusBarColor) {
        setStatusBar(false, statusBarColor, false);
    }

    public void setStatusBar(int statusBarColor, boolean isLightMode) {
        setStatusBar(false, statusBarColor, isLightMode);
    }

    public void setStatusBar(boolean isTranslucent, boolean isLightMode) {
        setStatusBar(isTranslucent, 0, isLightMode);
    }

    public void setStatusBar(boolean isTranslucent, int statusBarColor, boolean isLightMode) {
        StatusBarUtil.transparencyBar(this, isTranslucent, isLightMode);
        StatusBarUtil.setStatusBarColor(this, statusBarColor, isLightMode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onActivityResult(requestCode, resultCode, data);
        }
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityRouter.LOGIN_REQUEST_CODE && loginListener != null) {
            //登录返回
            if (resultCode == Activity.RESULT_OK && UserDataManager.getInstance().getUserSystem() != null && loginListener != null) {
                loginListener.onSuccess(UserDataManager.getInstance().getUserSystem());
            } else if (loginListener != null) {
                loginListener.onFailed(new ReturnJsonCodeException("未登录"));
            }
            loginListener = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (isNeedTakePhoto) {
            getTakePhotoHandler().onSaveInstanceState(outState);
        }
    }

    @Override
    public void finish() {
        super.finish();
        ActivityManagerUtil.getScreenManager().popActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fixInputMethodManagerLeak(this);
        UMShareAPI.get(this).release();
        // PayManager.unregisterReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*if (SmMediaPlayManager.getInstance().onKeyDown(keyCode, event)) {
            return true;
        }*/
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (analyze) {
            //统计页面跳转
            MobclickAgent.onPageEnd(this.getClass().getName());
        }
        //统计页面时长
        MobclickAgent.onPause(this);
        //取消所有的图片加载任务
        ImageLoader.getInstance().pause(SmContextWrap.obtain(this));
        //ThirdShareManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (analyze) {
            //统计页面跳转
            MobclickAgent.onPageStart(this.getClass().getName());
            //统计页面时长
            MobclickAgent.onResume(this);
        }
        ImageLoader.getInstance().resume(SmContextWrap.obtain(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().clear(this);
    }

    @Override
    public void onGetPhoto(List<String> data) {

    }

    @Override
    public void onModifyPhoto(ArrayList<String> list) {

    }

    /**
     * 获取状态栏以及凹槽屏高度中最高一项
     * android 6.0 以上支持（应用在6.0以上系统才状态栏兼容）
     */
    public int getCutOutAndStatusMaxHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return CutoutManager.getCutOutAndStatusMaxHeight(this);
        } else {
            return 0;
        }

    }

    public void setCutoutAndStatusbarPaddingToView(@IdRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = findViewById(id);
            if (view != null) {
                view.setPadding(0, getCutOutAndStatusMaxHeight(), 0, 0);
            }
        }


    }

    public void setCutoutAndStatusMaxHeightToView(View v) {
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

    public void setCutoutAndStatusMaxHeightToView(@IdRes int id) {
        View v = findViewById(id);
        setCutoutAndStatusMaxHeightToView(v);
    }

    public void setCutoutFullScreen() {
        CutoutManager.setCutoutFullScreen(this);
    }

    public void setCutoutOrStatusMaxHeightMarinTop(@IdRes int id) {
        View v = findViewById(id);
        if (v == null) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lp.topMargin = getCutOutAndStatusMaxHeight();
        } else {
            lp.topMargin = 0;
        }
        v.setLayoutParams(lp);
    }

    public boolean isCutoutScreen() {
        return CutoutManager.isCutoutScreen(this);
    }

    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object objGet = null;
        for (int i = 0; i < arr.length; i++) {
            String param = arr[i];
            try {
                f = imm.getClass().getDeclaredField(param);
                f.setAccessible(true);
                objGet = f.get(imm);
                if (objGet != null && objGet instanceof View) {
                    View vGet = (View) objGet;
                    if (vGet.getContext() == destContext) {
                        // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null);
                        // 置空，破坏掉path to gc节点
                        SmLogger.d("InputMethodManager 内存泄漏清理--> 已清理");
                    } else {
                        // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了

                        break;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
