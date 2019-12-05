package com.xx.module.me.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.annotation.Presenter;
import com.xx.module.common.imageload.ImageLoader;
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
import com.xx.module.common.view.widget.ViewRevealAnimatorUtils;
import com.xx.module.me.R;
import com.xx.module.me.model.entity.MyHistoryJson;
import com.xx.module.me.presenter.MyCollectionPresenter;
import com.xx.module.me.view.contract.IMyCollectionView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author someone
 * @date 2019-06-13
 */
@Presenter(MyCollectionPresenter.class)
public class MyCollectionFragment extends SimpleListFragment<MyCollectionPresenter> implements IMyCollectionView, View.OnClickListener {

    /**
     * 0视频  1趣闻
     */
    private int mType = 0;
    private ItemAdapter mItemAdapter;
    /**
     * 底部删除控制
     */
    private LinearLayout bottomLayout;
    private int bottomMeasureHeight;
    private View bottomALlView;
    private TextView bottomAllTv;
    private View bottomDelView;
    private TextView bottomDelTv;
    private View bottomCancelView;

    @NotNull
    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        Fragment fragment = new MyCollectionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getLayoutID() {
        return R.layout.fragment_my_collection;
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
        CommonUISetUtil.closeDefaultAnimator(getRecyclerView());
        bottomLayout = getRootView().findViewById(R.id.collection_bottom_layout);
        bottomLayout.setVisibility(View.INVISIBLE);
        bottomLayout.post(new Runnable() {
            @Override
            public void run() {
                bottomMeasureHeight = bottomLayout.getMeasuredHeight();
                if (bottomMeasureHeight != 0) {
                    ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
                    lp.height = 0;
                    bottomLayout.setLayoutParams(lp);
                }
            }
        });
        bottomALlView = getRootView().findViewById(R.id.collection_bottom_all);
        bottomAllTv = getRootView().findViewById(R.id.collection_bottom_all_tv);
        bottomALlView.setOnClickListener(this);

        bottomDelView = getRootView().findViewById(R.id.collection_bottom_del);
        bottomDelView.setOnClickListener(this);
        bottomDelTv = getRootView().findViewById(R.id.collection_bottom_del_tv);

        bottomCancelView = getRootView().findViewById(R.id.collection_bottom_cancel);
        bottomCancelView.setOnClickListener(this);
    }

    @Override
    protected void loadDataFromCacheAndNet() {
        getRecyclerView().post(new Runnable() {
            @Override
            public void run() {
                String token = getLoginUser() != null ? getLoginUser().getToken() : "";
                getPresenter().loadCollectionHistoryCache(token, mType);
            }
        });
    }

    @Override
    protected void loadData(int page) {
        String token = getLoginUser() != null ? getLoginUser().getToken() : "";
        getPresenter().loadCollectionHistory(token, mType, page);
    }

    @Override
    public void onCollectionHistoryCacheResult(@Nullable List<MyHistoryJson> list) {
        if (list != null) {
            initRecyclerViewOrListView(listAdapterListener);
            mItemAdapter.setData(list);
        }
        refreshData();
    }

    @Override
    public void onCollectionHistoryResult(List<MyHistoryJson> list, int page) {
        if (page == getStartingPageNum()) {
            initRecyclerViewOrListView(listAdapterListener);
            mItemAdapter.clearData();
        }
        mItemAdapter.addDataList(list);
    }

