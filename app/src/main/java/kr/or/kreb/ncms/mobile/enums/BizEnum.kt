/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.enums


/**
 * 사업
 */

enum class BizEnum(val value:Int) {
    LOTMAP(1),  //용지도
    LAD(2),     //토지
    THING(3),   //지장물
    BSN(4),     //영업ㆍ축산업ㆍ잠업
    FARM(5),    //농업
    RESIDNT(6), //거주자
    TOMB(7),    //분묘
    MINRGT(8),  //광업권
//    MNIDST(8),  //광?업권
//    FSHR(9)     //어업권
    FYHTS(9),     //어업권
    REST_LAD(98),        // 잔여지
    REST_THING(99)       // 잔여 지장물
}