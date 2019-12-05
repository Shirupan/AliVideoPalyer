package com.xx.module.video.view.constract

import com.xx.module.common.model.entity.SmError
import com.xx.module.common.view.contract.IBaseView
import com.xx.module.video.model.entity.MainVideoTab
import com.xx.module.video.model.entity.VideoTabManagerJson

/**
 *@author someone
 *@date 2019-06-04
 */
interface IVideoTabManagerView : IBaseView {
    fun onMyTabsResult(list: VideoTabManagerJson?, smError: SmError?)
    fun onDeleteSuccessResult(data: MainVideoTab)
    fun onAddSuccessResult(data: MainVideoTab)
    fun onChangeTabResult(from: MainVideoTab, to: MainVideoTab, success: Boolean)
}