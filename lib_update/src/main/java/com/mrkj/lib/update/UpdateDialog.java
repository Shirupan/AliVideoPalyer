package com.mrkj.lib.update;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.loader.file.SmNetProgressDialog;
import com.sm.lib_update.R;

/**
 * @Function 该类功能：升级弹窗
 * @Auditor
 * @Date 2017/5/4.
 */

public class UpdateDialog extends Dialog {
    private Context context;
    private Object title, content;
    private ProgressBar upgradepPogress;
    private TextView updateBtn;
    private ImageView cancelBtn;
    private Runnable cancelBtnClick;
    private String downloadUrl;
    private String version;
    private SmUpdateManager.OnPassUpdateCheckListener listener;
    private boolean forceUpdate;
    private boolean isDownloading;

    public UpdateDialog(Context context, Object title, Object content) {
        super(context, R.style.update_dialog);
        this.context = context;
        this.title = title;
        this.content = content;
        init();
    }

    private void init() {
        // 实例化对话框布局
        View view = LayoutInflater.from(context).inflate(R.layout.sm_dialog_upgrade, null);
        TextView dialogTitle = view.findViewById(R.id.tips_title_txt);
        dialogTitle.setText(getString(context, title));

        upgradepPogress = view.findViewById(R.id.to_upgrade_progress);
        upgradepPogress.setVisibility(View.INVISIBLE);

        TextView dialogContent = view.findViewById(R.id.tips_content_txt);
        dialogContent.setText(getString(context, content));
        dialogContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        updateBtn = view.findViewById(R.id.to_upgrade_txt);
        //立即升级
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDownloading) {
                    SmToast.show(context, "正在后台下载更新包");
                    dismiss();
                } else {
                    if (UpdateUtil.isNetWorkConnected(context) != 2) {
                        new AlertDialog.Builder(context)
                                .setTitle("网络提示")
                                .setMessage("您当前不在WIFI环境。为节约您的数据流量，建议您在连接WIFI环境下载更新包。")
                                .setNegativeButton("暂不下载", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        if (listener != null) {
                                            listener.onPass();
                                        }
                                    }
                                })
                                .setPositiveButton("坚持下载", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gotoDownload();
                                    }
                                }).show();
                    } else {
                        gotoDownload();
                    }
                }
            }
        });
        //取消
        cancelBtn = view.findViewById(R.id.dialog_cancel);
        setContentView(view);
        setCancelable(false);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mBound && mService != null) {
                    Message message = Message.obtain();
                    message.replyTo = mClientMessenger;
                    try {
                        message.what = 1;
                        mService.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    context.unbindService(appDownloadConnection);
                    mBound = false;
                }
            }
        });
    }

    /**
     * 向Service发送Message的Messenger对象
     */
    private Messenger mService = null;
    private Messenger mClientMessenger;
    /**
     * 判断有没有绑定Service
     */
    private boolean mBound;
    /**
     * 与Service通信
     */
    private ServiceConnection appDownloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            //客户端的Messenger对象（处理Service发来的消息）
            mClientMessenger = new Messenger(appDownloadHandler);
            Message message = Message.obtain();
            message.replyTo = mClientMessenger;
            try {
                message.what = 0;
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler appDownloadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 0: //开始
                case 1:// 进度
                    upgradepPogress.setVisibility(View.VISIBLE);
                    isDownloading = true;
                    updateBtn.setText("后台下载");
                    int progress = msg.arg1;
                    upgradepPogress.setIndeterminate(false);
                    upgradepPogress.setProgress(progress);
                    break;
                case 2: //完成,并跳转安装
                    dismiss();
                    break;
                case 3: //失败
                    SmToast.show(context, "下载失败，请稍后重试。");
                    isDownloading = false;
                    upgradepPogress.setVisibility(View.INVISIBLE);
                    updateBtn.setText("马上更新");
                    break;
            }
        }
    };

    private void gotoDownload() {
        if (TextUtils.isEmpty(downloadUrl)) {
            downloadUrl = UpdateConfig.GET_URL_BASC
                    + "android_client_update.html?doAction=downloadUpdateFile&and_code="
                    + UpdateUtil.getAppMetaData(context, UpdateConfig.META_KEY_UPDATE_CODE) + "&complete=0";
        }

        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setAction("com.mrkj.sm.DownloadAppIntentService");
        intent.putExtra("version", version);
        intent.putExtra("downUrl", downloadUrl);

        context.bindService(intent, appDownloadConnection, Context.BIND_AUTO_CREATE);
        if (forceUpdate) {
            new SmNetProgressDialog.Builder(context)
                    .setMessage("正在下载更新\n请稍等...")
                    .setCancelable(false)
                    .build()
                    .show();
        }
        if (listener != null) {
            listener.onUpdating();
        }
    }

    @Override
    public void show() {
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound && mService != null) {
                    Message message = Message.obtain();
                    message.replyTo = null;
                    try {
                        message.what = 2;
                        mService.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mBound = false;
                }
                if (cancelBtnClick != null) {
                    cancelBtnClick.run();
                }
                if (listener != null) {
                    listener.onUpdating();
                }
                dismiss();
            }
        });
        super.show();
    }

    /**
     * 解析参数
     *
     * @param context
     * @param param
     * @return
     */
    private static String getString(Context context, Object param) {
        if (param instanceof String) {
            return param.toString();
        } else if (param instanceof Integer) {
            return context.getString((Integer) param);
        }
        return null;
    }


    public UpdateDialog setCacelClickListener(Runnable r) {
        cancelBtnClick = r;
        return this;
    }


    public UpdateDialog setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public UpdateDialog setVersionName(String newVersonName) {
        this.version = newVersonName;
        return this;
    }

    public UpdateDialog setOnPassUpdateCheckListener(SmUpdateManager.OnPassUpdateCheckListener listener) {
        this.listener = listener;
        return this;
    }

    public UpdateDialog setForceUpdate(boolean force) {
        this.forceUpdate = force;
        return this;
    }

    public interface OnShowNoAnyMoreListener {
        void show(boolean isShow);
    }
}
