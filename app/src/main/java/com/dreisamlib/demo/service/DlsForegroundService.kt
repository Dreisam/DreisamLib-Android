package com.dreisamlib.demo.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dreisamlib.demo.R
import com.dreisamlib.demo.ui.MainActivity
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.lib.api.DreisamLib
import com.dreisamlib.demo.utils.PermissionManager

/**
 * 血糖的前台服务(用于保活和执行后台定时任务)
 */
class DlsForegroundService : Service() {


    companion object {
        // 使用伴生对象变量来跟踪服务状态
        @Volatile
        private var isServiceRunning = false

        // 获取服务运行状态
        fun isServiceRunning(): Boolean = isServiceRunning

        fun startService(context: Context) {
            if (!PermissionManager.checkBluetoothScanConnectPermission()) return
            if (!PermissionManager.checkNotifyPermission()) return
            // 如果服务已经在运行，则不再启动
            if (isServiceRunning) {
                AppLogUtils.debug("Service is running")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    startServiceIntent(context)
                } catch (e: ForegroundServiceStartNotAllowedException) {
                    AppLogUtils.debug("Service run fail：${e.message}")
                }
            } else {
                startServiceIntent(context)
            }
        }

        /**
         * 启动服务
         */
        private fun startServiceIntent(context: Context) {
            val intent = Intent(context, DlsForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * 停止服务
         */
        fun stopService(context: Context) {
            val intent = Intent(context, DlsForegroundService::class.java)
            context.stopService(intent)
            isServiceRunning = false
            AppLogUtils.debug("stopService")
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        AppLogUtils.debug("startForeground")
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DreisamLib.getConnectManage().startHeartbeat()
        AppLogUtils.debug("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }



    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        // 停止心跳检测（如果适用）
        DreisamLib.getConnectManage().stopHeartbeat()
    }

    /**
     * 前台服务的通知消息
     */
    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // 使用FLAG_IMMUTABLE和FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, "service")
            .setContentTitle("Real-time data")
            .setContentText("--")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 设置为持续通知，防止被清除
            .build()
        if (PermissionManager.checkNotifyPermission())
            startForeground(1011, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}