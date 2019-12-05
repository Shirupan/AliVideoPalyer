package com.xx.module.common.view.contract;

/**
 * @author
 * @date 2018/1/19 0019
 */

public interface IView {
    void showNoNetWork();

    void onLoadDataCompleted();

    boolean onLoadCacheSuccess();

    void onLoadDataFailed(Throwable result);
}
