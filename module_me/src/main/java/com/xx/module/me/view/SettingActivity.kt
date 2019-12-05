package com.xx.module.me.view

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import com.mrkj.base.bindView
import com.mrkj.lib.common.util.AppUtil
import com.mrkj.lib.net.impl.RxAsyncHandler
import com.xx.lib.db.dao.AppDatabase
import com.xx.lib.db.dao.UserSettingDao
import com.xx.lib.db.entity.UserSetting
import com.xx.module.common.UserDataManager
import com.xx.module.common.annotation.Path
import com.xx.module.common.model.cache.DataProviderManager
import com.xx.module.common.presenter.BasePresenter
import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterUrl
import com.xx.module.common.view.base.BaseActivity
import com.xx.module.common.view.contract.IBaseView
import com.xx.module.common.view.dialog.SmDefaultDialog
import com.xx.module.me.R
import io.reactivex.FlowableSubscriber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import java.util.*

/**
 *@author someone
 *@date 2019-06-14
 */
@Path(RouterUrl.ACTIVITY_SETTING)
class SettingActivity : BaseActivity<BasePresenter<IBaseView>>(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private var settings: UserSetting? = null
    private var userSettingDao: UserSettingDao? = null


    private val cacheSizeTv by bindView<TextView>(R.id.setting_cache_size_tv)
    private val versionTv by bindView<TextView>(R.id.setting_version_tv)

    private val notifySwitch by bindView<SwitchCompat>(R.id.setting_notify_switch)
    private val wifiSwitch by bindView<SwitchCompat>(R.id.setting_wifi_switch)

    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun initViewsAndEvents() {
        setToolBarTitle("设置")
        if (loginUser == null) {
            findViewById<View>(R.id.setting_notify_layout).visibility = View.GONE
        }
        findViewById<View>(R.id.setting_cache_size_tv_layout).setOnClickListener(this)
        findViewById<View>(R.id.setting_share_layout).setOnClickListener(this)
        findViewById<View>(R.id.setting_update_layout).setOnClickListener(this)
        findViewById<View>(R.id.setting_xieyi_layout).setOnClickListener(this)
        findViewById<View>(R.id.setting_yinsi_layout).setOnClickListener(this)
        userSettingDao = AppDatabase.getInstance(this).userSettingDao
        userSettingDao?.getSettingByToken(loginUser?.token ?: "")
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : FlowableSubscriber<List<UserSetting>> {
                    override fun onComplete() {

                    }

                    override fun onSubscribe(s: Subscription) {
                        s.request(1)
                    }

                    override fun onNext(t: List<UserSetting>) {
                        if (t.isEmpty()) {
                            settings = UserSetting()
                            settings!!.token = loginUser?.token ?: ""
                            insert2DB(settings!!)
                        } else {
                            settings = t[0]
                            val list = t.toMutableList()
                            list.remove(t[0])
                            deleteSettings(list)

                        }
                        setupData()
                    }

                    override fun onError(t: Throwable?) {
                        settings = UserSetting()
                        setupData()
                    }

                })
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (notifySwitch == buttonView) {
            settings?.notify = if (isChecked) 0 else 1
            if (isChecked) {
                UserDataManager.getInstance().startPushService(this@SettingActivity)
            } else {
                UserDataManager.getInstance().stopPushService(this@SettingActivity)
            }
        } else if (buttonView == wifiSwitch) {
            settings?.wifiAutoPlay = if (isChecked) 0 else 1
        }
        updateUserSetting(settings!!)
    }

    private fun updateUserSetting(setting: UserSetting) {
        object : RxAsyncHandler<Boolean>(this) {
            override fun doSomethingBackground(): Boolean {
                userSettingDao?.update(setting)
                return true
            }

            override fun onNext(data: Boolean?) {
            }
        }.execute()
    }


    private fun deleteSettings(list: List<UserSetting>) {
        object : RxAsyncHandler<Boolean>(this) {
            override fun doSomethingBackground(): Boolean {
                val array = Array(list.size) {
                    list[it]
                }
                val arrayList = ArrayList(list)
                arrayList.toArray()
                arrayList.toArray(array)
                userSettingDao?.delete(*array)
                return true
            }

            override fun onNext(data: Boolean?) {
            }
        }.execute()
    }

    private fun insert2DB(setting: UserSetting) {
        object : RxAsyncHandler<Boolean>(this) {
            override fun doSomethingBackground(): Boolean {
                userSettingDao?.insert(setting)
                return true
            }

            override fun onNext(data: Boolean?) {
            }

        }.execute()
    }


    private fun setupData() {
        notifySwitch.isChecked = settings?.notify == 0
        wifiSwitch.isChecked = settings?.wifiAutoPlay == 0
        object : RxAsyncHandler<String>() {
            override fun doSomethingBackground(): String {
                val sizeMap = DataProviderManager.getAllFilesSize(this@SettingActivity)
                return sizeMap["size"] + " " + sizeMap["type"]
            }

            override fun onNext(data: String) {
                cacheSizeTv.text = data
            }
        }.execute()
        versionTv.text = getString(R.string.setting_version_s, AppUtil.getAppVersionName(this))

        notifySwitch.setOnCheckedChangeListener(this)
        wifiSwitch.setOnCheckedChangeListener(this)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_cache_size_tv_layout -> SmDefaultDialog.Builder(this)
                    .setMessage("应用缓存包括图片、音频、数据等，是否要清理应用缓存？")
                    .setPositiveButton("清理") { dialog, _ ->
                        dialog.dismiss()
                        DataProviderManager.deleteFiles(this@SettingActivity) {
                            cacheSizeTv.text = "0M"
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            R.id.setting_share_layout -> {
                //分享
                val uri = Uri.parse("https://android.myapp.com/myapp/detail.htm?apkName=${this.packageName}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.setting_update_layout -> {
                //更新检测(跳转应用商店）
                try {
                    val uri = Uri.parse("market://details?id=${this.packageName}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    v.context.startActivity(intent)
                } catch (e: Exception) {
                    val uri = Uri.parse("https://android.myapp.com/myapp/detail.htm?apkName=${this.packageName}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            R.id.setting_yinsi_layout -> {
                //隐私条款
                ActivityRouter.get().startWebActivity(this, "file:///android_asset/privacy_agreement.html", "")
            }
            R.id.setting_xieyi_layout -> {
                //用户协议
                ActivityRouter.get().startWebActivity(this, "file:///android_asset/user_agreement.html", "")
            }
        }
    }
}