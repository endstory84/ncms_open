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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_land_info.view.*
import kotlinx.android.synthetic.main.fragment_land_search.*
import kotlinx.android.synthetic.main.fragment_land_search.view.*
import kotlinx.android.synthetic.main.fragment_restland_search.*
import kotlinx.android.synthetic.main.fragment_restland_search_item.view.*
import kotlinx.android.synthetic.main.include_wtnnc_camera.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.RestLandSearchImageAdapter
import kr.or.kreb.ncms.mobile.adapter.WtnncImageAdapter
import kr.or.kreb.ncms.mobile.base.BaseFragment
import kr.or.kreb.ncms.mobile.data.RestLandInfoObject
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.util.WtnncUtil
import org.json.JSONArray
import org.json.JSONObject

class RestLandSearchFragment(val activity: Activity?, context: Context?/*, v: View?, val fragmentActivity: FragmentActivity*/) :
    BaseFragment(),
    AdapterView.OnItemSelectedListener {

    private val mContext: Context? = context
//    private var logUtil: LogUtil = LogUtil("RestLandSearchFragment")
//    private var dialogUtil = DialogUtil(context, activity)
    private lateinit var restLandSearchImageAdapter: RestLandSearchImageAdapter
    private val wtnncUtill = WtnncUtil(activity!!, context!!)

    private var restLandInfoDataJson: JSONObject? = null
    private var responseString: String? = null


    /////////////////////////////////////////////////////////////////////////////

    var imageArr = mutableListOf<WtnncImage>()
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
        return inflater.inflate(R.layout.fragment_restland_search, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        getData()
        initUi(view)

//        //???????????? ??????????????????
//        includeCameraBtn.setOnClickListener {
//            nextView(
//                requireActivity(),
//                Constants.CAMERA_ACT,
//                null,
//                CameraEnum.DEFAULT,
//                null
//                , null
//            )
//        }

//        // ????????????
//        includePaclrMatterEdit.setOnEditorActionListener { textView, action, _ ->
//
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                logUtil.d("???????????? ????????? ??????")
//                val txtString = textView.text.toString()
//                RestLandInfoObject.paclrMatter = txtString
//            }
//            false
//        }
        // ????????????
//        includeReferMatterEdit.setOnEditorActionListener { textView, action, _ ->
//
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                logUtil.d("???????????? ????????? ??????")
//                val txtString = textView.text.toString()
//                RestLandInfoObject.referMatter = txtString
//            }
//            false
//
//        }
//        // ??????
//        includeRmEdit.setOnEditorActionListener { textView, action, _ ->
//
//            if (action == EditorInfo.IME_ACTION_DONE) {
//                logUtil.d("???????????? ????????? ??????")
//                val txtString = textView.text.toString()
//                RestLandInfoObject.rm = txtString
//            }
//            false
//        }

    }

    fun initUi(view: View) {
        logUtil.d("init response String --" + responseString)

        var dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String

        logUtil.d("LandInfo String ---------------------> " + dataString.toString());

        var dataJson = JSONObject(dataString)

        logUtil.d("LandInfo json --------------------> " + dataJson.toString());

        var landInfoDataJson = dataJson.getJSONObject("LandInfo")
        //?????? ??????
        // No
        val landNo = checkStringNull(landInfoDataJson!!.getString("no"))
        val landSubNo = checkStringNull(landInfoDataJson!!.getString("subNo"))

        view.landSearchNoText?.text = "$landNo-$landSubNo"
//        view.landSearchNoText?.text = checkStringNull(landInfoDataJson!!.getString("ladWtnCode"))
        // ?????????
        view.landSearchLocationText?.text =
            checkStringNull(landInfoDataJson!!.getString("legaldongNm"))
        // ?????? ??????
        view.landSearchBgnnLnmText?.text = checkStringNull(landInfoDataJson!!.getString("bgnnLnm"))
        // ?????? ??????
        view.landSearchincrprLnmText?.text =
            checkStringNull(landInfoDataJson!!.getString("incrprLnm"))
        // ??????
        view.landSearchNominationText?.text =
            checkStringNull(landInfoDataJson!!.getString("gobuLndcgrNm"))
        // ?????? ??????
        val relateLnmString = checkStringNull(landInfoDataJson!!.getString("relateLnm"))
//        if(relateLnmString.isEmpty()) {
//            view.landSearchRelatedLnmText?.setText("??????")
//        }
//        else {
            view.landSearchRelatedLnmText?.setText(relateLnmString)
//        }
        // ?????? ??????
        view.landSearchBgnnArText?.text = checkStringNull(landInfoDataJson!!.getString("bgnnAr"))
        // ?????? ??????
        view.landSearchIncrprArText?.text =
            checkStringNull(landInfoDataJson!!.getString("incrprAr"))

        // TODO : ????????? ???????????? ??????
        setRestSearchLayout(dataJson)

//        RestLandInfoObject.landSearchRealLngrJsonArray =
//            dataJson.getJSONArray("realLandInfo") as JSONArray
//
//        RestLandInfoObject.searchRealLand =
//            RestLandInfoObject.landSearchRealLngrJsonArray
//
//        if (RestLandInfoObject.landSearchRealLngrJsonArray!!.getJSONObject(0)
//                .getString("realLndcgrNm").equals("NoData")
//        ) {
//            logUtil.d("real land json no data")
//        }


//        landSearchaNrfrstAtChk = view.landSearchaNrfrstAtChk
//        landSearchClvtAtChk = view.landSearchClvtAtChk
//        landSearchBuildAtChk = view.landSearchBuildAtChk
//        landSearchPlotAtChk = view.landSearchPlotAtChk

        // ????????? ????????? ??????
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

    fun setRestSearchLayout(data: JSONObject) {

        var restData = JSONArray()

        if(!data.isNull("restLad")) {
            restData = data.getJSONArray("restLad")
        }


        val landSearchItem = arrayOf(
            landSearchItem01Layout,
            landSearchItem02Layout,
            landSearchItem03Layout,
            landSearchItem04Layout,
            landSearchItem05Layout,
            landSearchItem06Layout,
            landSearchItem07Layout,
            landSearchItem08Layout,
            landSearchItem09Layout,
            landSearchItem10Layout,
        )

        for (idx in 0..(landSearchItem.size - 1)) {
            val item = landSearchItem.get(idx)
            item.restLandSearchItemDesc1.text = resources.getStringArray(R.array.restLandQuestArr).get(idx)

            // EditText??? inputType??? textMultiLine?????? imeOptions ?????? ???????????? ?????????
            item.restLandSearchItemEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
            item.restLandSearchItemEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            item.restLandSearchItemEditText.hint = resources.getStringArray(R.array.restLandQeustArrHint).get(idx)
        }

        // EditText??? inputType??? textMultiLine?????? imeOptions ?????? ???????????? ?????????
        landSearchItemCause.restLandSearchItemEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        landSearchItemCause.restLandSearchItemEditText.imeOptions = EditorInfo.IME_ACTION_DONE

        landSearchItem01Layout.restLandSearchItemEditText.setOnEditorActionListener { textView, action, _ ->

            if (action == EditorInfo.IME_ACTION_DONE) {

            }
            false
        }

        wtnncUtill.wtnncSpinnerAdapter(R.array.restThingRewardChkArray, landSearchResultAtChk, this) // ?????????????????? ??????

        // TODO : ?????? ????????? ??????
        if(restData.length() > 0) {
            bqestPsnText.setText(checkStringNull(restData.getJSONObject(0).getString("rqestPsn")))
            bqestCnText.setText(checkStringNull(restData.getJSONObject(0).getString("rqestCn")))
            landSearchItem01Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin1Rslt")))
            landSearchItem02Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin2Rslt")))
            landSearchItem03Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin3Rslt")))
            landSearchItem04Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin4Rslt")))
            landSearchItem05Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin5Rslt")))
            landSearchItem06Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin6Rslt")))
            landSearchItem07Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin7Rslt")))
            landSearchItem08Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin8Rslt")))
            landSearchItem09Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin9Rslt")))
            landSearchItem10Layout.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("examin10Rslt")))
            val rewdAtString = checkStringNull(restData.getJSONObject(0).getString("rewdAt"))

            landSearchResultAtChk.setSelection(when (rewdAtString) {
                "Y" -> 1
                "N" -> 2
                else ->0
            })
            landSearchItemCause.restLandSearchItemEditText.setText(checkStringNull(restData.getJSONObject(0).getString("resn")))
        } else {
            landSearchResultAtChk.setSelection(0)
        }




        
    }

