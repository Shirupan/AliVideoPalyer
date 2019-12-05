package com.xx.module.video.view.constract

import com.xx.lib.db.entity.MainVideo
import com.xx.module.common.view.contract.IBaseListView

/**
 *@author someone
 *@date 2019-06-11
 */
interface IMainShortVideoListView : IBaseListView {
    fun getListCache(list: List<MainVideo>?)
    fun onMainListResult(list: List<MainVideo>, page: Int)
}