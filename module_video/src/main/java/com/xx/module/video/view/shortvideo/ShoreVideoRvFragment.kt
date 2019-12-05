package com.xx.module.video.view.shortvideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.alivc.player.AliyunErrorCode
import com.aliyun.vodplayer.media.AliyunLocalSource
import com.aliyun.vodplayer.media.AliyunVodPlayer
import com.aliyun.vodplayer.media.IAliyunVodPlayer
import com.google.gson.reflect.TypeToken
import com.mrkj.lib.common.util.*
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.UserSystem
import com.xx.lib.db.entity.MainVideo
import com.xx.lib.db.entity.SmContextWrap
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.view.base.BaseListFragment
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.IBaseAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener
import com.xx.module.common.view.widget.CommonUISetUtil
import com.xx.module.common.view.widget.SmAnimationUtil
import com.xx.module.video.R
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.model.SmMediaCacheManager
import com.xx.module.video.presenter.ShortVideoListViewPresenter
import com.xx.module.video.view.constract.IShortVideoListView
import com.xx.module.video.view.shortvideo.tipsview.TipsView


@Presenter(ShortVideoListViewPresenter::class)
class ShoreVideoRvFragment : BaseListFragment<ShortVideoListViewPresenter>(), IShortVideoListView {


    var mUser: UserSystem? = null
    var isRefreshing = false
    var pagerSnapHelper: PagerSnapHelper? = null

    //网络状态监听
    private var mNetWatchdog: NetWatchdog? = null
    private var mOnAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private var isFromRefresh = false

    override fun getLayoutID(): Int = R.layout.fragment_shore_video_detail_list
    override fun getRecyclerView(): RecyclerView = listRv!!

    companion object {
        fun getInstance(type: Int): ShoreVideoRvFragment {
            val bundle = Bundle()
            bundle.putInt(RouterParams.VideoView.VIEW_TYPE, type)
            val f = ShoreVideoRvFragment()
            f.arguments = bundle
            return f
        }
    }

    override fun beforeSetContentView() {
        setIsLazyFragmentMode(true)
    }

    private var listRv: RecyclerView? = null
    private var loadingIv: ImageView? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var mProgressBar: ProgressBar? = null

