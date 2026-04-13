package com.dreisamlib.demo.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import com.dreisamlib.demo.ctrl.ConnectCtrl
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.SharedPreferUtils
import com.dreisamlib.lib.api.DreisamLib
import java.io.PrintWriter
import java.io.StringWriter

class MyApp :Application(){


    companion object{
        lateinit var  sharedPreferUtils: SharedPreferUtils
        lateinit var context: Context
    }


    override fun onCreate() {
        super.onCreate()
        context = this
        sharedPreferUtils = SharedPreferUtils.getInstanse(this)
        DreisamLib.preInit(this)
        val versionName = DreisamLib.getVersionName()
        AppLogUtils.debug("versionName:" +versionName)
        initAppCrashLogCollect()
        registerActivityLifecycleCallbacks(activityLife)
        
        ConnectCtrl.initSDK()
    }


   var activityLife = object : ActivityLifecycleCallbacks {
        private var refCount = 0
        private var isFrist = true

       override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

       }

       override fun onActivityStarted(activity: Activity) {
           if (refCount == 0 && !isFrist) {
               AppLogUtils.debug("MyApp onActivityStarted")
           }
           isFrist = false
           refCount++
       }

       override fun onActivityResumed(activity: Activity) {
       }

       override fun onActivityPaused(activity: Activity) {
       }

       override fun onActivityStopped(activity: Activity) {
           refCount--
           if (refCount <= 0) {
               refCount = 0
               AppLogUtils.debug("MyApp onActivityStopped")
           }
       }

       override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

       }

       override fun onActivityDestroyed(activity: Activity) {
       }
   }

    /**
     * 设置本地崩溃和闪退日志的收集
     */
    private fun initAppCrashLogCollect() {
        // 保存系统默认的异常处理器
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            ex.printStackTrace(pw)
            pw.flush()
            val message =
                "崩溃线程: ${thread.name} (ID: ${thread.id})\n 崩溃信息: ${ex.message}\n堆栈跟踪:$sw"
            //将崩溃日志保存到本地日志中
            AppLogUtils.debug("message:" +message)
            ConnectCtrl.saveCrashLog("APP崩溃日志信息：\n$message")
            //如果系统有默认处理器，交给系统处理，保证正常退出
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex)
            } else {
                // 否则强制退出，确保不卡住
                Process.killProcess(Process.myPid())
                System.exit(1)
            }
        }
    }



}