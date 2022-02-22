/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.*
import kotlinx.android.synthetic.main.include_biz_all.view.*
import kr.or.kreb.ncms.mobile.data.BizJibunListInfo
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.Biz
import kr.or.kreb.ncms.mobile.util.*
import org.json.JSONArray
import java.util.*


class BizAdapter(
    callDataArr: MutableList<Biz>,
    val type: String
) : RecyclerView.Adapter<BizAdapter.ViewHolder>(), Filterable {

    var pos: Int = 0

    var unfFlteredList: MutableList<Biz> = callDataArr
    var filteredList = mutableListOf<Biz>()

    private var selectedItems: SparseBooleanArray = SparseBooleanArray()
    private var prePosition = -1


//    var saupCode = "보상0001-0003-1" // 사업코드 임시 진행 토지/지장물 사업코드
//    var saupCode = "보상0001-0002-1" // 사업코드 임시 진행 분묘 사업코드
//    var saupCode = "보상0013-0001-3" // 사업코드 임시 진행
//    var saupCode = "보상0008-0008-1" // 사업코드 광업, 어업 진행

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.include_biz_all, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
        holder.setIsRecyclable(false)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val context: Context = itemView.context

        @SuppressLint("SetTextI18n")
        fun bind(item: Biz) {

            with(itemView) {

                textViewBizListCategory.text = item.bsnsClNm
                textViewBizSaupCode.text = checkStringNull(item.saupCode)
                textViewBizListName.text = checkStringNull(item.bsnsNm)
                textViewBizListAddr.text = checkStringNull(item.bsnsLocplc)
                textViewBizListManager.text = "(${checkStringNull(item.ncm)}) | ${checkStringNull(item.bsnsPsClNm)} | ${checkStringNull(item.oclhgBnora)} | ${checkStringNull(item.excDtls)} | ${checkStringNull(item.cntrctDe)}"
                textViewBizListDept.text = "해당필지"

                if (type == "all") {
                    bizListSubJibun.goneView()
                    detailJibunListView.goneView()
                } else {
                    bizListSubJibun.visibleView()
                }
                // TODO: 2021-12-28 사업선택 내 지번 표출 이벤트
                bizListSubJibun.setOnClickListener { view ->

                    view.isActivated = !view.isActivated

                    if (view.isActivated) {
                        imageViewBizListSubMenuArrow.animate().rotation(180f).setDuration(200).start() // on
                        detailJibunListView.visibleView()

                        PreferenceUtil.apply {
                            setString(context, "bizCategory", item.bsnsClNm)
                            setString(context, "bsnsNm", item.bsnsNm)
                            setString(context, "saupCode", item.saupCode)
                            setString(context, "bizItem", item.item.toString())
                            setString(context, "id", item.loginId)
                        }


                        val jibunSearchDataArr = mutableListOf<BizJibunListInfo>()

                        if(item.landList != null) {
                            val jibunSearchLadData = item.landList as JSONArray
                            if (jibunSearchLadData.length() > 0) {
                                for (i in 0 until jibunSearchLadData.length()) {
                                    val dataObject = jibunSearchLadData.getJSONObject(i)

                                    dataObject.apply {
                                        jibunSearchDataArr.add(
                                            BizJibunListInfo(
                                                "LAD",
                                                checkStringNull(getString("ladWtnCode")),
                                                null,
                                                checkStringNull(getString("saupCode")),
                                                checkStringNull(getString("legaldongCode")),
                                                checkStringNull(getString("gobuLndcgrCl")),
                                                checkStringNull(getString("gobuLndcgrNm")),
                                                checkStringNull(getString("bgnnAr")),
                                                checkStringNull(getString("incrprAr")),
                                                checkStringNull(getString("no")),
                                                checkStringNull(getString("subNo")),
                                                checkStringNull(getString("legaldongNm")),
                                                checkStringNull(getString("bgnnLnm")),
                                                checkStringNull(getString("incrprLnm")),
                                                checkStringNull(getString("ownerName")),
                                                checkStringNull(getString("ownerCnt")),
                                                checkStringNull(getString("relatesName")),
                                                checkStringNull(getString("relatesCnt")),
                                                type
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if(item.thingList != null) {
                            val jibunSearchThingData = item.thingList as JSONArray

                            if(jibunSearchThingData.length() > 0) {
                                for(i in 0 until jibunSearchThingData.length()) {
                                    val dataObject = jibunSearchThingData.getJSONObject(i)


                                    dataObject.apply {
                                        jibunSearchDataArr.add(
                                            BizJibunListInfo(
                                                "THING",
                                                null,
                                                checkStringNull(getString("thingWtnCode")),
                                                checkStringNull(getString("saupCode")),
                                                checkStringNull(getString("legaldongCode")),
                                                checkStringNull(getString("gobuLndcgrCl")),
                                                checkStringNull(getString("gobuLndcgrNm")),
                                                checkStringNull(getString("bgnnAr")),
                                                checkStringNull(getString("incrprAr")),
                                                null,
                                                null,
                                                checkStringNull(getString("legaldongNm")),
                                                checkStringNull(getString("bgnnLnm")),
                                                checkStringNull(getString("incrprLnm")),
                                                checkStringNull(getString("ownerName")),
                                                checkStringNull(getString("ownerCnt")),
                                                checkStringNull(getString("relatesName")),
                                                checkStringNull(getString("relateCnt")),
                                                type
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        val layoutManager = LinearLayoutManager(context)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        detailJibunList.layoutManager = layoutManager
                        detailJibunList.adapter = BizJibunAdapter(jibunSearchDataArr)

//                        layout_jibun_label.visibleView()
//                        layout_jibun_value.visibleView()

                    } else {
                        imageViewBizListSubMenuArrow.animate().rotation(0f).setDuration(200).start() // off
                        detailJibunListView.goneView()
//                        layout_jibun_label.goneView()
//                        layout_jibun_value.goneView()
                    }

                }

                // 사업확인 이동
                cardViewdBizItemLayout.setOnClickListener {

                    // TODO: 2021-06-08  임시적으로 Preference 에 넣어놈 (전역)

                    PreferenceUtil.apply {
                        setString(context, "bizCategory", item.bsnsClNm)
                        setString(context, "bsnsNm", item.bsnsNm)
                        setString(context, "saupCode", item.saupCode)
                        setString(context, "bizItem", item.item.toString())
                        setString(context, "id", item.loginId)
                    }

                    nextView(context, Constants.BIZ_CNFIRM_ACT, null, null, null, null)
                }

            }

        }
    }

//    private fun changeVisibility(isExpanded: Boolean) {
//        // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열
//        val va = if (isExpanded) ValueAnimator.ofInt(0, 600) else ValueAnimator.ofInt(600, 0)
//        // Animation이 실행되는 시간, n/1000초
//        va.duration = 500
//        va.addUpdateListener { animation -> // imageView의 높이 변경
//            carViewdBizItem.getLayoutParams().height = animation.animatedValue as Int
//            iv_2.requestLayout()
//            // imageView가 실제로 사라지게하는 부분
//            iv_2.setVisibility(if (isExpanded) View.VISIBLE else View.GONE)
//        }
//        // Animation start
//        va.start()
//    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredList = if (charString.isEmpty()) {
                    unfFlteredList
                } else {
                    val filteringList = mutableListOf<Biz>()
                    for (item in unfFlteredList) {
                        if (item.bsnsNm.lowercase(Locale.ROOT)
                                .contains(charString.lowercase(Locale.ROOT).trim())
                        ) filteringList.add(item)
                    }
                    filteringList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results!!.values as MutableList<Biz>
                notifyDataSetChanged()
            }
        }
        //tvBizMainCnt.text = "총 ${adapter.filteredList.size}건 의 사업명이 조회되었습니다."
    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

}