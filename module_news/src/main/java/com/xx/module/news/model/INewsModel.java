package com.xx.module.news.model;

import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.news.model.entity.NewsDetailJson;
import com.xx.lib.db.entity.NewsJson;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-12
 */
public interface INewsModel {
    void loadNewsList(@NotNull String token, int page, @NotNull SimpleSubscriber<List<NewsJson>> uiCallback);

    void loadNewsDetails(@NotNull String token, int sid, @NotNull SimpleSubscriber<NewsDetailJson> uiCallback);
}
