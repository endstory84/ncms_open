/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_document_edit.*
import kotlinx.android.synthetic.main.fragment_document_edit.view.*
import kr.or.kreb.ncms.mobile.CameraActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.data.*
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths

private const val IMAGE_FILE_BITMAP ="imageFileByteArray"
private const val LON = "lon"
private const val LAT = "lat"
private const val AZIMUTH = "azimuth"
private const val SAUP_CODE = "saupCode"
private const val BIZ_CODE = "bizCode"
private const val FILE_CODE = "fileCode"
private const val FILE_CODE_NM = "fileCodeNm"

class DocumentEditViewFragment : DialogFragment(), View.OnClickListener {

    val logUtil:LogUtil = LogUtil(DocumentEditViewFragment::class.java.simpleName)

    var listener: EditViewListener? = null
    private var imageFile: String? = null
    private var lon: String? = null
    private var lat: String? = null
    private var azimuth: String? = null
    private var saupCode: String? = null
    private var bizCode: String? = null
    private var fileCode: String? = null
    private var fileCodeNm: String? = null

    private lateinit var getCropBitmap: Bitmap
    private lateinit var resultBitmap: Bitmap
    private var setImageProcessBitmap: Bitmap? = null

    var dialogUtil: DialogUtil? = null

    private var progressDialog: AlertDialog? = null

    private var rotate: Float = 0f

