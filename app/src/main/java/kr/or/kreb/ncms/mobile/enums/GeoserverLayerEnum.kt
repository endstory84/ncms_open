/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums

enum class GeoserverLayerEnum(val value:String) {
    SIDO("ncms:TL_SCCO_CTPRVN"), // 시도
    SIGUNGU("ncms:TL_SCCO_SIG"), // 시군구
    EMD("ncms:TL_SCCO_EMD"), // 읍면동
    LI("ncms:TL_SCCO_LI"), // 리경계
    CADASTRAL("ncms:TL_CADASTRAL"), // 연속지적도
    TL_BSNS_AREA("ncms:TL_BSNS_AREA"), // 사업구역 용지도
    CADASTRAL_EDIT("ncms:TL_CADASTRAL_EDIT"), // 연속지적도 편집내역
    TB_LAD_REALNGR("ncms:TB_LAD_REALNGR"), // 토지실제이용
    TB_THING_WTN("ncms:TB_THING_WTN"), // 물건조서
    TB_LAD_WTN("ncms:TB_LAD_WTN") // 토지조서

}