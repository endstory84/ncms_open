/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_restland_search_item.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.view.*
import kotlinx.android.synthetic.main.rest_thing_search_gnrl.*
import kotlinx.android.synthetic.main.thing_regstr_dialog.view.*
import kotlinx.android.synthetic.main.thing_rgist_dialog.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.ThingBuldDongEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.regstrDfnDtlsEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.rgistDfnDtlsEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.searchShetchBtn
import kotlinx.android.synthetic.main.thing_search_gnrl.thingArComputBasisEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBgnnArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBildngPrmisnClSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuildArComputBasisEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuildBgnnArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuildIncrprArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuildUnitSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuldFlratoEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuldNameEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingBuldhoNameEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingExaminMthSpnr
import kotlinx.android.synthetic.main.thing_search_gnrl.thingIncrprArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingIndoorTyChk
import kotlinx.android.synthetic.main.thing_search_gnrl.thingKndEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingNrmltpltAtChk
import kotlinx.android.synthetic.main.thing_search_gnrl.thingNrmltpltAtLl
import kotlinx.android.synthetic.main.thing_search_gnrl.thingNrtBuldAt
import kotlinx.android.synthetic.main.thing_search_gnrl.thingRedeBingAtChk
import kotlinx.android.synthetic.main.thing_search_gnrl.thingRegstrDialogBtn
import kotlinx.android.synthetic.main.thing_search_gnrl.thingRgistAtChk
import kotlinx.android.synthetic.main.thing_search_gnrl.thingRgistDialogBtn
import kotlinx.android.synthetic.main.thing_search_gnrl.thingRpmsnBasisChk
import kotlinx.android.synthetic.main.thing_search_gnrl.thingSearchBaseViewLl
import kotlinx.android.synthetic.main.thing_search_gnrl.thingSearchBaseWdpdViewLl
import kotlinx.android.synthetic.main.thing_search_gnrl.thingSearchBildAddview
import kotlinx.android.synthetic.main.thing_search_gnrl.thingSearchGnrlBaseView
import kotlinx.android.synthetic.main.thing_search_gnrl.thingSmallSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEditB
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEditH
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEditL
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEditR
import kotlinx.android.synthetic.main.thing_search_gnrl.thingStrctNdStndrdEditW
import kotlinx.android.synthetic.main.thing_search_gnrl.thingUnitSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdpUnitSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdpdKndEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdpdSmallSpinner
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdpdStrctNdStndrdEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdptBgnnArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWdptincrprArEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWtnStrctAreaLinear
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWtnStrctLinear
import kotlinx.android.synthetic.main.thing_search_gnrl.thingWtnWdpdStrctLinear
import kotlinx.android.synthetic.main.thing_search_gnrl.thingbuldStrctEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingbuldprposEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingwdpArComputBasisEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.thingwdptResnEdit
import kotlinx.android.synthetic.main.thing_search_gnrl.view.*
import kotlinx.android.synthetic.main.thing_search_gnrl.wtnncCommIndoorTy
import kotlinx.android.synthetic.main.thing_search_gnrl.wtnncCommIndoorTyLl
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.CustomDropDownAdapter
import kr.or.kreb.ncms.mobile.adapter.RestThingSearchRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.data.RestThingWtnObject
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import kr.or.kreb.ncms.mobile.util.*
import org.json.JSONArray
import org.json.JSONObject

class RestThingSearchFragment(val activity: Activity, context: Context, val fragmentActivity: FragmentActivity) :
    Fragment(),
    AdapterView.OnItemSelectedListener {

    private lateinit var restThingSearchAdapter: RestThingSearchRecyclerViewAdapter
    private val mActivity: Activity = activity
    private val mContext: Context = context
//    private lateinit var thingTypeView: View

    private var toastUtil: ToastUtil = ToastUtil(context)
    private var logUtil: LogUtil = LogUtil("RestThingSearchFragment")

    private val wtnncUtill = WtnncUtil(activity, context)

    var restThingDataJson: JSONObject? = null

    var wtnncImageAdapter: WtnncImageAdapter? = null

    init {
//        Constants.CAMERA_IMAGE_ARR.clear()
//        Constants.CAMERA_IMGAE_INDEX = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rest_thing_search_gnrl, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init(view)

        setRestSearchLayout()

        //토지조서 사진촬영버튼
        includeCameraBtn.setOnClickListener {
            nextView(
                requireActivity(),
                Constants.CAMERA_ACT,
                null,
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
                var txtString = textView.text.toString()

                RestThingWtnObject.restThingKnd = txtString
            }

            false
        }
        thingStrctNdStndrdEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                RestThingWtnObject.strctNdStndrd = txtString
            }

            false
        }
        thingStrctNdStndrdEditR.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                RestThingWtnObject.strctNdStrndrdR = txtString
                RestThingWtnObject.strctNdStndrd = txtString
            }

            false
        }

        thingStrctNdStndrdEditH.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                RestThingWtnObject.strctNdStrndrdH = txtString
            }

            false
        }
        thingWdptBgnnArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                RestThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingWdptincrprArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()

                RestThingWtnObject.incrprAr = txtString
            }

            false
        }

        thingNrmltpltAtChk.setOnClickListener {
            if (thingNrmltpltAtChk.isChecked) {
                RestThingWtnObject.nrmltpltAt = "Y"
            } else {
                RestThingWtnObject.nrmltpltAt = "N"
            }
        }
        thingNrmltpltAtLl.setOnClickListener {
            if (thingNrmltpltAtChk.isChecked) {
                thingNrmltpltAtChk.isChecked = false
                RestThingWtnObject.nrmltpltAt = "N"
            } else {
                thingNrmltpltAtChk.isChecked = true
                RestThingWtnObject.nrmltpltAt = "Y"
            }
        }
        thingwdptResnEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.wdptResn = txtString

            }

            false
        }

        includePaclrMatterEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.paclrMatter = txtString

            }

            false
        }
        thingBuildBgnnArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingBuildIncrprArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.incrprAr = txtString
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
                var txtString = textView.text.toString()
                RestThingWtnObject.buldName = txtString
            }

            false
        }

        ThingBuldDongEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.buldDongName = txtString
            }

            false
        }
        thingBuldhoNameEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.buldHoName = txtString
            }

            false
        }

        thingBgnnArEdit.setOnEditorActionListener { textView, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.bgnnAr = txtString
            }

            false
        }
        thingIncrprArEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.incrprAr = txtString
            }

            false
        }
