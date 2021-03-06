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
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.ThingFarmObject
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import kr.or.kreb.ncms.mobile.enums.ToastType
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
    private var addClvtViewCnt: Int = 0 // ?????? ?????? ??????
    private var addThingViewCnt: Int = 0 // ?????? ?????????

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

        //??????????????????
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.FARM,
                "A200006012",
                "????????????",
                CameraEnum.DEFAULT
            )
        }

        val farmLatLngArrSize = (requireContext() as MapActivity).naverMap?.resultFarmLatLngArr?.size

        btn_farmSearchEdit.setOnClickListener {
            //logUtil.d("?????? ??????")

            try {

                farmLatLngArrSize.let {

                    logUtil.d("????????? ????????? ?????????.")

                    (requireContext() as MapActivity).runOnUiThread {
                        showToast(ToastType.INFO, "????????? ????????? ???????????? ????????????.", 500)
                    }

                }


//                LandInfoObject.landRealArCurPos = curPos
//                logUtil.d("?????????????????? [??????] [$curPos]")

//                LandInfoObject.isEditable = true

//                for (i in 0..LandInfoObject.searchRealLand?.length()!!) {
//                    if (curPos == i) { //
//
//                        val editPolygonGeom = (LandInfoObject.searchRealLand?.get(i) as JSONObject).get("geoms").toString()
//                        getActivity().settingCartoMap(null, null)
//
//                        setEditLatLngArr(editPolygonGeom).let { ar ->
//                            getActivity().cartoMap?.modifyLandAr(ar)
//                        }
//
//                        break
//                    }
//                }


            } catch (e: Exception) {
                logUtil.e(e.message.toString())
            }

        }

        btn_farmSearchRemove.setOnClickListener {
            logUtil.d("?????? ??????")
        }

        farmBgnnAr.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
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


        // ????????????
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

        // ????????????
//        farmClvtPd.setOnClickListener {
//            wtnncUtill.wtnncDateRangePicker(requireActivity().supportFragmentManager, it as TextView, "farmClvtPd")
//        }

        // ????????? ????????????
//        frldbsDbtamtYear.setOnClickListener {
//            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, null)
//        }

        // ????????? ??????????????? ?????? ??????
