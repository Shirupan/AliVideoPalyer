package com.xx.module.common.view

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.xx.module.common.R

import com.xx.module.common.router.ActivityRouter
import com.xx.module.common.router.RouterUrl


class SmNotificationManager {

    companion object {
        val CHANNEL_DOWNLOAD_ID = "download"
        val CHANNEL_DOWNLOAD_NAME = "下载服务"
        val CHANNEL_DOWNLOAD_DESCRIPTION = "下载信息的通知分类"

        val CHANNEL_SYSTEM_ID = "system"
        val CHANNEL_SYSTEM_NAME = "系统消息"
        val CHANNEL_SYSTEM_DESCRIPTION = "系统消息提示分类"

        val CHANNEL_MEDIA_ID = "media"
        val CHANNEL_MEDIA_NAME = "媒体播放"
        val CHANNEL_MEDIA_DESCRIPTION = "当前媒体播放信息分类"

        /**
         * 创建通知通道
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun createChannel(context: Context) {
            val updateNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mChannel1 = NotificationChannel(CHANNEL_DOWNLOAD_ID, CHANNEL_DOWNLOAD_NAME, NotificationManager.IMPORTANCE_HIGH)
            mChannel1.description = CHANNEL_DOWNLOAD_DESCRIPTION
            mChannel1.setShowBadge(true)
            val butes = AudioAttributes.Builder()
            butes.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            butes.setLegacyStreamType(AudioManager.STREAM_RING)
            butes.setUsage(AudioAttributes.USAGE_MEDIA)
            mChannel1.setSound(Uri.EMPTY, butes.build())
            updateNotificationManager.createNotificationChannel(mChannel1)

            val mChannel2 = NotificationChannel(CHANNEL_SYSTEM_ID, CHANNEL_SYSTEM_NAME, NotificationManager.IMPORTANCE_HIGH)
            mChannel2.description = CHANNEL_SYSTEM_DESCRIPTION
            mChannel2.setShowBadge(true)
            val butes2 = AudioAttributes.Builder()
            butes2.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            butes2.setLegacyStreamType(AudioManager.STREAM_RING)
            butes2.setUsage(AudioAttributes.USAGE_MEDIA)
            updateNotificationManager.createNotificationChannel(mChannel2)


            val mChannel3 = NotificationChannel(CHANNEL_MEDIA_ID, CHANNEL_MEDIA_NAME, NotificationManager.IMPORTANCE_HIGH)
            mChannel3.description = CHANNEL_MEDIA_DESCRIPTION
            mChannel3.setShowBadge(true)
            val butes3 = AudioAttributes.Builder()
            butes3.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            butes3.setLegacyStreamType(AudioManager.STREAM_RING)
            butes3.setUsage(AudioAttributes.USAGE_MEDIA)
            mChannel3.setSound(Uri.EMPTY, butes.build())
            updateNotificationManager.createNotificationChannel(mChannel3)
        }

        /**
         * 检查当前渠道的通知是否被打开，并引导用户在设置中打开
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun checkAndNeedSetting(c: Context, channelId: String): Boolean {
            val updateNotificationManager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = updateNotificationManager.getNotificationChannel(channelId)
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, c.packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
                Toast.makeText(c, "请手动将[$channelId]类别的通知打开", Toast.LENGTH_SHORT).show()
                try {
                    c.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }
            return true
        }

        fun createForegroundNotification(c: Context): Notification {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(c, CHANNEL_MEDIA_ID)
            val intent = PendingIntent.getActivity(c, 0,
                    ActivityRouter.get().getIntent(c,RouterUrl.ACTIVITY_MAIN), PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(c.getString(R.string.app_name))
                    .setContentText("服务正在运行中")
                    .setAutoCancel(true)
                    //通知放置在正在运行
                    .setOngoing(false)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setChannelId(CHANNEL_MEDIA_ID)
                    // 设置为public后，通知栏将在锁屏界面显示
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setContentIntent(intent)
                    .priority = NotificationCompat.PRIORITY_DEFAULT
            return builder.build()
        }


        fun startForeground(service: Service) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                service.startForeground(1, createForegroundNotification(service))
            }
        }

        fun stopService(service: Service) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                service.stopForeground(true)
            }
            service.stopSelf()
        }
    }


}