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
 * Common utility class
 */
object CommonUtil {
    private var dialogCommon: CommDialog? = null



    /**
     * Whether location check is required
     * Only Android 12 and below need location enabled; Android 12+ no longer requires location for Bluetooth scanning
     */
    fun isNeedCheckLocation(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    }

    /**
     * Whether location is enabled
     */
    fun isLocationEnabled(): Boolean {
        val context = MyApp.context
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Check whether Bluetooth is enabled
     */
    fun isBlueEnable(): Boolean {
        return BluetoothAdapter.getDefaultAdapter().isEnabled
    }


    /**
     * Show dialog prompting user to enable Bluetooth
     */
    fun showOpenBluetoothDialog(context: Context) {
        dialogCommon?.dismiss()
        dialogCommon = CommDialog(context)
        dialogCommon?.message = "Bluetooth is turned off. Please enable it to use the app normally."
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                openPhoneBluetooth(ActivityUtils.currentActivity())//Enable Bluetooth
            }
        }
        dialogCommon?.show()
    }


    /**
     * Warn user that required sensitive permissions are missing and app features may not work properly
     * @param context
     * @param msgRes Dialog message text
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
     * Show dialog prompting user to enable location
     */
    fun showOpenLocationDialog(context: Context) {
        dialogCommon?.dismiss()
        dialogCommon = CommDialog(context)
        dialogCommon?.message = "Location is turned off. Please enable it to use the app normally."
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                openPhoneLocation(ActivityUtils.currentActivity())//Enable location
            }
        }
        dialogCommon?.show()
    }


    /**
     * Enable Bluetooth
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
     * Enable location services
     */
    fun openPhoneLocation(context: Activity?) {
        // Request to enable location
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            context?.startActivityForResult(this, 1010)
        }
    }

    /**
     * Show dialog when Bluetooth permission is denied
     */
    fun showWarnUserPermissionNullBluetoothDialog(context: Context?, listener: CommDialog.OnSelectListener?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            showWarnUserPermissionNullDialog(
                context,
                "Bluetooth scan and connect permissions are missing, so app features may not work properly. Go to Settings to enable them?",
                listener
            )
        else
            showWarnUserPermissionNullDialog(
                context,
                "Location permission is missing, so app features may not work properly. Go to Settings to enable it?",
                listener
            )

    }

}
