package com.dreisamlib.demo.ui

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dreisamlib.demo.utils.ActivityUtils

open class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    var handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSystemUi(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        ActivityUtils.addActivity(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        ActivityUtils.finishActivity(this)
    }

    fun setSystemUi(flag: Int) {
        //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        val window = getWindow()
        val decorView = getWindow().getDecorView()
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or flag
        decorView.setSystemUiVisibility(option)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(Color.TRANSPARENT)

        //导航栏颜色也可以正常设置
//            window.setNavigationBarColor(Color.TRANSPARENT);
    }



    fun showLoading() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
        progressDialog?.setCancelable(true)
        progressDialog?.show()
    }

    fun hideLoading() {
        if (progressDialog != null) if (progressDialog!!.isShowing()) {
            progressDialog!!.dismiss()
        }
    }
}