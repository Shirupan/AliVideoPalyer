package com.xx.module.common.view.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xx.module.common.R;
import com.xx.module.common.presenter.BaseListPresenter;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * 仅需要配置Adapter以及loadData等信息
 *
 * @author
 */

public abstract class SimpleListFragment<T extends BaseListPresenter> extends BaseListFragment<T> {
    private RecyclerView listRv;
    private AppBarLayout appBarLayout;

    public PtrFrameLayout refreshLayout;


    @Override
    public int getLayoutID() {
        return R.layout.fragment_base_list;
    }

    @Override
    protected void initViewsAndEvents(View view) {
        listRv = view.findViewById(R.id.recycler_view);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        initBeforeLoadData();
        if (!isLazyFragment()) {
            loadDataFromCacheAndNet();
        }
        setPtrFrameLayout(refreshLayout, 0);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * 重写该方法进行加载数据前的初始化
     */
    protected void initBeforeLoadData() {
    }

    public void setPtrFrameLayoutEnable(boolean enable) {
        refreshLayout.setEnabled(enable);
    }

    @Override
    protected void onFirstUserVisible() {
        loadDataFromCacheAndNet();
    }


    @Override
    public RecyclerView getRecyclerView() {
        return listRv;
    }


    public void setAppBarLayout(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
        if (refreshLayout == null) {
            return;
        }
        if (appBarLayout != null) {
            setPtrFrameLayout(refreshLayout, appBarLayout, 0);
        } else {
            setPtrFrameLayout(refreshLayout);
        }
    }

    protected abstract void loadDataFromCacheAndNet();

}
