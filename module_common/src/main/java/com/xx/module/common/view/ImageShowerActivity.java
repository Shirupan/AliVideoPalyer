package com.xx.module.common.view;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.mrkj.lib.common.view.SmToast;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.R;
import com.xx.module.common.annotation.Path;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.ImageLoaderListener;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;
import com.xx.module.common.view.base.BaseFragment;
import com.xx.module.common.view.widget.PinchImageDragCloseLayout;
import com.xx.module.common.view.widget.PinchImageView;
import com.xx.module.common.view.widget.SmDownloadProgressView;
import com.xx.module.common.view.widget.ViewRevealAnimatorUtils;

/**
 * 大图浏览界面
 *
 * @author Administrator
 */
@Path(RouterUrl.ACTIVITY_IMAGE_PREVIEW)
public class ImageShowerActivity extends BaseActivity {
    String[] uris;
    private int index;
    TextView numText;
    private ImageFragmentPageAdapter fragmentPagerAdapter;
    private ViewPager viewPager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_shower;
    }


    @Override
    protected void initViewsAndEvents() {
        setStatusBar(true, false);
        setCutoutFullScreen();

        uris = getIntent().getStringArrayExtra("urls");
        if (uris == null) {
            SmToast.show(this, "无法打开大图");
            return;
        }
        index = getIntent().getIntExtra("index", -1);

        numText = findViewById(R.id.imagelength_text);
        if (uris.length == 1) {
            numText.setVisibility(View.GONE);
        } else {
            numText.setText((index + 1) + "/" + uris.length);
        }
        viewPager = findViewById(R.id.viewpager);
        fragmentPagerAdapter = new ImageFragmentPageAdapter(getSupportFragmentManager(), uris, index);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                index = position;
                numText.setText((index + 1) + "/" + uris.length);
            }
        });
        viewPager.setCurrentItem(index, false);
    }

    @Override
    public void onBackPressed() {
        Object o = fragmentPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
        boolean isPressed = false;
        if (o instanceof ImageShowerItemFragment) {
            isPressed = ((ImageShowerItemFragment) o).onBackPressed();
        }
        if (!isPressed) {
            super.onBackPressed();
        }
    }

    private class ImageFragmentPageAdapter extends FragmentPagerAdapter {
        private String[] urls;
        private int firstPosition;

        ImageFragmentPageAdapter(FragmentManager fm, String[] urls, int firstPosition) {
            super(fm);
            this.urls = urls;
            this.firstPosition = firstPosition;
        }

        @Override
        public Fragment getItem(int position) {
            ImageShowerItemFragment fragment = new ImageShowerItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", urls[position]);
            if (position == firstPosition) {
                bundle.putInt("firstPosition", firstPosition);
            }
            bundle.putInt("position", position);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            if (urls != null) {
                return urls.length;
            }
            return 0;
        }
    }

    public static class ImageShowerItemFragment extends BaseFragment {

        private String realUrl;
        private String hdUrl;
        private SmDownloadProgressView smDownloadProgressView;
        private PinchImageView imageView;
        private View alphaView;

        @Override
        public int getLayoutID() {
            return R.layout.fragment_image_show_item;
        }


        @Override
        protected void initViewsAndEvents(View rootView) {
            if (getArguments() != null) {
                realUrl = getArguments().getString("url");
            }

            alphaView = rootView.findViewById(R.id.alpha_view);
            imageView = rootView.findViewById(R.id.imageview);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveToFinish(imageView, alphaView, false);
                }
            });
            if (TextUtils.isEmpty(realUrl)) {
                return;
            }
            hdUrl = realUrl;
            smDownloadProgressView = rootView.findViewById(R.id.sm_progress);
            smDownloadProgressView.setProgress(0);
            if (hdUrl.endsWith(".gif")) {
                ImageLoader.getInstance().loadGif(SmContextWrap.obtain(this), hdUrl, 0,
                        new ImageLoaderListener<Drawable>(imageView) {
                            @Override
                            public void onSuccess(Drawable data) {
                                smDownloadProgressView.setVisibility(View.GONE);
                                imageView.setImageDrawable(data);
                                if (data instanceof Animatable) {
                                    ((Animatable) data).start();
                                }
                            }

                            @Override
                            public void onProgress(int progress) {
                                smDownloadProgressView.setProgress(progress);
                            }

                            @Override
                            public void onLoadFailed() {
                                smDownloadProgressView.setVisibility(View.GONE);
                            }
                        });
            } else {
                ImageLoader.getInstance().loadWithThumb(SmContextWrap.obtain(this), hdUrl, realUrl,
                        new ImageLoaderListener<Drawable>(imageView) {
                            @Override
                            public void onSuccess(Drawable data) {
                                smDownloadProgressView.setVisibility(View.GONE);
                                imageView.setImageDrawable(data);
                            }

                            @Override
                            public void onProgress(int progress) {
                                smDownloadProgressView.setProgress(progress);
                            }

                            @Override
                            public void onLoadFailed() {
                                smDownloadProgressView.setVisibility(View.GONE);
                            }
                        });
            }
            PinchImageDragCloseLayout closeViewLayout = rootView.findViewById(R.id.drag_imageview);
            closeViewLayout.setPinchImageView(imageView);
            closeViewLayout.setOnViewMoveListener(new PinchImageDragCloseLayout.OnViewMoveListener() {
                float maxMoveY;
                private PointF mPointF = new PointF();

                @Override
                public void onDown(View view, float eventX, float eventY) {
                    mPointF.set(eventX, eventY);
                    //最大的向下滑动距离
                    maxMoveY = view.getMeasuredHeight() - eventY;
                }

                @Override
                public void onMove(View view, float eventX, float eventY) {
                    float dy = eventY - mPointF.y;
                    //确定透明度缩放值
                    float alpha = Math.abs(dy / maxMoveY);
                    alphaView.setAlpha(1 - alpha);
                    imageView.setTranslationY(dy);
                }

                @Override
                public void onClose(View view, float eventX, float eventY) {
                    moveToFinish(imageView, alphaView, imageView.getTranslationY() < 0);
                }

                @Override
                public void onCancel() {
                    alphaView.setAlpha(1);
                }
            });
        }

        ValueAnimator animator;
        private DisplayMetrics displayMetrics = new DisplayMetrics();

        /**
         * 屏幕滑动之后关闭Activity
         *
         * @param imageView
         * @param view
         */
        private void moveToFinish(final PinchImageView imageView, final View view, final boolean up) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            if (view == null) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return;
            }
            if (getActivity() != null) {
                getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(300);
            animator.setInterpolator(new LinearInterpolator());

            final float oAlpha = view.getAlpha();
            final float oTranslationY = imageView.getTranslationY();
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    view.setAlpha(oAlpha - oAlpha * value);
                    float offsetY = ((float) displayMetrics.heightPixels - oTranslationY) * value;
                    offsetY = up ? -offsetY : offsetY;
                    float dy = oTranslationY + offsetY;
                    imageView.setTranslationY(dy);
                }
            });
            animator.addListener(new ViewRevealAnimatorUtils.SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
            animator.start();
        }

        public boolean onBackPressed() {
            moveToFinish(imageView, alphaView, false);
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
