/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import androidx.core.content.ContextCompat
import com.carto.components.Layers
import com.carto.components.Options
import com.carto.core.*
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.Geometry
import com.carto.geometry.LineGeometry
import com.carto.geometry.PointGeometry
import com.carto.geometry.PolygonGeometry
import com.carto.graphics.Color
import com.carto.layers.*
import com.carto.projections.Projection
import com.carto.styles.*
import com.carto.ui.*
import com.carto.utils.BitmapUtils
import com.carto.vectorelements.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jhlabs.map.Point2D
import com.naver.maps.geometry.Coord
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.activity_map.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.SketchEnum
import kr.or.kreb.ncms.mobile.fragment.FarmSearchFragment
import kr.or.kreb.ncms.mobile.fragment.LandSearchFragment
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface
import kr.or.kreb.ncms.mobile.util.PermissionUtil.logUtil
import java.math.BigDecimal
import kotlin.math.roundToInt


class CartoMapUtil : DialogUtil.ClickListener {

    // App Util
    private lateinit var gpsUtil: GPSUtil
    private lateinit var logtUtil: LogUtil
    private lateinit var toastUtil: ToastUtil
    private lateinit var mathUtil: MathUtil
    private lateinit var dialogUtil: DialogUtil
    lateinit var dialogBuilder: MaterialAlertDialogBuilder

    // Common Variable
    var context: Context? = null
    private var activity: Activity? = null
    private lateinit var sketchEnumMode: SketchEnum // ????????? ??????
    var mFarmFragment: FarmSearchFragment? = null

    // Carto
    var cartoMapView: MapView
    private lateinit var mapOpt: Options
    lateinit var cartoProj: Projection
    lateinit var localVectorDataSource: LocalVectorDataSource
    lateinit var locatVectorText: LocalVectorDataSource

    private var redoMapPos = mutableListOf<MapPos>() // ?????? MapPos (redo)
    private var mapLayers = mutableListOf<EditableVectorLayer>()

    var makePolygonArr = mutableListOf<ArrayList<LatLng>>()
    lateinit var mapPosVector: MapPosVector
    lateinit var cartoEditLayer: EditableVectorLayer

    var _isModify = false
    private val radius = 6371009.0

    // Carto Style
    private lateinit var polyTextBuilderStyle: TextStyle
    private var centerTx: Text? = null

    // Carto Listener
    private var editListener = EditEventListener()
    var selectListener = VectorElementSelectEventListener()
    var deselectListener = VectorElementDeselectListener()

    // Naver
    private var naverMapUtil: NaverMapUtil? = null
    private var coord: Coord? = null

    private var searchType: BizEnum? = null
    private var sktechType: Int = -1
    private var thingListener: ThingViewPagerInterface? = null
    private var currentArea: Int? = null

    private var _isSketchDrawType: String = "2"

