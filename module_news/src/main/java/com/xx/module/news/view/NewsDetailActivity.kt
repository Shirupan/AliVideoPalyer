package com.xx.module.news.view

import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.AppUtil
import com.mrkj.lib.common.util.ColorUtils
import com.mrkj.lib.common.view.SmToast
import com.tencent.smtt.sdk.WebView
import com.xx.lib.db.entity.SmShare
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.model.ThirdShareManager
import com.xx.module.common.model.WebviewDelegate
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.dialog.SocialShareDialog
import com.xx.module.common.view.widget.SmAnimationUtil
import com.xx.module.news.R
import com.xx.module.news.model.entity.NewsDetailJson
import com.xx.module.news.presenter.NewsDetailPresenter
import com.xx.module.news.view.constract.INewsDetailView

/**
 *@author someone
 *@date 2019-06-12
 */
@Path(RouterUrl.ACTIVITY_NEWS_DETAIL)
@Presenter(NewsDetailPresenter::class)
class NewsDetailActivity : BaseActivity<NewsDetailPresenter>(), INewsDetailView {
    private val bottomLayout by bindView<View>(R.id.news_detail_bottom_layout)
    private var mSid: Int = -1
    private var mData: NewsDetailJson? = null

    override fun getLayoutId(): Int = R.layout.activity_news_detail

    override fun beforeSetContentView() {
        setShowLoadingView(true)
       // setTheme(R.style.SmTheme_Night)
    }

    override fun initViewsAndEvents() {
        bottomLayout.visibility = View.GONE
        mSid = getIntExtra(RouterParams.NewsView.SID, -1)
        if (mSid == -1) {
            loadingViewManager?.showFailed("内容不存在")
            return
        }
        loadData()
    }

    private fun loadData() {
        presenter?.loadNewsDetails(loginUser?.token ?: "", mSid)
    }

    /**
     * 详情返回
     */
    override fun onNewsDetailResult(json: NewsDetailJson) {
        loadingViewManager?.dismiss()
        mData = json
        setToolBarTitle(json.title)
        setToolBarRight(R.drawable.icon_tool_bar_share) {
            //分享
            val dialog = SocialShareDialog(this@NewsDetailActivity)
            val smShare = ThirdShareManager.getDefaultShare(this)
            smShare.title = json.title
            if (!TextUtils.isEmpty(json.shareurl)) {
                smShare.url = json.shareurl
            }
            smShare.imgurl = json.imgurl
            if (TextUtils.isEmpty(json.sharemsg)) {
                smShare.content = json.title
            } else {
                smShare.content = json.sharemsg
            }
            smShare.kind = 1
            smShare.qid = json.sid
            dialog.smShare = smShare
            dialog.show()
        }
        bottomLayout.visibility = View.VISIBLE
        bottomLayout.post {
            findViewById<View>(R.id.news_detail_web).setPadding(0, 0, 0, bottomLayout.measuredHeight)
        }
        val titleTv = findViewById<TextView>(R.id.news_detail_title)
        titleTv.text = json.title
        val timeTv = findViewById<TextView>(R.id.news_detail_time)
        timeTv.text = "发布于：${json.timestr}"
        val readCountTv = findViewById<TextView>(R.id.news_detail_read_count)
        readCountTv.text = "已有${json.clicks}人阅读"
        setupCollectionUI(false)

        val webview = findViewById<WebView>(R.id.news_detail_web)
        webview.isEnabled = false
        WebviewDelegate.setupWebView(this, webview, object : WebviewDelegate.OnWebViewJavascriptInterfaceCallback {
            override fun share(smShare: SmShare?) {
                runOnUiThread {
                    val dialog = SocialShareDialog(this@NewsDetailActivity)
                    dialog.smShare = smShare
                    dialog.show()
                }
            }

            override fun onCancel() {
            }

            override fun toWeb(url: String?) {
                ActivityRouter.get().startWebActivity(this@NewsDetailActivity, url, "")
            }

        })
        WebviewDelegate.setupWebviewTextSize(webview)
        setupCollectionUI(false)
        setupZanUI(false)
        webview.loadDataWithBaseURL(null, json.content, "text/html", "utf-8", null)
    }

