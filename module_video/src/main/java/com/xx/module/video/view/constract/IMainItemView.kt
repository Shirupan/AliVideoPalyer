package com.xx.module.video.view.constract

import com.xx.lib.db.entity.MainVideo
import com.xx.module.common.view.contract.IBaseListView

/**
 *@author someone
 *@date 2019-05-30
 */
interface IMainItemView : IBaseListView {
    fun getListCache(t: List<MainVideo>?)
    fun onMainListResult(list: List<MainVideo>,page:Int)
}