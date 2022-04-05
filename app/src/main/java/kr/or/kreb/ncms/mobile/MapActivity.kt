/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.WebMercatorCoord
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.PolygonOverlay
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.*
import kotlinx.android.synthetic.main.include_bizinfo.*
import kotlinx.android.synthetic.main.include_drawnavigation.*
import kotlinx.android.synthetic.main.include_drawnavigation_layer.*
import kotlinx.android.synthetic.main.include_mapsketch_toolbar.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.include_wtnnc.*
import kotlinx.android.synthetic.main.thing_dialog.view.*
import kotlinx.coroutines.*
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.databinding.ActivityMapWithDrawerlayoutBinding
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.BizEnum.*
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import kr.or.kreb.ncms.mobile.enums.GeoserverLayerEnum
import kr.or.kreb.ncms.mobile.enums.SketchEnum
import kr.or.kreb.ncms.mobile.fragment.*
import kr.or.kreb.ncms.mobile.listener.*
import kr.or.kreb.ncms.mobile.util.*
import kr.or.kreb.ncms.mobile.util.PermissionUtil.logUtil
import kr.or.kreb.ncms.mobile.view.InfoView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigDecimal


class MapActivity :
    BaseActivity<ActivityMapWithDrawerlayoutBinding>(R.layout.activity_map_with_drawerlayout, MapActivity::class.java.simpleName),
    View.OnClickListener,
    ContextDialogFragment.ContextDialogListener,
    ThingViewPagerInterface {

    var context = this
    var backPressedListener: OnBackPressedListener? = null

    // Naver
    var naverMap: NaverMapUtil? = null

    // Carto
    var cartoMap: CartoMapUtil? = null
    private lateinit var cartoMapType: String

    lateinit var menuItem: MenuItem
    var getCoord:String = ""
    var restData:String = ""

    var setContextPopupPosition: Int = 0
    var setcontextPopupName: String = ""

    // 시도, 시군구, 읍면동, 리, 연속지적도, 사업구역, 편집지적도, 토지, 지장물, 영업ㆍ축산업ㆍ잠업, 농업, 광업권, 어업권 거주자, 분묘 순
    var isSidoLayerChecked: Boolean = false
    var isSigunguLayerChecked: Boolean = false
    var isDongLayerChecked: Boolean = false
    var isLiLayerChecked: Boolean = false

    var isNaverCadastralLayerChecked: Boolean = false
    var isBsnsAreaLayerChecked: Boolean = true
    var isCadastralEditLayerpChecked: Boolean = false
    var isLadLayerChecked: Boolean = false
    var isLadRealLayerChecked: Boolean = false
    var isThingLayerChecked: Boolean = false
    var isBsnLayerChecked: Boolean = false
    var isFarmLayerChecked: Boolean = false
    var isResidntLayerChecked: Boolean = false
    var isTombLayerChecked: Boolean = false

   lateinit var sidoLayerSwitch: SwitchMaterial
   lateinit var sigunguLayerSwitch: SwitchMaterial
   lateinit var dongLayerSwitch: SwitchMaterial
   lateinit var riLayerSwitch: SwitchMaterial
   lateinit var naverCadstralLayerSwitch: SwitchMaterial
   lateinit var bsnsAreaLayerSwitch: SwitchMaterial
   lateinit var cadstralEditLayerSwitch: SwitchMaterial
   lateinit var ladLayerSwitch: SwitchMaterial
   lateinit var ladRealLayerSwitch: SwitchMaterial
   lateinit var thingLayerSwitch: SwitchMaterial
   lateinit var bsnLayerSwitch: SwitchMaterial
   lateinit var farmLayerSwitch: SwitchMaterial
   lateinit var residntLayerSwitch: SwitchMaterial
   lateinit var tombLayerSwitch: SwitchMaterial

    private var fab_open: Animation? = null
    private var fab_close:Animation? = null

    var fabVisableArr = mutableListOf<ExtendedFloatingActionButton>()
    private var fabArr = mutableListOf<ExtendedFloatingActionButton>()
    private var fabTranslationXArr = mutableListOf<Float>()

    //네트워크통신 다이얼로그
//    private var progressDialog: AlertDialog? = null

    val selectWtnccPolygonArr = mutableListOf<PolygonOverlay>()
    var choiceInfoWindowArr = mutableListOf<InfoWindow>()

    ////////////////////////////////////////////////////////////////////////////////////////

    var thingDataJson = JSONObject()
    var thingRequestData = JSONObject()
    var thingInfoData = JSONObject()

    var landUrl: String? = null
    var thingBuildUrl: String? = null
    var thingBsnUrl: String? = null
    var thingTombUrl: String? = null
    var thingMinrgtUrl: String? = null
    var thingFarmUrl: String? = null
    var thingResidntUrl: String? = null
    var thingFyhtsUrl: String? = null

    override fun initViewStart() {

        dialogUtil = DialogUtil(this, this)

        setToolBar(appToolbar)

        cartoMapView.goneView()
        cartoIndoorMapview.goneView()

        val naverMapFragment = findViewById<MapView>(R.id.naverMapView)
        getCoord = intent.extras?.get("coord").toString()
        restData = intent.extras?.get("restData").toString()


        logUtil.d("restData ---------------------------------> $restData")

        naverMap = NaverMapUtil(this, this, naverMapFragment, getCoord, restData)
        naverMap?.getMapView()?.getMapAsync(naverMap)

        toggleButtonBaseMap.apply { isChecked = true; text = null; textOn = null; textOff = null }
        toggleButtonHybrid.apply { isChecked = true; text = null; textOn = null; textOff = null }
        toggleButtonCadstral.apply { isChecked = true; text = null; textOn = null; textOff = null }
        toggleButtonLayer.apply { this!!.isChecked = true; text = null; textOn = null; textOff = null }

        // switch
        sidoLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_sido_layer).actionView as SwitchMaterial // 시도
        sigunguLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_sigungu_layer).actionView as SwitchMaterial // 시군구
        dongLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_dong_layer).actionView as SwitchMaterial // 읍면동
        riLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_li_layer).actionView as SwitchMaterial // 리경계
        naverCadstralLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_naverCadastral_layer).actionView as SwitchMaterial // 지적편집도(네이버)
        bsnsAreaLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_bsnArea_layer).actionView as SwitchMaterial // 사업구역(용지도)
        cadstralEditLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_cadastral_edit_layer).actionView as SwitchMaterial // 편집지적도

        ladLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_lad_layer).actionView as SwitchMaterial // 토지
        ladRealLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_lad_real_layer).actionView as SwitchMaterial //토지 실제이용
        thingLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_thing_layer).actionView as SwitchMaterial // 지장물
        bsnLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_bsn_layer).actionView as SwitchMaterial // 영업
        farmLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_farm_layer).actionView as SwitchMaterial // 농업
        residntLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_residnt_layer).actionView as SwitchMaterial // 거주자
        tombLayerSwitch = navigationViewLayer.menu.findItem(R.id.drawer_tomb_layer).actionView as SwitchMaterial // 분묘

        sidoLayerSwitch.isClickable = false
        sigunguLayerSwitch.isClickable = false
        dongLayerSwitch.isClickable = false
        riLayerSwitch.isClickable = false
        naverCadstralLayerSwitch.isClickable = false
        bsnsAreaLayerSwitch.isClickable = false
        cadstralEditLayerSwitch.isClickable = false
        ladLayerSwitch.isClickable = false
        ladRealLayerSwitch.isClickable = false
        thingLayerSwitch.isClickable = false
        bsnLayerSwitch.isClickable = false
        farmLayerSwitch.isClickable = false
        residntLayerSwitch.isClickable = false
        tombLayerSwitch.isClickable = false

        progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context))
        dialogBuilder = MaterialAlertDialogBuilder(this)
    }

    override fun initDataBinding() {}

    override fun initViewFinal() {

        // Drawer On/Off
//        appToolbar.setNigationOnClickListener {
//            layoutMapDrawerLayout.openDrawer(GravityCompat.START)
//            imageViewDrawerClose?.setOnClickListener { layoutMapDrawerLayout.closeDrawer(GravityCompat.START) }
//        }
//
//        appToolbar.setOnMenuItemClickListener { item -> logUtil.d(item.toString()); true }

        toggleButtonBaseMap.setOnCheckedChangeListener(ToggleButtonCheckedChangeListener(this, naverMap!!))
        toggleButtonHybrid.setOnCheckedChangeListener(ToggleButtonCheckedChangeListener(this, naverMap!!))
        toggleButtonCadstral.setOnCheckedChangeListener(ToggleButtonCheckedChangeListener(this, naverMap!!))
        toggleButtonLayer?.setOnCheckedChangeListener(ToggleButtonCheckedChangeListener(this, naverMap!!))

        Constants.BIZ_CATEGORY = PreferenceUtil.getString(applicationContext, "bizCategory","")
        Constants.BIZ_SUBCATEGORY = PreferenceUtil.getString(applicationContext, "bizSubCategory","")
        Constants.BIZ_NAME = PreferenceUtil.getString(applicationContext, "bsnsNm","")
        Constants.BIZ_SUBCATEGORY_KEY = PreferenceUtil.getBiz(applicationContext, "bizSubCategoryKey")

        if (Constants.BIZ_CATEGORY =="0") PreferenceUtil.setString(applicationContext, "bizSubCategory","용지도")

        val getBiz = PreferenceUtil.getString(applicationContext, "bizSubCategory", "")

        setBackButtonAboveActionBar(true, "$getBiz 조서")

        textViewBizInfoCategory.text = Constants.BIZ_CATEGORY
        textViewBizInfoSubCategory.text = Constants.BIZ_SUBCATEGORY
        textViewBizInfoName.text = Constants.BIZ_NAME

        textViewBizInfoSubCategory.visibleView()

        val getEmployee ="테스트"
        "현장조사원: $getEmployee".also { textViewBizInfoEmployee.text = it }

        //setColorBizCategory(this, textViewBizInfoCategory, textViewBizInfoSubCategory)

        val navListener = NavSetItemListener(this, context, getCoord)
        navigationViewMain.setNavigationItemSelectedListener(navListener)

        /**
         * 레이어 네비게이션 드로어 이벤트
         */
        navigationViewLayer.itemIconTintList = null // svg original color
        navigationViewLayer.setNavigationItemSelectedListener { menuItem ->

            this.menuItem = menuItem
            when (menuItem.itemId) {
                R.id.drawer_sido_layer -> isSidoLayerChecked = layerChecked(menuItem, sidoLayerSwitch) // 시도 레이어
                R.id.drawer_sigungu_layer -> isSigunguLayerChecked = layerChecked(menuItem, sigunguLayerSwitch) //시군구레이어
                R.id.drawer_dong_layer -> isDongLayerChecked = layerChecked(menuItem, dongLayerSwitch) //읍면동 레이어
                R.id.drawer_li_layer -> isLiLayerChecked = layerChecked(menuItem, riLayerSwitch) // 리 레이어
                R.id.drawer_naverCadastral_layer -> isNaverCadastralLayerChecked = layerChecked(menuItem, naverCadstralLayerSwitch)  // 지적편집도(네이버) 레이어
                R.id.drawer_bsnArea_layer -> isBsnsAreaLayerChecked = layerChecked(menuItem, bsnsAreaLayerSwitch) // 사업구역 레이어
                R.id.drawer_cadastral_edit_layer -> isCadastralEditLayerpChecked = layerChecked(menuItem, cadstralEditLayerSwitch) //편집 연속 지적도 레이어
                R.id.drawer_lad_layer -> isLadLayerChecked = layerChecked(menuItem, ladLayerSwitch)
                R.id.drawer_lad_real_layer -> isLadRealLayerChecked = layerChecked(menuItem, ladRealLayerSwitch)
                R.id.drawer_thing_layer -> isThingLayerChecked = layerChecked(menuItem, thingLayerSwitch)
                R.id.drawer_bsn_layer -> isBsnLayerChecked = layerChecked(menuItem, bsnLayerSwitch)
                R.id.drawer_farm_layer -> isFarmLayerChecked = layerChecked(menuItem, farmLayerSwitch)
                R.id.drawer_residnt_layer -> isResidntLayerChecked = layerChecked(menuItem, residntLayerSwitch)
                R.id.drawer_tomb_layer -> isTombLayerChecked = layerChecked(menuItem, tombLayerSwitch)
            }
            true
        }

        // fab event
        var isFabOpen = false

        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close)

        fabVisableArr = mutableListOf(
            floatingActionButtonToolbar,
            floatingActionButtonUndo,
            floatingActionButtonRedo,
            floatingActionButtonPoint,
            floatingActionButtonLine,
            floatingActionButtonModify,
            floatingActionButtonRemove,
            floatingActionButtonCancel,
            floatingActionButtonPolygon
        )

        fabArr = mutableListOf(
            floatingActionButtonPolygon,
            floatingActionButtonCancel,
            floatingActionButtonRemove,
            floatingActionButtonModify,
            floatingActionButtonLine,
            floatingActionButtonPoint,
            floatingActionButtonRedo,
            floatingActionButtonUndo
        )

        fabTranslationXArr = mutableListOf(-1200f ,-1050f, -900f, -750f, -600f, -450f, -300f, -150f)

        floatingActionButtonToolbar.setOnClickListener {
            isFabOpen = fabAnimateFunc(floatingActionButtonToolbar, !isFabOpen)
            toggleFab(isFabOpen)
        }

        fabArr.forEach { obj -> obj.setOnClickListener(this) }

        layoutMapSlide.addDrawerListener(DrawerLayoutEventListener(this))

        tombAddViewBtn.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        setPageCode(Constants.MAP_ACT)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (Intent.ACTION_SEARCH == intent!!.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            searchViewOwner.setQuery(query, false)
        }
    }

    /**
     * 스케치 툴바 ON/OFF
     */
    fun toggleFab(flag: Boolean) {
        if (!flag) {
            fabVisableArr.forEachIndexed { idx, obj ->
                obj.visibleView()
                if (idx > 0) {
                    ObjectAnimator.ofFloat(fabArr[idx - 1], "translationX", fabTranslationXArr[idx - 1]).run { start() }
                }
            }
        } else {
            fabArr.forEach { obj ->
                ObjectAnimator.ofFloat(obj, "translationX", 0f).run { start() }
            }
        }
    }

    /**
     * 레이어 ON/ OFF
     */
    private fun layerChecked(menuItem: MenuItem, switchMaterial: SwitchMaterial): Boolean {
        val flag: Boolean = !menuItem.isChecked
        menuItem.isChecked = flag

        val tagName = this.menuItem.toString()

        when {
            !switchMaterial.isChecked -> {
                log.d("${this.menuItem} On")
                when(tagName){
                    "지적편집도 (네이버)" -> naverMap?.naverMap?.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true)
                    "토지실제이용" -> naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, tagName)
                    "토지" -> naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_WTN.value, tagName)
                    "사업구역(용지도)" -> naverMap?.getWFSLayer(GeoserverLayerEnum.TL_BSNS_AREA.value, tagName)
                    "편집지적도" -> naverMap?.getWFSLayer(GeoserverLayerEnum.CADASTRAL_EDIT.value, tagName)
                    "지장물","농업","분묘","거주자","영업" -> naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, tagName)
                }
            }
            else -> {
                log.d("${this.menuItem} Off")
                when(tagName){
                    "지적편집도 (네이버)" -> {
                        naverMap?.naverMap?.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false)
                        naverMap!!.clearWMS(naverMap?.wmsNaverCadastralOverlayArr, tagName)
                    }
                    "사업구역(용지도)" -> naverMap?.clearWFS(naverMap?.wfsBsnAreaOverlayArr, tagName)
                    "편집지적도" -> naverMap?.clearWFS(naverMap?.wfsEditCadastralOverlayArr, tagName)
                    "토지실제이용" -> naverMap?.clearWFS(naverMap?.wfsRealLadOverlayArr, tagName)
                    "토지" -> naverMap?.clearWFS(naverMap?.wfsLadOverlayArr, tagName)
                    "지장물" -> naverMap?.clearWFS(naverMap?.wfsThingOverlayArr, tagName)
                    "농업" -> naverMap?.clearWFS(naverMap?.wfsFarmOverlayArr, tagName)
                    "분묘" -> naverMap?.clearWFS(naverMap?.wfsTombOverlayArr, tagName)
                    "거주자" -> naverMap?.clearWFS(naverMap?.wfsResidntOverlayArr, tagName)
                    "영업" -> naverMap?.clearWFS(naverMap?.wfsBsnOverlayArr, tagName)
                }

            }
        }
        switchMaterial.isChecked = !switchMaterial.isChecked
        return flag
    }

    /**
     * 조서 필지 작성 Context Popup
     * @param address
     * @param jibun
     * @param wfsJibun
     * @param lotMapPosition (용지도 ContextPopup Selected Position)
     */
    fun callerContextPopupFunc(
        address: String?,
        jibun: String?,
        wfsJibun: String?,
        lotMapPosition: Int?,
        legaldongCode: String?
    ) {

        var jibun = jibun ?: "null"
        if(jibun == "null")jibun = wfsJibun!!

        val mapView = layoutInflater.inflate(R.layout.activity_map, null)
        val currentSaupCode = PreferenceUtil.getString(applicationContext, "saupCode","default") // 임시 사업코드

        when (Constants.BIZ_SUBCATEGORY_KEY) {

            /**
             * 용지도 Context 항목 순서에 따라 BizEnum Value 변경
             */

            LOTMAP -> { // 용지도

                Constants.BIZ_SUBCATEGORY_KEY = when (lotMapPosition) {
                    0 -> LAD
                    1 -> THING
                    2 -> BSN
                    3 -> FARM
                    4 -> RESIDNT
                    5 -> TOMB
                    6 -> MINRGT
                    7 -> FYHTS
                    else -> LOTMAP
                }

                if(lotMapPosition == 0){

                    naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_WTN.value, "토지")
                    naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, "토지실제이용")

                    /*
                    1. 해당 토지 선택시 내역 표출
                    2. search탭에서 레이어 추가 버튼 클릭
                    3. carto 스케치 기능으로 레이어 그리기
                    */

                    // 조서 기본 조서 호출
                    val landUrl = context.resources.getString(R.string.mobile_url) + "landInfo"
                    val landInfoMap = HashMap<String, String>()
                    landInfoMap.put("saupCode", currentSaupCode)
                    landInfoMap.put("legaldongCode", legaldongCode!!)
                    landInfoMap.put("incrprLnm", jibun)
                    log.d("land info url $landUrl")

                    HttpUtil.getInstance(context)
                        .callerUrlInfoPostWebServer(landInfoMap, progressDialog, landUrl,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) =
                                    runOnUiThread {
                                        progressDialog?.dismiss()
                                        toast.msg_error(R.string.msg_server_connected_fail, 100)
                                    }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()
                                    log.d("landInfo response $responseString")

                                    progressDialog?.dismiss()
                                    runOnUiThread {
                                        if (!JSONObject(responseString).getJSONObject("list").getJSONObject("LandInfo").isNull("landWtnCode")) {
                                            toast.msg("해당 토지에 조서가 존재 하지 않습니다.", 100)
                                        } else {
                                            settingViewPager(mapView, responseString)
                                            WtnncBottomSheet()
                                        }
                                        progressDialog?.dismiss()

                                    }
                                }
                            }
                        )
                }

            }

            // 토지
            LAD -> {

                /*
                1. 해당 토지 선택시 내역 표출
                2. search탭에서 레이어 추가 버튼 클릭
                3. carto 스케치 기능으로 레이어 그리기
                */

                // 조서 기본 조서 호출
                val landUrl = context.resources.getString(R.string.mobile_url) + "landInfo"
                val landInfoMap = HashMap<String, String>()
                landInfoMap.put("saupCode", currentSaupCode)
                landInfoMap.put("legaldongCode", legaldongCode!!)
                landInfoMap.put("incrprLnm", jibun)
                log.d("land info url $landUrl")


                try {
                    HttpUtil.getInstance(context)
                        .callerUrlInfoPostWebServer(landInfoMap, progressDialog, landUrl,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) =
                                    runOnUiThread {
                                        progressDialog?.dismiss()
                                        toast.msg_error(R.string.msg_server_connected_fail, 100)
                                    }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()
                                    log.d("landInfo response $responseString")

                                    if(!responseString.contains("<!DOCTYPE")){
                                         progressDialog?.dismiss()
                                            runOnUiThread {
                                                if (JSONObject(responseString).getJSONObject("list").isNull("LandInfo")) {
                                                    toast.msg("해당 토지에 조서가 존재 하지 않습니다.", 100)
                                                } else {
                                                    settingViewPager(mapView, responseString)
                                                    WtnncBottomSheet()
                                                }
                                                progressDialog?.dismiss()

                                            }
                                    } else {
                                        progressDialog?.dismiss()
                                        runOnUiThread {
                                            toast.msg_error("해당 토지에 조서가 존재 하지 않습니다.", 100)
                                        }
                                    }

                                }
                            }
                        )
                } catch (e: Exception) {
                    throw IllegalAccessException (e.toString())
                }

            }

            THING -> {

                /** 물건 지장물 신규 생성
                 * 1. 해당 토지 정보 표출
                 * 2. Carto를 통한 Polygon 그리기
                 * 3. 물건 내용 입력
                 * 4. 저장
                 */
                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String, String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing Info url -------------------> $thingUrl")

//                val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.cancel()
                                toast.msg_error(R.string.msg_server_connected_fail, 100)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d("thingInfo response ------------------------> $responseString")


                                val dataObject = JSONObject(responseString)

                                ThingWtnObject.thingInfo = dataObject

                                runOnUiThread{
                                    settingViewPager(mapView, responseString)
                                    setThingSelectViewVisibility(true)
                                    ThingWtnObject.thingNewSearch = "Y"
                                    WtnncBottomSheet()
                                    progressDialog?.dismiss()
                                }
                            }

                        }
                    )
            }

            BSN -> {
//                WtnncUtill(this, this).viewPagerSetting(mapView, this, null)
//                WtnncBottomSheet()
                /**
                 * 영업 잠업 축산업 신규 생성
                 */
                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String, String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing Info Url ------------------------------> $thingUrl")

                HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {

                            val responseString = response.body!!.string()

                            log.d("thingInfo response -------------------> $responseString")

                            val dataObject = JSONObject(responseString)

                            ThingBsnObject.thingInfo = dataObject

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
                                ThingBsnObject.thingNewSearch = "Y"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }

                        }

                    })
            }

            FARM -> {
                /**
                 * 농업 신규 생성
                 */
                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String,String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing Info Url --------------------------> $thingUrl")

                HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()
                            log.d("thingInfo response -------------------> $responseString")

                            val dataObject = JSONObject(responseString)

                            ThingFarmObject.thingInfo = dataObject

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
                                ThingFarmObject.thingNewSearch = "Y"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }
                        }

                    })
            }

            RESIDNT -> {
                /**
                 * 거주자 신규 생성
                 */

                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String, String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing info url ------------------------------> $thingUrl")

                HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                    object: Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.e("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()
                            log.d("thingInfo response ----------------------> $responseString")

                            val dataObject = JSONObject(responseString)

                            ThingResidntObject.thingInfo = dataObject

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
                                ThingResidntObject.thingNewSearch = "Y"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()

                            }

                        }

                    })
            }

            TOMB -> {
                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String, String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing Info url -------------------> $thingUrl")

//                val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.cancel()
                                toast.msg_error(R.string.msg_server_connected_fail, 100)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d("thingInfo response ------------------------> $responseString")


                                val dataObject = JSONObject(responseString)

                                ThingTombObject.thingInfo = dataObject

                                runOnUiThread{
                                    settingViewPager(mapView, responseString)
//                                    setThingSelectViewVisibility(true)
                                    ThingTombObject.thingNewSearch = "Y"
                                    WtnncBottomSheet()
                                    progressDialog?.dismiss()
                                }
                            }

                        }
                    )
            }

            MINRGT -> {
                /**
                 * 광업권 신규 생성
                 * 1. 해당 토지 정보 표출
                 * 2. 조서 표출
                 * 3. carto를 통한 폴리곤 그리기
                 * 4. 광업권 내용 입력
                 * 5. 저장
                 */

                val thingUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
                val thingInfoMap = HashMap<String, String>()

                thingInfoMap.put("saupCode", currentSaupCode)
                thingInfoMap.put("incrprLnm", jibun)

                log.d("Thing Info Url ------------------? $thingUrl")

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                        object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.dismiss()
                                log.d("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d("thingInfo response -------------------> $responseString")

                                val dataObject = JSONObject(responseString)

                                ThingMinrgtObject.thingInfo = dataObject

                                runOnUiThread {
                                    settingViewPager(mapView, responseString)
                                    ThingMinrgtObject.thingNewSearch = "Y"
                                    WtnncBottomSheet()
                                    progressDialog?.dismiss()
                                }

                            }

                        })
            }

            FYHTS -> {
                /**
                 * 어업권 신규생성
                 * 1. 어업권은 토지생성여부와 상관없이 조서 작성
                 * 2. 조서 표출
                 * 3. 어업권 조서 확인은 레이어 선택으로 확인(토지 조서가 없음)
                 * 4. 필지등 선택으로 해당 신규 조서 작성
                 * 5. 조서 표출
                 * 6. carto를 통한 폴리곤 그리기;
                 * 7. 어업관 내용 입력
                 * 9. 저장
                 */
                settingViewPager(mapView, "")
                ThingFyhtsObject.thingNewSearch = "Y"
                WtnncBottomSheet()
            }

            REST_LAD -> {
                // 토지
                /*
                    1. 해당 토지 선택시 내역 표출
                    2. search탭에서 레이어 추가 버튼 클릭
                    3. carto 스케치 기능으로 레이어 그리기
                 */
                // 조서 기본 조서 호출
//                var landUrl = context!!.resources.getString(R.string.mobile_url) +"landInfo"
//                var landInfoMap = HashMap<String, String>()
//                val landUrl = context.resources.getString(R.string.mobile_url) + "landInfo"


                log.d("restData ---------------------<><><><><><><><><><>< $restData")

                val restDataJson = JSONObject(restData)


                val landUrl = context.resources.getString(R.string.mobile_url) + "restLadSearch"
                val landInfoMap = HashMap<String, String>()
                landInfoMap.put("saupCode", restDataJson.getString("saupCode"))
                landInfoMap.put("legaldongCode", restDataJson.getString("legaldongCode"))
                landInfoMap.put("incrprLnm", restDataJson.getString("incrprLnm"))
                landInfoMap.put("ladWtnCode", restDataJson.getString("ladWtnCode"))
                log.d("land info url $landUrl")

//                val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(landInfoMap, progressDialog, landUrl,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) =
                                runOnUiThread {
                                    progressDialog?.dismiss()
                                    toast.msg_error(R.string.msg_server_connected_fail, 100)
                                }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()
                                log.d("landInfo response $responseString")

                                progressDialog?.dismiss()
                                runOnUiThread {

                                    val jsonResponse = JSONObject(responseString)

                                    RestLandInfoObject.landInfo = jsonResponse

                                    settingViewPager(mapView, responseString)
                                    WtnncBottomSheet()


//                                    if (jsonResponse.has("list") && jsonResponse.getJSONObject("list").has("LandInfo") && jsonResponse.getJSONObject("list").getJSONObject("LandInfo").isNull("landWtnCode")) {
//
//                                    }
//                                    else {
//                                        toast.msg("해당 토지에 조서가 존재 하지 않습니다.", 100)
//                                    }
//                                    if (!JSONObject(responseString).getJSONObject("list").getJSONObject("LandInfo").isNull("landWtnCode")) {
//                                        toastUtil.msg("해당 토지에 조서가 존재 하지 않습니다.", 100)
//                                    } else {
//                                        settingViewPager(mapView, responseString)
//                                        WtnncBottomSheet()
//                                    }
                                    progressDialog?.dismiss()

                                }
                            }
                        }
                    )


            }

            REST_THING -> {
                log.d("restData ------------------------------<><><><><><><><><><> $restData")

                val restDataJson = JSONObject(restData)

                val thingUrl = context.resources.getString(R.string.mobile_url) + "/restThingSearch"
                val thingInfoMap = HashMap<String, String>()
                thingInfoMap.put("saupCode", restDataJson.getString("saupCode"))
                thingInfoMap.put("legaldongCode", restDataJson.getString("legaldongCode"))
                thingInfoMap.put("incrprLnm", restDataJson.getString("incrprLnm"))
                thingInfoMap.put("thingWtnCode", restDataJson.getString("thingWtnCode"))

                HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingInfoMap, progressDialog, thingUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        toast.msg_error(R.string.msg_server_connected_fail, 100)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        log.d("thingRest Info response ----------------- $responseString")
                        progressDialog?.dismiss()
                        runOnUiThread{
                            val jsonResponse = JSONObject(responseString)
                            RestThingWtnObject.thingInfo = jsonResponse.getJSONObject("list")

                            settingViewPager(mapView, responseString)
                            WtnncBottomSheet()
                        }

                    }

                })
            }
        }
    }

    fun settingViewPager(mapView: View, responseString: String?) {
        WtnncUtil(this, this).viewPagerSetting(mapView, this, responseString)
    }

    /**
     *  카토 맵 세팅
     *  리스너, 기존 폴리곤의 데이터 있을시 유무 확인
     *  피드백 후 수정이 필요함
     */
    fun settingCartoMap(editPolygonArr: MutableList<ArrayList<LatLng>>?, farmmFragment:FarmSearchFragment?) {
        //if (naverMap!!.getNaverMapZoom() >= 17) {
            cartoMapView.visibleView()

            cartoMap = CartoMapUtil(this, this, cartoMapView, naverMap!!, naverMap!!.marker.position, Constants.BIZ_SUBCATEGORY_KEY, farmmFragment)
            cartoMap!!.setScreenSync()

            layoutMapRightButtonGroup.goneView()
            layoutMapLeftButtonGroup.goneView()

            // 기존 폴리곤 데이터가 존재할 시에
            editPolygonArr?.let{ cartoMap?.drawPolygon(it) }


//        } else {
//            toast.msg_error(getString(R.string.msg_sketch_minzoom), 1500)
//        }
        toggleFab(false)
    }

    /**
     * 영업 및 거주자 내 건물이 존재할시에 해당 지오메트리를 파싱하여 맵에 표출
     * @param wkt WKT String
     */
    fun getBuldLinkToGeomData(wkt:String, type: String, sketchTy: Boolean){

        log.d("wktGEOM")
        log.d(wkt)

        val splitWKT = wkt.replace("MULTIPOLYGON (((", "").replace(")))", "").split(",")
        val isVisiablebuldPolygon = mutableListOf<LatLng>()

        splitWKT.forEach {

            val x = it.trim().split(" ")[0].toDouble()
            val y = it.trim().split(" ")[1].toDouble()

            val coordX = convertWGS84(x, y).y
            val coordY = convertWGS84(x, y).x

            val latLng = LatLng(coordX, coordY)

            log.d(latLng.toString())
            isVisiablebuldPolygon.add(latLng)
        }

        val buldPoly = PolygonOverlay()
        buldPoly.apply {
            coords = isVisiablebuldPolygon
            globalZIndex  = 160000
            outlineWidth = 5
            map = naverMap?.naverMap
        }

        if(type.equals("bsn") && sketchTy) {
            val buldPolygonOverlayArr = mutableListOf<PolygonOverlay>()
            buldPolygonOverlayArr.add(buldPoly)
            ThingBsnObject.thingBsnSketchPolygon = buldPolygonOverlayArr
        }
        if(type.equals("residnt") && sketchTy) {
            val buldPolygonOverlayArr = mutableListOf<PolygonOverlay>()
            buldPolygonOverlayArr.add(buldPoly)
            ThingResidntObject.thingResidntSketchPolygon = buldPolygonOverlayArr
        }

    }

    /**
     *  일반 카메라
     */
    fun  callerContextCamera() {
        log.d("camera")

        val wtnncSetRemovePostion = when(Constants.BIZ_SUBCATEGORY_KEY){
            THING -> 3
            else -> 2
        }

        when(Constants.BIZ_SUBCATEGORY_KEY){
            LAD -> {
                if(LandInfoObject.realLandInfoLength > 1){
                    nextViewCamera(
                        this,
                        Constants.CAMERA_ACT,
                        PreferenceUtil.getString(context, "saupCode", "defaual"),
                        LAD,
                        "A200006012",
                        "현장사진",
                        CameraEnum.DEFAULT
                    )
                } else {
                    toast.msg_error("실제이용현황이 존재하지 않으면 사진촬영을 진행 할 수 없습니다.", 500)
                }

            }
            else -> {
                val kndItems = naverMap?.contextDialogItems?.subList(wtnncSetRemovePostion, naverMap?.contextDialogItems!!.size)
                val wtnCodeItems = naverMap?.contextDialogWtnCodeItems
                val sumStrItems = mutableListOf<String>()

                val checkedItems = mutableListOf<Boolean>()
                val wtnCodeArr = ArrayList<String>()

                kndItems?.forEachIndexed() { idx, it ->
                    checkedItems.add(false)
                    sumStrItems.add("$it (조서번호: ${wtnCodeItems!![idx]})")
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("조서선택")
                    .setPositiveButton(R.string.msg_alert_y) { _, _ ->
                        nextViewMultiCamera(
                            this,
                            Constants.CAMERA_ACT,
                            PreferenceUtil.getString(context, "saupCode", "defaual"),
                            THING,
                            "A200006012",
                            "현장사진",
                            CameraEnum.DEFAULT,
                            wtnCodeArr,
                        )
                    }
                    .setNegativeButton(R.string.msg_alert_n) { dialog, _ -> dialog.dismiss() }
                    .setMultiChoiceItems(sumStrItems.toTypedArray(), checkedItems.toBooleanArray()) { dialog, which, checked ->
                        logUtil.d("현장조사 사진 조서 선택 위치 -> $which")

                        if(checked){
                            wtnCodeArr.add(wtnCodeItems!![which])
                        } else {
                            wtnCodeArr.removeAt(which)
                        }
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    /**
     *  도큐멘트 카메라
     */
    fun callerContextDocumentCamera() {
        log.d("camera(document)")
        nextView(this, Constants.CAMERA_ACT, null, CameraEnum.DOCUMENT, null, null)
    }

    /**
     *  Context Popup 컨트롤
     *  @param which
     *  @param name
     *  @param dataString
     */
    fun callerContextThing(which: Int, name: String, dataString: String?) {

        log.d("callerContextThing which ----------------------------> $which")
        log.d("callerContextThing name ----------------------------> $name")
        log.d("callerContextThing dataString ----------------------------> $dataString")
        val dataArray = JSONArray(dataString)

        when(Constants.BIZ_SUBCATEGORY_KEY) {
            LAD -> {

            }
            THING -> {
                val dataObject = dataArray.get(which-3) as JSONObject

                log.d("callerContextThing dataObject ----------------------------> $dataObject")
                val thingSmallCl = dataObject.getString("thingSmallCl")
                val saupCode = PreferenceUtil.getString(applicationContext, "saupCode","default")
                val ladWtnCode = dataObject.getString("ladWtnCode")
                val thingWtnCode = dataObject.getString("thingWtnCode")
                val mapView = layoutInflater.inflate(R.layout.activity_map, null)
                val geoms:String? = dataObject.getString("geoms")


                when(thingSmallCl) {
                    "A023002", "A023003" -> {
                        // 일반건축물, 집합 건축물
                        val thingBuild: String = if(geoms.equals("null")) {
                            context.resources.getString(R.string.mobile_url) + "thingEtcSearch"
                        } else {
                            context.resources.getString(R.string.mobile_url) + "thingBuildSearch"
                        }
//                        var thingBuild = context!!.resources.getString(R.string.mobile_url) + "thingBuildSearch"
                        val thingBuildMap = HashMap<String, String>()
                        thingBuildMap.put("thingSmallCl", thingSmallCl)
                        thingBuildMap.put("saupCode", saupCode)
                        thingBuildMap.put("ladWtnCode", ladWtnCode)
                        thingBuildMap.put("thingWtnCode", thingWtnCode)


//                        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))
                        HttpUtil.getInstance(context)
                            .callerUrlInfoPostWebServer(thingBuildMap, progressDialog, thingBuild,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        log.d("fail")
                                        progressDialog?.dismiss()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responseString = response.body!!.string()

                                        log.d("thingBuildSearch response --------------------------> $responseString")


                                        runOnUiThread {
                                            settingViewPager(mapView, responseString)
                                            setThingSelectViewVisibility(false)
                                            ThingWtnObject.thingNewSearch = "N"
                                            WtnncBottomSheet()
                                            progressDialog?.dismiss()
                                        }

                                    }

                                }
                            )

                    }
//                    "A023003" -> {
//                        // 집합 건축물
//                    }
                    "A023005" -> {
                        // 수목
                        val thingWdpt: String = if(geoms.equals("null")) {
                            context.resources.getString(R.string.mobile_url) + "thingEtcSearch"
                        } else {
                            context.resources.getString(R.string.mobile_url) + "thingWdptSearch"
                        }
//                        var thingWdpt = context!!.resources.getString(R.string.mobile_url) + "thingWdptSearch"
                        val thingWdptMap = HashMap<String, String>()
                        thingWdptMap.put("thingSmallCl", thingSmallCl)
                        thingWdptMap.put("saupCode", saupCode)
                        thingWdptMap.put("ladWtnCode", ladWtnCode)
                        thingWdptMap.put("thingWtnCode", thingWtnCode)


//                        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))
                        HttpUtil.getInstance(context)
                            .callerUrlInfoPostWebServer(thingWdptMap, progressDialog, thingWdpt,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        progressDialog?.dismiss()
                                        log.d("fail")
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responseString = response.body!!.string()


                                        log.d("thingWdptSearch response --------------------------> $responseString")


                                        runOnUiThread {
                                            settingViewPager(mapView, responseString)
                                            setThingSelectViewVisibility(false)
                                            ThingWtnObject.thingNewSearch = "N"
                                            WtnncBottomSheet()
                                            progressDialog?.dismiss()
                                        }

                                    }

                                }
                            )
                    }
                    else -> {
                        // 일반지장물 및 기타
                        val thingSearchUrl = context.resources.getString(R.string.mobile_url) + "thingEtcSearch"
                        val thingSearchMap = HashMap<String, String>()
                        thingSearchMap.put("thingSmallCl", thingSmallCl)
                        thingSearchMap.put("saupCode", saupCode)
                        thingSearchMap.put("ladWtnCode", ladWtnCode)
                        thingSearchMap.put("thingWtnCode", thingWtnCode)


//                        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))
                        HttpUtil.getInstance(context)
                            .callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingSearchUrl,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        log.d("fail")
                                        progressDialog?.dismiss()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responseString = response.body!!.string()

                                        log.d("thingEtcSearch response --------------------------> $responseString")


                                        runOnUiThread {
                                            settingViewPager(mapView, responseString)
                                            setThingSelectViewVisibility(false)
                                            ThingWtnObject.thingNewSearch = "N"
                                            WtnncBottomSheet()
                                            progressDialog?.dismiss()
                                        }

                                    }

                                }
                            )
                    }
                }
            }
            TOMB -> {
                val dataObject = dataArray.get(which-2) as JSONObject
                log.d("callerContextThing dataObject ----------------------------> $dataObject")

                val tombSearchUrl = context.resources.getString(R.string.mobile_url) + "thingTombSearch"
                val tombSearchMap = HashMap<String, String>()
                tombSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                tombSearchMap.put("saupCode", dataObject.getString("saupCode"))
                tombSearchMap.put("ladWtnCode", dataObject.getString("ladWtnCode"))
                tombSearchMap.put("tombWtnCode", dataObject.getString("tombWtnCode"))
                tombSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)
