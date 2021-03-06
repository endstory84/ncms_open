/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_land_info.*
import kotlinx.android.synthetic.main.fragment_land_info.view.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.LandInfoObject
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

class LandInfoFragment(context: Context) : Fragment() {

    private var toastUtil: ToastUtil = ToastUtil(context)
    private var logUtil: LogUtil = LogUtil("LandInfoFragment")
    private var landAtchInfo: JSONArray? = null

    lateinit var materialDialog: Dialog
    var dialogUtil: DialogUtil? = null
    private var progressDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_land_info, null)
        dialogUtil = DialogUtil(context, activity)
        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        init(view)
        return view
    }

    fun init(view: View) {

//        var dataString = ""

//        when (Constants.BIZ_SUBCATEGORY_KEY) {
//            BizEnum.LAD -> {
//                dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String
//            }
//            BizEnum.REST_LAD -> {
//                dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String
//            }
//        }

        val dataString = requireActivity().intent!!.extras!!.get("LandInfo") as String
        logUtil.d("LandInfo String ---------------------> $dataString")

        val dataJson = JSONObject(dataString)
        logUtil.d("LandInfo json --------------------> $dataJson")

        val landInfoJson = dataJson.getJSONObject("LandInfo")
        LandInfoObject.landInfo = landInfoJson


        val landNo = checkStringNull(landInfoJson.getString("no"))
        val landSubNo = checkStringNull(landInfoJson.getString("subNo"))

        view.landInfoNoText?.text = "$landNo-$landSubNo"
        view.landInfoLocationText?.text = checkStringNull(landInfoJson.getString("legaldongNm"))
        val dcsnAtString = checkStringNull(landInfoJson.getString("dcsnAt"))
        view.landInfoDcsnAtText?.text = dcsnAtString
        view.landInfoBgnnLnmText?.text = checkStringNull(landInfoJson.getString("bgnnLnm"))
        view.landInfoincrprLnmText?.text = checkStringNull(landInfoJson.getString("incrprLnm"))
        view.landInfoNominationText?.text = landInfoJson.getString("gobuLndcgrNm")
        val relateLnmString = checkStringNull(landInfoJson.getString("relateLnm"))
        if(relateLnmString == "") {
            view.landInfoRelatedLnmText?.text = "??????"
        } else {
            view.landInfoRelatedLnmText?.text = relateLnmString
        }

        view.landInfoBgnnArText?.text = checkStringNull(landInfoJson.getString("bgnnAr"))
        view.landInfoIncrprArText?.text = checkStringNull(landInfoJson.getString("incrprAr"))
        view.landInfoSpfcText?.text = checkStringNull(landInfoJson.getString("spfc"))
        view.landInfoOwnersText?.text = checkStringNull(landInfoJson.getString("ownerName"))
        view.lanfInfoRelationText?.text = checkStringNull(landInfoJson.getString("relatesName"))


        landAtchInfo = dataJson.getJSONArray("landAtchInfo")
        for(i in 0 until landAtchInfo!!.length()) {
            val landAtchItem = landAtchInfo!!.getJSONObject(i)

            val landFileInfo = landAtchItem.getString("fileseInfo")
            when(landFileInfo) {
                "A200006001" -> {
                    view.landInfoLedgerBtn.backgroundTintList = when (landFileInfo) {
                        "A200006001" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }
                }
                "A200006002" -> {
                    view.landInfoRegistMap.backgroundTintList = when (landFileInfo) {
                        "A200006002" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }
                }
                "A200006003" -> {
                    view.landInfoUsePlanBtn.backgroundTintList = when (landFileInfo) {
                        "A200006003" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }

                }
                "A200006004" -> {

                    view.landInfoRegisteredBtn.backgroundTintList = when (landFileInfo) {
                        "A200006004" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }

                }
                "A200006013" -> {
                    view.landInfoPreviousCompensateBtn.backgroundTintList = when (landFileInfo) {
                        "A200006013" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }

                }
                "A200006014" -> {
                    view.landInfoFeedChannelBtn.backgroundTintList = when (landFileInfo) {
                        "A200006014" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }
                }
                "A200006015" -> {
                    view.landInfoSurveyResultMap.backgroundTintList = when (landFileInfo) {
                        "A200006015" -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.aqua))
                        else -> ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.btnPopup))
                    }
                }

            }

        }


        if(dcsnAtString == "Y") {
            toastUtil.msg_info(R.string.searchDcsnAtLand,1000)
        }

    }

    //Activity?????? ????????? Fragment??? ????????? ???????????? ??????
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /**
         * ?????? ?????? Popup Button
         *
         * ?????? ?????? ???????????? ??? ?????? ??????
         */


        landInfoPreviousCompensateBtn.setOnClickListener {
            //A200006013
            WtnccDocViewFragment(null).show(requireActivity().supportFragmentManager, "docViewFragment")
        }

        landInfoLedgerBtn.setOnClickListener {
            //A200006001 ?????? ??????
            callLandFileFilter("A200006001")
        }
        landInfoRegistMap.setOnClickListener {
            //A200006002
            callLandFileFilter("A200006002")
        }

        landInfoRegisteredBtn.setOnClickListener {
            //A200006004
            callLandFileFilter("A200006004")
        }

        landInfoUsePlanBtn.setOnClickListener {
            //A200006003
            callLandFileFilter("A200006003")
        }
        landInfoPreviousCompensateBtn.setOnClickListener {
            callLandFileFilter("A200006013")
        }

        landInfoFeedChannelBtn.setOnClickListener {
            //A200006014
            callLandFileFilter("A200006014")
        }

        landInfoSurveyResultMap.setOnClickListener {
            //A200006015
            callLandFileFilter("A200006015")
        }

    }

    fun checkStringNull(nullString: String) : String{
        return if(nullString == "null") " " else { nullString }
    }

    fun callLandFileFilter(fileCode: String) {
        val landFileNm = when (fileCode) {
            "A200006001" -> "????????????"
            "A200006002" -> "?????????"
            "A200006003" -> "???????????????????????????"
            "A200006004" -> "???????????????"
            "A200006013" -> "??????_????????????"
            "A200006014" -> "???????????????"
            "A200006015" -> "???????????????"
            else -> ""
        }
        val array = mutableListOf<String>()
        var checkedItem = 0
        val landInfoLedgerArray = JSONArray()
        array.add("$landFileNm ??????")
        for(i in 0 until landAtchInfo!!.length()) {
            val landAtchItem = landAtchInfo!!.getJSONObject(i)

            val landFileInfo = landAtchItem.getString("fileseInfo")
            if(landFileInfo.equals(fileCode)) {
                logUtil.d("$fileCode-------------------------------------")
                array.add(landAtchItem.getString("rgsde"))
                landInfoLedgerArray.put(landAtchItem)
            }

        }
        materialDialog = MaterialAlertDialogBuilder(context!!)
            .setTitle("$landFileNm ??????")
            .setPositiveButton("??????") {_, _ ->
                logUtil.d("setPositiveButton------------------------------> ")
                if(checkedItem == 0) {
                    callLandCapture(fileCode, landFileNm)
                } else {
                    val item = landInfoLedgerArray.get(checkedItem-1) as JSONObject

                    callLandFileDownload(item)
                }
            }
            .setNegativeButton("??????") {_, _ ->
                logUtil.d("setNegativeButton------------------------------>")
            }
            .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                logUtil.d("setSingleChoiceItems------------------------------> $checkedItem")
                checkedItem = which
            }
            .setCancelable(false)
            .show()
    }

    fun callLandFileDownload(item: JSONObject) {
        val landAtchFileMap = HashMap<String, String>()
        landAtchFileMap["atchCode"] = item.getString("atchCode")
        landAtchFileMap["ladWtnCode"] = item.getString("ladWtnCode")
        landAtchFileMap["saupCode"] = item.getString("saupCode")
        landAtchFileMap["atfl"] = item.getString("atfl")
        landAtchFileMap["atflNm"] = item.getString( "atflNm")
        landAtchFileMap["fileseInfo"] = item.getString("fileseInfo")
        landAtchFileMap["fileseInfoNm"] = item.getString("fileseInfoNm")
        landAtchFileMap["atflSize"] = item.getString("atflSize")

        val landAtchFileUrl = context!!.resources.getString(R.string.mobile_url) + "landFileDownload"

        HttpUtil.getInstance(context!!)
            .callerUrlInfoPostWebServer(landAtchFileMap, progressDialog, landAtchFileUrl,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        toastUtil.msg_error(R.string.msg_server_connected_fail, 100)
                    }

                    override fun onResponse(call: Call, response: Response) {

                        val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
                        val fileNameString = "$downloadDirectory/${item.getString( "atfl")}"
                        var getLandFileBitmap: Bitmap? = null

                        logUtil.d("downloadDirectory-------------------------> $downloadDirectory")

                        FileUtil.run {
                            createDir(downloadDirectory)
                            if(getExtension(fileNameString)?.contains("png") == true){
                                saveBitmapToFileCache(getLandFileBitmap!!, fileNameString)
                            }
                        }

                        if(FileUtil.getExtension(fileNameString) == "pdf"){
                            FileUtil.savePdfToFileCache(response.body?.byteStream()!!, fileNameString)
                        } else {
                            getLandFileBitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                        }



                        val downloadFile = File(fileNameString)

                        WtnccDocViewFragment(downloadFile).show(requireActivity().supportFragmentManager, "docViewFragment")
                        progressDialog?.dismiss()

                    }

                })
    }

    fun callLandCapture(fileCode: String, fileCodeNm: String) {

        nextViewCamera(
            requireActivity(),
            Constants.CAMERA_ACT,
            PreferenceUtil.getString(context!!, "saupCode", "defaual"),
            BizEnum.LAD,
            fileCode,
            fileCodeNm,
            CameraEnum.DOCUMENT
        )
    }
}