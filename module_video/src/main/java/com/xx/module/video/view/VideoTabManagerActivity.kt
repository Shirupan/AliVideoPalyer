package com.xx.module.video.view

import android.app.Activity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mrkj.base.bindView
import com.mrkj.lib.common.view.SmToast
import com.mrkj.lib.net.tool.ExceptionUtl
import com.xx.lib.db.entity.UserSystem
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Path
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.client.ModuleManager
import com.xx.module.common.model.callback.SimpleSubscriber
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.video.R
import com.xx.module.video.VideoModuleClient
import com.xx.module.video.model.entity.MainVideoTab
import com.xx.module.video.model.entity.VideoTabManagerJson
import com.xx.module.video.presenter.VideoTabManagerViewPresenter
import com.xx.module.video.view.constract.IVideoTabManagerView
import java.util.*


/**
 *@author someone
 *@date 2019-06-04
 */
@Path(RouterUrl.ACTIVITY_VIDEO_TAB_MANAGER)
@Presenter(VideoTabManagerViewPresenter::class)
class VideoTabManagerActivity : BaseActivity<VideoTabManagerViewPresenter>(), IVideoTabManagerView {


    val cancelBtn by bindView<View>(R.id.video_tab_manager_cancel)
    val myRvTipTv by bindView<TextView>(R.id.video_tab_manager_my_rv_tip)
    val myRv by bindView<RecyclerView>(R.id.video_tab_manager_my_rv)
    var myAdapter: TabItemAdapter? = null

    val canAddRvTipTv by bindView<TextView>(R.id.video_tab_manager_all_rv_tip)
    val canAddRv by bindView<RecyclerView>(R.id.video_tab_manager_all_rv)
    var allAdapter: TabItemAdapter? = null

    var hasChanged = false

    override fun getLayoutId(): Int = R.layout.activity_video_tab_manager

    override fun beforeSetContentView() {
        setShowLoadingView(true)
    }

    override fun initViewsAndEvents() {
        setToolBarTitle("叉叉视频")
        cancelBtn.visibility = View.INVISIBLE
        cancelBtn.setOnClickListener {
            myAdapter?.isEditMode = false
            myAdapter?.notifyDataSetChanged()
            allAdapter?.isEditMode = false
            allAdapter?.notifyDataSetChanged()
            cancelBtn.visibility = View.INVISIBLE
        }
        getLoginUserAndLogin(object : UserDataManager.OnGetUserDataListener {
            override fun onSuccess(us: UserSystem) {
                presenter?.loadMyTabs(us.token)
            }

            override fun onFailed(e: Throwable?) {
                SmToast.show(this@VideoTabManagerActivity, ExceptionUtl.catchTheError(e))
                canAddRv.visibility = View.GONE
                canAddRvTipTv.visibility = View.VISIBLE
                canAddRvTipTv.text = "未登录"
                canAddRvTipTv.setOnClickListener(null)
                myRv.visibility = View.GONE
                myRvTipTv.visibility = View.VISIBLE
                myRvTipTv.text = "未登录"
                myRvTipTv.setOnClickListener(null)
                finish()
            }
        })
    }


