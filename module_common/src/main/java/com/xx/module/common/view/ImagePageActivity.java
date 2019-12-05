package com.xx.module.common.view;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.R;
import com.xx.module.common.annotation.Path;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ChengLu
 * Date: 13-8-13
 * Time: 上午9:47
 * To change this template use File | Settings | File Templates.
 *
 * @author Administrator
 */

@Path(RouterUrl.ACTIVITY_IMAGE_PAGE)
public class ImagePageActivity extends BaseActivity {
    private LinearLayout mNumLayout;
    private ImagePagerAdapter adapter;
    private Button mPreSelectedBt;

    private int selectPosition;
    private ArrayList<String> images;
    private int pagerPosition;
    //首次图片位置


    @Override
    public int getLayoutId() {
        return R.layout.ac_image_pager;
    }

    @Override
    protected void initViewsAndEvents() {
        setStatusBar(true, false);
        findViewById(R.id.image_page_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStateBroadcastAndFinish();
            }
        });
        findViewById(R.id.image_page_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (images.get(selectPosition) == null) {
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        images.remove(selectPosition);
                        if (images.size() == 0) {
                            sendStateBroadcastAndFinish();
                            return;
                        } else {
                            mNumLayout.removeViewAt(selectPosition);
                            if (selectPosition != 0) {
                                mPreSelectedBt = (Button) mNumLayout.getChildAt(selectPosition);
                                if (mPreSelectedBt != null) {
                                    mPreSelectedBt.setBackgroundResource(R.drawable.icon_dot_press);
                                }

                            } else {
                                mPreSelectedBt = (Button) mNumLayout.getChildAt(0);
                                if (mPreSelectedBt != null) {
                                    mPreSelectedBt.setBackgroundResource(R.drawable.icon_dot_press);
                                }
                            }
                        }
                        adapter.setmViewList(images);
                        adapter.notifyDataSetChanged();
                    }
                }, 100);
            }
        });
        ViewPager pager = findViewById(R.id.pager);
        mNumLayout = findViewById(R.id.count_linear);
        pagerPosition = getIntent().getIntExtra(TakePhotoUtil.Extra.IMAGE_POSITION, 0);
        if (images == null || images.isEmpty()) {
            images = getIntent().getStringArrayListExtra(TakePhotoUtil.Extra.IMAGES);
        }
        if (images == null) {
            finish();
            return;
        }
        adapter = new ImagePagerAdapter(this, images);
        pager.setAdapter(adapter);
        pager.setCurrentItem(pagerPosition);
        addTag();
        Button currentBt = (Button) mNumLayout.getChildAt(pagerPosition);
        if (currentBt != null) {
            currentBt.setBackgroundResource(R.drawable.icon_dot_press);
            mPreSelectedBt = currentBt;
            if (pagerPosition != 0) {
                selectPosition = pagerPosition;
            }
        }


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                selectPosition = position;
                if (mPreSelectedBt != null) {
                    mPreSelectedBt.setBackgroundResource(R.drawable.icon_dot_normal);
                }
                int count = mNumLayout.getChildCount();
                if (position <= count - 1) {
                    Button currentBt = (Button) mNumLayout.getChildAt(position);
                    currentBt.setBackgroundResource(R.drawable.icon_dot_press);
                    mPreSelectedBt = currentBt;
                }

                //Log.i("INFO", "current item:"+position);
            }


            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }


            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });


    }


    android.os.Handler handler = new android.os.Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sendStateBroadcastAndFinish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void addTag() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_dot_normal);
        for (int i = 0; i < images.size(); i++) {
            Button bt = new Button(this);
            bt.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
            bt.setBackgroundResource(R.drawable.icon_dot_normal);
            mNumLayout.addView(bt);
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private ArrayList<String> mViewList;
        private View[] imageViews;
        private LayoutInflater inflater;

        ImagePagerAdapter(Context context, ArrayList<String> mViewList) {
            this.mViewList = mViewList;
            if (mViewList != null) {
                imageViews = new View[mViewList.size()];
            } else {
                imageViews = new View[0];
            }
            inflater = getLayoutInflater();
        }


        public void setmViewList(ArrayList<String> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


        public void finishUpdate(View container) {
        }


        @Override
        public int getCount() {
            if (mViewList == null) {
                return 0;
            }
            return mViewList.size();
        }


        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            ImageView imageView = imageLayout.findViewById(R.id.image);
            imageViews[position] = imageView;
            if (position == pagerPosition) {
                ViewCompat.setTransitionName(imageViews[position], TakePhotoUtil.VIEW_NAME_HEADER_IMAGE);
            }

            String url = mViewList.get(position);
            if (url != null) {
                Uri uri = null;
                if (!url.startsWith("http://")) {
                    File file = new File(url);
                    uri = Uri.fromFile(file);
                }
                if (uri != null) {
                    //本地图片
                    ImageLoader.getInstance().loadUri(SmContextWrap.obtain(ImagePageActivity.this), uri, imageView, 0);
                } else {  //网络图片
                    ImageLoader.getInstance().load(SmContextWrap.obtain(ImagePageActivity.this), url, 0, imageView);
                }
            }
            view.addView(imageLayout, 0);
            return imageLayout;
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }


        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }


        @Override
        public Parcelable saveState() {
            return null;
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        public void startUpdate(View container) {
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra("index", selectPosition);
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
    }

    private void sendStateBroadcastAndFinish() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", images);
        Intent intent1 = new Intent();
        intent1.putExtra("bundle2", bundle);
        setResult(TakePhotoUtil.ACTIVITY_IMAGEPAGE_RESULT, intent1);

        finish();
        overridePendingTransition(R.anim.photo_enter_anim, R.anim.photo_exit_anim);
    }
}