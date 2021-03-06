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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_residnt_search.*
import kotlinx.android.synthetic.main.fragment_residnt_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.thing_buld_link_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.BuldSelectListAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.CommonCodeInfoList
import kr.or.kreb.ncms.mobile.data.ThingResidntObject
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
import kotlin.collections.HashMap
import kotlin.collections.set

class ResidntSearchFragment(activity: Activity, context: Context, val fragmentActivity: FragmentActivity) : BaseFragment(),
    AdapterView.OnItemSelectedListener,
    DialogUtil.ClickListener  {

    private lateinit var residntTypeView: View
    private val mActivity = activity
    private val mContext = context
    private val wtnncUtill = WtnncUtil(activity, context)
    private var addResidntViewCnt: Int = 0 // ?????? ???????????????
    private var addThingViewCnt: Int = 0 // ?????? ?????????
    private var addThingBuldViewCnt: Int = 0
//    private var selectBuldLinkData: ArrayList<BuldSelectListInfo>? = null

//    private var saupCodeInfo: String = ""
//    private var incrprLnmInfo: String = ""

//    private var logUtil: LogUtil = LogUtil("ResidntSearchFragment")
//    private val toastUtil = ToastUtil(context)
//    var dialogUtil: DialogUtil? = null
//    private var progressDialog: AlertDialog? = null

    var wtnncImageAdapter: WtnncImageAdapter? = null

    var dcsnAt: String? = "N"
//    var builder: MaterialAlertDialogBuilder? = null

    val wonFormat = DecimalFormat("#,###")
    private var residntAtchInfo: JSONArray? = null

    lateinit var materialDialog: Dialog
    init {
//        Constants.CAMERA_IMAGE_ARR.clear()
//        Constants.CAMERA_IMGAE_INDEX = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        residntTypeView = inflater.inflate(R.layout.fragment_residnt_search, null)
        dialogUtil = DialogUtil(context, activity)
        dialogUtil!!.setClickListener(this)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        return residntTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        wtnncUtill.wtnncSpinnerAdapter(R.array.residntSmallSclas, residntSclasSpinner, this) // ?????????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, residntUnitSpinner, this) // ??????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.residntSeArray, residntPssPssCl, this) // ????????????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.residntTyArray, residntTy, this) // ????????? ??????


        init(view)

        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.RESIDNT,
                "A200006012",
                "????????????",
                CameraEnum.DEFAULT
            )
        }

        searchShetchBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, null)
        }

        residntBgnnAr.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.bgnnAr = txtString
            }
            false
        }
        residntIncrprAr.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.incrprAr = txtString
            }
            false
        }
        residntPssRentName.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssRentName = txtString
            }
            false
        }
        residntPssHireName.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssHireName = txtString
            }
            false
        }
        residntPssGtn.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssGtn = txtString
            }
            false
        }
        residntPssMtht.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssMtht = txtString
            }
            false
        }
        residntPssCntrctLc.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssCntrctLc = txtString
            }
            false
        }
        residntPssCntrctAr.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssCntrctAr = txtString
            }
            false
        }
        residntPssSpccntr.setOnEditorActionListener{ textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                ThingResidntObject.pssSpccntr = txtString
            }
            false
        }
//        reincrprBgnnBsnsNm.setOnEditorActionListener{ textView, action, event ->
//            if(action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//
//                ThingResidntObject.pssSpccntr = txtString
//            }
//            false
//        }



        // ????????????
//        residntPssRent.setOnClickListener {
//            wtnncUtill.wtnncDateRangePicker(
//                requireActivity().supportFragmentManager,
//                it as TextView,
//                "residntPssRent"
//            )
//        }
        residntPssRentBgnDe.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                it as TextView,
                "residntPssRentBgnDe"
            )
        }
        residntPssRentEndde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                it as TextView,
                "residntPssRentEndde"
            )
        }



        // ??????????????? ??? ??????
        addResidntDtlsBtn.setOnClickListener {
            val residntViewGroup = addResidntDtlsBaseViewGroup // ?????? ??????????????? BaseViewGroup
            val addResidntView = R.layout.fragment_residnt_add_dtls_item // ???????????? Layout
            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = inflater.inflate(addResidntView, null)
            residntViewGroup?.addView(itemView)

            val addlayoutItem = residntViewGroup.getChildAt(addResidntViewCnt) as ViewGroup // ????????? ????????? ViewGroup
            val addViewGroup1 = addlayoutItem.getChildAt(1) as ViewGroup // 2?????? ??????
            val addSpinnerViewGroup1 = addViewGroup1.getChildAt(3) as ViewGroup // ????????? ?????? ViewGroup
            val addResidntTy = addSpinnerViewGroup1.getChildAt(0) as Spinner // ????????? ??????

            val addViewGroup2 = addlayoutItem.getChildAt(3) as ViewGroup // 4?????? ??????
            val addResidePdBgnde = addViewGroup2.getChildAt(1) as TextView // ????????????
            val addResidePdEndde = addViewGroup2.getChildAt(2) as TextView // ????????????
            val addResideSe = addViewGroup2.getChildAt(3) as EditText
//            val addBtnViewGroup = addViewGroup2.getChildAt(2) as ViewGroup // ?????? ViewGroup
//            val addRsgstAbstrctBtn = addBtnViewGroup.getChildAt(0) as Button // ?????????????????? ??????
//            val addEtcBasisDataBtn = addBtnViewGroup.getChildAt(1) as Button // ?????? ???????????? ??????

            wtnncUtill.wtnncSpinnerAdapter(R.array.residntTyArray, addResidntTy, this) // ????????? ??????

            addResidePdBgnde.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    it as TextView,
                    "addResidePdBgnde"
                )
            }
            addResidePdEndde.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    it as TextView,
                    "addResidePdEndde"
                )
            }

//            addRsgstAbstrctBtn.setOnClickListener {
//                toastUtil.msg("?????? ???????????? ?????? ????????? ???????????????.", 100)
//            }
//
//            addEtcBasisDataBtn.setOnClickListener {
//                toastUtil.msg("?????? ?????? ???????????? ????????? ???????????????.", 100)
//            }

            addResidntViewCnt++
            if(dcsnAt == "Y") {

            }

        }

