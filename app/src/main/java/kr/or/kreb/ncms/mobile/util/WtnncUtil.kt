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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.YearPickerDialogFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WtnncUtil(val activity: Activity, val context: Context) {
    private var tabLayout: TabLayout = activity.findViewById(R.id.tabLayout)
    private var viewPager2: ViewPager2 = activity.findViewById(R.id.wtnncViewPager)
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var logUtil: LogUtil

    var tabNameArr: ArrayList<String> = ArrayList()

    fun getActivity() : MapActivity = (context as MapActivity)

    fun viewPagerSetting(view: View, fragmentActivity: FragmentActivity, responseString: String?) {

        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        logUtil = LogUtil("WtnncUtil")

        viewPagerAdapter = ViewPagerAdapter(activity, context, fragmentActivity, Constants.BIZ_SUBCATEGORY_KEY)
        viewPager2.adapter = viewPagerAdapter
//        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                logUtil.d("===============>>>>>>>>>>>>>>        ViewPager OnPageSelected - position : ${position}, itemCnt : ${viewPagerAdapter.itemCount}, Type : ${viewPager2.adapter?.getItemViewType(position)}")
//                if(position + 1 == viewPager2.adapter?.itemCount) {
//                    viewPagerAdapter.showOwnerPopup()
//                }
//            }
//        })

        when (Constants.BIZ_SUBCATEGORY_KEY) {
            BizEnum.LAD -> {
                tabNameArr = arrayListOf("????????????", "????????????", "????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "LandInfo")
            }

            BizEnum.THING -> {
                tabNameArr = arrayListOf("???????????????", "????????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "ThingInfo")
            }

            BizEnum.TOMB -> {
                tabNameArr = arrayListOf("????????????", "?????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "tombInfo")
            }

            BizEnum.MINRGT -> {
                tabNameArr = arrayListOf("???????????????", "????????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "MinrgtInfo")
            }

            BizEnum.BSN -> {
                tabNameArr = arrayListOf("????????????", "?????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "BsnInfo")
            }

            BizEnum.FYHTS -> {
                tabNameArr = arrayListOf("???????????????", "????????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "FyhtsInfo")
            }

            BizEnum.FARM -> {
                tabNameArr = arrayListOf("????????????", "?????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "FarmInfo")
            }

            BizEnum.RESIDNT -> {
                tabNameArr = arrayListOf("???????????????", "????????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "ResidntInfo")
            }

            BizEnum.REST_LAD -> {
                tabNameArr = arrayListOf("????????????", "????????? ??????", "?????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "LandInfo")
            }

            BizEnum.REST_THING -> {
                tabNameArr = arrayListOf("???????????? ??????", "????????? ????????? ??? ?????????")
                wtnncTypeSetting(responseString, /*viewPager2.adapter, */tabNameArr, "ThingInfo")
            }

            else ->  {}
        }

        /**
         * ??? ???????????? get Index
         */
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab){
                Constants.GLOBAL_VIEW_PAGER = viewPager2
                Constants.GLOBAL_TAB_LAYOUT = tabLayout

                logUtil.d(tab.position.toString())

                if(ThingWtnObject.thingWtnncSaveFlag){
                    logUtil.d("'??????' ????????? ?????? ?????? -> ${ThingWtnObject.thingWtnncSaveFlag}")
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
     * ????????? ???????????? ??????
     */
    private fun wtnncTypeSetting(
        responseString: String?,
//        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?,
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

//        viewPager2.adapter = adapter

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
            setTitleText("?????? ??????")
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
                    ThingTombObject.burlDe = dateText // ????????????
                }
                BizEnum.BSN -> {
                    when (value) {
                        "bsnPrmisnDe" -> ThingBsnObject.bsnPrmisnDe = dateText // ????????? ??????
                        "bsnRgsde" -> ThingBsnObject.bizrdtlsBizDe = dateText // ??????????????????
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

    fun wtnncSpinnerAdapter(codeGroupId: String, spinner: Spinner, listener: AdapterView.OnItemSelectedListener?) {
        val codeArray = CommonCodeInfoList.getCodeDcArray(codeGroupId)
        if(codeArray.isNullOrEmpty()) {
            val reqUrl = context.getString(R.string.mobile_url) + "selectCommonCodeList"
            val map = HashMap<String, String>()
            map.put("newCodeGroupId", codeGroupId)
            HttpUtil.getInstance(context)
                .callUrlJsonCodeWebServer(reqUrl, map,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logUtil.e("error")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        activity.runOnUiThread {
                            val dataJSON = JSONObject(responseString)
                            val dataList = dataJSON.getJSONObject("list")
                            CommonCodeInfoList.addCode(dataList)

                            spinner.adapter =
                                CustomDropDownAdapter(context, CommonCodeInfoList.getCodeDcArray(codeGroupId))
                            spinner.onItemSelectedListener = listener
                        }
                    }

                })
        } else {
            spinner.adapter = CustomDropDownAdapter(context, CommonCodeInfoList.getCodeDcArray(codeGroupId))
            spinner.onItemSelectedListener = listener
        }

    }

    /*fun wtnncDateRangePicker(fragmentManager: FragmentManager, textView: TextView, value: String) {
        var firstDate: String = ""
        var secondDate: String = ""
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("?????? ??????")
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
                        "pssRentPeriodText" -> { // ????????????
                            ThingBsnObject.apply {
                                pssRentBgnde = firstDate
                                pssRentEndde = secondDate
                            }
                        }
                        "bsnPrmisnPc" -> { // ???????????????
                            ThingBsnObject.apply {
                                bsnPrmisnBgnde = firstDate
                                bsnPrmisnEndde = secondDate
                            }
                        }
                        "bsnBsnPd" -> { // ???????????????
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
                        "farmLesseePc" -> { // ????????????
                            ThingFarmObject.apply {
                                posesnRentBgnde = firstDate
                                posesnRentEndde = secondDate
                            }
                        }
                        "farmClvtPd" -> { // ????????????
                            ThingFarmObject.apply {
                                clvtBgnde = firstDate
                                clvtEndde = secondDate
                            }
                        }

                        "addFarmClvtPd" -> { // ????????????
                            ThingFarmObject.apply {
                                clvtBgndeList!!.add(firstDate)
                                clvtEnddeList!!.add(secondDate)
                            }
                        }

                    }
                }
                BizEnum.RESIDNT -> {
                    when (value) {
                        "addResidePd" -> { // ????????????
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

