package com.xx.module.common.view.contract;

/**
 * @Author
 * @Create 2016/12/26
 */
public interface IBaseListView extends IView {
    /**
     * 分页请求数据失败时，回滚页码等操作
     *
     * @param e
     */
    void onGetDataListFailed(Throwable e);


}
