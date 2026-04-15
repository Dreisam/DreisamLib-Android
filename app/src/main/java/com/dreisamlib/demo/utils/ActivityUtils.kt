package com.dreisamlib.demo.utils

import android.app.Activity
import android.os.Process
import java.util.Stack

/**
 * 当前应用程序Activity管理类
 *
 */
object ActivityUtils {
    private var activityStack: Stack<Activity> = Stack<Activity>()


    /**
     * 添加Activity
     *
     * @param activity activity
     */
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }

    /**
     * 获取当前Activity
     * - 注意！！！使用该方法必须对获得的Activity进行判空处理
     *
     * @return Activity
     */
    fun currentActivity(): Activity? {
        return if (activityStack.isEmpty()) {
            null
        } else {
            activityStack.lastElement()
        }
    }

    /**
     * 获取指定的Activity
     * - 注意！！！使用该方法必须对获得的Activity进行判空处理
     *
     * @param cls 目标Activity.class
     * @return Activity
     */
    fun getActivity(cls: Class<*>?): Activity? {
        if ( !activityStack.isEmpty()) for (activity in activityStack) {
            if (activity.javaClass == cls) {
                return activity
            }
        }
        return null
    }

    val activityStackNames: MutableList<String?>
        /**
         * 获取ActivityStack下的所有Activity名字
         *
         * @return Activity的名字
         */
        get() {
            if ( activityStack.isEmpty()) return ArrayList<String?>()
            val activityNames: MutableList<String?> = ArrayList<String?>()
            for (activity in activityStack) {
                activityNames.add(activity.javaClass.getSimpleName())
            }
            return activityNames
        }

    /**
     * 结束当前Activity
     */
    fun finishActivity() {
        if (activityStack.isEmpty()) return
        val activity: Activity? = activityStack.lastElement()
        finishActivity(activity)
    }

    /**
     * 结束指定的Activity
     *
     * @param activity activity
     */
    fun finishActivity(activity: Activity?) {
        if (!activityStack.isEmpty() && activity != null) {
            activityStack.remove(activity)
            activity.finish()
        }
    }

    /**
     * 结束指定类名的Activity
     *
     * @param class1 class
     */
    fun finishActivity(class1: Class<*>?) {
        if (activityStack.isEmpty()) return
        for (i in activityStack.indices) {
            if (((activityStack.get(i)).javaClass) == class1) {
                finishActivity(activityStack.get(i))
            }
        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        if (activityStack.isEmpty()) return
        var i = 0
        val size: Int = activityStack.size
        while (i < size) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish()
            }
            i++
        }
        activityStack.clear()
    }

    /**
     * 结束除参数外的所有Activity
     *
     * @param class1 class数组
     */
    fun finishAllOtherActivity(vararg class1: Class<*>) {
        if (activityStack.isEmpty()) return
        val activities = ArrayList<Activity?>()
        var i = 0
        while (i < activityStack.size) {
            val activity: Activity = activityStack[i]
            for (c in class1) {
                if (activity.javaClass.getSimpleName() == c.getSimpleName()) {
                    activityStack.removeAt(i)
                    i--
                    activities.add(activity)
                    break
                }
            }
            i++
        }
        finishAllActivity()
        activityStack.addAll(activities)
    }

    /**
     * 退出应用
     */
    fun AppExit() {
        try {
            finishAllActivity()
            // 杀死应用进程
            Process.killProcess(Process.myPid())
            System.exit(0)
            System.gc()
        } catch (e: Exception) {
        }
    }

}
