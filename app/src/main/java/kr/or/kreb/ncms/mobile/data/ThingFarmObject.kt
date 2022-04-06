/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.PolygonOverlay
import org.json.JSONArray
import org.json.JSONObject

object ThingFarmObject {
    var thingInfo: JSONObject? = null

    var thingNewSearch: String = "Y"

    var thingLrgeCl: String = ""
    var thingSmallCl: String = ""

    var thingKnd: String = ""

    var strctNdStrndrd: String = ""

    var relateLnm: String = ""

    var bgnnAr: String = ""
    var incrprAr: String = ""

    var unitCl: String = ""

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

    var frldbsLgalAt: String? = "" // (농지근거)적법여부

    var frldbsBasisCl: String? = "" // (농지근거)농지근거분류

    var frldbsDbtamtRepAt: String? = "" // (농지근거)직불금 수령여부

    var frldbsDbtamtYear: String? = "" // (농지근거)직불금 수령년도

    var frldbsDbtamtRepName: String? = "" // (농지근거)직불금 수령자

    var frldbsFrldLdgrAt: String? = "" // (농지근거)농지원부 여부

    var frldbsLadFarmerAt: String? = "" // (농지근거)토지주 해당지역 거주 농민여부

    var frmrbsLgalAt: String? = "" // (농민근거)적법여부

    var frmrbsBasisCl: String? = "" // (농민근거)농민근거분류

    var frmrbsCnfrmnDta1At: String? = "" // (농민근거)농업인확인서 여부

    var frmrbsCnfrmnDta2At: String? = "" // (농민근거)농어업경영체등록 확인서 여부

    var frmrbsDbtamtAt: String? = "" // (농민근거)직불금 수령_경작 일치여부

    var frmrbsAraResideAt: String? = "" // (농민근거)해당지역 거주여부

    var posesnLgalAt: String? = "" // (점유근거)적법여부

    var posesnClvthmTy: String? = "" // (점유근거)경작자타입

    var posesnOwnerCnfrmAt: String? = "" // (점유근거)농지소유자 확인(경작시설확인서) 여부

    var posesnCbCnfrmAt: String? = "" // (점유근거)이장, 통장 확인(경작시설확인서) 여부

    var posesnLrcdocAt: String? = "" // (점유근거)임대차계약서 여부

    var posesnRentName: String? = "" // (점유근거)임차인 성명

    var posesnHireName: String? = "" // (점유근거)임대인 성명

    var posesnRentBgnde: String? = "" // (점유근거)임차기간 시작일자

    var posesnRentEndde: String? = "" // (점유근거)임차기간 종료일자

    var posesnGtn: String? = "" // (점유근거)보증금

    var posesnMtht: String? = "" // (점유근거)월세

    var posesnCntrctLc: String? = "" // (점유근거)계약(소유)위치

    var posesnCntrctAr: String? = "" // (점유근거)계약(소유)면적

    var posesnSpccntr: String? = "" // (점유근거)특약

    var clvtBgnde: String? = "" // 경작기간 시작일자

    var clvtEndde: String? = "" // 경작기간 종료일자

    var posesnLadResideAt: String? = "N"

    var posesnLadFarmerAt: String? = "N"

    var posesnOwnerClvtCnfirmAt: String? = "N"

    var posesnDbtamtRepAt: String? = "N"

    var posesnDbtamtRepInf: String? = ""

    var clvtBgndeList: MutableList<String>? = mutableListOf() // 추가 경작기간 시작일자

    var clvtEnddeList: MutableList<String>? = mutableListOf() // 추가 경작기간 종료일자

    var farmAddClvtList: MutableList<MutableMap<String, String>>? = mutableListOf() // 경작, 실제소득 통합 리스트

    var farmAddThingList: MutableList<MutableMap<String, String>>? = mutableListOf() // 추가 시설물 통합 리스트

    //스케치레이어데이터
    var thingFarmSketchPolygon: MutableList<PolygonOverlay>? = null

    // TODO: 2021-11-15 농업 경작내역 Polygon Arr
    var thingFarmPolygonArr: MutableList<ArrayList<LatLng>>? = null // 농업 경작내역 폴리곤 Arr
    var thingFarmPolygonCurrentArea: Int? = null // 경작내역 면적

    // 농업 경작물 추가
    var addFarmClvtdlList: JSONObject? = null

    // 농업 시설물 추가
    var addFarmThignList: JSONObject? = null

    var addOwnerListInfo: JSONArray? = null

    var thingOwnerInfoJson: JSONArray? = null

    var wtnncImage: MutableList<WtnncImage>? = null

    var pointYn: String? = "2"
    fun cleanThingFarmObject() {
        thingInfo = null
        thingNewSearch  = "Y"
        thingLrgeCl  = ""
        thingSmallCl  = ""
        thingKnd  = ""
        strctNdStrndrd  = ""
        bgnnAr  = ""
        incrprAr  = ""
        unitCl  = ""
        arComputBasis  = ""
        strctNdStndrd  = ""
        inclsCl  = ""
        acqsCl  = ""
        ownerCnfirmBasisCl  = ""
        ownshipBeforeAt  = ""
        apasmtTrgetAt  = ""
        rwTrgetAt  = ""
        paclrMatter  = ""
        rm  = ""
        changeResn  = ""
        referMatter  = ""
        frldbsLgalAt = ""
        frldbsBasisCl = ""
        frldbsDbtamtRepAt = ""
        frldbsDbtamtYear = ""
        frldbsDbtamtRepName = ""
        frldbsFrldLdgrAt = ""
        frldbsLadFarmerAt = ""
        frmrbsLgalAt = ""
        frmrbsBasisCl = ""
        frmrbsCnfrmnDta1At = ""
        frmrbsCnfrmnDta2At = ""
        frmrbsDbtamtAt = ""
        frmrbsAraResideAt = ""
        posesnLgalAt = ""
        posesnClvthmTy = ""
        posesnOwnerCnfrmAt = ""
        posesnCbCnfrmAt = ""
        posesnLrcdocAt = ""
        posesnRentName = ""
        posesnHireName = ""
        posesnRentBgnde = ""
        posesnRentEndde = ""
        posesnGtn = ""
        posesnMtht = ""
        posesnCntrctLc = ""
        posesnCntrctAr = ""
        posesnSpccntr = ""
        clvtBgnde = ""
        clvtEndde = ""
        posesnLadResideAt = "N"
        posesnLadFarmerAt = "N"
        posesnDbtamtRepAt = "N"
        posesnOwnerClvtCnfirmAt = "N"
        posesnDbtamtRepInf = ""
        clvtBgndeList = null
        clvtEnddeList = null
        farmAddClvtList = null
        farmAddThingList = null
        thingFarmSketchPolygon = null
        addFarmClvtdlList  = null
        addFarmThignList  = null
        wtnncImage = null
        pointYn = "2"
    }
}