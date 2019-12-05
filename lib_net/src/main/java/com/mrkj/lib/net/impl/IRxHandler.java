package com.mrkj.lib.net.impl;

/**
 * @author
 * @date 2017/5/18
 */

public interface IRxHandler<T> {
    /**
     * 线程处理事件，结果回调主线程，到{@link IRxHandler#onNext(Object)}中。<p>
     * 可以在{@link IRxHandler#onNext(Object)}处理后续事宜<p>
     * 如果返回null,则{@link IRxHandler#onNext(Object)}不会执行，会报null错误
     *
     * @return
     */
    T doSomethingBackground();

    void dispose();

    void onStart();

    void onNext(T data);

    void onComplete();

    void onError(Throwable e);

    IRxHandler execute();
}
