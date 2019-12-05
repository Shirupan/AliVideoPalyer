package com.xx.video_dev.view;


import android.net.Uri;
import android.os.Build;
import android.view.WindowManager;

import com.mrkj.lib.common.util.SmLogger;
import com.xx.module.common.router.ActivityRouter;
import com.xx.module.common.view.base.BaseActivity;

/**
 * App Link 中转路由
 *
 * @author
 * @date 2018/6/14 0014
 */
public class RouterActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    protected void initViewsAndEvents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        Uri uri = getIntent().getData();
        if (uri != null) {
            SmLogger.d("RouterActivity跳转信息：" + uri.toString());
        }
        ActivityRouter.handleUri(this, uri);
        finish();
    }
}
