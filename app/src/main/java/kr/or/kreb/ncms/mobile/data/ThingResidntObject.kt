/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject

object ThingResidntObject {

    var thingInfo: JSONObject? = null

    var thingNewSearch:String? = "Y"

    var thingLrgeCl:String? = ""
    var thingSmallCl:String? = ""

    var thingKnd:String? = ""

    var strctNdStrndrd:String? = ""

    var bgnnAr:String? = ""
    var incrprAr:String? = ""

    var unitCl:String? = ""

    var arComputBasis:String? = ""

    var strctNdStndrd:String? = ""
    var inclsCl:String? = ""

    var acqsCl:String? = ""

    var ownerCnfirmBasisCl:String? = ""

    var ownshipBeforeAt:String? = ""

    var apasmtTrgetAt:String? = ""

    var rwTrgetAt:String? = ""

    var paclrMatter:String? = ""

    var rm:String? = ""

    var changeResn:String? = ""

    var referMatter:String? = ""

    var araLgalAt:String? = "" // (장소)적법여부

    var pssRentBgnde:String? = "" // 임차기간 시작일자

    var pssRentEndde:String? = "" // 임차기간 종료일자

    var reincrprNtfcDe:String? = "" // 사업인정고시일

    var reincrprRwDe:String? = "" // 보상일자


    var pssLgalAt:String? = "" // (점유)적법여부

    var pssPssCl:String? = "" // 정유분류

    var pssHireCntrctAt:String? = "" // 임대차계약서 여부

    var pssRentName:String? = "" // 임차인 성명

    var pssHireName:String? = "" // 임대인 성명

    var pssGtn:String? = "" // 보증금

    var pssMtht:String? = "" // 월세

    var pssCntrctLc:String? = "" // 계약(소유) 위치

    var pssCntrctAr:String? = "" // 계약(소유) 면적

    var pssSpccntr:String? = "" // 특약

    var reincrprProperAt:String? = "" // (재편입근거)적격여부

    var reincrprBgnnBsnsNm:String? = "" // 당초사업명

    var reincrprBsAt:String? = "" // 재편입근거 여부

    var residePdBgnde:String? = "" // 거주기간 시작일자

    var residePdEndde:String? = "" // 거주기간  종료일자

    var reincrprAddibs: String? = ""

    var residePdBgndeList: MutableList<String> = mutableListOf() // 추가 거주기간 시작일자

    var residePdEnddeList: MutableList<String> = mutableListOf() // 추가 거주기간 종료일자

    var residntAddDtlsList: MutableList<MutableMap<String, String>> = mutableListOf() // 경작, 실제소득 통합 리스트

    var residntAddThingList: MutableList<MutableMap<String, String>> = mutableListOf() // 추가 시설물 통합 리스트

    //스케치레이어데이터
    var thingResidntSketchPolygon: MutableList<PolygonOverlay>? = null
    var selectBuldLinkData: ArrayList<BuldSelectListInfo>? = null

    var addResidntDtlsList: JSONObject? = null
    var addBuldLinkList: JSONObject? = null

    var addOwnerListInfo: JSONArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var wtnncImage: MutableList<WtnncImage>? = null

    var pointYn: String? = "2"
    
    fun cleanThingResidntObject() {
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
        araLgalAt = "" // (장소)적법여부
        pssRentBgnde = "" // 임차기간 시작일자
        pssRentEndde = "" // 임차기간 종료일자
        reincrprNtfcDe = "" // 사업인정고시일
        reincrprRwDe = "" // 보상일자
        pssLgalAt = "" // (점유)적법여부
        pssPssCl = "" // 정유분류
        pssHireCntrctAt = "" // 임대차계약서 여부
        pssRentName = "" // 임차인 성명
        pssHireName = "" // 임대인 성명
        pssGtn = "" // 보증금
        pssMtht = "" // 월세
        pssCntrctLc = "" // 계약(소유) 위치
        pssCntrctAr = "" // 계약(소유) 면적
        pssSpccntr = "" // 특약
        reincrprProperAt = "" // (재편입근거)적격여부
        reincrprBgnnBsnsNm = "" // 당초사업명
        reincrprBsAt = "" // 재편입근거 여부
        residePdBgnde = "" // 거주기간 시작일자
        residePdEndde = "" // 거주기간  종료일자
        addResidntDtlsList = null
        addBuldLinkList = null
        wtnncImage = null
        pointYn = "2"
    }

}
