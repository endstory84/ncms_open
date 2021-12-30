/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject

object ThingBsnObject {

    var thingInfo: JSONObject? = null

    var thingNewSearch: String = "Y"

    var thingLrgeCl: String = ""
    var thingSmallCl: String = ""

    var thingKnd: String = ""

    var strctNdStrndrd: String = ""

    var bgnnAr: String = ""
    var incrprAr: String = ""

    var unitCl: String = ""

    var arComputBasis: String = ""

    var strctNdStndrd: String = ""

    var bsnCl: String = ""

    var sssMthCo: String = ""

    var inclsCl: String = ""

    var acqsCl: String = ""

    var ownerCnfirmBasisCl: String = ""

    var ownshipBeforeAt: String = ""

    var apasmtTrgetAt: String = ""

    var rwTrgetAt: String = ""

    var paclrMatter: String = ""

    var rm: String = ""

    var changeResn: String = ""

    var referMatter: String = ""


    // 장소의 적법성 근거
    var araLgalAt: String? = ""


    // 점유의 적법성 근거
    // 적법여부
    var pssLgalAt: String? = ""

    // 점유구분
    var pssPssTy: String? = ""

    // 임대차계약서 유무
    var pssHireCntrctAt: String? = ""

    // 임차인 성명
    var pssRentName: String? = ""

    // 임대인 성명
    var pssHireName: String? = ""

    // 임차기간시작
    var pssRentBgnde: String? = ""

    // 임차기간종료
    var pssRentEndde: String? = ""

    // 보증금
    var pssGtn: String? = ""

    // 월세
    var pssMtht: String? = ""

    // 계약(소유)위치
    var pssCntrctLc: String? = ""

    // 계약(소유)면적
    var pssCntrctAr: String? = ""

    // 특약
    var pssSpccntr: String? = ""


    // 영업의 적법성 근거
    // 적법여부
    var bsnProperAt: String? = ""

    // 영업중 여부
    var bsnSgnProsAt: String? = ""

    // 허가등 구분
    var bsnPrmisnCl: String? = ""

    // 허가등 받은자
    var bsnPrmsTrgetNm: String? = ""

    // 허가등 번호
    var bsnPrmisnNo: String? = ""

    // 허가등 일자
    var bsnPrmisnDe: String? = ""

    // 허가등기간 시작일자
    var bsnPrmisnBgnde: String? = ""

    // 허가등기간 종료일자
    var bsnPrmisnEndde: String? = ""

    // 허가등 기관
    var bsnPrmisnInstt: String? = ""

    // 영업기간 시작일자
    var bsnBsnpdBgnde: String? = ""

    // 영업기간 종료일자
    var bsnBsnpdEndde: String? = ""

    // 영업장 위치
    var bsnBsnplcLc: String? = ""

    // 영업장면적
    var bsnBsnplcAr: String? = ""


    // 인적, 물적 시설 여부
    // 적법여부
    var hmftyProperAt: String? = ""

    // 간판명
    var hmftySgnbrdNm: String? = ""

    // 상주인력
    var hmftyResdngHnf: String? = ""

    // 시설물
    var hmftyFtyAt: String? = ""

    // 동일세대원 다른 영업조상 수령여부
    var hmftySgnSecdAt: String? = ""


    // 사업자등록 내역
    // 현황부합여부
    var bizrdtlsSttAt: String? = ""

    // 영리구분
    var bizrdtlsPrftmkTy: String? = ""

    // 등록구분
    var bizrdtlsRegTy: String? = ""

    //대표자
    var bizrdtlsRprsntvNm: String? = ""

    // 상호
    var bizrdtlsMtlty: String? = ""

    // 업종
    var bizrdtlsInduty: String? = ""

    // 업태
    var bizrdtlsBizcnd: String? = ""

    // 등록번호
    var bizrdtlsBizrno: String? = ""

    // 등록 일자
    var bizrdtlsBizDe: String? = ""

