package com.xx.module.video.view.shortvideo

import android.os.PowerManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.SmLogger
import com.xx.module.common.annotation.Path
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.common.router.RouterParams
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IBaseView
import com.xx.module.video.R

@Path(RouterUrl.ACTIVITY_SHORT_VIDEO_MAIN)
class ShortVideoMainActivity : BaseActivity<BasePresenter<IBaseView>>(), View.OnClickListener {

    private var mType = -1
    private val mViewPager by bindView<ViewPager>(R.id.short_video_main_container)
    /**
     * 有没有显示过网络环境提示框
     */
    var hasEvenShowWifiDialog = false
    /**
     * 屏幕锁
     */
    var weaLock: PowerManager.WakeLock? = null

    override fun getLayoutId(): Int = R.layout.activity_short_video_main


    override fun initViewsAndEvents() {
        setStatusBar(true, false)
        findViewById<View>(R.id.short_video_main_back).setOnClickListener(this)
        mType = getIntExtra(RouterParams.VideoView.VIEW_TYPE, -1)
        initViewPager()
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
    }

    override fun onPause() {
        super.onPause()
        try {
            weaLock?.release()
        } catch (e: Exception) {
            SmLogger.e(e.message)
        }
    }

    private fun initViewPager() {
        mViewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return ShoreVideoRvFragment.getInstance(0)
            }

            override fun getCount(): Int = 1
        }
        mViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            var lastPosition = -1
            override fun onPageSelected(position: Int) {
                lastPosition = position
            }
        })
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.short_video_main_back -> {
                finish()
            }
        }
    }

}