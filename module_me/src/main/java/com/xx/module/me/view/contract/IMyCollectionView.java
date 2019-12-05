package com.xx.module.me.view.contract;

import com.xx.module.common.view.contract.IBaseListView;
import com.xx.module.me.model.entity.MyHistoryJson;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-13
 */
public interface IMyCollectionView extends IBaseListView {

    void onCollectionHistoryCacheResult(List<MyHistoryJson> list);

    void onCollectionHistoryResult(List<MyHistoryJson> list, int page);

    void onDelCollectionResult(String content);
}