//        thingRmEdit.setOnEditorActionListener { textView, action, event ->
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                var txtString = textView.getText().toString()
//                RestThingWtnObject.rm = txtString
//            }
//
//            false
//        }
        thingArComputBasisEdit.setOnEditorActionListener { textView, action, event ->

            if (action == EditorInfo.IME_ACTION_DONE) {
                var txtString = textView.text.toString()
                RestThingWtnObject.arComputBasis = txtString
            }
            false
        }


        //지장물 대장조회 dialog
        thingRegstrDialogBtn.setOnClickListener {
            layoutInflater.inflate(R.layout.rest_thing_regstr_dialog, null).let { view ->
                val thingRegstrDialog =
                    ThingRegstrDialogFragment(mContext, mActivity, view).apply {
                        isCancelable = false
                        show(fragmentActivity.supportFragmentManager, "Thing_Regstr_Dialog")
                    }

                view.regstrBuldNmTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldNm"))
                view.regstrBuldPrposTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldPrpos"))
                view.regstrBuldStrctTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldStrct"))
                view.regstrBuldDongNmTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldDong"))
                view.regstrBuldFlratoTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldFlrato"))
                view.regstrBuldHoNmTxt.text = checkStringNull(restThingDataJson!!.getString("regstrBuldHo"))
                view.regstrArTxt.text = checkStringNull(restThingDataJson!!.getString("regstrAr"))
                view.regstrUseConfmDeTxt.text = checkStringNull(restThingDataJson!!.getString("regstrUseConfmDe"))
                view.regstrChangeDtlsTxt.text = checkStringNull(restThingDataJson!!.getString("regstrChangeDtls"))

                view.thingRegstrDialogExitBtn.setOnClickListener {
                    thingRegstrDialog.dismiss()
                }

                view.regstrBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldNmDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldNmDfnAt = "N"
                    }
                }
                view.regstrBuldPrposAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldPrposDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldPrposDfnAt = "N"
                    }
                }
                view.regstrBuldStrctAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldStrctDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldStrctDfnAt = "N"
                    }
                }
                view.regstrBuldDongAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldDongDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldDongDfnAt = "N"
                    }
                }
                view.regstrBuldFlratoDfnAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldFlratoDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldFlratoDfnAt = "N"
                    }
                }
                view.regstrBuldHoAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldHoDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldHoDfnAt = "N"
                    }
                }
                view.regstrArAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.regstrBuldArDfnAt = "Y"
                        else -> RestThingWtnObject.regstrBuldArDfnAt = "N"
                    }
                }
                view.regstrBuldNmCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldNmTxt.text.toString()

                    mActivity.thingBuldNameEdit.setText(txtString)
                }
                view.regstrBuldPrposCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldPrposTxt.text.toString()

                    mActivity.thingbuldprposEdit.setText(txtString)
                }
                view.regstrBuldStrctCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldStrctTxt.text.toString()

                    mActivity.thingbuldStrctEdit.setText(txtString)
                }
                view.regstrBuldDongNmCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldDongNmTxt.text.toString()

                    mActivity.ThingBuldDongEdit.setText(txtString)
                }
                view.regstrBuldFlratoCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldFlratoTxt.text.toString()

                    mActivity.thingBuldhoNameEdit.setText(txtString)
                }
                view.regstrBuldHoNmCpBtn.setOnClickListener{
                    var txtString = view.regstrBuldHoNmTxt.text.toString()

                    mActivity.thingBuldFlratoEdit.setText(txtString)
                }
//                view.regstrBuldArCpBtn.setOnClickListener{
//                    var txtString = view.regstrArTxt.text.toString()
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
            layoutInflater.inflate(R.layout.rest_thing_rgist_dialog, null).let { view ->
                val thingRgistDialog =
                    ThingRgistDialogFragment(mContext, mActivity, view).apply {
                        isCancelable = false
                        show(fragmentActivity.supportFragmentManager, "Thing_Regstr_Dialog")
                    }


                view.rgistBuldNmTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldNm"))
                view.rgistBuldStrctTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldStrct"))
                view.rgistBuldPrposTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldPrpos"))
                view.rgistBuldDongNmTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldDong"))
                view.rgistBuldFlratoTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldFlrato"))
                view.rgistBuldHoNmTxt.text = checkStringNull(restThingDataJson!!.getString("rgistBuldHo"))
                view.rgistArTxt.text = checkStringNull(restThingDataJson!!.getString("rgistAr"))


                view.rgistBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldNmDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldNmDfnAt = "N"
                    }
                }
                view.rgistBuldStrctAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldStrctDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldStrctDfnAt = "N"
                    }
                }
                view.rgistBuldPrposAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldPrposDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldPrposDfnAt = "N"
                    }
                }
                view.rgistBuldDongAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldDongDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldDongDfnAt = "N"
                    }
                }
                view.rgistBuldFlratoDfnAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldFlratoDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldFlratoDfnAt = "N"
                    }
                }
                view.rgistBuldHoAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldHoDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldHoDfnAt = "N"
                    }
                }
                view.rgistArAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistArDfnAt = "Y"
                        else -> RestThingWtnObject.rgistArDfnAt = "N"
                    }
                }
                view.rgistBuldNmAtChk.setOnCheckedChangeListener{buttonView, isChecked ->

                    when (isChecked) {
                        true -> RestThingWtnObject.rgistBuldNmDfnAt = "Y"
                        else -> RestThingWtnObject.rgistBuldNmDfnAt = "N"
                    }
                }

                view.rgistBuldNmCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldNmTxt.text.toString()

                    mActivity.thingBuldNameEdit.setText(txtString)
                }
                view.rgistBuldStrctCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldStrctTxt.text.toString()

                    mActivity.thingbuldStrctEdit.setText(txtString)
                }
                view.rgistBuldPrposCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldPrposTxt.text.toString()

                    mActivity.thingbuldprposEdit.setText(txtString)
                }
                view.rgistBuldDongNmCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldDongNmTxt.text.toString()

                    mActivity.ThingBuldDongEdit.setText(txtString)
                }
                view.rgistBuldFlratoNmCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldFlratoTxt.text.toString()

                    mActivity.thingBuldhoNameEdit.setText(txtString)
                }
                view.rgistBuldHoNmCpBtn.setOnClickListener{
                    var txtString = view.rgistBuldHoNmTxt.text.toString()

                    mActivity.thingBuldFlratoEdit.setText(txtString)
                }
//                view.rgistArCpBtn.setOnClickListener{
//                    var txtString = view.rgistArTxt.text.toString()
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
        var dataString = requireActivity().intent!!.extras!!.get("RestThingInfo") as String

        logUtil.d("RestThingInfo String -----------------------> " + dataString.toString())


        var dataJson = JSONObject(dataString)
        logUtil.d("RestThingInfo dataJson -----------------------> " + dataJson.toString())

