package com.dreisamlib.demo.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dreisamlib.demo.R
import com.dreisamlib.demo.utils.TimeUtils
import com.dreisamlib.lib.bean.DreisamGlucoseModel

/**
 * 消息类型列表
 */
class ItemBloodSugarInfoAdapter(context: Context) :
    BaseRecyAdapter<ItemBloodSugarInfoAdapter.ViewHolder, DreisamGlucoseModel>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            mInflater.inflate(
                R.layout.item_blood_sugar_info_adapter,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        var viewHolder = holder as ViewHolder
        val entity = getItemData(position)
        viewHolder.tvTime.text = TimeUtils.formatMDHM(entity.timeCreate * 1000)
        viewHolder.tvPack.text = "${entity.packageNumber}"
        viewHolder.tvBloodSugar.text = "${entity.glucose}"

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvPack: TextView = itemView.findViewById(R.id.tvPack)
        var tvBloodSugar: TextView = itemView.findViewById(R.id.tvBloodSugar)
    }
}