package com.xx.module.video.view

import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aliyun.vodplayer.media.AliyunVodPlayer
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.AppUtil
import com.mrkj.lib.common.util.SmLogger
import com.mrkj.lib.common.view.SmToast
import com.xx.lib.db.dao.AppDatabase
import com.xx.lib.db.entity.MainVideo
import com.xx.lib.db.entity.SmContextWrap
import com.xx.lib.db.entity.UserSetting
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.model.ThirdShareManager
import com.xx.module.common.model.callback.SimpleFlowableSubscriber
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.dialog.SocialShareDialog
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.widget.SmAnimationUtil
import com.xx.module.video.R
import com.xx.module.video.model.entity.VideoDetail
import com.xx.module.video.model.entity.VideoRecommened
import com.xx.module.video.presenter.VideoDetailPresenter
import com.xx.module.video.view.constract.IVideoDetailView
import com.xx.module.video.view.widget.SmAliYunVideoControl
import com.xx.module.video.view.widget.SmVideoPlayerManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 *@author someone
 *@date 2019-06-04
 */
@Path(RouterUrl.ACTIVITY_VIDEO_DETAIL)
@Presenter(VideoDetailPresenter::class)
class VideoDetailActivity : BaseActivity<VideoDetailPresenter>(), IVideoDetailView {

    val mToolbar by bindView<Toolbar>(R.id.main_status_bar)
    val mAppBarLayout by bindView<AppBarLayout>(R.id.main_app_bar_layout)
    val mCollapsingToolbarLayout by bindView<CollapsingToolbarLayout>(R.id.main_collapsing_layout)
    val mRv by bindView<RecyclerView>(R.id.video_detail_rv)
    var mData: MainVideo? = null
    var videoId: Int? = null
    val mVideoControl by bindView<SmAliYunVideoControl>(R.id.video_detail_control)

    var mAliyunPlayer: AliyunVodPlayer? = null

    var mAdapter: ItemAdapter? = null

    var mUserSetting: UserSetting? = null

    /**
     * 屏幕锁
     */
    var weaLock: PowerManager.WakeLock? = null

    override fun getLayoutId(): Int = R.layout.activity_video_detail_main

    override fun beforeSetContentView() {
        setShowLoadingView(true)
    }

    override fun initViewsAndEvents() {
        setStatusBar(true, true)

        setCutoutAndStatusMaxHeightToView(R.id.main_status_bar_2)
        setCutoutAndStatusMaxHeightToView(R.id.main_status_bar_1)
        mToolbar.post {
            val lp = mToolbar.layoutParams
            lp.height = findViewById<View>(R.id.main_status_bar_layout).measuredHeight
            mToolbar.layoutParams = lp
        }
        mData = getInstanceExtra(RouterParams.VideoView.DATA, MainVideo::class.java)
        videoId = mData?.vid
        if (videoId == null) {
            videoId = getIntExtra(RouterParams.VideoView.VID, -1)
        }
        if (videoId == -1) {
            SmToast.show(this, "视频出错了")
            finish()
            return
        }
        getUSerSetting()
        loadingViewManager?.setOnRefreshClickListener {
            loadingViewManager?.loading()
            loadData()
        }
        loadingViewManager?.post {
            loadData()
        }
    }


    private fun loadData() {
        val token = loginUser?.token ?: ""
        presenter?.loadDetail(token, videoId!!)
    }

    private fun getUSerSetting() {
        AppDatabase.getInstance(this@VideoDetailActivity).userSettingDao
                .getSettingByToken(loginUser?.token ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SimpleFlowableSubscriber<List<UserSetting>>() {
                    override fun onNext(t: List<UserSetting>) {
                        if (t.isNotEmpty()) {
                            mUserSetting = t[0]
                        }
                    }
                })
    }

