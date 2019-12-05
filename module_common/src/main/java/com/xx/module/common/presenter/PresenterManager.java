package com.xx.module.common.presenter;

/**
 *
 *
 * @Author
 * @Create 2016/12/26
 */
public class PresenterManager {

    private PresenterManager() {

    }

    public static PresenterManager getInstance() {
        return SingleHolder.manager;
    }

    static class SingleHolder {
        static PresenterManager manager = new PresenterManager();

        SingleHolder() {

        }
    }

    public BasePresenter getPresenter(Class clz) {
        BasePresenter presenter = null;
        try {
            presenter = (BasePresenter) Class.forName(clz.getName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return presenter;
    }

    public BaseListPresenter getListPresenter(Class clz) {
        BaseListPresenter presenter = null;
        try {
            presenter = (BaseListPresenter) Class.forName(clz.getName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return presenter;
    }


}
