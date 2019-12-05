package com.xx.module.news;

import com.xx.module.common.annotation.AnnotationProcessor;
import com.xx.module.common.client.BaseClient;
import com.xx.module.news.model.INewsModel;
import com.xx.module.news.model.NewsModelImpl;
import com.xx.module.news.view.NewsDetailActivity;

import java.util.Map;

/**
 * @author someone
 * @date 2019-06-12
 */
public class NewsModule extends BaseClient<INewsModel> {
    @Override
    protected Class<? extends INewsModel> getModelClass() {
        return NewsModelImpl.class;
    }

    @Override
    protected void injectPageRouter(Map<String, Class<?>> map) {
        map.put(AnnotationProcessor.getActivityPath(NewsDetailActivity.class), NewsDetailActivity.class);
    }
}
