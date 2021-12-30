/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.annotation.SuppressLint
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.fragment.DelvyAddrChangeFragment

object Constants {

    /** Activity 액션 **/
    var PAGE_ACT: Int = 0

    const val LOGIN_ACT: Int = 1 // 로그인
    const val BIZ_LIST_ACT: Int = 2 // 사업 전체
    const val BIZ_CNFIRM_ACT: Int = 3 // 사업 확인
    const val MAP_ACT: Int = 4 // 사업 분류별 Map
    const val CAMERA_ACT: Int = 5 // 카메라

    /** 사업 내 공통 preferences Value */
    var BIZ_CATEGORY: String = ""
    var BIZ_SUBCATEGORY: String = ""
    var BIZ_NAME: String = ""
    lateinit var BIZ_SUBCATEGORY_KEY: BizEnum
    
    @SuppressLint("StaticFieldLeak")
    var CAMERA_ADAPTER: WtnncImageAdapter? = null
    var CAMERA_IMGAE_INDEX: Int = 0
    var CAMERA_IMAGE_ARR = mutableListOf<WtnncImage>()

    var GLOBAL_DELVY_FRAGMENT: DelvyAddrChangeFragment? = null
    var GLOBAL_TAB_LAYOUT: TabLayout? = null
    var GLOBAL_VIEW_PAGER: ViewPager2? = null

    /**
     *  Google Mercator: 구글지도/빙지도/야후지도/OSM 등 에서 사용중인 좌표계
     */
    val PROJ4_3857 = arrayOf("+proj=merc", "+a=6378137", "+b=6378137", "+lat_ts=0.0", "+lon_0=0.0", "+x_0=0.0", "+y_0=0", "+k=1.0", "+units=m", "+nadgrids=@null", "+Fwktext", "+no_defs")
}