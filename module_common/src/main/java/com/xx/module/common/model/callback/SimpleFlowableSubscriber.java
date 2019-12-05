package com.xx.module.common.model.callback;

import org.reactivestreams.Subscription;

import io.reactivex.FlowableSubscriber;

/**
 * @author someone
 * @date 2019-06-14
 */
public abstract class SimpleFlowableSubscriber<T> implements FlowableSubscriber<T> {
    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
