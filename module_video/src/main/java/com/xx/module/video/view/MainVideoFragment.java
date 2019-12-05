package com.xx.module.video.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.reflect.TypeToken;
import com.mrkj.lib.common.util.ScreenUtils;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.model.cache.DataProviderManager;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseFragment;
import com.xx.module.common.view.dialog.SmDefaultDialog;
import com.xx.module.video.R;
import com.xx.module.video.VideoModuleClient;
import com.xx.module.video.model.VideoCacheProvider;
import com.xx.module.video.model.entity.MainVideoTab;
import com.xx.module.video.view.constract.IMainActivityView;
import com.xx.module.video.view.constract.ITabChangeCallback;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.List;

/**
 * @author someone
 * @date 2019-05-30
 */
public class MainVideoFragment extends BaseFragment {

    private MagicIndicator magicIndicator;
    private ImageView tabAddLayout;
    private ViewPager mViewPager;

    @Override
    public int getLayoutID() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initViewsAndEvents(View rootView) {
        setCutOutAndStatusMaxHeightToView(rootView.findViewById(R.id.main_status_bar));
        setCutOutAndStatusMaxHeightToView(rootView.findViewById(R.id.main_status_bar_1));
        AppBarLayout appBarLayout = rootView.findViewById(R.id.main_app_bar_layout);
        if (getActivity() instanceof IMainActivityView) {
            ((IMainActivityView) getActivity()).setMainViewAppbarLayout(appBarLayout);
        }
        magicIndicator = rootView.findViewById(R.id.main_tab_layout);
        tabAddLayout = rootView.findViewById(R.id.main_tab_layout_add);
        mViewPager = rootView.findViewById(R.id.main_view_page);

        tabAddLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转标签编辑页面
                Intent intent = ActivityRouter.get().getIntent(v.getContext(), RouterUrl.ACTIVITY_VIDEO_TAB_MANAGER);
                startActivityForResult(intent, 100);
            }
        });

        loadTabs();
    }

    private void loadTabs() {
        String token = "";
        if (getLoginUser() != null) {
            token = getLoginUser().getToken();
        }
        //本地获取已经在闪屏页缓存的首页tab内容
        SimpleSubscriber<String> callback = new SimpleSubscriber<String>() {
            @Override
            public void onNext(String s) {
                super.onNext(s);
                ReturnBeanJson<List<MainVideoTab>> json = GsonSingleton.getInstance().fromJson(s, new TypeToken<ReturnBeanJson<List<MainVideoTab>>>() {
                }.getType());
                if (json == null) {
                    onError(new ReturnJsonCodeException("数据错误"));
                } else {
                    initViewPager(json.getContent());
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                getMainTabDate();
            }
        };
        DataProviderManager.get(VideoCacheProvider.class)
                .getMainVideoTabs(null, token)
                .compose(RetrofitManager.<String>rxTransformer())
                .subscribe(callback);
    }

    /**
     * 缓存首页tab集合
     */
    private void getMainTabDate() {
        String token = getLoginUser() == null ? "" : getLoginUser().getToken();
        ModuleManager.of(VideoModuleClient.class)
                .getModelClient()
                .getMainVideoTabs(token, new SimpleSubscriber<List<MainVideoTab>>() {
                    @Override
                    public void onNext(List<MainVideoTab> mainVideoTabs) {
                        super.onNext(mainVideoTabs);
                        initViewPager(mainVideoTabs);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        new SmDefaultDialog.Builder(getContext())
                                .setMessage(ExceptionUtl.catchTheError(e))
                                .setPositiveButton("重试", new SmDefaultDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialog, int resId) {
                                        dialog.dismiss();
                                        getMainTabDate();
                                    }
                                }).setNegativeButton("退出", new SmDefaultDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialog, int resId) {
                                dialog.dismiss();
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }
                        }).cancelOutside(false)
                                .show();
                    }
                });
    }

    private void initViewPager(final List<MainVideoTab> list) {
        if (list == null) {
            return;
        }
        CommonNavigator navigator = new CommonNavigator(getContext());
        navigator.setAdjustMode(false);
        navigator.setAdapter(new CommonNavigatorAdapter() {
            int normalColor;
            int selectedColor;

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                if (normalColor == 0) {
                    TypedValue typedValue = new TypedValue();
                    getActivity().getTheme().resolveAttribute(R.attr.smTipColor, typedValue, true);
                    normalColor = ContextCompat.getColor(context, typedValue.resourceId);
                }
                if (selectedColor == 0) {
                    selectedColor = ContextCompat.getColor(context, R.color.text_red);
                }
                SimplePagerTitleView titleView = new SimplePagerTitleView(context);
                titleView.setTextSize(16f);
                titleView.setPadding(titleView.getPaddingLeft(), titleView.getPaddingTop(), titleView.getPaddingRight(), ScreenUtils.dp2px(context, 6f));
                titleView.setNormalColor(normalColor);
                titleView.setSelectedColor(selectedColor);
                titleView.setText(list.get(index).getTypename());
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setRoundRadius(ScreenUtils.dp2px(context, 10f));
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setColors(selectedColor);
                return indicator;
            }
        });
        magicIndicator.setNavigator(navigator);

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return MainVideoItemFragment.Companion.getInstance(list.get(i).getTid());
            }

            @Override
            public int getCount() {
                return list.size();
            }
        });
        ViewPagerHelper.bind(magicIndicator, mViewPager);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //刷新tab和列表
            if (getActivity() instanceof ITabChangeCallback) {
                ((ITabChangeCallback) getActivity()).onchanged();
            }
        }
    }
}
