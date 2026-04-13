package com.dreisamlib.demo.utils

import android.app.Activity
import android.os.Process
import java.util.Stack

/**
 * Activity manager for the current application
 *
 */
object ActivityUtils {
    private var activityStack: Stack<Activity> = Stack<Activity>()


    /**
     * Add Activity
     *
     * @param activity activity
     */
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }

    /**
     * Get current Activity
     * - Note: The returned Activity must always be null-checked
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
     * Get specified Activity
     * - Note: The returned Activity must always be null-checked
     *
     * @param cls Target Activity.class
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
         * Get all Activity names in ActivityStack
         *
         * @return Activity names
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
     * Finish current Activity
     */
    fun finishActivity() {
        if (activityStack.isEmpty()) return
        val activity: Activity? = activityStack.lastElement()
        finishActivity(activity)
    }

    /**
     * Finish specified Activity
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
     * Finish Activity by class name
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
     * Finish all Activities
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
     * Finish all Activities except specified ones
     *
     * @param class1 class array
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
     * Exit app
     */
    fun AppExit() {
        try {
            finishAllActivity()
            // Kill app process
            Process.killProcess(Process.myPid())
            System.exit(0)
            System.gc()
        } catch (e: Exception) {
        }
    }

}
