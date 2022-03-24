/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.google.gson.JsonElement
import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ThingFyhtsObject {

    var thingInfo: JSONObject? = null

    var thingNewSearch: String = "Y"

    var legaldongCl:String = ""
    var legaldongNm:String = ""

    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    //대분류
    var minrgtLclas: String = "영업"

    //소분류
    var minrgtSlas: String = "광업"

    var thingLrgeCl: String = ""
    var thingSmallCl: String = ""

    var thingKnd: String = ""

    var bgnnAr: String = ""
    var incrprAr: String = ""

    var arComputBasis: String = ""

    var bsnCl: String = ""
    var sssMthCo: String = ""

    var strctNdStndrd: String = ""
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

    var unitCl: String = ""



    var administGrc: String ="" //행정관청

    var lcnsCl: String = ""

    var lcnsKnd: String ="" //면허종류

    var lcnsNo: String ="" //면허번호

    var lcnsDe: String = LocalDate.now().format(formatter) //면허일자

    var fyhtsCntnncPdBgnde: String = LocalDate.now().format(formatter) //존속기간 시작

    var fyhtsCntnncPdEndde: String = LocalDate.now().format(formatter) //존속기간 끝

    var fshlLc: String? = null //어장의 위치

    var fyhtsAr: String? = null //면적

    var fshrMth: String? = null //어업의 방법

    var srfwtrLcZoneAt: String? = null //수면의 위치 및 구역도 여부

    // 추가 물건 리스트
    var fyhtsAddItemList: MutableList<MutableMap<String, String>>? = null

    // 스케치레이어 데이터
    var thingFyhtsSketchPolygon: MutableList<PolygonOverlay>? = null

    // 스케치레이어 면적데이터
    var thingFyhtsSketchArea: Int? = 0

    var addFyhtsThing: JSONObject? = null // 어업권 시설물

    var addOwnerListInfo: JSONArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var getSameWtnCodeJsonArray: List<JsonElement>? = null // 일치하는 wtncCode의 지오메트리 정보 임시저장

    var wtnncImage: MutableList<WtnncImage>? = null

    var pointYn: String? = "2"

    fun clealThingFyhtsObject() {
        thingInfo = null
        thingNewSearch = "Y"
        legaldongCl = ""
        legaldongNm = ""
        minrgtLclas = "영업"
        minrgtSlas = "광업"
        thingLrgeCl = ""
        thingSmallCl = ""
        thingKnd = ""
        bgnnAr = ""
        incrprAr = ""
        bsnCl = ""
        sssMthCo = ""
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
        unitCl = ""
        administGrc ="" //행정관청
        lcnsCl = ""
        lcnsKnd ="" //면허종류
        lcnsNo ="" //면허번호
        lcnsDe = LocalDate.now().format(formatter) //면허일자
        fyhtsCntnncPdBgnde = LocalDate.now().format(formatter) //존속기간 시작
        fyhtsCntnncPdEndde = LocalDate.now().format(formatter) //존속기간 끝
        fshlLc  = null //어장의 위치
        fyhtsAr  = null //면적
        fshrMth  = null //어업의 방법
        srfwtrLcZoneAt  = null //수면의 위치 및 구역도 여부
        thingFyhtsSketchArea= 0
        addFyhtsThing= null // 어업권 시설물
        wtnncImage = null
        pointYn = "2"
    }

}