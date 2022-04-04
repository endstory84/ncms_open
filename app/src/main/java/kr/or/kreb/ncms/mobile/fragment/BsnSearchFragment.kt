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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_bsn_add_lvstck_layout.view.*
import kotlinx.android.synthetic.main.fragment_bsn_search.*
import kotlinx.android.synthetic.main.fragment_bsn_search.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.android.synthetic.main.thing_buld_link_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.BuldSelectListAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.data.CommonCodeInfoList
import kr.or.kreb.ncms.mobile.data.ThingBsnObject
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

class BsnSearchFragment(activity: Activity, context: Context, val fragmentActivity: FragmentActivity) : Fragment(),
    AdapterView.OnItemSelectedListener,
    DialogUtil.ClickListener  {

    private val mContext: Context = context
    private val mActivity: Activity = activity
    private lateinit var bsnTypeView: View
    private val wtnncUtill: WtnncUtil = WtnncUtil(activity, context)
    private var addViewCnt: Int = 0 // 추가 물건
    private var addBrdViewCnt: Int = 0 //추가 사육 내역
    private var bsnBrdQyNum = 0 // 사육수량
    private var lvstckNum: Int = 0 // 기준수량
    private var addThingBuldViewCnt: Int = 0
    private var logUtil: LogUtil = LogUtil("BsnSearchFragment")
    private val toastUtil = ToastUtil(context)
    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null
    var wtnncImageAdapter: WtnncImageAdapter? = null
    var builder: MaterialAlertDialogBuilder? = null

    val wonFormat = DecimalFormat("#,###")

    private var bsnAtchInfo: JSONArray? = null

    lateinit var materialDialog: Dialog

    init { }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bsnTypeView = inflater.inflate(R.layout.fragment_bsn_search, null)
        dialogUtil = DialogUtil(context, activity)
        dialogUtil!!.setClickListener(this)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))
        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        return bsnTypeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init(view)

        //사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.BSN,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

        //스케치버튼 클릭
        searchShetchBtn.setOnClickListener {
            (mActivity as MapActivity).settingCartoMap(null, null)
        }

        /**
         * 날짜선택
         */
        // 임차기간
        pssRentPeriodBgndeText.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                pssRentPeriodBgndeText,
                "pssRentPeriodBgndeText"
            )
        }
        pssRentPeriodEnddeText.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                pssRentPeriodEnddeText,
                "pssRentPeriodEnddeText"
            )
        }

        // 허가등 일자
        bsnPrmisnDe.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnPrmisnDe,
                "bsnPrmisnDe"
            )
        }

        // 허가등 기간
        bsnPrmisnPcBgned.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnPrmisnPcBgned,
                "bsnPrmisnPcBgned"
            )
        }
        bsnPrmisnPcEndde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnPrmisnPcEndde,
                "bsnPrmisnPcEndde"
            )
        }

        // 영업기간
        bsnBsnPdBgned.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnBsnPdBgned,
                "bsnBsnPdBgned"
            )
        }
        bsnBsnPdEndde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnBsnPdEndde,
                "bsnBsnPdEndde"
            )
        }

        // 사업등록 일자
        bsnRgsde.setOnClickListener {
            wtnncUtill.wtnncDatePicker(
                requireActivity().supportFragmentManager,
                bsnRgsde,
                "bsnRgsde"
            )
        }


        /**
         * 영업시설물추가(휴,실직자)
         */

        //휴, 실직자 레이아웃 추가
        bsnAddLayoutBtn.setOnClickListener {
            when (bsnSclasSpinner.selectedItemPosition) {
                0 -> toastUtil.msg_error("소분류를 선택해 주세요", 100)
                else -> {


                    val bsnViewGroup = bsnBaseViewGroup
                    val addThingView = R.layout.fragment_minrgt_add_layout
                    val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    inflater.inflate(addThingView, null)
                    val itemView = inflater.inflate(addThingView, null)
                    bsnViewGroup?.addView(itemView)

                    // 첫번째 spinner
                    val addLayoutItem = bsnViewGroup.getChildAt(addViewCnt) as ViewGroup
                    val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
                    val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
                    val selectLayout1 = addLayoutItem.getChildAt(4) as ViewGroup
                    val selectLayout2 = addLayoutItem.getChildAt(5) as ViewGroup
                    val selectLayout3 = addLayoutItem.getChildAt(6) as ViewGroup
                    val selectLayout4 = addLayoutItem.getChildAt(7) as ViewGroup
                    val addSpinner1 = addSpinnerLayout1.getChildAt(0) as Spinner // 소분류 Spinner

                    when (bsnSclasSpinner.selectedItemPosition) {
                        1 -> wtnncUtill.wtnncSpinnerAdapter(R.array.bsnCommSmallCtgArray, addSpinner1, this) // 영업
                        2 -> wtnncUtill.wtnncSpinnerAdapter(R.array.bsnSricltSmallCtgArray, addSpinner1, this) //잠업
                        3 -> wtnncUtill.wtnncSpinnerAdapter(R.array.bsnBrdSmallCtgArray, addSpinner1, this) // 축산업
                    }

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
//                    wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, addSpinner2, this)
                    wtnncUtill.wtnncSpinnerAdapter("A009", addSpinner2, this)

                    // 이전일자
                    val addViewGroup3 = addLayoutItem.getChildAt(7) as ViewGroup
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
            }
        }

        // 사욱내역 추가부분
        bsnAddBrdLayoutBtn.setOnClickListener {

            val bsnBrdViewGroup = bsnBrdBaseViewGroup
            val addThingView = R.layout.fragment_bsn_add_lvstck_layout
            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(addThingView, null)
            val itemView = inflater.inflate(addThingView, null)

            val requireArr = mutableListOf<TextView>(itemView.tv_bsn_lvstck_require1, itemView.tv_bsn_lvstck_require2, itemView.tv_bsn_lvstck_require3, itemView.tv_bsn_lvstck_require4)
            setRequireContent(requireArr)

            bsnBrdViewGroup?.addView(itemView)

            var addBsnBrdQyNum = 0
            var addLvstckNum = 0
            val bsnBrdBaseViewGroup = mActivity.bsnBrdBaseViewGroup
            val baseViewGroup = bsnBrdBaseViewGroup.getChildAt(addBrdViewCnt) as ViewGroup
            val bsnBrdViewFirst = baseViewGroup.getChildAt(1) as ViewGroup

            val bsnBrdPdBgndeText = bsnBrdViewFirst.getChildAt(2) as TextView
            val bsnBrdPdEnddeText = bsnBrdViewFirst.getChildAt(3) as TextView
            val bsnBrdQyText = bsnBrdViewFirst.getChildAt(4) as EditText

            val bsnBrdVierSecond = baseViewGroup.getChildAt(3) as ViewGroup

            val bsnStdLvstckClView = bsnBrdVierSecond.getChildAt(0) as ViewGroup
            val bsnStdLvstckClSpinner = bsnStdLvstckClView.getChildAt(0) as Spinner

            //val bsnStdQyText = bsnStdLvstckClView.getChildAt(1) as TextView
            //val bsnCnvrsnQyText = bsnStdLvstckClView.getChildAt(1) as TextView

            // FIXME: 2021-12-06 기존 사육내역추가 Bug Fix
            val bsnStdQyText = bsnBrdVierSecond.getChildAt(1) as TextView
            val bsnCnvrsnQyText = bsnBrdVierSecond.getChildAt(2) as TextView

            // 사육기간 시작일자0
//            addEditText2.setOnClickListener {
//                wtnncUtill.wtnncDateRangePicker(
//                    requireActivity().supportFragmentManager,
//                    addEditText2,
//                    "addBsnBrdPd"
//                )
//            }
            bsnBrdPdBgndeText.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    bsnBrdPdBgndeText,
                    "bsnBrdPdBgnde"
                )
            }
            bsnBrdPdEnddeText.setOnClickListener {
                wtnncUtill.wtnncDatePicker(
                    requireActivity().supportFragmentManager,
                    bsnBrdPdEnddeText,
                    "bsnBrdPdEndde"
                )
            }

            // 사육, 기준, 환산수량 부분
            bsnBrdQyText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            mActivity.runOnUiThread {
                                if (bsnBrdQyText.text.toString() != "" && bsnBrdQyText.text.toString() != "0") {
                                    addBsnBrdQyNum = (bsnBrdQyText.text.toString()).toInt()

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                            }
                        }
                    }, 1000) // 1초
                }
            })

            wtnncUtill.wtnncSpinnerAdapter(R.array.bsnLvstckStringArrayl, bsnStdLvstckClSpinner, this) // 기준 가축명

            bsnStdLvstckClSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        1 -> {
                            addLvstckNum = 200
                            bsnStdQyText.text = "200"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        2 -> {
                            addLvstckNum = 150
                            bsnStdQyText.text = "150"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        3 -> {
                            addLvstckNum = 150
                            bsnStdQyText.text = "150"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        4 -> {
                            addLvstckNum = 20
                            bsnStdQyText.text = "20"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        5 -> {
                            addLvstckNum = 5
                            bsnStdQyText.text = "5"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        6 -> {
                            addLvstckNum = 15
                            bsnStdQyText.text = "15"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        7 -> {
                            addLvstckNum = 20
                            bsnStdQyText.text = "20"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                        8 -> {
                            addLvstckNum = 20
                            bsnStdQyText.text = "20"

                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                bsnCnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


            addBrdViewCnt++
        }

//        // 사육 수량
        bsnGtnText.addTextChangedListener(wonFormatTransition(bsnGtnText))
        bsnMthtText.addTextChangedListener(wonFormatTransition(bsnMthtText))


        commPrmisnDtaBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val bsnAtchSelectArray = JSONArray()

            array.add("허가등자료 등록")
            if(ThingBsnObject.thingNewSearch.equals("N")) {
                for(i in 0 until bsnAtchInfo!!.length()) {
                    val bsnAtchItem = bsnAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006018")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        bsnAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("허가등자료 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006018", "허가등자료")
                        } else {
                            val item = bsnAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 영업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()

            }

        }
        commLsCtrtcBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val bsnAtchSelectArray = JSONArray()

            array.add("임대차계약서 등록")
            if(ThingBsnObject.thingNewSearch.equals("N")) {
                for(i in 0 until bsnAtchInfo!!.length()) {
                    val bsnAtchItem = bsnAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006019")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        bsnAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("임대차계약서 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006019", "임대차계약서")
                        } else {
                            val item = bsnAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 영업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()

            }
        }
        commBsnmCeregrtBtn.setOnClickListener {
            var array = mutableListOf<String>()
            var checkedItem = 0
            val bsnAtchSelectArray = JSONArray()

            array.add("사업자등록증 등록")
            if(ThingBsnObject.thingNewSearch.equals("N")) {
                for(i in 0 until bsnAtchInfo!!.length()) {
                    val bsnAtchItem = bsnAtchInfo!!.getJSONObject(i)
                    if(bsnAtchItem.getString("fileseInfo").equals("A200006020")) {
                        array.add(bsnAtchItem.getString("rgsde"))
                        bsnAtchSelectArray!!.put(bsnAtchItem)
                    }
                }

                materialDialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("사업자등록증 확인")
                    .setPositiveButton("확인") {_, _ ->
                        logUtil.d("setPositiveButton ---------------------------->")
                        if(checkedItem == 0) {
                            callThingCapture("A200006020", "사업자등록증")
                        } else {
                            val item = bsnAtchSelectArray!!.get(checkedItem-1) as JSONObject
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
                dialogUtil!!.confirmDialog("현재 작성 중인 영업 조서 완료 후 등록 해 주시기 바람니다.", builder!!, "확인").show()

            }
        }

    }


    fun callThingCapture(fileCode: String, fileCodeNm: String) {
        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.BSN,
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

    inner class wonFormatTransition(private val view: View) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {

            if(s != null && !s.toString().equals("")) {
                logUtil.d("s-------------------------------> $s")
                when(view.id) {
                    R.id.bsnGtnText -> {
                        bsnGtnText.removeTextChangedListener(this)
                        var ss = Regex("[^A-Za-z0-9]")
                        val sss = ss.replace(s, "")
                        bsnGtnText.setText(wonFormat.format(Integer.valueOf(sss.toString())))

                        bsnGtnText.setSelection(sss.length)
                        bsnGtnText.addTextChangedListener(wonFormatTransition(bsnGtnText))

                    }
                    R.id.bsnMthtText -> {
                        bsnMthtText.removeTextChangedListener(this)
                        var ss = Regex("[^A-Za-z0-9]")
                        val sss = ss.replace(s, "")
                        bsnMthtText.setText(wonFormat.format(Integer.valueOf(sss.toString())))

                        bsnMthtText.setSelection(sss.length)
                        bsnMthtText.addTextChangedListener(wonFormatTransition(bsnMthtText))

                    }
                }
            }
        }

    }


    fun init(view: View) {

        val requireArr = mutableListOf<TextView>(view.tv_bsn_require1, view.tv_bsn_require2, view.tv_bsn_require3, view.tv_bsn_require4, view.tv_bsn_require5, view.tv_bsn_require6, view.tv_bsn_require7, view.tv_bsn_require8)
        setRequireContent(requireArr)

        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnBsnSeArray, bsnSclasSpinner, this) // 영업구분
        // A009
//        wtnncUtill.wtnncSpinnerAdapter(R.array.thingUnitArray, bsnUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter("A009", bsnUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnCommPossesnSeArray, bsnCommPossesnSeSpinner, this) // 점유구분
//        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnPrmisnSeArray, bsnPrmisnSeSpinner, this) // 허가등 구분
        wtnncUtill.wtnncSpinnerAdapter("A028", bsnPrmisnSeSpinner, this) // 허가등 구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnPrftmkSeArray, bsnPrftmkSeSpinner, this) // 영리 구분
        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnSeArray, bsnRegistSeSpinner, this) // 등록 구분
//        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnLvstckStringArrayl, bsnStdrLvstckNmSpinner, this) // 기준 가축명
        wtnncUtill.wtnncSpinnerAdapter(R.array.bsnClDivArray, bsnClDivSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.acqsSeArray, bsnAcqsSeSpinner, this)
//        wtnncUtill.wtnncSpinnerAdapter(R.array.InclsSeArray, bsnInclsSeSpinner, this)
        wtnncUtill.wtnncSpinnerAdapter(R.array.ownerCnfirmBasisArray, bsnOwnerCnfirmBasisSpinner, this)


        val dataString = requireActivity().intent!!.extras!!.get("BsnInfo") as String

        val dataJson = JSONObject(dataString)

        val thingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        ThingBsnObject.thingInfo = thingDataJson


        val saupCodeInfo = checkStringNull(thingDataJson.getString("saupCode"))
        val incrprLnmInfo = checkStringNull(thingDataJson.getString("incrprLnm"))


        if(ThingBsnObject.thingNewSearch.equals("Y")) {
            val buldSelectMap = HashMap<String, String>()

            buldSelectMap.put("saupCode", saupCodeInfo)
            buldSelectMap.put("incrprLnm", incrprLnmInfo)

            val thingBuldSelectUrl = context!!.resources.getString(R.string.mobile_url) + "thingBuldSelect"

            val progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostWebServer(buldSelectMap, progressDialog, thingBuldSelectUrl,
                    object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            progressDialog.dismiss()
                            logUtil.e("fail")
                        }

                        @SuppressLint("InflateParams")
                        override fun onResponse(call: Call, response: Response) {
                            val responseString = response.body!!.string()

                            logUtil.d("responseString buldSelect response ---------------> $responseString")

                            progressDialog.dismiss()

                            val buldSelectJson = JSONObject(responseString).getJSONObject("list").getJSONArray("ThingSearch")

                            layoutInflater.inflate(R.layout.thing_buld_link_dialog, null).let{ view ->
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
                                    val selectBuldLinkData = (view.addBuldLinkListView.adapter as BuldSelectListAdapter).getSelectItem()

                                    logUtil.d("selectData Size ---------------------->${selectBuldLinkData.size}")

                                    if(selectBuldLinkData.size >0) {
                                        for(data in selectBuldLinkData) {
                                            val buldLinkViewGroup = addBsnBuldLinkViewGroup
                                            val addThingView = R.layout.fragment_thing_add_buld_link_item
                                            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                                            inflater.inflate(addThingView, null)
                                            val itemView = inflater.inflate(addThingView, null)

                                            buldLinkViewGroup?.addView(itemView)

                                            val addViewGroup = mActivity.addBsnBuldLinkViewGroup
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
                                        ThingBsnObject.selectBuldLinkData = selectBuldLinkData

                                        dialogUtil?.run {
                                            alertDialog(
                                                "영업 스케치 등록",
                                                "선택한 건물 스케치를 영업 스케치로 등록 하시겠습니까?",
                                                builder!!,
                                                "bsnSkitchConfirm"
                                            ).show()
                                        }




                                    }
                                    buldSelectDialog.dismiss()
                                }
                            }
                        }

                    })

        }

        //토지정보
        view.landSearchLocationText.text = checkStringNull(thingDataJson.getString("legaldongNm"))

        view.landSearchBgnnLnmText.text = checkStringNull(thingDataJson.getString("bgnnLnm"))
        view.landSearchincrprLnmText.text = checkStringNull(thingDataJson.getString("incrprLnm"))
        view.landSearchNominationText.text = checkStringNull(thingDataJson.getString("gobuLndcgrNm"))
        val relateLnmString = checkStringNull(thingDataJson.getString("relateLnm"))
        if(relateLnmString.equals("")) {
            view.landSearchRelatedLnmText.text = "없음"
        } else {
            view.landSearchRelatedLnmText.text = relateLnmString
        }
        view.landSearchBgnnArText.text = checkStringNull(thingDataJson.getString("ladBgnnAr"))
        view.landSearchIncrprArText.text = checkStringNull(thingDataJson.getString("ladIncrprAr"))
        view.landSearchOwnerText.text = checkStringNull(thingDataJson.getString("landOwnerName"))
        view.landSearchOwnerRText.text = checkStringNull(thingDataJson.getString("landRelatesName"))

        //영업기초정보
        val bsnWtnCodeString = thingDataJson.getInt("bsnWtnCode")
        if(bsnWtnCodeString == 0) {
            view.bsnWtnCodeText.text = "자동기입"
        } else {
            view.bsnWtnCodeText.text = bsnWtnCodeString.toString()
        }

        val bsnThingSmallCl = checkStringNull(thingDataJson.getString("thingSmallCl"))
        val bsnThingSmallNm = checkStringNull(thingDataJson.getString("thingSmallNm"))
        if(bsnThingSmallCl.equals("")) {
            view.bsnSclasSpinner.setSelection(0)
        } else {
            when(bsnThingSmallNm) {
                "일반영업" -> {
                    view.bsnSclasSpinner.setSelection(1)
                    view.bsnBrdBaseLayout.goneView()
                }
                "잠업" -> {
                    view.bsnSclasSpinner.setSelection(2)
                    view.bsnBrdBaseLayout.goneView()
                }
                "축산업" -> {
                    view.bsnSclasSpinner.setSelection(3)
                    view.bsnBrdBaseLayout.visibleView()

                }
                else -> {
                    view.bsnSclasSpinner.setSelection(0)
                    view.bsnBrdBaseLayout.goneView()
                }

            }
        }

        val bsnThingKndString = checkStringNull(thingDataJson.getString("thingKnd"))
        if(bsnThingKndString.equals("")) {
            view.bsnThingKnd.text = "자동기입"
        } else {
            view.bsnThingKnd.text = bsnThingKndString
        }

        val bsnStrctNdStrndedString = checkStringNull(thingDataJson.getString("strctNdStndrd"))
        if(bsnStrctNdStrndedString.equals("")) {
            view.bsnStrctNdStrndrd.text = "자동기입"
        }else {
            view.bsnStrctNdStrndrd.text = bsnStrctNdStrndedString
        }

        view.bsnBgnnAr.setText(checkStringNull(thingDataJson.getString("bgnnAr")))
        view.bsnIncrprAr.setText(checkStringNull(thingDataJson.getString("incrprAr")))

        val bsnUnitClString = checkStringNull(thingDataJson.getString("unitCl"))

//        if(bsnUnitClString.equals("")) {
//            view.bsnUnitSpinner.setSelection(0)
//        } else {
//            val unitClStringSub = bsnUnitClString.substring(5,7)
//            view.bsnUnitSpinner.setSelection(Integer.valueOf(unitClStringSub))
//        }
        view.bsnUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", bsnUnitClString) )

        view.bsnArComputBasis.setText(checkStringNull(thingDataJson.getString("arComputBasis")))

        //휴업/폐업

        val bsnClString = checkStringNull(thingDataJson.getString("bsnCl"))
        if(bsnClString.equals("")) {
            view.bsnClDivSpinner.setSelection(0)
        } else {
            val bsnClStringSub = bsnClString.substring(5,7)
            view.bsnClDivSpinner.setSelection(Integer.valueOf(bsnClStringSub))
            when(bsnClString) {
                "A017001" -> {
                    view.bssMthCoLayout.visibility = View.VISIBLE
                    view.bssMthCoText.setText(checkStringNull(thingDataJson.getString("sssMthCo")))

                }
                "A017002" -> {
                    view.bssMthCoLayout.visibility = View.INVISIBLE
                }
            }
        }



        //점유의 적법성
        val pssLgalAtString = checkStringNull(thingDataJson.getString("pssLgalAt"))
        view.pssLgalAt.isChecked = pssLgalAtString.equals("Y")

        val pssPssTyString = checkStringNull(thingDataJson.getString("pssPssTy"))
        when(pssPssTyString) {
            "1" -> {
                view.bsnCommPossesnSeSpinner.setSelection(1)
                view.bsnPssLinearLayout1.goneView()
                view.bsnPssLinearLayout2.goneView()
                view.bsnPssLinearLayout3.goneView()
            }
            "2" -> {
                view.bsnCommPossesnSeSpinner.setSelection(2)
                view.bsnPssLinearLayout1.visibleView()
                view.bsnPssLinearLayout2.visibleView()
                view.bsnPssLinearLayout3.visibleView()
            }
            else -> {
                view.bsnCommPossesnSeSpinner.setSelection(0)
                view.bsnPssLinearLayout1.goneView()
                view.bsnPssLinearLayout2.goneView()
                view.bsnPssLinearLayout3.goneView()
            }
        }
        val pssHireCntrctAtString = checkStringNull(thingDataJson.getString("pssHireCntrctAt"))
        view.pssHireCntrctAtChk.isChecked = pssHireCntrctAtString.equals("Y")


        view.pssRentNameText.setText(checkStringNull(thingDataJson.getString("pssRentName")))
        view.pssHireNameText.setText(checkStringNull(thingDataJson.getString("pssHireName")))

        view.pssRentPeriodBgndeText.text = checkStringNull(thingDataJson.getString("pssRentBgnde"))
        view.pssRentPeriodEnddeText.text = checkStringNull(thingDataJson.getString("pssRentEndde"))

//        view.pssRentPeriodText.setText(pssRentBgndeString + " ~ " + pssRentEnddeString)

        val bsnGtnTextString = checkStringNull(thingDataJson.getString("pssGtn"))
        view.bsnGtnText.setText(wonFormat.format(Integer.valueOf(bsnGtnTextString.toString())))

        val bsnMthtTextString = checkStringNull(thingDataJson.getString("pssMtht"))
        view.bsnMthtText.setText(wonFormat.format(Integer.valueOf(bsnMthtTextString.toString())))
        view.bsnCntrctLcText.setText(checkStringNull(thingDataJson.getString("pssCntrctLc")))
        view.bsnCntrctArText.setText(checkStringNull(thingDataJson.getString("pssCntrctAr")))
        view.bsnSpccntrText.setText(checkStringNull(thingDataJson.getString("pssSpccntr")))


        //영업의 적법성
        val bsnProperAtString = checkStringNull(thingDataJson.getString("bsnProperAt"))
        view.bsnProperAtChk.isChecked = bsnProperAtString.equals("Y")
        val bsnSgnProsAtString = checkStringNull(thingDataJson.getString("bsnSgnProsAt"))
        view.bsnOpenAtChk.isChecked = bsnSgnProsAtString.equals("Y")

        val bsnPrmisnClString = checkStringNull(thingDataJson.getString("bsnPrmisnCl"))
//        if(bsnPrmisnClString.equals("")) {
//            view.bsnPrmisnSeSpinner.setSelection(0)
//        } else {
//            val bsnPrmisnClStringSub = bsnPrmisnClString.substring(5,7)
//            view.bsnPrmisnSeSpinner.setSelection(Integer.valueOf(bsnPrmisnClStringSub))
//        }
        view.bsnPrmisnSeSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A028", bsnPrmisnClString) )

//        view.bsnPrmisnRecivNm.setText(checkStringNull(thingDataJson.getString("bsnPrmsTrgetNm")))
        view.bsnPrmisnNm.setText(checkStringNull(thingDataJson.getString("bsnPrmisnNo")))
        view.bsnPrmisnDe.text = checkStringNull(thingDataJson.getString("bsnPrmisnDe"))

        view.bsnPrmisnPcBgned.text = checkStringNull(thingDataJson.getString("bsnPrmisnBgnde"))
        view.bsnPrmisnPcEndde.text = checkStringNull(thingDataJson.getString("bsnPrmisnEndde"))
//        view.bsnPrmisnPc.setText(bsnPrmisnBgndeString + " ~ " + bsnPrmisnEnddeString)

        view.bsnPrmisnInstt.setText(checkStringNull(thingDataJson.getString("bsnPrmisnInstt")))

        view.bsnBsnPdBgned.text = checkStringNull(thingDataJson.getString("bsnBsnpdBgnde"))
        view.bsnBsnPdEndde.text = checkStringNull(thingDataJson.getString("bsnBsnpdEndde"))
//        view.bsnBsnPd.setText(bsnBsnPdBgndeString + " ~ " + bsnBsnPdEnddeString)

//        view.bsnBuzplcLc.setText(checkStringNull(thingDataJson.getString("bsnBsnplcLc")))
        view.bsnBuzplcAr.setText(checkStringNull(thingDataJson.getString("bsnBsnplcAr")))


        //인적물적시설여부
        val hmftyProperAtString = checkStringNull(thingDataJson.getString("hmftyProperAt"))
        view.hmftyProperAt.isChecked = hmftyProperAtString.equals("Y")

        view.bsnSgnbrdNm.setText(checkStringNull(thingDataJson.getString("hmftySgnbrdNm")))
        view.bsnResdngHn.setText(checkStringNull(thingDataJson.getString("hmftyResdngHnf")))

        val hmftyFtyAtString = checkStringNull(thingDataJson.getString("hmftyFtyAt"))
        view.bsnFcltsAtChk.isChecked = hmftyFtyAtString.equals("Y")

        val hmftySgnSecdAtString = checkStringNull(thingDataJson.getString("hmftySgnSecdAt"))
        view.bsnSamenssHshldAtChk.isChecked = hmftySgnSecdAtString.equals("Y")

        //사업자등록내역
        val bizrdtlsSttAtString = checkStringNull(thingDataJson.getString("bizrdtlsSttAt"))
        view.bsnSttusConsistAtChk.isChecked = bizrdtlsSttAtString.equals("Y")

        val bizrdtlsPrftmkTyString = checkStringNull(thingDataJson.getString("bizrdtlsPrftmkTy"))
        when(bizrdtlsPrftmkTyString) {
            "1" -> view.bsnPrftmkSeSpinner.setSelection(1)
            "2" -> view.bsnPrftmkSeSpinner.setSelection(2)
            else -> view.bsnPrftmkSeSpinner.setSelection(0)
        }

        val bizrdtlsRegTyString = checkStringNull(thingDataJson.getString("bizrdtlsRegTy"))
        when(bizrdtlsRegTyString) {
            "1" -> view.bsnRegistSeSpinner.setSelection(1)
            "2" -> view.bsnRegistSeSpinner.setSelection(2)
            else -> view.bsnRegistSeSpinner.setSelection(0)
        }
        view.bsnRprsntv.setText(checkStringNull(thingDataJson.getString("bizrdtlsRprsntvNm")))
        view.bsnCmpnm.setText(checkStringNull(thingDataJson.getString("bizrdtlsMtlty")))
        view.bsnInduty.setText(checkStringNull(thingDataJson.getString("bizrdtlsInduty")))
        view.bsnBizcnd.setText(checkStringNull(thingDataJson.getString("bizrdtlsBizcnd")))
        view.RegistrationNm.setText(checkStringNull(thingDataJson.getString("bizrdtlsBizrno")))
        view.bsnRgsde.text = checkStringNull(thingDataJson.getString("bizrdtlsBizDe"))

        val bsnNtfcRgsAtString = checkStringNull(thingDataJson.getString("bizrdtlsRegAt"))
        view.bsnNtfcRgsAtChk.isChecked = bsnNtfcRgsAtString.equals("Y")

        val ownerCnfirmBasisClString = checkStringNull(thingDataJson.getString("ownerCnfirmBasisCl"))
        if(ownerCnfirmBasisClString.equals("")) {
            view.bsnOwnerCnfirmBasisSpinner.setSelection(4)
        } else {
            val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5,7)
            view.bsnOwnerCnfirmBasisSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
        }

//        val acqsClString = checkStringNull(thingDataJson.getString("acqsCl"))
//        if(acqsClString.equals("")) {
//            view.bsnAcqsSeSpinner.setSelection(0)
//        } else {
//            val acqsClStringsub = acqsClString.substring(5,7)
//            view.bsnAcqsSeSpinner.setSelection(Integer.valueOf(acqsClStringsub))
//        }
//
//        val inclsClString = checkStringNull(thingDataJson.getString("inclsCl"))
//        if(inclsClString.equals("")) {
//            view.bsnInclsSeSpinner.setSelection(0)
//        } else {
//            inclsClString.substring(5,7)
//            view.bsnInclsSeSpinner.setSelection(Integer.valueOf(inclsClString))
//        }

        val rwTrgetAtString = checkStringNull(thingDataJson.getString("rwTrgetAt"))
        if(rwTrgetAtString.equals("")) {
            view.rwTrgetAtChk.isChecked = true
        } else {
            view.rwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
        }


        val apasmtTrgetAtString = checkStringNull(thingDataJson.getString("apasmtTrgetAt"))
        if(apasmtTrgetAtString.equals("")) {
            view.apasmtTrgetAtChk.isChecked = true
        }else {
            view.apasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")
        }


        view.includePaclrMatterEdit.setText(checkStringNull(thingDataJson.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(thingDataJson.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(thingDataJson.getString("rm")))




        if(ThingBsnObject.thingNewSearch.equals("N")) {
            //장소의적법성
            val bsnBuldLinkArray = dataJson.getJSONArray("bsnBuldLink") as JSONArray
            for (i in 0 until bsnBuldLinkArray.length()-1) {
                addThingBuldViewCnt = i
                val bsnBuldLinkObject = bsnBuldLinkArray.getJSONObject(i)

                val buldLinkViewGroup = addBsnBuldLinkViewGroup
                val addThingView = R.layout.fragment_thing_add_buld_link_item
                val inflater: LayoutInflater =
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                inflater.inflate(addThingView, null)
                val itemView = inflater.inflate(addThingView, null)

                buldLinkViewGroup?.addView(itemView)

                //                val addViewGroup = view.addBsnBuldLinkViewGroup
                val addLayout1 = buldLinkViewGroup.getChildAt(addThingBuldViewCnt) as ViewGroup
                val addLayout2 = addLayout1.getChildAt(1) as ViewGroup
                val addBuldThingKndText = addLayout2.getChildAt(1) as TextView
                val addBuldThingArText = addLayout2.getChildAt(2) as TextView
                val addNrtBuldAtView = addLayout2.getChildAt(3) as ViewGroup
                val addNrtBuldAtChk = addNrtBuldAtView.getChildAt(0) as CheckBox

                addBuldThingKndText.text = checkStringNull(bsnBuldLinkObject.getString("thingKnd"))
                addBuldThingArText.text = checkStringNull(bsnBuldLinkObject.getString("incrprLnm"))
                val nrtBuldAtString = checkStringNull(bsnBuldLinkObject.getString("nrtBuldAt"))
                addNrtBuldAtChk.isChecked = nrtBuldAtString.equals("Y")

                val addLayout3 = addLayout1.getChildAt(3) as ViewGroup
                val addBuldPrpos = addLayout3.getChildAt(0) as TextView
                val addRegstrPrpos = addLayout3.getChildAt(1) as TextView
                val addRgistPrpos = addLayout3.getChildAt(2) as TextView

                addBuldPrpos.text = checkStringNull(bsnBuldLinkObject.getString("buldPrpos"))
                addRegstrPrpos.text = checkStringNull(bsnBuldLinkObject.getString("regstrBuldPrpos"))
                addRgistPrpos.text = checkStringNull(bsnBuldLinkObject.getString("rgistBuldPrpos"))


                addThingBuldViewCnt++

            }


            //가축여부
            if (view.bsnBrdBaseLayout.visibility == View.VISIBLE) {

                val brdDtlsArray = dataJson.getJSONArray("brdDtlsList")

                for (i in 0 until brdDtlsArray.length()) {

                    val brdDtlsObject = brdDtlsArray.getJSONObject(i)

                    addBrdViewCnt = i

                    val bsnBrdViewGroup = bsnBrdBaseViewGroup
                    val addThingView = R.layout.fragment_bsn_add_lvstck_layout
                    val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    inflater.inflate(addThingView, null)
                    val itemView = inflater.inflate(addThingView, null)

                    val requireArr = mutableListOf<TextView>(
                        itemView.tv_bsn_lvstck_require1,
                        itemView.tv_bsn_lvstck_require2,
                        itemView.tv_bsn_lvstck_require3,
                        itemView.tv_bsn_lvstck_require4
                    )
                    setRequireContent(requireArr)

                    bsnBrdViewGroup?.addView(itemView)

                    var addBsnBrdQyNum = 0
                    var addLvstckNum = 0
                    //                    val tombViewGroup = mActivity.bsnBrdBaseViewGroup
                    val addBrdDtlsView = bsnBrdViewGroup.getChildAt(addBrdViewCnt) as ViewGroup
                    val brdDtlsViewFirst = addBrdDtlsView.getChildAt(1) as ViewGroup


                    val properAtView = brdDtlsViewFirst.getChildAt(0) as ViewGroup
                    val properAtChk = properAtView.getChildAt(0) as CheckBox
                    val brdLvstCkNmText = brdDtlsViewFirst.getChildAt(1) as EditText
                    val bsnBrdPdBgndeText = brdDtlsViewFirst.getChildAt(2) as TextView
                    val bsnBrdPdEnddeText = brdDtlsViewFirst.getChildAt(3) as TextView
                    val brdQyText = brdDtlsViewFirst.getChildAt(4) as EditText

                    val brdDtlsViewSecond = addBrdDtlsView.getChildAt(3) as ViewGroup
                    val stdLvstckClView = brdDtlsViewSecond.getChildAt(0) as ViewGroup
                    val stdLvstckClSpanner = stdLvstckClView.getChildAt(0) as Spinner
                    val stdQyText = brdDtlsViewSecond.getChildAt(1) as TextView
                    val cnvrsnQyText = brdDtlsViewSecond.getChildAt(2) as TextView


                    val properAtString = checkStringNull(brdDtlsObject.getString("properAt"))
                    properAtChk.isChecked = properAtString.equals("Y")

                    brdLvstCkNmText.setText(checkStringNull(brdDtlsObject.getString("brdLvstckNm")))

                    bsnBrdPdBgndeText.text = checkStringNull(brdDtlsObject.getString("brdPdBgnde"))
                    bsnBrdPdEnddeText.text = checkStringNull(brdDtlsObject.getString("brdPdEndde"))

                    brdQyText.setText(checkStringNull(brdDtlsObject.getString("brdQy")))

                    wtnncUtill.wtnncSpinnerAdapter(
                        R.array.bsnLvstckStringArrayl,
                        stdLvstckClSpanner,
                        this
                    ) // 기준 가축명

                    val stdLvstckClString = checkStringNull(brdDtlsObject.getString("stdLvstckCl"))
                    if (stdLvstckClString.equals("")) {
                        stdLvstckClSpanner.setSelection(0)
                    } else {
                        val stdLvstckClStringSub = stdLvstckClString.substring(5, 7)
                        stdLvstckClSpanner.setSelection(Integer.valueOf(stdLvstckClStringSub))
                    }

                    stdQyText.text = checkStringNull(brdDtlsObject.getString("stdQy"))

                    cnvrsnQyText.text = checkStringNull(brdDtlsObject.getString("cnvrsnQy"))


                    // 사육기간 시작일자
                    bsnBrdPdBgndeText.setOnClickListener {
                        wtnncUtill.wtnncDatePicker(
                            requireActivity().supportFragmentManager,
                            bsnBrdPdBgndeText,
                            "bsnBrdPdBgnde"
                        )
                    }
                    bsnBrdPdEnddeText.setOnClickListener {
                        wtnncUtill.wtnncDatePicker(
                            requireActivity().supportFragmentManager,
                            bsnBrdPdEnddeText,
                            "bsnBrdPdEndde"
                        )
                    }

                    // 사육, 기준, 환산수량 부분
                    brdLvstCkNmText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            val timer = Timer()
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    mActivity.runOnUiThread {
                                        if (brdLvstCkNmText.text.toString() != "" && brdLvstCkNmText.text.toString() != "0") {
                                            addBsnBrdQyNum = (brdLvstCkNmText.text.toString()).toInt()

                                            if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                                cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                            }
                                        }
                                    }
                                }
                            }, 1000) // 1초
                        }
                    })

                    wtnncUtill.wtnncSpinnerAdapter(
                        R.array.bsnLvstckStringArrayl,
                        stdLvstckClSpanner,
                        this
                    ) // 기준 가축명

                    stdLvstckClSpanner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            when (position) {
                                1 -> {
                                    addLvstckNum = 200
                                    stdQyText.text = "200"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                2 -> {
                                    addLvstckNum = 150
                                    stdQyText.text = "150"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                3 -> {
                                    addLvstckNum = 150
                                    stdQyText.text = "150"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                4 -> {
                                    addLvstckNum = 20
                                    stdQyText.text = "20"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                5 -> {
                                    addLvstckNum = 5
                                    stdQyText.text = "5"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                6 -> {
                                    addLvstckNum = 15
                                    stdQyText.text = "15"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                7 -> {
                                    addLvstckNum = 20
                                    stdQyText.text = "20"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                                8 -> {
                                    addLvstckNum = 20
                                    stdQyText.text = "20"

                                    if (addLvstckNum != 0 && addBsnBrdQyNum != 0) {
                                        cnvrsnQyText.text = (addBsnBrdQyNum / addLvstckNum).toString()
                                    }
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }


                    addBrdViewCnt++
                }

            }

            // 영업시설물
            val bsnSubThingArray = dataJson.getJSONArray("bsnSubThing")

            for (i in 0 until bsnSubThingArray.length()) {
                val bsnSubThingObject = bsnSubThingArray.getJSONObject(i)

                addViewCnt = i

                val bsnViewGroup = bsnBaseViewGroup
                val addThingView = R.layout.fragment_minrgt_add_layout
                val inflater: LayoutInflater =
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(addThingView, null)
                val itemView = inflater.inflate(addThingView, null)
                bsnViewGroup?.addView(itemView)

                val addLayoutItem = bsnViewGroup.getChildAt(addViewCnt) as ViewGroup
                val thingSubLayoutFirst = addLayoutItem.getChildAt(1) as ViewGroup
                val thingSubSmallClView = thingSubLayoutFirst.getChildAt(0) as ViewGroup
                val thingSubSmallClSpinner = thingSubSmallClView.getChildAt(0) as Spinner
                val thingSubKndText = thingSubLayoutFirst.getChildAt(1) as EditText
                val thingSubStrctNdStndrdText = thingSubLayoutFirst.getChildAt(2) as EditText

                val thingSubLayoutSecond = addLayoutItem.getChildAt(3) as ViewGroup
                val thingSubArView = thingSubLayoutSecond.getChildAt(0) as ViewGroup
                val thingSubBgnnAr = thingSubArView.getChildAt(0) as EditText
                val thingSubIncprpAr = thingSubArView.getChildAt(1) as EditText

                val thingUnitClView = thingSubLayoutSecond.getChildAt(1) as ViewGroup
                val thingUnitClSpinner = thingUnitClView.getChildAt(0) as Spinner

                val thingSubArComputBasisText = thingSubLayoutSecond.getChildAt(2) as EditText

                val thingSubSmallClString = checkStringNull(bsnSubThingObject.getString("thingSmallCl"))
                when (bsnSclasSpinner.selectedItemPosition) {
                    1 -> wtnncUtill.wtnncSpinnerAdapter(
                        R.array.bsnCommSmallCtgArray,
                        thingSubSmallClSpinner,
                        this
                    ) // 영업
                    2 -> wtnncUtill.wtnncSpinnerAdapter(
                        R.array.bsnSricltSmallCtgArray,
                        thingSubSmallClSpinner,
                        this
                    ) //잠업
                    3 -> wtnncUtill.wtnncSpinnerAdapter(
                        R.array.bsnBrdSmallCtgArray,
                        thingSubSmallClSpinner,
                        this
                    ) // 축산업
                }

                if (thingSubSmallClString.equals("")) {
                    thingSubSmallClSpinner.setSelection(0)
                } else {
//                        val thingSubSmallClStringsub = thingSubSmallClString.substring(5, 7)
//                        thingSubSmallClSpinner.setSelection(Integer.valueOf(thingSubSmallClStringsub))
                    when (thingSubSmallClString) {
                        "A016002" -> {thingSubSmallClSpinner.setSelection(1)}
                        "A016041" -> {thingSubSmallClSpinner.setSelection(1)}
                        "A016051" -> {thingSubSmallClSpinner.setSelection(1)}
                    }
                }

                thingSubKndText.setText(checkStringNull(bsnSubThingObject.getString("thingKnd")))
                thingSubStrctNdStndrdText.setText(checkStringNull(bsnSubThingObject.getString("strctNdStndrd")))
                thingSubBgnnAr.setText(checkStringNull(bsnSubThingObject.getString("bgnnAr")))
                thingSubIncprpAr.setText(checkStringNull(bsnSubThingObject.getString("incrprAr")))

                val thingUnitClString = checkStringNull(bsnSubThingObject.getString("unitCl"))
                if (thingUnitClString.equals("")) {
                    thingUnitClSpinner.setSelection(0)
                } else {
                    val thingUnitClStringsub = thingUnitClString.substring(5, 7)
                    thingUnitClSpinner.setSelection(Integer.valueOf(thingUnitClStringsub))
                }

                thingSubArComputBasisText.setText(checkStringNull(bsnSubThingObject.getString("arComputBasis")))

                addViewCnt++

            }

            //문서 버튼
            bsnAtchInfo = dataJson.getJSONArray("thingAtchInfo")
            for(i in 0 until bsnAtchInfo!!.length()) {
                val bsnAtchItem = bsnAtchInfo!!.getJSONObject(i)

                val bsnAtchFileInfo = bsnAtchItem.getString("fileseInfo")

                view.commPrmisnDtaBtn.backgroundTintList = when (bsnAtchFileInfo) {
                    "A200006018" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                view.commLsCtrtcBtn.backgroundTintList = when(bsnAtchFileInfo) {
                    "A200006019" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }
                view.commBsnmCeregrtBtn.backgroundTintList = when(bsnAtchFileInfo) {
                    "A200006020" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                    else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                }

            }

            settingSearchCamerasView(bsnAtchInfo)
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

        if(ThingBsnObject.thingNewSearch.equals("N")) {
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

                                    val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                                    val downloadFile = File("$downloadDirectory/${item.getString( "atfl")}")

                                    val wtnncPicBitmap = BitmapFactory.decodeStream(response.body?.byteStream())

                                    FileUtil.run {
                                        createDir(downloadDirectory)
                                        saveBitmapToFileCache(wtnncPicBitmap, "$downloadDirectory/${item.getString( "atfl")}")
                                    }

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
            R.id.bsnClDivSpinner -> {
                when (position) {
                    1 -> {
                        bssMthCoLayout.visibleView()
                    }
                    2 -> {
                        bssMthCoLayout.invisibleView()
                    }
                    else -> {
                        bssMthCoLayout.invisibleView()
                    }
                }
            }
            R.id.bsnSclasSpinner -> {
                when (position) {
                    0 -> bsnBrdBaseLayout.goneView()
                    1 -> bsnBrdBaseLayout.goneView()
                    2 -> bsnBrdBaseLayout.goneView()
                    3 -> {
                        bsnBrdBaseLayout.visibleView()
//                        var requireArr = mutableListOf<TextView>(tv_bsn_require6, tv_bsn_require7, tv_bsn_require8, tv_bsn_require9)
//                        setRequireContent(requireArr)
                    }
                }
            }

            // 점유구분
            R.id.bsnCommPossesnSeSpinner -> {
                when (position) {
                    0 -> {
                        bsnPssLinearLayout1.goneView()
                        bsnPssLinearLayout2.goneView()
                        bsnPssLinearLayout3.goneView()
                    }
                    1 -> {
                        bsnPssLinearLayout1.goneView()
                        bsnPssLinearLayout2.goneView()
                        bsnPssLinearLayout3.goneView()
                    }
                    2 -> {
                        bsnPssLinearLayout1.visibleView()
                        bsnPssLinearLayout2.visibleView()
                        bsnPssLinearLayout3.visibleView()
                    }
                }
            }

        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addBsnData() {

//        var dataChkValue: Int = 0 // 입력데이터 확인 (0 -> ok, 1 -> no)

        // 대분류 입력
        ThingBsnObject.thingLrgeCl = "A011005" // 영업
        Log.d("bsnTest", "대분류 : ${ThingBsnObject.thingLrgeCl}")

        // 소분류
        ThingBsnObject.thingSmallCl = when (mActivity.bsnSclasSpinner.selectedItemPosition) {
            1 -> "A016001" // 일반영업
            2 -> "A016050" // 잠업
            3 -> "A016040" // 축산
            else -> ""
        }
        Log.d("bsnTest", "소분류 : ${ThingBsnObject.thingSmallCl}")

        // 물건의 종류
        if(ThingBsnObject.thingSmallCl.equals("A016040")) {
            ThingBsnObject.thingKnd = "축산보상"//
        } else {
            ThingBsnObject.thingKnd = "영업보상"//
        }
        


        ThingBsnObject.bgnnAr = mActivity.bsnBgnnAr.text.toString() // 전체면적
        Log.d("bsnTest", "전체면적 : ${ThingBsnObject.bgnnAr}")

        ThingBsnObject.incrprAr = mActivity.bsnIncrprAr.text.toString() // 편입면적
        Log.d("bsnTest", "편입면적 : ${ThingBsnObject.incrprAr}")

        ThingBsnObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.bsnUnitSpinner.selectedItemPosition)
//            when (mActivity.bsnUnitSpinner.selectedItemPosition) { // 단위
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
        Log.d("bsnTest", "단위 : ${ThingBsnObject.unitCl}")

        ThingBsnObject.arComputBasis = mActivity.bsnArComputBasis.text.toString() // 면적산출근거
        Log.d("bsnTest", "면적산출근거 : ${ThingBsnObject.arComputBasis}")


        //휴업/폐업
        ThingBsnObject.bsnCl = when(mActivity.bsnClDivSpinner.selectedItemPosition) {
            1 -> "A017001"
            2 -> "A017002"
            else -> ""
        }
        ThingBsnObject.sssMthCo = mActivity.bssMthCoText.text.toString()


        //장소의 적법성
        val addBuldLinkCnt = mActivity.addBsnBuldLinkViewGroup.childCount
        val buldLinkArray = JSONArray()
        val buldLinkObject = JSONObject()
        for(i in 0 until addBuldLinkCnt) {
            val buldLinkItem = JSONObject()
            val buldSelectData = ThingBsnObject.selectBuldLinkData?.get(i)
            val buldLinkViewGroup = mActivity.addBsnBuldLinkViewGroup
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
        buldLinkObject.put("bsnBuldLink", buldLinkArray)
        ThingBsnObject.addBuldLinkList = buldLinkObject

        // 점유의 적법성 근거
        // 적법여부
        ThingBsnObject.pssLgalAt = when (mActivity.pssLgalAt.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "(점유)적법여부 : ${ThingBsnObject.pssLgalAt}")

        // 점유구분 입력
        ThingBsnObject.pssPssTy = when (mActivity.bsnCommPossesnSeSpinner.selectedItemPosition) {
            1 -> "1" // 자가
            2 -> "2" // 임차
            else -> ""
        }
        Log.d("bsnTest", "점유구분 : ${ThingBsnObject.pssPssTy}")

        // 임대차계약서 유무
        if(ThingBsnObject.pssPssTy.equals("1")) {
            ThingBsnObject.pssHireCntrctAt = "X"
        } else {
            ThingBsnObject.pssHireCntrctAt = when (mActivity.pssHireCntrctAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
        }

        Log.d("bsnTest", "임대차계약서 유무 : ${ThingBsnObject.pssHireCntrctAt}")

        // 임차인 성명
        ThingBsnObject.pssRentName = mActivity.pssRentNameText.text.toString()
        Log.d("bsnTest", "임차인 성명 : ${ThingBsnObject.pssRentName}")

        // 임대인 성명
        ThingBsnObject.pssHireName = mActivity.pssHireNameText.text.toString()
        Log.d("bsnTest", "임대인 성명 : ${ThingBsnObject.pssHireName}")

        // 임차기간 시작
        Log.d("bsnTest", "임차기간 시작 : ${ThingBsnObject.pssRentBgnde}")

        // 임차기간 종료
        Log.d("bsnTest", "임차기간 종료 : ${ThingBsnObject.pssRentEndde}")

        // 보증금
        val pssGrtString = mActivity.bsnGtnText.text.toString()
        var ss = Regex("[^A-Za-z0-9]")
        val pssGrtStringSS = ss.replace(pssGrtString, "")
        ThingBsnObject.pssGtn = pssGrtStringSS
        Log.d("bsnTest", "보증금 : ${ThingBsnObject.pssGtn}")

        // 월세
        val pssMtht = mActivity.bsnMthtText.text.toString()
        val pssMthtSS = ss.replace(pssMtht, "")
        ThingBsnObject.pssMtht = pssMthtSS
        Log.d("bsnTest", "월세 : ${ThingBsnObject.pssMtht}")

        // 계약(소유)위치
        ThingBsnObject.pssCntrctLc = mActivity.bsnCntrctLcText.text.toString()
        Log.d("bsnTest", "계약(소유)위치 : ${ThingBsnObject.pssCntrctLc}")

        // 계약(소유)면적(㎡)
        ThingBsnObject.pssCntrctAr = mActivity.bsnCntrctArText.text.toString()
        Log.d("bsnTest", "계약(소유)면적(㎡) : ${ThingBsnObject.pssCntrctAr}")

        // 특약
        ThingBsnObject.pssSpccntr = mActivity.bsnSpccntrText.text.toString()
        Log.d("bsnTest", "특약 : ${ThingBsnObject.pssSpccntr}")

        // 영업의 적법성 근거
        // 적법여부
        ThingBsnObject.bsnProperAt = when (mActivity.bsnProperAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "(영업)적법여부 : ${ThingBsnObject.bsnProperAt}")

        // 영업중 여부
        ThingBsnObject.bsnSgnProsAt = when (mActivity.bsnOpenAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "영업중 여부 : ${ThingBsnObject.bsnSgnProsAt}")

        // 허가등구분 입력
        ThingBsnObject.bsnPrmisnCl = CommonCodeInfoList.getCodeId("A028", mActivity.bsnPrmisnSeSpinner.selectedItemPosition)
//            when (mActivity.bsnPrmisnSeSpinner.selectedItemPosition) {
//            1 -> "A028001" // 자유업
//            2 -> "A028002" // 허가업
//            3 -> "A028003" // 인가업
//            4 -> "A028004" // 신고업
//            5 -> "A028005" // 면허업
//            6 -> "A028006" // 등록업
//            7 -> "A028007" // 기타업종
//            else -> ""
//        }
        Log.d("bsnTest", "허가등 구분 : ${ThingBsnObject.bsnPrmisnCl}")

        // 허가등 받은자
        ThingBsnObject.bsnPrmsTrgetNm = mActivity.bsnPrmisnRecivNm.text.toString()
        Log.d("bsnTest", "허가등 받은자 : ${ThingBsnObject.bsnPrmsTrgetNm}")

        // 허가등 번호
        ThingBsnObject.bsnPrmisnNo = mActivity.bsnPrmisnNm.text.toString()
        Log.d("bsnTest", "허가등 번호 : ${ThingBsnObject.bsnPrmisnNo}")

        // 허가등 일자
        ThingBsnObject.bsnPrmisnDe = mActivity.bsnPrmisnDe.text.toString()
        Log.d("bsnTest", "허가등 일자 : ${ThingBsnObject.bsnPrmisnDe}")

        // 허가등기간 시작일자
        Log.d("bsnTest", "허가등기간 시작일자 : ${ThingBsnObject.bsnPrmisnBgnde}")

        // 허가등기간 종료일자
        Log.d("bsnTest", "허가등기간 종료일자 : ${ThingBsnObject.bsnPrmisnEndde}")

        // 허가등 기관
        ThingBsnObject.bsnPrmisnInstt = mActivity.bsnPrmisnInstt.text.toString()
        Log.d("bsnTest", "허가등 기관 : ${ThingBsnObject.bsnPrmisnInstt}")

        // 영업기간 시작일자
        Log.d("bsnTest", "영업기간 시작일자 : ${ThingBsnObject.bsnBsnpdBgnde}")

        // 영업기간 종료일자
        Log.d("bsnTest", "영업기간 종료일자 : ${ThingBsnObject.bsnBsnpdEndde}")

        // 영업장위치
        ThingBsnObject.bsnBsnplcLc = mActivity.bsnBuzplcLc.text.toString()
        Log.d("bsnTest", "영업장위치 : ${ThingBsnObject.bsnBsnplcLc}")

        // 영업장면적
        ThingBsnObject.bsnBsnplcAr = mActivity.bsnBuzplcAr.text.toString()
        Log.d("bsnTest", "영업장면적 : ${ThingBsnObject.bsnBsnplcAr}")

        // 인적, 물적시설 여부
        // 적격여부
        ThingBsnObject.hmftyProperAt = when (mActivity.hmftyProperAt.isChecked) {
            true -> "Y"
            else -> "N"
        }

        // 간판명
        ThingBsnObject.hmftySgnbrdNm = mActivity.bsnSgnbrdNm.text.toString()
        Log.d("bsnTest", "간판명 : ${ThingBsnObject.hmftySgnbrdNm}")

        // 상주인력
        ThingBsnObject.hmftyResdngHnf = mActivity.bsnResdngHn.text.toString()
        Log.d("bsnTest", "상주인력 : ${ThingBsnObject.hmftyResdngHnf}")

        // 시설물 여부
        ThingBsnObject.hmftyFtyAt = when (mActivity.bsnFcltsAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "시설물 여부 : ${ThingBsnObject.hmftyFtyAt}")

        // 동일세대원 다른 영업보상 수령여부
        ThingBsnObject.hmftySgnSecdAt = when (mActivity.bsnSamenssHshldAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "동일세대원 다른 영업보상 수령여부 : ${ThingBsnObject.hmftySgnSecdAt}")

        // 현황부합여부
        ThingBsnObject.bizrdtlsSttAt = when (mActivity.bsnSttusConsistAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "현황부합여부 : ${ThingBsnObject.bizrdtlsSttAt}")

        // 영리구분
        ThingBsnObject.bizrdtlsPrftmkTy = when (mActivity.bsnPrftmkSeSpinner.selectedItemPosition) {
            1 -> "1" // 영리
            2 -> "2" // 비영리
            else -> ""
        }
        Log.d("bsnTest", "영리구분 : ${ThingBsnObject.bizrdtlsPrftmkTy}")

        // 등록구분
        ThingBsnObject.bizrdtlsRegTy = when (mActivity.bsnRegistSeSpinner.selectedItemPosition) {
            1 -> "1" // 개인
            2 -> "2" // 법인
            else -> ""
        }
        Log.d("bsnTest", "등록구분 : ${ThingBsnObject.bizrdtlsRegTy}")

        // 대표자
        ThingBsnObject.bizrdtlsRprsntvNm = mActivity.bsnRprsntv.text.toString()
        Log.d("bsnTest", "대표자 : ${ThingBsnObject.bizrdtlsRprsntvNm}")

        // 상호
        ThingBsnObject.bizrdtlsMtlty = mActivity.bsnCmpnm.text.toString()
        Log.d("bsnTest", "상호 : ${ThingBsnObject.bizrdtlsMtlty}")

        // 업종
        ThingBsnObject.bizrdtlsInduty = mActivity.bsnInduty.text.toString()
        Log.d("bsnTest", "업종 : ${ThingBsnObject.bizrdtlsInduty}")

        // 업태
        ThingBsnObject.bizrdtlsBizcnd = mActivity.bsnBizcnd.text.toString()
        Log.d("bsnTest", "업태 : ${ThingBsnObject.bizrdtlsBizcnd}")

        // 등록번호
        ThingBsnObject.bizrdtlsBizrno = mActivity.RegistrationNm.text.toString()
        Log.d("bsnTest", "등록번호 : ${ThingBsnObject.bizrdtlsBizrno}")

        // 등록일
        ThingBsnObject.bizrdtlsBizDe = mActivity.bsnRgsde.text.toString()
        Log.d("bsnTest", "등록일 : ${ThingBsnObject.bizrdtlsBizDe}")

        // 고시 1년전 등록여부
        ThingBsnObject.bizrdtlsRegAt = when (mActivity.bsnNtfcRgsAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }
        Log.d("bsnTest", "고시 1년전 등록여부 : ${ThingBsnObject.bizrdtlsRegAt}")

        ThingBsnObject.ownerCnfirmBasisCl = when (mActivity.bsnOwnerCnfirmBasisSpinner.selectedItemPosition) {
            1 -> "A035001"
            2 -> "A035002"
            3 -> "A035003"
            4 -> "A035004"
            5 -> "A035005"
            else -> ""
        }
//        ThingBsnObject.acqsCl = when (mActivity.bsnAcqsSeSpinner.selectedItemPosition) {
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
//        ThingBsnObject.inclsCl = when(mActivity.bsnInclsSeSpinner.selectedItemPosition) {
//            1->"A007001"
//            2->"A007002"
//            3->"A007003"
//            4->"A007004"
//            else->""
//        }

        ThingBsnObject.acqsCl = "A025001" // 취득
        ThingBsnObject.inclsCl = "A007001" // 편입

        ThingBsnObject.rwTrgetAt = when(mActivity.rwTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }

        ThingBsnObject.apasmtTrgetAt = when(mActivity.apasmtTrgetAtChk.isChecked) {
            true -> "Y"
            else -> "N"
        }

        // 특이사항
        ThingBsnObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString() // 특이사항
        Log.d("bsnTest", "특이사항 : ${ThingBsnObject.paclrMatter}")

        ThingBsnObject.referMatter = mActivity.includeReferMatterEdit.text.toString()
        ThingBsnObject.rm = mActivity.includeRmEdit.text.toString()

        /**
         * 축산업 사육 내역
         */
        val thingBrdDtlspdJSON = JSONObject()
        var thingBrdpdItem = JSONObject()
        val thingBrdpdArray = JSONArray()
        if(ThingBsnObject.thingSmallCl.equals("A016040")) {
//        사육내역 추가부분
            val addBrdCnt = mActivity.bsnBrdBaseViewGroup.childCount
            for (i in 0 until addBrdCnt) {
                val thingBrdpdAddItem = JSONObject()

                Log.d("bsnTest", "******************************************************************")

//                val bsnAddItemMap: MutableMap<String, String> = mutableMapOf()
                val bsnBrdViewGroup = mActivity.bsnBrdBaseViewGroup

                val addBrdDtlsView = bsnBrdViewGroup.getChildAt(addBrdViewCnt) as ViewGroup
                val brdDtlsViewFirst = addBrdDtlsView.getChildAt(1) as ViewGroup

                val bsnPrpperAtView = brdDtlsViewFirst.getChildAt(0) as ViewGroup
                val bsnPrpperAtChk = bsnPrpperAtView.getChildAt(0) as CheckBox

                val bsnBrdLvstckNmText = brdDtlsViewFirst.getChildAt(1) as EditText
                val bsnBrdPdBgndeText = brdDtlsViewFirst.getChildAt(2) as TextView
                val bsnBrdPdEnddeText = brdDtlsViewFirst.getChildAt(3) as TextView
                val bsnBrdQy = brdDtlsViewFirst.getChildAt(4) as EditText

                val brdDtlsViewSecond = addBrdDtlsView.getChildAt(3) as ViewGroup

                //val bsnStdLvstckClView = addBrdDtlsView.getChildAt(4) as ViewGroup
                //val bsnStdLvstckClSpinner = brdDtlsViewSecond.getChildAt(0) as Spinner

                // FIXME: 2021-12-06 기존 사육내역추가 Bug Fix
                val bsnStdLvstckClSpinner = ((addBrdDtlsView.getChildAt(3) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as Spinner
                val bsnStdQyText = brdDtlsViewSecond.getChildAt(1) as TextView
                val bsnCnvrsnQyText = brdDtlsViewSecond.getChildAt(2) as TextView

                // 적격여부
                thingBrdpdAddItem.put("brdProperAt", when (bsnPrpperAtChk.isChecked) {
                    true -> "Y"
                    else -> "N"
                })
                // 사육 가축명
                thingBrdpdAddItem.put("brdLvstckNm", bsnBrdLvstckNmText.text.toString())

                // 사육기간 시작일자
                if (ThingBsnObject.addBsnBrdPdBgnde.size != 0) {
                    thingBrdpdAddItem.put("brdPdBgnde",bsnBrdPdBgndeText.text.toString())
                }

                // 사육기간 종료일자
                if (ThingBsnObject.addBsnBrdPdEndde.size != 0) {
                    thingBrdpdAddItem.put("brdPdEndde",bsnBrdPdEnddeText.text.toString())
                }

                // 사육 수량
                thingBrdpdAddItem.put("brdQy",bsnBrdQy.text.toString())

                // 기준 가축명
                thingBrdpdAddItem.put("stdLvstckCl", when (bsnStdLvstckClSpinner.selectedItemPosition) {
                    1 -> "A001001"
                    2 -> "A001002"
                    3 -> "A001003"
                    4 -> "A001004"
                    5 -> "A001005"
                    6 -> "A001006"
                    7 -> "A001007"
                    8 -> "A001008"
                    else -> ""
                })

                // 기준 수량
                thingBrdpdAddItem.put("stdQy",bsnStdQyText.text.toString())
                // 환산 수량
                thingBrdpdAddItem.put("cnvrsnQy",bsnCnvrsnQyText.text.toString())

                thingBrdpdAddItem.put("stdUnit", when (bsnStdLvstckClSpinner.selectedItemPosition) {
                    1 -> "마리"
                    2 -> "마리"
                    3 -> "마리"
                    4 -> "마리"
                    5 -> "마리"
                    6 -> "마리"
                    7 -> "마리"
                    8 -> "군"
                    else -> ""
                })

                thingBrdpdArray.put(thingBrdpdAddItem)
                Log.d("bsnTest", "*****************************************************************")
            }

            thingBrdDtlspdJSON.put("brdDtlsList", thingBrdpdArray)
        } else {
//            val thingBrdpdArray = JSONArray()
            thingBrdDtlspdJSON.put("brdDtlsList", thingBrdpdArray)
        }
        ThingBsnObject.addBsnBrdpdList = thingBrdDtlspdJSON

        // 구조 및 규격
        if(ThingBsnObject.thingSmallCl == "A016040") { // 축산업 구조 및 규격
            ThingBsnObject.strctNdStndrd = StringBuilder().apply {
                when (ThingBsnObject.bsnCl) {
                    "A017001" -> append("폐업보상, ")
                    "A017002" -> append("휴업보상, ")
                }
                append(ThingBsnObject.hmftySgnbrdNm.toString())
                if(ThingBsnObject.pssPssTy.equals("1")) {
                    append("(자가, ")
                } else {
                    append("(임차, ")
                }
                when(ThingBsnObject.bsnPrmisnCl) {
                    "A028001" -> append("자유업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 자유업
                    "A028002" -> append("허가업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 허가업
                    "A028003" -> append("인가업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 인가업
                    "A028004" -> append("신고업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 신고업
                    "A028005" -> append("면허업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 면허업
                    "A028006" -> append("등록업(" + ThingBsnObject.bsnPrmisnNo + "), ")// 등록업
                    "A028007" -> append("기타업종(" + ThingBsnObject.bsnPrmisnNo + "), ")// 기타업종
                    else -> throw IllegalStateException("영업 -> 잘못된 업종 및 선택을 안함")
                }

                var itemSize = 0
                for (i in 0 until thingBrdpdArray.length()-1) {
                    val brdItem = thingBrdpdArray.getJSONObject(i) as JSONObject
                    append(brdItem.getString("brdLvstckNm") + "(" + brdItem.getString("brdQy") + brdItem.getString("stdUnit") +")")

                    itemSize++

                    if(itemSize != thingBrdpdArray.length()-1) {
                        append(", ")
                    }
                }
                append(")")
                logUtil.d("축산업 스트링완성 -> ${ThingBsnObject.strctNdStndrd}")


            }.toString()
        } else { // 영업,잠업 구조 및 규격
            ThingBsnObject.strctNdStndrd = StringBuilder().apply {

                when (ThingBsnObject.bsnCl) {
                    "A017001" -> {
                        append("폐업보상, ")
                    }
                    "A017002" ->{
                        append("휴업보상, ")
                    }
                }
                append(ThingBsnObject.hmftySgnbrdNm.toString())
                if(ThingBsnObject.pssPssTy.equals("1")) {
                    append("(자가, ")
                } else {
                    append("(임차, ")
                }
                when(ThingBsnObject.bsnPrmisnCl) {
                    "A028001" -> append("자유업(" + ThingBsnObject.bsnPrmisnNo + "))")// 자유업
                    "A028002" -> append("허가업(" + ThingBsnObject.bsnPrmisnNo + "))")// 허가업
                    "A028003" -> append("인가업(" + ThingBsnObject.bsnPrmisnNo + "))")// 인가업
                    "A028004" -> append("신고업(" + ThingBsnObject.bsnPrmisnNo + "))")// 신고업
                    "A028005" -> append("면허업(" + ThingBsnObject.bsnPrmisnNo + "))")// 면허업
                    "A028006" -> append("등록업(" + ThingBsnObject.bsnPrmisnNo + "))")// 등록업
                    "A028007" -> append("기타업종(" + ThingBsnObject.bsnPrmisnNo + "))")// 기타업종
                    //else -> throw IllegalStateException("영업 -> 잘못된 업종")
                    else -> ToastUtil(context).msg_error("영업 -> 잘못된 업종", 500)
                }

            }.toString()

            logUtil.d("영업 스트링완성 -> ${ThingBsnObject.strctNdStndrd}")

        }


        // 시설물 추가부분
        val addThingCnt = mActivity.bsnBaseViewGroup.childCount

        val thingBsnAddJSON = JSONObject()
        val thingBsnAddArray = JSONArray()
        if(addThingCnt > 0) {
            for (i in 0 until addThingCnt) {
                Log.d("bsnTest", "******************************************************************")

                val thingBsnAddItem = JSONObject()

                //            val bsnAddItemMap: MutableMap<String, String> = mutableMapOf()
                val bsnViewGroup = mActivity.bsnBaseViewGroup // 물건 추가 Base ViewGroup
                val addLayoutItem = bsnViewGroup.getChildAt(i) as ViewGroup // i번째 추가 부분
                val addViewGroup1 = addLayoutItem.getChildAt(1) as ViewGroup
                val addViewGroup2 = addLayoutItem.getChildAt(3) as ViewGroup
                val addViewGroup3 = addLayoutItem.getChildAt(5) as ViewGroup
                val addViewGroup4 = addLayoutItem.getChildAt(7) as ViewGroup

                // 소분류
                val addSpinnerLayout1 = addViewGroup1.getChildAt(0) as ViewGroup
                val addSmallClSpinner = addSpinnerLayout1.getChildAt(0) as Spinner // 추가 시설물 소분류
                when (addSmallClSpinner.selectedItemPosition) {
                    0 -> thingBsnAddItem.put("thingSmallCl", "")
                    1 -> { // 시설물
                        when (addSmallClSpinner.selectedItem.toString()) {
                            "(영업)시설물" -> thingBsnAddItem.put("thingSmallCl", "A016002") // (영업)시설물
                            "(잠업)시설물" -> thingBsnAddItem.put("thingSmallCl", "A016051") // (잠업)시설물
                            "(축산)시설물" -> thingBsnAddItem.put("thingSmallCl", "A016041") // (축산)시설물
                        }
                        //                    Log.d("bsnTest", "소분류 : ${bsnAddItemMap["thingSmallClValue"]}")
                    }
//                    2 -> { // 실직자
//                        when (addSmallClSpinner.selectedItem.toString()) {
//                            "(영업)실직자" -> thingBsnAddItem.put("thingSmallCl", "A016004") // (영업)실직자
//                            "(잠업)실직자" -> thingBsnAddItem.put("thingSmallCl", "A016014") // (잠업)실직자
//                            "(축산)실직자" -> thingBsnAddItem.put("thingSmallCl", "A016043") // (축산)실직자
//                        }
//                        //                    Log.d("bsnTest", "소분류 : ${bsnAddItemMap["thingSmallClValue"]}")
//
//                        // 구분
//                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
//                        //                    bsnAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
//                        thingBsnAddItem.put("unemplTy", addUnemplTy.text.toString())
//                        //                    Log.d("bsnTest", "구분 : ${bsnAddItemMap["bsnUnemplTy"]}")
//
//                        // 일수
//                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
//                        //                    bsnAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        thingBsnAddItem.put("unemplCo", addUnemplCo.text.toString())
//                        //                    Log.d("bsnTest", "일수 : ${bsnAddItemMap["bsnUnemplCo"]}")
//
//                        // 평균임금
//                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
//                        //                    bsnAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        thingBsnAddItem.put("avrgWage", addAvrgWage.text.toString())
//                        //                    Log.d("bsnTest", "평균임금 : ${bsnAddItemMap["bsnAvrgWage"]}")
//
//                        // 통상임금
//                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
//                        //                    bsnAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        thingBsnAddItem.put("odygs", addOdygs.text.toString())
//                        //                    Log.d("bsnTest", "통상임금 : ${bsnAddItemMap["bsnOdygs"]}")
//
//                        //  근로장소이전일자
//                        val addBeforeDe = addViewGroup4.getChildAt(0) as TextView
//                        //                    bsnAddItemMap["bsnBeforeDe"] = addBeforeDe.text.toString()
//                        thingBsnAddItem.put("beforeDe", addBeforeDe.text.toString())
//                        //                    Log.d("bsnTest", "근로장소이전일자 : ${bsnAddItemMap["bsnBeforeDe"]}")
//
//                        // 실직일자
//                        val addUnemplDe = addViewGroup4.getChildAt(1) as TextView
//                        //                    bsnAddItemMap["bsnUnemplDe"] = addUnemplDe.text.toString()
//                        thingBsnAddItem.put("unemplDe", addUnemplDe.text.toString())
//                        //                    Log.d("bsnTest", "실적일자 : ${bsnAddItemMap["bsnUnemplDe"]}")
//
//                        // 실직사유
//                        val addUnemplResn = addViewGroup4.getChildAt(2) as EditText
//                        //                    bsnAddItemMap["bsnUnemplResn"] = addUnemplResn.text.toString()
//                        thingBsnAddItem.put("unemplResn", addUnemplResn.text.toString())
//                        //                    Log.d("bsnTest", "실적사유 : ${bsnAddItemMap["bsnUnemplResn"]}")
//                    }
//                    3 -> { // 휴직자
//                        when (addSmallClSpinner.selectedItem.toString()) {
//                            "(영업)휴직자" -> thingBsnAddItem.put("thingSmallCl", "A016003") // (영업)휴직자
//                            "(잠업)휴직자" -> thingBsnAddItem.put("thingSmallCl", "A016013") // (잠업)휴직자
//                            "(축산)휴직자" -> thingBsnAddItem.put("thingSmallCl", "A016042") // (축산)휴직자
//                        }
//                        //                    Log.d("bsnTest", "휴직자 : ${bsnAddItemMap["thingSmallClValue"]}")
//
//                        // 구분
//                        val addUnemplTy = addViewGroup3.getChildAt(0) as EditText
//                        //                    bsnAddItemMap["bsnUnemplTy"] = addUnemplTy.text.toString()
//                        thingBsnAddItem.put("unemplTy", addUnemplTy.text.toString())
//                        //                    Log.d("bsnTest", "구분 : ${bsnAddItemMap["bsnUnemplTy"]}")
//
//                        // 일수
//                        val addUnemplCo = addViewGroup3.getChildAt(1) as EditText
//                        //                    bsnAddItemMap["bsnUnemplCo"] = addUnemplCo.text.toString()
//                        thingBsnAddItem.put("unemplCo", addUnemplCo.text.toString())
//                        //                    Log.d("bsnTest", "일수 : ${bsnAddItemMap["bsnUnemplCo"]}")
//
//                        // 평균임금
//                        val addAvrgWage = addViewGroup3.getChildAt(2) as EditText
//                        //                    bsnAddItemMap["bsnAvrgWage"] = addAvrgWage.text.toString()
//                        thingBsnAddItem.put("avrgWage", addAvrgWage.text.toString())
//                        //                    Log.d("bsnTest", "평균임금 : ${bsnAddItemMap["bsnAvrgWage"]}")
//
//                        // 통상임금
//                        val addOdygs = addViewGroup3.getChildAt(3) as EditText
//                        //                    bsnAddItemMap["bsnOdygs"] = addOdygs.text.toString()
//                        thingBsnAddItem.put("odygs", addOdygs.text.toString())
//                        //                    Log.d("bsnTest", "통상임금 : ${bsnAddItemMap["bsnOdygs"]}")
//                    }
//                    4 -> { // 영업보상특례
//                        when (addSmallClSpinner.selectedItem.toString()) {
//                            "(영업)영업보상특례" -> thingBsnAddItem.put("thingSmallCl", "A016005") // (영업)영업보상특례
//                            "(잠업)영업보상특례" -> thingBsnAddItem.put("thingSmallCl", "A016015") // (잠업)영업보상특례
//                            "(축산)영업보상특례" -> thingBsnAddItem.put("thingSmallCl", "A016044") // (축산)영업보상특례
//                        }
//                        //                    Log.d("bsnTest", "휴직자 : ${bsnAddItemMap["thingSmallClValue"]}")
//                    }
                }

                // 물건의 종류
                val addThingKndText = addViewGroup1.getChildAt(1) as EditText
                //            bsnAddItemMap["thingKnd"] = addThingKndText.text.toString()
                thingBsnAddItem.put("thingKnd", addThingKndText.text.toString())
                //            Log.d("bsnTest", "물건의 종류 : ${bsnAddItemMap["thingKnd"]}")

                // 구조 및 규격
                val addStrctNdStrndrdText = addViewGroup1.getChildAt(2) as EditText
                thingBsnAddItem.put("strctNdStrndrd", addStrctNdStrndrdText.text.toString())

                // 전체면적
                val addViewGroupLayout = addViewGroup2.getChildAt(0) as ViewGroup
                val addBgnnArText = addViewGroupLayout.getChildAt(0) as EditText
                thingBsnAddItem.put("bgnnAr", addBgnnArText.text.toString())

                // 편입면적
                val addIncrprArText = addViewGroupLayout.getChildAt(1) as EditText
                thingBsnAddItem.put("incrprAr", addIncrprArText.text.toString())

                // 단위
                val addSpinnerLayout2 = addViewGroup2.getChildAt(1) as ViewGroup
                val addUnitClSpinner = addSpinnerLayout2.getChildAt(0) as Spinner
                thingBsnAddItem.put(
                    "unitCl", CommonCodeInfoList.getCodeId("A009", addUnitClSpinner.selectedItemPosition)
//                    when (addUnitClSpinner.selectedItemPosition) {
//                        1 -> "A009001"
//                        2 -> "A009002"
//                        3 -> "A009003"
//                        4 -> "A009004"
//                        5 -> "A009005"
//                        6 -> "A009006"
//                        7 -> "A009007"
//                        8 -> "A009008"
//                        9 -> "A009009"
//                        10 -> "A009010"
//                        11 -> "A009011"
//                        12 -> "A009012"
//                        13 -> "A009013"
//                        14 -> "A009014"
//                        15 -> "A009015"
//                        16 -> "A009016"
//                        17 -> "A009017"
//                        18 -> "A009018"
//                        19 -> "A009019"
//                        20 -> "A009020"
//                        21 -> "A009021"
//                        22 -> "A009022"
//                        23 -> "A009023"
//                        24 -> "A009024"
//                        25 -> "A009025"
//                        26 -> "A009026"
//                        27 -> "A009027"
//                        28 -> "A009028"
//                        29 -> "A009029"
//                        30 -> "A009030"
//                        31 -> "A009031"
//                        32 -> "A009032"
//                        33 -> "A009033"
//                        34 -> "A009034"
//                        35 -> "A009035"
//                        36 -> "A009036"
//                        37 -> "A009037"
//                        38 -> "A009038"
//                        39 -> "A009039"
//                        40 -> "A009040"
//                        41 -> "A009041"
//                        42 -> "A009042"
//                        43 -> "A009043"
//                        44 -> "A009044"
//                        45 -> "A009045"
//                        46 -> "A009046"
//                        47 -> "A009047"
//                        48 -> "A009048"
//                        49 -> "A009049"
//                        50 -> "A009050"
//                        51 -> "A009051"
//                        52 -> "A009052"
//                        53 -> "A009053"
//                        54 -> "A009054"
//                        55 -> "A009055"
//                        56 -> "A009056"
//                        57 -> "A009057"
//                        58 -> "A009058"
//                        59 -> "A009059"
//                        60 -> "A009060"
//                        61 -> "A009061"
//                        62 -> "A009062"
//                        63 -> "A009063"
//                        64 -> "A009064"
//                        65 -> "A009065"
//                        66 -> "A009066"
//                        67 -> "A009067"
//                        68 -> "A009068"
//                        else -> ""
//                    }
                )

                // 면적산출근거
                val addArComputBasisText = addViewGroup2.getChildAt(2) as EditText
                thingBsnAddItem.put("arComputBasis", addArComputBasisText.text.toString())

                thingBsnAddArray.put(thingBsnAddItem)
                Log.d("bsnTest", "******************************************************************")
            }
            thingBsnAddJSON.put("bsnThing", thingBsnAddArray)
        } else {
            thingBsnAddJSON.put("bsnThing", thingBsnAddArray)

        }
        ThingBsnObject.addBsnThingList = thingBsnAddJSON
//        ThingBsnObject.addBsnBrdPdBgnde.clear()
//        ThingBsnObject.bsnAddItemList = bsnAddThingList

    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

    override fun onPositiveClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("type----------------------->: $type")
        when(type) {
            "bsnSkitchConfirm" -> {
                (activity as MapActivity).getBuldLinkToGeomData(ThingBsnObject.selectBuldLinkData!![0].geoms, "bsn", true)
            }

        }
    }

    override fun onNegativeClickListener(dialog: DialogInterface, type: String) {
        logUtil.d("type----------------------->: $type")
        when(type) {
            "bsnSkitchConfirm" -> {
                (activity as MapActivity).getBuldLinkToGeomData(ThingBsnObject.selectBuldLinkData!![0].geoms, "bsn", false)
            }
        }
    }
}