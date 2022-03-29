/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.ThingKndTitleListAdapter
import kr.or.kreb.ncms.mobile.adapter.ThingKndDetailListAdapter
import kr.or.kreb.ncms.mobile.adapter.ThingKndSubListAdapter
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface
import kr.or.kreb.ncms.mobile.util.DialogUtil
import kr.or.kreb.ncms.mobile.util.HttpUtil
import kr.or.kreb.ncms.mobile.util.PermissionUtil.logUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil
import kr.or.kreb.ncms.mobile.util.getDisplayDistance
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ThingDialogFragment(
    context: Context,
    activity: Activity,
    v: View,
    value: Int,
    thingListener: ThingViewPagerInterface,
    jibun: String?
//    fragmentActivity: FragmentActivity,

    ) : DialogFragment(),
    ThingKndTitleListAdapter.OnItemClickListener,
    ThingKndSubListAdapter.OnItemSubClickListener {

    private lateinit var thingKndListView: RecyclerView
    private lateinit var thingKndSubListView: RecyclerView
    private lateinit var thingKndDetailListView: RecyclerView
    private lateinit var thingAdapterKndTitlsAdapter: ThingKndTitleListAdapter
    private lateinit var thingAdapterKndDetailAdapter: ThingKndDetailListAdapter
    private lateinit var thingAdapterKndSubListAdapter: ThingKndSubListAdapter
    private val mActivity: Activity = activity
    private val mContext: Context = context
    private val v: View = v
    //지장물 임시 소분류
    private val arrayList: ArrayList<Int> = arrayListOf(R.string.thingDialogWdpt, R.string.thingDialogMaterial, R.string.thingDialogBuild)
//    private val treeImgList: ArrayList<Int> = arrayListOf()
//    private val treeTitleList: ArrayList<String> = arrayListOf()
//    private val treeTextList: ArrayList<String> = arrayListOf()
//    private val buildImgList: ArrayList<Int> = arrayListOf()
//    private val buildTitleList: ArrayList<String> = arrayListOf()
//    private val buildTextList: ArrayList<String> = arrayListOf()
//    private val commImgList: ArrayList<Int> = arrayListOf()
//    private val commTitleList: ArrayList<String> = arrayListOf()
//    private val commTextList: ArrayList<String> = arrayListOf()

    // TODO :
    /*
    private val treeSubList: ArrayList<Int> = arrayListOf()
    private val buildSubList: ArrayList<Int> = arrayListOf()
    private val materialSubList: ArrayList<Int> = arrayListOf()
    */
    private var treeSubList: ArrayList<String> = arrayListOf()
    private var buildSubList: ArrayList<String> = arrayListOf()
    private var materialSubList: ArrayList<String> = arrayListOf()

    private val value: Int = value
    private val listener: ThingViewPagerInterface = thingListener
    private val jibun: String? = jibun

    var builderMater: MaterialAlertDialogBuilder? = null
    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null
    private var toastUtil: ToastUtil = ToastUtil(mContext)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        builderMater = context?.let { MaterialAlertDialogBuilder(it) }!!
        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))




        thingKndListView = v.findViewById(R.id.thingKndList)
        thingKndSubListView = v.findViewById(R.id.thingKndSubList)
        thingKndDetailListView = v.findViewById(R.id.thingKndDetailList)

        var thingDialogExitBtn: ImageView = v.findViewById(R.id.thingDialogExitBtn)
        val builder = AlertDialog.Builder(mContext)

        builder.setView(v)

        val dlg = builder.create()

        //지장물 임시 소분류
//        arrayList.add(R.string.thingDialogWdpt)
//        arrayList.add(R.string.thingDialogMaterial)
//        arrayList.add(R.string.thingDialogBuild)

        subListInput()



        thingAdapterKndTitlsAdapter = ThingKndTitleListAdapter(arrayList, mContext)
        thingAdapterKndSubListAdapter = ThingKndSubListAdapter(treeSubList, mContext, 0, this, listener)

        thingKndSubListView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        thingAdapterKndSubListAdapter.setOnItemSubClickListener(this)
        thingKndSubListView.adapter = thingAdapterKndSubListAdapter

        thingKndListView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        thingAdapterKndTitlsAdapter.setOnItemClickListener(this)
        thingKndListView.adapter = thingAdapterKndTitlsAdapter



        thingDialogExitBtn.setOnClickListener{
            this.dismiss()
        }

        setThingKndDetail("0", "-1")

