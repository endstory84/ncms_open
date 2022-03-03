/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.dev.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.thing_dialog_item1.view.*
import kr.or.kreb.ncms.mobile.R


class ThingKndTitleListAdapter (private val arrayList: ArrayList<Int>, var context: Context) :
    RecyclerView.Adapter<ThingKndTitleListAdapter.ThingViewHolder>() {

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)
    private var dialogBtnPosition: Int = 0
    private var mOnItemClickListener: OnItemClickListener? = null


    interface OnItemClickListener {
        fun chkType(type: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    inner class ThingViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        fun bindItems(position: Int) {

            itemView.buttonThingDialog.setOnClickListener {
                dialogBtnPosition = position
                mOnItemClickListener?.chkType(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThingViewHolder {
        return ThingViewHolder(parent.inflate(R.layout.thing_dialog_item1))
    }

    override fun onBindViewHolder(holder: ThingViewHolder, position: Int) {
        holder.itemView.buttonThingDialog.setText(arrayList[position])
        holder.bindItems(position)
        holder.itemView.buttonThingDialog.isSelected = dialogBtnPosition == position
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}