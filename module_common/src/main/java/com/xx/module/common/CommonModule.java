package com.xx.module.common;

import android.content.Context;

import com.tencent.smtt.sdk.QbSdk;
import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.client.BaseClient;
import com.xx.module.common.model.CommonModel;
import com.xx.module.common.model.ICommonModel;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.view.ImagePageActivity;
import com.xx.module.common.view.ImageShowerActivity;
import com.xx.module.common.view.WebViewActivity;
import com.xx.module.common.view.login.LoginMainActivity;
import com.xx.module.common.view.login.PasswordSettingActivity;
import com.xx.module.common.view.login.PhoneLoginActivity;

import java.util.Map;

/**
 * @author someone
 * @date 2019-05-28
 */
public class CommonModule extends BaseClient<ICommonModel> {


    @Override
    protected Class<? extends ICommonModel> getModelClass() {
        return CommonModel.class;
    }

    @Override
    public void init(Context context) {
        super.init(context);
        //初始化X5 WebView
        QbSdk.initX5Environment(context, null);
        DataProviderManager.init(context);
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {
        map.put(AnnotationProcessor.getActivityPath(WebViewActivity.class), WebViewActivity.class);
        map.put(AnnotationProcessor.getActivityPath(ImageShowerActivity.class), ImageShowerActivity.class);
        map.put(AnnotationProcessor.getActivityPath(ImagePageActivity.class), ImagePageActivity.class);

        map.put(AnnotationProcessor.getActivityPath(LoginMainActivity.class), LoginMainActivity.class);
        map.put(AnnotationProcessor.getActivityPath(PhoneLoginActivity.class), PhoneLoginActivity.class);
        map.put(AnnotationProcessor.getActivityPath(PasswordSettingActivity.class), PasswordSettingActivity.class);


    }
}
