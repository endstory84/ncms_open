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
import kotlinx.android.synthetic.main.fragment_tomb_search.*
import kotlinx.android.synthetic.main.fragment_tomb_search.view.*
import kotlinx.android.synthetic.main.include_layout_add_btn.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.data.ThingTombObject
import kr.or.kreb.ncms.mobile.data.ThingTombObject.balmCl
import kr.or.kreb.ncms.mobile.data.ThingTombObject.balmClText
import kr.or.kreb.ncms.mobile.data.ThingTombObject.tombCl
import kr.or.kreb.ncms.mobile.data.ThingTombObject.tombClText
import kr.or.kreb.ncms.mobile.data.ThingTombObject.tombTy
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

class TombSearchFragment(activity: Activity, context: Context) : Fragment(),
    AdapterView.OnItemSelectedListener {

    private val mActivity: Activity = activity
    private val mContext: Context = context
    private lateinit var tombTypeView: View
    private var tombBurlTySelectItem: String = ""
    private var tombRlativSelectItem: String = ""
    private var tombStrctSelectItem: String = ""

    lateinit var materialDialog: Dialog

    private var logUtil: LogUtil = LogUtil("TombSearchFragment")
    private var toastUtil: ToastUtil = ToastUtil(mContext)
    private var wtnncUtill: WtnncUtil = WtnncUtil(mActivity, mContext)
    private var itemCnt: Int = 0

    private var addViewCnt: Int = 0

    private var tombAtchInfo: JSONArray? = null

    var wtnncImageAdapter: WtnncImageAdapter? = null

    var builder: MaterialAlertDialogBuilder? = null
    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null

    init { }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tombTypeView = inflater.inflate(R.layout.fragment_tomb_search, null)
        return tombTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        //사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.TOMB,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

        addTombThingBtn.setOnClickListener {
            val tombViewGroup = tombBaseViewGroup
            val addThingView = R.layout.fragment_tomb_thing_add_item
            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(addThingView, null)

            val itemView = inflater.inflate(addThingView, null)

            tombViewGroup?.addView(itemView)

            val addLayoutItem = tombViewGroup.getChildAt(addViewCnt) as ViewGroup

            val tombThingAddItemFirst = addLayoutItem.getChildAt(1) as ViewGroup
//            val tombThingAddItemSecond = addLayoutItem.getChildAt(2) as ViewGroup

            val tombThingAddThingUnit = tombThingAddItemFirst.getChildAt(4) as ViewGroup
            val tombThingAddThingUnitSpinner = tombThingAddThingUnit.getChildAt(0) as Spinner

            wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray,tombThingAddThingUnitSpinner, this)



            addViewCnt++
        }

        //물건의 종류
        tombThingKnd.setOnEditorActionListener { v, actionId, event ->
            ThingWtnObject.thingKnd = tombThingKnd.text.toString()
            false
        }


        //매장일자
        tombBurlDe.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                tombBurlDe,
                "tombBurlDe"
            )
        }

        //특이사항
        includePaclrMatterEdit.setOnEditorActionListener { v, actionId, event ->
            ThingWtnObject.paclrMatter = includePaclrMatterEdit.text.toString()
            false
        }

        //스케치버튼 클릭
        searchShetchBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, null)
        }

        thingTombBennArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingTombObject.bgnnAr = txtString
            }

            false
        }
        thingTombIncrprArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingTombObject.incrprAr = txtString
            }

            false
        }

        wtnncAddLayoutBtn.setOnClickListener { // 매장자 추가 버튼
            addTombRlativLayout(tombrlativBaseViewGroup.childCount)
        }
//        tombBuriedIhidnumEdit.setOnEditorActionListener { textView, action, event ->
//            if(action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//                if(txtString.length > 0 || txtString.length < 14) {
//                    var ihidnum1 = txtString.substring(0,6)
//                    var ihidnum2 = txtString.substring(6,13)
//                    var ihidnumString = ihidnum1 + "-" + ihidnum2
//                    tombBuriedIhidnumEdit.setText(ihidnumString)
//                } else {
//                    toastUtil.msg_error("비정상적인 주민번호 입니다. 확인 후 다시 입력해주시기 바랍니다.", 500)
//
//                }
//
//
//            }
//
//            false
//        }