    /**
     * 删除收藏返回
     *
     * @param content
     */
    @Override
    public void onDelCollectionResult(String content) {
        refreshData();
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

    private void setupDelText(int selectedCount) {
        if (selectedCount == 0) {
            bottomDelTv.setText("删除");
        } else {
            bottomDelTv.setText(String.format(Locale.getDefault(), "删除(%d)", selectedCount));
        }
    }

    private void setupAllText(boolean hasAll) {
        bottomAllTv.setText(hasAll ? "取消全选" : "全选");
    }

    @Override
    public void onClick(final View v) {
        if (bottomALlView == v) {
            //全部选择，全部取消
            boolean hasAll = mItemAdapter.selectedCount == mItemAdapter.getData().size();
            if (hasAll) {
                //执行全部取消
                mItemAdapter.clearAllSelected();
            } else {
                mItemAdapter.allSelected();
            }
            setupAllText(!hasAll);
            setupDelText(mItemAdapter.selectedCount);
        } else if (v == bottomDelView) {
            if (mItemAdapter.selectedCount == 0) {
                return;
            }            //执行删除
            getLoginUserAndLogin(new UserDataManager.SimpleOnGetUserDataListener() {
                @Override
                public void onSuccess(@NonNull UserSystem us) {
                    StringBuilder cids = new StringBuilder();
                    boolean first = true;
                    Set<Integer> set = mItemAdapter.selectedMap.keySet();
                    for (Integer key : set) {
                        Boolean value = mItemAdapter.selectedMap.get(key);
                        if (value != null && value) {
                            if (!first) {
                                cids.append("#");
                            }
                            MyHistoryJson json = mItemAdapter.getData().get(key);
                            cids.append(json.getId());
                            first = false;
                        }
                    }
                    getPresenter().delCollection(us.getToken(), cids.toString());
                }
            });
        } else if (v == bottomCancelView) {
            showBottomLayout(false);
            mItemAdapter.isEditMode = false;
            mItemAdapter.selectedCount = 0;
            mItemAdapter.selectedMap.clear();
            mItemAdapter.notifyItemRangeChanged(0, mItemAdapter.getData().size());
        }
    }


    /**
     * Adapter
     */
    class ItemAdapter extends BaseRVAdapter<MyHistoryJson> {
        private SmContextWrap smContextWrap;
        int selectedCount = 0;
        boolean isEditMode;
        Map<Integer, Boolean> selectedMap = new ArrayMap<>();

        public ItemAdapter() {
            setShowLoadingView(false);
            smContextWrap = SmContextWrap.obtain(MyCollectionFragment.this);
        }

        @Override
        protected void onBindItemViewHolder(final SparseArrayViewHolder holder, final int dataPosition, int viewType) {
            final MyHistoryJson json = getData().get(dataPosition);
            ImageView coverIv = holder.getView(R.id.zan_item_ic);
            String imageUrl = "";
            int readCount = 0;
            String title = "";
            if (json.getVideodata() != null) {
                imageUrl = json.getVideodata().getCoverurl();
                readCount = json.getVideodata().getClicks();
                title = json.getVideodata().getVideotitle();
            } else if (json.getNewsdata() != null) {
                imageUrl = json.getNewsdata().getImgurl();
                readCount = json.getNewsdata().getClicks();
                title = json.getNewsdata().getTitle();
            }
            //删除选中
            final RadioButton rb = holder.getView(R.id.collection_item_selected);
            rb.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            Boolean isSeleted = selectedMap.get(dataPosition);
            rb.setOnCheckedChangeListener(null);
            rb.setChecked(isSeleted != null && isSeleted);
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedMap.put(dataPosition, true);
                        selectedCount++;
                    } else {
                        selectedMap.put(dataPosition, false);
                        selectedCount--;
                    }
                    setupDelText(selectedCount);
                }
            });
            //视频时常
            TextView timeTv = holder.getView(R.id.zan_item_time);
            timeTv.setVisibility(json.getVideodata() != null ? View.VISIBLE : View.GONE);
            timeTv.setText(json.getVideodata() != null ? json.getVideodata().getVideolength() : "");

            ImageLoader.getInstance().load(smContextWrap, imageUrl, R.drawable.icon_default_vertical, coverIv);
            holder.setText(R.id.zan_item_title, title)
                    .setText(R.id.zan_item_read_count, String.format(Locale.getDefault(), "%d次阅读", readCount));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEditMode) {
                        rb.setChecked(!rb.isChecked());
                        return;
                    }
                    if (json.getVideodata() != null) {
                        //跳转视频详情
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
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    notifyItemRangeChanged(0, getData().size());
                    isEditMode = true;
                    showBottomLayout(true);
                    return true;
                }
            });
        }

        @Override
        public int getItemLayoutIds(int viewType) {
            return R.layout.fragment_collection_item;
        }

        public void clearAllSelected() {
            selectedMap.clear();
            selectedCount = 0;
            notifyItemRangeChanged(0, getData().size());
        }

        public void allSelected() {
            for (int i = 0; i < getData().size(); i++) {
                selectedMap.put(i, true);
            }
            selectedCount = getData().size();
            notifyItemRangeChanged(0, getData().size());
        }
    }


    private ValueAnimator bottomAnim;

    private void showBottomLayout(boolean show) {
        if (show) {
            if (bottomLayout.getVisibility() == View.VISIBLE) {
                return;
            }
            if (bottomAnim != null) {
                bottomAnim.cancel();
            }
            ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
            lp.height = 0;
            bottomLayout.setLayoutParams(lp);
            bottomAnim = ValueAnimator.ofInt(0, bottomMeasureHeight);
            bottomAnim.setDuration(300);
            bottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
                    lp.height = value;
                    bottomLayout.setLayoutParams(lp);
                    if (lp.height != 0) {
                        bottomLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            bottomAnim.start();
        } else {
            if (bottomLayout.getVisibility() != View.VISIBLE) {
                return;
            }
            if (bottomAnim != null) {
                bottomAnim.cancel();
            }
            ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
            lp.height = bottomMeasureHeight;
            bottomLayout.setLayoutParams(lp);
            bottomAnim = ValueAnimator.ofInt(bottomMeasureHeight, 0);
            bottomAnim.setDuration(300);
            bottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = bottomLayout.getLayoutParams();
                    lp.height = value;
                    bottomLayout.setLayoutParams(lp);
                    bottomLayout.setVisibility(View.VISIBLE);
                }
            });
            bottomAnim.addListener(new ViewRevealAnimatorUtils.SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    bottomLayout.setVisibility(View.GONE);
                }
            });
            bottomAnim.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bottomAnim != null) {
            bottomAnim.cancel();
        }
    }
}
