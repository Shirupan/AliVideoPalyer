package com.xx.module.me.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.ScreenUtils
import com.mrkj.lib.common.view.SmToast
import com.mrkj.lib.net.tool.ExceptionUtl
import com.xx.lib.db.entity.SmContextWrap
import com.xx.lib.db.entity.UserSystem
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Presenter
import com.xx.module.common.imageload.ImageLoader
import com.xx.module.common.model.entity.SmError
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseFragment
import com.xx.module.common.view.loading.BaseRVAdapter
import com.xx.module.common.view.loading.SparseArrayViewHolder
import com.xx.module.me.R
import com.xx.module.me.model.entity.MeMainInfo
import com.xx.module.me.model.entity.MeMenu
import com.xx.module.me.presenter.MeViewPresenter
import com.xx.module.me.view.contract.IMeChangeStatusCallback
import com.xx.module.me.view.contract.IMeView

/**
 *@author someone
 *@date 2019-05-31
 */

@Presenter(MeViewPresenter::class)
class MeFragment : BaseFragment<MeViewPresenter>(), IMeView, View.OnClickListener {

    val REQUEST_EDIT_INFO = 1011

    val nestSv by bindView<NestedScrollView>(R.id.icon_me_sv)
    //导航栏背景
    val toolbarBgView by bindView<View>(R.id.me_tool_bar_bg)
    //状态栏兼容高度
    val statusBarView by bindView<View>(R.id.me_status_bar)


    val settingView by bindView<ImageView>(R.id.me_tool_bar_setting)

    val kefuIv by bindView<ImageView>(R.id.me_tool_bar_kefu)
    val savorIv by bindView<ImageView>(R.id.icon_me_savor)
    val titleTv by bindView<TextView>(R.id.icon_me_name)
    val collectTv by bindView<TextView>(R.id.me_info_collect)
    val historyTv by bindView<TextView>(R.id.me_info_history)
    val zanTv by bindView<TextView>(R.id.me_info_zan)

    val mRvTip1 by bindView<View>(R.id.me_tip_1)
    val mRv1 by bindView<RecyclerView>(R.id.me_tool_rv)
    val mRv2 by bindView<RecyclerView>(R.id.me_tool_rv_2)
    val mTool1Adapter = lazy { MenuAdapter() }
    val mTool2Adapter = lazy { MenuAdapter() }


    override fun beforeSetContentView() {
        setShowLoadingView(true)
    }

    override fun getLayoutID(): Int = R.layout.fragment_me

