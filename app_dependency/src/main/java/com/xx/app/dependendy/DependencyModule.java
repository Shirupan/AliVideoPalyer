package com.xx.app.dependendy;

import com.xx.app.dependendy.model.DependencyModel;
import com.xx.app.dependendy.model.IDependencyModel;
import com.xx.app.dependendy.view.MainActivity;
import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.client.BaseClient;

import java.util.Map;

/**
 * @author someone
 * @date 2019-05-30
 */
public class DependencyModule extends BaseClient<IDependencyModel> {
    @Override
    protected Class<? extends IDependencyModel> getModelClass() {
        return DependencyModel.class;
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {
        map.put(AnnotationProcessor.getActivityPath(MainActivity.class), MainActivity.class);
    }
}