//        var thingDataJson = dataJson.getJSONObject("ThingInfo") as JSONObject
        restThingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        val thingOwnerInfoJson = dataJson.getJSONArray("ownerInfo") as JSONArray
        RestThingWtnObject.restThingOwnerInfoJson = thingOwnerInfoJson


        RestThingWtnObject.restThingInfo = restThingDataJson

        thingSpinnerAdapter(R.array.thingSmallCategoryArray, view.thingSmallSpinner)
        thingSpinnerAdapter(R.array.thingSmallCategoryArray, view.thingWdpdSmallSpinner)
        thingSpinnerAdapter(R.array.thingExaminMthArray, view.thingExaminMthSpnr)
        thingSpinnerAdapter(R.array.thingUnitArray, view.thingUnitSpinner)
        thingSpinnerAdapter(R.array.thingUnitArray, view.thingWdpUnitSpinner)
        thingSpinnerAdapter(R.array.thingUnitArray, view.thingBuildUnitSpinner)
        thingSpinnerAdapter(R.array.prmisnCl,view.thingBildngPrmisnClSpinner)


        view.thingWtnStrctLinear.visibility = View.VISIBLE
        view.thingWtnStrctAreaLinear.visibility = View.GONE


        view.thingLegalDongNmText?.text =
            checkStringNull(restThingDataJson!!.getString("legaldongNm").toString())
        view.thingBgnnLnmText?.text = checkStringNull(restThingDataJson!!.getString("bgnnLnm").toString())
        view.thingincrprLnmText?.text = checkStringNull(restThingDataJson!!.getString("incrprLnm").toString())
        view.thingGobuLadcgrNmText?.text =
            checkStringNull(restThingDataJson!!.getString("gobuLndcgrNm").toString())

        if (checkStringNull(restThingDataJson!!.getString("relateLnm").toString()).equals("")) {
            view.thingRelateLnmText?.text = context!!.getString(R.string.loanValue_b2_04)
        } else {
            view.thingRelateLnmText?.text =
                checkStringNull(restThingDataJson!!.getString("relateLnm").toString())
        }
        view.thingRelateLnmText?.text = checkStringNull(restThingDataJson!!.getString("relateLnm").toString())

        if(restThingDataJson!!.getString("thingKnd").toString().equals("null")) {
            view.thingKndEdit?.setText(checkStringNull(ThingWtnObject.thingKnd.toString()))

        } else {
            view.thingKndEdit?.setText(checkStringNull(restThingDataJson!!.getString("thingKnd").toString()))
        }
        var thingIndoorTyString = checkStringNull(restThingDataJson!!.getString("indoorTy"))
        view.thingIndoorTyChk.isChecked = thingIndoorTyString.equals("1")



        var thingSmallCl: String = ""
        if (restThingDataJson!!.getString("thingSmallCl").toString().equals("null")) {
            thingSmallCl = RestThingWtnObject.restThingSmallCl.toString()

        } else {
            thingSmallCl = checkStringNull(restThingDataJson!!.getString("thingSmallCl").toString())
            RestThingWtnObject.restThingSmallCl = thingSmallCl
        }
//        var thingSmallCl = checkStringNull(thingDataJson!!.getString("thingSmallCl").toString())

        when(thingSmallCl) {
            "A023002", "A023003" -> { // 건축물
                var smallClStringSub = thingSmallCl.substring(5,7)
                view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))


                setView(1)

                var thingNoTextString = checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                } else {
                    view.thingNoText?.text =
                        checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                }
                view.thingKndEdit?.setText(checkStringNull(restThingDataJson!!.getString("thingKnd").toString()))

                view.thingStrctNdStndrdEdit?.setText(checkStringNull(restThingDataJson!!.getString("strctNdStndrd").toString()))
                RestThingWtnObject.strctNdStndrd = checkStringNull(restThingDataJson!!.getString("strctNdStndrd").toString())
                view.thingBuildBgnnArEdit?.setText(checkStringNull(restThingDataJson!!.getString("bgnnAr").toString()))
                RestThingWtnObject.bgnnAr = checkStringNull(restThingDataJson!!.getString("bgnnAr").toString())
                view.thingBuildIncrprArEdit?.setText(checkStringNull(restThingDataJson!!.getString("incrprAr").toString()))
                RestThingWtnObject.incrprAr = checkStringNull(restThingDataJson!!.getString("incrprAr").toString())

                val buldUnitClString = checkStringNull(restThingDataJson!!.getString("unitCl"))
                val buldUnitClStringSub = buldUnitClString.substring(5,7)
                view.thingBuildUnitSpinner.setSelection(Integer.valueOf(buldUnitClStringSub))

                view.thingBuildArComputBasisEdit.setText(checkStringNull(restThingDataJson!!.getString("arComputBasis")))

//                val buldSttusMesrString = checkStringNull(restThingDataJson!!.getString("sttusMesrAt"))
//                if(buldSttusMesrString.equals("Y")) {
//                    view.thingBuildSttusMesrAtChk.isChecked = true
//                } else {
//                    view.thingBuildSttusMesrAtChk.isChecked = false
//                }
//
//                val buldOwnshipBeforeAtString = checkStringNull(restThingDataJson!!.getString("ownshipBeforeAt"))
//                if(buldOwnshipBeforeAtString.equals("Y")) {
//                    view.thingBuildOwnshipBeforeAtChk.isChecked = true
//                } else {
//                    view.thingBuildOwnshipBeforeAtChk.isChecked = false
//                }
//
//                val buldRwTrgetAtString = checkStringNull(restThingDataJson!!.getString("rwTrgetAt"))
//                if(buldRwTrgetAtString.equals("Y")){
//                    view.thingBuildRwTrgetAtChk.isChecked = true
//                } else {
//                    view.thingBuildRwTrgetAtChk.isChecked = false
//                }
//
//                val buldApasmtTrgetAtString = checkStringNull(restThingDataJson!!.getString("apasmtTrgetAt"))
//                if(buldApasmtTrgetAtString.equals("Y")) {
//                    view.thingBuildApasmtTrgetAtChk.isChecked = true
//                } else {
//                    view.thingBuildApasmtTrgetAtChk.isChecked = false
//                }

                val buldPrmisnAtString = checkStringNull(restThingDataJson!!.getString("prmisnAt"))
                view.thingRpmsnBasisChk.isChecked = buldPrmisnAtString.equals("Y")

                val buldBildngPrmisnClString = checkStringNull(restThingDataJson!!.getString("bildngPrmisnCl"))
                if(buldBildngPrmisnClString.equals("")) {
                    view.thingBildngPrmisnClSpinner.setSelection(0)
                } else {
                    val buldBildngPrmisnClStringSub = buldBildngPrmisnClString.substring(5,7)
                    view.thingBildngPrmisnClSpinner.setSelection(Integer.valueOf(buldBildngPrmisnClStringSub))

                }

                val buldredeBingAtString = checkStringNull(restThingDataJson!!.getString("redeBingAt"))
                view.thingRedeBingAtChk.isChecked = buldredeBingAtString.equals("Y")
                view.thingBuldNameEdit.setText(checkStringNull(restThingDataJson!!.getString("buldNm")))
                view.thingbuldprposEdit.setText(checkStringNull(restThingDataJson!!.getString("buldPrpos")))
                view.thingbuldStrctEdit.setText(checkStringNull(restThingDataJson!!.getString("buldStrct")))
                view.ThingBuldDongEdit.setText(checkStringNull(restThingDataJson!!.getString("buldDong")))
                view.thingBuldhoNameEdit.setText(checkStringNull(restThingDataJson!!.getString("buldHo")))
                view.thingBuldFlratoEdit.setText(checkStringNull(restThingDataJson!!.getString("buldFlrato")))