//        thingKndDetailListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        thingKndDetailListView.adapter = thingAdapterKndDetailAdapter

        return dlg

    }

    override fun onResume() {
        super.onResume()
        val outMetrics = DisplayMetrics()

//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
//            val display:Display? = mActivity.display
//            display?.getRealMetrics(outMetrics)
//
//        }else {
//            @Suppress("DEPRECATION")
//            val display = activity?.windowManager?.defaultDisplay
//            @Suppress("DEPRECATION")
//            display?.getRealMetrics(outMetrics)
//        }
//
//        val deviceHeight: Int = outMetrics.heightPixels
//        val deviceWidth: Int = outMetrics.widthPixels
//
//        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
//        params?.width = (deviceWidth * 0.95).toInt()
//        params?.height = (deviceHeight * 0.75).toInt()
//        dialog?.window?.attributes = params as WindowManager.LayoutParams
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        getDisplayDistance(dialog, activity, 0.95F, 0.75F)

    }

    override fun chkType(type: Int) {
        when (type) {
            0 -> {
                thingAdapterKndSubListAdapter = ThingKndSubListAdapter(treeSubList, mContext, type, this, listener)
                thingAdapterKndSubListAdapter.setOnItemSubClickListener(this)
                setThingKndDetail("0", "-1")
//                thingAdapterKndDetailAdapter = ThingKndDetailListAdapter(mActivity, mContext, type, treeImgList, treeTitleList, treeTextList, this, value, listener, jibun!!)
            }

            1 -> {
                thingAdapterKndSubListAdapter = ThingKndSubListAdapter(materialSubList, mContext, type, this, listener)
                thingAdapterKndSubListAdapter.setOnItemSubClickListener(this)
                //자제 전체
                setThingKndDetail("1", "-1")
//                thingAdapterKndDetailAdapter = ThingKndDetailListAdapter(mActivity, mContext, type, buildImgList, buildTitleList, buildTextList, this, value,listener, jibun!!)

            }
            2 -> {
                thingAdapterKndSubListAdapter = ThingKndSubListAdapter(buildSubList, mContext, type, this, listener)
                thingAdapterKndSubListAdapter.setOnItemSubClickListener(this)
                // 건물 전체
                setThingKndDetail("2", "-1")
//                thingAdapterKndDetailAdapter = ThingKndDetailListAdapter(mActivity, mContext, type, commImgList, commTitleList, commTextList, this, value, listener, jibun!!)
            }
        }
        thingKndSubListView.adapter = thingAdapterKndSubListAdapter
        thingKndDetailListView.adapter = thingAdapterKndDetailAdapter
    }

    override fun chkSubType(type: Int, thingType: Int) {

        if (setThingKndDetail(thingType, type) > 0) {
            thingKndSubListView.adapter = thingAdapterKndSubListAdapter
        }

//        if(thingType == 0) {
//            when (type) {
//                //전체
//                    0 -> setThingKndDetail("0", "-1")
//                //전국
//                1 -> setThingKndDetail("0", "10")
//                //서울
//                2 -> setThingKndDetail("0", "11")
//                //부산
//                3 -> setThingKndDetail("0", "26")
//                //대구
//                4 -> setThingKndDetail("0", "27")
//                //인천
//                5 -> setThingKndDetail("0", "28")
//                //광주
//                6 -> setThingKndDetail("0", "29")
//                //대전
//                7 -> setThingKndDetail("0", "30")
//                //울산
//                8 -> setThingKndDetail("0", "31")
//                //세종
//                9 -> setThingKndDetail("0", "36")
//                //경기
//                10-> setThingKndDetail("0", "41")
//                //강원
//                11-> setThingKndDetail("0", "42")
//                //충북
//                12-> setThingKndDetail("0", "43")
//                //충남
//                13-> setThingKndDetail("0", "44")
//                //전북
//                14-> setThingKndDetail("0", "45")
//                //전남
//                15-> setThingKndDetail("0", "46")
//                //경북
//                16-> setThingKndDetail("0", "47")
//                //경남
//                17-> setThingKndDetail("0", "48")
//                //제주
//                18-> setThingKndDetail("0", "50")
//
//            }
//        } else if(thingType == 1) {
//            when (type) {
//                //전체
//                0 -> setThingKndDetail("1", "-1")
//                //구조제
//                1 -> setThingKndDetail("1", "구조제")
//                //지붕재
//                2 -> setThingKndDetail("1", "지붕재")
//                //내외벽재
//                3 -> setThingKndDetail("1", "내외벽재")
//                //바닥재
//                4 -> setThingKndDetail("1", "바닥재")
//                //전청재
//                5 -> setThingKndDetail("1", "전청재")ㄴ
//                //단열재
//                6 -> setThingKndDetail("1", "단열재")
//                //창호
//                7 -> setThingKndDetail("1", "창호")
//            }
//        } else if(thingType == 2) {
//            when (type) {
//                //전체
//                0 -> setThingKndDetail("2", "-1")
////                일반주택
//                1 -> setThingKndDetail("2", "일반주택")
////                전통한옥_소형
//                2 -> setThingKndDetail("2", "전통한옥_소형")
////                신한옥
//                3 -> setThingKndDetail("2", "신한옥")
////                고급주택
//                4 -> setThingKndDetail("2", "고급주택")
////                다가구주택
//                5 -> setThingKndDetail("2", "다가구주택")
////                아파트
//                6 -> setThingKndDetail("2", "아파트")
////                주상복합아파트
//                7 -> setThingKndDetail("2", "주상복합아파트")
////                연립주택
//                8 -> setThingKndDetail("2", "연립주택")
////                연립주택 고급타운형
//                9 -> setThingKndDetail("2", "연립주택 고급타운형")
////                다세대주택
//                10 -> setThingKndDetail("2", "다세대주택")
////                기숙사
//                11 -> setThingKndDetail("2", "기숙사")
////                점포 및 상가
//                12 -> setThingKndDetail("2", "점포 및 상가")
////                목욕장
//                13 -> setThingKndDetail("2", "목욕장")
////                목욕장_사우나시설포함
//                14 -> setThingKndDetail("2", "목욕장_사우나시설포함")
////                일반창고
//                15 -> setThingKndDetail("2", "일반창고")
////                저온창고
//                16 -> setThingKndDetail("2", "저온창고")
////                냉동창고
//                17 -> setThingKndDetail("2", "냉동창고")
////                일반공장
//                18 -> setThingKndDetail("2", "일반공장")
////                냉동공장
//                19 -> setThingKndDetail("2", "냉동공장")
////                반도체공장
//                20 -> setThingKndDetail("2", "반도체공장")
////                아파트형공장
//                21 -> setThingKndDetail("2", "아파트형공장")
////                여관
//                22 -> setThingKndDetail("2", "여관")
////                호텔
//                23 -> setThingKndDetail("2", "호텔")
////                콘도미니엄_호텔형
//                24 -> setThingKndDetail("2", "콘도미니엄_호텔형")
////                콘도미니엄_빌라형
//                25 -> setThingKndDetail("2", "콘도미니엄_빌라형")
////                일반업무시설
//                26 -> setThingKndDetail("2", "일반업무시설")
////                오피스텔
//                27 -> setThingKndDetail("2", "오피스텔")
////                공공업무시설
//               28 -> setThingKndDetail("2", "공공업무시설")
////                백화점
//                29 -> setThingKndDetail("2", "백화점")
////                대형할인점
//                30 -> setThingKndDetail("2", "대형할인점")
////                볼링장
//                31 -> setThingKndDetail("2", "볼링장")
////                체육관
//                32 -> setThingKndDetail("2", "체육관")
////                레저시설
//                33 -> setThingKndDetail("2", "레저시설")
////                병원
//                34 -> setThingKndDetail("2", "병원")
////                장례식장
//                35 -> setThingKndDetail("2", "장례식장")
////                예식장
//                36 -> setThingKndDetail("2", "예식장")
////                영화관
//                37 -> setThingKndDetail("2", "영화관")
////                학교
//                38 -> setThingKndDetail("2", "학교")
////                주유소
//                39 -> setThingKndDetail("2", "주유소")
////                주차빌딩
//                40 -> setThingKndDetail("2", "주차빌딩")
////                교회
//                41 -> setThingKndDetail("2", "교회")
////                재실
//                42 -> setThingKndDetail("2", "재실")
////                유치원
//                43 -> setThingKndDetail("2", "유치원")
////                축사
//                44 -> setThingKndDetail("2", "축사")
//            }
//        }
//
//        thingKndSubListView.adapter = thingAdapterKndSubListAdapter
    }

    fun setThingKndDetail(thingKnd: Int, thingKndSub: Int): Int {

        val thingKndStringId = "thing_knd_sub_nm_${thingKnd}_${thingKndSub}"
        // Resource ID. 존재하지 않으면 0
        val thingKndResId = resources.getIdentifier(thingKndStringId, "string", activity!!.packageName)

        if (thingKndResId > 0) {
            setThingKndDetail(thingKnd.toString(), activity!!.getString(thingKndResId))
        }

        return thingKndResId
    }

    fun setThingKndDetail(thingKnd: String, thingKndSub: String) {

        val thingKndAllMap = HashMap<String, String>()
        thingKndAllMap["thingKnd"] = thingKnd //0: 수목 1: 자제 2: 건물
        thingKndAllMap["thingKndSub"] = thingKndSub // 수목 전체 : -1 전국 : 0

        val thingKndUrl = context!!.resources.getString(R.string.mobile_url) + "thingKndDownload"

        HttpUtil.getInstance(mContext)
            .callerUrlInfoPostWebServer(thingKndAllMap, progressDialog, thingKndUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dismissDialog()
                        mActivity.runOnUiThread {
                            toastUtil.msg_error(R.string.msg_server_connected_fail, 100)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        dismissDialog()

                        try {
                            val responseString = response.body!!.string()
                            logUtil.d(responseString)

                            val dataObject = JSONObject(responseString)
                            thingDetailListInput(thingKnd, dataObject, thingKndSub)
                            //                        thingAdapterKndDetailAdapter = ThingKndDetailListAdapter(mActivity, mContext, 0, commImgList, commTitleList, commTextList, this, value, listener, jibun!!)
                        }
                        catch(e: Exception) {
                            e.printStackTrace()
                            mActivity.runOnUiThread {
                                toastUtil.msg_error(R.string.msg_server_connected_fail, 100)
                            }
                        }


                    }

                })



