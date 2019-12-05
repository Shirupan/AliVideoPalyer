package com.xx.module.common.view.base;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.R;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.TakePhotoUtil;

/**
 * @author
 * @date 2017/9/13
 */

public class PhotosRvAdapter extends RecyclerView.Adapter<PhotosRvAdapter.ImageViewHolder> {

    private String[] urls;
    private boolean isPrivacy;  //是不是隐私帖
    private Activity activity;

    public PhotosRvAdapter(Activity activity, boolean isPrivacy) {
        this.isPrivacy = isPrivacy;
        this.activity = activity;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String[] getUrls() {
        return urls;
    }

    @Override
    public PhotosRvAdapter.ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ImageView imageView = new ImageView(activity);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setId(0);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder viewHolder, final int i) {
        final ImageView imageView = viewHolder.imageView;
        String url =urls[i];
        if (isPrivacy) {
            ImageLoader.getInstance().loadBlur(SmContextWrap.obtain(activity), url, -1, R.drawable.icon_default_round, viewHolder.imageView);
        } else {
            ImageLoader.getInstance().load(SmContextWrap.obtain(activity), url,
                    R.drawable.icon_default_round, (ImageView) viewHolder.itemView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //隐藏贴里，如果不是本人或者大师，就不能点击查看大图
                    if (activity instanceof AppCompatActivity) {
                        TakePhotoUtil.openImagesShower(activity, i, urls);
                    }
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        if (urls != null) {
            return urls.length;
        }
        return 0;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(0);
        }
    }
}
