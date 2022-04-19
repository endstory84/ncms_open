/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_fyhts_search.*
import kotlinx.android.synthetic.main.fragment_fyhts_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.CommonCodeInfoList
import kr.or.kreb.ncms.mobile.data.ThingFyhtsObject
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
import java.util.*

class FyhtsSearchFragment(activity: Activity, context: Context) : BaseFragment(),
    AdapterView.OnItemSelectedListener,
    DialogUtil.ClickListener  {

    private val mActivity = activity
    private val mContext = context
    private lateinit var fyhtsTypeView: View
    private val wtnncUtill: WtnncUtil = WtnncUtil(activity, context)
    private var addViewCnt: Int = 0

    val wonFormat = DecimalFormat("#,###")

    private var fyhtsAtchInfo: JSONArray? = null

    lateinit var materialDialog: Dialog

    var dcsnAt: String? = "N"

    var wtnncImageAdapter: WtnncImageAdapter? = null

    init { }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fyhtsTypeView = inflater.inflate(R.layout.fragment_fyhts_search, null)
        dialogUtil = DialogUtil(context, activity)
        dialogUtil!!.setClickListener(this)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        return fyhtsTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        init(view)

        //사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.FYHTS,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, fyhtsInclsSeSpinner, this) // 포함분류
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, fyhtsAcqsSeSpinner, this) // 취득분류


        searchShetchBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, null)
        }

        fyhtsBgnnAr.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.bgnnAr = txtString
            }
            false
        }
        fyhtsIncrprAr.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.incrprAr = txtString
            }
            false
        }
        fyhtsArComputBasis.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.arComputBasis = txtString
            }
            false
        }
        administGrc.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.administGrc = txtString
            }
            false
        }
        lcnsKnd.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.lcnsKnd = txtString
            }
            false
        }
        lcnsNo.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.lcnsNo = txtString
            }
            false
        }
        fshlLc.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.fshlLc = txtString
            }
            false
        }
        fyhtsAr.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.fyhtsAr = txtString
            }
            false
        }
        fshrMth.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.fshrMth = txtString
            }
            false
        }
        includePaclrMatterEdit.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()
                ThingFyhtsObject.referMatter = txtString
            }
            false
        }


        //면허일자
        lcnsDe.setOnClickListener {
            WtnncUtil(mActivity, mContext).wtnncDatePicker(
                requireActivity().supportFragmentManager,
                lcnsDe,
                "lcnsDe"
            )
        }
        fyhtsCntnncPdBgnde.setOnClickListener{
            WtnncUtil(mActivity, mContext).wtnncDatePicker(
                requireActivity().supportFragmentManager,
                fyhtsCntnncPdBgnde,
                "fyhtsCntnncPdBgnde"
            )
        }
        fyhtsCntnncPdEndde.setOnClickListener{
            toast.msg("영업중은 날짜 미기입", 1000)
            WtnncUtil(mActivity, mContext).wtnncDatePicker(
                requireActivity().supportFragmentManager,
                fyhtsCntnncPdEndde,
                "fyhtsCntnncPdEndde"
            )
        }

        //존속기간
//        fyhtsCntnncPd.setOnClickListener {
//            WtnncUtill(mActivity, mContext).wtnncDateRangePicker(
//                requireActivity().supportFragmentManager,
//                fyhtsCntnncPd,
//                "fyhtsCntnncPd"
//            )
//        }

        //수면의 위치 및 구역도 유무
