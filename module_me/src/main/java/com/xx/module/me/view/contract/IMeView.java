package com.xx.module.me.view.contract;

import com.xx.module.common.model.entity.SmError;
import com.xx.module.common.view.contract.IBaseView;
import com.xx.module.me.model.entity.MeMainInfo;

import org.jetbrains.annotations.Nullable;

/**
 * @author someone
 * @date 2019-05-31
 */
public interface IMeView extends IBaseView {
    void onToolResult(@Nullable MeMainInfo info, @Nullable SmError e);
}
