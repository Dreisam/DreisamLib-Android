package com.dreisamlib.demo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.dreisamlib.demo.R
import com.dreisamlib.demo.app.BaseActivity
import com.dreisamlib.demo.app.MyApp
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.demo.ctrl.ConnectCtrl
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.ToastUtil
import com.dreisamlib.lib.api.DreisamLib
import com.dreisamlib.lib.bean.DrisamDeviceModel
import com.dreisamlib.lib.bean.DreisamBindingEnum
import com.dreisamlib.lib.listener.OnBindListener


/**
 * BindDevice
 */
class BindActivity : BaseActivity(), View.OnClickListener {
    private var macName: String? = null
    private lateinit var scanBt : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)
        initView()
    }

    private fun initView() {
        scanBt =  findViewById<Button>(R.id.scanBt)
        scanBt.setOnClickListener(this)
        findViewById<View>(R.id.logout).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.scanBt) {
            startActivityForResult(Intent(this, ScanActivity::class.java), 200)
        }else if (v.getId() == R.id.logout){
            ConnectCtrl.logout()
            startActivity(Intent(this@BindActivity, LRActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 200) {
            macName = (data?.getStringExtra("sn"))
            showLoading()
            DreisamLib.getConnectManage().bindDevice(macName, object : OnBindListener {
                override fun onBindFail(code: Int, msg: String?) {
                    AppLogUtils.debug("onBindFail:" + msg)
                    hideLoading()
                    ToastUtil.showToast(msg)
                    scanBt.text  = "Go to Bind"
                }

                override fun onBindSuccess(drisamDeviceModel: DrisamDeviceModel) {
                    hideLoading()
                    if (!drisamDeviceModel.deviceSn.isEmpty()) {
                        AppLogUtils.debug("goMain macName:" + macName)
                        MyApp.sharedPreferUtils.putString(Constans.KEY_DEV_NAME, macName)
                        startActivity(Intent(this@BindActivity, MainActivity::class.java))
                        finish()
                        scanBt.text  = "Go to Bind"
                    }
                }

                override fun onBinding(bindingCode: DreisamBindingEnum) {
                    scanBt.text =  if (bindingCode == DreisamBindingEnum.CHEACKING){
                        "Verificationing"
                    }else if (bindingCode == DreisamBindingEnum.SCANTING){
                        "Scanning"
                    }else if (bindingCode == DreisamBindingEnum.CONNECTING){
                        "Connecting"
                    }else if (bindingCode == DreisamBindingEnum.INTERACTING){
                        "Connecting"
                    }else {
                        "Binding"
                    }

                }

            })
        }
    }
}