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

object RestLandInfoObject {

    // 토지 조서 정보
    var landInfo : JSONObject? = null

    var landWtnCode: String? = null
    var saupCode: String? = null

    var landOwnerInfoJson: JSONArray? = null

    // 청구인
    var rqestPsn: String? = ""
    // 청구내용
    var rqestCn: String? = ""
    //조서1
    var examin1Rslt: String? = ""
    //조서2
    var examin2Rslt: String? = ""
    //조서3
    var examin3Rslt: String? = ""
    //조서4
    var examin4Rslt: String? = ""
    //조서5
    var examin5Rslt: String? = ""
    //조서6
    var examin6Rslt: String? = ""
    //조서7
    var examin7Rslt: String? = ""
    //조서8
    var examin8Rslt: String? = ""
    //조서9
    var examin9Rslt: String? = ""
    //조서10
    var examin10Rslt: String? = ""

    // 확대보상여부결과
    var rewdAt: String? = ""

    // 이유
    var resn: String? = ""

    fun clealRestLadObject() {

        landInfo = null
        landWtnCode = null
        saupCode = null
        landOwnerInfoJson = null
        rqestPsn = ""
        rqestCn = ""
        examin1Rslt = ""
        examin2Rslt = ""
        examin3Rslt = ""
        examin4Rslt = ""
        examin5Rslt = ""
        examin6Rslt = ""
        examin7Rslt = ""
        examin8Rslt = ""
        examin9Rslt = ""
        examin10Rslt = ""
        rewdAt = ""
        resn = ""
    }



}