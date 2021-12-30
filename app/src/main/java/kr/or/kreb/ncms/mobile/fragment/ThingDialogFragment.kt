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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.ThingDialogRvAdapter1
import kr.or.kreb.ncms.mobile.adapter.ThingDialogRvAdapter2
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface
import kr.or.kreb.ncms.mobile.util.getDisplayDistance

class ThingDialogFragment(
    context: Context,
    activity: Activity,
    v: View,
    value: Int,
    thingListener: ThingViewPagerInterface,
    jibun: String?
//    fragmentActivity: FragmentActivity,

    ) : DialogFragment(),
    ThingDialogRvAdapter1.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var thingAdapter: ThingDialogRvAdapter1
    private lateinit var thingAdapter2: ThingDialogRvAdapter2
    private val mActivity: Activity = activity
    private val mContext: Context = context
    private val v: View = v
    private val arrayList: ArrayList<Int> = arrayListOf()
    private val treeImgList: ArrayList<Int> = arrayListOf()
    private val treeTitleList: ArrayList<String> = arrayListOf()
    private val treeTextList: ArrayList<String> = arrayListOf()
    private val buildImgList: ArrayList<Int> = arrayListOf()
    private val buildTitleList: ArrayList<String> = arrayListOf()
    private val buildTextList: ArrayList<String> = arrayListOf()
    private val commImgList: ArrayList<Int> = arrayListOf()
    private val commTitleList: ArrayList<String> = arrayListOf()
    private val commTextList: ArrayList<String> = arrayListOf()
    private val value: Int = value
    private val listener: ThingViewPagerInterface = thingListener
    private val jibun: String? = jibun

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        recyclerView = v.findViewById(R.id.recyclerViewThingDialog01)
        recyclerView2 = v.findViewById(R.id.recyclerViewThingDialog02)
        val builder = AlertDialog.Builder(mContext)

        builder.setView(v)

        val dlg = builder.create()

        //지장물 임시 소분류
        arrayList.add(R.string.thingDialogMobables)
        arrayList.add(R.string.thingDialogCommBuild)
        arrayList.add(R.string.thingDialogCollectiveBuild)
        arrayList.add(R.string.thingDialogWorkpiece)
        arrayList.add(R.string.thingDialogWdpt)
        arrayList.add(R.string.thingDialogClearing)
        arrayList.add(R.string.thingDialogPriceLoss)
        arrayList.add(R.string.thingDialogOwnership)
        arrayList.add(R.string.thingDialogEtc)

        //지장물 임시 수목
        treeImgList.add(R.drawable.thing_tree_01)
        treeImgList.add(R.drawable.thing_tree_02)
        treeImgList.add(R.drawable.thing_tree_03)
        treeImgList.add(R.drawable.thing_tree_04)
        treeImgList.add(R.drawable.thing_tree_05)

        treeTitleList.add("너도밤나무")
        treeTitleList.add("조팝나무")
        treeTitleList.add("아왜나무")
        treeTitleList.add("좀작살나무")
        treeTitleList.add("꽝꽝나무")

        treeTextList.add("밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무 밤나무")
        treeTextList.add("조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝 조팝")
        treeTextList.add("아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜 아왜")
        treeTextList.add("좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살 좀작살")
        treeTextList.add("꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝 꽝꽝")

        //지장물 임시 건물
        buildImgList.add(R.drawable.thing_bild_01)
        buildImgList.add(R.drawable.thing_bild_02)
        buildImgList.add(R.drawable.thing_bild_03)
        buildImgList.add(R.drawable.thing_bild_04)
        buildImgList.add(R.drawable.thing_bild_05)

        buildTitleList.add("단독주택")
        buildTitleList.add("다중주택")
        buildTitleList.add("다세대주택")
        buildTitleList.add("공장")
        buildTitleList.add("다가구주택")

        buildTextList.add("단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독 단독")
        buildTextList.add("다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중 다중")
        buildTextList.add("다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대 다세대")
        buildTextList.add("공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장 공장")
        buildTextList.add("다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구 다가구")

        //지장물 임시 일반반
        commImgList.add(R.drawable.thing_comm_01)
        commImgList.add(R.drawable.thing_comm_02)
        commImgList.add(R.drawable.thing_comm_03)
        commImgList.add(R.drawable.thing_comm_04)
        commImgList.add(R.drawable.thing_comm_05)

        commTitleList.add("개간비용")
        commTitleList.add("공작물2")
        commTitleList.add("공작물3")
        commTitleList.add("공작물4")
        commTitleList.add("공작물5")

        commTextList.add("개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간 개간")
        commTextList.add("공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1 공작물1")
        commTextList.add("공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2 공작물2")
        commTextList.add("공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3 공작물3")
        commTextList.add("공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4 공작물4")

        thingAdapter = ThingDialogRvAdapter1(arrayList, mContext)
        thingAdapter2 = ThingDialogRvAdapter2(mActivity, mContext, 0, commImgList, commTitleList, commTextList, this, value, listener, jibun!!)

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        thingAdapter.setOnItemClickListener(this)
        recyclerView.adapter = thingAdapter


        recyclerView2.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView2.adapter = thingAdapter2

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
            1 -> thingAdapter2 =
                ThingDialogRvAdapter2(mActivity, mContext, type, buildImgList, buildTitleList, buildTextList, this, value,listener, jibun!!)
            2 -> thingAdapter2 =
                ThingDialogRvAdapter2(mActivity, mContext, type, treeImgList, treeTitleList, treeTextList, this, value, listener, jibun!!)
            else -> thingAdapter2 =
                ThingDialogRvAdapter2(mActivity, mContext, type, commImgList, commTitleList, commTextList, this, value, listener, jibun!!)
        }
        recyclerView2.adapter = thingAdapter2
    }
}