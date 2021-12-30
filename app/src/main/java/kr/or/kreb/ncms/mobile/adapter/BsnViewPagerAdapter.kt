/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.or.kreb.ncms.mobile.fragment.BsnOwnerFragment
import kr.or.kreb.ncms.mobile.fragment.BsnSearchFragment

class BsnViewPagerAdapter (
    private val activity: Activity,
    private val context: Context,
    private val fragmentActivity: FragmentActivity,
    private val view: View
) : FragmentStateAdapter(fragmentActivity) {

    private val search = 0
    private val owner = 1
    private var listPager: List<Int> = listOf(search, owner)

    override fun getItemCount(): Int {
        return listPager.size
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            search -> BsnSearchFragment(activity, context, fragmentActivity)
            else -> BsnOwnerFragment(fragmentActivity)
        }
    }
}