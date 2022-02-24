/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.PixelFormat
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
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.SketchEnum
import kr.or.kreb.ncms.mobile.fragment.FarmSearchFragment
import kr.or.kreb.ncms.mobile.fragment.LandSearchFragment
import kr.or.kreb.ncms.mobile.listener.ThingViewPagerInterface
import java.math.BigDecimal
import kotlin.math.log
import kotlin.math.roundToInt

class CartoMapUtil : DialogUtil.ClickListener {

    // App Util
    private lateinit var gpsUtil: GPSUtil
    lateinit var logtUtil: LogUtil
    private lateinit var toastUtil: ToastUtil
    private lateinit var mathUtil: MathUtil
    private lateinit var dialogUtil: DialogUtil
    lateinit var dialogBuilder: MaterialAlertDialogBuilder

    // Common Variable
    var context: Context? = null
    private var activity: Activity? = null
    private lateinit var sketchEnumMode: SketchEnum // 스케치 모드
    var mFarmFragment: FarmSearchFragment? = null

    // Carto
    var cartoMapView: MapView? = null
    private lateinit var mapOpt: Options
    var cartoProj: Projection? = null
    private var distanceAreaDataSource: LocalVectorDataSource? = null
    private var locatVectorText: LocalVectorDataSource? = null

    private var mClickRedoMapPos = mutableListOf<MapPos>() // 임시 MapPos (redo)
    private var mapControlLayers = mutableListOf<VectorLayer>()

    var makePolygonArr = mutableListOf<ArrayList<LatLng>>()
    var mapPosVector: MapPosVector? = null
    var mCatroEditLayer: EditableVectorLayer? = null

    private var _isModify = false
    private val radius = 6371009.0

    // Carto Style
    private var polyTextBuilderStyle: TextStyle? = null
    private var centerTx: Text? = null

    // Carto Listener
    private var editListener = EditEventListener()
    private var selectListener = VectorElementSelectEventListener()
    var deselectListener = VectorElementDeselectListener()

    // Naver
    private var naverMinPos: MapPos? = null
    private var naverMaxPos: MapPos? = null
    private var naverMapUtil: NaverMapUtil? = null
    private var coord: Coord? = null

    private var searchType: BizEnum? = null
    private var sktechType: Int = -1
    private var thingListener: ThingViewPagerInterface? = null
    var currentArea: Int? = null

