package com.xx.module.common.presenter;/**
 * Created by someone on 2016/12/26.
 */

import com.xx.module.common.view.contract.IBaseListView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * @Author
 * @Create 2016/12/26
 */
public abstract class BaseListPresenter<V extends IBaseListView> {
    private SoftReference<V> mViewRef;
    private List<SoftReference<Disposable>> subscriptionSparseArray = new ArrayList<>();

    public void bindView(V view) {
        if (mViewRef == null || mViewRef.get() == null || mViewRef.get() != view) {
            mViewRef = new SoftReference<>(view);
        }
    }

    public void unBindView() {
        if (mViewRef != null) {
            mViewRef = null;
        }
        for (SoftReference<Disposable> subscription : subscriptionSparseArray) {
            if (subscription != null && subscription.get() != null) {
                subscription.get().dispose();
            }
        }
        subscriptionSparseArray.clear();
    }

    public V getView() {
        if (mViewRef != null && mViewRef.get() != null) {
            return mViewRef.get();
        }
        return null;
    }

    /**
     * 保存任务，用于在解除presenter绑定时候取消任务
     *
     * @param subscription
     */
    public void addSubscription(Disposable subscription) {
        for (SoftReference<Disposable> ref : subscriptionSparseArray) {
            if (ref.get() != null && ref.get() == subscription) {
                return;
            }
        }
        SoftReference<Disposable> newRef = new SoftReference<>(subscription);
        subscriptionSparseArray.add(newRef);
    }

    public void unsubscribe(Disposable subscription) {
        for (SoftReference<Disposable> ref : subscriptionSparseArray) {
            if (ref.get() != null && ref.get() == subscription) {
                subscriptionSparseArray.remove(ref);
                return;
            }
        }
        if (subscription != null) {
            subscription.dispose();
        }
    }


    /**
     * {@link com.mrkj.base.views.base.BaseListFragment}<p>
     * 这基类如果开启{@link com.mrkj.base.views.base.BaseListFragment#setIsLazyFragmentMode(boolean)}的话会在
     * {@link BaseListFragment#onUserVisible()}中调用
     * 获取内存中的缓存数据
     */
    public boolean getMemoryCacheData(int page) {
        return false;
    }


}