    /**
     * 收藏按钮点击事件
     */
    fun onShoucangClick(view: View) {
        mData?.let {
            if (it.iscollection == 1) {
                UserDataManager.getInstance().delCollection(this, it.sid, 1,
                        object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                it.iscollection = 0
                                it.collection--
                                setupCollectionUI(true)
                            }

                            override fun onError(e: SmError) {
                                SmToast.show(this@NewsDetailActivity, e.getMessage(this@NewsDetailActivity))
                            }
                        })
            } else {
                UserDataManager.getInstance().addCollection(this, it.sid, 1,
                        object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                it.iscollection = 1
                                it.collection++
                                setupCollectionUI(true)
                            }

                            override fun onError(e: SmError) {
                                SmToast.show(this@NewsDetailActivity, e.getMessage(this@NewsDetailActivity))
                            }
                        })
            }

        }
    }

    /**
     * 收藏的图标
     */
    private fun setupCollectionUI(showAnim: Boolean) {
        val collectionTv = findViewById<TextView>(R.id.news_detail_shjoucang)
        collectionTv.text = "${mData?.collection ?: 0}"
        if (mData?.iscollection ?: 0 == 0) {
            val resID = AppUtil.getThemeColor(this.theme, R.attr.smTipColor, R.color.text_99)
            val d = ColorUtils.setTintColorRes(this, R.drawable.icon_main_shouchang, resID)
            d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            collectionTv.setCompoundDrawables(d, null, null, null)
            collectionTv.setTextColor(ContextCompat.getColor(this, resID))
        } else {
            val d = ContextCompat.getDrawable(this, R.drawable.icon_main_shouchang_0)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            collectionTv.setCompoundDrawables(d, null, null, null)
            collectionTv.setTextColor(ContextCompat.getColor(this, R.color.text_red))
        }
        if (showAnim) {
            SmAnimationUtil.scale(collectionTv, 0.8f, true)
        }
    }

    /**
     * 点赞
     */
    private fun setupZanUI(showAnim: Boolean) {
        val zanTv = findViewById<TextView>(R.id.news_detail_zan)
        zanTv.text = "${mData?.praise ?: 0}"
        if (mData?.ispraise ?: 0 == 0) {
            val resID = AppUtil.getThemeColor(this.theme, R.attr.smTipColor, R.color.text_99)
            val d = ColorUtils.setTintColorRes(this, R.drawable.icon_main_zan, resID)
            d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            zanTv.setCompoundDrawables(d, null, null, null)
            zanTv.setTextColor(ContextCompat.getColor(this, resID))
        } else {
            val d = ContextCompat.getDrawable(this, R.drawable.icon_main_zan_0)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            zanTv.setCompoundDrawables(d, null, null, null)
            zanTv.setTextColor(ContextCompat.getColor(this, R.color.text_red))
        }
        if (showAnim) {
            SmAnimationUtil.scale(zanTv, 0.8f, true)
        }
    }

    /**
     * 点赞按钮点击事件
     */
    fun onZanClick(v: View) {
        mData?.let {
            if (it.ispraise == 1) {
                UserDataManager.getInstance().delPraise(this, it.sid, 1,
                        object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                it.ispraise = 0
                                it.praise--
                                setupZanUI(true)
                            }

                            override fun onError(e: SmError) {
                                SmToast.show(this@NewsDetailActivity, e.getMessage(this@NewsDetailActivity))
                            }
                        })
            } else {
                UserDataManager.getInstance().addPraise(this, it.sid, 1,
                        object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                it.ispraise = 1
                                it.praise++
                                setupZanUI(true)
                            }

                            override fun onError(e: SmError) {
                                SmToast.show(this@NewsDetailActivity, e.getMessage(this@NewsDetailActivity))
                            }
                        })
            }

        }
    }
}