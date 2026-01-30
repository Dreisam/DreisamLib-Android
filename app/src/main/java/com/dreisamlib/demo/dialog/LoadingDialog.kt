package com.dreisamlib.demo.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.dreisamlib.demo.R
import kotlin.concurrent.Volatile

class LoadingDialog private constructor() {
    private var dialog: Dialog? = null

    /**
     * 创建【自定义】对话框（位置：中心）
     *
     * @param view        对话框的layout
     * @param dialogStyle 对话框样式
     * @param width       对话框宽度（-1为默认宽度）
     * @param height      对话框高度（-1为默认高度）
     * @return 对话框对象
     */
    fun createCenterDialog(activity: Activity?, view: View, dialogStyle: Int, width: Int, height: Int): Dialog? {
        if (activity == null || activity.isFinishing()) return null
        dismissDialog()
        dialog = Dialog(activity, if (dialogStyle == 0) R.style.BaseDialog else dialogStyle)
        dialog!!.setContentView(view)
        setDialogWidthHeightGravity(dialog!!, width, height, Gravity.CENTER, -1)
        return dialog
    }

    /**
     * 创建和显示【加载中】对话框
     *
     * @param isCancelable        能否取消对话框
     * @param isCancelableOutside 点击外部能否取消对话框
     */
    fun createAndShowLoadingDialog(activity: Activity?, isCancelable: Boolean, isCancelableOutside: Boolean) {
        if (activity == null || activity.isFinishing()) return
        dialog = Dialog(activity, R.style.BaseNoFrameDialog)
        dialog!!.setContentView(View.inflate(activity, R.layout.dialog_loding, null))
        setDialogWidthHeightGravity(dialog!!, -1, -1, Gravity.CENTER, -1)
        if (dialog == null) return
        dialog!!.setCancelable(isCancelable)
        dialog!!.setCanceledOnTouchOutside(isCancelableOutside)
        dialog!!.show()
    }

    /**
     * 设置对话框的宽和高
     * --- gravity属性中如果传入Gravity.CENTER则另一个属性将不再处理
     * --- gravity属性其他时候必须都传入正确的值，或者传-1使用默认属性
     *
     * @param dialog   对话框
     * @param width    对话框的宽度
     * @param height   对话框的高度
     * @param gravity1 位置属性1（Gravity.XXX）传-1使用默认属性
     * @param gravity2 位置属性2（Gravity.XXX）传-1使用默认属性
     */
    fun setDialogWidthHeightGravity(dialog: Dialog, width: Int, height: Int, gravity1: Int, gravity2: Int) {
        val dialogWindow = dialog.getWindow()
        if (dialogWindow != null) {
            val p = dialogWindow.getAttributes()
            p.width = if (width > 0) width else ViewGroup.LayoutParams.MATCH_PARENT
            p.height = if (height > 0) height else ViewGroup.LayoutParams.WRAP_CONTENT

            // 其中一个属性为中心
            if (gravity1 == Gravity.CENTER || gravity2 == Gravity.CENTER) {
                p.gravity = Gravity.CENTER
            } else if (gravity1 == -1 || gravity2 == -1) {
                p.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            } else {
                p.gravity = gravity1 or gravity2
            }
            dialogWindow.setAttributes(p)
        }
    }

    fun createAndShowLoadingDialog(activity: Activity?) {
        createAndShowLoadingDialog(activity, true, true)
    }


    /**
     * 消除对话框
     */
    fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing()) {
            try {
                dialog!!.dismiss()
            } catch (e: Exception) {
                dialog = null
            }
        }
        dialog = null
    }

    companion object {
        @Volatile
        private var dialogUtils =  LoadingDialog()

        val instance: LoadingDialog
            // 获取单例
            get() {
                return dialogUtils
            }
    }
}
