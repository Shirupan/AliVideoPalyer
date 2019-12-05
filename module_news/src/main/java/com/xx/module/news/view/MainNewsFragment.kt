package com.xx.module.news.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.mrkj.base.bindView
import com.xx.lib.db.entity.SmContextWrap
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseListFragment
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.IBaseAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener
import com.xx.module.news.R
import com.xx.lib.db.entity.NewsJson
import com.xx.module.news.presenter.MainNewsPresenter
import com.xx.module.news.view.constract.IMainNewsView

/**
 *@author someone
 *@date 2019-06-12
 */
@Presenter(MainNewsPresenter::class)
class MainNewsFragment : BaseListFragment<MainNewsPresenter>(), IMainNewsView {


    private val listRv by bindView<RecyclerView>(R.id.recycler_view)
    private val mAdapter = lazy { DataAdapter() }


    override fun getLayoutID(): Int = R.layout.fragment_main_news_list

    override fun getRecyclerView(): RecyclerView = listRv


    override fun initViewsAndEvents(rootView: View) {
        setCutOutAndStatusMaxHeightToView(rootView.findViewById(R.id.main_status_bar_1))
        setPtrFrameLayout(rootView.findViewById(R.id.refresh_layout))
        listRv.post {
            presenter?.loadCache(loginUser?.token ?: "",startingPageNum)
        }
    }


    override fun loadData(page: Int) {
        presenter?.loadNewsList(loginUser?.token ?: "", page)
    }

    override fun loadNewsCacheList(list: List<NewsJson>?) {
        if (list != null) {
            initRecyclerViewOrListView(onCreateAdapterListenerAdapter)
            mAdapter.value.data = list
        }
        refreshData()
    }

    override fun onLoadNewsListResult(list: List<NewsJson>, page: Int) {
        if (page == startingPageNum) {
            initRecyclerViewOrListView(onCreateAdapterListenerAdapter)
            mAdapter.value.clearData()
        }
        mAdapter.value.addDataList(list)
    }

    private val onCreateAdapterListenerAdapter = object : SimpleOnCreateListAdapterListener() {
        override fun onCreateRecyclerViewAdapter(): IBaseAdapter<out IBaseAdapter<*, *>, *> {
            mAdapter.value.clearData()
            return mAdapter.value
        }
    }


    inner class DataAdapter : BaseRVAdapter<NewsJson>() {
        init {
            setShowLoadingView(false)
        }

        val smContextWrap = SmContextWrap.obtain(this@MainNewsFragment)

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            if (viewType == 101) {
                bindNormalHolder(holder, dataPosition)
            } else {
                bindImageHolder(holder, dataPosition)
            }
            holder.itemView.setOnClickListener {
                if (json.isadv == 1) {
                    ActivityRouter.get().startWebActivity(it.context, json.weburl, "")
                } else {
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.NewsView.SID] = "${json.sid}"
                    ActivityRouter.get().startActivity(smContextWrap.context, RouterUrl.ACTIVITY_NEWS_DETAIL, map, false, 0)
                }
            }
        }

        /**
         * 大图item
         */
        private fun bindImageHolder(holder: SparseArrayViewHolder, dataPosition: Int) {
            val json = data[dataPosition]
            val imageIv = holder.getView<ImageView>(R.id.sub_test_pic)
            ImageLoader.getInstance().load(smContextWrap, json.imgurl, R.drawable.icon_default_vertical, imageIv)
            holder.setText(R.id.sub_test_title, json.title)
                    .setText(R.id.sub_test_look, "${json.clicks}人阅读")
            holder.getView<View>(R.id.sub_test_comment).visibility = if (json.isadv == 1) View.VISIBLE else View.GONE
        }

        /**
         * 常规item
         */
        private fun bindNormalHolder(holder: SparseArrayViewHolder, dataPosition: Int) {
            val json = data[dataPosition]
            val imageIv = holder.getView<ImageView>(R.id.main_news_ic)
            ImageLoader.getInstance().load(smContextWrap, json.imgurl, R.drawable.icon_default_vertical, imageIv)
            holder.setText(R.id.main_news_tv, json.title)
                    .setText(R.id.main_news_look_count, "${json.clicks}人阅读")
            holder.getView<View>(R.id.main_news_reply_count).visibility = if (json.isadv == 1) View.VISIBLE else View.GONE
        }


        override fun getRealItemType(positionWithHeader: Int): Int {
            val json = data[positionWithHeader]
            return if (json.istop == 1) 102 else 101
        }

        override fun getItemLayoutIds(viewType: Int): Int {
            return if (viewType == 101) R.layout.fragment_main_news_item else R.layout.fragment_main_news_image_item
        }

    }

}