//                view.thingbuldArEdit.setText(checkStringNull(thingDataJson!!.getString("buld")))
                view.regstrDfnDtlsEdit.setText(checkStringNull(restThingDataJson!!.getString("regstrDfnDtls")))
                view.rgistDfnDtlsEdit.setText(checkStringNull(restThingDataJson!!.getString("rgistDfnDtls")))


            }
            "A023001","A023004","A023006","A023007", "A023008","A023009","A023010","A023011" -> { // 일반지장물

                var smallClStringSub = thingSmallCl.substring(5,7)
                view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))

                setView(2)

                var thingNoTextString = checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                } else {
                    view.thingNoText?.text =
                        checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                }
                view.thingKndEdit?.setText(checkStringNull(restThingDataJson!!.getString("thingKnd").toString()))

                view.thingStrctNdStndrdEdit?.setText(checkStringNull(restThingDataJson!!.getString("strctNdStndrd").toString()))
                RestThingWtnObject.strctNdStndrd = checkStringNull(restThingDataJson!!.getString("strctNdStndrd").toString())

                view.thingBgnnArEdit.setText(checkStringNull(restThingDataJson!!.getString("bgnnAr")))
                view.thingIncrprArEdit.setText(checkStringNull(restThingDataJson!!.getString("incrprAr")))

                val unitClString = checkStringNull(restThingDataJson!!.getString("unitCl"))
                val unitClStringSub = unitClString.substring(5,7)
                view.thingUnitSpinner.setSelection(Integer.valueOf(unitClStringSub))

                view.thingArComputBasisEdit.setText(checkStringNull(restThingDataJson!!.getString("arComputBasis")))

//                val sttusMestAtString = checkStringNull(restThingDataJson!!.getString("sttusMesrAt"))
//                if(sttusMestAtString.equals("Y")) {
//                    view.sttusMesrAtChk.isChecked = true
//                } else {
//                    view.sttusMesrAtChk.isChecked = false
//                }
//
//                val ownshipBeforeAtString = checkStringNull(restThingDataJson!!.getString("ownshipBeforeAt"))
//                if(ownshipBeforeAtString.equals("Y")) {
//                    view.ownshipBeforeAtChk.isChecked = true
//                } else {
//                    view.ownshipBeforeAtChk.isChecked = false
//                }
//
//                val rwTrgetAtString = checkStringNull(restThingDataJson!!.getString("rwTrgetAt"))
//                if(rwTrgetAtString.equals("Y")) {
//                    view.rwTrgetAtChk.isChecked = true
//                }else {
//                    view.rwTrgetAtChk.isChecked = false
//                }
//
//                val apasmtTrgetAtString = checkStringNull(restThingDataJson!!.getString("apasmtTrgetAt"))
//                if(apasmtTrgetAtString.equals("Y")) {
//                    view.apasmtTrgetAtChk.isChecked = true
//                } else {
//                    view.apasmtTrgetAtChk.isChecked = false
//                }

            }
            "A023005" -> { // 수목

//                var smallClStringSub = thingSmallCl.substring(5,7)
//                view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
//                view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))

                setView(3)

                var thingNoWdpdString = checkStringNull(restThingDataJson!!.getString("thingWtnCode"))
                if(thingNoWdpdString.equals("")) {
                    view.thingWdpdNoText.text = "자동기입"
                } else {
                    view.thingWdpdNoText.text = thingNoWdpdString
                }

                if(thingSmallCl.equals("")) {
                    view.thingSmallSpinner.setSelection(0)
                    view.thingWdpdSmallSpinner.setSelection(0)
                } else {
                    var smallClStringSub = thingSmallCl.substring(5,7)
                    view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                    view.thingWdpdSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))
                }


                view.thingWdpdKndEdit.setText(checkStringNull(restThingDataJson!!.getString("thingKnd")))

                view.thingWdptBgnnArEdit.setText(checkStringNull(restThingDataJson!!.getString("bgnnAr")))
                view.thingWdptincrprArEdit.setText(checkStringNull(restThingDataJson!!.getString("incrprAr")))

                val thingWdpdUnitClString = checkStringNull(restThingDataJson!!.getString("unitCl"))
                if(thingWdpdUnitClString.equals("")) {
                    view.thingWdpUnitSpinner.setSelection(0)

                } else {
                    val thingWdpdUnitClStringSub = thingWdpdUnitClString.substring(5,7)
                    view.thingWdpUnitSpinner.setSelection(Integer.valueOf(thingWdpdUnitClStringSub))

                }


                val thingWdpdExaminMthdString = checkStringNull(restThingDataJson!!.getString("examinMthd"))
                if(thingWdpdExaminMthdString.equals("")) {
                    view.thingExaminMthSpnr.setSelection(0)
                } else {
                    view.thingExaminMthSpnr.setSelection(
                        when (thingWdpdExaminMthdString) {
                            "개별" -> 1
                            "면적" -> 2
                            else -> 3
                        }
                    )
                }
