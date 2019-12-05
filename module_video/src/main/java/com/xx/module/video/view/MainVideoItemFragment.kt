package com.xx.module.video.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.TextView
import com.mrkj.lib.net.tool.ExceptionUtl
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.UserSystem
import com.xx.lib.db.entity.MainVideo
import com.xx.lib.db.entity.SmContextWrap
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.SimpleListFragment
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.IBaseAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener
import com.xx.module.common.view.widget.CommonUISetUtil
import com.xx.module.common.view.widget.SmAnimationUtil
import com.xx.module.video.R
import com.xx.module.video.presenter.MainItemViewPresenter
import com.xx.module.video.view.constract.IMainActivityView
import com.xx.module.video.view.constract.IMainItemView

/**
 *@author someone
 *@date 2019-05-30
 */
@Presenter(MainItemViewPresenter::class)
class MainVideoItemFragment : SimpleListFragment<MainItemViewPresenter>(), IMainItemView {

    private var mType = 0
    private val mAdapter = lazy { DataAdapter() }

    private val mHandler = object : android.os.Handler() {}

    companion object {
        const val TID = "tid"
        fun getInstance(tid: Int): Fragment {
            val fragment = MainVideoItemFragment()
            val bundle = Bundle()
            bundle.putInt(TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val onCreateListAdapterListener = object : SimpleOnCreateListAdapterListener() {
        override fun onCreateRecyclerViewAdapter(): IBaseAdapter<out IBaseAdapter<*, *>, *> {
            mAdapter.value.setShowLoadingView(false)
            mAdapter.value.clearData()
            return mAdapter.value
        }
    }

    override fun beforeSetContentView() {
        setIsLazyFragmentMode(true)
    }

    override fun loadDataFromCacheAndNet() {
        CommonUISetUtil.closeDefaultAnimator(recyclerView)
        mType = arguments?.getInt(TID) ?: -1
        presenter.getCache(mType,startingPageNum)
    }

    override fun loadData(page: Int) {
        getLoginUser(object : UserDataManager.OnGetUserDataListener {
            override fun onSuccess(us: UserSystem) {
                presenter.loadMainVideoList(page, mType, us.token)
            }

            override fun onFailed(e: Throwable?) {
                presenter.loadMainVideoList(page, mType, "")
            }
        })
    }

    override fun onGetDataListFailed(e: Throwable?) {
        super.onGetDataListFailed(e)
        if (nowPage == startingPageNum && mAdapter.value.data.isEmpty()) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            //空数据显示空数据页面
            mAdapter.value.setShowLoadingView(true)
            mAdapter.value.setLoadingViewEmptyClickListener {
                recyclerView.adapter = null
                refreshData(true)
                //   mAdapter.value.notifyDataSetChanged()
            }
            mAdapter.value.notifyLoadingViewItemViewStateChanged(ExceptionUtl.catchTheError(e), true)
        }
    }

    inner class DataAdapter : BaseRVAdapter<MainVideo>() {
        val smContextWrap = SmContextWrap.obtain(this@MainVideoItemFragment)
        var priseMap = mutableMapOf<Int, Boolean>()
        var collectionMap = mutableMapOf<Int, Boolean>()
        val nomalZan = lazy {
            val d = ContextCompat.getDrawable(context!!, R.drawable.icon_main_zan)
            d?.setBounds(0, 0, d.intrinsicHeight, d.intrinsicWidth)
            d
        }
        val hasZan = lazy {
            val d = ContextCompat.getDrawable(context!!, R.drawable.icon_main_zan_0)
            d?.setBounds(0, 0, d.intrinsicHeight, d.intrinsicWidth)
            d
        }

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            if (json.isadv == 1) {
                //广告条目
                val savorIv = holder.getView<ImageView>(R.id.main_item_ad_iv)
                ImageLoader.getInstance().load(smContextWrap, json.coverurl, R.drawable.icon_default_vertical, savorIv)
                holder.setText(R.id.main_item_ad_title, json.videotitle)
                holder.itemView.setOnClickListener {
                    ActivityRouter.get().startActivity(holder.itemView.context, json.videourl)
                }
            } else {
                val savorIv = holder.getView<ImageView>(R.id.main_item_video_savor)
                ImageLoader.getInstance().load(smContextWrap, json.coverurl, R.drawable.icon_default_vertical, savorIv)
                holder.setText(R.id.main_item_video_title, json.videotitle)  //标题
                        .setText(R.id.main_item_video_time, json.videolength)
                        .setText(R.id.main_item_video_look, "${json.clicks}")  //观看数
                        .setText(R.id.main_item_video_share, "${json.sharenum}")  //分享书
                //点赞数
                val zanTv = holder.getView<TextView>(R.id.main_item_video_zan)
                zanTv.setCompoundDrawables(if (json.ispraise == 1) hasZan.value else nomalZan.value, null, null, null)
                zanTv.text = "${json.praise}"
                if (priseMap[dataPosition] == true) {
                    SmAnimationUtil.scale(zanTv, 0.8f, true)
                    priseMap[dataPosition] = false
                }
                zanTv.setOnClickListener {
                    //点赞 取消点赞
                    if (json.ispraise == 1) {
                        UserDataManager.getInstance().delPraise(this@MainVideoItemFragment, json.vid, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        json.ispraise = 0
                                        json.praise--
                                        notifyItemChanged(dataPosition)
                                        priseMap[dataPosition] = true
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    } else {
                        UserDataManager.getInstance().addPraise(this@MainVideoItemFragment, json.vid, 0,
                                object : UserDataManager.PraiseZanCallback {
                                    override fun onSuccess() {
                                        json.ispraise = 1
                                        json.praise++
                                        notifyItemChanged(dataPosition)
                                        priseMap[dataPosition] = true
                                    }

                                    override fun onError(e: SmError?) {
                                    }
                                })
                    }
                }

                val collectIv = holder.getView<ImageView>(R.id.main_item_collect)
                collectIv.setImageResource(if (json.iscollection == 1) R.drawable.icon_main_shouchang_0 else R.drawable.icon_main_shouchang)
                if (collectionMap[dataPosition] == true) {
                    SmAnimationUtil.scale(collectIv, 0.8f, true)
                    collectionMap[dataPosition] = false
                }
                collectIv.setOnClickListener {
                    //收藏 取消
                    if (json.iscollection == 0) {
                        UserDataManager.getInstance().addCollection(this@MainVideoItemFragment, json.vid, 0, object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                json.iscollection = 1
                                notifyItemChanged(dataPosition)
                                collectionMap[dataPosition] = true
                            }

                            override fun onError(e: SmError?) {
                            }
                        })
                    } else {
                        UserDataManager.getInstance().delCollection(this@MainVideoItemFragment, json.vid, 0, object : UserDataManager.PraiseZanCallback {
                            override fun onSuccess() {
                                json.iscollection = 0
                                notifyItemChanged(dataPosition)
                                collectionMap[dataPosition] = true
                            }

                            override fun onError(e: SmError?) {
                            }
                        })
                    }
                }

                holder.setOnClickListener(R.id.main_item_more) {
                    //更多
                }

                holder.itemView.setOnClickListener {
                    //前往视频详情
                    val jsonStr = GsonSingleton.getInstance().toJson(json)
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.VideoView.DATA] = jsonStr
                    ActivityRouter.get().startActivity(it.context, RouterUrl.ACTIVITY_VIDEO_DETAIL, map, false, 0)
                }
            }
        }

        override fun getItemLayoutIds(viewType: Int): Int {
            return if (viewType == 2) R.layout.fragment_main_item else R.layout.fragment_item_ad
        }

        override fun getRealItemType(positionWithHeader: Int): Int {
            return if (data[positionWithHeader].isadv == 1) {
                //广告item
                1
            } else {
                //普通视频item
                2
            }

        }
    }

    /**
     * 先去获取缓存
     */
    override fun getListCache(t: List<MainVideo>?) {
        if (t != null) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
        }
        mAdapter.value.addDataList(t)
        mHandler.post {
            setAppBarLayout((activity as IMainActivityView).mainViewAppbarLayout)
            refreshData(true)
        }
    }


    override fun onMainListResult(list: List<MainVideo>, page: Int) {
        if (page == startingPageNum) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            mAdapter.value.clearData()
        }
        mAdapter.value.addDataList(list)
    }
}