package com.dreisamlib.demo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dreisamlib.demo.R
import com.dreisamlib.demo.adapter.ItemBloodSugarInfoAdapter
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.demo.ctrl.ConnectCtrl
import com.dreisamlib.demo.dialog.CommDialog
import com.dreisamlib.demo.service.DlsForegroundService
import com.dreisamlib.demo.utils.ActivityUtils
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.CommonUtil
import com.dreisamlib.demo.utils.NotifyUtils
import com.dreisamlib.demo.utils.TimeUtils
import com.dreisamlib.lib.bean.DreisamConnectEnum
import com.dreisamlib.lib.bean.DreisamGlucoseModel
import com.dreisamlib.lib.listener.OnAnalzeDatatListener
import com.dreisamlib.lib.listener.OnConnectListener
import com.dreisamlib.demo.utils.PermissionManager.OnRequestPermissionCallback
import com.dreisamlib.lib.listener.OnSyncDatasCallBack
import com.dreisamlib.demo.utils.PermissionManager
import com.dreisamlib.demo.utils.PermissionManager.requestBluetoothScanConnectPermission
import com.dreisamlib.lib.api.DreisamLib
import java.util.Collections


class MainActivity : BaseActivity(), View.OnClickListener {

    private lateinit var connectStateTv: TextView
    private lateinit var devConnectStateTv: TextView
    private lateinit var deviceNameTv: TextView
    private lateinit var glucoseTv: TextView
    private lateinit var timeTv: TextView
    private lateinit var syncLayout: View
    private lateinit var syncMaxTv: TextView
    private lateinit var syncTv: TextView
    private lateinit var rssiTv: TextView
    private lateinit var proBar: ProgressBar
    private lateinit var proLoading: ProgressBar
    private lateinit var viewStates: View

