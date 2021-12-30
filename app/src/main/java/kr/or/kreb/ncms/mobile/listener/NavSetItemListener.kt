/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.app.Activity
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import kr.or.kreb.ncms.mobile.R


class NavSetItemListener(var activity:Activity): NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.drawer_lotmap -> println("용지도")
            R.id.drawer_lad -> println("토지")
            R.id.drawer_thing -> println("지장물")
            R.id.drawer_bsn -> println("영업ㆍ축산업ㆍ잠업")
            R.id.drawer_farm -> println("농업")
            R.id.drawer_residnt -> println("거주자")
            R.id.drawer_tomb -> println("분묘")
            R.id.drawer_fshr -> println("어업권")
            R.id.drawer_mndist -> println("광업권")
        }
        return true
    }
}