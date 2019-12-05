package com.xx.module.me.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrkj.lib.common.util.ActivityManagerUtil;
import com.mrkj.lib.common.view.SmToast;
import com.xx.lib.db.entity.UserSystem;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.BaseConfig;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.annotation.Path;
import com.xx.module.common.annotation.Presenter;
import com.xx.module.common.client.ModuleManager;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.imageload.photo.CropOptions;
import com.xx.module.common.model.callback.ResultUICallback;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;
import com.xx.module.common.view.dialog.SmDefaultDialog;
import com.xx.module.me.MeModule;
import com.xx.module.me.R;
import com.xx.module.me.presenter.MeInfoEditPresenter;
import com.xx.module.me.view.contract.IMyInfoView;

import java.util.List;

/**
 * @author someone
 * @date 2019-06-03
 */
@Path(RouterUrl.ACTIVITY_ME_INFO_EDIT)
@Presenter(MeInfoEditPresenter.class)
public class MeInfoEditActivity extends BaseActivity<MeInfoEditPresenter> implements IMyInfoView, View.OnClickListener {


    private static final int REQUEST_NICK_NAME = 1011;
    private Handler mHandler = new Handler();
    private ImageView savorIv;
    private TextView nickTv;
    private CropOptions cropOptions;

    private boolean hasEdit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_me_info_edit;
    }

    @Override
    protected void beforeSetContentView() {
        setNeedTakePhoto(true);
    }

    @Override
    protected void initViewsAndEvents() {
        setToolBarTitle("个人资料");

        savorIv = findViewById(R.id.info_edit_savor);
        nickTv = findViewById(R.id.info_edit_nick);

        if (getLoginUser() != null) {
            UserSystem us = getLoginUser();
            ImageLoader.getInstance().loadCircle(SmContextWrap.obtain(this), us.getPhotourl(), savorIv, R.drawable.icon_head_circle_default);
            nickTv.setText(us.getNickname());
            nickTv.setOnClickListener(this);
            TextView idTv = findViewById(R.id.info_edit_id);
            idTv.setText("" + us.getUid());
            TextView phoneTv = findViewById(R.id.info_edit_phone);
            phoneTv.setText(us.getPhone());
            findViewById(R.id.info_edit_exit).setOnClickListener(this);
            findViewById(R.id.info_edit_savor_layout).setOnClickListener(this);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityRouter.LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            hasEdit = true;
            ActivityManagerUtil.getScreenManager().popAllActivityExceptOne(MeInfoEditActivity.class);
            //退出登录后重新登录，进入主页面
            ActivityRouter.get().startActivity(this, RouterUrl.ACTIVITY_MAIN);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        } else if (requestCode == REQUEST_NICK_NAME && resultCode == Activity.RESULT_OK) {
            nickTv.setText(getLoginUser().getNickname());
            hasEdit = true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.info_edit_exit) {
            new SmDefaultDialog.Builder(MeInfoEditActivity.this)
                    .setTitle("退出登录")
                    .setMessage("您确定要退出当前帐号吗？")
                    .setPositiveButton("确定", new SmDefaultDialog.OnClickListener() {

                        @Override
                        public void onClick(Dialog dialog, int resId) {
                            dialog.dismiss();
                            UserDataManager.getInstance().logout();
                            ActivityManagerUtil.getScreenManager().popAllActivityExceptOne(MeInfoEditActivity.class);
                            ActivityRouter.get().goToLoginActivity(MeInfoEditActivity.this);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);

                        }
                    }).show();
        } else if (v == nickTv) {
            ActivityRouter.get().startActivity(this, RouterUrl.ACTIVITY_ME_EDIT_NICK_NAME, REQUEST_NICK_NAME);
        } else if (v.getId() == R.id.info_edit_savor_layout) {
            new AlertDialog.Builder(this)
                    .setTitle("头像")
                    .setItems(new String[]{"查看大图", "拍照", "选择图片", "取消"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (cropOptions == null) {
                                        cropOptions = new CropOptions.Builder()
                                                .setWithOwnCrop(true)
                                                .setAspectX(1)
                                                .setAspectY(1)
                                                .setOutputX(BaseConfig.DEFAULT_SAVOR_SIZE)
                                                .setOutputY(BaseConfig.DEFAULT_SAVOR_SIZE)
                                                .create();
                                    }
                                    if (which == 0) {
                                        TakePhotoUtil.openImagesShower(MeInfoEditActivity.this, new String[]{mUserSystem.getPhotourl()}, 0);
                                    } else if (which == 1) {
                                        TakePhotoUtil.takePhotoAndCrop(MeInfoEditActivity.this, getTakePhotoHandler().getTakePhoto(), cropOptions);
                                    } else if (which == 2) {
                                        TakePhotoUtil.pickImageAndCrop(MeInfoEditActivity.this, getTakePhotoHandler().getTakePhoto(), cropOptions);
                                    }
                                }
                            })
                    .setCancelable(false)
                    .show();
        }
    }


    @Override
    public void onGetPhoto(List<String> data) {
        String url = data.get(0);
        //修改头像
        ModuleManager.of(MeModule.class)
                .getModelClient().postUserSavor(mUserSystem.getToken(), url,
                new ResultUICallback<UserSystem>(this, true, false) {
                    @Override
                    public void onNext(UserSystem userSystem) {
                        super.onNext(userSystem);
                        UserDataManager.getInstance().setUserSystem(userSystem);
                        SmToast.show(MeInfoEditActivity.this, "修改成功");
                        ImageLoader.getInstance().loadCircle(SmContextWrap.obtain(MeInfoEditActivity.this),
                                userSystem.getPhotourl(), savorIv, R.drawable.icon_head_circle_default);

                    }
                });

    }

    @Override
    public void finish() {
        if (hasEdit) {
            setResult(Activity.RESULT_OK);
        }
        super.finish();
    }
}
