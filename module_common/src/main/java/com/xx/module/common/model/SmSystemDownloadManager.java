package com.xx.module.common.model;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.Toast;

import com.mrkj.lib.common.util.ActivityManagerUtil;
import com.mrkj.lib.common.view.SmToast;
import com.xx.module.common.model.callback.SimpleSubscriber;

import io.reactivex.functions.Function;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

/**
 * 封装系统下载器
 *
 * @author
 * @date 2018/8/7 0007
 */
public class SmSystemDownloadManager {
    private DownloadManager.Request mRequest;
    private Context mContext;
    private DownloadManager downloadManager;
    private Long mRequestID;
    private MyReceiver myReceiver;

    private String mimeType;


    public SmSystemDownloadManager(@NonNull Context c, DownloadManager.Request request) {
        mRequest = request;
        mContext = c.getApplicationContext();
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void enqueue() {
        if (mRequestID != null) {
            return;
        }
        myReceiver = new MyReceiver();
        //注册广播接收者，监听下载状态
        mContext.registerReceiver(myReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        if (mRequest != null) {
            SmToast.show(mContext, "开始下载");
            mRequestID = downloadManager.enqueue(mRequest);
        }
    }


    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                checkStatus(context);
            }
        }
    }

    //检查下载状态
    private void checkStatus(Context context) {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(mRequestID);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    Toast.makeText(context, "已下载完成，请查看通知栏", Toast.LENGTH_SHORT).show();
                    if (!TextUtils.isEmpty(mimeType) && TextUtils.equals("application/vnd.android.package-archive", mimeType)) {
                        //下载完成安装APK
                        installAPKCheck();
                    } else {
                        unregisterReceiver();
                    }
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    SmToast.show(mContext, "下载失败");
                    unregisterReceiver();
                    break;
                default:
            }
        }
        c.close();
    }

    //下载到本地后执行安装
    private void installAPKCheck() {
        installAPK(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void jumpActivityAndResult(Activity activity) {
        RxActivityResult.on(activity)
                .startIntent(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + activity.getPackageName())))
                .map(new Function<Result<Activity>, Integer>() {
                    @Override
                    public Integer apply(Result<Activity> activityResult) throws Exception {
                        return activityResult.requestCode();
                    }
                }).subscribe(new SimpleSubscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                installAPK(false);
            }
        });
    }

    private void installAPK(boolean again) {
        Uri downloadFileUri;
        downloadFileUri = downloadManager.getUriForDownloadedFile(mRequestID);
        Intent installIntent;
        if (downloadFileUri != null) {
            Activity activity = ActivityManagerUtil.getScreenManager().currentActivity();
            installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installIntent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!again && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !mContext.getPackageManager().canRequestPackageInstalls()) {
                //8.0以上需要判断应用是否有安装权限，并且是否用户打开允许安装开关
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (activity != null) {
                    SmToast.show(activity, "请开启权限后重新点击安装");
                    activity.startActivity(intent);
                }
            } else {
                try {
                    if (activity != null) {
                        activity.startActivity(installIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SmToast.show(mContext, "安装器开启失败");
                }
            }
        }
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (myReceiver != null) {
            mContext.unregisterReceiver(myReceiver);
        }
        mRequestID = null;
    }

}
