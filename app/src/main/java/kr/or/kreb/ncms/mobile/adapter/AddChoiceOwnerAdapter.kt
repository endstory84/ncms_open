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
import kotlinx.android.synthetic.main.fragment_add_choice_owner_dialog_item.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.AddOwnerListInfo
import kr.or.kreb.ncms.mobile.util.withIhidNumAsterRisk
import org.json.JSONObject


class AddChoiceOwnerAdapter(var context: Context) : BaseAdapter() {

    var addOwnerChoiceData = ArrayList<AddOwnerListInfo>()
    var addOwnerChoiceSelectItem = ArrayList<AddOwnerListInfo>()


    override fun getCount(): Int {
        return addOwnerChoiceData.size
    }

    override fun getItem(position: Int): Any {
        return addOwnerChoiceData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val data = getItem(position) as AddOwnerListInfo

        val view: View

        if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.fragment_add_choice_owner_dialog_item, parent, false)
        } else {
            view = convertView
        }

        view.ownerChoiceSearchDivText.text =  checkStringNull(data.gubunSearchNm)

        view.ownerChoiceDivisionText.text = when(checkStringNull(data.indvdlGrpSe)) {
            "1" ->"개인"
            "2" ->"단체"
            else -> ""
        }
        view.ownerChoiceNameText.text = checkStringNull(data.indvdlGrpNm)
        val sameNameNoString = checkStringNull(data.sameNameNo)
        if(sameNameNoString.equals("")) {
            view.ownerChoiceSameNameNoText.text = "-"
        } else {
            view.ownerChoiceSameNameNoText.text = sameNameNoString
        }

        val ihidnumString = checkStringNull(data.inhbtntCprNo)
        if(ihidnumString.equals("")) {
            view.ownerChoiceIhidnumText.text = ihidnumString
        } else {
            //val ihidnumStringSub = ihidnumString.substring(0,8)
            //view.ownerChoiceIhidnumText.text = "$ihidnumStringSub ******"
            view.ownerChoiceIhidnumText.text = withIhidNumAsterRisk(ihidnumString)

        }
        view.ownerChoiceRgistAdresText.text = checkStringNull(data.rgistAdres)
        view.ownerChoiceDelvyAdresText.text = "${checkStringNull(data.delvyZip)} ${checkStringNull(data.delvyAdres)} ${checkStringNull(data.delvyAdresDetail)}"


        view.addChoiceOwnerChk.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                addOwnerChoiceSelectItem.add(data)
            } else {
                addOwnerChoiceSelectItem.remove(data)
            }
        }

        return view

    }

    fun addItem(dataJson: JSONObject) {
        var addOwnerChoiceInfo = AddOwnerListInfo()
        addOwnerChoiceInfo.gubunSearchNm = dataJson.getString("gubunSearchNm")
        addOwnerChoiceInfo.indvdlGrpSe = dataJson.getString("indvdlGrpTy")
        addOwnerChoiceInfo.indvdlGrpNm = dataJson.getString("grpNm")
        addOwnerChoiceInfo.sameNameNo = dataJson.getString("sameNameNo")
        addOwnerChoiceInfo.inhbtntCprNo = dataJson.getString("ihidnum")
        addOwnerChoiceInfo.rgistAdres = dataJson.getString("rgistAdres")
        addOwnerChoiceInfo.delvyZip = dataJson.getString("delvyZip")
        addOwnerChoiceInfo.delvyAdres = dataJson.getString("delvyAdres")
        addOwnerChoiceInfo.delvyAdresDetail = dataJson.getString("delvyAdresDetail")
        addOwnerChoiceInfo.indvdlGrpCode = dataJson.getString("indvdlGrpCode")
        addOwnerChoiceInfo.unDcsnOwnerAt = "N"
        addOwnerChoiceInfo.delvyChange = "N"
        addOwnerChoiceInfo.hapyuAt = ""
        addOwnerChoiceInfo.hapyuGroupCode = ""
        addOwnerChoiceInfo.plotCode = ""

        addOwnerChoiceData.add(addOwnerChoiceInfo)
    }

    fun getSelectItem(): ArrayList<AddOwnerListInfo> {
        return addOwnerChoiceSelectItem
    }



    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }
}