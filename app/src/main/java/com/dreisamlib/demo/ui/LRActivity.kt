package com.dreisamlib.demo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.dreisamlib.demo.R
import com.dreisamlib.demo.app.BaseActivity
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.demo.dialog.CommDialog
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.CommonUtil
import com.dreisamlib.demo.utils.NotifyUtils
import com.dreisamlib.demo.utils.PermissionManager
import com.dreisamlib.demo.utils.PermissionManager.OnRequestPermissionCallback
import com.dreisamlib.demo.utils.PermissionManager.requestBluetoothScanConnectPermission
import com.dreisamlib.demo.utils.ToastUtil
import com.dreisamlib.lib.api.DreisamLib
import com.dreisamlib.lib.bean.DrisamDeviceModel
import com.dreisamlib.lib.listener.OnLoginListener


/**
 * 登录/注册界面
 */
class LRActivity : BaseActivity(), View.OnClickListener {
    private lateinit var editText: EditText
    private var token: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lr)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.LR_BT_L).setOnClickListener(this)
        val jwt = "you token"

        editText = findViewById(R.id.LR_ET_USER_ID)
        token = MyApp.sharedPreferUtils.getString(Constans.KEY_USER_TOKEN, "")
        if (token?.isNotEmpty() == true) {
            editText.setText(token)
        } else {
            editText.setText(jwt)
        }



    }


    override fun onResume() {
        super.onResume()
        AppLogUtils.debug("onResume")
        if (token?.isNotEmpty() == true) {
            login()
        }
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.LR_BT_L) {
            token = editText.text.toString()
            token?.let {
                login()
            } ?: let {
                Toast.makeText(this, "token no empty", Toast.LENGTH_LONG)
            }
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
            if (granted) {
                PermissionManager.requestNotifyPermission(
                    this,
                    object : OnRequestPermissionCallback {
                        override fun onGranted(allGranted: Boolean) {
                            if (allGranted) {
                                showBatteryNeedsSettingDialog()
                            } else {
                                showWarnPer()
                            }
                        }
                    })
            } else {
                requestBluetoothScanConnectPermission(this, object : OnRequestPermissionCallback {
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
     *
     */
    private fun showBatteryNeedsSettingDialog() {
        if (PermissionManager.checkIgnoreBatteryOptimizationsPermission()) {
            goMain()
            return
        }
        val dialog = CommDialog(this)
        dialog.message =
            "The system may automatically close the application. To ensure that the application can obtain blood sugar data normally, please go to the phone Settings to turn off the battery optimization of the application or set it to unlimited."
        dialog.onSelectListener = object : CommDialog.OnSelectListener {
            override fun onConfirm() {
                super.onConfirm()
                PermissionManager.requestIgnoreBatteryOptimizationsPermission(
                    this@LRActivity,
                    object : OnRequestPermissionCallback {
                        override fun onGranted(allGranted: Boolean) {
                            goMain()
                        }
                    })
            }
        }
        dialog.show()
    }


    fun goMain() {
        val areaCode = "test"
        showLoading()
        DreisamLib.getConnectManage().login(areaCode, token, object : OnLoginListener {


            override fun loginSucc(hasAvailableDevice: Boolean, deviceInfo: DrisamDeviceModel) {
                hideLoading()
                MyApp.sharedPreferUtils.putString(Constans.KEY_USER_TOKEN, token)
                AppLogUtils.debug("goMain token:" + token)
                NotifyUtils.createNotificationChannel()
                NotifyUtils.sendGlucoseService()
                //如果没有可用设备则需要去绑定。
                if (hasAvailableDevice) {
                    MyApp.sharedPreferUtils.putString(Constans.KEY_DEV_NAME, deviceInfo.deviceSn)
                    startActivity(Intent(this@LRActivity, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@LRActivity, BindActivity::class.java))
                    finish()
                }
            }

            override fun loginFail(code: Int, msg: String?) {
                hideLoading()
                AppLogUtils.debug("loginFail: $code  $msg")
                MyApp.sharedPreferUtils.putString(Constans.KEY_USER_TOKEN, "")
                ToastUtil.showToast(msg)
            }

        })

    }


    fun showWarnPer() {
        val dialogCommon = CommDialog(this)
        dialogCommon?.message =
            "Without notification permission, the application function will not work properly. Do you want to enable permissions in the Settings?"
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