    var devName: String? = null
    private var isBackgroundLocationWarned = false//后台定位权限是否提醒过了
    private lateinit var commDialog: CommDialog

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemBloodSugarInfoAdapter
    private var isFrist = true


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initView()
        initData()
    }

    fun initView() {
        connectStateTv = findViewById(R.id.connectStateTv)
        deviceNameTv = findViewById(R.id.deviceNameTv)
        glucoseTv = findViewById(R.id.glucoseTv)

        timeTv = findViewById(R.id.timeTv)
        syncLayout = findViewById(R.id.syncLayout)
        syncMaxTv = findViewById(R.id.syncMaxTv)
        syncTv = findViewById(R.id.syncTv)
        proBar = findViewById(R.id.proBar)

        proLoading = findViewById(R.id.proLoading)
        viewStates = findViewById(R.id.viewStates)
        devConnectStateTv = findViewById(R.id.devConnectStateTv)
        rssiTv = findViewById(R.id.rssiTv)


        findViewById<View>(R.id.logout).setOnClickListener(this)
        findViewById<View>(R.id.dataLayout).setOnClickListener(this)
        findViewById<View>(R.id.logTv).setOnClickListener(this)

        commDialog = CommDialog(this)
        commDialog.title = "Log"
        commDialog.confirm = "Share"
        commDialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                ConnectCtrl.shareLog(this@MainActivity)
            }
        }

        recyclerView = findViewById(R.id.rvBloodSugar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemBloodSugarInfoAdapter(this)
        recyclerView.adapter = adapter


    }

    fun showLogDialog() {
        commDialog.setMsgData(ConnectCtrl.connectLogStr)
        commDialog.show()
    }


    fun initData() {
        devName = MyApp.sharedPreferUtils.getString(Constans.KEY_DEV_ID, "")
        AppLogUtils.debug("Connect Device：$devName  $devName")
        deviceNameTv.text = "$devName"

        val lastData = MyApp.sharedPreferUtils.getGlucoseNew()
        lastData?.let {
            if (it.type == 0) {
                glucoseTv.setText("Warm-Up")
            } else {
                glucoseTv.setText(it.glucose.toString())
            }
            timeTv.setText(TimeUtils.formatHM(it.timeCreate * 1000))
            NotifyUtils.sendGlucoseData(it)
        }

        ConnectCtrl.addOnConnectListener(onConnectListener)
        ConnectCtrl.addOnAnalzeDatatListener(onAnalzeDatatListener)
        ConnectCtrl.addOnSyncDatasCallBack(onSyncDatasCallBack)

        showBatteryNeedsSettingDialog()//申请定位权限和电池忽略权限

    }

    private fun getBloodSugarData() {
        val startTime = TimeUtils.getTimeStartFromDay(System.currentTimeMillis()) / 1000
        val endTime = TimeUtils.getTimeEndFromDay(System.currentTimeMillis()) / 1000

        AppLogUtils.debug("startTime:$startTime  endTime:$endTime")
        DreisamLib.getConnectManage().getHistory(startTime, endTime) { datas ->
            Collections.sort(datas) { p0, p1 -> (p1.timeCreate - p0.timeCreate).toInt() }
            adapter.setDataList(datas)
        }

    }

    override fun onResume() {
        super.onResume()
        connectDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectCtrl.removeOnConnectListener(onConnectListener)
        ConnectCtrl.removeAnalzeDatatListener(onAnalzeDatatListener)
        ConnectCtrl.removeOnSyncDatasCallBack(onSyncDatasCallBack)
        isFrist = true
    }

    val onConnectListener = object : OnConnectListener {
        override fun onConnectFail(state: DreisamConnectEnum?) {
            if (state == DreisamConnectEnum.BLE_OFF) {
                devConnectStateTv.text = "Ble：Off"
                ActivityUtils.currentActivity()?.baseContext?.let {
                    CommonUtil.showOpenBluetoothDialog(it)
                }
                Toast.makeText(this@MainActivity, "Ble Off", Toast.LENGTH_LONG)
            }else if (state == DreisamConnectEnum.AUTHENTICATION_FAIL) {
                proLoading.visibility = View.VISIBLE
                viewStates.visibility = View.GONE
                devConnectStateTv.text = "Auth Fail"
                connectStateTv.text = "Authentication Fail"
            }else if (state == DreisamConnectEnum.LACK_PERMISSION) {
                connectStateTv.text = "No Permission"
                devConnectStateTv.text = "No Permission"
                if (!CommonUtil.isBlueEnable()) {
                    AppLogUtils.debug("Ble Off")
                    Toast.makeText(this@MainActivity, "Ble Off", Toast.LENGTH_LONG)
                    return
                }
                if (!CommonUtil.isLocationEnabled() && CommonUtil.isNeedCheckLocation()) {
                    AppLogUtils.debug("No Location Permission")
                    Toast.makeText(this@MainActivity, "No Location Permission", Toast.LENGTH_LONG)
                    return
                }

                if (!PermissionManager.checkBluetoothScanConnectPermission()) {
                    AppLogUtils.debug("No Ble Permission")
                    Toast.makeText(this@MainActivity, "No Ble Permission", Toast.LENGTH_LONG)
                    return
                }

            }else if (state == DreisamConnectEnum.DEVICE_FINISH) {
                proLoading.visibility = View.VISIBLE
                viewStates.visibility = View.GONE
                devConnectStateTv.text = "Device Finish"
                connectStateTv.text = "Device Finish"
            }
        }

        override fun onConnectState(state: DreisamConnectEnum?) {
            AppLogUtils.debug("State：$state")
            if (state == DreisamConnectEnum.SHOW_CONNECTING) {
                proLoading.visibility = View.VISIBLE
                viewStates.visibility = View.GONE
                connectStateTv.text = "Connecting"
            }   else if (state == DreisamConnectEnum.DEVICE_DISCONNECT) {
                devConnectStateTv.text = "Ble：Disconnect"

            }
        }

        override fun onConnectSuccess() {
            connectStateTv.text = "Connected"
            devConnectStateTv.text = "Ble：Connected"
            proLoading.visibility = View.GONE
            viewStates.visibility = View.VISIBLE
            if (isFrist) getBloodSugarData()
            isFrist = false
            handler.postDelayed({ AppLogUtils.debug("deviceInfo:${DreisamLib.getConnectManage().deviceInfo}")  }, 1000)
        }

    }

    val onAnalzeDatatListener = OnAnalzeDatatListener {
        AppLogUtils.debug("realTimeDataCallBack: " + it.printMessage())
        if (it.type == 0) {
            glucoseTv.setText("Warm-Up")
        } else {
            glucoseTv.setText(it.glucose.toString())
        }

        timeTv.setText(TimeUtils.formatHM(it.timeCreate * 1000))
        if (it.type != 0)
            adapter.addFirstData(it)
    }

    val onSyncDatasCallBack = object : OnSyncDatasCallBack {
        override fun onSyncStart(totalCount: Int) {
            handler.post {
                syncLayout.visibility = View.VISIBLE
                syncMaxTv.text = "Total：$totalCount"
                proBar.visibility = View.VISIBLE
            }
        }

        override fun onSyncProgress(progress: Int) {
            handler.post {
                syncTv.text = "Progress：$progress"
            }
        }

        override fun onSyncComplete(success: Boolean, datas: List<DreisamGlucoseModel?>?) {
            handler.post {
                syncLayout.visibility = View.GONE
                syncTv.text = ""
                syncMaxTv.text = ""
                proBar.visibility = View.GONE
                AppLogUtils.debug("SyncComplete data：" + datas?.size + " start： " + datas?.get(0)?.printMessage() + "  end：" + datas?.last()?.printMessage())
                val lastData = datas?.last()
                lastData?.let {
                    if (it.type == 0) {
                        glucoseTv.setText("Warm-Up")
                    } else {
                        glucoseTv.setText(it.glucose.toString())
                    }

                    timeTv.setText(TimeUtils.formatHM(it.timeCreate * 1000))
                }
                Toast.makeText(this@MainActivity, "Sync Complete", Toast.LENGTH_LONG)
                getBloodSugarData()

            }

        }
    }


    private fun connectDevice() {
        if (!CommonUtil.isBlueEnable()) {
            CommonUtil.showOpenBluetoothDialog(this)
            return
        }
        if (!CommonUtil.isLocationEnabled() && CommonUtil.isNeedCheckLocation()) {
            CommonUtil.showOpenLocationDialog(this)
            return
        }

        DreisamLib.getConnectManage().checkPreConditions { granted, missingPermissions ->
            if (granted) {
                connectStateTv.text = "Connecting"
                ConnectCtrl.connectDevice(devName)
                DlsForegroundService.startService(this@MainActivity)//启动前台服务
                showBackGroundLocationPermissionDialog()
            } else {
                requestBluetoothScanConnectPermission(
                    this, object : OnRequestPermissionCallback {
                        override fun onGranted(allGranted: Boolean) {
                            if (allGranted) {
                                connectStateTv.text = "Connecting"
                                ConnectCtrl.connectDevice(devName)
                                DlsForegroundService.startService(this@MainActivity)//启动前台服务
                                showBackGroundLocationPermissionDialog()
                            } else {
                                CommonUtil.showWarnUserPermissionNullBluetoothDialog(this@MainActivity, null)
                            }
                        }

                    })
            }
        }
    }


    /**
     * 显示申请电池优化询问的弹框(因为文案是AI翻译的，暂时先不使用)
     */
    private fun showBatteryNeedsSettingDialog() {
        if (PermissionManager.checkIgnoreBatteryOptimizationsPermission()) {
            return
        }
        val dialog = CommDialog(this)
        dialog.message =
            "The system may automatically close the application. To ensure that the application can obtain blood sugar data normally, please go to the phone Settings to turn off the battery optimization of the application or set it to unlimited."
        dialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                requestIgnoreBatteryOptimizationsPermission()
            }
        }
        dialog.show()
    }


    /**
     *
     */
    private fun logout() {
        val dialog = CommDialog(this)
        dialog.message = "Logout will clear all data. Operate with caution"
        dialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                ConnectCtrl.destroy()
                startActivity(Intent(this@MainActivity, LRActivity::class.java))
                finish()
            }
        }
        dialog.show()
    }


    /**
     * 申请电池优化忽略的权限
     */
    private fun requestIgnoreBatteryOptimizationsPermission() {
        PermissionManager.requestIgnoreBatteryOptimizationsPermission(
            this, object : OnRequestPermissionCallback {
                override fun onGranted(allGranted: Boolean) {
                }
            })
    }


    /**
     * 显示后台定位权限的提醒弹框
     * 该弹框主要让用户说明后台定位权限申请的重要性
     */
    private fun showBackGroundLocationPermissionDialog() {
        if (isBackgroundLocationWarned) return
        isBackgroundLocationWarned = true
        if (PermissionManager.checkBackgroundLocationPermission()) return
        val dialog = CommDialog(this)
        dialog.message =
            "Background location permission is required to support automatic reconnection of Bluetooth devices when the APP is running in the background. Otherwise, this function will not work."
        dialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                PermissionManager.requestBackgroundLocationPermission(this@MainActivity, null)
            }
        }
        dialog.show()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.logout) {
            logout()
        } else if (v?.id == R.id.dataLayout) {
            startActivity(Intent(this, DataListActivity::class.java))
        } else if (v?.id == R.id.logTv) {
            showLogDialog()
        }
    }


}