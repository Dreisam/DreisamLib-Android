package com.dreisamlib.demo.ctrl

import android.content.Context
import android.widget.Toast
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.demo.ui.BaseActivity
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.FileShare
import com.dreisamlib.demo.utils.FileUtils.deleteFile
import com.dreisamlib.demo.utils.FileUtils.makeFilePath
import com.dreisamlib.demo.utils.NotifyUtils
import com.dreisamlib.demo.utils.TimeUtils
import com.dreisamlib.lib.api.DreisamLib
import com.dreisamlib.lib.bean.DreisamLibBuilder
import com.dreisamlib.lib.bean.DreisamConnectEnum
import com.dreisamlib.lib.bean.DreisamGlucoseModel
import com.dreisamlib.lib.listener.OnAnalzeDatatListener
import com.dreisamlib.lib.listener.OnConnectListener
import com.dreisamlib.lib.listener.OnSyncDatasCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.CopyOnWriteArrayList

object ConnectCtrl {

    val onConnectListeners = CopyOnWriteArrayList<OnConnectListener>()

    val onAnalzeDatatListeners = CopyOnWriteArrayList<OnAnalzeDatatListener>()

    val onSyncDatasCallBacks = CopyOnWriteArrayList<OnSyncDatasCallBack>()

    var connectLogStr = ""


    //从开发平台获取自己的appId，必传
    val appid = "xxxx"

    fun initSDK() {
        val builder = DreisamLibBuilder()
        builder.isHideLog = false   //开发日志是否隐藏，true：隐藏，false：显示。正式上线建议隐藏
        DreisamLib.initSDK(appid, builder)
        setConnceListener()
    }


    fun connectDevice(deviceName: String?) {
        DreisamLib.getConnectManage().connectDevice(deviceName)
    }


    private fun setConnceListener() {
        setConnectLog()
        connectListener()
        realTimeDataCallBack()
        syncDatasListener()
    }

    private fun setConnectLog() {
        DreisamLib.getConnectManage().setConnectLog { state, msg ->
            AppLogUtils.debug("setConnectLog: " + msg)
            connectLogStr += (TimeUtils.formatHMSS(System.currentTimeMillis()) + ":  " + msg + "\n\n")
        }
    }

    private fun connectListener() {
        DreisamLib.getConnectManage().connectListener(object : OnConnectListener {
            override fun onConnectFail(state: DreisamConnectEnum?) {
                val iter: MutableIterator<OnConnectListener?> = onConnectListeners.iterator()
                while (iter.hasNext()) {
                    val listener: OnConnectListener? = iter.next()
                    listener?.onConnectFail(state)
                }
            }
            override fun onConnectState(state: DreisamConnectEnum) {
                val iter: MutableIterator<OnConnectListener?> = onConnectListeners.iterator()
                while (iter.hasNext()) {
                    val listener: OnConnectListener? = iter.next()
                    listener?.onConnectState(state)
                }
            }

            override fun onConnectSuccess() {
                val iter: MutableIterator<OnConnectListener?> = onConnectListeners.iterator()
                while (iter.hasNext()) {
                    val listener: OnConnectListener? = iter.next()
                    listener?.onConnectSuccess()
                }

            }

        })

    }

    private fun realTimeDataCallBack() {
        DreisamLib.getConnectManage().realTimeDataCallBack {
            MyApp.sharedPreferUtils.setGlucoseNew(it)
            NotifyUtils.sendGlucoseData(it)
            val iter: MutableIterator<OnAnalzeDatatListener?> = onAnalzeDatatListeners.iterator()
            while (iter.hasNext()) {
                val listener: OnAnalzeDatatListener? = iter.next()
                listener?.analzeData(it)
            }
        }
    }


