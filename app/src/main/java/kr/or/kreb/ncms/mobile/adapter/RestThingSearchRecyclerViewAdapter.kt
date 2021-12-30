/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.thing_search_gnrl_item.view.*
import kr.or.kreb.ncms.mobile.R

class RestThingSearchRecyclerViewAdapter : RecyclerView.Adapter<RestThingSearchRecyclerViewAdapter.RestThingSearchViewHolder>() {
        //임시 item
    private val thingNo: ArrayList<String> = arrayListOf("001","002","003","004")
    private val thingSmallCatecory: ArrayList<String> = arrayListOf("소분류","소분류","소분류","소분류")
    private val thingKind: ArrayList<String> = arrayListOf("일반","일반","일반","일반")
    private val thingArea1: ArrayList<String> = arrayListOf("전체","전체","전체","전체")
    private val thingArea2: ArrayList<String> = arrayListOf("편입","편입","편입","편입")
    private val thingUnit: ArrayList<String> = arrayListOf("단위","단위","단위","단위")

    private fun ViewGroup.inflate(layoutRes: Int): View =
        android.view.LayoutInflater.from(context).inflate(layoutRes, this, false)

    inner class RestThingSearchViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestThingSearchViewHolder {

        return RestThingSearchViewHolder(parent.inflate(R.layout.thing_search_gnrl_item))
    }

    override fun onBindViewHolder(holder: RestThingSearchViewHolder, position: Int) {

        when (holder) {
            is RestThingSearchViewHolder -> {
                holder.itemView.apply {
                    textViewThingGnrlItem05.text = thingNo[position]
                    textViewThingGnrlItem06.text = thingSmallCatecory[position]
                    textViewThingGnrlItem07.text = thingKind[position]
                    textViewThingGnrlItem13.text = thingArea1[position]
                    textViewThingGnrlItem14.text = thingArea2[position]
                    textViewThingGnrlItem15.text = thingUnit[position]
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return thingNo.size
    }

}