    var getMapPos = MapPos()
    var mapSelectLayerVectorIndex = 0

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        naverMap: NaverMapUtil,
        coord: Coord,
        searchType: BizEnum,
        fragment: FarmSearchFragment?
    ) {
        this.context = context
        this.activity = activity
        this.cartoMapView = mapView
        this.naverMapUtil = naverMap
        this.coord = coord
        this.searchType = searchType
        this.mFarmFragment = fragment
        init()
    }

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        naverMap: NaverMapUtil,
        coord: Coord,
        searchType: BizEnum,
        sketchType: Int,
        thingListener: ThingViewPagerInterface
    ) {
        this.context = context
        this.activity = activity
        this.cartoMapView = mapView
        this.naverMapUtil = naverMap
        this.coord = coord
        this.searchType = searchType
        this.sktechType = sketchType
        this.thingListener = thingListener
        init()
    }

    private fun init() {

        gpsUtil = GPSUtil(context!!)
        logtUtil = LogUtil("CartoMap")
        toastUtil = ToastUtil(context!!)
        mathUtil = MathUtil
        dialogUtil = DialogUtil(context, activity)
        dialogBuilder = MaterialAlertDialogBuilder(activity!!)
        dialogUtil.setClickListener(this)

        mapPosVector = MapPosVector()

        logtUtil.d("CartoMap Util Init")
        PreferenceUtil.setString(context!!, "cartoMapType", "default") // ?????? ?????? ?????????

        val overlayBitmap = BitmapUtils.loadBitmapFromAssets("img_transparent.png")

        //MapView.registerLicense(context!!.resources.getString(R.string.catro_license_code), context)

        mapOpt = cartoMapView.options!!
        cartoProj = mapOpt.baseProjection

        val mMinPos = cartoProj.fromWgs84(naverMapUtil!!.getMinPos())
        val mMaxPos = cartoProj.fromWgs84(naverMapUtil!!.getMaxPos())

        val bounds = MapBounds(mMinPos, mMaxPos)
        val screenBounds = ScreenBounds(
            ScreenPos(0f, 0f),
            ScreenPos(
                naverMapUtil!!.naverMap.contentWidth.toFloat(),
                naverMapUtil!!.naverMap.contentHeight.toFloat()
            )
        )

        /**
         * 2021-05-17  ?????? 20 ?????? (Naver Map Lv ==  Carto Map Lv)
         */

        // ??? ??????
        mapOpt.apply {
            backgroundBitmap = overlayBitmap
            tiltRange = MapRange(90f, 90f) // ?????? ??????
            isRotatable = false // ??????
            isZoomGestures = false
            watermarkScale = 0.01f
        }

        // ??? ????????????
        cartoMapView.apply {
            holder.setFormat(PixelFormat.TRANSPARENT) // ??? ?????????
            moveToFitBounds(bounds, screenBounds, false, 0.5f)
            setFocusPos(cartoProj.fromWgs84(MapPos(naverMapUtil!!.lon, naverMapUtil!!.lat)), 0f) // ?????? ?????? ??????
            mapEventListener = CartoMapEventListener()
        }

        localVectorDataSource = LocalVectorDataSource(cartoProj)
        locatVectorText = LocalVectorDataSource(cartoProj)

        empty()
        reset()
    }

    /**
     * ?????? ???????????? ????????????
     */
    fun getActivity(): MapActivity = (context as MapActivity)

    /**
     * ?????? ????????? ????????????
     */
    fun getLayers(): Layers? = cartoMapView.layers

    /**
     * ?????? ???????????? ???
     */
    fun getLayerCount(): Int = cartoMapView.layers?.count()!!

    /**
     * MapPos??? ?????????
     */
    fun getMapPosSize(): Int = LandInfoObject.mapPos.size

    /**
     * ????????? ????????? ?????????
     */
    fun setMode(sketchEnumMode: SketchEnum) {
        this.sketchEnumMode = sketchEnumMode
        sketchFunc()
    }

    /**
     * ??????, ??????, ??????, ??????, ??????(???????????? ??????), ?????????(????????????) ??????
     */
    private fun sketchFunc() {
        when (sketchEnumMode) {
            SketchEnum.UNDO -> undo()
            SketchEnum.REDO -> redo()
            SketchEnum.POINT -> point()
            SketchEnum.LINE -> line()
            SketchEnum.MODIFY -> modify()
            SketchEnum.CANCEL -> {
                dialogUtil.run {
                    alertDialog(
                        "????????? ??????",
                        "????????????????????? ???????????? ????????? ??????????????????. \n ????????? ???????????? ??????????????????????",
                        dialogBuilder,
                        "???????????????"
                    ).show()
                }
            }
            SketchEnum.REMOVE -> reset()
            SketchEnum.POLYGON -> {

                val getPolygonArray = polygon() //Carto?????? ????????? ???????????? data

                try {
                    if (getPolygonArray.size > 0) {

                        for (i in getPolygonArray.indices) {
                            naverMapUtil!!.setNaverMapPolygon(getPolygonArray[i], searchType)
                        }

                        currentArea = (mathUtil.layersForArea(LandInfoObject.clickLatLng, radius) * 100.0 / 100.0).roundToInt() // ?????????????????? ??????

                        when (searchType) {
                            BizEnum.LAD -> {
                                LandInfoObject.selectPolygonCurrentArea = LandInfoObject.currentArea
                                LandInfoObject.currentArea = currentArea

                                logtUtil.d("?????????????????? ?????? ?????? ?????? => [${LandInfoObject.selectPolygonCurrentArea}]")
                                logtUtil.d("?????????????????? ?????? => [$currentArea]")
                                logtUtil.d("????????? Index ${LandInfoObject.landRealArCurPos}")
//
//                                /**
//                                 *  ?????????????????? ???????????? ?????? ?????? ?????? update ??????
//                                 *  ??? ??? add ??????
//                                 */

                                logUtil.d("???????????? ?????????: [${LandInfoObject.isEditable}]")

                                when (LandInfoObject.isEditable) {
                                    true -> LandSearchFragment(activity, context).landRealUpdate()
                                    else -> LandSearchFragment(activity, context).landRealAdd(getPolygonArray)
                                }

                                empty()
                            }
                            BizEnum.THING -> ThingWtnObject.pointYn = _isSketchDrawType
                            BizEnum.TOMB -> ThingTombObject.pointYn = _isSketchDrawType
                            BizEnum.FARM -> {
                                ThingFarmObject.pointYn = _isSketchDrawType
                                logtUtil.d("???????????????.")
                                logtUtil.d("?????? ???????????? -> $currentArea")
                                mFarmFragment?.addTableRow(currentArea)
                                empty()
                            }
                            BizEnum.BSN -> ThingBsnObject.pointYn = _isSketchDrawType
                            BizEnum.RESIDNT -> ThingResidntObject.pointYn = _isSketchDrawType
                            BizEnum.MINRGT -> ThingMinrgtObject.pointYn = _isSketchDrawType
                            else -> logtUtil.d("none")
                        }

                    }
                } catch (e: Exception) {
                    logtUtil.d(e.toString())
                }
            }
        }
    }

    /**
     * ???????????? ?????????
     */
    fun empty() {

        localVectorDataSource.clear()
        cartoEditLayer = EditableVectorLayer(localVectorDataSource)
        mapPosVector.clear()

        LandInfoObject.clickLatLng.clear()
        LandInfoObject.mapPos.clear()
        LandInfoObject.lineCenterTxList.clear()

    }

    /**
     * ??????
     */
    fun undo() {

        if (getMapPosSize() > 0) {

            redoMapPos.add(LandInfoObject.mapPos.removeAt(LandInfoObject.mapPos.size - 1))
            mapPosVector.clear()

            LandInfoObject.mapPos.forEach { mapPos -> mapPosVector.add(mapPos) }
            sketchDistanceArea()
        } else {
            toastUtil.msg_warning(context!!.getString(R.string.msg_undo), 100)
        }
    }

    /**
     * ??????
     */
    fun redo() {

        if (redoMapPos.size > 0) {

            LandInfoObject.mapPos.add(redoMapPos.removeAt(redoMapPos.size - 1))
            mapPosVector.clear()

            LandInfoObject.mapPos.forEach { mapPos -> mapPosVector.add(mapPos) }
            sketchDistanceArea()
        } else {
            toastUtil.msg_warning(context!!.getString(R.string.msg_redo), 500)
        }
    }

    /**
     * ????????? ?????? (???)
     */
    fun point() {
        try {
            toastUtil.msg_info("????????? ????????? (???)?????? ?????????????????????.", 300)
            _isSketchDrawType = "1"
            _isModify = false
            empty()
            removeMapLayers(cartoMapView)
            setEditModeView(false)
        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }
    }

    /**
     * ????????? ?????? (???)
     */
    fun line() {
        try {
            toastUtil.msg_info("????????? ????????? (???)?????? ?????????????????????.", 300)
            _isSketchDrawType = "2"
            _isModify = false
            empty()
            removeMapLayers(cartoMapView)
            setEditModeView(false)
        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }
    }

    /** ?????? */
    fun modify() {

        // TODO: 2022-04-01 ???????????? ???????????? ??? ?????? (??????)
        var selectInfoWindowMapPos: MapPos
        var selectPolygonOverlayMapPos: MapPos

        val newSearch = setModifyTargetSetMap()["thingNewSearch"]
        val pointYn = setModifyTargetSetMap()["pointYn"]

        val featureConnt = localVectorDataSource.featureCollection.featureCount

        _isModify = true // ???????????? True

        logtUtil.d("?????? or ?????? => $newSearch")
        logtUtil.d("????????? or ????????? => $pointYn")

        try {

            if(newSearch == "N"){

                empty()

                // ??????????????? ?????? Map WFS ?????????
                when(Constants.BIZ_SUBCATEGORY_KEY){
                    BizEnum.THING -> {
                        naverMapUtil?.apply {
                            wfsThingOverlayArr.forEach { it.map = null }
                            clearWFS(wfsThingOverlayArr, "?????????")
                        }
                    }
                    else -> {}
                }

                when(pointYn){
                    // ????????? ??????
                    "1" -> {
                        getActivity().choiceInfoWindowArr.forEach { choice ->

                            choice.position.apply {
                                selectInfoWindowMapPos = cartoProj.fromWgs84(MapPos(longitude, latitude))!!
                            }
                            LandInfoObject.mapPos.add(selectInfoWindowMapPos) // ????????? InfoWindow??? MapPos??? ??????????????? ????????????.

                            choice.map = null
                            logtUtil.d("selectInfoWindowMapPos => $selectInfoWindowMapPos")

                            val point = Point(selectInfoWindowMapPos, getPointStyle())

                            localVectorDataSource.add(point)
                        }

                    }

                    // ????????? ??????
                    "2" -> {
                        val modifyPosVector = MapPosVector()

                        getActivity().contextPopupPolygonArr.forEach { popupPoly ->
                            popupPoly.coords.forEach { coord ->
                                selectPolygonOverlayMapPos =
                                    cartoProj.fromWgs84(MapPos(coord.longitude, coord.latitude))!!
                                popupPoly.map = null

                                logtUtil.d("selectPolygonOverlayMapPos => $selectPolygonOverlayMapPos")
                                modifyPosVector.add(selectPolygonOverlayMapPos)

                            }
                        }

                        val modifyPolygon = Polygon(modifyPosVector, getPolygonStyle())
                        localVectorDataSource.add(modifyPolygon)
                    }
                }

                setEditLayer()
                setEditModeView(true)

            // ??????
            } else {

                if (featureConnt <= 0){
                    toastUtil.msg_warning("????????? ????????? ???????????? ????????????.", 500)
                    setEditModeView(false)
                } else {
                    setEditLayer()
                    setEditModeView(true)
                }

            }

        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }
    }

    /**
     * ?????????????????? ????????? ?????? ?????? ?????? ??????
     */
    fun modifyLandAr(getLandAr: MutableList<LatLng>) {

        _isModify = true // ???????????? True
        val modifyLandArMapPosVector = MapPosVector()

        try {
            getLandAr.forEach {
                val modifyLandArMapPos = cartoProj.fromWgs84(MapPos(it.longitude, it.latitude))
                modifyLandArMapPosVector.add(modifyLandArMapPos)
            }

            val modifyLandArPolygon = Polygon(modifyLandArMapPosVector, getPolygonStyle())
            localVectorDataSource.add(modifyLandArPolygon)

            setEditLayer()
            setEditModeView(true)

            LandInfoObject.selectLandPolygonArr = getLandAr // ????????? ???????????? ????????? ????????? ?????????.

            getActivity().naverMap?.clearCartoPolygon()

        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }
    }

    /** ????????? */
    fun reset() {

        empty()
        removeMapLayers(cartoMapView)

    }

    /** ?????? */
    fun cancel() {
        empty()
        getActivity().run {
            layoutMapRightButtonGroup.visibleView()
            layoutMapLeftButtonGroup.visibleView()
            toggleFab(true)
            cartoMapView.goneView()
            fabVisableArr.forEach { obj -> obj.goneView() }
        }
    }

    fun setEditLayer(){
        cartoEditLayer.vectorEditEventListener = editListener
        cartoEditLayer.vectorElementEventListener = selectListener

        cartoMapView.layers?.add(cartoEditLayer) // ??? ????????? ??????
        mapLayers.add(cartoEditLayer) // ??? ????????? Arr
    }

    /**
     * ??????????????? ?????? ?????? ?????? Object Value Set ?????????.
     * ??????????????? ?????? ?????? WFS Layer ?????????
     */
    private fun setModifyTargetSetMap(): HashMap<String, String> {

        val targetMap = HashMap<String, String>()

        when (Constants.BIZ_SUBCATEGORY_KEY) {
            BizEnum.LAD -> {}
            BizEnum.THING -> {

//                naverMapUtil?.apply {
//                    wfsThingOverlayArr.forEach { it.map = null }
//                    clearWFS(wfsThingOverlayArr, "?????????")
//                }

                targetMap["pointYn"] = ThingWtnObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingWtnObject.thingNewSearch.toString()
            }
            BizEnum.BSN -> {

                naverMapUtil?.apply {
                    wfsBsnOverlayArr.forEach { it.map = null }
                    clearWFS(wfsBsnOverlayArr, "??????")
                }

                targetMap["pointYn"] = ThingBsnObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingBsnObject.thingNewSearch
            }
            BizEnum.FARM -> {

                naverMapUtil?.apply {
                    wfsFarmOverlayArr.forEach { it.map = null }
                    clearWFS(wfsFarmOverlayArr, "??????")
                }

                targetMap["pointYn"] = ThingFarmObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingFarmObject.thingNewSearch
            }
            BizEnum.MINRGT -> {

                targetMap["pointYn"] = ThingMinrgtObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingMinrgtObject.thingNewSearch
            }
            BizEnum.FYHTS -> {
                targetMap["pointYn"] = ThingFyhtsObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingFyhtsObject.thingNewSearch
            }
            BizEnum.RESIDNT -> {

                naverMapUtil?.apply {
                    wfsResidntOverlayArr.forEach { it.map = null }
                    clearWFS(wfsResidntOverlayArr, "??????")
                }

                targetMap["pointYn"] = ThingResidntObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingResidntObject.thingNewSearch.toString()
            }
            BizEnum.TOMB -> {

                naverMapUtil?.apply {
                    wfsTombOverlayArr.forEach { it.map = null }
                    clearWFS(wfsTombOverlayArr, "??????")
                }

                targetMap["pointYn"] = ThingTombObject.pointYn.toString()
                targetMap["thingNewSearch"] = ThingTombObject.thingNewSearch
            }
            else -> {}
        }

        return targetMap
    }

    private fun setEditModeView(flag: Boolean){

        getActivity().btn_EditMode.apply {
            if(flag){
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnColor4))
                text = "???????????? ON"
            } else {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnColor1))
                text = "???????????? OFF"
            }
        }


    }

    /** ???????????? */
    fun select(element: VectorElement?) {
        cartoEditLayer.selectedVectorElement = element

        val selectVectorElementSourceCenterMapPos = element?.geometry?.centerPos // ????????? Element??? MapPos
        val vectorElementSourceCenterMapPos = (cartoEditLayer.dataSource as LocalVectorDataSource).featureCollection // ?????? ??????????????? ?????? Vector Element

        for(i in 0 .. vectorElementSourceCenterMapPos.featureCount){
            if(selectVectorElementSourceCenterMapPos == vectorElementSourceCenterMapPos.getFeature(i).geometry.centerPos){
                mapSelectLayerVectorIndex = i
                logtUtil.d("????????? ????????? Vector Index [$mapSelectLayerVectorIndex]")
                break
            }
        }

    }

    /** ???????????? ?????? */
    fun deselect() {
        cartoEditLayer.selectedVectorElement = null
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inner class CartoMapEventListener : MapEventListener() {

        override fun onMapClicked(mapClickInfo: MapClickInfo?) {
            super.onMapClicked(mapClickInfo)

            val pos3857 = mapClickInfo!!.clickPos
            val posWGS84 = cartoProj.toWgs84(pos3857)

            when (mapClickInfo.clickType) {

                // ?????????
                ClickType.CLICK_TYPE_SINGLE -> {
                    LandInfoObject.clickLatLng.add(posWGS84)
                    LandInfoObject.mapPos.add(pos3857)
                    mapPosVector.add(pos3857)
                    sketchDistanceArea()
                }

                // ???????????? - ??????
                else -> {}
            }

        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    /**
     * ????????? ?????????
     */
    private fun getPointStyle(): PointStyle? {
        val pStyleBuilder = PointStyleBuilder()
        pStyleBuilder.apply {
            color = Color(setObjectColor(context!!, R.color.red, 255))
            size = 15.0f
        }
        return pStyleBuilder.buildStyle()
    }

    /**
     * ????????? ?????????
     */
    private fun getPolygonStyle(): PolygonStyle? {

        // ?????????
        val polyTextStyleBuilder = TextStyleBuilder()
        polyTextStyleBuilder.apply {
            fontSize = 20f
            color = Color(setObjectColor(context!!, R.color.red, 255))
        }
        polyTextBuilderStyle = polyTextStyleBuilder.buildStyle()

        // ?????????
        val polyLineStyleBuilder = LineStyleBuilder()
        polyLineStyleBuilder.apply {
            width = 1.0f
            color = Color(setObjectColor(context!!, R.color.green, 255))
            lineEndType = LineEndType.LINE_END_TYPE_NONE
        }

        // ?????????
        val polyLineStyleBuilderStyle = polyLineStyleBuilder.buildStyle()
        val polyStyleBuilder = PolygonStyleBuilder()
        polyStyleBuilder.apply {
            color = Color(setObjectColor(context!!, R.color.green, 80))
            lineStyle = polyLineStyleBuilderStyle
        }

        return polyStyleBuilder.buildStyle()
    }

    /**
     * Catro ????????? ??????
     */
    fun sketchDistanceArea() {

        if(!_isModify){

            var point: Point?

            // ?????????
            removeMapLayers(cartoMapView)

            if(!_isModify){
                for (pos in LandInfoObject.mapPos) {
                    point = Point(pos, getPointStyle())
                    localVectorDataSource.add(point)
                }
            }

            // ???, ??????
            if (_isSketchDrawType == "2") {
                val ply = Polygon(mapPosVector, getPolygonStyle())
                LandInfoObject.mapCenter = ply.bounds.center
                localVectorDataSource.add(ply)
            }

            // ??????
            if (_isSketchDrawType == "2") {
                if (getMapPosSize() > 1) {
                    setDistanceLineText("sketch")
                }
            }

            // ??????
            if (_isSketchDrawType == "2") {
                if (getMapPosSize() > 2) {
                    setDistancePolyText(LandInfoObject.mapCenter, "sketch")
                }
            }

            cartoEditLayer = EditableVectorLayer(localVectorDataSource)

            cartoMapView.layers.add(cartoEditLayer)
            mapLayers.add(cartoEditLayer)
        }

    }

    /** ?????? ????????? */
    inner class VectorElementSelectEventListener : VectorElementEventListener() {
        override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
            select(clickInfo.vectorElement)
            return true
        }
    }

    /** ?????? ?????? ????????? */
    inner class VectorElementDeselectListener : MapEventListener() {
        override fun onMapClicked(mapClickInfo: MapClickInfo?) {
            deselect()
        }
    }

    /** ?????? ????????? */
    inner class EditEventListener : VectorEditEventListener() {

        private var styleNormal: PointStyle? = null
        private var styleVirtual: PointStyle? = null
        private var styleSelected: PointStyle? = null

        override fun onElementModify(element: VectorElement?, geometry: Geometry?) {

            when (element) {
                is Point -> {
                    element.geometry = geometry as PointGeometry
                }
                is Line -> {
                    element.geometry = geometry as LineGeometry
                }
                is Polygon -> {
                    element.geometry = geometry as PolygonGeometry

                    LandInfoObject.clickLatLng.clear()
                    LandInfoObject.mapPos.clear()

                    for (i in 0 until element.geometry.poses.size()) {

                        val posWGS84 = cartoProj.toWgs84(element.geometry.poses[i.toInt()])
                        val pos3857 = cartoProj.fromLatLong(
                            element.geometry.poses.get(i.toInt()).x,
                            element.geometry.poses.get(i.toInt()).y
                        )

                        LandInfoObject.clickLatLng.add(posWGS84)

                        if(_isModify)
                            LandInfoObject.mapPos.add(element.poses[i.toInt()])
                        else
                            LandInfoObject.mapPos.add(pos3857)
                    }

                    LandInfoObject.mapCenter = element.bounds.center

                    //setDistanceLineText("modify") //??????

                    if (LandInfoObject.clickLatLng.size > 2)
                        setDistancePolyText(LandInfoObject.mapCenter, "modify") //??????

                }
            }
        }

        override fun onElementDelete(element: VectorElement?) {
            localVectorDataSource.remove(element)
        }

        override fun onDragStart(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            //logtUtil.d("onDragStart MapPos => ${dragInfo?.mapPos}")
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

        }

        override fun onDragMove(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            //logtUtil.d("onDragMove MapPos => ${dragInfo?.mapPos}")
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY
        }

        override fun onDragEnd(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            //logtUtil.d("onDragEnd MapPos => ${dragInfo?.mapPos}")

            if(ThingWtnObject.pointYn == "1"){
                val dragEndMapPosArr = mutableListOf<MapPos>()

                for(i in 0 until getMapPosSize()){
                    dragEndMapPosArr.add(LandInfoObject.mapPos[i]) // ?????? ????????? ?????? MapPos ??????
                }

                getMapPos = dragInfo?.mapPos!!
                dragEndMapPosArr[mapSelectLayerVectorIndex] = getMapPos

                LandInfoObject.mapPos = dragEndMapPosArr
            }

            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

        }

        override fun onSelectDragPointStyle(
            element: VectorElement?,
            dragPointStyle: VectorElementDragPointStyle
        ): PointStyle? {
            if (null == styleNormal) {
                val builder = PointStyleBuilder()
                builder.color = Color(setObjectColor(context!!, R.color.red, 255))
                builder.size = 15f
                styleNormal = builder.buildStyle()
                builder.size = 15f
                styleVirtual = builder.buildStyle()
                builder.color = Color(setObjectColor(context!!, R.color.yellow, 255))
                builder.size = 15f
                styleSelected = builder.buildStyle()
            }

            if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_NORMAL) {
                return styleSelected
            }
            return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) styleVirtual else styleNormal
        }
    }

    /**
     * ?????? Text
     */
    private fun setDistanceLineText(type: String) {
        LandInfoObject.lineCenterTxList.clear()
        for (i in 1..LandInfoObject.mapPos.size) {
            var lineCenterPos: MapPos?
            var lineCenterTx: Text?

            if (i == getMapPosSize()) {
                lineCenterPos = MapPos(
                    (LandInfoObject.mapPos[i - 1].x + LandInfoObject.mapPos[0].x) / 2,
                    (LandInfoObject.mapPos[i - 1].y + LandInfoObject.mapPos[0].y) / 2
                )
                lineCenterTx = Text(
                    lineCenterPos,
                    polyTextBuilderStyle,
                    "${
                        (mathUtil.getLineDistance(
                            LandInfoObject.clickLatLng[i - 1],
                            LandInfoObject.clickLatLng[0],
                            radius
                        ) * 100.0 / 100.0).roundToInt()
                    }m"
                )
            } else {
                lineCenterPos = MapPos(
                    (LandInfoObject.mapPos[i - 1].x + LandInfoObject.mapPos[i].x) / 2,
                    (LandInfoObject.mapPos[i - 1].y + LandInfoObject.mapPos[i].y) / 2
                )
                lineCenterTx = Text(
                    lineCenterPos,
                    polyTextBuilderStyle,
                    "${
                        (mathUtil.getLineDistance(
                            LandInfoObject.clickLatLng[i - 1],
                            LandInfoObject.clickLatLng[i],
                            radius
                        ) * 100.0 / 100.0).roundToInt()
                    }m"
                )
            }
            LandInfoObject.lineCenterTxList.add(lineCenterTx)

            // ?????????
            if (type == "sketch") {
                localVectorDataSource.add(lineCenterTx)
            }
        }

        // ??????
        if (type == "modify") {
            for (i in 0 until LandInfoObject.lineCenterTxList.size) {
                localVectorDataSource.add(LandInfoObject.lineCenterTxList[i])
            }
        }
    }

    /**
     * ?????? Text
     */
    private fun setDistancePolyText(mapCenter: MapPos?, type: String) {
        // ??????
        if (type == "modify") {
            centerTx?.let {
                localVectorDataSource.remove(it)
            }
        }

        currentArea = (mathUtil.layersForArea(LandInfoObject.clickLatLng, radius) * 100.0 / 100.0).roundToInt()
        centerTx = Text(mapCenter, polyTextBuilderStyle, "${currentArea}???")
        localVectorDataSource.add(centerTx)

        LandInfoObject.currentArea = currentArea //???????????? ??????
    }

    fun setScreenSync() {
        logtUtil.d("setScreenSync-------------------------------<><>")

        val mMinPos = cartoProj.fromWgs84(naverMapUtil!!.getMinPos())
        val mMaxPos = cartoProj.fromWgs84(naverMapUtil!!.getMaxPos())

        val bounds = MapBounds(mMinPos, mMaxPos)
        val screenBounds = ScreenBounds(
            ScreenPos(0f, 0f),
            ScreenPos(
                naverMapUtil!!.naverMap.contentHeight.toFloat(),
                naverMapUtil!!.naverMap.contentWidth.toFloat()
            )
        )

        cartoMapView.moveToFitBounds(bounds, screenBounds, false, 0.5f)
        //cartoMapView!!.setZoom(20f, 0f) // 20?????? ?????? (????????? ?????? ??????)

        logUtil.d("naverMapUtill getNaverMapZoom ------------------> ${naverMapUtil?.getNaverMapZoom()}")
        cartoMapView.setZoom((naverMapUtil?.getNaverMapZoom()!!.toFloat()+0.9955).toFloat(), 0.5f)
        cartoMapView.setFocusPos(
            cartoProj.fromWgs84(MapPos(naverMapUtil!!.lon, naverMapUtil!!.lat)), 0.5f
        ) // ?????? ?????? ??????
    }

    /**
     * ????????? ????????? ?????? ??????
     */
    private fun removeMapLayers(mapView: MapView) {
        try {
            localVectorDataSource.clear()
            for (i in 0 until getLayerCount()) {
                mapView.layers.remove(mapView.layers[i])
            }
            mapLayers.clear()

            logtUtil.d("cartoMapLayer size ${getLayerCount()}")
            logtUtil.d("mapLayers size ${mapLayers.size}")

        } catch (e: Exception) {
            logtUtil.d(e.toString())
        }
    }

    /**
     * ????????? ?????????(?????????) ??????
     */
    fun  polygon(): MutableList<ArrayList<LatLng>> {

        when {

            // 1. ?????????
            getMapPosSize() == 1 -> {
                setMarkerPointToPolygon()
                _isSketchDrawType = "1"
            }

            // 2. ???
            getMapPosSize() == 2 -> {

                when (_isSketchDrawType) {
                    "2" -> {
                        setMarkerLineToPolygon()
                    }
                    else -> {
                        setMarkerPointToPolygon()
                    }
                }
            }
        }

//        if (getMapPosSize() > 2) {

            // ????????? '??? or ??????'
            if (_isSketchDrawType == "2") {
                drawLineOrPolygonObject()
            } else {
                drawPointObject()
            }
//        }

        setEditModeView(false)
        getActivity().btn_EditMode.goneView()

        return makePolygonArr
    }

    /**
     * ????????? ????????? ??????
     */

    fun drawPointObject() : MutableList<ArrayList<LatLng>>{

        try {
            var mapPos: MapPos
            val mapPosArr = mutableListOf<MapPos>()

            for (i in 0 until LandInfoObject.mapPos.size) {
                mapPos = LandInfoObject.mapPos[i]
                mapPosArr.add(mapPos)
            }

            val mapPosArray = arrayListOf<MutableList<MapPos>>()
            for (j in 0 until mapPosArr.size) {
                val basicX = 0.5
                val basicY = 0.5

                val x = BigDecimal(mapPosArr[j].x)
                val y = BigDecimal(mapPosArr[j].y)

                val mapPosArr = mutableListOf<MapPos>()
                for (i in 1 until 4) {
                    var addPoint: MapPos? = null
                    when (i) {
                        1 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
                        2 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble(), 0.0)
                        3 -> addPoint = MapPos(x.toDouble(), y.toDouble() + basicY, 0.0)
                    }
                    mapPosArr.add(addPoint!!)
                }
                mapPosArray.add(mapPosArr)
            }

            val convertWGS84LatLngArr = mutableListOf<ArrayList<LatLng>>()
            var convertWGS84Coord: Point2D.Double?
            for (k in 0 until mapPosArray.size) {
                val latLngArr = ArrayList<LatLng>()
                for (l in 0 until mapPosArray[k].size) {
                    convertWGS84Coord = convertWGS84(mapPosArray[k][l].x, mapPosArray[k][l].y)
                    latLngArr.add(LatLng(convertWGS84Coord.y, convertWGS84Coord.x))
                }
                convertWGS84LatLngArr.add(latLngArr)
            }

            makePolygonArr = convertWGS84LatLngArr

        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }

        return makePolygonArr
    }

    /**
     * ????????? ??????????? ??????
     */
    fun drawLineOrPolygonObject(): MutableList<ArrayList<LatLng>>{
        try {
            val getLatLng = ArrayList<LatLng>()

            for (i in 0 until LandInfoObject.mapPos.size) {
                val convertWGS84Coord = convertWGS84(LandInfoObject.mapPos[i].x, LandInfoObject.mapPos[i].y)
                getLatLng.add(LatLng(convertWGS84Coord.y, convertWGS84Coord.x))
            }
            makePolygonArr.add(getLatLng)

        } catch (e: Exception) {
            logtUtil.e(e.toString())
        }
        return makePolygonArr
    }


    /**
     * ????????? ????????? ????????? ????????? ??????
     */

    fun setMarkerPointToPolygon() {
        val basicX = 0.5
        val basicY = 0.5

        val mapPos = LandInfoObject.mapPos

        val x = BigDecimal(mapPos[0].x)
        val y = BigDecimal(mapPos[0].y)

//        for (i in 1 until 4) {
//            var addPoint: MapPos? = null
//            when (i) {
//                1 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//                2 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//                3 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//            }
//
//            LandInfoObject.mapPos.add(addPoint!!)
//            val wgs84Point = cartoProj.toWgs84(addPoint)
//
//            LandInfoObject.clickLatLng.add(wgs84Point)
//        }
        var addPoint: MapPos? = null
//        when (i) {
//            1 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//            2 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//            3 -> addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
//        }
        addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
        LandInfoObject.mapPos.add(addPoint!!)
        val wgs84Point = cartoProj.toWgs84(addPoint)

        LandInfoObject.clickLatLng.add(wgs84Point)
    }

    /**
     * ????????? ????????? ??????????????? ????????? ??????
     */

    fun setMarkerLineToPolygon() {

        val mapPos = LandInfoObject.mapPos

        for (i in 0 until mapPos.size) {
            val x = BigDecimal(mapPos[i].x)
            val y = BigDecimal(mapPos[i].y)

            var addPoint: MapPos?

            addPoint = MapPos(x.toDouble(), y.toDouble(), 0.0)
            LandInfoObject.mapPos.add(addPoint)
            val wgs84Point = cartoProj.toWgs84(addPoint)
            LandInfoObject.clickLatLng.add(wgs84Point)
        }
    }


    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
        when (type) {
            "???????????????" -> cancel()
        }
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        dialog.dismiss()
    }

}