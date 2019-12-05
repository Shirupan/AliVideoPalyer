package com.xx.module.common.view.contract;

import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.model.entity.SmError;

import org.jetbrains.annotations.Nullable;

/**
 * @author someone
 * @date 2019-05-28
 */
public interface IViewManager {

    interface IMainLoginView extends IBaseView {

        void onLoginResult(UserSystem userSystem, SmError error);

        void onCheckUserResult(UserSystem us, SmError error);

        void onNoPwdLoginResult(UserSystem userSystem, SmError error);

        void onThirdLoginResult(UserSystem userSystem, SmError error);
    }

    interface IPhoneLoginView extends IBaseView {

        void onBindPhoneResult(boolean success, @Nullable SmError e);
    }

    interface IPasswordView extends IBaseView {

        void onRegisterResult(@Nullable UserSystem us, @Nullable SmError error);

        void onChangePasswordResult(@Nullable UserSystem us, @Nullable SmError error);
    }

}