//                val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))

                HttpUtil.getInstance(context).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
                        tombSearchUrl, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.d("fail")

                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            log.d("tombSearch response -----------------------> $responseString")

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
//                                ThingWtnObject.thingNewSearch = "N"
                                ThingTombObject.thingNewSearch = "N"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }

                        }

                    })

            }
            MINRGT -> {
                val dataObject = dataArray.get(which-2) as JSONObject

                log.d("callerContextThing dataObjecty ------------------------> $dataObject")

                val mirgtSearchUrl = context.resources.getString(R.string.mobile_url) + "thingMinrgtSearch"
                val mirgtSearchMap = HashMap<String, String>()
                mirgtSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                mirgtSearchMap.put("thingLrgeCl", dataObject.getString("thingLrgeCl"))
                mirgtSearchMap.put("saupCode", dataObject.getString("saupCode"))
                mirgtSearchMap.put("ladWtnCode", dataObject.getString("ladWtnCode"))
                mirgtSearchMap.put("minrgtWtnCode", dataObject.getString("minrgtWtnCode"))
                mirgtSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(mirgtSearchMap, progressDialog, mirgtSearchUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.d("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            log.d("mirgtSearchMap response -----------------------> $responseString")

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
//                                ThingWtnObject.thingNewSearch = "N"
                                ThingMinrgtObject.thingNewSearch = "N"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }
                        }

                    })
            }
            BSN -> {
                val dataObject = dataArray.get(which-2) as JSONObject

                log.d("callerContextThing dataObjecty ------------------------> $dataObject")

                val bsnSearchUrl = context.resources.getString(R.string.mobile_url) + "thingBsnSearch"
                val bsnSearchMap = HashMap<String, String>()
                bsnSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                bsnSearchMap.put("thingLrgeCl", dataObject.getString("thingLrgeCl"))
                bsnSearchMap.put("saupCode", dataObject.getString("saupCode"))
                bsnSearchMap.put("ladWtnCode", dataObject.getString("ladWtnCode"))
                bsnSearchMap.put("bsnWtnCode", dataObject.getString("bsnWtnCode"))
                bsnSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnSearchUrl,
                        object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.dismiss()
                                log.d("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d("bsnSearchMap response -----------------------> $responseString")

                                runOnUiThread {
                                    settingViewPager(mapView, responseString)
//                                ThingWtnObject.thingNewSearch = "N"
                                    ThingBsnObject.thingNewSearch = "N"
                                    WtnncBottomSheet()
                                    progressDialog?.dismiss()
                                }
                            }


                        }
                    )
            }
            FARM -> {
                val dataObject = dataArray.get(which -2) as JSONObject

                log.d("callerContextThing dataObjecty ------------------------> $dataObject")

                val farmSearchUrl = context.resources.getString(R.string.mobile_url) + "thingFarmSearch"
                val farmSearchMap = HashMap<String, String>()
                farmSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                farmSearchMap.put("thingLrgeCl", dataObject.getString("thingLrgeCl"))
                farmSearchMap.put("saupCode", dataObject.getString("saupCode"))
                farmSearchMap.put("ladWtnCode", dataObject.getString("ladWtnCode"))
                farmSearchMap.put("farmWtnCode", dataObject.getString("farmWtnCode"))
                farmSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmSearchUrl,
                        object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.dismiss()
                                log.d("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d(" SearchMap response -----------------------> $responseString")

                                runOnUiThread {
                                    settingViewPager(mapView, responseString)
//                                ThingWtnObject.thingNewSearch = "N"
                                    ThingFarmObject.thingNewSearch = "N"
                                    WtnncBottomSheet()
                                    progressDialog?.dismiss()
                                }
                            }

                        })

            }
            RESIDNT -> {
                val dataObject = dataArray.get(which-2) as JSONObject
                log.d("callerContextThing dataObject-----------------> $dataObject")

                val residntSearchUrl = context.resources.getString(R.string.mobile_url) + "thingResidntSearch"
                val residntSearchMap = HashMap<String, String>()

                residntSearchMap.put("thingLrgeCl", dataObject.getString("thingLrgeCl"))
                residntSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                residntSearchMap.put("saupCode", dataObject.getString("saupCode"))
                residntSearchMap.put("ladWtnCode", dataObject.getString("ladWtnCode"))
                residntSearchMap.put("residntWtnCode", dataObject.getString("residntWtnCode"))
                residntSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntSearchUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.d("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            log.d("residntSearch response -------------------> $responseString")

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
                                ThingResidntObject.thingNewSearch = "N"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }

                        }

                    })

            }
            FYHTS -> {
                val dataObject = dataArray.get(which-2) as JSONObject
                log.d("callerContextThing DataObject-----------------> $dataObject")

                val fyhtsSearchUrl = context.resources.getString(R.string.mobile_url) + "thingFyhtsSearch"
                val fyhtsSearchMap = HashMap<String, String>()

                fyhtsSearchMap.put("thingLrgeCl", dataObject.getString("thingLrgeCl"))
                fyhtsSearchMap.put("thingSmallCl", dataObject.getString("thingSmallCl"))
                fyhtsSearchMap.put("saupCode", dataObject.getString("saupCode"))
                fyhtsSearchMap.put("thingWtnCode", dataObject.getString("thingWtnCode"))
                fyhtsSearchMap.put("fyhtsWtnCode", dataObject.getString("fyhtsWtnCode"))

                val mapView = layoutInflater.inflate(R.layout.activity_map, null)

                HttpUtil.getInstance(context)
                    .callerUrlInfoPostWebServer(fyhtsSearchMap, progressDialog, fyhtsSearchUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog?.dismiss()
                            log.d("fail")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            log.d("fyhtsSearch response ------------> $responseString")

                            runOnUiThread {
                                settingViewPager(mapView, responseString)
                                ThingFyhtsObject.thingNewSearch = "N"
                                WtnncBottomSheet()
                                progressDialog?.dismiss()
                            }
                        }

                    })
            }
            else -> {}
        }
    }

    /**
     * 열려있는 조서창 닫기 (숨기기)
     */
    fun bottomPanelClose() {
        bottompanel.goneView()

        naverMap!!.isVisableContextPopup = false
//        cartoMap!!.removeDrawDistanceArea()
        if(cartoMap != null) {
            cartoMap!!.empty()
        }

        // TODO: 2021-11-16  토지 조서가 닫히면 실제이용현황 레이어도 클리어 시켜준다.
        naverMap!!.clearWFS(naverMap!!.wfsRealLadOverlayArr, "토지실제이용")

       ladRealLayerSwitch.isChecked = false
       isLadRealLayerChecked = false

    }

    /**
     * 조서 창 bottomsheet 컨트롤러
     */
    private fun WtnncBottomSheet() {

        bottompanel.isClickable = true
        val bottomSheetBehavior: BottomSheetBehavior<*>
        bottomSheetBehavior = BottomSheetBehavior.from(bottompanel)

        bottompanel.visibleView()

        viewSearchConfirmBtn.visibleView()
        viewSearchSaveBtn.visibleView()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = true

        bottomSheetBehavior.isFitToContents = false
        bottomSheetDownBtn.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        viewSearchConfirmBtn.setOnClickListener(this)
        viewSearchSaveBtn.setOnClickListener(this)

        /*bottompanel.setOnClickListener { bottomPanelClose() }*/

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, state: Int) {
                when (state) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        log.d("STATE_EXPANDED")
                        bottomSheetDownBtn.visibility = Button.VISIBLE
                        viewSearchConfirmBtn.visibleView()
                        viewSearchSaveBtn.visibleView()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> { // peek 높이 만큼 보이는 상태
                        log.d("STATE_COLLAPSED")
                        bottomSheetDownBtn.visibility = Button.GONE
                        viewSearchConfirmBtn.goneView()
                        viewSearchSaveBtn.goneView()
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> { // 반만 보임
                        log.d("STATE_HALF_EXPANDED")
                        bottomSheetDownBtn.visibility = Button.VISIBLE
                        viewSearchConfirmBtn.visibleView()
                        viewSearchSaveBtn.visibleView()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> { // 숨김 상태
                        log.d("STATE_HIDDEN")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        //TODO()
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        //TODO()m
                    }
                }
            }

            override fun onSlide(
                bottomSheet: View,
                slideOffset: Float
            ) {
            }
        })
    }

    override fun loadViewPage(thingSmallType: String, thingKnd: String, jibun: String) {
        log.d("loadViewPage thingType ----------------------> $thingSmallType")
        log.d("loadViewPage thingKnd ----------------------> $thingKnd")
        log.d("loadViewPage jibun ----------------------> $jibun")


        bottomPanelClose()


        val mapView = layoutInflater.inflate(R.layout.activity_map, null)

        val currentSaupCode = PreferenceUtil.getString(applicationContext, "saupCode","default")
        val landUrl = context.resources.getString(R.string.mobile_url) + "ThingInfo"
        val landInfoMap = HashMap<String, String>()
        landInfoMap.put("saupCode", currentSaupCode)
        landInfoMap.put("incrprLnm", jibun)
        log.d("land info url $landUrl")

        val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(this))

        HttpUtil.getInstance(context)
            .callerUrlInfoPostWebServer(landInfoMap, progressDialog, landUrl,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) =
                        runOnUiThread {
                            progressDialog.cancel()
                            toast.msg_error(R.string.msg_server_connected_fail, 100)
                        }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()
                        log.d("landInfo response $responseString")

                        runOnUiThread {
                            ThingWtnObject.thingKnd = thingKnd
                            ThingWtnObject.thingSmallCl = thingSmallType

                            settingViewPager(mapView, responseString)
                            setThingSelectViewVisibility(true)
                            WtnncBottomSheet()
                            progressDialog.cancel()
                        }
                    }
                }
            )




    }

    fun setThingSelectViewVisibility(visible: Boolean) {
        if(visible) {
            thingDialogsStBtn.visibleView()
            //지장물 dialog
            thingDialogsStBtn.setOnClickListener {
                layoutInflater.inflate(R.layout.thing_dialog, null).let { view ->
                    val jibun = ThingWtnObject.thingInfo?.getString("incrprLnm")
                    val thingdialog = ThingDialogFragment(context, this, view, 1, this, jibun).apply { // value 1: 물건선택 버튼 선택
                        isCancelable = false
                        show(supportFragmentManager, "Thing_Dialog")
                    }
                    view.thingDialogExitBtn.setOnClickListener {
                        thingdialog.dismiss()
                    }
                }
            }
        } else {
            thingDialogsStBtn.goneView()
        }

    }

    /***************************** 조서 데이터 서버 삽입 로직 부분 *******************************/

    /**
     * 토지
     */
    fun searchSaveLand() {

        LandInfoObject.realLandPolygonArr?.clear() // 항시 초기화

        val landInfo = LandInfoObject.landInfo
        val searchRealLand = LandInfoObject.searchRealLand as JSONArray
        val realLandPolygon = LandInfoObject.realLandPolygon
        var realLandPolygonLatLngArr = LandInfoObject.realLandPolygonArr
        val seleceLandPolygon = LandInfoObject.selectLandPolygonArr

        val realRequestData = JSONArray()

        var realLandPolygonSize = 1
        var mulitGeomString: String? = ""
        var muilitElemString: String? = ""
        var polyStartPoint = 1
        var polySelectStartPoint = 1

        LandSearchFragment(this, this).addLandData() // 조서 입력 사항 체크

        /**
         * 1. 실제이용현황 '신규등록'
         * 2. 실제이용현황 '수정'
         */

        when (realLandPolygon) {

            // 기존 Data
            null -> {
                log.d("realLandPolygon is null")

                realLandPolygonLatLngArr = naverMap?.resultRealLandLatLngArr
                realLandPolygonLatLngArr?.forEach { realLandPolygonData ->
                    log.d(realLandPolygonData.toString())

                    var geomString: String? = ""
                    var realCoordsSize = 0
                    val realLandObject = JSONObject()
                    muilitElemString += "$polyStartPoint, 1003, 1"

                    realLandPolygonData.forEachIndexed { index, code ->

                        val webMercatorCoord = WebMercatorCoord.valueOf(code)

                        val x = BigDecimal.valueOf(webMercatorCoord.x)
                        val y = BigDecimal.valueOf(webMercatorCoord.y)

                        geomString += "$x,$y"

                        realCoordsSize++
                        if (realCoordsSize != realLandPolygonData.size) {
                            geomString += ","
                        }
                        polyStartPoint += 2
                    }

                    log.d("realLandPolygon String geom -> $geomString")

                    mulitGeomString += geomString.toString()
                    if (realLandPolygonSize != realLandPolygon?.size) {
                        mulitGeomString  +=  ","
                        muilitElemString +=  ","
                    }

                    // 실제이용현황 서버 전송
                    val realData = searchRealLand.get(realLandPolygonSize) as JSONObject

                    /**
                     * lad_wtn_code 토지조서코드
                     * saup_code 사업코드
                     * real_lndcgr_cl실제이용지목분류
                     * real_lndcgr_cn실제이용지역지구
                     * real_lndcgr_ar실제이용면적
                     * register등록자 > 사번이용 추후 사용자로그인 정보 이용 사용자 아이디 등록
                     * geom공간데이터
                     *    -> elemArray공간데이터 배열 정보 입력 < ex)"1,1003,1""배열시작위치/배열타입/폴리곤유형">
                     *    -> ordinateArray공간데이터 포인트 배열 <폴리곤 포인트 배열 ex) x1,y1,x2,y2,x3,y3,x4,y4,x1,x2>
                     *    -> 참고) https://docs.oracle.com/database/121/SPATL/sdo_geometry-object-type.htm#SPATL493
                     */

                    realLandObject.put("ladWtnCode", realData.getString("ladWtnCode"))
                    realLandObject.put("saupCode", realData.getString("saupCode"))
                    realLandObject.put("realLndcgrCode", realData.getString("realLndcgrCode"))
                    realLandObject.put("realLndcgrCl", realData.getString("realLndcgrCl"))
                    realLandObject.put("realLndcgrCn", realData.getString("realLndcgrCn"))
                    realLandObject.put("realLndcgrAr", realData.getString("realLndcgrAr"))
                    realLandObject.put("user", PreferenceUtil.getString(context!!, "id", "defaual"))
                    realLandObject.put("elemArray", "1,1003,1")
                    realLandObject.put("ordinateArray", geomString.toString())

                    realRequestData.put(realLandObject)

                    realLandPolygonSize++
                }
            }

            // 신규
            else -> {
                log.d("realLandPolygon is not null")

//                val tempA = ArrayList<LatLng>()
                val tempB = mutableListOf<ArrayList<LatLng>>()



                realLandPolygon.forEach {
                    val tempA = ArrayList<LatLng>()
                    it.coords.forEach { coord ->
                        tempA.add(coord)
                    }
                    log.d(it.toString())
                    tempB.add(tempA)
                    log.d("tempB size -> ${tempB.size}")
                }

                realLandPolygonLatLngArr = tempB

                ////

                realLandPolygonLatLngArr.forEach { realLandPolygonData ->
                    log.d(realLandPolygonData.toString())

                    var geomString: String? = ""
                    var realCoordsSize = 0
                    val realLandObject = JSONObject()
//                    muilitElemString += "$polyStartPoint, 1003, 1"

                    realLandPolygonData.forEachIndexed { _, code ->

                        val webMercatorCoord = WebMercatorCoord.valueOf(code)

                        val x = BigDecimal.valueOf(webMercatorCoord.x)
                        val y = BigDecimal.valueOf(webMercatorCoord.y)

                        geomString += "$x,$y"

                        realCoordsSize++
                        if (realCoordsSize != realLandPolygonData.size) {
                            geomString += ","
                        }
                        polyStartPoint += 2
                    }

                    log.d("realLandPolygon String geom -> $geomString")

//                    mulitGeomString += geomString.toString()
//                    if (realLandPolygonSize != realLandPolygon.size) {
//                        mulitGeomString  +=  ","
//                        muilitElemString +=  ","
//                    }

                    // 실제이용현황 서버 전송
                    val realData = searchRealLand.get(realLandPolygonSize) as JSONObject

                    /**
                     * lad_wtn_code 토지조서코드
                     * saup_code 사업코드
                     * real_lndcgr_cl실제이용지목분류
                     * real_lndcgr_cn실제이용지역지구
                     * real_lndcgr_ar실제이용면적
                     * register등록자 > 사번이용 추후 사용자로그인 정보 이용 사용자 아이디 등록
                     * geom공간데이터
                     *    -> elemArray공간데이터 배열 정보 입력 < ex)"1,1003,1""배열시작위치/배열타입/폴리곤유형">
                     *    -> ordinateArray공간데이터 포인트 배열 <폴리곤 포인트 배열 ex) x1,y1,x2,y2,x3,y3,x4,y4,x1,x2>
                     *    -> 참고) https://docs.oracle.com/database/121/SPATL/sdo_geometry-object-type.htm#SPATL493
                     */

                    realLandObject.put("ladWtnCode", realData.getString("ladWtnCode"))
                    realLandObject.put("saupCode", realData.getString("saupCode"))
                    realLandObject.put("realLndcgrCode", realData.getString("realLndcgrCode"))
                    realLandObject.put("realLndcgrCl", realData.getString("realLndcgrCl"))
                    realLandObject.put("realLndcgrCn", realData.getString("realLndcgrCn"))
                    realLandObject.put("realLndcgrAr", realData.getString("realLndcgrAr"))
                    realLandObject.put("user", PreferenceUtil.getString(context!!, "id", "defaual"))
                    realLandObject.put("elemArray", "1,1003,1")
                    realLandObject.put("ordinateArray", geomString.toString())


                    realRequestData.put(realLandObject)

                    realLandPolygonSize++
                }

                var selectCoordsSize = 0
                seleceLandPolygon.forEachIndexed { _, code ->

                    val webMercatorCoord = WebMercatorCoord.valueOf(code)

                    val x = BigDecimal.valueOf(webMercatorCoord.x)
                    val y = BigDecimal.valueOf(webMercatorCoord.y)

                    mulitGeomString += "$x,$y"

                    selectCoordsSize++
                    if (selectCoordsSize != seleceLandPolygon.size) {
                        mulitGeomString += ","
                    }
                }

            }
        }

        val landRequestData = JSONObject()
        val landInfoData = JSONObject()
        landInfoData.put("ladWtnCode", landInfo!!.getString("ladWtnCode"))
        landInfoData.put("saupCode", landInfo.getString("saupCode"))
        landInfoData.put("no", landInfo.getString("no"))
        landInfoData.put("subNo", landInfo.getString("subNo"))
        landInfoData.put("legaldongCode", landInfo.getString("legaldongCode"))
        landInfoData.put("nrfrstAt", LandInfoObject.nrfrstAtChk) // 자연림여부
        landInfoData.put("clvtAt", LandInfoObject.clvtAtChk)   //경작여부
        landInfoData.put("buildAt", LandInfoObject.buildAtChk)   //건축물여부
        landInfoData.put("plotAt", LandInfoObject.plotAtChk) // 대지권여부
        landInfoData.put("sttusMesrAt", LandInfoObject.sttusMesrAtChk)   //측량요청
        landInfoData.put("rwTrgetAt", LandInfoObject.rwTrgetAt)
        landInfoData.put("partitnTrgetAt", LandInfoObject.partitnTrgetAt)
        landInfoData.put("spclLadCl", LandInfoObject.spclLadCl)   //특수용지
        landInfoData.put("spclLadCn", LandInfoObject.spclLadCn)   //특수용지내용
        landInfoData.put("ownerCnfirmBasisCl", LandInfoObject.ownerCnfirmBasisCl)   //소유자확인근거
        landInfoData.put("paclrMatter", LandInfoObject.paclrMatter) //특이사항
        landInfoData.put("referMatter", LandInfoObject.referMatter) //참고사항
        landInfoData.put("rm", LandInfoObject.rm) //비고
        landInfoData.put("user",PreferenceUtil.getString(context!!, "id", "defaual")) // 수정자 추후 앱 로그인 사번으로 변경 5자리

        //if(realLandPolygon == null){
        landInfoData.put("elemArray", "1, 1003, 1")
        //} else {
        //landInfoData.put("elemArray", muilitElemString)
        //}

        landInfoData.put("ordinateArray", lastCommaRemove(mulitGeomString))

        landRequestData.put("real", realRequestData)
        landRequestData.put("land", landInfoData)

        landUrl = if(LandInfoObject.realLngrty.equals("Y")) {
            context.resources.getString(R.string.mobile_url) +"updateLand"
        } else {
            context.resources.getString(R.string.mobile_url) +"registLand"
        }

        log.d("landInfoData $landInfo")

        HttpUtil.getInstance(context)
            .callUrlJsonWebServer(landRequestData, progressDialog, landUrl!!,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        log.d("fail")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()
                        log.d("realRequest response -> $responseString")
                        progressDialog?.dismiss()

                        runOnUiThread{
                            toast.msg_info("토지조서가 등록 되었습니다.", 500)
                            bottomPanelClose()
                            GlobalScope.launch {
                                delay(500)
                                withContext(Dispatchers.Main) {
                                    naverMap?.clearCartoPolygon()
//                                    naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_WTN.value, "토지")
                                    naverMap?.getWFSLayer(GeoserverLayerEnum.TB_LAD_REALNGR.value, "토지실제이용")
                                }
                            }

                        }

                    }
                }
            )

    }
    // 잔여건물
    fun searchSaveRestThing() {
        RestThingSearchFragment(this, this, this).addThingRestData()

        val thingRestInfo = RestThingWtnObject.thingInfo as JSONObject
        val thingInfo = thingRestInfo.getJSONObject("ThingSearch")

        val ownerInfo = thingRestInfo!!.getJSONArray("ownerInfo") as JSONArray

        var thingWtnOwnerCode = ArrayList<String>()

        if(ownerInfo.length() > 0) {
            for(i in 0 until ownerInfo.length() -1) {
                val item = ownerInfo.getJSONObject(i)
                thingWtnOwnerCode.add(item.getString("thingWtnOwnerCode"))
            }
        }

        val thingRestRequstData = HashMap<String,String>()

        thingRestRequstData["saupCode"] = thingInfo.getString("saupCode")
        thingRestRequstData["thingWtnCode"] = thingInfo.getString("thingWtnCode")
        thingRestRequstData["rewdAt"] = RestThingWtnObject.rewdAt.toString()
        thingRestRequstData["resn"] = RestThingWtnObject.resn.toString()
        thingRestRequstData["examin1Rslt"] = RestThingWtnObject.examin1Rslt.toString()
        thingRestRequstData["examin2Rslt"] = RestThingWtnObject.examin2Rslt.toString()
        thingRestRequstData["examin3Rslt"] = RestThingWtnObject.examin3Rslt.toString()
        thingRestRequstData["examin4Rslt"] = RestThingWtnObject.examin4Rslt.toString()
        thingRestRequstData["examin5Rslt"] = RestThingWtnObject.examin5Rslt.toString()
        thingRestRequstData["examin6Rslt"] = RestThingWtnObject.examin6Rslt.toString()
        thingRestRequstData["examin7Rslt"] = RestThingWtnObject.examin7Rslt.toString()
        thingRestRequstData["examin8Rslt"] = RestThingWtnObject.examin8Rslt.toString()
        thingRestRequstData["examin9Rslt"] = RestThingWtnObject.examin9Rslt.toString()
        thingRestRequstData["examin10Rslt"] = RestThingWtnObject.examin10Rslt.toString()
        thingRestRequstData["rqestPsn"] = RestThingWtnObject.rqestPsn.toString()
        thingRestRequstData["rqestCn"] = RestThingWtnObject.rqestCn.toString()
        thingRestRequstData["register"] = PreferenceUtil.getString(context!!, "id", "defaual")
        thingRestRequstData["thingOwner"] = thingWtnOwnerCode.joinToString(separator = ",")

        var thingRestUrl = ""

        if(thingRestInfo.getJSONArray("restThing").length() > 0) {
            thingRestUrl = context!!.resources.getString(R.string.mobile_url) + "/updateRestThing"
        } else {
            thingRestUrl = context!!.resources.getString(R.string.mobile_url) + "/registRestThing"
        }

        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingRestRequstData, progressDialog, thingRestUrl,
        object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                progressDialog?.dismiss()
                log.d("fail")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body!!.string()
                val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                val messageNum = messageJSON.getString("messageNum")
                val message = messageJSON.getString("message")


                progressDialog?.dismiss()
                if(messageNum.equals("-1")) {
                    runOnUiThread { toast.msg_error(message.toString(), 500) }
                } else {
                    runOnUiThread {
                        toast.msg_info("잔여건물 조서가 등록 되었습니다.", 500)
                        RestThingWtnObject.cleanThingWtnObject()
                        bottomPanelClose()

                    }
                }
            }

        })

    }

    // 잔여지
    fun searchSaveRestLand() {

        RestLandSearchFragment(this, this).addLandRestData()

        val landRestInfo = RestLandInfoObject.landInfo!!.getJSONObject("list") as JSONObject
        val landInfo = landRestInfo!!.getJSONObject("LandInfo")


        val ownerInfo = landRestInfo!!.getJSONArray("ownerInfo") as JSONArray

        var ladWtnOwnerCode = ArrayList<String>()
        if(ownerInfo.length() > 0) {
            for(i in 0 until ownerInfo.length() -1) {
                val item = ownerInfo.getJSONObject(i)
                ladWtnOwnerCode.add(item.getString("ladWtnOwnerCode"))

            }
        }


        val landRestRequestData = HashMap<String, String>()
        val landInfoData = JSONObject()

        landRestRequestData["saupCode"] = landInfo.getString("saupCode")
        landRestRequestData["ladWtnCode"] = landInfo.getString("ladWtnCode")
        landRestRequestData["rewdAt"] = RestLandInfoObject.rewdAt.toString()
        landRestRequestData["resn"] = RestLandInfoObject.resn.toString()
        landRestRequestData["examin1Rslt"] = RestLandInfoObject.examin1Rslt.toString()
        landRestRequestData["examin2Rslt"] = RestLandInfoObject.examin2Rslt.toString()
        landRestRequestData["examin3Rslt"] = RestLandInfoObject.examin3Rslt.toString()
        landRestRequestData["examin4Rslt"] = RestLandInfoObject.examin4Rslt.toString()
        landRestRequestData["examin5Rslt"] = RestLandInfoObject.examin5Rslt.toString()
        landRestRequestData["examin6Rslt"] = RestLandInfoObject.examin6Rslt.toString()
        landRestRequestData["examin7Rslt"] = RestLandInfoObject.examin7Rslt.toString()
        landRestRequestData["examin8Rslt"] = RestLandInfoObject.examin8Rslt.toString()
        landRestRequestData["examin9Rslt"] = RestLandInfoObject.examin9Rslt.toString()
        landRestRequestData["examin10Rslt"] = RestLandInfoObject.examin10Rslt.toString()
        landRestRequestData["rqestPsn"] = RestLandInfoObject.rqestPsn.toString()
        landRestRequestData["rqestCn"] = RestLandInfoObject.rqestCn.toString()
        landRestRequestData["register"] = PreferenceUtil.getString(context!!, "id", "defaual")
        landRestRequestData["ladOwner"] = ladWtnOwnerCode.joinToString(separator = ",")


        var landRestLadUrl = ""

        if(landRestInfo.getJSONArray("restLad").length() > 0) {
            landRestLadUrl = context!!.resources.getString(R.string.mobile_url) + "/updateRestLad"
        } else {
            landRestLadUrl = context!!.resources.getString(R.string.mobile_url) + "/registRestLad"
        }

        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(landRestRequestData, progressDialog, landRestLadUrl,
        object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                progressDialog?.dismiss()
                log.d("fail")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body!!.string()
                 val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                val messageNum = messageJSON.getString("messageNum")
                val message = messageJSON.getString("message")

                progressDialog?.dismiss()
                if (messageNum.equals("-1")) {
                    runOnUiThread { toast.msg_error(message.toString(), 500) }
                } else {
                    runOnUiThread{
                        toast.msg_info("잔여지 조서가 등록 되었습니다.", 500)
                        RestLandInfoObject.clealRestLadObject()
                        bottomPanelClose()
                        //naverMap?.clearCartoPolygon()
                    }
                }

            }

        })

    }

    /**
     * 지장물 Type '저장' 이전 액션 (강제 탭 이동)
     */
    fun searchSaveThing() {
        ThingWtnObject.thingWtnncSaveFlag = true
        setWtnccTabReset()
    }

    /**
     * 지장물 종류에 따라 HashMap Put 분기처리
     */
    fun setThingJsonData(){

        val callBackListener: DialogUtilCallbackListener?

        when(Constants.BIZ_SUBCATEGORY_KEY){

            THING -> {
                val thingPolygonData = ThingWtnObject.thingSketchPolygon
                log.d("thingPolygonData -> $thingPolygonData")

                if(ThingWtnObject.thingIndoorTy == "N" || thingPolygonData == null) {
                    toast.msg_error("지장물 조서의 스케치가 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 1000)
                    return
                }

                thingDataJson = ThingWtnObject.thingInfo as JSONObject
                thingRequestData = JSONObject()
                thingInfoData = JSONObject()

                when (ThingWtnObject.thingSmallCl) {

                    "A023002", "A023003"-> { //건축물

                        var mulitGeomString: String? = ""
                        var mulitElemString: String? = ""
                        var coordSize = 0

                        log.d("thingPolygonData ---------------------------------><><><><><><")

                        mulitElemString += "1, 1003, 1"
                        for (coord in thingPolygonData[0]?.coords) {

                            val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                            val x = BigDecimal.valueOf(webMercatorCoord.x)
                            val y = BigDecimal.valueOf(webMercatorCoord.y)

                            coordSize++

                            mulitGeomString += "$x,$y"
                            if (coordSize != thingPolygonData[0].coords.size) {
                                mulitGeomString += ","
                            }
                        }

                        log.d("ThingPolygon String geom " + mulitGeomString.toString())

                        thingInfoData = JSONObject()
                        thingRequestData = JSONObject()

                        if (ThingWtnObject.thingNewSearch.equals("Y")) {
                            thingBuildUrl = context.resources.getString(R.string.mobile_url) + "registThingBuld"
                            thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                            thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                            thingInfoData.put("incrprLnm", thingDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingLrgeCl", ThingWtnObject.thingLrgeCl) //물건 대분류 코드 //임시
                            thingInfoData.put("thingSmallCl", ThingWtnObject.thingSmallCl) //물건 소분류 코드 //임시
                            thingInfoData.put("legaldongCode", thingDataJson.getString("legaldongCode"))  //소재지코드
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                            thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의 종류
                            thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및 구격  strctNdStrndrd
                            thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래 수량
                            thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                            thingInfoData.put("unitCl", ThingWtnObject.unitCl) //단위 코드
                            thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                            thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                            thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                            thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                            thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                            thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                            thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                            thingInfoData.put("redeBingAt", ThingWtnObject.redeBingAt) //주거용건축물여부
                            thingInfoData.put("rgistAt",ThingWtnObject.rgistAt) //등기여부
                            thingInfoData.put("prmisnAt", ThingWtnObject.prmisnAt) // 허가여부
                            thingInfoData.put("bldngPrmisnCl", ThingWtnObject.bildngPrmisnCl) //건축허가분류
                            thingInfoData.put("rm", ThingWtnObject.rm) //비고
                            thingInfoData.put("referMatter", ThingWtnObject.referMatter) // 참고사항
                            thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) //실내외여부 1: 실내 2: 실외
                            thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) //등록자 // 테블릿 이용자 아이디(사번-5자리)
                            thingInfoData.put("elemArray", mulitElemString)
                            thingInfoData.put("ordinateArray", mulitGeomString)
                            thingInfoData.put("buldNm", ThingWtnObject.buldName) //건물명
                            thingInfoData.put("buldPrpos", ThingWtnObject.buldPrpos) //건물용도
                            thingInfoData.put("buldStrct", ThingWtnObject.buldStrct) //건물구조
                            thingInfoData.put("buldDong", ThingWtnObject.buldDongName) //건물동
                            thingInfoData.put("buldHo", ThingWtnObject.buldHoName) //건물호
                            thingInfoData.put("buldFlrato", ThingWtnObject.buldFlrato) //건물층별
                            thingInfoData.put("buldAr", ThingWtnObject.buldAr)
                            thingInfoData.put("nrtBuldAt", ThingWtnObject.thingNrtBuldAt)
                            thingInfoData.put("ownerInfo", ThingWtnObject.thingOwnerInfoJson)
                            if(ThingWtnObject.pointYn == "POINT") {
                                thingInfoData.put("pointYn", "1")
                            } else {
                                thingInfoData.put("pointYn", "2")
                            }
                            thingRequestData.put("thing", thingInfoData)
                        } else {
                            thingBuildUrl = context.resources.getString(R.string.mobile_url) + "updateThingBuld"
                            thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                            thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                            thingInfoData.put("thingWtnCode", thingDataJson.getString("thingWtnCode"))  //물건조서코드
                            thingInfoData.put("thingLrgeCl", ThingWtnObject.thingLrgeCl) //물건 대분류 코드 //임시
                            thingInfoData.put("thingSmallCl", ThingWtnObject.thingSmallCl) //물건 소분류 코드 //임시
                            thingInfoData.put("legaldongCode", thingDataJson.getString("legaldongCode"))  //소재지코드
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                            thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의종류
                            thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및구격
                            thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래수량
                            thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                            thingInfoData.put("unitCl", ThingWtnObject.unitCl) //면적단위코드
                            thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                            thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                            thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                            thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                            thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                            thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                            thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                            thingInfoData.put("redeBingAt", ThingWtnObject.redeBingAt) //주거용건축물여부
                            thingInfoData.put("rgistAt",ThingWtnObject.rgistAt) //등기여부
                            thingInfoData.put("prmisnAt", ThingWtnObject.prmisnAt) // 허가여부
                            thingInfoData.put("bldngPrmisnCl", ThingWtnObject.bildngPrmisnCl) //건축허가분류
                            thingInfoData.put("rm", ThingWtnObject.rm) //비고
                            thingInfoData.put("referMatter", ThingWtnObject.referMatter) //참고사항
                            thingInfoData.put("changeResn", ThingWtnObject.changeResn) //변경사유
                            thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) //실내여부
                            thingInfoData.put("updusr", PreferenceUtil.getString(context!!, "id", "defaual")) //업데이트 //임시 // 테블릿 이용자
                            thingInfoData.put("elemArray", mulitElemString)
                            thingInfoData.put("ordinateArray", mulitGeomString)
                            thingInfoData.put("buldWtnCode", thingDataJson.getString("buldWtnCode")) //건물조서코드
                            thingInfoData.put("buldNm", ThingWtnObject.buldName) //건물명
                            thingInfoData.put("buldPrpos", ThingWtnObject.buldPrpos) //건물용도
                            thingInfoData.put("buldFlrato", ThingWtnObject.buldFlrato) //건물층별
                            thingInfoData.put("buldDong", ThingWtnObject.buldDongName) //건물동명
                            thingInfoData.put("buldHo", ThingWtnObject.buldHoName) //건물호명
                            thingInfoData.put("buldStrct",ThingWtnObject.buldStrct) // 건물구조
                            thingInfoData.put("buldAr", ThingWtnObject.buldAr)
                            thingInfoData.put("regstrBuldNmDfnAt", ThingWtnObject.regstrBuldNmDfnAt) //대장상 건축명상이여부
                            thingInfoData.put("regstrBuldPrposDfnAt", ThingWtnObject.regstrBuldPrposDfnAt)//대장상 건축 용더 상이여부
                            thingInfoData.put("regstrBuldStrctDfnAt", ThingWtnObject.regstrBuldStrctDfnAt) //대장상 건축물구조
                            thingInfoData.put("regstrBuldDongDfnAt", ThingWtnObject.regstrBuldDongDfnAt)//대장상 건축 동 상이여부
                            thingInfoData.put("regstrBuldFlratoDfnAt", ThingWtnObject.regstrBuldFlratoDfnAt)//대장상 건축층별 상이여부
                            thingInfoData.put("regstrBuldHoDfnAt", ThingWtnObject.regstrBuldHoDfnAt)//대장상 건축 호 상이여부
                            thingInfoData.put("regstrArDfnAt", ThingWtnObject.regstrBuldArDfnAt)//대장상 건축물 면적 상이여부
                            thingInfoData.put("regstrDfnDtls", ThingWtnObject.regstrDfnDtls)//대장상 상이내역
                            thingInfoData.put("rgistBuldNmDfnAt", ThingWtnObject.rgistBuldNmDfnAt)//등기상 건물 건물명 상이여부
                            thingInfoData.put("rgistBuldPrposDfnAt", ThingWtnObject.rgistBuldPrposDfnAt)//등기상 건물 용도 상이여부
                            thingInfoData.put("rgistBuldStrctDfnAt", ThingWtnObject.rgistBuldStrctDfnAt)//등기상 건물 구조 상이여부
                            thingInfoData.put("rgistBuldDongDfnAt", ThingWtnObject.rgistBuldDongDfnAt)//등기상 건물 동 상이여부
                            thingInfoData.put("rgistBuldFlratoDfnAt", ThingWtnObject.rgistBuldFlratoDfnAt)//등기상 건물 층별 상이여부
                            thingInfoData.put("rgistBuldHoDfnAt", ThingWtnObject.rgistBuldHoDfnAt)//등기상 건물  호 상이여부
                            thingInfoData.put("rgistArDfnAt", ThingWtnObject.rgistArDfnAt)//등기상 건물 면적 상이 여부
                            thingInfoData.put("rgistDfnDtls", ThingWtnObject.rgistDfnDtls)//등기상 상이 내역
                            thingInfoData.put("nrtBuldAt", ThingWtnObject.thingNrtBuldAt)
                            thingRequestData.put("thing", thingInfoData)
                        }

                    }

                    "A023005" -> { //수목
                        var mulitGeomString: String? = ""
                        var mulitElemString: String? = ""
                        var coordSize = 0

                        log.d("thingPolygonData ---------------------------------><><><><><><")

                        mulitElemString += "1, 1003, 1"
                        if(ThingWtnObject.pointYn == "POINT") {
                            for (i in 0 until thingPolygonData.size) {
                                var coord = thingPolygonData[i].coords[0]

                                val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                                val x = BigDecimal.valueOf(webMercatorCoord.x)
                                val y = BigDecimal.valueOf(webMercatorCoord.y)

                                coordSize++

                                mulitGeomString += "$x,$y"
                                if (coordSize != thingPolygonData.size) {
                                    mulitGeomString += ","
                                }
                            }
                        } else {
                            for (coord in thingPolygonData[0].coords) {

                                val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                                val x = BigDecimal.valueOf(webMercatorCoord.x)
                                val y = BigDecimal.valueOf(webMercatorCoord.y)

                                coordSize++

                                mulitGeomString += "$x,$y"
                                if (coordSize != thingPolygonData[0].coords.size) {
                                    mulitGeomString += ","
                                }

                            }
                        }

                        log.d("ThingPolygon String geom " + mulitGeomString.toString())

                        if (ThingWtnObject.thingNewSearch.equals("Y")) {
                            thingBuildUrl = context.resources.getString(R.string.mobile_url) + "registThingWdpt"
                            thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                            thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                            thingInfoData.put("incrprLnm", thingDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingLrgeCl", "A011001") //물건 대분류 코드 //임시
                            thingInfoData.put("thingSmallCl", ThingWtnObject.thingSmallCl) //물건 소분류 코드 //임시
                            thingInfoData.put("legaldongCode", thingDataJson.getString("legaldongCode"))  //소재지코드
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                            thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의 종류
                            thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및 구격
                            thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래 수량
                            thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                            thingInfoData.put("unitCl", ThingWtnObject.unitCl) //단위 코드
                            thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                            thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                            thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                            thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                            thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                            thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                            thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                            thingInfoData.put("rm", ThingWtnObject.rm) //비고
                            thingInfoData.put("referMatter", ThingWtnObject.referMatter) // 참고사항
                            thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                            thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) //등록자 // 테블릿 이용자 아이디(사번-5자리)
                            thingInfoData.put("elemArray", mulitElemString)
                            thingInfoData.put("ordinateArray", mulitGeomString)
                            //수목 -> 정상식여부, 사유, 조사방식
                            thingInfoData.put("examinMthd", ThingWtnObject.examinMthd)
                            thingInfoData.put("nrmltpltAt", ThingWtnObject.nrmltpltAt)
                            thingInfoData.put("wdptResn", ThingWtnObject.wdptResn)
                            thingInfoData.put("ownerInfo", ThingWtnObject.thingOwnerInfoJson)
                            if(ThingWtnObject.pointYn == "POINT") {
                                thingInfoData.put("pointYn", "1")
                            } else {
                                thingInfoData.put("pointYn", "2")
                            }

                            thingRequestData.put("thing", thingInfoData)
                        } else {
                            thingBuildUrl = context.resources.getString(R.string.mobile_url) + "updateThingWdpt"
                            thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                            thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                            thingInfoData.put("thingWtnCode", thingDataJson.getString("thingWtnCode"))  //물건조서코드
                            thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                            thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의종류
                            thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및구격
                            thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래수량
                            thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                            thingInfoData.put("unitCl",   ThingWtnObject.unitCl) //면적단위코드
                            thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                            thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                            thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                            thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                            thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                            thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                            thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                            thingInfoData.put("rm", ThingWtnObject.rm) //비고
                            thingInfoData.put("referMatter", ThingWtnObject.referMatter) //참고사항
                            thingInfoData.put("changeResn", ThingWtnObject.changeResn) //변경사유
                            thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                            thingInfoData.put("updusr", PreferenceUtil.getString(context!!, "id", "defaual")) //업데이트 //임시 // 테블릿 이용자
                            thingInfoData.put("elemArray", mulitElemString)
                            thingInfoData.put("ordinateArray", mulitGeomString)
                            //수목 -> 정상식여부, 사유
                            thingInfoData.put("nrmltpltAt", ThingWtnObject.nrmltpltAt)
                            thingInfoData.put("wdptResn", ThingWtnObject.wdptResn)
                            thingInfoData.put("examinMthd", ThingWtnObject.examinMthd)
                            thingRequestData.put("thing", thingInfoData)
                        }
                    }

                    else -> {

                        var mulitGeomString: String? = ""
                        var mulitElemString: String? = ""
                        var coordSize = 0

                        log.d("thingPolygonData ---------------------------------><><><><><><")

                        mulitElemString += "1, 1003, 1"
                        for (coord in thingPolygonData[0].coords) {

                            val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                            val x = BigDecimal.valueOf(webMercatorCoord.x)
                            val y = BigDecimal.valueOf(webMercatorCoord.y)

                            coordSize++

                            mulitGeomString += "$x,$y"
                            if (coordSize != thingPolygonData.get(0).coords.size) {
                                mulitGeomString += ","
                            }

                        }

                        log.d("ThingPolygon String geom ${mulitGeomString.toString()}")

                        when {
                            ThingWtnObject.thingNewSearch.equals("Y") -> {
                                thingBuildUrl = context.resources.getString(R.string.mobile_url) + "registThing"
                                thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                                thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                                thingInfoData.put("incrprLnm", thingDataJson.getString("incrprLnm"))
                                thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                                thingInfoData.put("thingLrgeCl", ThingWtnObject.thingLrgeCl) //물건 대분류 코드 //임시
                                thingInfoData.put("thingSmallCl", ThingWtnObject.thingSmallCl) //물건 소분류 코드 //임시
                                thingInfoData.put("legaldongCode", thingDataJson.getString("legaldongCode"))  //소재지코드
                                thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의 종류
                                thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및 구격
                                thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래 수량
                                thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                                thingInfoData.put("unitCl", ThingWtnObject.unitCl) //단위 코드
                                thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                                thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                                thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                                thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                                thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                                thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                                thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                                thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                                thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                                thingInfoData.put("rm", ThingWtnObject.rm) //비고
                                thingInfoData.put("referMatter", ThingWtnObject.referMatter) // 참고사항
                                thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                                thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) //등록자 // 테블릿 이용자 아이디(사번-5자리)
                                thingInfoData.put("elemArray", mulitElemString)
                                thingInfoData.put("ordinateArray", mulitGeomString)
                                thingInfoData.put("ownerInfo", ThingWtnObject.thingOwnerInfoJson)
                                if(ThingWtnObject.pointYn == "POINT") {
                                    thingInfoData.put("pointYn", "1")
                                } else {
                                    thingInfoData.put("pointYn", "2")
                                }
                                thingRequestData.put("thing", thingInfoData)
                            }
                            else -> {
                                thingBuildUrl = context.resources.getString(R.string.mobile_url) + "updateThing"
                                thingInfoData.put("saupCode", thingDataJson.getString("saupCode")) //사업코드
                                thingInfoData.put("ladWtnCode", thingDataJson.getString("ladWtnCode")) //토지조서코드
                                thingInfoData.put("incrprLnm", thingDataJson.getString("incrprLnm"))
                                thingInfoData.put("thingWtnCode", thingDataJson.getString("thingWtnCode"))  //물건조서코드
                                thingInfoData.put("indoorTy", ThingWtnObject.thingIndoorTy) // 실내여부
                                thingInfoData.put("thingKnd", ThingWtnObject.thingKnd) //물건의종류
                                thingInfoData.put("strctNdStndrd", ThingWtnObject.strctNdStndrd) //구조및구격
                                thingInfoData.put("bgnnAr", ThingWtnObject.bgnnAr) //원래수량
                                thingInfoData.put("incrprAr", ThingWtnObject.incrprAr) //편입수량
                                thingInfoData.put("unitCl", ThingWtnObject.unitCl) //면적단위코드
                                thingInfoData.put("arComputBasis", ThingWtnObject.arComputBasis) //면적산출근거
                                thingInfoData.put("acqsCl", ThingWtnObject.acqsCl)
                                thingInfoData.put("inclsCl", ThingWtnObject.inclsCl)
                                thingInfoData.put("ownerCnfirmBasisCl", ThingWtnObject.ownerCnfirmBasisCl)
                                thingInfoData.put("rwTrgetAt", ThingWtnObject.rwTrgetAt) //보상대상여부
                                thingInfoData.put("apasmtTrgetAt", ThingWtnObject.apasmtTrgetAt) //감정평가대상여부
                                thingInfoData.put("ownshipBeforeAt", ThingWtnObject.ownshipBeforeAt) //소유권이전여부
                                thingInfoData.put("sttusMesrAt", ThingWtnObject.sttusMesrAt) //현황측량여부
                                thingInfoData.put("rm", ThingWtnObject.rm) //비고
                                thingInfoData.put("referMatter", ThingWtnObject.referMatter) //참고사항
                                thingInfoData.put("changeResn", ThingWtnObject.changeResn) //변경사유
                                thingInfoData.put("paclrMatter", ThingWtnObject.paclrMatter) //특이사항
                                thingInfoData.put("updusr", PreferenceUtil.getString(context!!, "id", "defaual")) //업데이트 //임시 // 테블릿 이용자
                                thingInfoData.put("elemArray", mulitElemString)
                                thingInfoData.put("ordinateArray", mulitGeomString)
                                thingRequestData.put("thing", thingInfoData)
                            }
                        }
                    }

                }

                callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingBuildUrl!!)
                dialogUtil.run {
                    wtnncCnfirmDialog(dialogBuilder, "지장물조서").show()
                    setClickListener(callBackListener)
                }
            }

            BSN -> {
                val bsnDataJson = ThingBsnObject.thingInfo as JSONObject

                val thingPolygonData = ThingBsnObject.thingBsnSketchPolygon

                if(thingPolygonData == null) {
                    toast.msg_error("물건(영업) 조서의 스케치가 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 1000)
                } else {
                    var mulitGeomString: String? = ""
                    var mulitElemString: String? = ""
                    var coordSize = 0

                    log.d("thingBsn ----------------------------<><><><><><")

                    mulitElemString += "1, 1003, 1"
                    for (coord in thingPolygonData[0].coords) {

                        val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                        val x = BigDecimal.valueOf(webMercatorCoord.x)
                        val y = BigDecimal.valueOf(webMercatorCoord.y)

                        coordSize++

                        mulitGeomString += "$x,$y"
                        if (coordSize != thingPolygonData[0].coords.size) {
                            mulitGeomString += ","
                        }
                    }

                    log.d("thingBsnPloygon Strin gome --------------------> $mulitGeomString")

                    val thingRequestData = JSONObject()
                    val thingInfoData = JSONObject()

                    if (ThingBsnObject.thingNewSearch == "Y") {
                        thingBsnUrl = context.resources.getString(R.string.mobile_url) + "registThingBsn"
                        // 물건조서 기초
                        thingInfoData.put("saupCode", bsnDataJson.getString("saupCode"))
                        thingInfoData.put("ladWtnCode", bsnDataJson.getString("ladWtnCode"))
                        thingInfoData.put("thingLrgeCl", ThingBsnObject.thingLrgeCl)
                        thingInfoData.put("thingSmallCl", ThingBsnObject.thingSmallCl)
                        thingInfoData.put("incrprLnm", bsnDataJson.getString("incrprLnm"))
                        thingInfoData.put("legaldongCode", bsnDataJson.getString("legaldongCode"))
                        thingInfoData.put("thingKnd", ThingBsnObject.thingKnd)
                        thingInfoData.put("strctNdStndrd", ThingBsnObject.strctNdStndrd)
                        thingInfoData.put("bgnnAr", ThingBsnObject.bgnnAr)
                        thingInfoData.put("incrprAr", ThingBsnObject.incrprAr)
                        thingInfoData.put("unitCl", ThingBsnObject.unitCl)
                        thingInfoData.put("arComputBasis", ThingBsnObject.arComputBasis)
                        thingInfoData.put("acqsCl", ThingBsnObject.acqsCl)
                        thingInfoData.put("inclsCl", ThingBsnObject.inclsCl)
                        thingInfoData.put("rwTrgetAt", ThingBsnObject.rwTrgetAt)
                        thingInfoData.put("apasmtTrgetAt", ThingBsnObject.apasmtTrgetAt)
                        thingInfoData.put("ownshipBeforeAt", ThingBsnObject.ownshipBeforeAt)
                        thingInfoData.put("ownerCnfirmBasisCl", ThingBsnObject.ownerCnfirmBasisCl)
                        thingInfoData.put("rm", ThingBsnObject.rm)
                        thingInfoData.put("referMatter", ThingBsnObject.referMatter)
                        thingInfoData.put("paclrMatter", ThingBsnObject.paclrMatter)
                        thingInfoData.put("bsnCl", ThingBsnObject.bsnCl)
                        thingInfoData.put("sssMthCo", ThingBsnObject.sssMthCo)
                        //장소의적법성
                        thingInfoData.put("araLgalAt", ThingBsnObject.araLgalAt)
                        thingInfoData.put("buldWtnCode", bsnDataJson.getString("buldWtnCode"))
                        //점유의적법성
                        thingInfoData.put("pssLgalAt", ThingBsnObject.pssLgalAt)
                        thingInfoData.put("pssPssTy", ThingBsnObject.pssPssTy)
                        thingInfoData.put("pssHireCntrctAt", ThingBsnObject.pssHireCntrctAt)
                        thingInfoData.put("pssRentName", ThingBsnObject.pssRentName)
                        thingInfoData.put("pssHireName", ThingBsnObject.pssHireName)
                        thingInfoData.put("pssRentBgnde", ThingBsnObject.pssRentBgnde)
                        thingInfoData.put("pssRentEndde", ThingBsnObject.pssRentEndde)
                        thingInfoData.put("pssGtn", ThingBsnObject.pssGtn)
                        thingInfoData.put("pssMtht", ThingBsnObject.pssMtht)
                        thingInfoData.put("pssCntrctLc", ThingBsnObject.pssCntrctLc)
                        thingInfoData.put("pssCntrctAr", ThingBsnObject.pssCntrctAr)
                        thingInfoData.put("pssSpccntr", ThingBsnObject.pssSpccntr)
                        //영업의적법성
                        thingInfoData.put("bsnProperAt", ThingBsnObject.bsnProperAt)
                        thingInfoData.put("bsnSgnProsAt", ThingBsnObject.bsnSgnProsAt)
                        thingInfoData.put("bsnPrmisnCl", ThingBsnObject.bsnPrmisnCl)
                        thingInfoData.put("bsnPrmsTrgetNm", ThingBsnObject.bsnPrmsTrgetNm)
                        thingInfoData.put("bsnPrmisnNo", ThingBsnObject.bsnPrmisnNo)
                        thingInfoData.put("bsnPrmisnDe", ThingBsnObject.bsnPrmisnDe)
                        thingInfoData.put("bsnPrmisnBgnde", ThingBsnObject.bsnPrmisnBgnde)
                        thingInfoData.put("bsnPrmisnEndde", ThingBsnObject.bsnPrmisnEndde)
                        thingInfoData.put("bsnPrmisnInstt", ThingBsnObject.bsnPrmisnInstt)
                        thingInfoData.put("bsnBsnpdBgnde", ThingBsnObject.bsnBsnpdBgnde)
                        thingInfoData.put("bsnBsnpdEndde", ThingBsnObject.bsnBsnpdEndde)
                        thingInfoData.put("bsnBsnplcLc", ThingBsnObject.bsnBsnplcLc)
                        thingInfoData.put("bsnBsnplcAr", ThingBsnObject.bsnBsnplcAr)
                        //인적물적시설
                        thingInfoData.put("hmftyProperAt", ThingBsnObject.hmftyProperAt)
                        thingInfoData.put("hmftySgnbrdNm", ThingBsnObject.hmftySgnbrdNm)
                        thingInfoData.put("hmftyResdngHnf", ThingBsnObject.hmftyResdngHnf)
                        thingInfoData.put("hmftyFtyAt", ThingBsnObject.hmftyFtyAt)
                        thingInfoData.put("hmftySgnSecdAt", ThingBsnObject.hmftySgnSecdAt)
                        //사업자등록 내역
                        thingInfoData.put("bizrdtlsSttAt", ThingBsnObject.bizrdtlsSttAt)
                        thingInfoData.put("bizrdtlsPrftmkTy", ThingBsnObject.bizrdtlsPrftmkTy)
                        thingInfoData.put("bizrdtlsRegTy", ThingBsnObject.bizrdtlsRegTy)
                        thingInfoData.put("bizrdtlsRprsntvNm", ThingBsnObject.bizrdtlsRprsntvNm)
                        thingInfoData.put("bizrdtlsMtlty", ThingBsnObject.bizrdtlsMtlty)
                        thingInfoData.put("bizrdtlsInduty", ThingBsnObject.bizrdtlsInduty)
                        thingInfoData.put("bizrdtlsBizcnd", ThingBsnObject.bizrdtlsBizcnd)
                        thingInfoData.put("bizrdtlsBizrno", ThingBsnObject.bizrdtlsBizrno)
                        thingInfoData.put("bizrdtlsBizDe", ThingBsnObject.bizrdtlsBizDe)
                        thingInfoData.put("bizrdtlsRegAt", ThingBsnObject.bizrdtlsRegAt)

                        thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                        thingInfoData.put("elemArray", mulitElemString)
                        thingInfoData.put("ordinateArray", mulitGeomString)
                        if(ThingBsnObject.pointYn == "POINT") {
                            thingInfoData.put("pointYn", "1")
                        } else {
                            thingInfoData.put("pointYn", "2")
                        }
                        thingInfoData.put("brdDtlsList", ThingBsnObject.addBsnBrdpdList)
                        thingInfoData.put("bsnBuldLink", ThingBsnObject.addBuldLinkList)
                        thingInfoData.put("bsnThing", ThingBsnObject.addBsnThingList)
                        thingInfoData.put("ownerInfo", ThingBsnObject.thingOwnerInfoJson) // 영업 소유자

                        thingRequestData.put("thing", thingInfoData)

                    } else {
                        thingBsnUrl = context.resources.getString(R.string.mobile_url) + "updateThingBsn"
                        // 물건조서 기초
                        thingInfoData.put("saupCode", bsnDataJson.getString("saupCode"))
                        thingInfoData.put("ladWtnCode", bsnDataJson.getString("ladWtnCode"))
                        thingInfoData.put("incrprLnm", bsnDataJson.getString("incrprLnm"))
                        thingInfoData.put("thingWtnCode", bsnDataJson.getString("thingWtnCode"))
                        thingInfoData.put("bsnWtnCode", bsnDataJson.getString("bsnWtnCode"))
                        thingInfoData.put("thingLrgeCl", ThingBsnObject.thingLrgeCl)
                        thingInfoData.put("thingSmallCl", ThingBsnObject.thingSmallCl)
                        thingInfoData.put("legaldongCode", bsnDataJson.getString("legaldongCode"))
                        thingInfoData.put("thingKnd", ThingBsnObject.thingKnd)
                        thingInfoData.put("strctNdStndrd", ThingBsnObject.strctNdStndrd)
                        thingInfoData.put("bgnnAr", ThingBsnObject.bgnnAr)
                        thingInfoData.put("incrprAr", ThingBsnObject.incrprAr)
                        thingInfoData.put("unitCl", ThingBsnObject.unitCl)
                        thingInfoData.put("arComputBasis", ThingBsnObject.arComputBasis)
                        thingInfoData.put("acqsCl", ThingBsnObject.acqsCl)
                        thingInfoData.put("inclsCl", ThingBsnObject.inclsCl)
                        thingInfoData.put("rwTrgetAt", ThingBsnObject.rwTrgetAt)
                        thingInfoData.put("apasmtTrgetAt", ThingBsnObject.apasmtTrgetAt)
                        thingInfoData.put("ownshipBeforeAt", ThingBsnObject.ownshipBeforeAt)
                        thingInfoData.put("ownerCnfirmBasisCl", ThingBsnObject.ownerCnfirmBasisCl)
                        thingInfoData.put("rm", ThingBsnObject.rm)
                        thingInfoData.put("referMatter", ThingBsnObject.referMatter)
                        thingInfoData.put("paclrMatter", ThingBsnObject.paclrMatter)
                        //장소의적법성
                        thingInfoData.put("araLgalAt", ThingBsnObject.araLgalAt)
                        thingInfoData.put("buldWtnCode", bsnDataJson.getString("buldWtnCode"))
                        //점유의적법성
                        thingInfoData.put("pssLgalAt", ThingBsnObject.pssLgalAt)
                        thingInfoData.put("pssPssTy", ThingBsnObject.pssPssTy)
                        thingInfoData.put("pssHireCntrctAt", ThingBsnObject.pssHireCntrctAt)
                        thingInfoData.put("pssRentName", ThingBsnObject.pssRentName)
                        thingInfoData.put("pssHireName", ThingBsnObject.pssHireName)
                        thingInfoData.put("pssRentBgnde", ThingBsnObject.pssRentBgnde)
                        thingInfoData.put("pssRentEndde", ThingBsnObject.pssRentEndde)
                        thingInfoData.put("pssGtn", ThingBsnObject.pssGtn)
                        thingInfoData.put("pssMtht", ThingBsnObject.pssMtht)
                        thingInfoData.put("pssCntrctLc", ThingBsnObject.pssCntrctLc)
                        thingInfoData.put("pssCntrctAr", ThingBsnObject.pssCntrctAr)
                        thingInfoData.put("pssSpccntr", ThingBsnObject.pssSpccntr)
                        //영업의적법성
                        thingInfoData.put("bsnProperAt", ThingBsnObject.bsnProperAt)
                        thingInfoData.put("bsnSgnProsAt", ThingBsnObject.bsnSgnProsAt)
                        thingInfoData.put("bsnPrmisnCl", ThingBsnObject.bsnPrmisnCl)
                        thingInfoData.put("bsnPrmsTrgetNm", ThingBsnObject.bsnPrmsTrgetNm)
                        thingInfoData.put("bsnPrmisnNo", ThingBsnObject.bsnPrmisnNo)
                        thingInfoData.put("bsnPrmisnDe", ThingBsnObject.bsnPrmisnDe)
                        thingInfoData.put("bsnPrmisnBgnde", ThingBsnObject.bsnPrmisnBgnde)
                        thingInfoData.put("bsnPrmisnEndde", ThingBsnObject.bsnPrmisnEndde)
                        thingInfoData.put("bsnPrmisnInstt", ThingBsnObject.bsnPrmisnInstt)
                        thingInfoData.put("bsnBsnpdBgnde", ThingBsnObject.bsnBsnpdBgnde)
                        thingInfoData.put("bsnBsnpdEndde", ThingBsnObject.bsnBsnpdEndde)
                        thingInfoData.put("bsnBsnplcLc", ThingBsnObject.bsnBsnplcLc)
                        thingInfoData.put("bsnBsnplcAr", ThingBsnObject.bsnBsnplcAr)
                        //인적물적시설
                        thingInfoData.put("hmftyProperAt", ThingBsnObject.hmftyProperAt)
                        thingInfoData.put("hmftySgnbrdNm", ThingBsnObject.hmftySgnbrdNm)
                        thingInfoData.put("hmftyResdngHnf", ThingBsnObject.hmftyResdngHnf)
                        thingInfoData.put("hmftyFtyAt", ThingBsnObject.hmftyFtyAt)
                        thingInfoData.put("hmftySgnSecdAt", ThingBsnObject.hmftySgnSecdAt)
                        //사업자등록 내역
                        thingInfoData.put("bizrdtlsSttAt", ThingBsnObject.bizrdtlsSttAt)
                        thingInfoData.put("bizrdtlsPrftmkTy", ThingBsnObject.bizrdtlsPrftmkTy)
                        thingInfoData.put("bizrdtlsRegTy", ThingBsnObject.bizrdtlsRegTy)
                        thingInfoData.put("bizrdtlsRprsntvNm", ThingBsnObject.bizrdtlsRprsntvNm)
                        thingInfoData.put("bizrdtlsMtlty", ThingBsnObject.bizrdtlsMtlty)
                        thingInfoData.put("bizrdtlsInduty", ThingBsnObject.bizrdtlsInduty)
                        thingInfoData.put("bizrdtlsBizcnd", ThingBsnObject.bizrdtlsBizcnd)
                        thingInfoData.put("bizrdtlsBizrno", ThingBsnObject.bizrdtlsBizrno)
                        thingInfoData.put("bizrdtlsBizDe", ThingBsnObject.bizrdtlsBizDe)
                        thingInfoData.put("bizrdtlsRegAt", ThingBsnObject.bizrdtlsRegAt)

                        thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                        thingInfoData.put("elemArray", mulitElemString)
                        thingInfoData.put("ordinateArray", mulitGeomString)
                        thingInfoData.put("brdDtlsList", ThingBsnObject.addBsnBrdpdList)
                        thingInfoData.put("bsnBuldLink", ThingBsnObject.addBuldLinkList)
                        thingInfoData.put("bsnThing", ThingBsnObject.addBsnThingList)

                        thingRequestData.put("thing", thingInfoData)
                    }

                    // TODO: 2021-10-25 자동화 작업
                    /*(
                      장소 (적법 or 부적법)
                      점유 (자가 or 임차)
                      영업 (허가등구분 기재)
                      허가등번호 기재, (단 자유업일 경우 미기재)
                      인적물적시설 (적격 or 부적격)
                      사업자등록(등록일 기재, 상호명 기재)
                     )*/

                    callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingBsnUrl!!)

                    dialogUtil.run{
                        wtnncCnfirmDialog(dialogBuilder, "영업조서").show()
                        setClickListener(callBackListener)
                    }

                }
            }

            FARM -> {
                val farmDataJson = ThingFarmObject.thingInfo as JSONObject
                val thingFarmPolygon = ThingFarmObject.thingFarmSketchPolygon

                if(thingFarmPolygon == null) {
                    toast.msg_error("물건(농업) 조서의 스케치가 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 1000)
                    return
                } else {
                    val thingFarmPolygonArr: MutableList<ArrayList<LatLng>>?

                    val thingFarmPolygonSize = 1

                    var mulitGeomString: String? = ""
                    var mulitElemString: String? = ""
                    var polyStartPoint = 1

                    log.d("thingFarm -------------------------<><><><><><><")

                    try {
                        val tempB = mutableListOf<ArrayList<LatLng>>()

                        thingFarmPolygon.forEach {
                            val tempA = ArrayList<LatLng>()
                            it.coords.forEach { coord ->
                                tempA.add(coord)
                            }
                            log.d(it.toString())
                            tempB.add(tempA)
                            log.d("tempB size -> ${tempB.size}")
                        }

                        thingFarmPolygonArr = tempB

                        thingFarmPolygonArr.forEach { thingFarmPolygon ->
                            log.d(thingFarmPolygon.toString())

                            var thingFarmCoordsSize = 0
                            var geomString: String? = ""

                            mulitElemString += "$polyStartPoint, 1003, 1"

                            thingFarmPolygon.forEachIndexed { _, code ->

                                val webMercatorCoord = WebMercatorCoord.valueOf(code)
                                val x = BigDecimal.valueOf(webMercatorCoord.x)
                                val y = BigDecimal.valueOf(webMercatorCoord.y)

                                geomString += "$x,$y"
                                thingFarmCoordsSize++

                                if (thingFarmCoordsSize != thingFarmPolygon.size) {
                                    geomString += ","
                                }
                                polyStartPoint += 2
                            }

                            log.d("thingFarmPolygon String geom -> $geomString")

                            mulitGeomString += geomString.toString()
                            if (thingFarmPolygonSize != thingFarmPolygonArr.size) {
                                mulitGeomString += ","
                                mulitElemString += ","
                            }
                        }

                        log.d("thingFarmPloygon String geom --------------------------> $mulitGeomString")

                        thingRequestData = JSONObject()
                        thingInfoData = JSONObject()

                        if(ThingFarmObject.thingNewSearch == "Y") {
                            thingFarmUrl = context.resources.getString(R.string.mobile_url) + "registThingFarm"
                            // 물건조서 기초
                            thingInfoData.put("saupCode", farmDataJson.getString("saupCode"))
                            thingInfoData.put("ladWtnCode",farmDataJson.getString("ladWtnCode"))
                            thingInfoData.put("incrprLnm", farmDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingLrgeCl",ThingFarmObject.thingLrgeCl)
                            thingInfoData.put("thingSmallCl",ThingFarmObject.thingSmallCl)
                            thingInfoData.put("legaldongCode",farmDataJson.getString("legaldongCode"))
                            thingInfoData.put("incrprLnm", farmDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingKnd",ThingFarmObject.thingKnd)
                            thingInfoData.put("strctNdStndrd",ThingFarmObject.strctNdStndrd)
                            thingInfoData.put("bgnnAr",ThingFarmObject.bgnnAr)
                            thingInfoData.put("incrprAr",ThingFarmObject.incrprAr)
                            thingInfoData.put("unitCl",ThingFarmObject.unitCl)
                            thingInfoData.put("arComputBasis",ThingFarmObject.arComputBasis)
                            thingInfoData.put("acqsCl",ThingFarmObject.acqsCl)
                            thingInfoData.put("inclsCl",ThingFarmObject.inclsCl)
                            thingInfoData.put("rwTrgetAt",ThingFarmObject.rwTrgetAt)
                            thingInfoData.put("apasmtTrgetAt",ThingFarmObject.apasmtTrgetAt)
                            thingInfoData.put("ownshipBeforeAt",ThingFarmObject.ownshipBeforeAt)
                            thingInfoData.put("ownerCnfirmBasisCl",ThingFarmObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rm",ThingFarmObject.rm)
                            thingInfoData.put("referMatter",ThingFarmObject.referMatter)
                            thingInfoData.put("paclrMatter",ThingFarmObject.paclrMatter)
                            thingInfoData.put("indoorTy","2")
                            //농지의 적법성
                            thingInfoData.put("farmWtnCode", farmDataJson.getString("farmWtnCode"))
                            thingInfoData.put("frldbsLgalAt",ThingFarmObject.frldbsLgalAt)
                            thingInfoData.put("frldbsBasisCl",ThingFarmObject.frldbsBasisCl)
                            thingInfoData.put("frldbsFrldLdgrAt",ThingFarmObject.frldbsFrldLdgrAt)

                            //농민의 적법성
                            thingInfoData.put("frmrbsLgalAt",ThingFarmObject.frmrbsLgalAt)
                            thingInfoData.put("frmrbsBasisCl",ThingFarmObject.frmrbsBasisCl)
                            thingInfoData.put("frmrbsCnfrmnDta1At",ThingFarmObject.frmrbsCnfrmnDta1At)
                            thingInfoData.put("frmrbsCnfrmnDta2At",ThingFarmObject.frmrbsCnfrmnDta2At)
                            thingInfoData.put("frmrbsDbtamtAt",ThingFarmObject.frmrbsDbtamtAt)
                            thingInfoData.put("frmrbsAraResideAt",ThingFarmObject.frmrbsAraResideAt)

                            //점유의 적법성
                            thingInfoData.put("posesnLgalAt",ThingFarmObject.posesnLgalAt)
                            thingInfoData.put("posesnClvthmTy",ThingFarmObject.posesnClvthmTy)
                            thingInfoData.put("posesnLadResideAt",ThingFarmObject.posesnLadResideAt)
                            thingInfoData.put("posesnLadFarmerAt",ThingFarmObject.posesnLadFarmerAt)
                            thingInfoData.put("posesnOwnerClvtCnfirmAt",ThingFarmObject.posesnOwnerClvtCnfirmAt)
                            thingInfoData.put("posesnDbtamtRepAt",ThingFarmObject.posesnDbtamtRepAt)
                            thingInfoData.put("posesnDbtamtRepInf",ThingFarmObject.posesnDbtamtRepInf)
                            thingInfoData.put("posesnLrcdocAt",ThingFarmObject.posesnLrcdocAt)
                            thingInfoData.put("posesnRentName",ThingFarmObject.posesnRentName)
                            thingInfoData.put("posesnHireName",ThingFarmObject.posesnHireName)
                            thingInfoData.put("posesnRentBgnde",ThingFarmObject.posesnRentBgnde)
                            thingInfoData.put("posesnRentEndde",ThingFarmObject.posesnRentEndde)
                            thingInfoData.put("posesnGtn",ThingFarmObject.posesnGtn)
                            thingInfoData.put("posesnMtht",ThingFarmObject.posesnMtht)
                            thingInfoData.put("posesnCntrctLc",ThingFarmObject.posesnCntrctLc)
                            thingInfoData.put("posesnCntrctAr",ThingFarmObject.posesnCntrctAr)
                            thingInfoData.put("posesnSpccntr",ThingFarmObject.posesnSpccntr)

                            thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                            thingInfoData.put("elemArray",mulitElemString)
                            thingInfoData.put("ordinateArray",mulitGeomString)
                            if(ThingFarmObject.pointYn == "POINT") {
                                thingInfoData.put("pointYn", "1")
                            } else {
                                thingInfoData.put("pointYn", "2")
                            }
                            thingInfoData.put("farmClvtdlList", ThingFarmObject.addFarmClvtdlList) // 경작여부
                            thingInfoData.put("farmThing", ThingFarmObject.addFarmThignList) //농업 시설물
                            thingInfoData.put("ownerInfo", ThingFarmObject.thingOwnerInfoJson) // 농업 소유자

                            thingRequestData.put("thing", thingInfoData)
                        } else {
                            thingFarmUrl = context.resources.getString(R.string.mobile_url) + "updateThingFarm"

                            // 물건조서 기초
                            thingInfoData.put("saupCode", farmDataJson.getString("saupCode"))
                            thingInfoData.put("ladWtnCode",farmDataJson.getString("ladWtnCode"))
                            thingInfoData.put("incrprLnm", farmDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingWtnCode", farmDataJson.getString("thingWtnCode"))
                            thingInfoData.put("thingLrgeCl",ThingFarmObject.thingLrgeCl)
                            thingInfoData.put("thingSmallCl",ThingFarmObject.thingSmallCl)
                            thingInfoData.put("legaldongCode",farmDataJson.getString("legaldongCode"))
                            thingInfoData.put("incrprLnm", farmDataJson.getString("incrprLnm"))
                            thingInfoData.put("thingKnd",ThingFarmObject.thingKnd)
                            thingInfoData.put("strctNdStndrd",ThingFarmObject.strctNdStndrd)
                            thingInfoData.put("bgnnAr",ThingFarmObject.bgnnAr)
                            thingInfoData.put("incrprAr",ThingFarmObject.incrprAr)
                            thingInfoData.put("unitCl",ThingFarmObject.unitCl)
                            thingInfoData.put("arComputBasis",ThingFarmObject.arComputBasis)
                            thingInfoData.put("acqsCl",ThingFarmObject.acqsCl)
                            thingInfoData.put("inclsCl",ThingFarmObject.inclsCl)
                            thingInfoData.put("rwTrgetAt",ThingFarmObject.rwTrgetAt)
                            thingInfoData.put("apasmtTrgetAt",ThingFarmObject.apasmtTrgetAt)
                            thingInfoData.put("ownshipBeforeAt",ThingFarmObject.ownshipBeforeAt)
                            thingInfoData.put("ownerCnfirmBasisCl",ThingFarmObject.ownerCnfirmBasisCl)
                            thingInfoData.put("rm",ThingFarmObject.rm)
                            thingInfoData.put("referMatter",ThingFarmObject.referMatter)
                            thingInfoData.put("paclrMatter",ThingFarmObject.paclrMatter)
                            thingInfoData.put("indoorTy","2")
                            //농지의 적법성
                            thingInfoData.put("frldbsLgalAt",ThingFarmObject.frldbsLgalAt)
                            thingInfoData.put("frldbsBasisCl",ThingFarmObject.frldbsBasisCl)
                            thingInfoData.put("frldbsFrldLdgrAt",ThingFarmObject.frldbsFrldLdgrAt)

                            //농민의 적법성
                            thingInfoData.put("frmrbsLgalAt",ThingFarmObject.frmrbsLgalAt)
                            thingInfoData.put("frmrbsBasisCl",ThingFarmObject.frmrbsBasisCl)
                            thingInfoData.put("frmrbsCnfrmnDta1At",ThingFarmObject.frmrbsCnfrmnDta1At)
                            thingInfoData.put("frmrbsCnfrmnDta2At",ThingFarmObject.frmrbsCnfrmnDta2At)
                            thingInfoData.put("frmrbsDbtamtAt",ThingFarmObject.frmrbsDbtamtAt)
                            thingInfoData.put("frmrbsAraResideAt",ThingFarmObject.frmrbsAraResideAt)

                            //점유의 적법성
                            thingInfoData.put("posesnLgalAt",ThingFarmObject.posesnLgalAt)
                            thingInfoData.put("posesnClvthmTy",ThingFarmObject.posesnClvthmTy)
                            thingInfoData.put("posesnLadResideAt",ThingFarmObject.posesnLadResideAt)
                            thingInfoData.put("posesnLadFarmerAt",ThingFarmObject.posesnLadFarmerAt)
                            thingInfoData.put("posesnOwnerClvtCnfirmAt",ThingFarmObject.posesnOwnerClvtCnfirmAt)
                            thingInfoData.put("posesnDbtamtRepAt",ThingFarmObject.posesnDbtamtRepAt)
                            thingInfoData.put("posesnDbtamtRepInf",ThingFarmObject.posesnDbtamtRepInf)
                            thingInfoData.put("posesnLrcdocAt",ThingFarmObject.posesnLrcdocAt)
                            thingInfoData.put("posesnRentName",ThingFarmObject.posesnRentName)
                            thingInfoData.put("posesnHireName",ThingFarmObject.posesnHireName)
                            thingInfoData.put("posesnRentBgnde",ThingFarmObject.posesnRentBgnde)
                            thingInfoData.put("posesnRentEndde",ThingFarmObject.posesnRentEndde)
                            thingInfoData.put("posesnGtn",ThingFarmObject.posesnGtn)
                            thingInfoData.put("posesnMtht",ThingFarmObject.posesnMtht)
                            thingInfoData.put("posesnCntrctLc",ThingFarmObject.posesnCntrctLc)
                            thingInfoData.put("posesnCntrctAr",ThingFarmObject.posesnCntrctAr)
                            thingInfoData.put("posesnSpccntr",ThingFarmObject.posesnSpccntr)

                            thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                            thingInfoData.put("elemArray",mulitElemString)
                            thingInfoData.put("ordinateArray",mulitGeomString)
                            thingInfoData.put("farmClvtdlList", ThingFarmObject.addFarmClvtdlList) // 경작여부
                            thingInfoData.put("farmThing", ThingFarmObject.addFarmThignList) //농업 시설물
                            thingInfoData.put("ownerInfo", ThingFarmObject.thingOwnerInfoJson) // 농업 소유자

                            thingRequestData.put("thing", thingInfoData)


                            thingRequestData.put("thing", thingInfoData)

                        }

                        // TODO: 2021-10-25 자동화 작업
                        /* (
                          농지 (적법 or 부적법)
                          농민 (적법 o 부적법)
                          점유 (자경 or 임차농)
                          경작 (경작여부 'Y'인 경우 해당하는 작물명 기재
                          실제소득 증빙이 'Y'인 경우 연간평균 수입 기재
                         )*/

                        callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingFarmUrl!!)

                        dialogUtil.run{
                            wtnncCnfirmDialog(dialogBuilder, "농업조서").show()
                            setClickListener(callBackListener)
                        }

                    } catch (e: Exception) {
                        log.e(e.toString())

                    }
                }
            }

            RESIDNT -> {
                val residntDataJson = ThingResidntObject.thingInfo as JSONObject

                val thingPolygonData = ThingResidntObject.thingResidntSketchPolygon

                var mulitGeomString: String? = ""
                var mulitElemString: String? = ""
                var coordSize = 0

                log.d("thingResidnt --------------------------------<><><><>><<>><<>")

                mulitElemString += "1, 1003, 1"
                for(coord in thingPolygonData!![0].coords) {

                    val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                    val x = BigDecimal.valueOf(webMercatorCoord.x)
                    val y = BigDecimal.valueOf(webMercatorCoord.y)

                    coordSize++

                    mulitGeomString += "$x,$y"
                    if(coordSize != thingPolygonData[0].coords.size) {
                        mulitGeomString += ","
                    }
                }

                log.d("thingResidntPolygon String geom ----------------------> $mulitGeomString")

                thingRequestData = JSONObject()
                thingInfoData = JSONObject()

                ResidntSearchFragment(this,this, this).addResidntData()

                if(ThingResidntObject.thingNewSearch.equals("Y")) {
                    thingResidntUrl = context.resources.getString(R.string.mobile_url) + "registThingResidnt"
                    thingInfoData.put("saupCode", residntDataJson.getString("saupCode"))
                    thingInfoData.put("ladWtnCode",residntDataJson.getString("ladWtnCode"))
                    thingInfoData.put("thingLrgeCl",ThingResidntObject.thingLrgeCl)
                    thingInfoData.put("thingSmallCl",ThingResidntObject.thingSmallCl)
                    thingInfoData.put("legaldongCode",residntDataJson.getString("legaldongCode"))
                    thingInfoData.put("thingKnd",ThingResidntObject.thingKnd)
                    thingInfoData.put("strctNdStndrd",ThingResidntObject.strctNdStndrd)
                    thingInfoData.put("bgnnAr",ThingResidntObject.bgnnAr)
                    thingInfoData.put("incrprAr",ThingResidntObject.incrprAr)
                    thingInfoData.put("unitCl",ThingResidntObject.unitCl)
                    thingInfoData.put("arComputBasis",ThingResidntObject.arComputBasis)
                    thingInfoData.put("acqsCl",ThingResidntObject.acqsCl)
                    thingInfoData.put("inclsCl",ThingResidntObject.inclsCl)
                    thingInfoData.put("rwTrgetAt",ThingResidntObject.rwTrgetAt)
                    thingInfoData.put("apasmtTrgetAt",ThingResidntObject.apasmtTrgetAt)
                    thingInfoData.put("ownshipBeforeAt",ThingResidntObject.ownshipBeforeAt)
                    thingInfoData.put("ownerCnfirmBasisCl",ThingResidntObject.ownerCnfirmBasisCl)
                    thingInfoData.put("rm",ThingResidntObject.rm)
                    thingInfoData.put("referMatter",ThingResidntObject.referMatter)
                    thingInfoData.put("paclrMatter",ThingResidntObject.paclrMatter)
                    // 장소의 적법성
//            thingInfoData.put("araLgalAt", ThingResidntObject.araLgalAt)
                    // 점유의 적법성
                    thingInfoData.put("pssLgalAt", ThingResidntObject.pssLgalAt)
                    thingInfoData.put("pssPssCl", ThingResidntObject.pssPssCl)
                    thingInfoData.put("pssHireCntrctAt", ThingResidntObject.pssHireCntrctAt)
                    thingInfoData.put("pssRentName", ThingResidntObject.pssRentName)
                    thingInfoData.put("pssHireName", ThingResidntObject.pssHireName)
                    thingInfoData.put("pssRentBgnde", ThingResidntObject.residePdBgnde)
                    thingInfoData.put("pssRentEndde", ThingResidntObject.residePdEndde)
                    thingInfoData.put("pssGtn", ThingResidntObject.pssGtn)
                    thingInfoData.put("pssMtht", ThingResidntObject.pssMtht)
                    thingInfoData.put("pssCntrctLc", ThingResidntObject.pssCntrctLc)
                    thingInfoData.put("pssCntrctAr", ThingResidntObject.pssCntrctAr)
                    thingInfoData.put("pssSpccntr", ThingResidntObject.pssSpccntr)
//            //공익사업 재편입근거
//            thingInfoData.put("reincrprProperAt", ThingResidntObject.reincrprProperAt)
//            thingInfoData.put("reincrprBgnnBsnsNm", ThingResidntObject.reincrprBgnnBsnsNm)
//            thingInfoData.put("reincrprNtfcDe", ThingResidntObject.reincrprNtfcDe)
//            thingInfoData.put("reincrprRwDe", ThingResidntObject.reincrprRwDe)
//            thingInfoData.put("reincrprBsAt", ThingResidntObject.reincrprBsAt)
//            thingInfoData.put("reincrprAddibs",ThingResidntObject.reincrprAddibs)

                    //거주자내역
                    thingInfoData.put("residntDtlsList", ThingResidntObject.addResidntDtlsList)
                    //장소의적법성 내역
                    thingInfoData.put("residntBuldLink", ThingResidntObject.addBuldLinkList)
                    //거주자 소유자
                    thingInfoData.put("ownerInfo", ThingResidntObject.thingOwnerInfoJson)

                    //기타.
                    thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                    thingInfoData.put("elemArray",mulitElemString)
                    thingInfoData.put("ordinateArray",mulitGeomString)
                    if(ThingResidntObject.pointYn == "POINT") {
                        thingInfoData.put("pointYn", "1")
                    } else {
                        thingInfoData.put("pointYn", "2")
                    }

                    thingRequestData.put("thing", thingInfoData)

                } else {
                    thingResidntUrl = context.resources.getString(R.string.mobile_url) + "updateThingResidnt"

                    thingInfoData.put("saupCode", residntDataJson.getString("saupCode"))
                    thingInfoData.put("ladWtnCode",residntDataJson.getString("ladWtnCode"))
                    thingInfoData.put("thingWtnCode", residntDataJson.getString("thingWtnCode"))
                    thingInfoData.put("residntWtnCode", residntDataJson.getString("residntWtnCode"))
                    thingInfoData.put("thingLrgeCl",ThingResidntObject.thingLrgeCl)
                    thingInfoData.put("thingSmallCl",ThingResidntObject.thingSmallCl)
                    thingInfoData.put("legaldongCode",residntDataJson.getString("legaldongCode"))
                    thingInfoData.put("thingKnd",ThingResidntObject.thingKnd)
                    thingInfoData.put("strctNdStndrd",ThingResidntObject.strctNdStndrd)
                    thingInfoData.put("bgnnAr",ThingResidntObject.bgnnAr)
                    thingInfoData.put("incrprAr",ThingResidntObject.incrprAr)
                    thingInfoData.put("unitCl",ThingResidntObject.unitCl)
                    thingInfoData.put("arComputBasis",ThingResidntObject.arComputBasis)
                    thingInfoData.put("acqsCl",ThingResidntObject.acqsCl)
                    thingInfoData.put("inclsCl",ThingResidntObject.inclsCl)
                    thingInfoData.put("rwTrgetAt",ThingResidntObject.rwTrgetAt)
                    thingInfoData.put("apasmtTrgetAt",ThingResidntObject.apasmtTrgetAt)
                    thingInfoData.put("ownshipBeforeAt",ThingResidntObject.ownshipBeforeAt)
                    thingInfoData.put("ownerCnfirmBasisCl",ThingResidntObject.ownerCnfirmBasisCl)
                    thingInfoData.put("rm",ThingResidntObject.rm)
                    thingInfoData.put("referMatter",ThingResidntObject.referMatter)
                    thingInfoData.put("paclrMatter",ThingResidntObject.paclrMatter)
                    // 장소의 적법성
//            thingInfoData.put("araLgalAt", ThingResidntObject.araLgalAt)
                    // 점유의 적법성
                    thingInfoData.put("pssLgalAt", ThingResidntObject.pssLgalAt)
                    thingInfoData.put("pssPssCl", ThingResidntObject.pssPssCl)
                    thingInfoData.put("pssHireCntrctAt", ThingResidntObject.pssHireCntrctAt)
                    thingInfoData.put("pssRentName", ThingResidntObject.pssRentName)
                    thingInfoData.put("pssHireName", ThingResidntObject.pssHireName)
                    thingInfoData.put("pssRentBgnde", ThingResidntObject.residePdBgnde)
                    thingInfoData.put("pssRentEndde", ThingResidntObject.residePdEndde)
                    thingInfoData.put("pssGtn", ThingResidntObject.pssGtn)
                    thingInfoData.put("pssMtht", ThingResidntObject.pssMtht)
                    thingInfoData.put("pssCntrctLc", ThingResidntObject.pssCntrctLc)
                    thingInfoData.put("pssCntrctAr", ThingResidntObject.pssCntrctAr)
                    thingInfoData.put("pssSpccntr", ThingResidntObject.pssSpccntr)
//            //공익사업 재편입근거
//            thingInfoData.put("reincrprProperAt", ThingResidntObject.reincrprProperAt)
//            thingInfoData.put("reincrprBgnnBsnsNm", ThingResidntObject.reincrprBgnnBsnsNm)
//            thingInfoData.put("reincrprNtfcDe", ThingResidntObject.reincrprNtfcDe)
//            thingInfoData.put("reincrprRwDe", ThingResidntObject.reincrprRwDe)
//            thingInfoData.put("reincrprBsAt", ThingResidntObject.reincrprBsAt)
//            thingInfoData.put("reincrprAddibs",ThingResidntObject.reincrprAddibs)

                    //거주자내역
                    thingInfoData.put("residntDtlsList", ThingResidntObject.addResidntDtlsList)
                    //장소의적법성 내역
                    thingInfoData.put("residntBuldLink", ThingResidntObject.addBuldLinkList)

                    //기타.
                    thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual"))// 임시등록자
                    thingInfoData.put("elemArray",mulitElemString)
                    thingInfoData.put("ordinateArray",mulitGeomString)

                    thingRequestData.put("thing", thingInfoData)
                }

                // TODO: 2021-10-25 자동화 작업
                /* (
                  1. 이주정착금
                    장소 (적법 or 부적법)
                    소유 (점유구분이 '자가'인 경우 적법 or 부적법)
                    거주 (거주자 성명 중 보상대상자의 이름이 있는 경우 적격여부인 적격 or 부적격 기재)
                    거주장소 보상금액 (장소의 적법성 근거에 기재된 건물등에 대한 보상금액 모두 기재)
                 )*/

                callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingResidntUrl!!)

                dialogUtil.run{
                    wtnncCnfirmDialog(dialogBuilder, "거주자조서").show()
                    setClickListener(callBackListener)
                }

            }

            TOMB -> {
                val thingPolygonData = ThingTombObject.thingTombSketchPolyton
                if (thingPolygonData == null) {
                    toast.msg_error("분묘 조서의 스케치가 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 1000)
                } else {

                    val tombDataJson = ThingTombObject.thingInfo as JSONObject

                    var mulitGeomString: String? = ""
                    var mulitElemString: String? = ""
                    var coordSize = 0

                    log.d("thingTomb-------------------------------------<><><><><<><")

                    mulitElemString += "1, 1003, 1"
                    for (coord in thingPolygonData[0].coords) {

                        val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                        val x = BigDecimal.valueOf(webMercatorCoord.x)
                        val y = BigDecimal.valueOf(webMercatorCoord.y)

                        coordSize++

                        mulitGeomString += x.toString() + "," + y.toString()
                        if (coordSize != thingPolygonData.get(0).coords.size) {
                            mulitGeomString += ","
                        }
                    }

                    log.d("ThingTombPolygon String geom ----------------> $mulitGeomString")

                    thingRequestData = JSONObject()
                    thingInfoData = JSONObject()

                    if (ThingTombObject.thingNewSearch == "Y") {
                        thingTombUrl =
                            context.resources.getString(R.string.mobile_url) + "registThingTomb"
                        thingInfoData.put("saupCode", tombDataJson.getString("saupCode")) //사업자코드
                        thingInfoData.put(
                            "ladWtnCode",
                            tombDataJson.getString("ladWtnCode")
                        ) //토지조서코드
                        thingInfoData.put(
                            "incrprLnm",
                            ThingTombObject.thingInfo!!.getString("incrprLnm")
                        )
                        thingInfoData.put("thingLrgeCl", ThingTombObject.thingLrgeCl) //대분류
                        thingInfoData.put("thingSmallCl", ThingTombObject.thingSmallCl) //소분류
                        thingInfoData.put("legaldongCode", ThingTombObject.legaldongCode) //소재지코드
                        thingInfoData.put("thingKnd", ThingTombObject.thingKnd) //물건의종류
                        thingInfoData.put("strctNdStndrd", ThingTombObject.strctNdStndrd) //구조및규격
                        thingInfoData.put("bgnnAr", ThingTombObject.bgnnAr) //원래수량
                        thingInfoData.put("incrprAr", ThingTombObject.incrprAr) //편입수량
                        thingInfoData.put("unitCl", ThingTombObject.unitCl) //단위코드
                        thingInfoData.put("arComputBasis", ThingTombObject.arComputBasis) //면적산출근거
                        thingInfoData.put("rm", ThingTombObject.rm) //비고
                        thingInfoData.put("referMatter", ThingTombObject.referMatter) // 참고사항
                        thingInfoData.put("paclrMatter", ThingTombObject.paclrMatter) //특이사항
                        thingInfoData.put("rwTrgetAt", ThingTombObject.rwTrgetAt) // 보상대상여부
                        thingInfoData.put("apasmtTrgetAt", ThingTombObject.apasmtTrgetAt) // 감정평가대상여부
                        thingInfoData.put("ownerCnfirmBasisCl", ThingTombObject.ownerCnfirmBasisCl) // 소유자확인근거
                        thingInfoData.put("acqsCl", ThingTombObject.acqsCl) // 취득분류
                        thingInfoData.put("inclsCl", ThingTombObject.inclsCl)   // 포함분류
                        thingInfoData.put("tombNo", ThingTombObject.tombNo) //분묘번호
                        thingInfoData.put("tombTy", ThingTombObject.tombTy) // 분묘 타입
                        thingInfoData.put("burlDe", ThingTombObject.burlDe) // 매장일자
                        thingInfoData.put("balmCl", ThingTombObject.balmCl) //연고자 유무
                        thingInfoData.put("tombCl", ThingTombObject.tombCl) // 분묘유형
                        thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) //등록자//임시로그인등록번호
                        thingInfoData.put("elemArray", mulitElemString) // 폴리곤어레이
                        thingInfoData.put("ordinateArray", mulitGeomString) //폴리곤정보
                        if(ThingTombObject.pointYn == "POINT") {
                            thingInfoData.put("pointYn", "1")
                        } else {
                            thingInfoData.put("pointYn", "2")
                        }
                        thingInfoData.put("buriedPerson", ThingTombObject.addBuriedPerson) // 매장자
                        thingInfoData.put("buriedThing", ThingTombObject.addBuriedThing) // 분묘 시설물
                        thingInfoData.put("ownerInfo", ThingTombObject.thingOwnerInfoJson)
                        thingRequestData.put("thing", thingInfoData)
                    } else {
                        thingTombUrl = context.resources.getString(R.string.mobile_url) + "updateThingTomb"

                        thingInfoData.put("saupCode", tombDataJson.getString("saupCode")) //사업자코드
                        thingInfoData.put(
                            "ladWtnCode",
                            tombDataJson.getString("ladWtnCode")
                        ) //토지조서코드
                        thingInfoData.put(
                            "incrprLnm",
                            ThingTombObject.thingInfo!!.getString("incrprLnm")
                        )
                        thingInfoData.put("thingWtnCode", tombDataJson.getString("thingWtnCode"))
                        thingInfoData.put("thingLrgeCl", ThingTombObject.thingLrgeCl) //대분류
                        thingInfoData.put("thingSmallCl", ThingTombObject.thingSmallCl) //소분류
                        thingInfoData.put("legaldongCode", ThingTombObject.legaldongCode) //소재지코드
                        thingInfoData.put("thingKnd", ThingTombObject.thingKnd) //물건의종류
                        thingInfoData.put("strctNdStndrd", ThingTombObject.strctNdStndrd) //구조및규격
                        thingInfoData.put("bgnnAr", ThingTombObject.bgnnAr) //원래수량
                        thingInfoData.put("incrprAr", ThingTombObject.incrprAr) //편입수량
                        thingInfoData.put("unitCl", ThingTombObject.unitCl) //단위코드
                        thingInfoData.put("arComputBasis", ThingTombObject.arComputBasis) //면적산출근거
                        thingInfoData.put("rm", ThingTombObject.rm) //비고
                        thingInfoData.put("referMatter", ThingTombObject.referMatter) // 참고사항
                        thingInfoData.put("paclrMatter", ThingTombObject.paclrMatter) //특이사항
                        thingInfoData.put("rwTrgetAt", ThingTombObject.rwTrgetAt) //보상대상여부
                        thingInfoData.put("apasmtTrgetAt", ThingTombObject.apasmtTrgetAt) //감정평가대상여부
                        thingInfoData.put("ownerCnfirmBasisCl", ThingTombObject.ownerCnfirmBasisCl) // 소유자확인근거
                        thingInfoData.put("tombWtnCode", tombDataJson.getString("tombWtnCode"))
                        thingInfoData.put("tombNo", ThingTombObject.tombNo)     //분묘 번호
                        thingInfoData.put("tombTy", ThingTombObject.tombTy) // 분묘 타입
                        thingInfoData.put("burlDe", ThingTombObject.burlDe) // 매장일자
                        thingInfoData.put("balmCl", ThingTombObject.balmCl) //연고자 유무
                        thingInfoData.put("tombCl", ThingTombObject.tombCl) // 분묘유형
                        thingInfoData.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) //등록자
                        thingInfoData.put("elemArray", mulitElemString) // 폴리곤어레이
                        thingInfoData.put("ordinateArray", mulitGeomString) //폴리곤정보
                        thingInfoData.put("buriedPerson", ThingTombObject.addBuriedPerson) // 매장자
                        thingInfoData.put("buriedThing", ThingTombObject.addBuriedThing) // 분묘 시설물
                        thingRequestData.put("thing", thingInfoData)

                    }

                    callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingTombUrl!!)

                    // TODO: 2021-10-25 자동화 작업 (분묘구분 Spinner 포지션이 '0'번째 일 경우에는 Insert 시키면 안됨.)
                    dialogUtil.run {
                        wtnncCnfirmDialog(dialogBuilder, "분묘조서").show()
                        setClickListener(callBackListener)
                    }
                }
            }

            MINRGT -> {
                val minrgtDataJson = ThingMinrgtObject.thingInfo as JSONObject
                val thingPolygonData = ThingMinrgtObject.thingMinrgtSketchPolygon

                var mulitGeomString: String? = ""
                var mulitElemString: String? = ""
                var coordSize = 0

                log.d("thingMinrgt --------------------------------------<><><><><>")

                mulitElemString += "1, 1003, 1"
                for(coord in thingPolygonData!![0].coords) {

                    val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                    val x = BigDecimal.valueOf(webMercatorCoord.x)
                    val y = BigDecimal.valueOf(webMercatorCoord.y)

                    coordSize++

                    mulitGeomString += "$x,$y"
                    if(coordSize != thingPolygonData[0].coords.size) {
                        mulitGeomString += ","
                    }
                }

                log.d("ThingMinrgtPolygon String geom ----------------> $mulitGeomString")

                thingRequestData = JSONObject()
                thingInfoData = JSONObject()

                MinrgtSearchFragment(this, this).addMinrgtData()

                if(ThingMinrgtObject.thingNewSearch == "Y") {
                    thingMinrgtUrl = context.resources.getString(R.string.mobile_url) + "registThingMinrgt"
                    thingInfoData.put("saupCode", minrgtDataJson.getString("saupCode"))
                    thingInfoData.put("ladWtnCode",minrgtDataJson.getString("ladWtnCode"))
                    thingInfoData.put("incrprLnm", minrgtDataJson.getString("incrprLnm"))
                    thingInfoData.put("thingLrgeCl",ThingMinrgtObject.thingLrgeCl)
                    thingInfoData.put("thingSmallCl",ThingMinrgtObject.thingSmallCl)
                    thingInfoData.put("legaldongCode",minrgtDataJson.getString("legaldongCode"))
                    thingInfoData.put("thingKnd",ThingMinrgtObject.thingKnd)
                    thingInfoData.put("strctNdStndrd",ThingMinrgtObject.strctNdStndrd)
                    thingInfoData.put("bgnnAr",ThingMinrgtObject.bgnnAr)
                    thingInfoData.put("incrprAr",ThingMinrgtObject.incrprAr)
                    thingInfoData.put("unitCl",ThingMinrgtObject.unitCl)
                    thingInfoData.put("arComputBasis",ThingMinrgtObject.arComputBasis)
                    thingInfoData.put("bsnCl", ThingMinrgtObject.bsnCl)
                    thingInfoData.put("sssMthCo", ThingMinrgtObject.sssMthCo)
                    thingInfoData.put("acqsCl",ThingMinrgtObject.acqsCl)
                    thingInfoData.put("inclsCl",ThingMinrgtObject.inclsCl)
                    thingInfoData.put("rwTrgetAt",ThingMinrgtObject.rwTrgetAt)
                    thingInfoData.put("apasmtTrgetAt",ThingMinrgtObject.apasmtTrgetAt)
                    thingInfoData.put("ownshipBeforeAt",ThingMinrgtObject.ownshipBeforeAt)
                    thingInfoData.put("ownerCnfirmBasisCl",ThingMinrgtObject.ownerCnfirmBasisCl)
                    thingInfoData.put("rm",ThingMinrgtObject.rm)
                    thingInfoData.put("referMatter",ThingMinrgtObject.referMatter)
                    thingInfoData.put("paclrMatter",ThingMinrgtObject.paclrMatter)
                    thingInfoData.put("minrgtRegNo",ThingMinrgtObject.minrgtRegNo)   //광업등록번호
                    thingInfoData.put("minrgtRegDe",ThingMinrgtObject.minrgtRegDe)   //광업등록일자
                    thingInfoData.put("cntnncPdBgnde",ThingMinrgtObject.cntnncPdBgnde) //존속기간시작일
                    thingInfoData.put("cntnncPdEndde",ThingMinrgtObject.cntnncPdEndde) //존속기간끝일
                    thingInfoData.put("mnrlKnd",ThingMinrgtObject.mnrlKnd)       //광종
                    thingInfoData.put("minrgtAr",ThingMinrgtObject.minrgtAr)      //광업면적
                    thingInfoData.put("prsptnPlanStemDe",ThingMinrgtObject.prsptnPlanStemDe)  //탐광계획신고일자
                    thingInfoData.put("miningPlanCnfmDe",ThingMinrgtObject.miningPlanCnfmDe)  //채광계획(변경)인가일자
                    thingInfoData.put("mnrlPrdnRprtAt",ThingMinrgtObject.mnrlPrdnRprtAt)    //광물샌산자보고자료여부
                    thingInfoData.put("minrgtLgstr", ThingMinrgtObject.minrgtLgstr)     //광업지적
                    thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual")) // 임시등록자
                    thingInfoData.put("elemArray",mulitElemString)
                    thingInfoData.put("ordinateArray",mulitGeomString)
                    if(ThingMinrgtObject.pointYn == "POINT") {
                        thingInfoData.put("pointYn", "1")
                    } else {
                        thingInfoData.put("pointYn", "2")
                    }
                    thingInfoData.put("ownerInfo", ThingMinrgtObject.thingOwnerInfoJson)
                    thingInfoData.put("minrgtThing",ThingMinrgtObject.addMinrgtThing)   //광업권시설물
                    thingRequestData.put("thing", thingInfoData)
                } else {
                    thingMinrgtUrl = context.resources.getString(R.string.mobile_url) + "updateThingMinrgt"
                    thingInfoData.put("saupCode", minrgtDataJson.getString("saupCode"))
                    thingInfoData.put("ladWtnCode",minrgtDataJson.getString("ladWtnCode"))
                    thingInfoData.put("incrprLnm", minrgtDataJson.getString("incrprLnm"))
                    thingInfoData.put("thingWtnCode", minrgtDataJson.getString("thingWtnCode"))
                    thingInfoData.put("minrgtWtnCode", minrgtDataJson.getString("minrgtWtnCode"))
                    thingInfoData.put("thingLrgeCl",ThingMinrgtObject.thingLrgeCl)
                    thingInfoData.put("thingSmallCl",ThingMinrgtObject.thingSmallCl)
                    thingInfoData.put("legaldongCode",minrgtDataJson.getString("legaldongCode"))
                    thingInfoData.put("thingKnd",ThingMinrgtObject.thingKnd)
                    thingInfoData.put("strctNdStndrd",ThingMinrgtObject.strctNdStndrd)
                    thingInfoData.put("bgnnAr",ThingMinrgtObject.bgnnAr)
                    thingInfoData.put("incrprAr",ThingMinrgtObject.incrprAr)
                    thingInfoData.put("unitCl",ThingMinrgtObject.unitCl)
                    thingInfoData.put("arComputBasis",ThingMinrgtObject.arComputBasis)
                    thingInfoData.put("bsnCl", ThingMinrgtObject.bsnCl)
                    thingInfoData.put("sssMthCo", ThingMinrgtObject.sssMthCo)
                    thingInfoData.put("acqsCl",ThingMinrgtObject.acqsCl)
                    thingInfoData.put("inclsCl",ThingMinrgtObject.inclsCl)
                    thingInfoData.put("rwTrgetAt",ThingMinrgtObject.rwTrgetAt)
                    thingInfoData.put("apasmtTrgetAt",ThingMinrgtObject.apasmtTrgetAt)
                    thingInfoData.put("ownshipBeforeAt",ThingMinrgtObject.ownshipBeforeAt)
                    thingInfoData.put("ownerCnfirmBasisCl",ThingMinrgtObject.ownerCnfirmBasisCl)
                    thingInfoData.put("rm",ThingMinrgtObject.rm)
                    thingInfoData.put("referMatter",ThingMinrgtObject.referMatter)
                    thingInfoData.put("paclrMatter",ThingMinrgtObject.paclrMatter)
                    thingInfoData.put("minrgtRegNo",ThingMinrgtObject.minrgtRegNo)   //광업등록번호
                    thingInfoData.put("minrgtRegDe",ThingMinrgtObject.minrgtRegDe)   //광업등록일자
                    thingInfoData.put("cntnncPdBgnde",ThingMinrgtObject.cntnncPdBgnde) //존속기간시작일
                    thingInfoData.put("cntnncPdEndde",ThingMinrgtObject.cntnncPdEndde) //존속기간끝일
                    thingInfoData.put("mnrlKnd",ThingMinrgtObject.mnrlKnd)       //광종
                    thingInfoData.put("minrgtAr",ThingMinrgtObject.minrgtAr)      //광업면적
                    thingInfoData.put("prsptnPlanStemDe",ThingMinrgtObject.prsptnPlanStemDe)  //탐광계획신고일자
                    thingInfoData.put("miningPlanCnfmDe",ThingMinrgtObject.miningPlanCnfmDe)  //채광계획(변경)인가일자
                    thingInfoData.put("mnrlPrdnRprtAt",ThingMinrgtObject.mnrlPrdnRprtAt)    //광물샌산자보고자료여부
                    thingInfoData.put("minrgtLgstr", ThingMinrgtObject.minrgtLgstr)     //광업지적
                    thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual")) // 임시등록자
                    thingInfoData.put("elemArray",mulitElemString)
                    thingInfoData.put("ordinateArray",mulitGeomString)
                    thingInfoData.put("minrgtThing",ThingMinrgtObject.addMinrgtThing)   //광업권시설물
                    thingRequestData.put("thing", thingInfoData)
                }

                // TODO: 2021-10-25 자동화 작업 (광업권 등록내역(등록번호 기재, 등록일자 기재, 존속기간 기재), 광업지적(광업지적 기재), 광종(광종기재) )
                callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingMinrgtUrl!!)

                dialogUtil.run{
                    wtnncCnfirmDialog(dialogBuilder, "광업조서").show()
                    setClickListener(callBackListener)
                }
            }

            FYHTS -> {
                val thingPolygonData = ThingFyhtsObject.thingFyhtsSketchPolygon

                if(thingPolygonData == null) {
                    toast.msg_error("어업권 조서의 스케치가 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 500)
                    return
                } else {
                    var mulitGeomString: String? = ""
                    var mulitElemString: String? = ""
                    var coordSize = 0

                    log.d("thingFyhts ----------------------------------><><><><><><><<>")

                    mulitElemString += "1, 1003, 1"
                    for(coord in thingPolygonData[0].coords) {

                        val webMercatorCoord = WebMercatorCoord.valueOf(coord)

                        val x = BigDecimal.valueOf(webMercatorCoord.x)
                        val y = BigDecimal.valueOf(webMercatorCoord.y)

                        coordSize++

                        mulitGeomString += "$x,$y"
                        if(coordSize != thingPolygonData[0].coords.size) {
                            mulitGeomString += ","
                        }

                    }

                    log.d("ThingFyhtsPolygon String geom ------------------> $mulitGeomString")

                    thingRequestData = JSONObject()
                    thingInfoData = JSONObject()

                    FyhtsSearchFragment(this, this).addFyhtsThing()

                    if(ThingFyhtsObject.thingNewSearch == "Y") {
                        thingFyhtsUrl = context.resources.getString(R.string.mobile_url) + "registThingFyhts"
                        thingInfoData.put("saupCode", PreferenceUtil.getString(applicationContext, "saupCode", "defaual"))
                        thingInfoData.put("ladWtnCode","")
                        thingInfoData.put("thingLrgeCl", ThingFyhtsObject.thingLrgeCl)
                        thingInfoData.put("thingSmallCl", ThingFyhtsObject.thingSmallCl)
                        thingInfoData.put("legaldongCode", ThingFyhtsObject.legaldongCl)
                        thingInfoData.put("thingKnd", ThingFyhtsObject.thingKnd)
                        thingInfoData.put("strctNdStndrd", ThingFyhtsObject.strctNdStndrd)
                        thingInfoData.put("bgnnAr", ThingFyhtsObject.bgnnAr)
                        thingInfoData.put("incrprAr", ThingFyhtsObject.incrprAr)
                        thingInfoData.put("unitCl", ThingFyhtsObject.unitCl)
                        thingInfoData.put("arComputBasis", ThingFyhtsObject.arComputBasis)
                        thingInfoData.put("acqsCl", ThingFyhtsObject.acqsCl)
                        thingInfoData.put("inclsCl", ThingFyhtsObject.inclsCl)
                        thingInfoData.put("rwTrgetAt", ThingFyhtsObject.rwTrgetAt)
                        thingInfoData.put("apasmtTrgetAt", ThingFyhtsObject.apasmtTrgetAt)
                        thingInfoData.put("ownshipBeforeAt", ThingFyhtsObject.ownshipBeforeAt)
                        thingInfoData.put("ownerCnfirmBasisCl", ThingFyhtsObject.ownerCnfirmBasisCl)
                        thingInfoData.put("rm", ThingFyhtsObject.rm)
                        thingInfoData.put("referMatter", ThingFyhtsObject.referMatter)
                        thingInfoData.put("paclrMatter", ThingFyhtsObject.paclrMatter)
                        //어업
                        thingInfoData.put("bsnCl", ThingFyhtsObject.bsnCl) //
                        thingInfoData.put("sssMthCo", ThingFyhtsObject.sssMthCo)
                        thingInfoData.put("administGrc", ThingFyhtsObject.administGrc) //행정관청
                        thingInfoData.put("lcnsCl", ThingFyhtsObject.lcnsCl)
                        thingInfoData.put("lcnsKnd", ThingFyhtsObject.lcnsKnd)//면허종류
                        thingInfoData.put("lcnsNo", ThingFyhtsObject.lcnsNo)//면허번호
                        thingInfoData.put("lcnsDe", ThingFyhtsObject.lcnsDe)//면허일자
                        thingInfoData.put("cntnncPdBgnde", ThingFyhtsObject.fyhtsCntnncPdBgnde)//존속기간 시작
                        thingInfoData.put("cntnncPdEndde", ThingFyhtsObject.fyhtsCntnncPdEndde)//존속기간 끝
                        thingInfoData.put("fshlLc", ThingFyhtsObject.fshlLc)//어장의 위치
                        thingInfoData.put("fyhtsAr", ThingFyhtsObject.fyhtsAr)//면적
                        thingInfoData.put("fshrMth", ThingFyhtsObject.fshrMth)//어업의 방법
                        thingInfoData.put("srfwtrLcZoneAt", ThingFyhtsObject.srfwtrLcZoneAt)//수면의 위치 및 구역도 여부

                        thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual")) // 임시등록자
                        thingInfoData.put("elemArray",mulitElemString)
                        thingInfoData.put("ordinateArray",mulitGeomString)
                        if(ThingFyhtsObject.pointYn == "POINT") {
                            thingInfoData.put("pointYn", "1")
                        } else {
                            thingInfoData.put("pointYn", "2")
                        }

                        thingInfoData.put("ownerInfo", ThingFyhtsObject.thingOwnerInfoJson)
                        thingInfoData.put("fyhtsThing", ThingFyhtsObject.addFyhtsThing)

                        thingRequestData.put("thing", thingInfoData)

                    } else {
                        val fyhtsDataJson = ThingFyhtsObject.thingInfo as JSONObject
                        thingFyhtsUrl = context.resources.getString(R.string.mobile_url) + "updateThingFyhts"
                        thingInfoData.put("saupCode",fyhtsDataJson.getString("saupCode"))
                        thingInfoData.put("ladWtnCode","")
                        thingInfoData.put("thingWtnCode", fyhtsDataJson.getString("thingWtnCode"))
                        thingInfoData.put("thingLrgeCl", ThingFyhtsObject.thingLrgeCl)
                        thingInfoData.put("thingSmallCl", ThingFyhtsObject.thingSmallCl)
                        thingInfoData.put("legaldongCode", ThingFyhtsObject.legaldongCl)
                        thingInfoData.put("thingKnd", ThingFyhtsObject.thingKnd)
                        thingInfoData.put("strctNdStndrd", ThingFyhtsObject.strctNdStndrd)
                        thingInfoData.put("bgnnAr", ThingFyhtsObject.bgnnAr)
                        thingInfoData.put("incrprAr", ThingFyhtsObject.incrprAr)
                        thingInfoData.put("unitCl", ThingFyhtsObject.unitCl)
                        thingInfoData.put("arComputBasis", ThingFyhtsObject.arComputBasis)
                        thingInfoData.put("acqsCl", ThingFyhtsObject.acqsCl)
                        thingInfoData.put("inclsCl", ThingFyhtsObject.inclsCl)
                        thingInfoData.put("rwTrgetAt", ThingFyhtsObject.rwTrgetAt)
                        thingInfoData.put("apasmtTrgetAt", ThingFyhtsObject.apasmtTrgetAt)
                        thingInfoData.put("ownshipBeforeAt", ThingFyhtsObject.ownshipBeforeAt)
                        thingInfoData.put("ownerCnfirmBasisCl", ThingFyhtsObject.ownerCnfirmBasisCl)
                        thingInfoData.put("rm", ThingFyhtsObject.rm)
                        thingInfoData.put("referMatter", ThingFyhtsObject.referMatter)
                        thingInfoData.put("paclrMatter", ThingFyhtsObject.paclrMatter)
                        //어업
                        thingInfoData.put("fyhtsWtnCode", fyhtsDataJson.getString("fyhtsWtnCode"))
                        thingInfoData.put("bsnCl", ThingFyhtsObject.bsnCl) //
                        thingInfoData.put("sssMthCo", ThingFyhtsObject.sssMthCo)
                        thingInfoData.put("administGrc", ThingFyhtsObject.administGrc) //행정관청
                        thingInfoData.put("lcnsCl", ThingFyhtsObject.lcnsCl)
                        thingInfoData.put("lcnsKnd", ThingFyhtsObject.lcnsKnd)//면허종류
                        thingInfoData.put("lcnsNo", ThingFyhtsObject.lcnsNo)//면허번호
                        thingInfoData.put("lcnsDe", ThingFyhtsObject.lcnsDe)//면허일자
                        thingInfoData.put("cntnncPdBgnde", ThingFyhtsObject.fyhtsCntnncPdBgnde)//존속기간 시작
                        thingInfoData.put("cntnncPdEndde", ThingFyhtsObject.fyhtsCntnncPdEndde)//존속기간 끝
                        thingInfoData.put("fshlLc", ThingFyhtsObject.fshlLc)//어장의 위치
                        thingInfoData.put("fyhtsAr", ThingFyhtsObject.fyhtsAr)//면적
                        thingInfoData.put("fshrMth", ThingFyhtsObject.fshrMth)//어업의 방법
                        thingInfoData.put("srfwtrLcZoneAt", ThingFyhtsObject.srfwtrLcZoneAt)//수면의 위치 및 구역도 여부

                        thingInfoData.put("register",PreferenceUtil.getString(context!!, "id", "defaual")) // 임시등록자
                        thingInfoData.put("elemArray",mulitElemString)
                        thingInfoData.put("ordinateArray",mulitGeomString)


                        thingInfoData.put("ownerInfo", ThingFyhtsObject.thingOwnerInfoJson)
                        thingInfoData.put("fyhtsThing", ThingFyhtsObject.addFyhtsThing)
                    }

                    // TODO: 2021-10-25 자동화 작업 ( 행정관청(행정관청 기재), 면허내역(면허종류 기재. 면허번호 기재), 어업의방법 기재 )
                    callBackListener = DialogUtilCallbackListener(this, this, thingRequestData, progressDialog!!, thingFyhtsUrl!!)

                    dialogUtil.run{
                        wtnncCnfirmDialog(dialogBuilder, "어업조서").show()
                        setClickListener(callBackListener)
                    }

                }
            }

        }
    }

    /**
     * 탭 위치 강제적으로 0번째 이동
     */
    private fun setWtnccTabReset() {

        Constants.GLOBAL_TAB_LAYOUT?.run {


            if (selectedTabPosition != 0) {
                setScrollPosition(0, 0f, true)
                runOnUiThread {
                    Constants.GLOBAL_VIEW_PAGER?.currentItem = 0
                }
                .also {
                    log.d("select position -> $selectedTabPosition")
                }
            }
        }

    }

    /**
     * 지장물 Data Set (코루틴 비동기 사용)
     * @description job1 (JSON DATA Set)
     * @description job2 (가져온 Data를 JSON Object에 담는다)
     */
    fun addWtnccThingTypeSetData(){

        val thingFragmet = ThingSearchFragment(this, this, this)
        val bsnFragment = BsnSearchFragment(this, this, this)
        val farmFragment = FarmSearchFragment(this,this,this)
        val tombFragment = TombSearchFragment(this, this)
        val minrgtFragment = MinrgtSearchFragment(this, this)
        val residntFragment = ResidntSearchFragment(this,this, this)
        val fyhtsFragmet = FyhtsSearchFragment(this, this)

        GlobalScope.launch { Dispatchers.Main
                val job1 = async(Dispatchers.Main) {
                    try {
                        progressDialog?.show()
                        when(Constants.BIZ_SUBCATEGORY_KEY) {
                            THING -> thingFragmet.addThingData()
                            BSN -> bsnFragment.addBsnData()
                            FARM -> farmFragment.addFarmData()
                            TOMB -> tombFragment.addTombThingData()
                            MINRGT -> minrgtFragment.addMinrgtData()
                            RESIDNT -> residntFragment.addResidntData()
                            FYHTS -> fyhtsFragmet.addFyhtsThing()
                            else -> null
                        }

                    } catch (e: Exception) {
                        log.d(e.toString())
                        progressDialog?.dismiss()
                    }
                }
                val job2 = async(Dispatchers.Main){
                    delay(2000)
                    setThingJsonData()
                    progressDialog?.dismiss()
                }

                job1.await()
                job2.await()
        }
    }

    /**
     * Http Data Set (지장물)
     */
    fun setWtnccHttpConnectionData(requestDataJson: JSONObject) {

        val url = when(Constants.BIZ_SUBCATEGORY_KEY){
            THING -> thingBuildUrl
            BSN -> thingBsnUrl
            TOMB -> thingTombUrl
            MINRGT -> thingMinrgtUrl
            FARM -> thingFarmUrl
            RESIDNT -> thingResidntUrl
            FYHTS -> thingFyhtsUrl
            else -> ""
        }

        val newThingSearch = when(Constants.BIZ_SUBCATEGORY_KEY){
            THING -> ThingWtnObject.thingNewSearch
            BSN -> ThingBsnObject.thingNewSearch
            TOMB -> ThingTombObject.thingNewSearch
            MINRGT -> ThingMinrgtObject.thingNewSearch
            FARM -> ThingFarmObject.thingNewSearch
            RESIDNT -> ThingResidntObject.thingNewSearch
            FYHTS -> ThingFyhtsObject.thingNewSearch
            else -> "N"
        }

        HttpUtil.getInstance(context)
            .callUrlJsonWebServer(requestDataJson, progressDialog, url!!,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) { progressDialog?.dismiss(); log.e("fail") }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()
                        log.d("$url response $responseString")
                        progressDialog?.dismiss()

                        val thingDataJson = JSONObject(responseString).getJSONObject("list").getJSONObject("ThingSearch") as JSONObject



                        runOnUiThread{
                            toast.msg_info("조서가 등록 되었습니다.", 500)

                            if(newThingSearch.equals("Y")) {
                                updateSearchImage(thingDataJson)
                            } else {
                                //object 초기화
                                when(Constants.BIZ_SUBCATEGORY_KEY){
                                    THING -> {
                                        ThingWtnObject.cleanThingWtnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "지장물")
                                    }
                                    BSN -> {
                                        ThingBsnObject.cleanThingBsnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "영업")
                                    }
                                    TOMB -> {
                                        ThingTombObject.cleanThingTombObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "분묘")
                                    }
                                    MINRGT -> {
                                        ThingMinrgtObject.cleanThingMinrgtObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "광업권")
                                    }
                                    FARM -> {
                                        ThingFarmObject.cleanThingFarmObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "농업")
                                    }
                                    RESIDNT -> {
                                        ThingResidntObject.cleanThingResidntObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "거주자")
                                    }
                                    FYHTS -> {
                                        ThingFyhtsObject.clealThingFyhtsObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "어업권")
                                    }
                                    else -> {}
                                }
                            }



                            bottomPanelClose()
                            naverMap?.clearCartoPolygon()

                            clearCameraValue()
                        }
                    }

                }
            )
    }

    fun updateSearchImage(thingDataJson: JSONObject) {
        var dataSearchImage: MutableList<WtnncImage>? = null
        dataSearchImage = when(Constants.BIZ_SUBCATEGORY_KEY){
            THING -> ThingWtnObject.wtnncImage
            BSN -> ThingBsnObject.wtnncImage
            TOMB -> ThingTombObject.wtnncImage
            MINRGT -> ThingMinrgtObject.wtnncImage
            FARM -> ThingFarmObject.wtnncImage
            RESIDNT -> ThingResidntObject.wtnncImage
            FYHTS -> ThingFyhtsObject.wtnncImage
            else -> null
        }

        if(dataSearchImage != null) {
            for (i in 0 until dataSearchImage!!.size) {
                val item = dataSearchImage.get(i)

                if (!item.fileNameString.equals("")) {
                    val atchRequestMap = HashMap<String, String>()
                    var atchRequestUrl: String? = null

                    val saveImage = File(item.fileNameString)

                    atchRequestUrl = context!!.resources.getString(R.string.mobile_url) + "thingSearchAtchFileUpload"

                    atchRequestMap.put("ladWtnCode", "")
                    atchRequestMap.put("thingWtnCode", thingDataJson.getString("thingWtnCode"))
                    atchRequestMap.put("saupCode", item.saupCode)
                    atchRequestMap.put("rm", item.rmTxt)
                    atchRequestMap.put("atflNm", saveImage.absolutePath)
                    atchRequestMap.put("fileseInfo", item.fileCode)
                    atchRequestMap.put("fileCodeNm", item.fileCodeNm)
                    atchRequestMap.put("register", PreferenceUtil.getString(context!!, "id", "defaual")) // 임시 등록자
                    atchRequestMap.put("atflSize", "")
                    atchRequestMap.put("atflExtsn", ".png")
                    atchRequestMap.put("lon", item.lon)
                    atchRequestMap.put("lat", item.lat)
                    atchRequestMap.put("azimuth", item.azimuth)

                    HttpUtil.getInstance(context!!)
                        .callerUrlInfoPostFileUpload(atchRequestMap, progressDialog, atchRequestUrl, saveImage,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    progressDialog?.dismiss()
                                    logUtil.e("fail")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    logUtil.d("responseString >>>>>>>>>>>>>>>>>>>>>>>>>> $responseString")

                                    progressDialog?.dismiss()
                                    runOnUiThread {
                                        //object 초기화
                                        GlobalScope.launch {
                                            delay(500)
                                            withContext(Dispatchers.Main) {
                                                when(Constants.BIZ_SUBCATEGORY_KEY){
                                                    THING -> {
                                                        ThingWtnObject.cleanThingWtnObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "지장물")
                                                    }
                                                    BSN -> {
                                                        ThingBsnObject.cleanThingBsnObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "영업")
                                                    }
                                                    TOMB -> {
                                                        ThingTombObject.cleanThingTombObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "분묘")
                                                    }
                                                    MINRGT -> {
                                                        ThingMinrgtObject.cleanThingMinrgtObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "광업권")
                                                    }
                                                    FARM -> {
                                                        ThingFarmObject.cleanThingFarmObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "농업")
                                                    }
                                                    RESIDNT -> {
                                                        ThingResidntObject.cleanThingResidntObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "거주자")
                                                    }
                                                    FYHTS -> {
                                                        ThingFyhtsObject.clealThingFyhtsObject()
                                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "어업권")
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        }
                                    }

                                }

                            })
                }else {
                    runOnUiThread {
                        //object 초기화
                        GlobalScope.launch {
                            delay(500)
                            withContext(Dispatchers.Main) {
                                when(Constants.BIZ_SUBCATEGORY_KEY){
                                    THING -> {
                                        ThingWtnObject.cleanThingWtnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "지장물")
                                    }
                                    BSN -> {
                                        ThingBsnObject.cleanThingBsnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "영업")
                                    }
                                    TOMB -> {
                                        ThingTombObject.cleanThingTombObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "분묘")
                                    }
                                    MINRGT -> {
                                        ThingMinrgtObject.cleanThingMinrgtObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "광업권")
                                    }
                                    FARM -> {
                                        ThingFarmObject.cleanThingFarmObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "농업")
                                    }
                                    RESIDNT -> {
                                        ThingResidntObject.cleanThingResidntObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "거주자")
                                    }
                                    FYHTS -> {
                                        ThingFyhtsObject.clealThingFyhtsObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "어업권")
                                    }
                                    else -> {}
                                }
                            }
                        }
