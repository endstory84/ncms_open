/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_delvy_addr_change.*
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.databinding.FragmentDelvyAddrChangeBinding
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class DelvyAddrChangeFragment(val bizCode: BizEnum, val ownerInfoJson: JSONObject) :
    BaseDialogFragment<FragmentDelvyAddrChangeBinding>(FragmentDelvyAddrChangeBinding::inflate, DelvyAddrChangeFragment::class.java.simpleName),
    View.OnClickListener,
    SignFragment.PreviewListener,
    DocumentEditViewFragment.EditViewListener {

    var signFragment: SignFragment = SignFragment(this, ownerInfoJson)
    var documentEditViewFragment: DocumentEditViewFragment = DocumentEditViewFragment()

    var signBitmap: Bitmap? = null
    var documentBitmap: Bitmap? = null

    var dialogUtil: DialogUtil? = null

    private var progressDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDelvyCancel.setOnClickListener(this)
        btnDelvySave.setOnClickListener(this)
        tvDelvySignGuide.setOnClickListener(this)
        tvDelvyDocImgGuide.setOnClickListener(this)

        Constants.GLOBAL_DELVY_FRAGMENT = this

        dialogUtil = DialogUtil(context, activity)

        progressDialog = dialogUtil!!.progressDialog(MaterialAlertDialogBuilder(context!!))

        val delvyZipString = checkStringNull(ownerInfoJson.getString("delvyZip"))
        val delvyAdresString = checkStringNull(ownerInfoJson.getString("delvyAdres"))
        val delvyAdresDetailString = checkStringNull(ownerInfoJson.getString("delvyAdresDetail"))

        editBeforeDelvyAddres.text = "($delvyZipString) $delvyAdresString $delvyAdresDetailString"

//        singOwnerRelateName = checkStringNull(ownerInfoJson.getString("ownerNm"))

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDelvySave -> {
                val sumStr = "${editDelvyPostNumber.text}, ${editDelvyAddr.text}, ${editDelvyAddrDetail.text}"
                toastUtil.msg("$sumStr ???????????? ??????", 500)

                changeDelvySave()
            }
            R.id.btnDelvyCancel -> dialog?.dismiss()
            // ??????
            R.id.tvDelvySignGuide -> {
                SignFragment(this, ownerInfoJson)?.show((context as MapActivity).supportFragmentManager, "signFragment")
                signFragment.setPreviewListener(this)
            }
            // ??????????????? ??? ?????? ??????
            R.id.tvDelvyDocImgGuide -> {
                (context as MapActivity).callerContextDocumentCamera()
                documentEditViewFragment.setEditViewListener(this)
            }
        }
    }

    override fun onPreview(bitmap: Bitmap) {
        logUtil.d("?????????????????? (??????) ????????? ??????")

        ivDelvySignPreview.visibleView()
        tvDelvySignGuide.text = ""

        signBitmap = bitmap

        ivDelvySignPreview.setImageBitmap(bitmap)
    }

    override fun onEditPreview(bitmap: Bitmap) {
        logUtil.d("?????????????????? (??????) ????????? ??????")

        try{
            ivDelvyDocImgPreview.visibleView()
            tvDelvyDocImgGuide.text = ""

            documentBitmap = bitmap
            ivDelvyDocImgPreview.setImageBitmap(bitmap)

        }catch (e:Exception){
            logUtil.d(e.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.65F, 0.6F)
    }

    fun changeDelvySave() {
        val delvyChangeMap = HashMap<String, String>()
        var delvyChangeUrl: String? = null

        if(editDelvyPostNumber.text.equals("")) {
            toastUtil.msg_error("?????? ?????? ???????????? ?????? ????????? ?????? ??? ????????? ????????????",500)
        } else if(editDelvyAddr.text.equals("")) {
            toastUtil.msg_error("?????? ?????? ???????????? ????????? ?????? ??? ????????? ????????????",500)
        } else if(editDelvyAddrDetail.text.equals("")) {
            toastUtil.msg_error("?????? ?????? ???????????? ??????????????? ?????? ??? ????????? ????????????",500)
        } else if(signBitmap == null) {
            toastUtil.msg_error("?????? ?????? ????????? ????????? ?????? ??? ????????? ????????????",500)
        } else if(documentBitmap == null) {
            toastUtil.msg_error("?????? ?????? ????????? ??????????????? ?????? ??? ????????? ????????????.",500)
        } else {


            val ownerNmString = ownerInfoJson.getString("ownerNm")
            val signFileNm = "????????????????????????_$ownerNmString.png"

            val downloadDirectory = Paths.get((context as MapActivity).downloadRootPath, "RAB").toString()
            val signFileNmPath = "$downloadDirectory/$signFileNm"

            val documentFileNm = "??????????????????????????????_$ownerNmString.png"
            val documentFileNmPath = "$downloadDirectory/$documentFileNm"

            FileUtil.run {
                createDir(downloadDirectory) // ???????????? ??? ????????? ???????????? ?????? ?????????
                saveBitmapToFileCache(signBitmap!!, signFileNmPath) // Bitmap -> OutStream ??????
                saveBitmapToFileCache(documentBitmap!!, documentFileNmPath) // Bitmap -> OutStream ??????
            }

            val saveSignFile = File(signFileNmPath)
            val saveDocumentFile = File(documentFileNmPath)

//            when(bizCode) {
//                BizEnum.LAD -> {
//                    delvyChangeUrl = context!!.resources.getString(R.string.mobile_url) + "changeDelvyLand"
//                    delvyChangeMap["saupCode"] = ownerInfoJson.getString("saupCode")
//                    delvyChangeMap["indvdlGrpCode"] = ownerInfoJson.getString("indvdlGrpCode")
//                    delvyChangeMap["indvdlGrpTy"] = ownerInfoJson.getString("indvdlGrpTy")
//                    delvyChangeMap["delvyZip"] = editDelvyPostNumber.text.toString()
//                    delvyChangeMap["delvyAddr"] = editDelvyAddr.text.toString()
//                    delvyChangeMap["delvyAddrDetail"] = editDelvyAddrDetail.text.toString()
//                    delvyChangeMap["signFileNm"] = signFileNm
//                    delvyChangeMap["signFileseInfo"] = "A200006999"
//                    delvyChangeMap["signAtflExtsn"] = ".png"
//                    delvyChangeMap["documentFileNm"] = documentFileNm
//                    delvyChangeMap["documentFileseInfo"] = "A200006007"
//                    delvyChangeMap["documentAtflExtsn"] = ".png"
//                    delvyChangeMap["register"] = PreferenceUtil.getString(context!!, "id", "defaual") //---????????? ?????? ?????? ??????
//                }
//
//            }
            delvyChangeUrl = context!!.resources.getString(R.string.mobile_url) + "changeDelvyLand"
            delvyChangeMap["saupCode"] = ownerInfoJson.getString("saupCode")
            delvyChangeMap["indvdlGrpCode"] = ownerInfoJson.getString("indvdlGrpCode")
            delvyChangeMap["indvdlGrpTy"] = ownerInfoJson.getString("indvdlGrpTy")
            delvyChangeMap["delvyZip"] = editDelvyPostNumber.text.toString()
            delvyChangeMap["delvyAddr"] = editDelvyAddr.text.toString()
            delvyChangeMap["delvyAddrDetail"] = editDelvyAddrDetail.text.toString()
            delvyChangeMap["signFileNm"] = signFileNm
            delvyChangeMap["signFileseInfo"] = "A200006999"
            delvyChangeMap["signAtflExtsn"] = ".png"
            delvyChangeMap["documentFileNm"] = documentFileNm
            delvyChangeMap["documentFileseInfo"] = "A200006007"
            delvyChangeMap["documentAtflExtsn"] = ".png"
            delvyChangeMap["register"] = PreferenceUtil.getString(context!!, "id", "defaual") //---????????? ?????? ?????? ??????
            HttpUtil.getInstance(context!!)
                .callerUrlInfoPostDelvyChange(delvyChangeMap, progressDialog, delvyChangeUrl!!, saveSignFile, saveDocumentFile,
                object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        progressDialog?.dismiss()
                        logUtil.e("fail")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body!!.string()

                        logUtil.d("responseString>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>$responseString")
                        progressDialog?.dismiss()

                        activity?.runOnUiThread {
                            dialog?.dismiss()
                            activity?.onBackPressed()
                        }

                    }
                })

        }

        "${editDelvyPostNumber.text}, ${editDelvyAddr.text}, ${editDelvyAddrDetail.text}"
    }



    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }
}