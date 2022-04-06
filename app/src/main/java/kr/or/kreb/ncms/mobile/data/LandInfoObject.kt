/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.data

import com.carto.core.MapPos
import com.carto.vectorelements.Text
import com.google.gson.JsonArray
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.PolygonOverlay
import kr.or.kreb.ncms.mobile.adapter.LandSearchRealngrAdapter
import org.json.JSONArray
import org.json.JSONObject

object LandInfoObject {

    // 토지 조서 정보
    var landInfo : JSONObject? = null
    //자연림여부
    var nrfrstAtChk : String? ="N"
    //경작여부
    var clvtAtChk : String? ="N"
    //건축물여부
    var buildAtChk : String? ="N"
    //대지권여부
    var plotAtChk : String? ="N"

    var rwTrgetAt: String? = "N"
    //측량요청
    var sttusMesrAtChk : String? ="N"

    var partitnTrgetAt: String? = "N"

    //특수용지
    var spclLadCl : String? =""
    //특수용지내용
    var spclLadCn : String? = ""
    //소유자확인근거
    var ownerCnfirmBasisCl : String? = ""

    var realLngrty : String? = "N"

    //현실적이용현황
    var searchRealLand : JSONArray? = null
    //레이어데이터
    var latLngArr : MutableList<LatLng>? = null
    var realLandPolygon : MutableList<PolygonOverlay>? = null

    // TODO: 2021-11-02 WFS 실제이용현황 폴리곤 Data Arr 추가
    var realLandPolygonArr: MutableList<ArrayList<LatLng>>? = null // 실제이용현황 폴리곤 Arr

    var polygonData : ArrayList<HashMap<String, String>>? = null

    // 편입지번
    var incrprLnm: String = ""

    // 관련지번
    var relateLnm: String = ""

    //특이사항
    var paclrMatter : String? =""

    var referMatter : String? = ""

    var rm : String? = ""

    /////// 실제이용현황 ///////
    var _isPolygonVisible = false // 실제이용현황 편집 alert check 여부
    var selectLandPolygonArr = mutableListOf<LatLng>()
    var selectPolygonCenterTxt = ""
    var selectPolygonCurrentArea:  Int? = 0 // 실제이용현황 (수정 전) 임시 데이터 저장

    var clickLatLng  = mutableListOf<MapPos>()
    var clickLatLngArr = arrayListOf<MutableList<MapPos>>()
    var mapPos  = mutableListOf<MapPos>()

    var mapCenter: MapPos? = null
    var lineCenterTxList = mutableListOf<Text>()
    var currentArea: Int? = 0 // 실제이용현황 면적
    var landSearchRealLngrAdpater: LandSearchRealngrAdapter? = null // 실제이용현황 어댑터
    var landSearchRealLngrJsonArray: JSONArray? = null // 실제이용현황 JsonArray

    var landOwnerInfoJson: JSONArray? = null

    var wtnCode = ""
    var wtnCodeBgnnLnm = ""
    var wtnCodeIncrprLnm = ""
    var wtnCodeLegaldongCode = ""
    var getSameWtnCode = ""  // 일치하는 wtnCode 임시저장
    var getSameWtnCodeJsonArray = JsonArray() // 일치하는 wtncCode의 지오메트리 정보 임시저장

    var ladCadastralJsonArray = JsonArray()
    var ladEditCadastralJsonArray = JsonArray()

    var addOwnerListInfo: JSONArray? = null

    var wtnncImage: MutableList<WtnncImage>? = null

    var realLandInfoLength = 0


    //사진1
    //사진2
    //사진3
    //사진4
    //사진5
    //사진6
    //사진7
    //사진8
    //사진9
    //사진10


}