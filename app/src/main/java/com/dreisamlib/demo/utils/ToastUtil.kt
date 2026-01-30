package com.dreisamlib.demo.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.dreisamlib.demo.app.MyApp

object ToastUtil {
    fun showToast(message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        val context: Context = MyApp.context ?: return
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (ignore: Throwable) {
        }
    }

    fun showToast(id: Int) {
        val context: Context = MyApp.context ?: return
        val message = MyApp.context.getString(id)
        if (TextUtils.isEmpty(message)) {
            return
        }
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (ignore: Throwable) {
        }
    }
}