package com.xx.module.common.model.callback;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.lib.db.exception.SmCacheException;
import com.xx.module.common.view.contract.IBaseListView;

import java.util.List;

import io.reactivex.exceptions.CompositeException;
import io.rx_cache2.RxCacheException;

/**
 * @Function 该类功能：
 * @Author
 * @Date 2017/3/17
 */

public abstract class ResultListUICallback<T> extends SimpleSubscriber<T> {

    private IBaseListView mView;
    private boolean showFailedToast = true;
    /**
     * 是否完整的加载（而非任务被中断）
     */
    private boolean isLoadFinished;

    public ResultListUICallback() {
        super(null);
    }


    /**
     * @param view 如果实现 {@link IBaseListView}或其子接口，
     *             将在合适的位置回调方法<p>
     *             {@link com.xx.module.common.view.contract.IBaseView#onLoadDataFailed(Throwable)}、<p>
     *             {@link com.xx.module.common.view.contract.IBaseView#onLoadDataCompleted()}
     */
    public ResultListUICallback(IBaseListView view) {
        super(view);
        mView = view;
    }


    @Override
    public void onError(Throwable t) {
        String message;
        Throwable error = t;
        IBaseListView resultView = mView;
        if (t instanceof CompositeException) {
            List<Throwable> exs = ((CompositeException) t).getExceptions();
            message = t.getLocalizedMessage();
            for (Throwable e : exs) {
                if (e instanceof SmCacheException || e instanceof RxCacheException) {
                    showFailedToast = false;
                    message = "加载失败";
                    resultView = null;
                    //不回调给loading页面
                    error = new SmCacheException("加载失败");
                    break;
                } else {
                    resultView = mView;
                    showFailedToast = true;
                    message = ExceptionUtl.catchTheError(e);
                }
            }
        } else {
            message = ExceptionUtl.catchTheError(t);
        }

        if (resultView != null) {
            resultView.onGetDataListFailed(error);
            resultView.onLoadDataFailed(error);
        }
        if (showFailedToast) {
            Context context = null;
            if (mView instanceof Activity) {
                context = (Context) mView;
            } else if (mView instanceof Fragment) {
                context = ((Fragment) mView).getContext();
            }
            if (context != null) {
                SmToast.showToastRight(context, message);
            } else {
                String mes = t.getLocalizedMessage();
                if (!TextUtils.isEmpty(mes)) {
                    SmLogger.d(mes);
                }
            }
        }
        isLoadFinished = true;
        onComplete();
    }

    @CallSuper
    @Override
    public void onNext(T t) {
        isLoadFinished = true;
        super.onNext(t);
    }

    @Override
    public void onComplete() {
        if (mView != null) {
            mView.onLoadDataCompleted();
        }
    }


    /**
     * 是否启用默认的错误信息提醒.重写_onFailure()方法的话，需要关闭默认错误信息提醒
     *
     * @return
     */
    public ResultListUICallback<T> unShowDefaultMessage() {
        showFailedToast = false;
        return this;
    }

}
