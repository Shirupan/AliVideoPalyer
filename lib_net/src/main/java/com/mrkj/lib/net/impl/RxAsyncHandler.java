package com.mrkj.lib.net.impl;


import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;


/**
 * Rxjava标准任务线程封装。开启线程执行任务doSomethingBackground()，回调到主线程中
 * 根据传入的RxAppCompatActivity或者RxFragment绑定到生命周期。当然也可以不传
 *
 * @author
 * @date 2016-10-18
 */

public abstract class RxAsyncHandler<T> implements IRxHandler<T> {
    private RxFragment mFragment;
    private RxAppCompatActivity mActivity;
    private Disposable disposable;

    public RxAsyncHandler() {

    }

    public RxAsyncHandler(Object bindLift) {
        if (bindLift instanceof RxFragment) {
            mFragment = (RxFragment) bindLift;
        } else if (bindLift instanceof RxAppCompatActivity) {
            mActivity = (RxAppCompatActivity) bindLift;
        }
    }

    @Override
    public IRxHandler execute() {
        LifecycleTransformer<T> tLifecycleTransformer = null;
        if (mActivity != null) {
            tLifecycleTransformer = mActivity.bindUntilEvent(ActivityEvent.DESTROY);
        } else if (mFragment != null) {
            tLifecycleTransformer = mFragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW);
        }

        Observer<T> ob = new Observer<T>() {

            @Override
            public void onError(Throwable e) {
                RxAsyncHandler.this.onError(e);
                RxAsyncHandler.this.onComplete();
            }

            @Override
            public void onComplete() {
                RxAsyncHandler.this.onComplete();
            }


            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                RxAsyncHandler.this.onStart();
            }

            @Override
            public void onNext(T t) {
                RxAsyncHandler.this.onNext(t);
            }
        };
        if (tLifecycleTransformer != null) {
            Observable.create(new ObservableOnSubscribe<T>() {
                @Override
                public void subscribe(ObservableEmitter<T> e) throws Exception {
                    try {
                        T data = doSomethingBackground();
                        if (data == null) {
                            e.onError(new NullPointerException());
                        } else {
                            e.onNext(data);
                        }
                        e.onComplete();
                    } catch (Exception e1) {
                        throw Exceptions.propagate(e1);
                    }
                }
            }).subscribeOn(Schedulers.computation())
                    .compose(tLifecycleTransformer)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ob);
        } else {
            Observable.create(new ObservableOnSubscribe<T>() {
                @Override
                public void subscribe(ObservableEmitter<T> e) throws Exception {
                    try {
                        T data = doSomethingBackground();
                        e.onNext(data);
                        e.onComplete();
                    } catch (Exception e1) {
                        throw Exceptions.propagate(e1);
                    }
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ob);
        }
        return this;
    }


    /**
     * 取消任务
     */
    @Override
    public void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
    }

    public Disposable getDisposable() {
        return disposable;
    }
}
