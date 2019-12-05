package com.xx.module.me.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.ColorUtils;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.annotation.Presenter;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.model.entity.SmError;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.router.RouterParams;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.SimpleListFragment;
import com.xx.module.common.view.contract.OnCreateListAdapterListener;
import com.xx.module.common.view.loading.BaseRVAdapter;
import com.xx.module.common.view.loading.IBaseAdapter;
import com.xx.module.common.view.loading.SparseArrayViewHolder;
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener;
import com.xx.module.common.view.widget.CommonUISetUtil;
import com.xx.module.common.view.widget.SmAnimationUtil;
import com.xx.module.me.R;
import com.xx.module.me.model.entity.MyHistoryJson;
import com.xx.module.me.presenter.MyPraisePresenter;
import com.xx.module.me.view.contract.IMyPraiseView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author someone
 * @date 2019-06-13
 */
@Presenter(MyPraisePresenter.class)
public class MyPraiseFragment extends SimpleListFragment<MyPraisePresenter> implements IMyPraiseView {

    /**
     * 0视频  1趣闻
     */
    private int mType = 0;
    private ItemAdapter mItemAdapter;

    @NotNull
    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        Fragment fragment = new MyPraiseFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void beforeSetContentView() {
        setShowLoadingView(true);
    }

    @Override
    protected void initBeforeLoadData() {
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mType = bundle.getInt("position", 0);
        }
    }

    @Override
    protected void loadDataFromCacheAndNet() {
        CommonUISetUtil.closeDefaultAnimator(getRecyclerView());
        getRecyclerView().post(new Runnable() {
            @Override
            public void run() {
                String token = getLoginUser() != null ? getLoginUser().getToken() : "";
                getPresenter().loadPraiseHistoryCache(token, mType);
            }
        });
    }

    @Override
    protected void loadData(int page) {
        String token = getLoginUser() != null ? getLoginUser().getToken() : "";
        getPresenter().loadPraiseHistory(token, mType, page);
    }

    @Override
    public void onPraiseHistoryCacheResult(@Nullable List<MyHistoryJson> list) {
        if (list != null) {
            initRecyclerViewOrListView(listAdapterListener);
            mItemAdapter.setData(list);
        }
        refreshData();
    }

    @Override
    public void onPraiseHistoryResult(List<MyHistoryJson> list, int page) {
        if (page == getStartingPageNum()) {
            initRecyclerViewOrListView(listAdapterListener);
            mItemAdapter.clearData();
        }
        mItemAdapter.addDataList(list);
    }


    private OnCreateListAdapterListener listAdapterListener = new SimpleOnCreateListAdapterListener() {
        @Override
        public IBaseAdapter onCreateRecyclerViewAdapter() {
            if (mItemAdapter == null) {
                mItemAdapter = new ItemAdapter();
            }
            mItemAdapter.clearData();
            return mItemAdapter;
        }
    };

    /**
     * Adapter
     */
    class ItemAdapter extends BaseRVAdapter<MyHistoryJson> {
        private SmContextWrap smContextWrap;

        private Drawable unPraiseDrawable;
        private Drawable praiseDrawable;

        private Map<Integer, Boolean> praiseMap = new ArrayMap<>();

        public ItemAdapter() {
            setShowLoadingView(false);
            smContextWrap = SmContextWrap.obtain(MyPraiseFragment.this);
            int resId = AppUtil.getThemeColor(getActivity().getTheme(), R.attr.smTipColor, R.color.text_99);
            unPraiseDrawable = ColorUtils.setTintColorRes(getContext(), R.drawable.icon_main_zan, resId);
            unPraiseDrawable.setBounds(0, 0, unPraiseDrawable.getIntrinsicWidth(), unPraiseDrawable.getIntrinsicHeight());
            praiseDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_main_zan_0);
            praiseDrawable.setBounds(0, 0, praiseDrawable.getIntrinsicWidth(), praiseDrawable.getIntrinsicHeight());
        }

        @Override
        protected void onBindItemViewHolder(SparseArrayViewHolder holder, final int dataPosition, int viewType) {
            final MyHistoryJson json = getData().get(dataPosition);
            ImageView coverIv = holder.getView(R.id.zan_item_ic);
            String imageUrl = "";
            int readCount = 0;
            boolean isPraise = false;
            String title = "";
            int praiseCount = 0;
            if (json.getVideodata() != null) {
                imageUrl = json.getVideodata().getCoverurl();
                readCount = json.getVideodata().getClicks();
                title = json.getVideodata().getVideotitle();
                isPraise = json.getVideodata().getIspraise() == 1;
                praiseCount = json.getVideodata().getPraise();
            } else if (json.getNewsdata() != null) {
                imageUrl = json.getNewsdata().getImgurl();
                readCount = json.getNewsdata().getClicks();
                title = json.getNewsdata().getTitle();
                praiseCount = json.getNewsdata().getPraise();
                isPraise = json.getNewsdata().getIspraise() == 1;
            }
            ImageLoader.getInstance().load(smContextWrap, imageUrl, R.drawable.icon_default_vertical, coverIv);
            holder.setText(R.id.zan_item_title, title)
                    .setText(R.id.zan_item_read_count, String.format(Locale.getDefault(), "%d次阅读", readCount));

            TextView zanTv = holder.getView(R.id.zan_item_zan_count);
            zanTv.setText("" + praiseCount);
            if (isPraise) {
                zanTv.setCompoundDrawables(praiseDrawable, null, null, null);
            } else {
                zanTv.setCompoundDrawables(unPraiseDrawable, null, null, null);
            }
            final boolean finalIsPraise = isPraise;
            zanTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postPraise(json, finalIsPraise, dataPosition);
                }
            });
            Boolean needAnim = praiseMap.get(dataPosition);
            if (needAnim != null && needAnim) {
                SmAnimationUtil.scale(zanTv, 0.8f, true);
            }
            praiseMap.put(dataPosition, false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (json.getVideodata() != null) {
                        //跳转视频详情
                        //前往视频详情
                        String jsonStr = GsonSingleton.getInstance().toJson(json.getVideodata());
                        Map<String, String> map = new ArrayMap<>();
                        ((ArrayMap<String, String>) map).put(RouterParams.VideoView.DATA, jsonStr);
                        ActivityRouter.get().startActivity(getContext(), RouterUrl.ACTIVITY_VIDEO_DETAIL, map, false, 0);
                    } else if (json.getNewsdata() != null) {
                        //跳转资讯详情
                        Map<String, String> map = new ArrayMap<>();
                        ((ArrayMap<String, String>) map).put(RouterParams.NewsView.SID, "" + json.getNewsdata().getSid());
                        ActivityRouter.get().startActivity(getContext(), RouterUrl.ACTIVITY_NEWS_DETAIL, map, false, 0);

                    }
                }
            });
        }

        @Override
        public int getItemLayoutIds(int viewType) {
            return R.layout.fragment_zan_item;
        }


        private void postPraise(final MyHistoryJson json, boolean isPraise, final int dataPosition) {
            int vid = json.getVideodata() != null ? json.getVideodata().getVid() : json.getNewsdata().getSid();
            final boolean isVideo = json.getVideodata() != null;
            if (isPraise) {
                UserDataManager.getInstance().delPraise(MyPraiseFragment.this, vid, isVideo ? 0 : 1,
                        new UserDataManager.PraiseZanCallback() {
                            @Override
                            public void onSuccess() {
                                if (isVideo) {
                                    json.getVideodata().setPraise(json.getVideodata().getPraise() - 1);
                                    json.getVideodata().setIspraise(0);
                                } else if (json.getNewsdata() != null) {
                                    json.getNewsdata().setPraise(json.getNewsdata().getPraise() - 1);
                                    json.getNewsdata().setIspraise(0);
                                }
                                praiseMap.put(dataPosition, true);
                                notifyItemChanged(dataPosition);
                            }

                            @Override
                            public void onError(SmError e) {

                            }
                        });
            } else {
                UserDataManager.getInstance().addPraise(MyPraiseFragment.this, vid, isVideo ? 0 : 1,
                        new UserDataManager.PraiseZanCallback() {
                            @Override
                            public void onSuccess() {
                                if (isVideo) {
                                    json.getVideodata().setPraise(json.getVideodata().getPraise() + 1);
                                    json.getVideodata().setIspraise(1);
                                } else if (json.getNewsdata() != null) {
                                    json.getNewsdata().setPraise(json.getNewsdata().getPraise() + 1);
                                    json.getNewsdata().setIspraise(1);
                                }
                                praiseMap.put(dataPosition, true);
                                notifyItemChanged(dataPosition);
                            }

                            @Override
                            public void onError(SmError e) {

                            }
                        });
            }

        }
    }

}
