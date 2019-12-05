package com.xx.module.common.view;


import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.ProgressBar;

import com.xx.lib.db.entity.SmShare;
import com.xx.module.common.R;
import com.xx.module.common.annotation.Path;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.model.ThirdShareManager;
import com.xx.module.common.model.WebviewDelegate;
import com.xx.module.common.router.RouterParams;
import com.xx.module.common.router.RouterUrl;
import com.xx.module.common.view.base.BaseActivity;
import com.xx.module.common.view.dialog.SocialShareDialog;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author
 * @Create 2016/12/21
 */
@Path(RouterUrl.ACTIVITY_WEB_VIEW)
public class WebViewActivity extends BaseActivity {
    String contenturl;
    public String title;

    String shareTitle;
    String shareImageUrl;

    public String shareContent;
    String shareUrl;
    public String shareKind;
    private boolean isWebError;

    private com.tencent.smtt.sdk.WebView webView;
    private SocialShareDialog sharePopupWindow;
    private ProgressBar progressBar;
    private SocialShareDialog shareDialog;

    @Override
    public int getLayoutId() {
        return R.layout.layout_web;
    }


    /**
     *
     */
    @Override
    protected void initViewsAndEvents() {
        //防止视频播放是闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getIntentData();

        sharePopupWindow = new SocialShareDialog(this);
        shareKind = "-1";
        if (TextUtils.isEmpty(shareUrl)) {
            shareUrl = contenturl;
        }
        if (TextUtils.isEmpty(contenturl)) {
            onBackPressed();
            return;
        }
        progressBar = findViewById(R.id.progress_bar);
        setToolBarBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
        setToolBarTitle(title);
        webView = findViewById(R.id.webView);
        initWebView();
        if (getLoadingViewManager() != null) {
            getLoadingViewManager().setOnRefreshClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    isWebError = false;
                    load(contenturl);
                    if (getLoadingViewManager() != null) {
                        getLoadingViewManager().loading();
                    }
                }
            });
        }
        Uri uri = Uri.parse(shareUrl);
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            setToolBarRight(R.drawable.icon_tool_bar_share, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareDialog == null) {
                        shareDialog = new SocialShareDialog(WebViewActivity.this);
                        SmShare smShare = new SmShare();
                        smShare.setKind(2);
                        smShare.setUrl(contenturl);
                        smShare.setTitle(title);
                        if (TextUtils.isEmpty(shareContent)) {
                            smShare.setContent("求桃花？求事业？求贵人？求平安？...大师帮您在线测算。");
                        } else {
                            smShare.setContent(shareContent);
                        }
                        shareDialog.setSmShare(smShare);
                    }
                    shareDialog.show();
                }
            });
        }
        load(contenturl);
    }

    /**
     * 获取通讯的数据
     */
    private void getIntentData() {
        contenturl = getIntent().getStringExtra(RouterParams.WebView.URL);
        shareUrl = getIntent().getStringExtra(RouterParams.WebView.SHARE_URL);
        shareImageUrl = getIntent().getStringExtra(RouterParams.WebView.SHARE_IMAGE);
        shareTitle = getIntent().getStringExtra(RouterParams.WebView.SHARE_TITLE);
        shareKind = getIntent().getStringExtra(RouterParams.WebView.SHARE_KIND);
        title = getIntent().getStringExtra(RouterParams.WebView.TITLE);
        shareContent = getIntent().getStringExtra(RouterParams.WebView.SHARE_CONTENT);
        shareKind = getIntent().getStringExtra(RouterParams.WebView.SHARE_KIND);

        //判断注解拿到的title是否为空
        if (TextUtils.isEmpty(title)) {
            title = getIntent().getStringExtra(RouterParams.WebView.TITLE);
        }
        if (TextUtils.isEmpty(shareTitle)) {
            shareTitle = title;
        }
        if (TextUtils.isEmpty(shareContent)) {
            shareContent = getIntent().getStringExtra(RouterParams.WebView.SHARE_CONTENT);
        }
    }


    private void load(String url) {
        Map<String, String> map = new HashMap<>();
        map.put("Referer", url);
        webView.loadUrl(WebviewDelegate.getUrlWithParams(url), map);
    }


    private void initWebView() {
        WebviewDelegate.setupWebView(this, webView,
                new WebviewDelegate.SimpleOnWebViewJavascriptInterfaceCallback() {
                    @Override
                    public void share(final SmShare smShare) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (smShare.getShareType() != null && smShare.getShareType().length == 1) {
                                    SHARE_MEDIA shareMedia = ThirdShareManager.getShareMedia(smShare.getShareType()[0]);
                                    ThirdShareManager.share(WebViewActivity.this, smShare, contenturl, shareMedia, null);
                                } else {
                                    sharePopupWindow.setSmShare(smShare);
                                    sharePopupWindow.setContentUrl(contenturl);
                                    sharePopupWindow.show();
                                }
                            }
                        });
                    }


                    @Override
                    public void toWeb(String url) {
                        //跳转系统浏览器
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });

        webView.setWebChromeClient(new com.tencent.smtt.sdk.WebChromeClient() {
            @Override
            public void onProgressChanged(com.tencent.smtt.sdk.WebView view, int newProgress) {
                if (webView != null && webView.canGoBack()) {
                    showLeftSecondedBack(true);
                }
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress > 50) {
                    if (getLoadingViewManager() != null && !isWebError) {
                        getLoadingViewManager().dismiss();
                    }
                }
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(com.tencent.smtt.sdk.WebView view, String title) {
                if (!TextUtils.isEmpty(title)) {
                    WebViewActivity.this.title = title;
                    setToolBarTitle(title);
                }
            }

            @Override
            public boolean onShowFileChooser(com.tencent.smtt.sdk.WebView webView, com.tencent.smtt.sdk.ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
                mUploadMessage = valueCallback;
                TakePhotoUtil.pickFileFromSystemChooser(WebViewActivity.this, PICK_FILE);
                return true;
            }
        });

    }

    private final int PICK_FILE = 1010;
    private ValueCallback<Uri[]> mUploadMessage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            Uri result = data == null ? null : data.getData();
            if (mUploadMessage != null) {
                Uri[] uris = new Uri[1];
                uris[0] = result;
                mUploadMessage.onReceiveValue(uris);
                return;
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (webView != null) {
            WebviewDelegate.destroyWebView(webView);
        }
        sharePopupWindow = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


}
