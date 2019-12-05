package com.xx.module.me.view.contract

import com.xx.module.common.view.contract.IBaseListView
import com.xx.module.me.model.entity.MyHistoryJson

/**
 *@author someone
 *@date 2019-06-12
 */
interface IMyHistoryView : IBaseListView {
    fun onLoadHistoryCacheResult(list: List<MyHistoryJson>?)
    fun onLoadHistoryResult(list: List<MyHistoryJson>,page: Int)

}