    // 고시 1년전 등록여부
    var bizrdtlsRegAt: String? = ""


    // 사육 내역
    // 적격여부
    var brdProperAt: String? = ""

    // 사육기간 시작일자
    var bsnBrdpdBgnde: String? = ""

    // 사육기간 종료일자
    var bsnBrdpdEndde: String? = ""


    // 추가 사육기간 시작일자
    var addBsnBrdPdBgnde: MutableList<String> = mutableListOf()

    // 추가 사육기간 종료일자
    var addBsnBrdPdEndde: MutableList<String> = mutableListOf()

    // 사육내역 리스트
    var bsnAddLvstckList: MutableList<MutableMap<String, String>>? = null

    // 시설물 추가 리스트
    var bsnAddItemList: MutableList<MutableMap<String, String>>? = null

    //스케치레이어데이터
    var thingBsnSketchPolygon: MutableList<PolygonOverlay>? = null

    // 영업 축산업 사육내역
    var addBsnBrdpdList: JSONObject? = null

    // 영업 시설물 추가
    var addBsnThingList: JSONObject? = null

    //장소의 적법성
    var addBuldLinkList: JSONObject? = null

    var selectBuldLinkData: ArrayList<BuldSelectListInfo>? = null

    var addOwnerListInfo: JSONArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var wtnncImage: MutableList<WtnncImage>? = null

    fun cleanThingBsnObject() {
        thingInfo = null
        thingNewSearch = "Y"
        thingLrgeCl = ""
        thingSmallCl = ""
        thingKnd = ""
        strctNdStrndrd = ""
        bgnnAr = ""
        incrprAr = ""
        unitCl = ""
        arComputBasis = ""
        strctNdStndrd = ""
        inclsCl = ""
        acqsCl = ""
        ownerCnfirmBasisCl = ""
        ownshipBeforeAt = ""
        apasmtTrgetAt = ""
        rwTrgetAt = ""
        paclrMatter = ""
        rm = ""
        changeResn = ""
        referMatter = ""
        bsnCl = ""
        sssMthCo = ""
        araLgalAt = ""
        pssLgalAt = ""
        pssPssTy = ""
        pssHireCntrctAt = ""
        pssRentName = ""
        pssHireName = ""
        pssRentBgnde = ""
        pssRentEndde = ""
        pssGtn = ""
        pssMtht = ""
        pssCntrctLc = ""
        pssCntrctAr = ""
        pssSpccntr = ""
        bsnProperAt = ""
        bsnSgnProsAt = ""
        bsnPrmisnCl = ""
        bsnPrmsTrgetNm = ""
        bsnPrmisnNo = ""
        bsnPrmisnDe = ""
        bsnPrmisnBgnde = ""
        bsnPrmisnEndde = ""
        bsnPrmisnInstt = ""
        bsnBsnpdBgnde = ""
        bsnBsnpdEndde = ""
        bsnBsnplcLc = ""
        bsnBsnplcAr = ""
        hmftyProperAt = ""
        hmftySgnbrdNm = ""
        hmftyResdngHnf = ""
        hmftyFtyAt = ""
        hmftySgnSecdAt = ""
        bizrdtlsSttAt = ""
        bizrdtlsPrftmkTy = ""
        bizrdtlsRegTy = ""
        bizrdtlsRprsntvNm = ""
        bizrdtlsMtlty = ""
        bizrdtlsInduty = ""
        bizrdtlsBizcnd = ""
        bizrdtlsBizrno = ""
        bizrdtlsBizDe = ""
        bizrdtlsRegAt = ""
        brdProperAt = ""
        bsnBrdpdBgnde = ""
        bsnBrdpdEndde = ""
        bsnAddLvstckList= null
        bsnAddItemList = null
        thingBsnSketchPolygon = null
        addBsnBrdpdList= null
        addBsnThingList = null
        selectBuldLinkData = null
        addBuldLinkList = null
        wtnncImage = null
    }
}