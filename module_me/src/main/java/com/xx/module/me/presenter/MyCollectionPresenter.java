package com.xx.module.me.presenter;

import com.google.gson.reflect.TypeToken;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.callback.ResultListUICallback;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.presenter.BaseListPresenter;
import com.xx.module.me.MeModule;
import com.xx.module.me.model.MeCacheProvider;
import com.xx.module.me.model.entity.MyHistoryJson;
import com.xx.module.me.view.contract.IMyCollectionView;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-13
 */
public class MyCollectionPresenter extends BaseListPresenter<IMyCollectionView> {

    public void loadCollectionHistoryCache(String token, int type) {
        DataProviderManager.get(MeCacheProvider.class)
                .loadCollectionHistory(null, token, type)
                .compose(RetrofitManager.<List<MyHistoryJson>>rxTransformer(null,
                        new TypeToken<List<MyHistoryJson>>() {
                        }.getType()))
                .subscribe(new ResultUICallback<List<MyHistoryJson>>() {
                    @Override
                    public void onNext(List<MyHistoryJson> myHistoryJsons) {
                        super.onNext(myHistoryJsons);
                        if (getView() != null) {
                            getView().onCollectionHistoryCacheResult(myHistoryJsons);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (getView() != null) {
                            getView().onCollectionHistoryCacheResult(null);
                        }
                    }
                });
    }

    public void loadCollectionHistory(String token, int type, final int page) {
        ModuleManager.of(MeModule.class)
                .getModelClient()
                .loadCollectionHistory(token, type, page, new ResultListUICallback<List<MyHistoryJson>>(getView()) {
                    @Override
                    public void onNext(List<MyHistoryJson> myHistoryJsons) {
                        super.onNext(myHistoryJsons);
                        if (getView() != null) {
                            getView().onCollectionHistoryResult(myHistoryJsons, page);
                        }
                    }
                }.unShowDefaultMessage());
    }

    public void delCollection(String token, String cids) {
        ModuleManager.of(MeModule.class)
                .getModelClient()
                .delCollection(token, cids, new ResultUICallback<String>(getView(), true, false) {
                    @Override
                    public void onNext(String content) {
                        super.onNext(content);
                        if (getView() != null) {
                            getView().onDelCollectionResult(content);
                        }
                    }
                });
    }
}
