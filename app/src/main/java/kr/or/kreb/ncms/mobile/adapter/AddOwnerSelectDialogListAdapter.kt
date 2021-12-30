/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.dialog_owner_info_item.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.RelateOwnerListInfo
import org.json.JSONObject
import kotlin.text.substring as substring1

class AddOwnerSelectDialogListAdapter(val context: Context): BaseAdapter() {

    var ownerInfoData = ArrayList<RelateOwnerListInfo>()

    override fun getCount(): Int {
        return ownerInfoData.size
    }

    override fun getItem(position: Int): Any {
        return ownerInfoData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val data = getItem(position) as RelateOwnerListInfo

        val view: View

        if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.dialog_owner_info_item, parent, false)
        } else {
            view = convertView
        }

        view.ownerNoText.text = position.toString()
        view.ownerDivisionText.text = "소유자"
        view.ownerNameText.text = data.name
        val sameNameNoString = data.sameNameNo
        if(sameNameNoString.equals("")) {
            view.ownerSameNameText.text = "-"
        } else {
            view.ownerSameNameText.text = sameNameNoString
        }
        view.ownerDelvyAddrText.text = data.delvyZip +
                                    data.delvyAdres +
                                    data.delvyAdresDetail

        view.ownerPosesnQota.setText(data.posesnQota)

        val unDcsnOwnerAtString = data.unDcsnOwnerAt
        if(unDcsnOwnerAtString.equals("Y")) {
            view.ownerUnDcsnOwnerAt.isChecked = true
        } else {
            view.ownerUnDcsnOwnerAt.isChecked = false
        }

        val ihidnumString = data.ihidnum
        if(ihidnumString.equals("")) {
            view.ownerCrpNoText.text = ihidnumString
        } else {
            val ihidnumStringSub = ihidnumString.substring1(0,8)
            view.ownerCrpNoText.text = ihidnumStringSub + "******"
        }

        view.ownerRgistAddrText.text = data.rgistAdres

        view.ownerPosesnQota.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                var ownerItem = LandInfoObject.addOwnerListInfo!!.getJSONObject(position) as JSONObject
                ownerItem.put("posesnQota", txtString)

                LandInfoObject.addOwnerListInfo!!.put(position, ownerItem)

            }
            false
        }
        view.ownerUnDcsnOwnerAt.setOnCheckedChangeListener{ buttonView, isChecked ->
            var ownerItem = LandInfoObject.addOwnerListInfo!!.getJSONObject(position) as JSONObject
            if(isChecked) {
                ownerItem.put("unDcsnOwnerAt", "Y")
            } else {
                ownerItem.put("unDcsnOwnerAt", "N")
            }

            LandInfoObject.addOwnerListInfo!!.put(position, ownerItem)

            false

        }

        return view
    }

    fun addItem(dataJson: JSONObject) {
        val relateOwnerListInfo = RelateOwnerListInfo()
        relateOwnerListInfo.posesnSe = checkStringNull(dataJson.getString("posesnSe"))
        relateOwnerListInfo.name = checkStringNull(dataJson.getString("ownerNm"))
        relateOwnerListInfo.sameNameNo = checkStringNull(dataJson.getString("sameNameNo"))
        relateOwnerListInfo.delvyAdres = checkStringNull(dataJson.getString("delvyAdres"))
        relateOwnerListInfo.delvyZip = checkStringNull(dataJson.getString("delvyZip"))
        relateOwnerListInfo.delvyAdresDetail = checkStringNull(dataJson.getString("delvyAdresDetail"))
        relateOwnerListInfo.posesnQota = checkStringNull(dataJson.getString("posesnQota"))
        relateOwnerListInfo.unDcsnOwnerAt = checkStringNull(dataJson.getString("unDcsnOwnerAt"))
        relateOwnerListInfo.ihidnum = checkStringNull(dataJson.getString("ihidnum"))
        relateOwnerListInfo.rgistAdres = checkStringNull(dataJson.getString("rgistAdres"))

        ownerInfoData.add(relateOwnerListInfo)
    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }
}