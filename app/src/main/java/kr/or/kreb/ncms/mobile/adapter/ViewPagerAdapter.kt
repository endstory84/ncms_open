/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.*

class ViewPagerAdapter(
    val activity: Activity,
    val context: Context,
    val fragmentActivity: FragmentActivity,
    val bizType: BizEnum) :
    FragmentStateAdapter(fragmentActivity) {

    private val TYPE_LAD_INFO       = 0
    private val TYPE_LAD_SEARCH     = 1
    private val TYPE_LAD_OWNER      = 2

    private val TYPE_THING_SEARCH   = 0
    private val TYPE_THING_OWNER    = 1

    private var listPager: List<Int> = when(bizType) {
        BizEnum.LAD, BizEnum.REST_LAD -> listOf(TYPE_LAD_INFO, TYPE_LAD_SEARCH, TYPE_LAD_OWNER)
        else -> listOf(TYPE_THING_SEARCH, TYPE_THING_OWNER)
    }

    override fun getItemCount(): Int {
        return listPager.size
    }

    override fun createFragment(position: Int): Fragment {

        val fragment = when(bizType) {
            BizEnum.LAD, BizEnum.REST_LAD -> {
                when (position) {
                    TYPE_LAD_INFO -> LandInfoFragment(context)
                    TYPE_LAD_SEARCH -> getSearchFragment()
                    else -> getOwnerFragment()
                }
            }
            else -> {
                when (position) {
                    TYPE_THING_SEARCH -> getSearchFragment()
                    else -> getOwnerFragment()
                }
            }
        }

        return fragment

    }

    fun getSearchFragment() : Fragment {

        return when (bizType) {
            BizEnum.LAD -> LandSearchFragment(activity, context)
            BizEnum.THING -> ThingSearchFragment(activity, context, fragmentActivity)
            BizEnum.BSN -> BsnSearchFragment(activity, context, fragmentActivity)
            BizEnum.FARM -> FarmSearchFragment(activity, context, fragmentActivity)
            BizEnum.RESIDNT -> ResidntSearchFragment(activity, context, fragmentActivity)
            BizEnum.TOMB -> TombSearchFragment(activity, context)
            BizEnum.MINRGT -> MinrgtSearchFragment(activity, context)
            BizEnum.FYHTS -> FyhtsSearchFragment(activity, context)
            BizEnum.REST_LAD -> RestLandSearchFragment(activity, context)
            BizEnum.REST_THING -> RestThingSearchFragment(activity, context, fragmentActivity)
            else -> LandOwnerFragment(fragmentActivity)
        }

    }

    fun getOwnerFragment() : Fragment {

        return when (bizType) {
            BizEnum.LAD -> LandOwnerFragment(fragmentActivity)
            BizEnum.THING -> ThingOwnerFragment(fragmentActivity)
            BizEnum.BSN -> BsnOwnerFragment(fragmentActivity)
            BizEnum.FARM -> FarmOwnerFragment(fragmentActivity)
            BizEnum.RESIDNT -> ResidntOwnerFragment(fragmentActivity)
            BizEnum.TOMB -> TombOwnerFragment(fragmentActivity)
            BizEnum.MINRGT -> MinrgtOwnerFragment(fragmentActivity)
            BizEnum.FYHTS -> FyhtsOwnerFragment(fragmentActivity)
            BizEnum.REST_LAD -> RestLandOwnerFragment(fragmentActivity)
            BizEnum.REST_THING -> RestThingOwnerFragment(fragmentActivity)
            else -> LandOwnerFragment(fragmentActivity)
        }

    }

}