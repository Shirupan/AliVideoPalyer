package com.xx.module.common.view.refresh;

import android.support.design.widget.AppBarLayout;

public interface IRefreshLayout {
    boolean isRefreshing();

    void refreshComplete();

    void setAppbarLayout(AppBarLayout appBarLayout);

    void unBind();
    void onBind();

    void setBackgroundColorRes(int backgroundColorRes);

    void autoRefresh();

    void setEnable(boolean enable);


}
