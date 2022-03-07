/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_minrgt_search.*
import kotlinx.android.synthetic.main.fragment_minrgt_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.data.ThingMinrgtObject
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
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.HashMap

class MinrgtSearchFragment(activity: Activity, context: Context) : Fragment(),
    AdapterView.OnItemSelectedListener {

    private val mActivity = activity
    private val mContext = context
    private lateinit var minrgtTypeView: View
    private val wtnncUtill: WtnncUtil = WtnncUtil(activity, context)
    private var addViewCnt: Int = 0

    private var logUtil: LogUtil = LogUtil("MintgtSearchFragment");
    private var toastUtil: ToastUtil = ToastUtil(mContext)

    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null
    var builder: MaterialAlertDialogBuilder? = null

    private var minrgtAtchInfo: JSONArray? = null

    val wonFormat = DecimalFormat("#,###")

    lateinit var materialDialog: Dialog

    var wtnncImageAdapter: WtnncImageAdapter? = null

    init { }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        minrgtTypeView = inflater.inflate(R.layout.fragment_minrgt_search, null)
        return minrgtTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init(view)
        //사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.MINRGT,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }
        //등록일자
        minrgtRegistDe.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                minrgtRegistDe,
                "minrgtRegistDe"
            )
        }

        //스케치버튼 클릭
        searchShetchBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, null)
        }


        minrgtCntnncPdBgnde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                minrgtCntnncPdBgnde,
                "minrgtCntnncPdBgnde"
            )
        }
        minrgtCntnncPdEndde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                minrgtCntnncPdEndde,
                "minrgtCntnncPdEndde"
            )
        }

        //광업지적
        minrgtLgstr.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingMinrgtObject.minrgtLgstr = txtString
            }
