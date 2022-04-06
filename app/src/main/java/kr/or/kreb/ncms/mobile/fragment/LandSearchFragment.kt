/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_land_search.*
import kotlinx.android.synthetic.main.fragment_land_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.LandSearchRealngrAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class LandSearchFragment(val activity: Activity?, context: Context?) : BaseFragment(),
    AdapterView.OnItemSelectedListener {

    private val mContext: Context? = context
//    private var logUtil: LogUtil = LogUtil("LandSearchFragment")
    private val wtnncUtill = WtnncUtil(activity!!, context!!)
    private var responseString: String? = null

//    var dialogUtil: DialogUtil? = null
//    private var progressDialog: AlertDialog? = null

//    private var toastUtil: ToastUtil = ToastUtil(context)

    var imageArr = mutableListOf<WtnncImage>()
    var wtnncImageAdapter: WtnncImageAdapter? = null

    init { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_land_search, null)
    }

    fun settingSearchRealView() {
        logUtil.d("settingSearchRealView--!!!!!!!")

        if(LandInfoObject.landSearchRealLngrJsonArray!!.length() > 1) {
            LandInfoObject.realLngrty = "Y"
        } else {
            LandInfoObject.realLngrty = "N"
        }


        activity?.runOnUiThread {
            //landSearchRealngrAdapter = LandSearchRealngrAdapter(landSearchRealLandJson!!, clicklistener, this, mContext)
            LandInfoObject.landSearchRealLngrAdpater = LandSearchRealngrAdapter(LandInfoObject.landSearchRealLngrJsonArray!!, mContext, activity)
            landSearchRealUse.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            landSearchRealUse.adapter = LandInfoObject.landSearchRealLngrAdpater
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

//        getData()
        initUi(view)

        //토지조서 사진촬영버튼
        includeCameraBtn.setOnClickListener {
//            nextView(
//                requireActivity(),
//                Constants.CAMERA_ACT,
//                null,
//                CameraEnum.DEFAULT
//            )
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.LAD,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

        includePaclrMatterEdit.setOnEditorActionListener { textView, action, _ ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                logUtil.d("확인버튼 이벤트 처리")
                val txtString = textView.text.toString()
                LandInfoObject.paclrMatter = txtString
            }
            false
        }

        landSearchaNrfrstAtLl.setOnClickListener {
            logUtil.d("자연림 여부 레이어 선택")
            if (landSearchaNrfrstAtChk.isChecked) {
                landSearchaNrfrstAtChk.isChecked = false
                LandInfoObject.nrfrstAtChk = "N"
            } else {
                landSearchaNrfrstAtChk.isChecked = true
                LandInfoObject.nrfrstAtChk = "Y"
            }
        }

        landSearchaNrfrstAtChk!!.setOnClickListener {
            logUtil.d("자연림 여부 선택")
            if (landSearchaNrfrstAtChk.isChecked) {
                LandInfoObject.nrfrstAtChk = "N"
            } else {
                LandInfoObject.nrfrstAtChk = "Y"
            }
        }

        landSearchClvtAtLl.setOnClickListener {
            logUtil.d("경작 여부 레이아웃 선택")
            if (landSearchClvtAtChk.isChecked) {
                landSearchClvtAtChk.isChecked = false
                LandInfoObject.clvtAtChk = "N"
            } else {
                landSearchClvtAtChk.isChecked = true
                LandInfoObject.clvtAtChk = "Y"
            }
        }

        landSearchClvtAtChk!!.setOnClickListener {
            logUtil.d("경작 여부 선택")
            if (landSearchClvtAtChk.isChecked) {
                LandInfoObject.clvtAtChk = "N"
            } else {
                LandInfoObject.clvtAtChk = "Y"
            }
        }

        landSearchBuildAtLl.setOnClickListener {
            logUtil.d("건축물 여부 레이아웃 선택")
            if (landSearchBuildAtChk.isChecked) {
                landSearchBuildAtChk.isChecked = false
                LandInfoObject.buildAtChk = "N"
            } else {
                landSearchBuildAtChk.isChecked = true
                LandInfoObject.buildAtChk = "Y"
            }
        }

        landSearchBuildAtChk!!.setOnClickListener {
            logUtil.d("건축물 여부 선택")
            if (landSearchBuildAtChk.isChecked) {
                LandInfoObject.buildAtChk = "N"
            } else {
                LandInfoObject.buildAtChk = "Y"
            }
        }

        landSearchPlotAtLl.setOnClickListener {
            logUtil.d("대지권 여부 레이아웃 선택")
            if (landSearchPlotAtChk.isChecked) {
                landSearchPlotAtChk.isChecked = false
                LandInfoObject.plotAtChk = "N"
            } else {
                landSearchPlotAtChk.isChecked = true
                LandInfoObject.plotAtChk = "Y"
            }
        }

        landSearchPlotAtChk!!.setOnClickListener {
            logUtil.d("대지권 여부 선택")
            if (landSearchPlotAtChk.isChecked) {
                LandInfoObject.plotAtChk = "N"
            } else {
                LandInfoObject.plotAtChk = "Y"
            }
        }

        landSearchSttusMesrAtLl.setOnClickListener {
            logUtil.d("측량요청 여부 레이아웃 선택")
            if (landSearchSttusMesrAtChk.isChecked) {
                landSearchSttusMesrAtChk.isChecked = false
                LandInfoObject.sttusMesrAtChk = "N"
            } else {
                landSearchSttusMesrAtChk.isChecked = true
                LandInfoObject.sttusMesrAtChk = "Y"
            }
        }

        landSearchSttusMesrAtChk!!.setOnClickListener {
            logUtil.d("측량요청 여부 선택")
            if (landSearchSttusMesrAtChk.isChecked) {
                LandInfoObject.sttusMesrAtChk = "N"
            } else {
                LandInfoObject.sttusMesrAtChk = "Y"
            }
        }


    }

    fun initUi(view: View) {

        //view.forEachChildView {  it.isEnabled = false  } // 뷰그룹 내 자식 forEach disabled

        logUtil.d("init response String --" + responseString)

        val dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String

        logUtil.d("LandInfo String ---------------------> $dataString")

        val dataJson = JSONObject(dataString)

        logUtil.d("LandInfo json --------------------> $dataJson")

        wtnncUtill.wtnncSpinnerAdapter(R.array.spclLadClArray, landSearchSpclLadClAtChk, this) // 소분류
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownercnfirmbasisArray, landSearchOwnerCnfirmBasisClChk, this) // 소분류

        val landInfoDataJson = dataJson.getJSONObject("LandInfo")

        //토지 정보

        val landNo = checkStringNull(landInfoDataJson.getString("no"))
        val landSubNo = checkStringNull(landInfoDataJson.getString("subNo"))

        view.landSearchNoText?.text = "$landNo-$landSubNo"
        view.landSearchLocationText?.text = checkStringNull(landInfoDataJson.getString("legaldongNm"))
        view.landSearchBgnnLnmText?.text = checkStringNull(landInfoDataJson.getString("bgnnLnm"))
        view.landSearchincrprLnmText?.text = checkStringNull(landInfoDataJson.getString("incrprLnm"))
        view.landSearchNominationText?.text = checkStringNull(landInfoDataJson.getString("gobuLndcgrNm"))
        view.landSearchRelatedLnmText?.setText(checkStringNull(landInfoDataJson.getString("relateLnm")))
        view.landSearchBgnnArText?.text = checkStringNull(landInfoDataJson.getString("bgnnAr"))
        view.landSearchIncrprArText?.text = checkStringNull(landInfoDataJson.getString("incrprAr"))
        view.includePaclrMatterEdit?.setText(checkStringNull(landInfoDataJson.getString("paclrMatter")))
        view.includeReferMatterEdit?.setText(checkStringNull(landInfoDataJson.getString("referMatter")))
        view.includeRmEdit?.setText(checkStringNull(landInfoDataJson.getString("rm")))

        //자연림여부
        val nrfrstAtString = checkStringNull(landInfoDataJson.getString("nrfrstAt"))
        view.landSearchaNrfrstAtChk.isChecked = nrfrstAtString == "Y"

        //경작여부
        val clvtAtString = checkStringNull(landInfoDataJson.getString("clvtAt"))
        view.landSearchClvtAtChk.isChecked = clvtAtString == "Y"

        //건축물여부
        val buildAtString = checkStringNull(landInfoDataJson.getString("buldAt"))
        view.landSearchBuildAtChk.isChecked = buildAtString == "Y"

        //대지권여부
        val plotAtString = checkStringNull(landInfoDataJson.getString("plotAt"))
        view.landSearchPlotAtChk.isChecked = plotAtString == "Y"

//        if(plotAtString == "Y"){

//            TabLayoutMediator(Constants.GLOBAL_TAB_LAYOUT!!, Constants.GLOBAL_VIEW_PAGER!!) { tab, position ->
//                when (position) {
//                    0 -> tab.text = "토지내역"
//                    1 -> tab.text = "토지조서"
//                }
//            }.attach()
//
//            ToastUtil(context).msg_error("대지권 여부가 'Y'일 경우에는 소유자 및 관계자를 확인 할 수 없습니다.", 500)
//        }

        //보상대상여부
        val rwTrgetAtString = checkStringNull(landInfoDataJson.getString("rwTrgetAt"))
        view.landSearchRwTrgetAtChk.isChecked = rwTrgetAtString == "Y"

        //측량요청청
        val sttusMesrAtString = checkStringNull(landInfoDataJson.getString("sttusMesrAt"))
        view.landSearchSttusMesrAtChk.isChecked = sttusMesrAtString == "Y"

        //분할대상여부
        val partitnTrgetAtString = checkStringNull(landInfoDataJson.getString("partitnTrgetAt"))
        view.landSearchPartitnTrgetAtChk.isChecked = partitnTrgetAtString == "Y"

        //특수용지
        val spclLadClString = checkStringNull(landInfoDataJson.getString("spclLadCl"))
        if(spclLadClString == "") {
            view.landSearchSpclLadClAtChk.setSelection(0)
        } else {
            val spclLadClStringSub = spclLadClString.substring(5,7)
            view.landSearchSpclLadClAtChk.setSelection(Integer.valueOf(spclLadClStringSub))
        }

        view.landSearchSpclLadCnEditText.setText(checkStringNull(landInfoDataJson.getString("spclLadCn")))

        val ownerCnfirmBasisClString = checkStringNull(landInfoDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmBasisClString.equals("")) {
            view.landSearchOwnerCnfirmBasisClChk.setSelection(0)
        } else {
            val ownerCnfirmBasisClStringSub = ownerCnfirmBasisClString.substring(5,7)
            view.landSearchOwnerCnfirmBasisClChk.setSelection(Integer.valueOf(ownerCnfirmBasisClStringSub))
        }


        LandInfoObject.landSearchRealLngrJsonArray =
            dataJson.getJSONArray("realLandInfo") as JSONArray

        LandInfoObject.searchRealLand = LandInfoObject.landSearchRealLngrJsonArray

        if (LandInfoObject.landSearchRealLngrJsonArray!!.getJSONObject(0)
                .getString("realLndcgrNm").equals("NoData")
        ) {
            logUtil.d("real land json no data")
        }

        settingSearchRealView()

        settingSearchCamerasView(dataJson.getJSONArray("landAtchInfo"))


//        landSearchaNrfrstAtChk = view.landSearchaNrfrstAtChk
//        landSearchClvtAtChk = view.landSearchClvtAtChk
//        landSearchBuildAtChk = view.landSearchBuildAtChk
//        landSearchPlotAtChk = view.landSearchPlotAtChk

//        // 카메라 어댑터 세팅
//        for (i in 0..4) {
//            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(i, null, "","","","",""))
//        }
//
//        Constants.CAMERA_ADAPTER = WtnncImageAdapter(requireContext(), Constants.CAMERA_IMAGE_ARR)
//        val layoutManager = LinearLayoutManager(requireContext())
//        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
//        includeImageViewRv.also {
//            it.layoutManager = layoutManager
//            it.adapter = Constants.CAMERA_ADAPTER
//        }
    }

    /**
     * 토지 이용현황추가
     */
    fun landRealAdd() {
        logUtil.d("토지실제이용 추가!!!!")

        val lndData = LandInfoObject.landInfo as JSONObject


        val realLndcgr = JSONObject()
        realLndcgr.put("realLndcgrAr", LandInfoObject.currentArea)
        realLndcgr.put("realLndcgrCl", "")
        realLndcgr.put("realLndcgrCn", "")
        realLndcgr.put("realLndcgrCode","")
        realLndcgr.put("saupCode",lndData.getString("saupCode"))
        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))

        //val jArrLength = LandInfoObject.landSearchRealLngrJsonArray?.length()!!
        //logUtil.d(jArrLength.toString())

        LandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
        LandInfoObject.searchRealLand = LandInfoObject.landSearchRealLngrJsonArray
        LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
    }

    /**
     * 토지 이용현황추가 (전체)
     */
    fun landRealAddAll() {
        logUtil.d("토지실제이용 전체 필지 추가!!!!")

        val lndData = LandInfoObject.landInfo as JSONObject

        val realLndcgr = JSONObject()
        realLndcgr.put("realLndcgrAr", LandInfoObject.currentArea)
        realLndcgr.put("realLndcgrCl", "")
        realLndcgr.put("realLndcgrCn", "")
        realLndcgr.put("realLndcgrCode","")
        realLndcgr.put("saupCode",lndData.getString("saupCode"))
        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))


        //val jArrLength = LandInfoObject.landSearchRealLngrJsonArray?.length()!!
        //logUtil.d(jArrLength.toString())

        LandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
        LandInfoObject.searchRealLand = LandInfoObject.landSearchRealLngrJsonArray
        LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
    }

    /**
     * 토지 이용현황 면적 수정
     */
    fun landRealUpdate() {
        logUtil.d("토지실제이용 수정!!!!")

        val jArray = LandInfoObject.landSearchRealLngrJsonArray

        for (i in 0 until jArray?.length()!!) {
            logUtil.d(jArray.get(i).toString())

            logUtil.d("${LandInfoObject.currentArea.toString()}, ${(jArray.get(i) as JSONObject).get("realLndcgrAr")}")
            logUtil.d("${LandInfoObject.selectPolygonCurrentArea.toString()}, ${(jArray.get(i) as JSONObject).get("realLndcgrAr")}")

            when {
                jArray.length() == 2 -> { (LandInfoObject.landSearchRealLngrJsonArray?.get(1) as JSONObject).put("realLndcgrAr", LandInfoObject.currentArea) }
                else -> {
                    if (LandInfoObject.selectPolygonCenterTxt == (LandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).get("realLndcgrAr").toString()) {
                        logUtil.d("data 일치")
                        (LandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).put(
                            "realLndcgrAr",
                            LandInfoObject.currentArea
                        )
                    }
                }
            }


            // loop문 속에서 선택한 면적과 일치하였을 경우에 실행
//            if(LandInfoObject.selectPolygonCurrentArea.toString() == (LandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).get("realLndcgrAr").toString()){
//                logUtil.d("data 일치")
//                (LandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).put("realLndcgrAr", LandInfoObject.currentArea)
//            }
        }

        LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
    }

    fun settingSearchCamerasView(dataArray: JSONArray) {

        val searchImageArray = JSONArray()

        for(i in 0 until dataArray.length()) {
            val dataItem = dataArray.getJSONObject(i)

            if(dataItem.getString("fileseInfo").equals("A200006012")) {
                searchImageArray.put(dataItem)
            }
        }

        clearCameraValue()

        IntRange(0, 4).map {
            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(it, null, "","","","","","","","",""))
        }

        Constants.CAMERA_ADAPTER = WtnncImageAdapter(requireContext(), Constants.CAMERA_IMAGE_ARR)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        includeImageViewRv.also {
            it.layoutManager = layoutManager
            it.adapter = Constants.CAMERA_ADAPTER
        }

        logUtil.d("searchImageArray length --------------------------------> ${searchImageArray.length()}")

        val landAtchFileMapArr = ArrayList<HashMap<String, String>>()
        var item = JSONObject()

        for(i in 0 until searchImageArray.length()) {
            item = searchImageArray.getJSONObject(i)
            val landAtchFileMap = HashMap<String, String>()
            landAtchFileMap["atchCode"] = item.getString("atchCode")
            landAtchFileMap["ladWtnCode"] = item.getString("ladWtnCode")
            landAtchFileMap["saupCode"] = item.getString("saupCode")
            landAtchFileMap["atfl"] = item.getString("atfl")
            landAtchFileMap["atflNm"] = item.getString( "atflNm")
            landAtchFileMap["fileseInfo"] = item.getString("fileseInfo")
            landAtchFileMap["fileseInfoNm"] = item.getString("fileseInfoNm")
            landAtchFileMap["atflSize"] = item.getString("atflSize")
            landAtchFileMap["la"] = item.getString("la")
            landAtchFileMap["lo"] = item.getString("lo")
            landAtchFileMap["drc"] = item.getString("drc")

            landAtchFileMapArr.add(landAtchFileMap)
        }

        val landAtchFileUrl = context!!.resources.getString(R.string.mobile_url) + "landFileDownload"

           landAtchFileMapArr.forEachIndexed { idx, it ->

               val getLon = it["lo"]
               val getLat = it["la"]
               val getDirection = it["drc"]

               logUtil.d("$getLon, $getLat, $getDirection")

               (context as MapActivity).naverMap?.setWtnncPicMarkers(getLat?.toDouble(),getLon?.toDouble(), getDirection?.toFloat())

               HttpUtil.getInstance(context!!)
                   .callerUrlInfoPostWebServer(it, progressDialog, landAtchFileUrl,
                       object: Callback {
                           override fun onFailure(call: Call, e: IOException) {
                               progressDialog?.dismiss()
                               toast.msg_error(R.string.msg_server_connected_fail, 100)
                           }

                           override fun onResponse(call: Call, response: Response) {

                               val downLoadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                               val downloadFile = File("$downLoadDirectory/${item.getString("atfl")}")

                               FileUtil.createDir(downLoadDirectory)

                               val wtnncPicBitmap = BitmapFactory.decodeStream(response.body?.byteStream())

                               CoroutineScope(Dispatchers.Main).launch {

                                   val wtnncImage = WtnncImage(
                                       idx,
                                       wtnncPicBitmap,
                                       item.getString("saupCode"),
                                       "LAD",
                                       item.getString("rm"),
                                       downloadFile.name.toString(),
                                       item.getString("fileseInfo"),
                                       item.getString("fileseInfoNm"),
                                       item.getString("la"),
                                       item.getString("lo"),
                                       item.getString("drc")
                                   )

                                   Constants.CAMERA_IMGAE_INDEX++

                                   if (Constants.CAMERA_IMGAE_INDEX > 5) {
                                       Constants.CAMERA_ADAPTER?.addItem(wtnncImage, Constants.BIZ_SUBCATEGORY_KEY.toString())
                                   } else {
                                       Constants.CAMERA_ADAPTER?.updateItem(wtnncImage, Constants.CAMERA_IMGAE_INDEX, Constants.BIZ_SUBCATEGORY_KEY.toString())
                                   }

                                   progressDialog?.dismiss()

                               }
                           }

                       })

            }

    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id) {
            R.id.landSearchSpclLadClAtChk -> {
                when (position) {
                    0 -> LandInfoObject.spclLadCl = ""
                    1 -> LandInfoObject.spclLadCl = "A026001"
                    2 -> LandInfoObject.spclLadCl = "A026002"
                    3 -> LandInfoObject.spclLadCl = "A026003"
                    4 -> LandInfoObject.spclLadCl = "A026004"
                    5 -> LandInfoObject.spclLadCl = "A026005"
                    6 -> LandInfoObject.spclLadCl = "A026006"
                    7 -> LandInfoObject.spclLadCl = "A026007"
                    8 -> LandInfoObject.spclLadCl = "A026008"
                    9 -> LandInfoObject.spclLadCl = "A026009"
                    10 -> LandInfoObject.spclLadCl = "A026010"
                    11 -> LandInfoObject.spclLadCl = "A026011"
                    12 -> LandInfoObject.spclLadCl = "A026012"
                    13 -> LandInfoObject.spclLadCl = "A026013"
                }
            }

            R.id.landSearchOwnerCnfirmBasisClChk -> {
                when(position) {
                    0 -> LandInfoObject.ownerCnfirmBasisCl = ""
                    1 -> LandInfoObject.ownerCnfirmBasisCl = "A035001"
                    2 -> LandInfoObject.ownerCnfirmBasisCl = "A035002"
                    3 -> LandInfoObject.ownerCnfirmBasisCl = "A035003"
                    4 -> LandInfoObject.ownerCnfirmBasisCl = "A035004"
                    5 -> LandInfoObject.ownerCnfirmBasisCl = "A035005"
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addLandData() {

        if(Constants.GLOBAL_TAB_LAYOUT?.selectedTabPosition != 0){

            Constants.GLOBAL_TAB_LAYOUT?.setScrollPosition(0,0f,true)
            Constants.GLOBAL_VIEW_PAGER?.currentItem = 0
            logUtil.d(Constants.GLOBAL_TAB_LAYOUT?.selectedTabPosition.toString())

            // 관련지번
            val landRelateLnmString = activity!!.landSearchRelatedLnmText.text.toString()
            if(!getString(R.string.landInfoRelatedLnmText).equals(landRelateLnmString)) {
                LandInfoObject.relateLnm = landRelateLnmString
            }

            //자연림여부
            LandInfoObject.nrfrstAtChk = when(activity!!.landSearchaNrfrstAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //경작여부
            LandInfoObject.clvtAtChk = when(activity.landSearchClvtAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //건축물여부
            LandInfoObject.buildAtChk = when(activity.landSearchBuildAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //대지권여부
            LandInfoObject.plotAtChk = when(activity.landSearchPlotAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //보상대상여부
            LandInfoObject.rwTrgetAt = when(activity.landSearchRwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //분할대상여부
            LandInfoObject.partitnTrgetAt = when(activity.landSearchPartitnTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //측량요청
            LandInfoObject.sttusMesrAtChk = when(activity.landSearchSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //특수용지
            LandInfoObject.spclLadCl = when(activity.landSearchSpclLadClAtChk.selectedItemPosition) {
                1 -> "A026001"
                2 -> "A026002"
                3 -> "A026003"
                4 -> "A026004"
                5 -> "A026005"
                6 -> "A026006"
                7 -> "A026007"
                8 -> "A026008"
                9 -> "A026009"
                10 -> "A026010"
                11 -> "A026011"
                12 -> "A026012"
                13 -> "A026013"
                else -> ""
            }
            //특수용지내용
            LandInfoObject.spclLadCn = activity.landSearchSpclLadCnEditText.text.toString()
            //소우자확인근거
            LandInfoObject.ownerCnfirmBasisCl = when(activity.landSearchOwnerCnfirmBasisClChk.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }
            //특이사항
            LandInfoObject.paclrMatter = activity.includePaclrMatterEdit.text.toString()
            //참고사항
            LandInfoObject.referMatter = activity.includeReferMatterEdit.text.toString()
            //비고
            LandInfoObject.rm = activity.includeRmEdit.text.toString()



        }
    }

    override fun showOwnerPopup() {
//        TODO("Not yet implemented")
    }
}