    private val myHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            val what = msg?.what
            if (what == 0) {
                val index = msg.arg1
                if (index > -1) {
                    mAdapter.currentIndex = index
                    var last = index - 1
                    val next = index + 1
                    if (last < 0) {
                        last = 0
                    }
                    mProgressBar?.progress = 0
                    mAdapter.notifyItemRangeChanged(last, next)
                }
            }
        }
    }

    override fun initViewsAndEvents(rootView: View?) {
        listRv = rootView?.findViewById(R.id.short_video_rv)
        listRv?.post { listRv?.requestFocus() }
        loadingIv = rootView?.findViewById(R.id.short_video_loading_iv)
        refreshLayout = rootView?.findViewById(R.id.short_video_refresh)
        mProgressBar = rootView?.findViewById(R.id.short_video_progress)

        initNetWatchdog()
        pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper?.attachToRecyclerView(listRv)
        listRv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滑动停止的情况下，设置播放停止
                    if (mAdapter.data.size > 0 && listRv?.layoutManager is LinearLayoutManager) {
                        val lm = listRv?.layoutManager as LinearLayoutManager
                        val index = lm.findFirstCompletelyVisibleItemPosition()
                        if (mAdapter.currentIndex == index) {
                            return
                        }
                        val message = Message.obtain(myHandler)
                        message.what = 0
                        message.arg1 = index
                        myHandler.removeCallbacksAndMessages(0)
                        myHandler.sendMessageDelayed(message, 100)
                    }
                }
            }
        })
        CommonUISetUtil.closeDefaultAnimator(recyclerView)
    }


    override fun onFirstUserVisible() {
        getLoginUser(object : UserDataManager.OnGetUserDataListener {
            override fun onSuccess(us: UserSystem?) {
                mUser = us
                loadDataFromCacheAndNet()
            }

            override fun onFailed(e: Throwable?) {
                loadDataFromCacheAndNet()
            }
        })
    }

    private fun loadDataFromCacheAndNet() {
        CommonUISetUtil.initSwipeRefreshLayout(refreshLayout!!) {
            if (isRefreshing) {
                return@initSwipeRefreshLayout
            }
            refreshData()
        }
        //进入Activity时候可能携带了数据
        val json = activity?.intent?.getStringExtra(RouterParams.VideoView.VIDEO_LIST)
        val list = GsonSingleton.getInstance().fromJson<List<MainVideo>>(json, object : TypeToken<List<MainVideo>>() {}.type)
        val position = StringUtil.integerValueOf(activity?.intent?.getStringExtra(RouterParams.VideoView.VIDEO_LIST_POSITION), 0)
        var resultList: MutableList<MainVideo>? = null
        if (list == null || list.isEmpty()) {
            val single = GsonSingleton.getInstance().fromJson<MainVideo>(json, MainVideo::class.java)
            if (single != null) {
                resultList = mutableListOf()
                resultList.add(single)
            }
        } else {
            resultList = list.toMutableList()
        }
        if (resultList == null || resultList.isEmpty()) {
            showLoading()
            isFromRefresh = true
            refreshData()
        } else {
            loadingIv?.visibility = View.GONE
            initRecyclerViewOrListView(onCreateListAdapterListener)
            mAdapter.clearData()
            mAdapter.addDataList(resultList)
            isFromRefresh = false
            if (position >= 0 && position < mAdapter.data.size) {
                mAdapter.currentIndex = position
                listRv?.scrollToPosition(position)
            }
        }
    }

    private fun showLoading() {
        loadingIv?.visibility = View.VISIBLE
        loadingIv?.post {
            if (loadingIv?.drawable is AnimationDrawable) {
                val d = loadingIv?.drawable as AnimationDrawable
                d.isOneShot = false
                d.start()
            }
        }
    }


    override fun loadData(page: Int) {
        presenter?.loadMainVideoList(page, 0, loginUser?.token ?: "")
    }

    override fun onMainListResult(list: MutableList<out MainVideo>, page: Int) {
        if (isFromRefresh && page == startingPageNum) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            mAdapter.clearData()
        }
        mAdapter.addDataList(list)
        isFromRefresh = false
    }

    /**
     * 初始化网络监听
     */
    private fun initNetWatchdog() {
        mNetWatchdog = NetWatchdog(context)
        mNetWatchdog?.setNetChangeListener(object : NetWatchdog.NetChangeListener {
            override fun onWifiTo4G() {
                if (mAdapter.data.isEmpty()) {
                    return
                }
                val json = mAdapter.data[mAdapter.currentIndex]
                //如果已经显示错误了，那么就不用显示网络变化的提示了。
                if (json?.errorCode != -1) {
                    return
                }
                //wifi变成4G，先暂停播放
                //显示网络变化的提示
                mAdapter.netChange(false)
            }

            override fun on4GToWifi() {
                mAdapter.netChange(true)
            }

            override fun onNetDisconnected() {
                //网络断开。
                // NOTE： 由于安卓这块网络切换的时候，有时候也会先报断开。所以这个回调是不准确的。
            }

        })
        mNetWatchdog?.startWatch()
    }

    override fun onPause() {
        super.onPause()
        mAdapter.pause()
    }

    override fun onResume() {
        super.onResume()
        mAdapter.resume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter.currentPlayer?.stop()
        mAdapter.currentPlayer?.release()
        mAdapter.progressUpdateTimer.removeCallbacksAndMessages(0)
        mNetWatchdog?.stopWatch()
        mNetWatchdog = null
        mOnAudioFocusChangeListener?.let {
            ModuleManager.of(VideoModuleClient::class.java).unRegisterAudioFocus(context, it)
        }
    }


    override fun onUserInvisible() {
        onFirstUserVisible()
    }

    override fun onFirstUserInvisible() {
        val index = mAdapter.currentIndex
        mAdapter.currentPlayer?.stop()
        mAdapter.currentIndex = -1
        mAdapter.notifyItemChanged(index)
    }

    override fun onLoadDataCompleted() {
        super.onLoadDataCompleted()
        refreshLayout?.isRefreshing = false
    }

    private val mAdapter = DataAdapter()
    private val onCreateListAdapterListener = object : SimpleOnCreateListAdapterListener() {
        override fun onCreateRecyclerViewAdapter(): IBaseAdapter<out IBaseAdapter<*, *>, *> {
            mAdapter.setShowLoadingView(false)
            if (AppUtil.getNetworkInfoType(context) != ConnectivityManager.TYPE_WIFI) {
                mAdapter.isNotWifi = true
            }
            mAdapter.unShowFooterView()

            return mAdapter
        }
    }


    inner class DataAdapter : BaseRVAdapter<MainVideo>() {
        var isNotWifi = false
        var currentIndex = 0
        var currentPlayer: AliyunVodPlayer? = null
        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            initData(holder, json)
            initTipsView(holder, json)
            initCoverView(holder, json)
            if (json.isadv != 1) {
                setupSurfaceViewSize(holder, 0, 0)
                //只有当前播放的item初始化播放器
                if (currentIndex == dataPosition) {
                    createPlayerAndPlay(holder, json, dataPosition)
                }
                val videoContainer = holder.getView<FrameLayout>(R.id.short_video_surfaceView_container)
                videoContainer.setOnClickListener {
                    currentIndex = dataPosition
                    if (currentPlayer?.isPlaying == true) {
                        //isPlaying 包括了开始和暂停
                        onPlayOrPause(currentPlayer!!, holder, json, dataPosition)
                    } else {
                        createPlayerAndPlay(holder, json, dataPosition)
                    }
                }
                holder.getView<View>(R.id.short_video_play).visibility = View.VISIBLE
            } else {
                holder.getView<View>(R.id.short_video_play).visibility = View.GONE
            }
        }

        private fun createPlayerAndPlay(holder: SparseArrayViewHolder, json: MainVideo, dataPosition: Int) {
            currentPlayer?.stop()
            currentPlayer?.release()
            val p = AliyunVodPlayer(holder.itemView.context)
            currentPlayer = p

            //只有当前激活对象会有Player对象
            currentPlayer?.let {
                initAliVcPlayer(holder, it, json, dataPosition)
                initSurfaceView(holder, it, json, dataPosition)
            }
        }


        override fun getItemLayoutIds(viewType: Int): Int = R.layout.fragment_short_video_item
        /**
         * 初始化封面
         */
        private fun initCoverView(holder: SparseArrayViewHolder, json: MainVideo) {
            val coverview = holder.getView<ImageView>(R.id.short_video_cover)
            coverview?.visibility = View.VISIBLE
            ImageLoader.getInstance().load(SmContextWrap.obtain(this@ShoreVideoRvFragment),
                    json.coverurl, R.drawable.icon_default_round, coverview)
        }


        private fun initData(holder: SparseArrayViewHolder, json: MainVideo) {
            //标题
            val contentTv = holder.getView<TextView>(R.id.short_video_content)
            contentTv?.text = json.videotitle
            //按钮
            val btn = holder.getView<TextView>(R.id.short_video_btn)
            if (json.isadv != 1) {
                btn?.visibility = View.GONE
            } else {
                btn?.visibility = View.VISIBLE
                val onClickListener = View.OnClickListener {
                    ActivityRouter.get().startWebActivity(it.context, json.videourl, "")
                }
                btn?.setOnClickListener(onClickListener)
                holder.getView<ImageView>(R.id.short_video_cover).setOnClickListener(onClickListener)
            }
            //称赞按钮
            val czTv = holder.getView<TextView>(R.id.short_video_menu_zan)
            if (json.isadv == 1) {
                czTv.visibility = View.GONE
            } else {
                czTv.visibility = View.VISIBLE
                setupCzBtn(czTv, json, false)
            }

            //分享数
            val shareTv = holder.getView<TextView>(R.id.short_video_menu_share)
            shareTv?.text = "${json.sharenum}"
        }

        /**
         * 设置称赞按钮
         */
        private fun setupCzBtn(czTv: TextView, json: MainVideo, anim: Boolean) {
            czTv.text = "${json.praise}"
            if (json.ispraise == 1) {
                val d = ColorUtils.setTintColorRes(context!!, R.drawable.xsp_dianzan, R.color.text_red)
                d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                czTv.setCompoundDrawables(null, d, null, null)
            } else {
                val d = ContextCompat.getDrawable(context!!, R.drawable.xsp_dianzan)
                d?.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                czTv.setCompoundDrawables(null, d, null, null)
            }
            if (anim) {
                SmAnimationUtil.scale(czTv, 0.8f, true)
            }
            czTv.setOnClickListener {
                if (json.ispraise == 1) {
                    UserDataManager.getInstance().delPraise(this@ShoreVideoRvFragment, json.vid, 0,
                            object : UserDataManager.PraiseZanCallback {
                                override fun onSuccess() {
                                    json.ispraise = 0
                                    json.praise--
                                    setupCzBtn(czTv, json, true)
                                }

                                override fun onError(e: SmError?) {
                                }

                            })
                } else {
                    UserDataManager.getInstance().addPraise(this@ShoreVideoRvFragment, json.vid, 0,
                            object : UserDataManager.PraiseZanCallback {
                                override fun onSuccess() {
                                    json.ispraise = 1
                                    json.praise++
                                    setupCzBtn(czTv, json, true)
                                }

                                override fun onError(e: SmError?) {
                                }

                            })
                }
            }
        }


        private fun initSurfaceView(holder: SparseArrayViewHolder, player: AliyunVodPlayer, json: MainVideo, dataPosition: Int) {
            val contaner = holder.getView<FrameLayout>(R.id.short_video_surfaceView_container)
            contaner?.removeAllViews()
            val mTextureView = TextureView(holder.itemView.context)
            mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    player.setSurface(Surface(surface))
                    //如果是当前的item，才播放
                    if (currentIndex == dataPosition) {
                        val tipsView = getTipViewFromHolderTag(holder)
                        if (tipsView?.isErrorShow == true) {
                            return
                        }
                        var hasShown = true
                        if (activity is ShortVideoMainActivity) {
                            hasShown = (activity as ShortVideoMainActivity).hasEvenShowWifiDialog
                        }
                        if (isNotWifi && !hasShown) {
                            tipsView?.showNetChangeTipView()
                        } else {
                            start(player, holder, json)
                        }
                    } else {
                        val statusIv = holder.getView<ImageView>(R.id.short_video_play)
                        statusIv.visibility = View.GONE
                    }
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    val mCoverView = holder.getView<ImageView>(R.id.short_video_cover)
                    mCoverView?.visibility = View.VISIBLE
                    return true
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                    player.surfaceChanged()
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                    player.setSurface(Surface(surface))
                }
            }
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            contaner.addView(mTextureView, params)
        }

        /**
         * 点击暂停或者播放
         */
        private fun onPlayOrPause(player: AliyunVodPlayer, holder: SparseArrayViewHolder, json: MainVideo, position: Int) {
            when {
                player.playerState == IAliyunVodPlayer.PlayerState.Started -> {
                    val mPlayStateIv = holder.getView<ImageView>(R.id.short_video_play)
                    mPlayStateIv.visibility = View.VISIBLE
                    stopProgressUpdateTimer(holder, json)
                    player.pause()
                }
                else -> {
                    currentIndex = position
                    start(player, holder, json)
                }
            }
        }

        /**
         * 初始化提示view
         */
        private fun initTipsView(holder: SparseArrayViewHolder, json: MainVideo) {
            val tipsView = getTipViewFromHolderTag(holder)
            tipsView?.hideBufferLoadingTipView()
            //设置tip中的点击监听事件
            tipsView?.setOnTipClickListener(object : TipsView.OnTipClickListener {
                override fun onContinuePlay() {
                    //继续播放。如果没有prepare或者stop了，需要重新prepare
                    tipsView.hideAll()
                    if (currentPlayer?.playerState == IAliyunVodPlayer.PlayerState.Idle
                            || currentPlayer?.playerState == IAliyunVodPlayer.PlayerState.Stopped) {
                        val source = AliyunLocalSource.AliyunLocalSourceBuilder()
                        source.setSource(json.videourl)
                        currentPlayer?.prepareAsync(source.build())
                    } else {
                        start(currentPlayer, holder, json)
                    }
                }

                override fun onStopPlay() {
                    // 结束播放
                    tipsView.hideAll()
                    currentPlayer?.stop()
                }

                override fun onRetryPlay() {
                    //重试
                    start(currentPlayer, holder, json)
                }

                override fun onReplay() {
                    //重播
                    onRetryPlay()
                }
            })

            tipsView?.setOnNetChangeClickListener(object : TipsView.OnNetChangeClickListener {
                override fun onContinuePlay() {
                    if (activity is ShortVideoMainActivity) {
                        (activity as ShortVideoMainActivity).hasEvenShowWifiDialog = true
                    }
                    start(currentPlayer, holder, json)
                }

                override fun onStopPlay() {
                    tipsView.hideAll()
                    currentPlayer?.stop()
                    activity?.finish()
                }
            })
            currentPlayer?.stop()
            if (json.errorCode != -1) {
                tipsView?.showErrorTipView(json.errorCode, 0, json.errorMsg)
            } else if (!isNotWifi) {
                //关闭网络切换提醒
                tipsView?.hideNetErrorTipView()
            }
        }

        /**
         * 创建提示布局，并添加到item中
         */
        private fun getTipViewFromHolderTag(holder: SparseArrayViewHolder): TipsView? {
            val holderView = holder.getView<View>(R.id.short_video_surfaceView_container)
            return if (holderView.tag is TipsView) {
                holderView.tag as TipsView
            } else {
                val d = TipsView(holderView.context)
                val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                (holder.itemView as ViewGroup).addView(d, params)//添加到布局中
                holderView.tag = d
                d
            }
        }

        /**
         * 初始化播放器
         */
        private fun initAliVcPlayer(holder: SparseArrayViewHolder, player: AliyunVodPlayer, json: MainVideo, position: Int) {
            if (currentIndex != position) {
                //不是当前视频不做初始化
                return
            }
            player.stop()
            player.reset()
            SmMediaCacheManager.setupAliyunVideoCachePath(holder.itemView.context, player)
            player.setAutoPlay(false)
            //设置准备回调
            player.setOnPreparedListener {
                if (player.videoWidth < player.videoHeight) {
                    player.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                } else {
                    player.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                }
                setupSurfaceViewSize(holder, player.videoWidth, player.videoHeight)
                val tipsView = getTipViewFromHolderTag(holder)
                if (tipsView?.isErrorShow == true) {
                    return@setOnPreparedListener
                }
                player.setMaxBufferDuration(player.duration.toInt())
                if (currentIndex == position && userVisibleHint
                        && player.playerState == IAliyunVodPlayer.PlayerState.Prepared) {
                    start(player, holder, json)
                }
            }
            //播放器出错监听
            player.setOnErrorListener { errorCode, errorEvent, errorMsg ->
                var error = errorCode
                var eMsg = errorMsg
                if (errorCode == AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE.code) {
                    //当播放本地报错4003的时候，可能是文件地址不对，也有可能是没有权限。
                    //如果是没有权限导致的，就做一个权限的错误提示。其他还是正常提示：
                    val storagePermissionRet = ContextCompat.checkSelfPermission(
                            this@ShoreVideoRvFragment.context?.applicationContext!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (storagePermissionRet != PackageManager.PERMISSION_GRANTED) {
                        eMsg = AliyunErrorCode.ALIVC_ERR_NO_STORAGE_PERMISSION.getDescription(context)
                    } else if (!NetWatchdog.hasNet(this@ShoreVideoRvFragment.context)) {
                        //也可能是网络不行
                        error = AliyunErrorCode.ALIVC_ERR_NO_NETWORK.code
                        eMsg = AliyunErrorCode.ALIVC_ERR_NO_NETWORK.getDescription(this@ShoreVideoRvFragment.context)
                    }
                }
                //关闭定时器
                stopProgressUpdateTimer(holder, json)
                val tipsView = getTipViewFromHolderTag(holder)
                tipsView?.hideAll()
                json.errorCode = error
                json.errorMsg = eMsg
                tipsView?.showErrorTipView(error, 0, eMsg)
            }
            player.setOnBufferingUpdateListener {
                mProgressBar?.secondaryProgress = it
            }
            //播放器加载回调
            player.setOnLoadingListener(object : IAliyunVodPlayer.OnLoadingListener {
                override fun onLoadStart() {
                    val tipsView = getTipViewFromHolderTag(holder)
                    tipsView?.showBufferLoadingTipView()
                }

                override fun onLoadEnd() {
                    //进度加载
                    val tipsView = getTipViewFromHolderTag(holder)
                    tipsView?.showBufferLoadingTipView()
                    tipsView?.hideBufferLoadingTipView()
                    tipsView?.hideErrorTipView()
                }

                override fun onLoadProgress(percent: Int) {
                    val tipsView = getTipViewFromHolderTag(holder)
                    tipsView?.updateLoadingPercent(percent)
                }
            })
            //播放结束
            player.setOnCompletionListener {
                prepareVideo(player, holder, json)
            }
            player.setOnBufferingUpdateListener {
                val progressBar = holder.getView<ProgressBar>(R.id.short_video_progress)
                progressBar.secondaryProgress = it * player.duration.toInt()
            }
            //重播监听
            player.setOnRePlayListener {
                //重播、重试成功
                val tipsView = getTipViewFromHolderTag(holder)
                tipsView?.hideAll()
                //开始启动更新进度的定时器
                startProgressUpdateTimer(holder)
            }
            //第一帧显示
            player.setOnFirstFrameStartListener {
                val tipsView = getTipViewFromHolderTag(holder)
                tipsView?.hideBufferLoadingTipView()
                val coverIv = holder.getView<ImageView>(R.id.short_video_cover)
                coverIv.visibility = View.GONE
                // presenter.watchVideo(mUser?.userId ?: 0, json.vid)
                //开始启动更新进度的定时器
                startProgressUpdateTimer(holder)
            }
        }

        /**
         * 开始进度条更新计时器
         */
        private fun startProgressUpdateTimer(holder: SparseArrayViewHolder) {
            progressUpdateTimer.removeMessages(0)
            val playStateIv = holder.getView<ImageView>(R.id.short_video_play)
            playStateIv.visibility = View.GONE
            progressUpdateTimer.sendEmptyMessageDelayed(0, 1000)
        }

        /**
         * 停止进度条更新计时器
         */
        private fun stopProgressUpdateTimer(holder: SparseArrayViewHolder, json: MainVideo) {
            progressUpdateTimer.removeCallbacksAndMessages(0)
            if (json.isadv != 1) {
                val playStateIv = holder.getView<ImageView>(R.id.short_video_play)
                playStateIv.visibility = View.VISIBLE
            }

        }


        /**
         * 进度更新计时器
         */
        val progressUpdateTimer = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                if (mAdapter.currentPlayer?.playerState != IAliyunVodPlayer.PlayerState.Started) {
                    return
                }
                val max = (mAdapter.currentPlayer?.duration)?.toInt()
                mProgressBar?.max = max ?: 0
                val current: Int? = (mAdapter.currentPlayer?.currentPosition)?.toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mProgressBar?.setProgress(current ?: 0, true)
                } else {
                    mProgressBar?.progress = current ?: 0
                }
                sendEmptyMessageDelayed(0, 1000)
            }
        }

        private var screenHeight = 0
        /**
         * 根据视频的宽高设置布局高度
         */
        private fun setupSurfaceViewSize(holder: SparseArrayViewHolder, w: Int, h: Int) {
            val contaner = holder.getView<FrameLayout>(R.id.short_video_surfaceView_container)
            val lp = contaner.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            if (h == 0 || w == 0) {
                contaner.layoutParams = lp
                return
            }
            //现在竖屏视频都做全屏处理CoverCrop
            /*  //只处理竖屏视频
              if (h > w) {
                  lp.width = ScreenUtils.getWidth(context)
                  lp.height = lp.width * h / w
              }*/
            contaner.layoutParams = lp
        }

        /**
         * 开始播放
         */
        fun start(player: AliyunVodPlayer?, holder: SparseArrayViewHolder, json: MainVideo) {
            if (player == null) {
                return
            }
            if (mOnAudioFocusChangeListener == null) {
                mOnAudioFocusChangeListener = ModuleManager.of(VideoModuleClient::class.java)
                        .registerAudioFocus(context) {
                            // mAliyunVodPlayer?.pause()
                        }
            }
            val playerState = player.playerState
            if (playerState == IAliyunVodPlayer.PlayerState.Paused
                    || playerState == IAliyunVodPlayer.PlayerState.Prepared) {
                val url = player.mediaInfo?.videoId
                if (TextUtils.isEmpty(url) || !TextUtils.equals(url, json.videourl)) {
                    player.stop()
                    start(player, holder, json)
                } else {
                    player.start()
                    startProgressUpdateTimer(holder)
                    SmLogger.d("开始播放视频：${json.videourl}\n$holder")
                }
                val playStateIv = holder.getView<ImageView>(R.id.short_video_play)
                playStateIv.visibility = View.GONE
            } else {
                player.stop()
                prepareVideo(player, holder, json)
            }
        }

        /**
         * 设置资源开始播放iniini
         */
        private fun prepareVideo(player: AliyunVodPlayer?, holder: SparseArrayViewHolder, json: MainVideo) {
            val build = AliyunLocalSource.AliyunLocalSourceBuilder()
            build.setCoverPath(json.coverurl)
            build.setTitle(json.videotitle)
            build.setSource(json.videourl)
            holder.itemView.tag = json.videourl
            player?.prepareAsync(build.build())

            //播放按钮状态隐藏
            val playStateIv = holder.getView<ImageView>(R.id.short_video_play)
            playStateIv.visibility = View.GONE
            //显示加载菊花图
            val tipsView = getTipViewFromHolderTag(holder)
            tipsView?.showBufferLoadingTipView()
        }

        fun netChange(isWifi: Boolean) {
            isNotWifi = !isWifi
            notifyItemChanged(currentIndex)
        }

        fun pause() {
            val index = currentIndex
            currentIndex = -1
            currentPlayer?.pause()
            notifyItemChanged(index)
        }

        fun resume() {
            notifyItemChanged(currentIndex)
        }
    }
}