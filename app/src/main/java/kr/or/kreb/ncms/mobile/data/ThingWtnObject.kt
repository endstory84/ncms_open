/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.google.gson.JsonArray
import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject

object ThingWtnObject {

    // 물건조서 신규 여부
    var thingNewSearch: String? = ""

    // 물건조서정보
    var thingInfo: JSONObject? = null

    // 실내여부
    var thingIndoorTy: String = "2"

    // 물건 종류
    var thingKnd: String? = ""

    // 물건건 대분류
    var thingLrgeCl: String? = ""

    // 물건 소분류
    var thingSmallCl: String? = ""

    // 수목 조사 방식
    var thingWdptSearchType: String? = ""

    //구조 및 규격
    var strctNdStndrd: String? = ""

    // 구조 및 규격 수목 R
    var strctNdStrndrdR: String? = ""

    // 구조 및 규격 수목 H
    var strctNdStrndrdH: String? = ""

    // 원래면적(전체면적, 전체수량)
    var bgnnAr: String? = ""

    //편입면적
    var incrprAr: String? = ""

    //단위
    var unitCl: String? = ""

    //면적산출근거
    var arComputBasis: String? = ""

    //현황측량여부
    var sttusMesrAt: String? = ""

    //소유권이전여부
    var ownshipBeforeAt: String? = ""

    //보상대상여부
    var rwTrgetAt: String? = ""

    //감정평가대상여부
    var apasmtTrgetAt: String? = ""

    // 정상식 여부
    var nrmltpltAt: String? = "N"

    //수목사유
    var wdptResn: String? = ""

    //비고
    var rm: String? = ""

    //건물명
    var buldName: String? = ""

    // 건물동명
    var buldDongName: String? = ""

    // 건물 호명
    var buldHoName: String? = ""

    // 건물용도
    var buldPrpos: String? = ""

    // 건물층별
    var buldFlrato: String? = ""

    //건물구조
    var buldStrct: String? = ""

    var buldAr: String = ""

    //특이사항
    var paclrMatter: String? = ""

    //참고사항
    var referMatter: String? = ""

    //변경사유
    var changeResn: String? = ""
    var examinMthd: String? = ""

    // 허가근거
    var prmisnAt: String? = ""

    // 건축허가 분류
    var bildngPrmisnCl: String? = ""

    // 등기여부
    var rgistAt: String = ""

    // 주거용건물여부
    var redeBingAt: String = ""

    //스케치레이어데이터
    var thingSketchPolygon: MutableList<PolygonOverlay>? = null

    //대장상 면적 상이여부
    var regstrBuldArDfnAt: String? = "N"

    //대장상 건물명 상이여부
    var regstrBuldNmDfnAt: String? = "N"

    //대장상 용도 상이여부
    var regstrBuldPrposDfnAt: String? = "N"

    //대장상 구조 상이여부
    var regstrBuldStrctDfnAt: String? = "N"

    //대장상 동명 상이여부
    var regstrBuldDongDfnAt: String? = "N"

    //대장상 층 상이여부
    var regstrBuldFlratoDfnAt: String? = "N"

    //대장상 호명 상이여부
    var regstrBuldHoDfnAt: String? = "N"

    //등기상 면적 상이여부
    var rgistArDfnAt: String? = "N"

    //등기상 건물명 상이여부
    var rgistBuldNmDfnAt: String? = "N"

    //등기상 용도 상이여부
    var rgistBuldPrposDfnAt: String? = "N"

    //등기상 구조 상이여부
    var rgistBuldStrctDfnAt: String? = "N"

    //등기상 동명 상이여부
    var rgistBuldDongDfnAt: String? = "N"

    //등기상 층 상이여부
    var rgistBuldFlratoDfnAt: String? = "N"

    //등기상 호명 상이여부
    var rgistBuldHoDfnAt: String? = "N"

    var regstrDfnDtls: String? = ""

    var rgistDfnDtls: String? = ""

    // 포함분류
    var inclsCl: String? = ""

    // 취득분류
    var acqsCl: String? = ""

    var ownerCnfirmBasisCl: String? = ""

    //무허가건축물 여부
    var thingNrtBuldAt: String = "N"

    // 지장물 전체 Data 저장 (필터링해서 사용할 예정)
    var thingWtnncJsonArray: JsonArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var addOwnerListInfo: JSONArray? = null

    var naverGeoAddressName: String? = null

    var naverGeoAddress: String? = null

    var naverLegaldongCode: String? = null

    var thingWtnncSaveFlag = false

    var thingWtnncSaveCnfirmFlag = false

    var wtnncImage: MutableList<WtnncImage>? = null


    fun cleanThingWtnObject() {
        thingNewSearch = ""
        thingInfo = null
        thingKnd = ""
        thingSmallCl = ""
        thingWdptSearchType = ""
        strctNdStndrd = ""
        strctNdStrndrdR = ""
        strctNdStrndrdH = ""
        bgnnAr = ""
        incrprAr = ""
        unitCl = null
        arComputBasis = ""
        nrmltpltAt = "N"
        wdptResn = ""
        rm = ""
        buldName = ""
        buldDongName = ""
        buldHoName = ""
        buldPrpos = ""
        buldFlrato = ""
        buldStrct = ""
        paclrMatter = ""
        referMatter = ""
        changeResn = ""
        thingSketchPolygon = null
        examinMthd = ""
        thingOwnerInfoJson = null
        ownerCnfirmBasisCl = ""
        acqsCl = ""
        inclsCl = ""
        wtnncImage = null
    }
}