
/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_land_search_item.view.*
import kotlinx.android.synthetic.main.fragment_land_search_item_footer.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.LandInfoObject
import kr.or.kreb.ncms.mobile.util.DialogUtil
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.convertWGS84
import kr.or.kreb.ncms.mobile.util.ToastUtil
import org.json.JSONArray
import org.json.JSONObject


class LandSearchRealngrAdapter(
    private var realLandJson: JSONArray,
    private val context: Context?,
    private val activity: Activity?,
    private val dcsnAt: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TYPE_ITEM = 1
    private val TYPE_FOOTER = 2

    private fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

    inner class LoanViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var logUtil: LogUtil = LogUtil("LandSearchRealngrAdapter")
    private var toast = ToastUtil(context!!)

    //var dialogUtil = (activity as MapActivity).naverMap?.dialogUtil
    //var dialogBuilder = (activity as MapActivity).naverMap?.dialogBuilder

    var dialogUtil: DialogUtil? = null
    var dialogBuilder: MaterialAlertDialogBuilder? = null

    //var dialogBuilder = activity.naverMap?.dialogBuilder

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    init {
        dialogUtil = getActivity().naverMap?.dialogUtil!!
        dialogBuilder = getActivity().naverMap?.dialogBuilder!!
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(parent.inflate(R.layout.fragment_land_search_item_footer))
            else -> LoanViewHolder2(parent.inflate(R.layout.fragment_land_search_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is LoanViewHolder2 -> {
                holder.itemView.apply {

                    val curPos :Int = position

                    /*????????????*/
                    btn_landSearchEdit.setOnClickListener {

                        try {

                            LandInfoObject.landRealArCurPos = curPos
                            logUtil.d("?????????????????? [??????] [$curPos]")

                            LandInfoObject.isEditable = true

                            for (i in 0..LandInfoObject.searchRealLand?.length()!!) {
                                if (curPos == i) { //

                                    val editPolygonGeom = (LandInfoObject.searchRealLand?.get(i) as JSONObject).get("geoms").toString()
                                    getActivity().settingCartoMap(null, null)

                                    setEditLatLngArr(editPolygonGeom).let { ar ->
                                        getActivity().cartoMap?.modifyLandAr(ar)
                                    }

                                    break
                                }
                            }


                        } catch (e: Exception) {
                            logUtil.e(e.toString())
                        }

                    }

                    // ????????????
                    btn_landSearchRemove.setOnClickListener {
                        try {

                            LandInfoObject.landRealArCurPos = curPos
                            logUtil.d("?????????????????? [??????] : [$curPos]")

                            dialogUtil?.run {
                                alertDialogYesNo(
                                    "?????????????????? ??????",
                                    "????????? ?????? ????????????????????? ?????????????????????????",
                                    dialogBuilder!!,
                                    "?????????????????? ??????"
                                ).show()
                            }
                        } catch (e: Exception) {
                            logUtil.e(e.toString())
                        }

                    }

                    logUtil.d("RealngrAdapter data $realLandJson")
                    val realLandData = realLandJson.get(position) as JSONObject
                    val realLndcgrAr = realLandData.getString("realLndcgrAr")
                    val realLndcgrCl = realLandData.getString("realLndcgrCl")
                    val realLndcgrCn = realLandData.getString("realLndcgrCn")

//                    landSearchRealCl.setText(realLndcgrCl)
                    landSearchRealCn.setText(realLndcgrCn)
                    landSearchRealAr.setText(realLndcgrAr)

                    /*???????????? ???????????? Spinner Adapter*/
                    landSpinnerAdapter(R.array.landUseRealtyUseSttusArray, landSearchRealCl)

                    if(!realLndcgrCl.equals("")) {
                        when {
                            realLndcgrCl.isNotEmpty() -> {
                                val realLndcgrSelect = realLndcgrCl.substring(5,7)
                                landSearchRealCl.setSelection(Integer.valueOf(realLndcgrSelect))
                            }
                        }
                    }

                    if(dcsnAt == "Y") {
                        landSearchRealCl.isEnabled = false
                        landSearchRealCn.isEnabled = false
                        landSearchRealAr.isEnabled = false
                        btn_landSearchRemove.isEnabled = false
                    }

                    landSearchRealCl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, spinnerPosition: Int, id: Long) {

                            logUtil.d("landSearchRealCl Spinner click $spinnerPosition")
                            logUtil.d("real Land position $position")

                            val realData = LandInfoObject.searchRealLand
                            logUtil.d("real Data ---"+ realData?.get(position).toString())

                            val realDataPostion = realData?.get(position) as JSONObject
                            val dataPosition: String = when (spinnerPosition) {
                                0 -> "A021001"
                                1 -> "A021001" /*???*/
                                2 -> "A021002" /* ???*/
                                3 -> "A021003" /*?????????*/
                                4 -> "A021004" /*????????????*/
                                5 -> "A021005" /*??????*/
                                6 -> "A021006" /*?????????*/
                                7 -> "A021007" /*??????*/
                                8 -> "A021008" /*???*/
                                9 -> "A021009" /*????????????*/
                                10 -> "A021010" /*????????????*/
                                11 -> "A021011" /*?????????*/
                                12 -> "A021012" /*???????????????*/
                                13 -> "A021013" /*????????????*/
                                14 -> "A021014" /*??????*/
                                15 -> "A021015" /*??????*/
                                16 -> "A021016" /*??????*/
                                17 -> "A021017" /*??????*/
                                18 -> "A021018" /*??????*/
                                19 -> "A021019" /*?????????*/
                                20 -> "A021020" /*????????????*/
                                21 -> "A021021" /*??????*/
                                22 -> "A021022" /*????????????*/
                                23 -> "A021023" /*?????????*/
                                24 -> "A021024" /*????????????*/
                                25 -> "A021025" /*?????????*/
                                26 -> "A021026" /*??????*/
                                27 -> "A021027" /*?????????*/
                                28 -> "A021028" /*????????????*/
                                29 -> "A021035" /*????????????*/
                                else -> "A021001"
                            }

                            realDataPostion.put("realLndcgrCl", dataPosition)
                            realData.put(position, realDataPostion)

                            logUtil.d("real Data ---"+ realData.get(position).toString())
                            LandInfoObject.searchRealLand = realData
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                    }

                    landSearchRealCn.setOnEditorActionListener { textView, action, _ ->

                        if(action == EditorInfo.IME_ACTION_DONE) {
                            logUtil.d("???????????? ????????? ??????")
                            val txtString = textView.text.toString()

                            logUtil.d("text view String $txtString")
                            logUtil.d("real Land position $position")

                            val realData = LandInfoObject.searchRealLand
                            logUtil.d("real Data ---"+ realData?.get(position).toString())


                            val realDataPostion = realData?.get(position) as JSONObject
                            realDataPostion.put("realLndcgrCn",txtString )
                            realData.put(position, realDataPostion)

                            logUtil.d("real Data ---"+ realData.get(position).toString())
                            LandInfoObject.searchRealLand = realData
                        }
                        false
                    }
                    landSearchRealAr.setOnEditorActionListener { textView, action, _ ->

                        if(action == EditorInfo.IME_ACTION_DONE) {
                            logUtil.d("???????????? ????????? ??????")

                            var txtString = textView.text.toString()
                            txtString = txtString.replace("???","")
                            logUtil.d("text view String $txtString")
                            logUtil.d("real Land position $position")

                            val realData = LandInfoObject.searchRealLand
                            logUtil.d("real Data ---"+ realData?.get(position).toString())


                            val realDataPostion = realData?.get(position) as JSONObject
                            realDataPostion.put("realLndcgrAr",txtString )
                            realData.put(position, realDataPostion)
                            logUtil.d("real Data ---"+ realData.get(position).toString())

                            LandInfoObject.searchRealLand = realData
                        }
                        false
                    }
                }

            }
            else -> {

                if(realLandJson.length() == 1) {
                    logUtil.d("realData null")

                    dialogUtil?.run {
                        alertDialogYesNo(
                            "???????????? ????????????",
                            "?????? ?????? ????????? ?????? ???????????? ??????????????? ????????? ?????????????",
                            dialogBuilder!!,
                            "?????????????????? ??????"
                        ).show()
                    }
                }

                // ?????????????????? ?????? -> CartoMap ??????
                holder.itemView.landRealAddBtn.setOnClickListener {
                    logUtil.d("?????????????????? ?????? ?????? ??????")
                    if(dcsnAt == "Y") {
                        toast.msg_error(R.string.msg_search_dcsc_at_resut, 100)
                    } else {
                        LandInfoObject.isEditable = false

                        (activity as MapActivity).settingCartoMap(null, null) // ?????? ????????? ??????
                    }


                    //Toast.makeText(holder.itemView.context,"???????????? ??????????????? ??????????????????", Toast.LENGTH_SHORT).show()
                    //clickListener?.onClick(realListener)
                }
            }
        }
    }

//    override fun onPositiveClickListener(dialog: DialogInterface) {
//        logUtil.d("LandSearchAdpater -> y")
//        //toastUtil.msg_info(R.string.landSearchRealChange, 1000)
//    }
//
//    override fun onNegativeClickListener(dialog: DialogInterface) {
//        logUtil.d("LandSearchAdpater -> n")
//        //toastUtil.msg_info(R.string.landSearchRealNotChange, 200)
//    }

    override fun getItemCount(): Int {
        return realLandJson.length()
        //return 1
    }

    override fun getItemViewType(position: Int): Int {
        if (realLandJson.length() == 1) {
            return TYPE_FOOTER
        }
        return when (position) {
            0 -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    private fun getActivity(): MapActivity = (activity as MapActivity)

    private fun setEditLatLngArr(str: String): MutableList<LatLng>{

        val resultLatLngArr = mutableListOf<LatLng>()
        val convertStr = str.replace("MULTIPOLYGON (((", "").replace(")))", "").split(",")

        convertStr.forEach {

            val x = it.trim().split(" ")[0].toDouble()
            val y = it.trim().split(" ")[1].toDouble()

            val coordX = convertWGS84(x, y).y
            val coordY = convertWGS84(x, y).x

            val latLng = LatLng(coordX, coordY)
            resultLatLngArr.add(latLng)

            logUtil.d(latLng.toString())

        }
        return resultLatLngArr
    }

    private fun landSpinnerAdapter(stringArray: Int, spinner: Spinner) {
        spinner.adapter = CustomDropDownAdapter(context!!, listOf(context.resources.getStringArray(stringArray))[0])
    }
}