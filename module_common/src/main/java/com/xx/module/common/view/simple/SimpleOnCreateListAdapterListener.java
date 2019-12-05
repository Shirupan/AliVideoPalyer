package com.xx.module.common.view.simple;

import com.xx.multiadapter.MultilItemAdapter;
import com.xx.module.common.view.contract.OnCreateListAdapterListener;
import com.xx.module.common.view.loading.IBaseAdapter;

import java.util.List;

/**
 * @author someone
 * @date 2019-05-30
 */
public abstract class SimpleOnCreateListAdapterListener implements OnCreateListAdapterListener {
    @Override
    public IBaseAdapter onCreateListViewAdapter() {
        return null;
    }

    @Override
    public IBaseAdapter onCreateRecyclerViewAdapter() {
        return null;
    }

    @Override
    public List<MultilItemAdapter> onCreateSubAdapters() {
        return null;
    }
}
