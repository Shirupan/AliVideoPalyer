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
import com.xx.module.me.view.contract.IMyPraiseView;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-13
 */
public class MyPraisePresenter extends BaseListPresenter<IMyPraiseView> {

    public void loadPraiseHistoryCache(String token, int type) {
        DataProviderManager.get(MeCacheProvider.class)
                .loadPraiseHistory(null, token, type)
                .compose(RetrofitManager.<List<MyHistoryJson>>rxTransformer(null,
                        new TypeToken<List<MyHistoryJson>>() {
                        }.getType()))
                .subscribe(new ResultUICallback<List<MyHistoryJson>>() {
                    @Override
                    public void onNext(List<MyHistoryJson> myHistoryJsons) {
                        super.onNext(myHistoryJsons);
                        if (getView() != null) {
                            getView().onPraiseHistoryCacheResult(myHistoryJsons);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        if (getView() != null) {
                            getView().onPraiseHistoryCacheResult(null);
                        }
                    }
                });
    }

    public void loadPraiseHistory(String token, int type, final int page) {
        ModuleManager.of(MeModule.class)
                .getModelClient()
                .loadPraiseHistory(token, type, page, new ResultListUICallback<List<MyHistoryJson>>(getView()) {
                    @Override
                    public void onNext(List<MyHistoryJson> myHistoryJsons) {
                        super.onNext(myHistoryJsons);
                        if (getView() != null) {
                            getView().onPraiseHistoryResult(myHistoryJsons,page);
                        }
                    }
                }.unShowDefaultMessage());
    }
}
