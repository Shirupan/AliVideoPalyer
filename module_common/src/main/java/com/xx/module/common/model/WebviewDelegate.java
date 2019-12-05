package com.xx.module.common.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mrkj.lib.common.permission.PermissionUtil;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.FileUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.common.util.StringUtil;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.tencent.smtt.sdk.WebView;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.SmShare;
import com.xx.module.common.UserDataManager;
import com.xx.module.common.imageload.BitmapUtil;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.model.callback.SimpleSubscriber;
import com.xx.module.common.router.ActivityRouter;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.exceptions.Exceptions;

public class WebviewDelegate {
    public static void destroyWebView(android.webkit.WebView webView) {
        if (webView == null) {
            return;
        }
        ViewParent parent = webView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(webView);
        }

        webView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        webView.getSettings().setJavaScriptEnabled(false);
        webView.clearHistory();
        webView.removeAllViews();
        webView.destroy();
    }

    public static void destroyWebView(WebView webView) {
        if (webView == null) {
            return;
        }
        ViewParent parent = webView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(webView);
        }

        webView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        webView.getSettings().setJavaScriptEnabled(false);
        webView.clearHistory();
        webView.removeAllViews();
        webView.destroy();
    }

    public static void setupWebView(final Context context, final WebView webView,
                                    final WebviewDelegate.OnWebViewJavascriptInterfaceCallback callback) {
        //解决点击webview会跳会top的情况(让webview保持焦点)
        webView.setFocusable(false);
        com.tencent.smtt.sdk.WebSettings settings = webView.getSettings();
        //启用js 4.2以下版本
        settings.setJavaScriptEnabled(true);
        //js和android交互 (js打开新窗口)
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //设置webview推荐使用的窗口
        settings.setUseWideViewPort(true);
        //设置webview加载的页面的模
        settings.setLoadWithOverviewMode(true);
        //设置，可能的话使所有列的宽度不超过屏幕宽度
        settings.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置可以使用localStorage
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        //设置H5的缓存打开,默认关闭
        settings.setAppCacheEnabled(true);
        String appCachePath = context.getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        // 允许访问文件
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        //关闭zoom按钮
        settings.setSupportZoom(false);
        //关闭zoom
        settings.setBuiltInZoomControls(false);
        webView.setDownloadListener(new com.tencent.smtt.sdk.DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                startDownload(context.getApplicationContext(), url, mimetype, contentLength);
            }
        });
        final WebViewClient viewClient = new WebViewClient(context);
        webView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                return viewClient.shouldOverrideUrlLoading(webView, url);
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                viewClient.onPageFinished(webView, s);
            }
        });
        if (context instanceof Activity) {
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    WebView.HitTestResult result = webView.getHitTestResult();
                    if (result == null) {
                        return false;
                    }
                    int type = result.getType();
                    if (type == WebView.HitTestResult.IMAGE_TYPE) {
                        WebviewDelegate.LongClickedDialogBuilder builder = new WebviewDelegate.LongClickedDialogBuilder((Activity) context);
                        String imgurl = result.getExtra();
                        builder.setImageUrl(imgurl);
                        builder.show();
                        return true;
                    }
                    return false;
                }
            });
        }

        webView.addJavascriptInterface(new JavascriptObject(context, callback), "android");
    }

    /**
     * 给定文字大小
     *
     * @param webView
     */
    public static void setupWebviewTextSize(WebView webView) {
        com.tencent.smtt.sdk.WebSettings settings = webView.getSettings();
        settings.setTextZoom(200);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public static void setupWebView(final Context context, final android.webkit.WebView webView,
                                    final WebviewDelegate.OnWebViewJavascriptInterfaceCallback callback) {
        //解决点击webview会跳会top的情况(让webview保持焦点)
        webView.setFocusable(false);
        WebSettings settings = webView.getSettings();
        //启用js 4.2以下版本
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 允许访问文件
        settings.setAllowFileAccess(true);
        //设置H5的缓存打开,默认关闭
        settings.setAppCacheEnabled(true);
        //设置webview推荐使用的窗口
        settings.setUseWideViewPort(true);
        //设置webview加载的页面的模
        settings.setLoadWithOverviewMode(true);
        //设置，可能的话使所有列的宽度不超过屏幕宽度
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置可以使用localStorage
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = context.getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);

        //关闭zoom按钮
        settings.setSupportZoom(false);
        //关闭zoom
        settings.setBuiltInZoomControls(false);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                startDownload(context.getApplicationContext(), url, mimetype, contentLength);
            }
        });

        final WebViewClient viewClient = new WebViewClient(context);
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                return viewClient.shouldOverrideUrlLoading(webView, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, WebResourceRequest request) {
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewClient.shouldOverrideUrlLoading(webView, request.getUrl().toString());
            }

            @Override
            public void onReceivedSslError(android.webkit.WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                viewClient.onReceivedSslError(webView, sslErrorHandler, sslError);
            }
        });

        if (context instanceof Activity) {
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    android.webkit.WebView.HitTestResult result = webView.getHitTestResult();
                    if (null == result) {
                        return false;
                    }
                    int type = result.getType();
                    if (type == WebView.HitTestResult.IMAGE_TYPE) {
                        WebviewDelegate.LongClickedDialogBuilder builder = new WebviewDelegate.LongClickedDialogBuilder((Activity) context);
                        String imgurl = result.getExtra();
                        builder.setImageUrl(imgurl);
                        builder.show();
                        return true;
                    }
                    return false;
                }
            });
        }
        webView.addJavascriptInterface(new JavascriptObject(context, callback), "android");
    }


    public abstract static class SimpleOnWebViewJavascriptInterfaceCallback implements WebviewDelegate.OnWebViewJavascriptInterfaceCallback {

        @Override
        public void share(SmShare smShare) {

        }


        @Override
        public void onCancel() {

        }

        @Override
        public void toWeb(String url) {

        }
    }

    public interface OnWebViewJavascriptInterfaceCallback {
        void share(SmShare smShare);

        void onCancel();

        void toWeb(String url);
    }

    private static class WebViewClient {
        private Context mContext;


        public WebViewClient(Context context) {
            mContext = context;
        }

        public boolean shouldOverrideUrlLoading(Object webView, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            SmLogger.d("跳转：" + url);
            //跳转支付宝客户端
            if (url.contains("alipays://")) {
                if (AppUtil.isAppInstalled(mContext, "com.eg.android.AlipayGphone")) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                } else {
                    if (webView instanceof WebView) {
                        ((WebView) webView).loadUrl(url);
                    } else if (webView instanceof android.webkit.WebView) {
                        ((android.webkit.WebView) webView).loadUrl(url);
                    }
                    return true;
                }
            }
            if (url.contains("http://") || url.contains("https://")) {
                if (webView instanceof WebView) {
                    String originalUrl = ((WebView) webView).getOriginalUrl();
                    Map<String, String> extraHeaders = new HashMap<>();
                    //微信支付的时候手动添加Referer(传递发起微信支付的域名或者url)
                    if (!TextUtils.isEmpty(originalUrl) && url.startsWith("https://wx.tenpay.com/")) {
                        extraHeaders.put("Referer", originalUrl);
                        ((WebView) webView).loadUrl(url, extraHeaders);
                    } else {
                        ((WebView) webView).loadUrl(url);
                    }
                } else if (webView instanceof android.webkit.WebView) {
                    String originalUrl = ((android.webkit.WebView) webView).getOriginalUrl();

                    Map<String, String> extraHeaders = new HashMap<>();
                    //微信支付的时候手动添加Referer
                    if (!TextUtils.isEmpty(originalUrl) && url.startsWith("https://wx.tenpay.com/")) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                            if (!TextUtils.isEmpty(originalUrl) && originalUrl.startsWith("https://wx.tenpay.com/")) {
                                return false;
                            }
                            ((android.webkit.WebView) webView).loadDataWithBaseURL(originalUrl,
                                    "<script>window.location.href=\"" + url + "\";</script>",
                                    "text/html", "utf-8", null);
                        } else {
                            extraHeaders.put("Referer", originalUrl);
                            ((android.webkit.WebView) webView).loadUrl(url, extraHeaders);
                        }
                    } else {
                        ((android.webkit.WebView) webView).loadUrl(url);
                    }
                }
                return true;
            }
            //跳转微信客户端
            else if (url.contains("weixin://")) {
                if (AppUtil.isAppInstalled(mContext, "com.tencent.mm")) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                } else {
                    SmToast.show(mContext, "您未安装微信");
                }
                return true;
            } else {
                return false;
            }
        }

        public void onPageFinished(WebView view, String url) {
          /*  if (url.startsWith("https://wx.tenpay.com/")) {
                view.loadUrl("javascript:window.android.getSource('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }*/
        }

        public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError error) {
            //此方法是为了处理在5.0以上Htts的问题，必须加上
            handler.proceed();
        }

        public void onReceivedSslError(WebView view, com.tencent.smtt.export.external.interfaces.SslErrorHandler handler, com.tencent.smtt.export.external.interfaces.SslError error) {
            //此方法是为了处理在5.0以上Htts的问题，必须加上
            handler.proceed();
        }
    }

    /**
     * JavaScript调用
     */
    private static class JavascriptObject {
        private Context mContext;
        private WebviewDelegate.OnWebViewJavascriptInterfaceCallback callback;

        public JavascriptObject(Context context, WebviewDelegate.OnWebViewJavascriptInterfaceCallback callback) {
            this.mContext = context;
            this.callback = callback;
        }


        //网页中的分享按钮
        @JavascriptInterface
        public void share(String json) {
            //友盟统计
            MobclickAgent.onEvent(mContext.getApplicationContext(), "share_from_js");
            Gson gson = new Gson();
            SmShare smShare = gson.fromJson(json, SmShare.class);
            if (smShare.getShareMode() == 0) {
                smShare.setShareMode(ThirdShareManager.SHARE_MODE_WEB);
            }
            if (smShare.getQid() == 0) {
                smShare.setQid(ThirdShareManager.SHARE_MODE_WEB);
            }
            if (!TextUtils.isEmpty(smShare.getSharetype())) {
                String[] temp = smShare.getSharetype().split("#");
                List<Integer> types = new ArrayList<>();
                for (String t : temp) {
                    types.add(StringUtil.integerValueOf(t, 0));
                }
                if (!types.isEmpty()) {
                    Integer[] ts = new Integer[types.size()];
                    types.toArray(ts);
                    smShare.setShareType(ts);
                }

            }

            if (callback != null) {
                callback.share(smShare);
            }
        }


        //路由跳转指定页面
        @JavascriptInterface
        public void toActivity(String url, String paramsStr) {
            //友盟统计
            MobclickAgent.onEvent(mContext.getApplicationContext(), "to_activity_from_js");
            //6.5.5版本加入
            Map<String, String> params = GsonSingleton.getInstance().fromJson(paramsStr, new TypeToken<Map<String, String>>() {
            }.getType());
            ActivityRouter.get().startActivity(mContext, url, params, false, 0);
        }

        //路由跳转指定页面
        @JavascriptInterface
        public void toActivity(String url) {
            //友盟统计
            MobclickAgent.onEvent(mContext.getApplicationContext(), "to_activity_from_js");
            ActivityRouter.get().startActivity(mContext, url, null, false, 0);
        }

        @JavascriptInterface
        public void toWeb(String url) {
            if (callback != null) {
                callback.toWeb(url);
            }
        }

        /**
         * 保存图片到本地
         *
         * @param url
         */
        @JavascriptInterface
        public void saveImg(String url) {
            BitmapUtil.saveBitmapLocal(mContext, url, AppUtil.getAppExtraDCIMDir(mContext),
                    new SimpleSubscriber<String>() {
                        @Override
                        public void onNext(String s) {
                            super.onNext(s);
                            SmToast.show(mContext, "图片已保存：" + s);
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            SmToast.show(mContext, ExceptionUtl.catchTheError(e));
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();

                        }
                    });
        }


        @JavascriptInterface
        public void onCancel() {
            if (callback != null) {
                callback.onCancel();
            }
        }
    }

    /**
     * 网页内容下载服务
     *
     * @param c
     * @param url
     * @param mimetype
     * @param contentLength
     */
    private static void startDownload(final Context c, final String url, final String mimetype, final long contentLength) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType(mimetype);
        request.setVisibleInDownloadsUi(true);
        File f;
        File path;
        if (TextUtils.equals(mimetype, "application/vnd.android.package-archive")) {
            path = new File(AppUtil.getAppCachePath(c));
        } else if (!TextUtils.isEmpty(mimetype) && mimetype.contains("image")) {
            path = c.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        } else {
            path = c.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }
        String fileName = FileUtil.getNameFromUrl(url);
        f = new File(path, fileName);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw Exceptions.propagate(new IOException("保存文件失败"));
        }
        request.setDestinationUri(Uri.fromFile(f));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        SmSystemDownloadManager manager = new SmSystemDownloadManager(c, request);
        manager.setMimeType(mimetype);
        manager.enqueue();
    }

    public static class LongClickedDialogBuilder extends AlertDialog.Builder {
        private String imageUrl;
        private Activity mActivity;

        protected LongClickedDialogBuilder(@NonNull Activity activity) {
            super(activity);
            mActivity = activity;
            setItems(new String[]{"查看大图", "保存到手机", "取消"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which) {
                        case 0:
                            TakePhotoUtil.openImagesShower(mActivity, new String[]{imageUrl}, 0);
                            break;
                        case 1:
                            PermissionUtil.checkAndRequestPermissions(mActivity,
                                    new PermissionUtil.SimpleOnPermissionRequestCallback() {
                                        @Override
                                        public void onSuccess() {
                                            String path = AppUtil.getAppExtraDCIMDir(mActivity) + File.separator + "images";
                                            BitmapUtil.saveBitmapLocal(mActivity, imageUrl, path, new SimpleSubscriber<String>(mActivity) {
                                                @Override
                                                public void onNext(String s) {
                                                    super.onNext(s);
                                                    SmToast.showToastRight(mActivity, "保存成功：" + s);
                                                }
                                            });
                                        }
                                    }, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                            break;
                        default:
                    }
                }
            });
        }

        public LongClickedDialogBuilder setImageUrl(String url) {
            imageUrl = url;
            return this;
        }

    }


    public static String getUrlWithParams(String url) {
        if (TextUtils.isEmpty(url) || UserDataManager.getInstance().getUserSystem() == null) {
            return url;
        }
        return url;
    }
}
