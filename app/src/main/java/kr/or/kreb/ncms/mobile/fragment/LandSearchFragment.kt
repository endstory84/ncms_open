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
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_land_search.*
import kotlinx.android.synthetic.main.fragment_land_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
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

    var dcsnAt: String? = "N"

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
            LandInfoObject.landSearchRealLngrAdpater = LandSearchRealngrAdapter(LandInfoObject.landSearchRealLngrJsonArray!!, mContext, activity, dcsnAt)
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

        //???????????? ??????????????????
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
                "????????????",
                CameraEnum.DEFAULT
            )
        }

        includePaclrMatterEdit.setOnEditorActionListener { textView, action, _ ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                logUtil.d("???????????? ????????? ??????")
                val txtString = textView.text.toString()
                LandInfoObject.paclrMatter = txtString
            }
            false
        }

        landSearchaNrfrstAtLl.setOnClickListener {
            logUtil.d("????????? ?????? ????????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchaNrfrstAtChk.isEnabled = false
            } else {
                if (landSearchaNrfrstAtChk.isChecked) {
                    landSearchaNrfrstAtChk.isChecked = false
                    LandInfoObject.nrfrstAtChk = "N"
                } else {
                    landSearchaNrfrstAtChk.isChecked = true
                    LandInfoObject.nrfrstAtChk = "Y"
                }
            }
        }

        landSearchaNrfrstAtChk!!.setOnClickListener {
            logUtil.d("????????? ?????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchaNrfrstAtChk.isEnabled = false
            } else {
                if (landSearchaNrfrstAtChk.isChecked) {
                    landSearchaNrfrstAtChk.isChecked = false
                    LandInfoObject.nrfrstAtChk = "N"
                } else {
                    landSearchaNrfrstAtChk.isChecked = true
                    LandInfoObject.nrfrstAtChk = "Y"
                }
            }
        }

        landSearchClvtAtLl.setOnClickListener {
            logUtil.d("?????? ?????? ???????????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchClvtAtChk.isEnabled = false
            } else {
                if (landSearchClvtAtChk.isChecked) {
                    landSearchClvtAtChk.isChecked = false
                    LandInfoObject.clvtAtChk = "N"
                } else {
                    landSearchClvtAtChk.isChecked = true
                    LandInfoObject.clvtAtChk = "Y"
                }
            }
        }

        landSearchClvtAtChk!!.setOnClickListener {
            logUtil.d("?????? ?????? ??????")
            if (landSearchClvtAtChk.isChecked) {
                LandInfoObject.clvtAtChk = "N"
            } else {
                LandInfoObject.clvtAtChk = "Y"
            }
        }

        landSearchBuildAtLl.setOnClickListener {
            logUtil.d("????????? ?????? ???????????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchBuildAtChk.isEnabled = false
            } else {
                if (landSearchBuildAtChk.isChecked) {
                    landSearchBuildAtChk.isChecked = false
                    LandInfoObject.buildAtChk = "N"
                } else {
                    landSearchBuildAtChk.isChecked = true
                    LandInfoObject.buildAtChk = "Y"
                }
            }
        }

        landSearchBuildAtChk!!.setOnClickListener {
            logUtil.d("????????? ?????? ??????")
            if (landSearchBuildAtChk.isChecked) {
                LandInfoObject.buildAtChk = "N"
            } else {
                LandInfoObject.buildAtChk = "Y"
            }
        }

        landSearchPlotAtLl.setOnClickListener {
            logUtil.d("????????? ?????? ???????????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchPlotAtChk.isEnabled = false
            } else {
                if (landSearchPlotAtChk.isChecked) {
                    landSearchPlotAtChk.isChecked = false
                    LandInfoObject.plotAtChk = "N"
                } else {
                    landSearchPlotAtChk.isChecked = true
                    LandInfoObject.plotAtChk = "Y"
                }
            }
        }

        landSearchPlotAtChk!!.setOnClickListener {
            logUtil.d("????????? ?????? ??????")
            if (landSearchPlotAtChk.isChecked) {
                LandInfoObject.plotAtChk = "N"
            } else {
                LandInfoObject.plotAtChk = "Y"
            }
        }

        landSearchSttusMesrAtLl.setOnClickListener {
            logUtil.d("???????????? ?????? ???????????? ??????")
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                landSearchSttusMesrAtChk.isEnabled = false
            } else {
                if (landSearchSttusMesrAtChk.isChecked) {
                    landSearchSttusMesrAtChk.isChecked = false
                    LandInfoObject.sttusMesrAtChk = "N"
                } else {
                    landSearchSttusMesrAtChk.isChecked = true
                    LandInfoObject.sttusMesrAtChk = "Y"
                }
            }
        }

        landSearchSttusMesrAtChk!!.setOnClickListener {
            logUtil.d("???????????? ?????? ??????")
            if (landSearchSttusMesrAtChk.isChecked) {
                LandInfoObject.sttusMesrAtChk = "N"
            } else {
                LandInfoObject.sttusMesrAtChk = "Y"
            }
        }


    }

    fun initUi(view: View) {

        //view.forEachChildView {  it.isEnabled = false  } // ????????? ??? ?????? forEach disabled

        logUtil.d("init response String --" + responseString)

        val dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String

        logUtil.d("LandInfo String ---------------------> $dataString")

        val dataJson = JSONObject(dataString)

        logUtil.d("LandInfo json --------------------> $dataJson")

        wtnncUtill.wtnncSpinnerAdapter(R.array.spclLadClArray, landSearchSpclLadClAtChk, this) // ?????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownercnfirmbasisArray, landSearchOwnerCnfirmBasisClChk, this) // ?????????

        val landInfoDataJson = dataJson.getJSONObject("LandInfo")

        //?????? ??????

        dcsnAt = checkStringNull(landInfoDataJson.getString("dcsnAt"))

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

        //???????????????
        val nrfrstAtString = checkStringNull(landInfoDataJson.getString("nrfrstAt"))
        view.landSearchaNrfrstAtChk.isChecked = nrfrstAtString == "Y"

        //????????????
        val clvtAtString = checkStringNull(landInfoDataJson.getString("clvtAt"))
        view.landSearchClvtAtChk.isChecked = clvtAtString == "Y"

        //???????????????
        val buildAtString = checkStringNull(landInfoDataJson.getString("buldAt"))
        view.landSearchBuildAtChk.isChecked = buildAtString == "Y"

        //???????????????
        val plotAtString = checkStringNull(landInfoDataJson.getString("plotAt"))
        view.landSearchPlotAtChk.isChecked = plotAtString == "Y"

//        if(plotAtString == "Y"){

//            TabLayoutMediator(Constants.GLOBAL_TAB_LAYOUT!!, Constants.GLOBAL_VIEW_PAGER!!) { tab, position ->
//                when (position) {
//                    0 -> tab.text = "????????????"
//                    1 -> tab.text = "????????????"
//                }
//            }.attach()
//
//            ToastUtil(context).msg_error("????????? ????????? 'Y'??? ???????????? ????????? ??? ???????????? ?????? ??? ??? ????????????.", 500)
//        }

        //??????????????????
        val rwTrgetAtString = checkStringNull(landInfoDataJson.getString("rwTrgetAt"))
        view.landSearchRwTrgetAtChk.isChecked = rwTrgetAtString == "Y"

        //???????????????
        val sttusMesrAtString = checkStringNull(landInfoDataJson.getString("sttusMesrAt"))
        view.landSearchSttusMesrAtChk.isChecked = sttusMesrAtString == "Y"

        //??????????????????
        val partitnTrgetAtString = checkStringNull(landInfoDataJson.getString("partitnTrgetAt"))
        view.landSearchPartitnTrgetAtChk.isChecked = partitnTrgetAtString == "Y"

        //????????????
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

        if(dcsnAt == "Y") {
            view.landSearchaNrfrstAtChk.isEnabled = false
            view.landSearchClvtAtChk.isEnabled = false
            view.landSearchBuildAtChk.isEnabled = false
            view.landSearchPlotAtChk.isEnabled = false
            view.landSearchRwTrgetAtChk.isEnabled = false
            view.landSearchSttusMesrAtChk.isEnabled = false
            view.landSearchPartitnTrgetAtChk.isEnabled = false
            view.landSearchSpclLadClAtChk.isEnabled = false
            view.landSearchOwnerCnfirmBasisClChk.isEnabled = false
            view.landSearchSpclLadCnEditText.isEnabled = false
//            view.includePaclrMatterEdit.isEnabled = false
//            view.includeReferMatterEdit.isEnabled = false
//            view.includeRmEdit.isEnabled = false
            view.landSearchRelatedLnmText.isEnabled = false
        }

        settingSearchRealView()

        settingSearchCamerasView(dataJson.getJSONArray("landAtchInfo"))

    }

    /**
     * ?????? ??????????????????
     */
    fun landRealAdd(getPolygonArray: MutableList<ArrayList<LatLng>>) {
        logUtil.d("?????? ???????????? ??????")

        val lndData = LandInfoObject.landInfo as JSONObject


        val realLndcgr = JSONObject()
        realLndcgr.put("realLndcgrAr", LandInfoObject.currentArea)
        realLndcgr.put("realLndcgrCl", "")
        realLndcgr.put("realLndcgrCn", "")
        realLndcgr.put("realLndcgrCode","")
        realLndcgr.put("saupCode",lndData.getString("saupCode"))
        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))

        val arr = mutableListOf<String>()
        getPolygonArray[0].forEach {

            val x = it.longitude
            val y = it.latitude

            val coordX = convertEPSG3857(y, x).x
            val coordY = convertEPSG3857(y, x).y

            arr.add("$coordX $coordY")
        }

        val convertCoord = arr.toString().replace("[", "").replace("]", "")
        val resultGeoms = "MULTIPOLYGON ((($convertCoord)))"

        realLndcgr.put("geoms",resultGeoms)

        LandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
        LandInfoObject.searchRealLand = LandInfoObject.landSearchRealLngrJsonArray
        LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
    }

    /**
     * ?????? ?????????????????? (??????)
     */
    fun landRealAddAll() {
        logUtil.d("?????????????????? ?????? ?????? ??????!!!!")

        val lndData = LandInfoObject.landInfo as JSONObject

        val realLndcgr = JSONObject()
        realLndcgr.put("realLndcgrAr", LandInfoObject.currentArea)
        realLndcgr.put("realLndcgrCl", "")
        realLndcgr.put("realLndcgrCn", "")
        realLndcgr.put("realLndcgrCode","")
        realLndcgr.put("saupCode",lndData.getString("saupCode"))
        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))

        LandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
        LandInfoObject.searchRealLand = LandInfoObject.landSearchRealLngrJsonArray
        LandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
    }

    /**
     * ?????? ???????????? ?????? ??????
     */
    fun landRealUpdate() {

        logUtil.d("?????????????????? ?????? ??????")

        val targetObj =  (LandInfoObject.landSearchRealLngrJsonArray?.get(LandInfoObject.landRealArCurPos) as JSONObject)
        val getRealLandAr = targetObj.get("realLndcgrAr")

        logUtil.d("getRealLandAr => $getRealLandAr")

        targetObj.put("realLndcgrAr", LandInfoObject.currentArea)
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
                LandInfoObject.spclLadCl = when (position) {
                    0 -> ""
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
                    else -> null
                }
            }

            R.id.landSearchOwnerCnfirmBasisClChk -> {
                LandInfoObject.ownerCnfirmBasisCl  = when(position) {
                    0 -> ""
                    1 -> "A035001"
                    2 -> "A035002"
                    3 -> "A035003"
                    4 -> "A035004"
                    5 -> "A035005"
                    else -> null
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

            // ????????????
            val landRelateLnmString = activity!!.landSearchRelatedLnmText.text.toString()
//            if(!getString(R.string.landInfoRelatedLnmText).equals(landRelateLnmString)) {
                LandInfoObject.relateLnm = landRelateLnmString
//            }

            //???????????????
            LandInfoObject.nrfrstAtChk = when(activity.landSearchaNrfrstAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //????????????
            LandInfoObject.clvtAtChk = when(activity.landSearchClvtAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //???????????????
            LandInfoObject.buildAtChk = when(activity.landSearchBuildAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //???????????????
            LandInfoObject.plotAtChk = when(activity.landSearchPlotAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //??????????????????
            LandInfoObject.rwTrgetAt = when(activity.landSearchRwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //??????????????????
            LandInfoObject.partitnTrgetAt = when(activity.landSearchPartitnTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            //????????????
            LandInfoObject.sttusMesrAtChk = when(activity.landSearchSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            //????????????
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
            //??????????????????
            LandInfoObject.spclLadCn = activity.landSearchSpclLadCnEditText.text.toString()
            //?????????????????????
            LandInfoObject.ownerCnfirmBasisCl = when(activity.landSearchOwnerCnfirmBasisClChk.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }
            //????????????
            LandInfoObject.paclrMatter = activity.includePaclrMatterEdit.text.toString()
            //????????????
            LandInfoObject.referMatter = activity.includeReferMatterEdit.text.toString()
            //??????
            LandInfoObject.rm = activity.includeRmEdit.text.toString()






        }
    }
}
