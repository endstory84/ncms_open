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
import kotlinx.android.synthetic.main.thing_buld_link_dialog_item.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.BuldSelectListInfo
import org.json.JSONObject

class BuldSelectListAdapter(var context: Context) : BaseAdapter() {

    var buldSelectData = ArrayList<BuldSelectListInfo>()
    var buldSelectListItem = ArrayList<BuldSelectListInfo>()

    override fun getCount(): Int {
        return buldSelectData.size
    }

    override fun getItem(position: Int): Any {
        return buldSelectData[position]
    }

    override fun getItemId(position: Int): Long {
       return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val data = getItem(position) as BuldSelectListInfo

        val view: View = if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.thing_buld_link_dialog_item, parent, false)
        } else {
            convertView
        }

        view.thingBuldLinkKndText.text = data.thingKnd
        view.thingBuldLinkArText.text = data.incrprAr + data.unitNm
        view.thingBuldLinkPrposText.text = data.buldPrpos
        view.thingBuldLinkRegstrPrposText.text = data.regstrBuldPrpos
        view.thingBuldLinkRgistPrposText.text = data.rgistBuldPrpos
        view.thingBuldLinkNrtBuldAt.isChecked = data.nrtBuldAt == "Y"
        view.thingBuldLinkSelectChk.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                buldSelectListItem.add(data)
            } else {
                buldSelectListItem.remove(data)
            }
        }


        return view
    }

    fun getSelectItem():ArrayList<BuldSelectListInfo> {
        return buldSelectListItem
    }

    fun addItem(dataJson: JSONObject) {
        val buldLinkDataInfo = BuldSelectListInfo()

        buldLinkDataInfo.thingWtnCode = checkStringNull(dataJson.getString("thingWtnCode"))
        buldLinkDataInfo.saupCode = checkStringNull(dataJson.getString("saupCode"))
        buldLinkDataInfo.thingLrgeCl = checkStringNull(dataJson.getString("thingLrgeCl"))
        buldLinkDataInfo.thingSmallCl = checkStringNull(dataJson.getString("thingSmallCl"))
        buldLinkDataInfo.thingSmallNm = checkStringNull(dataJson.getString("thingSmallNm"))
        buldLinkDataInfo.thingKnd = checkStringNull(dataJson.getString("thingKnd"))
        buldLinkDataInfo.strctNdStndtd = checkStringNull(dataJson.getString("strctNdStndrd"))
        buldLinkDataInfo.legaldongNm = checkStringNull(dataJson.getString("legaldongNm"))
        buldLinkDataInfo.bgnnLnm = checkStringNull(dataJson.getString("bgnnLnm"))
        buldLinkDataInfo.incrprLnm = checkStringNull(dataJson.getString("incrprLnm"))
        buldLinkDataInfo.incrprAr = checkStringNull(dataJson.getString("incrprAr"))
        buldLinkDataInfo.unitCl = checkStringNull(dataJson.getString("unitCl"))
        buldLinkDataInfo.unitNm = checkStringNull(dataJson.getString("unitNm"))
        buldLinkDataInfo.buldPrpos = checkStringNull(dataJson.getString("buldPrpos"))
        buldLinkDataInfo.regstrBuldPrpos = checkStringNull(dataJson.getString("regstrBuldPrpos"))
//        buldLinkDataInfo.regstrBuldPrpos = checkStringNull(dataJson.getString("regstrBuldPrPos"))
        buldLinkDataInfo.rgistBuldPrpos = checkStringNull(dataJson.getString("rgistBuldPrpos"))
        buldLinkDataInfo.nrtBuldAt = checkStringNull(dataJson.getString("nrtBuldAt"))
//        buldLinkDataInfo.nrtBuldAt = checkStringNull(dataJson.getString("netBuldAt"))
        buldLinkDataInfo.geoms = checkStringNull(dataJson.getString("geoms"))

        buldSelectData.add(buldLinkDataInfo)



    }


    fun checkStringNull(nullString: String): String = if (nullString == "null") "" else { nullString }
}