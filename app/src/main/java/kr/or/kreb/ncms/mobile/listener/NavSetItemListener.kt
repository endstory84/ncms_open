/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.app.Activity
import android.content.Context
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.Constants
import kr.or.kreb.ncms.mobile.util.PreferenceUtil
import kr.or.kreb.ncms.mobile.util.nextView


class NavSetItemListener(var activity:Activity, val context: Context, val coord: String?): NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        activity.finish()

        when(menuItem.itemId){
            R.id.drawer_lotmap -> {
                PreferenceUtil.setString(context, "bizSubCategory","용지도")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.LOTMAP)
                nextView(context, Constants.MAP_ACT, BizEnum.LOTMAP, null, coord, null)
            }
            R.id.drawer_lad -> {
                PreferenceUtil.setString(context, "bizSubCategory","토지")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.LAD)
                nextView( context, Constants.MAP_ACT, BizEnum.LAD, null, coord, null)
            }
            R.id.drawer_thing -> {
                PreferenceUtil.setString(context, "bizSubCategory","지장물")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.THING)
                nextView( context, Constants.MAP_ACT, BizEnum.THING, null, coord, null)
            }
            R.id.drawer_bsn -> {
                PreferenceUtil.setString(context, "bizSubCategory","영업ㆍ축산업ㆍ잠업")
                PreferenceUtil.setBiz(context,"bizSubCategoryKey", BizEnum.BSN)
                nextView( context, Constants.MAP_ACT, BizEnum.BSN, null, coord, null)
            }
            R.id.drawer_farm -> {
                PreferenceUtil.setString(context, "bizSubCategory","농업")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.FARM)
                nextView(context, Constants.MAP_ACT, BizEnum.FARM, null, coord, null)
            }
            R.id.drawer_residnt -> {
                PreferenceUtil.setString(context, "bizSubCategory","거주자")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.RESIDNT)
                nextView(context, Constants.MAP_ACT, BizEnum.RESIDNT, null, coord, null)
            }
            R.id.drawer_tomb -> {
                PreferenceUtil.setString(context, "bizSubCategory","분묘")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.TOMB)
                nextView(context, Constants.MAP_ACT, BizEnum.TOMB, null, coord, null)
            }
            R.id.drawer_fyhts -> {
                PreferenceUtil.setString(context, "bizSubCategory","어업권")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.FYHTS)
                nextView(context, Constants.MAP_ACT, BizEnum.FYHTS, null, coord, null)
            }
            R.id.drawer_minrgt -> {
                PreferenceUtil.setString(context, "bizSubCategory","광업권")
                PreferenceUtil.setBiz(context, "bizSubCategoryKey", BizEnum.MINRGT)
                nextView(context, Constants.MAP_ACT, BizEnum.MINRGT, null, coord, null)
            }
        }
        return true
    }
}