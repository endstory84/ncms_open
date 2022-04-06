/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.naver.maps.geometry.LatLng
import org.json.JSONArray

object JsonArrayParseUtil {
    private var logUtil: LogUtil = LogUtil(JsonArrayParseUtil::class.java.simpleName)

    /**
     * Geoserver에서 가져온 GeoJson을 파싱하여 LatLng로 변환하여 return
     * @param resultArr
     * @param resultAddrArr -> 임시 주소 Addr
     * @param resultLadWtnCodeArr -> 임시 ladWtnCode Addr
     * @param geomArr
     * @return MutableList<ArrayList<LatLng>>
     */

    fun getGeomertyArrayParse(
        resultArr: JsonArray,
        resultAddrArr: MutableList<String>?,
        resultLadWtnCodeArr: MutableList<String>?,
        geomArr: MutableList<JsonArray>
    ): MutableList<ArrayList<LatLng>> {

        val resultLatLngArr = mutableListOf<ArrayList<LatLng>>()

        resultArr.forEachIndexed { _, dataArr ->
            if (dataArr.asJsonObject.get("geometry").toString() != "null") {
                when {
                    resultAddrArr != null -> {
                        resultAddrArr.add(dataArr.asJsonObject.get("properties").asJsonObject.get("jibun").asString) // WFS DATA (지적도 지번)
                    }
                }
                geomArr.add(dataArr.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray) // WFS DATA (지오메트리)
            }

            when {
                resultLadWtnCodeArr != null -> {
                    resultLadWtnCodeArr.add(dataArr.asJsonObject.get("properties").asJsonObject.get("LAD_WTN_CODE").asString)
                }
            }
        }

        geomArr.forEachIndexed { _, jsonArray ->
            val geomJsonArr = jsonArray[0].asJsonArray.get(0).asJsonArray
            val tempGeomStringArr = mutableListOf<String>()
            val tempGeomLatLngArr = mutableListOf<LatLng>()

            geomJsonArr.forEach { obj ->
                val splitStr = obj.toString().replace("[", "").replace("]", "").split(",")
                val sumStr = "${splitStr[1]}, ${splitStr[0]}"
                    tempGeomStringArr.add(sumStr)
            }

            tempGeomStringArr.forEach {
                tempGeomLatLngArr.add(LatLng(it.split(",")[0].toDouble(), it.split(",")[1].toDouble()))
            }

            resultLatLngArr.add(tempGeomLatLngArr as ArrayList<LatLng>)
        }

        return resultLatLngArr
    }

    fun getGeomertyArrayParseBsn(
        resultArr: JsonArray,
        resultAddrArr: MutableList<String>?,
        resultLadWtnCodeArr: MutableList<String>?,
        geomArr: MutableList<JsonArray>
    ): MutableList<ArrayList<LatLng>> {

        val resultLatLngArr = mutableListOf<ArrayList<LatLng>>()
        var polygonType = "";

        resultArr.forEachIndexed { _, dataArr ->
            if (dataArr.asJsonObject.get("geometry").toString() != "null") {
//                when {
//                    resultAddrArr != null -> {
//                        resultAddrArr.add(dataArr.asJsonObject.get("properties").asJsonObject.get("jibun").asString) // WFS DATA (지적도 지번)
//                    }
//                }
                geomArr.add(dataArr.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray) // WFS DATA (지오메트리)
                polygonType = dataArr.asJsonObject.get("geometry").asJsonObject.get("type").toString()
            }

            when {
                resultLadWtnCodeArr != null -> {
                    resultLadWtnCodeArr.add(dataArr.asJsonObject.get("properties").asJsonObject.get("LAD_WTN_CODE").asString)
                }
            }
        }

        geomArr.forEachIndexed { _, jsonArray ->


            val tempGeomStringArr = mutableListOf<String>()
            val tempGeomLatLngArr = mutableListOf<LatLng>()
            var geomJsonArr = JSONArray()

            if(polygonType.equals("\"Polygon\"")) {

                val geomJsonArr = jsonArray[0].asJsonArray

                geomJsonArr.forEach { obj ->
                    val splitStr = obj.toString().replace("[", "").replace("]", "").split(",")
                    val sumStr = "${splitStr[1]}, ${splitStr[0]}"
                    tempGeomStringArr.add(sumStr)
                }

            } else {
                val geomJsonArr = jsonArray[0].asJsonArray.get(0).asJsonArray
                geomJsonArr.forEach { obj ->
                    val splitStr = obj.toString().replace("[", "").replace("]", "").split(",")
                    val sumStr = "${splitStr[1]}, ${splitStr[0]}"
                    tempGeomStringArr.add(sumStr)
                }
            }


            tempGeomStringArr.forEach {
                tempGeomLatLngArr.add(LatLng(it.split(",")[0].toDouble(), it.split(",")[1].toDouble())) // convert
            }

            resultLatLngArr.add(tempGeomLatLngArr as ArrayList<LatLng>)
        }

        return resultLatLngArr
    }

