/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums

enum class CoordinatesEnum(val value:String) {
    WGS84("EPSG:4326"), // 위경도
    MERCATOR("EPSG:3857"), // Google Mercator(구글, osm, 브이월드)
}