    /**
     * 我的类别返回
     */
    override fun onMyTabsResult(list: VideoTabManagerJson?, smError: SmError?) {
        loadingViewManager?.dismiss()
        if (smError != null) {
            val message = smError.getMessage(this)
            canAddRvTipTv.text = message
            myRvTipTv.text = message
            val listener = View.OnClickListener { presenter?.loadMyTabs(mUserSystem.token) }
            myRvTipTv.setOnClickListener(listener)
            canAddRvTipTv.setOnClickListener(listener)
            myRvTipTv.visibility = View.VISIBLE
            canAddRvTipTv.visibility = View.VISIBLE
        } else {
            allAdapter = TabItemAdapter()
            val listener = object : OnEditListener {
                override fun onDelete(data: MainVideoTab) {
                    presenter?.deleteMyTab(mUserSystem.token, data)
                }

                override fun onAdd(data: MainVideoTab) {
                    presenter?.addTab(mUserSystem.token, data)
                }

                override fun onEdit() {
                    myAdapter?.isEditMode = true
                    myAdapter?.notifyDataSetChanged()
                    allAdapter?.isEditMode = true
                    allAdapter?.notifyDataSetChanged()
                    cancelBtn.visibility = View.VISIBLE
                }
            }
            if (list?.obj1 != null) {
                myRv.visibility = View.VISIBLE
                myAdapter = TabItemAdapter()
                myAdapter?.isMyTabs = true
                myAdapter?.data = list.obj1
                myRv.layoutManager = GridLayoutManager(this, 5)
                myRv.adapter = myAdapter
                myRv.isNestedScrollingEnabled = false
                myRvTipTv.visibility = View.GONE
                mItemTouchHepler.attachToRecyclerView(myRv)
            } else {
                myRvTipTv.visibility = View.VISIBLE
                myRvTipTv.text = "无"
            }
            if (list?.obj2 != null) {
                canAddRv.visibility = View.VISIBLE
                allAdapter = TabItemAdapter()
                allAdapter?.data = list.obj2
                canAddRv.layoutManager = GridLayoutManager(this, 5)
                canAddRv.adapter = allAdapter
                canAddRv.isNestedScrollingEnabled = false
                canAddRvTipTv.visibility = View.GONE
            } else {
                canAddRvTipTv.visibility = View.VISIBLE
                canAddRvTipTv.text = "无"
            }
            myAdapter?.listener = listener
            allAdapter?.listener = listener
        }
    }

    /**
     * 删除成功返回
     */
    override fun onDeleteSuccessResult(data: MainVideoTab) {
        hasChanged = true
        val position = myAdapter?.data?.indexOf(data) ?: -1
        if (position >= 0) {
            myAdapter?.data?.removeAt(position)
            myAdapter?.notifyItemRemoved(position)
        }
        loadNewTabs()
    }

    /**
     * 添加成功返回
     */
    override fun onAddSuccessResult(data: MainVideoTab) {
        hasChanged = true
        myAdapter?.addData(data)
        val position = allAdapter?.data?.indexOf(data) ?: -1
        if (position >= 0) {
            allAdapter?.data?.removeAt(position)
            allAdapter?.notifyItemRemoved(position)
        }
        loadNewTabs()
    }

    /**
     * 调整顺序是否成功
     */
    override fun onChangeTabResult(from: MainVideoTab, to: MainVideoTab, success: Boolean) {
        if (!success) {
            myAdapter?.let {
                val fromPosition = it.data.indexOf(from)
                val toPosition = it.data.indexOf(to)
                if (fromPosition < 0 || toPosition < 0) {
                    return
                } else {
                    //换回来
                    Collections.swap(it.data, fromPosition, toPosition)
                    it.notifyDataSetChanged()
                }
            }
        } else {
            hasChanged = true
            loadNewTabs()
        }
    }

    override fun onBackPressed() {
        if (hasChanged) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    inner class TabItemAdapter : BaseRVAdapter<MainVideoTab>() {
        var isEditMode = false
        var isMyTabs = false
        var listener: OnEditListener? = null

        init {
            unShowFooterView()
        }

        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            holder.setText(R.id.video_tab_item_tv, json.typename)
            val editBtn = holder.getView<ImageView>(R.id.video_tab_item_iv)
            if (isEditMode) {
                if (isMyTabs) {
                    editBtn.visibility = if (dataPosition != 0) View.VISIBLE else View.GONE
                    editBtn.setImageResource(R.drawable.icon_video_tab_sanchu)
                    editBtn.setOnClickListener {
                        listener?.onDelete(json)
                    }
                } else {
                    editBtn.visibility = View.VISIBLE
                    editBtn.setImageResource(R.drawable.icon_video_tab_tianjia)
                    editBtn.setOnClickListener {
                        listener?.onAdd(json)
                    }
                }
            } else {
                editBtn.visibility = View.GONE
            }
            holder.itemView.setOnLongClickListener {
                isEditMode = true
                listener?.onEdit()
                return@setOnLongClickListener true
            }
        }

        override fun getItemLayoutIds(viewType: Int): Int = R.layout.activity_video_tab_item
    }


