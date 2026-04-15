package com.dreisamlib.demo.utils;

import android.util.Log;

/**
 * LOG 工具类
 *
 * @author ZCH
 * @version V1.2
 * @date 2025/12/02
 */
public class AppLogUtils {
    private static final String TAG = "DLS_App";

    private static boolean IS_SHOW_LOG = true;

    public static void info(String message) {
        logI(TAG+"-I", message);
    }

    public static void info(Object message) {
        info(message + "");
    }

    public static void info(String msg, Object... format) {
        info(String.format(msg, format));
    }

    public static void debug(String message) {
        logD(TAG+"-D", message);
    }

    public static void debug(Object message) {
        debug(message + "");
    }

    public static void debug(String msg, Object... format) {
        debug(String.format(msg, format));
    }

    public static void error(String message) {
        logE(TAG+"-E", message);
    }

    public static void error(Object message) {
        error(message + "");
    }

    public static void error(String msg, Object... format) {
        error(String.format(msg, format));
    }

    public static void exception(Exception e) {
        if (IS_SHOW_LOG && e != null) {
            e.printStackTrace();
        }
    }

    public static void logE(String tag, String message) {
        if (IS_SHOW_LOG) {
            Log.e(tag, message);
        }
    }

    public static void logD(String tag, String message) {
        if (IS_SHOW_LOG) {
            Log.d(tag, message);
        }
    }

    public static void logI(String tag, String message) {
        if (IS_SHOW_LOG) {
            Log.i(tag, message);
        }
    }

    public static void setIsShowLog(boolean isShowLog) {
        IS_SHOW_LOG = isShowLog;
    }
}