    var _isSketchDrawType: String = "LINE"

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    constructor(context: Context, activity: Activity, mapView: MapView?) {
        this.context = context; this.activity = activity; this.cartoMapView = mapView; init()
    }

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        minPos: MapPos,
        maxPos: MapPos
    ) {
        this.context = context; this.activity = activity; this.cartoMapView =
            mapView; this.naverMinPos = minPos; this.naverMaxPos = maxPos; init()
    }

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        naverMap: NaverMapUtil,
        coord: Coord
    ) {
        this.context = context; this.activity = activity; this.cartoMapView =
            mapView; this.naverMapUtil = naverMap; this.coord = coord; init()
    }

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        naverMap: NaverMapUtil,
        coord: Coord,
        searchType: BizEnum
    ) {
        this.context = context; this.activity = activity; this.cartoMapView =
            mapView; this.naverMapUtil = naverMap; this.coord = coord; this.searchType =
            searchType; init()
    }

    constructor(
        context: Context,
        activity: Activity,
        mapView: MapView,
        naverMap: NaverMapUtil,
        coord: Coord,
        searchType: BizEnum,
        fragment: FarmSearchFragment?
    ) {
        this.context = context; this.activity = activity; this.cartoMapView =
            mapView; this.naverMapUtil = naverMap; this.coord = coord; this.searchType =
            searchType; this.mFarmFragment = fragment; init()
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
        this.context = context; this.activity = activity; this.cartoMapView =
            mapView; this.naverMapUtil = naverMap; this.coord = coord; this.searchType =
            searchType; this.sktechType = sketchType; this.thingListener = thingListener; init()
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
        PreferenceUtil.setString(context!!, "cartoMapType", "default") // 조서 필지 스케치

        val overlayBitmap = BitmapUtils.loadBitmapFromAssets("img_transparent.png")

        mapOpt = cartoMapView?.options!!
        cartoProj = mapOpt.baseProjection

        val mMinPos = cartoProj?.fromWgs84(naverMapUtil!!.getMinPos())
        val mMaxPos = cartoProj?.fromWgs84(naverMapUtil!!.getMaxPos())

        val bounds = MapBounds(mMinPos, mMaxPos)
        val screenBounds = ScreenBounds(
            ScreenPos(0f, 0f),
            ScreenPos(
                naverMapUtil!!.naverMap.contentWidth.toFloat(),
                naverMapUtil!!.naverMap.contentHeight.toFloat()
            )
        )

        /**
         * 2021-05-17  레벨 20 지정 (Naver Map Lv ==  Carto Map Lv)
         */

        mapOpt.apply {
            backgroundBitmap = overlayBitmap
            zoomRange = MapRange(20f, 20f) // 줌 지정
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
            tileThreadPoolSize = 2
        }

        cartoMapView!!.apply {
            holder.setFormat(PixelFormat.TRANSPARENT) // 맵 투명화
            moveToFitBounds(bounds, screenBounds, false, 0.5f)
            setFocusPos(
                cartoProj?.fromWgs84(MapPos(naverMapUtil!!.lon, naverMapUtil!!.lat)),
                0f
            ) // 현재 위치 이동
            isHorizontalFadingEdgeEnabled = false
            isHorizontalScrollBarEnabled = false
            isVerticalFadingEdgeEnabled = false
            isVerticalScrollBarEnabled = false
            isSoundEffectsEnabled = false
            mapEventListener = CartoMapEventListener()
        }

        distanceAreaDataSource = LocalVectorDataSource(cartoProj!!)
        locatVectorText = LocalVectorDataSource(cartoProj!!)

        //LandInfoObject.lineCenterTxList = mutableListOf()
    }

    /**
     * 레이어 스케치 모듈화
     */
    fun setMode(sketchEnumMode: SketchEnum) {
        this.sketchEnumMode = sketchEnumMode
        sketchFunc()
    }

    /**
     * 이전, 다음, 수정, 삭제, 취소(네이버맵 전환), 폴리곤(필지도면) 생성
     */
    private fun sketchFunc() {
        when (sketchEnumMode) {
            SketchEnum.UNDO -> undo()
            SketchEnum.REDO -> redo()
            SketchEnum.POINT -> point()
            SketchEnum.LINE -> line()
            SketchEnum.MODIFY -> modify(mCatroEditLayer)
            SketchEnum.CANCEL -> {

                dialogUtil.run {
                    alertDialog(
                        "스케치 모드",
                        "스케치모드에서 작업중인 내용은 초기화됩니다. \n 스케치 모드에서 나가시겠습니까?",
                        dialogBuilder,
                        "스케치모드"
                    ).show()
                }
            }
            SketchEnum.REMOVE -> remove()
            else -> {

                val getPolygonArray = polygon() //Carto에서 그려진 폴리곤의 data

                try {
                    if (getPolygonArray.size > 0) {

                        for (i in getPolygonArray.indices) {
                            naverMapUtil!!.setNaverMapPolygon(getPolygonArray[i], searchType)
                        }


                        currentArea = (mathUtil.layersForArea(
                            LandInfoObject.clickLatLng,
                            radius
                        ) * 100.0 / 100.0).roundToInt() // 실제이용현황 면적

                        // TODO: 2021-09-01 모든 객체 클래스화 진행 (리스너 삭제)
                        when (searchType) {
                            BizEnum.LAD -> {
                                LandInfoObject.selectPolygonCurrentArea = LandInfoObject.currentArea
                                LandInfoObject.currentArea = currentArea
                                logtUtil.d("실제이용현황 변경 이전 면적 : ${LandInfoObject.selectPolygonCurrentArea}")
                                logtUtil.d("실제이용현황 면적 : $currentArea")

                                /**
                                 *  실제이용현황 편집모드 진일 했을 시에 update 실행
                                 *  그 외 add 실행
                                 */
                                if (!LandInfoObject._isPolygonVisible) {
                                    LandSearchFragment(activity, context).landRealAdd()
                                } else {
                                    LandSearchFragment(activity, context).landRealUpdate()
                                }
                                empty()
                            }
                            BizEnum.THING -> {
//                                if(mSketchType == 0) {
//                                    mThingListener!!.loadViewPage()
//                                }
                            }
                            BizEnum.TOMB -> {

                            }
                            BizEnum.FARM -> {
                                logtUtil.d("농업입니다.")
                                logtUtil.d("농업 필지면적 -> $currentArea")
                                mFarmFragment?.addTableRow(currentArea)
                                //removeDrawDistanceArea()
                                empty()
                            }
                            else -> logtUtil.d("none")
                        }

                    }
                } catch (e: Exception) {
                    logtUtil.d(e.toString())
                }
            }
        }
    }

    /** 촤초 마커포인트 */
    fun initStartPoint() {

        when (searchType) {
            BizEnum.LOTMAP -> {

            }
            BizEnum.LAD -> {

            }
            BizEnum.THING -> {
//                if(LandInfoObject.clickLatLng.size == 0) {
//                    LandInfoObject.clickLatLng.add(0, MapPos((mCoord as LatLng).longitude, (mCoord as LatLng).latitude)) // 네이버 마커위치 최초 포인트 지정
//                }
//
//                if(LandInfoObject.mapPos.size == 0){
//                    LandInfoObject.mapPos.add(0, mCartoMapView!!.options.baseProjection.fromLatLong((mCoord as LatLng).latitude, (mCoord as LatLng).longitude)) // WGS84 -> EPSG:3857 변경
//                    mPosVector?.add(LandInfoObject.mapPos[0]) // Catro 스케치 라인 최초 포인트 지정
//                }

            }
            BizEnum.BSN -> {

            }
            BizEnum.FARM -> {

            }
            BizEnum.RESIDNT -> {

            }
            BizEnum.TOMB -> {

            }
            BizEnum.MINRGT -> {

            }
            BizEnum.FYHTS -> {

            }
        }


    }

    /**
     * 오브젝트 초기화
     */
    fun empty() {
        if (mCatroEditLayer != null) {
            cartoMapView!!.layers.remove(mCatroEditLayer)

            distanceAreaDataSource!!.clear()
            distanceAreaDataSource = LocalVectorDataSource(cartoProj!!)
            mapPosVector!!.clear()

            LandInfoObject.clickLatLng.clear()
            LandInfoObject.mapPos.clear()
            LandInfoObject.lineCenterTxList.clear()

            mCatroEditLayer = null
            toastUtil.msg_success(context!!.getString(R.string.msg_action_remove), 100)
        } else {
            toastUtil.msg_error("스케치가 존재하지 않습니다.", 500)
            return
        }
    }

    /**
     * 이전
     */
    fun undo() {

        if (LandInfoObject.mapPos.size > 0) {

            mClickRedoMapPos.add(LandInfoObject.mapPos.removeAt(LandInfoObject.mapPos.size - 1))
            mapPosVector?.clear()

            LandInfoObject.mapPos.forEach { mapPos -> mapPosVector?.add(mapPos) }
            sketchDistanceArea()
        } else {
            toastUtil.msg_warning(context!!.getString(R.string.msg_undo), 100)
        }
    }

    /**
     * 다음
     */
    fun redo() {

        if (mClickRedoMapPos.size > 0) {

            LandInfoObject.mapPos.add(mClickRedoMapPos.removeAt(mClickRedoMapPos.size - 1))
            mapPosVector?.clear()

            LandInfoObject.mapPos.forEach { mapPos -> mapPosVector?.add(mapPos) }
            sketchDistanceArea()
        } else {
            toastUtil.msg_warning(context!!.getString(R.string.msg_redo), 500)
        }
    }

    /**
     * 스케치 유형 (점)
     */
    fun point() {
        logtUtil.d("point")
        toastUtil.msg_info("스케치 유형이 (점)으로 설정되었습니다.", 300)
        _isSketchDrawType = "POINT"
    }


    /**
     * 스케치 유형 (선)
     */
    fun line() {
        logtUtil.d("line")
        toastUtil.msg_info("스케치 유형이 (선)으로 설정되었습니다.", 300)
        _isSketchDrawType = "LINE"
    }

    /** 수정 */
    fun modify(editLayer: EditableVectorLayer?) {
        _isModify = true

        editLayer?.vectorEditEventListener = editListener
        editLayer?.vectorElementEventListener = selectListener
        //mCartoMapView?.mapEventListener = deselectListener

        toastUtil.msg_info(R.string.msg_action_modfity, 100)
    }

    /** 삭제 */
    fun remove() {
        empty()
        initStartPoint()
    }

    /** 취소 */
    fun cancel() {
        empty()
        initStartPoint()

        (context as MapActivity).run {
            layoutMapRightButtonGroup.visibleView()
            layoutMapLeftButtonGroup.visibleView()
            toggleFab(true)
            cartoMapView.goneView()
            fabVisableArr.forEach { obj -> obj.goneView() }
        }
    }

    /** 선택모드 */
    fun select(element: VectorElement?) {
        mCatroEditLayer?.selectedVectorElement = element; logtUtil.d("select")
    }


    /** 선택모드 해제 */
    fun deselect() {
        //editLayer?.selectedVectorElement = null
        //editLayer?.vectorEditEventListener  = editListener
        //mCartoMapView?.mapEventListener = null
        logtUtil.d("deselect")
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inner class CartoMapEventListener : MapEventListener() {

        override fun onMapClicked(mapClickInfo: MapClickInfo?) {
            super.onMapClicked(mapClickInfo)

            val pos3857 = mapClickInfo!!.clickPos
            val posWGS84 = cartoProj!!.toWgs84(pos3857)

            initStartPoint()

            when (mapClickInfo.clickType) {

                // 스케치
                ClickType.CLICK_TYPE_SINGLE -> {
                    LandInfoObject.clickLatLng.add(posWGS84)
                    LandInfoObject.mapPos.add(pos3857)
                    mapPosVector?.add(pos3857)
                    sketchDistanceArea()
                }
                // 선택모드 - 해제
                else -> {

                    //mCartoMapView!!.mapEventListener = VectorElementDeselectListener()
                }
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
     * 포인트 스타일
     */
    private fun getPointStyle(): PointStyle? {
        val pStyleBuilder = PointStyleBuilder()
        pStyleBuilder.apply {
            color = Color(setObjectColor(context!!, R.color.red, 255))
            size = 8.0f
        }
        return pStyleBuilder.buildStyle()
    }

    /**
     * 폴리곤 스타일
     */
    private fun getPolygonStyle(): PolygonStyle? {

        // 텍스트
        val polyTextStyleBuilder = TextStyleBuilder()
        polyTextStyleBuilder.apply {
            fontSize = 20f
            color = Color(setObjectColor(context!!, R.color.red, 255))
        }
        polyTextBuilderStyle = polyTextStyleBuilder.buildStyle()

        // 외곽선
        val polyLineStyleBuilder = LineStyleBuilder()
        polyLineStyleBuilder.apply {
            width = 1.0f
            color = Color(setObjectColor(context!!, R.color.green, 255))
            lineEndType = LineEndType.LINE_END_TYPE_NONE
        }

        // 채우기
        val polyLineStyleBuilderStyle = polyLineStyleBuilder.buildStyle()
        val polyStyleBuilder = PolygonStyleBuilder()
        polyStyleBuilder.apply {
            color = Color(setObjectColor(context!!, R.color.green, 80))
            lineStyle = polyLineStyleBuilderStyle
        }

        return polyStyleBuilder.buildStyle()
    }

    /**
     * Catro 스케치 표현
     * @param _isSketchDrawType 스케치유형 [POINT, LINE]
     */
    fun sketchDistanceArea() {

        var point: Point? = null

        // 초기화
        removeDrawDistanceArea()

        for (pos in LandInfoObject.mapPos) {
            point = Point(pos, getPointStyle())
            distanceAreaDataSource!!.add(point)
        }

        if (_isSketchDrawType == "LINE") {
            val ply = Polygon(mapPosVector, getPolygonStyle())
            LandInfoObject.mapCenter = ply.bounds.center
            distanceAreaDataSource!!.add(ply)
        } else {
            logtUtil.d("sketchDistanceArea Point val -> $point")
        }

        logtUtil.d("LandInfoObject.mapPos.size -> ${LandInfoObject.mapPos.size}")

        // 거리
        if (_isSketchDrawType == "LINE") {
            if (LandInfoObject.mapPos.size > 1) setDistanceLineText("sketch")
        }

        // 면적
        if (_isSketchDrawType == "LINE") {
            if (LandInfoObject.mapPos.size > 2) setDistancePolyText(
                LandInfoObject.mapCenter,
                "sketch"
            )
        }

        mCatroEditLayer = EditableVectorLayer(distanceAreaDataSource)

        cartoMapView!!.layers.add(mCatroEditLayer)
        mapControlLayers.add(mCatroEditLayer!!)
    }

    inner class VectorElementSelectEventListener internal constructor() :
        VectorElementEventListener() {
        override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
            select(clickInfo.vectorElement)
            return true
        }
    }

    inner class VectorElementDeselectListener : MapEventListener() {
        override fun onMapClicked(mapClickInfo: MapClickInfo?) {
            deselect()
        }
    }

    inner class EditEventListener : VectorEditEventListener() {

        private var styleNormal: PointStyle? = null
        private var styleVirtual: PointStyle? = null
        private var styleSelected: PointStyle? = null
        private var source: LocalVectorDataSource? = null

        override fun onElementModify(element: VectorElement?, geometry: Geometry?) {
            if (element is Point && geometry is PointGeometry) element.geometry = geometry
            if (element is Line && geometry is LineGeometry) element.geometry = geometry
            if (element is Polygon && geometry is PolygonGeometry) {

                element.geometry = geometry

                for (i in 0 until LandInfoObject.mapPos.size) {
                    //mLogUtil.d("onElementModify lineCenterTx remove")
                    //mLogUtil.d("onElementModify lineCenterTx remove LandInfoObject.mapPos = " + i + "size = " + LandInfoObject.mapPos.size)
                    //mLogUtil.d("onElementModify lineCneterTx Text size" + lineCenterTxList.size)
                    //mLogUtil.d("onElementModify lineCneterTx Text " + lineCenterTxList[i].text.toString())

                    //mDistanceAreaDataSource!!.remove(lineCenterTxList[i])
                    logtUtil.d("거리 arr size -> ${LandInfoObject.lineCenterTxList.size}")
                    //mDistanceAreaDataSource!!.remove(LandInfoObject.lineCenterTxList!![i])
                    //mLocalVectorText!!.clear()

                }
                LandInfoObject.clickLatLng.clear()
                LandInfoObject.mapPos.clear()

                for (i in 0 until element.geometry.poses.size()) {
                    logtUtil.d("element geometry size i ->" + i.toInt())
                    logtUtil.d("ElementModify element geometry X->" + element.geometry.poses.get(i.toInt()).x)
                    logtUtil.d("ElementModify element geometry Y->" + element.geometry.poses.get(i.toInt()).y)
//                    mCartoMapView!!.options.baseProjection.toWgs84(mapClickInfo.clickPos)
                    val posWGS84 = cartoProj!!.toWgs84(element.geometry.poses[i.toInt()])
                    val pos3857 = cartoProj!!.fromLatLong(
                        element.geometry.poses.get(i.toInt()).x,
                        element.geometry.poses.get(i.toInt()).y
                    )

                    logtUtil.d("ElementModify element geometry PosWGS84 X->" + posWGS84.x)
                    logtUtil.d("ElementModify element geometry PosWGS84 Y->" + posWGS84.y)

                    LandInfoObject.clickLatLng.add(posWGS84)
                    LandInfoObject.mapPos.add(pos3857)

                }

                LandInfoObject.mapCenter = element.bounds.center
                setDistanceLineText("modify") //거리
                if (LandInfoObject.clickLatLng.size > 2) setDistancePolyText(
                    LandInfoObject.mapCenter,
                    "modify"
                ) //면적
            }
        }

        override fun onElementDeselected(element: VectorElement?) {
            _isModify = false
            super.onElementDeselected(element)
        }

        override fun onElementSelect(element: VectorElement?): Boolean {
            _isModify = true
            if (element is Polygon) {
                element.poses.size()
            }
            return super.onElementSelect(element)
        }

        override fun onElementDelete(element: VectorElement?) {
            source!!.remove(element)
        }

        override fun onDragStart(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY
        }

        override fun onDragMove(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY
        }

        override fun onDragEnd(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
            logtUtil.d("onDragEnd")
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

        }

        override fun onSelectDragPointStyle(
            element: VectorElement?,
            dragPointStyle: VectorElementDragPointStyle
        ): PointStyle? {
            if (null == styleNormal) {
                val builder = PointStyleBuilder()
                builder.color = Color(setObjectColor(context!!, R.color.red, 255))
                builder.size = 20f
                styleNormal = builder.buildStyle()
                builder.size = 15f
                styleVirtual = builder.buildStyle()
                builder.color = Color(setObjectColor(context!!, R.color.yellow, 255))
                builder.size = 30f
                styleSelected = builder.buildStyle()
            }

            if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_SELECTED) {
                return styleSelected
            }
            return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) {
                styleVirtual
            } else styleNormal
        }
    }

    /**
     * 거리 Text
     */
    private fun setDistanceLineText(type: String) {
        LandInfoObject.lineCenterTxList.clear()
        for (i in 1..LandInfoObject.mapPos.size) {
            var lineCenterPos: MapPos?
            var lineCenterTx: Text?

            if (i == LandInfoObject.mapPos.size) {
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

            // 스케치
            if (type == "sketch") {
                distanceAreaDataSource!!.add(lineCenterTx)
            }
        }

        // 편집
        if (type == "modify") {
            for (i in 0 until LandInfoObject.lineCenterTxList.size) {
                distanceAreaDataSource!!.add(LandInfoObject.lineCenterTxList[i])
            }
        }
    }

    /**
     * 면적 Text
     */
    private fun setDistancePolyText(mapCenter: MapPos?, type: String) {

        // 편집
        if (type == "modify") distanceAreaDataSource!!.remove(centerTx)
        currentArea = (mathUtil.layersForArea(
            LandInfoObject.clickLatLng,
            radius
        ) * 100.0 / 100.0).roundToInt()

        centerTx = Text(mapCenter, polyTextBuilderStyle, "${currentArea}㎡")
        distanceAreaDataSource!!.add(centerTx)


    }

    fun setScreenSync() {
        logtUtil.d("setScreenSync-------------------------------<><>")

        val mMinPos = cartoProj?.fromWgs84(naverMapUtil!!.getMinPos())
        val mMaxPos = cartoProj?.fromWgs84(naverMapUtil!!.getMaxPos())

        val bounds = MapBounds(mMinPos, mMaxPos)
        val screenBounds = ScreenBounds(
            ScreenPos(0f, 0f),
            ScreenPos(
                naverMapUtil!!.naverMap.contentHeight.toFloat(),
                naverMapUtil!!.naverMap.contentWidth.toFloat()
            )
        )

        cartoMapView!!.moveToFitBounds(bounds, screenBounds, false, 0.5f)
        cartoMapView!!.setZoom(20f, 0f) // 20레벨 지정 (네이버 맵과 일치)
        cartoMapView!!.setFocusPos(
            cartoProj?.fromWgs84(
                MapPos(
                    naverMapUtil!!.lon,
                    naverMapUtil!!.lat
                )
            ), 0f
        ) // 현재 위치 이동
    }

    /**
     * 레이어 초기화
     */

    private fun removeDrawDistanceArea() {
        distanceAreaDataSource!!.clear() // Layer 내 datasource 초기화
        removeMapControlLayers(cartoMapView!!) //추가한 vectorlayer 삭제
    }

    private fun removeMapControlLayers(mapView: MapView) {
        for (i in 0 until mapView.layers.count()) {
            mapView.layers.remove(mapView.layers[i])
        }
    }

    /**
     * 사용자 폴리곤(스케치) 생성
     */
    fun polygon(): MutableList<ArrayList<LatLng>> {
        removeDrawDistanceArea() // 그려진 data 초기화

        val getLatLng = ArrayList<LatLng>()
        val addVectorLayer = VectorLayer(distanceAreaDataSource)
        cartoMapView!!.layers.insert(cartoMapView!!.layers.count(), addVectorLayer)

        if (LandInfoObject.mapPos.size == 1) {            // 포인트 정의
            setMarkerPointToPolygon()
        } else if (LandInfoObject.mapPos.size == 2) {     // 라인 정의
            if (_isSketchDrawType == "LINE") {
                setMarkerLineToPolygon()
            } else {
                setMarkerPointToPolygon()
            }
        }

        if (LandInfoObject.mapPos.size > 2) {
            /**
             * 선 유형일 경우
             */
            if (_isSketchDrawType == "LINE") {
                logtUtil.d("line :: MapPos Size -> ${LandInfoObject.mapPos.size}")
                for (i in 0 until LandInfoObject.mapPos.size) {
                    val convertWGS84Coord = convertWGS84(LandInfoObject.mapPos[i].x, LandInfoObject.mapPos[i].y)
                    getLatLng.add(LatLng(convertWGS84Coord.y, convertWGS84Coord.x))
                }
                makePolygonArr.add(getLatLng)

            /**
             * 점 유형일 경우
             */
            } else {
                logtUtil.d("point :: MapPos Size -> ${LandInfoObject.mapPos.size}")
                var tempMapPos: MapPos
                val tempMapPosArr = mutableListOf<MapPos>()

                for (i in 0 until LandInfoObject.mapPos.size) {
                    tempMapPos = LandInfoObject.mapPos[i]
                    tempMapPosArr.add(tempMapPos)
                }

                val mapPosArray = arrayListOf<MutableList<MapPos>>()
                for (j in 0 until tempMapPosArr.size) {
                    val basicX = 0.5
                    val basicY = 0.5

                    val x = BigDecimal(tempMapPosArr[j].x)
                    val y = BigDecimal(tempMapPosArr[j].y)

                    val mapPosArr = mutableListOf<MapPos>()
                    for (i in 1 until 4) {
                        var addPoint: MapPos? = null
                        when (i) {
                            1 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble(), 0.0)
                            2 -> addPoint = MapPos(x.toDouble(), y.toDouble() + basicY, 0.0)
                            3 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble() + basicY, 0.0)
                        }
                        mapPosArr.add(addPoint!!)
                    }
                    mapPosArray.add(mapPosArr)
                }

                val convertWGS84LatLngArr = mutableListOf<ArrayList<LatLng>>()
                var convertWGS84Coord: Point2D.Double?
                for (k in 0 until mapPosArray.size) {
                    val latLngArr = ArrayList<LatLng>()
                    //logtUtil.d(mapPosArray.toString())
                    for (l in 0 until mapPosArray[k].size) {
                        convertWGS84Coord = convertWGS84(mapPosArray[k][l].x, mapPosArray[k][l].y)
                        latLngArr.add(LatLng(convertWGS84Coord.y, convertWGS84Coord.x))
                    }
                    convertWGS84LatLngArr.add(latLngArr)
                }

                makePolygonArr = convertWGS84LatLngArr

            }

        }
        return makePolygonArr
    }


    /**
     * 기존에 그려져있던것을 편집해서 들어왔을시에 실행되는 method
     */
    fun drawPolygon(editPolygonArr: MutableList<ArrayList<LatLng>>) {

        removeDrawDistanceArea()

        val drawVectorSources = LocalVectorDataSource(cartoProj)
        val drawPolygonPoses = MapPosVector()

        editPolygonArr[0].forEach { editPolygon ->
            logtUtil.d(editPolygon.toString())
            //mCartoProjection?.fromWgs84(MapPos(editPolygon.longitude, editPolygon.latitude))?.let { LandInfoObject.mapPos.add(it) }

            val drawMapPos = MapPos(editPolygon.longitude, editPolygon.latitude)
            LandInfoObject.mapPos.add(drawMapPos)

            drawPolygonPoses.add(
                cartoProj?.fromWgs84(
                    MapPos(
                        editPolygon.longitude,
                        editPolygon.latitude
                    )
                )
            )
            //drawPolygonPoses.add(MapPos(editPolygon.longitude, editPolygon.latitude))
        }

        distanceAreaDataSource = drawVectorSources

        val drawPolygon = Polygon(drawPolygonPoses, getPolygonStyle())
        drawPolygon.setMetaDataElement("ClickText", Variant("Polygon"))
        drawVectorSources.add(drawPolygon)
        mCatroEditLayer = EditableVectorLayer(drawVectorSources)

        LandInfoObject.mapCenter = drawPolygon.bounds.center

        // 면적
        if (LandInfoObject.mapCenter != null) setDistancePolyText(
            LandInfoObject.mapCenter,
            "sketch"
        )

        cartoMapView!!.layers.insert(cartoMapView!!.layers.count(), mCatroEditLayer)

        modify(mCatroEditLayer)

    }

    fun setMarkerPointToPolygon() {
        val basicX = 0.5
        val basicY = 0.5

        val mapPos = LandInfoObject.mapPos

        val x = BigDecimal(mapPos[0].x)
        val y = BigDecimal(mapPos[0].y)

        for (i in 1 until 4) {
            var addPoint: MapPos? = null
            when (i) {
                1 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble(), 0.0)
                2 -> addPoint = MapPos(x.toDouble(), y.toDouble() + basicY, 0.0)
                3 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble() + basicY, 0.0)
            }

            LandInfoObject.mapPos.add(addPoint!!)
            val wgs84Point = cartoProj!!.toWgs84(addPoint)

            LandInfoObject.clickLatLng.add(wgs84Point)
        }
    }

    fun setMarkerLineToPolygon() {
        val basicX = 0.5
        val basicY = 0.5

        val mapPos = LandInfoObject.mapPos

        for (i in 0 until mapPos.size) {
            val x = BigDecimal(mapPos[i].x)
            val y = BigDecimal(mapPos[i].y)

            var addPoint: MapPos? = null

            when (i) {
                0 -> addPoint = MapPos(x.toDouble(), y.toDouble() + basicY, 0.0)
                1 -> addPoint = MapPos(x.toDouble() + basicX, y.toDouble() + basicY, 0.0)
            }

            LandInfoObject.mapPos.add(addPoint!!)
            val wgs84Point = cartoProj!!.toWgs84(addPoint)
            LandInfoObject.clickLatLng.add(wgs84Point)
        }
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
        when (type) {
            "스케치모드" -> cancel()
        }
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        dialog.dismiss()
    }

}