package com.xx.video_dev.common;

import com.xx.module.common.client.BaseClient;

import java.util.Map;

/**
 * @author someone
 * @date 2019-06-14
 */
public class AppModuel extends BaseClient<IAppModel> {
    @Override
    protected Class<? extends IAppModel> getModelClass() {
        return AppModelImpl.class;
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {

    }
}
