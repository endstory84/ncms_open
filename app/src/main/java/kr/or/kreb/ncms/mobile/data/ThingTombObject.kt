/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject

object ThingTombObject {

    var thingInfo: JSONObject? = null

    var thingNewSearch: String = "N"

    var tombBurldDtlsArray: JSONArray? = null

    // 물건대분류
    var thingLrgeCl: String? = ""

    // 물건소분류
    var thingSmallCl: String? = ""

    // 구조 및 규격
    var strctNdStndrd: String? = ""

    // 법정동코드
    var legaldongCode: String? = ""

    // 물건종류
    var thingKnd: String? = ""

    // 원래면적
    var bgnnAr: String? = ""

    // 편입면적
    var incrprAr: String? = ""

    //단위분류
    var unitCl: String? = ""

    // 면적산출근거
    var arComputBasis: String = ""

    // 취득분류
    var acqsCl: String = ""

    // 포함분류:
    var inclsCl: String = ""

    // 소유자확인근거분류
    var ownerCnfirmBasisCl: String = ""

    // 비고
    var rm: String = ""

    // 변경사유
    var changeResn: String = ""

    // 참고사항
    var referMatter: String = ""

    // 특이사항
    var paclrMatter: String = ""

    // 보상대상여부
    var rwTrgetAt: String = ""

    // 감정평가대상여부
    var apasmtTrgetAt: String = ""


    // 분묘조서

    // 분묘 번호
    var tombNo: String? = ""

    // 분묘 연고자 유무
    var balmCl: String? = ""

    // 분묘 연고자 한글
    var balmClText: String? = ""

    // 분묘 유형
    var tombCl: String? = ""

    // 분묘 유형 한글
    var tombClText: String? = ""

    // 분묘 타입
    var tombTy: String? = ""

    //  매장일자
    var burlDe: String? = ""

    // 분묘조서 코드
    var tombWtnCode: Int? = 0






    // 스케치레이어 데이터
    var thingTombSketchPolyton: MutableList<PolygonOverlay>? = null

    // 스케치레이어 면적데이터
    var thingTombSketchArea: Int? = 0


    //
    var tombSe: String? = ""
    
    // 매장자
    var addBuriedPerson: JSONObject? = null
    
    // 분묘시설물
    var addBuriedThing: JSONObject? = null


    //시설물 배열
    var addSclasArray: MutableList<String>? = mutableListOf()
    var addThingKndArray: MutableList<String>? = mutableListOf()
    var  addStrctStndrdArray: MutableList<String>? = mutableListOf()
    var addIncrprQyArray: MutableList<String>? = mutableListOf()
    var addUnitArray: MutableList<String>? = mutableListOf()

    // 매장자 정보
    var tombAddItemList: MutableList<MutableMap<String, String>>? = null

    var addOwnerListInfo: JSONArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var wtnncImage: MutableList<WtnncImage>? = null

    var pointYn: String? = "2"

    fun cleanThingTombObject() {
         thingInfo = null
         thingNewSearch = "N"
         thingLrgeCl = ""
         thingSmallCl = ""
         strctNdStndrd = ""
         legaldongCode = ""
         thingKnd = ""
         bgnnAr = ""
         incrprAr = ""
         unitCl = ""
         arComputBasis = ""
         acqsCl = ""
         inclsCl = ""
         ownerCnfirmBasisCl = ""
         rm = ""
         changeResn = ""
         referMatter = ""
         paclrMatter = ""
         rwTrgetAt = ""
         apasmtTrgetAt = ""
         balmCl = ""
         balmClText = ""
         tombCl = ""
         tombClText = ""
         tombTy = ""
         burlDe = ""
         tombWtnCode = 0
         thingTombSketchPolyton= null
         thingTombSketchArea = 0
         tombSe = ""
         addSclasArray = null
         addThingKndArray = null
         addStrctStndrdArray = null
         addIncrprQyArray = null
         addUnitArray = null
         tombAddItemList = null
         addBuriedPerson = null
         addBuriedThing = null
        wtnncImage = null
        pointYn = "2"




    }
}