package com.xx.module.common.view.base;


import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;

import com.xx.module.common.R;
import com.xx.module.common.presenter.BaseListPresenter;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * @author
 * @date 2017/6/21
 */

public abstract class SimpleBaseListActivity<T extends BaseListPresenter> extends BaseListActivity<T> {
    protected RecyclerView listRv;
    private AppBarLayout appBarLayout;
    protected PtrFrameLayout refreshLayout;

    @Override
    public RecyclerView getRecyclerView() {
        return listRv;
    }


    public void setAppBarLayout(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
    }

    protected void loadDataFromCacheAndNet() {

    }


    /**
     * 重写该方法进行加载数据前的初始化
     */
    protected void initBeforeLoadData() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_base_list_2;
    }

    @Override
    protected void beforeSetContentView() {
        setShowLoadingView(true);
    }

    @Override
    protected void initViewsAndEvents() {
        listRv = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        if (appBarLayout != null) {
            setPtrFrameLayout(refreshLayout, appBarLayout, 0);
        } else {
            setPtrFrameLayout(refreshLayout);
        }
        initBeforeLoadData();
        loadDataFromCacheAndNet();
    }
}