    interface OnEditListener {
        fun onDelete(data: MainVideoTab)
        fun onAdd(data: MainVideoTab)
        fun onEdit()
    }

    /**
     * 获取新的tab列表缓存
     */
    fun loadNewTabs() {
        ModuleManager.of(VideoModuleClient::class.java!!)
                .modelClient!!
                .getMainVideoTabs(loginUser?.token
                        ?: "", object : SimpleSubscriber<List<MainVideoTab>>() {
                    override fun onNext(mainVideoTabs: List<MainVideoTab>) {
                        super.onNext(mainVideoTabs)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
    }


    /**
     * 拖动排序
     */
    private val mItemTouchHepler = object : ItemTouchHelper(object : ItemTouchHelper.Callback() {
        private var lastHolder: RecyclerView.ViewHolder? = null
        private var fromPosition = -1
        private var toPosition = -1


        override fun getMovementFlags(recyclerView: RecyclerView, p1: RecyclerView.ViewHolder): Int {
            return if (recyclerView.layoutManager is GridLayoutManager) {
                val dragFlags = UP or DOWN or LEFT or RIGHT
                val swipeFlags = 0
                makeMovementFlags(dragFlags, swipeFlags)
            } else {
                val dragFlags = UP or DOWN
                val swipeFlags = 0
                makeMovementFlags(dragFlags, swipeFlags)
            }
        }

        override fun onMove(p0: RecyclerView, target: RecyclerView.ViewHolder, viewHolder: RecyclerView.ViewHolder): Boolean {
            if (p0.adapter == null || p0.adapter !is BaseRVAdapter<*>) {
                return false
            }
            //得到当拖拽的viewHolder的Position
            toPosition = viewHolder.adapterPosition
            //拿到当前拖拽到的item的viewHolder
            fromPosition = target.adapterPosition
            if (toPosition == 0) {
                //第一个不能选择
                return false
            }
            val adapter = p0.adapter as BaseRVAdapter<*>
            Collections.swap(adapter.data, fromPosition, toPosition)
            adapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, position: Int) {
        }


        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (viewHolder != null) {
                //高亮显示
                val holder = viewHolder as SparseArrayViewHolder
                holder.getView<TextView>(R.id.video_tab_item_tv).setBackgroundResource(R.drawable.button_round_theme_color)
            } else {
                //移动完成，执行交换接口(此时adapter中的集合已经交换，所以from 和 to 的取值需要互换才能回到原始顺序)
                if (fromPosition != -1 && toPosition != -1) {
                    var from: MainVideoTab? = null
                    var to: MainVideoTab? = null
                    if (fromPosition >= 0 && fromPosition < myAdapter?.data?.size ?: 0) {
                        from = myAdapter?.data?.get(fromPosition)
                    }
                    if (toPosition >= 0 && toPosition < myAdapter?.data?.size ?: 0) {
                        to = myAdapter?.data?.get(toPosition)
                    }
                    if (from != null && to != null) {
                        //from和to在原集合中已经互换，这里在接口调用时候需要互换回原来集合位置对应的对象
                        presenter?.changeTabPosition(mUserSystem.token, to, from, toPosition)
                    }
                }
            }
            if (lastHolder != null) {
                val holder = lastHolder as SparseArrayViewHolder
                holder.getView<TextView>(R.id.video_tab_item_tv).setBackgroundResource(R.drawable.button_round_theme_bg_dark_stroke_line)
            }
            fromPosition = -1
            toPosition = -1
            lastHolder = viewHolder
        }

    }) {}

}