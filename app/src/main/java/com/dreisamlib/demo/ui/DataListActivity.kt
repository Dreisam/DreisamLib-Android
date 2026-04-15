package com.dreisamlib.demo.ui

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dreisamlib.demo.R
import com.dreisamlib.demo.adapter.ItemBloodSugarInfoAdapter
import com.dreisamlib.demo.ctrl.ConnectCtrl
import com.dreisamlib.demo.utils.AppLogUtils
import com.dreisamlib.demo.utils.TimeUtils
import com.dreisamlib.lib.api.DreisamLib
import java.util.Collections
import kotlin.math.abs

class DataListActivity: BaseActivity()  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemBloodSugarInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)
        initView()
        getBloodSugarData()
    }

    fun initView(){
        recyclerView = findViewById(R.id.rvBloodSugar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemBloodSugarInfoAdapter(this)
        recyclerView.adapter = adapter

        findViewById<View?>(R.id.back).setOnClickListener { v: View? -> finish() }

        ConnectCtrl.addOnAnalzeDatatListener{
            AppLogUtils.debug("addBottomData:${it.printMessage()}")
            adapter.addFirstData(it)
        }
    }



    private fun getBloodSugarData() {
        val startTime = TimeUtils.getTimeStartFromDay(System.currentTimeMillis())/1000
        val endTime = TimeUtils.getTimeEndFromDay(System.currentTimeMillis())/1000

        AppLogUtils.debug("startTime:$startTime  endTime:$endTime" )
        DreisamLib.getConnectManage().getHistory(0,endTime) { datas ->
            Collections.sort(datas) { p0, p1 -> (p1.timeCreate - p0.timeCreate).toInt() }
            adapter.setDataList(datas)
        }

    }

}