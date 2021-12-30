/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

data class LadWtnnc(

    val lndcgr : ArrayList<String?> = ArrayList(), //현실적인 이용현황
    val area: ArrayList<String?> = ArrayList(), //면적
    val spfc: ArrayList<String?> = ArrayList(), //용도지역 및 지구

    val no : ArrayList<String?> = ArrayList(),
    val type : ArrayList<String?> = ArrayList(), //구분
    val name : ArrayList<String?> = ArrayList(), //성명(명칭)
    val tell : ArrayList<String?> = ArrayList(), //연락처
    val addr1 : ArrayList<String?> = ArrayList(), //공부상 주소
    val addr2 : ArrayList<String?> = ArrayList(), //초본상 주소
    val addr3 : ArrayList<String?> = ArrayList(), //송달 주소

)
