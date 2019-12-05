package com.xx.module.common.view.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.imageload.photo.ITakePhoto;
import com.xx.module.common.imageload.photo.PhotoResult;
import com.xx.module.common.imageload.photo.TakePhotoImpl;

import java.util.List;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class TakePhotoHandler implements ITakePhoto.TakeResultListener {

    private Activity activity;
    private Fragment fragment;


    public TakePhotoHandler(Activity activity) {
        this.activity = activity;
    }

    public TakePhotoHandler(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void takeSuccess(final PhotoResult result) {
        new RxAsyncHandler<List<String>>(this) {
            @Override
            public List<String> doSomethingBackground() {
                return TakePhotoUtil.dealWithTResult(result);
            }

            @Override
            public void onNext(List<String> data) {
                if (fragment instanceof ITakePhotoView) {
                    ((ITakePhotoView) fragment).onGetPhoto(data) ;
                } else if (activity instanceof ITakePhotoView) {
                    ((ITakePhotoView) activity).onGetPhoto(data);
                }
            }
        }.execute();
    }

    @Override
    public void takeFail(PhotoResult result, String msg) {
        SmLogger.i("takeFail:" + msg);
    }

    @Override
    public void takeCancel() {
        SmLogger.i("取消了");
    }


    private ITakePhoto takePhoto;

    public void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        if (resultCode == TakePhotoUtil.ACTIVITY_IMAGEPAGE_RESULT) {
            Bundle bundle = data.getBundleExtra("bundle2");
            if (bundle != null) {
                if (fragment instanceof ITakePhotoView) {
                    ((ITakePhotoView) fragment).onModifyPhoto(bundle.getStringArrayList("list"));
                } else if (activity instanceof ITakePhotoView) {
                    ((ITakePhotoView) activity).onModifyPhoto(bundle.getStringArrayList("list"));
                }

            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    public ITakePhoto getTakePhoto() {
        if (takePhoto == null) {
            if (fragment != null) {
                takePhoto = new TakePhotoImpl(fragment, this);
            } else {
                takePhoto = new TakePhotoImpl(activity, this);
            }
        }
        return takePhoto;
    }
}
