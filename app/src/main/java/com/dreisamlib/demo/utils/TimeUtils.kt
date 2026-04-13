package com.dreisamlib.demo.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期工具类
 * yyyy-MM-dd HH:mm:ss
 * MM-dd HH:mm
 */
object TimeUtils {


    fun formatHM(timestamp: Long): String {
        return format(timestamp = timestamp, pattern = "HH:mm")
    }

    fun formatMDHM(timestamp: Long): String {
        return format(timestamp = timestamp, pattern = "MM-dd HH:mm:ss")
    }

    fun formatHMSS(timestamp: Long): String {
        return format(timestamp = timestamp, pattern = "HH:mm:ss:SSS")
    }


    private fun format(timestamp: Long, pattern: String): String {
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.CHINA)
        return simpleDateFormat.format(Date(timestamp))
    }


    /**
     * 任意一天的数据 00:00:00
     */
    fun getTimeStartFromDay(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.time = Date(time)
        cal[Calendar.HOUR_OF_DAY] = 0 //控制时
        cal[Calendar.MINUTE] = 0 //控制分
        cal[Calendar.SECOND] = 0 //控制秒
        return cal.timeInMillis
    }

    /**
     * 任意一天的数据 23：59：59
     */
    fun getTimeEndFromDay(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.time = Date(time)
        cal[Calendar.HOUR_OF_DAY] = 23 //控制时
        cal[Calendar.MINUTE] = 59 //控制分
        cal[Calendar.SECOND] = 59 //控制秒
        return cal.timeInMillis
    }



}