//                        when (Constants.BIZ_SUBCATEGORY_KEY) {
//                            THING -> ThingWtnObject.cleanThingWtnObject()
//                            BSN -> ThingBsnObject.cleanThingBsnObject()
//                            TOMB -> ThingTombObject.cleanThingTombObject()
//                            MINRGT -> ThingMinrgtObject.cleanThingMinrgtObject()
//                            FARM -> ThingFarmObject.cleanThingFarmObject()
//                            RETS -> ThingFyhtsObject.clealThingFyhtsObject()
//                            else SIDNT -> ThingResidntObject.cleanThingResidntObject()
//                            FYH-> {
//                            }
//                        }
                    }

                }

            }
        } else {
            runOnUiThread {
                        //object 초기화
                        GlobalScope.launch {
                            delay(500)
                            withContext(Dispatchers.Main) {
                                when(Constants.BIZ_SUBCATEGORY_KEY){
                                    THING -> {
                                        ThingWtnObject.cleanThingWtnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "지장물")
                                    }
                                    BSN -> {
                                        ThingBsnObject.cleanThingBsnObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "영업")
                                    }
                                    TOMB -> {
                                        ThingTombObject.cleanThingTombObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "분묘")
                                    }
                                    MINRGT -> {
                                        ThingMinrgtObject.cleanThingMinrgtObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "광업권")
                                    }
                                    FARM -> {
                                        ThingFarmObject.cleanThingFarmObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "농업")
                                    }
                                    RESIDNT -> {
                                        ThingResidntObject.cleanThingResidntObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "거주자")
                                    }
                                    FYHTS -> {
                                        ThingFyhtsObject.clealThingFyhtsObject()
                                        naverMap?.getWFSLayer(GeoserverLayerEnum.TB_THING_WTN.value, "어업권")
                                    }
                                    else -> {}
                                }
                            }
                        }
