package com.dreisamlib.demo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dreisamlib.demo.R
import com.google.zxing.Result
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView

/**
 * 通过扫描二维码添加设备
 */
class ScanActivity : BaseActivity(), ZXingScannerView.ResultHandler {
    private var zXingScannerView: ZXingScannerView? = null
    private val REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device_by_scan)
        initView()
    }

    private fun initView() {
        val frameLayout = findViewById<FrameLayout>(R.id.SCAN_ZX)
        zXingScannerView = object : ZXingScannerView(this) {
            override fun createViewFinderView(context: Context?): IViewFinder {
                return CustomViewFinderView(context)
            }
        }
        zXingScannerView!!.setLaserEnabled(true)
        frameLayout.addView(zXingScannerView)
        findViewById<View?>(R.id.back).setOnClickListener { v: View? -> finish() }
    }


    override fun onResume() {
        super.onResume()
        if (!checkCameraPermission()) {
            requestCameraPermission()
        } else {
            zXingScannerView!!.setResultHandler(this)
            zXingScannerView!!.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        zXingScannerView!!.stopCamera()
    }

    override fun handleResult(result: Result) {
        if (!TextUtils.isEmpty(result.getText())) {
            val UUID = result.getText()
            val intent = Intent()
            intent.putExtra("id",UUID)
            setResult(RESULT_OK,intent)
            finish()

        } else {
            resumeCameraPreview()
        }
    }


    /*从新扫描*/
    private fun resumeCameraPreview() {
        Handler().postDelayed(Runnable { zXingScannerView!!.resumeCameraPreview(this@ScanActivity) }, 2000)
    }

    /*检查相机权限*/
    fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    /*请求相机权限*/
    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.CAMERA), REQUEST_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.size > 0) {
            var hasPermissions = true
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    hasPermissions = false
                    break
                }
            }
            if (!hasPermissions) {
                Toast.makeText(this, "You need to enable the \"Camera\" permission to use this function", 2000)
            } else {
                zXingScannerView!!.setResultHandler(this)
                zXingScannerView!!.startCamera()
            }
        }
    }

    private  class CustomViewFinderView : ViewFinderView {
        constructor(context: Context?) : super(context) {
            setSquareViewFinder(true)
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            setSquareViewFinder(true)
        }
    }
}
