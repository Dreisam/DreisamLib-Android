package com.dreisamlib.demo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.dreisamlib.demo.R
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.demo.ctrl.ConnectCtrl
import com.dreisamlib.demo.dialog.CommDialog
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.CommonUtil
import com.dreisamlib.demo.utils.NotifyUtils
import com.dreisamlib.demo.utils.PermissionManager
import com.dreisamlib.demo.utils.PermissionManager.requestBluetoothScanConnectPermission
import com.dreisamlib.lib.api.DreisamLib

/**
 * 登录/注册界面
 */
class LRActivity : BaseActivity(), View.OnClickListener {
    private lateinit var editText: EditText
    private var macName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lr)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.LR_BT_L).setOnClickListener(this)
        findViewById<View>(R.id.scanBt).setOnClickListener(this)
        editText = findViewById(R.id.LR_ET_USER_ID)

        val devName = MyApp.sharedPreferUtils.getString(Constans.KEY_DEV_ID, "")
        if (devName?.isEmpty() == true){
            editText.setText("DLS-11111")
        }else{
            editText.setText(devName)
        }
        AppLogUtils.debug("devName:$devName")
    }

    override fun onResume() {
        super.onResume()
        AppLogUtils.debug("onResume")

        AppLogUtils.debug("user:" + MyApp.sharedPreferUtils.getInt(Constans.KEY_USER_LOGIN))
        if (MyApp.sharedPreferUtils.getInt(Constans.KEY_USER_LOGIN) > 0) {
            macName = editText.text.toString()
            login()
        }
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.LR_BT_L) {
            macName = editText.text.toString()
            macName?.let {
                login()
            } ?: let {
                Toast.makeText(this, "DeviceID no empty", Toast.LENGTH_LONG)
            }

        } else if (v.getId() == R.id.scanBt) {
            startActivityForResult(Intent(this, ScanActivity::class.java), 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 200) {
            editText.setText(data?.getStringExtra("id"))
        }
    }


    fun login() {
        if (!CommonUtil.isBlueEnable()) {
            CommonUtil.showOpenBluetoothDialog(this)
            return
        }
        if (!CommonUtil.isLocationEnabled() && CommonUtil.isNeedCheckLocation()) {
            CommonUtil.showOpenLocationDialog(this)
            return
        }
        DreisamLib.getConnectManage().checkPreConditions { granted, missingPermissions ->
            if (granted){
                PermissionManager.requestNotifyPermission(
                    this,
                    object : PermissionManager.OnRequestPermissionCallback {
                        override fun onGranted(allGranted: Boolean) {
                            if (allGranted) {
                                showBatteryNeedsSettingDialog()
                            } else {
                                showWarnPer()
                            }
                        }
                    })
            }else{
                requestBluetoothScanConnectPermission(this, object : PermissionManager.OnRequestPermissionCallback {
                    override fun onGranted(allGranted: Boolean) {
                        if (allGranted) {
                            login()
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
        if (PermissionManager.checkIgnoreBatteryOptimizationsPermission() || MyApp.sharedPreferUtils.getInt(Constans.KEY_USER_LOGIN) > 0) {
            goMain()
            return
        }
        val dialog = CommDialog(this)
        dialog.message = "The system may automatically close the application. To ensure that the application can obtain blood sugar data normally, please go to the phone Settings to turn off the battery optimization of the application or set it to unlimited."
        dialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                PermissionManager.requestIgnoreBatteryOptimizationsPermission(
                    this@LRActivity,
                    object : PermissionManager.OnRequestPermissionCallback {
                        override fun onGranted(allGranted: Boolean) {
                            goMain()
                        }
                    })
            }
        }
        dialog.show()
    }


    fun goMain(){
        ConnectCtrl.initSDK()
        MyApp.sharedPreferUtils.putInt(Constans.KEY_USER_LOGIN, 1)
        AppLogUtils.debug("goMain macName:" + macName)
        MyApp.sharedPreferUtils.putString(Constans.KEY_DEV_ID, macName)
        NotifyUtils.createNotificationChannel()
        NotifyUtils.sendGlucoseService()
        startActivity(Intent(this@LRActivity, MainActivity::class.java))
        finish()
    }


    fun showWarnPer() {
        val dialogCommon = CommDialog(this)
        dialogCommon?.message = "Without notification permission, the application function will not work properly. Do you want to enable permissions in the Settings?"
        dialogCommon?.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", this@LRActivity?.packageName, null)
                intent.setData(uri)
                this@LRActivity?.startActivity(intent)

            }

            override fun onCancel() {
                super.onCancel()
            }
        }
        dialogCommon?.show()
    }


}