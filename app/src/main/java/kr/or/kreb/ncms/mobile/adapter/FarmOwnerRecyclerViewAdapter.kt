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
import kotlinx.android.synthetic.main.fragment_add_owner_item_footer.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.util.ToastUtil

class FarmOwnerRecyclerViewAdapter (
    val context: Context,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2
    private val toastUtil: ToastUtil = ToastUtil(context)

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    //임시 농업 소유자 관계
    private var no: ArrayList<String> = arrayListOf("001","002","003")
    private var type: ArrayList<String> = arrayListOf("소유자","관계인","관계인")
    private var name: ArrayList<String> = arrayListOf("홍길동","홍길동","홍길동")
    private var tell: ArrayList<String> = arrayListOf("010-1111-1111","010-1111-1111","010-1111-1111")
    private var addr1: ArrayList<String> = arrayListOf("경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1")
    private var addr2: ArrayList<String> = arrayListOf("경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1")
    private var addr3: ArrayList<String> = arrayListOf("경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1","경기도 파주시 운정동 10-1")

    inner class FarmOwnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        init{
//            itemView.loanBtn_c3_01.setOnClickListener {
//                Toast.makeText(context, addr3[adapterPosition], Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.addOwnerBtn.setOnClickListener {
                toastUtil.msg_info(context.resources.getString(R.string.landOwnAdd), 200)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_add_owner_item_footer))
            else -> FarmOwnerViewHolder(parent.inflate(R.layout.fragment_add_owner_item))
        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is FarmOwnerViewHolder -> {

                holder.itemView.apply {
                }
            }
            else -> {
            }
        }
    }

    override fun getItemCount(): Int {
        return no.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

}