//        tombAddRlativBtnLayout.setOnClickListener {
//            addTombRlativLayout(tombrlativBaseViewGroup.childCount)
//        }

        // 카메라 어댑터 세팅
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 묘적부
        landInfoPreviousCompensateBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val tombInfoLedgerArray = JSONArray()
            array.add("묘적부 등록")

            if(ThingTombObject.thingNewSearch.equals("N")) {
                for(i in 0 until tombAtchInfo!!.length()) {
                    val tombAtchItem = tombAtchInfo!!.getJSONObject(i)
                    if(tombAtchItem.getString("fileseInfo").equals("A200006025")) {
                        array.add(tombAtchItem.getString("rgsde"))
                        tombInfoLedgerArray!!.put(tombAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("묘적부 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton -------------------------> ")
                        if(checkedItem == 0) {
                            callThingCapture("A200006025", "묘적부")
                        } else {
                            val item = tombInfoLedgerArray!!.get(checkedItem-1) as JSONObject
                            callThingFileDownload(item)
                        }
                    }
                    .setNegativeButton("취소") {_,_ ->
                        logUtil.d("setNegativeButton ----------------------------->")
                    }
                    .setSingleChoiceItems(array.toTypedArray(), checkedItem) {_, which ->
                        logUtil.d("setSingleChoiceItems --------------------------------> $checkedItem")
                        checkedItem = which
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // 묘적부 등록은 조서 등록 후 진행
                dialogUtil!!.confirmDialog("현재 작성 중인 분묘 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()
            }


        }

    }

    fun callThingCapture(fileCode: String, fileCodeNm: String) {
        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.TOMB,
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
    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (parent?.id) {
            R.id.tombSeSpinner -> { //분묘구분
                when (position) {
                    0 -> { // 선택
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleText.goneView()
                        tombBytgtScaleLayout.goneView()
                        tombStrctNdStrndrd.text = ""
                        tombTy = "0"
                        tombStrctSelectItem = ""
                        tombClText = tombStrctSelectItem
                    }
                    1 -> { // 분묘
                        tombBytgtScaleText.goneView()
                        tombBytgtScaleLayout.goneView()
//                        tombBurlTySpinner.setSelection(0)
//                        tombBurlScaleSpinner.setSelection(0)
                        tombStrctNdStrndrd.text = ""
                        tombTy = "1"
                        tombStrctSelectItem = ""
                        tombClText = tombStrctSelectItem
                    }
                    2 -> { // 가묘
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleText.goneView()
                        tombBytgtScaleLayout.goneView()
                        tombStrctNdStrndrd.text = "가묘"
                        tombTy = "2"
                        tombStrctSelectItem = "가묘"
                        tombClText = tombStrctSelectItem
                    }
                }
            }

            R.id.tombRlativSpinner -> { // 분묘 연고자
                when (position) {
                    0 -> {
                        tombRlativSelectItem = ""
                        balmCl = ""
                        balmClText = ""
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    1 -> { // 유연
                        tombRlativSelectItem = "유연"
                        balmCl = "A012001"
                        balmClText = tombRlativSelectItem
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    2 -> { //무연
                        tombRlativSelectItem = "무연"
                        balmCl = "A012002"
                        balmClText = tombRlativSelectItem
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    3 -> { //기타
                        tombRlativSelectItem = "기타"
                        balmCl = "A012003"
                        balmClText = tombRlativSelectItem
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                }
            }

            R.id.tombBurlTySpinner -> { //분묘 매장자 유형
                when (position) {
                    0 -> { //선택
                        tombRlativTextLayout.visibleView()
                        tombBurlScaleSpinner.isEnabled = false
                        tombBytgtScaleSpinner.isEnabled = false
                        tombStrctNdStrndrd.text = ""
                        tombBurlTySelectItem = ""
                        tombStrctSelectItem = ""
                        tombCl = ""
                        tombClText = ""
                    }
                    1 -> { //일반
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleText.goneView()
                        tombBurlScaleSpinner.isEnabled = true
                        tombBytgtScaleSpinner.isEnabled = true
                        tombStrctNdStrndrd.text = ""
                        tombBurlTySelectItem = ""
                        tombCl = ""
                        tombClText = ""
                    }
                    2 -> { //아장
                        tombBurlScaleSpinner.isEnabled = true
                        tombBytgtScaleSpinner.isEnabled = true
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleLayout.goneView()
                        tombBurlTySelectItem = "아장"
                        tombStrctNdStrndrd.text = ""
                        tombCl = ""
                        tombClText = ""
                    }
                    3 -> { //자연장
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleText.visibleView()
                        tombBytgtScaleLayout.goneView()
                        tombBurlScaleSpinner.isEnabled = false
                        tombBytgtScaleSpinner.isEnabled = false
                        tombBurlTySelectItem = ""
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                        tombCl = "A013013"
                        tombStrctSelectItem = "자연장(납골분묘)"
                        tombClText = tombStrctSelectItem
                    }
                }
//                }
            }

            R.id.tombBurlScaleSpinner -> { //매장규모
                when (position) {
                    0 -> { // 선택
                        tombRlativTextLayout.visibleView()
                        tombBytgtScaleLayout.goneView()
                        tombBytgtScaleText.goneView()
                        tombStrctSelectItem = ""
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                        tombCl = ""
                        tombClText = tombStrctSelectItem
                    }
                    1 -> { //단장
                        tombBytgtScaleText.goneView()
                        tombBytgtScaleLayout.goneView()
                        if(balmCl.equals("A012002")) {
                            tombRlativTextLayout.goneView()
                        } else {
                            tombRlativTextLayout.visibleView()
                        }
                        tombrlativBaseViewGroup.visibleView()



                        tombIncludeBtnLayout.goneView()

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "아장"
                                tombCl = "A013007"
                                tombClText = tombStrctSelectItem

                            }
                            else -> {
                                tombStrctSelectItem = "단장"
                                tombCl = "A013001"
                                tombClText = tombStrctSelectItem
                            }
                        }
                        tombrlativBaseViewGroup.removeAllViews()

                        addTombRlativLayout(0)

                        // TODO: 2021-10-25 분묘 자동화 (연고유무 기재, 유형기재)
                        tombStrctNdStrndrd.text = when (tombTy) {
                            "2" -> "$tombRlativSelectItem $tombStrctSelectItem (가묘)"
                            else -> "$tombRlativSelectItem $tombStrctSelectItem"
                        }

                    }
                    2 -> { //합장
                        tombBytgtScaleText.visibleView()
                        tombBytgtScaleLayout.visibleView()
                        tombRlativTextLayout.visibleView()
                        tombIncludeBtnLayout.visibleView()
                        tombStrctSelectItem = ""
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                        tombCl = ""
                        tombClText = tombStrctSelectItem
                    }
                }
            }

            R.id.tombBytgtScaleSpinner -> { //합장 수 선택 이벤트
                when (position) {
                    0 -> { // 선택
                        if(ThingTombObject.thingNewSearch.equals("Y")) {
                            tombrlativBaseViewGroup.goneView()
                            tombStrctSelectItem = ""
                            tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                            tombCl = ""
                            tombClText = tombStrctSelectItem
                            Log.d("선택", "2번 : $tombCl")
                        }

                    }
                    1 -> { // 2
                        tombrlativBaseViewGroup.visibleView()
                        tombIncludeBtnLayout.goneView()
                        tombrlativBaseViewGroup.removeAllViews()


                        for (i in 0 until 2) {
                            addTombRlativLayout(i)
                        }

                        itemCnt = 0

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "2합장(아장)"
                                tombCl = "A013008"
                                tombClText = tombStrctSelectItem
                            }
                            else -> {
                                tombStrctSelectItem = "2합장"
                                tombCl = "A013002"
                                tombClText = tombStrctSelectItem
                            }
                        }
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    2 -> { // 3
                        tombrlativBaseViewGroup.visibleView()
                        tombIncludeBtnLayout.goneView()
                        tombrlativBaseViewGroup.removeAllViews()
                        for (i in 0 until 3) {
                            addTombRlativLayout(i)
                        }
                        itemCnt = 0

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "3합장(아장)"
                                tombCl = "A013009"
                                tombClText = tombStrctSelectItem
                            }
                            else -> {
                                tombStrctSelectItem = "3합장"
                                tombCl = "A013003"
                                tombClText = tombStrctSelectItem
                            }
                        }
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    3 -> {
                        tombrlativBaseViewGroup.visibleView()
                        tombIncludeBtnLayout.goneView()
                        tombrlativBaseViewGroup.removeAllViews()
                        for (i in 0 until 4) {
                            addTombRlativLayout(i)
                        }
                        itemCnt = 0

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "4합장(아장)"
                                tombCl = "A013010"
                                tombClText = tombStrctSelectItem
                            }
                            else -> {
                                tombStrctSelectItem = "4합장"
                                tombCl = "A013004"
                                tombClText = tombStrctSelectItem
                            }
                        }
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                    4 -> {
                        tombrlativBaseViewGroup.visibleView()
                        tombIncludeBtnLayout.goneView()
                        tombrlativBaseViewGroup.removeAllViews()
                        for (i in 0 until 5) {
                            addTombRlativLayout(i)
                        }
                        itemCnt = 0

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "5합장(아장)"
                                tombCl = "A013011"
                                tombClText = tombStrctSelectItem
                            }
                            else -> {
                                tombStrctSelectItem = "5합장"
                                tombCl = "A013005"
                                tombClText = tombStrctSelectItem
                            }
                        }

                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                        
                    }
                    5 -> {
                        tombrlativBaseViewGroup.visibleView()
                        tombIncludeBtnLayout.visibleView()
                        tombrlativBaseViewGroup.removeAllViews()
                        for (i in 0 until 6) {
                            addTombRlativLayout(i)
                        }
                        itemCnt = 0

                        when (tombBurlTySelectItem) {
                            "아장" -> {
                                tombStrctSelectItem = "6합장 이상(아장)"
                                tombCl = "A013012"
                                tombClText = tombStrctSelectItem
                            }
                            else -> {
                                tombStrctSelectItem = "6합장 이상"
                                tombCl = "A013006"
                                tombClText = tombStrctSelectItem
                            }
                        }
                        tombStrctNdStrndrd.text = "$tombRlativSelectItem $tombStrctSelectItem"
                    }
                }
            }
            R.id.thingTombUnitSpinner -> {
                when (position) {
                    1 -> ThingTombObject.unitCl = "A009001"
                    2 -> ThingTombObject.unitCl = "A009002"
                    3 -> ThingTombObject.unitCl = "A009003"
                    4 -> ThingTombObject.unitCl = "A009004"
                    5 -> ThingTombObject.unitCl = "A009005"
                    6 -> ThingTombObject.unitCl = "A009006"
                    7 -> ThingTombObject.unitCl = "A009007"
                    8 -> ThingTombObject.unitCl = "A009008"
                    9 -> ThingTombObject.unitCl = "A009009"
                    10 -> ThingTombObject.unitCl = "A009010"
                    11 -> ThingTombObject.unitCl = "A009011"
                    12 -> ThingTombObject.unitCl = "A009012"
                    13 -> ThingTombObject.unitCl = "A009013"
                    14 -> ThingTombObject.unitCl = "A009014"
                    15 -> ThingTombObject.unitCl = "A009015"
                    16 -> ThingTombObject.unitCl = "A009016"
                    17 -> ThingTombObject.unitCl = "A009017"
                    18 -> ThingTombObject.unitCl = "A009018"
                    19 -> ThingTombObject.unitCl = "A009019"
                    20 -> ThingTombObject.unitCl = "A009020"
                    21 -> ThingTombObject.unitCl = "A009021"
                    22 -> ThingTombObject.unitCl = "A009022"
                    23 -> ThingTombObject.unitCl = "A009023"
                    24 -> ThingTombObject.unitCl = "A009024"
                    25 -> ThingTombObject.unitCl = "A009025"
                    26 -> ThingTombObject.unitCl = "A009026"
                    27 -> ThingTombObject.unitCl = "A009027"
                    28 -> ThingTombObject.unitCl = "A009028"
                    29 -> ThingTombObject.unitCl = "A009029"
                    30 -> ThingTombObject.unitCl = "A009030"
                    31 -> ThingTombObject.unitCl = "A009031"
                    32 -> ThingTombObject.unitCl = "A009032"
                    33 -> ThingTombObject.unitCl = "A009033"
                    34 -> ThingTombObject.unitCl = "A009034"
                    35 -> ThingTombObject.unitCl = "A009035"
                    36 -> ThingTombObject.unitCl = "A009036"
                    37 -> ThingTombObject.unitCl = "A009037"
                    38 -> ThingTombObject.unitCl = "A009038"
                    39 -> ThingTombObject.unitCl = "A009039"
                    40 -> ThingTombObject.unitCl = "A009040"
                    41 -> ThingTombObject.unitCl = "A009041"
                    42 -> ThingTombObject.unitCl = "A009042"
                    43 -> ThingTombObject.unitCl = "A009043"
                    44 -> ThingTombObject.unitCl = "A009044"
                    45 -> ThingTombObject.unitCl = "A009045"
                    46 -> ThingTombObject.unitCl = "A009046"
                    47 -> ThingTombObject.unitCl = "A009047"
                    48 -> ThingTombObject.unitCl = "A009048"
                    49 -> ThingTombObject.unitCl = "A009049"
                    50 -> ThingTombObject.unitCl = "A009050"
                    51 -> ThingTombObject.unitCl = "A009051"
                    52 -> ThingTombObject.unitCl = "A009052"
                    53 -> ThingTombObject.unitCl = "A009053"
                    54 -> ThingTombObject.unitCl = "A009054"
                    55 -> ThingTombObject.unitCl = "A009055"
                    56 -> ThingTombObject.unitCl = "A009056"
                    57 -> ThingTombObject.unitCl = "A009057"
                    58 -> ThingTombObject.unitCl = "A009058"
                    59 -> ThingTombObject.unitCl = "A009059"
                    60 -> ThingTombObject.unitCl = "A009060"
                    61 -> ThingTombObject.unitCl = "A009061"
                    62 -> ThingTombObject.unitCl = "A009062"
                    63 -> ThingTombObject.unitCl = "A009063"
                    64 -> ThingTombObject.unitCl = "A009064"
                    65 -> ThingTombObject.unitCl = "A009065"
                    66 -> ThingTombObject.unitCl = "A009066"
                    67 -> ThingTombObject.unitCl = "A009067"
                    68 -> ThingTombObject.unitCl = "A009068"
                    else -> ThingTombObject.unitCl = ""
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun initTombBurldLayout(recentItemCnt: Int?, data: JSONObject) {
        itemCnt = recentItemCnt!!

        val baseViewGroup = tombrlativBaseViewGroup
        val addThingView = R.layout.fragment_tomb_addview_item
        val inflater: LayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(addThingView, null)
        val itemView = inflater.inflate(addThingView, null)
        baseViewGroup?.addView(itemView)

        val tombBurldViewGroup = baseViewGroup.getChildAt(itemCnt) as ViewGroup
        val tombBurldNameView = tombBurldViewGroup.getChildAt(0) as EditText
        val tombBurldRelateView = tombBurldViewGroup.getChildAt(1) as EditText
        val tombBurldSexSpinnerGroupView = tombBurldViewGroup.getChildAt(2) as ViewGroup
        val tombBurldSexSpinner = tombBurldSexSpinnerGroupView.getChildAt(0) as Spinner
        val tombBurldIhidnumView = tombBurldViewGroup.getChildAt(3) as EditText

        wtnncUtill.wtnncSpinnerAdapter(R.array.tombSexdstnArray, tombBurldSexSpinner, this)

        tombBurldNameView.setText(checkStringNull(data.getString("burldNm")))
        tombBurldRelateView.setText(checkStringNull(data.getString("rlarivRelate")))
//        tombBurldRelateView.setText(checkStringNull(data.getString("rlativRelate")))
        val sexString = checkStringNull(data.getString("sexdstn"))
        tombBurldSexSpinner.setSelection(when (sexString) {
            "1" ->1
            "2" ->2
            else ->0
        })
        val ihidnumString = checkStringNull(data.getString("ihidnum"))
        if(ihidnumString.equals("")) {
            tombBurldIhidnumView.setText(ihidnumString)
        } else {
            //val ihidnumStringSub = ihidnumString.substring(0,8)
            val ihidnumStringSub = withIhidNumAsterRisk(ihidnumString)
            tombBurldIhidnumView.setText(ihidnumStringSub)
        }

        tombBurldIhidnumView.setOnEditorActionListener { textView, action, event ->
            if(action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                if(txtString.isNotEmpty() || txtString.length < 14) {
                    val ihidnum1 = txtString.substring(0,6)
                    val ihidnum2 = txtString.substring(6,13)
                    val ihidnumString = "$ihidnum1-$ihidnum2"
                    tombBurldIhidnumView.setText(ihidnumString)
                } else {
                    toastUtil.msg_error("비정상적인 주민번호 입니다. 확인 후 다시 입력해주시기 바랍니다.", 500)

                }


            }

            false
        }

        itemCnt++

    }

    fun addTombRlativLayout(presentItemCnt: Int?) {

        itemCnt = presentItemCnt!!
        val baseViewGroup = tombrlativBaseViewGroup
        val addThingView = R.layout.fragment_tomb_addview_item
        val inflater: LayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(addThingView, null)
        val itemView = inflater.inflate(addThingView, null)
        baseViewGroup?.addView(itemView)

        val tombBurldViewGroup = baseViewGroup.getChildAt(itemCnt) as ViewGroup
        val tombBurldNameView = tombBurldViewGroup.getChildAt(0) as EditText
        val tombBurldRelateView = tombBurldViewGroup.getChildAt(1) as EditText
        val tombBurldSexSpinnerGroupView = tombBurldViewGroup.getChildAt(2) as ViewGroup
        val tombBurldSexSpinner = tombBurldSexSpinnerGroupView.getChildAt(0) as Spinner
        val tombBurldIhidnumfrontView = tombBurldViewGroup.getChildAt(3) as EditText
        val tombBurldIhidNumBackView = tombBurldViewGroup.getChildAt(5) as EditText

        wtnncUtill.wtnncSpinnerAdapter(R.array.tombSexdstnArray, tombBurldSexSpinner, this)
//        if(ThingTombObject.tombBurldDtlsArray!!.length() > itemCnt) {



        if(ThingTombObject.thingNewSearch == "N") {
            val data = ThingTombObject.tombBurldDtlsArray!!.getJSONObject(itemCnt)

            if (!data.isNull("burldDtlsCode")) {
                tombBurldNameView.setText(checkStringNull(data.getString("burldNm")))
                ///tombBurldRelateView.setText(checkStringNull(data.getString("rlarivRelate")))
                tombBurldRelateView.setText(checkStringNull(data.getString("rlativRelate")))
                val sexString = checkStringNull(data.getString("sexdstn"))
                tombBurldSexSpinner.setSelection(
                    when (sexString) {
                        "1" -> 1
                        "2" -> 2
                        else -> 0
                    }
                )
                val ihidnumString = checkStringNull(data.getString("ihidnum"))
                if (ihidnumString == "") {
                    tombBurldIhidnumfrontView.setText(ihidnumString)
                    tombBurldIhidNumBackView.setText(ihidnumString)
                } else {
                    //val ihidnumStringSub = ihidnumString.substring(0, 8)
                    //tombBurldIhidnumView.setText("$ihidnumStringSub ******")
//                    tombBurldIhidnumView.setText(withIhidNumAsterRisk(ihidnumString))
                    val ihidnumStringSplit = ihidnumString.split("-")
                    tombBurldIhidnumfrontView.setText(ihidnumStringSplit[0])
                    tombBurldIhidNumBackView.setText(ihidnumStringSplit[1])
                }
            }


        }



//        tombBurldIhidnumView.setOnEditorActionListener { textView, action, event ->
//            if(action == EditorInfo.IME_ACTION_DONE) {
//                val txtString = textView.text.toString()
//                if(txtString.isNotEmpty() || txtString.length == 14) {
//                    val ihidnum1 = txtString.substring(0,6)
//                    val ihidnum2 = txtString.substring(6,13)
//                    val ihidnumString = "$ihidnum1-$ihidnum2"
//                    tombBurldIhidnumView.setText(ihidnumString)
//                } else {
//                    toastUtil.msg_error("비정상적인 주민번호 입니다. 확인 후 다시 입력해주시기 바랍니다.", 500)
//
//                }
//
//
//            }
//
//            false
//        }

        itemCnt++

    }

    fun initTombSubThingLayout(recentItemCnt: Int, data: JSONObject) {
        addViewCnt = recentItemCnt

        val tombViewGroup = tombBaseViewGroup
        val addThingView = R.layout.fragment_tomb_thing_add_item
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val itemView = inflater.inflate(addThingView, null)

        tombViewGroup?.addView(itemView)

        val subThingBaseLayout = tombViewGroup.getChildAt(addViewCnt) as ViewGroup
        val subThingFirstView = subThingBaseLayout.getChildAt(1) as ViewGroup
        val subThingKndView = subThingFirstView.getChildAt(1) as EditText
        val subThingBgnnArView = subThingFirstView.getChildAt(2) as EditText
        val subThingIncrprArView = subThingFirstView.getChildAt(3) as EditText
        val subThingUnitClBaseView = subThingFirstView.getChildAt(4) as ViewGroup
        val subThingUnitClSpinner = subThingUnitClBaseView.getChildAt(0) as Spinner

        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray,subThingUnitClSpinner, this)

        val subThingSecendView = subThingBaseLayout.getChildAt(3) as ViewGroup
        val subThingStrctNdStndrdView = subThingSecendView.getChildAt(0) as EditText
        val subThingArComputBasisView = subThingSecendView.getChildAt(1) as EditText
        val subThingRmView = subThingSecendView.getChildAt(2) as EditText

        subThingKndView.setText(checkStringNull(data.getString("thingKnd")))
        subThingBgnnArView.setText(checkStringNull(data.getString("bgnnAr")))
        subThingIncrprArView.setText(checkStringNull(data.getString("incrprAr")))

        val unitClString = checkStringNull(data.getString("unitCl"))
        if(unitClString.equals("")) {
            subThingUnitClSpinner.setSelection(0)
        } else {
            val unitClStringSub = unitClString.substring(5, 7)
            subThingUnitClSpinner.setSelection(Integer.valueOf(unitClStringSub))
        }

        subThingStrctNdStndrdView.setText(checkStringNull(data.getString("strctNdStndrd")))
        subThingArComputBasisView.setText(checkStringNull(data.getString("arComputBasis")))
        subThingRmView.setText(checkStringNull(data.getString("rm")))

        addViewCnt++

    }

    fun init(view: View) {

        val requireArr = mutableListOf<TextView>(view.tv_tomb_require1, view.tv_tomb_require2, view.tv_tomb_require3)
        setRequireContent(requireArr)

        //분묘조서 spinner
        wtnncUtill.wtnncSpinnerAdapter(R.array.tombSeArray, tombSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.tombBurlTyArray, tombBurlTySpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.tombBurlScaleArray, tombBurlScaleSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.tombBytgtScaleArray, tombBytgtScaleSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.tombRlativArray, tombRlativSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, thingTombUnitSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.tombSexdstnArray, tombSexdstnAddSpinner, this) // 매장자 성별
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, tombAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, tombInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, tombOwnerCnfirmBasisSpinner, this)

        val dataString = activity?.intent?.extras?.get("tombInfo") as String
        logUtil.d("tombInfo dataString -------------------> $dataString")

        val dataJson = JSONObject(dataString)
        logUtil.d("tombInfo dataJson ------------------------> $dataJson")

        val tombDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        ThingTombObject.thingInfo = tombDataJson

        if(ThingTombObject.thingNewSearch.equals("N")) {
            ThingTombObject.tombBurldDtlsArray = dataJson.getJSONArray("tombBurld")
            ThingTombObject.thingOwnerInfoJson = dataJson.getJSONArray("ownerInfo")
        }

        view.landSearchLocationText.text = checkStringNull(tombDataJson.getString("legaldongNm"))
        view.landSearchBgnnLnmText.text = checkStringNull(tombDataJson.getString("bgnnLnm"))
        view.landSearchincrprLnmText.text = checkStringNull(tombDataJson.getString("incrprLnm"))
        view.landSearchNominationText.text = checkStringNull(tombDataJson.getString("gobuLndcgrNm"))
        val relatedLnmString = checkStringNull(tombDataJson.getString("relateLnm"))
        if (relatedLnmString.equals("")) {
            view.landSearchRelatedLnmText.text = "없음"
        } else {
            view.landSearchRelatedLnmText.text = relatedLnmString
        }
        view.landSearchBgnnArText.text = checkStringNull(tombDataJson.getString("ladBgnnAr"))
        view.landSearchIncrprArText.text = checkStringNull(tombDataJson.getString("ladIncrprAr"))
        view.landSearchOwnerText.text = checkStringNull(tombDataJson.getString("landOwnerName"))
        view.landSearchOwnerRText.text = checkStringNull(tombDataJson.getString("landRelatesName"))


        //분묘 조서 시작
        val thingTombCodeInt = tombDataJson.getInt("tombWtnCode")
        ThingTombObject.tombWtnCode = thingTombCodeInt
        if (thingTombCodeInt == 0) {
            view.thingTombCodeText.text = "자동기입"
        } else {
            view.thingTombCodeText.text = thingTombCodeInt.toString()
        }
        val thingTombSmallNmString = checkStringNull(tombDataJson.getString("thingSmallNm"))
        if(thingTombSmallNmString.equals("")) {
            view.thingTombSmallNmText.text = "분묘"
        } else {
            view.thingTombSmallNmText.text = checkStringNull(tombDataJson.getString("thingSmallNm"))
        }

        val thingTombKndString = checkStringNull(tombDataJson.getString("thingKnd"))
        if (thingTombKndString.equals("")) {
            view.tombThingKnd.setText("분묘번호자동기입")
        } else {
            view.tombThingKnd.setText(thingTombKndString)
        }

        val thingTombStrctString = checkStringNull(tombDataJson.getString("strctNdStndrd"))
        if (thingTombStrctString.equals("")) {
            view.tombStrctNdStrndrd.text = "자동기입"
        } else {
            view.tombStrctNdStrndrd.text = thingTombStrctString
        }

        view.thingTombBennArEdit.setText(checkStringNull(tombDataJson.getString("bgnnAr")))
        ThingTombObject.bgnnAr = checkStringNull(tombDataJson.getString("bgnnAr"))
        view.thingTombIncrprArEdit.setText(checkStringNull(tombDataJson.getString("incrprAr")))
        ThingTombObject.incrprAr = checkStringNull(tombDataJson.getString("incrprAr"))

        val unitClString = checkStringNull(tombDataJson.getString("unitCl").toString())
        if (unitClString.equals("")) {
            view.thingTombUnitSpinner.setSelection(0)
        } else {
            val unitClStringSub = unitClString.substring(5, 7)
            view.thingTombUnitSpinner.setSelection(Integer.valueOf(unitClStringSub))
        }
        val thingTombTyString = checkStringNull(tombDataJson.getString("tombTy"))
        if (thingTombTyString.equals("1")) {
            view.tombSeSpinner.setSelection(1)
        } else if (thingTombTyString.equals("2")) {
            view.tombSeSpinner.setSelection(2)
        } else {
            view.tombSeSpinner.setSelection(0)
        }
        tombTy = checkStringNull(tombDataJson.getString("tombTy"))

        view.tombBurlDe.text = checkStringNull(tombDataJson.getString("burlDe"))

        val thingTombCl = checkStringNull(tombDataJson.getString("tombCl"))
        val thingBalmCl = checkStringNull(tombDataJson.getString("balmCl"))

        with(ThingTombObject){
           balmCl = thingBalmCl
           tombCl = thingTombCl
        }

        when (thingBalmCl) {
            "A012001" -> view.tombRlativSpinner.setSelection(1)
            "A012002" -> view.tombRlativSpinner.setSelection(2)
            "A012003" -> view.tombRlativSpinner.setSelection(3)
            else -> view.tombRlativSpinner.setSelection(0)
        }
        when (thingTombCl) {
            "A013001" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(1)
                view.tombBytgtScaleSpinner.isEnabled = false
                view.tombBytgtScaleText.goneView()
                view.tombBytgtScaleLayout.goneView()
            }
            "A013002" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(1)
            }
            "A013003" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(2)
            }
            "A013004" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(3)
            }
            "A013005" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(4)
            }
            "A013006" -> {
                view.tombBurlTySpinner.setSelection(1)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(5)
            }
            "A013007" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(1)
                view.tombBytgtScaleSpinner.isEnabled = false
                view.tombBytgtScaleText.goneView()
                view.tombBytgtScaleLayout.goneView()
