package com.mrkj.lib.net.impl;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 调度到主线程
 *
 * @author
 */

public abstract class RxMainThreadScheduler implements IRxHandler<Integer> {
    private Disposable disposable;

    @Override
    public Integer doSomethingBackground() {
        return 0;
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
    public void onError(Throwable e) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public IRxHandler execute() {
        Observable.just(0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        RxMainThreadScheduler.this.onNext(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        RxMainThreadScheduler.this.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        RxMainThreadScheduler.this.onComplete();
                    }
                });
        return this;
    }
}