    private fun syncDatasListener() {
        DreisamLib.getConnectManage().setSyncDatasListener(object : OnSyncDatasCallBack {
            override fun onSyncStart(totalCount: Int) {
                AppLogUtils.debug("onSyncStart:" + totalCount)
                val iter: MutableIterator<OnSyncDatasCallBack?> = onSyncDatasCallBacks.iterator()
                while (iter.hasNext()) {
                    val listener: OnSyncDatasCallBack? = iter.next()
                    listener?.onSyncStart(totalCount)
                }
            }

            override fun onSyncProgress(progress: Int) {
                AppLogUtils.debug("onSyncProgress:" + progress)
                val iter: MutableIterator<OnSyncDatasCallBack?> = onSyncDatasCallBacks.iterator()
                while (iter.hasNext()) {
                    val listener: OnSyncDatasCallBack? = iter.next()
                    listener?.onSyncProgress(progress)
                }
            }

            override fun onSyncComplete(success: Boolean, datas: List<DreisamGlucoseModel?>?) {
                AppLogUtils.debug("onSyncComplete:" + datas?.size)
                if (success) {
                    AppLogUtils.debug("The datas updated to the server")
                } else {
                    AppLogUtils.debug("It indicates that there is no new data, and the returned data is still the last one")
                }
                val lastData = datas?.last()
                lastData?.let {
                    MyApp.sharedPreferUtils.setGlucoseNew(it)
                    NotifyUtils.sendGlucoseData(it)
                }
                val iter: MutableIterator<OnSyncDatasCallBack?> = onSyncDatasCallBacks.iterator()
                while (iter.hasNext()) {
                    val listener: OnSyncDatasCallBack? = iter.next()
                    listener?.onSyncComplete(success, datas)
                }
            }

        })
    }

    fun destroy() {
        val entity = DreisamGlucoseModel()
        entity.type = 0
        NotifyUtils.sendGlucoseData(entity)
        connectLogStr = ""
        val devName = MyApp.sharedPreferUtils.getString(Constans.KEY_DEV_ID, "")
        MyApp.sharedPreferUtils.clear()
        DreisamLib.unInit()
        MyApp.sharedPreferUtils.putString(Constans.KEY_DEV_ID, devName)
        deleteFile(MyApp.context,"AndroidDreisamLog.txt")
    }

    fun addOnConnectListener(onConnectListener: OnConnectListener) {
        if (!onConnectListeners.contains(onConnectListener)) {
            this.onConnectListeners.add(onConnectListener)
        }

    }

    fun removeOnConnectListener(listener: OnConnectListener) {
        for (onConnectListener in onConnectListeners) {
            if (listener == onConnectListener)
                onConnectListeners.remove(listener)
        }
    }

    fun addOnAnalzeDatatListener(listener: OnAnalzeDatatListener) {
        if (!onAnalzeDatatListeners.contains(listener)) {
            this.onAnalzeDatatListeners.add(listener)
        }
    }

    fun removeAnalzeDatatListener(listener: OnAnalzeDatatListener) {
        for (onAnalzeDatatListener in onAnalzeDatatListeners) {
            if (listener == onAnalzeDatatListener)
                onAnalzeDatatListeners.remove(listener)
        }
    }

    fun addOnSyncDatasCallBack(listener: OnSyncDatasCallBack) {
        if (!onSyncDatasCallBacks.contains(listener)) {
            this.onSyncDatasCallBacks.add(listener)
        }
    }

    fun removeOnSyncDatasCallBack(listener: OnSyncDatasCallBack) {
        for (onSyncDatasCallBack in onSyncDatasCallBacks) {
            if (listener == onSyncDatasCallBack)
                onSyncDatasCallBacks.remove(listener)
        }
    }


    private fun getLogFile(context: Context, fileName: String): File {
        return File(context.cacheDir.path + "/${fileName}")
    }


    //导出
    fun shareLog(activity: BaseActivity) {
        activity.showLoading()
        val fileName = "AndroidDreisamLog.txt"
        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch {
            try {
                //创建文件
                val file =   makeFilePath(
                    MyApp.context.cacheDir.path,
                    "/$fileName"
                )
                val raf = RandomAccessFile(file, "rwd")
                raf.seek(file.length())
                raf.write(connectLogStr.toByteArray())
                raf.close()
            } catch (e: IOException) {
                activity.handler.post({
                    activity.hideLoading()
                    Toast.makeText(activity, "Export Fail", Toast.LENGTH_LONG)
                })
                e.printStackTrace() // 异常处理
            }
            activity.handler.post({
                //数据文件分享
                activity.hideLoading()
                val fileSharer = FileShare(activity)
                fileSharer.shareFile(getLogFile(MyApp.context, fileName), "*/*")
            })
        }
        runBlocking { job.join() }
        scope.cancel()

    }


    //导出
    fun saveCrashLog(msg:String) {
        val fileName = "AndroidDreisamLog.txt"
        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch {
            try {
                //创建文件
                val file =   makeFilePath(
                    MyApp.context.cacheDir.path,
                    "/$fileName"
                )
                val raf = RandomAccessFile(file, "rwd")
                raf.seek(file.length())
                raf.write(msg.toByteArray())
                raf.close()
            } catch (e: IOException) {
                e.printStackTrace() // 异常处理
            }

        }
        runBlocking { job.join() }
        scope.cancel()

    }

}