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
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.lib.api.DreisamLib
import com.dreisamlib.demo.utils.PermissionManager
import com.dreisamlib.demo.ui.MainActivity

/**
 * Foreground service for glucose monitoring (used for keep-alive and background scheduled tasks)
 */
class DlsForegroundService : Service() {


    companion object {
        // Use a companion object variable to track service status
        @Volatile
        private var isServiceRunning = false

        // Get service running status
        fun isServiceRunning(): Boolean = isServiceRunning

        fun startService(context: Context) {
            if (!PermissionManager.checkBluetoothScanConnectPermission()) return
            if (!PermissionManager.checkNotifyPermission()) return
            // Do not start again if the service is already running
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
         * Start service
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
         * Stop service
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
        // Stop heartbeat check (if applicable)
        DreisamLib.getConnectManage().stopHeartbeat()
    }

    /**
     * Notification message for foreground service
     */
    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Use FLAG_IMMUTABLE and FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, "service")
            .setContentTitle("Real-time data")
            .setContentText("--")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Set as ongoing notification to prevent dismissal
            .build()
        if (PermissionManager.checkNotifyPermission())
            startForeground(1011, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}
