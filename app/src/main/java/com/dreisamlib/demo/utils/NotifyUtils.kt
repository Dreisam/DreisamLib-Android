package com.dreisamlib.demo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.dreisamlib.demo.R
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.ui.MainActivity
import com.dreisamlib.lib.bean.DreisamGlucoseModel

/**
 * Notification message management
 */
object NotifyUtils {
    const val serviceId = "service"
    private const val serviceName = "Real-time data"
    private const val glucoseDataName = "Monitor"
    private const val glucoseDataId = "glucoseData"
    private const val glucoseWarnId = "glucoseWarn"
    const val notifyIdService = 1011//Notification ID for the foreground service
    private const val bluetoothSignal = 1012//Notification ID for missing Bluetooth signal

    /**
     * Create notification channels
     */
    fun createNotificationChannel() {
        val context = MyApp.context
        // Create notification channels (only for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Notification channel for foreground service
            if (!TextUtils.isEmpty(serviceName)) {
                var notificationChannel = NotificationChannel(
                    serviceId,
                    serviceName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableVibration(false)//Whether vibration is enabled
                notificationChannel.setSound(null, null)//Disable ringtone
                notificationManager.createNotificationChannel(notificationChannel)
            }

            // Notification channel for glucose data
            if (!TextUtils.isEmpty(glucoseDataName)) {
                var notificationChannel = NotificationChannel(
                    glucoseDataId,
                    glucoseDataName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableVibration(true)//Whether vibration is enabled
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }


    private fun sendNotifyMessage(title: String, message: String, channelId: String, id: Int) {
        if (PermissionManager.checkNotifyPermission()) {
            getNotificationManager().notify(
                id,
                getNotificationCompat(title, message, channelId).build()
            )
        }
    }



    public fun cncelConnectLossAlarmNotifyMessage(){
        AppLogUtils.debug("取消信号丢失通知")
        if (PermissionManager.checkNotifyPermission()) {
            getNotificationManager().cancel(bluetoothSignal)
        }

    }






    /**
     * Glucose data
     */
    fun sendGlucoseData(entity: DreisamGlucoseModel) {
        var title = "Real-time data"
        var time = TimeUtils.formatHM(entity.timeCreate * 1000)
        var value = entity.glucose
        var message = if (entity.type == 0) "--" else "Time：$time Value：$value"
        sendNotifyMessage(title, message, serviceId, notifyIdService)
    }

    /**
     * Send notification for persistent service
     */
    fun sendGlucoseService() {
        var title ="Real-time data"
        val entity = MyApp.sharedPreferUtils.getGlucoseNew()
        var message = "--"
        entity.let {
            if (entity.type == 0) "--" else "Time：${ TimeUtils.formatHM(it.timeCreate * 1000)} Value：${it.glucose}"
        }
        sendNotifyMessage(title, message, serviceId, notifyIdService)
    }



    /**
     * Cancel all notification messages
     */
    fun cancelAllNotify() {
        getNotificationManager().cancelAll()
    }

    private fun getNotificationCompat(
        title: String,
        message: String,
        channelId: String
    ): NotificationCompat.Builder {
        var context = MyApp.context
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = ComponentName(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

    }

    private fun getNotificationManager(): NotificationManager =
        MyApp.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


}
