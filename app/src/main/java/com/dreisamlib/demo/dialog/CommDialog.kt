package com.dreisamlib.demo.dialog


import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.dreisamlib.demo.R
import kotlin.let

/**
 * 通用提示dialog
 */
class CommDialog(context: Context?) : BaseDialog(context) {
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvCancel: TextView
    private lateinit var tvConfirm: TextView
    private lateinit var viewFgx: View
    var title: String? = null
    var message: String? = null
    var cancel: String? = null
    var confirm: String? = null
    var onSelectListener: OnSelectListener? = null

    init {
        val win = this.window
        win?.let {
            win!!.requestFeature(Window.FEATURE_NO_TITLE)
            win!!.decorView.setPadding(0, 0, 0, 0)
            val lp = win!!.attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.windowAnimations = R.style.ProtocolDialog
            lp.gravity = Gravity.CENTER
            win!!.attributes = lp
            win!!.setBackgroundDrawableResource(R.color.transparent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_comm)
//        setCanceledOnTouchOutside(false) //禁止触摸其它位置取消弹框
//        setCancelable(false) //禁止返回键取消弹框
        tvTitle = findViewById(R.id.tvTitle)
        viewFgx = findViewById(R.id.viewFgx)
        tvMessage = findViewById(R.id.tvMessage)
        tvCancel = findViewById(R.id.tvCancel)
        tvConfirm = findViewById(R.id.tvConfirm)
        title?.let { tvTitle.text = it }
        message?.let { tvMessage.text = it }
        cancel?.let { tvCancel.text = it }
        confirm?.let { tvConfirm.text = it }
        tvCancel.setOnClickListener {
            onSelectListener?.onCancel()
            dismiss()
        }
        tvConfirm.setOnClickListener {
            onSelectListener?.onConfirm()
            dismiss()
        }
    }

    fun setMsgData(msg: String){
        this.message = msg
        if (this::tvMessage.isInitialized){
            message?.let { tvMessage.text = it }
        }

    }

    fun hideCancel() {
        tvCancel?.visibility = View.GONE
    }

    fun hideConfirm() {
        tvConfirm?.visibility = View.GONE
    }

    fun hideViewFgx() {
        viewFgx?.visibility = View.GONE
    }



    /**
     * 确定和取消的选择
     */
    interface OnSelectListener {
        fun onConfirm() {

        }

        fun onCancel() {

        }
    }


}