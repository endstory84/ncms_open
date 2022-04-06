/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ThingMinrgtObject {

        var thingInfo: JSONObject? = null

        var thingNewSearch: String = "Y"

        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        //대분류
        var minrgtLclas: String = "영업"

        //소분류
        var minrgtSlas: String = "광업"

        var thingLrgeCl: String = ""
        var thingSmallCl: String = ""

        var thingKnd: String = ""

        var relateLnm: String = ""

        var bgnnAr: String = ""
        var incrprAr: String = ""

        var arComputBasis: String = ""

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


        //물건의 종류
        var minrgtThingKnd: String = ""

        var minrgtBgnnAr: String = ""
        var minrgtIncrprAr: String = ""

        var unitCl: String = ""

        var minrgtArComputBasis: String = ""

        var bsnCl: String = ""

        var sssMthCo: String = ""


        //등록번호
        var minrgtRegNo: String = ""

        //등록일자
        var minrgtRegDe: String = LocalDate.now().format(formatter)

        //존속기간 시작일
        var cntnncPdBgnde: String = LocalDate.now().format(formatter)

        //존속기간 끝일
        var cntnncPdEndde: String = LocalDate.now().format(formatter)

        //광업지적(임시)
        var minrgtLgstr: String = ""

        //광종
        var mnrlKnd: String =""

        //면적(ha)
        var minrgtAr: String =""

        //탐광계획 신고일자
        var prsptnPlanStemDe: String? = null

        //채광계획(변경) 인가일자
        var miningPlanCnfmDe: String? = null

        // 광물생산자보고자료여부
        var mnrlPrdnRprtAt: String? = null

        // 추가 물건 리스트
        var minrgtAddItemList: MutableList<MutableMap<String, String>>? = null

        // 스케치레이어 데이터
        var thingMinrgtSketchPolygon: MutableList<PolygonOverlay>? = null

        // 스케치레이어 면적데이터
        var thingMinrgtSketchArea: Int? = 0

        // 광업권 시설물
        var addMinrgtThing: JSONObject? = null

        var addOwnerListInfo: JSONArray? = null

        var thingOwnerInfoJson: JSONArray? = null

        var wtnncImage: MutableList<WtnncImage>? = null

        var pointYn: String? = "2"

        fun cleanThingMinrgtObject() {
                thingInfo = null
                thingNewSearch = "N"
                minrgtLclas = "영업"
                minrgtSlas = "광업"
                thingLrgeCl = ""
                thingSmallCl = ""
                thingKnd = ""
                bgnnAr = ""
                incrprAr = ""
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
                minrgtThingKnd = ""
                minrgtBgnnAr = ""
                minrgtIncrprAr = ""
                unitCl = ""
                minrgtArComputBasis = ""
                minrgtRegNo = ""
                minrgtRegDe = LocalDate.now().format(formatter)
                cntnncPdBgnde = LocalDate.now().format(formatter)
                cntnncPdEndde = LocalDate.now().format(formatter)
                minrgtLgstr = ""
                mnrlKnd =""
                minrgtAr =""
                prsptnPlanStemDe = null
                miningPlanCnfmDe = null
                mnrlPrdnRprtAt = null
                minrgtAddItemList = null
                thingMinrgtSketchPolygon = null
                thingMinrgtSketchArea = 0
                addMinrgtThing = null
                wtnncImage = null
                pointYn = "2"
        }


}