//        thingAdapterKndDetailAdapter = ThingKndDetailListAdapter(mActivity, mContext, 0, commImgList, commTitleList, commTextList, this, value, listener, jibun!!)

//        thingKndDetailListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        thingKndDetailListView.adapter = thingAdapterKndDetailAdapter
    }

    fun thingDetailListInput(thingType: String, data: JSONObject, thingKndSub: String) {
        mActivity.runOnUiThread {
            var dataArray = data.getJSONObject("list").getJSONArray("data") as JSONArray

            if(thingKndDetailListView.layoutManager == null) {
                thingAdapterKndDetailAdapter =
                    ThingKndDetailListAdapter(mActivity, mContext, thingType, thingKndSub, dataArray, this, value, listener, jibun!!)
                thingKndDetailListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                thingKndDetailListView.adapter = thingAdapterKndDetailAdapter
            } else {
                thingAdapterKndDetailAdapter.setJSONArray(dataArray, thingType, thingKndSub)
                thingAdapterKndDetailAdapter.notifyDataSetChanged()
            }

        }
    }


    fun subListInput() {

        var thingTreeArr = resources.getStringArray(R.array.thingTreeArr)
        treeSubList = thingTreeArr.toCollection(ArrayList<String>())

        var materialArr = resources.getStringArray(R.array.materialArr)
        materialSubList = materialArr.toCollection(ArrayList<String>())

        var buildArr = resources.getStringArray(R.array.buildArr)
        buildSubList = buildArr.toCollection(ArrayList<String>())

//        //지장물 임시 수목
//        treeImgList.add(R.drawable.thing_tree_01)
//        treeImgList.add(R.drawable.thing_tree_02)
//        treeImgList.add(R.drawable.thing_tree_03)
//        treeImgList.add(R.drawable.thing_tree_04)
//        treeImgList.add(R.drawable.thing_tree_05)
//
//        treeTitleList.add("너도밤나무")
//        treeTitleList.add("조팝나무")
//        treeTitleList.add("아왜나무")
//        treeTitleList.add("좀작살나무")
//        treeTitleList.add("꽝꽝나무")
//
//        treeTextList.add("밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무")
//        treeTextList.add("조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝")
//        treeTextList.add("아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜")
//        treeTextList.add("좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살")
//        treeTextList.add("꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝")
//
//        //지장물 임시 건물
//        buildImgList.add(R.drawable.thing_bild_01)
//        buildImgList.add(R.drawable.thing_bild_02)
//        buildImgList.add(R.drawable.thing_bild_03)
//        buildImgList.add(R.drawable.thing_bild_04)
//        buildImgList.add(R.drawable.thing_bild_05)
//
//        buildTitleList.add("단독주택")
//        buildTitleList.add("다중주택")
//        buildTitleList.add("다세대주택")
//        buildTitleList.add("공장")
//        buildTitleList.add("다가구주택")
//
//        buildTextList.add("단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독")
//        buildTextList.add("다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중")
//        buildTextList.add("다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대")
//        buildTextList.add("공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장")
//        buildTextList.add("다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구")
//
//        //지장물 임시 일반반
//        commImgList.add(R.drawable.thing_comm_01)
//        commImgList.add(R.drawable.thing_comm_02)
//        commImgList.add(R.drawable.thing_comm_03)
//        commImgList.add(R.drawable.thing_comm_04)
//        commImgList.add(R.drawable.thing_comm_05)
//
//        commTitleList.add("개간비용")
//        commTitleList.add("공작물2")
//        commTitleList.add("공작물3")
//        commTitleList.add("공작물4")
//        commTitleList.add("공작물5")
//
//        commTextList.add("개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간")
//        commTextList.add("공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1")
//        commTextList.add("공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2")
//        commTextList.add("공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3")
//        commTextList.add("공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4")


    }

    fun dismissDialog() {
        activity?.runOnUiThread {
            progressDialog?.dismiss()
        }
    }
}