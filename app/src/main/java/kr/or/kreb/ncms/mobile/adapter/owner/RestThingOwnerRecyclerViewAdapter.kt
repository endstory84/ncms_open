/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter.owner

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_add_owner_item.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_item_footer.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.RelateOwnerListAdapter
import kr.or.kreb.ncms.mobile.util.ToastUtil
import org.json.JSONArray
import org.json.JSONObject

class RestThingOwnerRecyclerViewAdapter(
    private val context: Context,
    private val activity: Activity,
    private var restThingOwnerInfo: JSONArray,
    val delvyAddrBtnListener: onItemClickDelvyAddrBtnListener,
    val addRelateBtnListener: onItemClickaddRelateBtnListener,
    val addOwnerBtnListener: onItemClickaddOwnerBtnListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2
    private val toastUtil: ToastUtil = ToastUtil(context)

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    //임시 물건 소유자 관계
    inner class RestThingOwnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        init{
//            itemView.loanBtn_c3_01.setOnClickListener {
//                Toast.makeText(context, addr3[adapterPosition], Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.addNewOwnerBtn.setOnClickListener {
                toastUtil.msg_info(context.resources.getString(R.string.landOwnAdd), 200)
                addOwnerBtnListener.onAddOwnerBtnClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_add_owner_item_footer))
            else -> RestThingOwnerViewHolder(parent.inflate(R.layout.fragment_add_owner_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is RestThingOwnerViewHolder -> {

                holder.itemView.apply {
                    val ownerInfoJson = restThingOwnerInfo.getJSONObject(position)

                    //No
                    ownerCodeNoText.setText(position.toString())
                    //구분
                    val posesnSeString = checkStringNull(ownerInfoJson.getString("posesnSe"))
                    if(posesnSeString.equals("O")) {
                        posesnSeText.setText("소유자")
                    } else {
                        posesnSeText.setText("")
                    }

                    //성명
                    ownerName.setText(checkStringNull(ownerInfoJson.getString("ownerNm")))
                    //연락처
                    val telnoString = checkStringNull(ownerInfoJson.getString("telno"))
                    val moblPhonString = checkStringNull(ownerInfoJson.getString("moblphon"))

                    phoneNumber.setText("전화번호:"+telnoString + "\n" +"휴대전화:"+moblPhonString)
                    //공부상주소
                    rgistAdresText.setText(checkStringNull(ownerInfoJson.getString("rgistAdres")))
                    //지분
                    PosesnQotaText.setText(checkStringNull(ownerInfoJson.getString("posesnQota")))
                    //소유구분
                    val indvdlGrptyText = checkStringNull(ownerInfoJson.getString("indvdlGrpTy"))

                    indvdlGrpTyText.setText(indvdlGrptyText)
                    //미확정소유자구분
                    val unDcsnOwnerAtString = checkStringNull(ownerInfoJson.getString("unDcsnOwnerAt"))
                    if(unDcsnOwnerAtString.equals("Y")) {
                        UnDcsnOwnerAtText.setText("확정")
                    } else {
                        UnDcsnOwnerAtText.setText("미확정")
                    }
                    //주민번호
                    val ihidNumString = checkStringNull(ownerInfoJson.getString("ihidnum"))
                    val ihidNumStringSub = ihidNumString.split("-")
                    if(indvdlGrptyText.equals("개인")) {

                        ihidnumText.setText(ihidNumString[0] + "-*******")
                    } else {
                        ihidnumTitle.setText("법인등록번호")
                        ihidnumText.setText(ihidNumString[0] + "-*******")

                    }

                    //초본상주소
                    AbstrAddrText.setText(checkStringNull(ownerInfoJson.getString("abstrctAdres")))
                    //송달주소
                    DelvyAddrText.setText(checkStringNull(ownerInfoJson.getString("delvyAdres")))

                    val relateData = ownerInfoJson.getJSONArray("relateData")
                    if(relateData.length() > 0) {
                        relateOwnerItemLayout.visibility = View.VISIBLE
                        relateOwnerList.adapter = RelateOwnerListAdapter(context!!)
                        for(i in 0 until relateData.length()) {
                            (relateOwnerList.adapter as RelateOwnerListAdapter).addItem(relateData.getJSONObject(i))
                        }
//                        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                        layoutInflater.inflate(R.layout.view_indoorsketch_editor_text, null).let { view ->
//                            view.relateOwnerList.adapter = RelateOwnerListAdapter(context!!)
//
//                            for(i in 0 until relateData.length()) {
//                                (view.relateOwnerList.adapter as RelateOwnerListAdapter).addItem(relateData.getJSONObject(i))
//                            }
//                        }
                    } else {
                        relateOwnerItemLayout.visibility = View.GONE
                    }

                    delvyAddrChange.setOnClickListener {
                        delvyAddrBtnListener.onDelvyAddrClick(ownerInfoJson)
                    }

                    addRelateOwnerBtn.setOnClickListener {
                        addRelateBtnListener.onAddRelateBtnClick(ownerInfoJson)
                    }

                }
                if(getItemCount() == 1) {
                    holder.itemView.lndOwnerItemBaseView.visibility = View.GONE
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

        if(restThingOwnerInfo == null) {
            return TYPE_FOOTER
        } else {
            var itemMaxCount = restThingOwnerInfo.length() - 1

            return when (position) {
                itemMaxCount  -> TYPE_FOOTER
                else -> TYPE_ITEM
            }
        }
    }

    fun setJSONArray(data: JSONArray) {
        this.restThingOwnerInfo = data
    }

    fun checkStringNull(nullString: String): String = if (nullString == "null") "" else {
        nullString
    }

    interface onItemClickDelvyAddrBtnListener {
        fun onDelvyAddrClick(data: JSONObject)
    }
    interface onItemClickaddRelateBtnListener {
        fun onAddRelateBtnClick(data: JSONObject)
    }
    interface onItemClickaddOwnerBtnListener {
        fun onAddOwnerBtnClick()
    }

}