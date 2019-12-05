package com.xx.module.news.view.constract

import com.xx.module.common.view.contract.IBaseListView
import com.xx.lib.db.entity.NewsJson

/**
 *@author someone
 *@date 2019-06-12
 */
interface IMainNewsView : IBaseListView {
    fun loadNewsCacheList(list: List<NewsJson>?)
    fun onLoadNewsListResult(list: List<NewsJson>, page: Int)
}