    override fun initViewsAndEvents(rootView: View?) {
        setCutOutAndStatusMaxHeightToView(statusBarView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val lp = (savorIv.layoutParams as ViewGroup.MarginLayoutParams)
            lp.topMargin = cutOutAndStatusMaxHeight + ScreenUtils.dp2px(context, 55f)
            savorIv.layoutParams = lp
        }

        settingView.setOnClickListener(this)
        kefuIv.setOnClickListener(this)
        titleTv.setOnClickListener(this)
        collectTv.setOnClickListener(this)
        historyTv.setOnClickListener(this)
        zanTv.setOnClickListener(this)
        savorIv.setOnClickListener(this)
        nestSv.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            var totalDy = 0
            override fun onScrollChange(view: NestedScrollView, dx: Int, dy: Int, oldDx: Int, oldDy: Int) {
                val currentDy = dy - oldDy
                totalDy += currentDy
                when {
                    totalDy >= toolbarBgView.measuredHeight -> toolbarBgView.alpha = 1.0f
                    totalDy <= 0 -> toolbarBgView.alpha = 0.0f
                    else -> {
                        //补间位移
                        val currentAlpha = currentDy.toFloat() / toolbarBgView.measuredHeight.toFloat()
                        toolbarBgView.alpha = Math.abs(currentAlpha)
                    }
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        if (mRv1.adapter == null || mRv2.adapter == null) {
            getLoginUser(object : UserDataManager.SimpleOnGetUserDataListener() {
                override fun onSuccess(us: UserSystem) {
                    setupData(us)
                }

                override fun onFailed(e: Throwable?) {
                    super.onFailed(e)
                    setupData(null)
                }
            })
        }
    }

    private var hasReloadUser = false

    private fun setupData(us: UserSystem?) {
        if (us == null) {
            savorIv.setImageResource(R.drawable.icon_head_circle_default)
            titleTv.text = "登录获得更多精彩内容"
        } else {
            ImageLoader.getInstance().loadCircle(SmContextWrap.obtain(this), us.photourl, savorIv, R.drawable.icon_head_circle_default)
            titleTv.text = us.nickname
            if (!hasReloadUser) {
                UserDataManager.getInstance().getUserInfoFromNet(object : UserDataManager.OnGetUserDataListener {
                    override fun onSuccess(us: UserSystem) {
                        setupData(us)
                        hasReloadUser = true
                    }

                    override fun onFailed(e: Throwable?) {
                        SmToast.show(context, ExceptionUtl.catchTheError(e))
                    }
                })
            }
        }
        presenter.getTools(mRv1.adapter == null || mRv2.adapter == null)
    }

    /**
     * 获取工具回调
     */
    override fun onToolResult(info: MeMainInfo?, e: SmError?) {
        if (e != null) {
            if (mRv1.adapter == null) {
                loadingViewManager?.setOnRefreshClickListener {
                    presenter.getTools(mRv1.adapter == null || mRv2.adapter == null)
                }
                loadingViewManager?.showFailed(e.getMessage(context))
            } else {
                SmToast.show(context, e.getMessage(context))
            }
        } else if (info != null) {
            if (activity is IMeChangeStatusCallback) {
                val pass = (activity as IMeChangeStatusCallback).getStatus()
                checkAppStatus(info, pass)
            }
        } else {
            if (mRv1.adapter == null) {
                loadingViewManager?.setOnRefreshClickListener {
                    presenter.getTools(mRv1.adapter == null || mRv2.adapter == null)
                }
                loadingViewManager?.showFailed(getString(R.string.rv_footer_again))
            } else {
                loadingViewManager?.dismiss()
            }
        }
    }

    /**
     * 审核状态
     */
    private fun checkAppStatus(info: MeMainInfo, pass: Boolean) {
        loadingViewManager?.dismiss()
        val adapter1 = if (mRv1.adapter == null) {
            val lm = GridLayoutManager(context, 4)
            mRv1.layoutManager = lm
            mRv1.isNestedScrollingEnabled = false
            mRv1.adapter = mTool1Adapter.value
            mTool1Adapter.value
        } else {
            mRv1.adapter as MenuAdapter
        }
        adapter1.clearData()
        adapter1.data = info.obj1

        val adapter2 = if (mRv2.adapter == null) {
            val lm = GridLayoutManager(context, 4)
            mRv2.layoutManager = lm
            mRv2.isNestedScrollingEnabled = false
            mRv2.adapter = mTool2Adapter.value
            mTool2Adapter.value
        } else {
            mRv2.adapter as MenuAdapter
        }
        adapter2.clearData()
        adapter2.data = info.obj2

        if (pass) {
            mRv1.visibility = View.VISIBLE
            mRvTip1.visibility = View.VISIBLE
        } else {
            mRv1.visibility = View.GONE
            mRvTip1.visibility = View.GONE
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            kefuIv -> {
                //客服
                ActivityRouter.openQQCustom(context)
            }
            settingView -> {
                //设置项
                ActivityRouter.get().startActivity(context, RouterUrl.ACTIVITY_SETTING)
            }
            collectTv -> {
                //我的收藏
                if (loginUser == null) {
                    ActivityRouter.get().goToLoginActivity(this)
                    return
                }
                ActivityRouter.get().startActivity(context, RouterUrl.ACTIVITY_ME_COLLECTION)
            }
            historyTv -> {
                //历史
                if (loginUser == null) {
                    ActivityRouter.get().goToLoginActivity(this)
                    return
                }
                ActivityRouter.get().startActivity(context, RouterUrl.ACTIVITY_ME_HISTORY)
            }
            zanTv -> {
                //我的点赞
                if (loginUser == null) {
                    ActivityRouter.get().goToLoginActivity(this)
                    return
                }
                ActivityRouter.get().startActivity(context, RouterUrl.ACTIVITY_ME_PRAISE)
            }
            titleTv,
            savorIv -> {
                //头像点击
                if (loginUser == null) {
                    ActivityRouter.get().goToLoginActivity(this)
                } else {
                    val intent = ActivityRouter.get().getIntent(context, RouterUrl.ACTIVITY_ME_INFO_EDIT)
                    startActivityForResult(intent, REQUEST_EDIT_INFO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_INFO && resultCode == Activity.RESULT_OK) {
            setupData(loginUser)
        } else if (requestCode == ActivityRouter.LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setupData(loginUser)
        }
    }

    inner class MenuAdapter : BaseRVAdapter<MeMenu>() {
        val smContextWrap = SmContextWrap.obtain(this@MeFragment)


        init {
            unShowFooterView()
        }


        override fun onBindItemViewHolder(holder: SparseArrayViewHolder, dataPosition: Int, viewType: Int) {
            val json = data[dataPosition]
            val iv = holder.getView<ImageView>(R.id.me_tool_item_img)
            ImageLoader.getInstance().load(smContextWrap, json.img, 0, iv)
            holder.setText(R.id.me_tool_item_title, json.title)

            holder.itemView.setOnClickListener {
                if (TextUtils.isEmpty(json.url)) {
                    return@setOnClickListener
                }
                if (json.url.startsWith(RouterUrl.SM_SCHEME)) {
                    //本地页面
                    ActivityRouter.get().startActivity(smContextWrap.context, json.url)
                } else {
                    ActivityRouter.get().startWebActivity(smContextWrap.context, json.url, json.title)
                }
            }
        }

        override fun getItemLayoutIds(viewType: Int): Int = R.layout.fragment_me_tool_item
    }
}