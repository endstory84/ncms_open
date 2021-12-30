/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.util.CartoMapUtil
import kr.or.kreb.ncms.mobile.util.LogUtil

class FarmCropsAdapter (
//    private var realLandJson: JSONArray,
//    val clickListener: LandRealAddInterface,
//    val realListener: LandSearchRealInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    inner class LoanViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var logUtil: LogUtil = LogUtil("LandSearchRealngrAdapter")

    var mCartoMap : CartoMapUtil? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_land_search_item_footer))
            else -> LoanViewHolder2(parent.inflate(R.layout.fragment_land_search_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LoanViewHolder2 -> {
                holder.itemView.apply {
//                    logUtil.d("RealngrAdapter data "+realLandJson.toString())
//                    var realLandData = realLandJson.get(position) as JSONObject
//                    var realLndcgrAr = realLandData!!.getString("realLndcgrAr")
//                    var realLndcgrCl = realLandData!!.getString("realLndcgrCl")
//                    var realLndcgrCn = realLandData!!.getString("realLndcgrCn")
//
//                    landSearchRealCl.setText(realLndcgrCl)
//                    landSearchRealCn.setText(realLndcgrCn)
//                    landSearchRealAr.setText(realLndcgrAr +"㎥")
                }
            }
            else -> {
//                holder.itemView.landRealAddLl.setOnClickListener {
////                    Toast.makeText(holder.itemView.context,"지도에서 조사내역을 추가해주세요", Toast.LENGTH_SHORT).show()
//                    clickListener.onClick(realListener)
//                }
//
//                holder.itemView.landRealAddBtn.setOnClickListener {
//                    clickListener.onClick(realListener)
//                }
            }
        }
    }

    override fun getItemCount(): Int {
//        return realLandJson.length()
        //return 1

        return 5
    }

    override fun getItemViewType(position: Int): Int {
//        if (realLandJson.length() == 1) {
//            return TYPE_FOOTER
//        }
        return when (position) {
            0 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }
}