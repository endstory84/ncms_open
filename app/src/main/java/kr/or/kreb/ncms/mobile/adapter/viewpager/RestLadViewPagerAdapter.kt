/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter.viewpager

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.or.kreb.ncms.mobile.fragment.*

class RestLadViewPagerAdapter(
    val activity: Activity,
    val context: Context,
    val fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val info = 0
    private val search = 1
    private val owner = 2
    private var listPager: List<Int> = listOf(info, search, owner)

    override fun getItemCount(): Int {
        return listPager.size
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            info -> LandInfoFragment(context)
            search -> RestLandSearchFragment(activity, context)
            else -> RestLandOwnerFragment(fragmentActivity)
        }
    }
}