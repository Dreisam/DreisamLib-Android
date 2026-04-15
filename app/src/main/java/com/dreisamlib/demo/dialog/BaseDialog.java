package com.dreisamlib.demo.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.dreisamlib.demo.R;


/**
 * 基础dialog
 */
public class BaseDialog extends Dialog {

    protected Context mContext;
    private Window mWindow;

    public BaseDialog(Context context) {
        this(context, R.style.BaseDialog);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mWindow = null;
        this.mContext = context;
        this.mWindow = this.getWindow();
    }

    protected void setDialogSize(int width, int height) {
        if (this.mWindow != null) {
            WindowManager.LayoutParams lp = this.mWindow.getAttributes();
            lp.width = width;
            if (height > 0) {
                lp.height = height;
            }

            this.mWindow.setAttributes(lp);
        }

    }

    public void show() {
        if (isShowing()) {
            return;
        }
        if (this.isActivityValid()) {
            try {
                super.show();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        }else{
        }
    }

    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    private boolean isActivityValid() {
        if (null != this.mContext && this.mContext instanceof Activity) {
            Activity at = (Activity) this.mContext;
            return !at.isFinishing();
        } else {
            return false;
        }
    }

}
