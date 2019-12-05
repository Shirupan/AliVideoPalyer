package com.mrkj.lib.net.impl;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 延时任务
 *
 * @author
 * @date 2017/5/18
 */

public abstract class RxDelayHandler implements IRxHandler<Integer> {

    private RxFragment mFragment;
    private RxAppCompatActivity mActivity;
    private Disposable disposable;
    private int delayTime;
    private TimeUnit mTimeUntil;

    public RxDelayHandler(int delayTime, TimeUnit timeUntil) {
        this.delayTime = delayTime;
        this.mTimeUntil = timeUntil;
    }

    /**
     * @param delayTime
     * @param timeUntil
     * @param bindLift  绑定到相应生命周期 {@link RxAppCompatActivity}或者{@link RxFragment}
     */
    public RxDelayHandler(int delayTime, TimeUnit timeUntil, Object bindLift) {
        this.delayTime = delayTime;
        this.mTimeUntil = timeUntil;
        if (bindLift instanceof RxFragment) {
            mFragment = (RxFragment) bindLift;
        } else if (bindLift instanceof RxAppCompatActivity) {
            mActivity = (RxAppCompatActivity) bindLift;
        }
    }

    public Disposable getDisposable() {
        return disposable;
    }

    @Override
    public void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
    }


    @Override
    public void onComplete() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Integer data) {

    }

    @Override
    public IRxHandler execute() {
        LifecycleTransformer<Integer> tLifecycleTransformer = null;
        if (mActivity != null) {
            tLifecycleTransformer = mActivity.bindUntilEvent(ActivityEvent.DESTROY);
        } else if (mFragment != null) {
            tLifecycleTransformer = mFragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW);
        }
        Observer<Integer> ob = new Observer<Integer>() {
            @Override
            public void onError(Throwable e) {
                RxDelayHandler.this.onError(e);
            }

            @Override
            public void onComplete() {
                RxDelayHandler.this.onComplete();
            }


            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Integer t) {
                RxDelayHandler.this.onNext(t);
            }
        };
        if (tLifecycleTransformer != null) {
            Observable.just(0)
                    .delay(delayTime, mTimeUntil)
                    .throttleFirst(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.computation())
                    .map(new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) throws Exception {
                            return doSomethingBackground();
                        }
                    })
                    .compose(tLifecycleTransformer)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ob);
        } else {
            Observable.just(0)
                    .delay(delayTime, mTimeUntil)
                    .throttleFirst(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(io.reactivex.schedulers.Schedulers.computation())
                    .map(new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) throws Exception {
                            return doSomethingBackground();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ob);
        }
        return this;
    }
}
