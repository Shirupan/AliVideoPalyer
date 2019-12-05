package com.xx.module.common.view.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.lib.db.entity.ReturnJson;
import com.xx.lib.db.entity.SmShare;
import com.xx.module.common.R;
import com.xx.module.common.model.ThirdShareManager;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;

/**
 * @Function 该类功能：公共的第三方分享对话框
 * @Author
 * @Date 2017/3/16
 */

public class SocialShareDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final int KIND_QUES = 1; //问题详情页的分享
    public static final int KIND_INFO = 2; //资讯页的分享
    public static final int KIND_TEST = 3;  //自测的分享
    public static final int KIND_MAIN_BANNER = 4;  //主页广告的分享
    public static final int KIND_SETTING = 5;  //其他设置的分享
    public static final int KIND_PERSIONNAL = 6;  //个人运势的分享
    public static final int KIND_TAROT = 7;  //塔罗测试的分享
    public static final int KIND_NEWYEAR_BEFORE = 8;  //元旦活动,充值页面分享
    public static final int KIND_NEWYEAR_AFTER = 9;  //元旦活动,充值后分享
    public static final int KIND_PRAY = 10;  //祈祷
    public static final int KIND_MASTER_INFO = 11;  //大师详情（网页）


    private int[] shareTypes;  //可以分享的渠道下标
    private SnsPlatform[] platforms = {SHARE_MEDIA.WEIXIN.toSnsPlatform(),
            SHARE_MEDIA.WEIXIN_CIRCLE.toSnsPlatform(),
            SHARE_MEDIA.QQ.toSnsPlatform(),
            SHARE_MEDIA.QZONE.toSnsPlatform(),
            SHARE_MEDIA.SINA.toSnsPlatform()};

    //   private SmShare smShare = new SmShare();
    private Activity mContext;
    private View rootView;
    private String contentUrl;

    private PlatformsAdapter mAdapter;
    private ThirdShareManager.OnShareResultListener onShareResultListener;


    public SocialShareDialog(@NonNull Activity context) {
        super(context, R.style.custom_dialog_style);
        mContext = context;
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.activity_information_share, null, false);
        rootView.findViewById(R.id.tv_cancel).setOnClickListener(this);
        GridView gvPlatforms = rootView.findViewById(R.id.gv_platforms);
        mAdapter = new PlatformsAdapter(mContext);
        gvPlatforms.setAdapter(mAdapter);
        mAdapter.setSmShare(null);
        gvPlatforms.setOnItemClickListener(this);

    }

    @Override
    public void show() {
        if (rootView == null) {
            initView();
        } else if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        Window window = getWindow();
        if (window != null) {
            getWindow().setContentView(rootView);
            getWindow().setWindowAnimations(R.style.Animation_Popwindow_anim_bottom);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams wl = getWindow().getAttributes();
            wl.width = WindowManager.LayoutParams.MATCH_PARENT;
            wl.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(wl);
        }

        super.show();
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    private void share(final SHARE_MEDIA plaform) {
        ThirdShareManager.share(mContext, mAdapter.getSmShare(), contentUrl, plaform,
                new ThirdShareManager.OnShareResultListener() {
                    @Override
                    public void onFailed(Throwable e) {
                        dismiss();
                        if (onShareResultListener != null) {
                            onShareResultListener.onFailed(e);
                        }
                    }

                    @Override
                    public void onSuccess(SHARE_MEDIA shareMedia, ReturnJson json) {
                        if (onShareResultListener != null) {
                            onShareResultListener.onSuccess(shareMedia, json);
                        }
                    }
                });
    }

    public SocialShareDialog setSmShare(SmShare smShare) {
        mAdapter.setSmShare(smShare);
        mAdapter.notifyDataSetChanged();
        return this;
    }

    public SocialShareDialog setOnShareResultListener(ThirdShareManager.OnShareResultListener onShareResultListener) {
        this.onShareResultListener = onShareResultListener;
        return this;
    }


    public SmShare getSmShare() {
        return mAdapter.getSmShare();
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_cancel == v.getId()) {
            this.dismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ThirdShareManager.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        this.dismiss();
        final SHARE_MEDIA platform = platforms[shareTypes[position]].mPlatform;
        if (onShareItemClickListener != null) {
            onShareItemClickListener.onClick(mAdapter.getSmShare(), platform);
        } else {
            share(platform);
        }
    }

    /**
     * 如果设置了item监听，将不走默认分享。
     *
     * @param onShareItemClickListener
     */
    public SocialShareDialog setOnShareItemClickListener(OnShareItemClickListener onShareItemClickListener) {
        this.onShareItemClickListener = onShareItemClickListener;
        return this;
    }

    private OnShareItemClickListener onShareItemClickListener;


    public interface OnShareItemClickListener {
        void onClick(@Nullable SmShare smShare, SHARE_MEDIA platform);
    }


    private class PlatformsAdapter extends BaseAdapter {

        private int[] platformNames;
        private int[] platformIcons;
        private SmShare mShare;

        private LayoutInflater inflater;

        PlatformsAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            TypedArray nameTa = context.getResources().obtainTypedArray(R.array.information_share_platform_names);
            TypedArray iconTa = context.getResources().obtainTypedArray(R.array.information_share_platform_icons);
            platformNames = new int[nameTa.length()];
            platformIcons = new int[iconTa.length()];
            for (int i = 0; i < nameTa.length(); i++) {
                platformNames[i] = nameTa.getResourceId(i, 0);
                platformIcons[i] = iconTa.getResourceId(i, 0);
            }
            nameTa.recycle();
            iconTa.recycle();
        }

        public void setSmShare(SmShare smShare) {
            mShare = smShare;
            if (mShare == null) {
                shareTypes = new int[platforms.length];
                for (int i = 0; i < shareTypes.length; i++) {
                    shareTypes[i] = i;
                }
            } else {
                String shares = mShare.getSharetype();
                if (!TextUtils.isEmpty(shares)) {
                    if (shares.equals("-1")) {
                        shareTypes = new int[platformIcons.length];
                        for (int i = 0; i < shareTypes.length; i++) {
                            shareTypes[i] = i;
                        }
                    } else {
                        String[] array = shares.split("#");
                        shareTypes = new int[array.length];
                        for (int i = 0; i < array.length; i++) {
                            shareTypes[i] = Integer.valueOf(array[i]) - 1;
                        }
                    }
                } else {
                    shareTypes = new int[platforms.length];
                    for (int i = 0; i < shareTypes.length; i++) {
                        shareTypes[i] = i;
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
        }

        public SmShare getSmShare() {
            return mShare;
        }

        @Override
        public int getCount() {
            if (shareTypes != null && platformNames != null) {
                return shareTypes.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PlatformsAdapter.ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_information_share_platform, parent, false);
                holder = new PlatformsAdapter.ViewHolder();
                holder.iv_img = convertView.findViewById(R.id.iv_img);
                holder.tv_name = convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (PlatformsAdapter.ViewHolder) convertView.getTag();
            }
            int realPosition = shareTypes[position];
            holder.iv_img.setImageResource(platformIcons[realPosition]);
            holder.tv_name.setText(platformNames[realPosition]);
            convertView.setVisibility(View.VISIBLE);
            return convertView;
        }

        private class ViewHolder {
            private ImageView iv_img;
            private TextView tv_name;
        }
    }


}
