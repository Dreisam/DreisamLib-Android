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
 * 通知消息的管理
 */
object NotifyUtils {
    const val serviceId = "service"
    private const val serviceName = "Real-time data"
    private const val glucoseDataName = "Monitor"
    private const val glucoseDataId = "glucoseData"
    private const val glucoseWarnId = "glucoseWarn"
    const val notifyIdService = 1011//前台服务的通知ID
    private const val bluetoothSignal = 1012//蓝牙信号缺失的通知ID

    /**
     * 创建通知消息的推送通道
     */
    fun createNotificationChannel() {
        val context = MyApp.context
        // 创建通知渠道（仅适用于 Android 8.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //前台服务的通知渠道
            if (!TextUtils.isEmpty(serviceName)) {
                var notificationChannel = NotificationChannel(
                    serviceId,
                    serviceName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableVibration(false)//是否震动
                notificationChannel.setSound(null, null)//屏蔽铃声
                notificationManager.createNotificationChannel(notificationChannel)
            }

            //血糖数据的通知渠道
            if (!TextUtils.isEmpty(glucoseDataName)) {
                var notificationChannel = NotificationChannel(
                    glucoseDataId,
                    glucoseDataName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableVibration(true)//是否震动
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
     * 发送蓝牙信号缺失的通知
     */
    fun sendConnectSignalLossAlarm() {
        val title = "连接丢失警报"
        val message ="连接丢失，功能不可用，请检查连接"
        AppLogUtils.debug("蓝牙信号缺失，进行响铃提醒")
        sendNotifyMessage(title, message, glucoseWarnId, bluetoothSignal)
    }





    /**
     * 血糖数据
     */
    fun sendGlucoseData(entity: DreisamGlucoseModel) {
        var title = "Real-time data"
        var time = TimeUtils.formatHM(entity.timeCreate * 1000)
        var value = entity.glucose
        var message = if (entity.type == 0) "--" else "Time：$time Value：$value"
        sendNotifyMessage(title, message, serviceId, notifyIdService)
    }

    /**
     * 发送常驻服务的通知
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
     * 取消所有的通知消息
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