//        incomePblctYear.setOnClickListener {
//            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, null)
//        }

        // ????????????, ????????? ???????????? ?????? ??????
        farmAddClvtDtlsBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, this)
        }

        // ????????? ??????
        farmAddThingLayoutBtn.setOnClickListener {

            val farmViewGroup = farmBaseViewGroup // ???????????? ???????????? ??????
            var addThingView = R.layout.fragment_farm_add_thing_item // ????????? ?????? Layout
            val inflater: LayoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            inflater.inflate(addThingView, null)
            val itemView = inflater.inflate(addThingView, null)
            farmViewGroup?.addView(itemView)

            // ????????? spinner
            val addLayoutItem = farmViewGroup.getChildAt(addThingViewCnt) as ViewGroup //???????????? layout??? ?????? ViewGroup
            val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup // 2?????? ??????
            val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup // 2?????? ????????? ????????? ??????
            val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner // ????????? Spinner
            wtnncUtill.wtnncSpinnerAdapter(R.array.farmSmallCategorySubArray, addSpinner1, this)

            // ????????? spinner
            val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup // 4?????? ??????
            val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup // 4?????? ????????? ????????? ??????
            val addSpinner2 = addSpinnerLayout2.getChildAt(0) as Spinner // ??????
            // A009
//            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)
            wtnncUtill.wtnncSpinnerAdapter("A009", addSpinner2, this)

            addThingViewCnt++
        }

        // ????????????
        frlndLedgerBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("???????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006009")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("???????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006009", "????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ??????????????????
        farmerCnfirmBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006023")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006023", "??????????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ?????????????????????????????????
        fngmtRegistCnfirmBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("????????????????????????????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006024")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("????????????????????????????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006024", "?????????????????????????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ????????? ??????
        dirctCashInqireBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006021")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006021", "?????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ??????????????????
        commRsgstAbstrctBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006007")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006007", "??????????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ??????????????????
        commLsCtrtcBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006019")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006019", "??????????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
            }
        }
        // ????????????????????????
        clvtFactCnfirm.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val farmAtchSelectArray = JSONArray()

            array.add("???????????????????????? ??????")
            if (ThingFarmObject.thingNewSearch.equals("N")) {
                for (i in 0 until farmAtchInfo!!.length()) {
                    val bsnAtchItem = farmAtchInfo!!.getJSONObject(i)
                    if (bsnAtchItem.getString("fileseInfo").equals("A200006022")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        farmAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("???????????????????????? ??????")
                    .setPositiveButton("??????") { _, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if (checkedItem == 0) {
                            callThingCapture("A200006022", "????????????????????????")
                        } else {
                            val item = farmAtchSelectArray!!.get(checkedItem - 1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") { _, _ ->
                        logUtil.d("setNegativeButton ------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ?????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()
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
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        toast.msg_error(R.string.msg_server_connected_fail, 100)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                        val fileNameString = "$downloadDirectory/${item.getString("atfl")}"
                        var getLandFileBitmap: Bitmap? = null

                        if (FileUtil.getExtension(fileNameString) == "pdf") {
                            FileUtil.savePdfToFileCache(response.body?.byteStream()!!, fileNameString)
                        } else {
                            getLandFileBitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                        }

                        FileUtil.run {
                            createDir(downloadDirectory)
                            if (getExtension(fileNameString)?.contains("png") == true) {
                                saveBitmapToFileCache(getLandFileBitmap!!, fileNameString)
                            }
                        }

                        val downloadFile = File(fileNameString)

                        WtnccDocViewFragment(downloadFile).show(
                            requireActivity().supportFragmentManager,
                            "docViewFragment"
                        )
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


//        wtnncUtill.wtnncSpinnerAdapter(R.array.farmSmallCategoryArray, farmSclasSpinner, this) // ?????????
        // A009
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, farmUnitSpinner, this) // ??????
        wtnncUtill.wtnncSpinnerAdapter("A009", farmUnitSpinner, this) // ??????
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmFrlndBasisArray, frldbsBasisCl, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmFarmerBasisArray, frmrbsBasisCl, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.farmPrmisnSeArray, posesnClvthmTy, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.frmrbsDbtamtAt, frmrbsDbtamtAtSp, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnLadResideAt, posesnLadResideAtSp, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnLadFarmerAt, posesnLadFarmerAtSp, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnOwnerClvtCnfirmAt, posesnOwnerClvtCnfirmAtSp, this) // ????????????
        wtnncUtill.wtnncSpinnerAdapter(R.array.posesnDbtamtRepAt, posesnDbtamtRepAtSp, this) // ????????????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, farmAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, farmInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, farmOwnerCnfirmBasisSpinner, this)


        //wtnncUtill.wtnncSpinnerAdapter(R.array.farmIncomeOriginSeArray, realIncomeOrgCl, this) // ?????????????????? ??????

        view.landSearchLocationText.setText(checkStringNull(farmDataJson.getString("legaldongNm")))
        view.landSearchBgnnLnmText.setText(checkStringNull(farmDataJson.getString("bgnnLnm")))
        view.landSearchincrprLnmText.setText(checkStringNull(farmDataJson.getString("incrprLnm")))
        view.landSearchNominationText.setText(checkStringNull(farmDataJson.getString("gobuLndcgrNm")))
        val relateLnmString = checkStringNull(farmDataJson.getString("relateLnm"))
        if (relateLnmString.equals("")) {
            view.landSearchRelatedLnmText.setText("??????")
        } else {
            view.landSearchRelatedLnmText.setText(relateLnmString)
        }

        view.landSearchBgnnArText.setText(checkStringNull(farmDataJson.getString("ladBgnnAr")))
        view.landSearchIncrprArText.setText(checkStringNull(farmDataJson.getString("ladIncrprAr")))
        view.landSearchOwnerText.setText(checkStringNull(farmDataJson.getString("landOwnerName")))
        view.landSearchOwnerRText.setText(checkStringNull(farmDataJson.getString("landRelatesName")))

        val farmWtnCodeString = farmDataJson.getInt("farmWtnCode")
        if (farmWtnCodeString == 0) {
            view.farmWtnCodeText.setText("????????????")
        } else {
            view.farmWtnCodeText.setText(farmWtnCodeString.toString())
        }

        val framThingSmallCl = checkStringNull(farmDataJson.getString("thingSmallCl"))
        val farmThingSmallNm = checkStringNull(farmDataJson.getString("thingSmallNm"))
        if (farmThingSmallNm.equals("")) {
            view.farmSmallText.setText("????????????")

        } else {
            view.farmSmallText.setText(farmThingSmallNm)
        }
        val farmThingKndString = checkStringNull(farmDataJson.getString("thingKnd"))
        if (farmThingKndString.equals("")) {
            view.farmThingKndText.setText("????????????")

        } else {
            view.farmThingKndText.setText(farmThingKndString)
        }
        val farmStrctNdStrndeString = checkStringNull(farmDataJson.getString("strctNdStndrd"))
        if (farmStrctNdStrndeString.equals("")) {
            view.farmStrctNdStrndrdText.setText("????????????")

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
        view.farmUnitSpinner.setSelection(CommonCodeInfoList.getIdxFromCodeId("A009", farmUnitClString))

        view.farmArComputBasis.setText(checkStringNull(farmDataJson.getString("arComputBasis")))

        /*
        ????????? ???????????????
         */
        val frldbsLgalAtString = checkStringNull(farmDataJson.getString("frldbsLgalAt"))
        if (frldbsLgalAtString.equals("Y")) {
            view.frldbsLgalAt.isChecked = true
        } else {
            view.frldbsLgalAt.isChecked = false;
        }

        val frldbsBasisClString = checkStringNull(farmDataJson.getString("frldbsBasisCl"))
        if (frldbsBasisClString.equals("")) {
            view.frldbsBasisCl.setSelection(0)
        } else {
            val frldbsBasisClStringSub = frldbsBasisClString.substring(5, 7)
            view.frldbsBasisCl.setSelection(Integer.valueOf(frldbsBasisClStringSub))
        }

        val frldbsFrldLdgrAtString = checkStringNull(farmDataJson.getString("frldbsFrldLdgrAt"))
        if (frldbsFrldLdgrAtString.equals("Y")) {
            view.frldbsFrldLdgrAt.isChecked = true
        } else {
            view.frldbsFrldLdgrAt.isChecked = false
        }

        /*
        ????????? ????????? ??????
         */
        val frmrbsLgalAtString = checkStringNull(farmDataJson.getString("frmrbsLgalAt"))
        if (frmrbsLgalAtString.equals("")) {
            view.frmrbsLgalAt.isChecked = false
        } else {
            view.frmrbsLgalAt.isChecked = true
        }

        val frmrbsBasisClString = checkStringNull(farmDataJson.getString("frmrbsBasisCl"))
        if (frmrbsBasisClString.equals("")) {
            view.frmrbsBasisCl.setSelection(0)
        } else {
            val frmrbsBasisClStringSub = frmrbsBasisClString.substring(5, 7)
            view.frmrbsBasisCl.setSelection(Integer.valueOf(frmrbsBasisClStringSub))
        }

        val frmrbsCnfrmnDta1AtString = checkStringNull(farmDataJson.getString("frmrbsCnfrmnDta1At"))
        if (frmrbsCnfrmnDta1AtString.equals("Y")) {
            view.frmrbsCnfrmnDta1At.isChecked = true
        } else {
            view.frmrbsCnfrmnDta1At.isChecked = false
        }

        val frmrbsCnfrmnDta2AtString = checkStringNull(farmDataJson.getString("frmrbsCnfrmnDta2At"))
        if (frmrbsCnfrmnDta2AtString.equals("Y")) {
            view.frmrbsCnfrmnDta2At.isChecked = true
        } else {
            view.frmrbsCnfrmnDta2At.isChecked = false
        }

        val frmrbsDbtamtAtString = checkStringNull(farmDataJson.getString("frmrbsDbtamtAt"))
        when (frmrbsDbtamtAtString) {
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
        if (frmrbsAraResideAtString.equals("Y")) {
            view.frmrbsAraResideAt.isChecked = true
        } else {
            view.frmrbsAraResideAt.isChecked = false
        }

        /*
        ????????? ????????? ?????????
         */
        val posesnLgalAtString = checkStringNull(farmDataJson.getString("posesnLgalAt"))
        if (posesnLgalAtString.equals("Y")) {
            view.posesnLgalAt.isChecked = true
        } else {
            view.posesnLgalAt.isChecked = false
        }
        val posesnClvthmTyString = checkStringNull(farmDataJson.getString("posesnClvthmTy"))
        if(posesnClvthmTyString.equals("")) {
            view.posesnClvthmTy.setSelection(0)
        } else {
            when (posesnClvthmTyString) {
                "1" -> view.posesnClvthmTy.setSelection(1)
                "2" -> view.posesnClvthmTy.setSelection(2)
                else -> view.posesnClvthmTy.setSelection(0)
            }
        }

        val posesnLadResideAtString = checkStringNull(farmDataJson.getString("posesnLadResideAt"))
        view.posesnLadResideAtSp.setSelection(
            when (posesnLadResideAtString) {
                "Y" -> 1
                "N" -> 2
                "X" -> 3
                else -> 0
            }
        )
//        if(posesnLadResideAtString.equals("Y")) {
//            view.posesnLadResideAt.isChecked = true
//        } else {
//            view.posesnLadResideAt.isChecked = false
//        }

        val posesnLadFarmerAtString = checkStringNull(farmDataJson.getString("posesnLadFarmerAt"))
        view.posesnLadFarmerAtSp.setSelection(
            when (posesnLadFarmerAtString) {
                "Y" -> 1
                "N" -> 2
                "X" -> 3
                else -> 0
            }
        )
//        if(posesnLadFarmerAtString.equals("Y")) {
//            view.posesnLadFarmerAt.isChecked = true
//        } else {
//            view.posesnLadFarmerAt.isChecked = false
//        }

        val posesnDbtamtRepAtString = checkStringNull(farmDataJson.getString("posesnDbtamtRepAt"))
        view.posesnDbtamtRepAtSp.setSelection(
            when (posesnDbtamtRepAtString) {
                "Y" -> 1
                "N" -> 2
                "X" -> 3
                else -> 0
            }
        )
//        if(posesnDbtamtRepAtString.equals("Y")) {
//            view.posesnDbtamtRepAt.isChecked = true
//        } else {
//            view.posesnDbtamtRepAt.isChecked = false
//        }

        view.posesnDbtamtRepInf.setText(checkStringNull(farmDataJson.getString("posesnDbtamtRepInf")))

        val posesnOwnerClvtCnfirmAtString = checkStringNull(farmDataJson.getString("posesnOwnerClvtCnfirmAt"))
        view.posesnOwnerClvtCnfirmAtSp.setSelection(
            when (posesnOwnerClvtCnfirmAtString) {
                "Y" -> 1
                "N" -> 2
                "X" -> 3
                else -> 0
            }
        )
//        if(posesnOwnerClvtCnfirmAtString.equals("Y")) {
//            view.posesnOwnerClvtCnfirmAt.isChecked = true
//        } else {
//            view.posesnOwnerClvtCnfirmAt.isChecked = false
//        }

        val posesnLrcdocAtString = checkStringNull(farmDataJson.getString("posesnLrcdocAt"))
        if (posesnLrcdocAtString.equals("Y")) {
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
        if (ownerCnfirmBasisClString.equals("")) {
            view.farmOwnerCnfirmBasisSpinner.setSelection(5)
        } else {
            val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5, 7)
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
        if (rwTrgetAtString.equals("")) {
            view.rwTrgetAtChk.isChecked = true
        } else {
            view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
        }

        val apasmtTrgetAtString = checkStringNull(farmDataJson.getString("apasmtTrgetAt"))
        if (apasmtTrgetAtString.equals("Y")) {
            view.apasmtTrgetAtChk.isChecked = true
        } else {
            view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")
        }

        view.includePaclrMatterEdit.setText(checkStringNull(farmDataJson.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(farmDataJson.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(farmDataJson.getString("rm")))


        if (ThingFarmObject.thingNewSearch.equals("N")) {
            // ???????????? init
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
                    val farmClvtBgnde = farmClvtLayoutFirst.getChildAt(2) as TextView // ???????????? ????????????
                    val farmClvtEndde = farmClvtLayoutFirst.getChildAt(3) as TextView // ???????????? ????????????
                    val farmClvtArText = farmClvtLayoutFirst.getChildAt(4) as TextView

                    farmClvtBgnde.setOnClickListener { // ???????????? ??????
                        wtnncUtill.wtnncDatePicker(
                            requireActivity().supportFragmentManager,
                            it as TextView,
                            "addFarmClvtBgnde"
                        )
                    }
                    farmClvtEndde.setOnClickListener { // ???????????? ??????
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



                    farmFyerIncomeText.setOnClickListener { // ????????? ??????????????? ???????????? ??????
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
            // ??????????????? init
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
                    farmSubThingUnitClSpinner.setSelection(CommonCodeInfoList.getIdxFromCodeId("A009", subUnitClString))
                    farmSubThingArComputerBasisText.setText(checkStringNull(farmSubThingObject.getString("arComputBasis")))

                    addThingViewCnt++

                }
            }

            // ?????? ??????

            farmAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for (i in 0 until farmAtchInfo!!.length()) {
                val farmAtchItem = farmAtchInfo!!.getJSONObject(i)

                val farmAtchFileInfo = farmAtchItem.getString("fileseInfo")

                // ????????????
                view.frlndLedgerBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006009" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ??????????????????
                view.farmerCnfirmBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006023" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ?????????????????????????????????
                view.fngmtRegistCnfirmBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006024" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ????????? ??????
                view.dirctCashInqireBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006021" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ??????????????????
                view.commRsgstAbstrctBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006007" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ??????????????????
                view.commLsCtrtcBtn.backgroundTintList = when (farmAtchFileInfo) {
                    "A200006019" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                // ????????????????????????
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
        for (i in 0..4) {
            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(i, null, "", "", "", "", "", "", "", "", ""))
        }

        Constants.CAMERA_ADAPTER = WtnncImageAdapter(requireContext(), Constants.CAMERA_IMAGE_ARR)

        val layoutManager = LinearLayoutManager(requireContext())

        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        includeImageViewRv.also {
            it.layoutManager = layoutManager
            it.adapter = Constants.CAMERA_ADAPTER
        }

        if (ThingFarmObject.thingNewSearch.equals("N")) {
            if (dataArray!!.length() > 0) {
                var searchImageArray = JSONArray()

                for (i in 0 until dataArray!!.length()) {
                    val dataItem = dataArray!!.getJSONObject(i)

                    if (dataItem.getString("fileseInfo").equals("A200006012")) {
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

                                    val downLoadDirectory =
                                        Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
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
            R.id.posesnClvthmTy -> when (position) { // ????????????
                2 -> farmLesseeViewGroup.visibleView()
                else -> farmLesseeViewGroup.goneView()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addFarmData() {

        // ????????? ??????
        ThingFarmObject.thingLrgeCl = "A011002" // ??????
        Log.d("farmTest", "????????? : ${ThingFarmObject.thingLrgeCl}")

        ThingFarmObject.thingSmallCl = "A039001"
        Log.d("farmTest", "????????? : ${ThingFarmObject.thingSmallCl}")

        // ????????? ??????
        ThingFarmObject.thingKnd = "????????????"//mActivity.farmSmallText.text.toString()
        Log.d("farmTest", "????????? ?????? : ${ThingFarmObject.thingKnd}")

        // ????????????
//        val farmRelateLnmString = activity!!.landSearchRelatedLnmText.text.toString()
////        if(!getString(R.string.landInfoRelatedLnmText).equals(farmRelateLnmString)) {
////            ThingFarmObject.relateLnm = farmRelateLnmString
////        }
//        ThingFarmObject.relateLnm = farmRelateLnmString

        ThingFarmObject.bgnnAr = mActivity.farmBgnnAr.text.toString() // ????????????
        Log.d("farmTest", "???????????? : ${ThingFarmObject.bgnnAr}")

        ThingFarmObject.incrprAr = mActivity.farmIncrprAr.text.toString() // ????????????
        Log.d("farmTest", "???????????? : ${ThingFarmObject.incrprAr}")

        ThingFarmObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.farmUnitSpinner.selectedItemPosition)
//            when (mActivity.farmUnitSpinner.selectedItemPosition) { // ??????
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
        Log.d("farmTest", "?????? : ${ThingFarmObject.unitCl}")

        ThingFarmObject.arComputBasis = mActivity.farmArComputBasis.text.toString() // ??????????????????
        Log.d("farmTest", "?????????????????? : ${ThingFarmObject.arComputBasis}")

        // ????????? ????????? ??????
        //????????????
        ThingFarmObject.frldbsLgalAt = when (mActivity.frldbsLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)???????????? : ${ThingFarmObject.frldbsLgalAt}")

        // ????????????
        ThingFarmObject.frldbsBasisCl = when (mActivity.frldbsBasisCl.selectedItemPosition) {
            1 -> "A113001" // ???, ???, ?????????
            2 -> "A113002" // ????????? ??????(3?????????)
            3 -> "A113003" // ???????????? ?????? ??????
            4 -> "A113004" // ?????? ????????????
            5 -> "A113005" // ??????
            6 -> "A113006" // ???????????????VH
            else -> ""
        }
        Log.d("farmTest", "(????????????)???????????? ?????? : ${ThingFarmObject.frldbsBasisCl}")
        // ???????????? ??????
        ThingFarmObject.frldbsFrldLdgrAt = when (mActivity.frldbsFrldLdgrAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)???????????? ?????? : ${ThingFarmObject.frldbsFrldLdgrAt}")

        // ????????? ????????? ??????
        //????????????
        ThingFarmObject.frmrbsLgalAt = when (mActivity.frmrbsLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)???????????? : ${ThingFarmObject.frmrbsLgalAt}")

        // ????????????
        ThingFarmObject.frmrbsBasisCl = when (mActivity.frmrbsBasisCl.selectedItemPosition) {
            1 -> "A114001" // 1,000??? ?????? ??????
            2 -> "A114002" // 1?????? 90??? ?????? ??????
            3 -> "A114003" // 330??? ?????? VH
            4 -> "A114004" // ??????
            else -> ""
        }
        Log.d("farmTest", "(????????????)???????????? ?????? : ${ThingFarmObject.frmrbsBasisCl}")

        // ?????????????????? ??????
        ThingFarmObject.frmrbsCnfrmnDta1At = when (mActivity.frmrbsCnfrmnDta1At.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)?????????????????? ?????? : ${ThingFarmObject.frmrbsCnfrmnDta1At}")

        //???????????????????????? ????????? ??????
        ThingFarmObject.frmrbsCnfrmnDta2At = when (mActivity.frmrbsCnfrmnDta2At.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)?????????????????? ?????? ????????? ?????? : ${ThingFarmObject.frmrbsCnfrmnDta2At}")

        // ????????? ???????????? ????????? ?????? ??????
//        ThingFarmObject.frmrbsDbtamtAt = when (mActivity.frmrbsDbtamtAt.isChecked) {
//            true -> "Y"
//            else -> "N"
//        }

        ThingFarmObject.frmrbsDbtamtAt = when (mActivity.frmrbsDbtamtAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""

        }
        Log.d("farmTest", "(????????????)????????? ???????????? ????????? ???????????? : ${ThingFarmObject.frmrbsDbtamtAt}")

        //????????? ???????????? ????????????
        ThingFarmObject.frmrbsAraResideAt = when (mActivity.frmrbsAraResideAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)????????? ???????????? ???????????? : ${ThingFarmObject.frmrbsAraResideAt}")

        // ????????? ????????? ??????
        // ????????????
        ThingFarmObject.posesnLgalAt = when (mActivity.posesnLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)???????????? : ${ThingFarmObject.posesnLgalAt}")

        // ???????????? ??????
        ThingFarmObject.posesnClvthmTy = when (mActivity.posesnClvthmTy.selectedItemPosition) {
            1 -> "1" // ??????
            2 -> "2" // ?????????
            else -> ""
        }
        Log.d("farmTest", "(????????????)????????? ?????? : ${ThingFarmObject.posesnClvthmTy}")

        //????????? ???????????? ?????? ??????
        ThingFarmObject.posesnLadResideAt = when (mActivity.posesnLadResideAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // ????????? ?????? ??????
        ThingFarmObject.posesnLadFarmerAt = when (mActivity.posesnLadFarmerAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // ??????????????? ?????? ????????????????????? ??????
        ThingFarmObject.posesnOwnerClvtCnfirmAt = when (mActivity.posesnOwnerClvtCnfirmAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // ????????? ????????????
        ThingFarmObject.posesnDbtamtRepAt = when (mActivity.posesnDbtamtRepAtSp.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            3 -> "X"
            else -> ""
        }

        // ????????? ?????? ??????
        ThingFarmObject.posesnDbtamtRepInf = mActivity.posesnDbtamtRepInf.text.toString()


        // ?????????????????? ????????????
        ThingFarmObject.posesnLrcdocAt = when (mActivity.posesnLrcdocAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("farmTest", "(????????????)?????????????????? ???????????? : ${ThingFarmObject.posesnLrcdocAt}")

        // ????????? ??????
        ThingFarmObject.posesnRentName = mActivity.posesnRentName.text.toString()
        Log.d("farmTest", "(????????????)????????? ?????? : ${ThingFarmObject.posesnRentName}")

        // ????????? ??????
        ThingFarmObject.posesnHireName = mActivity.posesnHireName.text.toString()
        Log.d("farmTest", "(????????????)????????? ?????? : ${ThingFarmObject.posesnHireName}")

        // ???????????? ??????
        Log.d("farmTest", "(????????????)???????????? ?????? : ${ThingFarmObject.posesnRentBgnde}")

        // ???????????? ??????
        Log.d("farmTest", "(????????????)???????????? ?????? : ${ThingFarmObject.posesnRentEndde}")

        // ?????????
        ThingFarmObject.posesnGtn = mActivity.posesnGtn.text.toString()
        Log.d("farmTest", "(????????????)????????? : ${ThingFarmObject.posesnGtn}")

        // ??????
        ThingFarmObject.posesnMtht = mActivity.posesnMtht.text.toString()
        Log.d("farmTest", "(????????????)?????? : ${ThingFarmObject.posesnMtht}")

        // ??????(??????)??????
        ThingFarmObject.posesnCntrctLc = mActivity.posesnCntrctLc.text.toString()
        Log.d("farmTest", "(????????????)??????(??????)?????? : ${ThingFarmObject.posesnCntrctLc}")

        // ??????(??????)??????(???)
        ThingFarmObject.posesnCntrctAr = mActivity.posesnCntrctAr.text.toString()
        Log.d("farmTest", "(????????????)??????(??????)??????(???) : ${ThingFarmObject.posesnCntrctAr}")

        // ??????
        ThingFarmObject.posesnSpccntr = mActivity.posesnSpccntr.text.toString()
        Log.d("farmTest", "(????????????)?????? : ${ThingFarmObject.posesnSpccntr}")


        ThingFarmObject.ownerCnfirmBasisCl = when (mActivity.farmOwnerCnfirmBasisSpinner.selectedItemPosition) {
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

        ThingFarmObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        ThingFarmObject.apasmtTrgetAt = when (mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        // ????????????
        ThingFarmObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString() // ????????????
        Log.d("farmTest", "???????????? : ${ThingFarmObject.paclrMatter}")

        ThingFarmObject.referMatter = mActivity.includereReferMatter.text.toString() // ????????????

        ThingFarmObject.rm = mActivity.includeRm.text.toString() //  ??????


        // ????????????
        var thingFarmClvtdlJson = JSONObject()
        var thingFarmClvtdlItem = JSONObject()
        var thingFarmClvtdlArray = JSONArray()

        // ??????, ?????????????????? ????????????
        var addClvtCnt = mActivity.farmRealIncomeBasisViewGroup.childCount // ????????? View ?????????

        for (i in 0 until addClvtCnt) {

            Log.d("farmTest", "******************************************************************")

            var thingFarmClvtdlItem = JSONObject()

            val farmViewGroup = mActivity.farmRealIncomeBasisViewGroup // ?????? ???????????? BaseViewGroup
            val addLayout1 = farmViewGroup.getChildAt(i) as ViewGroup // ????????? ?????? ????????? ??????

            val addLayout2 = addLayout1.getChildAt(2) as ViewGroup // 3?????? ??????
            val addChkLayout1 = addLayout2.getChildAt(0) as ViewGroup // ???????????? ViewGroup
            val addClvtAt = addChkLayout1.getChildAt(0) as CheckBox // ????????????
            val addClvt = addLayout2.getChildAt(1) as EditText // ?????????
            val addClvtAr = addLayout2.getChildAt(4) as TextView // ????????? ????????????

            val addLayout3 = addLayout1.getChildAt(5) as ViewGroup // 6?????? ??????
            val addChkLayout2 = addLayout3.getChildAt(0) as ViewGroup // ???????????? ???????????? ViewGroup
            val addRealIncomeAtSpinner = addChkLayout2.getChildAt(0) as Spinner // ???????????? ????????????
            val addRealIncomeFarmAtView = addLayout3.getChildAt(1) as ViewGroup
            val addRealIncomeFarmAt = addRealIncomeFarmAtView.getChildAt(0) as CheckBox // ???????????? ?????? ?????? ??????
            val addIncomePblctYear = addLayout3.getChildAt(2) as TextView // ????????? ??????????????? ????????????
            val addClvtIncomeRt = addLayout3.getChildAt(3) as EditText // ??????(??????)????????? ???????????????
            val addRealIncome = addLayout3.getChildAt(4) as EditText // ??????(??????)????????? ???????????????

            val addLayout4 = addLayout1.getChildAt(7) as ViewGroup // 8?????? ??????
            val addSpinnerLayout = addLayout4.getChildAt(0) as ViewGroup // ???????????? ???????????? ViewGroup
            val addRealIncomeOrgCl = addSpinnerLayout.getChildAt(0) as Spinner // ???????????? ????????????
            val addRealIncomeBasisNm = addLayout4.getChildAt(1) as EditText // ???????????? ???????????????
            val addFyerIncome = addLayout4.getChildAt(2) as EditText // ????????? ?????? ????????????

            // ????????????
            thingFarmClvtdlItem.put(
                "clvtAt", when (addClvtAt.isChecked) {
                    true -> "Y"
                    else -> "N"
                }
            )
            // ?????????
            thingFarmClvtdlItem.put("clvt", addClvt.text.toString())

            // ???????????? ??????
            if (ThingFarmObject.clvtBgndeList!!.size != 0) {
                thingFarmClvtdlItem.put("clvtBgnde", ThingFarmObject.clvtBgndeList!!.get(i))
            }

            // ???????????? ??????
            if (ThingFarmObject.clvtEnddeList!!.size != 0) {
                thingFarmClvtdlItem.put("clvtEndde", ThingFarmObject.clvtEnddeList!!.get(i))
            }

            // ????????? ????????????
            thingFarmClvtdlItem.put("clvtAr", addClvtAr.text.toString())


            // ????????? ???????????? ??????
            // ?????????????????? ??????
            thingFarmClvtdlItem.put(
                "realIncomeAt", when (addRealIncomeAtSpinner.selectedItemPosition) {
                    1 -> "Y"
                    2 -> "N"
                    3 -> "X"
                    else -> ""
                }
            )
            // ???????????????????????? ??????
            thingFarmClvtdlItem.put(
                "realIncomeFarmAt", when (addRealIncomeFarmAt.isChecked) {
                    true -> "Y"
                    else -> "N"
                }
            )

            // ????????? ??????????????? ????????????
            thingFarmClvtdlItem.put("incomePblctYear", addIncomePblctYear.text.toString())

            // ??????(??????)????????? ???????????????
            thingFarmClvtdlItem.put("clvtIncomeRt", addClvtIncomeRt.text.toString())

            // ??????(??????)????????? ?????? ?????????
            thingFarmClvtdlItem.put("realIncome", addRealIncome.text.toString())

            // ???????????? ????????????
            thingFarmClvtdlItem.put(
                "realIncomeOrgCl", when (addRealIncomeOrgCl.selectedItemPosition) {
                    1 -> "A115001" // ?????????(???????????? ???)
                    2 -> "A115002" // ????????????/?????????
                    3 -> "A115003" // ??????
                    4 -> "A115004" // ??????/?????????
                    5 -> "A115005" // ??????/?????????
                    6 -> "A115006" // ??????
                    else -> ""
                }
            )

            // ???????????? ???????????????
            thingFarmClvtdlItem.put("realIncomeBasisNm", addRealIncomeBasisNm.text.toString())

            // ????????? ?????? ????????????
            thingFarmClvtdlItem.put("fyerIncome", addFyerIncome.text.toString())

            thingFarmClvtdlArray.put(thingFarmClvtdlItem)
            Log.d("farmTest", "*****************************************************************")
        }


        thingFarmClvtdlJson.put("farmClvtdlList", thingFarmClvtdlArray)
        ThingFarmObject.addFarmClvtdlList = thingFarmClvtdlJson

        ThingFarmObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        ThingFarmObject.apasmtTrgetAt = when (mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }


        // ?????? ??? ??????
//        ThingFarmObject.strctNdStrndrd = mActivity.farmStrctNdStrndrdText.text.toString()
        ThingFarmObject.strctNdStndrd = StringBuilder().apply {

            if (ThingFarmObject.posesnClvthmTy.equals("1")) {
                append("??????(")
            } else {
                append("?????????(")
            }
            val addFarmArray = ThingFarmObject.addFarmClvtdlList!!.getJSONArray("farmClvtdlList") as JSONArray
            var addFarmItemCnt = 0
            if (addFarmArray != null) {
                for (i in 0 until addFarmArray.length()) {
                    val addFarmObject = addFarmArray.getJSONObject(i)

                    val clvt = addFarmObject.getString("clvt")
                    val realIncomeAt = addFarmObject.getString("realIncomeAt")
                    val realIncome = addFarmObject.getString("realIncome")

                    if (realIncomeAt.equals("Y")) {
                        append(clvt + "," + realIncome)
                    } else {
                        append(clvt)
                    }
                    addFarmItemCnt++
                    if (addFarmItemCnt != addFarmArray.length() - 1) {
                        append(",")
                    }
                }

            }
            append(")")

        }.toString()

        logUtil.d("?????? ????????? -> ${ThingFarmObject.strctNdStrndrd}")

//        Log.d("farmTest", "?????? ??? ?????? : ${ThingFarmObject.strctNdStrndrd}")

        // ????????? ????????????
//        var farmAddThingList = ThingFarmObject.farmAddThingList // ?????? ????????? ?????? ?????????
        var thingFarmAddJson = JSONObject()
        var thingFarmAddArray = JSONArray()
        var addThingCnt = mActivity.farmBaseViewGroup.childCount // ????????? ????????? ?????????
        if (addThingCnt > 0) {
            for (i in 0 until addThingCnt) {
                Log.d("farmTest", "******************************************************************")

                var thingFarmAddItem = JSONObject()
                //            val farmAddItemMap: MutableMap<String, String> = mutableMapOf()
                val farmViewGroup = mActivity.farmBaseViewGroup // ?????? ?????? Base ViewGroup
                val addLayoutItem = farmViewGroup.getChildAt(i) as ViewGroup // ????????? i?????? view

                val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup // 2?????? ??????
                val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup // ????????? ViewGroup
                val addSmallClSpinner = addSpinnerLayout1.getChildAt(0) as Spinner // ?????????
                val addThingKnd = addViewGroup1.getChildAt(1) as EditText // ????????? ??????
                val addStrctStndrd = addViewGroup1.getChildAt(2) as EditText // ?????? ??? ??????

                val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup // 4?????? ??????
                val addAreaViewGroup = addViewGroup2.getChildAt(0) as ViewGroup // ?????? ??? ?????? ViewGroup
                val addAllAr = addAreaViewGroup.getChildAt(0) as EditText // ????????????(??????)
                val addIncrpr = addAreaViewGroup.getChildAt(1) as EditText // ????????????(??????)
                val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup // ?????? ViewGroup
                val addUnit = addSpinnerLayout2.getChildAt(0) as Spinner // ??????
                val addArComputBasis = addViewGroup2.getChildAt(2) as EditText // ??????????????????

                // ?????????
                thingFarmAddItem.put(
                    "thingSmallCl", when (addSmallClSpinner.selectedItemPosition) {
                        1 -> "A039002" // ???????????????
                        2 -> "A039003" // ?????????
                        3 -> "A039004" // ?????????
                        else -> ""
                    }
                )

                // ????????? ??????
                thingFarmAddItem.put("thingKnd", addThingKnd.text.toString())

                // ?????? ??? ??????
                thingFarmAddItem.put("strctNdStrndrd", addStrctStndrd.text.toString())

                // ????????????
                thingFarmAddItem.put("bgnnAr", addAllAr.text.toString())

                // ????????????
                thingFarmAddItem.put("incrprAr", addIncrpr.text.toString())

                // ??????
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

                // ??????????????????
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
     * ???????????? ??? ???????????? ????????? ?????? (????????????????????? ???????????? ?????? ???????????? ?????????)
     * @param currentArea ?????? ??????
     */

    fun addTableRow(currentArea: Int?) {

        ThingFarmObject.thingFarmPolygonCurrentArea = currentArea // ????????????

        val farmViewGroup = farmRealIncomeBasisViewGroup
        val addFarmView = R.layout.fragment_farm_add_item
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(addFarmView, null)
        val itemView = inflater.inflate(addFarmView, null)
        farmViewGroup?.addView(itemView)

        val farmClvtdlView = farmViewGroup.getChildAt(addClvtViewCnt) as ViewGroup // ????????? ????????? ViewGroup

        val farmClvtdlViewFirst = farmClvtdlView.getChildAt(2) as ViewGroup // 3?????? ??????
        val farmClvtBgnde = farmClvtdlViewFirst.getChildAt(2) as TextView // ???????????? ????????????
        val farmClvtEndde = farmClvtdlViewFirst.getChildAt(3) as TextView // ???????????? ????????????

        // TODO: 2021-11-16 ???????????? ???????????? ??????
        var currentFarmTextView: TextView = farmClvtdlViewFirst.getChildAt(4) as TextView
        currentFarmTextView.text = currentArea.toString()

        val addViewGroup2 = farmClvtdlView.getChildAt(5) as ViewGroup // 6?????? ??????
        val farmRealIncomeAtView = addViewGroup2.getChildAt(0) as ViewGroup
        val farmRealIncomeSpinner = farmRealIncomeAtView.getChildAt(0) as Spinner
        val addFarmIncomeDtaPblctYear = addViewGroup2.getChildAt(2) as TextView // ????????? ??????????????? ???????????? ????????????
        val addViewGroup3 = farmClvtdlView.getChildAt(7) as ViewGroup // 8?????? ??????
        val addSpinnerViewGroup = addViewGroup3.getChildAt(0) as ViewGroup // ???????????? ??????????????? ViewGroup
        val addFarmIncomeOriginSe = addSpinnerViewGroup.getChildAt(0) as Spinner // ???????????? ???????????? ????????????

        wtnncUtill.wtnncSpinnerAdapter(R.array.realIncomeAt, farmRealIncomeSpinner, this) // ???????????? ???????????? Spinner


        farmClvtBgnde.setOnClickListener { // ???????????? ??????
            wtnncUtill.wtnncDatePicker(requireActivity().supportFragmentManager, it as TextView, "addFarmClvtBgnde")
        }
        farmClvtEndde.setOnClickListener { // ???????????? ??????
            wtnncUtill.wtnncDatePicker(requireActivity().supportFragmentManager, it as TextView, "addFarmClvtEndde")
        }

        addFarmIncomeDtaPblctYear.setOnClickListener { // ????????? ??????????????? ???????????? ??????
            wtnncUtill.wtnncYearPicker(fragmentActivity, it as TextView, "incomePblctYear")
        }

        wtnncUtill.wtnncSpinnerAdapter(
            R.array.farmIncomeOriginSeArray,
            addFarmIncomeOriginSe,
            this
        ) // ???????????? ???????????? Spinner


        addClvtViewCnt++
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
    }

}