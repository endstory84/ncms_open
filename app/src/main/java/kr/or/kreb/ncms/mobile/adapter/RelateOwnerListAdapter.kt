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
import kotlinx.android.synthetic.main.fragment_add_relate_item.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.RelateOwnerListInfo
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.DelvyAddrChangeFragment
import org.json.JSONObject

class RelateOwnerListAdapter(val context: Context): BaseAdapter()  {

    var relateOwnerData = ArrayList<RelateOwnerListInfo>()


    override fun getCount(): Int {
        return relateOwnerData.size
    }

    override fun getItem(position: Int): Any {
        return relateOwnerData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        val data = getItem(position) as RelateOwnerListInfo
        val view: View

        if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.fragment_add_relate_item, parent, false)
        } else {
            view = convertView
        }
        view.ownberCodeNoText.text = data.indvdlGrpCode
        if(data.posesnSe.equals("R")) {
            view.posesnSeText.text = "관계자"
        } else {
            view.posesnSeText.text = ""
        }

        view.ownerClText.text = when(data.ownerCl) {
            "A015001" -> "사유"
            "A015002" -> "국유"
            "A015003" -> "공유"
            "A015004" -> "알수없음"
                else -> ""
        }
        view.ownerName.text = data.ownerNm

        val telnoString = data.telno
        val moblPhonString = data.moblphon
        view.phoneNumber.text = "전화번호:"+telnoString + "\n" +"휴대전화:"+moblPhonString

        view.rgistAdresText.text = data.rgistAdres
        view.indvdlGrpTyText.text = data.indvdlGrpTy

        view.pcnRightRelateText.text = data.pcnRightRelate
        view.spotNmText.text = data.spotNm
        if(data.indvdlGrpTy.equals("개인")) {
            view.ihidnumTitle.text = "주민번호"
            view.ihidnumText.text = data.ihidnum
        } else {
            view.ihidnumTitle.text = "법인등록번호"
            view.ihidnumText.text = data.ihidnum
        }

        view.AbstrAddrText.text = data.abstrctAdres
        view.DelvyAddrText.text = "(${data.delvyZip}) ${data.delvyAdres} ${data.delvyAdresDetail}"
//        view.relateInfoRm.text = data.rm

        view.delvyRelateAddrChange.setOnClickListener {
            DelvyAddrChangeFragment(BizEnum.LAD, data.relateOwnerJSON!!).show((context as MapActivity).supportFragmentManager, "delvyAddrChangeFragment")

        }

        return view

    }



    fun addItem(dataJson: JSONObject) {
        val relateOwnerListInfo = RelateOwnerListInfo()
        relateOwnerListInfo.indvdlGrpCode = checkStringNull(dataJson.getString("indvdlGrpCode"))
        relateOwnerListInfo.indvdlGrpTy = checkStringNull(dataJson.getString("indvdlGrpTy"))
        relateOwnerListInfo.ownerCl = checkStringNull(dataJson.getString("ownerCl"))
        relateOwnerListInfo.ownerNm = checkStringNull(dataJson.getString("name"))
        relateOwnerListInfo.posesnSe = checkStringNull(dataJson.getString("posesnSe"))
        relateOwnerListInfo.telno = checkStringNull(dataJson.getString("telno"))
        relateOwnerListInfo.moblphon = checkStringNull(dataJson.getString("moblphon"))
        relateOwnerListInfo.rgistAdres = checkStringNull(dataJson.getString("rgistAdres"))
        relateOwnerListInfo.pcnRightRelate = checkStringNull(dataJson.getString("pcnRightRelate"))
        relateOwnerListInfo.spotNm = checkStringNull(dataJson.getString("spotNm"))
        relateOwnerListInfo.ihidnum = checkStringNull(dataJson.getString("ihidnum"))
        relateOwnerListInfo.abstrctAdres = checkStringNull(dataJson.getString("pcnAbstrctAdres"))
        relateOwnerListInfo.delvyZip = checkStringNull(dataJson.getString("delvyZip"))
        relateOwnerListInfo.delvyAdres = checkStringNull(dataJson.getString("delvyAdres"))
        relateOwnerListInfo.delvyAdresDetail = checkStringNull(dataJson.getString("delvyAdresDetail"))

        relateOwnerListInfo.rm = checkStringNull(dataJson.getString("rm"))
        relateOwnerListInfo.relateOwnerJSON = dataJson

        relateOwnerData.add(relateOwnerListInfo)


    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }
}