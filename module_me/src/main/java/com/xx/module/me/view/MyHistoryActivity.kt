package com.xx.module.me.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mrkj.lib.common.util.TimeUtil
import com.xx.base.GsonSingleton
import com.xx.lib.db.entity.SmContextWrap
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.SimpleBaseListActivity
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.IBaseAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.common.view.simple.SimpleOnCreateListAdapterListener
import com.xx.module.me.R
import com.xx.module.me.model.entity.MyHistoryJson
import com.xx.module.me.presenter.MyHistoryPresenter
import com.xx.module.me.view.contract.IMyHistoryView
import java.util.*

/**
 *@author someone
 *@date 2019-06-12
 */
@Path(RouterUrl.ACTIVITY_ME_HISTORY)
@Presenter(MyHistoryPresenter::class)
class MyHistoryActivity : SimpleBaseListActivity<MyHistoryPresenter>(), IMyHistoryView {

    val mAdapter = lazy { ItemAdapter() }
    override fun beforeSetContentView() {
        // setTheme(R.style.SmTheme_Night)
    }

    override fun initBeforeLoadData() {
        setToolBarTitle("历史浏览")
    }

    override fun loadDataFromCacheAndNet() {
        presenter?.loadCache(loginUser?.token ?: "")
    }


    override fun loadData(page: Int) {
        presenter?.loadHistory(loginUser?.token ?: "", page)
    }


    override fun onLoadHistoryCacheResult(list: List<MyHistoryJson>?) {
        if (list != null) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
            mAdapter.value.data = list
        }
        recyclerView.post {
            refreshDataByLayout(true)
        }
    }

    override fun onLoadHistoryResult(list: List<MyHistoryJson>, page: Int) {
        if (page == defaultPageOne) {
            initRecyclerViewOrListView(onCreateListAdapterListener)
        }
        mAdapter.value.addDataList(list)
    }


    private val onCreateListAdapterListener = object : SimpleOnCreateListAdapterListener() {
        override fun onCreateRecyclerViewAdapter(): IBaseAdapter<out IBaseAdapter<*, *>, *> {
            mAdapter.value.clearData()
            return mAdapter.value
        }
    }

    inner class ItemAdapter : BaseRVAdapter<MyHistoryJson>() {
        val DAY = 24 * 60 * 60 * 1000
        val smContextWrap = SmContextWrap.obtain(this@MyHistoryActivity)
        var todayCalendar: Calendar? = null

        init {
            setShowLoadingView(false)
            todayCalendar = Calendar.getInstance()
            todayCalendar?.timeInMillis = System.currentTimeMillis()
            TimeUtil.calendarFromDayBegin(todayCalendar!!)
        }

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            if (json.newsdata == null && json.videodata == null) {
                return
            }
            holder.setText(R.id.history_item_title, if (json.newsdata == null) json.videodata.videotitle else json.newsdata.title)
                    .setText(R.id.history_item_read_count, if (json.newsdata == null) "${json.videodata.clicks}次播放" else "${json.newsdata.clicks}次阅读")
            val ic = holder.getView<ImageView>(R.id.history_item_ic)
            ImageLoader.getInstance().load(smContextWrap, if (json.newsdata == null) json.videodata.coverurl else json.newsdata.imgurl,
                    R.drawable.icon_default_vertical, ic)
            //计算要不要显示时间tip
            val itemDay = Calendar.getInstance()
            itemDay.timeInMillis = json.createtime
            TimeUtil.calendarFromDayBegin(itemDay)
            val dayTv = holder.getView<TextView>(R.id.history_item_day)
            val lastCalendar = Calendar.getInstance()
            if (dataPosition - 1 < 0) {
                lastCalendar.timeInMillis = todayCalendar!!.timeInMillis
            } else {
                lastCalendar.timeInMillis = data[dataPosition - 1].createtime
            }
            TimeUtil.calendarFromDayBegin(lastCalendar)
            dayTv.visibility = if (dataPosition == 0 || lastCalendar.timeInMillis != itemDay.timeInMillis) View.VISIBLE else View.GONE
            setupDayTv(dayTv, itemDay)

            holder.itemView.setOnClickListener {
                if (json.newsdata != null) {
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.NewsView.SID] = "${json.newsdata.sid}"
                    ActivityRouter.get().startActivity(smContextWrap.context, RouterUrl.ACTIVITY_NEWS_DETAIL, map, false, 0)
                } else {
                    //前往视频详情
                    val jsonStr = GsonSingleton.getInstance().toJson(json.videodata)
                    val map = mutableMapOf<String, String>()
                    map[RouterParams.VideoView.DATA] = jsonStr
                    ActivityRouter.get().startActivity(it.context, RouterUrl.ACTIVITY_VIDEO_DETAIL, map, false, 0)
                }
            }
        }

        private fun setupDayTv(dayTv: TextView, calendar: Calendar) {
            val dx = todayCalendar!!.timeInMillis - calendar.timeInMillis
            when {
                dx < DAY -> dayTv.text = "今天"
                dx < 2 * DAY -> dayTv.text = "昨天"
                dx < 3 * DAY -> dayTv.text = "前天"
                else -> dayTv.text = "早些时候"
            }
        }


        override fun getItemLayoutIds(viewType: Int): Int = R.layout.activity_history_item
    }
}