//                val thingWdpdExamInMthdStringSub = thingWdpdExaminMthdString.substring(5,7)
//                view.thingExaminMthSpnr.setSelection(Integer.valueOf(thingWdpdExamInMthdStringSub))

                if(RestThingWtnObject.restThingNewSearch.equals("Y")) {
                    view.thingWtnStrctAreaLinear.visibility = View.VISIBLE
                    view.thingWtnWdpdStrctLinear.visibility = View.GONE
                } else {
                    view.thingWtnStrctAreaLinear.visibility = View.GONE
                    view.thingWtnWdpdStrctLinear.visibility = View.VISIBLE

                    view.thingWdpdStrctNdStndrdEdit.setText(checkStringNull(restThingDataJson!!.getString("strctNdStndrd")))

                }

                view.thingwdpArComputBasisEdit.setText(checkStringNull(restThingDataJson!!.getString("arComputBasis")))
                val thingWdpdNrmltpltAtString = checkStringNull(restThingDataJson!!.getString("nrmltpltAt"))
                view.thingNrmltpltAtChk.isChecked = thingWdpdNrmltpltAtString.equals("Y")

                view.thingwdptResnEdit.setText(checkStringNull(restThingDataJson!!.getString("wdptResn")))

                val buldSttusMesrString = checkStringNull(restThingDataJson!!.getString("sttusMesrAt"))
                view.thingWdpdSttusMesrAtChk.isChecked = buldSttusMesrString.equals("Y")

                val buldOwnshipBeforeAtString = checkStringNull(restThingDataJson!!.getString("ownshipBeforeAt"))
                view.thingWdpdOwnshipBeforeAtChk.isChecked = buldOwnshipBeforeAtString.equals("Y")

                val buldRwTrgetAtString = checkStringNull(restThingDataJson!!.getString("rwTrgetAt"))
                view.thingWdpdRwTrgetAtChk.isChecked = buldRwTrgetAtString.equals("Y")

                val buldApasmtTrgetAtString = checkStringNull(restThingDataJson!!.getString("apasmtTrgetAt"))
                view.thingWdpdApasmtTrgetAtChk.isChecked = buldApasmtTrgetAtString.equals("Y")
            }

            else -> {

                view.thingSmallSpinner.setSelection(0)
                view.thingWdpdSmallSpinner.setSelection(0)

                view.thingSmallSpinner.setSelection(0)
                view.thingWdpdSmallSpinner.setSelection(0)

                setView(2)

                var thingNoTextString = checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                if (thingNoTextString.equals("")) {
                    view.thingNoText?.text = "자동기입"
                    view.thingWdpdNoText?.text = "자동기입"
                } else {
                    view.thingNoText?.text =
                        checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
                }

            }
        }
        view.includePaclrMatterEdit.setText(checkStringNull(restThingDataJson!!.getString("paclrMatter")))
        view.includeReferMatterEdit.setText(checkStringNull(restThingDataJson!!.getString("referMatter")))
        view.includeRmEdit.setText(checkStringNull(restThingDataJson!!.getString("rm")))

    }

    fun setRestSearchLayout() {

        val restThingSearchItem = arrayOf(
            restThingSearchItem01Layout,
            restThingSearchItem02Layout,
            restThingSearchItem03Layout,
            restThingSearchItem04Layout,
            restThingSearchItem05Layout,
            restThingSearchItem06Layout,
            restThingSearchItem07Layout,
            restThingSearchItem08Layout,
            restThingSearchItem09Layout,
            restThingSearchItem10Layout,
        )

        for (idx in 0..(restThingSearchItem.size - 1)) {
            val item = restThingSearchItem.get(idx)
            item.restLandSearchItemDesc1.text = resources.getStringArray(R.array.restLandQuestArr).get(idx)

            // EditText의 inputType이 textMultiLine에서 imeOptions 값을 설정하기 위해서
            item.restLandSearchItemEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
            item.restLandSearchItemEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        // EditText의 inputType이 textMultiLine에서 imeOptions 값을 설정하기 위해서
        restThingSearchItemCause.restLandSearchItemEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        restThingSearchItemCause.restLandSearchItemEditText.imeOptions = EditorInfo.IME_ACTION_DONE

        restThingSearchItem01Layout.restLandSearchItemEditText.setOnEditorActionListener { textView, action, _ ->

            if (action == EditorInfo.IME_ACTION_DONE) {

            }
            false
        }

        // TODO : 기존 입력값 셋팅
        restThingSearchItem01Layout.restLandSearchItemEditText.setText("1")
        restThingSearchItem02Layout.restLandSearchItemEditText.setText("2")
        restThingSearchItem03Layout.restLandSearchItemEditText.setText("3")
        restThingSearchItem04Layout.restLandSearchItemEditText.setText("4")
        restThingSearchItem05Layout.restLandSearchItemEditText.setText("5")
        restThingSearchItem06Layout.restLandSearchItemEditText.setText("6")
        restThingSearchItem07Layout.restLandSearchItemEditText.setText("7")
        restThingSearchItem08Layout.restLandSearchItemEditText.setText("8")
        restThingSearchItem09Layout.restLandSearchItemEditText.setText("9")
        restThingSearchItem10Layout.restLandSearchItemEditText.setText("10")

        wtnncUtill.wtnncSpinnerAdapter(R.array.restThingRewardChkArray, restThingSearchResultAtChk, this) // 확대보상여부 결과

        restThingSearchResultAtChk.setSelection(0)

    }


    fun thingSpinnerAdapter(stringArray: Int, spinner: Spinner?) {

        spinner?.adapter = CustomDropDownAdapter(context!!, listOf(resources.getStringArray(stringArray))[0])
        spinner?.onItemSelectedListener = this

        // 카메라 어댑터 세팅
        for (i in 0..4) {
            Constants.CAMERA_IMAGE_ARR.add(WtnncImage(i, null, "","","","","","","","",""))
        }

        Constants.CAMERA_ADAPTER = WtnncImageAdapter(requireContext(), Constants.CAMERA_IMAGE_ARR)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        includeImageViewRv.also {
            it.layoutManager = layoutManager
            it.adapter = Constants.CAMERA_ADAPTER
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val thingAddView: LinearLayout = activity.findViewById(R.id.thingLinearAddView)

        when (parent?.id) {
            R.id.thingSmallSpinner -> {
                when (position) {
//                    1 -> { //동산이전
//                        setView(2)
//
//                        thingAddView.removeAllViews()
//
//                        RestThingWtnObject.restThingSmallCl = "A023001"
//                    }
//                    2 -> { // 일반건축물
//                        setView(1)
//
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023002"
//                    }
//                    3 -> { // 집합건축물
//                        setView(1)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023003"
//                    }
//                    4 -> { // 공작물
//                        setView(2)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023004"
//                    }
//                    5 -> { // 수목
//                        setView(3)
//                        thingAddView.removeAllViews()
//                        thingWdpdSmallSpinner.setSelection(5)
//                        RestThingWtnObject.restThingSmallCl = "A023005"
//                    }
//                    6 -> { // 개간비
//                        setView(2)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023006"
//                    }
//                    7 -> { //잔여지가격손실
//                        setView(2)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023007"
//                    }
//                    8 -> { //소유권이외의권리
//                        setView(2)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023008"
//                    }

                    2, 3 -> {
                        setView(1)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    1, 4, 6, 7, 8 -> {
                        setView(2)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    5 -> {
                        setView(3)
                        thingAddView.removeAllViews()
                        thingWdpdSmallSpinner.setSelection(5)
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    else -> {
                        setView(2)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = ""
                    }
                }
            }
            R.id.thingWdpdSmallSpinner -> {
                when (position) {
//                    1 -> { //동산이전
//                        setView(2)
//
//                        thingAddView.removeAllViews()
//                        thingSmallSpinner.setSelection(1)
//
//                        RestThingWtnObject.restThingSmallCl = "A023001"
//                    }
//                    2 -> { // 일반건축물
//                        setView(1)
//                        thingSmallSpinner.setSelection(2)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023002"
//                    }
//                    3 -> { // 집합건축물
//                        setView(1)
//                        thingSmallSpinner.setSelection(3)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023003"
//                    }
//                    4 -> { // 공작물
//                        setView(2)
//                        thingSmallSpinner.setSelection(4)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023004"
//                    }
//                    5 -> { // 수목
//                        setView(3)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023005"
//                    }
//                    6 -> { // 개간비
//                        setView(2)
//                        thingSmallSpinner.setSelection(6)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023006"
//                    }
//                    7 -> { //잔여지가격손실
//                        setView(2)
//                        thingSmallSpinner.setSelection(7)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023007"
//                    }
//                    8 -> { //소유권이외의권리
//                        setView(2)
//                        thingSmallSpinner.setSelection(8)
//                        thingAddView.removeAllViews()
//                        RestThingWtnObject.restThingSmallCl = "A023008"
//                    }

                    2, 3 -> { // 집합건축물
                        setView(1)
                        thingSmallSpinner.setSelection(position)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    1, 4, 6, 7, 8 -> { // 공작물
                        setView(2)
                        thingSmallSpinner.setSelection(position)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    5 -> { // 수목
                        setView(3)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = String.format("A0230%02d", position)
                    }
                    else -> {
                        setView(2)
                        thingSmallSpinner.setSelection(9)
                        thingAddView.removeAllViews()
                        RestThingWtnObject.restThingSmallCl = ""
                    }
                }
            }
            R.id.thingExaminMthSpnr -> {
                when (position) {
                    0 -> { // 선택
                        logUtil.d("thingExaminMthSpnr select 0")
                        thingWtnStrctLinear.visibility = View.VISIBLE
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        RestThingWtnObject.examinMthd = ""
                    }
                    1 -> { // 개별
                        logUtil.d("thingExaminMthSpnr select 1")
                        if(RestThingWtnObject.restThingNewSearch.equals("Y")) {
                            thingWtnWdpdStrctLinear.visibility = View.GONE
                            thingWtnStrctAreaLinear.visibility = View.VISIBLE
                        } else {
                            thingWtnWdpdStrctLinear.visibility = View.VISIBLE
                            thingWtnStrctAreaLinear.visibility = View.GONE
                        }

                        RestThingWtnObject.examinMthd = "개별"
                    }
                    2 -> { // 면적
                        logUtil.d("thingExaminMthSpnr select 2")
                        if(RestThingWtnObject.restThingNewSearch.equals("Y")) {
                            thingWtnWdpdStrctLinear.visibility = View.GONE
                            thingWtnStrctAreaLinear.visibility = View.VISIBLE
                        } else {
                            thingWtnWdpdStrctLinear.visibility = View.VISIBLE
                            thingWtnStrctAreaLinear.visibility = View.GONE
                        }
                        RestThingWtnObject.examinMthd = "면적"
                    }
                    3 -> { // 기타
                        logUtil.d("thingExaminMthSpnr select 3")
                        thingWtnWdpdStrctLinear.visibility = View.VISIBLE
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        RestThingWtnObject.examinMthd = "기타"
                    }
                    else -> {
                        thingWtnWdpdStrctLinear.visibility = View.VISIBLE
                        thingWtnStrctAreaLinear.visibility = View.GONE
                        RestThingWtnObject.examinMthd = ""
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addThingData() {

        RestThingWtnObject.restThingIndoorTy = when(mActivity.thingIndoorTyChk.isChecked) {
            true ->"1"
            else ->"2"
        }

        RestThingWtnObject.restThingLrgeCl = "A011001" //대분류 지장물
        RestThingWtnObject.restThingSmallCl = when(activity.thingSmallSpinner.selectedItemPosition) {
//            1-> "A023001"
//            2-> "A023002"
//            3-> "A023003"
//            4-> "A023004"
//            5-> "A023005"
//            6-> "A023006"
//            7-> "A023007"
//            8-> "A023008"
//            9-> "A023009"
            in 1..9 -> {
                String.format("A0230%02d", activity.thingSmallSpinner.selectedItemPosition)
            }
            else ->""
        }


        if(RestThingWtnObject.restThingSmallCl.equals("A023002") ||
            RestThingWtnObject.restThingSmallCl.equals("A023003")) { //건축물
            RestThingWtnObject.restThingKnd = mActivity.thingKndEdit.text.toString()
            RestThingWtnObject.strctNdStndrd = mActivity.thingStrctNdStndrdEdit.text.toString()
            RestThingWtnObject.bgnnAr = mActivity.thingBuildBgnnArEdit.text.toString()
            RestThingWtnObject.incrprAr = mActivity.thingBuildIncrprArEdit.text.toString()

            RestThingWtnObject.unitCl = when (mActivity.thingBuildUnitSpinner.selectedItemPosition) {
//                1 -> "A009001"
//                2 -> "A009002"
//                3 -> "A009003"
//                4 -> "A009004"
//                5 -> "A009005"
//                6 -> "A009006"
//                7 -> "A009007"
//                8 -> "A009008"
//                9 -> "A009009"
//                10 -> "A009010"
//                11 -> "A009011"
//                12 -> "A009012"
//                13 -> "A009013"
//                14 -> "A009014"
//                15 -> "A009015"
//                16 -> "A009016"
//                17 -> "A009017"
//                18 -> "A009018"
//                19 -> "A009019"
//                20 -> "A009020"
//                21 -> "A009021"
//                22 -> "A009022"
//                23 -> "A009023"
//                24 -> "A009024"
//                25 -> "A009025"
//                26 -> "A009026"
//                27 -> "A009027"
//                28 -> "A009028"
//                29 -> "A009029"
//                30 -> "A009030"
//                31 -> "A009031"
//                32 -> "A009032"
//                33 -> "A009033"
//                34 -> "A009034"
//                35 -> "A009035"
//                36 -> "A009036"
//                37 -> "A009037"
//                38 -> "A009038"
//                39 -> "A009039"
//                40 -> "A009040"
//                41 -> "A009041"
//                42 -> "A009042"
//                43 -> "A009043"
//                44 -> "A009044"
//                45 -> "A009045"
//                46 -> "A009046"
//                47 -> "A009047"
//                48 -> "A009048"
//                49 -> "A009049"
//                50 -> "A009050"
//                51 -> "A009051"
//                52 -> "A009052"
//                53 -> "A009053"
//                54 -> "A009054"
//                55 -> "A009055"
//                56 -> "A009056"
//                57 -> "A009057"
//                58 -> "A009058"
//                59 -> "A009059"
//                60 -> "A009060"
//                61 -> "A009061"
//                62 -> "A009062"
//                63 -> "A009063"
//                64 -> "A009064"
//                65 -> "A009065"
//                66 -> "A009066"
//                67 -> "A009067"
//                68 -> "A009068"
                in 1..68 -> {
                    String.format("A0090%02d", mActivity.thingBuildUnitSpinner.selectedItemPosition)
                }
                else -> ""
            }

            RestThingWtnObject.arComputBasis = mActivity.thingBuildArComputBasisEdit.text.toString()

            RestThingWtnObject.sttusMesrAt = when (mActivity.thingBuildSttusMesrAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            RestThingWtnObject.ownshipBeforeAt = when (mActivity.thingBuildOwnshipBeforeAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            RestThingWtnObject.rwTrgetAt = when (mActivity.thingBuildRwTrgetAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.apasmtTrgetAt = when (mActivity.thingBuildApasmtTrgetAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.prmisnAt = when (mActivity.thingRpmsnBasisChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.thingNrtBuldAt = when (mActivity.thingNrtBuldAt.isChecked) {
                true ->"Y"
                else ->"N"
            }

            RestThingWtnObject.bildngPrmisnCl = when (mActivity.thingBildngPrmisnClSpinner.selectedItemPosition) {
//                1 -> "A029001"
//                2 -> "A029002"
//                3 -> "A029003"
//                4 -> "A029004"
                in 1..4 -> {
                    String.format("A0290%02d", mActivity.thingBildngPrmisnClSpinner.selectedItemPosition)
                }
                else -> ""
            }
            RestThingWtnObject.rgistAt = when (mActivity.thingRgistAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.redeBingAt = when (mActivity.thingRedeBingAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }

            RestThingWtnObject.buldName = mActivity.thingBuldNameEdit.text.toString()
            RestThingWtnObject.buldPrpos = mActivity.thingbuldprposEdit.text.toString()
            RestThingWtnObject.buldStrct = mActivity.thingbuldStrctEdit.text.toString()
            RestThingWtnObject.buldDongName = mActivity.ThingBuldDongEdit.text.toString()
            RestThingWtnObject.buldHoName = mActivity.thingBuldhoNameEdit.text.toString()
            RestThingWtnObject.buldFlrato = mActivity.thingBuldFlratoEdit.text.toString()
//            RestThingWtnObject.buldAr = mActivity.thingbuldArEdit.text.toString()
            RestThingWtnObject.regstrDfnDtls = mActivity.regstrDfnDtlsEdit.text.toString()
            RestThingWtnObject.rgistDfnDtls = mActivity.rgistDfnDtlsEdit.text.toString()


        } else if(RestThingWtnObject.restThingSmallCl.equals("A023005")) { // 수목
            RestThingWtnObject.restThingKnd = mActivity.thingWdpdKndEdit.text.toString()

            RestThingWtnObject.bgnnAr = mActivity.thingWdptBgnnArEdit.text.toString()
            RestThingWtnObject.incrprAr = mActivity.thingWdptincrprArEdit.text.toString()

            RestThingWtnObject.unitCl = when (mActivity.thingWdpUnitSpinner.selectedItemPosition) {
//                1 -> "A009001"
//                2 -> "A009002"
//                3 -> "A009003"
//                4 -> "A009004"
//                5 -> "A009005"
//                6 -> "A009006"
//                7 -> "A009007"
//                8 -> "A009008"
//                9 -> "A009009"
//                10 -> "A009010"
//                11 -> "A009011"
//                12 -> "A009012"
//                13 -> "A009013"
//                14 -> "A009014"
//                15 -> "A009015"
//                16 -> "A009016"
//                17 -> "A009017"
//                18 -> "A009018"
//                19 -> "A009019"
//                20 -> "A009020"
//                21 -> "A009021"
//                22 -> "A009022"
//                23 -> "A009023"
//                24 -> "A009024"
//                25 -> "A009025"
//                26 -> "A009026"
//                27 -> "A009027"
//                28 -> "A009028"
//                29 -> "A009029"
//                30 -> "A009030"
//                31 -> "A009031"
//                32 -> "A009032"
//                33 -> "A009033"
//                34 -> "A009034"
//                35 -> "A009035"
//                36 -> "A009036"
//                37 -> "A009037"
//                38 -> "A009038"
//                39 -> "A009039"
//                40 -> "A009040"
//                41 -> "A009041"
//                42 -> "A009042"
//                43 -> "A009043"
//                44 -> "A009044"
//                45 -> "A009045"
//                46 -> "A009046"
//                47 -> "A009047"
//                48 -> "A009048"
//                49 -> "A009049"
//                50 -> "A009050"
//                51 -> "A009051"
//                52 -> "A009052"
//                53 -> "A009053"
//                54 -> "A009054"
//                55 -> "A009055"
//                56 -> "A009056"
//                57 -> "A009057"
//                58 -> "A009058"
//                59 -> "A009059"
//                60 -> "A009060"
//                61 -> "A009061"
//                62 -> "A009062"
//                63 -> "A009063"
//                64 -> "A009064"
//                65 -> "A009065"
//                66 -> "A009066"
//                67 -> "A009067"
//                68 -> "A009068"
                in 1..68 -> {
                    String.format("A0090%02d", mActivity.thingWdpUnitSpinner.selectedItemPosition)
                }
                else -> ""
            }

            RestThingWtnObject.examinMthd = when (mActivity.thingExaminMthSpnr.selectedItemPosition) {
                1 -> "개별"
                2 -> "면적"
                3 -> "기타"
                else -> ""
            }

            RestThingWtnObject.arComputBasis = mActivity.thingwdpArComputBasisEdit.text.toString()

            RestThingWtnObject.nrmltpltAt = when( mActivity.thingNrmltpltAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.wdptResn = mActivity.thingwdptResnEdit.text.toString()

            if(mActivity.thingWtnStrctAreaLinear.visibility == View.VISIBLE) {

                val strctNdStrndrdRString = mActivity.thingStrctNdStndrdEditR.text.toString()
                val strctNdStrndrdBString = mActivity.thingStrctNdStndrdEditB.text.toString()
                val strctNdStrndrdHString = mActivity.thingStrctNdStndrdEditH.text.toString()
                val strctNdStrndrdLString = mActivity.thingStrctNdStndrdEditL.text.toString()
                val strctNdStrndrdWString = mActivity.thingStrctNdStndrdEditW.text.toString()

                RestThingWtnObject.strctNdStndrd = StringBuilder().apply {
                    if(!strctNdStrndrdRString.equals("")) {
                        if(!strctNdStrndrdBString.equals("")
                            || !strctNdStrndrdHString.equals("")
                            || !strctNdStrndrdLString.equals("")
                            || !strctNdStrndrdWString.equals("")) {
                            append("R" + strctNdStrndrdRString + "×")
                        } else {
                            append("R" + strctNdStrndrdRString)
                        }
                    }
                    if(!strctNdStrndrdBString.equals("")) {
                        if(!strctNdStrndrdHString.equals("")
                            || !strctNdStrndrdLString.equals("")
                            || !strctNdStrndrdWString.equals("")) {
                            append("B" + strctNdStrndrdBString + "×")
                        } else {
                            append("B" + strctNdStrndrdBString)
                        }
                    }
                    if(!strctNdStrndrdHString.equals("")) {
                        if(!strctNdStrndrdLString.equals("")
                            || !strctNdStrndrdWString.equals("")) {
                            append("H" + strctNdStrndrdHString + "×")
                        } else {
                            append("H" + strctNdStrndrdHString)
                        }
                    }
                    if(!strctNdStrndrdLString.equals("")) {
                        if(!strctNdStrndrdWString.equals("")) {
                            append("L" + strctNdStrndrdLString + "×")
                        } else {
                            append("L" + strctNdStrndrdLString)
                        }
                    }
                    if(!strctNdStrndrdWString.equals("")) {
                        append("W" + strctNdStrndrdWString)
                    }
                }.toString()


            } else {
                RestThingWtnObject.strctNdStndrd = mActivity.thingWdpdStrctNdStndrdEdit.text.toString()
            }




        } else { // 일반

            RestThingWtnObject.restThingKnd = mActivity.thingKndEdit.text.toString()
            RestThingWtnObject.strctNdStndrd = mActivity.thingStrctNdStndrdEdit.text.toString()

            RestThingWtnObject.bgnnAr = mActivity.thingBgnnArEdit.text.toString()
            RestThingWtnObject.incrprAr = mActivity.thingIncrprArEdit.text.toString()

            RestThingWtnObject.unitCl = when (mActivity.thingUnitSpinner.selectedItemPosition) {
//                1 -> "A009001"
//                2 -> "A009002"
//                3 -> "A009003"
//                4 -> "A009004"
//                5 -> "A009005"
//                6 -> "A009006"
//                7 -> "A009007"
//                8 -> "A009008"
//                9 -> "A009009"
//                10 -> "A009010"
//                11 -> "A009011"
//                12 -> "A009012"
//                13 -> "A009013"
//                14 -> "A009014"
//                15 -> "A009015"
//                16 -> "A009016"
//                17 -> "A009017"
//                18 -> "A009018"
//                19 -> "A009019"
//                20 -> "A009020"
//                21 -> "A009021"
//                22 -> "A009022"
//                23 -> "A009023"
//                24 -> "A009024"
//                25 -> "A009025"
//                26 -> "A009026"
//                27 -> "A009027"
//                28 -> "A009028"
//                29 -> "A009029"
//                30 -> "A009030"
//                31 -> "A009031"
//                32 -> "A009032"
//                33 -> "A009033"
//                34 -> "A009034"
//                35 -> "A009035"
//                36 -> "A009036"
//                37 -> "A009037"
//                38 -> "A009038"
//                39 -> "A009039"
//                40 -> "A009040"
//                41 -> "A009041"
//                42 -> "A009042"
//                43 -> "A009043"
//                44 -> "A009044"
//                45 -> "A009045"
//                46 -> "A009046"
//                47 -> "A009047"
//                48 -> "A009048"
//                49 -> "A009049"
//                50 -> "A009050"
//                51 -> "A009051"
//                52 -> "A009052"
//                53 -> "A009053"
//                54 -> "A009054"
//                55 -> "A009055"
//                56 -> "A009056"
//                57 -> "A009057"
//                58 -> "A009058"
//                59 -> "A009059"
//                60 -> "A009060"
//                61 -> "A009061"
//                62 -> "A009062"
//                63 -> "A009063"
//                64 -> "A009064"
//                65 -> "A009065"
//                66 -> "A009066"
//                67 -> "A009067"
//                68 -> "A009068"
                in 1..68 -> {
                    String.format("A0090%02d", mActivity.thingUnitSpinner.selectedItemPosition)
                }
                else -> ""
            }

            RestThingWtnObject.arComputBasis = mActivity.thingArComputBasisEdit.text.toString()

            RestThingWtnObject.sttusMesrAt = when (mActivity.sttusMesrAtChk.isChecked) {
                true ->"Y"
                else ->"N"
            }
            RestThingWtnObject.ownshipBeforeAt = when (mActivity.ownshipBeforeAtChk.isChecked) {
                true->"Y"
                else ->"N"
            }
            RestThingWtnObject.rwTrgetAt = when (mActivity.rwTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }
            RestThingWtnObject.apasmtTrgetAt = when (mActivity.apasmtTrgetAtChk.isChecked) {
                true -> "Y"
                else -> "N"
            }


        }
        RestThingWtnObject.paclrMatter = mActivity.includePaclrMatterEdit.text.toString()

        RestThingWtnObject.referMatter = mActivity.includeReferMatterEdit.text.toString()

        RestThingWtnObject.rm = mActivity.includeRmEdit.text.toString()
    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

    fun setView(viewCode: Int) {

        if(viewCode == 1) { //건축물
            thingSearchBaseViewLl.visibility = View.VISIBLE
            thingSearchBaseWdpdViewLl.visibility = View.GONE
            thingSearchBildAddview.visibility = View.VISIBLE
            thingSearchGnrlBaseView.visibility = View.GONE
            wtnncCommIndoorTy.visibility = View.VISIBLE
            wtnncCommIndoorTyLl.visibility = View.VISIBLE
        } else if(viewCode == 2) { //일반
            thingSearchBaseViewLl.visibility = View.VISIBLE
            thingSearchBaseWdpdViewLl.visibility = View.GONE
            thingSearchBildAddview.visibility = View.GONE
            thingSearchGnrlBaseView.visibility = View.VISIBLE
            wtnncCommIndoorTy.visibility = View.VISIBLE
            wtnncCommIndoorTyLl.visibility = View.VISIBLE
        } else if(viewCode == 3) { //수목
            thingSearchBaseViewLl.visibility = View.GONE
            thingSearchBaseWdpdViewLl.visibility = View.VISIBLE
            thingSearchBildAddview.visibility = View.GONE
            thingSearchGnrlBaseView.visibility = View.GONE
            wtnncCommIndoorTy.visibility = View.GONE
            wtnncCommIndoorTyLl.visibility = View.GONE
            thingWtnWdpdStrctLinear.visibility = View.VISIBLE
        }

    }
}