package com.xx.module.common.router;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.chenenyu.router.Router;
import com.chenenyu.router.template.RouteTable;
import com.mrkj.lib.common.util.ActivityManagerUtil;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.view.SmToast;
import com.xx.module.common.BaseConfig;

import java.util.Map;
import java.util.Set;

/**
 * @author someone
 * @date 2019-05-28
 */
public class ActivityRouter {


    public static final int LOGIN_REQUEST_CODE = 400;

    public static ActivityRouter get() {
        return Singleton.instance;
    }

    /**
     * {@link Activity#startActivityForResult(Intent, int)} with  {@link ActivityRouter#LOGIN_REQUEST_CODE}
     * if login success, {@link Activity#onActivityResult(int, int, Intent)} will  return with resultCode {@link Activity#RESULT_OK}
     *
     * @param activity
     */
    public void goToLoginActivity(Activity activity) {
        Router.build(RouterUrl.ACTIVITY_LOGIN_MAIN).requestCode(LOGIN_REQUEST_CODE).go(activity);
    }

    /**
     * {@link Fragment#startActivityForResult(Intent, int)} with  {@link ActivityRouter#LOGIN_REQUEST_CODE}
     * if login success, {@link Fragment#onActivityResult(int, int, Intent)} will  return with resultCode {@link Activity#RESULT_OK}
     *
     * @param fragment
     */
    public void goToLoginActivity(Fragment fragment) {
        Router.build(RouterUrl.ACTIVITY_LOGIN_MAIN).requestCode(LOGIN_REQUEST_CODE).go(fragment);
    }

    public void handleRouteTable(RouteTable routeTable) {
        Router.handleRouteTable(routeTable);
    }


    public Intent getIntent(Context context, String url) {
        return Router.build(url).getIntent(context);
    }

    public void startActivity(Context context, String url) {
        startActivity(context, url, 0);
    }


    public void startActivity(Context context, String url, int requestCode) {
        if (context == null) {
            return;
        }
        if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
            startWebActivity(context, url, "");
        } else {
            Intent intent = getIntent(context, url);
            startActivity(context, url, requestCode, intent);
        }
    }

    private void startActivity(Context context, String url, int requestCode, Intent intent) {
        if (intent == null) {
            return;
        }
        String uri = intent.getData() != null ? intent.getDataString() : "";
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null && uri.equals(url)) {
            return;
        }
        try {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startActivity(Context context, String url, Map<String, String> parmas, boolean newTask,
                              int requestCode) {
        Intent intent = Router.build(url).requestCode(requestCode).getIntent(context);
        if (intent == null) {
            return;
        }
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        putExtra2Intent(parmas, intent);
        startActivity(context, url, requestCode, intent);
    }

    private static void putExtra2Intent(Map<String, String> parmas, Intent intent) {
        if (parmas != null) {
            Set<String> keys = parmas.keySet();
            for (String key : keys) {
                String value = parmas.get(key);
                if (!TextUtils.isEmpty(value)) {
                    intent.putExtra(key, value);
                }
            }
        }
    }

    /**
     * 根据uri数据跳转指定页面
     *
     * @param context
     * @param uri
     */
    public static void handleUri(Context context, Uri uri) {
        if (uri == null) {
            SmLogger.d("正常启动应用");
            startLaunchActivity(context, null);
            return;
        }
        if (ActivityManagerUtil.getScreenManager().isMainActivityOpened()) {
            ActivityRouter.get().startActivity(context, uri.toString());
        } else {
            startLaunchActivity(context, uri);
        }
    }

    private static void startLaunchActivity(Context context, Uri uri) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            //SmToast.show(context, "正常启动应用");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            if (uri != null) {
                intent.setData(uri);
            }
            context.startActivity(intent);
        } else {
            SmToast.show(context, "启动启动页null");
        }
    }


    public void startWebActivity(Context context, @Nullable String url, @Nullable String title) {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put(RouterParams.WebView.TITLE, title);
        map.put(RouterParams.WebView.URL, url);
        startActivity(context, RouterUrl.ACTIVITY_WEB_VIEW, map, false, 0);
    }

    /**
     * 打开知命QQ客服聊天窗口
     *
     * @param context
     */
    public static void openQQCustom(final Context context) {
        if (AppUtil.isAppInstalled(context, AppUtil.PACKAGE_NAME_QQ)) {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("即将打开QQ客服聊天窗口。\n若消息发送失败，要先添加好友噢。")
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppUtil.copyToBoard(context, BaseConfig.CUSTOM_QQ, "客户QQ号已复制剪切板");
                            dialog.dismiss();
                            String url1 = "mqqwpa://im/chat?chat_type=wpa&uin=" + BaseConfig.CUSTOM_QQ;
                            Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
                            i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i1.setAction(Intent.ACTION_VIEW);
                            context.startActivity(i1);
                        }
                    }).show();

        } else {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("您需要先下载手机QQ客户端，才可以联系客服QQ").setPositiveButton("前往下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        Uri uri = Uri.parse("market://details?id=com.tencent.mobileqq");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage(context.getPackageName());
                        context.startActivity(intent);
                    } catch (Exception e) {
                        SmToast.show(context, "打开应用市场失败，请手动下载");
                    }

                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }


    static class Singleton {
        static ActivityRouter instance = new ActivityRouter();
    }


}