//                        when (Constants.BIZ_SUBCATEGORY_KEY) {
//                            THING -> ThingWtnObject.cleanThingWtnObject()
//                            BSN -> ThingBsnObject.cleanThingBsnObject()
//                            TOMB -> ThingTombObject.cleanThingTombObject()
//                            MINRGT -> ThingMinrgtObject.cleanThingMinrgtObject()
//                            FARM -> ThingFarmObject.cleanThingFarmObject()
//                            RETS -> ThingFyhtsObject.clealThingFyhtsObject()
//                            else SIDNT -> ThingResidntObject.cleanThingResidntObject()
//                            FYH-> {
//                            }
//                        }
                    }
        }


    }

    /**
     * 클릭이벤트 리스너
     */
    override fun onClick(v: View?) {

        cartoMapType = PreferenceUtil.getString(applicationContext, "cartoMapType","default") // 스케치 MapType

        when (cartoMapType) {

            /**
             * 1. 기본 필지 (토지)
             */
            "default" -> {
                when (v?.id) {
                    // 필지 스케치
                    R.id.floatingActionButtonUndo -> cartoMap?.setMode(SketchEnum.UNDO)
                    R.id.floatingActionButtonRedo -> cartoMap?.setMode(SketchEnum.REDO)
                    // TODO: 2022-02-23 스케치 툴바기능 추가 (점, 선)
                    R.id.floatingActionButtonPoint -> cartoMap?.setMode(SketchEnum.POINT)
                    R.id.floatingActionButtonLine -> cartoMap?.setMode(SketchEnum.LINE)
                    R.id.floatingActionButtonModify -> cartoMap?.setMode(SketchEnum.MODIFY)
                    R.id.floatingActionButtonRemove -> cartoMap?.setMode(SketchEnum.REMOVE)
                    R.id.floatingActionButtonCancel -> cartoMap?.setMode(SketchEnum.CANCEL)
                    R.id.floatingActionButtonPolygon -> { // 폴리곤 완료

                        cartoMap?.setMode(SketchEnum.POLYGON)

                        toast.msg_success(getString(R.string.msg_sketch_success), 500)
                        cartoMapView.goneView()

                        fabVisableArr.forEach { obj -> obj.goneView() }


                        layoutMapRightButtonGroup.visibleView()
                        toggleButtonLayer.visibleView()

                    }

                    // 조서 확인, 완료
                    R.id.viewSearchSaveBtn -> { //완료버튼

                        when (Constants.BIZ_SUBCATEGORY_KEY) {

                            BizEnum.LAD -> {
                                log.d("viewSearchSaveBtn Click")
                                try {
                                    val landPolygonData = LandInfoObject.realLandPolygon
                                    log.d("Land RealLandPolygonData -----------------> $landPolygonData")

                                    if (landPolygonData == null) {
                                        toast.msg_error(" 토지 조서의 현실적인 이용현황이 작성되지 않았습니다. 해당 조서의 스케치를 완료 해주시기 바람니다.", 500)
                                    } else {
                                        searchSaveLand()
                                    }

                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }
                            }

                            BizEnum.MINRGT -> { //광업
                                setWtnccTabReset()
                                val thingPolygonData = ThingMinrgtObject.thingMinrgtSketchPolygon
                                log.d("Minrgt ThingPolygonData -----------------> $thingPolygonData")

                                try {
                                    if(ThingMinrgtObject.thingNewSearch.equals("Y")) {
                                        if(ThingMinrgtObject.thingOwnerInfoJson == null || ThingMinrgtObject.thingOwnerInfoJson!!.length() == 1) {
                                            toast.msg_error("광업권 조서에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            addWtnccThingTypeSetData()
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }

                            }

                            BizEnum.FYHTS -> { //어업
                                setWtnccTabReset()
                                try {
                                    if(ThingFyhtsObject.thingNewSearch.equals("Y")) {
                                        if(ThingFyhtsObject.thingOwnerInfoJson == null || ThingFyhtsObject.thingOwnerInfoJson!!.length() == 1) {
                                            toast.msg_error("물건(어업권)에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            addWtnccThingTypeSetData()
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }

                            }

                            BizEnum.THING -> { // 지장물
                                setWtnccTabReset()
                                val thingPolygonData = ThingWtnObject.thingSketchPolygon
                                log.d("thingPolygonData ---------------------> $thingPolygonData")

                                try {
                                    if(ThingWtnObject.thingNewSearch.equals("Y")) {
                                        if(ThingWtnObject.thingOwnerInfoJson!!.length() == 1) {
                                            toast.msg_error("지장물에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            addWtnccThingTypeSetData()
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }
                            }

                            BizEnum.TOMB -> {
                                setWtnccTabReset()
                                val thingPolygonData = ThingTombObject.thingTombSketchPolyton
                                log.d("Tomb thingPolygonData -----------------------> $thingPolygonData")

                                try {
                                    if(ThingTombObject.thingNewSearch == "Y") {
                                        if(ThingTombObject.thingOwnerInfoJson == null){
                                            toast.msg_error("분묘에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            if(ThingTombObject.thingOwnerInfoJson!!.length() == 1) {
                                                toast.msg_error("분묘에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                                return
                                            } else {
                                                addWtnccThingTypeSetData()
                                            }
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }
                            }

                            BizEnum.BSN -> {
                                setWtnccTabReset()
                                val thingPolygonData = ThingBsnObject.thingBsnSketchPolygon
                                log.d("Bsn thingPolygonData ---------------> $thingPolygonData")

                                try {
                                    if(ThingBsnObject.thingNewSearch == "Y") {
                                        if(ThingBsnObject.thingOwnerInfoJson == null){
                                            toast.msg_error("물건(영업)에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            if(ThingBsnObject.thingOwnerInfoJson!!.length() == 1) {
                                                toast.msg_error("물건(영업)에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                                return
                                            } else {
                                                addWtnccThingTypeSetData()
                                            }
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }
                            }

                            BizEnum.FARM -> {
                                setWtnccTabReset()
                                try {
                                    if(ThingFarmObject.thingNewSearch == "Y") {
                                        if(ThingFarmObject.thingOwnerInfoJson == null || ThingFarmObject.thingOwnerInfoJson!!.length() == 1) {
                                            toast.msg_error("물건(농업)에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            addWtnccThingTypeSetData()
                                        }
                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.d(e.toString())
                                }
                            }

                            BizEnum.RESIDNT -> {
                                setWtnccTabReset()
                                try {
                                    if (ThingResidntObject.thingNewSearch.equals("Y")) {
                                        if(ThingResidntObject.thingOwnerInfoJson == null || ThingResidntObject.thingOwnerInfoJson!!.length() == 1) {
                                            toast.msg_error("물건(거주자)에 대한 소유자가 등록 되지 않았습니다. 다시 시도해 주시기 바람니다.", 1000)
                                            return
                                        } else {
                                            addWtnccThingTypeSetData()
                                        }

                                    } else {
                                        addWtnccThingTypeSetData()
                                    }
                                } catch (e: Exception) {
                                    log.e(e.toString())
                                }

                            }
                            BizEnum.REST_LAD -> {
                                try {
                                    searchSaveRestLand()
                                } catch (e: Exception) {
                                    log.e(e.toString())
                                }

                            }
                            BizEnum.REST_THING -> {
                                try {
                                    searchSaveRestThing()
                                } catch (e: Exception) {
                                    log.e(e.toString())
                                }
                            }

                        }
                    }

                    R.id.viewSearchConfirmBtn -> { // 취소버튼
                        log.d("viewSearchconfirmBtn Click")
                        naverMap?.apply {
                            selectCadastralPolygonArr.forEach {
                                it.map = null
                            }
                            selectCadastralPolygonArr.clear()
                        }

                        naverMap?.clearWtnncMarker()
                        naverMap?.clearCartoPolygon()

                        when(Constants.BIZ_SUBCATEGORY_KEY){
                            THING -> ThingWtnObject.cleanThingWtnObject()
                            BSN -> ThingBsnObject.cleanThingBsnObject()
                            TOMB -> ThingTombObject.cleanThingTombObject()
                            MINRGT -> ThingMinrgtObject.cleanThingMinrgtObject()
                            FARM -> ThingFarmObject.cleanThingFarmObject()
                            RESIDNT -> ThingResidntObject.cleanThingResidntObject()
                            FYHTS -> ThingFyhtsObject.clealThingFyhtsObject()
                            else -> {}
                        }

                        // 현장사진 관련 초기화
                        clearCameraValue()

                        bottomPanelClose()




//                        tombAddViewBtn.visibility = View.GONE

                    }

                    R.id.tombAddViewBtn -> {
                        toast.msg_info("임시버튼",1000)
                        settingCartoMap(null, null)
                    }
                }
            }
        }
    }

    /**
     * [ContextDialogFragment] 콜백 리스너
     * @param dialog
     * @param popupType -> ContextPopup Type으로 이벤트를 할당해준다.
     * @param dataString -> dataString
     */

    override fun onDialogPositiveClick(dialog: DialogFragment, popupType: String?, legaldongCode: String?) {

        /**
         * @description popupType 팝업 기능명 ( 하나의 리스너를 통해서 CustomPopup 전체 컨트롤 권장함.)
         * lotMapClickPopup -> 용지도
         * ladClickPopup -> 토지
         * thingClickPopup -> 지장물
         * tombClickPopup -> 분묘
         * minrgtClickPopup -> 광업
         * fyhtsClickPopup -> 어업
         * bsnClickPopup -> 영업
         * farmClickPopup -> 농업
         * residntClickPopup -> 거주자
         * thingViewClickPopup -> 지장물 필지 스케치 폴리곤 선택
         * thingBuldYes -> 지장물 건축물 여부 YES
         */

        log.d("CustomContextPopup getPosition $setContextPopupPosition")
        log.d("CustomContextPopup getPopupType $popupType")

        when (setContextPopupPosition) {

            0 -> {
                dialog.dismiss()
                when (popupType) {

                    "ladClickPopup", "thingClickPopup", "tombClickPopup" ,
                    "minrgtClickPopup", "bsnClickPopup" , "farmClickPopup",
                    "residntClickPopup", "fyhtsClickPopup", "restLadClickPopup", "restThingClickPopup" -> {
                        callerContextPopupFunc((dialog as ContextDialogFragment).address, dialog.jibun, null, setContextPopupPosition, legaldongCode)
                    }

                    "lotMapClickPopup" -> {
                        log.d("용지도 -> 토지 조서")
                        dialog.dismiss()
                        callerContextPopupFunc(
                            (dialog as ContextDialogFragment).address,
                            dialog.jibun,
                            null,
                            setContextPopupPosition,
                            legaldongCode
                        )
                    }

                    // 건축물 여부 'Y' 일 경우 실내스케치 진행
                    "thingBuldYes" -> {
                        log.d("thingViewClickPopup -> 실내스케치 선택")

                        IndoorSketchFragment().show(supportFragmentManager, "indoorSketchFragment")

                        layoutMapLeftButtonGroup.goneView()
                        layoutMapRightButtonGroup.goneView()

                        bottomPanelClose()
                    }
                }
            }

            1 -> {
                when (popupType) {

                    "ladClickPopup", "bsnClickPopup", "farmClickPopup", "minrgtClickPopup", "residntClickPopup", "tombClickPopup", "fyhtsClickPopup" -> callerContextCamera()

                    "lotMapClickPopup" -> {
                        log.d("용지도 -> 지장물 조서")
                        dialog.dismiss()

                        val thingSearchMap = HashMap<String, String>()
                        val thingSearchUrl = context.resources.getString(R.string.mobile_url) + "ThingSearch"
                        val thingLandConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                        thingSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "defaual")
                        thingSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        thingSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingLandConfirmUrl,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    progressDialog?.dismiss()
                                    log.d("fail")
                                }
                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    log.d("ThingLandConfirm response --------------> $responseString")

                                    val messageJSON =
                                        JSONObject(responseString).getJSONObject("list") as JSONObject
                                    val messageNum = messageJSON.getString("messageNum")
                                    val message = messageJSON.getString("message")

                                    progressDialog?.dismiss()
                                    if (messageNum.equals("-1")) {
                                        runOnUiThread { toast.msg_error(message.toString(), 500) }
                                    } else {
                                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingSearchMap, progressDialog, thingSearchUrl,
                                            object : Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    log.d("fail")
                                                    progressDialog?.dismiss()
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    val responseString = response.body!!.string()

                                                    log.d("thingSearch response ------------------> $responseString")
                                                    val thingDataJSON =
                                                        JSONObject(responseString).getJSONObject("list") as JSONObject
                                                    val noSkitchThingData =
                                                        thingDataJSON.getJSONArray("ThingInfo") as JSONArray

                                                    progressDialog?.dismiss()
                                                    naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                        ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, THING)
                                                }
                                            }
                                        )
                                    }

                                }

                            }
                        )

                        //naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!, dataString!!, true, BizEnum.THING)
                    }

                    "thingClickPopup" -> {
                        // 물건 조서 선택 팝업 다이얼로그
                        val view: View = layoutInflater.inflate(R.layout.thing_dialog, null)
                        val thingdialog = ThingDialogFragment(this, this, view, 2, this, (dialog as ContextDialogFragment).jibun)
                        thingdialog.isCancelable = false
                        thingdialog.show(supportFragmentManager,"Thing_Dialog")
                    }
                }
            }

            2 -> {
                when (popupType) {

                    "thingClickPopup" -> callerContextCamera()

                    "lotMapClickPopup" -> {
                        log.d("용지도 -> 영업 조서")
                        dialog.dismiss()

                        log.d("naver Map Click BSN -------------------------<><><><><>")

                        val bsnSearchMap = HashMap<String, String>()
                        val bsnSearchUrl = context.resources.getString(R.string.mobile_url) + "bsnSearch"
                        val bsnLadConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                        bsnSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "defaual")
                        bsnSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        bsnSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnLadConfirmUrl,
                            object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    log.d("fail")
                                    progressDialog?.dismiss()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    log.d("BsnLandConfirm response ------------------> $responseString")

                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                    val messageNum = messageJSON.getString("messageNum")
                                    val message = messageJSON.getString("message")

                                    progressDialog?.dismiss()

                                    if(messageNum.equals("-1")) {
                                        runOnUiThread { toast.msg_error(message.toString(), 500) }
                                    } else {
                                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(bsnSearchMap, progressDialog, bsnSearchUrl,
                                            object: Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    log.e("fail")
                                                    progressDialog?.dismiss()
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    val responseString = response.body!!.string()

                                                    log.d("thingSearch BSN response --------------------------->$responseString")

                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                    progressDialog?.dismiss()
                                                    naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                        ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, BSN)
                                                }

                                            })
                                    }


                                }

                            })
                    }

                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }

                }

            }

            3 -> {
                when (popupType) {
                    "lotMapClickPopup" -> {
                        log.d("naver Map Click FARM ---------------------- <><><><><>")

                        val farmSearchMap = HashMap<String, String>()
                        val farmSearchUrl = context.resources.getString(R.string.mobile_url) + "farmSearch"
                        val farmLadConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                        farmSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "defaual")
                        farmSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        farmSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmLadConfirmUrl,
                            object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    log.d("fail")
                                    progressDialog?.dismiss()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    log.d("FarmLandConfirm response ---------------> $responseString")

                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                    val messageNum = messageJSON.getString("messageNum")
                                    val message = messageJSON.getString("message")

                                    progressDialog?.dismiss()

                                    if(messageNum.equals("-1")) {
                                        runOnUiThread { toast.msg_error(message.toString(), 500) }
                                    } else {
                                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(farmSearchMap, progressDialog, farmSearchUrl,
                                            object: Callback {
                                                override fun onFailure(call: Call, e: IOException) {

                                                    log.e("fail")
                                                    progressDialog?.dismiss()
                                                }

                                                override fun onResponse(call: Call, response: Response) {

                                                    val responseString = response.body!!.string()

                                                    log.d("thingSearch FARM response ------------------> $responseString")

                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray?

                                                    progressDialog?.dismiss()
                                                    naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                        ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, FARM)
                                                }

                                            })
                                    }

                                }

                            })
                    }
                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }
                }
            }

            4 -> {
                when(popupType){
                    "lotMapClickPopup" -> {

                    log.d("naver Map Click RESIDNT ------------------------ <><><><><><")

                    val residntSearchMap = HashMap<String, String>()
                    val residntSearchUrl = context.resources.getString(R.string.mobile_url) + "residntSearch"
                    val residntConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                    residntSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "defaual")
                    residntSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                    residntSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                    HttpUtil.getInstance(context).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntConfirmUrl,
                        object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                log.d("fail")
                                progressDialog?.dismiss()

                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseString = response.body!!.string()

                                log.d("residntLandConfirm response ------------------> $responseString")

                                val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject
                                val messageNum = messageJSON.getString("messageNum")
                                val message = messageJSON.getString("message")

                                progressDialog?.dismiss()

                                if(messageNum.equals("-1")) {
                                    runOnUiThread { toast.msg_error(message.toString(), 500) }
                                } else {
                                    HttpUtil.getInstance(context).callerUrlInfoPostWebServer(residntSearchMap, progressDialog, residntSearchUrl,
                                        object: Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                log.e("fail")
                                                progressDialog?.dismiss()
                                            }

                                            override fun onResponse(call: Call,response: Response) {
                                                var responseString = response.body!!.string()

                                                log.d("thingSearch RESIDNT response ------------------> $responseString")

                                                val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray?

                                                progressDialog?.dismiss()
                                                naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                    ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, RESIDNT)
                                            }

                                        })
                                }

                            }

                        })
                    }
                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }
                }
            }

            5 -> {
                when(popupType) {
                    "lotMapClickPopup" -> {
                        log.d("naver Map Click TOME -----------------------------------<><><><")

                        val tombSearchMap = HashMap<String, String>()
                        val tombSearchUrl = context.resources.getString(R.string.mobile_url) + "tombSearch"
                        val tombladConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                        tombSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext,"saupCode", "defaual")
                        tombSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        tombSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()

                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
                            tombladConfirmUrl, object: Callback {
                                override fun onFailure(call: Call, e: IOException) {

                                    log.d("fail")
                                    progressDialog?.dismiss()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    log.d("TombLandConfirm response ------------------> $responseString")

                                    val messageJSON = JSONObject(responseString).getJSONObject("list") as JSONObject

                                    val messageNum = messageJSON.getString("messageNum")
                                    val message = messageJSON.getString("message")

                                    progressDialog?.dismiss()

                                    if(messageNum.equals("-1")) {
                                        runOnUiThread { toast.msg_error(message.toString(), 500) }

                                    } else {
                                        // 해당 필지 분묘 존재 확인인
                                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(tombSearchMap, progressDialog,
                                            tombSearchUrl, object: Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    log.d("fail")
                                                    progressDialog?.dismiss()
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    val responseString = response.body!!.string()

                                                    log.d("thingSearch Tomb response -------------------> $responseString")

                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                                    progressDialog?.dismiss()
                                                    naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                        ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, TOMB)

                                                }

                                            })
                                    }

                                }

                            })
                    }
                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }
                }
            }

            6 -> {
                when(popupType) {
                    "lotMapClickPopup" -> {

                        log.d("naver map click THING MNIDST")

                        val thingSearchMap = HashMap<String, String>()
                        val thingSearchUrl = context.resources.getString(R.string.mobile_url) + "MnidstSearch"
                        val thingLandConfirmUrl = context.resources.getString(R.string.mobile_url) + "LandSearchConfirm"

                        thingSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "defaual")
                        thingSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        thingSearchMap["incrprLnm"] = ThingWtnObject.naverGeoAddress.toString()


                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,
                            thingLandConfirmUrl, object: Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    progressDialog?.dismiss()
                                    log.d("fail")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseString = response.body!!.string()

                                    log.d("ThingLandConfirm response ------------------> $responseString")

                                    val messageJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                    val messageNum = messageJson.getString("messageNum")
                                    val message = messageJson.getString("message")

                                    progressDialog?.dismiss()
                                    if(messageNum.equals("-1")) {
                                        runOnUiThread { toast.msg_error(message.toString(), 500) }
                                    } else {
                                        HttpUtil.getInstance(context).callerUrlInfoPostWebServer(thingSearchMap, progressDialog,thingSearchUrl,
                                            object: Callback{
                                                override fun onFailure(call: Call, e: IOException) {
                                                    progressDialog?.dismiss()
                                                    log.d("fail")
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    val responseString = response.body!!.string()

                                                    log.d("thingSearch response ------------------> $responseString")

                                                    val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                                    val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch")

                                                    progressDialog?.dismiss()
                                                    naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                                        ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, MINRGT)
                                                }

                                            })
                                    }
                                }

                            })
                    }
                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }
                }
            }

            7 -> {
                when(popupType) {
                    "lotMapClickPopup" -> {

                        log.d("naver map click THING FSHR")

                        val fyhtsSearchUrl = context.resources.getString(R.string.mobile_url) + "fyhtsSearch"
                        val fyhtsSearchMap = HashMap<String,String>()

                        fyhtsSearchMap["saupCode"] = PreferenceUtil.getString(applicationContext, "saupCode", "")
                        fyhtsSearchMap["legaldongCl"] = ThingWtnObject.naverLegaldongCode.toString()
                        fyhtsSearchMap["legaldongCode"] = ThingFyhtsObject.legaldongCl

                        HttpUtil.getInstance(context)
                            .callerUrlInfoPostWebServer(fyhtsSearchMap, progressDialog, fyhtsSearchUrl,
                                object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        log.d("fail")
                                        progressDialog?.dismiss()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responseString = response.body!!.string()

                                        log.d("thingSearch FYHTS response --------------------------->$responseString")

                                        val thingDataJson = JSONObject(responseString).getJSONObject("list") as JSONObject
                                        val noSkitchThingData = thingDataJson.getJSONArray("ThingSearch") as JSONArray

                                        progressDialog?.dismiss()
                                        naverMap?.setNaverMapContextPopup(this@MapActivity, ThingWtnObject.naverGeoAddressName.toString(), ThingWtnObject.naverGeoAddress!!,
                                            ThingWtnObject.naverLegaldongCode.toString(), noSkitchThingData.toString(), true, FYHTS)
                                        //setNaverMapContextPopup(activity!!, naverGeoAddressName.toString(), naverGeoAddress, noSkitchThingData.toString(), false, null)
                                    }

                                })

                        /*callerContextPopupFunc((dialog as ContextDialogFragment).address, dialog.jibun, null, setContextPopupPosition)*/
                    }
                    else -> {
                        callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
                    }
                }
            }

            else -> {
                val dataArray = JSONArray((dialog as ContextDialogFragment).dataString)
                log.d(dataArray.toString())
                callerContextThing(setContextPopupPosition, setcontextPopupName, (dialog as ContextDialogFragment).dataString)
            }


        }

        setContextPopupPosition = 0

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onSingleChoiceItems(dialog: DialogFragment, position: Int) {

        setContextPopupPosition = position
        setcontextPopupName = (dialog as ContextDialogFragment).array[position]

        try {
            choiceInfoWindowArr.forEach { it.map = null }
            choiceInfoWindowArr.clear()

            ThingWtnObject.thingWtnncJsonArray?.forEach {

                val kndName = it.asJsonObject.get("properties").asJsonObject.get("THING_KND").asString
                val moNo = it.asJsonObject.get("properties").asJsonObject.get("MO_NO").asString

                if (kndName.equals(setcontextPopupName)) {
                    log.d("moNo => $moNo")

                    val coordArr = it.asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray.get(0).asJsonArray.get(0).asJsonArray

                    for (j in 0 until coordArr.size() - 1) {

                        log.d("coord => ${coordArr[j]}")

                        val coord = coordArr[j].toString().replace("[", "").replace("]", "").split(",")

                        val x = coord[1].toDouble()
                        val y = coord[0].toDouble()

                        log.d("x=$x, y=$y");

                        val infoWindow = InfoWindow()
                        val infoView: InfoView?
                        infoView = InfoView(context, null, R.layout.include_wtncc_info_choice_view)
                        infoView.setText(moNo, "wtncc")

                        infoWindow.adapter = object : InfoWindow.ViewAdapter() {
                            override fun getView(p0: InfoWindow): View = infoView
                        }
                        infoWindow.position = LatLng(x, y)
                        infoWindow.offsetX = 0
                        infoWindow.offsetY = 0

                        //infoWindow.map = this.naverMap?.naverMap

                        choiceInfoWindowArr.add(infoWindow)

                    }
                } else {

                }
            }

            choiceInfoWindowArr.forEach {
                it.map = this.naverMap?.naverMap
            }


        } catch (e: Exception) {
            log.d(e.toString())
            log.d("onSingleChoice Itms 내 폴리곤 데이터가 없는 메뉴 선택")
        }

    }


    /**
     * 뒤로가기
     */
    override fun onBackPressed() {
        if (backPressedListener != null) {
            backPressedListener!!.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

}