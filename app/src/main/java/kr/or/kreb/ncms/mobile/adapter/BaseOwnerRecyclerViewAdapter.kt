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
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil
import org.json.JSONArray
import org.json.JSONObject

open class BaseOwnerRecyclerViewAdapter(
    var context: Context,
    var bizType: BizEnum,
    var ownerInfo: JSONArray,
    var onOwnerEventListener: OnOwnerEventListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    open val TYPE_ITEM = 1
    open val TYPE_FOOTER = 2
    open val toastUtil: ToastUtil = ToastUtil(context)
    open val logutil: LogUtil = LogUtil(BaseOwnerRecyclerViewAdapter::class.java.simpleName)

    open fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    //임시 물건 소유자 관계
    open inner class OwnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    open inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

            itemView.addNewOwnerBtn.setOnClickListener {
                toastUtil.msg_info(context.resources.getString(R.string.landOwnAdd) + "1111", 200)
                onOwnerEventListener.onAddNewOwnerBtnClicked()
            }

//            itemView.minusNewOwnerBtn.setOnClickListener {
//                onOwnerEventListener.onMinusNewOwnerBtnClicked()
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_add_owner_item_footer))
            else -> OwnerViewHolder(parent.inflate(R.layout.fragment_add_owner_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return ownerInfo.length()
    }

    override fun getItemViewType(position: Int): Int {

        if(ownerInfo == null) {
            return TYPE_FOOTER
        } else {
            var itemMaxCount = ownerInfo.length() - 1

            return when (position) {
                itemMaxCount  -> TYPE_FOOTER
                else -> TYPE_ITEM
            }
        }
    }

    open fun setJSONArray(data: JSONArray) {
        this.ownerInfo = data
    }

    open fun checkStringNull(nullString: String): String = if (nullString == "null") "" else {
        nullString
    }

//    interface onItemClickDelvyAddrBtnListener {
//        fun onDelvyAddrClick(data: JSONObject)
//    }
//    interface onItemClickaddRelateBtnListener {
//        fun onAddRelateBtnClick(data: JSONObject)
//    }
//    interface onItemClickaddOwnerBtnListener {
//        fun onAddOwnerBtnClick()
//    }
    interface OnOwnerEventListener {
        fun onDelvyAddrClicked(data: JSONObject)
        fun onAddRelateBtnClicked(data: JSONObject)
        fun onAddNewOwnerBtnClicked()
    }

}