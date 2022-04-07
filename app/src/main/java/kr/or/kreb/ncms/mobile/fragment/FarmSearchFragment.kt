/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

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
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_farm_search.*
import kotlinx.android.synthetic.main.fragment_farm_search.view.*
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
import kr.or.kreb.ncms.mobile.data.ThingFarmObject
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

class FarmSearchFragment(activity: Activity, context: Context, val fragmentActivity: FragmentActivity) : BaseFragment(),
    AdapterView.OnItemSelectedListener,
    DialogUtil.ClickListener{

    private val mActivity = activity
    private val mContext = context
    private lateinit var farmTypeView: View
    private val wtnncUtill: WtnncUtil = WtnncUtil(activity, context)
    private var addClvtViewCnt: Int = 0 // 추가 경작 근거
    private var addThingViewCnt: Int = 0 // 추가 시설물

//    private var logUtil: LogUtil = LogUtil("FarmSearchFragment");
//    private var toastUtil: ToastUtil = ToastUtil(mContext)

//    var dialogUtil: DialogUtil? = null
//    private var progressDialog: AlertDialog? = null
//    var builder: MaterialAlertDialogBuilder? = null

    private var farmAtchInfo: JSONArray? = null


    val wonFormat = DecimalFormat("#,###")

    lateinit var materialDialog: Dialog

    var wtnncImageAdapter: WtnncImageAdapter? = null

    init { }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        farmTypeView = inflater.inflate(R.layout.fragment_farm_search, null)
        dialogUtil = DialogUtil(context, activity)
        dialogUtil!!.setClickListener(this)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        return farmTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init(view)

        //사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.FARM,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

        farmBgnnAr.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.bgnnAr = txtString
            }
            false
        }
        farmIncrprAr.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.incrprAr = txtString
            }
            false
        }
        farmArComputBasis.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.arComputBasis = txtString
            }
            false
        }
//        frldbsDbtamtRepName.setOnEditorActionListener { textView, action, event ->
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//
//                ThingFarmObject.frldbsDbtamtRepName = txtString
//            }
//            false
//        }
        posesnRentName.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnRentName = txtString
            }
            false
        }
        posesnHireName.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnHireName = txtString
            }
            false
        }
        posesnGtn.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnGtn = txtString
            }
            false
        }
        posesnMtht.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnMtht = txtString
            }
            false
        }
        posesnCntrctLc.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnCntrctLc = txtString
            }
            false
        }
        posesnCntrctAr.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnCntrctAr = txtString
            }
            false
        }
        posesnSpccntr.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.getText().toString()

                ThingFarmObject.posesnSpccntr = txtString
            }
            false
        }


            // 임차기간
        posesnRentBgnde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                it as TextView,
                "posesnRentBgnde"
            )
        }
        posesnRentEndde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                it as TextView,
                "posesnRentEndde"
            )
        }
//        farmLesseePc.setOnClickListener {
//            wtnncUtill.wtnncDateRangePicker(
//                requireActivity().supportFragmentManager,
//                it as TextView,
//                "farmLesseePc"
//            )
//        }

        // 경작기간
//        farmClvtPd.setOnClickListener {
//            wtnncUtill.wtnncDateRangePicker(requireActivity().supportFragmentManager, it as TextView, "farmClvtPd")
//        }

        // 직불금 수령년도
//        frldbsDbtamtYear.setOnClickListener {
//            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, null)
//        }

        // 적용된 소득자료집 발간 년도
