/*
 * Create by sgablc team.eco-chain on 2022.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.thing_dialog_item1.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.fragment.ThingDialogFragment
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface

class ThingKndSubListAdapter (
    private val thingSubList: ArrayList<String>,
    var context: Context,
    private val thingType: Int,
    val v: ThingDialogFragment,
    val thingListener: ThingViewPagerInterface
    ):
    RecyclerView.Adapter<ThingKndSubListAdapter.ThingViewHolder>(){

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)
    private var dialogBtnPosition: Int = 0
    private var mOnItemClickListener: OnItemSubClickListener? = null


    interface OnItemSubClickListener {
        fun chkSubType(type: Int, thingType: Int)
    }

    fun setOnItemSubClickListener(listener: ThingKndSubListAdapter.OnItemSubClickListener) {
        mOnItemClickListener = listener
    }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThingKndSubListAdapter.ThingViewHolder {
       return ThingViewHolder(parent.inflate(R.layout.thing_dialog_item1))
    }

    override fun onBindViewHolder(holder: ThingKndSubListAdapter.ThingViewHolder, position: Int) {
        holder.itemView.buttonThingDialog.setText(thingSubList[position])
        holder.bindItems(position)
        holder.itemView.buttonThingDialog.isSelected = dialogBtnPosition == position
    }

    override fun getItemCount(): Int {
        return thingSubList.size
    }

    inner class ThingViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        fun bindItems(position: Int) {

            itemView.buttonThingDialog.setOnClickListener {
                dialogBtnPosition = position
                mOnItemClickListener?.chkSubType(position, thingType)
                notifyDataSetChanged()
            }
        }
    }
}

