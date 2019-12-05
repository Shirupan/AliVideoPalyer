package com.xx.module.video.view.shortvideo

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.MainVideo
import com.xx.lib.db.entity.SmContextWrap
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.SimpleListFragment
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.IBaseAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener
import com.xx.module.video.R
import com.xx.module.video.presenter.MainShortVideoListViewPresenter
import com.xx.module.video.view.constract.IMainShortVideoListView

@Presenter(MainShortVideoListViewPresenter::class)
class ShortVideoMainListFragment : SimpleListFragment<MainShortVideoListViewPresenter>(), IMainShortVideoListView {
    /**
     * 小视频的分类为0
     */
    private val shortVideoType = 0

    private val dataAdapter = lazy { DataAdapter() }


    override fun getLayoutID(): Int = R.layout.fragment_main_video_list


    override fun initBeforeLoadData() {
        initRecyclerViewOrListView(onCreateListAdapterListener)
        setCutOutAndStatusMaxHeightToView(getRootView().findViewById(R.id.main_status_bar_1))
        setPtrFrameLayout(getRootView().findViewById(R.id.refresh_layout))
    }


    override fun loadDataFromCacheAndNet() {
        presenter?.getCache(shortVideoType, startingPageNum)
    }

    override fun loadData(page: Int) {
        //加载数据
        val token = loginUser?.token ?: ""
        presenter?.loadMainVideoList(page, shortVideoType, token)
    }

    override fun getListCache(list: List<MainVideo>?) {
        if (list != null) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            dataAdapter.value.clearData()
            dataAdapter.value.addDataList(list)
            loadData(startingPageNum)
            loadingViewManager?.dismiss()
        }
        recyclerView.post { refreshData(true) }
    }

    override fun onMainListResult(list: List<MainVideo>, page: Int) {
        val adapter = if (recyclerView.adapter == null) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            dataAdapter.value
        } else {
            recyclerView.adapter as DataAdapter
        }
        if (page == startingPageNum) {
            adapter.clearData()
        }
        adapter.addDataList(list)
        loadingViewManager?.dismiss()
    }

    private val onCreateListAdapterListener = object : SimpleOnCreateListAdapterListener() {
        override fun onCreateRecyclerViewAdapter(): IBaseAdapter<out IBaseAdapter<*, *>, *> {
            dataAdapter.value.setShowLoadingView(true)
            dataAdapter.value.unShowFooterView()
            return dataAdapter.value
        }
    }

    inner class DataAdapter : BaseRVAdapter<MainVideo>() {
        init {
            setShowLoadingView(false)
            unShowFooterView()
        }

        private val smContextWrap = SmContextWrap.obtain(this@ShortVideoMainListFragment)

        override fun getLayoutManager(context: Context?): RecyclerView.LayoutManager {
            return StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        }

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            holder.setText(R.id.main_video_list_title, json.videotitle)
                    .setText(R.id.main_video_list_zan, "${json.praise}")
                    .setText(R.id.main_video_list_look, "${json.clicks}")
            //高度设置
            val cardView = holder.getView<FrameLayout>(R.id.main_video_list_image_layout)
            val lp = cardView.layoutParams as ConstraintLayout.LayoutParams
            if (dataPosition == 1) {
                lp.dimensionRatio = "3:2"
            } else {
                lp.dimensionRatio = "9:16"
            }
            cardView.layoutParams = lp

            //图片
            val iv = holder.getView<ImageView>(R.id.main_video_list_image)
            iv.setBackgroundResource(0)
            val image = if (!TextUtils.isEmpty(json.imgurl2)) json.imgurl2 else json.coverurl
            ImageLoader.getInstance().load(smContextWrap, image, R.drawable.icon_default_vertical, iv)
            if (json.isadv == 1) {
                //广告条目
                holder.getView<View>(R.id.main_video_list_play).visibility = View.GONE
                holder.getView<View>(R.id.main_video_list_zan).visibility = View.GONE
                holder.getView<View>(R.id.main_video_list_ad).visibility = View.VISIBLE
            } else {
                holder.getView<View>(R.id.main_video_list_play).visibility = View.VISIBLE
                holder.getView<View>(R.id.main_video_list_zan).visibility = View.VISIBLE
                holder.getView<View>(R.id.main_video_list_ad).visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                //小视频播放列表
                if (json.isadv == 1) {
                    ActivityRouter.get().startActivity(holder.itemView.context, json.videourl)
                } else {
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.VideoView.VIDEO_LIST] = GsonSingleton.getInstance().toJson(json)
                    ActivityRouter.get().startActivity(it.context, RouterUrl.ACTIVITY_SHORT_VIDEO_MAIN, map, false, 0)
                }
            }
        }


        override fun getItemLayoutIds(viewType: Int): Int = R.layout.fragment_main_video_list_item
    }
}