//        incomePblctYear.setOnClickListener {
//            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, null)
//        }

        // 경작내역, 농작물 실제소득 근거 추가
        farmAddClvtDtlsBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, this)
        }

        // 시설물 추가
        farmAddThingLayoutBtn.setOnClickListener {

            val farmViewGroup = farmBaseViewGroup // 시설물이 추가되는 부분
            var addThingView = R.layout.fragment_farm_add_thing_item // 시설물 추가 Layout
            val inflater: LayoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            inflater.inflate(addThingView, null)
            val itemView = inflater.inflate(addThingView, null)
            farmViewGroup?.addView(itemView)

            // 첫번째 spinner
            val addLayoutItem = farmViewGroup.getChildAt(addThingViewCnt) as ViewGroup //추가되는 layout의 전체 ViewGroup
            val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup // 2번째 라인
            val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup // 2번째 라인의 첫번째 항목
            val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner // 소분류 Spinner
            wtnncUtill.wtnncSpinnerAdapter(R.array.farmSmallCategorySubArray, addSpinner1, this)

            // 두번째 spinner
            val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup // 4번째 라인
            val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup // 4번째 라인의 첫번째 항목
            val addSpinner2 = addSpinnerLayout2.getChildAt(0) as Spinner // 단위
            // A009
//            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)
            wtnncUtill.wtnncSpinnerAdapter("A009", addSpinner2, this)

            addThingViewCnt++
        }

        // 농지원부
        frlndLedgerBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("농지원부 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006009")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("농지원부 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006009", "농지원부")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 농업인확인서
        farmerCnfirmBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("농업인확인서 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006023")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("농업인확인서 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006023", "농업인확인서")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 농어업경영체등록확인서
        fngmtRegistCnfirmBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("농어업경영체등록확인서 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006024")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("농어업경영체등록확인서 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006024", "농어업경영체등록확인서")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 직불금 조회
        dirctCashInqireBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("직불금 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006021")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("직불금 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006021", "직불금")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 주민등록초본
        commRsgstAbstrctBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("주민등록초본 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006007")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("주민등록초본 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006007", "주민등록초본")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 임대차계약서
        commLsCtrtcBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("임대차계약서 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006019")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("임대차계약서 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006019", "임대차계약서")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }
        // 경작사실확인서서
        clvtFactCnfirm.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("경작사실확인서서 등록")
            if(ThingFarmObject.thingNewSearch.equals("N")) {
                for(i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006022")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("경작사실확인서서 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006022", "경작사실확인서서")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 농업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }
        }

    }

    fun callThingCapture(fileCode: String, fileCodeNm: String) {
        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.FARM,
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

        var requireArr = mutableListOf<TextView>(view.tv_farm_require1)
        setRequireContent(requireArr)

        val dataString = requireActivity().intent!!.extras!!.get("FarmInfo") as String

        var dataJson = JSONObject(dataString)

        var farmDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject
        logUtil.d("farmInfo dataJson ----------------------------> ${farmDataJson.toString()}")

        ThingFarmObject.thingInfo = farmDataJson


//        wtnncUtill.wtnncSpinnerAdapter(R.array.farmSmallCategoryArray, farmSclasSpinner, this) // 소분류
        // A009
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, farmUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter("A009", farmUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmFrlndBasisArray, frldbsBasisCl, this) // 농지근거
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmFarmerBasisArray, frmrbsBasisCl, this) // 농민근거
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmPrmisnSeArray, posesnClvthmTy, this) // 점유구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.frmrbsDbtamtAt, frmrbsDbtamtAtSp, this) // 점유구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnLadResideAt, posesnLadResideAtSp, this) // 점유구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnLadFarmerAt, posesnLadFarmerAtSp, this) // 점유구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnOwnerClvtCnfirmAt, posesnOwnerClvtCnfirmAtSp, this) // 점유구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnDbtamtRepAt, posesnDbtamtRepAtSp, this) // 점유구분
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, farmAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, farmInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, farmOwnerCnfirmBasisSpinner, this)


        //wtnncUtill.wtnncSpinnerAdapter(R.array.farmIncomeOriginSeArray, realIncomeOrgCl, this) // 실제소득출처 구분

        view.landSearchLocationText.setText(checkStringNull(farmDataJson.getString("legaldongNm")))
        view.landSearchBgnnLnmText.setText(checkStringNull(farmDataJson.getString("bgnnLnm")))
        view.landSearchincrprLnmText.setText(checkStringNull(farmDataJson.getString("incrprLnm")))
        view.landSearchNominationText.setText(checkStringNull(farmDataJson.getString("gobuLndcgrNm")))
        val relateLnmString = checkStringNull(farmDataJson.getString("relateLnm"))
        if(relateLnmString.equals("")) {
            view.landSearchRelatedLnmText.setText("없음")
        } else {
            view.landSearchRelatedLnmText.setText(relateLnmString)
        }

        view.landSearchBgnnArText.setText(checkStringNull(farmDataJson.getString("ladBgnnAr")))
        view.landSearchIncrprArText.setText(checkStringNull(farmDataJson.getString("ladIncrprAr")))
        view.landSearchOwnerText.setText(checkStringNull(farmDataJson.getString("landOwnerName")))
        view.landSearchOwnerRText.setText(checkStringNull(farmDataJson.getString("landRelatesName")))

        val farmWtnCodeString = farmDataJson.getInt("farmWtnCode")
        if(farmWtnCodeString == 0) {
            view.farmWtnCodeText.setText("자동기입")
        } else {
            view.farmWtnCodeText.setText(farmWtnCodeString.toString())
        }

        val framThingSmallCl = checkStringNull(farmDataJson.getString("thingSmallCl"))
        val farmThingSmallNm = checkStringNull(farmDataJson.getString("thingSmallNm"))
        if(farmThingSmallNm.equals("")) {
            view.farmSmallText.setText("농업보상")

        } else {
            view.farmSmallText.setText(farmThingSmallNm)
        }
        val farmThingKndString = checkStringNull(farmDataJson.getString("thingKnd"))
        if(farmThingKndString.equals("")) {
            view.farmThingKndText.setText("자동기입")

        } else {
            view.farmThingKndText.setText(farmThingKndString)
        }
        val farmStrctNdStrndeString = checkStringNull(farmDataJson.getString("strctNdStndrd"))
        if(farmStrctNdStrndeString.equals("")) {
            view.farmStrctNdStrndrdText.setText("자동기입")

        } else {
            view.farmStrctNdStrndrdText.setText(farmStrctNdStrndeString)
        }

        view.farmBgnnAr.setText(checkStringNull(farmDataJson.getString("bgnnAr")))
        view.farmIncrprAr.setText(checkStringNull(farmDataJson.getString("incrprAr")))

        val farmUnitClString = checkStringNull(farmDataJson.getString("unitCl"))
//        if(farmUnitClString.equals("")) {
//            view.farmUnitSpinner.setSelection(0)
//        } else {
//            val farmUnitClStringSub = farmUnitClString.substring(5,7)
//            view.farmUnitSpinner.setSelection(Integer.valueOf(farmUnitClStringSub))
//        }
        view.farmUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", farmUnitClString) )

        view.farmArComputBasis.setText(checkStringNull(farmDataJson.getString("arComputBasis")))

        /*
        농지의 적법성근거
         */
        val frldbsLgalAtString = checkStringNull(farmDataJson.getString("frldbsLgalAt"))
        if(frldbsLgalAtString.equals("Y")) {
            view.frldbsLgalAt.isChecked = true
        } else {
            view.frldbsLgalAt.isChecked = false;
        }

        val frldbsBasisClString = checkStringNull(farmDataJson.getString("frldbsBasisCl"))
        if(frldbsBasisClString.equals("")) {
            view.frldbsBasisCl.setSelection(0)
        } else {
            val frldbsBasisClStringSub = frldbsBasisClString.substring(5,7)
            view.frldbsBasisCl.setSelection(Integer.valueOf(frldbsBasisClStringSub))
        }

        val frldbsFrldLdgrAtString = checkStringNull(farmDataJson.getString("frldbsFrldLdgrAt"))
        if(frldbsFrldLdgrAtString.equals("Y")) {
            view.frldbsFrldLdgrAt.isChecked = true
        } else {
            view.frldbsFrldLdgrAt.isChecked = false
        }

        /*
        농민의 적법성 근거
         */
        val frmrbsLgalAtString = checkStringNull(farmDataJson.getString("frmrbsLgalAt"))
        if(frmrbsLgalAtString.equals("")) {
            view.frmrbsLgalAt.isChecked = false
        } else {
            view.frmrbsLgalAt.isChecked = true
        }

        val frmrbsBasisClString = checkStringNull(farmDataJson.getString("frmrbsBasisCl"))
        if(frmrbsBasisClString.equals("")) {
            view.frmrbsBasisCl.setSelection(0)
        } else {
            val frmrbsBasisClStringSub = frmrbsBasisClString.substring(5,7)
            view.frmrbsBasisCl.setSelection(Integer.valueOf(frmrbsBasisClStringSub))
        }

        val frmrbsCnfrmnDta1AtString = checkStringNull(farmDataJson.getString("frmrbsCnfrmnDta1At"))
        if(frmrbsCnfrmnDta1AtString.equals("Y")) {
            view.frmrbsCnfrmnDta1At.isChecked = true
        } else {
            view.frmrbsCnfrmnDta1At.isChecked = false
        }

        val frmrbsCnfrmnDta2AtString = checkStringNull(farmDataJson.getString("frmrbsCnfrmnDta2At"))
        if(frmrbsCnfrmnDta2AtString.equals("Y")) {
            view.frmrbsCnfrmnDta2At.isChecked = true
        } else {
            view.frmrbsCnfrmnDta2At.isChecked = false
        }

        val frmrbsDbtamtAtString = checkStringNull(farmDataJson.getString("frmrbsDbtamtAt"))
        when(frmrbsDbtamtAtString) {
            "Y" -> view.frmrbsDbtamtAtSp.setSelection(1)
            "N" -> view.frmrbsDbtamtAtSp.setSelection(2)
            "X" -> view.frmrbsDbtamtAtSp.setSelection(3)
            else -> view.frmrbsDbtamtAtSp.setSelection(0)

        }