//                view.tombBytgtScaleSpinner.setSelection(1)
            }
            "A013008" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(1)
            }
            "A013009" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(2)
            }
            "A013010" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(3)
            }
            "A013011" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(4)
            }
            "A013012" -> {
                view.tombBurlTySpinner.setSelection(2)
                view.tombBurlScaleSpinner.setSelection(2)
                view.tombBytgtScaleSpinner.isEnabled = true
                view.tombBytgtScaleText.visibleView()
                view.tombBytgtScaleLayout.visibleView()
                view.tombBytgtScaleSpinner.setSelection(5)
            }
            "A013013" -> {
                view.tombBurlTySpinner.setSelection(3)
                view.tombBurlScaleSpinner.isEnabled = false
                view.tombBurlScaleSpinner.setSelection(0)
                view.tombBytgtScaleText.goneView()
                view.tombBytgtScaleLayout.goneView()
//                view.tombBytgtScaleSpinner.setSelection(1)
            }
            else -> {

            }
        }

        val ownerCnfirmBasisClString = checkStringNull(tombDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmBasisClString.equals("")) {
            view.tombOwnerCnfirmBasisSpinner.setSelection(0)
        } else {
            val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5,7)
            view.tombOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
        }

//        val acqsClString = checkStringNull(tombDataJson.getString("acqsCl"))
//        if(acqsClString.equals("")) {
//            view.tombAcqsSeSpinner.setSelection(0)
//        } else {
//            val acqsClStringsub = acqsClString.substring(5,7)
//            view.tombAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringsub))
//        }
//
//        val inclsClString = checkStringNull(tombDataJson.getString("inclsCl"))
//        if(inclsClString.equals("")) {
//            view.tombInclsSeSpinner.setSelection(0)
//        } else {
//            val inclsClStringsub = inclsClString.substring(5,7)
//            view.tombInclsSeSpinner.setSelection(Integer.valueOf(inclsClStringsub))
//        }

        val rwTrgetAtString = checkStringNull(tombDataJson.getString("rwTrgetAt"))
        view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")

        val apasmtTrgetAtString = checkStringNull(tombDataJson.getString("apasmtTrgetAt"))
        view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")

        view.includePaclrMatterEdit.setText(checkStringNull(tombDataJson.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(tombDataJson.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(tombDataJson.getString("rm")))




//        // 실제 분묘 매장자 정보 init
//        val tombBurldData = dataJson.getJSONArray("tombBurld")
//
//        for(i in 0 until tombBurldData.length()) {
//            val data = tombBurldData.getJSONObject(i)
//            initTombBurldLayout(i, data)
//        }

        // 실제 분묘 시설물 정보 init, 묘적부 여부 확인, 현장 사진 세팅
        if(ThingTombObject.thingNewSearch == "N") {
            //분묘 시설물
            val tombSubThingData = dataJson.getJSONArray("tombSubThing")

            for (i in 0 until tombSubThingData.length()) {
                val data = tombSubThingData.getJSONObject(i)
                initTombSubThingLayout(i, data)
            }

            // 묘적부
            tombAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until tombAtchInfo!!.length()) {
                val tombAtchItem = tombAtchInfo!!.getJSONObject(i)

                val tombAtchFileInfo = tombAtchItem.getString("fileseInfo")

                view.landInfoPreviousCompensateBtn.backgroundTintList = when (tombAtchFileInfo) {
                    "A200006025" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }

            }

            //현장사진
            settingSearchCamerasView(dataJson.getJSONArray("thingAtchInfo"))
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

        if(ThingTombObject.thingNewSearch.equals("N")) {
            if (dataArray!!.length() > 0) {

                var searchImageArray = JSONArray()

                for (i in 0 until dataArray!!.length()) {
                    val dataItem = dataArray!!.getJSONObject(i)

                    if (dataItem.getString("fileseInfo").equals("A200006012")) {
                        searchImageArray.put(dataItem)
                    }
                }



                logUtil.d("searchImageArray length ---------------------------> ${searchImageArray.length()}")

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
                    logUtil.d(it.toString())

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
                                            "TOMB",
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

    fun addTombThingData() {

        val tombDataJson = ThingTombObject.thingInfo


        // 분묘 조서
        ThingTombObject.apply {
            thingLrgeCl = "A011003" // 분묘 대분류

            thingSmallCl = "A040001" // 분묘 소분류

            strctNdStndrd = mActivity.tombStrctNdStrndrd.text.toString() // 구조 및 규격
            legaldongCode = tombDataJson?.getString("legaldongCode") // 지역코드
            thingKnd = "분묘번호" // 물건의 종류
            bgnnAr = mActivity.thingTombBennArEdit.text.toString() // 면적 및 수량 전체
            incrprAr = mActivity.thingTombIncrprArEdit.text.toString() // 면적 및 수량 편입

            tombNo = mActivity.thingTombNoEdit.text.toString()


            acqsCl = "A025001" // 취득
            inclsCl = "A007001" // 편입

            paclrMatter = mActivity.includePaclrMatterEdit.text.toString() //특이사항
            rwTrgetAt = when(mActivity.rwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            apasmtTrgetAt = when(mActivity.apasmtTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            ownerCnfirmBasisCl = when (mActivity.tombOwnerCnfirmBasisSpinner.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""

            }
            balmCl = when (mActivity.tombRlativSpinner.selectedItemPosition) {
                0 -> ""
                1 -> "A012001"
                2 -> "A012002"
                3 -> "A012003"
                else -> ""
            } //연고분류
        }

        when(mActivity.tombBurlTySpinner.selectedItemPosition) {
            1 -> {
                tombCl = when(mActivity.tombBurlScaleSpinner.selectedItemPosition) {
                    1 -> "A013001"
                    2 -> {
                        when(mActivity.tombBytgtScaleSpinner.selectedItemPosition) {
                            1 -> "A013002"
                            2 -> "A013003"
                            3 -> "A013004"
                            4 -> "A013005"
                            5 -> "A013006"
                            else -> ""
                        }
                    }
                    else -> ""
                }
            }
            2 -> {
                tombCl = when(mActivity.tombBurlScaleSpinner.selectedItemPosition) {
                    1 -> "A013007"
                    2 -> {
                        when(mActivity.tombBytgtScaleSpinner.selectedItemPosition) {
                            1 -> "A013008"
                            2 -> "A013009"
                            3 -> "A013010"
                            4 -> "A013011"
                            5 -> "A013012"
                            else -> ""
                        }
                    }
                    else -> ""
                }
            }
            3 -> tombCl = "A013013"
            else ->  tombCl = ""

        }

        tombTy = when(mActivity.tombSeSpinner.selectedItemPosition) {
            1 -> "1" //뷴묘
            2 -> "2" // 가묘
            else -> ""
        } //분묘타입
        ThingTombObject.burlDe = mActivity.tombBurlDe.text.toString() //매장일자


        // 매장자 추가
        if(mActivity.tombRlativTextLayout.visibility == View.VISIBLE) {
            val buriedPersonJSON = JSONObject()
            var buriedPersonItem = JSONObject()
            val buriedpersonArray = JSONArray()

            val buriedPersonAddItem = mActivity.tombrlativBaseViewGroup.childCount

            for(i in 0 until buriedPersonAddItem) {
                val baseViewGroup = mActivity.tombrlativBaseViewGroup
                val buriedPersonLayout = baseViewGroup.getChildAt(i) as ViewGroup
                val itemBurldNm = buriedPersonLayout.getChildAt(0) as EditText
                val itemRlativRelate = buriedPersonLayout.getChildAt(1) as EditText
                val itemSexdstnLayout = buriedPersonLayout.getChildAt(2) as ViewGroup
                val itemSexdstn = itemSexdstnLayout.getChildAt(0) as Spinner
                val itemIhidnumFront = buriedPersonLayout.getChildAt(3) as EditText
                val itemIhidnumBack = buriedPersonLayout.getChildAt(5) as EditText

                val itemBuriedPersonItem = JSONObject()

                itemBuriedPersonItem.put("burldNm", itemBurldNm.text.toString())
                itemBuriedPersonItem.put("sexdstn",when(itemSexdstn.selectedItemPosition) { // 첫번째 성별
                    1 -> "1"
                    2 -> "2"
                    else -> ""
                })
                itemBuriedPersonItem.put("ihidnum", "${itemIhidnumFront.text.toString()}-${itemIhidnumBack.text.toString()}")
                itemBuriedPersonItem.put("rlativRelate", itemRlativRelate.text.toString())
                if(ThingTombObject.thingNewSearch.equals("N")) {
//                    val burldDtlsItem = tombBurldData!!.getJSONObject(i)
                    val burldDtlsItem = ThingTombObject.tombBurldDtlsArray!!.getJSONObject(i)
                    itemBuriedPersonItem.put("burldDtlsCode",burldDtlsItem.getString("burldDtlsCode"))
                    itemBuriedPersonItem.put("tombWtnCode", burldDtlsItem.getString("tombWtnCode"))
                    itemBuriedPersonItem.put("thingWtnCode", burldDtlsItem.getString("thingWtnCode"))
                    itemBuriedPersonItem.put("saupCode",burldDtlsItem.getString("saupCode"))
                    itemBuriedPersonItem.put("refThingWtnCode", ThingTombObject.thingInfo!!.getString("thingWtnCode"))
                }

                buriedpersonArray.put(itemBuriedPersonItem)
            }

            buriedPersonJSON.put("buriedPerson", buriedpersonArray)

            ThingTombObject.addBuriedPerson = buriedPersonJSON

        } else {
            val buriedPersonJSON = JSONObject()
            val itemBuriedPersonItem = JSONObject()
            val buriedpersonArray = JSONArray()

            buriedpersonArray.put(itemBuriedPersonItem)
            buriedPersonJSON.put("buriedPerson",buriedpersonArray)

            ThingTombObject.addBuriedPerson = buriedPersonJSON
        }

        // 분묘 시설물 추가
        val buriedThingCnt = mActivity.tombBaseViewGroup.childCount

        if(buriedThingCnt > 0) {
            val buriedThingArray = JSONArray()
            val buriedThingJSON = JSONObject()
            for (i in 0 until buriedThingCnt) {
                val tombBaseViewGroup = mActivity.tombBaseViewGroup
                val tombBuriedThing = tombBaseViewGroup.getChildAt(i) as ViewGroup

                val tombBuiedThingFirst = tombBuriedThing.getChildAt(1) as ViewGroup
                val tombBuiedThingSecond = tombBuriedThing.getChildAt(3) as ViewGroup

                val itemBuriedThingKnd = tombBuiedThingFirst.getChildAt(1) as EditText
                val itemBuriedThingBgnnAr = tombBuiedThingFirst.getChildAt(2) as EditText
                val itemBuriedThingIncrprAr = tombBuiedThingFirst.getChildAt(3) as EditText
                val itemBuriedThingSpinnerView = tombBuiedThingFirst.getChildAt(4) as ViewGroup
                val itemBuriedThingSpinner = itemBuriedThingSpinnerView.getChildAt(0) as Spinner
                val itemBuriedThingStrctNdStrndrd = tombBuiedThingSecond.getChildAt(0) as EditText
                val itemBuriedThingArComputBasis = tombBuiedThingSecond.getChildAt(1) as EditText
                val itemBuriedThingRm = tombBuiedThingSecond.getChildAt(2) as EditText

                val buriedThingItem = JSONObject()

                buriedThingItem.put("thingLrgeCl","A011003")
                buriedThingItem.put("thingSmallCl","A040002")
                buriedThingItem.put("legaldongCode",tombDataJson?.getString("legaldongCode"))
                buriedThingItem.put("thingKnd",itemBuriedThingKnd.text.toString())
                buriedThingItem.put("strctNdStndrd",itemBuriedThingStrctNdStrndrd.text.toString())
                buriedThingItem.put("bgnnAr",itemBuriedThingBgnnAr.text.toString())
                buriedThingItem.put("incrprAr",itemBuriedThingIncrprAr.text.toString())
                buriedThingItem.put("unitCl",when(itemBuriedThingSpinner.selectedItemPosition) {
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
                buriedThingItem.put("arComputBasis",itemBuriedThingArComputBasis.text.toString())
                buriedThingItem.put("rm",itemBuriedThingRm.text.toString())
                buriedThingArray.put(buriedThingItem)

            }


            buriedThingJSON.put("buriedThing", buriedThingArray)
            ThingTombObject.addBuriedThing = buriedThingJSON
        } else {
            val buriedThingJSON = JSONObject()
            val buriedThingArray = JSONArray()
            val buriedThingItem = JSONObject()

//            buriedThingArray.put(buriedThingItem)
            buriedThingJSON.put("buriedThing",buriedThingArray)

            ThingTombObject.addBuriedThing = buriedThingJSON
        }



    }



    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }
}
