/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.Location
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.carto.core.MapPos
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.include_wtnnc.*
import kotlinx.coroutines.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.CoordinatesEnum
import kr.or.kreb.ncms.mobile.enums.GeoserverLayerEnum
import kr.or.kreb.ncms.mobile.fragment.ContextDialogFragment
import kr.or.kreb.ncms.mobile.fragment.LandSearchFragment
import kr.or.kreb.ncms.mobile.view.InfoView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import kotlin.math.roundToInt


class NaverMapUtil(
    var context: Context?,
    var activity: Activity?,
    private var mapView: MapView?,
    val coord: String?,
    val restData: String?
) :
    OnMapReadyCallback,
    NaverMap.OnCameraIdleListener,
    NaverMap.OnCameraChangeListener,
    NaverMap.OnLocationChangeListener,
    NaverMap.OnMapClickListener,
    NaverMap.OnMapLongClickListener,
    NaverMap.OnMapDoubleTapListener,
    DialogUtil.ClickListener {

    /**
     * Boolean
     */
    var isCadastralVisable = false // ???????????? ????????? On / Off
    var isVisableContextPopup = false

    /**
     * ??????
     */
    private var gpsUtil = GPSUtil(context!!)
    private var logUtil: LogUtil = LogUtil(NaverMapUtil::class.java.simpleName)
    private var toastUtil: ToastUtil  = ToastUtil(context!!)
    var dialogUtil: DialogUtil
    var dialogBuilder: MaterialAlertDialogBuilder
    private var progressDialog: AlertDialog? = null

    /**
     * ????????? ID ??? key ????????? ?????? ??????
     * ?????? ???????????? ??????????????? ID / KEY ??????
     */

    private var naverID ="9mek16psq2"
    private var naverKey ="1yoMMipAFWX37VohQWgXULY3AjPOGAwouy7feZqo"
    lateinit var naverMap: NaverMap
    private lateinit var naverUiSettings: UiSettings
    private lateinit var fusedLocationSource: FusedLocationSource

    /**
     * Naver Map SDK Object
     */
    val marker = Marker()
    var lon: Double = 0.0
    var lat: Double = 0.0
    var zoom: Double = 0.0


    /**
     * Naver Map SDK Object Variable
     */

    private var naverCameraidleCnt: Int = 0 // ????????? ??? ?????? ???????????? ?????????????????? ?????????
    var geoserverWmsBitmap: Bitmap? = null
    var globalMapClickCoord = ""

    /**
     * Geoserver WMS Layer Array
     */
    var wmsSidoOverlayArr = mutableListOf<GroundOverlay>() // ??????
    var wmsSigunguOverlayArr = mutableListOf<GroundOverlay>() // ?????????
    var wmsDongOverlayArr = mutableListOf<GroundOverlay>() // ?????????
    var wmsLiOverlayArr = mutableListOf<GroundOverlay>() // ?????????
    var wmsNaverCadastralOverlayArr = mutableListOf<GroundOverlay>() // ???????????????

    /**
     * Geoserver WFS Layer Array
     */
    var wfsLadOverlayArr = mutableListOf<PolygonOverlay>() // ??????
    var wfsRealLadOverlayArr = mutableListOf<PolygonOverlay>() // ??????????????????
    var wfsThingOverlayArr = mutableListOf<PolygonOverlay>() // ?????????
    var wfsBsnOverlayArr = mutableListOf<PolygonOverlay>() // ??????
    var wfsFarmOverlayArr = mutableListOf<PolygonOverlay>() // ??????
    var wfsResidntOverlayArr = mutableListOf<PolygonOverlay>() // ?????????
    var wfsTombOverlayArr = mutableListOf<PolygonOverlay>() // ??????
    var wfsCadastralOverlayArr = mutableListOf<PolygonOverlay>() // ???????????????
    var wfsBsnAreaOverlayArr = mutableListOf<PolygonOverlay>() // ????????????(?????????)
    var wfsEditCadastralOverlayArr = mutableListOf<PolygonOverlay>() // ???????????????

    /**
     * Map ??? Sketch Data Object Arr
     */

    var polygonOverlayArr = mutableListOf<PolygonOverlay>() // ????????? ?????? ?????????
    private var selectPolygonOverlayArr = mutableListOf<PolygonOverlay>() // ????????? ?????? ????????? ?????????

    /**
     * ????????? InfoView Arr
     */

    var cadastralInfoViewArr = mutableListOf<InfoWindow>() // ??????????????? ????????? InfoView
    var wtnccInfoLadViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (??????)
    var wtnccInfoThingViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (?????????)
    var wtnccInfoFarmViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (??????)
    var wtnccInfoTombmViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (??????)
    var wtnccInfoResidntViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (?????????)
    var wtnccInfoBsnViewArr = mutableListOf<InfoWindow>() // ?????? ????????? InfoView (??????)

    /**
     * LatLng (Naver Map SDK) Arr (???,??????)
     */

    var resultLatLngArr = mutableListOf<ArrayList<LatLng>>()

    var resultCadastralLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultRealLandLatLngArr = mutableListOf<ArrayList<LatLng>>()

    var resultEditCadastralLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultBsnsAreaLatLngArr = mutableListOf<ArrayList<LatLng>>()

    var resultLandLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultThingLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultTombLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultFarmLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultResidntLatLngArr = mutableListOf<ArrayList<LatLng>>()
    var resultBsnLatLngArr = mutableListOf<ArrayList<LatLng>>()

    /**
     * WFS Data Properties Arr
     */

    var resultAddrArr = mutableListOf<String>()
    var resultLadPropertiesNoArr = mutableListOf<String>()
    var resultLadPropertiesWtnCodeArr = mutableListOf<String>()
    var resultThingPropertiesWtnCodeArr = mutableListOf<String>()
    var resultLadWtnCodeArr = mutableListOf<String>()

    /**
     * Polygon (Naver Map SDK) Arr (???,??????)
     */
    private var modifyPolygon = mutableListOf<LatLng>()
    private var modifyPolygonOverlayArr = mutableListOf<ArrayList<LatLng>>() // ?????? ????????? ?????? ???????????? ??????????????? ?????? ?????????

    var contextDialogItems = mutableListOf<String>()
    var contextDialogWtnCodeItems = mutableListOf<String>()

    private var contextPopupFragment: Fragment? = null
    private var contextPopupFragmentManager: FragmentManager? = null

    val selectCadastralPolygonArr = mutableListOf<PolygonOverlay>() // ????????? ???????????? Polygon Arr
    val highlightPolygonArr = mutableListOf<PolygonOverlay>()


    // ???????????? ?????? ???,?????? ??? ????????? ??????
    var wtnncPicMarkerArr = mutableListOf<Marker>()


    init {
        setLocationSource()
        NaverMapSdk.getInstance(context!!).client = NaverMapSdk.NaverCloudPlatformClient(naverID)
        mapView?.getMapAsync(this)

        gpsUtil.getLocation() // ???????????? ????????????
        lat = gpsUtil.getLatitude()
        lon = gpsUtil.getLongitude()

        dialogUtil = DialogUtil(context, activity)
        dialogBuilder = MaterialAlertDialogBuilder(activity!!)
        dialogUtil.setClickListener(this)

        progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))
        getActivity().btnMapZoom.text.let { "18" }

        contextPopupFragmentManager = getActivity().supportFragmentManager

    }


    /** ?????? ???????????? ???????????? */
    private fun getActivity(): MapActivity = (activity as MapActivity)

    /** ???????????? ?????? Setting */
    private fun setLocationSource(){ fusedLocationSource = activity?.let { FusedLocationSource(it, PERMISSION_REQUEST_CODE) }!! }

    private fun initCadastral(){
        getActivity().toggleButtonCadstral.background = ContextCompat.getDrawable(context!!, R.drawable.img_toggle_cadastral_on)
        isCadastralVisable = true
    }

    /** ????????? NaverMap Settging */
    override fun onMapReady(naverMap: NaverMap) {

        this.naverMap = naverMap
        this.naverUiSettings = naverMap.uiSettings

        this.naverMap.apply {
            locationSource = fusedLocationSource
            isIndoorEnabled = true
            locationTrackingMode = LocationTrackingMode.Follow

            // ??? ??????
            minZoom = 5.0
            maxZoom = 21.0
            zoom = cameraPosition.zoom
        }

        this.naverMap.addOnCameraIdleListener(this) // ????????? ??????

        this.naverUiSettings.apply {
            isScaleBarEnabled = true
            isZoomGesturesEnabled = true
            isZoomControlEnabled = true
            isLocationButtonEnabled = true
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isScrollGesturesEnabled = true
        }




        if(coord != "null"){
            coord.let {
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(it!!.split(",")[0].toDouble(), it.split(",")[1].toDouble()), 19.0).animate(CameraAnimation.Fly, 1500) // ???????????? '??????' ??????????????? ??????
                this.naverMap.moveCamera(cameraUpdate)
            }
        } else {
//            this.naverMap.locationTrackingMode = LocationTrackingMode.Follow // ????????? ?????? ?????? ??????



    // ?????? ?????? ?????????

    //        valcameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.739252058042496, 126.71188187147061), 19.0).animate(CameraAnimation.Fly, 1500)
//        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.632429499999944, 126.80901999999973), 19.0).animate(CameraAnimation.Fly, 1500) // ????????? ????????? ????????? 435-3
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.63007654100637, 126.8392148530448), 19.0).animate(CameraAnimation.Fly, 1500) // ????????? ?????????  33-2
////            val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.54423997439402, 126.98655510433768), 19.0).animate(CameraAnimation.Fly, 1500) // ????????? ????????? ?????????2??? - ???????????????
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.61826946795486, 126.92674726263856), 19.0).animate(CameraAnimation.Fly, 1500) // ????????? ????????? 331-81 - ?????????
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.61563184712451, 126.37731113159548), 19.0).animate(CameraAnimation.Fly, 1500) // ???
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.740744199999924, 126.71003249999977), 19.0).animate(CameraAnimation.Fly, 1500) // ???????????? 343
//
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.740646653488554, 126.71143287086625), 19.0).animate(CameraAnimation.Fly, 1500)
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.74023881703989, 126.7138217388224), 19.0).animate(CameraAnimation.Fly, 1500)
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.73898478460851, 126.7117508788454), 19.0).animate(CameraAnimation.Fly, 1500)
////        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.67310319542753, 126.73827157639812), 19.0).animate(CameraAnimation.Fly, 1500) // ??????
//            this.naverMap.moveCamera(cameraUpdate)
        }

        this.naverMap.onMapClickListener = this
        this.naverMap.onMapLongClickListener = this
        this.naverMap.onMapDoubleTapListener = this

        initCadastral()


        /**
         * @description 2?????? ?????? ??? withContext ?????? (????????? ?????????)
         */

        GlobalScope.launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                when(Constants.BIZ_SUBCATEGORY_KEY){
                    BizEnum.LAD -> {
                        getActivity().apply {
                            ladLayerSwitch.isChecked = true
                            isLadLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_LAD_WTN.value, "??????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    BizEnum.THING -> {
                        getActivity().apply {
                            thingLayerSwitch.isChecked = true
                            isThingLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "?????????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    BizEnum.TOMB -> {
                        getActivity().apply {
                            tombLayerSwitch.isChecked = true
                            isTombLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    BizEnum.FARM -> {
                        getActivity().apply {
                            farmLayerSwitch.isChecked = true
                            isFarmLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    BizEnum.RESIDNT -> {
                        getActivity().apply {
                            residntLayerSwitch.isChecked = true
                            isResidntLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "?????????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    BizEnum.BSN, BizEnum.MINRGT, BizEnum.FYHTS -> {
                        getActivity().apply {
                            bsnLayerSwitch.isChecked = true
                            isBsnLayerChecked = true
                            bsnsAreaLayerSwitch.isChecked = true
                            isBsnsAreaLayerChecked = true
                        }
                        getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????")
//                        getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                    }
                    else -> {}
                }
            }
        }

    }

    /**
     * ??? ??????
     * @return mapView -> ?????? ??????
     */
    fun getMapView(): MapView? = mapView

    /**
     * Zoom Lv ????????????
     * @return zoom -> ?????? ???
     */
    fun getNaverMapZoom(): Double = this.naverMap.cameraPosition.zoom

    /**
     * ????????? ??? Type Switch
     * @param type ????????? ??? Type
     */
    fun setNaverMapType(type: String) {
        when (type) {
            "basic" -> this.naverMap.mapType = NaverMap.MapType.Basic
            "hybrid" -> this.naverMap.mapType = NaverMap.MapType.Hybrid
            "cadastralOn" -> {
                getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)")
                getWFSLayer(GeoserverLayerEnum.CADASTRAL.value, "???????????????")
                getWFSLayer(GeoserverLayerEnum.CADASTRAL_EDIT.value, "???????????????")

                getActivity().bsnsAreaLayerSwitch.isChecked = true
                isCadastralVisable = true
                getActivity().cadstralEditLayerSwitch.isChecked = true

            }
            "cadastralOff" -> {
                clearWFS(wfsBsnAreaOverlayArr, "????????????(?????????)")
                clearWFS(wfsCadastralOverlayArr, "???????????????")
                clearWFS(wfsEditCadastralOverlayArr, "???????????????")
                isCadastralVisable = false
                getActivity().cadstralEditLayerSwitch.isChecked = false

                getActivity().bsnsAreaLayerSwitch.isChecked = false
            }
        }
    }

    /**
     * ????????? ??? getExtent
     * @return String -> Map Extent
     */
    private fun getExtent(): String = arrayListOf(naverMap.coveringBounds.southWest.longitude, naverMap.coveringBounds.southWest.latitude, naverMap.coveringBounds.northEast.longitude, naverMap.coveringBounds.northEast.latitude).joinToString(separator =",")

    /**
     *  GEOSERVER WMS LAYER ?????????
     *  @param layerName -> WMS Layer ???
     */
    fun getWMSLayer(layerName: String) {

        HttpUtil.getInstance(context!!).callerUrlInfoPostGeoServer(
            setGeoserverRequestQuery("WMS", layerName), progressDialog, context!!.getString(R.string.geoserver_wms_url),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    progressDialog?.dismiss()
                    logUtil.e(e.toString())
                }
                override fun onResponse(call: Call, response: Response) {
                    geoserverWmsBitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                    logUtil.d("WMS Layer ????????? ??????")

                    activity!!.runOnUiThread {
                        FileUtil.convertToPNG(geoserverWmsBitmap!!) // bitmap -> png (??????????????? ??????)
                        when (layerName) {
                            GeoserverLayerEnum.SIDO.value -> setWMSLayerImage(wmsSidoOverlayArr)
                            GeoserverLayerEnum.SIGUNGU.value -> setWMSLayerImage(wmsSigunguOverlayArr)
                            GeoserverLayerEnum.EMD.value -> setWMSLayerImage(wmsDongOverlayArr)
                            GeoserverLayerEnum.LI.value -> setWMSLayerImage(wmsLiOverlayArr)
                        }

                        progressDialog?.dismiss()
                    }

                }
            })
    }

    /**
     *  GEOSERVER WMS LAYER ????????? ?????????
     *  @param layerName -> WMS Layer ???
     */
    fun setWMSLayerImage(arr:MutableList<GroundOverlay>){
        val wmsGroundOverLay = GroundOverlay()
        arr.add(wmsGroundOverLay)
        arr.forEach { layer ->
            layer.apply {
                bounds = naverMap.coveringBounds
                image = OverlayImage.fromBitmap(FileUtil.replaceColor(geoserverWmsBitmap!!)!!)
                map = naverMap
            }
        }
    }

    /**
     *  GEOSERVER WFS LAYER ?????????
     *  @param layerName -> Geoserver ????????? ???
     *  @param tagName -> Geoserver tag ???
     */
    fun getWFSLayer(layerName: String, tagName: String) {

        val geoserverWfsUrl = when(layerName){
            GeoserverLayerEnum.CADASTRAL.value -> context!!.getString(R.string.geoserver_kais_wfs_url)
            else -> context!!.getString(R.string.geoserver_wfs_url)
        }

        val geoserverWfsLayerName = when(layerName){
            GeoserverLayerEnum.CADASTRAL.value -> "cite:eubri_all_wgs84"
            else -> layerName
        }

        HttpUtil.getInstance(context!!).callerUrlInfoPostGeoServer(setGeoserverRequestQuery("WFS", geoserverWfsLayerName), progressDialog, geoserverWfsUrl,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) { progressDialog?.dismiss(); logUtil.e(e.toString()) }
                    override fun onResponse(call: Call, response: Response) {

                        val resultGeomArr = mutableListOf<JsonArray>()

                        if (response.isSuccessful) {

                            /** @description WFS ????????? ????????? ?????? ?????? ????????? ???????????? ????????????. */

                            try{

                                val resultData = httpResultToJsonObject(response.body!!.string())
                                val resultCnt: Int = resultData?.get("totalFeatures")!!.asInt
                                val resultArr: JsonArray = resultData.get("features")!!.asJsonArray // ?????? ?????????

                                activity!!.runOnUiThread {

                                    when(tagName) {

                                        "???????????????" -> {

                                            clearWFS(wfsEditCadastralOverlayArr, tagName)

                                            if (getNaverMapZoom() > 13) {
                                                if (resultCnt > 0) {
                                                    val getGeomArr = JsonArrayParseUtil.getGeomertyArrayParse(resultArr, null, null, resultGeomArr)

                                                    LandInfoObject.ladEditCadastralJsonArray = resultArr
                                                    resultEditCadastralLatLngArr = getGeomArr

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, null)

                                                } else {
                                                    progressDialog?.dismiss()
                                                }

                                            } else {
                                                progressDialog?.dismiss()
                                            }
                                            return@runOnUiThread
                                        }

                                        "???????????????" -> {
                                            clearWFS(wfsCadastralOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {
                                                    val getGeomArr = JsonArrayParseUtil.getGeomertyArrayParse(resultArr, resultAddrArr, null, resultGeomArr)
                                                    LandInfoObject.ladCadastralJsonArray = resultArr
                                                    resultLatLngArr = getGeomArr
                                                    resultCadastralLatLngArr = getGeomArr

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, null)
                                                    setCadastralLayerInfo(resultAddrArr, cadastralInfoViewArr)

                                                } else {
                                                    isCadastralVisable = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_empty)!!, 100)
                                                    activity!!.toggleButtonCadstral.background = ContextCompat.getDrawable(context!!, R.drawable.img_toggle_cadastral)
                                                }

                                            } else {
                                                isCadastralVisable = false
                                                progressDialog?.dismiss()
                                                activity!!.toggleButtonCadstral.background = ContextCompat.getDrawable(context!!, R.drawable.img_toggle_cadastral)
                                                toastUtil.msg_error(context?.resources?.getString(R.string.map_wfs_cadastral_minzoom)!!, 100)
                                            }
                                            return@runOnUiThread
                                        }
                                        "????????????(?????????)" -> {
                                            clearWFS(wfsBsnAreaOverlayArr, tagName)
                                            if (getNaverMapZoom() > 13) {
                                                if (resultCnt > 0) {
                                                    val getGeomArr = JsonArrayParseUtil.getGeomertyArrayParseBsn(resultArr, null, null, resultGeomArr)

                                                    resultBsnsAreaLatLngArr = getGeomArr

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, null)

                                                } else {
                                                    progressDialog?.dismiss()
                                                }

                                            } else {
                                                progressDialog?.dismiss()
                                            }
                                            return@runOnUiThread
                                        }

                                        "??????" -> {
                                            clearWFS(wfsLadOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccLandLayerGeometryArrayParse(resultArr, resultLadPropertiesNoArr, resultLadPropertiesWtnCodeArr, resultGeomArr)

                                                    logUtil.d("?????? WFS Geom Array -> $getGeomArr")
                                                    logUtil.d("?????? WFS NO Array -> $resultLadPropertiesNoArr")
                                                    logUtil.d("?????? WFS LAD_WTN_CODE Array -> $resultLadPropertiesWtnCodeArr")

                                                    LandInfoObject.getSameWtnCodeJsonArray = resultArr
                                                    resultLandLatLngArr = getGeomArr

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, null)
                                                    setWtncclLayerInfo(resultLadPropertiesNoArr, wtnccInfoLadViewArr, resultLandLatLngArr)

                                                  } else {
                                                    getActivity().isLadLayerChecked = false
                                                    getActivity().ladLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_Lad_empty)!!, 100)
                                                }
                                            } else {
                                                getActivity().isLadLayerChecked = false
                                                getActivity().ladLayerSwitch.isChecked = false
                                                progressDialog?.dismiss()
                                                toastUtil.msg_error(context?.resources?.getString(R.string.map_wfs_reallad_minzoom)!!, 100)
                                            }
                                        }

                                        "??????????????????" -> {
                                            clearWFS(wfsRealLadOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val getGeomArr = JsonArrayParseUtil.getWtnccLandLayerGeometryArrayParse(resultArr, null, null, resultGeomArr)

                                                    logUtil.d("?????????????????? WFS Geom Array -> $getGeomArr")
                                                    resultRealLandLatLngArr = getGeomArr

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon( getGeomArr, tagName, polyColor, polyLineColor, null)
                                                    setWtncclLayerInfo(resultLadPropertiesNoArr, wtnccInfoLadViewArr, resultLandLatLngArr)

                                                } else {
                                                    getActivity().isLadRealLayerChecked = false
                                                    getActivity().ladRealLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_realLad_empty)!!, 100)
                                                }
                                            } else {
                                                getActivity().isLadRealLayerChecked = false
                                                getActivity().ladRealLayerSwitch.isChecked = false
                                                progressDialog?.dismiss()
                                                toastUtil.msg_error(context?.resources?.getString(R.string.map_wfs_reallad_minzoom)!!, 100)
                                            }
                                        }

                                        /**
                                         * @description ???????????? ?????? ??????
                                         *
                                         * 1. THING_LRGE_CL
                                         * A011001 ?????????
                                         * A011002 ??????
                                         * A011003 ??????
                                         * A011004 ?????????
                                         * A011005 ??????/??????/??????
                                         *
                                         * 1-1 ) THING_SMALL_CL  ex) A011005 -> ?????? ?????? ????????? ????????? 'A016020'??? ????????? ?????????.
                                         * A016020 ??????
                                         * A016030 ??????
                                         *
                                         * 1-2 ) THING_SMALL_CL ex) A011005 -> ?????? ?????? ????????? ????????? 'A016001'??? ????????? '??????'??? ?????????.
                                         * A016001  ??????
                                         * A016040  ??????
                                         * A016050  ??????
                                         */

                                        "?????????" -> {
                                            clearWFS(wfsThingOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val filterArr = wtnccArrFilter(resultArr, "A011001", tagName)
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccThingLayerGeometryArrayParse(filterArr, resultThingPropertiesWtnCodeArr, resultGeomArr)

                                                    ThingWtnObject.thingWtnncJsonArray = resultArr

                                                    resultThingLatLngArr = getGeomArr
                                                    logUtil.d("resultThingGeomArr -> $getGeomArr")

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

//                                                    var moPointYn = resultArr.asJsonObject.get("properties").asJsonObject.get("MO_POINT_YN").asString


//                                                    var moPointYn = "1"
//                                                    if(moPointYn.equals("1")) {
//                                                        setWtncclLayerInfoPoint(resultThingPropertiesWtnCodeArr, wtnccInfoThingViewArr, resultThingLatLngArr)
//                                                    } else {
//                                                        setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
//                                                        setWtncclLayerInfo(resultThingPropertiesWtnCodeArr, wtnccInfoThingViewArr, resultThingLatLngArr)
//                                                    }
//
                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
                                                    setWtncclLayerInfoPoint(filterArr, resultThingPropertiesWtnCodeArr, wtnccInfoThingViewArr, resultThingLatLngArr)


                                                } else {
                                                    getActivity().isThingLayerChecked = false
                                                    getActivity().thingLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_thing_empty)!!, 100)
                                                }
                                            } else {
                                                getActivity().isThingLayerChecked = false
                                                getActivity().thingLayerSwitch.isChecked = false
                                                progressDialog?.dismiss()
                                                toastUtil.msg_error(context?.resources?.getString(R.string.map_wfs_thing_minzoom)!!, 100)
                                            }
                                            return@runOnUiThread
                                        }

                                        "??????" -> {
                                            clearWFS(wfsFarmOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val filterArr = wtnccArrFilter(resultArr, "A011002", tagName)
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccThingLayerGeometryArrayParse(filterArr, resultThingPropertiesWtnCodeArr, resultGeomArr)

                                                    resultFarmLatLngArr = getGeomArr
                                                    logUtil.d("resultFarmLatLngArr -> $getGeomArr")

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
                                                    setWtncclLayerInfo(resultThingPropertiesWtnCodeArr, wtnccInfoFarmViewArr, resultFarmLatLngArr)

                                                    progressDialog?.dismiss()

                                                } else {
                                                    getActivity().isFarmLayerChecked = false
                                                    getActivity().farmLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_farm_empty)!!, 100)
                                                }
                                            }
                                            return@runOnUiThread
                                        }

                                        "??????" -> {
                                            clearWFS(wfsTombOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val filterArr = wtnccArrFilter(resultArr, "A011003", tagName)
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccThingLayerGeometryArrayParse(filterArr, resultThingPropertiesWtnCodeArr, resultGeomArr)

                                                    resultTombLatLngArr = getGeomArr
                                                    logUtil.d("resultTombLatLngArr -> $getGeomArr")

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
                                                    setWtncclLayerInfo(resultThingPropertiesWtnCodeArr, wtnccInfoTombmViewArr, resultTombLatLngArr)

                                                    progressDialog?.dismiss()

                                                } else {
                                                    getActivity().isTombLayerChecked = false
                                                    getActivity().tombLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_tomb_empty)!!, 100)
                                                }
                                            }
                                            return@runOnUiThread
                                        }

                                        "?????????" -> {
                                            clearWFS(wfsResidntOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val filterArr = wtnccArrFilter(resultArr, "A011004", tagName)
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccThingLayerGeometryArrayParse(filterArr, resultThingPropertiesWtnCodeArr, resultGeomArr)

                                                    resultResidntLatLngArr = getGeomArr
                                                    logUtil.d("resultResidntLatLngArr -> $getGeomArr")

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
                                                    setWtncclLayerInfo(resultThingPropertiesWtnCodeArr, wtnccInfoResidntViewArr, resultResidntLatLngArr)

                                                    progressDialog?.dismiss()

                                                } else {
                                                    getActivity().isResidntLayerChecked = false
                                                    getActivity().residntLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_residnt_empty)!!, 100)
                                                }
                                            }
                                            return@runOnUiThread
                                        }

                                        "??????" -> {
                                            clearWFS(wfsBsnOverlayArr, tagName)
                                            if (getNaverMapZoom() > 17) {
                                                if (resultCnt > 0) {

                                                    val filterArr = wtnccArrFilter(resultArr, "A011005", tagName)
                                                    val getGeomArr = JsonArrayParseUtil.getWtnccThingLayerGeometryArrayParse(filterArr, resultThingPropertiesWtnCodeArr, resultGeomArr)

                                                    ThingFyhtsObject.getSameWtnCodeJsonArray = filterArr

                                                    resultBsnLatLngArr = getGeomArr
                                                    logUtil.d("resultBsnLatLngArr -> $getGeomArr")

                                                    val polyColor = setWFSLayerColorStyle(tagName)
                                                    val polyLineColor = setWFSLayerColorStrokeStyle(tagName)

                                                    setDrawPolygon(getGeomArr, tagName, polyColor, polyLineColor, filterArr)
                                                    setWtncclLayerInfo(resultThingPropertiesWtnCodeArr, wtnccInfoBsnViewArr, resultBsnLatLngArr)

                                                    progressDialog?.dismiss()

                                                } else {
                                                    getActivity().isBsnLayerChecked = false
                                                    getActivity().bsnLayerSwitch.isChecked = false
                                                    progressDialog?.dismiss()
                                                    toastUtil.msg_warning(context?.resources?.getString(R.string.msg_server_connected_resut_bsn_empty)!!, 100)
                                                }
                                            }
                                            return@runOnUiThread
                                        }
                                }
                            }

                            }catch (e:Exception){
                                logUtil.d(e.toString())
                            }

                        }
                    }
                })
    }

    /**
     * ???????????? ????????? ?????? Array??? ??????????????????.
     */
    fun wtnccArrFilter(jsonArr:JsonArray, wtnccCode:String, tagName: String): List<JsonElement>{

        var filterArr: List<JsonElement>? = null

        when(tagName){

            "?????????", "??????", "?????????", "??????" -> {
                filterArr = jsonArr.filter {
                    ((it as JsonObject).get("properties") as JsonObject).get("THING_LRGE_CL").asString == wtnccCode
                }
                logUtil.d("${filterArr.size} ($tagName) ?????? (GeoJson) ???????????? ?????????.")
            }
            "??????" -> {
                filterArr = jsonArr.filter {
                    ((it as JsonObject).get("properties") as JsonObject).get("THING_SMALL_CL").asString == wtnccCode
                }
                logUtil.d("${filterArr.size} ($tagName) ?????? (GeoJson) ???????????? ?????????.")
            }

            "??????" -> {

                filterArr = jsonArr.filter {
                    ((it as JsonObject).get("properties") as JsonObject).get("THING_LRGE_CL").asString == wtnccCode
                }

                when(Constants.BIZ_SUBCATEGORY_KEY){

                    /*
                     * A016020  ??????
                     * A016030  ??????
                     * A016001  ??????
                     * A016040  ??????
                     * A016050  ??????
                    */

                    BizEnum.MINRGT -> {
                        val filterMinrgtArr = filterArr.filter {
                            ((it.asJsonObject.get("properties") as JsonObject).get("THING_SMALL_CL").asString == "A016020") // -> ??????
                        }
                        filterArr = filterMinrgtArr
                        logUtil.d("${filterArr.size} (??????) ?????? (GeoJson) ???????????? ?????????.")
                    }

                    BizEnum.FYHTS -> {
                        val filterFyhtsArr = filterArr.filter {
                            ((it.asJsonObject.get("properties") as JsonObject).get("THING_SMALL_CL").asString == "A016030") // -> ??????
                        }
                        filterArr = filterFyhtsArr
                        logUtil.d("${filterArr.size} (??????) ?????? (GeoJson) ???????????? ?????????.")
                    }

                    BizEnum.BSN -> {
                        val filterBsnArr = filterArr.filter {
                            ((it.asJsonObject.get("properties") as JsonObject).get("THING_SMALL_CL").asString == "A016001") or
                            ((it.asJsonObject.get("properties") as JsonObject).get("THING_SMALL_CL").asString == "A016040") or
                            ((it.asJsonObject.get("properties") as JsonObject).get("THING_SMALL_CL").asString == "A016050")  // -> ??????, ??????, ??????
                        }
                        filterArr = filterBsnArr
                        logUtil.d("${filterArr.size} (??????/?????????/??????) ?????? (GeoJson) ???????????? ?????????.")
                    }


                    else -> {}
                }
            }
        }

        return filterArr!!
    }


    /**
     * Geoserver WFS ????????? ??????
     * @param latLngArr
     * @param tagName
     * @param polyColor
     * @param strokeColor
     * @param jsonArr -> WFS data Arr
     */

    fun setDrawPolygon(latLngArr: MutableList<ArrayList<LatLng>>, tagName: String, polyColor:Int, strokeColor: Int, jsonArr: List<JsonElement>?) {

        /**
         * NaverMap globalZindex
         * ?????? ???: 400000
         * ?????? ????????????: 300000
         * ??????: 200000
         * ????????? ????????????: 100000
         * (?????? ??????)
         * ????????? ????????????: -100000
         * ?????????(?????????, ????????????, ??????): -200000
         * ?????? ????????????: -300000
         * (?????? ??????)
         */

        val zindex = when(tagName){"???????????????" -> 160000  "???????????????" -> -50000 else -> 150000 }

        //logUtil.d("tagName = $tagName, zindex = $zindex")
        when (tagName) {
            "???????????????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsEditCadastralOverlayArr, null) }
            "???????????????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsCadastralOverlayArr, null) }
            "????????????(?????????)" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsBsnAreaOverlayArr, null) }
            "??????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsLadOverlayArr, null) }
            "??????????????????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsRealLadOverlayArr, null) }
            "?????????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsThingOverlayArr, jsonArr) }
            "??????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsTombOverlayArr, null) }
            "??????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsFarmOverlayArr, null) }
            "?????????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsResidntOverlayArr, null) }
            "??????" -> { setDrawPolygonOptions(latLngArr, polyColor, strokeColor, zindex, tagName, wfsBsnOverlayArr, null) }
        }
    }

    /**
     * Geoserver WFS ???????????? ????????? ??????
     * @param latLngArr
     * @param polyColor
     * @param strokeColor
     * @param getZindex
     * @param tagName
     * @param setLayerPolygonArr -> ?????? array??? ????????? ????????? ??????
     * @param jsonArr -> WFS data Arr
     */

    private fun setDrawPolygonOptions(latLngArr: MutableList<ArrayList<LatLng>>, polyColor:Int, strokeColor: Int, getZindex: Int, tagName: String, setLayerPolygonArr: MutableList<PolygonOverlay>, jsonArr: List<JsonElement>?){


        latLngArr.forEachIndexed { latlngIndex, it ->
            when (tagName) {

                /**
                 * @description WFS ????????? ?????? ?????????
                 */

                "???????????????", "???????????????" -> {
                    val drawPolygonOverlay  = PolygonOverlay()
                    drawPolygonOverlay.apply {
                        coords = it
                        color = polyColor
                        outlineWidth = 3
                        outlineColor = strokeColor
                        tag = tagName
                        globalZIndex = getZindex
                        map = naverMap
                    }
                    setLayerPolygonArr.add(drawPolygonOverlay)
                    try {
                        setLayerPolygonArr.forEachIndexed { index, arrData ->
                            arrData.setOnClickListener {
                                logUtil.d("$tagName ??????")

                                val latLngArr = mutableListOf<LatLng>()

                                LandInfoObject.selectLandPolygonArr = latLngArr

                                val getJsonArr:JsonArray = when(tagName) {
                                    "???????????????" -> LandInfoObject.ladCadastralJsonArray
                                    "???????????????" -> LandInfoObject.ladEditCadastralJsonArray
                                    else -> throw IllegalAccessException ("??????????????? ??? ?????????????????? ?????? Data")
                                }

                                try {

                                    var getJibun: String
                                    var getPnuCode: String? = null

                                    getJsonArr.forEachIndexed { idx, it ->
                                        if(index == idx){
                                            logUtil.d("????????? $tagName info -> $it}")

                                            if(tagName == "???????????????"){
                                                getPnuCode = with(it.asJsonObject.get("properties")) { asJsonObject.get("pnu").asString }

                                                logUtil.d("$tagName PNU CODE -> $getPnuCode")

                                                val isMountainCheck = when(getPnuCode!!.substring(10, 11)){
                                                    "2" -> "???"
                                                    else -> ""
                                                }

                                                getJibun = with(it.asJsonObject.get("properties")) {
                                                    "${asJsonObject.get("name").asString} $isMountainCheck${lastCommaRemove(asJsonObject.get("jibun").asString)}"
                                                }
                                            } else {
                                                getJibun = lastCommaRemove(it.asJsonObject.get("properties").asJsonObject.get("JIBUN").asString)
                                            }

                                            (it.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray.get(0).asJsonArray.get(0) as JsonArray).forEach {
                                                logUtil.d(it.toString())
                                                val replaceStr = it.toString().replace("[", "").replace("]", "")
                                                latLngArr.add(LatLng(replaceStr.split(",")[1].toDouble(), replaceStr.split(",")[0].toDouble()))
                                            }

                                            val getCoord = findPolygonCenter(latLngArr)

                                            highlightPolygonArr.forEach { it.map = null } // ??????????????? ??????????????? ?????????

                                            val hilightPoly = PolygonOverlay()
                                            hilightPoly.apply {
                                                coords = latLngArr
                                                globalZIndex  = 160000
                                                color = setObjectColor(context!!, R.color.red, 20)
                                                outlineWidth = 5
                                                outlineColor = setObjectColor(context!!, R.color.red, 255)
                                                map = naverMap
                                            }

                                            highlightPolygonArr.add(hilightPoly)

                                            logUtil.d("getCoord -> $getCoord")
                                            logUtil.d("getJibun -> $getJibun")

                                            val tempLegaldongCode = getPnuCode!!.substring(0,10)

                                            when(tagName){
                                                "???????????????" -> {
                                                    ThingWtnObject.naverGeoAddressName = it.asJsonObject.get("properties").asJsonObject.get("name").asString
                                                    ThingWtnObject.naverGeoAddress = lastCommaRemove(it.asJsonObject.get("properties").asJsonObject.get("jibun").asString)
                                                }
                                                else -> {
                                                    ThingWtnObject.naverGeoAddressName = it.asJsonObject.get("properties").asJsonObject.get("ADDR").asString
                                                    ThingWtnObject.naverGeoAddress = lastCommaRemove(it.asJsonObject.get("properties").asJsonObject.get("JIBUN").asString)
                                                }
                                            }

                                            ThingWtnObject.naverLegaldongCode = tempLegaldongCode
                                            val naverGeoAddressName = ThingWtnObject.naverGeoAddressName.toString()
                                            val naverGeoAddress = ThingWtnObject.naverGeoAddress.toString()

                                            activity!!.runOnUiThread {
                                                when (Constants.BIZ_SUBCATEGORY_KEY) {

                                                    BizEnum.LAD -> {
                                                        // ?????????????????? ??????
                                                        val landSearchUrl = context!!.resources.getString(R.string.mobile_url) + "landSearch"

                                                        val landSearchMap = HashMap<String, String>()
                                                        landSearchMap["saupCode"] = PreferenceUtil.getString(context!!,"saupCode", "")
                                                        landSearchMap["legaldongCode"] = tempLegaldongCode
                                                        landSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(landSearchMap, progressDialog, landSearchUrl, object : Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                progressDialog?.dismiss()
                                                                logUtil.d("fail")
                                                            }

                                                            override fun onResponse(call: Call, response: Response) {

                                                                val responseString = response.body!!.string()
                                                                logUtil.d("landSearch response --------------> $responseString")

                                                                //httpResultToJsonObject(responseString).asJsonObject.get("list").asJsonObject.get("realLandInfo").asJsonArray.size()

                                                                val convertJSONData = httpResultToJsonObject(responseString)?.asJsonObject?.get("list")
                                                                val realndInfoDataLength = convertJSONData?.asJsonObject?.get("realLandInfo")?.asJsonArray?.size()
                                                                val landInfo = convertJSONData?.asJsonObject?.get("LandInfo")
                                                                if(!landInfo!!.isJsonNull) {
                                                                    logUtil.d(convertJSONData.toString())
                                                                    logUtil.d(realndInfoDataLength.toString())
                                                                    logUtil.d(landInfo.toString())

                                                                    LandInfoObject.wtnCode = landInfo.asJsonObject?.get("ladWtnCode")!!.asString
                                                                    LandInfoObject.realLandInfoLength = realndInfoDataLength!!
                                                                    activity!!.runOnUiThread {
                                                                        setMarker(getCoord.latitude, getCoord.longitude, "????????????")
                                                                        setNaverMapContextPopup(activity!!, naverGeoAddressName, naverGeoAddress, tempLegaldongCode, "", false, null)
                                                                    }
                                                                } else {
                                                                    activity!!.runOnUiThread {
                                                                        toastUtil.msg_error(
                                                                            "?????? ????????? ?????? ?????? ???????????????. ?????? ??? ?????? ????????? ????????? ???????????????",
                                                                            1000
                                                                        )
                                                                    }
                                                                }

                                                                progressDialog?.dismiss()

                                                            }

                                                        })


                                                    }

                                                    BizEnum.THING -> {
                                                        logUtil.d("naver map click THING-----------------------------")

                                                        val thingSearchMap = HashMap<String, String>()
                                                        val thingSearchUrl = context!!.resources.getString(R.string.mobile_url) + "ThingSearch"
                                                        val thingLandConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"


                                                        thingSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        thingSearchMap["legaldongCode"] = tempLegaldongCode
                                                        thingSearchMap["incrprLnm"] = naverGeoAddress

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingLandConfirmUrl, object : Callback {
                                                            override fun onFailure(call: Call, e: IOException) = logUtil.d("fail")
                                                            override fun onResponse(call: Call, response: Response) {
                                                                val responseString = response.body!!.string()


                                                                logUtil.d("ThingLandConfirm response --------------> $responseString")

                                                                val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                val messageNum = messageJSON.getString("messageNum")
                                                                val message = messageJSON.getString("message")

                                                                progressDialog?.dismiss()
                                                                if (messageNum.equals("-1")) {
                                                                    activity!!.runOnUiThread { toastUtil.msg_error(message.toString(), 500) }
                                                                } else {
                                                                    HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingSearchUrl,
                                                                        object : Callback {
                                                                            override fun onFailure(call: Call, e: IOException) {
                                                                                logUtil.d("fail")
                                                                                progressDialog?.dismiss()
                                                                            }
                                                                            override fun onResponse(call: Call,response: Response) {
                                                                                val responseString = response.body!!.string()

                                                                                logUtil.d("thingSearch response ------------------> $responseString")
                                                                                val thingDataJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                val noSkitchThingData = thingDataJSON.getJSONArray("ThingInfo") as JSONArray

                                                                                //ThingWtnObject.thingInfo = noSkitchThingData.get(0) as JSONObject

                                                                                progressDialog?.dismiss()
                                                                                setNaverMapContextPopup(activity!!,
                                                                                    naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                            }
                                                                        }
                                                                    )
                                                                }

                                                            }

                                                        }
                                                        )
                                                    }
                                                    BizEnum.TOMB -> { //??????
                                                        logUtil.d("naver Map Click TOME -----------------------------------<><><><")

                                                        setMarker(getCoord.latitude, getCoord.longitude,"????????????")

                                                        val tombSearchMap = HashMap<String, String>()
                                                        val tombSearchUrl = context!!.resources.getString(R.string.mobile_url) + "tombSearch"
                                                        val tombladConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                                                        tombSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        tombSearchMap["incrprLnm"] = naverGeoAddress
                                                        tombSearchMap["legaldongCode"] = tempLegaldongCode

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
                                                            tombladConfirmUrl, object: Callback {
                                                                override fun onFailure(call: Call, e: IOException) {

                                                                    logUtil.d("fail")
                                                                    progressDialog?.dismiss()
                                                                }

                                                                override fun onResponse(call: Call, response: Response) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("TombLandConfirm response ------------------> $responseString")

                                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject

                                                                    val messageNum = messageJSON.getString("messageNum")
                                                                    val message = messageJSON.getString("message")

                                                                    progressDialog?.dismiss()

                                                                    if(messageNum.equals("-1")) {
                                                                        activity!!.runOnUiThread { toastUtil.msg_error(message.toString(), 500) }
                                                                    } else {
                                                                        // ?????? ?????? ?????? ?????? ?????????
                                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
                                                                            tombSearchUrl, object: Callback {
                                                                                override fun onFailure(call: Call, e: IOException) {
                                                                                    logUtil.d("fail")
                                                                                    progressDialog?.dismiss()
                                                                                }

                                                                                override fun onResponse(call: Call, response: Response) {
                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("thingSearch Tomb response -------------------> $responseString")

                                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                                                    progressDialog?.dismiss()
                                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)

                                                                                }

                                                                            })
                                                                    }

                                                                }

                                                            })
                                                    }
                                                    BizEnum.MINRGT -> { //?????????
                                                        setMarker(getCoord.latitude, getCoord.longitude,"???????????????")

                                                        logUtil.d("naver map click THING MNIDST")

                                                        val thingSearchMap = HashMap<String, String>()
                                                        val thingSearchUrl = context!!.resources.getString(R.string.mobile_url) + "MnidstSearch"
                                                        val thingLandConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                                                        thingSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        thingSearchMap["incrprLnm"] = naverGeoAddress
                                                        thingSearchMap["legaldongCode"] = tempLegaldongCode


                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,
                                                            thingLandConfirmUrl, object: Callback{
                                                                override fun onFailure(call: Call, e: IOException) {
                                                                    progressDialog?.dismiss()
                                                                    logUtil.d("fail")
                                                                }

                                                                override fun onResponse(call: Call, response: Response) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("ThingLandConfirm response ------------------> $responseString")

                                                                    val messageJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                    val messageNum = messageJson.getString("messageNum")
                                                                    val message = messageJson.getString("message")

                                                                    progressDialog?.dismiss()
                                                                    if(messageNum.equals("-1")) {
                                                                        activity!!.runOnUiThread {
                                                                            toastUtil.msg_error(message.toString(), 500)
                                                                        }
                                                                    } else {
                                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,thingSearchUrl,
                                                                            object: Callback{
                                                                                override fun onFailure(call: Call, e: IOException) {
                                                                                    progressDialog?.dismiss()
                                                                                    logUtil.d("fail")
                                                                                }

                                                                                override fun onResponse(call: Call, response: Response) {
                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("thingSearch response ------------------> $responseString")

                                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch")

                                                                                    progressDialog?.dismiss()

                                                                                    setNaverMapContextPopup(activity!!,
                                                                                        naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                                }

                                                                            })
                                                                    }
                                                                }

                                                            })
                                                    }
                                                    BizEnum.FYHTS -> { //?????????
                                                        setMarker(getCoord.latitude, getCoord.longitude, "???????????????")

                                                        logUtil.d("naver map click THING FSHR")

                                                        val fyhtsSearchUrl = context!!.resources.getString(R.string.mobile_url) + "fyhtsSearch"
                                                        val fyhtsSearchMap = HashMap<String,String>()

                                                        ThingFyhtsObject.legaldongCl = getPnuCode!!.substring(0,10)
                                                        ThingFyhtsObject.legaldongNm = getJibun

                                                        fyhtsSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        fyhtsSearchMap["legaldongCode"] = ThingFyhtsObject.legaldongCl

                                                        HttpUtil.getInstance(context!!)
                                                            .callerUrlInfoPostWebServer(fyhtsSearchMap, progressDialog, fyhtsSearchUrl,
                                                                object: Callback {
                                                                    override fun onFailure(call: Call, e: IOException) {
                                                                        logUtil.d("fail")
                                                                        progressDialog?.dismiss()
                                                                    }

                                                                    override fun onResponse(call: Call, response: Response) {
                                                                        val responseString = response.body!!.string()

                                                                        logUtil.d("thingSearch FYHTS response --------------------------->$responseString")

                                                                        val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                        val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                                        progressDialog?.dismiss()
                                                                        setNaverMapContextPopup(activity!!,
                                                                            naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                    }

                                                                })

                                                    }
                                                    BizEnum.BSN -> { //??????
                                                        logUtil.d("naver Map Click BSN -------------------------<><><><><>")

                                                        setMarker(getCoord.latitude, getCoord.longitude, "????????????")

                                                        val bsnSearchMap = HashMap<String, String>()
                                                        val bsnSearchUrl = context!!.resources.getString(R.string.mobile_url) + "bsnSearch"
                                                        val bsnLadConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                                                        bsnSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        bsnSearchMap["incrprLnm"] = naverGeoAddress
                                                        bsnSearchMap["legaldongCode"] = tempLegaldongCode

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnLadConfirmUrl,
                                                            object: Callback {
                                                                override fun onFailure(call: Call, e: IOException) {
                                                                    logUtil.d("fail")
                                                                    progressDialog?.dismiss()
                                                                }

                                                                override fun onResponse(call: Call, response: Response) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("BsnLandConfirm response ------------------> $responseString")

                                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                    val messageNum = messageJSON.getString("messageNum")
                                                                    val message = messageJSON.getString("message")

                                                                    progressDialog?.dismiss()

                                                                    if(messageNum.equals("-1")) {
                                                                        activity!!.runOnUiThread {
                                                                            toastUtil.msg_error(message.toString(), 500)
                                                                        }
                                                                    } else {
                                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnSearchUrl,
                                                                            object: Callback {
                                                                                override fun onFailure(call: Call, e: IOException) {
                                                                                    logUtil.e("fail")
                                                                                    progressDialog?.dismiss()
                                                                                }

                                                                                override fun onResponse(call: Call, response: Response) {
                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("thingSearch BSN response --------------------------->$responseString")

                                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                                                    progressDialog?.dismiss()
                                                                                    setNaverMapContextPopup(activity!!,
                                                                                        naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                                }

                                                                            })
                                                                    }


                                                                }

                                                            })

                                                    }
                                                    //??????
                                                    BizEnum.FARM -> {
                                                        logUtil.d("naver Map Click FARM ---------------------- <><><><><>")

                                                        setMarker(getCoord.latitude, getCoord.longitude, "????????????")

                                                        val farmSearchMap = HashMap<String, String>()
                                                        val farmSearchUrl = context!!.resources.getString(R.string.mobile_url) + "farmSearch"
                                                        val farmLadConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                                                        farmSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        farmSearchMap["incrprLnm"] = naverGeoAddress
                                                        farmSearchMap["legaldongCode"] = tempLegaldongCode

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmLadConfirmUrl,
                                                            object: Callback {
                                                                override fun onFailure(call: Call, e: IOException) {
                                                                    logUtil.d("fail")
                                                                    progressDialog?.dismiss()
                                                                }

                                                                override fun onResponse(call: Call, response: Response) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("FarmLandConfirm response ---------------> $responseString")

                                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                    val messageNum = messageJSON.getString("messageNum")
                                                                    val message = messageJSON.getString("message")

                                                                    progressDialog?.dismiss()

                                                                    if(messageNum.equals("-1")) {
                                                                        activity!!.runOnUiThread {
                                                                            toastUtil.msg_error(message.toString(), 500)
                                                                        }
                                                                    } else {
                                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmSearchUrl,
                                                                            object: Callback {
                                                                                override fun onFailure(call: Call, e: IOException) {

                                                                                    logUtil.e("fail")
                                                                                    progressDialog?.dismiss()
                                                                                }

                                                                                override fun onResponse(call: Call, response: Response) {

                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("thingSearch FARM response ------------------> $responseString")

                                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch")

                                                                                    progressDialog?.dismiss()
                                                                                    setNaverMapContextPopup(activity!!,
                                                                                        naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                                }

                                                                            })
                                                                    }

                                                                }

                                                            })


                                                    }
                                                    //?????????
                                                    BizEnum.RESIDNT -> {
                                                        logUtil.d("naver Map Click RESIDNT ------------------------ <><><><><><")

                                                        setMarker(getCoord.latitude, getCoord.longitude, "????????? ??????")

                                                        val residntSearchMap = HashMap<String, String>()
                                                        val residntSearchUrl = context!!.resources.getString(R.string.mobile_url) + "residntSearch"
                                                        val residntConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                                                        residntSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                                        residntSearchMap["incrprLnm"] = naverGeoAddress
                                                        residntSearchMap["legaldongCode"] = tempLegaldongCode

                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntConfirmUrl,
                                                            object: Callback {
                                                                override fun onFailure(call: Call, e: IOException) {
                                                                    logUtil.d("fail")
                                                                    progressDialog?.dismiss()

                                                                }

                                                                override fun onResponse(call: Call, response: Response) {
                                                                    val responseString = response.body!!.string()

                                                                    logUtil.d("residntLandConfirm response ------------------> $responseString")

                                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                    val messageNum = messageJSON.getString("messageNum")
                                                                    val message = messageJSON.getString("message")

                                                                    progressDialog?.dismiss()

                                                                    if(messageNum.equals("-1")) {
                                                                        activity!!.runOnUiThread {
                                                                            toastUtil.msg_error(message.toString(), 500)
                                                                        }
                                                                    } else {
                                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntSearchUrl,
                                                                            object: Callback {
                                                                                override fun onFailure(call: Call, e: IOException) {
                                                                                    logUtil.e("fail")
                                                                                    progressDialog?.dismiss()
                                                                                }

                                                                                override fun onResponse(call: Call,response: Response) {
                                                                                    val responseString = response.body!!.string()

                                                                                    logUtil.d("thingSearch RESIDNT response ------------------> $responseString")

                                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch")

                                                                                    progressDialog?.dismiss()
                                                                                    setNaverMapContextPopup(activity!!,
                                                                                        naverGeoAddressName, naverGeoAddress, tempLegaldongCode, noSkitchThingData.toString(), false, null)
                                                                                }

                                                                            })
                                                                    }

                                                                }

                                                            })


                                                    }
                                                    BizEnum.REST_LAD -> {
                                                        logUtil.d("naver Map Cliock REST_LAD----------------------<><><><")

                                                        setMarker(getCoord.latitude, getCoord.longitude, "????????? ??????")


                                                        logUtil.d("--------------------------------restData -----------------------$restData")
                                                        val restDataJson = JSONObject(restData!!)
                                                        val ladLegaldongCode = restDataJson.getString("legaldongCode")
                                                        val ladLegaldongNm = restDataJson.getString("legaldongNm")
                                                        val ladIncrprLnm = restDataJson.getString("incrprLnm")


                                                        if(tempLegaldongCode.equals(ladLegaldongCode) && naverGeoAddress.equals(ladIncrprLnm)) {
                                                            setNaverMapContextPopup(activity!!,
                                                                naverGeoAddressName, naverGeoAddress, tempLegaldongCode, null, false, null)
                                                        } else {
                                                            activity?.runOnUiThread {
                                                                toastUtil.msg_info("???????????? ????????? ????????? ????????? ????????????.", 500)
                                                            }
                                                        }



//                                                        val restLadSearchMap = HashMap<String, String>()
//                                                        val restLadSearchUrl = context!!.resources.getString(R.string.mobile_url) + "restLadSearch"
//
//                                                        restLadSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
//                                                        restLadSearchMap["incrprLnm"] = naverGeoAddress
//                                                        restLadSearchMap["legaldongCode"] = tempLegaldongCode
//                                                        setNaverMapContextPopup(activity!!,
//                                                            naverGeoAddressName, naverGeoAddress, tempLegaldongCode, null, false, null)
                                                    }
                                                    BizEnum.REST_THING -> {
                                                        logUtil.d("naver Map Cliock REST_THING----------------------<><><><")
                                                        
                                                        setMarker(getCoord.latitude, getCoord.longitude, "???????????? ??????")
                                                        
                                                        val restDataJson = JSONObject(restData!!)
                                                        val thingLegaldongCode = restDataJson.getString("legaldongCode")
                                                        val thingLegaldongNm = restDataJson.getString("legaldongNm")
                                                        val thingIncrprLnm = restDataJson.getString("incrprLnm")

                                                        if(tempLegaldongCode.equals(thingLegaldongCode) && naverGeoAddress.equals(thingIncrprLnm)) {
                                                            setNaverMapContextPopup(activity!!,
                                                                naverGeoAddressName, naverGeoAddress, tempLegaldongCode, null, false, null)
                                                        } else {
                                                            activity?.runOnUiThread {
                                                                toastUtil.msg_info("???????????? ????????? ???????????? ????????? ????????????.", 500)
                                                            }
                                                        }
                                                    }


                                                    else -> {
//                                                        setMarker(getCoord.latitude, getCoord.longitude,"????????????")
//                                                        setNaverMapContextPopup(activity!!,
//                                                            naverGeoAddressName, naverGeoAddress, "", false, null)
                                                    }

                                                }

                                            }

                                        }
                                    }
                                } catch (e: Exception) {
                                    logUtil.d(e.toString())
                                }

                                //logUtil.d(findPolygonCenter(arrData.coords).toString())
                                true
                            }
                        }
                    } catch (e: Exception) {
                        throw IllegalAccessException (e.toString())
                    }
                }

                "??????" -> {
                    val drawPolygonOverlay  = PolygonOverlay()
                    drawPolygonOverlay.apply {
                        coords = it
                        color = polyColor
                        outlineWidth = 3
                        outlineColor = strokeColor
                        tag = tagName
                        globalZIndex = getZindex
                        map = naverMap
                    }
                    setLayerPolygonArr.add(drawPolygonOverlay)
                    setLayerPolygonArr.forEachIndexed { index, arrData ->
                        arrData.setOnClickListener {

                            logUtil.d("WFS Layer [$index] [$tagName]") // Layer Index ??????

                            try{
                             LandInfoObject.getSameWtnCodeJsonArray.forEachIndexed { idx, it ->

                                // index??? ????????? ????????? ?????? ????????? ????????????.
                                if(index == idx){
                                    logUtil.d("????????? ????????? ?????? ????????? -> $it}")

                                    LandInfoObject.getSameWtnCode = it.asJsonObject.get("id").asString.split(".")[1]
                                    val incrprLnm = it.asJsonObject.get("properties").asJsonObject.get("INCRPR_LNM").asString ?: ""
                                    val legaldongCode = it.asJsonObject.get("properties").asJsonObject.get("LEGALDONG_CODE").asString ?: ""

//                                    LandInfoObject.wtnCodeBgnnLnm = bgnnLnm
                                    LandInfoObject.wtnCodeIncrprLnm = incrprLnm
                                    LandInfoObject.wtnCodeLegaldongCode = legaldongCode

                                    // 19?????? ???????????? ????????? ?????? ??? ?????? ?????? ??????
                                    if(getNaverMapZoom() > 17){

                                        getActivity().callerContextPopupFunc(null, null, incrprLnm, 0, legaldongCode) // ?????? ????????????

                                        getActivity().ladRealLayerSwitch.isChecked = true
                                        getActivity().isLadRealLayerChecked = true
                                        getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, "??????????????????")

                                    } else {
                                        activity?.runOnUiThread {
                                            toastUtil.msg_info("19?????? ???????????? ????????? ??? ??? ????????????.", 500)
                                        }
                                    }
                                }
                            }

                            }catch (e:Exception){
                                logUtil.d(e.toString())
                            }

                            true
                        }
                    }
                }

                "?????????" -> {
                   var pointYn = jsonArr!![latlngIndex].asJsonObject.get("properties").asJsonObject.get("MO_POINT_YN")

                    if(pointYn.asString.equals("2")) {
                        val drawPolygonOverlay = PolygonOverlay()
                        drawPolygonOverlay.apply {
                            coords = it
                            color = polyColor
                            outlineWidth = 3
                            outlineColor = strokeColor
                            tag = tagName
                            globalZIndex = getZindex
                            map = naverMap
                        }

                        wfsThingOverlayArr.add(drawPolygonOverlay)

                    }

                }

                "??????" -> {
                    val drawPolygonOverlay  = PolygonOverlay()
                    drawPolygonOverlay.apply {
                        coords = it
                        color = polyColor
                        outlineWidth = 3
                        outlineColor = strokeColor
                        tag = tagName
                        globalZIndex = getZindex
                        map = naverMap
                    }
                    setLayerPolygonArr.add(drawPolygonOverlay)
                    setLayerPolygonArr.forEachIndexed { index, arrData ->
                        arrData.setOnClickListener {

                            logUtil.d("WFS Layer [$index] [$tagName]") // Layer Index ??????
                            try{

                                ThingFyhtsObject.getSameWtnCodeJsonArray?.forEachIndexed { idx, _ ->

                                    //index??? ????????? ????????? ?????? ????????? ????????????.
                                    if(index == idx){
                                        logUtil.d("????????? ????????? ?????? ????????? -> $it}")

                                    }
                                }

                            }catch (e:Exception){
                                logUtil.d(e.toString())
                            }

                            true
                        }
                    }
                }
                else -> {
                    val drawPolygonOverlay = PolygonOverlay()
                        drawPolygonOverlay.apply {
                            coords = it
                            color = polyColor
                            outlineWidth = 3
                            outlineColor = strokeColor
                            tag = tagName
                            globalZIndex = getZindex
                            map = naverMap
                        }
                    setLayerPolygonArr.add(drawPolygonOverlay)
                }

            }

        }
        progressDialog?.dismiss()
        //logUtil.d("$tagName ????????? Arr size -> ${setArr.size}")
    }

    /**
     * WFS (???????????????) ????????? ?????? ??????
     * @param resultArr
     * @param infoWindowArr
     */

    fun setCadastralLayerInfo(resultArr: MutableList<String>, infoWindowArr: MutableList<InfoWindow>) {

        infoWindowArr.forEach { it.map = null }
        infoWindowArr.clear()

        try {
            /* ??????????????? ?????? loop??? ?????? Map??? ?????????. */
            for (i in 0 until resultArr.size) {
                val infoWindow = InfoWindow()
                var infoView: InfoView?
                infoView = InfoView(context!!, null, R.layout.inlcude_cadastral_info_view)
                infoView.setText(resultArr[i], "cadastral")
                infoWindow.adapter = object : InfoWindow.ViewAdapter() { override fun getView(p0: InfoWindow): View = infoView }
                infoWindow.position = findPolygonCenter(resultLatLngArr[i])
                infoWindowArr.add(infoWindow)
            }

            logUtil.d("cadastralInfoViewArr Size -> ${infoWindowArr.size}")

            if( getNaverMapZoom() > 17) {
                infoWindowArr.forEach {
                    it.map = naverMap
                }
            }

        } catch (e: Exception) {
            logUtil.d(e.toString())
        }
    }

    /**
     * WFS (????????????, ????????????) ????????? ?????? ??????
     * @param resultArr
     * @param infoWindowArr
     */

    fun setWtncclLayerInfo(resultArr: MutableList<String>, infoWindowArr: MutableList<InfoWindow>, latLngArr: MutableList<ArrayList<LatLng>>) {

        infoWindowArr.forEach { it.map = null }
        infoWindowArr.clear()

        try {

            /* ??????????????? ?????? loop??? ?????? Map??? ?????????. */
            for (i in 0 until resultArr.size) {
                val infoWindow = InfoWindow()
                var infoView: InfoView?
                infoView = InfoView(context!!, null, R.layout.include_wtncc_info_view)
                infoView.setText(resultArr[i], "wtncc")
                infoWindow.adapter = object : InfoWindow.ViewAdapter() { override fun getView(p0: InfoWindow): View = infoView }
                infoWindow.position = findPolygonCenter(latLngArr[i])
//                infoWindow.offsetX = -100
                infoWindowArr.add(infoWindow)
            }

            logUtil.d("wtnccInfoViewArr Size -> ${infoWindowArr.size}")

            infoWindowArr.forEach { it.map = naverMap }

        } catch (e: Exception) {
            throw IllegalAccessException (e.toString())
        }
    }
    fun setWtncclLayerInfoPoint(jsonArr: List<JsonElement>?, resultArr: MutableList<String>, infoWindowArr: MutableList<InfoWindow>, latLngArr: MutableList<ArrayList<LatLng>>) {

        infoWindowArr.forEach { it.map = null }
        infoWindowArr.clear()

        try {

            /* ??????????????? ?????? loop??? ?????? Map??? ?????????. */
            for (i in 0 until resultArr.size) {
                var pointYn = jsonArr!![i].asJsonObject.get("properties").asJsonObject.get("MO_POINT_YN")

                logUtil.d("pointYn -> $pointYn")

                if(pointYn.asString.equals("1")) {
                    for(j in 0 until latLngArr[i].size-1) {
                        val infoWindow = InfoWindow()
                        var infoView: InfoView?

                        infoView = InfoView(context!!, null, R.layout.include_wtncc_info_view)
                        infoView.setText(resultArr[i], "wtncc")
                        infoWindow.adapter = object : InfoWindow.ViewAdapter() { override fun getView(p0: InfoWindow): View =
                            infoView as InfoView
                        }
                        //                infoWindow.position = findPolygonCenter(latLngArr[i])
                        infoWindow.position = latLngArr[i][j]
    //                    latitude = 37.74335264
    //                            longitude = 126.70393214
                        infoWindow.offsetX = 0
                        infoWindow.offsetY = 0
                        infoWindowArr.add(infoWindow)
                    }
                } else {
//                    for (i in 0 until resultArr.size-1) {
                        val infoWindow = InfoWindow()
                        var infoView: InfoView?
                        infoView = InfoView(context!!, null, R.layout.include_wtncc_info_view)
                        infoView.setText(resultArr[i], "wtncc")
                        infoWindow.adapter = object : InfoWindow.ViewAdapter() { override fun getView(p0: InfoWindow): View = infoView }
                        infoWindow.position = findPolygonCenter(latLngArr[i])
                        infoWindow.offsetX = 0
                        infoWindow.offsetY = 0
                        infoWindowArr.add(infoWindow)
//                    }
                }

            }

            logUtil.d("wtnccInfoViewArr Size -> ${infoWindowArr.size}")

            infoWindowArr.forEach { it.map = naverMap }

        } catch (e: Exception) {
            throw IllegalAccessException (e.toString())
        }
    }
    /**
     * ???????????? ????????? ?????? (???)
     * @param tagName
     */
    fun setWFSLayerColorStyle(tagName: String): Int{

        /* WFS ????????? ?????? tagName ?????? ?????? ?????? ???????????? ???????????????. */
        var setWFSLayerColor: Int = when (tagName) {
            "????????????(?????????)" -> setObjectColor(context!!, R.color.black, 70)
            "???????????????" -> setObjectColor(context!!, R.color.layer_color_cadastralEdit, 70)
            "??????" -> setObjectColor(context!!, R.color.layer_color_lad, 70)
            "??????????????????" -> setObjectColor(context!!, R.color.layer_color_realLad, 70)
            "?????????" -> setObjectColor(context!!, R.color.layer_color_thing, 70)
            "???????????????" -> setObjectColor(context!!, R.color.sky_blue, 70)
            "??????" -> setObjectColor(context!!, R.color.layer_color_tomb, 70)
            "??????" -> setObjectColor(context!!, R.color.layer_color_farm, 70)
            "?????????" -> setObjectColor(context!!, R.color.layer_color_residnt, 70)
            "??????" -> {
                 when(Constants.BIZ_SUBCATEGORY_KEY){
                    BizEnum.MINRGT -> setObjectColor(context!!, R.color.layer_color_minrgt, 70)
                    BizEnum.FYHTS -> setObjectColor(context!!, R.color.layer_color_fyhts, 70)
                    else -> setObjectColor(context!!, R.color.layer_color_bsn, 70)
                }
            }
            else -> setObjectColor(context!!, R.color.black, 70)
        }

        return setWFSLayerColor
    }

    /**
     * * ???????????? ????????? ?????? (?????????)
     * @param tagName
     */
    fun setWFSLayerColorStrokeStyle(tagName: String): Int {

        /* WFS ????????? ?????? tagName ?????? ?????? ?????? ???????????? ???????????????. */
        var setWFSLayerOutlineStrokeColor: Int = when (tagName) {
            "????????????(?????????)" -> context!!.getColor(R.color.black)
            "???????????????" -> context!!.getColor(R.color.layer_stroke_color_cadastralEdit)
            "??????" -> context!!.getColor(R.color.layer_stroke_color_lad)
            "??????????????????" -> setObjectColor(context!!, R.color.layer_stroke_color_realLad, 255)
            "?????????" -> setObjectColor(context!!, R.color.layer_stroke_color_thing, 255)
            "???????????????" -> setObjectColor(context!!, R.color.sky_blue, 255)
            "??????" -> setObjectColor(context!!, R.color.layer_stroke_color_tomb, 255)
            "??????" -> setObjectColor(context!!, R.color.layer_stroke_color_farm, 255)
            "?????????" -> setObjectColor(context!!, R.color.layer_stroke_color_residnt, 255)
            "??????" -> {
                when(Constants.BIZ_SUBCATEGORY_KEY){
                    BizEnum.MINRGT -> setObjectColor(context!!, R.color.layer_stroke_color_minrgt, 255)
                    BizEnum.FYHTS -> setObjectColor(context!!, R.color.layer_stroke_color_fyhts, 255)
                    else -> setObjectColor(context!!, R.color.layer_stroke_color_bsn, 255)
                }
            }
            else -> setObjectColor(context!!, R.color.black, 255)
        }
        return setWFSLayerOutlineStrokeColor
    }

    /**
     * ???????????? ????????? ??? ?????? (wfs ?????? ????????? ?????? ??????)
     * @param arr -> 1?????? ???????????? ?????? LatLng
     */
    fun findPolygonCenter(arr: MutableList<LatLng>): LatLng {
        val bounds = LatLngBounds.Builder()
        for(i in 0 until arr.size) {
            val point = LatLng(arr[i].latitude, arr[i].longitude)
            bounds.include(point)
        }
//        logUtil.d("findPolygonCenter -> ${bounds.build().center}")
        return bounds.build().center
    }

    /**
     * Geoserver Request Query (key, value) ??????
     * @param type Query Type
     * @param layerName Geoserver ??????????????? ?????????
     */
    private fun setGeoserverRequestQuery(type: String, layerName: String?): MutableMap<String, String> {

        val queryArr = mutableMapOf<String, String>()
        val wfsFilterUrl ="http://www.opengis.net/ogc"
        val wfsFilterEnvelopeUrl ="http://www.opengis.net/gml"

        when (type) {
            "WMS" -> {
                queryArr["service"] = type
                queryArr["version"] ="1.1.0"
                queryArr["request"] ="GetMap"
                queryArr["layers"] = layerName!!
                queryArr["bbox"] = getExtent()
                queryArr["width"] = naverMap.contentWidth.toString()
                queryArr["height"] = naverMap.contentHeight.toString()
                queryArr["format"] ="image/png"
                queryArr["srs"] = CoordinatesEnum.WGS84.value
            }
            "WFS" -> {
                queryArr["SERVICE"] = type
                queryArr["VERSION"] ="1.1.0"
                queryArr["REQUEST"] ="GetFeature"
                queryArr["typename"] = layerName!!
                queryArr["OUTPUTFORMAT"] ="application/json"
                queryArr["SRSNAME"] = CoordinatesEnum.WGS84.value

                /**
                 * @description WFS ????????? ???????????? ??????
                 */

                when(layerName){

                    // ????????????, ????????????

                    /**
                     *  @param SAUP_CODE
                     */

                    GeoserverLayerEnum.TB_LAD_WTN.value, GeoserverLayerEnum.TB_THING_WTN.value,
                    GeoserverLayerEnum.CADASTRAL_EDIT.value, GeoserverLayerEnum.TL_BSNS_AREA.value,
                    GeoserverLayerEnum.TB_LAD_REALNGR.value -> {
                        queryArr["FILTER"] = """(<Filter xmlns="$wfsFilterUrl"><And><PropertyIsEqualTo><PropertyName>SAUP_CODE</PropertyName><Literal>${PreferenceUtil.getString(context!!, "saupCode", "")}</Literal></PropertyIsEqualTo><BBOX><PropertyName>GEOM</PropertyName><Envelope xmlns="$wfsFilterEnvelopeUrl" srsName="${CoordinatesEnum.WGS84.value}"><lowerCorner>${naverMap.coveringBounds.southWest.longitude} ${naverMap.coveringBounds.southWest.latitude}</lowerCorner><upperCorner>${naverMap.coveringBounds.northEast.longitude} ${naverMap.coveringBounds.northEast.latitude}</upperCorner></Envelope></BBOX></And></Filter>)"""
                        logUtil.d(queryArr["FILTER"].toString())
                    }
                    // ??????????????????
//                    GeoserverLayerEnum.TB_LAD_REALNGR.value -> {
//                        //queryArr["FILTER"] = """(<Filter xmlns="$wfsFilterUrl"><PropertyIsEqualTo><PropertyName>SAUP_CODE</PropertyName><Literal>${prefUtil?.getString("saupCode", "")}</Literal></PropertyIsEqualTo></Filter>)"""
//
//                        /**
//                         *  @param LandInfoObject.getSameWtnCode (????????????)
//                         */
//                        queryArr["FILTER"] = """(<Filter xmlns="$wfsFilterUrl"><And><PropertyIsEqualTo><PropertyName>LAD_WTN_CODE</PropertyName><Literal>${LandInfoObject.getSameWtnCode}</Literal></PropertyIsEqualTo></And></Filter>)"""
//                    }

                    // ??? ??? (SAUP_CODE ???????????? ???????????? ?????? ???
                    else -> {
                        queryArr["FILTER"] ="""(<Filter xmlns="$wfsFilterUrl"><BBOX><PropertyName>wkb_geometry</PropertyName><Envelope xmlns="$wfsFilterEnvelopeUrl" srsName="${CoordinatesEnum.WGS84.value}"><lowerCorner>${naverMap.coveringBounds.southWest.longitude} ${naverMap.coveringBounds.southWest.latitude}</lowerCorner><upperCorner>${naverMap.coveringBounds.northEast.longitude} ${naverMap.coveringBounds.northEast.latitude}</upperCorner></Envelope></BBOX></Filter>)"""
                    }

               }
            }
        }

        return queryArr
    }

    /**
     *  WMS Layer ?????????
     */
    fun clearWMS(wmsOverlayArr: MutableList<GroundOverlay>?, tagName: String) {

        if (wmsOverlayArr != null) {
            for (wmsData in wmsOverlayArr) wmsData.map = null
            wmsOverlayArr.clear()
        }
    }

    /**
     *  WFS Layer ?????????
     */
    fun clearWFS(wfsLayerArr: MutableList<PolygonOverlay>?, tagName: String) {

        if (wfsLayerArr != null) {
            for (wfsData in wfsLayerArr) wfsData.map = null
            wfsLayerArr.clear()
        }

        when(tagName){
            "???????????????" -> {

                for(info in cadastralInfoViewArr) info.map = null
                cadastralInfoViewArr.clear()

                // ????????? ??????????????? ??????????????? ?????????
                for(selectPoly in selectCadastralPolygonArr) selectPoly.map = null
                selectCadastralPolygonArr.clear()

                resultLatLngArr.clear()
                resultCadastralLatLngArr.clear()
                resultAddrArr.clear()
            }

            "???????????????" -> resultEditCadastralLatLngArr.clear()

            "????????????(?????????)" -> resultBsnsAreaLatLngArr.clear()

            "??????" -> {
                for(info in wtnccInfoLadViewArr) info.map = null
                wtnccInfoLadViewArr.clear()

                resultLadPropertiesNoArr.clear()
                resultLadPropertiesWtnCodeArr.clear()

                resultLadWtnCodeArr.clear()
                resultLandLatLngArr.clear()
            }
            "??????????????????" -> resultRealLandLatLngArr.clear()

            "?????????" -> {
                for(info in wtnccInfoThingViewArr) info.map = null

                wtnccInfoThingViewArr.clear()

                resultThingPropertiesWtnCodeArr.clear()
                resultThingLatLngArr.clear()
            }

            "??????" -> {
                for(info in wtnccInfoTombmViewArr) info.map = null
                wtnccInfoTombmViewArr.clear()

                resultThingPropertiesWtnCodeArr.clear()
                resultTombLatLngArr.clear()
            }

            "??????" -> {
                for(info in wtnccInfoFarmViewArr) info.map = null
                wtnccInfoFarmViewArr.clear()

                resultThingPropertiesWtnCodeArr.clear()
                resultFarmLatLngArr.clear()
            }

            "?????????" -> {
                for(info in wtnccInfoResidntViewArr) info.map = null
                wtnccInfoResidntViewArr.clear()

                resultThingPropertiesWtnCodeArr.clear()
                resultResidntLatLngArr.clear()
            }

            "??????" -> {
                for(info in wtnccInfoBsnViewArr) info.map = null
                wtnccInfoBsnViewArr.clear()

                resultThingPropertiesWtnCodeArr.clear()
                resultBsnLatLngArr.clear()
            }
        }
    }

    /**
     * Catro?????? ????????? ????????? ?????????
     */

    fun clearCartoPolygon() {
        for (cartoPolygonData in polygonOverlayArr)
            cartoPolygonData.map = null
//        polygonOverlayArr =





//        for (i in polygonOverlayArr.indices) {
//            polygonOverlayArr.removeAt(i)
//        }

        polygonOverlayArr.clear()



    }

    /**
     *  WFS ????????? ??????
     *  @param latLngArr Catro ?????? ????????? Geometry Array
     */

    fun setNaverMapPolygon(latLngArr: MutableList<LatLng>, searchType:BizEnum?) {

        val mapPoly = PolygonOverlay()

        for (i in latLngArr.indices) {
            mapPoly.apply {
                coords = latLngArr
                globalZIndex  = 160000
                color = setObjectColor(context!!, R.color.blue, 20)
                outlineWidth = 5
                outlineColor = setObjectColor(context!!, R.color.blue, 255)
                tag = selectPolygonOverlayArr.size + 1
                map = naverMap
            }
        }

        polygonOverlayArr.add(mapPoly)

        // ????????? ????????? ???????????????
        selectPolygonOverlayArr.add(mapPoly)

        val addAllMapPos = mutableListOf<MapPos>()

        when (searchType) {

            // ??????
            BizEnum.LAD -> {
                LandInfoObject.realLandPolygon = polygonOverlayArr
            }

            // ?????????
            BizEnum.THING -> {
                ThingWtnObject.thingSketchPolygon = polygonOverlayArr
            }

            BizEnum.TOMB -> {
                ThingTombObject.thingTombSketchPolyton = polygonOverlayArr
                // ????????????
            }

            BizEnum.MINRGT -> {
                ThingMinrgtObject.thingMinrgtSketchPolygon = polygonOverlayArr
                // ????????????
            }

            BizEnum.BSN -> {
                ThingBsnObject.thingBsnSketchPolygon = polygonOverlayArr
            }

            BizEnum.FARM -> {
                ThingFarmObject.thingFarmSketchPolygon = polygonOverlayArr

                /**
                 * ????????????????????? ?????? ????????????????????? ?????? ???????????? ??????.
                 */

            }

            BizEnum.RESIDNT -> {
                ThingResidntObject.thingResidntSketchPolygon = polygonOverlayArr
            }

            BizEnum.FYHTS -> {
                ThingFyhtsObject.thingFyhtsSketchPolygon = polygonOverlayArr
            }

            else -> {}
        }
    }

    /**
     * ???????????? ?????? ??????????????? ??????
     */
    fun setWtnncPicMarkers(lat: Double?, lon: Double?, direction: Float?){

        val wtnccPicMarker = Marker()
        val picVectorImage = OverlayImage.fromResource(R.drawable.ic_arrow_direction)

        with(wtnccPicMarker) {
            icon = picVectorImage
            position = LatLng(lat!!, lon!!)
            marker.width = width
            marker.height = height
            angle = direction!!
            minZoom = 12.0
            maxZoom = 20.0
            map = naverMap
        }

        wtnncPicMarkerArr.add(wtnccPicMarker)

    }

    /**
     * ???????????? ?????? ??????????????? ?????? ?????????
     */
    fun clearWtnncMarker(){
        for (data in wtnncPicMarkerArr) data.map = null
        wtnncPicMarkerArr.clear()
    }

    /** [DialogUtil] Listener */
    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {

        logUtil.d("y")

        when(type){
            "?????????????????? ??????" -> {
                toastUtil.msg_info(R.string.landSearchRealChange, 1000)
            }
            "?????????????????? ??????" -> {
                logUtil.d("?????????????????? ?????? ?????? ????????? success")
                logUtil.d(LandInfoObject.searchRealLand?.get(LandInfoObject.landRealArCurPos).toString())

                val url = context!!.resources.getString(R.string.mobile_url) + "delRealLandAr"
                val jObj = JSONObject()
                jObj.put("realLndcgrCode", (LandInfoObject.searchRealLand?.get(LandInfoObject.landRealArCurPos) as JSONObject).get("realLndcgrCode"))

                HttpUtil.getInstance(context!!)
                    .callUrlJsonWebServer(jObj, progressDialog, url,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.dismiss()
                                logUtil.d("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()
                                logUtil.d("?????????????????? ?????? Response -> $responseString")
                                progressDialog?.dismiss()

                                getActivity().runOnUiThread {
                                    toastUtil.msg_success(JSONObject(responseString).get("message").toString(), 500)

                                    LandInfoObject.landSearchRealLngrJsonArray?.remove(LandInfoObject.landRealArCurPos)
                                    LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()

                                    getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, "??????????????????")

                                    clearCartoPolygon()
                                }


                            }
                        })

            }
        }

    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("n")

        val addAllMapPos = mutableListOf<MapPos>()

        when(type){

            "?????????????????? ??????" -> {
                toastUtil.msg_info(R.string.landSearchRealNotChange, 200)


                setNaverMapPolygon(LandInfoObject.selectLandPolygonArr, BizEnum.LAD)

                LandInfoObject.selectLandPolygonArr.forEach {
                    addAllMapPos.add(MapPos(it.longitude, it.latitude))
                }

                LandInfoObject.clickLatLng = addAllMapPos
                val currentArea = (MathUtil.layersForArea(LandInfoObject.clickLatLng, 6371009.0) * 100.0 / 100.0).roundToInt() // ?????????????????? ??????
                LandInfoObject.currentArea = currentArea
                LandSearchFragment(activity, context).landRealAddAll() // ?????? ?????? ?????? add


                //val getIndexLatLng = resultAddrArr.filter { it.contains(LandInfoObject.incrprLnm) }
                //logUtil.d(getIndexLatLng.toString())

//                resultAddrArr.forEachIndexed { idx, it ->
//                    if(it == getIndexLatLng[0]){
//                        setNaverMapPolygon(resultCadastralLatLngArr[idx], BizEnum.LAD)
//                        logUtil.d("????????? ????????? ????????? ??????????????? -> ${resultCadastralLatLngArr[idx]}")
//                        resultCadastralLatLngArr[idx].forEach { addAllMapPos.add(MapPos(it.longitude, it.latitude)) }
//
//                        LandInfoObject.clickLatLng = addAllMapPos
//
//                        val currentArea = (MathUtil.layersForArea(LandInfoObject.clickLatLng, 6371009.0) * 100.0 / 100.0).roundToInt() // ?????????????????? ??????
//                        LandInfoObject.currentArea = currentArea
//                        LandSearchFragment(activity, context).landRealAddAll() // ?????? ?????? ?????? add
//
//                    }
//                }

            }
        }

    }
    /** [DialogUtil] Listener End */

    /**
     *  ????????? Context Popup
     *  @param activity
     *  @param address
     *  @param addressJiben
     *  @param jsonArrayData
     *  @param lotMapCheckable ????????? Check ??????
     *  @param wtnncType ??????????????? ????????? 2Depth??? ?????? ??????
     */
    fun setNaverMapContextPopup(activity: Activity, address: String, addressJiben: String, legaldongCode: String?, jsonArrayData: String?, lotMapCheckable: Boolean, wtnncType:BizEnum?) {

//        if(getActivity().bottompanel.visibility == View.GONE){

        if(getActivity().bottompanel.visibility == View.VISIBLE){
            activity.runOnUiThread {
                getActivity().bottompanel.visibility = View.GONE
            }
        }
        when (Constants.BIZ_SUBCATEGORY_KEY) {

            // TODO: 2021-11-19 ????????? ???????????? ??????

            /**
             * ??????????????? ????????? ??? ?????? ??????
             */

            BizEnum.LOTMAP -> {
                when(lotMapCheckable){

                    false -> {
                        contextDialogItems = mutableListOf("?????? ??????", "????????? ??????", "?????? ??????", "?????? ??????", "????????? ??????", "?????? ??????", "????????? ??????", "????????? ??????")
                        contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "  ?????????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "lotMapClickPopup")
                        (contextPopupFragment as ContextDialogFragment).apply {
                            show(contextPopupFragmentManager!!, "contextPopup")
                            isCancelable = false
                        }
                    }

                    true -> {
                        when(wtnncType){

                            BizEnum.LAD -> {

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.LAD
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")
                                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "  ?????? ??????", contextDialogItems, address, addressJiben,legaldongCode,jsonArrayData, "ladClickPopup")
                                (contextPopupFragment as ContextDialogFragment).apply {
                                    show(contextPopupFragmentManager!!, "contextPopup")
                                    isCancelable = false
                                }

                            }

                            BizEnum.THING -> {

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.THING
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????", "?????? ??????")
                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)

                                    var geomNullCheck = false
                                    for (value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        if (data.getString("geoms").equals("null")) {
                                            geomNullCheck = true
                                        }
                                        contextDialogItems.add(data.getString("thingKnd").toString())

                                    }
                                    activity.runOnUiThread {
//                                            if (thingData.length() > 0 && geomNullCheck) {
//                                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                                            } else {
//                                                toastUtil.msg_info("????????? ?????? ??????", 500)
//                                            }
                                        toastUtil.msg_info("????????? ?????? ??????", 500)

                                    }
                                    contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "thingClickPopup")
                                    (contextPopupFragment as ContextDialogFragment).apply {
                                        show(contextPopupFragmentManager!!, "contextPopup")
                                        isCancelable = false
                                    }
                                }

                            }
                            BizEnum.BSN ->{

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.BSN
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("??????/??????/????????? ?????? ??????", "?????? ??????")
                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for (value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(
                                            data.getString("thingKnd").toString()
                                        )
                                    }
                                    activity.runOnUiThread {
//                                            if (thingData.length() > 0) {
//                                                toastUtil.msg_info(
//                                                    "????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.",
//                                                    500
//                                                )
//                                            } else {
//                                                toastUtil.msg_info("??????/??????/????????? ?????? ??????", 500)
//                                            }
                                        toastUtil.msg_info("??????/??????/????????? ?????? ??????", 500)
                                    }
                                    contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ??????/??????/????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "bsnClickPopup")
                                    contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                                    (contextPopupFragment as ContextDialogFragment).apply {
                                        show(contextPopupFragmentManager!!, "contextPopup")
                                        isCancelable = false
                                    }
                                }
                            }

                            BizEnum.FARM -> {

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.FARM
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")

                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for(value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(data.getString("thingKnd").toString())
                                    }
                                    activity.runOnUiThread {
//                                            if(thingData.length() > 0) {
//                                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ???????????? ????????? ????????????.", 500)
//                                            } else {
//                                                toastUtil.msg_info("?????? ?????? ??????", 500)
//                                            }
                                        toastUtil.msg_info("?????? ?????? ??????", 500)

                                    }
                                }

                                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "farmClickPopup")
                                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                                (contextPopupFragment as ContextDialogFragment).apply {
                                    show(contextPopupFragmentManager!!, "contextPopup")
                                    isCancelable = false
                                }

                            }

                            BizEnum.RESIDNT -> {

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.RESIDNT
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")

                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for(value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(data.getString("thingKnd").toString())
                                    }
                                    activity.runOnUiThread {
//                                            if(thingData.length() > 0) {
//                                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ???????????? ????????? ????????????.", 500)
//                                            } else {
//                                                toastUtil.msg_info("????????? ?????? ??????", 500)
//                                            }
                                        toastUtil.msg_info("????????? ?????? ??????", 500)
                                    }
                                }

                                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "residntClickPopup")
                                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                                (contextPopupFragment as ContextDialogFragment).apply {
                                    show(contextPopupFragmentManager!!, "contextPopup")
                                    isCancelable = false
                                }

                            }

                            BizEnum.TOMB -> {

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.TOMB
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")
                                if (jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for (value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(
                                            data.getString("thingKnd").toString()
                                        )

                                    }
                                    activity.runOnUiThread {
//                                            if (thingData.length() > 0) { toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                                            } else {
//                                                toastUtil.msg_info("?????? ?????? ??????", 500)
//                                            }
                                        toastUtil.msg_info("?????? ?????? ??????", 500)

                                    }
                                }
                            }
                            BizEnum.MINRGT ->{

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.MINRGT
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")
                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for(value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(data.getString("thingKnd").toString())
                                    }
                                    activity.runOnUiThread {
//                                            if(thingData.length() >0) {
//                                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                                            } else {
//
//                                            }
                                        toastUtil.msg_info("????????? ?????? ??????", 500)
                                    }
                                }
                                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "minrgtClickPopup")
                                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                                (contextPopupFragment as ContextDialogFragment).apply {
                                    show(contextPopupFragmentManager!!, "contextPopup")
                                    isCancelable = false
                                }

                            }
                            BizEnum.FYHTS ->{

                                Constants.BIZ_SUBCATEGORY_KEY = BizEnum.FYHTS
                                logUtil.d("????????? ?????? $wtnncType check -> $lotMapCheckable")

                                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")
                                if(jsonArrayData != "") {
                                    val thingData = JSONArray(jsonArrayData)
                                    for(value in 0 until thingData.length()) {
                                        val data = thingData.getJSONObject(value) as JSONObject
                                        contextDialogItems.add(data.getString("thingKnd").toString())
                                    }
                                }

                                activity.runOnUiThread {
                                    toastUtil.msg_info("?????? ?????? ??????", 500)
                                }

                                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "fyhtsClickPopup")
                                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                                (contextPopupFragment as ContextDialogFragment).apply {
                                    show(contextPopupFragmentManager!!, "contextPopup")
                                    isCancelable = false
                                }

                            }
                            else ->{}
                        }
                    }
                }
            }

            /**
             * ???????????? ?????? ?????? ????????? ??????
             */

            BizEnum.LAD -> {
                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "  ?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "ladClickPopup")
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }

            BizEnum.THING -> {
                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????", "?????? ??????")
                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    var geomNullCheck: Boolean = false
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
//                        contextDialogItems.plus(thingData.getJSONObject(value).getString("thingKnd"))
//                        contextDialogItems.plus(thingData.getJSONObject(value).getString("thingKnd").toString())
                        if(data.getString("geoms").equals("null")) {
                            geomNullCheck = true
                        }
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())

                    }
                    activity.runOnUiThread {
                        toastUtil.msg_info("????????? ?????? ??????", 500)

                    }
                }
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "thingClickPopup")
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }

            BizEnum.TOMB -> {
                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")
                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
//                        contextDialogItems.plus(thingData.getJSONObject(value).getString("thingKnd"))
//                        contextDialogItems.plus(thingData.getJSONObject(value).getString("thingKnd").toString())
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())

                    }
                    activity.runOnUiThread {
//                            if(thingData.length() >0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("?????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("?????? ?????? ??????", 500)

                    }
                }
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "tombClickPopup")
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }

            BizEnum.MINRGT -> {
                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")
                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())
                    }
                    activity.runOnUiThread {
//                            if(thingData.length() >0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("????????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("????????? ?????? ??????", 500)
                    }
                }
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "minrgtClickPopup")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }
            BizEnum.FYHTS -> {
                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")
                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())
                    }
                    activity.runOnUiThread {
//                            if(thingData.length() >0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("??????/??????/????????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("?????? ?????? ??????", 500)
                    }

                }

                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "fyhtsClickPopup")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }
            BizEnum.BSN -> {
                contextDialogItems = mutableListOf("??????/??????/????????? ?????? ??????", "?????? ??????")
                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())
                    }
                    activity.runOnUiThread {
//                            if(thingData.length() >0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("??????/??????/????????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("??????/??????/????????? ?????? ??????", 500)
                    }

                }
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "???????????? ??????/??????/????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "bsnClickPopup")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }
            BizEnum.FARM -> {
                contextDialogItems = mutableListOf("?????? ?????? ??????", "?????? ??????")

                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())
                    }
                    activity.runOnUiThread {
//                            if(thingData.length() > 0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ???????????? ????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("?????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("?????? ?????? ??????", 500)

                    }
                }

                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "farmClickPopup")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }
            BizEnum.RESIDNT -> {
                contextDialogItems = mutableListOf("????????? ?????? ??????", "?????? ??????")

                if(jsonArrayData != "") {
                    val thingData = JSONArray(jsonArrayData)
                    for(value in 0 until thingData.length()) {
                        val data = thingData.getJSONObject(value) as JSONObject
                        contextDialogWtnCodeItems.add(data.getString("thingWtnCode").toString())
                        contextDialogItems.add(data.getString("thingKnd").toString())
                    }
                    activity.runOnUiThread {
//                            if(thingData.length() > 0) {
//                                toastUtil.msg_info("????????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ?????? ????????? ???????????? ???????????? ????????? ????????????.", 500)
//                            } else {
//                                toastUtil.msg_info("????????? ?????? ??????", 500)
//                            }
                        toastUtil.msg_info("????????? ?????? ??????", 500)
                    }
                }

                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "residntClickPopup")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }
            // TODO : (??????) ?????????, ?????? ??????
            BizEnum.REST_LAD -> {
                contextDialogItems = mutableListOf("?????? ?????? ?????? ??????")
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "?????? ?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "restLadClickPopup")
                (contextPopupFragment as ContextDialogFragment).show(contextPopupFragmentManager!!, "contextPopup")
            }
            BizEnum.REST_THING -> {
                contextDialogItems = mutableListOf("?????? ?????? ?????? ??????")
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research, "?????? ?????? ??????", contextDialogItems, address, addressJiben, legaldongCode, jsonArrayData, "restThingClickPopup")
                (contextPopupFragment as ContextDialogFragment).show(contextPopupFragmentManager!!, "contextPopup")
            }
            else -> {
                contextDialogItems = mutableListOf("?????? ??????", "?????? ??????")
                contextPopupFragment = ContextDialogFragment(R.drawable.ic_research,"???????????? ??????", contextDialogItems, address, addressJiben, legaldongCode, "","etc")
                contextPopupFragmentManager = (activity as MapActivity).supportFragmentManager
                (contextPopupFragment as ContextDialogFragment).apply {
                    show(contextPopupFragmentManager!!, "contextPopup")
                    isCancelable = false
                }
            }

        }
    }

    @JvmName("getMinPos1")
    fun getMinPos(): MapPos = MapPos(naverMap.coveringBounds.southWest.latitude, naverMap.coveringBounds.southWest.longitude)

    @JvmName("getMaxPos1")
    fun getMaxPos(): MapPos = MapPos(naverMap.coveringBounds.northEast.latitude, naverMap.coveringBounds.northEast.longitude)

    /**
     *  ?????? ?????? ??????
     *  @param lat ??????
     *  @param lon ??????
     *  @param textStr ?????? ?????????
     */
    private fun setMarker(lat: Double, lon: Double, textStr: String) {
        marker.position = LatLng(lat, lon)
        setMarkerStyle(marker, MarkerIcons.BLACK, R.color.royal_blue, Marker.SIZE_AUTO, Marker.SIZE_AUTO)
        setMarkerText(marker, textStr, Align.Top)
    }

    /**
     *  ?????? ?????????
     */
    private fun setMarkerStyle(marker: Marker, icons: OverlayImage, iconColor: Int, width: Int, height: Int): Marker {
        with(marker) {
            icon = icons
            iconTintColor = iconColor
            marker.width = width
            marker.height = height
            minZoom = 12.0
            maxZoom = 20.0
            map = naverMap
        }
        return marker
    }

    /**
     *  ?????? ????????? (?????????)
     */
    private fun setMarkerText(marker: Marker, text: String, aligns: Align): Marker {
        with(marker) {
            captionText = text
            captionMinZoom = 16.0
            captionMaxZoom = 20.0
            setCaptionAligns(aligns)
        }
        return marker
    }

    /**
     *  ??? ?????? ?????? ?????????
     */
    override fun onCameraIdle() {

        naverCameraidleCnt++

        if (naverCameraidleCnt == 1) {
            lon = naverMap.cameraPosition.target.longitude
            lat = naverMap.cameraPosition.target.latitude

            logUtil.d("zoom -> ${getNaverMapZoom()}")

            progressDialog?.dismiss()

            getActivity().cartoMap?.setScreenSync()

            getActivity().btnMapZoom.text = getNaverMapZoom().roundToInt().toString()

            if (getActivity().isSidoLayerChecked && getNaverMapZoom() in 5f..8f) getWMSLayer(GeoserverLayerEnum.SIDO.value) else clearWMS(wmsSidoOverlayArr,"??????") // ??????

            if (getActivity().isSigunguLayerChecked && getNaverMapZoom() in 9f..12f) getWMSLayer(GeoserverLayerEnum.SIGUNGU.value) else clearWMS(wmsSigunguOverlayArr,"?????????") // ?????????

            if (getActivity().isDongLayerChecked && getNaverMapZoom() in 13f..14f) getWMSLayer(GeoserverLayerEnum.EMD.value) else clearWMS(wmsDongOverlayArr,"?????????") // ?????????

            if (getActivity().isLiLayerChecked && getNaverMapZoom() in 15f..21f) getWMSLayer(GeoserverLayerEnum.LI.value) else clearWMS(wmsLiOverlayArr,"?????????") // ?????????

            if (isCadastralVisable && getNaverMapZoom() in 18f..21f) { clearWFS(wfsCadastralOverlayArr, "???????????????"); clearWFS(wfsEditCadastralOverlayArr, "???????????????"); getWFSLayer(GeoserverLayerEnum.CADASTRAL.value, "???????????????"); getWFSLayer(GeoserverLayerEnum.CADASTRAL_EDIT.value, "???????????????") } else { clearWFS(wfsCadastralOverlayArr, "???????????????"); clearWFS(wfsEditCadastralOverlayArr, "???????????????") }

            if (getActivity().isLadLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsLadOverlayArr, "??????"); getWFSLayer(GeoserverLayerEnum.TB_LAD_WTN.value, "??????"); getActivity().ladLayerSwitch.isChecked = true } else clearWFS(wfsLadOverlayArr, "??????")

            if (getActivity().isLadRealLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsRealLadOverlayArr, "??????????????????"); getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, "??????????????????"); getActivity().ladRealLayerSwitch.isChecked = true } else clearWFS(wfsLadOverlayArr, "??????????????????")

            if (getActivity().isThingLayerChecked && getNaverMapZoom() in 18f..21f ){ clearWFS(wfsThingOverlayArr, "?????????"); getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "?????????"); getActivity().thingLayerSwitch.isChecked = true } else clearWFS(wfsThingOverlayArr, "?????????")

            if (getActivity().isTombLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsTombOverlayArr, "??????"); getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????"); getActivity().tombLayerSwitch.isChecked = true } else clearWFS(wfsTombOverlayArr, "??????")

            if (getActivity().isFarmLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsFarmOverlayArr, "??????"); getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????"); getActivity().farmLayerSwitch.isChecked = true } else clearWFS(wfsFarmOverlayArr, "??????")

            if (getActivity().isResidntLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsResidntOverlayArr, "?????????"); getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "?????????"); getActivity().residntLayerSwitch.isChecked = true } else clearWFS(wfsResidntOverlayArr, "?????????")

            if (getActivity().isBsnLayerChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsBsnOverlayArr, "??????"); getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "??????"); getActivity().bsnLayerSwitch.isChecked = true } else clearWFS(wfsBsnOverlayArr, "??????")

            if (getActivity().isCadastralEditLayerpChecked && getNaverMapZoom() in 18f..21f){ clearWFS(wfsEditCadastralOverlayArr, "???????????????"); getWFSLayer(GeoserverLayerEnum.CADASTRAL_EDIT.value, "???????????????"); getActivity().cadstralEditLayerSwitch.isChecked = true } else clearWFS(wfsEditCadastralOverlayArr, "???????????????")

            if (isCadastralVisable && getActivity().isBsnsAreaLayerChecked && getNaverMapZoom() in 13f..21f){ clearWFS(wfsBsnAreaOverlayArr, "????????????(?????????)"); getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, "????????????(?????????)"); getActivity().bsnsAreaLayerSwitch.isChecked = true } else clearWFS(wfsBsnAreaOverlayArr, "????????????(?????????)")

        } else if (naverCameraidleCnt > 1) {
            naverCameraidleCnt = 0
        }
    }

    override fun onCameraChange(reason: Int, animated: Boolean) {
        logUtil.d("moveCamera onCameraChange latitude" + naverMap.cameraPosition.target.latitude)
        logUtil.d("moveCamera onCameraChange longitude" + naverMap.cameraPosition.target.longitude)
    }

    override fun onLocationChange(p0: Location) {
        logUtil.d("??????")
    }

    /**
     *  ??? ?????? ?????? ?????????
     */
    override fun onMapClick(point: PointF, coord: LatLng) {
        marker.map = null

        /**
         * @description ?????? ????????? ????????? ?????? ?????? ?????? ?????? -> ????????? GeoCodeing_gc
         * @url https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc
         * @param coords {??????_??????}
         * @param sourcecrs {?????????}
         * @param orders {??????_??????_??????}
         * @param output {??????_??????}
         */

        logUtil.d("?????? ?????? ?????? ?????? " + coord.longitude +"," + coord.latitude)

        globalMapClickCoord = "${coord.longitude},${coord.latitude}"

        if((activity as MapActivity).supportActionBar?.title == "????????? ??????"){
            Constants.BIZ_SUBCATEGORY_KEY = BizEnum.LOTMAP
        }


        when(Constants.BIZ_SUBCATEGORY_KEY) {
            BizEnum.FYHTS -> {
                logUtil.d("onMapClick------------------------------------------<><><><><><><><><><><><><")

                val naverGeoCodeAdr = StringBuilder(context!!.getString(R.string.naver_geocoding_gc))

                naverGeoCodeAdr.append("?" + URLEncoder.encode("coords","utf-8") +"=" + coord.longitude +"," + coord.latitude)
                naverGeoCodeAdr.append("&" + URLEncoder.encode("sourcecrs","utf-8") +"=" +"epsg:4326")
                naverGeoCodeAdr.append("&" + URLEncoder.encode("orders","utf-8") +"=" +"legalcode,addr,roadaddr") //?????????,??????,?????????
                naverGeoCodeAdr.append("&" + URLEncoder.encode("output","utf-8") +"=" +"json")

                HttpUtil.getInstance(context!!)
                .urlGetNaver(naverGeoCodeAdr.toString(), naverID, naverKey,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logUtil.d("fail")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (getNaverMapZoom() > 17) {
                            val responseString = response.body!!.string()
                            logUtil.d("naverGeoCodeAdr response -> $responseString")

                            val dataJsonObject = JSONObject(responseString)

                            val naverGeoAddressName = StringBuilder((dataJsonObject.getJSONArray("results").get(0)
                                    as JSONObject).getJSONObject("region").getJSONObject("area1").getString("name"))

                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area2").getString("name")}")
                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area3").getString("name")}")
                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area4").getString("name")}")

                            val naverGeoAddressArray = dataJsonObject.getJSONArray("results")
                            var naverGeoAddressMnnm = ""
                            var naverGeoAddressSlno = ""
                            var naverGeoAddress = ""

                            if(naverGeoAddressArray.length() > 1) {
                                //????????? ????????? ??????????????? ??????(??????????????? ????????? ?????????)
                                naverGeoAddressMnnm = (naverGeoAddressArray.get(1) as JSONObject).getJSONObject("land").getString("number1").toString()
                                naverGeoAddressSlno = (naverGeoAddressArray.get(1) as JSONObject).getJSONObject("land").getString("number2").toString()
                                naverGeoAddress = if (naverGeoAddressSlno != "") "$naverGeoAddressMnnm-$naverGeoAddressSlno" else naverGeoAddressMnnm
                                ThingFyhtsObject.legaldongCl = (naverGeoAddressArray.get(0) as JSONObject).getJSONObject("code").getString("id")
                                ThingFyhtsObject.legaldongNm = naverGeoAddressName.toString()

                            } else {
                                naverGeoAddress = "0"

                                ThingFyhtsObject.legaldongCl = (naverGeoAddressArray.get(0) as JSONObject).getJSONObject("code").getString("id")
                                ThingFyhtsObject.legaldongNm = naverGeoAddressName.toString()
                            }


                            activity!!.runOnUiThread {
                                setMarker(coord.latitude, coord.longitude, "???????????????")
                                //
                                logUtil.d("naver map click THING FSHR")

                                val fyhtsSearchUrl = context!!.resources.getString(R.string.mobile_url) + "fyhtsSearch"
                                val fyhtsSearchMap = HashMap<String, String>()

                                fyhtsSearchMap["saupCode"] = PreferenceUtil.getString(context!!, "saupCode", "")
                                fyhtsSearchMap["legaldongCode"] = ThingFyhtsObject.legaldongCl

                                HttpUtil.getInstance(context!!)
                                    .callerUrlInfoPostWebServer(fyhtsSearchMap, progressDialog, fyhtsSearchUrl,
                                        object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                logUtil.d("fail")
                                                progressDialog?.dismiss()
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                val responseString = response.body!!.string()

                                                logUtil.d("thingSearch FYHTS response --------------------------->$responseString")

                                                val thingDataJson =
                                                    JSONObject(responseString).getJSONObject("list") as JSONObject
                                                val noSketchThingData =
                                                    thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                progressDialog?.dismiss()
                                                setNaverMapContextPopup(
                                                    activity!!,
                                                    naverGeoAddressName.toString(),
                                                    naverGeoAddress,
                                                    ThingFyhtsObject.legaldongCl,
                                                    noSketchThingData.toString(),
                                                    false,
                                                    null
                                                )
                                            }

                                        })
                            }
                        }
                    }


                })
            }
        }



