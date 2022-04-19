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
import kotlinx.android.synthetic.main.fragment_add_owner_item.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_item_footer.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.RelateOwnerListAdapter
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.DelvyAddrChangeFragment
import kr.or.kreb.ncms.mobile.util.*
import org.json.JSONArray
import org.json.JSONObject


class RestLandOwnerRecyclerViewAdapter(
    private val context: Context,
    private var restLandOwnerInfo: JSONArray,
    val delvyAddrBtnListener: onItemClickDelvyAddrBtnListener,
    val addRelateBtnListener: onItemClickaddRelateBtnListener,
    val addOwnerBtnListener: onItemClickaddOwnerBtnListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2
    private val toastUtil: ToastUtil = ToastUtil(context)
    private val logutil: LogUtil = LogUtil(RestLandOwnerRecyclerViewAdapter::class.java.simpleName)

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    //임시 토지 소유자 관계

    inner class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//        init{
//            itemView.loanBtn_c3_01.setOnClickListener {
//                Toast.makeText(context, addr3[adapterPosition], Toast.LENGTH_SHORT).show()
//            }
//        }
    }
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        init{
            itemView.addNewOwnerBtn.setOnClickListener {
                toastUtil.msg_info(context.resources.getString(R.string.landOwnAdd), 200)
                addOwnerBtnListener.onAddOwnerBtnClick()
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_add_owner_item_footer))
            else -> LoanViewHolder(parent.inflate(R.layout.fragment_add_owner_item))
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is LoanViewHolder -> {

                holder.itemView.apply {
                    val ownerInfoJson = restLandOwnerInfo.getJSONObject(position)


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

                    phoneNumber.text = "전화번호:$telnoString\n휴대전화:$moblPhonString"
                    //공부상주소
                    rgistAdresText.text = checkStringNull(ownerInfoJson.getString("rgistAdres"))
                    //지분
                    PosesnQotaText.text = checkStringNull(ownerInfoJson.getString("posesnQota"))
                    //소유구분
                    val indvdlGrptyText = checkStringNull(ownerInfoJson.getString("indvdlGrpTy"))

                    indvdlGrpTyText.text = indvdlGrptyText
                    //미확정소유자구분
                    val unDcsnOwnerAtString = checkStringNull(ownerInfoJson.getString("unDcsnOwnerAt"))
                    if(unDcsnOwnerAtString == "Y") {
                        UnDcsnOwnerAtText.text = "확정"
                    } else {
                        UnDcsnOwnerAtText.text = "미확정"
                    }
                    //주민번호
                    val ihidNumString = checkStringNull(ownerInfoJson.getString("ihidnum"))
                    if(ihidNumString == "") {
                        ihidnumText.text = ihidNumString
                    } else {
                        //val ihidNumStringSub = ihidNumString.substring(0,8)
                        val ihidNumStringSub = withIhidNumAsterRisk(ihidNumString)
                        ihidnumText.text = ihidNumStringSub
                    }

                    //초본상주소
                    AbstrAddrText.text = checkStringNull(ownerInfoJson.getString("abstrctAdres"))
                    //송달주소
                    DelvyAddrText.text = checkStringNull(ownerInfoJson.getString("delvyAdres"))

//                    ownerInfoRm.setText(checkStringNull(ownerInfoJson.getString("rm")))

                    val relateData = ownerInfoJson.getJSONArray("relateData")
                    if(relateData.length() > 0) {
                        relateOwnerItemLayout.visibleView()
                        relateOwnerList.adapter = RelateOwnerListAdapter(context!!, "")
                        for(i in 0 until relateData.length()) {
                            (relateOwnerList.adapter as RelateOwnerListAdapter).addItem(relateData.getJSONObject(i))
                        }
//                        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                        layoutInflater.inflate(R.layout.view_indoorsketch_editor_text, null).let { view ->
//                            view.relateOwnerList.adapter = RelateOwnerListAdapter(context!!, "")
//
//                            for(i in 0 until relateData.length()) {
//                                (view.relateOwnerList.adapter as RelateOwnerListAdapter).addItem(relateData.getJSONObject(i))
//                            }
//                        }
                    } else {
                        relateOwnerItemLayout.goneView()
                    }

                    delvyAddrChange.setOnClickListener {

                        delvyAddrBtnListener.onDelvyAddrClick(ownerInfoJson)
                        logutil.d("land -> 송달주소 변경")

                        DelvyAddrChangeFragment(BizEnum.LAD, ownerInfoJson).show((context as MapActivity).supportFragmentManager, "delvyAddrChangeFragment")

                    }

                    addRelateOwnerBtn.setOnClickListener {
                        addRelateBtnListener.onAddRelateBtnClick(ownerInfoJson)
                    }

                }
            }
            else -> {
            }
        }
    }


    override fun getItemCount(): Int {
        return restLandOwnerInfo.length()
    }

    override fun getItemViewType(position: Int): Int {

        val itemMaxCount = restLandOwnerInfo.length() - 1

        return when (position) {
            itemMaxCount  -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    fun setJSONArray(data: JSONArray) {
        this.restLandOwnerInfo = data
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