//    /**
//     * ?????? ??????????????????
//     */
//    fun landRealAdd() {
//        logUtil.d("?????????????????? ??????!!!!")
//
//        val lndData = RestLandInfoObject.landInfo as JSONObject
//
//
//        val realLndcgr = JSONObject()
//        realLndcgr.put("realLndcgrAr", RestLandInfoObject.currentArea)
//        realLndcgr.put("realLndcgrCl", "")
//        realLndcgr.put("realLndcgrCn", "")
//        realLndcgr.put("realLndcgrCode","")
//        realLndcgr.put("saupCode",lndData.getString("saupCode"))
//        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))
//
//        //val jArrLength = RestLandInfoObject.landSearchRealLngrJsonArray?.length()!!
//        //logUtil.d(jArrLength.toString())
//
//        RestLandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
//        RestLandInfoObject.searchRealLand = RestLandInfoObject.landSearchRealLngrJsonArray
//        RestLandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
//    }

    /**
     * ?????? ?????????????????? (??????)
     */
//    fun landRealAddAll() {
//        logUtil.d("?????????????????? ?????? ?????? ??????!!!!")
//
//        val lndData = RestLandInfoObject.landInfo as JSONObject
//
//        val realLndcgr = JSONObject()
//        realLndcgr.put("realLndcgrAr", RestLandInfoObject.currentArea)
//        realLndcgr.put("realLndcgrCl", "")
//        realLndcgr.put("realLndcgrCn", "")
//        realLndcgr.put("realLndcgrCode","")
//        realLndcgr.put("saupCode",lndData.getString("saupCode"))
//        realLndcgr.put("ladWtnCode",lndData.getString("ladWtnCode"))
//
//
//        //val jArrLength = RestLandInfoObject.landSearchRealLngrJsonArray?.length()!!
//        //logUtil.d(jArrLength.toString())
//
//        RestLandInfoObject.landSearchRealLngrJsonArray?.put(realLndcgr)
//        RestLandInfoObject.searchRealLand = RestLandInfoObject.landSearchRealLngrJsonArray
//        RestLandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
//    }

    /**
     * ?????? ???????????? ?????? ??????
     */