//        val naverGeoCodeAdr = StringBuilder(context!!.getString(R.string.naver_geocoding_gc))
//        //naverGeoCodeAdr.append("?" + URLEncoder.encode("query","utf-8") +"= ????????? ????????? ???????????? 348-2")
//
//        naverGeoCodeAdr.append("?" + URLEncoder.encode("coords","utf-8") +"=" + coord.longitude +"," + coord.latitude)
//        naverGeoCodeAdr.append("&" + URLEncoder.encode("sourcecrs","utf-8") +"=" +"epsg:4326")
//        naverGeoCodeAdr.append("&" + URLEncoder.encode("orders","utf-8") +"=" +"legalcode,addr,roadaddr") //?????????,??????,?????????
//        naverGeoCodeAdr.append("&" + URLEncoder.encode("output","utf-8") +"=" +"json")
//
//        logUtil.d("????????? ?????? ?????? ?????? URL -> $naverGeoCodeAdr")
//
//        HttpUtil.getInstance(context!!)
//            .urlGetNaver(naverGeoCodeAdr.toString(), naverID, naverKey,
//                object : Callback {
//                    override fun onFailure(call: Call, e: IOException) = logUtil.d("?????? ?????? ??????")
//                    override fun onResponse(call: Call, response: Response) {
//                        if (getNaverMapZoom() > 17) {
//                            val responseString = response.body!!.string()
//                            logUtil.d("naverGeoCodeAdr response -> $responseString")
//                            val dataJsonObject = JSONObject(responseString)
//
//                            val naverGeoAddressName = StringBuilder((dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area1").getString("name"))
//
//                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area2").getString("name")}")
//                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area3").getString("name")}")
//                            naverGeoAddressName.append(" ${(dataJsonObject.getJSONArray("results").get(0) as JSONObject).getJSONObject("region").getJSONObject("area4").getString("name")}")
//
//                            val naverGeoAddressArray = dataJsonObject.getJSONArray("results")
//                            var naverGeoAddressMnnm = ""
//                            var naverGeoAddressSlno = ""
//                            var naverGeoAddress = ""
//
//                            if(naverGeoAddressArray.length() > 1) {
//                                //????????? ????????? ??????????????? ??????(??????????????? ????????? ?????????)
//                                naverGeoAddressMnnm = (naverGeoAddressArray.get(1) as JSONObject).getJSONObject("land").getString("number1").toString()
//                                naverGeoAddressSlno = (naverGeoAddressArray.get(1) as JSONObject).getJSONObject("land").getString("number2").toString()
//                                naverGeoAddress = if (naverGeoAddressSlno != "") "$naverGeoAddressMnnm-$naverGeoAddressSlno" else naverGeoAddressMnnm
//                                when(Constants.BIZ_SUBCATEGORY_KEY) {
//                                    BizEnum.FYHTS -> {
//                                        ThingFyhtsObject.legaldongCl = (naverGeoAddressArray.get(0) as JSONObject).getJSONObject("code").getString("id")
//                                        ThingFyhtsObject.legaldongNm = naverGeoAddressName.toString()
//                                    }
//                                }
//
//                            } else {
//                                when(Constants.BIZ_SUBCATEGORY_KEY) {
//                                    BizEnum.FYHTS -> {
//                                        naverGeoAddress = "0"
//
//                                        ThingFyhtsObject.legaldongCl = (naverGeoAddressArray.get(0) as JSONObject).getJSONObject("code").getString("id")
//                                        ThingFyhtsObject.legaldongNm = naverGeoAddressName.toString()
//
//
//                                    }
//                                }
//
//                            }
//
//                            logUtil.d("naverGeoCodeAdr response JSON -> $dataJsonObject")
//                            logUtil.d("naverGeoCodeAdr response JSON -> $naverGeoAddress")
//
//                            ThingWtnObject.naverGeoAddressName = naverGeoAddressName.toString()
//                            ThingWtnObject.naverGeoAddress = naverGeoAddress
//
//                            activity!!.runOnUiThread {
//                                when (Constants.BIZ_SUBCATEGORY_KEY) {
//
//                                    BizEnum.LAD -> {
//                                        // ?????????????????? ??????
//                                        val landSearchUrl = context!!.resources.getString(R.string.mobile_url) + "landSearch"
//
//                                        val landSearchMap = HashMap<String, String>()
//                                        landSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        landSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(landSearchMap, progressDialog, landSearchUrl, object : Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    progressDialog?.dismiss()
//                                                    logUtil.d("fail")
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//
//                                                    val responseString = response.body!!.string()
//                                                    logUtil.d("landSearch response --------------> $responseString")
//                                                    progressDialog?.dismiss()
//
//                                                }
//
//                                            })
//
//                                        activity!!.runOnUiThread {
//                                            setMarker(coord.latitude, coord.longitude, "????????????")
//                                            setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, "", false, null)
//                                        }
//                                    }
//
//                                    BizEnum.THING -> {
//                                        logUtil.d("naver map click THING-----------------------------")
//
//                                        val thingSearchMap = HashMap<String, String>()
//                                        val thingSearchUrl = context!!.resources.getString(R.string.mobile_url) + "ThingSearch"
//                                        val thingLandConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//
//                                        thingSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        thingSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingLandConfirmUrl, object : Callback {
//                                                    override fun onFailure(call: Call, e: IOException) = logUtil.d("fail")
//                                                    override fun onResponse(call: Call, response: Response) {
//                                                        val responseString = response.body!!.string()
//
//                                                        logUtil.d("ThingLandConfirm response --------------> $responseString")
//
//                                                        val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                        val messageNum = messageJSON.getString("messageNum")
//                                                        val message = messageJSON.getString("message")
//
//                                                        progressDialog?.dismiss()
//                                                        if (messageNum.equals("-1")) {
//                                                            activity!!.runOnUiThread { toastUtil.msg_error(message.toString(), 500) }
//                                                        } else {
//                                                            HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingSearchUrl,
//                                                                    object : Callback {
//                                                                        override fun onFailure(call: Call, e: IOException) {
//                                                                            logUtil.d("fail")
//                                                                            progressDialog?.dismiss()
//                                                                        }
//                                                                        override fun onResponse(call: Call,response: Response) {
//                                                                            val responseString = response.body!!.string()
//
//                                                                            logUtil.d("thingSearch response ------------------> $responseString")
//                                                                            val thingDataJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                            val noSkitchThingData = thingDataJSON.getJSONArray("ThingInfo") as JSONArray
//
//                                                                            progressDialog?.dismiss()
//                                                                            setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                                        }
//                                                                    }
//                                                            )
//                                                        }
//
//                                                    }
//
//                                                }
//                                            )
//                                    }
//                                    BizEnum.TOMB -> { //??????
//                                        logUtil.d("naver Map Click TOME -----------------------------------<><><><")
//
//                                        setMarker(coord.latitude, coord.longitude,"????????????")
//
//                                        val tombSearchMap = HashMap<String, String>()
//                                        val tombSearchUrl = context!!.resources.getString(R.string.mobile_url) + "tombSearch"
//                                        val tombladConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//                                        tombSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        tombSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
//                                            tombladConfirmUrl, object: Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//
//                                                    logUtil.d("fail")
//                                                    progressDialog?.dismiss()
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("TombLandConfirm response ------------------> $responseString")
//
//                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//
//                                                    val messageNum = messageJSON.getString("messageNum")
//                                                    val message = messageJSON.getString("message")
//
//                                                    progressDialog?.dismiss()
//
//                                                    if(messageNum.equals("-1")) {
//                                                        activity!!.runOnUiThread { toastUtil.msg_error(message.toString(), 500) }
//                                                    } else {
//                                                        // ?????? ?????? ?????? ?????? ?????????
//                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
//                                                            tombSearchUrl, object: Callback {
//                                                                override fun onFailure(call: Call, e: IOException) {
//                                                                    logUtil.d("fail")
//                                                                    progressDialog?.dismiss()
//                                                                }
//
//                                                                override fun onResponse(call: Call, response: Response) {
//                                                                    val responseString = response.body!!.string()
//
//                                                                    logUtil.d("thingSearch Tomb response -------------------> $responseString")
//
//                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray
//
//                                                                    progressDialog?.dismiss()
//                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//
//                                                                }
//
//                                                            })
//                                                    }
//
//                                                }
//
//                                            })
//                                    }
//                                    BizEnum.MINRGT -> { //?????????
//                                        setMarker(coord.latitude, coord.longitude,"???????????????")
//
//                                        logUtil.d("naver map click THING MNIDST")
//
//                                        val thingSearchMap = HashMap<String, String>()
//                                        val thingSearchUrl = context!!.resources.getString(R.string.mobile_url) + "MnidstSearch"
//                                        val thingLandConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//                                        thingSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        thingSearchMap["incrprLnm"] = naverGeoAddress
//
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,
//                                            thingLandConfirmUrl, object: Callback{
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    progressDialog?.dismiss()
//                                                    logUtil.d("fail")
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("ThingLandConfirm response ------------------> $responseString")
//
//                                                    val messageJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                    val messageNum = messageJson.getString("messageNum")
//                                                    val message = messageJson.getString("message")
//
//                                                    progressDialog?.dismiss()
//                                                    if(messageNum.equals("-1")) {
//                                                        activity!!.runOnUiThread {
//                                                            toastUtil.msg_error(message.toString(), 500)
//                                                        }
//                                                    } else {
//                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,thingSearchUrl,
//                                                            object: Callback{
//                                                                override fun onFailure(call: Call, e: IOException) {
//                                                                    progressDialog?.dismiss()
//                                                                    logUtil.d("fail")
//                                                                }
//
//                                                                override fun onResponse(call: Call, response: Response) {
//                                                                    val responseString = response.body!!.string()
//
//                                                                    logUtil.d("thingSearch response ------------------> $responseString")
//
//                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch")
//
//                                                                    progressDialog?.dismiss()
//
//                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                                }
//
//                                                            })
//                                                    }
//                                                }
//
//                                            })
//                                    }
//                                    BizEnum.FYHTS -> { //?????????
//                                        setMarker(coord.latitude, coord.longitude, "???????????????")
//
//                                        logUtil.d("naver map click THING FSHR")
//
//                                        val fyhtsSearchUrl = context!!.resources.getString(R.string.mobile_url) + "fyhtsSearch"
//                                        val fyhtsSearchMap = HashMap<String,String>()
//
//                                        fyhtsSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        fyhtsSearchMap["legaldongCode"] = ThingFyhtsObject.legaldongCl
//
//                                        HttpUtil.getInstance(context!!)
//                                            .callerUrlInfoPostWebServer(fyhtsSearchMap, progressDialog, fyhtsSearchUrl,
//                                            object: Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    logUtil.d("fail")
//                                                    progressDialog?.dismiss()
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("thingSearch FYHTS response --------------------------->$responseString")
//
//                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray
//
//                                                    progressDialog?.dismiss()
//                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                }
//
//                                            })
//
//
////                                       setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, "", false, null)
//
//
//                                    }
//                                    BizEnum.BSN -> { //??????
//                                        logUtil.d("naver Map Click BSN -------------------------<><><><><>")
//
//                                        setMarker(coord.latitude, coord.longitude, "????????????")
//
//                                        val bsnSearchMap = HashMap<String, String>()
//                                        val bsnSearchUrl = context!!.resources.getString(R.string.mobile_url) + "bsnSearch"
//                                        val bsnLadConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//                                        bsnSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        bsnSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnLadConfirmUrl,
//                                            object: Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    logUtil.d("fail")
//                                                    progressDialog?.dismiss()
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("BsnLandConfirm response ------------------> $responseString")
//
//                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                    val messageNum = messageJSON.getString("messageNum")
//                                                    val message = messageJSON.getString("message")
//
//                                                    progressDialog?.dismiss()
//
//                                                    if(messageNum.equals("-1")) {
//                                                        activity!!.runOnUiThread {
//                                                            toastUtil.msg_error(message.toString(), 500)
//                                                        }
//                                                    } else {
//                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnSearchUrl,
//                                                            object: Callback {
//                                                                override fun onFailure(call: Call, e: IOException) {
//                                                                    logUtil.e("fail")
//                                                                    progressDialog?.dismiss()
//                                                                }
//
//                                                                override fun onResponse(call: Call, response: Response) {
//                                                                    val responseString = response.body!!.string()
//
//                                                                    logUtil.d("thingSearch BSN response --------------------------->$responseString")
//
//                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray
//
//                                                                    progressDialog?.dismiss()
//                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                                }
//
//                                                            })
//                                                    }
//
//
//                                                }
//
//                                            })
//
//                                    }
//                                    //??????
//                                    BizEnum.FARM -> {
//                                        logUtil.d("naver Map Click FARM ---------------------- <><><><><>")
//
//                                        setMarker(coord.latitude, coord.longitude, "????????????")
//
//                                        val farmSearchMap = HashMap<String, String>()
//                                        val farmSearchUrl = context!!.resources.getString(R.string.mobile_url) + "farmSearch"
//                                        val farmLadConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//                                        farmSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        farmSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmLadConfirmUrl,
//                                            object: Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    logUtil.d("fail")
//                                                    progressDialog?.dismiss()
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("FarmLandConfirm response ---------------> $responseString")
//
//                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                    val messageNum = messageJSON.getString("messageNum")
//                                                    val message = messageJSON.getString("message")
//
//                                                    progressDialog?.dismiss()
//
//                                                    if(messageNum.equals("-1")) {
//                                                        activity!!.runOnUiThread {
//                                                            toastUtil.msg_error(message.toString(), 500)
//                                                        }
//                                                    } else {
//                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmSearchUrl,
//                                                            object: Callback {
//                                                                override fun onFailure(call: Call, e: IOException) {
//
//                                                                    logUtil.e("fail")
//                                                                    progressDialog?.dismiss()
//                                                                }
//
//                                                                override fun onResponse(call: Call, response: Response) {
//
//                                                                    var responseString = response.body!!.string()
//
//                                                                    logUtil.d("thingSearch FARM response ------------------> $responseString")
//
//                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray?
//
//                                                                    progressDialog?.dismiss()
//                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                                }
//
//                                                            })
//                                                    }
//
//                                                }
//
//                                            })
//
//
//                                    }
//                                    //?????????
//                                    BizEnum.RESIDNT -> {
//                                        logUtil.d("naver Map Click RESIDNT ------------------------ <><><><><><")
//
//                                        setMarker(coord.latitude, coord.longitude, "????????? ??????")
//
//                                        val residntSearchMap = HashMap<String, String>()
//                                        val residntSearchUrl = context!!.resources.getString(R.string.mobile_url) + "residntSearch"
//                                        val residntConfirmUrl = context!!.resources.getString(R.string.mobile_url) + "LandSearchConfirm"
//
//                                        residntSearchMap["saupCode"] = prefUtil!!.getString("saupCode", "")
//                                        residntSearchMap["incrprLnm"] = naverGeoAddress
//
//                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntConfirmUrl,
//                                            object: Callback {
//                                                override fun onFailure(call: Call, e: IOException) {
//                                                    logUtil.d("fail")
//                                                    progressDialog?.dismiss()
//
//                                                }
//
//                                                override fun onResponse(call: Call, response: Response) {
//                                                    val responseString = response.body!!.string()
//
//                                                    logUtil.d("residntLandConfirm response ------------------> $responseString")
//
//                                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                    val messageNum = messageJSON.getString("messageNum")
//                                                    val message = messageJSON.getString("message")
//
//                                                    progressDialog?.dismiss()
//
//                                                    if(messageNum.equals("-1")) {
//                                                        activity!!.runOnUiThread {
//                                                            toastUtil.msg_error(message.toString(), 500)
//                                                        }
//                                                    } else {
//                                                        HttpUtil.getInstance(context!!).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntSearchUrl,
//                                                            object: Callback {
//                                                                override fun onFailure(call: Call, e: IOException) {
//                                                                    logUtil.e("fail")
//                                                                    progressDialog?.dismiss()
//                                                                }
//
//                                                                override fun onResponse(call: Call,response: Response) {
//                                                                    var responseString = response.body!!.string()
//
//                                                                    logUtil.d("thingSearch RESIDNT response ------------------> $responseString")
//
//                                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
//                                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray?
//
//                                                                    progressDialog?.dismiss()
//                                                                    setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
//                                                                }
//
//                                                            })
//                                                    }
//
//                                                }
//
//                                            })
//
//
//                                    }
//
//
//                                    else -> {
//                                        setMarker(coord.latitude, coord.longitude,"????????????")
//                                        setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, "", false, null)
//                                    }
//
//                                }
//
//                            }
//
//                        }
//                    }
//                }
//            )

    }

    override fun onMapLongClick(point: PointF, coord: LatLng) {}

    override fun onMapDoubleTap(point: PointF, coord: LatLng): Boolean = true

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
    }
}