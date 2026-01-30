package com.dreisamlib.demo.utils

import android.content.Context
import android.os.Build
import com.dreisamlib.demo.app.MyApp
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import java.util.ArrayList

/**
 * 动态权限管理
 */
object PermissionManager {



    /**
     * 判断推送的通知权限
     */
    fun checkNotifyPermission(): Boolean {
        return isGrantedPermission(
            PermissionLists.getPostNotificationsPermission(),
        )
    }

    /**
     * 获取手机的推送通知权限
     */
    fun requestNotifyPermission(context: Context, callback: OnRequestPermissionCallback?) {
        if (checkNotifyPermission()) {
            callback?.let { it.onGranted(true) }
            return
        }
        XXPermissions.with(context)
            .permission(PermissionLists.getPostNotificationsPermission())
            .request { grantedList, deniedList ->
                if (deniedList.isNotEmpty()) {//失败
                    callback?.let { it.onGranted(false) }
                } else {
                    callback?.let { it.onGranted(true) }
                }
            }

    }




    /**
     * 判断安卓蓝牙权限是否授权了
     *  true:授予了 false：未授予
     */
    fun checkBluetoothScanConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isGrantedPermission(
            PermissionLists.getBluetoothScanPermission(),
            PermissionLists.getBluetoothConnectPermission()
        ) else isGrantedPermission(
            PermissionLists.getAccessFineLocationPermission(),
            PermissionLists.getAccessCoarseLocationPermission()
        )

    }


    /**
     * 互获取安卓蓝牙相关的权限
     * 蓝牙扫描和连接权限，安卓12
     * 安卓6.0权限 ACCESS_FINE_LOCATION、ACCESS_COARSE_LOCATION 定位权限
     * 安卓10权限  ACCESS_BACKGROUND_LOCATION 后台定位权限
     * 安卓12权限  BLUETOOTH_SCAN、BLUETOOTH_ADVERTISE、BLUETOOTH_CONNECT 蓝牙扫描
     *
     *  安卓11以上的要求  ACCESS_BACKGROUND_LOCATION 需要单独申请
     */
    fun requestBluetoothScanConnectPermission(
        context: Context?,
        callback: OnRequestPermissionCallback,
    ) {
        if (context == null) {
            callback.onGranted(false)
            return
        }
        val onPermissionCallback = OnPermissionCallback { grantedList, deniedList ->
            if (deniedList.isNotEmpty()) {//失败
                callback?.let { it.onGranted(false) }
            } else {
                callback?.let { it.onGranted(true) }
            }
        }
        if (checkBluetoothScanConnectPermission()) {
            callback?.onGranted(true)
            return
        }
        //安卓6.0~10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            XXPermissions.with(context)
                .permission(PermissionLists.getAccessFineLocationPermission())
                .permission(PermissionLists.getAccessCoarseLocationPermission())
                .request(onPermissionCallback)
        }
        //安卓10和安卓11不再申请后台定位权限，这个权限后面单独申请
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q || Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            XXPermissions.with(context)
                .permission(PermissionLists.getAccessFineLocationPermission())
                .permission(PermissionLists.getAccessCoarseLocationPermission())
                .request(onPermissionCallback)
        }
        //安卓12及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XXPermissions.with(context)
                .permission(PermissionLists.getBluetoothScanPermission())
                .permission(PermissionLists.getBluetoothConnectPermission())
                .request(onPermissionCallback)
        }
    }

    /**
     * 判断后台定位权限是否授予
     * 只有安卓10和安卓11才需要判断该权限，其它版本直接默认已经授权了
     */
    fun checkBackgroundLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q || Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            return isGrantedPermission(PermissionLists.getAccessBackgroundLocationPermission())
        }
        return true

    }


    /**
     * 申请后台定位权限
     */
    fun requestBackgroundLocationPermission(
        context: Context?,
        callback: OnRequestPermissionCallback?,
    ) {
        if (context == null) {
            callback?.onGranted(false)
            return
        }
        val onPermissionCallback = OnPermissionCallback { grantedList, deniedList ->
            if (deniedList.isNotEmpty()) {//失败
                callback?.let { it.onGranted(false) }
            } else {
                callback?.let { it.onGranted(true) }
            }
        }
        if (checkBackgroundLocationPermission()) {
            callback?.onGranted(true)
            return
        }

        //安卓10
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            XXPermissions.with(context)
                .permission(PermissionLists.getAccessBackgroundLocationPermission())
                .request(onPermissionCallback)
        }
        //安卓11以上（安卓11以上要求ACCESS_BACKGROUND_LOCATION权限单独申请）
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            XXPermissions.with(context)
                .permission(PermissionLists.getAccessFineLocationPermission())
                .permission(PermissionLists.getAccessCoarseLocationPermission())
                .request { grantedList, deniedList ->
                    if (deniedList.isNotEmpty()) {//失败
                        callback?.let { it.onGranted(false) }
                    } else {
                        XXPermissions.with(context)
                            .permission(PermissionLists.getAccessBackgroundLocationPermission())
                            .request(onPermissionCallback)
                    }
                }
        }
    }




    /**
     * 判断电池优化的权限是否授予
     */
    fun checkIgnoreBatteryOptimizationsPermission(): Boolean {
        return isGrantedPermission(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())
    }


    /**
     * 请求忽略电池优化的权限
     */
    fun requestIgnoreBatteryOptimizationsPermission(
        context: Context,
        callback: OnRequestPermissionCallback?
    ) {
        if (isGrantedPermission(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())) {
            callback?.let { it.onGranted(true) }
            return
        }
        XXPermissions.with(context)
            .permission(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())
            .request { grantedList, deniedList ->
                if (deniedList.isNotEmpty()) {//失败
                    callback?.let { it.onGranted(false) }
                } else {
                    callback?.let { it.onGranted(true) }
                }
            }
    }

    /**
     * 判断权限是否授予
     */
    private fun isGrantedPermission(vararg permissions: IPermission): Boolean {
        val data = ArrayList<IPermission>()
        for (permission in permissions) {
            data.add(permission)
        }
        return XXPermissions.isGrantedPermissions(MyApp.context , data)
    }

    interface OnRequestPermissionCallback {
        fun onGranted(allGranted: Boolean)
    }


}