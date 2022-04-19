/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_bsn_search.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.android.synthetic.main.thing_regstr_dialog.view.*
import kotlinx.android.synthetic.main.thing_rgist_dialog.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.*
import kotlinx.android.synthetic.main.thing_search_gnrl.searchShetchBtn
import kotlinx.android.synthetic.main.thing_search_gnrl.view.*
import kotlinx.coroutines.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.CustomDropDownAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.CommonCodeInfoList
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import kr.or.kreb.ncms.mobile.util.*
import kr.or.kreb.ncms.mobile.util.PermissionUtil.logUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class ThingSearchFragment(val activity: Activity, context: Context, val fragmentActivity: FragmentActivity) :
    BaseFragment(),
    AdapterView.OnItemSelectedListener {

    private val mActivity: Activity = activity
    private val mContext: Context = context

    private val wtnncUtill: WtnncUtil = WtnncUtil(activity, context)

    var thingDataJson: JSONObject? = null

    var wtnncImageAdapter: WtnncImageAdapter? = null

    var dcsnAt: String? = "N"

//    var builder: MaterialAlertDialogBuilder? = null
//    var dialogUtil: DialogUtil? = null
//    private var progressDialog: AlertDialog? = null
//    private var toastUtil: ToastUtil = ToastUtil(mContext)

    init { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.thing_search_gnrl, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        builder = context?.let { MaterialAlertDialogBuilder(it) }!!
        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)

        //지장물조서 사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextViewCamera(
                requireActivity(),
                Constants.CAMERA_ACT,
                PreferenceUtil.getString(context!!, "saupCode", "defaual"),
                BizEnum.THING,
                "A200006012",
                "현장사진",
                CameraEnum.DEFAULT
            )
        }

        //스케치버튼 클릭
        searchShetchBtn.setOnClickListener {

            LandInfoObject.mapPos.clear()
            LandInfoObject.clickLatLng.clear()

            (mActivity as MapActivity).settingCartoMap(null, null)
        }

        thingKndEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.thingKnd = txtString
            }

            false
        }
        thingStrctNdStndrdEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.strctNdStndrd = txtString
            }

            false
        }
        thingStrctNdStndrdEditR.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.strctNdStrndrdR = txtString
                ThingWtnObject.strctNdStndrd = txtString
            }

            false
        }

        thingStrctNdStndrdEditH.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.strctNdStrndrdH = txtString
            }

            false
        }
        thingWdptBgnnArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingWdptincrprArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()

                ThingWtnObject.incrprAr = txtString
            }

            false
        }

        thingNrmltpltAtChk.setOnClickListener {
            if (thingNrmltpltAtChk.isChecked) {
                ThingWtnObject.nrmltpltAt = "Y"
            } else {
                ThingWtnObject.nrmltpltAt = "N"
            }
        }
        thingNrmltpltAtLl.setOnClickListener {
            if(dcsnAt == "Y") {
                toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
            } else {

                if (thingNrmltpltAtChk.isChecked) {
                    thingNrmltpltAtChk.isChecked = false
                    ThingWtnObject.nrmltpltAt = "N"
                } else {
                    thingNrmltpltAtChk.isChecked = true
                    ThingWtnObject.nrmltpltAt = "Y"
                }
            }
        }
        thingwdptResnEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.wdptResn = txtString

            }

            false
        }

        includePaclrMatterEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.paclrMatter = txtString

            }

            false
        }
        thingBuildBgnnArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingBuildIncrprArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.incrprAr = txtString
            }

            false
        }
//        thingBuildRmEdit.setOnEditorActionListener { textView, action, event ->
//
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//                ThingWtnObject.rm = txtString
//            }
//
//            false
//        }

        thingBuldNameEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.buldName = txtString
            }

            false
        }

        ThingBuldDongEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.buldDongName = txtString
            }

            false
        }
        thingBuldhoNameEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.buldHoName = txtString
            }

            false
        }

        thingBgnnArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingIncrprArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.incrprAr = txtString
            }

            false
        }
//        thingRmEdit.setOnEditorActionListener { textView, action, event ->
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//                ThingWtnObject.rm = txtString
//            }
//
//            false
//        }
        thingArComputBasisEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                val txtString = textView.text.toString()
                ThingWtnObject.arComputBasis = txtString
            }
            false
        }


        //지장물 대장조회 dialog
        thingRegstrDialogBtn.setOnClickListener {
            layoutInflater.inflate(R.layout.thing_regstr_dialog, null).let { view ->
                val thingRegstrDialog =
                    ThingRegstrDialogFragment(mContext, mActivity, view).apply {
                        isCancelable = false
                        show(fragmentActivity.supportFragmentManager, "Thing_Regstr_Dialog")
                    }

                view.regstrBuldNmTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldNm"))
                view.regstrBuldPrposTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldPrpos"))
                view.regstrBuldStrctTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldStrct"))
                view.regstrBuldDongNmTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldDong"))
                view.regstrBuldFlratoTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldFlrato"))
                view.regstrBuldHoNmTxt.text = checkStringNull(thingDataJson!!.getString("regstrBuldHo"))
                view.regstrArTxt.text = checkStringNull(thingDataJson!!.getString("regstrAr"))
                view.regstrPrmisnDe.text = checkStringNull(thingDataJson!!.getString("regstrPrmisnDe"))
                view.regstrUseConfmDeTxt.text = checkStringNull(thingDataJson!!.getString("regstrUseConfmDe"))
                view.regstrChgConfmDe.text = checkStringNull(thingDataJson!!.getString("regstrChgConfmDe"))
                view.regstrChangeDtlsTxt.text = checkStringNull(thingDataJson!!.getString("regstrChangeDtls"))

                if(dcsnAt == "Y") {
                    view.regstrBuldNmAtChk.isEnabled = false
                    view.regstrBuldNmCpBtn.isEnabled = false
                    view.regstrBuldPrposAtChk.isEnabled = false
                    view.regstrBuldPrposCpBtn.isEnabled = false
                    view.regstrBuldStrctAtChk.isEnabled = false
                    view.regstrBuldStrctCpBtn.isEnabled = false
                    view.regstrBuldDongAtChk.isEnabled = false
                    view.regstrBuldDongNmCpBtn.isEnabled = false
                    view.regstrBuldFlratoDfnAtChk.isEnabled = false
                    view.regstrBuldFlratoCpBtn.isEnabled = false
                    view.regstrBuldHoAtChk.isEnabled = false
                    view.regstrBuldHoNmCpBtn.isEnabled = false
                    view.regstrArAtChk.isEnabled = false
                    view.regstrBuldArCpBtn.isEnabled = false

                }

//                view.thingRegstrDialogExitBtn.setOnClickListener {
//                    thingRegstrDialog.dismiss()
//                }

                view.regstrBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldNmDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldNmDfnAt = "N"
                    }
                }
                view.regstrBuldPrposAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldPrposDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldPrposDfnAt = "N"
                    }
                }
                view.regstrBuldStrctAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldStrctDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldStrctDfnAt = "N"
                    }
                }
                view.regstrBuldDongAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldDongDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldDongDfnAt = "N"
                    }
                }
                view.regstrBuldFlratoDfnAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldFlratoDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldFlratoDfnAt = "N"
                    }
                }
                view.regstrBuldHoAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldHoDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldHoDfnAt = "N"
                    }
                }
                view.regstrArAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.regstrBuldArDfnAt = "Y"
                        else -> ThingWtnObject.regstrBuldArDfnAt = "N"
                    }
                }
                view.regstrBuldNmCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldNmTxt.text.toString()

                    mActivity.thingBuldNameEdit.setText(txtString)
                }
                view.regstrBuldPrposCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldPrposTxt.text.toString()

                    mActivity.thingbuldprposEdit.setText(txtString)
                }
                view.regstrBuldStrctCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldStrctTxt.text.toString()

                    mActivity.thingbuldStrctEdit.setText(txtString)
                }
                view.regstrBuldDongNmCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldDongNmTxt.text.toString()

                    mActivity.ThingBuldDongEdit.setText(txtString)
                }
                view.regstrBuldFlratoCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldFlratoTxt.text.toString()

                    mActivity.thingBuldhoNameEdit.setText(txtString)
                }
                view.regstrBuldHoNmCpBtn.setOnClickListener{
                    val txtString = view.regstrBuldHoNmTxt.text.toString()

                    mActivity.thingBuldFlratoEdit.setText(txtString)
                }
