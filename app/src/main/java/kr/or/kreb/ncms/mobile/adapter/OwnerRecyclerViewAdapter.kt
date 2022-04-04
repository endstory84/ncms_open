/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_add_owner_item.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.DelvyAddrChangeFragment
import kr.or.kreb.ncms.mobile.util.withIhidNumAsterRisk
import org.json.JSONArray

class OwnerRecyclerViewAdapter(
    context: Context,
    bizType: BizEnum,
    ownerInfo: JSONArray,
    onOwnerEventListener: OnOwnerEventListener
) : BaseOwnerRecyclerViewAdapter(context, bizType, ownerInfo, onOwnerEventListener) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is OwnerViewHolder -> {

                holder.itemView.apply {
                    val ownerInfoJson = ownerInfo.getJSONObject(position)

                    //No
                    ownerCodeNoText.text = position.toString()
                    //구분
                    val posesnSeString = checkStringNull(ownerInfoJson.getString("posesnSe"))
                    if(posesnSeString.equals("O")) {
                        posesnSeText.text = "소유자"
                    } else {
                        posesnSeText.text = ""
                    }

                    //성명
                    ownerName.text = checkStringNull(ownerInfoJson.getString("ownerNm"))
                    //연락처
                    val telnoString = checkStringNull(ownerInfoJson.getString("telno"))
                    val moblPhonString = checkStringNull(ownerInfoJson.getString("moblphon"))

                    phoneNumber.text = "전화번호:${telnoString}\n휴대전화:${moblPhonString}"
                    //공부상주소
                    rgistAdresText.text = checkStringNull(ownerInfoJson.getString("rgistAdres"))
                    //지분
                    PosesnQotaText.text = checkStringNull(ownerInfoJson.getString("posesnQota"))
                    //소유구분
                    val indvdlGrptyText = checkStringNull(ownerInfoJson.getString("indvdlGrpTy"))

                    indvdlGrpTyText.text = indvdlGrptyText
                    //미확정소유자구분
                    val unDcsnOwnerAtString = checkStringNull(ownerInfoJson.getString("unDcsnOwnerAt"))
                    if(unDcsnOwnerAtString.equals("Y")) {
                        UnDcsnOwnerAtText.setText("확정")
                    } else {
                        UnDcsnOwnerAtText.setText("미확정")
                    }
                    //주민번호
                    val ihidNumString = checkStringNull(ownerInfoJson.getString("ihidnum"))
                    if(indvdlGrptyText.equals("개인")) {
                        ihidnumText.text = withIhidNumAsterRisk(ihidNumString)
                    } else {
                        ihidnumTitle.text = "법인등록번호"
                        ihidnumText.text = withIhidNumAsterRisk(false, ihidNumString)

                    }

                    //초본상주소
                    AbstrAddrText.text = checkStringNull(ownerInfoJson.getString("abstrctAdres"))

                    //송달주소
                    val delvyZipString = checkStringNull(ownerInfoJson.getString("delvyZip"))
                    val delvyAdresString = checkStringNull(ownerInfoJson.getString("delvyAdres"))
                    val delvyAdresDetailString = checkStringNull(ownerInfoJson.getString("delvyAdresDetail"))
                    DelvyAddrText.text = when(bizType) {
                        BizEnum.LAD -> {
                            delvyAdresString
                        }
                        else -> {
                            "($delvyZipString) $delvyAdresString $delvyAdresDetailString"
                        }
                    }

                    val relateData = ownerInfoJson.getJSONArray("relateData")
                    if(relateData.length() > 0) {
                        relateOwnerItemLayout.visibility = View.VISIBLE
                        relateOwnerList.adapter = RelateOwnerListAdapter(context!!)
                        for(i in 0 until relateData.length()) {
                            (relateOwnerList.adapter as RelateOwnerListAdapter).addItem(relateData.getJSONObject(i))
                        }
                    } else {
                        relateOwnerItemLayout.visibility = View.GONE
                    }

                    delvyAddrChange.setOnClickListener {
                        onOwnerEventListener.onDelvyAddrClicked(ownerInfoJson)

                        when(bizType) {
                            BizEnum.LAD, BizEnum.REST_LAD -> {
                                DelvyAddrChangeFragment(BizEnum.LAD, ownerInfoJson).show((context as MapActivity).supportFragmentManager, "delvyAddrChangeFragment")
                            }
                        }
                    }

                    addRelateOwnerBtn.setOnClickListener {
                        onOwnerEventListener.onAddRelateBtnClicked(ownerInfoJson)
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

}