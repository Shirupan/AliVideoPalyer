package com.xx.module.common.view.contract;


import com.xx.multiadapter.MultilItemAdapter;
import com.xx.module.common.view.loading.IBaseAdapter;

import java.util.List;

/**
 * RecyclerView,vlayout,ListView统一初始化接口
 * RecyclerView的适配器检索顺序{@link OnCreateListAdapterListener#onCreateRecyclerViewAdapter()}--->{@link OnCreateListAdapterListener#onBindVLayoutAdapter(List)} ()}<p></>
 * 即前一个方法返回对象不为空，即认为得到Adapter。则不会执行下一个方法
 *
 * @author
 */

public interface OnCreateListAdapterListener {

    IBaseAdapter onCreateListViewAdapter();

    IBaseAdapter onCreateRecyclerViewAdapter();

    List<MultilItemAdapter> onCreateSubAdapters();
}