    interface EditViewListener { fun onEditPreview(bitmap: Bitmap) }
    fun setEditViewListener(listener: EditViewListener) { this.listener = listener }

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
        }

        dialogUtil = DialogUtil(context, activity)

        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        val getByteArr = arguments?.getByteArray(IMAGE_FILE_BITMAP)
        getCropBitmap = BitmapFactory.decodeByteArray(getByteArr, 0, getByteArr!!.size)
        resultBitmap = getCropBitmap


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewEnhance.setImageBitmap(getCropBitmap)

        if(fileCode.equals("A200006999")) {
            view.documentBigoLayout.goneView()
        } else {
            view.documentBigoLayout.visibleView()
        }
        initializeElement()
    }

    private fun initializeElement() {
        btnImageRotateLeft.setOnClickListener(this)
        btnImageRotateRight.setOnClickListener(this)
        btnImageToOriginal.setOnClickListener(this)
        btnImageToMagicColor.setOnClickListener(this)
        btnImageToGray.setOnClickListener(this)
        btnImageToBW.setOnClickListener(this)
        btnCropViewSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnImageRotateLeft -> {
                rotate += 90f
                resultBitmap = if (setImageProcessBitmap !== null) {
                    ImageUtil.rotateBitmap(setImageProcessBitmap!!, rotate)
                } else {
                    ImageUtil.rotateBitmap(getCropBitmap, rotate)
                }
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnImageRotateRight -> {
                rotate -= -90f
                resultBitmap = if (setImageProcessBitmap !== null) {
                    ImageUtil.rotateBitmap(setImageProcessBitmap!!, rotate)
                } else {
                    ImageUtil.rotateBitmap(getCropBitmap, rotate)
                }
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnImageToOriginal -> {
                setImageProcessBitmap = null
                resultBitmap = ImageUtil.rotateBitmap(getCropBitmap, rotate)
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnImageToMagicColor -> {
                setImageProcessBitmap = ImageUtil.matToBitmap(ImageUtil.setMagicColorBitmap(getCropBitmap))
                resultBitmap = ImageUtil.rotateBitmap(setImageProcessBitmap!!, rotate)
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnImageToBW -> {
                setImageProcessBitmap = ImageUtil.matToBitmap(ImageUtil.setBWBitmap(getCropBitmap))
                resultBitmap = ImageUtil.rotateBitmap(setImageProcessBitmap!!, rotate)
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnImageToGray -> {
                setImageProcessBitmap = ImageUtil.matToBitmap(ImageUtil.setGrayScaleBitmap(getCropBitmap))
                resultBitmap = ImageUtil.rotateBitmap(setImageProcessBitmap!!, rotate)
                imageViewEnhance.setImageBitmap(resultBitmap)
            }
            R.id.btnCropViewSave -> {
                logUtil.d("Doc 편집뷰 -> 저장")

                val atchRequestMap = HashMap<String, String>()
                val atchRequestUrl: String?
                val saveImageFile: File?

                val downloadDirectory = Paths.get((context as CameraActivity).downloadRootPath, "RAB").toString()

                progressDialog?.show()

                val searchInfoJson = when(bizCode) {
                    "LAD" -> LandInfoObject.landInfo
                    "THING" -> ThingWtnObject.thingInfo
                    "BSN" -> ThingBsnObject.thingInfo
                    "TOMB" -> ThingTombObject.thingInfo
                    "FARM" -> ThingFarmObject.thingInfo
                    "RESIDNT" -> ThingResidntObject.thingInfo
                    "FYHTS" -> ThingFyhtsObject.thingInfo
                    else -> {}
                } as JSONObject

                val legaldongNm = searchInfoJson.getString("legaldongNm")
                val incrprLnm = searchInfoJson.getString("incrprLnm")
                val legaldongNmSplit = legaldongNm.split(" ").last()
                val saveFileName = "${fileCodeNm}_$legaldongNmSplit$incrprLnm"
                val fileNameString = "$downloadDirectory/$saveFileName.png"

                FileUtil.run {
                    createDir(downloadDirectory) // 디렉토리 내 폴더가 없을시에 생성 시켜줌
                    saveBitmapToFileCache(resultBitmap, fileNameString) // Bitmap -> OutStream 생성
                }

                saveImageFile = File(fileNameString)

                val rmTxt = documentRmEdit.text.toString()

                if(bizCode.equals("LAD")) {
                    atchRequestUrl = context!!.resources.getString(R.string.mobile_url) + "ladSearchAtchFileUpload"
                    atchRequestMap["ladWtnCode"] = searchInfoJson.getString("ladWtnCode")
                    atchRequestMap["thingWtnCode"] = ""
                } else {
                    atchRequestUrl = context!!.resources.getString(R.string.mobile_url) + "thingSearchAtchFileUpload"
                    atchRequestMap["ladWtnCode"] = ""
                    atchRequestMap["thingWtnCode"] = searchInfoJson.getString("thingWtnCode")

                }

                atchRequestMap["saupCode"] = saupCode!!
                atchRequestMap["rm"] = rmTxt
                atchRequestMap["atflNm"] = "$saveFileName.png"
                atchRequestMap["fileseInfo"] = fileCode!!
                atchRequestMap["fileCodeNm"] = fileCodeNm!!
                atchRequestMap["register"] = PreferenceUtil.getString(context!!, "id", "defaual")
                atchRequestMap["atflSize"] = ""
                atchRequestMap["atflExtsn"] = ".png"
                atchRequestMap["lon"] = lon!!
                atchRequestMap["lat"] = lat!!
                atchRequestMap["azimuth"] = azimuth!!
                atchRequestMap["mulit"] = "N"

                if(fileCode.equals("A200006999")) {

                    Constants.GLOBAL_DELVY_FRAGMENT?.onEditPreview(resultBitmap)

                    dialog?.dismiss()
                    activity?.onBackPressed()

                } else {
                    HttpUtil.getInstance(context!!)
                         .callerUrlInfoPostFileUpload(atchRequestMap, progressDialog,
                             atchRequestUrl, saveImageFile,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    progressDialog?.dismiss()
                                    logUtil.e("fail")
                                }

                                override fun onResponse(call: Call, response: Response) {

                                    val responseString = response.body!!.string()
                                    logUtil.d("responseString>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>$responseString")

                                    activity?.runOnUiThread {
                                        progressDialog?.dismiss()

                                        dialog?.dismiss()
                                        activity?.onBackPressed()
                                    }

                                }

                            })
                }


//                dialog?.dismiss()
//                activity?.onBackPressed()


            }
            R.id.btnCropViewCancel -> {
                dialog?.dismiss()
                activity?.onBackPressed()
            }
        }
    }

    companion object {

        // cpp data load
        init {
            System.loadLibrary("opencv_java4")
        }

        @JvmStatic
        fun newInstance(
            imageFile: ByteArray,
            lon: String?,
            lat: String?,
            azimuth: String?,
            saupCode: String?,
            bizCode: String?,
            fileCode: String?,
            fileCodeNm: String?
        ) =
            DocumentEditViewFragment().apply {
                arguments = Bundle().apply {
                    putByteArray(IMAGE_FILE_BITMAP, imageFile)
                    putString(LON, lon)
                    putString(LAT, lat)
                    putString(AZIMUTH, azimuth)
                    putString(SAUP_CODE, saupCode)
                    putString(BIZ_CODE, bizCode)
                    putString(FILE_CODE, fileCode)
                    putString(FILE_CODE_NM, fileCodeNm)
                }
            }
    }
}