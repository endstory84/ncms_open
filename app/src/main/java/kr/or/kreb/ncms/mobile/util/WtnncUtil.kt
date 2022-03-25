/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.YearPickerDialogFragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WtnncUtil(val activity: Activity, val context: Context) {
    private var tabLayout: TabLayout = activity.findViewById(R.id.tabLayout)
    private var viewPager2: ViewPager2 = activity.findViewById(R.id.wtnncViewPager)
    lateinit var logUtil: LogUtil

    var tabNameArr: ArrayList<String> = ArrayList()

    fun getActivity() : MapActivity = (context as MapActivity)

    fun viewPagerSetting(view: View, fragmentActivity: FragmentActivity, responseString: String?) {

        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        logUtil = LogUtil("WtnncUtil")

        viewPager2.adapter = ViewPagerAdapter(activity, context, fragmentActivity, Constants.BIZ_SUBCATEGORY_KEY)

        when (Constants.BIZ_SUBCATEGORY_KEY) {
            BizEnum.LAD -> {
                tabNameArr = arrayListOf("토지내역", "토지조서", "소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "LandInfo")
            }

            BizEnum.THING -> {
                tabNameArr = arrayListOf("지장물조서", "지장물 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "ThingInfo")
            }

            BizEnum.TOMB -> {
                tabNameArr = arrayListOf("분묘조서", "분묘 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "tombInfo")
            }

            BizEnum.MINRGT -> {
                tabNameArr = arrayListOf("광업권조사", "광업권 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "MinrgtInfo")
            }

            BizEnum.BSN -> {
                tabNameArr = arrayListOf("영업조사", "영업 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "BsnInfo")
            }

            BizEnum.FYHTS -> {
                tabNameArr = arrayListOf("어업권조사", "어업권 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "FyhtsInfo")
            }

            BizEnum.FARM -> {
                tabNameArr = arrayListOf("농업조사", "농업 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "FarmInfo")
            }

            BizEnum.RESIDNT -> {
                tabNameArr = arrayListOf("거주자조사", "거주자 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "ResidntInfo")
            }

            BizEnum.REST_LAD -> {
                tabNameArr = arrayListOf("토지내역", "잔여지 조서", "토지 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "LandInfo")
            }

            BizEnum.REST_THING -> {
                tabNameArr = arrayListOf("잔여건물 조서", "지장물 소유자 및 관계인")
                wtnncTypeSetting(responseString, viewPager2.adapter, tabNameArr, "ThingInfo")
            }

            else ->  {}
        }

        /**
         * 탭 레이아웃 get Index
         */
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab){
                Constants.GLOBAL_VIEW_PAGER = viewPager2
                Constants.GLOBAL_TAB_LAYOUT = tabLayout

                logUtil.d(tab.position.toString())

                if(ThingWtnObject.thingWtnncSaveFlag){
                    logUtil.d("'저장' 이벤트 실행 여부 -> ${ThingWtnObject.thingWtnncSaveFlag}")
                    ThingWtnObject.thingWtnncSaveFlag = !ThingWtnObject.thingWtnncSaveFlag
                    setThingData()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) = logUtil.d("reselect -> ${tab.position}")
        })
    }

    fun setThingData(){
        getActivity().addWtnccThingTypeSetData()
    }

    /**
     * 조서별 뷰페이저 세팅
     */
    private fun wtnncTypeSetting(
        responseString: String?,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?,
        tabNameArr: ArrayList<String>,
        intentStr: String
    ) {

        when {
            responseString != "" -> {
                val responseJson = JSONObject(responseString)
                logUtil.d("responseJSON $responseJson")

                val landInfoJson = responseJson.getJSONObject("list")
                activity.intent.putExtra(intentStr, landInfoJson.toString())
            }
            else -> activity.intent.putExtra(intentStr, "")
        }

        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = tabNameArr[0]
                1 -> tab.text = tabNameArr[1]
                else -> {
                    if(tabNameArr.size > 2){
                        tab.text = tabNameArr[2]
                    }
                }
            }
        }.attach()
    }

    fun thingViewPagerClose() {
        val bottomSheet = activity.findViewById<CoordinatorLayout>(R.id.include_wtnncs)
        bottomSheet.goneView()
    }

    fun wtnncDatePicker(fragmentManager: FragmentManager, textView: TextView, value: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText("날짜 선택")
            setSelection(MaterialDatePicker.thisMonthInUtcMilliseconds())

        }.build()
        datePicker.addOnPositiveButtonClickListener {
            val dateText = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(it)
            textView.text = dateText

            when (Constants.BIZ_SUBCATEGORY_KEY) {
                BizEnum.MINRGT -> {
                    when (value) {
                        "mnidstRegistDe" -> ThingMinrgtObject.minrgtRegNo = dateText
                        "mnidstProspectPlan" -> ThingMinrgtObject.prsptnPlanStemDe = dateText
                        "mnidstMiningPlan" -> ThingMinrgtObject.miningPlanCnfmDe = dateText
                    }
                }
                BizEnum.FYHTS -> {
                    when (value) {
                        "lcnsDe" -> ThingFyhtsObject.lcnsDe = dateText
                        "fyhtsCntnncPdBgnde" -> ThingFyhtsObject.fyhtsCntnncPdBgnde = dateText
                        "fyhtsCntnncPdEndde" -> ThingFyhtsObject.fyhtsCntnncPdEndde = dateText
                    }

                }
                BizEnum.TOMB -> {
                    ThingTombObject.burlDe = dateText // 매장일자
                }
                BizEnum.BSN -> {
                    when (value) {
                        "bsnPrmisnDe" -> ThingBsnObject.bsnPrmisnDe = dateText // 허가등 일자
                        "bsnRgsde" -> ThingBsnObject.bizrdtlsBizDe = dateText // 사업등록일자
                        "bsnBsnPdBgned" -> ThingBsnObject.bsnBsnpdBgnde = dateText
                        "bsnBsnPdEndde" -> ThingBsnObject.bsnBsnpdEndde = dateText
                        "bsnPrmisnPcBgned" -> ThingBsnObject.bsnPrmisnBgnde = dateText
                        "bsnPrmisnPcEnded" -> ThingBsnObject.bsnPrmisnEndde = dateText
                        "pssRentPeriodBgndeText" -> ThingBsnObject.pssRentBgnde = dateText
                        "pssRentPeriodEnddeText" -> ThingBsnObject.pssRentEndde = dateText
                        "bsnBrdPdBgnde" -> ThingBsnObject. addBsnBrdPdBgnde.add(dateText)
                        "bsnBrdPdEndde" -> ThingBsnObject. addBsnBrdPdEndde.add(dateText)
                    }
                }
                BizEnum.FARM -> {
                    when (value) {
                        "posesnRentBgnde" -> ThingFarmObject.posesnRentBgnde = dateText
                        "posesnRentEndde" -> ThingFarmObject.posesnRentEndde = dateText
                        "addFarmClvtBgnde" -> ThingFarmObject.clvtBgndeList!!.add(dateText)
                        "addFarmClvtEndde" -> ThingFarmObject.clvtEnddeList!!.add(dateText)
                    }
                }
                BizEnum.RESIDNT -> {
                    when (value) {
                        "residntPssRentBgnDe" -> ThingResidntObject.pssRentBgnde = dateText
                        "residntPssRentEndde" -> ThingResidntObject.pssRentEndde = dateText
                        "addResidePdBgnde" -> ThingResidntObject.residePdBgndeList.add(dateText)
                        "addResidePdEndde" -> ThingResidntObject.residePdEnddeList.add(dateText)

                    }
                }
                else -> {}
            }
        }
        datePicker.show(fragmentManager, datePicker.toString())
    }

    fun wtnncSpinnerAdapter(stringArray: Int, spinner: Spinner, listener: AdapterView.OnItemSelectedListener?) {
        spinner.adapter = CustomDropDownAdapter(context, listOf(context.resources.getStringArray(stringArray))[0])
        spinner.onItemSelectedListener = listener
    }

    /*fun wtnncDateRangePicker(fragmentManager: FragmentManager, textView: TextView, value: String) {
        var firstDate: String = ""
        var secondDate: String = ""
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("기간 선택")
                .setSelection(
                    Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .build()
        dateRangePicker.addOnPositiveButtonClickListener {
            firstDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(it.first)
            secondDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(it.second)
            textView.text = "$firstDate ~ $secondDate"

            when (Constants.BIZ_SUBCATEGORY_KEY) {
                BizEnum.MINRGT -> {
                    ThingMinrgtObject.apply {
                        cntnncPdBgnde = firstDate
                        cntnncPdEndde = secondDate
                    }
                }
                BizEnum.FYHTS -> {
                    ThingFyhtsObject.apply {
                        fyhtsCntnncPdBgnde = firstDate
                        fyhtsCntnncPdEndde = secondDate
                    }
                }
                BizEnum.BSN -> {
                    when (value) {
                        "pssRentPeriodText" -> { // 임차기간
                            ThingBsnObject.apply {
                                pssRentBgnde = firstDate
                                pssRentEndde = secondDate
                            }
                        }
                        "bsnPrmisnPc" -> { // 허가등기간
                            ThingBsnObject.apply {
                                bsnPrmisnBgnde = firstDate
                                bsnPrmisnEndde = secondDate
                            }
                        }
                        "bsnBsnPd" -> { // 허가등기간
                            ThingBsnObject.apply {
                                bsnBsnpdBgnde = firstDate
                                bsnBsnpdEndde = secondDate
                            }
                        }
                        "bsnBrdPd" -> {
                            ThingBsnObject.apply {
                                bsnBrdpdBgnde = firstDate
                                bsnBrdpdEndde = secondDate
                            }
                        }
                        "addBsnBrdPd" -> {
                            ThingBsnObject.apply {
                                addBsnBrdPdBgnde.add(firstDate)
                                addBsnBrdPdEndde.add(secondDate)
                            }
                        }
                    }
                }

                BizEnum.FARM -> {
                    when (value) {
                        "farmLesseePc" -> { // 임차기간
                            ThingFarmObject.apply {
                                posesnRentBgnde = firstDate
                                posesnRentEndde = secondDate
                            }
                        }
                        "farmClvtPd" -> { // 경작기간
                            ThingFarmObject.apply {
                                clvtBgnde = firstDate
                                clvtEndde = secondDate
                            }
                        }

                        "addFarmClvtPd" -> { // 경작기간
                            ThingFarmObject.apply {
                                clvtBgndeList!!.add(firstDate)
                                clvtEnddeList!!.add(secondDate)
                            }
                        }

                    }
                }
                BizEnum.RESIDNT -> {
                    when (value) {
                        "addResidePd" -> { // 거주기간
                            ThingResidntObject.apply {
                                residePdBgndeList.add(firstDate)
                                residePdEnddeList.add(secondDate)
                            }

                        }
                    }
                }

            }
        }
        dateRangePicker.show(fragmentManager, dateRangePicker.toString())
    }*/

    fun wtnncYearPicker(fragmentActivity: FragmentActivity, textView: TextView, value: String?) {
        activity.layoutInflater.inflate(R.layout.wtnnc_year_picker, null).let {
            val yearPickerDialog = YearPickerDialogFragment(activity, textView)
            yearPickerDialog.show(fragmentActivity.supportFragmentManager, "yearPicker")

        }
    }
}

