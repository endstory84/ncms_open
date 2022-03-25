/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter.owner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_add_new_owner_item.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_item_footer.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.RestThingWtnObject
import kr.or.kreb.ncms.mobile.util.ToastUtil
import kr.or.kreb.ncms.mobile.util.withIhidNumAsterRisk
import org.json.JSONArray

class RestThingNewOwnerRecyclerViewAdapter(
    context: Context,
    private var restThingOwnerInfo: JSONArray,
    val addOwnerBtnListener: onItemClickAddOwnerBtnListener,
    val addOwnerViewListener: onItemClickAddOwnerViewListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2
    private val toastUtil: ToastUtil = ToastUtil(context)

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    inner class RestThingOwnerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init{
            itemView.addOwnerBtn.setOnClickListener {
                toastUtil.msg_info("신규 소유자 추가", 200)
                addOwnerBtnListener.onAddNewOwnerBtnClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_add_owner_item_footer))
            else -> RestThingOwnerViewHolder(parent.inflate(R.layout.fragment_add_new_owner_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is RestThingOwnerViewHolder -> {
                holder.itemView.apply {
                    val ownerInfoJson = restThingOwnerInfo.getJSONObject(position)


                    //No
                    newOwnerNoText.text = "신규"
                    //구분
                    val posesnSeString = checkStringNull(ownerInfoJson.getString("posesnSe"))
                    newOwnerDivisionText.text = when (posesnSeString) {
                        "1"->"개인"
                        "2"->"단체"
                        else -> ""
                    }
                    //이름
                    newOwnerNameText.text = checkStringNull(ownerInfoJson.getString("ownerNm"))
                    //같은이름
                    val newSameNameNoString = checkStringNull(ownerInfoJson.getString("sameNameNo"))
                    if(newSameNameNoString.equals("")) {
                        newOwnerSameNameText.text = "-"
                    } else {
                        newOwnerSameNameText.text = newSameNameNoString
                    }

                    //공부상주소
                    newOwnerRgistAddrText.text = checkStringNull(ownerInfoJson.getString("rgistAdres"))
                    //지분
                    newOwnerPosesnQota.text =  checkStringNull(ownerInfoJson.getString("posesnQota"))
                    // 미확정 소유자 구분
                    val newOwnerUnDcsnOwnerAtString = checkStringNull(ownerInfoJson.getString("unDcsnOwnerAt"))
                    newOwnerUnDcsnOwnerAt.isChecked = when (newOwnerUnDcsnOwnerAtString) {
                        "Y" -> true
                        else -> false
                    }
                    //주민(법인)번호
                    val newOwnerCrpNoString = checkStringNull(ownerInfoJson.getString("ihidnum"))
                    if(newOwnerCrpNoString.equals("")) {
                        newOwnerCrpNoText.text = newOwnerCrpNoString
                    } else {
                        //val newOwnerCrpNoStringSub = newOwnerCrpNoString.substring(0,8)
                        //newOwnerCrpNoText.text = "$newOwnerCrpNoStringSub ******"
                        newOwnerCrpNoText.text = withIhidNumAsterRisk(newOwnerCrpNoString)
                    }
                    //송달주소
                    newOwnerDelvyAddrText.text = """${checkStringNull(ownerInfoJson.getString("delvyZip"))} ${checkStringNull(ownerInfoJson.getString("delvyAdres"))} ${checkStringNull(ownerInfoJson.getString("delvyAdresDetail"))}"""

                    addItemView.setOnClickListener {
                        toastUtil.msg_info("select Position $position", 400)
                        addOwnerViewListener.onAddNewOnwerViewClick(position)
                    }


                }
//                holder.itemView.setOnClickListener {
//                    toastUtil.msg_info("111111111111111111111", 400)
//                }
                if(itemCount ==1) {

                }

            }
            else -> {

            }
        }
    }

    override fun getItemCount(): Int {
        return restThingOwnerInfo.length()
    }

    override fun getItemViewType(position: Int): Int {

//        if(restThingOwnerInfo == null) {
//            return TYPE_FOOTER
//        } else {
//            var itemMaxCount = restThingOwnerInfo.length() - 1
//
//            return when (position) {
//                itemMaxCount  -> TYPE_FOOTER
//                else -> TYPE_ITEM
//            }
//        }
        if(restThingOwnerInfo.length() == 1) {
            return TYPE_FOOTER
        }
        return when(position) {
            0 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    fun setJSONArray(data: JSONArray) {
        this.restThingOwnerInfo = data
        RestThingWtnObject.thingOwnerInfoJson = data
    }

    fun checkStringNull(nullString: String): String = if (nullString == "null") "" else {
        nullString
    }

    interface onItemClickAddOwnerBtnListener {
        fun onAddNewOwnerBtnClick()
    }
    interface onItemClickAddOwnerViewListener {
        fun onAddNewOnwerViewClick(position: Int)
    }
}