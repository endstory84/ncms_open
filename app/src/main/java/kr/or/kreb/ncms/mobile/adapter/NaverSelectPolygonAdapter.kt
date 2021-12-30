/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.map.overlay.PolygonOverlay
import kotlinx.android.synthetic.main.listview_selectpolygon.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.NaverSelectPolygon
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.toggleNaverPolygonColor

class NaverSelectPolygonAdapter(
    var context: Context,
    val dataList: MutableList<NaverSelectPolygon>
) : RecyclerView.Adapter<NaverSelectPolygonAdapter.ViewHolder>() {

    var pos: Int = 0
    var _isChecked = false
    var getPolygonTag: String =""
    lateinit var getPolygon: NaverSelectPolygon

    var logUtil: LogUtil = LogUtil(NaverSelectPolygonAdapter::class.java.simpleName)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NaverSelectPolygonAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_selectpolygon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NaverSelectPolygonAdapter.ViewHolder, position: Int) {

        holder.bind(dataList[position])

        holder.itemView.textViewNaverSelectPolygon.apply {
            setOnClickListener {

                pos = holder.adapterPosition

                isChecked = !isChecked
                _isChecked= isChecked
                logUtil.d("ui checkbox 선택 -> $pos, $_isChecked")
                dataList[pos].isChecked = _isChecked
                logUtil.d("class checkbox 선택 -> $pos, ${dataList[pos].isChecked}")

                if (_isChecked) {
                    toggleNaverPolygonColor(context, dataList[pos].polygon, R.color.red)
                } else if (!_isChecked) {
                    toggleNaverPolygonColor(context, dataList[pos].polygon, R.color.blue)
                }

                addItem(getPolygonTag, dataList[pos].isChecked, dataList[pos].polygon)

            }
        }

    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: NaverSelectPolygon) {
            itemView.textViewNaverSelectPolygon.text = item.seq
        }
    }

    fun addItem(tag: String, checked: Boolean, polygonOverlay: PolygonOverlay) {
        getPolygon = NaverSelectPolygon(tag, !checked, polygonOverlay)
        getPolygonTag = tag
        updateItem(getPolygonTag)
        logUtil.d("선택된 필지레이어 -> $dataList")
        notifyDataSetChanged()
    }

    fun updateItem(getTag:String){
        if(dataList.size == 0){
            dataList.add(getPolygon)
        } else {
            if(dataList[pos].seq != getTag){
                if(!dataList.contains(getPolygon)){
                    dataList.add(getPolygon)
                }
            } else {
                dataList[pos].isChecked = !dataList[pos].isChecked
            }
        }
    }
}


