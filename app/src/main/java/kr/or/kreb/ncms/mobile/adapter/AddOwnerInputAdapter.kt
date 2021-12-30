/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.add_owner_dialog_item.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.AddOwnerListInfo
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.withIhidNumAsterRisk
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AddOwnerInputAdapter(var context: Context) :
    RecyclerView.Adapter<AddOwnerInputAdapter.ViewHolder>(), Filterable {

    var addOwnerData = ArrayList<AddOwnerListInfo>()
    var addSelectListItem = AddOwnerListInfo()

    var rbSelectPosition: Int = -1

    // filter
    var unfFlteredList: MutableList<AddOwnerListInfo> = addOwnerData
    var filteredList = mutableListOf<AddOwnerListInfo>()

    private var logUtil: LogUtil = LogUtil("AddOwnerInputAdapter")

    fun getSelectItem(): AddOwnerListInfo = addSelectListItem

    fun addItem(dataJson: JSONObject) {
        val addOwnerListInfo = AddOwnerListInfo()
        addOwnerListInfo.indvdlGrpSe = dataJson.getString("indvdlGrpSe")
        addOwnerListInfo.indvdlGrpNm = dataJson.getString("indvdlGrpNm")
        addOwnerListInfo.sameNameNo = dataJson.getString("sameNameNo")
        addOwnerListInfo.inhbtntCprNo = dataJson.getString("inhbtntCprNo")
        addOwnerListInfo.delvyAdres = dataJson.getString("delvyAdres")
        addOwnerListInfo.indvdlGrpCode = dataJson.getString("indvdlGrpCode")
        addOwnerListInfo.indvdlGrpSeNm = dataJson.getString("indvdlGrpSeNm")
        addOwnerListInfo.delvyZip = dataJson.getString("delvyZip")
        addOwnerListInfo.delvyAdresDetail = dataJson.getString("delvyAdresDetail")
        addOwnerData.add(addOwnerListInfo)

    }

    fun checkStringNull(nullString: String): String = if (nullString == "null") "" else { nullString }

    // 추가 (필터 적용 및 베이스 어댑터 변경)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddOwnerInputAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_owner_dialog_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddOwnerInputAdapter.ViewHolder, position: Int) {
        holder.bind(filteredList[position])

//        if(rbSelectPosition == position){
//            holder.itemView.isSelected = !holder.itemView.isSelected
//        } else {
//            holder.itemView.isSelected = false
//        }
    }

    override fun getItemCount(): Int = filteredList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(item: AddOwnerListInfo) {

            with(itemView) {
                ownerDivisionText.text = checkStringNull(item.indvdlGrpSeNm)

                ownerNameText.text = checkStringNull(item.indvdlGrpNm)
                val sameNameNoString = checkStringNull(item.sameNameNo)

                if (sameNameNoString == "") {
                    ownerSameNameText.text = "-"
                } else {
                    ownerSameNameText.text = sameNameNoString
                }

                val cprNoString = checkStringNull(item.inhbtntCprNo)
                if (cprNoString == "") {
                    ownerCrpNoText.text = cprNoString
                } else {
                    //val cprNoStringSub = cprNoString.substring(0, 8)
                    //ownerCrpNoText.text = "$cprNoStringSub******"
                    ownerCrpNoText.text = withIhidNumAsterRisk(cprNoString)
                }

                //ownerDelvyAddrText.text = checkStringNull(data.delvyZip) + " " + checkStringNull(data.delvyAdres) + " " + checkStringNull(data.delvyAdresDetail)
                ownerDelvyAddrText.text = "${checkStringNull(item.delvyZip)} ${checkStringNull(item.delvyAdres)} ${checkStringNull(item.delvyAdresDetail)}"
                ownerSelectRb.isChecked = adapterPosition == rbSelectPosition

                ownerSelectRb.setOnCheckedChangeListener { _, _ ->
                    ownerSelectRb.setOnClickListener {
                        addSelectListItem = item
                        rbSelectPosition = adapterPosition
                        logUtil.d("rbSelectPosition --------------------------------> $rbSelectPosition")
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredList = if (charString.isEmpty()) {
                    unfFlteredList
                } else {
                    val filteringList = mutableListOf<AddOwnerListInfo>()
                    for (item in unfFlteredList) {
                        if (item.indvdlGrpNm.lowercase(Locale.ROOT).contains(charString.lowercase(
                                Locale.ROOT).trim())) filteringList.add(item)
                    }
                    filteringList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results!!.values as MutableList<AddOwnerListInfo>
                notifyDataSetChanged()
            }
        }
    }
}