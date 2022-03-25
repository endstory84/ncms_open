/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter.viewpager

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.or.kreb.ncms.mobile.fragment.LandInfoFragment
import kr.or.kreb.ncms.mobile.fragment.LandOwnerFragment
import kr.or.kreb.ncms.mobile.fragment.LandSearchFragment

class ViewPagerFragmentAdapter(
    val activity: Activity,
    val context: Context,
    val fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val TYPE_A = 0
    private val TYPE_B = 1
    private val TYPE_C = 2
    private var listPager: List<Int> = listOf(TYPE_A, TYPE_B, TYPE_C)


    /**
     * Http Request Query (key, value) 세팅
     * @param type Query Type
     * @param layerName Geoserver 통신할시에 필요함
     */
//    private fun setRequestQuery(type:String, layerName: String?):MutableMap<String, String>{
//
//        val queryArr = mutableMapOf<String, String>()
//
//        when(type){
//           "LAND" -> {
//                queryArr["type"] ="0"
//                queryArr["saupCode"] ="보상0001-0003-1"
//                queryArr["mnnm"] ="714"
//                queryArr["slno"] ="1"
//            }
//        }
//
//        return queryArr
//    }


    override fun getItemCount(): Int {
        return listPager.size
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            TYPE_A -> LandInfoFragment(context)
            //TYPE_B -> LandSearchFragment(activity, context, clickListener)
            TYPE_B -> LandSearchFragment(activity, context)
            else -> LandOwnerFragment(fragmentActivity)
        }
    }

}