//                view.regstrBuldArCpBtn.setOnClickListener{
//                    val txtString = view.regstrArTxt.text.toString()
//
//                    mActivity.thingbuldArEdit.setText(txtString)
//                }

                // 지장물 대장조회 확인버튼 이벤트
                view.thingRegstrChkBtn.setOnClickListener {
                    thingRegstrDialog.dismiss()
                }

            }
        }

        //지장물 등기조회 dialog
        thingRgistDialogBtn.setOnClickListener {
            layoutInflater.inflate(R.layout.thing_rgist_dialog, null).let { view ->
                val thingRgistDialog =
                    ThingRgistDialogFragment(mContext, mActivity, view).apply {
                        isCancelable = false
                        show(fragmentActivity.supportFragmentManager, "Thing_Regstr_Dialog")
                    }

                view.rgistBuldNmTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldNm"))
                view.rgistBuldStrctTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldStrct"))
                view.rgistBuldPrposTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldPrpos"))
                view.rgistBuldDongNmTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldDong"))
                view.rgistBuldFlratoTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldFlrato"))
                view.rgistBuldHoNmTxt.text = checkStringNull(thingDataJson!!.getString("rgistBuldHo"))
                view.rgistArTxt.text = checkStringNull(thingDataJson!!.getString("rgistAr"))

                if(dcsnAt == "Y") {
                    view.rgistBuldNmAtChk.isEnabled = false
                    view.rgistBuldNmCpBtn.isEnabled = false
                    view.rgistBuldPrposAtChk.isEnabled = false
                    view.rgistBuldStrctCpBtn.isEnabled = false
                    view.rgistBuldStrctAtChk.isEnabled = false
                    view.rgistBuldPrposCpBtn.isEnabled = false
                    view.rgistBuldDongAtChk.isEnabled = false
                    view.rgistBuldDongNmCpBtn.isEnabled = false
                    view.rgistBuldHoAtChk.isEnabled = false
                    view.rgistBuldFlratoNmCpBtn.isEnabled = false
                    view.rgistBuldFlratoDfnAtChk.isEnabled = false
                    view.rgistBuldHoNmCpBtn.isEnabled = false
                    view.rgistArAtChk.isEnabled = false
                }


                view.rgistBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldNmDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldNmDfnAt = "N"
                    }
                }
                view.rgistBuldStrctAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldStrctDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldStrctDfnAt = "N"
                    }
                }
                view.rgistBuldPrposAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldPrposDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldPrposDfnAt = "N"
                    }
                }
                view.rgistBuldDongAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldDongDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldDongDfnAt = "N"
                    }
                }
                view.rgistBuldFlratoDfnAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldFlratoDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldFlratoDfnAt = "N"
                    }
                }
                view.rgistBuldHoAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldHoDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldHoDfnAt = "N"
                    }
                }
                view.rgistArAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistArDfnAt = "Y"
                        else -> ThingWtnObject.rgistArDfnAt = "N"
                    }
                }
                view.rgistBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> ThingWtnObject.rgistBuldNmDfnAt = "Y"
                        else -> ThingWtnObject.rgistBuldNmDfnAt = "N"
                    }
                }

                view.rgistBuldNmCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldNmTxt.text.toString()

                    mActivity.thingBuldNameEdit.setText(txtString)
                }
                view.rgistBuldStrctCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldStrctTxt.text.toString()

                    mActivity.thingbuldStrctEdit.setText(txtString)
                }
                view.rgistBuldPrposCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldPrposTxt.text.toString()

                    mActivity.thingbuldprposEdit.setText(txtString)
                }
                view.rgistBuldDongNmCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldDongNmTxt.text.toString()

                    mActivity.ThingBuldDongEdit.setText(txtString)
                }
                view.rgistBuldFlratoNmCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldFlratoTxt.text.toString()

                    mActivity.thingBuldhoNameEdit.setText(txtString)
                }
                view.rgistBuldHoNmCpBtn.setOnClickListener{
                    val txtString = view.rgistBuldHoNmTxt.text.toString()

                    mActivity.thingBuldFlratoEdit.setText(txtString)
                }
