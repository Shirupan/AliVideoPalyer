package com.xx.app.dependendy.view

import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.view.View
import android.widget.RadioGroup
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.ActivityManagerUtil
import com.mrkj.lib.common.view.SmToast
import com.mrkj.lib.net.retrofit.RetrofitManager
import com.mrkj.lib.update.SmUpdateManager
import com.xx.app.dependendy.R
import com.xx.app.dependendy.model.DependencyCacheProvider
import com.xx.lib.db.entity.ReturnBeanJson
import com.xx.module.common.annotation.Path
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.model.callback.ResultUICallback
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IBaseView
import com.xx.module.me.view.MeFragment
import com.xx.module.me.view.contract.IMeChangeStatusCallback
import com.xx.module.news.view.MainNewsFragment
import com.xx.module.video.view.MainVideoFragment
import com.xx.module.video.view.constract.IMainActivityView
import com.xx.module.video.view.constract.ITabChangeCallback
import com.xx.module.video.view.shortvideo.ShortVideoMainListFragment
import com.umeng.analytics.MobclickAgent
import java.util.*

/**
 *@author someone
 *@date 2019-05-30
 */
@Path(RouterUrl.ACTIVITY_MAIN)
class MainActivity : BaseActivity<BasePresenter<IBaseView>>(), IMainActivityView, ITabChangeCallback, IMeChangeStatusCallback {


    private val mainRg by bindView<RadioGroup>(R.id.main_bg)

    private var exitTime: Long = 0
    private var currentFragment: Fragment? = null
    private var fragmentArray = arrayOfNulls<Fragment>(4)
    var mainViewAppbar: AppBarLayout? = null
    var lastSelectItem = 0
    /**
     * 应用审核状态
     */
    private var isAppStatusPass = false

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun beforeSetContentView() {
        //  setTheme(R.style.SmTheme_Night)
    }

    override fun initViewsAndEvents() {
        setStatusBar(true, true)
        //SplashActivity中已经联网查询过，这里只从缓存中读取
        DataProviderManager.get(DependencyCacheProvider::class.java)
                .getAppStatus(null)
                .compose(RetrofitManager.rxTransformer<ReturnBeanJson<*>>(this, ReturnBeanJson::class.java))
                .subscribe(object : ResultUICallback<ReturnBeanJson<*>>(this) {
                    override fun onNext(t: ReturnBeanJson<*>) {
                        super.onNext(t)
                        showBottomBar(true)
                    }

                    override fun onError(t: Throwable) {
                        super.onError(t)
                        showBottomBar(false)
                    }
                }.unShowDefaultMessage())
        SmUpdateManager.buglyCheckUpgrade(this)
    }

    /**
     * 获取应用当前审核信息
     */
    private fun showBottomBar(isPass: Boolean) {
        isAppStatusPass = isPass
        findViewById<View>(R.id.main_bg_1).visibility = if (isAppStatusPass) View.VISIBLE else View.GONE
        if (isAppStatusPass) {
            mainRg.check(R.id.main_bg_1)
            lastSelectItem = R.id.main_bg_1
            showFragment(0)
        } else {
            mainRg.check(R.id.main_bg_2)
            lastSelectItem = R.id.main_bg_2
            showFragment(1)
        }
        mainRg.setOnCheckedChangeListener { group, checkedId ->
            val position = when (checkedId) {
                R.id.main_bg_1 -> 0
                R.id.main_bg_2 -> 1
                R.id.main_bg_3 -> 2
                R.id.main_bg_4 -> 3
                else -> 0
            }
            if (lastSelectItem == checkedId) {
                return@setOnCheckedChangeListener
            }
            lastSelectItem = checkedId
            showFragment(position)
        }
    }

    private fun showFragment(position: Int) {
        val ft = supportFragmentManager.beginTransaction()
        val fragment = if (position == 0 && refreshTabOne) {
            fragmentArray[position]?.let {
                ft.remove(it)
            }
            val f = MainVideoFragment()
            ft.add(R.id.main_fragment, f, "main_view_$position")
            fragmentArray[position] = f
            f
        } else if (fragmentArray[position] == null) {
            val f = when (position) {
                0 -> MainVideoFragment()
                1 -> {
                    ShortVideoMainListFragment() as Fragment
                }
                2 -> MainNewsFragment()
                3 -> MeFragment()
                else -> MainVideoFragment()
            }

            ft.add(R.id.main_fragment, f, "main_view_$position")
            fragmentArray[position] = f
            f
        } else {
            fragmentArray[position]!!
        }
        val list = supportFragmentManager.fragments
        for (f in list) {
            if (f !== fragment) {
                ft.hide(f)
            }
        }
        ft.show(fragment)
        ft.commitAllowingStateLoss()
        currentFragment = fragment
    }

    override fun onBackPressed() {
        //先回到首页
        if (isAppStatusPass && currentFragment is MainVideoFragment) {
            checkExitApp()
        } else if (!isAppStatusPass && currentFragment is ShortVideoMainListFragment) {
            checkExitApp()
        } else {
            mainRg.check(R.id.main_bg_1)
        }
    }

    override fun getStatus(): Boolean = isAppStatusPass

    private fun checkExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            SmToast.show(this, String.format(Locale.CHINESE, "再按一次退出%1s",
                    getString(R.string.app_name)))
            exitTime = System.currentTimeMillis()
        } else {
            MobclickAgent.onKillProcess(this@MainActivity)
            super.onBackPressed()
            ActivityManagerUtil.getScreenManager().popAllActivity()
        }
    }

    override fun getMainViewAppbarLayout(): AppBarLayout? {
        return mainViewAppbar
    }

    override fun setMainViewAppbarLayout(appbarLayout: AppBarLayout?) {
        mainViewAppbar = appbarLayout
    }

    override fun onStart() {
        super.onStart()
        ActivityManagerUtil.getScreenManager().pushActivity(this, true)
    }

    private var refreshTabOne = false

    override fun onchanged() {
        refreshTabOne = true
        showFragment(0)
    }
}