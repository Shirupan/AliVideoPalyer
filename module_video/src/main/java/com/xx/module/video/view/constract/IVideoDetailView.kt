package com.xx.module.video.view.constract

import com.xx.module.common.view.contract.IBaseView
import com.xx.module.video.model.entity.VideoDetail

/**
 *@author someone
 *@date 2019-06-04
 */
interface IVideoDetailView:IBaseView {
    fun onVideoDetailResult(data: VideoDetail)
}