    override fun onVideoDetailResult(data: VideoDetail) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mVideoControl.setTopBarPaddingTop(cutOutAndStatusMaxHeight)
        }
        mVideoControl.setCoverImage(data.obj1.coverurl)
        mVideoControl.setupTextureView()
        mToolbar.alpha = 0.0f
        setStatusBar(true, false)
        loadingViewManager?.dismiss()
        mAdapter = ItemAdapter()
        mAdapter?.mVideo = data.obj1
        mAdapter?.data = data.obj2
        mRv.layoutManager = LinearLayoutManager(this)
        mRv.adapter = mAdapter

        setToolBarTitle(data.obj1.videotitle)
        mAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, p1 ->
            val lp = mCollapsingToolbarLayout.layoutParams as AppBarLayout.LayoutParams
            if (lp.scrollFlags != 0) {
                val alpha = Math.abs(p1).toFloat() / p0.totalScrollRange.toFloat()
                mToolbar.alpha = alpha
            } else {
                mToolbar.alpha = 0.0f
            }
        })
        //准备视频播放
        prepareMediaPlayer(data.obj1.videourl)
    }

    private fun prepareMediaPlayer(url: String?) {
        if (mAliyunPlayer == null) {
            mAliyunPlayer = AliyunVodPlayer(this)
        }
        mVideoControl.setPlayerCallback(object : SmAliYunVideoControl.OnPlayCallback {
            override fun onPrepared() {

            }

            override fun onStart() {
                val lp = mCollapsingToolbarLayout.layoutParams as AppBarLayout.LayoutParams
                lp.scrollFlags = 0
                mCollapsingToolbarLayout.layoutParams = lp
            }

            override fun onError(what: Int, extra: Int, message: String?) {
            }

            override fun onCompleted() {
                val lp = mCollapsingToolbarLayout.layoutParams as AppBarLayout.LayoutParams
                lp.scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
                mCollapsingToolbarLayout.layoutParams = lp
            }

            override fun onPause() {
                val lp = mCollapsingToolbarLayout.layoutParams as AppBarLayout.LayoutParams
                lp.scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
                mCollapsingToolbarLayout.layoutParams = lp
            }

            override fun onStop() {
            }
        })
        mVideoControl.setPlayer(mAliyunPlayer, url)
        mVideoControl.startButton.post {
            if (AppUtil.getNetworkInfoType(this) == ConnectivityManager.TYPE_WIFI && mUserSetting?.wifiAutoPlay ?: 0 == 0) {
                mVideoControl.startButton.performClick()
            }
        }
    }

    override fun onBackPressed() {
        if (!mVideoControl.backPress()) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (weaLock == null) {
            val pManager = getSystemService(POWER_SERVICE) as PowerManager
            weaLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, this.javaClass.name)
        }
        try {
            weaLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        } catch (e: Exception) {
            SmLogger.d(e.localizedMessage)
        }
        SmVideoPlayerManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        try {
            weaLock?.release()
        } catch (e: Exception) {
            SmLogger.e(e.message)
        }
        SmVideoPlayerManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SmVideoPlayerManager.release()
    }


    inner class ItemAdapter : BaseRVAdapter<VideoRecommened>() {
        var mVideo: MainVideo? = null
        val smContextWrap = SmContextWrap.obtain(this@VideoDetailActivity)
        var hasCollected = lazy {
            val d = ContextCompat.getDrawable(this@VideoDetailActivity, R.drawable.icon_main_shouchang_0)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            d
        }
        var unCollected = lazy {
            val d = ContextCompat.getDrawable(this@VideoDetailActivity, R.drawable.icon_main_shouchang)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            d
        }
        var hasZan = lazy {
            val d = ContextCompat.getDrawable(this@VideoDetailActivity, R.drawable.icon_main_zan_0)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            d
        }
        var unZan = lazy {
            val d = ContextCompat.getDrawable(this@VideoDetailActivity, R.drawable.icon_main_zan)
            d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            d
        }

        init {
            unShowFooterView()
            addHeader(R.layout.activity_viceo_detail_header, SparseArrayViewHolder::class.java)
        }

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            val coverIv = holder.getView<ImageView>(R.id.video_detail_item_iv)
            ImageLoader.getInstance().load(smContextWrap, json.coverurl, R.drawable.icon_default_vertical, coverIv)
            holder.setText(R.id.video_detail_item_title, json.videotitle)

            val adTagTv = holder.getView<View>(R.id.video_detail_item_ad)
            val adCountTv = holder.getView<TextView>(R.id.video_detail_item_ad_count)
            val videoCountTv = holder.getView<TextView>(R.id.video_detail_item_count)
            val titleTv = holder.getView<TextView>(R.id.video_detail_item_title)

            val cardView = holder.getView<CardView>(R.id.video_detail_item_card)
            val lp = cardView.layoutParams as ConstraintLayout.LayoutParams
            if (json.isadv == 1) {
                lp.dimensionRatio = "12:5"
                //广告条目
                titleTv.setLines(1)
                adTagTv.visibility = View.VISIBLE
                adCountTv.visibility = View.VISIBLE
                videoCountTv.visibility = View.GONE
                adCountTv.text = "${json.clicks}人已测"
            } else {
                lp.dimensionRatio = "5:3"
                titleTv.setLines(2)
                adTagTv.visibility = View.GONE
                adCountTv.visibility = View.GONE
                videoCountTv.visibility = View.VISIBLE
                videoCountTv.text = "${json.clicks}次播放"
            }
            cardView.layoutParams = lp

            holder.itemView.setOnClickListener {
                if (json.isadv == 1) {
                    ActivityRouter.get().startActivity(holder.itemView.context, json.videourl)
                } else {
                    //前往视频详情
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.VideoView.VID] = "${json.vid}"
                    ActivityRouter.get().startActivity(it.context, RouterUrl.ACTIVITY_VIDEO_DETAIL, map, false, 0)
                }
            }
        }

        override fun getItemLayoutIds(viewType: Int): Int = R.layout.activity_viceo_detail_item

        var priseMap = mutableMapOf<Int, Boolean>()
        var collectionMap = mutableMapOf<Int, Boolean>()

        override fun onBindHeadViewHolder(holder: SparseArrayViewHolder, position: Int) {
            if (position == 0) {
                val subFormat = "%d次播放\t\t\t\t发布时间：${mVideo?.timestr ?: ""}"
                holder.setText(R.id.video_detail_item_title, mVideo?.videotitle ?: "")
                        .setText(R.id.video_detail_item_share, "${mVideo?.sharenum ?: 0}")
                        .setText(R.id.video_detail_item_play_count,
                                String.format(Locale.getDefault(), subFormat,
                                        mVideo?.clicks ?: 0, 0, 0, 0))
                holder.setOnClickListener(R.id.video_detail_item_share) {
                    //分享
                    val smShare = ThirdShareManager.getDefaultShare(it.context)
                    val dialog = SocialShareDialog(this@VideoDetailActivity)
                    smShare.title = mData?.videotitle ?: ""
                    smShare.content = mData?.videotitle ?: ""
                    smShare.imgurl = mData?.videourl ?: ""
                    if (!TextUtils.isEmpty(mData?.shareurl)) {
                        smShare.url = mData?.shareurl
                    }
                    smShare.kind = 0
                    smShare.qid = mData?.vid ?: 0
                    dialog.smShare = smShare
                    dialog.show()
                }
                //收藏示意
                val collectTv = holder.getView<TextView>(R.id.video_detail_item_shoucang)
                collectTv.setCompoundDrawables(null, if (mVideo?.iscollection ?: 0 == 0) unCollected.value else hasCollected.value, null, null)
                if (collectionMap[position] == true) {
                    collectionMap[position] = false
                    SmAnimationUtil.scale(collectTv, 0.8f, true)
                }
                collectTv.setOnClickListener {
                    //收藏
                    if (mVideo?.iscollection == 0) {
                        UserDataManager.getInstance().addCollection(this@VideoDetailActivity, mVideo?.vid
                                ?: 0, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        mVideo?.let {
                                            it.iscollection = 1
                                        }
                                        collectionMap[position] = true
                                        notifyItemChanged(position)
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    } else {
                        UserDataManager.getInstance().delCollection(this@VideoDetailActivity, mVideo?.vid
                                ?: 0, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        mVideo?.let {
                                            it.iscollection = 0
                                        }
                                        collectionMap[position] = true
                                        notifyItemChanged(position)
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    }

                }
                //点赞
                val zanTv = holder.getView<TextView>(R.id.video_detail_item_zan)
                zanTv.text = "${mVideo?.praise ?: 0}"
                zanTv.setCompoundDrawables(if (mVideo?.ispraise ?: 0 == 1) hasZan.value else unZan.value, null, null, null)
                if (priseMap[position] == true) {
                    priseMap[position] = false
                    SmAnimationUtil.scale(zanTv, 0.8f, true)
                }
                zanTv.setOnClickListener {
                    //点赞
                    if (mVideo?.ispraise == 0) {
                        UserDataManager.getInstance().addPraise(this@VideoDetailActivity, mVideo?.vid
                                ?: 0, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        mVideo?.let {
                                            it.praise++
                                            it.ispraise = 1
                                        }
                                        priseMap[position] = true
                                        notifyItemChanged(position)
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    } else {
                        UserDataManager.getInstance().delPraise(this@VideoDetailActivity, mVideo?.vid
                                ?: 0, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        mVideo?.let {
                                            it.praise--
                                            it.ispraise = 0
                                        }
                                        priseMap[position] = true
                                        notifyItemChanged(position)
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    }
                }
            }
        }
    }
}