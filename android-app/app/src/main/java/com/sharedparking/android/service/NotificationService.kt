package com.sharedparking.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * 通知服务
 * 管理本地通知，如消息提醒、预订提醒等
 */
class NotificationService : Service() {

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager

    inner class LocalBinder : android.os.Binder() {
        fun getService(): NotificationService = this@NotificationService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "通知服务启动")
        return START_STICKY
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "消息通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "接收来自其他用户的消息通知"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(messageChannel)

            val bookingChannel = NotificationChannel(
                CHANNEL_BOOKINGS,
                "预订通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "预订状态变更通知"
            }
            notificationManager.createNotificationChannel(bookingChannel)

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "系统通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "系统公告和提示"
            }
            notificationManager.createNotificationChannel(systemChannel)
        }
    }

    /**
     * 发送新消息通知
     */
    fun showNewMessageNotification(senderName: String, content: String, messageId: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_notifications)
            .setContentTitle("来自 $senderName 的消息")
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID_MESSAGE + messageId, notification)
    }

    /**
     * 发送预订状态通知
     */
    fun showBookingNotification(title: String, content: String, bookingId: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_BOOKINGS)
            .setSmallIcon(android.R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID_BOOKING + bookingId, notification)
    }

    /**
     * 发送系统通知
     */
    fun showSystemNotification(title: String, content: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_SYSTEM)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SYSTEM, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "通知服务停止")
    }

    companion object {
        private const val TAG = "NotificationService"
        private const val CHANNEL_MESSAGES = "channel_messages"
        private const val CHANNEL_BOOKINGS = "channel_bookings"
        private const val CHANNEL_SYSTEM = "channel_system"
        private const val NOTIFICATION_ID_MESSAGE = 1000
        private const val NOTIFICATION_ID_BOOKING = 2000
        private const val NOTIFICATION_ID_SYSTEM = 3000
    }
}
