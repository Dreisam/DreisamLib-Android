package com.dreisamlib.demo.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.WindowManager
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.dialog.CommDialog
import kotlin.apply
import kotlin.let
import kotlin.text.toFloat
import kotlin.text.toInt
import kotlin.text.toLong
import kotlin.toString


/**
 * 通用工具类
 */
object CommonUtil {
    private var dialogCommon: CommDialog? = null



    /**
     * 是否需要检验定位
     * 安卓12以下才需要检验定位是否开启，安卓12及以上蓝牙扫描不再需要定位
     */
    fun isNeedCheckLocation(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    }

    /**
     * 定位是否开启
     */
    fun isLocationEnabled(): Boolean {
        val context = MyApp.context
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * 检查蓝牙是否开启
     */
    fun isBlueEnable(): Boolean {
        return BluetoothAdapter.getDefaultAdapter().isEnabled
    }


    /**
     * 显示打开手机蓝牙的弹框
     */
    fun showOpenBluetoothDialog(context: Context) {
        dialogCommon?.dismiss()
        dialogCommon = CommDialog(context)
        dialogCommon?.message = "蓝牙开关已关闭，需要开启才能正常使用应用。"
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                openPhoneBluetooth(ActivityUtils.currentActivity())//打开蓝牙开关
            }
        }
        dialogCommon?.show()
    }


    /**
     * 提醒用户，敏感权限缺失，APP功能无法正常使用
     * @param context
     * @param msgRes 弹框提示文案
     * @param permissions
     */
    fun showWarnUserPermissionNullDialog(
        context: Context?,
        msg: String,
        listener: CommDialog.OnSelectListener?
    ) {
        dialogCommon?.dismiss()
        dialogCommon = CommDialog(context)
        dialogCommon?.message = msg
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                listener?.onConfirm()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context?.packageName, null)
                intent.setData(uri)
                context?.startActivity(intent)
            }

            override fun onCancel() {
                super.onCancel()
                listener?.onCancel()
            }
        }
        dialogCommon?.show()
    }

    /**
     * 显示打开手机定位的弹框
     */
    fun showOpenLocationDialog(context: Context) {
        dialogCommon?.dismiss()
        dialogCommon = CommDialog(context)
        dialogCommon?.message = "位置开关已关闭，需要开启才能正常使用应用。"
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                openPhoneLocation(ActivityUtils.currentActivity())//打开蓝牙开关
            }
        }
        dialogCommon?.show()
    }


    /**
     * 打开蓝牙开关
     */
    fun openPhoneBluetooth(context: Activity?) {
        PermissionManager.requestBluetoothScanConnectPermission(
            context,
            object : PermissionManager.OnRequestPermissionCallback {
                override fun onGranted(allGranted: Boolean) {
                    if (allGranted) {
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                            context?.startActivityForResult(this, 1010)
                        }
                    } else {
                        showWarnUserPermissionNullBluetoothDialog(context, null)
                    }
                }
            })
    }

    /**
     * 打开定位开关
     */
    fun openPhoneLocation(context: Activity?) {
        // 请求打开蓝牙
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            context?.startActivityForResult(this, 1010)
        }
    }

    /**
     * 显示蓝牙权限被拒绝的弹框
     */
    fun showWarnUserPermissionNullBluetoothDialog(context: Context?, listener: CommDialog.OnSelectListener?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            showWarnUserPermissionNullDialog(
                context,
                "没有蓝牙扫描和蓝牙连接权限，应用功能将无法正常工作。您要去设置中开启权限吗？",
                listener
            )
        else
            showWarnUserPermissionNullDialog(
                context,
                "没有位置权限，应用功能将无法正常工作。您要去设置中开启权限吗？",
                listener
            )

    }

}