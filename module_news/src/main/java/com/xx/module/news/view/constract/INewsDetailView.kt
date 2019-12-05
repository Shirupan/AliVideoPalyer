package com.xx.module.news.view.constract

import com.xx.module.common.view.contract.IBaseView
import com.xx.module.news.model.entity.NewsDetailJson

/**
 *@author someone
 *@date 2019-06-12
 */
interface INewsDetailView :IBaseView {
    fun onNewsDetailResult(json: NewsDetailJson)
}