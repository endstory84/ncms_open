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
import android.widget.Spinner
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.fragment_restland_search_item.view.*
import kotlinx.android.synthetic.main.rest_thing_search_gnrl.*
import kotlinx.android.synthetic.main.rest_thing_search_gnrl.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.CustomDropDownAdapter
import kr.or.kreb.ncms.mobile.adapter.RestThingSearchRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.CommonCodeInfoList
import kr.or.kreb.ncms.mobile.data.RestThingWtnObject
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.util.WtnncUtil
import org.json.JSONArray
import org.json.JSONObject

class RestThingSearchFragment(val activity: Activity, context: Context, val fragmentActivity: FragmentActivity) :
    BaseFragment(),
    AdapterView.OnItemSelectedListener {

    private lateinit var restThingSearchAdapter: RestThingSearchRecyclerViewAdapter

//    private var toastUtil: ToastUtil = ToastUtil(context)
//    private var logUtil: LogUtil = LogUtil("RestThingSearchFragment")

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
    }

    fun init(view: View) {
        var dataString = requireActivity().intent!!.extras!!.get("ThingInfo") as String

        logUtil.d("RestThingInfo String -----------------------> " + dataString.toString())


        var dataJson = JSONObject(dataString)
        logUtil.d("RestThingInfo dataJson -----------------------> " + dataJson.toString())

//        var thingDataJson = dataJson.getJSONObject("ThingInfo") as JSONObject
        restThingDataJson = dataJson.getJSONObject("ThingSearch") as JSONObject

        val thingOwnerInfoJson = dataJson.getJSONArray("ownerInfo") as JSONArray
        RestThingWtnObject.thingOwnerInfoJson = thingOwnerInfoJson


//        RestThingWtnObject.thingInfo = restThingDataJson

        thingSpinnerAdapter(R.array.thingSmallCategoryArray, view.thingSmallSpinner)
        // A009
//        thingSpinnerAdapter(R.array.thingUnitArray, view.thingBuildUnitSpinner)
        thingSpinnerAdapter("A009", view.thingBuildUnitSpinner)


        view.thingLegalDongNmText?.text =
            checkStringNull(restThingDataJson!!.getString("legaldongNm").toString())
        view.thingBgnnLnmText?.text = checkStringNull(restThingDataJson!!.getString("bgnnLnm").toString())
        view.thingincrprLnmText?.text = checkStringNull(restThingDataJson!!.getString("incrprLnm").toString())
        view.thingGobuLadcgrNmText?.text =
            checkStringNull(restThingDataJson!!.getString("gobuLndcgrNm").toString())

        if (checkStringNull(restThingDataJson!!.getString("relateLnm").toString()).equals("")) {
            view.thingRelateLnmText?.setText(context!!.getString(R.string.loanValue_b2_04))
        } else {
            view.thingRelateLnmText?.setText(checkStringNull(restThingDataJson!!.getString("relateLnm").toString()))
        }
//        view.thingRelateLnmText?.setText(checkStringNull(restThingDataJson!!.getString("relateLnm").toString()))

        if(restThingDataJson!!.getString("thingKnd").toString().equals("null")) {
            view.thingKndEdit?.setText(checkStringNull(ThingWtnObject.thingKnd.toString()))

        } else {
            view.thingKndEdit?.setText(checkStringNull(restThingDataJson!!.getString("thingKnd").toString()))
        }

        var thingSmallCl: String = ""
        if (restThingDataJson!!.getString("thingSmallCl").toString().equals("null")) {

        } else {
            thingSmallCl = checkStringNull(restThingDataJson!!.getString("thingSmallCl").toString())
        }

        var smallClStringSub = thingSmallCl.substring(5,7)
        view.thingSmallSpinner.setSelection(Integer.valueOf(smallClStringSub))

        var thingNoTextString = checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
        if (thingNoTextString.equals("")) {
            view.thingNoText?.text = "자동기입"
        } else {
            view.thingNoText?.text =
                checkStringNull(restThingDataJson!!.getString("thingWtnCode").toString())
        }
        view.thingKndEdit?.setText(checkStringNull(restThingDataJson!!.getString("thingKnd").toString()))

        view.thingStrctNdStndrdEdit?.setText(checkStringNull(restThingDataJson!!.getString("strctNdStndrd").toString()))
        view.thingBuildBgnnArEdit?.setText(checkStringNull(restThingDataJson!!.getString("bgnnAr").toString()))
        view.thingBuildIncrprArEdit?.setText(checkStringNull(restThingDataJson!!.getString("incrprAr").toString()))

        val buldUnitClString = checkStringNull(restThingDataJson!!.getString("unitCl"))
//        val buldUnitClStringSub = buldUnitClString.substring(5,7)
//        view.thingBuildUnitSpinner.setSelection(Integer.valueOf(buldUnitClStringSub))
        view.thingBuildUnitSpinner.setSelection( CommonCodeInfoList.getIdxFromCodeId("A009", buldUnitClString) )

        view.thingBuildArComputBasisEdit.setText(checkStringNull(restThingDataJson!!.getString("arComputBasis")))



        val buldredeBingAtString = checkStringNull(restThingDataJson!!.getString("redeBingAt"))
        view.thingBuldNameEdit.setText(checkStringNull(restThingDataJson!!.getString("buldNm")))
        view.thingbuldprposEdit.setText(checkStringNull(restThingDataJson!!.getString("buldPrpos")))
        view.thingbuldStrctEdit.setText(checkStringNull(restThingDataJson!!.getString("buldStrct")))
        view.ThingBuldDongEdit.setText(checkStringNull(restThingDataJson!!.getString("buldDong")))
        view.thingBuldhoNameEdit.setText(checkStringNull(restThingDataJson!!.getString("buldHo")))
        view.thingBuldFlratoEdit.setText(checkStringNull(restThingDataJson!!.getString("buldFlrato")))
        val nrtBuldAtString = checkStringNull(restThingDataJson!!.getString("nrtBuldAt"))
        view.thingNrtBuldAt.isChecked = nrtBuldAtString.equals("Y")




        setRestSearchLayout(dataJson)

    }

    fun setRestSearchLayout(data: JSONObject) {

        var restData = JSONArray()

        if(!data.isNull("restThing")) {
            restData = data.getJSONArray("restThing")
        }

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
            item.restLandSearchItemEditText.hint = resources.getStringArray(R.array.restLandQeustArrHint).get(idx)
        }

        // EditText의 inputType이 textMultiLine에서 imeOptions 값을 설정하기 위해서
        restThingSearchItemCause.restLandSearchItemEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        restThingSearchItemCause.restLandSearchItemEditText.imeOptions = EditorInfo.IME_ACTION_DONE

        restThingSearchItem01Layout.restLandSearchItemEditText.setOnEditorActionListener { textView, action, _ ->

            if (action == EditorInfo.IME_ACTION_DONE) {

            }
            false
        }
        wtnncUtill.wtnncSpinnerAdapter(
            R.array.restThingRewardChkArray,
            restThingSearchResultAtChk,
            this
        ) // 확대보상여부 결과
        // TODO : 기존 입력값 셋팅
        if(restData.length() > 0) {
            bqestPsnText.setText(checkStringNull(restData.getJSONObject(0).getString("rqestPsn")))
            bqestCnText.setText(checkStringNull(restData.getJSONObject(0).getString("rqestCn")))
            restThingSearchItem01Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin1Rslt")))
            restThingSearchItem02Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin2Rslt")))
            restThingSearchItem03Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin3Rslt")))
            restThingSearchItem04Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin4Rslt")))
            restThingSearchItem05Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin5Rslt")))
            restThingSearchItem06Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin6Rslt")))
            restThingSearchItem07Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin7Rslt")))
            restThingSearchItem08Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin8Rslt")))
            restThingSearchItem09Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin9Rslt")))
            restThingSearchItem10Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin10Rslt")))

            val rewdAtString = checkStringNull(restData.getJSONObject(0).getString("rewdAt"))

            restThingSearchResultAtChk.setSelection(when (rewdAtString) {
                "Y" -> 1
                "N" -> 2
                else ->0
            })
            restThingSearchItemCause.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("resn")))

        } else {
            restThingSearchResultAtChk.setSelection(0)
        }

    }


    fun thingSpinnerAdapter(stringArray: Int, spinner: Spinner?) {
        spinner?.adapter = CustomDropDownAdapter(context!!, listOf(resources.getStringArray(stringArray))[0])
        spinner?.onItemSelectedListener = this
    }

    fun thingSpinnerAdapter(codeGrroupId: String, spinner: Spinner?) {
        spinner?.adapter = CustomDropDownAdapter(context!!, CommonCodeInfoList.getCodeDcArray("A009"))
        spinner?.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addThingRestData() {
        // 서버 저장 전

        RestThingWtnObject.rqestPsn = activity!!.bqestPsnText.text.toString()

        RestThingWtnObject.rqestCn = activity!!.bqestCnText.text.toString()

        RestThingWtnObject.examin1Rslt = activity!!.restThingSearchItem01Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin2Rslt = activity!!.restThingSearchItem02Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin3Rslt = activity!!.restThingSearchItem03Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin4Rslt = activity!!.restThingSearchItem04Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin5Rslt = activity!!.restThingSearchItem05Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin6Rslt = activity!!.restThingSearchItem06Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin7Rslt = activity!!.restThingSearchItem07Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin8Rslt = activity!!.restThingSearchItem08Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin9Rslt = activity!!.restThingSearchItem09Layout.restLandSearchItemEditText.text.toString()
        RestThingWtnObject.examin10Rslt = activity!!.restThingSearchItem10Layout.restLandSearchItemEditText.text.toString()



        RestThingWtnObject.rewdAt = when(activity!!.restThingSearchResultAtChk.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            else -> ""
        }

        RestThingWtnObject.resn = activity!!.restThingSearchItemCause.restLandSearchItemEditText.text.toString()


    }

    override fun showOwnerPopup() {
//        TODO("Not yet implemented")
    }
}