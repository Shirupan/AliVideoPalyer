package com.xx.module.common.model.callback;


import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.lib.db.exception.SmCacheException;
import com.xx.module.common.view.contract.IView;

import java.net.ConnectException;
import java.util.List;

import io.reactivex.exceptions.CompositeException;
import io.rx_cache2.RxCacheException;

/**
 * @author someone
 * @Function 该类功能：
 * @Author
 * @Date 2017/3/17
 */

public class ResultUICallback<T> extends SimpleSubscriber<T> {
    private IView mView;
    private boolean isShowFailedToast = true;


    /**
     * @param bindLifeObject 如果实现{@link com.xx.module.common.view.contract.IBaseView} 或其子接口，
     *                       将在合适的位置回调方法<p>
     *                       {@link com.xx.module.common.view.contract.IBaseView#onLoadDataFailed(Throwable)}、<p>
     *                       {@link com.xx.module.common.view.contract.IBaseView#onLoadDataCompleted()} ()}
     */
    public ResultUICallback(Object bindLifeObject) {
        this(bindLifeObject, false);
    }

    public ResultUICallback(Object bindLifeObject, boolean showDialog) {
        this(bindLifeObject, showDialog, false);
    }

    /**
     * @param bindLifeObject
     * @param showDialog     是否显示loading
     * @param canCancel      loading是否可以关闭（默认可以关闭）
     */
    public ResultUICallback(Object bindLifeObject, boolean showDialog, boolean canCancel) {
        super(bindLifeObject, showDialog, canCancel);
        if (bindLifeObject instanceof IView) {
            mView = (IView) bindLifeObject;
        }
    }

    public ResultUICallback() {
        super();
    }


    @Override
    public void onError(Throwable t) {
        super.onError(t);
        Throwable result = t;
        String message;
        IView resultView = mView;
        if (t instanceof CompositeException) {
            List<Throwable> exs = ((CompositeException) t).getExceptions();
            message = t.getLocalizedMessage();
            for (Throwable e : exs) {
                result = e;
                if (e instanceof SmCacheException || e instanceof RxCacheException) {
                    isShowFailedToast = false;
                    message = "加载失败";
                    resultView = null;
                    //  mView = null; //不回调给loading页面
                } else {
                    resultView = mView;
                    result = e;
                    isShowFailedToast = true;
                    message = ExceptionUtl.catchTheError(e);
                    break;
                }
            }
        } else {
            message = ExceptionUtl.catchTheError(t);
        }
        if (isShowFailedToast) {
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
        if (resultView != null) {
            if (t instanceof ConnectException) {
                resultView.showNoNetWork();
            } else {
                resultView.onLoadDataFailed(result);
            }
        }
    }


    @Override
    public void onComplete() {
        super.onComplete();
        if (mView != null) {
            mView.onLoadDataCompleted();
        }
    }

    /**
     * 是否启用默认的错误信息提醒.重写_onFailure()方法的话，需要关闭默认错误信息提醒
     *
     * @return
     */
    public ResultUICallback<T> unShowDefaultMessage() {
        isShowFailedToast = false;
        return this;
    }

    public ResultUICallback<T> setLoadingDialogMessage(String message) {
        setDialogMessage(message);
        return this;
    }


}
