package com.xx.module.video.view.constract;


import com.xx.lib.db.entity.MainVideo;
import com.xx.module.common.view.contract.IBaseListView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IShortVideoListView extends IBaseListView {
    void onMainListResult(@NotNull List<? extends MainVideo> list, int page);
}
