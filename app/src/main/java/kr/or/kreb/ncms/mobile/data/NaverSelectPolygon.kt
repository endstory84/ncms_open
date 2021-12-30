/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.map.overlay.PolygonOverlay

data class NaverSelectPolygon(
    var seq: String,
    var isChecked: Boolean,
    var polygon: PolygonOverlay
)