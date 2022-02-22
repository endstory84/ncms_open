/*
 * Create by sgablc team.eco-chain on 2022.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.include_sketch_info_list.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.SketchInfo

class SketchThingBuldAdpater(
    private val callDataArr: MutableList<SketchInfo>?
) : RecyclerView.Adapter<SketchThingBuldAdpater.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SketchThingBuldAdpater.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.include_sketch_info_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(callDataArr!![position])
    }

    override fun getItemCount(): Int = callDataArr?.size!!

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val context: Context = itemView.context

        @SuppressLint("SetTextI18n")
        fun bind(item: SketchInfo) {

            with(itemView) {
                item.apply {
                    tvSketchKndName.text = item.thingKndName
                }
            }
        }
    }

}