//        // ????????? ??????
//        residntAddThingLayoutBtn.setOnClickListener {
//
//            val residntViewGroup = residntThingBaseViewGroup // ???????????? ???????????? ??????
//            var addThingView = R.layout.fragment_farm_add_thing_item // ????????? ?????? Layout
//            val inflater: LayoutInflater =
//                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            val itemView = inflater.inflate(addThingView, null)
//            residntViewGroup?.addView(itemView)
//
//            // ????????? spinner
//            val addLayoutItem = residntViewGroup.getChildAt(addThingViewCnt) as ViewGroup //???????????? layout??? ?????? ViewGroup
//            val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup // 2?????? ??????
//            val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup // 2?????? ????????? ????????? ??????
//            val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner // ????????? Spinner
//            wtnncUtill.wtnncSpinnerAdapter(R.array.residntSmallSclas, addSpinner1, this)
//
//            // ????????? spinner
//            val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup // 4?????? ??????
//            val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup // 4?????? ????????? ????????? ??????
//            val addSpinner2 = addSpinnerLayout2.getChildAt(0) as Spinner // ??????
//            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)
//
//            addThingViewCnt++
//        }


//        // ????????? ????????? ??????
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

        //??????????????????
        commRsgstAbstrctBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val residntAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if(ThingResidntObject.thingNewSearch.equals("N")) {
                for(i in 0 until residntAtchInfo!!.length()) {
                    val residntAtchItem = residntAtchInfo!!.getJSONObject(i)
                    if(residntAtchItem.getString("fileseInfo").equals("A200006007")) {
                        array.add(residntAtchItem.getString("rgsde"))
                        residntAtchSelectArray.put(residntAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006007", "??????????????????")
                        } else {
                            val item = residntAtchSelectArray.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") {_, _ ->
                        logUtil.d("setNegativeButton ------------------------->" )
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ????????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()

            }
        }
        //??????????????????
        commRsgstTrnscrBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val residntAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if(ThingResidntObject.thingNewSearch.equals("N")) {
                for(i in 0 until residntAtchInfo!!.length()) {
                    val residntAtchItem = residntAtchInfo!!.getJSONObject(i)
                    if(residntAtchItem.getString("fileseInfo").equals("A200006008")) {
                        array.add(residntAtchItem.getString("rgsde"))
                        residntAtchSelectArray.put(residntAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006007", "??????????????????")
                        } else {
                            val item = residntAtchSelectArray.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") {_, _ ->
                        logUtil.d("setNegativeButton ------------------------->" )
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ????????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()

            }
        }
        //?????????????????????
        commTrnsFrnHshldRdBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val residntAtchSelectArray = JSONArray()

            array.add("????????????????????? ??????")
            if(ThingResidntObject.thingNewSearch.equals("N")) {
                for(i in 0 until residntAtchInfo!!.length()) {
                    val residntAtchItem = residntAtchInfo!!.getJSONObject(i)
                    if(residntAtchItem.getString("fileseInfo").equals("A200006026")) {
                        array.add(residntAtchItem.getString("rgsde"))
                        residntAtchSelectArray.put(residntAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("????????????????????? ??????")
                    .setPositiveButton("??????") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006026", "?????????????????????")
                        } else {
                            val item = residntAtchSelectArray.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") {_, _ ->
                        logUtil.d("setNegativeButton ------------------------->" )
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ????????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()

            }
        }
        //??????????????????
        commLsCtrtcBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val residntAtchSelectArray = JSONArray()

            array.add("?????????????????? ??????")
            if(ThingResidntObject.thingNewSearch.equals("N")) {
                for(i in 0 until residntAtchInfo!!.length()) {
                    val residntAtchItem = residntAtchInfo!!.getJSONObject(i)
                    if(residntAtchItem.getString("fileseInfo").equals("A200006019")) {
                        array.add(residntAtchItem.getString("rgsde"))
                        residntAtchSelectArray.put(residntAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("?????????????????? ??????")
                    .setPositiveButton("??????") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006019", "??????????????????")
                        } else {
                            val item = residntAtchSelectArray.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("??????") {_, _ ->
                        logUtil.d("setNegativeButton ------------------------->" )
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSignleChoiceItems----------------------------->")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                dialogUtil!!.confirmDialog("?????? ?????? ?????? ????????? ?????? ?????? ??? ?????? ??? ????????? ????????????.", builder!!, "??????").show()

            }
        }

    }

    fun callThingCapture(fileCode: String, fileCodeNm: String) {
        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.RESIDNT,
            fileCode,
            fileCodeNm,
            CameraEnum.DOCUMENT
        )
    }

    fun callThingFileDownload(item: JSONObject) {
        val thingAtchFileMap = java.util.HashMap<String, String>()
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

        var requireArr = mutableListOf<TextView>(view.tv_residnt_require1)
        setRequireContent(requireArr)

        wtnncUtill.wtnncSpinnerAdapter(R.array.residntSmallSclas, residntSclasSpinner, this) // ?????????
        // A009
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, residntUnitSpinner, this) // ??????
        wtnncUtill.wtnncSpinnerAdapter("A009", residntUnitSpinner, this) // ??????
        wtnncUtill.wtnncSpinnerAdapter(R.array.residntSeArray, residntPssPssCl, this) // ????????????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.residntTyArray, residntTy, this) // ????????? ??????
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, residntAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, residntInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, residntOwnerCnfirmBasisSpinner, this)

        val dataString = requireActivity().intent!!.extras!!.get("ResidntInfo") as String

        var dataJson = JSONObject(dataString)

        var thingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        ThingResidntObject.thingInfo = thingDataJson

        logUtil.d("residntInfo dataJson ------------------------> $thingDataJson")


        var saupCodeInfo = checkStringNull(thingDataJson.getString("saupCode"))
        var incrprLnmInfo = checkStringNull(thingDataJson.getString("incrprLnm"))

        if(ThingResidntObject.thingNewSearch.equals("Y")) {
            var buldSelectMap = HashMap<String, String>()

            buldSelectMap.put("saupCode", saupCodeInfo)
            buldSelectMap.put("incrprLnm", incrprLnmInfo)

            val thingBuldSelectUrl = context!!.resources.getString(R.string.mobile_url) + "thingBuldResidntSelect"

            val progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostWebServer(buldSelectMap, progressDialog, thingBuldSelectUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog.dismiss()
                        logUtil.e("fail")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        logUtil.d("responseString buldSelect response >>>>>>>>>> $responseString")

                        progressDialog.dismiss()

                        val buldSelectJson = JSONObject(responseString).getJSONObject("list").getJSONArray("ThingSearch")

                        layoutInflater.inflate(R.layout.thing_buld_link_dialog, null).let { view ->
                            view.addBuldLinkListView.adapter = BuldSelectListAdapter(context!!)

                            for(i in 0 until buldSelectJson.length()) {
                                (view.addBuldLinkListView.adapter as BuldSelectListAdapter).addItem(buldSelectJson.getJSONObject(i))
                            }
                            val buldSelectDialog = BuldSelectDialogFragment(mContext, mActivity, view).apply {
                                isCancelable = false
                                show(fragmentActivity.supportFragmentManager, "buldSelectDialog")
                            }

                            view.cancelBtn.setOnClickListener {
                                buldSelectDialog.dismiss()
                            }

                            view.selectInputBtn.setOnClickListener {
                                var selectBuldLinkData = (view.addBuldLinkListView.adapter as BuldSelectListAdapter).getSelectItem()

                                logUtil.d("selectData Size ---------------------->${selectBuldLinkData.size}")

                                if(selectBuldLinkData.size >0) {
                                    for(data in selectBuldLinkData) {
                                        val buldLinkViewGroup = addResidntBuldLinkViewGroup
                                        val addThingView = R.layout.fragment_thing_add_buld_link_item
                                        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                                        inflater.inflate(addThingView, null)
                                        val itemView = inflater.inflate(addThingView, null)

                                        buldLinkViewGroup?.addView(itemView)

                                        val addViewGroup = mActivity.addResidntBuldLinkViewGroup
                                        val addLayout1 = addViewGroup.getChildAt(addThingBuldViewCnt) as ViewGroup
                                        val addLayout2 = addLayout1.getChildAt(1) as ViewGroup
                                        val addBuldThingKndText = addLayout2.getChildAt(1) as TextView
                                        val addBuldThingArText = addLayout2.getChildAt(2) as TextView
                                        val addNrtBuldAtView = addLayout2.getChildAt(3) as ViewGroup
                                        val addNrtBuldAtChk = addNrtBuldAtView.getChildAt(0) as CheckBox

                                        addBuldThingKndText.text = data.thingKnd
                                        addBuldThingArText.text = data.incrprAr

                                        addNrtBuldAtChk.isChecked = data.nrtBuldAt.equals("Y")

                                        val addLayout3 = addLayout1.getChildAt(3) as ViewGroup
                                        val addBuldPrpos = addLayout3.getChildAt(0) as TextView
                                        val addRegstrPrpos = addLayout3.getChildAt(1) as TextView
                                        val addRgistPrpos = addLayout3.getChildAt(2) as TextView

                                        addBuldPrpos.text = data.buldPrpos
                                        addRegstrPrpos.text = data.regstrBuldPrpos
                                        addRgistPrpos.text = data.rgistBuldPrpos


                                        addThingBuldViewCnt++
                                    }
                                    ThingResidntObject.selectBuldLinkData = selectBuldLinkData

                                    dialogUtil?.run {
                                        alertDialog(
                                            "????????? ????????? ??????",
                                            "????????? ?????? ???????????? ????????? ???????????? ?????? ???????????????????",
                                            builder!!,
                                            "residntSkitchConfirm"
                                        ).show()
                                    }
                                }
                                buldSelectDialog.dismiss()
                            }
                        }
                    }

                })
        }

        dcsnAt = checkStringNull(thingDataJson.getString("dcsnAt"))

        //????????????
        view.landSearchLocationText.text = checkStringNull(thingDataJson.getString("legaldongNm"))
        view.thingdcsnAtText.text = dcsnAt
        view.landSearchBgnnLnmText.text = checkStringNull(thingDataJson.getString("bgnnLnm"))
        view.landSearchincrprLnmText.text = checkStringNull(thingDataJson.getString("incrprLnm"))
        view.landSearchNominationText.text = checkStringNull(thingDataJson.getString("gobuLndcgrNm"))

        val relateLnmString = checkStringNull(thingDataJson.getString("relateLnm"))
//        if(relateLnmString == "") {
//            view.landSearchRelatedLnmText.setText("??????")
//        } else {
            view.landSearchRelatedLnmText.setText(relateLnmString)
//        }

        view.landSearchBgnnArText.text = checkStringNull(thingDataJson.getString("ladBgnnAr"))
        view.landSearchIncrprArText.text = checkStringNull(thingDataJson.getString("ladIncrprAr"))
        view.landSearchOwnerText.text = checkStringNull(thingDataJson.getString("landOwnerName"))
        view.landSearchOwnerRText.text = checkStringNull(thingDataJson.getString("landRelatesName"))

        val residntWtnCodeString = thingDataJson.getInt("residntWtnCode")
        if(residntWtnCodeString == 0) {
            view.residntWtnCodeText.text = "????????????"
        } else {
            view.residntWtnCodeText.text = residntWtnCodeString.toString()
        }

        val residntThingSmallCl = checkStringNull(thingDataJson.getString("thingSmallCl"))
        val residntThingSmallNm = checkStringNull(thingDataJson.getString("thingSmallNm"))
        if(residntThingSmallCl.equals("")) {
            view.residntSclasSpinner.setSelection(0)
        } else {
            val residntThingSmallClSub = residntThingSmallCl.substring(5,7)
            view.residntSclasSpinner.setSelection(Integer.valueOf(residntThingSmallClSub))
        }
        view.residntSclasSpinner.isEnabled = ThingResidntObject.thingNewSearch.equals("Y")

        val residntThingKndString = checkStringNull(thingDataJson.getString("thingKnd"))
        if(residntThingKndString.equals("")) {
            view.residntThingKndText.text = "????????????"
        } else {
            view.residntThingKndText.text = residntThingKndString
        }
        val residntStrctNdStndrdString = checkStringNull(thingDataJson.getString("strctNdStndrd"))
        if(residntStrctNdStndrdString.equals("")) {
            view.residntStrctNdStrndrdText.text = "????????????"
        } else {
            view.residntStrctNdStrndrdText.text = residntStrctNdStndrdString
        }

        view.residntBgnnAr.setText(checkStringNull(thingDataJson.getString("bgnnAr")))
        view.residntIncrprAr.setText(checkStringNull(thingDataJson.getString("incrprAr")))

        val residntUnitClString = checkStringNull(thingDataJson.getString("unitCl"))
//        if(residntUnitClString.equals("")) {
//            view.residntUnitSpinner.setSelection(0)
//        } else {
//            val residntUnitClStringSub = residntUnitClString.substring(5,7)
//            view.residntUnitSpinner.setSelection(Integer.valueOf(residntUnitClStringSub))
//        }
        view.residntUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", residntUnitClString) )

        view.residntArComputBasis.setText(checkStringNull(thingDataJson.getString("arComputBasis")))



        //????????? ?????????

        val pssLgalAtString = checkStringNull(thingDataJson.getString("pssLgalAt"))
        view.residntPssLgalAtChk.isChecked = pssLgalAtString.equals("Y")

        val pssPssClString = checkStringNull(thingDataJson.getString("pssPssCl"))
        if(pssPssClString.equals("")) {
            view.residntPssPssCl.setSelection(0)
        } else {
            val pssPssClStringSub = pssPssClString.substring(5,7)
            view.residntPssPssCl.setSelection(Integer.valueOf(pssPssClStringSub))
        }

        val pssHireCntrctAtString = checkStringNull(thingDataJson.getString("pssHireCntrctAt"))
        view.residntPssHireCntrctAt.isChecked = pssHireCntrctAtString.equals("Y")

        view.residntPssRentName.setText(checkStringNull(thingDataJson.getString("pssRentName")))
        view.residntPssHireName.setText(checkStringNull(thingDataJson.getString("pssHireName")))

        view.residntPssRentBgnDe.text = checkStringNull(thingDataJson.getString("pssRentBgnde"))
        view.residntPssRentEndde.text = checkStringNull(thingDataJson.getString("pssRentEndde"))
//        view.residntPssRent.setText(pssRentBgndeString + "~" + pssRentEnddeString)

        view.residntPssGtn.setText(checkStringNull(thingDataJson.getString("pssGtn")))
        view.residntPssMtht.setText(checkStringNull(thingDataJson.getString("pssMtht")))
        view.residntPssCntrctLc.setText(checkStringNull(thingDataJson.getString("pssCntrctLc")))
        view.residntPssCntrctAr.setText(checkStringNull(thingDataJson.getString("pssCntrctAr")))
        view.residntPssSpccntr.setText(checkStringNull(thingDataJson.getString("pssSpccntr")))


        val ownerCnfirmBasisClString = checkStringNull(thingDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmBasisClString.equals("")) {
            view.residntOwnerCnfirmBasisSpinner.setSelection(5)
        } else {
            val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5,7)
            view.residntOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
        }

//        val acqsClString = checkStringNull(thingDataJson.getString("acqsCl"))
//        if(acqsClString.equals("")) {
//            view.residntAcqsSeSpinner.setSelection(0)
//        } else {
//            val acqsClStringsub = acqsClString.substring(5,7)
//            view.residntAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringsub))
//        }
//
//        val inclsClString = checkStringNull(thingDataJson.getString("inclsCl"))
//        if(inclsClString.equals("")) {
//            view.residntInclsSeSpinner.setSelection(0)
//        } else {
//            val inclsClStringsub = inclsClString.substring(5,7)
//            view.residntInclsSeSpinner.setSelection(Integer.valueOf(inclsClStringsub))
//        }


        val rwTrgetAtString = checkStringNull(thingDataJson.getString("rwTrgetAt"))
        if(rwTrgetAtString.equals("")) {
            view.rwTrgetAtChk.isChecked = true
        } else {
            view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
        }


        val apasmtTrgetAtString = checkStringNull(thingDataJson.getString("apasmtTrgetAt"))
        view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")


        if(ThingResidntObject.thingNewSearch.equals("N")) {
            // ????????? ?????????

            val residntBuldLinkArray = dataJson.getJSONArray("residntBuldLink") as JSONArray
            for(i in 0 until residntBuldLinkArray.length()) {
                addThingBuldViewCnt = i

                val residntBuldLinkObject = residntBuldLinkArray.getJSONObject(i)

                val buldLinkViewGroup = addResidntBuldLinkViewGroup
                val addThingView = R.layout.fragment_thing_add_buld_link_item
                val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                inflater.inflate(addThingView, null)
                val itemView = inflater.inflate(addThingView, null)

                buldLinkViewGroup?.addView(itemView)

//                    val addViewGroup = buldLinkViewGroup.addResidntBuldLinkViewGroup
                val addLayout1 = buldLinkViewGroup.getChildAt(addThingBuldViewCnt) as ViewGroup
                val addLayout2 = addLayout1.getChildAt(1) as ViewGroup
                val addAraLgalAtView = addLayout2.getChildAt(0) as ViewGroup
                val addAraLganAtChk = addAraLgalAtView.getChildAt(0) as CheckBox
                val addBuldThingKndText = addLayout2.getChildAt(1) as TextView
                val addBuldThingArText = addLayout2.getChildAt(2) as TextView
                val addNrtBuldAtView = addLayout2.getChildAt(3) as ViewGroup
                val addNrtBuldAtChk = addNrtBuldAtView.getChildAt(0) as CheckBox


                val araLgalAtString = checkStringNull(residntBuldLinkObject.getString("araLgalAt"))
                addAraLganAtChk.isChecked = araLgalAtString.equals("Y")

                addBuldThingKndText.text = checkStringNull(residntBuldLinkObject.getString("thingKnd"))
                addBuldThingArText.text = checkStringNull(residntBuldLinkObject.getString("incrprAr"))

                val nrtBuldAtString = checkStringNull(residntBuldLinkObject.getString("nrtBuldAt"))

                addNrtBuldAtChk.isChecked = nrtBuldAtString.equals("Y")

                val addLayout3 = addLayout1.getChildAt(3) as ViewGroup
                val addBuldPrpos = addLayout3.getChildAt(0) as TextView
                val addRegstrPrpos = addLayout3.getChildAt(1) as TextView
                val addRgistPrpos = addLayout3.getChildAt(2) as TextView

                addBuldPrpos.text = checkStringNull(residntBuldLinkObject.getString("buldPrpos"))
                addRegstrPrpos.text = checkStringNull(residntBuldLinkObject.getString("regstrBuldPrpos"))
                addRgistPrpos.text = checkStringNull(residntBuldLinkObject.getString("rgistBuldPrpos"))


                addThingBuldViewCnt++
                if(dcsnAt == "Y") {
                    addAraLganAtChk.isEnabled = false
                }
            }

            // ????????? ??????

            val residntDtlsArray = dataJson.getJSONArray("residntDtlsInfo") as JSONArray
            for(i in 0 until residntDtlsArray.length()) {
            val residntViewGroup = addResidntDtlsBaseViewGroup // ?????? ??????????????? BaseViewGroup
            val addResidntView = R.layout.fragment_residnt_add_dtls_item // ???????????? Layout
            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = inflater.inflate(addResidntView, null)
            residntViewGroup?.addView(itemView)

            val addlayoutItem = residntViewGroup.getChildAt(addResidntViewCnt) as ViewGroup // ????????? ????????? ViewGroup
            val addViewGroup1 = addlayoutItem.getChildAt(1) as ViewGroup // 2?????? ??????
            val addResideAr = addViewGroup1.getChildAt(0) as EditText
            val addProperAtGroup = addViewGroup1.getChildAt(1) as ViewGroup
                val addProperAt = addProperAtGroup.getChildAt(0) as CheckBox
            val addName = addViewGroup1.getChildAt(2) as EditText
            val addSpinnerViewGroup1 = addViewGroup1.getChildAt(3) as ViewGroup // ????????? ?????? ViewGroup
            val addResidntTy = addSpinnerViewGroup1.getChildAt(0) as Spinner // ????????? ??????

            val addViewGroup2 = addlayoutItem.getChildAt(3) as ViewGroup // 4?????? ??????
            val addHshldrRelate = addViewGroup2.getChildAt(0) as EditText
            val addResidePdBgnde = addViewGroup2.getChildAt(1) as TextView // ????????????
            val addResidePdEndde = addViewGroup2.getChildAt(2) as TextView // ????????????
            val addResideSe = addViewGroup2.getChildAt(3) as EditText
//                val addRsgstAbstrctBtn = addBtnViewGroup.getChildAt(0) as Button // ?????????????????? ??????
//                val addEtcBasisDataBtn = addBtnViewGroup.getChildAt(1) as Button // ?????? ???????????? ??????

            wtnncUtill.wtnncSpinnerAdapter(R.array.residntTyArray, addResidntTy, this) // ????????? ??????

            val residntDtlsData = residntDtlsArray.getJSONObject(i)
            addResideAr.setText(checkStringNull(residntDtlsData.getString("resideAr")))
            val properAtString = checkStringNull(residntDtlsData.getString("properAt"))
                addProperAt.isChecked = properAtString.equals("Y")

            addName.setText(checkStringNull(residntDtlsData.getString("name")))
            val residntTyString = checkStringNull(residntDtlsData.getString("residntTy"))
            if(residntTyString.equals("")) {
                addResidntTy.setSelection(0)
            } else {
                when(residntTyString) {
                    "1" -> addResidntTy.setSelection(1)
                    "2" -> addResidntTy.setSelection(2)
                }
            }

            addHshldrRelate.setText(checkStringNull(residntDtlsData.getString("hshldrRelate")))
                addResidePdBgnde.text = checkStringNull(residntDtlsData.getString("residePdBgnde"))
                addResidePdEndde.text = checkStringNull(residntDtlsData.getString("residePdEndde"))
//                addResidePd.setText(residePdBgndeString + " ~ " + residePdEnddeString)
            addResideSe.setText(checkStringNull(residntDtlsData.getString("resideBs")))


                addResidePdBgnde.setOnClickListener {
                    wtnncUtill.wtnncDatePicker(
                        requireActivity().supportFragmentManager,
                        it as TextView,
                        "addResidePdBgnde"
                    )
                }
                addResidePdEndde.setOnClickListener {
                    wtnncUtill.wtnncDatePicker(
                        requireActivity().supportFragmentManager,
                        it as TextView,
                        "addResidePdEndde"
                    )
                }
            addResidntViewCnt++


                if(dcsnAt == "Y") {
                    addResideAr.isEnabled = false
                    addProperAt.isEnabled = false
                    addName.isEnabled = false
                    addResidntTy.isEnabled = false
                    addHshldrRelate.isEnabled = false
                    addResidePdBgnde.isEnabled = false
                    addResidePdEndde.isEnabled = false
                    addResideSe.isEnabled = false
                }

            }


            // ????????? ?????? ??????
            residntAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until residntAtchInfo!!.length()) {
                val residntAtchItem = residntAtchInfo!!.getJSONObject(i)

                val residntFileInfo = residntAtchItem.getString("fileseInfo")

                //??????????????????
                view.commRsgstAbstrctBtn.backgroundTintList = when (residntFileInfo) {
                    "A200006007" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                //??????????????????
                view.commRsgstTrnscrBtn.backgroundTintList = when (residntFileInfo) {
                    "A200006008" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                //?????????????????????
                view.commTrnsFrnHshldRdBtn.backgroundTintList = when (residntFileInfo) {
                    "A200006026" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                //??????????????????
                view.commLsCtrtcBtn.backgroundTintList = when (residntFileInfo) {
                    "A200006019" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
            }

            settingSearchCamerasView(residntAtchInfo)

        } else {
            settingSearchCamerasView(null)
        }


        if(dcsnAt == "Y") {
            toast.msg_info(R.string.searchDcsnAtThing, 1000)

            view.landSearchRelatedLnmText.isEnabled = false
            view.residntSclasSpinner.isEnabled = false
            view.residntBgnnAr.isEnabled = false
            view.residntIncrprAr.isEnabled = false
            view.residntUnitSpinner.isEnabled = false
            view.residntArComputBasis.isEnabled = false
            view.residntPssLgalAtChk.isEnabled = false
            view.residntPssPssCl.isEnabled = false
            view.residntPssHireCntrctAt.isEnabled = false
            view.residntPssRentName.isEnabled = false
            view.residntPssHireName.isEnabled = false
            view.residntPssRentBgnDe.isEnabled = false
            view.residntPssRentEndde.isEnabled = false
            view.residntPssGtn.isEnabled = false
            view.residntPssMtht.isEnabled = false
            view.residntPssCntrctLc.isEnabled = false
            view.residntPssCntrctAr.isEnabled = false
            view.residntPssSpccntr.isEnabled = false
            view.residntOwnerCnfirmBasisSpinner.isEnabled = false
            view.rwTrgetAtChk.isEnabled = false
            view.apasmtTrgetAtChk.isEnabled = false
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

        if(ThingResidntObject.thingNewSearch.equals("N")) {
            if(dataArray!!.length() > 0) {
                val searchImageArray = JSONArray()

                for(i in 0 until dataArray.length()) {
                    val dataItem = dataArray.getJSONObject(i)

                    if(dataItem.getString("fileseInfo").equals("A200006012")) {
                        searchImageArray.put(dataItem)
                    }
                }

                PermissionUtil.logUtil.d("searchImageArray length ---------------------------> ${searchImageArray.length()}")

                val thingAtchFileMapArr = ArrayList<java.util.HashMap<String, String>>()
                var item = JSONObject()

                for (i in 0 until searchImageArray.length()) {
                    item = searchImageArray.getJSONObject(i)
                    val thingAtchFileMap = java.util.HashMap<String, String>()
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
                                            "BSN",
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
            R.id.residntPssPssCl -> when (residntPssPssCl.selectedItem.toString()) { // ????????????
                "??????(?????????)" -> residntLesseeViewGroup.visibleView()
                "??????(????????????)" -> residntLesseeViewGroup.visibleView()
                else -> residntLesseeViewGroup.goneView()
            }
        }
//        when (parent?.id) {
//            R.id.residntSclasSpinner -> when (residntSclasSpinner.selectedItem.toString()) {
//                "?????????????????????" -> {
//                    posesesnLgalBasisInputView.goneView()
//                    residntDtlsBasisInputView.goneView()
////                    residntRamtCalBasisInputView.visibleView()
//                }
//                "??????" -> {
//
//                }
//                else -> {
//
//                    posesesnLgalBasisInputView.visibleView()
//                    residntDtlsBasisInputView.visibleView()
////                    residntRamtCalBasisInputView.goneView()
//
//                    var buldSelectMap = HashMap<String, String>()
//
////                    buldSelectMap.put("saupCode", saupCodeInfo)
////                    buldSelectMap.put("incrprLnm", incrprLnmInfo)
//
//
//                    val thingBuldSelectUrl = context!!.resources.getString(R.string.mobile_url) + "thingBuldSelect"
//
//                    val progressDialog = dialogUtil.progressDialog(MaterialAlertDialogBuilder(context!!))
//
//
//                }
//            }
//        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addResidntData() {


        // ????????? ??????
        ThingResidntObject.thingLrgeCl = "A011004" // ?????????

        // ?????????
        ThingResidntObject.thingSmallCl = when (mActivity.residntSclasSpinner.selectedItem.toString()) {
            "???????????????" -> "A002002"
            "?????????" -> "A002001"
            else -> ""
        }

        // ????????? ??????
        if(ThingResidntObject.thingNewSearch.equals("Y")) {
            ThingResidntObject.thingKnd = "???????????????"
        } else {
            ThingResidntObject.thingKnd = mActivity.residntThingKndText.text.toString()
        }

        // ????????????
//        val residntRelateLnmString = activity!!.landSearchRelatedLnmText.text.toString()
//        if(!getString(R.string.landInfoRelatedLnmText).equals(residntRelateLnmString)) {
//            ThingResidntObject.relateLnm = residntRelateLnmString
//        }

        ThingResidntObject.bgnnAr = mActivity.residntBgnnAr.text.toString() // ????????????

        ThingResidntObject.incrprAr = mActivity.residntIncrprAr.text.toString() // ????????????

        ThingResidntObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.residntUnitSpinner.selectedItemPosition)
//            when (mActivity.residntUnitSpinner.selectedItemPosition) { // ??????
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

        ThingResidntObject.arComputBasis = mActivity.residntArComputBasis.text.toString() // ??????????????????
        // ????????? ?????????
        //????????????
//
        val addBuldLinkCnt = mActivity.addResidntBuldLinkViewGroup.childCount
        val buldLinkArray = JSONArray()
        val buldLinkObject = JSONObject()
        for(i in 0 until addBuldLinkCnt) {
            val buldLinkItem = JSONObject()
            val buldSelectData = ThingResidntObject.selectBuldLinkData?.get(i)
            val buldLinkViewGroup = mActivity.addResidntBuldLinkViewGroup
            val addLayout1 = buldLinkViewGroup.getChildAt(i) as ViewGroup
            val addLayout2 = addLayout1.getChildAt(1) as ViewGroup
            val addAraLgalAtView = addLayout2.getChildAt(0) as ViewGroup
            val addAraLgalAtChk = addAraLgalAtView.getChildAt(0) as CheckBox

            if(addAraLgalAtChk.isChecked) {
                buldLinkItem.put("araLgalAt", "Y")
            } else {
                buldLinkItem.put("araLgalAt", "N")
            }
            buldLinkItem.put("saupCode", buldSelectData!!.saupCode)
            buldLinkItem.put("refThingWtnCode", buldSelectData.thingWtnCode)
            buldLinkItem.put("rewdAmt", "")
            buldLinkItem.put("linkCl", "buld")
            buldLinkArray.put(buldLinkItem)
        }
        buldLinkObject.put("residntBuldLink", buldLinkArray)
        ThingResidntObject.addBuldLinkList = buldLinkObject


        // ????????? ????????? ??????
        // ????????????
        ThingResidntObject.pssLgalAt = when (mActivity.residntPssLgalAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        // ???????????? ??????
        ThingResidntObject.pssPssCl = when (mActivity.residntPssPssCl.selectedItemPosition) {
            1 -> "A041001" // ??????
            2 -> "A041002" // ??????(?????????)
            3 -> "A041003" // ??????(????????????)
            else -> ""
        }

        if(ThingResidntObject.pssPssCl.equals("A041001")) {
            ThingResidntObject.pssHireCntrctAt = "X"
        } else {
            // ?????????????????? ??????
            ThingResidntObject.pssHireCntrctAt = when (mActivity.residntPssHireCntrctAt.isChecked) {
                true -> "Y"
                else -> "N"
            }
        }

        // ????????? ??????
        ThingResidntObject.pssRentName = mActivity.residntPssRentName.text.toString()

        // ????????? ??????
        ThingResidntObject.pssHireName = mActivity.residntPssHireName.text.toString()

        // ???????????? ??????
        Log.d("residntTest", "???????????? ?????? : ${ThingResidntObject.pssRentBgnde}")

        // ???????????? ??????
        Log.d("residntTest", "???????????? ?????? : ${ThingResidntObject.pssRentEndde}")

        // ?????????
        ThingResidntObject.pssGtn = mActivity.residntPssGtn.text.toString()

        // ??????
        ThingResidntObject.pssMtht = mActivity.residntPssMtht.text.toString()

        // ??????(??????)??????
        ThingResidntObject.pssCntrctLc = mActivity.residntPssCntrctLc.text.toString()

        // ??????(??????)??????(???)
        ThingResidntObject.pssCntrctAr = mActivity.residntPssCntrctAr.text.toString()

        // ??????
        ThingResidntObject.pssSpccntr = mActivity.residntPssSpccntr.text.toString()

        // ????????????
        ThingResidntObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString() // ????????????
        ThingResidntObject.referMatter = mActivity.includeReferMatterEdit.text.toString() // ????????????
        ThingResidntObject.rm = mActivity.includeRmEdit.text.toString()

        ThingResidntObject.ownerCnfirmBasisCl = when (mActivity.residntOwnerCnfirmBasisSpinner.selectedItemPosition) {
            1 -> "A035001"
            2 -> "A035002"
            3 -> "A035003"
            4 -> "A035004"
            5 -> "A035005"
            else -> ""
        }
//        ThingResidntObject.acqsCl = when (mActivity.residntAcqsSeSpinner.selectedItemPosition) {
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
//        ThingResidntObject.inclsCl = when (mActivity.residntInclsSeSpinner.selectedItemPosition) {
//            1->"A007001"
//            2->"A007002"
//            3->"A007003"
//            4->"A007004"
//            else->""
//        }

        ThingResidntObject.acqsCl = "A025001"
        ThingResidntObject.inclsCl = "A007001"


        ThingResidntObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        ThingResidntObject.apasmtTrgetAt = when(mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }


        val thingResidntDtlsJSON = JSONObject()
        val thingResidntDtlsArray = JSONArray()

        // ???????????? ????????????
        val addDtlsCnt = mActivity.addResidntDtlsBaseViewGroup.childCount // ????????? View ?????????

        for (i in 0 until addDtlsCnt) {
            val thingResidntDtlsItem = JSONObject()

            Log.d("residntTest", "******************************************************************")

            val residntViewGroup = mActivity.addResidntDtlsBaseViewGroup // ?????? ???????????? BaseViewGroup
            val addLayout1 = residntViewGroup.getChildAt(i) as ViewGroup // ????????? ?????? ????????? ??????

            val addLayout2 = addLayout1.getChildAt(1) as ViewGroup // 3?????? ??????
            val addResideAr = addLayout2.getChildAt(0) as EditText // ????????????
            val addChkLayout1 = addLayout2.getChildAt(1) as ViewGroup // ???????????? ViewGroup
            val addProperAt = addChkLayout1.getChildAt(0) as CheckBox // ????????????
            val addName = addLayout2.getChildAt(2) as EditText // ??????
            val addSpinnerLayout1 = addLayout2.getChildAt(3) as ViewGroup // ??????????????? ViewGroup
            val addResidntTy = addSpinnerLayout1.getChildAt(0) as Spinner // ???????????????

            val addLayout3 = addLayout1.getChildAt(3) as ViewGroup // 5?????? ??????
            val addHshldrRelate = addLayout3.getChildAt(0) as EditText // ??????????????? ??????
            val addResidePdBgnde = addLayout3.getChildAt(1) as TextView // ????????????
            val addResidePdEndde = addLayout3.getChildAt(2) as TextView // ????????????
            val addResideBs = addLayout3.getChildAt(3) as EditText

            // ????????????
            thingResidntDtlsItem.put("resideAr", addResideAr.text.toString())

            // ????????????
            thingResidntDtlsItem.put("properAt", when (addProperAt.isChecked) {
                true -> "Y"
                else -> "N"
            })

            // ??????
            thingResidntDtlsItem.put("name", addName.text.toString())

            // ????????? ??????
            thingResidntDtlsItem.put("residntTy", when (addResidntTy.selectedItemPosition) {
                1 -> "1" // ?????????
                2 -> "2" // ?????????
                else -> ""
            })

            // ??????????????? ??????
            thingResidntDtlsItem.put("hshldrRelate", addHshldrRelate.text.toString())

            // ???????????? ????????????
            if (ThingResidntObject.residePdBgndeList.size != 0) {
                thingResidntDtlsItem.put("residePdBgnde", ThingResidntObject.residePdBgndeList[i])
            }

            // ???????????? ????????????
            if (ThingResidntObject.residePdEnddeList.size != 0) {
                thingResidntDtlsItem.put("residePdEndde", ThingResidntObject.residePdEnddeList[i])
            }
            thingResidntDtlsItem.put("resideBs",addResideBs.text.toString())
            thingResidntDtlsArray.put(thingResidntDtlsItem)

            Log.d("residntTest", "*****************************************************************")
        }
        thingResidntDtlsJSON.put("residntDtlsList", thingResidntDtlsArray)
        ThingResidntObject.addResidntDtlsList = thingResidntDtlsJSON


        // ?????? ??? ??????
//        ThingResidntObject.strctNdStndrd = mActivity.residntStrctNdStrndrdText.text.toString()
//        Log.d("residntTest", "?????? ??? ?????? : ${ThingResidntObject.strctNdStndrd}")
        when(ThingResidntObject.thingSmallCl) {
            "A002001" -> { //?????????,
                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
                    // ??????, ????????? ????????? ?????? ??????(45???)

//                    var buldPrposString = ThingResidntObject.selectBuldLinkData!!.get(0).buldPrpos
//
//                    append("??????("+ buldPrposString + "), ")
//
//                    if(ThingResidntObject.pssLgalAt.equals("Y")) {
//                        append("??????(??????, "+when (mActivity.residntPssPssCl.selectedItemPosition) {
//                            1 -> "??????" // ??????
//                            2 -> "??????(?????????)" // ??????(?????????)
//                            3 -> "??????(????????????)" // ??????(????????????)
//                            else -> ""
//                        } + "), ")
//                    }else {
//                        append("??????(?????????, "+when (mActivity.residntPssPssCl.selectedItemPosition) {
//                            1 -> "??????" // ??????
//                            2 -> "??????(?????????)" // ??????(?????????)
//                            3 -> "??????(????????????)" // ??????(????????????)
//                            else -> ""
//                        } + "), ")
//                    }
                    append(when (mActivity.residntPssPssCl.selectedItemPosition) {
                            1 -> "??????" // ??????
                            2 -> "??????(?????????)" // ??????(?????????)
                            3 -> "??????(????????????)" // ??????(????????????)
                            else -> ""
                        } )
                    append(",")
                    append("????????? ????????? ????????????("+ThingResidntObject.pssCntrctAr+"???)")

                }.toString()
            }
            "A002002" -> {  //???????????????
                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
                    // ??????(?????????), ?????????(3???)
//                    append("")
//                    //??????
//                    val addBuldLinkList = ThingResidntObject.addBuldLinkList as JSONObject
//                    val addBuldLinkArray = addBuldLinkList.getJSONArray("residntBuldLink")
//                    var placeAraLgalAtString = "N"
//                    for(i in 0 until addBuldLinkArray.length()) {
//                        val dataItem = addBuldLinkArray.getJSONObject(i)
//
//                        if(dataItem.getString("araLgalAt").equals("Y")) {
//                            placeAraLgalAtString = "Y"
//                        }
//                    }
//                    if(placeAraLgalAtString.equals("Y")) {
//                        append("??????(??????), ")
//                    } else {
//                        append("??????(?????????), ")
//                    }
//                    if(ThingResidntObject.pssLgalAt.equals("Y")) {
//                        append("??????(??????, "+when (mActivity.residntPssPssCl.selectedItemPosition) {
//                            1 -> "??????" // ??????
//                            2 -> "??????(?????????)" // ??????(?????????)
//                            3 -> "??????(????????????)" // ??????(????????????)
//                            else -> ""
//                        } + "), ")
//                    }else {
//                        append("??????(?????????, "+when (mActivity.residntPssPssCl.selectedItemPosition) {
//                            1 -> "??????" // ??????
//                            2 -> "??????(?????????)" // ??????(?????????)
//                            3 -> "??????(????????????)" // ??????(????????????)
//                            else -> ""
//                        } + "), ")
//                    }
//
                    append(when (mActivity.residntPssPssCl.selectedItemPosition) {
                        1 -> "??????" // ??????
                        2 -> "??????(?????????)" // ??????(?????????)
                        3 -> "??????(????????????)" // ??????(????????????)
                        else -> ""
                    } )
                    append(",")
                    val addResidntDtlsList = ThingResidntObject.addResidntDtlsList as JSONObject
                    val addResidntDtlsArray = addResidntDtlsList.getJSONArray("residntDtlsList")
                    append("?????????("+addResidntDtlsArray.length().toString()+"???)")
                }.toString()
            }
//            "A002003" -> {  //???????????????
//                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
//                    append("")
//                    //??????
//                    val addBuldLinkList = ThingResidntObject.addBuldLinkList as JSONObject
//                    val addBuldLinkArray = addBuldLinkList.getJSONArray("residntBuldLink")
//                    var placeAraLgalAtString = "N"
//                    for(i in 0 until addBuldLinkArray.length()) {
//                        val dataItem = addBuldLinkArray.getJSONObject(i)
//
//                        if(dataItem.getString("araLgalAt").equals("Y")) {
//                            placeAraLgalAtString = "Y"
//                        }
//                    }
//                    if(placeAraLgalAtString.equals("Y")) {
//                        append("??????(??????), ")
//                    } else {
//                        append("??????(?????????), ")
//                    }
//                    append("??????(, "+when (mActivity.residntPssPssCl.selectedItemPosition) {
//                        1 -> "??????" // ??????
//                        else -> "?????????"
//                    } + "), ")
//                    val addResidntDtlsList = ThingResidntObject.addResidntDtlsList as JSONObject
//                    val addResidntDtlsArray = addBuldLinkList.getJSONArray("residntDtlsList")
//                    var addResidntDtlsProperAtString = "N"
//                    for(i in 0 until addResidntDtlsArray.length()) {
//                        val dataItem = addResidntDtlsArray.getJSONObject(i)
//
//                        if(dataItem.getString("properAt").equals("Y")) {
//                            addResidntDtlsProperAtString = "Y"
//                        }
//                    }
//                    if(addResidntDtlsProperAtString.equals("Y")) {
//                        append("??????(??????), ")
//                    } else {
//                        append("??????(?????????), ")
//                    }
//                    append("???????????? ????????????(0???)") // ???????????? ???????????? ??????
//                }.toString()
//            }
//            "A002004" -> {  //????????????????????????
//                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
//                    append("")
//                }.toString()
//            }
//            "A002005" -> {  //?????????????????????
//                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
//                    append("")
//                    append("?????? ???????????? ??????(")
//                    if(ThingResidntObject.reincrprProperAt.equals("Y")) {
//                        append("??????, " + ThingResidntObject.reincrprBgnnBsnsNm + "(" +ThingResidntObject.reincrprNtfcDe+"), " )
//                    } else {
//                        append("?????????, " + ThingResidntObject.reincrprBgnnBsnsNm + "(" +ThingResidntObject.reincrprNtfcDe+"), " )
//                    }
//                    append("????????????("+ ThingResidntObject.reincrprRwDe + "), ")
//                    append("?????? ???????????? ???????????? ?????????(0???)") // ???????????? ???????????? ??????
//                }.toString()
//            }
            else -> {
                ThingResidntObject.strctNdStndrd = StringBuilder().apply {
                    append("")
                }.toString()
            }
        }
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("type----------------------->: $type")
        when(type) {
            "residntSkitchConfirm" -> {
                (activity as MapActivity).getBuldLinkToGeomData(ThingResidntObject.selectBuldLinkData!![0].geoms, "residnt", true)
            }
        }
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("type----------------------->: $type")
        when(type) {
            "residntSkitchConfirm" -> {
                (activity as MapActivity).getBuldLinkToGeomData(ThingResidntObject.selectBuldLinkData!![0].geoms, "residnt", false)
            }
        }
    }


}