//    fun landRealUpdate() {
//        logUtil.d("?????????????????? ??????!!!!")
//
//        val jArray = RestLandInfoObject.landSearchRealLngrJsonArray
//
//        for (i in 0 until jArray?.length()!!) {
//            logUtil.d(jArray.get(i).toString())
//
//            logUtil.d(
//                "${RestLandInfoObject.currentArea.toString()}, ${
//                    (jArray.get(i) as JSONObject).get(
//                        "realLndcgrAr"
//                    )
//                }"
//            )
//            logUtil.d(
//                "${RestLandInfoObject.selectPolygonCurrentArea.toString()}, ${
//                    (jArray.get(i) as JSONObject).get(
//                        "realLndcgrAr"
//                    )
//                }"
//            )
//
//            when {
//                jArray.length() == 2 -> {
//                    (RestLandInfoObject.landSearchRealLngrJsonArray?.get(1) as JSONObject).put(
//                        "realLndcgrAr",
//                        RestLandInfoObject.currentArea
//                    )
//                }
//                else -> {
//                    if (RestLandInfoObject.selectPolygonCenterTxt == (RestLandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).get("realLndcgrAr").toString()) {
//                        logUtil.d("data ??????")
//                        (RestLandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).put(
//                            "realLndcgrAr",
//                            RestLandInfoObject.currentArea
//                        )
//                    }
//                }
//            }
//
//
//            // loop??? ????????? ????????? ????????? ??????????????? ????????? ??????
////            if(RestLandInfoObject.selectPolygonCurrentArea.toString() == (RestLandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).get("realLndcgrAr").toString()){
////                logUtil.d("data ??????")
////                (RestLandInfoObject.landSearchRealLngrJsonArray?.get(i) as JSONObject).put("realLndcgrAr", RestLandInfoObject.currentArea)
////            }
//        }
//
//        RestLandInfoObject.landSearchRealLngrAdpater?.notifyDataSetChanged()
//    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id) {
            R.id.landSearchResultAtChk -> {
                when (position) {
//                    0 -> RestLandInfoObject.spclLadCl = ""
//                    1 -> RestLandInfoObject.spclLadCl = "A026001"
//                    2 -> RestLandInfoObject.spclLadCl = "A026002"
                }
            }

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun addLandRestData() {

        RestLandInfoObject.rqestPsn = activity!!.bqestPsnText.text.toString()

        RestLandInfoObject.rqestCn = activity!!.bqestCnText.text.toString()

        RestLandInfoObject.examin1Rslt = activity!!.landSearchItem01Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin2Rslt = activity!!.landSearchItem02Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin3Rslt = activity!!.landSearchItem03Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin4Rslt = activity!!.landSearchItem04Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin5Rslt = activity!!.landSearchItem05Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin6Rslt = activity!!.landSearchItem06Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin7Rslt = activity!!.landSearchItem07Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin8Rslt = activity!!.landSearchItem08Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin9Rslt = activity!!.landSearchItem09Layout.restLandSearchItemEditText.text.toString()
        RestLandInfoObject.examin10Rslt = activity!!.landSearchItem10Layout.restLandSearchItemEditText.text.toString()



        RestLandInfoObject.rewdAt = when(activity!!.landSearchResultAtChk.selectedItemPosition) {
            1 -> "Y"
            2 -> "N"
            else -> ""
        }

        RestLandInfoObject.resn = activity!!.landSearchItemCause.restLandSearchItemEditText.text.toString()

    }
}
