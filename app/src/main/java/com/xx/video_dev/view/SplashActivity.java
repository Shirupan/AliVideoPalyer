package com.xx.video_dev.view;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.impl.RxDelayHandler;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.app.dependendy.DependencyModule;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.lib.db.entity.SplashAdvert;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.ImageLoaderListener;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;
import com.xx.video_dev.R;
import com.xx.video_dev.common.AppCacheProvider;
import com.xx.video_dev.common.AppModuel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * @author someone
 * @date 2019-05-29
 */
public class SplashActivity extends BaseActivity implements Handler.Callback {
    private ImageView adIv;
    private TextView mStepTv;
    private Disposable mAdDisposable;
    private Handler mHandler;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBar(true, true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViewsAndEvents() {
        adIv = findViewById(R.id.splash_ad);
        mStepTv = findViewById(R.id.splash_ad_step);
        mStepTv.setVisibility(View.GONE);
        mStepTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdDisposable != null) {
                    mAdDisposable.dispose();
                }
                startMainActivity();
            }
        });
        int size = getCutOutAndStatusMaxHeight();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mStepTv.getLayoutParams();
        lp.topMargin = lp.topMargin + size;
        mStepTv.setLayoutParams(lp);
        mHandler = new Handler(this);
        PermissionUtil.checkAndRequestPermissions(this, new PermissionUtil.SimpleOnPermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        loadAdvert();
                    }

                    @Override
                    public void onFailed() {
                        loadAdvert();
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE);
        //推送
        UserDataManager.getInstance().startPushService(this);
    }

    /**
     * 联网获取闪屏信息
     */
    private void loadAdvert() {
        ModuleManager.of(AppModuel.class)
                .getModelClient()
                .loadSplashAD(new ResultUICallback<SplashAdvert>(this) {
                    @Override
                    public void onNext(SplashAdvert splashAdvert) {
                        super.onNext(splashAdvert);
                        showAd(splashAdvert, false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        showAd(null, false);
                    }
                });
        //获取当前版本审核状态
        ModuleManager.of(DependencyModule.class)
                .getModelClient()
                .getAppStatus(new ResultUICallback<String>() {
                    @Override
                    public void onNext(String splashAdvert) {
                        super.onNext(splashAdvert);
                        SmLogger.i("当前版本审核状态：" + splashAdvert);
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);

                    }
                });
    }

    /**
     * 从缓存中获取广告
     */
    private void loadAdCache() {
        DataProviderManager.get(AppCacheProvider.class)
                .loadSplashADCache(null)
                .compose(RetrofitManager.<SplashAdvert>rxTransformer(null, SplashAdvert.class))
                .subscribe(new ResultUICallback<SplashAdvert>() {
                    @Override
                    public void onNext(SplashAdvert splashAdvert) {
                        super.onNext(splashAdvert);
                        showAd(splashAdvert, true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        showAd(null, false);
                    }
                }.unShowDefaultMessage());
    }

    /**
     * 加载广告图
     *
     * @param advert
     * @param fromCache
     */
    private void showAd(final SplashAdvert advert, boolean fromCache) {

        if (advert == null) {
            if (!fromCache) {
                loadAdCache();
            } else {
                startMainActivity();
            }
        } else {
            adIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdDisposable != null) {
                        mAdDisposable.dispose();
                    }
                    startMainActivity();
                    if (!TextUtils.isEmpty(advert.getWeburl()) && advert.getWeburl().startsWith("http")) {
                        ActivityRouter.get().startWebActivity(v.getContext(), advert.getWeburl(), "");
                    } else {
                        ActivityRouter.get().startActivity(v.getContext(), advert.getWeburl());
                    }
                    delayToSelfFinish();
                }
            });
            ImageLoader.getInstance().load(SmContextWrap.obtain(this), advert.getImgurl(), 0,
                    new ImageLoaderListener<Drawable>(adIv) {

                        @Override
                        public void onSuccess(Drawable data) {
                            if (data != null) {
                                adIv.setImageDrawable(data);
                                final int adTime = advert.getRestime() <= 0 ? 3 : advert.getRestime();
                                startTimeCountDown(adTime);
                            }
                        }

                        @Override
                        public void onLoadFailed() {
                            startMainActivity();
                        }
                    });

        }
    }


    /**
     * 开始倒计时
     *
     * @param adTime
     */
    private void startTimeCountDown(final int adTime) {
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(adTime + 1)
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long aLong) throws Exception {
                        return (int) (adTime - aLong);
                    }
                })
                .compose(RetrofitManager.<Integer>rxTransformer())
                .subscribe(new ResultUICallback<Integer>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        mAdDisposable = disposable;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        super.onNext(integer);
                        mStepTv.setVisibility(View.VISIBLE);
                        mStepTv.setText(String.format(Locale.getDefault(), "跳过%ds", integer));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        startMainActivity();
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        startMainActivity();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = ActivityRouter.get().getIntent(this, RouterUrl.ACTIVITY_MAIN);
        if (intent == null) {
            new RxDelayHandler(1, TimeUnit.SECONDS) {
                @Override
                public Integer doSomethingBackground() {
                    return 1;
                }

                @Override
                public void onNext(Integer data) {
                    startMainActivity();
                }
            }.execute();
            return;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_enter, R.anim.alpha_exit);

        if (getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            ActivityRouter.get().startActivity(this, uri.toString());
        }
        delayToSelfFinish();
    }


    /**
     * 延迟自毁
     */
    private void delayToSelfFinish() {
        mHandler.sendEmptyMessageDelayed(0, 2000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(0);
        if (mAdDisposable != null) {
            mAdDisposable.dispose();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        finish();
        return true;
    }
}