//                view.rgistArCpBtn.setOnClickListener{
//                    val txtString = view.rgistArTxt.text.toString()
//
//                    mActivity.thingBuldNameEdit.setText(txtString)
//                }

                // 지장물 대장조회 확인버튼 이벤트
                view.thingRgistChkBtn.setOnClickListener {
                    thingRgistDialog.dismiss()
                }

            }
        }

    }

    fun init(view: View) {

        val dataString = requireActivity().intent!!.extras!!.get("ThingInfo") as String
        (context as MapActivity).log.d("ThingInfo String -----------------------> $dataString")

        val dataJson = JSONObject(dataString)
        (context as MapActivity).log.d("ThingInfo dataJson -----------------------> $dataJson")

        thingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        dcsnAt = checkStringNull(thingDataJson!!.getString("dcsnAt"))

        val thingOwnerInfoJson = dataJson.getJSONArray("ownerInfo") as JSONArray
        ThingWtnObject.thingOwnerInfoJson = thingOwnerInfoJson
        ThingWtnObject.thingInfo = thingDataJson

        thingSpinnerAdapter(R.array.thingSmallCategoryArray, view.thingSmallSpinner)
        thingSpinnerAdapter(R.array.thingSmallCategoryArray, view.thingWdpdSmallSpinner)
        thingSpinnerAdapter(R.array.thingExaminMthArray, view.thingExaminMthSpnr)

        // A009
//        thingSpinnerAdapter(R.array.thingUnitArray, view.thingUnitSpinner)
//        thingSpinnerAdapter(R.array.thingUnitArray, view.thingWdpUnitSpinner)
//        thingSpinnerAdapter(R.array.thingUnitArray, view.thingBuildUnitSpinner)
//        thingSpinnerAdapter("A009", view.thingUnitSpinner)
//        thingSpinnerAdapter("A009", view.thingWdpUnitSpinner)
//        thingSpinnerAdapter("A009", view.thingBuildUnitSpinner)
        wtnncUtill.wtnncSpinnerAdapter("A009", thingUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter("A009", thingWdpUnitSpinner, this) // 단위
        wtnncUtill.wtnncSpinnerAdapter("A009", thingBuildUnitSpinner, this) // 단위

        thingSpinnerAdapter(R.array.prmisnCl,view.thingBildngPrmisnClSpinner)
//        thingSpinnerAdapter(R.array.acqsSeArray,view.thingWdpdAcqsClSpinner)
//        thingSpinnerAdapter(R.array.InclsSeArray,view.thingWdpdInclsClSpinner)
        thingSpinnerAdapter(R.array.ownerCnfirmBasisArray,view.thingWdpdOwnerCnfirmBasisClSpinner)
//        thingSpinnerAdapter(R.array.acqsSeArray,view.thingAcqsClSpinner)
//        thingSpinnerAdapter(R.array.InclsSeArray,view.thingInclsClSpinner)
        thingSpinnerAdapter(R.array.ownerCnfirmBasisArray,view.thingOwnerCnfirmBasisClSpinner)
//        thingSpinnerAdapter(R.array.acqsSeArray,view.thingBuldAcqsClSpinner)
//        thingSpinnerAdapter(R.array.InclsSeArray,view.thingBuldInclsClSpinner)
        thingSpinnerAdapter(R.array.ownerCnfirmBasisArray,view.thingBuldOwnerCnfirmBasisClSpinner)

        view.thingWtnStrctLinear.visibleView()
        view.thingWtnStrctAreaLinear.goneView()

        view.thingLegalDongNmText?.text = checkStringNull(thingDataJson!!.getString("legaldongNm").toString())
        view.thingdcsnAtText?.text = dcsnAt
        view.thingBgnnLnmText?.text = checkStringNull(thingDataJson!!.getString("bgnnLnm").toString())
        view.thingincrprLnmText?.text = checkStringNull(thingDataJson!!.getString("incrprLnm").toString())
        view.thingGobuLadcgrNmText?.text =
            checkStringNull(thingDataJson!!.getString("gobuLndcgrNm").toString())

//        if (checkStringNull(thingDataJson!!.getString("relateLnm").toString()).equals("")) {
//            view.thingRelateLnmText?.setText("")
//        } else {
//            view.thingRelateLnmText?.setText(checkStringNull(thingDataJson!!.getString("relateLnm").toString()))
//        }
        view.thingRelateLnmText?.setText(checkStringNull(thingDataJson!!.getString("relateLnm").toString()))

        if(thingDataJson!!.getString("thingKnd").toString().equals("null")) {
            view.thingKndEdit?.setText(checkStringNull(ThingWtnObject.thingKnd.toString()))

        } else {
            view.thingKndEdit?.setText(checkStringNull(thingDataJson!!.getString("thingKnd").toString()))
        }
        val thingIndoorTyString = checkStringNull(thingDataJson!!.getString("indoorTy"))
        (thingIndoorTyString == "1").also { view.thingIndoorTyChk.isChecked = it }


        val thingSmallCl: String
        if (thingDataJson!!.getString("thingSmallCl").toString().equals("null")) {
            thingSmallCl = ThingWtnObject.thingSmallCl.toString()

        } else {
            thingSmallCl = checkStringNull(thingDataJson!!.getString("thingSmallCl").toString())
            ThingWtnObject.thingSmallCl = thingSmallCl
        }
//        val thingSmallCl = checkStringNull(thingDataJson!!.getString("thingSmallCl").toString())

        view.includePaclrMatterEdit.setText(checkStringNull(thingDataJson!!.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(thingDataJson!!.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(thingDataJson!!.getString("rm")))

        when (thingSmallCl) {
            "A023002", "A023003" -> { // 건축물
                val smallClStringSub = thingSmallCl.substring(5, 7)
                view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))

                setView(1)

                val thingNoTextString = checkStringNull(thingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                } else {
                    view.thingNoText?.text =
                        checkStringNull(thingDataJson!!.getString("moNo").toString())
                }

                if(!ThingWtnObject.thingKnd.equals("")) {
                    view.thingKndEdit.setText(ThingWtnObject.thingKnd)
                } else {
                    view.thingKndEdit.setText(checkStringNull(thingDataJson!!.getString("thingKnd")))
                }
                if(!ThingWtnObject.strctNdStndrd.equals("")) {
                    view.thingStrctNdStndrdEdit.setText(ThingWtnObject.strctNdStndrd)
                } else {
                    view.thingStrctNdStndrdEdit?.setText(
                            checkStringNull(
                                thingDataJson!!.getString("strctNdStndrd").toString()
                            )
                        )
                    ThingWtnObject.strctNdStndrd = checkStringNull(thingDataJson!!.getString("strctNdStndrd").toString())
                }
                view.thingBuildBgnnArEdit?.setText(checkStringNull(thingDataJson!!.getString("bgnnAr").toString()))
                ThingWtnObject.bgnnAr = checkStringNull(thingDataJson!!.getString("bgnnAr").toString())
                view.thingBuildIncrprArEdit?.setText(checkStringNull(thingDataJson!!.getString("incrprAr").toString()))
                ThingWtnObject.incrprAr = checkStringNull(thingDataJson!!.getString("incrprAr").toString())

                val buldUnitClString = checkStringNull(thingDataJson!!.getString("unitCl"))
                view.thingBuildUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", buldUnitClString) )

                view.thingBuildArComputBasisEdit.setText(checkStringNull(thingDataJson!!.getString("arComputBasis")))

                val buldPrmisnAtString = checkStringNull(thingDataJson!!.getString("prmisnAt"))
                view.thingRpmsnBasisChk.isChecked = buldPrmisnAtString.equals("Y")

                val buldBildngPrmisnClString = checkStringNull(thingDataJson!!.getString("bildngPrmisnCl"))
                if (buldBildngPrmisnClString.equals("")) {
                    view.thingBildngPrmisnClSpinner.setSelection(0)
                } else {
                    val buldBildngPrmisnClStringSub = buldBildngPrmisnClString.substring(5, 7)
                    view.thingBildngPrmisnClSpinner.setSelection(Integer.valueOf(buldBildngPrmisnClStringSub))
                }

                val buldredeBingAtString = checkStringNull(thingDataJson!!.getString("redeBingAt"))
                view.thingRedeBingAtChk.isChecked = buldredeBingAtString.equals("Y")
                view.thingBuldNameEdit.setText(checkStringNull(thingDataJson!!.getString("buldNm")))
                view.thingbuldprposEdit.setText(checkStringNull(thingDataJson!!.getString("buldPrpos")))
                view.thingbuldStrctEdit.setText(checkStringNull(thingDataJson!!.getString("buldStrct")))
                view.ThingBuldDongEdit.setText(checkStringNull(thingDataJson!!.getString("buldDong")))
                view.thingBuldhoNameEdit.setText(checkStringNull(thingDataJson!!.getString("buldHo")))
                view.thingBuldFlratoEdit.setText(checkStringNull(thingDataJson!!.getString("buldFlrato")))
//                view.thingbuldArEdit.setText(checkStringNull(thingDataJson!!.getString("buld")))
                view.regstrDfnDtlsEdit.setText(checkStringNull(thingDataJson!!.getString("regstrDfnDtls")))
                view.rgistDfnDtlsEdit.setText(checkStringNull(thingDataJson!!.getString("rgistDfnDtls")))
                val nrtBuldAtString = checkStringNull(thingDataJson!!.getString("nrtBuldAt")).toUpperCase()
                view.thingNrtBuldAt.isChecked = nrtBuldAtString.equals("Y")

                val ownerCnfirmBasisClString = checkStringNull(thingDataJson!!.getString("ownerCnfirmBasisCl"))
                if (ownerCnfirmBasisClString.equals("")) {
                    view.thingBuldOwnerCnfirmBasisClSpinner.setSelection(5)
                } else {
                    val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5, 7)
                    view.thingBuldOwnerCnfirmBasisClSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
                }

                val buldSttusMesrString = checkStringNull(thingDataJson!!.getString("sttusMesrAt"))
                view.thingBuldSttusMesrAtChk.isChecked = buldSttusMesrString.equals("Y")

                val buldOwnshipBeforeAtString = checkStringNull(thingDataJson!!.getString("ownshipBeforeAt"))
                if(buldOwnshipBeforeAtString.equals("")) {
                    view.thingBuldOwnshipBeforeAtChk.isChecked = true
                } else {
                    view.thingBuldOwnshipBeforeAtChk.isChecked = buldOwnshipBeforeAtString.equals("Y")
                }


                val buldRwTrgetAtString = checkStringNull(thingDataJson!!.getString("rwTrgetAt"))
                if(buldRwTrgetAtString.equals("")) {
                    view.thingBuldRwTrgetAtChk.isChecked = true
                } else {
                    view.thingBuldRwTrgetAtChk.isChecked = buldRwTrgetAtString.equals("Y")
                }
                view.thingBuldRwTrgetAtChk.isChecked = buldRwTrgetAtString.equals("Y")

                val buldApasmtTrgetAtString = checkStringNull(thingDataJson!!.getString("apasmtTrgetAt"))
                view.thingBuldApasmtTrgetAtChk.isChecked = buldApasmtTrgetAtString.equals("Y")



                if(dcsnAt == "Y") {
                    view.thingIndoorTyChk.isEnabled = false
                    view.thingRelateLnmText.isEnabled = false
                    view.thingSmallSpinner.isEnabled = false
                    view.thingKndEdit.isEnabled = false
                    view.thingStrctNdStndrdEdit.isEnabled = false
                    view.thingBuildBgnnArEdit.isEnabled = false
                    view.thingBuildIncrprArEdit.isEnabled = false
                    view.thingBuildUnitSpinner.isEnabled = false
                    view.thingBuildArComputBasisEdit.isEnabled = false
                    view.thingRpmsnBasisChk.isEnabled = false
                    view.thingBildngPrmisnClSpinner.isEnabled = false
                    view.thingRgistAtChk.isEnabled = false
                    view.thingBuldNameEdit.isEnabled = false
                    view.thingbuldprposEdit.isEnabled = false
                    view.thingbuldStrctEdit.isEnabled = false
                    view.ThingBuldDongEdit.isEnabled = false
                    view.thingBuldhoNameEdit.isEnabled = false
                    view.thingBuldFlratoEdit.isEnabled = false
                    view.thingNrtBuldAt.isEnabled = false
                    view.regstrDfnDtlsEdit.isEnabled = false
                    view.rgistDfnDtlsEdit.isEnabled = false
                    view.thingBuldOwnerCnfirmBasisClSpinner.isEnabled = false
                    view.thingBuldSttusMesrAtChk.isEnabled = false
                    view.thingBuldOwnshipBeforeAtChk.isEnabled = false
                    view.thingBuldRwTrgetAtChk.isEnabled = false
                    view.thingBuldApasmtTrgetAtChk.isEnabled = false
//                    view.includePaclrMatterEdit.isEnabled = false
//                    view.includeReferMatterEdit.isEnabled = false
//                    view.includeRmEdit.isEnabled = false
                    view.thingRedeBingAtChk.isEnabled = false

                }


            }
            "A023001", "A023004", "A023006", "A023007", "A023008", "A023009", "A023010", "A023011" -> { // 일반지장물

                val smallClStringSub = thingSmallCl.substring(5, 7)
                view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))

                setView(2)

                val thingNoTextString = checkStringNull(thingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                } else {
                    view.thingNoText?.text =
                        checkStringNull(thingDataJson!!.getString("moNo").toString())
                }

                if(!ThingWtnObject.thingKnd.equals("")) {
                    view.thingKndEdit.setText(ThingWtnObject.thingKnd)
                } else {
                    view.thingKndEdit.setText(checkStringNull(thingDataJson!!.getString("thingKnd")))
                }
                view.thingStrctNdStndrdEdit?.setText(
                    checkStringNull(
                        thingDataJson!!.getString("strctNdStndrd").toString()
                    )
                )
                ThingWtnObject.strctNdStndrd = checkStringNull(thingDataJson!!.getString("strctNdStndrd").toString())

                view.thingBgnnArEdit.setText(checkStringNull(thingDataJson!!.getString("bgnnAr")))
                view.thingIncrprArEdit.setText(checkStringNull(thingDataJson!!.getString("incrprAr")))

                val thingUnitClString = checkStringNull(thingDataJson!!.getString("unitCl"))
                view.thingUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", thingUnitClString) )


                view.thingArComputBasisEdit.setText(checkStringNull(thingDataJson!!.getString("arComputBasis")))

                val ownerCnfirmBasisClString = checkStringNull(thingDataJson!!.getString("ownerCnfirmBasisCl"))
                if (ownerCnfirmBasisClString.equals("")) {
                    view.thingOwnerCnfirmBasisClSpinner.setSelection(5)
                } else {
                    val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5, 7)
                    view.thingOwnerCnfirmBasisClSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
                }

                val sttusMestAtString = checkStringNull(thingDataJson!!.getString("sttusMesrAt"))
                view.thingSttusMesrAtChk.isChecked = sttusMestAtString.equals("Y")

                val ownshipBeforeAtString = checkStringNull(thingDataJson!!.getString("ownshipBeforeAt"))
                if(ownshipBeforeAtString.equals("")) {
                    view.thingOwnshipBeforeAtChk.isChecked = true
                } else {
                    view.thingOwnshipBeforeAtChk.isChecked = ownshipBeforeAtString.equals("Y")
                }

                val rwTrgetAtString = checkStringNull(thingDataJson!!.getString("rwTrgetAt"))
                if(rwTrgetAtString.equals("")) {
                    view.thingRwTrgetAtChk.isChecked = true
                } else {
                    view.thingRwTrgetAtChk.isChecked = rwTrgetAtString.equals("Y")
                }

                val apasmtTrgetAtString = checkStringNull(thingDataJson!!.getString("apasmtTrgetAt"))
                view.thingApasmtTrgetAtChk.isChecked = apasmtTrgetAtString.equals("Y")

                if(dcsnAt == "Y") {
                    view.thingIndoorTyChk.isEnabled = false
                    view.thingRelateLnmText.isEnabled = false
                    view.thingSmallSpinner.isEnabled = false
                    view.thingKndEdit.isEnabled = false
                    view.thingStrctNdStndrdEdit.isEnabled = false
                    view.thingBgnnArEdit.isEnabled = false
                    view.thingIncrprArEdit.isEnabled = false
                    view.thingUnitSpinner.isEnabled = false
                    view.thingArComputBasisEdit.isEnabled = false
                    view.thingOwnerCnfirmBasisClSpinner.isEnabled = false
                    view.thingSttusMesrAtChk.isEnabled = false
                    view.thingOwnshipBeforeAtChk.isEnabled = false
                    view.thingRwTrgetAtChk.isEnabled = false
                    view.thingApasmtTrgetAtChk.isEnabled = false
                    view.includePaclrMatterEdit.isEnabled = false
                    view.includeReferMatterEdit.isEnabled = false
                    view.includeRmEdit.isEnabled = false
                }

            }
            "A023005" -> { // 수목

                setView(3)

                val thingNoWdpdString = checkStringNull(thingDataJson!!.getString("thingWtnCode"))
                if (thingNoWdpdString.equals("")) {
                    view.thingWdpdNoText.text = "자동기입"
                } else {
//                    view.thingWdpdNoText.text = thingNoWdpdString
                    view.thingWdpdNoText?.setText(checkStringNull(thingDataJson!!.getString("moNo").toString()))
                }

                if (thingSmallCl.equals("")) {
                    view.thingSmallSpinner.setSelection(0)
                    view.thingWdpdSmallSpinner.setSelection(0)
                } else {
                    val smallClStringSub = thingSmallCl.substring(5, 7)
                    view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                    view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                }


                if(!ThingWtnObject.thingKnd.equals("")) {
                    view.thingWdpdKndEdit.setText(ThingWtnObject.thingKnd)
                } else {
                    view.thingWdpdKndEdit.setText(checkStringNull(thingDataJson!!.getString("thingKnd")))
                }


                view.thingWdptBgnnArEdit.setText(checkStringNull(thingDataJson!!.getString("bgnnAr")))
                view.thingWdptincrprArEdit.setText(checkStringNull(thingDataJson!!.getString("incrprAr")))

                val thingWdpdUnitClString = checkStringNull(thingDataJson!!.getString("unitCl"))
                view.thingWdpUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", thingWdpdUnitClString) )


                val thingWdpdExaminMthdString = checkStringNull(thingDataJson!!.getString("examinMthd"))
                if (thingWdpdExaminMthdString.equals("")) {
                    view.thingExaminMthSpnr.setSelection(1)
                } else {
                    view.thingExaminMthSpnr.setSelection(
                        when (thingWdpdExaminMthdString) {
                            "개별" -> 1
                            "면적" -> 2
                            else -> 3
                        }
                    )
                }

                if (ThingWtnObject.thingNewSearch.equals("Y")) {
                    view.thingWtnStrctAreaLinear.visibleView()
                    view.thingWtnWdpdStrctLinear.visibility = View.GONE
                } else {
                    view.thingWtnStrctAreaLinear.visibility = View.GONE
                    view.thingWtnWdpdStrctLinear.visibleView()

                    view.thingWdpdStrctNdStndrdEdit.setText(checkStringNull(thingDataJson!!.getString("strctNdStndrd")))

                }

                view.thingwdpArComputBasisEdit.setText(checkStringNull(thingDataJson!!.getString("arComputBasis")))
                val thingWdpdNrmltpltAtString = checkStringNull(thingDataJson!!.getString("nrmltpltAt"))
                view.thingNrmltpltAtChk.isChecked = thingWdpdNrmltpltAtString.equals("Y")

                view.thingwdptResnEdit.setText(checkStringNull(thingDataJson!!.getString("wdptResn")))


                val ownerCnfirmBasisClString = checkStringNull(thingDataJson!!.getString("ownerCnfirmBasisCl"))
                if (ownerCnfirmBasisClString.equals("")) {
                    view.thingWdpdOwnerCnfirmBasisClSpinner.setSelection(5)
                } else {
                    val ownerCnfirmBasisClStringsub = ownerCnfirmBasisClString.substring(5, 7)
                    view.thingWdpdOwnerCnfirmBasisClSpinner.setSelection(Integer.valueOf(ownerCnfirmBasisClStringsub))
                }

                val buldSttusMesrString = checkStringNull(thingDataJson!!.getString("sttusMesrAt"))
                view.thingWdpdSttusMesrAtChk.isChecked = buldSttusMesrString.equals("Y")

                val buldOwnshipBeforeAtString = checkStringNull(thingDataJson!!.getString("ownshipBeforeAt"))
                if(buldOwnshipBeforeAtString.equals("")) {
                    view.thingWdpdOwnshipBeforeAtChk.isChecked = false
                } else {
                    view.thingWdpdOwnshipBeforeAtChk.isChecked = buldOwnshipBeforeAtString.equals("Y")
                }

                val buldRwTrgetAtString = checkStringNull(thingDataJson!!.getString("rwTrgetAt"))
                if(buldRwTrgetAtString.equals("")) {
                    view.thingWdpdRwTrgetAtChk.isChecked = true
                } else {
                    view.thingWdpdRwTrgetAtChk.isChecked = buldRwTrgetAtString.equals("Y")
                }

                val buldApasmtTrgetAtString = checkStringNull(thingDataJson!!.getString("apasmtTrgetAt"))
                view.thingWdpdApasmtTrgetAtChk.isChecked = buldApasmtTrgetAtString.equals("Y")

                if(dcsnAt == "Y") {
                    view.thingIndoorTyChk.isEnabled = false
                    view.thingRelateLnmText.isEnabled = false
                    view.thingWdpdSmallSpinner.isEnabled = false
                    view.thingWdpdKndEdit.isEnabled = false
                    view.thingWdptBgnnArEdit.isEnabled = false
                    view.thingWdptincrprArEdit.isEnabled = false
                    view.thingWdpUnitSpinner.isEnabled = false
                    view.thingExaminMthSpnr.isEnabled = false
                    view.thingStrctNdStndrdEditR.isEnabled = false
                    view.thingStrctNdStndrdEditB.isEnabled = false
                    view.thingStrctNdStndrdEditH.isEnabled = false
                    view.thingStrctNdStndrdEditL.isEnabled = false
                    view.thingStrctNdStndrdEditW.isEnabled = false
                    view.thingWdpdStrctNdStndrdEdit.isEnabled = false
                    view.thingwdpArComputBasisEdit.isEnabled = false
                    view.thingNrmltpltAtChk.isEnabled = false
                    view.thingwdptResnEdit.isEnabled = false
                    view.thingWdpdOwnerCnfirmBasisClSpinner.isEnabled = false
                    view.thingWdpdSttusMesrAtChk.isEnabled = false
                    view.thingWdpdOwnshipBeforeAtChk.isEnabled = false
                    view.thingWdpdRwTrgetAtChk.isEnabled = false
                    view.thingWdpdApasmtTrgetAtChk.isEnabled = false
                    view.includePaclrMatterEdit.isEnabled = false
                    view.includeReferMatterEdit.isEnabled = false
                    view.includeRmEdit.isEnabled = false
                }
            }

            else -> {

                view.thingSmallSpinner.setSelection(0)
                view.thingWdpdSmallSpinner.setSelection(0)

                view.thingSmallSpinner.setSelection(0)
                view.thingWdpdSmallSpinner.setSelection(0)

                setView(2)

                val thingNoTextString = checkStringNull(thingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                    view.thingWdpdNoText?.setText("자동기입")
                } else {
                    view.thingNoText?.setText(checkStringNull(thingDataJson!!.getString("moNo").toString()))
                }

            }
        }


        if(ThingWtnObject.thingNewSearch == "N") {
            settingSearchCamerasView(dataJson.getJSONArray("thingAtchInfo"))
        } else {
            settingSearchCamerasView(null)
        }

        if(dcsnAt == "Y") {
            toast.msg_info(R.string.searchDcsnAtThing, 1000)
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

        if(ThingWtnObject.thingNewSearch.equals("N") ) {
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
                                            "THING",
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


    fun thingSpinnerAdapter(stringArray: Int, spinner: Spinner?) {

        spinner?.adapter = CustomDropDownAdapter(context!!, listOf(resources.getStringArray(stringArray))[0])
        spinner?.onItemSelectedListener = this

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

    fun thingSpinnerAdapter(codeGroupId: String, spinner: Spinner?) {

        spinner?.adapter = CustomDropDownAdapter(context!!, CommonCodeInfoList.getCodeDcArray(codeGroupId))
        spinner?.onItemSelectedListener = this

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val thingAddView: LinearLayout = activity.findViewById(R.id.thingLinearAddView)

        when (parent?.id) {
            R.id.thingSmallSpinner -> {
                when (position) {
                    1 -> { //동산이전
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023001"
                    }
                    2 -> { // 일반건축물
                        setView(1)

                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023002"
                    }
                    3 -> { // 집합건축물
                        setView(1)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023003"
                    }
                    4 -> { // 공작물
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023004"
                    }
                    5 -> { // 수목
                        setView(3)
                        thingAddView.removeAllViews()
                        thingWdpdSmallSpinner.setSelection(5)
                        ThingWtnObject.thingSmallCl = "A023005"
                    }
                    6 -> { // 개간비
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023006"
                    }
                    7 -> { //잔여지가격손실
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023007"
                    }
                    8 -> { //소유권이외의권리
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023008"
                    }
                    else -> {
                        setView(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = ""
                    }
                }
            }
            R.id.thingWdpdSmallSpinner -> {
                when (position) {
                    1 -> { //동산이전
                        setView(2)
                        thingAddView.removeAllViews()
                        thingSmallSpinner.setSelection(1)
                        ThingWtnObject.thingSmallCl = "A023001"
                    }
                    2 -> { // 일반건축물
                        setView(1)
                        thingSmallSpinner.setSelection(2)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023002"
                    }
                    3 -> { // 집합건축물
                        setView(1)
                        thingSmallSpinner.setSelection(3)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023003"
                    }
                    4 -> { // 공작물
                        setView(2)
                        thingSmallSpinner.setSelection(4)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023004"
                    }
                    5 -> { // 수목
                        setView(3)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023005"
                    }
                    6 -> { // 개간비
                        setView(2)
                        thingSmallSpinner.setSelection(6)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023006"
                    }
                    7 -> { //잔여지가격손실
                        setView(2)
                        thingSmallSpinner.setSelection(7)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023007"
                    }
                    8 -> { //소유권이외의권리
                        setView(2)
                        thingSmallSpinner.setSelection(8)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = "A023008"
                    }
                    else -> {
                        setView(2)
                        thingSmallSpinner.setSelection(9)
                        thingAddView.removeAllViews()
                        ThingWtnObject.thingSmallCl = ""
                    }
                }
            }
            R.id.thingExaminMthSpnr -> {
                when (position) {
                    0 -> { // 선택
                        logUtil.d("thingExaminMthSpnr select 0")
                        thingWtnStrctLinear.visibleView()
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        ThingWtnObject.examinMthd = ""
                    }
                    1 -> { // 개별
                        logUtil.d("thingExaminMthSpnr select 1")
                        if(ThingWtnObject.thingNewSearch.equals("Y")) {
                            thingWtnWdpdStrctLinear.visibility = View.GONE
                            thingWtnStrctAreaLinear.visibleView()
                        } else {
                            thingWtnWdpdStrctLinear.visibleView()
                            thingWtnStrctAreaLinear.visibility = View.GONE
                        }

                        ThingWtnObject.examinMthd = "개별"
                    }
                    2 -> { // 면적
                        logUtil.d("thingExaminMthSpnr select 2")
                        if(ThingWtnObject.thingNewSearch.equals("Y")) {
                            thingWtnWdpdStrctLinear.visibility = View.GONE
                            thingWtnStrctAreaLinear.visibleView()
                        } else {
                            thingWtnWdpdStrctLinear.visibleView()
                            thingWtnStrctAreaLinear.visibility = View.GONE
                        }
                        ThingWtnObject.examinMthd = "면적"
                    }
                    3 -> { // 기타
                        logUtil.d("thingExaminMthSpnr select 3")
                        thingWtnWdpdStrctLinear.visibleView()
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        ThingWtnObject.examinMthd = "기타"
                    }
                    else -> {
                        thingWtnWdpdStrctLinear.visibleView()
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        ThingWtnObject.examinMthd = ""
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    fun addThingData() {

        ThingWtnObject.thingIndoorTy = when (mActivity.thingIndoorTyChk.isChecked) {
            true -> "1"
            else -> "2"
        }

        ThingWtnObject.thingLrgeCl = "A011001" //대분류 지장물
        ThingWtnObject.thingSmallCl = when (activity.thingSmallSpinner.selectedItemPosition) {
            1 -> "A023001"
            2 -> "A023002"
            3 -> "A023003"
            4 -> "A023004"
            5 -> "A023005"
            6 -> "A023006"
            7 -> "A023007"
            8 -> "A023008"
            9 -> "A023009"
            else -> ""
            //else -> throw NullPointerException("activity.thingSmallSpinner.selectedItemPosition is Null")
        }

        // 관련지번
        val thingRelateLnmString = activity.thingRelateLnmText.text.toString()
//        if(!thingRelateLnmString.isNullOrEmpty()) {
            ThingWtnObject.relateLnm = thingRelateLnmString
//        }

        if (ThingWtnObject.thingSmallCl.equals("A023002") || ThingWtnObject.thingSmallCl.equals("A023003")) { //건축물
            ThingWtnObject.thingKnd = mActivity.thingKndEdit.text.toString()
            ThingWtnObject.strctNdStndrd = mActivity.thingStrctNdStndrdEdit.text.toString()
            ThingWtnObject.bgnnAr = mActivity.thingBuildBgnnArEdit.text.toString()
            ThingWtnObject.incrprAr = mActivity.thingBuildIncrprArEdit.text.toString()

            ThingWtnObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.thingBuildUnitSpinner.selectedItemPosition) 

            ThingWtnObject.arComputBasis = mActivity.thingBuildArComputBasisEdit.text.toString()

            ThingWtnObject.prmisnAt = when (mActivity.thingRpmsnBasisChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.thingNrtBuldAt = when (mActivity.thingNrtBuldAt.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.bildngPrmisnCl = when (mActivity.thingBildngPrmisnClSpinner.selectedItemPosition) {
                1 -> "A029001"
                2 -> "A029002"
                3 -> "A029003"
                4 -> "A029004"
                else -> ""
            }
            ThingWtnObject.rgistAt = when (mActivity.thingRgistAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.redeBingAt = when (mActivity.thingRedeBingAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

            ThingWtnObject.buldName = mActivity.thingBuldNameEdit.text.toString()
            ThingWtnObject.buldPrpos = mActivity.thingbuldprposEdit.text.toString()
            ThingWtnObject.buldStrct = mActivity.thingbuldStrctEdit.text.toString()
            ThingWtnObject.buldDongName = mActivity.ThingBuldDongEdit.text.toString()
            ThingWtnObject.buldHoName = mActivity.thingBuldhoNameEdit.text.toString()
            ThingWtnObject.buldFlrato = mActivity.thingBuldFlratoEdit.text.toString()
//            ThingWtnObject.buldAr = mActivity.thingbuldArEdit.text.toString()
            ThingWtnObject.regstrDfnDtls = mActivity.regstrDfnDtlsEdit.text.toString()
            ThingWtnObject.rgistDfnDtls = mActivity.rgistDfnDtlsEdit.text.toString()

//            ThingWtnObject.acqsCl = when (mActivity.thingBuldAcqsClSpinner.selectedItemPosition) {
//                1 -> "A025001"
//                2 -> "A025002"
//                3 -> "A025003"
//                4 -> "A025004"
//                5 -> "A025005"
//                6 -> "A025006"
//                7 -> "A025007"
//                8 -> "A025008"
//                9 -> "A025009"
//                10 -> "A025010"
//                11 -> "A025011"
//                12 -> "A025012"
//                else -> ""
//            }
//            ThingWtnObject.inclsCl = when (mActivity.thingBuldInclsClSpinner.selectedItemPosition) {
//                1 -> "A007001"
//                2 -> "A007002"
//                3 -> "A007003"
//                4 -> "A007004"
//                else -> ""
//            }
            ThingWtnObject.acqsCl = "A025001" // 취득
            ThingWtnObject.inclsCl = "A007001" // 편입

            ThingWtnObject.ownerCnfirmBasisCl = when (mActivity.thingBuldOwnerCnfirmBasisClSpinner.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }

            ThingWtnObject.sttusMesrAt = when (mActivity.thingBuldSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.ownshipBeforeAt = when (mActivity.thingBuldOwnshipBeforeAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.rwTrgetAt = when (mActivity.thingBuldRwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.apasmtTrgetAt = when (mActivity.thingBuldApasmtTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }

        } else if (ThingWtnObject.thingSmallCl.equals("A023005")) { // 수목
            ThingWtnObject.thingKnd = mActivity.thingWdpdKndEdit.text.toString()

            ThingWtnObject.bgnnAr = mActivity.thingWdptBgnnArEdit.text.toString()
            ThingWtnObject.incrprAr = mActivity.thingWdptincrprArEdit.text.toString()

            ThingWtnObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.thingWdpUnitSpinner.selectedItemPosition)

            ThingWtnObject.examinMthd = when (mActivity.thingExaminMthSpnr.selectedItemPosition) {
                1 -> "개별"
                2 -> "면적"
                3 -> "기타"
                else -> ""
            }

            ThingWtnObject.arComputBasis = mActivity.thingwdpArComputBasisEdit.text.toString()

            ThingWtnObject.nrmltpltAt = when (mActivity.thingNrmltpltAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.wdptResn = mActivity.thingwdptResnEdit.text.toString()

            if (mActivity.thingWtnStrctAreaLinear.visibility == View.VISIBLE) {

                val strctNdStrndrdRString = mActivity.thingStrctNdStndrdEditR.text.toString()
                val strctNdStrndrdBString = mActivity.thingStrctNdStndrdEditB.text.toString()
                val strctNdStrndrdHString = mActivity.thingStrctNdStndrdEditH.text.toString()
                val strctNdStrndrdLString = mActivity.thingStrctNdStndrdEditL.text.toString()
                val strctNdStrndrdWString = mActivity.thingStrctNdStndrdEditW.text.toString()

                ThingWtnObject.strctNdStndrd = StringBuilder().apply {
                    when(mActivity.thingNrmltpltAtChk.isChecked) {
                        true -> append("정상식, ")
                        else -> {
                            append("비정상식(")
                            append(mActivity.thingwdptResnEdit.text.toString())
                            append("), ")
                        }

                    }

                    when (mActivity.thingExaminMthSpnr.selectedItemPosition) {
                        1 -> append("개별조사방식(")
                        2 -> append("면적조사방식(")
                        else -> append("기타조사방식(")
                    }

                    if (strctNdStrndrdRString != "") {
                        if (strctNdStrndrdBString != ""
                            || strctNdStrndrdHString != ""
                            || strctNdStrndrdLString != ""
                            || strctNdStrndrdWString != ""
                        ) {
                            append("R$strctNdStrndrdRString×")
                        } else {
                            append("R$strctNdStrndrdRString")
                        }
                    }
                    if (strctNdStrndrdBString != "") {
                        if (strctNdStrndrdHString != ""
                            || strctNdStrndrdLString != ""
                            || strctNdStrndrdWString != ""
                        ) {
                            append("B$strctNdStrndrdBString×")
                        } else {
                            append("B$strctNdStrndrdBString")
                        }
                    }
                    if (strctNdStrndrdHString != "") {
                        if (strctNdStrndrdLString != "" || strctNdStrndrdWString != "") {
                            append("H$strctNdStrndrdHString×")
                        } else {
                            append("H$strctNdStrndrdHString")
                        }
                    }
                    if (!strctNdStrndrdLString.equals("")) {
                        if (!strctNdStrndrdWString.equals("")) {
                            append("L$strctNdStrndrdLString×")
                        } else {
                            append("L$strctNdStrndrdLString")
                        }
                    }
                    if (!strctNdStrndrdWString.equals("")) {
                        append("W$strctNdStrndrdWString")
                    }

                    append(",")
                    append(mActivity.thingwdpArComputBasisEdit.text.toString())
                    append(")")
                }.toString()

            } else {
                ThingWtnObject.strctNdStndrd = mActivity.thingWdpdStrctNdStndrdEdit.text.toString()
            }

//            ThingWtnObject.acqsCl = when (mActivity.thingWdpdAcqsClSpinner.selectedItemPosition) {
//                1 -> "A025001"
//                2 -> "A025002"
//                3 -> "A025003"
//                4 -> "A025004"
//                5 -> "A025005"
//                6 -> "A025006"
//                7 -> "A025007"
//                8 -> "A025008"
//                9 -> "A025009"
//                10 -> "A025010"
//                11 -> "A025011"
//                12 -> "A025012"
//                else -> ""
//            }
//            ThingWtnObject.inclsCl = when (mActivity.thingWdpdInclsClSpinner.selectedItemPosition) {
//                1 -> "A007001"
//                2 -> "A007002"
//                3 -> "A007003"
//                4 -> "A007004"
//                else -> ""
//            }
            ThingWtnObject.acqsCl = "A025001" // 취득
            ThingWtnObject.inclsCl = "A007001" // 편입
            ThingWtnObject.ownerCnfirmBasisCl = when (mActivity.thingWdpdOwnerCnfirmBasisClSpinner.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }

            ThingWtnObject.sttusMesrAt = when (mActivity.thingWdpdSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.ownshipBeforeAt = when (mActivity.thingWdpdOwnshipBeforeAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.rwTrgetAt = when (mActivity.thingWdpdRwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.apasmtTrgetAt = when (mActivity.thingWdpdApasmtTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }


        } else { // 일반

            ThingWtnObject.thingKnd = mActivity.thingKndEdit.text.toString()
            ThingWtnObject.strctNdStndrd = mActivity.thingStrctNdStndrdEdit.text.toString()

            ThingWtnObject.bgnnAr = mActivity.thingBgnnArEdit.text.toString()
            ThingWtnObject.incrprAr = mActivity.thingIncrprArEdit.text.toString()

            ThingWtnObject.unitCl = CommonCodeInfoList.getCodeId("A009", mActivity.thingUnitSpinner.selectedItemPosition)

            ThingWtnObject.arComputBasis = mActivity.thingArComputBasisEdit.text.toString()

//            ThingWtnObject.acqsCl = when (mActivity.thingAcqsClSpinner.selectedItemPosition) {
//                1 -> "A025001"
//                2 -> "A025002"
//                3 -> "A025003"
//                4 -> "A025004"
//                5 -> "A025005"
//                6 -> "A025006"
//                7 -> "A025007"
//                8 -> "A025008"
//                9 -> "A025009"
//                10 -> "A025010"
//                11 -> "A025011"
//                12 -> "A025012"
//                else -> ""
//            }
//            ThingWtnObject.inclsCl = when (mActivity.thingInclsClSpinner.selectedItemPosition) {
//                1 -> "A007001"
//                2 -> "A007002"
//                3 -> "A007003"
//                4 -> "A007004"
//                else -> ""
//            }
            ThingWtnObject.ownerCnfirmBasisCl = when (mActivity.thingOwnerCnfirmBasisClSpinner.selectedItemPosition) {
                1 -> "A035001"
                2 -> "A035002"
                3 -> "A035003"
                4 -> "A035004"
                5 -> "A035005"
                else -> ""
            }
            ThingWtnObject.acqsCl = "A025001" // 취득
            ThingWtnObject.inclsCl = "A007001" // 편입
            ThingWtnObject.sttusMesrAt = when (mActivity.thingSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.ownshipBeforeAt = when (mActivity.thingOwnshipBeforeAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.rwTrgetAt = when (mActivity.thingRwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            ThingWtnObject.apasmtTrgetAt = when (mActivity.thingApasmtTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
        }
        ThingWtnObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString()
        ThingWtnObject.referMatter = mActivity.includeReferMatterEdit.text.toString()
        ThingWtnObject.rm = mActivity.includeRmEdit.text.toString()

    }

//    fun checkStringNull(nullString: String): String {
//        return if (nullString == "null") {
//            ""
//        } else {
//            nullString
//        }
//    }

    fun setView(viewCode: Int) {
        when (viewCode) {
            1 -> { //건축물
                thingSearchBaseViewLl.visibleView()
                thingSearchBaseWdpdViewLl.visibility = View.GONE
                thingSearchBildAddview.visibleView()
                thingSearchGnrlBaseView.visibility = View.GONE
                wtnncCommIndoorTy.visibleView()
                wtnncCommIndoorTyLl.visibleView()
                thingBuldOwnerCnfirmBasisClSpinner.setSelection(5)
                thingBuldOwnshipBeforeAtChk.isChecked = true
                thingBuldRwTrgetAtChk.isChecked = true
                thingBuldApasmtTrgetAtChk.isChecked = true

            }
            2 -> { //일반
                thingSearchBaseViewLl.visibleView()
                thingSearchBaseWdpdViewLl.visibility = View.GONE
                thingSearchBildAddview.visibility = View.GONE
                thingSearchGnrlBaseView.visibleView()
                wtnncCommIndoorTy.visibleView()
                wtnncCommIndoorTyLl.visibleView()
                thingOwnerCnfirmBasisClSpinner.setSelection(5)
                thingRwTrgetAtChk.isChecked = true
                thingApasmtTrgetAtChk.isChecked = true
            }
            3 -> { //수목
                thingSearchBaseViewLl.visibility = View.GONE
                thingSearchBaseWdpdViewLl.visibleView()
                thingSearchBildAddview.visibility = View.GONE
                thingSearchGnrlBaseView.visibility = View.GONE
                wtnncCommIndoorTy.visibility = View.GONE
                wtnncCommIndoorTyLl.visibility = View.GONE
//                thingWtnWdpdStrctLinear.visibleView()
                thingExaminMthSpnr.setSelection(1)
                thingWdpdOwnerCnfirmBasisClSpinner.setSelection(5)
                thingWdpdApasmtTrgetAtChk.isChecked = true
                thingWdpdRwTrgetAtChk.isChecked = true
            }
        }

    }

}