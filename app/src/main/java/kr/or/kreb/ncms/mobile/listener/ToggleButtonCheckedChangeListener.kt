/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.app.Activity
import android.widget.CompoundButton
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_map.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.util.NaverMapUtil

class ToggleButtonCheckedChangeListener(var activity: Activity, private var naverMap: NaverMapUtil) : CompoundButton.OnCheckedChangeListener {
    override fun onCheckedChanged(button: CompoundButton?, isChecked: Boolean) {
        when (button?.id) {
            R.id.toggleButtonBaseMap -> { // 기본
                if (activity.toggleButtonBaseMap.isClickable) {
                    activity.toggleButtonHybrid.background = activity.getDrawable(R.drawable.img_toggle_hybrid)
                    activity.toggleButtonBaseMap.background = activity.getDrawable(R.drawable.img_toggle_basemap_on)
                }
                naverMap.setNaverMapType("basic")
            }
            R.id.toggleButtonHybrid -> { // 위성(하이브리드)
                if (activity.toggleButtonBaseMap.isClickable) {
                    activity.toggleButtonBaseMap.background = activity.getDrawable(R.drawable.img_toggle_basemap)
                    activity.toggleButtonHybrid.background = activity.getDrawable(R.drawable.img_toggle_hybrid_on)
                }
                naverMap.setNaverMapType("hybrid")
            }
            R.id.toggleButtonCadstral -> { // 부동산원 연속지적도
                if (isChecked) {
                    activity.toggleButtonCadstral.background = activity.getDrawable(R.drawable.img_toggle_cadastral_on)
                    naverMap.setNaverMapType("cadastralOn")
                } else {
                    naverMap.setNaverMapType("cadastralOff")
                    naverMap.clearWFS(naverMap.wfsCadastralOverlayArr, "연속지적도")
                    activity.toggleButtonCadstral.background = activity.getDrawable(R.drawable.img_toggle_cadastral)
                }
            }
            R.id.toggleButtonLayer -> { // 레이어
                if(!activity.layoutMapSlide.isDrawerOpen(GravityCompat.START)){
                    activity.layoutMapSlide.openDrawer(GravityCompat.START)
                } else {
                    activity.layoutMapSlide.closeDrawer(GravityCompat.START)
                }
            }
        }
    }
}