//        srfwtrLcZoneAt.setOnClickListener {
//            val fyhtsData = arrayOf("유", "무")
//            val builder = AlertDialog.Builder(mContext)
//
////            builder.setSingleChoiceItems(fyhtsData, 0){dialogInterface, i ->
////                ToastUtil(mContext).msg(fyhtsData[i], Toast.LENGTH_SHORT)
////            }
//
//            builder.setSingleChoiceItems(fyhtsData, 0, null)
//
//            builder.setNegativeButton("취소", null)
//            builder.setPositiveButton("확인") { dialogInterface, i ->
//                val alert = dialogInterface as AlertDialog
//                val idx = alert.listView.checkedItemPosition
//
//                when (idx) {
//                    0 -> FshrSearchObject.srfwtrLcZoneAt = "Y"
//                    1 -> FshrSearchObject.srfwtrLcZoneAt = "N"
//                }
//                srfwtrLcZoneAt.text = fyhtsData[idx]
//            }
//            builder.show()
//        }


        //휴, 실직자 레이아웃 추가
        addFyhtsThingBtn.setOnClickListener {
            val fyhtsViewGroup = fyhtsBaseViewGroup
            val addThingView = R.layout.fragment_minrgt_add_layout
            val inflater: LayoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(addThingView, null)
            val itemView = inflater.inflate(addThingView, null)
            fyhtsViewGroup?.addView(itemView)

            // 첫번째 spinner
            val addLayoutItem = fyhtsViewGroup.getChildAt(addViewCnt) as ViewGroup
            val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
            val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
            val selectLayout1 = addLayoutItem.getChildAt(4) as ViewGroup
            val selectLayout2 = addLayoutItem.getChildAt(5) as ViewGroup
            val selectLayout3 = addLayoutItem.getChildAt(6) as ViewGroup
            val selectLayout4 = addLayoutItem.getChildAt(7) as ViewGroup
            val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner
            wtnncUtill.wtnncSpinnerAdapter(R.array.fyhtsSclasArray, addSpinner1, this)
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
            // A009
//            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)
            wtnncUtill.wtnncSpinnerAdapter("A009", addSpinner2, this)

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

            addViewCnt++
        }

        // 허가등자료
        commPrmisnDtaBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val fyhtsAtchSelectArray = JSONArray()

            array.add("허가등자료 등록")
            if(ThingFyhtsObject.thingNewSearch.equals("N")) {
                for(i in 0 until fyhtsAtchInfo!!.length()) {
                    val fyhtsAtchItem = fyhtsAtchInfo!!.getJSONObject(i)
                    if(fyhtsAtchItem.getString("fileseInfo").equals("A200006018")) {
                        array.add(fyhtsAtchItem.getString("rgsde"))
                        fyhtsAtchSelectArray!!.put(fyhtsAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("허가등자료 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006018", "허가등자료")
                        } else {
                            val item = fyhtsAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 어업권 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
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
            BizEnum.FYHTS,
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
                        toast.msg_error(R.string.msg_server_connected_fail, 100)
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

        var requireArr = mutableListOf<TextView>(view.tv_fyhts_require1, view.tv_fyhts_require2, view.tv_fyhts_require3, view.tv_fyhts_require4, view.tv_fyhts_require5,view.tv_fyhts_require6)

        setRequireContent(requireArr)

        // A009
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, fyhtsUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter("A009", fyhtsUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, fyhtsOwnerCnfirmBasisSpinner, this) // 소유자확인근거
        wtnncUtill.wtnncSpinnerAdapter(R.array.fyhtsBsnClArray, bsnClDivSpinner, this) // 어업손실분류
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, fyhtsAcqsSeSpinner, this) //
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, fyhtsInclsSeSpinner, this) //
        wtnncUtill.wtnncSpinnerAdapter(R.array.lcnsClArray, lcnsClSpinner, this) //

        val dataString = requireActivity().intent!!.extras!!.get("FyhtsInfo") as String

        view.landSearchLocationText.setText(ThingFyhtsObject.legaldongNm)

        if(dataString.equals("")) {
//            view.landSearchLocationText.setText(ThingFyhtsObject.legaldongNm)
            view.fyhtsWtnCodeText.setText("자동기입")
            view.fyhtsThingKnd.setText("어업권보상")
            view.fyhtsStrctNdStrndrd.setText("자동기입")

            view.fyhtsOwnerCnfirmBasisSpinner.setSelection(4)
            view.rwTrgetAtChk.isChecked = true
            view.apasmtTrgetAtChk.isChecked = true
        } else {
            // 데이터가 있을때 화면

            var dataJson = JSONObject(dataString)

            var fyhtsDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

            dcsnAt = checkStringNull(fyhtsDataJson.getString("dcsnAt"))

            view.thingdcsnAtText.text = dcsnAt

            view.fyhtsWtnCodeText.setText(checkStringNull(fyhtsDataJson.getString("fyhtsWtnCode")))
            view.fyhtsThingKnd.setText(checkStringNull(fyhtsDataJson.getString("thingKnd")))
            view.fyhtsStrctNdStrndrd.setText(checkStringNull(fyhtsDataJson.getString("strctNdStndrd")))

            view.fyhtsBgnnAr.setText(checkStringNull(fyhtsDataJson.getString("bgnnAr")))
            view.fyhtsIncrprAr.setText(checkStringNull(fyhtsDataJson.getString("incrprAr")))

            val unitClString = checkStringNull(fyhtsDataJson.getString("unitCl"))
//            if(unitClString.equals("")) {
//                view.fyhtsUnitSpinner.setSelection(0)
//            } else {
//                val unitClStringSub = unitClString.substring(5,7)
//                view.fyhtsUnitSpinner.setSelection(Integer.valueOf(unitClStringSub))
//            }
            view.fyhtsUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", unitClString) )

            view.fyhtsArComputBasis.setText(checkStringNull(fyhtsDataJson.getString("arComputBasis")))

            val bsnClString = checkStringNull(fyhtsDataJson.getString("bsnCl"))
            if(bsnClString.equals("")) {
                view.bsnClDivSpinner.setSelection(0)
            } else {
                val bsnClStringSub = bsnClString.substring(5,7)
                view.bsnClDivSpinner.setSelection(Integer.valueOf(bsnClStringSub))
            }
            view.bssMthCoText.setText(checkStringNull(fyhtsDataJson.getString("sssMthCo")))

            //어업권 설정
            view.administGrc.setText(checkStringNull(fyhtsDataJson.getString("administGrc")))

            val lcnsClString = checkStringNull(fyhtsDataJson.getString("lcnsCl"))
            if(lcnsClString.equals("")) {
                view.lcnsClSpinner.setSelection(0)
            } else {
                val lcnsClStringsub = lcnsClString.substring(5,7)
                view.lcnsClSpinner.setSelection(Integer.valueOf(lcnsClStringsub))
            }

            view.lcnsKnd.setText(checkStringNull(fyhtsDataJson.getString("lcnsKnd")))
            view.lcnsNo.setText(checkStringNull(fyhtsDataJson.getString("lcnsNo")))
            val lcnsDeString = checkStringNull(fyhtsDataJson.getString("lcnsDe"))
            val lcnsDeStringSplit = lcnsDeString.split(" ")
            view.lcnsDe.setText(lcnsDeStringSplit[0])

            val srfwtrLcZoneAtString = checkStringNull(fyhtsDataJson.getString("srfwtrLcZoneAt"))
            if(srfwtrLcZoneAtString.equals("Y")) {
                view.srfwtrLcZoneAt.isChecked = true
            } else {
                view.srfwtrLcZoneAt.isChecked = false
            }

            val cntnncPdBgndeString = checkStringNull(fyhtsDataJson.getString("cntnncPdBgnde"))
            val cntnncPdBgndeSplit = cntnncPdBgndeString.split(" ")

            view.fyhtsCntnncPdBgnde.setText(cntnncPdBgndeSplit[0])

            val cntnncPdEnddeString = checkStringNull(fyhtsDataJson.getString("cntnncPdBgnde"))
            val cntnncPdEnddeSplit = cntnncPdEnddeString.split(" ")
            view.fyhtsCntnncPdEndde.setText(cntnncPdEnddeSplit[0])

            view.fshlLc.setText(checkStringNull(fyhtsDataJson.getString("fshlLc")))
            view.fyhtsAr.setText(checkStringNull(fyhtsDataJson.getString("fyhtsAr")))
            view.fshrMth.setText(checkStringNull(fyhtsDataJson.getString("fshrMth")))

            val ownerCnfirmBasisString = checkStringNull(fyhtsDataJson.getString("ownerCnfirmBasisCl"))
            if(ownerCnfirmBasisString.equals("")) {
                view.fyhtsOwnerCnfirmBasisSpinner.setSelection(4)
            } else {
                val ownerCnfirmBasisStringsub = ownerCnfirmBasisString.substring(5,7)
                view.fyhtsOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisStringsub))
            }

//            val acqsClString = checkStringNull(fyhtsDataJson.getString("acqsCl"))
//            if(acqsClString.equals("")) {
//                view.fyhtsAcqsSeSpinner.setSelection(0)
//            } else {
//                val acqsClStringsub = acqsClString.substring(5,7)
//                view.fyhtsAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringsub))
//            }
//
//            val inclsClString = checkStringNull(fyhtsDataJson.getString("inclsCl"))
//            if(inclsClString.equals("")) {
//                view.fyhtsInclsSeSpinner.setSelection(0)
//            } else {
//                val inclsClStringsub = inclsClString.substring(5,7)
//                view.fyhtsInclsSeSpinner.setSelection(Integer.valueOf(inclsClStringsub))
//            }

            val rwTrgetAtString = checkStringNull(fyhtsDataJson.getString("rwTrgetAt"))
            if(rwTrgetAtString.equals("")) {
                view.rwTrgetAtChk.isChecked = true
            } else {
                view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
            }

            val apasmtTrgetAtString = checkStringNull(fyhtsDataJson.getString("apasmtTrgetAt"))
            if(apasmtTrgetAtString.equals("")) {
                view.apasmtTrgetAtChk.isChecked = true
            } else {
                view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")
            }

            view.includePaclrMatterEdit.setText(checkStringNull(fyhtsDataJson.getString("paclrMatter")))
            view.includeReferMatterEdit.setText(checkStringNull(fyhtsDataJson.getString("referMatter")))
            view.includeRmEdit.setText(checkStringNull(fyhtsDataJson.getString("rm")))

            val fyhtsSubThingData = dataJson.getJSONArray("fyhtsSubThing")
            if(fyhtsSubThingData != null) {
                for(i in 0 until fyhtsSubThingData.length()) {
                    val fyhtsSubThingItem = fyhtsSubThingData.getJSONObject(i)

                    addViewCnt = i

                    val fyhtsViewGroup = fyhtsBaseViewGroup
                    val addThingView = R.layout.fragment_minrgt_add_layout
                    val inflater: LayoutInflater =
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    inflater.inflate(addThingView, null)
                    val itemView = inflater.inflate(addThingView, null)
                    fyhtsViewGroup?.addView(itemView)

                    val addLayoutItemGroup = fyhtsViewGroup.getChildAt(addViewCnt) as ViewGroup
                    val addLayoutFirst = addLayoutItemGroup.getChildAt(1) as ViewGroup
                    val thingSmallClView = addLayoutFirst.getChildAt(0) as ViewGroup
                    val subThignSmallClSpinner = thingSmallClView.getChildAt(0) as Spinner
                    wtnncUtill.wtnncSpinnerAdapter(R.array.fyhtsSclasArray, subThignSmallClSpinner, this)

                    val subThingThingKndText = addLayoutFirst.getChildAt(1) as EditText
                    val subThingStrctNdStndrd = addLayoutFirst.getChildAt(2) as EditText

                    val subThingSmallClString = checkStringNull(fyhtsSubThingItem.getString("thingSmallCl"))
                    if(subThingSmallClString.equals("")) {
                        subThignSmallClSpinner.setSelection(0)
                    } else if(subThingSmallClString.equals("A016032")){
                        subThignSmallClSpinner.setSelection(1)
                    }

                    subThingThingKndText.setText(checkStringNull(fyhtsSubThingItem.getString("thingKnd")))
                    subThingStrctNdStndrd.setText(checkStringNull(fyhtsSubThingItem.getString("strctNdStndrd")))

                    val addLayoutSecond = addLayoutItemGroup.getChildAt(3) as ViewGroup
                    val subThingArView = addLayoutSecond.getChildAt(0) as ViewGroup
                    val subThingBgnnArText = subThingArView.getChildAt(0) as EditText
                    val subThingIncrprArText = subThingArView.getChildAt(1) as EditText
                    val subThingUnitClView = addLayoutSecond.getChildAt(1) as ViewGroup
                    val subThingUnitClSpinner = subThingUnitClView.getChildAt(0) as Spinner
                    val subThingArComputBasisText = addLayoutSecond.getChildAt(2) as EditText
                    // A009
//                    wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, subThingUnitClSpinner, this)
                    wtnncUtill.wtnncSpinnerAdapter("A009", subThingUnitClSpinner, this)

                    subThingBgnnArText.setText(checkStringNull(fyhtsSubThingItem.getString("bgnnAr")))
                    subThingIncrprArText.setText(checkStringNull(fyhtsSubThingItem.getString("incrprAr")))

                    val subThingUnitClString = checkStringNull(fyhtsSubThingItem.getString("unitCl"))
//                    if(subThingUnitClString.equals("")) {
//                        subThingUnitClSpinner.setSelection(0)
//                    } else {
//                        val subThingUnitClStringsub = subThingUnitClString.substring(5,7)
//                        subThingUnitClSpinner.setSelection(Integer.valueOf(subThingUnitClStringsub))
//                    }
                    subThingUnitClSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", subThingUnitClString) )

                    subThingArComputBasisText.setText(checkStringNull(fyhtsSubThingItem.getString("arComputBasis")))

                    addViewCnt++


                    if(dcsnAt == "Y") {
                        subThignSmallClSpinner.isEnabled = false
                        subThingThingKndText.isEnabled = false
                        subThingStrctNdStndrd.isEnabled = false
                        subThingBgnnArText.isEnabled = false
                        subThingIncrprArText.isEnabled = false
                        subThingUnitClSpinner.isEnabled = false
                        subThingArComputBasisText.isEnabled = false
                    }
                }
            }

            fyhtsAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until fyhtsAtchInfo!!.length()) {
                val fyhstAtchItem = fyhtsAtchInfo!!.getJSONObject(i)

                val fyhstAtchFileInfo = fyhstAtchItem!!.getString("fileseInfo")

                // 허가등자료
                view.commPrmisnDtaBtn.backgroundTintList = when (fyhstAtchFileInfo) {
                    "A200006018" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }

            }
            settingSearchCamerasView(fyhtsAtchInfo)




            if(dcsnAt == "Y") {
                toast.msg_info(R.string.searchDcsnAtThing, 1000)

                view.fyhtsBgnnAr.isEnabled = false
                view.fyhtsIncrprAr.isEnabled = false
                view.fyhtsUnitSpinner.isEnabled = false
                view.fyhtsArComputBasis.isEnabled = false

                view.bsnClDivSpinner.isEnabled = false
                view.bssMthCoText.isEnabled = false

                view.administGrc.isEnabled = false
                view.lcnsClSpinner.isEnabled = false

                view.lcnsKnd.isEnabled = false
                view.lcnsNo.isEnabled = false

                view.srfwtrLcZoneAt.isEnabled = false
                view.fyhtsCntnncPdBgnde.isEnabled = false
                view.fyhtsCntnncPdEndde.isEnabled = false
                view.fshlLc.isEnabled = false
                view.fyhtsAr.isEnabled = false

                view.fshrMth.isEnabled = false

                view.fyhtsOwnerCnfirmBasisSpinner.isEnabled = false
                view.rwTrgetAtChk.isEnabled = false
                view.apasmtTrgetAtChk.isEnabled = false

            }


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

        if(ThingFyhtsObject.thingNewSearch.equals("N")) {
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
                                    toast.msg_error(R.string.msg_server_connected_fail, 100)
                                }

                                override fun onResponse(call: Call, response: Response) {

                                    val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                                    val downloadFile = File("$downloadDirectory/${item.getString("atfl")}")

                                    FileUtil.createDir(downloadDirectory)

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

    fun fyhtsSpinnerAdapter(stringArray: Int, spinner: Spinner) {
        ArrayAdapter.createFromResource(
            requireContext(),
            stringArray,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

//        when (parent?.id) {
//            R.id.fyhtsSpinner01 -> {
//                when (position) {
//                    0 -> {
//                        fyhtsClvtSe01.visibleView()
//                        fyhtsClvtSe02.goneView()
//                    }
//                    1 -> {
//                        fyhtsClvtSe01.goneView()
//                        fyhtsClvtSe02.visibleView()
//                    }
//                }
//            }
//        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addFyhtsThing() {

        // 대분류 입력
        ThingFyhtsObject.thingLrgeCl = "A011005" // 영업
        Log.d("fyhtsTest", "대분류 : ${ThingFyhtsObject.thingLrgeCl}")

        //소분류 입력
        ThingFyhtsObject.thingSmallCl = "A016030" // 어업
        Log.d("fyhtsTest", "소분류 : ${ThingFyhtsObject.thingSmallCl}")


        ThingFyhtsObject.thingKnd = mActivity.fyhtsThingKnd.text.toString()

        ThingFyhtsObject.bgnnAr = mActivity.fyhtsBgnnAr.text.toString() // 전체면적
        Log.d("fyhtsTest", "전체면적 : ${ThingFyhtsObject.bgnnAr}")

        ThingFyhtsObject.incrprAr = mActivity.fyhtsIncrprAr.text.toString() // 편입면적
        Log.d("fyhtsTest", "편입면적 : ${ThingFyhtsObject.incrprAr}")

        ThingFyhtsObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.fyhtsUnitSpinner.selectedItemPosition)
//            when (mActivity.fyhtsUnitSpinner.selectedItemPosition) { // 단위
//            1 -> "A009001"
//            2 -> "A009002"
//            3 -> "A009003"
//            4 -> "A009004"
//            5 -> "A009005"
//            6 -> "A009006"
//            7 -> "A009007"
//            8 -> "A009008"
//            9 -> "A009009"
//            10 -> "A009010"
//            11 -> "A009011"
//            12 -> "A009012"
//            13 -> "A009013"
//            14 -> "A009014"
//            15 -> "A009015"
//            16 -> "A009016"
//            17 -> "A009017"
//            18 -> "A009018"
//            19 -> "A009019"
//            20 -> "A009020"
//            21 -> "A009021"
//            22 -> "A009022"
//            23 -> "A009023"
//            24 -> "A009024"
//            25 -> "A009025"
//            26 -> "A009026"
//            27 -> "A009027"
//            28 -> "A009028"
//            29 -> "A009029"
//            30 -> "A009030"
//            31 -> "A009031"
//            32 -> "A009032"
//            33 -> "A009033"
//            34 -> "A009034"
//            35 -> "A009035"
//            36 -> "A009036"
//            37 -> "A009037"
//            38 -> "A009038"
//            39 -> "A009039"
//            40 -> "A009040"
//            41 -> "A009041"
//            42 -> "A009042"
//            43 -> "A009043"
//            44 -> "A009044"
//            45 -> "A009045"
//            46 -> "A009046"
//            47 -> "A009047"
//            48 -> "A009048"
//            49 -> "A009049"
//            50 -> "A009050"
//            51 -> "A009051"
//            52 -> "A009052"
//            53 -> "A009053"
//            54 -> "A009054"
//            55 -> "A009055"
//            56 -> "A009056"
//            57 -> "A009057"
//            58 -> "A009058"
//            59 -> "A009059"
//            60 -> "A009060"
//            61 -> "A009061"
//            62 -> "A009062"
//            63 -> "A009063"
//            64 -> "A009064"
//            65 -> "A009065"
//            66 -> "A009066"
//            67 -> "A009067"
//            68 -> "A009068"
//            else -> ""
//        }
        Log.d("fyhtsTest", "단위 : ${ThingFyhtsObject.unitCl}")

        ThingFyhtsObject.bsnCl = when (mActivity.bsnClDivSpinner.selectedItemPosition) {
            1 -> "A135001"
            2 -> "A135002"
            3 -> "A135003"
            4 -> "A135004"
            else -> ""
        }
        ThingFyhtsObject.sssMthCo = mActivity.bssMthCoText.text.toString()

        ThingFyhtsObject.arComputBasis = mActivity.fyhtsArComputBasis.text.toString() // 면적산출근거
        Log.d("fyhtsTest", "면적산출근거 : ${ThingFyhtsObject.arComputBasis}")


        ThingFyhtsObject.administGrc = mActivity.administGrc.text.toString() // 행정관청
        Log.d("fyhtsTest", "행정관청 : ${ThingFyhtsObject.administGrc}")

        ThingFyhtsObject.lcnsCl = when (mActivity.lcnsClSpinner.selectedItemPosition) {
            1 -> "A137001"
            2 -> "A137002"
            3 -> "A137003"
            4 -> "A137004"
            else -> ""
        }

        ThingFyhtsObject.lcnsKnd = mActivity.lcnsKnd.text.toString() // 면허종류
        Log.d("fyhtsTest", "면허종류 : ${ThingFyhtsObject.lcnsKnd}")

        ThingFyhtsObject.lcnsNo = mActivity.lcnsNo.text.toString() // 면허번호
        Log.d("fyhtsTest", "면호번호 : ${ThingFyhtsObject.lcnsNo}")

        Log.d("fyhtsTest", "면허일자 : ${ThingFyhtsObject.lcnsDe}")

        Log.d("fyhtsTest", "존속기간시작 : ${ThingFyhtsObject.fyhtsCntnncPdBgnde}")

        Log.d("fyhtsTest", "존속기간끝 : ${ThingFyhtsObject.fyhtsCntnncPdEndde}")

        ThingFyhtsObject.fshlLc = mActivity.fshlLc.text.toString() // 어장의 위치
        Log.d("fyhtsTest", "어장의 위치 : ${ThingFyhtsObject.fshlLc}")

        ThingFyhtsObject.fyhtsAr = mActivity.fyhtsAr.text.toString() // 면적
        Log.d("fyhtsTest", "면적 : ${ThingFyhtsObject.fyhtsAr}")

        ThingFyhtsObject.fshrMth = mActivity.fshrMth.text.toString() // 어업의 방법
        Log.d("fyhtsTest", "어업의 방법 : ${ThingFyhtsObject.fshrMth}")

        when (mActivity.srfwtrLcZoneAt.isChecked) { // 수면의 위치 및 구역도 여부
            true -> ThingFyhtsObject.srfwtrLcZoneAt = "Y"
            else -> ThingFyhtsObject.srfwtrLcZoneAt = "N"
        }
        Log.d("fyhtsTest", "수면의 위치 및 구역도 여부 : ${ThingFyhtsObject.srfwtrLcZoneAt}")

        ThingFyhtsObject.ownerCnfirmBasisCl = when(mActivity.fyhtsOwnerCnfirmBasisSpinner.selectedItemPosition) {
            1 -> "A035001"
            2 -> "A035002"
            3 -> "A035003"
            4 -> "A035004"
            5 -> "A035005"
            else -> ""
        }
        ThingFyhtsObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }

        ThingFyhtsObject.apasmtTrgetAt = when (mActivity.apasmtTrgetAtChk.isChecked) {
            true ->"Y"
            else ->"N"
        }

//        ThingFyhtsObject.acqsCl = when(mActivity.fyhtsAcqsSeSpinner.selectedItemPosition) {
//            1->"A025001"
//            2->"A025002"
//            3->"A025003"
//            4->"A025004"
//            5->"A025005"
//            6->"A025006"
//            7->"A025007"
//            8->"A025008"
//            9->"A025009"
//            10->"A025010"
//            11->"A025011"
//            12->"A025012"
//            else ->""
//        }
//        ThingFyhtsObject.inclsCl = when(mActivity.fyhtsInclsSeSpinner.selectedItemPosition) {
//            1->"A007001"
//            2->"A007002"
//            3->"A007003"
//            4->"A007004"
//            else->""
//        }

        ThingFyhtsObject.acqsCl = "A025001"
        ThingFyhtsObject.inclsCl = "A007001"

        ThingFyhtsObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString()

        ThingFyhtsObject.referMatter = mActivity.includeReferMatterEdit.text.toString()

        ThingFyhtsObject.rm = mActivity.includeRmEdit.text.toString()



        //구조 및 규격
        ThingFyhtsObject.strctNdStndrd = StringBuilder().apply {
//            append("관청(" + ThingFyhtsObject.administGrc + "), ") //행정관청
//            append("사업명(" + ThingFyhtsObject.lcnsKnd + "), ") //면허종류
//            append("사업번호(" + ThingFyhtsObject.lcnsNo + ")") //면허번호
            when (ThingFyhtsObject.bsnCl) {
                "A135001" -> {
                    append("취소보상(행정관청"+ThingFyhtsObject.administGrc +")")
                }
                "A135002" -> {
                    append("제한보상(행정관청"+ThingFyhtsObject.administGrc +"), ")
                }
                "A135003" -> {
                    append("정지보상(행정관청"+ThingFyhtsObject.administGrc +"), ")
                }
                "A135004" -> {
                    append("어업보상불가(행정관청"+ThingFyhtsObject.administGrc +"), ")
                }
            }
            append("면허내역(" + ThingFyhtsObject.lcnsKnd + ", " + ThingFyhtsObject.lcnsNo + "), ")

            append("어업의 방법(" + ThingFyhtsObject.fshrMth + ")")

        }.toString()
        Log.d("fyhtsTest", "구조 및 규격 : ${ThingFyhtsObject.strctNdStndrd}")

        var itemCnt = mActivity.fyhtsBaseViewGroup.childCount
//        var fyhtsAddItemArray: MutableList<MutableMap<String, String>> = mutableListOf()


        // 물건 추가 부분
        var fyhtsThingArray = JSONArray()
        var fyhtsThingJSON = JSONObject()
        if(itemCnt > 0) {
            for (i in 0 until itemCnt) {

                Log.d("fyhtsTest", "******************************************************************")

                //            val fyhtsAddItemMap: MutableMap<String, String> = hashMapOf()
                val fyhtsViewGroup = mActivity.fyhtsBaseViewGroup // 물건 추가 Base ViewGroup
                val addLayoutItem = fyhtsViewGroup.getChildAt(i) as ViewGroup // i번째 추가 부분
                val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
                val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup
                val addViewGroup3 = addLayoutItem.getChildAt(5) as ViewGroup
                val addViewGroup4 = addLayoutItem.getChildAt(7) as ViewGroup

                // 소분류
                val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
                val addSmallClSpinner = addSpinnerLayout1.getChildAt(0) as Spinner

                var fyhtsThingItem = JSONObject()

                when (addSmallClSpinner.selectedItemPosition) {

                    1 -> { // 시설물
                        fyhtsThingItem.put("thingSmallCl", "A016032")
                    }
//                    2 -> { // 실직자
////                        fyhtsAddItemMap["thingSmallClValue"] = "A016034"
//                        fyhtsThingItem.put("thingSmallCl","A016034")
////                        Log.d("fyhtsTest", "소분류 : ${fyhtsAddItemMap["thingSmallClValue"]}")
//
//                        // 구분
//                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
////                        fyhtsAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
//                        fyhtsThingItem.put("unemplTy", addUnemplTy.text.toString())
////                        Log.d("fyhtsTest", "구분 : ${fyhtsAddItemMap["bsnUnemplTy"]}")
//
//                        // 일수
//                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
////                        fyhtsAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        fyhtsThingItem.put("unemplCo",addUnemplCo.text.toString())
////                        Log.d("fyhtsTest", "일수 : ${fyhtsAddItemMap["bsnUnemplCo"]}")
//
//                        // 평균임금
//                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
////                        fyhtsAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        fyhtsThingItem.put("avrgWage",addAvrgWage.text.toString())
////                        Log.d("fyhtsTest", "평균임금 : ${fyhtsAddItemMap["bsnAvrgWage"]}")
//
//                        // 통상임금
//                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
////                        fyhtsAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        fyhtsThingItem.put("odygs",addOdygs.text.toString())
////                        Log.d("fyhtsTest", "통상임금 : ${fyhtsAddItemMap["bsnOdygs"]}")
//
//                        //  근로장소이전일자
//                        val addBeforeDe = addViewGroup4.getChildAt(0) as TextView
////                        fyhtsAddItemMap["bsnBeforeDe"] = addBeforeDe.text.toString()
//                        fyhtsThingItem.put("beforeDe",addBeforeDe.text.toString())
////                        Log.d("fyhtsTest", "근로장소이전일자 : ${fyhtsAddItemMap["bsnBeforeDe"]}")
//
//                        // 실직일자
//                        val addUnemplDe = addViewGroup4.getChildAt(1) as TextView
////                        fyhtsAddItemMap["bsnUnemplDe"] = addUnemplDe.text.toString()
//                        fyhtsThingItem.put("unemplDe",addUnemplDe.text.toString())
////                        Log.d("fyhtsTest", "실적일자 : ${fyhtsAddItemMap["bsnUnemplDe"]}")
//
//                        // 실직사유
//                        val addUnemplResn = addViewGroup4.getChildAt(2) as EditText
////                        fyhtsAddItemMap["bsnUnemplResn"] = addUnemplResn.text.toString()
//                        fyhtsThingItem.put("unemplResn", addUnemplResn.text.toString())
////                        Log.d("fyhtsTest", "실적사유 : ${fyhtsAddItemMap["bsnUnemplResn"]}")
//                    }
//                    3 -> { // 휴직자
////                        fyhtsAddItemMap["thingSmallClValue"] = "A016033"
//                        fyhtsThingItem.put("thingSmallCl","A016033")
////                        Log.d("fyhtsTest", "휴직자 : ${fyhtsAddItemMap["thingSmallClValue"]}")
//
//                        // 구분
//                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
////                        fyhtsAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
//                        fyhtsThingItem.put("unemplTy", addUnemplTy.text.toString())
////                        Log.d("fyhtsTest", "구분 : ${fyhtsAddItemMap["bsnUnemplTy"]}")
//
//                        // 일수
//                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
////                        fyhtsAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        fyhtsThingItem.put("unemplCo", addUnemplCo.text.toString())
////                        Log.d("fyhtsTest", "일수 : ${fyhtsAddItemMap["bsnUnemplCo"]}")
//
//                        // 평균임금
//                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
////                        fyhtsAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        fyhtsThingItem.put("avrgWage", addAvrgWage.text.toString())
////                        Log.d("fyhtsTest", "평균임금 : ${fyhtsAddItemMap["bsnAvrgWage"]}")
//
//                        // 통상임금
//                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
////                        fyhtsAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        fyhtsThingItem.put("bsnOdygs", addOdygs.text.toString())
////                        Log.d("fyhtsTest", "통상임금 : ${fyhtsAddItemMap["bsnOdygs"]}")
//                    }
                }

                // 물건의 종류
                val addThingKndText = addViewGroup1.getChildAt(1) as EditText
                fyhtsThingItem.put("thingKnd", addThingKndText.text.toString())

                // 구조 및 규격
                val addStrctNdStrndrdText = addViewGroup1.getChildAt(2) as EditText
                fyhtsThingItem.put("strctNdStndrd",addStrctNdStrndrdText.text.toString())

                // 전체면적
                val addViewGroupLayout = addViewGroup2.getChildAt(0) as ViewGroup
                val addBgnnArText = addViewGroupLayout.getChildAt(0) as EditText
                fyhtsThingItem.put("bgnnAr",addBgnnArText.text.toString())

                // 편입면적
                val addIncrprArText = addViewGroupLayout.getChildAt(1) as EditText
                fyhtsThingItem.put("incrprAr", addIncrprArText.text.toString())

                // 단위
                val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup
                val addUnitClSpinner = addSpinnerLayout2.getChildAt(0) as Spinner
                fyhtsThingItem.put("unitCl", CommonCodeInfoList.getCodeId("A009", addUnitClSpinner.selectedItemPosition))
//                    when (addUnitClSpinner.selectedItemPosition) {
//                    1 -> "A009001"
//                    2 -> "A009002"
//                    3 -> "A009003"
//                    4 -> "A009004"
//                    5 -> "A009005"
//                    6 -> "A009006"
//                    7 -> "A009007"
//                    8 -> "A009008"
//                    9 -> "A009009"
//                    10 -> "A009010"
//                    11 -> "A009011"
//                    12 -> "A009012"
//                    13 -> "A009013"
//                    14 -> "A009014"
//                    15 -> "A009015"
//                    16 -> "A009016"
//                    17 -> "A009017"
//                    18 -> "A009018"
//                    19 -> "A009019"
//                    20 -> "A009020"
//                    21 -> "A009021"
//                    22 -> "A009022"
//                    23 -> "A009023"
//                    24 -> "A009024"
//                    25 -> "A009025"
//                    26 -> "A009026"
//                    27 -> "A009027"
//                    28 -> "A009028"
//                    29 -> "A009029"
//                    30 -> "A009030"
//                    31 -> "A009031"
//                    32 -> "A009032"
//                    33 -> "A009033"
//                    34 -> "A009034"
//                    35 -> "A009035"
//                    36 -> "A009036"
//                    37 -> "A009037"
//                    38 -> "A009038"
//                    39 -> "A009039"
//                    40 -> "A009040"
//                    41 -> "A009041"
//                    42 -> "A009042"
//                    43 -> "A009043"
//                    44 -> "A009044"
//                    45 -> "A009045"
//                    46 -> "A009046"
//                    47 -> "A009047"
//                    48 -> "A009048"
//                    49 -> "A009049"
//                    50 -> "A009050"
//                    51 -> "A009051"
//                    52 -> "A009052"
//                    53 -> "A009053"
//                    54 -> "A009054"
//                    55 -> "A009055"
//                    56 -> "A009056"
//                    57 -> "A009057"
//                    58 -> "A009058"
//                    59 -> "A009059"
//                    60 -> "A009060"
//                    61 -> "A009061"
//                    62 -> "A009062"
//                    63 -> "A009063"
//                    64 -> "A009064"
//                    65 -> "A009065"
//                    66 -> "A009066"
//                    67 -> "A009067"
//                    68 -> "A009068"
//                    else -> ""
//                })
                // 면적산출근거
                val addArComputBasisText = addViewGroup2.getChildAt(2) as EditText
                fyhtsThingItem.put("arComputBasis", addArComputBasisText.text.toString())

                fyhtsThingArray.put(fyhtsThingItem)
                Log.d("fyhtsTest", "******************************************************************")
            }
            fyhtsThingJSON.put("fyhtsThing", fyhtsThingArray)
        } else {
            fyhtsThingJSON.put("fyhtsThing", fyhtsThingArray)
        }

        ThingFyhtsObject.addFyhtsThing = fyhtsThingJSON
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {

    }

}