//        if(frmrbsDbtamtAtString.equals("Y")) {
//            view.frmrbsDbtamtAt.isChecked = true
//        } else {
//            view.frmrbsDbtamtAt.isChecked = false
//        }

        val frmrbsAraResideAtString = checkStringNull(farmDataJson.getString("frmrbsAraResideAt"))
        if(frmrbsAraResideAtString.equals("Y")) {
            view.frmrbsAraResideAt.isChecked = true
        } else {
            view.frmrbsAraResideAt.isChecked = false
        }

        /*
        점유의 적법성 근거거
         */
        val posesnLgalAtString = checkStringNull(farmDataJson.getString("posesnLgalAt"))
        if(posesnLgalAtString.equals("Y")) {
            view.posesnLgalAt.isChecked = true
        } else {
            view.posesnLgalAt.isChecked = false
        }
        val posesnClvthmTyString = checkStringNull(farmDataJson.getString("posesnClvthmTy"))
        if(posesnClvthmTyString.equals("")) {
            view.posesnClvthmTy.setSelection(0)
        } else {
            when(posesnClvthmTyString) {
                "1" -> view.posesnClvthmTy.setSelection(1)
                "2" -> view.posesnClvthmTy.setSelection(2)
                else -> view.posesnClvthmTy.setSelection(0)
            }
        }

        val posesnLadResideAtString = checkStringNull(farmDataJson.getString("posesnLadResideAt"))
        view.posesnLadResideAtSp.setSelection(when (posesnLadResideAtString) {
            "Y" -> 1
            "N" -> 2
            "X" -> 3
            else -> 0
        })
//        if(posesnLadResideAtString.equals("Y")) {
//            view.posesnLadResideAt.isChecked = true
//        } else {
//            view.posesnLadResideAt.isChecked = false
//        }

        val posesnLadFarmerAtString = checkStringNull(farmDataJson.getString("posesnLadFarmerAt"))
        view.posesnLadFarmerAtSp.setSelection(when (posesnLadFarmerAtString) {
            "Y" -> 1
            "N" -> 2
            "X" -> 3
            else -> 0
        })
//        if(posesnLadFarmerAtString.equals("Y")) {
//            view.posesnLadFarmerAt.isChecked = true
//        } else {
//            view.posesnLadFarmerAt.isChecked = false
//        }

        val posesnDbtamtRepAtString = checkStringNull(farmDataJson.getString("posesnDbtamtRepAt"))
        view.posesnDbtamtRepAtSp.setSelection(when (posesnDbtamtRepAtString) {
            "Y" -> 1
            "N" -> 2
            "X" -> 3
            else -> 0
        })
//        if(posesnDbtamtRepAtString.equals("Y")) {
//            view.posesnDbtamtRepAt.isChecked = true
//        } else {
//            view.posesnDbtamtRepAt.isChecked = false
//        }

        view.posesnDbtamtRepInf.setText(checkStringNull(farmDataJson.getString("posesnDbtamtRepInf")))

        val posesnOwnerClvtCnfirmAtString = checkStringNull(farmDataJson.getString("posesnOwnerClvtCnfirmAt"))
        view.posesnOwnerClvtCnfirmAtSp.setSelection(when (posesnOwnerClvtCnfirmAtString) {
            "Y" -> 1
            "N" -> 2
            "X" -> 3
            else -> 0
        })
//        if(posesnOwnerClvtCnfirmAtString.equals("Y")) {
//            view.posesnOwnerClvtCnfirmAt.isChecked = true
//        } else {
//            view.posesnOwnerClvtCnfirmAt.isChecked = false
//        }

        val posesnLrcdocAtString = checkStringNull(farmDataJson.getString("posesnLrcdocAt"))
        if(posesnLrcdocAtString.equals("Y")) {
            view.posesnLrcdocAt.isChecked = true
        } else {
            view.posesnLrcdocAt.isChecked = false
        }

        view.posesnRentName.setText(checkStringNull(farmDataJson.getString("posesnRentName")))
        view.posesnHireName.setText(checkStringNull(farmDataJson.getString("posesnHireName")))

        view.posesnRentBgnde.setText(checkStringNull(farmDataJson.getString("posesnRentBgnde")))
        view.posesnRentBgnde.setText(checkStringNull(farmDataJson.getString("posesnRentEndde")))

//        view.farmLesseePc.setText(posesnRentBgndeString + "~" + posesnRentEnddeString)

        view.posesnGtn.setText(checkStringNull(farmDataJson.getString("posesnGtn")))
        view.posesnMtht.setText(checkStringNull(farmDataJson.getString("posesnMtht")))
        view.posesnCntrctLc.setText(checkStringNull(farmDataJson.getString("posesnCntrctLc")))
        view.posesnCntrctAr.setText(checkStringNull(farmDataJson.getString("posesnCntrctAr")))
        view.posesnSpccntr.setText(checkStringNull(farmDataJson.getString("posesnSpccntr")))



        val ownerCnfirmBasisClString = checkStringNull(farmDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmBasisClString.equals("")) {
            view.farmOwnerCnfirmBasisSpinner.setSelection(5)
        } else {
            val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5,7)
            view.farmOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
        }


