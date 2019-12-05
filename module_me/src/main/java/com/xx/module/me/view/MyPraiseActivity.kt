package com.xx.module.me.view

import android.content.Context
import android.graphics.Typeface
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.TypedValue
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.ScreenUtils
import com.xx.module.common.annotation.Path
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IBaseView
import com.xx.module.me.R
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

/**
 *@author someone
 *@date 2019-06-13
 */
@Path(RouterUrl.ACTIVITY_ME_PRAISE)
class MyPraiseActivity : BaseActivity<BasePresenter<IBaseView>>() {

    private val mIndicator by bindView<MagicIndicator>(R.id.my_praise_indicator)

    private val mViewPager by bindView<ViewPager>(R.id.my_praise_vp)

    override fun getLayoutId(): Int = R.layout.activity_my_praise

    override fun initViewsAndEvents() {
        setToolBarTitle("我的点赞")
        initViewPager()
    }


    private fun initViewPager() {
        val navigator = CommonNavigator(this)
        navigator.isAdjustMode = false
        navigator.adapter = object : CommonNavigatorAdapter() {
            var normalColor: Int = 0
            var selectedColor: Int = 0

            override fun getCount(): Int {
                return 2
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                if (normalColor == 0) {
                    val typedValue = TypedValue()
                    theme.resolveAttribute(R.attr.smTipColor, typedValue, true)
                    normalColor = ContextCompat.getColor(context, typedValue.resourceId)
                }
                if (selectedColor == 0) {
                    selectedColor = ContextCompat.getColor(context, R.color.text_red)
                }
                val titleView = SimplePagerTitleView(context)
                titleView.textSize = 16f
                titleView.setPadding(titleView.paddingLeft,titleView.paddingTop,titleView.paddingRight,ScreenUtils.dip2px(context,6f))
                titleView.typeface = Typeface.DEFAULT_BOLD
                titleView.normalColor = normalColor
                titleView.selectedColor = selectedColor
                titleView.text = if (index == 0) "视频" else "趣闻"
                titleView.setOnClickListener { mViewPager.currentItem = index }
                return titleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.roundRadius= ScreenUtils.dp2px(context,10f).toFloat()
                indicator.mode=LinePagerIndicator.MODE_WRAP_CONTENT
                indicator.setColors(selectedColor)
                return indicator
            }
        }
        mIndicator.navigator = navigator

        mViewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return MyPraiseFragment.getInstance(position)
            }

            override fun getCount(): Int {
                return 2
            }
        }
        ViewPagerHelper.bind(mIndicator, mViewPager)
    }
}