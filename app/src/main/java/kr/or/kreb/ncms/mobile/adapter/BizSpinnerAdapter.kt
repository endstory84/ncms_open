/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.biz_spinner_item.view.*
import kr.or.kreb.ncms.mobile.R

class BizSpinnerAdapter(
    var context: Context,
    var itemArr: ArrayList<String>
) : BaseAdapter() {

    override fun getCount(): Int = itemArr.size

    override fun getItem(position: Int): Any = itemArr[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =  LayoutInflater.from(context).inflate(R.layout.biz_spinner_item, null)
        view.tvBizSpinnerItem.text = itemArr[position]
        return view
    }

}