/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.carto.core.MapPos

/**
 * 토지 이용현황
 */

data class LandRealLngr(
    var seq: Int?, // 토지실제이용 index
    var mapPosArr: MutableList<ArrayList<MapPos>>? // 토지실제이용 내 폴리곤의 Array
)
