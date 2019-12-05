package com.xx.module.me.view.contract;

import com.xx.module.common.view.contract.IBaseListView;
import com.xx.module.me.model.entity.MyHistoryJson;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-13
 */
public interface IMyPraiseView extends IBaseListView {

    void onPraiseHistoryCacheResult(List<MyHistoryJson> list);

    void onPraiseHistoryResult(List<MyHistoryJson> list,int page);
}
