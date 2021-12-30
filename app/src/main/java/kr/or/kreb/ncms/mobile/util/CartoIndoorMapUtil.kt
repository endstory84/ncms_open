/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.content.Context
import com.carto.ui.MapView


class CartoIndoorMapUtil(context: Context, activity: Activity, mapView: MapView, naverMap: NaverMapUtil) {

    /** 미사용 **/

    /*// App Util
    private lateinit var mGpsUtil: GPSUtil
    private lateinit var logUtil: LogUtil
    private lateinit var mToastUtil: ToastUtil
    private lateinit var mPrefUtil: PreferenceUtil
    private lateinit var mMathUtil: MathUtil
    private var mNaverMapUtil: NaverMapUtil? = naverMap

    // Common Variable
    var mContext: Context? = context
    private var mActivity: Activity? = activity
    private lateinit var mSketchEnumMode: SketchEnum // 스케치 모드

    // Carto
    var mCartoIndoorMapView: MapView? = mapView
    private lateinit var mIndoorOpt: Options
    private var mCartoIndoorProjetion: Projection? = null
    private var mIndoorSketchAreaDataSource: LocalVectorDataSource? = null
    private var mIndoorDistanceAreaDataSource: LocalVectorDataSource? = null
    private var mIndoorLocalVectorText: LocalVectorDataSource? = null

    var mIndoorMapPosVector: MapPosVector? = null
    var mIndoorEditLayer: EditableVectorLayer? = null
    private var mIndoorClickLatLng = mutableListOf<MapPos>()
    private var mIndoorClickMapPos = mutableListOf<MapPos>()
    private var mIndoorClickRedoMapPos = mutableListOf<MapPos>() // 임시 MapPos (redo)
    private var mIndoorMapControlLayers = mutableListOf<VectorLayer>()
    private var mIndoorAddMapControlLayers = mutableListOf<VectorLayer>()
    private var mIndoorAddMapPosVectorArr = mutableListOf<MapPosVector>()
    private var _isModify = false
    private val radius = 6371009.0

    // Carto Style
    private var polyTextBuilderStyle: TextStyle? = null
    private var centerTx: Text? = null
    private var lineCenterTxList = mutableListOf<Text>()

    // Carto Listener
    private var editListener = EditEventListener()
    private var selectListener = VectorElementSelectEventListener()

    var makePolygonArr = mutableListOf<ArrayList<LatLng>>()

    init {
        init()
    }

    private fun init() {
        mGpsUtil = GPSUtil(mContext!!)
        logUtil = LogUtil("CartoMap")
        mToastUtil = ToastUtil(mContext!!)
        mPrefUtil = PreferenceUtil(mContext!!)
        mMathUtil = MathUtil

        mIndoorMapPosVector = MapPosVector()

        logUtil.d("CartoIndoorMapUtil.....................................init")
        mPrefUtil.setString("cartoMapType","indoor") // 실내 스케치

        mActivity!!.toggleButtonBaseMap.goneView()
        mActivity!!.toggleButtonHybrid.goneView()
        mActivity!!.toggleButtonCadstral.goneView()
        mActivity!!.layoutMapLeftButtonGroup.goneView()

        //(mActivity!! as MapActivity).toggleFab(false) // 스케치 툴바 toggle
        this.mCartoIndoorMapView?.visibleView()


        mIndoorOpt = mCartoIndoorMapView?.options!!
        mCartoIndoorProjetion = mIndoorOpt.baseProjection

        mIndoorOpt.apply{
            zoomRange = MapRange(20f, 20f) // 줌 지정
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
            tileThreadPoolSize = 2
        }

        mCartoIndoorMapView!!.apply{
            holder.setFormat(android.graphics.PixelFormat.TRANSPARENT) // 맵 투명화
            isHorizontalFadingEdgeEnabled = false
            isHorizontalScrollBarEnabled = false
            isVerticalFadingEdgeEnabled = false
            isVerticalScrollBarEnabled = false
            isSoundEffectsEnabled = false
            mapEventListener = CartoIndoorMapEventListener()
        }

        mIndoorSketchAreaDataSource = LocalVectorDataSource(mCartoIndoorProjetion!!)
        mIndoorLocalVectorText = LocalVectorDataSource(mCartoIndoorProjetion!!)

        setIndoorSketchPosition()
    }

    *//**
     * 실내배치도 레이어 스케치 모듈화
     *//*
    fun setMode(sketchEnumMode:SketchEnum){
        mSketchEnumMode = sketchEnumMode
        sketchFunc()
    }

    *//**
     * 이전, 다음, 수정, 삭제, 추가, 폴리곤(필지도면) 생성
     *//*
    private fun sketchFunc() {
        when(mSketchEnumMode){
            SketchEnum.UNDO -> undo()
            SketchEnum.REDO -> redo()
            SketchEnum.MODIFY -> modify()
            SketchEnum.REMOVE -> remove()
            SketchEnum.ADD -> add()
            else -> {
                logUtil.d("실내 스케치 완료")
            }
        }
    }

    *//**
     * 실내지도 최초 (0, 0 좌표 이동)
     *//*
    private fun setIndoorSketchPosition(){
        mCartoIndoorMapView?.setFocusPos(mCartoIndoorProjetion?.fromWgs84(MapPos(0.0, 0.0)), 0f)
        mCartoIndoorMapView!!.setZoom(20f, 0f) // 20레벨 지정 (네이버 맵과 일치)
    }

    *//**
     * 포인트 스타일
     *//*
    private fun getPointStyle(): PointStyle? {
        val pStyleBuilder = PointStyleBuilder()
        pStyleBuilder.apply {
            color = Color(setObjectColor(mContext!!, R.color.green, 255))
            size = 7.0f
        }
        return pStyleBuilder.buildStyle()
    }

    *//**
     * 폴리곤 스타일
     *//*
    private fun getPolygonStyle(): PolygonStyle? {

        // 텍스트
        val polyTextStyleBuilder = TextStyleBuilder()
        polyTextStyleBuilder.apply {
            fontSize = 20f
            color = Color(setObjectColor(mContext!!, R.color.red, 255))
        }
        polyTextBuilderStyle = polyTextStyleBuilder.buildStyle()

        // 외곽선
        val polyLineStyleBuilder = LineStyleBuilder()
        polyLineStyleBuilder.apply {
            width = 1.0f
            color = Color(setObjectColor(mContext!!, R.color.orange, 255))
            lineEndType = LineEndType.LINE_END_TYPE_NONE
        }

        // 채우기
        val polyLineStyleBuilderStyle = polyLineStyleBuilder.buildStyle()
        val polyStyleBuilder = PolygonStyleBuilder()
        polyStyleBuilder.apply {
            color = Color(setObjectColor(mContext!!, R.color.orange, 100))
            lineStyle = polyLineStyleBuilderStyle
        }

        return polyStyleBuilder.buildStyle()
    }

    *//**
     * 오브젝트 초기화
     *//*
    private fun empty(){

        mCartoIndoorMapView!!.layers.remove(mIndoorEditLayer)

        mIndoorSketchAreaDataSource!!.clear()
        mIndoorSketchAreaDataSource = LocalVectorDataSource(mCartoIndoorProjetion!!)
        mIndoorMapPosVector!!.clear()
        mIndoorClickMapPos.clear()
        mIndoorClickLatLng.clear()
        lineCenterTxList.clear()
    }

    *//**
     * 이전
     *//*
    fun undo(){

        if(mIndoorClickMapPos.size > 0){

            mIndoorClickRedoMapPos.add(mIndoorClickMapPos.removeAt(mIndoorClickMapPos.size -1))
            mIndoorMapPosVector?.clear()

            mIndoorClickMapPos.forEach { mapPos -> mIndoorMapPosVector?.add(mapPos) }
            indoorSketchDistanceArea()
        } else {
            mToastUtil.msg_warning(mContext?.resources?.getString(R.string.msg_undo)!!, 100)
        }
    }

    *//**
     * 다음
     *//*
    fun redo(){

        if(mIndoorClickRedoMapPos.size > 0){

            mIndoorClickMapPos.add(mIndoorClickRedoMapPos.removeAt(mIndoorClickRedoMapPos.size - 1))
            mIndoorMapPosVector?.clear()

            mIndoorClickMapPos.forEach { mapPos -> mIndoorMapPosVector?.add(mapPos) }
            indoorSketchDistanceArea()
        } else {
            mToastUtil.msg_warning(mContext?.resources?.getString(R.string.msg_redo)!!, 500)
        }
    }

    *//** 수정 *//*
    fun modify(){
        _isModify = true

        mIndoorEditLayer?.vectorEditEventListener  = editListener
        mIndoorEditLayer?.vectorElementEventListener  = selectListener
        //mCartoMapView?.mapEventListener = deselectListener

        mToastUtil.msg_info(mContext?.resources?.getString(R.string.msg_action_modfity)!!, 100)
    }

    *//** 삭제 *//*
    fun remove(){
        empty()
        mToastUtil.msg_success(mContext?.resources?.getString(R.string.msg_action_remove)!!, 100)
    }

    *//**
     * 추가
     *//*
    fun add(){

        if(mCartoIndoorMapView!!.layers.count() == 1){
            mCartoIndoorMapView!!.layers.clear()
        }

        //val mIndoorAddMapPosArr = mutableListOf<List<MapPos>>()
        //mIndoorAddMapPosArr.add(mIndoorClickMapPos)

        mIndoorAddMapPosVectorArr.add(mIndoorMapPosVector!!)

        mIndoorAddMapPosVectorArr.forEach { mapPosVector ->

            val addPolygon = Polygon(mapPosVector, getPolygonStyle())
            logUtil.d(addPolygon.poses.toString())
            val addPolygonLayerSource = LocalVectorDataSource(mCartoIndoorProjetion)
            addPolygonLayerSource.add(addPolygon)

            val addVectorLayer = VectorLayer(addPolygonLayerSource)
            mCartoIndoorMapView!!.layers.insert(mCartoIndoorMapView!!.layers.count(), addVectorLayer)
            empty()
        }

        mToastUtil.msg("추가된 레이어: ${mCartoIndoorMapView!!.layers.count()}", 100)
    }

    fun polygon(): MutableList<ArrayList<LatLng>> {

        removeIndoorDistanceArea()

        val getLatLng = ArrayList<LatLng>()
        val addVectorLayer = VectorLayer(mIndoorDistanceAreaDataSource)
        mCartoIndoorMapView!!.layers.insert(mCartoIndoorMapView!!.layers.count(), addVectorLayer)

        if(LandInfoObject.mapPos.size > 2){
            for (i in 0 until LandInfoObject.mapPos.size) {
                logUtil.d("getCartoWgs84() ---------------------> ")
                getLatLng.add(LatLng(LandInfoObject.clickLatLng[i].y, LandInfoObject.clickLatLng[i].x))
            }
            // 2번째 배열 진행
            //mLatLngArrFirstDepth.add(mLatLngArrSecondDepth)
            makePolygonArr.add(getLatLng)

            logUtil.d("makePolygonArr Size -> ${makePolygonArr.size}")
        }

        //empty()
        return makePolygonArr
    }

    *//** 선택모드 *//*
    fun select(element: VectorElement?) { mIndoorEditLayer?.selectedVectorElement = element; logUtil.d("select") }

    *//** 선택모드 해제 *//*
    fun deselect( ) {
        //editLayer?.selectedVectorElement = null
        //editLayer?.vectorEditEventListener  = editListener
        //mCartoMapView?.mapEventListener = null
        logUtil.d("deselect")
    }

    *//**
     * Catro (실내 배치도) 스케치 표현
     *//*
    fun indoorSketchDistanceArea() {

        // 초기화
        removeIndoorDistanceArea()

        mIndoorSketchAreaDataSource!!.clear()

        for(pos in mIndoorClickMapPos) {
            val point = Point(pos, getPointStyle())
            mIndoorSketchAreaDataSource!!.add(point)
        }

        val ply = Polygon(mIndoorMapPosVector, getPolygonStyle())
        val mapCenter = ply.bounds.center

        mIndoorSketchAreaDataSource!!.add(ply)

        // 거리
        if(mIndoorClickLatLng.size >= 1) setDistanceLineText("sketch")

        // 면적
        if (mIndoorClickLatLng.size > 2) setDistancePolyText(mapCenter,"sketch")

        mIndoorEditLayer = EditableVectorLayer(mIndoorSketchAreaDataSource)

        mCartoIndoorMapView!!.layers.add(mIndoorEditLayer)
        mIndoorMapControlLayers.add(mIndoorEditLayer!!)
    }

    *//**
     * 거리 Text
     *//*
    private fun setDistanceLineText(type:String) {
        lineCenterTxList.clear()
        for (i in 1..mIndoorClickMapPos.size) {
            var lineCenterPos: MapPos?
            var lineCenterTx: Text?

            if (i == mIndoorClickMapPos.size) {
                lineCenterPos = MapPos((mIndoorClickMapPos[i-1].x + mIndoorClickMapPos[0].x)/2, (mIndoorClickMapPos[i-1].y + mIndoorClickMapPos[0].y)/2)
                lineCenterTx = Text(lineCenterPos, polyTextBuilderStyle,"${(mMathUtil.getLineDistance(mIndoorClickLatLng[i - 1], mIndoorClickLatLng[0], radius) * 100.0 / 100.0).roundToInt()}m")
            } else {
                lineCenterPos = MapPos((mIndoorClickMapPos[i-1].x + mIndoorClickMapPos[i].x)/2, (mIndoorClickMapPos[i-1].y + mIndoorClickMapPos[i].y)/2)
                lineCenterTx = Text(lineCenterPos, polyTextBuilderStyle,"${(mMathUtil.getLineDistance(mIndoorClickLatLng[i - 1], mIndoorClickLatLng[i], radius) * 100.0 / 100.0).roundToInt()}m")
            }
            lineCenterTxList.add(lineCenterTx)

            // 스케치
            if (type =="sketch") mIndoorSketchAreaDataSource!!.add(lineCenterTx)
        }

        // 편집
        if(type =="modify") for(i in 0 until lineCenterTxList.size) mIndoorSketchAreaDataSource!!.add(lineCenterTxList[i])
    }

    *//**
     * 면적 Text
     *//*
    private fun setDistancePolyText(mapcenter: MapPos?, type:String) {

        // 편집
        if(type =="modify") mIndoorSketchAreaDataSource!!.remove(centerTx)

        centerTx = Text(mapcenter, polyTextBuilderStyle,"${(mMathUtil.layersForArea(mIndoorClickLatLng, radius) * 100.0 / 100.0).roundToInt()}m")
        mIndoorSketchAreaDataSource!!.add(centerTx)


    }

    *//**
     * 레이어 초기화
     *//*

    private fun removeIndoorDistanceArea() {
        mIndoorSketchAreaDataSource!!.clear() // Layer 내 datasource 초기화
        removeIndoorMapControlLayers(mCartoIndoorMapView!!) //추가한 vectorlayer 삭제
    }

    private fun removeIndoorMapControlLayers(mapView: MapView) {
        for (i in mIndoorMapControlLayers.indices) {
            mapView.layers.remove(mIndoorMapControlLayers[i])
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inner class CartoIndoorMapEventListener : MapEventListener() {

        override fun onMapClicked(mapClickInfo: MapClickInfo?) {
            super.onMapClicked(mapClickInfo)

            val pos3857 = mapClickInfo!!.clickPos
            val posWGS84 = mCartoIndoorProjetion!!.toWgs84(pos3857)

            logUtil.d("Indoor MapPos -> $posWGS84")

            when(mapClickInfo.clickType){

                ClickType.CLICK_TYPE_SINGLE -> {
                    logUtil.d("carto indoor single click !")

                    mIndoorClickLatLng.add(posWGS84)
                    mIndoorClickMapPos.add(pos3857)
                    mIndoorMapPosVector?.add(pos3857)
                    indoorSketchDistanceArea()

                }

                ClickType.CLICK_TYPE_DOUBLE -> {
                    logUtil.d("carto indoor double click !")
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

    inner class VectorElementSelectEventListener internal constructor() : VectorElementEventListener() {
        override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
            select(clickInfo?.vectorElement)
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

                for (i in 0 until mIndoorClickMapPos.size) {
                    logUtil.d("onElementModify lineCenterTx remove")
                    logUtil.d("onElementModify lineCenterTx remove mClickMapPos =" + i +"size =" + mIndoorClickMapPos.size)
                    logUtil.d("onElementModify lineCneterTx Text size" + lineCenterTxList.size)
                    logUtil.d("onElementModify lineCneterTx Text" + lineCenterTxList[i].text.toString())

                    mIndoorSketchAreaDataSource!!.remove(lineCenterTxList[i])
                    //mLocalVectorText!!.clear()

                }
                mIndoorClickLatLng.clear()
                mIndoorClickMapPos.clear()

                for (i in 0 until element.geometry.poses.size()) {
                    logUtil.d("element geometry size i ->" + i.toInt())
                    logUtil.d("ElementModify element geometry X->" + element.geometry.poses.get(i.toInt()).x)
                    logUtil.d("ElementModify element geometry Y->" + element.geometry.poses.get(i.toInt()).y)
//                    mCartoMapView!!.options.baseProjection.toWgs84(mapClickInfo.clickPos)
                    val posWGS84 = mCartoIndoorProjetion!!.toWgs84(element.geometry.poses[i.toInt()])
                    val pos3857 = mCartoIndoorProjetion!!.fromLatLong(
                        element.geometry.poses.get(i.toInt()).x,
                        element.geometry.poses.get(i.toInt()).y
                    )


                    logUtil.d("ElementModify element geometry PosWGS84 X->" + posWGS84.x)
                    logUtil.d("ElementModify element geometry PosWGS84 Y->" + posWGS84.y)

                    mIndoorClickLatLng.add(posWGS84)
                    mIndoorClickMapPos.add(pos3857)

                }

                val mapCenter = element.bounds.center
                setDistanceLineText("modify") //거리
                if (mIndoorClickLatLng.size > 2) setDistancePolyText(mapCenter,"modify") //면적

            }
        }

        override fun onElementDeselected(element: VectorElement?) {
            _isModify = false
            super.onElementDeselected(element)
        }

        override fun onElementSelect(element: VectorElement?): Boolean {
            _isModify = true
            if(element is Polygon ) {
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
            logUtil.d("onDragEnd")
            return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

        }

        override fun onSelectDragPointStyle(
            element: VectorElement?,
            dragPointStyle: VectorElementDragPointStyle
        ): PointStyle? {
            if (null == styleNormal) {
                val builder = PointStyleBuilder()
                builder.color = Color(setObjectColor(mContext!!, R.color.red, 255))
                builder.size = 10f
                styleNormal = builder.buildStyle()
                builder.size = 10f
                styleVirtual = builder.buildStyle()
                builder.color = Color(setObjectColor(mContext!!, R.color.yellow, 255))
                builder.size = 10f
                styleSelected = builder.buildStyle()
            }

            if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_SELECTED) {
                return styleSelected
            }
            return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) {
                styleVirtual
            } else styleNormal
        }
    }*/

}