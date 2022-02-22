/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_camera_view.*
import kr.or.kreb.ncms.mobile.CameraActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.databinding.FragmentCameraViewBinding
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths


// byteArray, 좌표, 방위각
private const val IMAGE_FILE_BITMAP ="imageFileByteArray"
private const val LON = "lon"
private const val LAT = "lat"
private const val AZIMUTH = "azimuth"
private const val SAUP_CODE = "saupCode"
private const val BIZ_CODE = "bizCode"
private const val FILE_CODE = "fileCode"
private const val FILE_CODE_NM = "fileCodeNm"
private const val FILE_WTNCODE_ARR = "wtnCodeArr"

class CameraViewFragment :
    BaseDialogFragment<FragmentCameraViewBinding>(FragmentCameraViewBinding::inflate, CameraViewFragment::class.java.simpleName),
    SignFragment.PreviewListener{

    private var imageFile: String? = null
    private var lon: String? = null
    private var lat: String? = null
    private var azimuth: String? = null
    private var saupCode: String? = null
    private var bizCode: String? = null
    private var fileCode: String? = null
    private var fileCodeNm: String? = null
    private var wtnCodeArr: ArrayList<String>? = null

    var resultBitmap: Bitmap? = null
    var resultLonLat: String? = null
    var resultAzimuth: Float? = null
    var resultAzimuthStr: String? = null

    var callback : CameraImageCallback? = null

    var signFragment: SignFragment? = null

    var dialogUtil: DialogUtil? = null

    private var progressDialog: AlertDialog? = null

    interface CameraImageCallback{
        fun onSetImage(
            bitmap: Bitmap?,
            saupCode: String,
            bizCode: String?,
            rmTxt: String,
            fileNameString: String,
            fileCode:String,
            fileCodeNm:String,
            lon: String,
            lat: String,
            azimuth: String
        )
        fun onClosed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as CameraImageCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context" +"must implement CameraImageCallback"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            imageFile = it.getString(IMAGE_FILE_BITMAP)
            lon = it.getString(LON)
            lat = it.getString(LAT)
            azimuth = it.getString(AZIMUTH)
            saupCode = it.getString(SAUP_CODE)
            bizCode = it.getString(BIZ_CODE)
            fileCode = it.getString(FILE_CODE)
            fileCodeNm = it.getString(FILE_CODE_NM)
            wtnCodeArr = it.getStringArrayList(FILE_WTNCODE_ARR)
        }

        dialogUtil = DialogUtil(context, activity)

        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        val getByteArr = arguments?.getByteArray(IMAGE_FILE_BITMAP)
        resultBitmap = BitmapFactory.decodeByteArray(getByteArr, 0 , getByteArr!!.size)
//        resultLonLat = arguments?.getString(ARG_PARAM2)

        resultAzimuth  = arguments?.getString(AZIMUTH)?.toFloat()
        resultAzimuthStr = GPSUtil(context!!).directionConvertStr(resultAzimuth!!)

        signFragment = SignFragment(null, null)
        signFragment?.setPreviewListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            imageViewFragmentCameraPreview.setImageBitmap(resultBitmap)

//            textViewFragmentCameraViewLonLat.text = resultLonLat
            imageViewFragmentCameraViewAzimuth.animate().rotation(-resultAzimuth!!).setDuration(200).start()
            textViewFragmentCameraViewAzimuth.text = resultAzimuthStr

//            textViewFragmentCameraViewSignLayout.setOnClickListener {
//                logUtil.d("sign")
//                signFragment?.show((context as CameraActivity).supportFragmentManager, "signFragment")
//            }
        }

        binding.buttonCameraViewSubmit.setOnClickListener {

            logUtil.d("image 파일 저장")

            //val _isMulti = true // 멀티 카메라 여부 Flag
            val _isMulti = wtnCodeArr?.isEmpty()

            val atchRequestMap = HashMap<String, String>()
            var atchRequestUrl = ""

            val downloadDirectory = Paths.get((context as CameraActivity).downloadRootPath, "RAB").toString()
            val saveImageFile: File?
            val rmTxt = textViewFragmentCameraViewEtc.text.toString()

            val legaldongNm: String?
            val incrprLnm: String?
            var saveFileName = ""
            val searchInfoJson: JSONObject

            // 멀티 여부에 따라 변수의 값을 달리준다. (단일, 멀티 로직 분기처리)
            when {
                _isMulti == true || wtnCodeArr == null -> {

                    searchInfoJson = when (bizCode) {
                        "LAD" -> LandInfoObject.landInfo
                        "THING" -> ThingWtnObject.thingInfo
                        "BSN" -> ThingBsnObject.thingInfo
                        "TOMB" -> ThingTombObject.thingInfo
                        "FARM" -> ThingFarmObject.thingInfo
                        "RESIDNT" -> ThingResidntObject.thingInfo
                        "FYHTS" -> ThingFyhtsObject.thingInfo
                        else -> { }

                    } as JSONObject

                    legaldongNm = searchInfoJson.getString("legaldongNm")
                    incrprLnm = searchInfoJson.getString("incrprLnm")
                    val legaldongNmSplit = legaldongNm.split(" ").last()
                    saveFileName = "${fileCodeNm}_$legaldongNmSplit$incrprLnm"

                    // 현장조사 사진 key, value SET
                    if(bizCode.equals("LAD")) {
                        atchRequestUrl = "${context!!.resources.getString(R.string.mobile_url)}ladSearchAtchFileUpload"
                        atchRequestMap["ladWtnCode"] = searchInfoJson.getString("ladWtnCode")
                        atchRequestMap["thingWtnCode"] = ""
                    } else {
                        atchRequestUrl = "${context!!.resources.getString(R.string.mobile_url)}thingSearchAtchFileUpload"
                        atchRequestMap["ladWtnCode"] = ""
                        atchRequestMap["thingWtnCode"] = searchInfoJson.getString("thingWtnCode")
                    }
                    atchRequestMap["saupCode"] = saupCode!!
                    atchRequestMap["rm"] = rmTxt
                    atchRequestMap["atflNm"] = "$saveFileName.png"
                    atchRequestMap["fileseInfo"] = fileCode!!
                    atchRequestMap["fileCodeNm"] = fileCodeNm!!
                    atchRequestMap["register"] = PreferenceUtil.getString(context!!, "id", "defaual") // 등록자 임시 등록
                    atchRequestMap["atflSize"] = ""
                    atchRequestMap["atflExtsn"] = ".png"
                    atchRequestMap["lon"] = lon!!
                    atchRequestMap["lat"] = lat!!
                    atchRequestMap["azimuth"] = azimuth!!
                    atchRequestMap["mulit"] = "N"

                }

                _isMulti == false -> {

                    legaldongNm = ThingWtnObject.naverGeoAddressName!!.split(" ")[2]
                    incrprLnm = ThingWtnObject.naverGeoAddress
                    saveFileName = "${fileCodeNm}_$legaldongNm$incrprLnm"

                    // 현장조사 사진 key, value SET
                    if(bizCode.equals("LAD")) {
                        atchRequestUrl = "${context!!.resources.getString(R.string.mobile_url)}ladSearchAtchFileUpload"
                        //atchRequestMap["ladWtnCode"] = searchInfoJson.getString("ladWtnCode")
                        atchRequestMap["ladWtnCode"] = LandInfoObject.wtnCode
                        atchRequestMap["thingWtnCode"] = ""
                    } else {
                        atchRequestUrl = "${context!!.resources.getString(R.string.mobile_url)}thingSearchAtchFileUpload"
                        atchRequestMap["ladWtnCode"] = ""
                        atchRequestMap["thingWtnCode"] = wtnCodeArr!!.joinToString(separator =",")
                    }
                    atchRequestMap["saupCode"] = saupCode!!
                    atchRequestMap["rm"] = rmTxt
                    atchRequestMap["atflNm"] = "$saveFileName.png"
                    atchRequestMap["fileseInfo"] = fileCode!!
                    atchRequestMap["fileCodeNm"] = fileCodeNm!!
                    atchRequestMap["register"] = PreferenceUtil.getString(context!!, "id", "defaual") // 등록자 임시 등록
                    atchRequestMap["atflSize"] = ""
                    atchRequestMap["atflExtsn"] = ".png"
                    atchRequestMap["lon"] = lon!!
                    atchRequestMap["lat"] = lat!!
                    atchRequestMap["azimuth"] = azimuth!!
                    atchRequestMap["mulit"] = when(_isMulti){
                        true  ->  "N"
                        false ->  "Y"
                    }

                }
            }

            val fileNameString = "$downloadDirectory/$saveFileName.png"
            logUtil.d("멀티체크여부: $_isMulti, 파일디렉토리: $fileNameString")

            FileUtil.run {
                createDir(downloadDirectory) // 디렉토리 내 폴더가 없을시에 생성 시켜줌
                saveBitmapToFileCache(resultBitmap!!, fileNameString) // Bitmap -> OutStream 생성
            }

            saveImageFile = File(fileNameString)

            var newSearchYn = when(bizCode) {
                "LAD" -> "N"
                "THING" -> ThingWtnObject.thingNewSearch
                "BSN" -> ThingBsnObject.thingNewSearch
                "TOMB" -> ThingTombObject.thingNewSearch
                "FARM" -> ThingFarmObject.thingNewSearch
                "RESIDNT" -> ThingResidntObject.thingNewSearch
                "FYHTS" -> ThingFyhtsObject.thingNewSearch
                else -> {}
            }
            if(newSearchYn == "") newSearchYn = "N"

            if(newSearchYn == "N") {
                HttpUtil.getInstance(context!!)
                    .callerUrlInfoPostFileUpload(atchRequestMap, progressDialog, atchRequestUrl, saveImageFile,
                        object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                progressDialog?.dismiss()
                                logUtil.e("fail")
                            }

                            override fun onResponse(call: Call, response: Response) {

                                val responseString = response.body!!.string()
                                logUtil.d("responseString >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $responseString")

                                progressDialog?.dismiss()

                                activity?.runOnUiThread {
                                    callback?.onSetImage(resultBitmap!!, saupCode!!, bizCode, rmTxt, saveFileName, fileCode!!, fileCodeNm!!, lon!!, lat!!, azimuth!!)
                                }
                                //callback?.onSetImage(resultBitmap)
                            }

                        }
                    )
            } else {
                val fm = activity?.supportFragmentManager
                fm?.popBackStack()
                val transaction = fm?.beginTransaction()
                transaction.also {
                    it?.remove(this)?.commit()
                }

                callback?.onSetImage(resultBitmap, saupCode!!, bizCode, rmTxt, saveFileName, fileCode!!, fileCodeNm!!, lon!!, lat!!, azimuth!!)
            }

        }

    }

    override fun onPreview(bitmap: Bitmap) {

//        tvSignPreview.goneView()
//        imageViewSignPreview.visibleView()
//
//        imageViewSignPreview.setImageBitmap(bitmap)
    }

    companion object {
        @JvmStatic
        fun newInstance(imageFile: ByteArray, lon: String?, lat: String?, azimuth: String?,
                        saupCode: String?, bizCode: String?, fileCode: String?, fileCodeNm: String?, wtnCodeArr:ArrayList<String>?) =
            CameraViewFragment().apply {
                arguments = Bundle().apply {
                    putByteArray(IMAGE_FILE_BITMAP, imageFile)
                    putString(LON, lon)
                    putString(LAT, lat)
                    putString(AZIMUTH, azimuth)
                    putString(SAUP_CODE, saupCode)
                    putString(BIZ_CODE, bizCode)
                    putString(FILE_CODE, fileCode)
                    putString(FILE_CODE_NM, fileCodeNm)
                    putStringArrayList(FILE_WTNCODE_ARR, wtnCodeArr)
                }
            }
    }

}