    /**
     * 토지조서 Geometry Parse fn
     * @param jsonArr 토지조서 코드에 분류되어진 필터링 된 Arr
     * @param infoValueArr 토지조서 layer Info String Arr (GeoJSon -> properties -> 'No')
     * @param wtnCodeArr 토지조서 layer Info String Arr (GeoJSon -> properties -> 'WtnCode')
     * @param geomArr Geometry Array
     * @return MutableList<ArrayList<LatLng>>
     */
    fun getWtnccLandLayerGeometryArrayParse(resultArr: JsonArray, infoValueArr: MutableList<String>?, wtnCodeArr: MutableList<String>?, geomArr:MutableList<JsonArray>): MutableList<ArrayList<LatLng>>{
        val resultLatLngArr = mutableListOf<ArrayList<LatLng>>()

        resultArr.forEach {
            if (it.asJsonObject.get("geometry").toString() != "null") {

                when {
                    infoValueArr != null -> {
                        //resultLandInfoArr.add(it.asJsonObject.get("id").asString.split(".")[1]) // 토지레이어 'No' value 대입
                        infoValueArr.add(it.asJsonObject.get("properties").asJsonObject.get("NO").asString) // 토지레이어 'No' value 대입
                        wtnCodeArr?.add(it.asJsonObject.get("id").asString.split(".")[1]) // 토지레이어 'WtnCode' value 대입
                    }
                }

                geomArr.add(it.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray)
            }
        }

        geomArr.forEachIndexed { _, jsonArray ->
            val geomJsonArr = jsonArray[0].asJsonArray.get(0).asJsonArray
            val tempGeomStringArr = mutableListOf<String>()
            val tempGeomLatLngArr = mutableListOf<LatLng>()

            geomJsonArr.forEach { obj ->
                val splitStr = obj.toString().replace("[", "").replace("]", "").split(",")
                val sumStr = "${splitStr[1]}, ${splitStr[0]}"
                tempGeomStringArr.add(sumStr)
            }

            tempGeomStringArr.forEach {
                tempGeomLatLngArr.add(LatLng(it.split(",")[0].toDouble(), it.split(",")[1].toDouble()))
            }

            resultLatLngArr.add(tempGeomLatLngArr as ArrayList<LatLng>)
        }

        return resultLatLngArr
    }

    /**
     * 물건조서 Geometry Parse fn
     * @param jsonArr 물건조서 코드에 분류되어진 필터링 된 Arr
     * @param infoValueArr 토지조서 layer Info String Arr (GeoJSon -> properties -> 'wtnCode')
     * @param geomArr Geometry Array
     * @return MutableList<ArrayList<LatLng>>
     */
    fun getWtnccThingLayerGeometryArrayParse(jsonArr: List<JsonElement>, infoValueArr: MutableList<String>?, geomArr:MutableList<JsonArray>): MutableList<ArrayList<LatLng>>{

        val resultLatLngArr = mutableListOf<ArrayList<LatLng>>()

        jsonArr.forEach {
            if (it.asJsonObject.get("geometry").toString() != "null") {

                when {
                    infoValueArr != null -> {
                        /**
                         * AS-IS = 물건조서 코드
                         * TO-BE = MO_NO (모바일 번호)
                         */

                        //infoValueArr.add(it.asJsonObject.get("id").asString.split(".")[1]) // Geojson -> WTN Code
                        try {
                            infoValueArr.add(it.asJsonObject.get("properties").asJsonObject.get("MO_NO").asString)
                        }
                        catch(e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                geomArr.add(it.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray)
            }
        }

        geomArr.forEachIndexed { _, jsonArray ->
            val geomJsonArr = jsonArray[0].asJsonArray.get(0).asJsonArray
            val tempGeomStringArr = mutableListOf<String>()
            val tempGeomLatLngArr = mutableListOf<LatLng>()

            geomJsonArr.forEach { obj ->
                val splitStr = obj.toString().replace("[", "").replace("]", "").split(",")
                val sumStr = "${splitStr[1]}, ${splitStr[0]}"
                tempGeomStringArr.add(sumStr)
            }

            tempGeomStringArr.forEach {
                tempGeomLatLngArr.add(LatLng(it.split(",")[0].toDouble(), it.split(",")[1].toDouble()))
            }

            resultLatLngArr.add(tempGeomLatLngArr as ArrayList<LatLng>)
        }

        return resultLatLngArr

    }
}