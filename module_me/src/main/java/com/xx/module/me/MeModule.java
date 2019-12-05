package com.xx.module.me;

import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.client.BaseClient;
import com.xx.module.me.model.IMeModel;
import com.xx.module.me.model.MeModelImpl;
import com.xx.module.me.view.InitNickNameActivity;
import com.xx.module.me.view.MeInfoEditActivity;
import com.xx.module.me.view.MyCollectionActivity;
import com.xx.module.me.view.MyHistoryActivity;
import com.xx.module.me.view.MyPraiseActivity;
import com.xx.module.me.view.SettingActivity;

import java.util.Map;

/**
 * @author someone
 * @date 2019-05-31
 */
public class MeModule extends BaseClient<IMeModel> {
    @Override
    protected Class<? extends IMeModel> getModelClass() {
        return MeModelImpl.class;
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {
        map.put(AnnotationProcessor.getActivityPath(MeInfoEditActivity.class), MeInfoEditActivity.class);
        map.put(AnnotationProcessor.getActivityPath(InitNickNameActivity.class), InitNickNameActivity.class);
        map.put(AnnotationProcessor.getActivityPath(MyHistoryActivity.class), MyHistoryActivity.class);
        map.put(AnnotationProcessor.getActivityPath(MyPraiseActivity.class), MyPraiseActivity.class);
        map.put(AnnotationProcessor.getActivityPath(MyCollectionActivity.class), MyCollectionActivity.class);
        map.put(AnnotationProcessor.getActivityPath(SettingActivity.class), SettingActivity.class);
    }
}