//            ThingMinrgtObject.minrgtLgstr = "당진" + minrgtLgstr.text.toString() + "호"
            false
        }

        //광종
        mnrlKnd.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingMinrgtObject.mnrlKnd = txtString
            }
            false
        }

        //면적
        minrgtAr.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingMinrgtObject.minrgtAr = txtString
            }
            false
        }

        //탐광계획 신고일자
        minrgtProspectPlan.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                minrgtProspectPlan,
                "minrgtProspectPlan"
            )
        }

        //채광계획 인가일자
        minrgtMiningPlan.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                minrgtMiningPlan,
                "minrgtMiningPlan"
            )
        }

        //특이사항
        includePaclrMatterEdit.setOnEditorActionListener { v, actionId, event ->
            ThingMinrgtObject.paclrMatter = includePaclrMatterEdit.text.toString()
            false
        }

        //휴, 실직자 레이아웃 추가
        addMinrgtThingBtn.setOnClickListener {
            val minrgtViewGroup = minrgtBaseViewGroup
            val addThingView = R.layout.fragment_minrgt_add_layout
            val inflater: LayoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(addThingView, null)
            val itemView = inflater.inflate(addThingView, null)
            minrgtViewGroup?.addView(itemView)

            // 첫번째 spinner
            val addLayoutItem = minrgtViewGroup.getChildAt(addViewCnt) as ViewGroup
            val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
            val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
            val selectLayout1 = addLayoutItem.getChildAt(4) as ViewGroup
            val selectLayout2 = addLayoutItem.getChildAt(5) as ViewGroup
            val selectLayout3 = addLayoutItem.getChildAt(6) as ViewGroup
            val selectLayout4 = addLayoutItem.getChildAt(7) as ViewGroup
            val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner
            wtnncUtill.wtnncSpinnerAdapter(R.array.wtnncCommSclasArray, addSpinner1, this)
            addSpinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        2 -> {
                            selectLayout1.visibleView()
                            selectLayout2.visibleView()
                            selectLayout3.visibleView()
                            selectLayout4.visibleView()
                        }
                        3 -> {
                            selectLayout1.visibleView()
                            selectLayout2.visibleView()
                            selectLayout3.goneView()
                            selectLayout4.goneView()
                        }
                        else -> {
                            selectLayout1.goneView()
                            selectLayout2.goneView()
                            selectLayout3.goneView()
                            selectLayout4.goneView()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }

            // 두번째 spinner
            val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup
            val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup
            val addSpinner2 = addSpinnerLayout2.getChildAt(0) as Spinner
            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)



            // 이전일자
            var addViewGroup3 = addLayoutItem.getChildAt(7) as ViewGroup
            val bfDateTextView = addViewGroup3.getChildAt(0) as TextView
            bfDateTextView.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    bfDateTextView,
                    "bfDateTextView"
                )
            }

            // 실직일자
            val unemployedDeTextView = addViewGroup3.getChildAt(1) as TextView
            unemployedDeTextView.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    unemployedDeTextView,
                    "unemployedDeTextView"
                )
            }

            addViewCnt ++
        }

        // 허가등자료
        commPrmisnDtaBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val minrgtAtchSelectArray = JSONArray()

            array.add("허가등자료 등록")
            if(ThingMinrgtObject.thingNewSearch.equals("N")) {
                for(i in 0 until minrgtAtchInfo!!.length()) {
                    val minrgtAtchItem = minrgtAtchInfo!!.getJSONObject(i)
                    if(minrgtAtchItem.getString("fileseInfo").equals("A200006018")) {
                        array.add(minrgtAtchItem.getString("rgsde"))
                        minrgtAtchSelectArray!!.put(minrgtAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("허가등자료 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006018", "허가등자료")
                        } else {
                            val item = minrgtAtchSelectArray!!.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("취소") {_, _ ->
                        logUtil.d("setNegativeButton ------------------------->" )
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("현재 작성 중인 광업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }

//        // 카메라 어댑터 세팅
//        for (i in 0..4) {
//            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(i, null, "","","","","","","","",""))
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

    fun callThingCapture(fileCode: String, fileCodeNm: String) {
        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.MINRGT,
            fileCode,
            fileCodeNm,
            CameraEnum.DOCUMENT
        )
    }

    fun callThingFileDownload(item: JSONObject) {
        val thingAtchFileMap = HashMap<String, String>()
        thingAtchFileMap["atchCode"] = item.getString("atchCode")
        thingAtchFileMap["thingWtnCode"] = item.getString("thingWtnCode")
        thingAtchFileMap["saupCode"] = item.getString("saupCode")
        thingAtchFileMap["atfl"] = item.getString("atfl")
        thingAtchFileMap["atflNm"] = item.getString("atflNm")
        thingAtchFileMap["fileseInfo"] = item.getString("fileseInfo")
        thingAtchFileMap["fileseInfoNm"] = item.getString("fileseInfoNm")
        thingAtchFileMap["atflSize"] = item.getString("atflSize")

        val thingAtchFileUrl = context!!.resources.getString(R.string.mobile_url) + "thingFiledownload"

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(thingAtchFileMap, progressDialog, thingAtchFileUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        toastUtil.msg_error(R.string.msg_server_connected_fail, 100)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                        val fileNameString = "$downloadDirectory/${item.getString( "atfl")}"
                        var getLandFileBitmap: Bitmap? = null

                        if(FileUtil.getExtension(fileNameString) == "pdf"){
                            FileUtil.savePdfToFileCache(response.body?.byteStream()!!, fileNameString)
                        } else {
                            getLandFileBitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                        }

                        FileUtil.run {
                            createDir(downloadDirectory)
                            if(getExtension(fileNameString)?.contains("png") == true){
                                saveBitmapToFileCache(getLandFileBitmap!!, fileNameString)
                            }
                        }

                        val downloadFile = File(fileNameString)

                        WtnccDocViewFragment(downloadFile).show(requireActivity().supportFragmentManager, "docViewFragment")
                        progressDialog?.dismiss()
                    }

                })
    }

    fun init(view: View) {

        var requireArr = mutableListOf<TextView>(view.tv_minrgt_require1, view.tv_minrgt_require2, view.tv_minrgt_require4, view.tv_minrgt_require5)
        setRequireContent(requireArr)

        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, minrgtUnitSpinner, this) // 단위
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, minrgtAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, minrgtInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, minrgtOwnerCnfirmBasisSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.minrgtBsnClArray, bsnClDivSpinner, this)


        val dataString = requireActivity().intent!!.extras!!.get("MinrgtInfo") as String

        var dataJson = JSONObject(dataString)

        var thingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        ThingMinrgtObject.thingInfo = thingDataJson

        view.landSearchLocationText.setText(checkStringNull(thingDataJson.getString("legaldongNm")))

        view.landSearchBgnnLnmText.setText(checkStringNull(thingDataJson.getString("bgnnLnm")))
        view.landSearchincrprLnmText.setText(checkStringNull(thingDataJson.getString("incrprLnm")))
        val relateLnmString = checkStringNull(thingDataJson.getString("relateLnm"))
        if(relateLnmString.equals("")) {
            view.landSearchRelatedLnmText.setText("없음")
        } else {
            view.landSearchRelatedLnmText.setText(relateLnmString)
        }
        view.landSearchBgnnArText.setText(checkStringNull(thingDataJson.getString("ladBgnnAr")))
        view.landSearchIncrprArText.setText(checkStringNull(thingDataJson.getString("ladIncrprAr")))

        view.landSearchOwnerText.setText(checkStringNull(thingDataJson.getString("landOwnerName")))
        view.landSearchOwnerRText.setText(checkStringNull(thingDataJson.getString("landRelatesName")))


        val minrgtWtnCodeInt = thingDataJson.getInt("minrgtWtnCode")
        if(minrgtWtnCodeInt == 0) {
            view.minrgtWtnCodeText.setText("자동기입")
        } else {
            view.minrgtWtnCodeText.setText(minrgtWtnCodeInt.toString())
        }

        val minrgtThingKndString = checkStringNull(thingDataJson.getString("thingKnd"))
        if(minrgtThingKndString.equals("")) {
            view.minrgtThingKnd.setText("광업권보상")
        } else {
            view.minrgtThingKnd.setText(minrgtThingKndString)
        }

        val minrgtStrctNdStrndrdString = checkStringNull(thingDataJson.getString("strctNdStndrd"))
        if(minrgtStrctNdStrndrdString.equals("")) {
            view.minrgtStrctNdStrndrd.setText("자동기입")
        } else {
            view.minrgtStrctNdStrndrd.setText(minrgtStrctNdStrndrdString)
        }

        view.minrgtBgnnAr.setText(checkStringNull(thingDataJson.getString("bgnnAr")))
        view.minrgtIncrprAr.setText(checkStringNull(thingDataJson.getString("incrprAr")))
        val unitClString = checkStringNull(thingDataJson.getString("unitCl"))

        if (unitClString.equals("")) {
            view.minrgtUnitSpinner.setSelection(0)
        } else {
            val unitClStringSub = unitClString.substring(5, 7)
            view.minrgtUnitSpinner.setSelection(Integer.valueOf(unitClStringSub))
        }

        view.minrgtArComputBasis.setText(checkStringNull(thingDataJson.getString("arComputBasis")))

        // 휴업/폐업
        val bsnClString = checkStringNull(thingDataJson.getString("bsnCl"))
        if(bsnClString.equals("")) {
            view.bsnClDivSpinner.setSelection(0)
        } else {
            val bsnClStringsub = bsnClString.substring(5,7)
            view.bsnClDivSpinner.setSelection(Integer.valueOf(bsnClStringsub))
            when(bsnClString) {
                "A134001" -> {
                    view.bssMthCoLayout.visibility = View.VISIBLE
                    view.bssMthCoText.setText(checkStringNull(thingDataJson.getString("sssMthCo")))
                }
                "A134002", "A134003" -> {
                    view.bssMthCoLayout.visibility = View.GONE
                }
            }
        }



        view.minrgtRegNo.setText(checkStringNull(thingDataJson.getString("minrgtRegNo")))
        view.minrgtRegistDe.setText(checkStringNull(thingDataJson.getString("minrgtRegDe")))

        view.minrgtCntnncPdBgnde.setText(checkStringNull(thingDataJson.getString("cntnncPdBgnde")))
        view.minrgtCntnncPdEndde.setText(checkStringNull(thingDataJson.getString("cntnncPdEndde")))


//        view.minrgtCntnncPd.setText(checkStringNull(thingDataJson.getString("cntnncPdBgnde")) + '~'
//                                + checkStringNull(thingDataJson.getString("cntnncPdEndde")))
        view.minrgtLgstr.setText(checkStringNull(thingDataJson.getString("minrgtLgstr")))
        view.mnrlKnd.setText(checkStringNull(thingDataJson.getString("mnrlKnd")))
        view.minrgtAr.setText(checkStringNull(thingDataJson.getString("minrgtAr")))
        view.minrgtProspectPlan.setText(checkStringNull(thingDataJson.getString("prsptnPlanStemDe")))
        // 채광계획인가일자(임시주석)
        view.minrgtMiningPlan.setText(checkStringNull(thingDataJson.getString("miningPlanCnfmDe")))

//        val inclsClString = checkStringNull(thingDataJson.getString("inclsCl"))
//        if(inclsClString.equals("")) {
//            view.minrgtInclsSeSpinner.setSelection(0)
//        } else {
//            val inclsClStringSub = inclsClString.substring(5,7)
//            view.minrgtInclsSeSpinner.setSelection(Integer.valueOf(inclsClStringSub))
//        }
//
//        val acqsClString = checkStringNull(thingDataJson.getString("acqsCl"))
//        if(acqsClString.equals("")) {
//            view.minrgtAcqsSeSpinner.setSelection(0)
//        } else {
//            val acqsClStringSub = acqsClString.substring(5,7)
//            view.minrgtAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringSub))
//        }

        val ownerCnfirmString = checkStringNull(thingDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmString.equals("")) {
            view.minrgtOwnerCnfirmBasisSpinner.setSelection(4)
        } else {
            val ownerCnfirmStringSub = ownerCnfirmString.substring(5,7)
            view.minrgtOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmStringSub))
        }

        val rwTrgetAtString = checkStringNull(thingDataJson.getString("rwTrgetAt"))
        if(rwTrgetAtString.equals("")) {
            view.rwTrgetAtChk.isChecked = true
        } else {
            view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
        }

        val apasmtTrgetAtString = checkStringNull(thingDataJson.getString("apasmtTrgetAt"))
        if(apasmtTrgetAtString.equals("")) {
            view.apasmtTrgetAtChk.isChecked = true
        } else {
            view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")
        }

        view.includePaclrMatterEdit.setText(checkStringNull(thingDataJson.getString("paclrMatter")))

        view.includeReferMatterEdit.setText(checkStringNull(thingDataJson.getString("referMatter")))

        view.includeRmEdit.setText(checkStringNull(thingDataJson.getString("rm")))

        if(ThingMinrgtObject.thingNewSearch.equals("N")) {
            val thingSubArray = dataJson.getJSONArray("minrgtSubThing")
            for (i in 0 until thingSubArray.length()) {
                val data = thingSubArray.getJSONObject(i)

                initMinrgtSubThingLayout(i, data)
            }

            // 광업 문서
            minrgtAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until minrgtAtchInfo!!.length()) {
                val minrgtAtchItem = minrgtAtchInfo!!.getJSONObject(i)

                val minrgtAtchFileInfo = minrgtAtchItem.getString("fileseInfo")

                // 허가등자료
                view.commPrmisnDtaBtn.backgroundTintList = when (minrgtAtchFileInfo) {
                    "A200006018" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
            }

            settingSearchCamerasView(minrgtAtchInfo)

        } else {
            settingSearchCamerasView(null)
        }


    }

    fun settingSearchCamerasView(dataArray: JSONArray?) {
        for(i in 0..4) {
            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(i, null, "","","","","","","","",""))
        }
        Constants.CAMERA_ADAPTER = WtnncImageAdapter(requireContext(), Constants.CAMERA_IMAGE_ARR)

        val layoutManager = LinearLayoutManager(requireContext())

        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        includeImageViewRv.also {
            it.layoutManager = layoutManager
            it.adapter = Constants.CAMERA_ADAPTER
        }

        if(ThingMinrgtObject.thingNewSearch.equals("N")) {
            if(dataArray!!.length() > 0) {
                var searchImageArray = JSONArray()

                for(i in 0 until dataArray!!.length()) {
                    val dataItem = dataArray!!.getJSONObject(i)

                    if(dataItem.getString("fileseInfo").equals("A200006012")) {
                        searchImageArray.put(dataItem)
                    }
                }

                PermissionUtil.logUtil.d("searchImageArray length ---------------------------> ${searchImageArray.length()}")

                val thingAtchFileMapArr = ArrayList<HashMap<String, String>>()
                var item = JSONObject()

                for (i in 0 until searchImageArray.length()) {
                    item = searchImageArray.getJSONObject(i)
                    val thingAtchFileMap = HashMap<String, String>()
                    thingAtchFileMap["atchCode"] = item.getString("atchCode")
                    thingAtchFileMap["thingWtnCode"] = item.getString("thingWtnCode")
                    thingAtchFileMap["saupCode"] = item.getString("saupCode")
                    thingAtchFileMap["atfl"] = item.getString("atfl")
                    thingAtchFileMap["atflNm"] = item.getString("atflNm")
                    thingAtchFileMap["fileseInfo"] = item.getString("fileseInfo")
                    thingAtchFileMap["fileseInfoNm"] = item.getString("fileseInfoNm")
                    thingAtchFileMap["atflSize"] = item.getString("atflSize")

                    thingAtchFileMapArr.add(thingAtchFileMap)
                }

                val thingAtchFileUrl = context!!.resources.getString(R.string.mobile_url) + "thingFileDownload"

                thingAtchFileMapArr.forEachIndexed { idx, it ->
                    PermissionUtil.logUtil.d(it.toString())

                    HttpUtil.getInstance(context!!)
                        .callerUrlInfoPostWebServer(it, progressDialog, thingAtchFileUrl,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    progressDialog?.dismiss()
                                    toastUtil.msg_error(R.string.msg_server_connected_fail, 100)
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
                                            "FARM",
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
                                            Constants.CAMERA_ADAPTER?.addItem(
                                                wtnncImage,
                                                Constants.BIZ_SUBCATEGORY_KEY.toString()
                                            )
                                        } else {
                                            Constants.CAMERA_ADAPTER?.updateItem(
                                                wtnncImage,
                                                Constants.CAMERA_IMGAE_INDEX,
                                                Constants.BIZ_SUBCATEGORY_KEY.toString()
                                            )
                                        }

                                        progressDialog?.dismiss()
                                    }
                                }

                            })
                }
            }
        }
    }

    fun initMinrgtSubThingLayout(recentItemCnt: Int, data: JSONObject) {
        addViewCnt = recentItemCnt

         val minrgtViewGroup = minrgtBaseViewGroup
        val addThingView = R.layout.fragment_minrgt_add_layout
        val inflater: LayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(addThingView, null)
        val itemView = inflater.inflate(addThingView, null)
        minrgtViewGroup?.addView(itemView)

        // 첫번째 spinner
        val subThingBaseLayout = minrgtViewGroup.getChildAt(addViewCnt) as ViewGroup
        val subThingFirstView = subThingBaseLayout.getChildAt(1) as ViewGroup
        val subThingSmallClView = subThingFirstView.getChildAt(0) as ViewGroup
        val subThingSmallCl = subThingSmallClView.getChildAt(0) as Spinner
        val subThingKnd = subThingFirstView.getChildAt(1) as EditText
        val subThingStrctNdStndrd = subThingFirstView.getChildAt(2) as EditText


        val subThingSecendView = subThingBaseLayout.getChildAt(3) as ViewGroup
        val subThingArView = subThingSecendView.getChildAt(0) as ViewGroup
        val subThingBgnnAr = subThingArView.getChildAt(0) as EditText
        val subThingIncrprAr = subThingArView.getChildAt(1) as EditText
        val subThingUnitClView = subThingSecendView.getChildAt(1) as ViewGroup
        val subThingUnitCl = subThingUnitClView.getChildAt(0) as Spinner
        val subThingArComputBasis = subThingSecendView.getChildAt(2) as EditText


        wtnncUtill.wtnncSpinnerAdapter(R.array.wtnncCommSclasArray, subThingSmallCl, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, subThingUnitCl, this)

        val thingSmallClString = checkStringNull(data.getString("thingSmallCl"))
        if(thingSmallClString.equals("A016022")) {
            subThingSmallCl.setSelection(1)
        }
        subThingKnd.setText(checkStringNull(data.getString("thingKnd")))
        subThingStrctNdStndrd.setText(checkStringNull(data.getString("strctNdStndrd")))
        subThingBgnnAr.setText(checkStringNull(data.getString("bgnnAr")))
        subThingIncrprAr.setText(checkStringNull(data.getString("incrprAr")))

        val thingUnitClString = checkStringNull(data.getString("unitCl"))
        if(thingUnitClString.equals("")) {
            subThingUnitCl.setSelection(0)
        } else {
            val thingUnitClStringSub = thingUnitClString.substring(5,7)
            subThingUnitCl.setSelection(Integer.valueOf(thingUnitClStringSub))
        }
        subThingArComputBasis.setText(checkStringNull(data.getString("arComputBasis")))


        addViewCnt ++
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when(parent?.id) {
            R.id.minrgtUnitSpinner -> {
                when (position) {
                    1 -> ThingMinrgtObject.unitCl = "A009001"
                    2 -> ThingMinrgtObject.unitCl = "A009002"
                    3 -> ThingMinrgtObject.unitCl = "A009003"
                    4 -> ThingMinrgtObject.unitCl = "A009004"
                    5 -> ThingMinrgtObject.unitCl = "A009005"
                    6 -> ThingMinrgtObject.unitCl = "A009006"
                    7 -> ThingMinrgtObject.unitCl = "A009007"
                    8 -> ThingMinrgtObject.unitCl = "A009008"
                    9 -> ThingMinrgtObject.unitCl = "A009009"
                    10 -> ThingMinrgtObject.unitCl = "A009010"
                    11 -> ThingMinrgtObject.unitCl = "A009011"
                    12 -> ThingMinrgtObject.unitCl = "A009012"
                    13 -> ThingMinrgtObject.unitCl = "A009013"
                    14 -> ThingMinrgtObject.unitCl = "A009014"
                    15 -> ThingMinrgtObject.unitCl = "A009015"
                    16 -> ThingMinrgtObject.unitCl = "A009016"
                    17 -> ThingMinrgtObject.unitCl = "A009017"
                    18 -> ThingMinrgtObject.unitCl = "A009018"
                    19 -> ThingMinrgtObject.unitCl = "A009019"
                    20 -> ThingMinrgtObject.unitCl = "A009020"
                    21 -> ThingMinrgtObject.unitCl = "A009021"
                    22 -> ThingMinrgtObject.unitCl = "A009022"
                    23 -> ThingMinrgtObject.unitCl = "A009023"
                    24 -> ThingMinrgtObject.unitCl = "A009024"
                    25 -> ThingMinrgtObject.unitCl = "A009025"
                    26 -> ThingMinrgtObject.unitCl = "A009026"
                    27 -> ThingMinrgtObject.unitCl = "A009027"
                    28 -> ThingMinrgtObject.unitCl = "A009028"
                    29 -> ThingMinrgtObject.unitCl = "A009029"
                    30 -> ThingMinrgtObject.unitCl = "A009030"
                    31 -> ThingMinrgtObject.unitCl = "A009031"
                    32 -> ThingMinrgtObject.unitCl = "A009032"
                    33 -> ThingMinrgtObject.unitCl = "A009033"
                    34 -> ThingMinrgtObject.unitCl = "A009034"
                    35 -> ThingMinrgtObject.unitCl = "A009035"
                    36 -> ThingMinrgtObject.unitCl = "A009036"
                    37 -> ThingMinrgtObject.unitCl = "A009037"
                    38 -> ThingMinrgtObject.unitCl = "A009038"
                    39 -> ThingMinrgtObject.unitCl = "A009039"
                    40 -> ThingMinrgtObject.unitCl = "A009040"
                    41 -> ThingMinrgtObject.unitCl = "A009041"
                    42 -> ThingMinrgtObject.unitCl = "A009042"
                    43 -> ThingMinrgtObject.unitCl = "A009043"
                    44 -> ThingMinrgtObject.unitCl = "A009044"
                    45 -> ThingMinrgtObject.unitCl = "A009045"
                    46 -> ThingMinrgtObject.unitCl = "A009046"
                    47 -> ThingMinrgtObject.unitCl = "A009047"
                    48 -> ThingMinrgtObject.unitCl = "A009048"
                    49 -> ThingMinrgtObject.unitCl = "A009049"
                    50 -> ThingMinrgtObject.unitCl = "A009050"
                    51 -> ThingMinrgtObject.unitCl = "A009051"
                    52 -> ThingMinrgtObject.unitCl = "A009052"
                    53 -> ThingMinrgtObject.unitCl = "A009053"
                    54 -> ThingMinrgtObject.unitCl = "A009054"
                    55 -> ThingMinrgtObject.unitCl = "A009055"
                    56 -> ThingMinrgtObject.unitCl = "A009056"
                    57 -> ThingMinrgtObject.unitCl = "A009057"
                    58 -> ThingMinrgtObject.unitCl = "A009058"
                    59 -> ThingMinrgtObject.unitCl = "A009059"
                    60 -> ThingMinrgtObject.unitCl = "A009060"
                    61 -> ThingMinrgtObject.unitCl = "A009061"
                    62 -> ThingMinrgtObject.unitCl = "A009062"
                    63 -> ThingMinrgtObject.unitCl = "A009063"
                    64 -> ThingMinrgtObject.unitCl = "A009064"
                    65 -> ThingMinrgtObject.unitCl = "A009065"
                    66 -> ThingMinrgtObject.unitCl = "A009066"
                    67 -> ThingMinrgtObject.unitCl = "A009067"
                    68 -> ThingMinrgtObject.unitCl = "A009068"
                    else -> ThingMinrgtObject.unitCl = ""
                }
            }
            R.id.bsnClDivSpinner -> {
                when (position) {
                    1 -> bssMthCoLayout.visibleView()
                    2 -> bssMthCoLayout.goneView()
                    3 -> bssMthCoLayout.goneView()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addMinrgtData() {

        // 대분류 입력
        ThingMinrgtObject.thingLrgeCl = "A011005" // 영업
//        Log.d("minrgtTest", "대분류 : ${ThingWtnObject.thingLclas}")

        //소분류 입력
        ThingMinrgtObject.thingSmallCl = "A016020" // 광업

        ThingMinrgtObject.thingKnd = "광업권."

        Log.d("minrgtTest", "소분류 : ${ThingMinrgtObject.thingSmallCl}")

        ThingMinrgtObject.bgnnAr = mActivity.minrgtBgnnAr.text.toString() // 전체면적
        Log.d("minrgtTest", "전체면적 : ${ThingMinrgtObject.bgnnAr}")

        ThingMinrgtObject.incrprAr = mActivity.minrgtIncrprAr.text.toString() // 편입면적
        Log.d("minrgtTest", "편입면적 : ${ThingMinrgtObject.incrprAr}")

        ThingMinrgtObject.unitCl = when (mActivity.minrgtUnitSpinner.selectedItemPosition) { // 단위
            1 -> "A009001"
            2 -> "A009002"
            3 -> "A009003"
            4 -> "A009004"
            5 -> "A009005"
            6 -> "A009006"
            7 -> "A009007"
            8 -> "A009008"
            9 -> "A009009"
            10 -> "A009010"
            11 -> "A009011"
            12 -> "A009012"
            13 -> "A009013"
            14 -> "A009014"
            15 -> "A009015"
            16 -> "A009016"
            17 -> "A009017"
            18 -> "A009018"
            19 -> "A009019"
            20 -> "A009020"
            21 -> "A009021"
            22 -> "A009022"
            23 -> "A009023"
            24 -> "A009024"
            25 -> "A009025"
            26 -> "A009026"
            27 -> "A009027"
            28 -> "A009028"
            29 -> "A009029"
            30 -> "A009030"
            31 -> "A009031"
            32 -> "A009032"
            33 -> "A009033"
            34 -> "A009034"
            35 -> "A009035"
            36 -> "A009036"
            37 -> "A009037"
            38 -> "A009038"
            39 -> "A009039"
            40 -> "A009040"
            41 -> "A009041"
            42 -> "A009042"
            43 -> "A009043"
            44 -> "A009044"
            45 -> "A009045"
            46 -> "A009046"
            47 -> "A009047"
            48 -> "A009048"
            49 -> "A009049"
            50 -> "A009050"
            51 -> "A009051"
            52 -> "A009052"
            53 -> "A009053"
            54 -> "A009054"
            55 -> "A009055"
            56 -> "A009056"
            57 -> "A009057"
            58 -> "A009058"
            59 -> "A009059"
            60 -> "A009060"
            61 -> "A009061"
            62 -> "A009062"
            63 -> "A009063"
            64 -> "A009064"
            65 -> "A009065"
            66 -> "A009066"
            67 -> "A009067"
            68 -> "A009068"
            else -> ""
        }
        Log.d("minrgtTest", "단위 : ${ThingMinrgtObject.unitCl}")

        ThingMinrgtObject.arComputBasis = mActivity.minrgtArComputBasis.text.toString() // 면적산출근거
        Log.d("minrgtTest", "면적산출근거 : ${ThingMinrgtObject.arComputBasis}")

        // 휴업 /폐업
        ThingMinrgtObject.bsnCl = when(mActivity.bsnClDivSpinner.selectedItemPosition) {
            1 -> "A134001"
            2 -> "A134002"
            3 -> "A134003"
            else ->""

        }
        ThingMinrgtObject.sssMthCo = mActivity.bssMthCoText.text.toString()

        ThingMinrgtObject.minrgtRegNo = "제" + mActivity.minrgtRegNo.text.toString() + "호" // 등록번호
        Log.d("minrgtTest", "등록번호 : ${ThingMinrgtObject.minrgtRegNo}")

        ThingMinrgtObject.minrgtRegDe = mActivity.minrgtRegistDe.text.toString() // 등록일자
        Log.d("minrgtTest", "등록일자 : ${ThingMinrgtObject.minrgtRegNo}")

        Log.d("minrgtTest", "존속기간시작 : ${ThingMinrgtObject.cntnncPdBgnde}")

        Log.d("minrgtTest", "존속기간끝 : ${ThingMinrgtObject.cntnncPdEndde}")

        ThingMinrgtObject.minrgtLgstr = "당진" + mActivity.minrgtLgstr.text.toString() + "호" // 광업지적
        Log.d("minrgtTest", "광업지적 : ${ThingMinrgtObject.minrgtLgstr}")

        ThingMinrgtObject.mnrlKnd = mActivity.mnrlKnd.text.toString() // 광종
        Log.d("minrgtTest", "광종 : ${ThingMinrgtObject.mnrlKnd}")

        ThingMinrgtObject.minrgtAr = mActivity.minrgtAr.text.toString() // 면적
        Log.d("minrgtTest", "면적 : ${ThingMinrgtObject.minrgtAr}")

        ThingMinrgtObject.prsptnPlanStemDe = mActivity.minrgtProspectPlan.text.toString()
        Log.d("minrgtTest", "탐광계획신고일자 : ${ThingMinrgtObject.prsptnPlanStemDe}")

        ThingMinrgtObject.miningPlanCnfmDe = mActivity.minrgtMiningPlan.text.toString()
        Log.d("minrgtTest", "채광계획인가일자 : ${ThingMinrgtObject.miningPlanCnfmDe}")

        //구조 및 규격
//        ThingMinrgtObject.strctNdStndrd = StringBuilder().apply {
//            append("광업지적(" + ThingMinrgtObject.minrgtLgstr + "), ") //광업지적
//            append("광종(" + ThingMinrgtObject.mnrlKnd + "), ") //광종
//            append("등록(" + ThingMinrgtObject.minrgtRegNo + "/") //등록번호
//            append(ThingMinrgtObject.minrgtRegDe + "), ") //등록일자
//            append("존속기간(" + ThingMinrgtObject.cntnncPdBgnde + " ~ ") //존속기간 시작
//            append(ThingMinrgtObject.cntnncPdEndde + ")") //존속기간 끝
//        }.toString()
//        Log.d("minrgtTest", "구조 및 규격 : ${ThingMinrgtObject.strctNdStndrd}")

        ThingMinrgtObject.strctNdStndrd = StringBuilder().apply {
            when(ThingMinrgtObject.bsnCl) {
                "A134001" -> append("휴업보상, (")
                "A134002" -> append("감구보상, (")
                "A134003" -> append("광업보상불가, (")
            }
            append("등록내역(${ThingMinrgtObject.minrgtRegNo})")
            append(", 광업지적(${ThingMinrgtObject.minrgtLgstr})")
            append(", 광종(${ThingMinrgtObject.mnrlKnd})")
        }.toString()

//        ThingMinrgtObject.inclsCl = when (mActivity.minrgtInclsSeSpinner.selectedItemPosition) { // 포함구분
//            1 -> "A007001"
//            2 -> "A007001"
//            3 -> "A007001"
//            else -> ""
//        }
//        Log.d("minrgtTest", "포함구분 : ${ThingMinrgtObject.inclsCl}")
//
//        ThingMinrgtObject.acqsCl = when (mActivity.minrgtAcqsSeSpinner.selectedItemPosition) { // 취득구분
//            1 -> "A025001"
//            2 -> "A025002"
//            3 -> "A025003"
//            4 -> "A025004"
//            5 -> "A025005"
//            6 -> "A025006"
//            7 -> "A025007"
//            8 -> "A025008"
//            9 -> "A025009"
//            10 -> "A025010"
//            11 -> "A025011"
//            12 -> "A025012"
//            else -> ""
//        }

        ThingMinrgtObject.acqsCl = "A025001"
        ThingMinrgtObject.inclsCl = "A007001"

        ThingMinrgtObject.ownerCnfirmBasisCl =
            when (mActivity.minrgtOwnerCnfirmBasisSpinner.selectedItemPosition) { // 소유자확인근거
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }
        Log.d("minrgtTest", "소유자확인근거 : ${ThingMinrgtObject.ownerCnfirmBasisCl}")

        ThingMinrgtObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) { // 보상대상여부
            true -> "Y"
            else -> "N"
        }
        Log.d("minrgtTest", "보상대상여뷰 : ${ThingMinrgtObject.rwTrgetAt}")

        ThingMinrgtObject.apasmtTrgetAt = when (mActivity.apasmtTrgetAtChk.isChecked) { // 감정평가대상여부
            true -> "Y"
            else -> "N"
        }
        Log.d("minrgtTest", "감정평가대상여부 : ${ThingMinrgtObject.apasmtTrgetAt}")

        ThingMinrgtObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString() // 특이사항
        Log.d("minrgtTest", "특이사항 : ${ThingMinrgtObject.paclrMatter}")

        ThingMinrgtObject.referMatter = mActivity.includeReferMatterEdit.text.toString()

        ThingMinrgtObject.rm = mActivity.includeRmEdit.text.toString()


//        var itemCnt = mActivity.minrgtBaseViewGroup.childCount
        var minrgtAddItemArray: MutableList<MutableMap<String, String>> = mutableListOf()

        // 광업권 시설물 추가
        val minrgtThingCnt = mActivity.minrgtBaseViewGroup.childCount
        var minrgtThingArray = JSONArray()
        var minrgtThingJSON = JSONObject()
        if(minrgtThingCnt > 0) {
            for (i in 0 until minrgtThingCnt) {
                Log.d("minrgtTest", "******************************************************************")

//            val minrgtAddItemMap: MutableMap<String, String> = hashMapOf()
                val minrgtViewGroup = mActivity.minrgtBaseViewGroup // 물건 추가 Base ViewGroup
                val addLayoutItem = minrgtViewGroup.getChildAt(i) as ViewGroup // i번째 추가 부분
                val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
                val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup
                val addViewGroup3 = addLayoutItem.getChildAt(5) as ViewGroup
                val addViewGroup4 = addLayoutItem.getChildAt(7) as ViewGroup

                // 소분류
                val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
                val addSmallClSpinner = addSpinnerLayout1.getChildAt(0) as Spinner

                var minrgtThingItem = JSONObject()
                when (addSmallClSpinner.selectedItemPosition) {

                    1 -> { // 시설물
//                        minrgtAddItemMap["thingSmallClValue"] = "A016022"
                        minrgtThingItem.put("thingSmallClValue", "A016022")
//                        Log.d("minrgtTest", "소분류 : ${minrgtAddItemMap["thingSmallClValue"]}")
                    }
                    2 -> { // 실직자
//                        minrgtAddItemMap["thingSmallClValue"] = "A016024"
                        minrgtThingItem.put("thingSmallClValue","A016024")

//                        Log.d("minrgtTest", "소분류 : ${minrgtAddItemMap["thingSmallClValue"]}")

                        // 구분
                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
//                        minrgtAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
                        minrgtThingItem.put("bsnUnemplTy", addUnemplTy.text.toString())
//                        Log.d("minrgtTest", "구분 : ${minrgtAddItemMap["bsnUnemplTy"]}")

                        // 일수
                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
                        minrgtThingItem.put("bsnUnemplCo", addUnemplCo.text.toString())
//                        minrgtAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        Log.d("minrgtTest", "일수 : ${minrgtAddItemMap["bsnUnemplCo"]}")

                        // 평균임금
                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
                        minrgtThingItem.put("bsnAvrgWage", addAvrgWage.text.toString())
//                        minrgtAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        Log.d("minrgtTest", "평균임금 : ${minrgtAddItemMap["bsnAvrgWage"]}")

                        // 통상임금
                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
                        minrgtThingItem.put("bsnOdygs", addOdygs.text.toString())
//                        minrgtAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        Log.d("minrgtTest", "통상임금 : ${minrgtAddItemMap["bsnOdygs"]}")

                        //  근로장소이전일자
                        val addBeforeDe = addViewGroup4.getChildAt(0) as TextView
                        minrgtThingItem.put("bsnBeforeDe", addBeforeDe.text.toString())
//                        minrgtAddItemMap["bsnBeforeDe"] = addBeforeDe.text.toString()
//                        Log.d("minrgtTest", "근로장소이전일자 : ${minrgtAddItemMap["bsnBeforeDe"]}")

                        // 실직일자
                        val addUnemplDe = addViewGroup4.getChildAt(1) as TextView
                        minrgtThingItem.put("bsnUnemplDe", addUnemplDe.text.toString())
//                        minrgtAddItemMap["bsnUnemplDe"] = addUnemplDe.text.toString()
//                        Log.d("minrgtTest", "실적일자 : ${minrgtAddItemMap["bsnUnemplDe"]}")

                        // 실직사유
                        val addUnemplResn = addViewGroup4.getChildAt(2) as EditText
                        minrgtThingItem.put("bsnUnemplRes", addUnemplResn.text.toString())
//                        minrgtAddItemMap["bsnUnemplResn"] = addUnemplResn.text.toString()
//                        Log.d("minrgtTest", "실적사유 : ${minrgtAddItemMap["bsnUnemplResn"]}")
                    }
                    3 -> { // 휴직자
//                        minrgtAddItemMap["thingSmallClValue"] = "A016023"
//                        Log.d("minrgtTest", "휴직자 : ${minrgtAddItemMap["thingSmallClValue"]}")
                        minrgtThingItem.put("thingSmallClValue", "A016023")
                        // 구분
                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
                        minrgtThingItem.put("bsnUnemplTy", addUnemplTy.text.toString())
//                        minrgtAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
//                        Log.d("minrgtTest", "구분 : ${minrgtAddItemMap["bsnUnemplTy"]}")

                        // 일수
                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
                        minrgtThingItem.put("bsnUnemplCo", addUnemplCo.text.toString())
//                        minrgtAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        Log.d("minrgtTest", "일수 : ${minrgtAddItemMap["bsnUnemplCo"]}")

                        // 평균임금
                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
                        minrgtThingItem.put("bsnAvrgWage", addAvrgWage.text.toString())
//                        minrgtAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        Log.d("minrgtTest", "평균임금 : ${minrgtAddItemMap["bsnAvrgWage"]}")

                        // 통상임금
                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
                        minrgtThingItem.put("bsnOdygs", addOdygs.text.toString())
//                        minrgtAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        Log.d("minrgtTest", "통상임금 : ${minrgtAddItemMap["bsnOdygs"]}")
                    }
                }

                // 물건의 종류
                val addThingKndText = addViewGroup1.getChildAt(1) as EditText
                minrgtThingItem.put("thingKnd", addThingKndText.text.toString())
//                minrgtAddItemMap["thingKnd"] = addThingKndText.text.toString()
//                Log.d("minrgtTest", "물건의 종류 : ${minrgtAddItemMap["thingKnd"]}")

                // 구조 및 규격
                val addStrctNdStrndrdText = addViewGroup1.getChildAt(2) as EditText
                minrgtThingItem.put("strctNdStrndrd", addStrctNdStrndrdText.text.toString())
//                minrgtAddItemMap["strctNdStrndrd"] = addStrctNdStrndrdText.text.toString()
//                Log.d("minrgtTest", "구조 및 규격 : ${minrgtAddItemMap["strctNdStrndrd"]}")

                // 전체면적
                val addViewGroupLayout = addViewGroup2.getChildAt(0) as ViewGroup
                val addBgnnArText = addViewGroupLayout.getChildAt(0) as EditText
                minrgtThingItem.put("bgnnAr", addBgnnArText.text.toString())
//                minrgtAddItemMap["bgnnAr"] = addBgnnArText.text.toString()
//                Log.d("minrgtTest", "전체면적 : ${minrgtAddItemMap["bgnnAr"]}")

                // 편입면적
                val addIncrprArText = addViewGroupLayout.getChildAt(1) as EditText
                minrgtThingItem.put("incrprAr", addIncrprArText.text.toString())
//                minrgtAddItemMap["incrprAr"] = addIncrprArText.text.toString()
//                Log.d("minrgtTest", "편입면적 : ${minrgtAddItemMap["incrprAr"]}")

                // 단위
                val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup
                val addUnitClSpinner = addSpinnerLayout2.getChildAt(0) as Spinner
                minrgtThingItem.put("unitCl", when(addUnitClSpinner.selectedItemPosition) {
//                minrgtAddItemMap["unitCl"] = when (addUnitClSpinner.selectedItemPosition) {
                    1 -> "A009001"
                    2 -> "A009002"
                    3 -> "A009003"
                    4 -> "A009004"
                    5 -> "A009005"
                    6 -> "A009006"
                    7 -> "A009007"
                    8 -> "A009008"
                    9 -> "A009009"
                    10 -> "A009010"
                    11 -> "A009011"
                    12 -> "A009012"
                    13 -> "A009013"
                    14 -> "A009014"
                    15 -> "A009015"
                    16 -> "A009016"
                    17 -> "A009017"
                    18 -> "A009018"
                    19 -> "A009019"
                    20 -> "A009020"
                    21 -> "A009021"
                    22 -> "A009022"
                    23 -> "A009023"
                    24 -> "A009024"
                    25 -> "A009025"
                    26 -> "A009026"
                    27 -> "A009027"
                    28 -> "A009028"
                    29 -> "A009029"
                    30 -> "A009030"
                    31 -> "A009031"
                    32 -> "A009032"
                    33 -> "A009033"
                    34 -> "A009034"
                    35 -> "A009035"
                    36 -> "A009036"
                    37 -> "A009037"
                    38 -> "A009038"
                    39 -> "A009039"
                    40 -> "A009040"
                    41 -> "A009041"
                    42 -> "A009042"
                    43 -> "A009043"
                    44 -> "A009044"
                    45 -> "A009045"
                    46 -> "A009046"
                    47 -> "A009047"
                    48 -> "A009048"
                    49 -> "A009049"
                    50 -> "A009050"
                    51 -> "A009051"
                    52 -> "A009052"
                    53 -> "A009053"
                    54 -> "A009054"
                    55 -> "A009055"
                    56 -> "A009056"
                    57 -> "A009057"
                    58 -> "A009058"
                    59 -> "A009059"
                    60 -> "A009060"
                    61 -> "A009061"
                    62 -> "A009062"
                    63 -> "A009063"
                    64 -> "A009064"
                    65 -> "A009065"
                    66 -> "A009066"
                    67 -> "A009067"
                    68 -> "A009068"
                    else -> ""
                })
//                Log.d("minrgtTest", "단위 : ${minrgtAddItemMap["unitCl"]}")

                // 면적산출근거
                val addArComputBasisText = addViewGroup2.getChildAt(2) as EditText
                minrgtThingItem.put("arComputBasis", addArComputBasisText.text.toString())
//                minrgtAddItemMap["arComputBasis"] = addArComputBasisText.text.toString()
//                Log.d("minrgtTest", "면적산출근거 : ${minrgtAddItemMap["arComputBasis"]}")

//                minrgtAddItemArray.add(i, minrgtAddItemMap)
                if(ThingMinrgtObject.thingNewSearch.equals("N")) {
                    minrgtThingItem.put("thingWtnCode", ThingMinrgtObject.thingInfo!!.getString("thingWtnCode"))
                }
                Log.d("minrgtTest", "******************************************************************")



                minrgtThingArray.put(minrgtThingItem)
            }
            minrgtThingJSON.put("minrgtThing", minrgtThingArray)


        } else {
            minrgtThingJSON.put("minrgtThing", minrgtThingArray)
        }
        // 물건 추가 부분
        ThingMinrgtObject.addMinrgtThing = minrgtThingJSON

        // 물건 추가 부분
//        ThingMinrgtObject.minrgtAddItemList = minrgtAddItemArray
    }
    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

}