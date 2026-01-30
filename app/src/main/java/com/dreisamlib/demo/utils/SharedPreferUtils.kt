package com.dreisamlib.demo.utils

import android.content.Context
import android.content.SharedPreferences
import com.dreisamlib.demo.constant.Constans
import com.dreisamlib.lib.bean.DreisamGlucoseModel
import com.google.gson.Gson

/**
 * SharedPreferences工具类
 *
 */
class SharedPreferUtils private constructor(context: Context) {
    private var prefs: SharedPreferences?
    private var editor: SharedPreferences.Editor?

    init {
        val SHAREDPREFER_CONIF = "AppData"
        prefs = context.getSharedPreferences(SHAREDPREFER_CONIF, Context.MODE_PRIVATE)
        editor = prefs?.edit()
        editor?.apply()
    }

    /**
     * 保存字符串
     *
     * @param key   key
     * @param value value
     */


    fun putString(key: String?, value: String?) {
        editor?.putString(key, value)
        editor?.apply()
    }


    fun getString(key: String?, defaultValue: String? = ""): String? {
        return prefs?.getString(key, defaultValue)
    }


    /**
     * 保存int类型
     *
     * @param key   key
     * @param value value
     */


    fun putInt(key: String?, value: Int) {
        editor?.putInt(key, value)
        editor?.apply()
    }

    /**
     * 获取int类型
     *
     * @param key kye
     * @return int
     */
    fun getInt(key: String?, value: Int = -1): Int {
        return prefs!!.getInt(key, value)
    }

    /**
     * 保存boolean类型
     *
     * @param key   key
     * @param value value
     * @return int
     */
    fun putBoolean(key: String?, value: Boolean) {
        editor?.putBoolean(key, value)
        editor?.commit()
    }

    /**
     * 获取boolean类型
     *
     * @param key 键值
     * @return boolean
     */
    fun getBoolean(key: String? ,defaultValue :Boolean = false): Boolean {
        return prefs!!.getBoolean(key, defaultValue)
    }

    /**
     * 设置最新的血糖数据
     */
    fun setGlucoseNew( entity: DreisamGlucoseModel?) {
        var data: String? = "{}"
        if (entity != null) data = Gson().toJson(entity)
        editor?.putString(Constans.KEY_GLUCOSE, data)?.apply()
    }

    fun getGlucoseNew(): DreisamGlucoseModel {
        val data: String? = prefs!!.getString(Constans.KEY_GLUCOSE, "{}")
        var entity: DreisamGlucoseModel? = Gson().fromJson<DreisamGlucoseModel?>(data, DreisamGlucoseModel::class.java)
        if (entity == null) entity = DreisamGlucoseModel()
        return entity
    }


    /**
     * 清空数据
     *
     * @return boolean
     */
    fun clear() {
        editor?.clear()
        editor?.commit()
    }


    companion object {
        private var sharedPreferHelper: SharedPreferUtils? = null

        /**
         * 单利模式
         *
         * @param context 上下文
         * @return SharepreferUtils对象
         */
        fun getInstanse(context: Context): SharedPreferUtils {
            if (sharedPreferHelper == null) {
                synchronized(SharedPreferUtils::class.java) {
                    if (sharedPreferHelper == null) {
                        sharedPreferHelper = SharedPreferUtils(context)
                    }
                }
            }
            return sharedPreferHelper!!
        }
    }
}