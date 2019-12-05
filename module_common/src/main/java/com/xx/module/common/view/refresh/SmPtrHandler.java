package com.xx.module.common.view.refresh;

import android.view.View;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * @author
 * @date 2018/3/5 0005
 */

public class SmPtrHandler implements PtrHandler {
    /**
     * 刷新
     */
    private Runnable refreshCallback;

    public SmPtrHandler(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }
}
