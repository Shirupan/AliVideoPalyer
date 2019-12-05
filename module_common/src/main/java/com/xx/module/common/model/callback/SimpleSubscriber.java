package com.xx.module.common.model.callback;


import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.mrkj.lib.net.loader.file.SmNetProgressDialog;
import com.xx.module.common.presenter.BasePresenter;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author
 * @date 2016-10-18
 */


public abstract class SimpleSubscriber<T> implements Observer<T> {
    private Object bindLifeObject;
    private Disposable subscription;
    private SimpleSubscriber superSubscriber;
    private Dialog progressDialog;
    private String dialogMessage = "请稍等...";
    private boolean showDialog = false;
    private boolean canDiadlogCancel = true;

    /**
     * {@link com.trello.rxlifecycle2.components.support.RxAppCompatActivity}<p>
     * 或者{@link com.trello.rxlifecycle2.components.RxFragment} bindLifeObject<p>
     * 或者{@link BasePresenter}添加到任务队列当中
     * 绑定Rxjava到对应的生命周期
     */
    public SimpleSubscriber(Object bindLifeObject) {
        this.bindLifeObject = bindLifeObject;
    }

    public SimpleSubscriber(SimpleSubscriber subscriber) {
        superSubscriber = subscriber;
        if (subscriber != null) {
            this.bindLifeObject = subscriber.getBindLifeObject();
        }
    }

    public SimpleSubscriber(Object bindLifeObject, boolean showDialog) {
        this(bindLifeObject, showDialog, true);
    }

    public SimpleSubscriber(Object bindLifeObject, boolean showDialog, boolean canCancel) {
        this(bindLifeObject);
        this.showDialog = showDialog;
        this.canDiadlogCancel = canCancel;
    }

    public SimpleSubscriber() {
    }

    @Override
    public void onNext(T t) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onComplete() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (subscription != null && bindLifeObject instanceof BasePresenter) {
            ((BasePresenter) bindLifeObject).unsubscribe(subscription);
        }
        if (superSubscriber != null) {
            superSubscriber.onComplete();
        }
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        if (bindLifeObject instanceof BasePresenter) {
            subscription = disposable;
            ((BasePresenter) bindLifeObject).addSubscription(disposable);
        }
        if (superSubscriber != null) {
            superSubscriber.onSubscribe(disposable);
        }
        if (showDialog) {
            showLoadingDialog(disposable);
        }
    }

    private void showLoadingDialog(Disposable disposable) {
        Activity realActivity = null;
        SmNetProgressDialog.Builder builder = null;
        if (getBindLifeObject() instanceof Activity) {
            Activity activity = (Activity) getBindLifeObject();
            builder = new SmNetProgressDialog.Builder(activity).setSubscription(disposable);
            realActivity = activity;
        } else if (getBindLifeObject() instanceof Fragment) {
            Fragment fragment = (Fragment) getBindLifeObject();
            builder = new SmNetProgressDialog.Builder(fragment.getContext()).setSubscription(disposable);
            realActivity = fragment.getActivity();
        }
        if (builder != null && !TextUtils.isEmpty(dialogMessage) && realActivity != null) {
            final SmNetProgressDialog.Builder finalBuilder = builder.setCancelable(canDiadlogCancel);
            final Activity finalRealActivity = realActivity;
            realActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!finalRealActivity.isFinishing()) {
                        finalBuilder.setMessage(dialogMessage);
                        finalBuilder.setDimBehind(false);
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        progressDialog = finalBuilder.build();
                        progressDialog.show();
                    }
                }
            });
        }
    }

    @Override
    public void onError(Throwable e) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (subscription != null && bindLifeObject instanceof BasePresenter) {
            ((BasePresenter) bindLifeObject).unsubscribe(subscription);
        }
        if (superSubscriber != null) {
            superSubscriber.onError(e);
        }
    }

    /**
     * 返回的是{@link com.trello.rxlifecycle2.components.support.RxAppCompatActivity}或者{@link com.trello.rxlifecycle2.components.RxFragment}
     * 用于绑定RxJAVA到对应的生命周期
     *
     * @return
     */
    public Object getBindLifeObject() {
        return bindLifeObject;
    }

    public Disposable getSubscription() {
        return subscription;
    }

    public SimpleSubscriber<T> setDialogMessage(String message) {
        dialogMessage = message;
        return this;
    }

    public Dialog getProgressDialog() {
        return progressDialog;
    }
}
