package com.xx.module.me.view.contract;


import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.model.entity.SmError;
import com.xx.module.common.view.contract.IBaseView;

/**
 * @author
 * @date 2017/7/10
 */

public interface IInitDataView extends IBaseView {
    void onSaveUserResult(UserSystem us, SmError e);
}