//        val acqsClString = checkStringNull(farmDataJson.getString("acqsCl"))
//        if(acqsClString.equals("")) {
//            view.farmAcqsSeSpinner.setSelection(0)
//        } else {
//            val acqsClStringsub = acqsClString.substring(5,7)
//            view.farmAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringsub))
//        }
//
//        val inclsClString = checkStringNull(farmDataJson.getString("inclsCl"))
//        if(inclsClString.equals("")) {
//            view.farmInclsSeSpinner.setSelection(0)
//        } else {
//            val inclsClStringsub = inclsClString.substring(5,7)
//            view.farmInclsSeSpinner.setSelection(Integer.valueOf(inclsClString))
//        }

        val rwTrgetAtString = checkStringNull(farmDataJson.getString("rwTrgetAt"))
        if(rwTrgetAtString.equals("")) {
            view.rwTrgetAtChk.isChecked = true
        } else {
            view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
        }

        val apasmtTrgetAtString = checkStringNull(farmDataJson.getString("apasmtTrgetAt"))
        if(apasmtTrgetAtString.equals("Y")) {
            view.apasmtTrgetAtChk.isChecked = true
        } else {
            view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")
        }

        view.includePaclrMatterEdit.setText(checkStringNull(farmDataJson.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(farmDataJson.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(farmDataJson.getString("rm")))


        if(ThingFarmObject.thingNewSearch.equals("N")) {
            // 경작내역 init
            val farmClvtdArray = dataJson.getJSONArray("farmClvtdlList") as JSONArray

            if (farmClvtdArray != null) {
                for (i in 0 until farmClvtdArray.length()) {
                    val farmClvtdlObject = farmClvtdArray.getJSONObject(i)

                    addClvtViewCnt = i

                    val farmViewGroup = farmRealIncomeBasisViewGroup

                    val addFarmView = R.layout.fragment_farm_add_item

                    val inflater: LayoutInflater =
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    val itemView = inflater.inflate(addFarmView, null)

                    farmViewGroup?.addView(itemView)

                    val farmClvtdBaseLayout = farmViewGroup.getChildAt(addClvtViewCnt) as ViewGroup

                    val farmClvtLayoutFirst = farmClvtdBaseLayout.getChildAt(2) as ViewGroup
                    val farmClvtAtView = farmClvtLayoutFirst.getChildAt(0) as ViewGroup
                    val farmClvtAtChk = farmClvtAtView.getChildAt(0) as CheckBox
                    val farmClvtText = farmClvtLayoutFirst.getChildAt(1) as EditText
                    val farmClvtBgnde = farmClvtLayoutFirst.getChildAt(2) as TextView // 경작기간 입력부분
                    val farmClvtEndde = farmClvtLayoutFirst.getChildAt(3) as TextView // 경작기간 입력부분
                    val farmClvtArText = farmClvtLayoutFirst.getChildAt(4) as TextView

                    farmClvtBgnde.setOnClickListener { // 경작기간 입력
                        wtnncUtill.wtnncDatePicker(
                            requireActivity().supportFragmentManager,
                            it as TextView,
                            "addFarmClvtBgnde"
                        )
                    }
                    farmClvtEndde.setOnClickListener { // 경작기간 입력
                        wtnncUtill.wtnncDatePicker(
                            requireActivity().supportFragmentManager,
                            it as TextView,
                            "addFarmClvtEndde"
                        )
                    }


                    val clvtAtString = checkStringNull(farmClvtdlObject.getString("clvtAt"))
                    if (clvtAtString.equals("Y")) {
                        farmClvtAtChk.isChecked = true
                    } else {
                        farmClvtAtChk.isChecked = false
                    }

                    farmClvtText.setText(checkStringNull(farmClvtdlObject.getString("clvt")))

                    farmClvtBgnde.setText(checkStringNull(farmClvtdlObject.getString("clvtBgnde")))
                    farmClvtEndde.setText(checkStringNull(farmClvtdlObject.getString("clvtEndde")))

                    //                farmClvtDtText.text = clvtBgndeString + "~" + clvtEnddeString

                    farmClvtArText.text = checkStringNull(farmClvtdlObject.getString("clvtAr"))

                    val farmClvtLayoutSecond = farmClvtdBaseLayout.getChildAt(5) as ViewGroup
                    val farmRealIncomeAtView = farmClvtLayoutSecond.getChildAt(0) as ViewGroup
                    val farmRealIncomeAtSpinner = farmRealIncomeAtView.getChildAt(0) as Spinner
                    val farmRealIncomeFarmAtView = farmClvtLayoutSecond.getChildAt(1) as ViewGroup
                    val farmRealIncomeFarmAtChk = farmRealIncomeFarmAtView.getChildAt(0) as CheckBox
                    val farmIncomePblctYearText = farmClvtLayoutSecond.getChildAt(2) as TextView
                    val farmClvtIncomeRtText = farmClvtLayoutSecond.getChildAt(3) as EditText
                    val farmRealIncomeText = farmClvtLayoutSecond.getChildAt(4) as EditText


                    val realIncomeAtString = checkStringNull(farmClvtdlObject.getString("realIncomeAt"))
                    farmRealIncomeAtSpinner.setSelection(
                        when (realIncomeAtString) {
                            "Y" -> 1
                            "N" -> 2
                            "X" -> 3
                            else -> 0

                        }
                    )
                    //                if(realIncomeAtString.equals("Y")) {
                    //                    farmRealIncomeAtChk.isChecked = true
                    //                } else {
                    //                    farmRealIncomeAtChk.isChecked = false
                    //                }

                    val realIncomeFarmAtString = checkStringNull(farmClvtdlObject.getString("realIncomeFarmAt"))
                    if (realIncomeFarmAtString.equals("Y")) {
                        farmRealIncomeFarmAtChk.isChecked = true
                    } else {
                        farmRealIncomeFarmAtChk.isChecked = false
                    }

                    farmIncomePblctYearText.text = checkStringNull(farmClvtdlObject.getString("incomePblctYear"))
                    farmClvtIncomeRtText.setText(checkStringNull(farmClvtdlObject.getString("clvtIncomeRt")))
                    farmRealIncomeText.setText(checkStringNull(farmClvtdlObject.getString("realIncome")))

                    val farmClvtLayoutThird = farmClvtdBaseLayout.getChildAt(7) as ViewGroup
                    val farmRealIncomeOrgClView = farmClvtLayoutThird.getChildAt(0) as ViewGroup
                    val farmRealIncomeOrgClSpinner = farmRealIncomeOrgClView.getChildAt(0) as Spinner
                    val farmRealIncomeBasisNmText = farmClvtLayoutThird.getChildAt(1) as EditText
                    val farmFyerIncomeText = farmClvtLayoutThird.getChildAt(2) as EditText



                    farmFyerIncomeText.setOnClickListener { // 적용된 소득자료집 발간년도 입력
                        wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, "incomePblctYear")
                    }

                    wtnncUtill.wtnncSpinnerAdapter(R.array.farmIncomeOriginSeArray, farmRealIncomeOrgClSpinner, this)

                    val realIncomeOrgClString = checkStringNull(farmClvtdlObject.getString("realIncomeOrgCl"))
                    if (realIncomeOrgClString.equals("")) {
                        farmRealIncomeOrgClSpinner.setSelection(0)
                    } else {
                        val realIncomeOrgClStringsub = realIncomeOrgClString.substring(5, 7)
                        farmRealIncomeOrgClSpinner.setSelection(Integer.valueOf(realIncomeOrgClStringsub))

                    }

                    farmRealIncomeBasisNmText.setText(checkStringNull(farmClvtdlObject.getString("realIncomeBasisNm")))
                    farmFyerIncomeText.setText(checkStringNull(farmClvtdlObject.getString("fyerIncome")))

                    addClvtViewCnt++
                }
            }
            // 농업시설물 init
            val farmSubThingArray = dataJson.getJSONArray("farmSubThing")
            if (farmSubThingArray != null) {
                for (i in 0 until farmSubThingArray.length()) {
                    val farmSubThingObject = farmSubThingArray.getJSONObject(i)

                    addThingViewCnt = i

                    val farmViewGroup = farmBaseViewGroup
                    val addThingView = R.layout.fragment_farm_add_thing_item

                    val inflater: LayoutInflater =
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    val itemView = inflater.inflate(addThingView, null)
                    farmViewGroup?.addView(itemView)

                    val farmSubThingBaseView = farmViewGroup.getChildAt(addThingViewCnt) as ViewGroup
                    val farmSubThingViewFirst = farmSubThingBaseView.getChildAt(1) as ViewGroup
                    val farmSubThingSmallClView = farmSubThingViewFirst.getChildAt(0) as ViewGroup
                    val farmSubThingSmallClSpinner = farmSubThingSmallClView.getChildAt(0) as Spinner
                    val farmSubThingKndText = farmSubThingViewFirst.getChildAt(1) as EditText
                    val farmSubThingStrctNdStndrd = farmSubThingViewFirst.getChildAt(2) as EditText

                    wtnncUtill.wtnncSpinnerAdapter(R.array.farmSmallCategorySubArray, farmSubThingSmallClSpinner, this)

                    val subThingSmallClString = checkStringNull(farmSubThingObject.getString("thingSmallCl"))
                    if (subThingSmallClString.equals("")) {
                        farmSubThingSmallClSpinner.setSelection(0)
                    } else {
                        val subThingSmallClStringsub = subThingSmallClString.substring(5, 7)
                        farmSubThingSmallClSpinner.setSelection(Integer.valueOf(subThingSmallClStringsub))
                    }

                    farmSubThingKndText.setText(checkStringNull(farmSubThingObject.getString("thingKnd")))
                    farmSubThingStrctNdStndrd.setText(checkStringNull(farmSubThingObject.getString("strctNdStndrd")))

                    val farmSubThingViewSecond = farmSubThingBaseView.getChildAt(3) as ViewGroup
                    val farmSubThingArView = farmSubThingViewSecond.getChildAt(0) as ViewGroup
                    val farmSubThingBgnnArText = farmSubThingArView.getChildAt(0) as EditText
                    val farmSubThingIncrprArText = farmSubThingArView.getChildAt(1) as EditText
                    val farmSubThingUnitClView = farmSubThingViewSecond.getChildAt(1) as ViewGroup
                    val farmSubThingUnitClSpinner = farmSubThingUnitClView.getChildAt(0) as Spinner
                    val farmSubThingArComputerBasisText = farmSubThingViewSecond.getChildAt(2) as EditText

//                    wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, farmSubThingUnitClSpinner, this)
                    wtnncUtill.wtnncSpinnerAdapter("A009", farmSubThingUnitClSpinner, this)

                    farmSubThingBgnnArText.setText(checkStringNull(farmSubThingObject.getString("bgnnAr")))
                    farmSubThingIncrprArText.setText(checkStringNull(farmSubThingObject.getString("incrprAr")))
                    val subUnitClString = checkStringNull(farmSubThingObject.getString("unitCl"))
//                    if (subUnitClString.equals("")) {
//                        farmSubThingUnitClSpinner.setSelection(0)
//                    } else {
//                        val subUnitClStringsub = subUnitClString.substring(5, 7)
//                        farmSubThingUnitClSpinner.setSelection(Integer.valueOf(subUnitClStringsub))
//                    }
                    farmSubThingUnitClSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", subUnitClString) )
                    farmSubThingArComputerBasisText.setText(checkStringNull(farmSubThingObject.getString("arComputBasis")))

                    addThingViewCnt++

                }
            }

            // 농업 문서

            farmAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until farmAtchInfo!!.length()) {
                val farmAtchItem = farmAtchInfo!!.getJSONObject(i)

                val farmAtchFileInfo = farmAtchItem.getString("fileseInfo")

                // 농지원부
                view.frlndLedgerBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006009" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 농업인확인서
                view.farmerCnfirmBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006023" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 농어업경영체등록확인서
                view.fngmtRegistCnfirmBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006024" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 직불금 조회
                view.dirctCashInqireBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006021" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 주민등록초본
                view.commRsgstAbstrctBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006007" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 임대차계약서
                view.commLsCtrtcBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006019" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // 경작사실확인서서
                view.clvtFactCnfirm.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006022" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
            }



            settingSearchCamerasView(farmAtchInfo)
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

        if(ThingFarmObject.thingNewSearch.equals("N")) {
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (parent?.id) {
            R.id.posesnClvthmTy -> when (position) { // 점유구분
                2 -> farmLesseeViewGroup.visibleView()
                else -> farmLesseeViewGroup.goneView()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addFarmData() {

        // 대분류 입력
        ThingFarmObject.thingLrgeCl = "A011002" // 농업
        Log.d("farmTest", "대분류 : ${ThingFarmObject.thingLrgeCl}")

        ThingFarmObject.thingSmallCl = "A039001"
        Log.d("farmTest", "소분류 : ${ThingFarmObject.thingSmallCl}")

        // 물건의 종류
        ThingFarmObject.thingKnd = "농업보상"//mActivity.farmSmallText.text.toString()
        Log.d("farmTest", "물건의 종류 : ${ThingFarmObject.thingKnd}")

        // 관련지번
        val farmRelateLnmString = activity!!.landSearchRelatedLnmText.text.toString()
        if(!getString(R.string.landInfoRelatedLnmText).equals(farmRelateLnmString)) {
            ThingFarmObject.relateLnm = farmRelateLnmString
        }

        ThingFarmObject.bgnnAr = mActivity.farmBgnnAr.text.toString() // 전체면적
        Log.d("farmTest", "전체면적 : ${ThingFarmObject.bgnnAr}")

        ThingFarmObject.incrprAr = mActivity.farmIncrprAr.text.toString() // 편입면적
        Log.d("farmTest", "편입면적 : ${ThingFarmObject.incrprAr}")

        ThingFarmObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.farmUnitSpinner.selectedItemPosition)
//            when (mActivity.farmUnitSpinner.selectedItemPosition) { // 단위
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
        Log.d("farmTest", "단위 : ${ThingFarmObject.unitCl}")

        ThingFarmObject.arComputBasis = mActivity.farmArComputBasis.text.toString() // 면적산출근거
        Log.d("farmTest", "면적산출근거 : ${ThingFarmObject.arComputBasis}")

        // 농지의 적법성 근거
        //적법여부
        ThingFarmObject.frldbsLgalAt = when (mActivity.frldbsLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농지근거)적법여부 : ${ThingFarmObject.frldbsLgalAt}")

        // 농지근거
        ThingFarmObject.frldbsBasisCl = when (mActivity.frldbsBasisCl.selectedItemPosition) {
            1 -> "A113001" // 전, 답, 과수원
            2 -> "A113002" // 다년생 경작(3년이상)
            3 -> "A113003" // 전용허가 받은 임야
            4 -> "A113004" // 경작 부속시설
            5 -> "A113005" // 기타
            6 -> "A113006" // 고정식온실VH
            else -> ""
        }
        Log.d("farmTest", "(농지근거)농지근거 분류 : ${ThingFarmObject.frldbsBasisCl}")
        // 농지원부 유무
        ThingFarmObject.frldbsFrldLdgrAt = when (mActivity.frldbsFrldLdgrAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농지근거)농지원부 유무 : ${ThingFarmObject.frldbsFrldLdgrAt}")

        // 농민의 적법성 근거
        //적법여부
        ThingFarmObject.frmrbsLgalAt = when (mActivity.frmrbsLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농민근거)적법여부 : ${ThingFarmObject.frmrbsLgalAt}")

        // 농민근거
        ThingFarmObject.frmrbsBasisCl = when (mActivity.frmrbsBasisCl.selectedItemPosition) {
            1 -> "A114001" // 1,000㎡ 이상 경작
            2 -> "A114002" // 1년중 90일 이상 경작
            3 -> "A114003" // 330㎡ 이상 VH
            4 -> "A114004" // 기타
            else -> ""
        }
        Log.d("farmTest", "(농민근거)농민근거 분류 : ${ThingFarmObject.frmrbsBasisCl}")

        // 농업인확인서 여부
        ThingFarmObject.frmrbsCnfrmnDta1At = when (mActivity.frmrbsCnfrmnDta1At.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농민근거)농업인확인서 여부 : ${ThingFarmObject.frmrbsCnfrmnDta1At}")

        //농어업경영체등록 확인서 유무
        ThingFarmObject.frmrbsCnfrmnDta2At = when (mActivity.frmrbsCnfrmnDta2At.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농민근거)농어업경영체 등록 확인서 여부 : ${ThingFarmObject.frmrbsCnfrmnDta2At}")

        // 직불금 수령자와 경작자 일치 여부
//        ThingFarmObject.frmrbsDbtamtAt = when (mActivity.frmrbsDbtamtAt.isChecked) {
//            true -> "Y"
//            else -> "N"
//        }

        ThingFarmObject.frmrbsDbtamtAt = when(mActivity.frmrbsDbtamtAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""

        }
        Log.d("farmTest", "(농민근거)직불금 수령자와 경작자 일치여부 : ${ThingFarmObject.frmrbsDbtamtAt}")

        //경작자 해당지역 거주여부
        ThingFarmObject.frmrbsAraResideAt = when (mActivity.frmrbsAraResideAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(농민근거)경작자 해당지역 거주여부 : ${ThingFarmObject.frmrbsAraResideAt}")

        // 점유의 적법성 근거
        // 적법여부
        ThingFarmObject.posesnLgalAt = when (mActivity.posesnLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(점유근거)적법여부 : ${ThingFarmObject.posesnLgalAt}")

        // 점유구분 입력
        ThingFarmObject.posesnClvthmTy = when (mActivity.posesnClvthmTy.selectedItemPosition) {
            1 -> "1" // 자경
            2 -> "2" // 임차농
            else -> ""
        }
        Log.d("farmTest", "(점유근거)경작자 타입 : ${ThingFarmObject.posesnClvthmTy}")

        //토지주 해당지역 거주 여부
        ThingFarmObject.posesnLadResideAt = when(mActivity.posesnLadResideAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // 토지주 농민 여부
        ThingFarmObject.posesnLadFarmerAt = when(mActivity.posesnLadFarmerAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // 농지소유자 확인 경작사실확인서 유무
        ThingFarmObject.posesnOwnerClvtCnfirmAt = when(mActivity.posesnOwnerClvtCnfirmAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // 직불금 수령여부
        ThingFarmObject.posesnDbtamtRepAt = when(mActivity.posesnDbtamtRepAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // 직블금 수령 연도
        ThingFarmObject.posesnDbtamtRepInf = mActivity.posesnDbtamtRepInf.text.toString()


        // 임대차계약서 존재여부
        ThingFarmObject.posesnLrcdocAt = when (mActivity.posesnLrcdocAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(점유근거)임대차계약서 존재유무 : ${ThingFarmObject.posesnLrcdocAt}")

        // 임차인 성명
        ThingFarmObject.posesnRentName = mActivity.posesnRentName.text.toString()
        Log.d("farmTest", "(점유근거)임차인 성명 : ${ThingFarmObject.posesnRentName}")

        // 임대인 성명
        ThingFarmObject.posesnHireName = mActivity.posesnHireName.text.toString()
        Log.d("farmTest", "(점유근거)임대인 성명 : ${ThingFarmObject.posesnHireName}")

        // 임차기간 시작
        Log.d("farmTest", "(점유근거)임차기간 시작 : ${ThingFarmObject.posesnRentBgnde}")

        // 임차기간 종료
        Log.d("farmTest", "(점유근거)임차기간 종료 : ${ThingFarmObject.posesnRentEndde}")

        // 보증금
        ThingFarmObject.posesnGtn = mActivity.posesnGtn.text.toString()
        Log.d("farmTest", "(점유근거)보증금 : ${ThingFarmObject.posesnGtn}")

        // 월세
        ThingFarmObject.posesnMtht = mActivity.posesnMtht.text.toString()
        Log.d("farmTest", "(점유근거)월세 : ${ThingFarmObject.posesnMtht}")

        // 계약(소유)위치
        ThingFarmObject.posesnCntrctLc = mActivity.posesnCntrctLc.text.toString()
        Log.d("farmTest", "(점유근거)계약(소유)위치 : ${ThingFarmObject.posesnCntrctLc}")

        // 계약(소유)면적(㎡)
        ThingFarmObject.posesnCntrctAr = mActivity.posesnCntrctAr.text.toString()
        Log.d("farmTest", "(점유근거)계약(소유)면적(㎡) : ${ThingFarmObject.posesnCntrctAr}")

        // 특약
        ThingFarmObject.posesnSpccntr = mActivity.posesnSpccntr.text.toString()
        Log.d("farmTest", "(점유근거)특약 : ${ThingFarmObject.posesnSpccntr}")


        ThingFarmObject.ownerCnfirmBasisCl = when(mActivity.farmOwnerCnfirmBasisSpinner.selectedItemPosition) {
            1 -> "A035001"
            2 -> "A035002"
            3 -> "A035003"
            4 -> "A035004"
            5 -> "A035005"
            else -> ""
        }

//        ThingFarmObject.acqsCl = when(mActivity.farmAcqsSeSpinner.selectedItemPosition) {
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
//
//        ThingFarmObject.inclsCl = when(mActivity.farmInclsSeSpinner.selectedItemPosition) {
//            1->"A007001"
//            2->"A007002"
//            3->"A007003"
//            4->"A007004"
//            else->""
//        }

        ThingFarmObject.acqsCl = "A025001"
        ThingFarmObject.inclsCl = "A007001"

        ThingFarmObject.rwTrgetAt = when(mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        ThingFarmObject.apasmtTrgetAt = when(mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        // 특이사항
        ThingFarmObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString() // 특이사항
        Log.d("farmTest", "특이사항 : ${ThingFarmObject.paclrMatter}")

        ThingFarmObject.referMatter = mActivity.includereReferMatter.text.toString() // 참고사항

        ThingFarmObject.rm = mActivity.includeRm.text.toString() //  비고


        // 경작내역
        var thingFarmClvtdlJson = JSONObject()
        var thingFarmClvtdlItem = JSONObject()
        var thingFarmClvtdlArray = JSONArray()

        // 경작, 실제소득근거 추가부분
        var addClvtCnt = mActivity.farmRealIncomeBasisViewGroup.childCount // 추가된 View 카운트

        for (i in 0 until addClvtCnt) {

            Log.d("farmTest", "******************************************************************")

            var thingFarmClvtdlItem = JSONObject()

            val farmViewGroup = mActivity.farmRealIncomeBasisViewGroup // 뷰가 추가되는 BaseViewGroup
            val addLayout1 = farmViewGroup.getChildAt(i) as ViewGroup // 추가된 뷰의 첫번째 항목

            val addLayout2 = addLayout1.getChildAt(2) as ViewGroup // 3번째 라인
            val addChkLayout1 = addLayout2.getChildAt(0) as ViewGroup // 경작여부 ViewGroup
            val addClvtAt = addChkLayout1.getChildAt(0) as CheckBox // 경작여부
            val addClvt = addLayout2.getChildAt(1) as EditText // 작물명
            val addClvtAr = addLayout2.getChildAt(4) as TextView // 작물별 경작면적

            val addLayout3 = addLayout1.getChildAt(5) as ViewGroup // 6번째 라인
            val addChkLayout2 = addLayout3.getChildAt(0) as ViewGroup // 실제소득 증빙여부 ViewGroup
            val addRealIncomeAtSpinner = addChkLayout2.getChildAt(0) as Spinner // 실제소득 증빙여부
            val addRealIncomeFarmAtView = addLayout3.getChildAt(1) as ViewGroup
            val addRealIncomeFarmAt = addRealIncomeFarmAtView.getChildAt(0) as CheckBox // 실제소득 영농 가능 여부
            val addIncomePblctYear = addLayout3.getChildAt(2) as TextView // 적용된 소득자료집 발간년도
            val addClvtIncomeRt = addLayout3.getChildAt(3) as EditText // 동일(유사)작목군 평균소득률
            val addRealIncome = addLayout3.getChildAt(4) as EditText // 동일(유사)작목군 평균소득액

            val addLayout4 = addLayout1.getChildAt(7) as ViewGroup // 8번째 라인
            val addSpinnerLayout = addLayout4.getChildAt(0) as ViewGroup // 실제소득 출처구분 ViewGroup
            val addRealIncomeOrgCl = addSpinnerLayout.getChildAt(0) as Spinner // 실제소득 출처구분
            val addRealIncomeBasisNm = addLayout4.getChildAt(1) as EditText // 실제소득 출처업체명
            val addFyerIncome = addLayout4.getChildAt(2) as EditText // 작물별 연간 평균수입

            // 경작여부
            thingFarmClvtdlItem.put("clvtAt", when (addClvtAt.isChecked) {
                true -> "Y"
                else -> "N"
            })
            // 작물명
            thingFarmClvtdlItem.put("clvt", addClvt.text.toString())

            // 경작기간 시작
            if (ThingFarmObject.clvtBgndeList!!.size != 0) {
                thingFarmClvtdlItem.put("clvtBgnde", ThingFarmObject.clvtBgndeList!!.get(i))
            }

            // 경작기간 종료
            if (ThingFarmObject.clvtEnddeList!!.size != 0) {
                thingFarmClvtdlItem.put("clvtEndde", ThingFarmObject.clvtEnddeList!!.get(i))
            }

            // 작물별 경작면적
            thingFarmClvtdlItem.put("clvtAr", addClvtAr.text.toString())


            // 농작물 실제소득 근거
            // 실제소득증빙 여부
            thingFarmClvtdlItem.put("realIncomeAt", when (addRealIncomeAtSpinner.selectedItemPosition) {
                1 -> "Y"
                2 -> "N"
                3 -> "X"
                else -> ""
            })
            // 실제소득영농가능 여부
            thingFarmClvtdlItem.put("realIncomeFarmAt", when (addRealIncomeFarmAt.isChecked) {
                true -> "Y"
                else -> "N"
            })

            // 적용된 소득자료집 발간년도
            thingFarmClvtdlItem.put("incomePblctYear", addIncomePblctYear.text.toString())

            // 동일(유사)작목군 평균소득률
            thingFarmClvtdlItem.put("clvtIncomeRt", addClvtIncomeRt.text.toString())

            // 동일(유사)작목군 평균 소득액
            thingFarmClvtdlItem.put("realIncome", addRealIncome.text.toString())

            // 실제소득 출처구분
            thingFarmClvtdlItem.put("realIncomeOrgCl", when (addRealIncomeOrgCl.selectedItemPosition) {
                1 -> "A115001" // 농안법(도매시장 등)
                2 -> "A115002" // 대형마트/백화점
                3 -> "A115003" // 호텔
                4 -> "A115004" // 국가/지자체
                5 -> "A115005" // 세관/세무처
                6 -> "A115006" // 기타
                else -> ""
            })

            // 실제소득 출처업체명
            thingFarmClvtdlItem.put("realIncomeBasisNm", addRealIncomeBasisNm.text.toString())

            // 작물별 연간 평균수입
            thingFarmClvtdlItem .put("fyerIncome", addFyerIncome.text.toString())

            thingFarmClvtdlArray.put(thingFarmClvtdlItem)
            Log.d("farmTest", "*****************************************************************")
        }


        thingFarmClvtdlJson.put("farmClvtdlList", thingFarmClvtdlArray)
        ThingFarmObject.addFarmClvtdlList = thingFarmClvtdlJson

        ThingFarmObject.rwTrgetAt = when(mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        ThingFarmObject.apasmtTrgetAt = when(mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }



        // 구조 및 규격
//        ThingFarmObject.strctNdStrndrd = mActivity.farmStrctNdStrndrdText.text.toString()
        ThingFarmObject.strctNdStndrd = StringBuilder().apply {

            if(ThingFarmObject.posesnClvthmTy.equals("1")) {
                append("자경(")
            } else {
                append("임차농(")
            }
            val addFarmArray = ThingFarmObject.addFarmClvtdlList!!.getJSONArray("farmClvtdlList") as JSONArray
            var addFarmItemCnt = 0
            if(addFarmArray != null) {
                for(i in 0 until addFarmArray.length()) {
                    val addFarmObject = addFarmArray.getJSONObject(i)

                    val clvt = addFarmObject.getString("clvt")
                    val realIncomeAt = addFarmObject.getString("realIncomeAt")
                    val realIncome = addFarmObject.getString("realIncome")

                    if(realIncomeAt.equals("Y")) {
                        append(clvt + "," + realIncome)
                    } else {
                        append(clvt)
                    }
                    addFarmItemCnt++
                    if(addFarmItemCnt != addFarmArray.length()-1) {
                        append(",")
                    }
                }

            }
            append(")")

        }.toString()

        logUtil.d("농업 스트링 -> ${ThingFarmObject.strctNdStrndrd}")

//        Log.d("farmTest", "구조 및 규격 : ${ThingFarmObject.strctNdStrndrd}")

        // 시설물 추가부분
//        var farmAddThingList = ThingFarmObject.farmAddThingList // 추가 시설물 통합 리스트
        var thingFarmAddJson = JSONObject()
        var thingFarmAddArray = JSONArray()
        var addThingCnt = mActivity.farmBaseViewGroup.childCount // 추가된 시설물 카운트
        if(addThingCnt > 0) {
            for (i in 0 until addThingCnt) {
                Log.d("farmTest", "******************************************************************")

                var thingFarmAddItem = JSONObject()
                //            val farmAddItemMap: MutableMap<String, String> = mutableMapOf()
                val farmViewGroup = mActivity.farmBaseViewGroup // 물건 추가 Base ViewGroup
                val addLayoutItem = farmViewGroup.getChildAt(i) as ViewGroup // 추가된 i번째 view

                val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup // 2번째 라인
                val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup // 소분류 ViewGroup
                val addSmallClSpinner = addSpinnerLayout1.getChildAt(0) as Spinner // 소분류
                val addThingKnd = addViewGroup1.getChildAt(1) as EditText // 물건의 종류
                val addStrctStndrd = addViewGroup1.getChildAt(2) as EditText // 구조 및 규격

                val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup // 4번째 라인
                val addAreaViewGroup = addViewGroup2.getChildAt(0) as ViewGroup // 면적 및 수량 ViewGroup
                val addAllAr = addAreaViewGroup.getChildAt(0) as EditText // 전체면적(수량)
                val addIncrpr = addAreaViewGroup.getChildAt(1) as EditText // 편입면적(수량)
                val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup // 단위 ViewGroup
                val addUnit = addSpinnerLayout2.getChildAt(0) as Spinner // 단위
                val addArComputBasis = addViewGroup2.getChildAt(2) as EditText // 면적산출근거

                // 소분류
                thingFarmAddItem.put("thingSmallCl", when (addSmallClSpinner.selectedItemPosition) {
                    1 -> "A039002" // 농업시설물
                    2 -> "A039003" // 농기구
                    3 -> "A039004" // 농작물
                    else -> ""
                })

                // 물건의 종류
                thingFarmAddItem.put("thingKnd",addThingKnd.text.toString())

                // 구조 및 규격
                thingFarmAddItem.put("strctNdStrndrd",addStrctStndrd.text.toString())

                // 전체면적
                thingFarmAddItem.put("bgnnAr", addAllAr.text.toString())

                // 편입면적
                thingFarmAddItem.put("incrprAr", addIncrpr.text.toString())

                // 단위
                thingFarmAddItem.put("unitCl", CommonCodeInfoList.getCodeId("A009", addUnit.selectedItemPosition))
//                    when (addUnit.selectedItemPosition) {
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
                thingFarmAddItem.put("arComputBasis", addArComputBasis.text.toString())

                thingFarmAddArray.put(thingFarmAddItem)

                Log.d("farmTest", "******************************************************************")
            }
            thingFarmAddJson.put("farmThing", thingFarmAddArray)
        } else {
            thingFarmAddJson.put("farmThing", thingFarmAddArray)
        }

        ThingFarmObject.addFarmThignList = thingFarmAddJson


    }


    /**
     * 경작내역 및 소득근거 테이블 추가 (실제이용현황이 그려졌을 시에 실행되는 메소드)
     * @param currentArea 경작 면적
     */

    fun addTableRow(currentArea:Int?){

        ThingFarmObject.thingFarmPolygonCurrentArea = currentArea // 경작면적

        val farmViewGroup = farmRealIncomeBasisViewGroup
        val addFarmView = R.layout.fragment_farm_add_item
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(addFarmView, null)
        val itemView = inflater.inflate(addFarmView, null)
        farmViewGroup?.addView(itemView)

        val farmClvtdlView = farmViewGroup.getChildAt(addClvtViewCnt) as ViewGroup // 추가된 첫번째 ViewGroup

        val farmClvtdlViewFirst = farmClvtdlView.getChildAt(2) as ViewGroup // 3번째 라인
        val farmClvtBgnde = farmClvtdlViewFirst.getChildAt(2) as TextView // 경작기간 입력부분
        val farmClvtEndde = farmClvtdlViewFirst.getChildAt(3) as TextView // 경작기간 입력부분

        // TODO: 2021-11-16 경작면적 자동입력 추가
        var currentFarmTextView :TextView = farmClvtdlViewFirst.getChildAt(4) as TextView
        currentFarmTextView.text = currentArea.toString()

        val addViewGroup2 = farmClvtdlView.getChildAt(5) as ViewGroup // 6번째 라인
        val farmRealIncomeAtView = addViewGroup2.getChildAt(0) as ViewGroup
        val farmRealIncomeSpinner = farmRealIncomeAtView.getChildAt(0) as Spinner
        val addFarmIncomeDtaPblctYear = addViewGroup2.getChildAt(2) as TextView // 적용된 소득자료집 발간년도 입력부분
        val addViewGroup3 = farmClvtdlView.getChildAt(7) as ViewGroup // 8번째 라인
        val addSpinnerViewGroup = addViewGroup3.getChildAt(0) as ViewGroup // 실제소득 출처구분의 ViewGroup
        val addFarmIncomeOriginSe = addSpinnerViewGroup.getChildAt(0) as Spinner // 실제소득 출처구분 입력부분

        wtnncUtill.wtnncSpinnerAdapter(R.array.realIncomeAt, farmRealIncomeSpinner, this) // 실제소득 출처구분 Spinner


        farmClvtBgnde.setOnClickListener { // 경작기간 입력
            wtnncUtill.wtnncDatePicker(requireActivity().supportFragmentManager, it as TextView, "addFarmClvtBgnde")
        }
        farmClvtEndde.setOnClickListener { // 경작기간 입력
            wtnncUtill.wtnncDatePicker(requireActivity().supportFragmentManager, it as TextView, "addFarmClvtEndde")
        }

        addFarmIncomeDtaPblctYear.setOnClickListener { // 적용된 소득자료집 발간년도 입력
            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, "incomePblctYear")
        }

        wtnncUtill.wtnncSpinnerAdapter(R.array.farmIncomeOriginSeArray, addFarmIncomeOriginSe, this) // 실제소득 출처구분 Spinner


        addClvtViewCnt++
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
    }

}