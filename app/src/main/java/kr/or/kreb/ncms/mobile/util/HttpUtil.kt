/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File


class HttpUtil() {

    val logUtil = LogUtil(TAG!!)

    private val client = OkHttpClient()
    private val request = Request.Builder()

    val JSON ="application/json; charset=utf-8".toMediaTypeOrNull()
    val IMAGE ="image/jpeg".toMediaTypeOrNull()
    val FILE ="multipart/form-data".toMediaTypeOrNull()

    /**
     * Http GET
     */
    fun callerUrlInfoGet(httpUrl: String, callback: Callback) {
        val request = Request.Builder()
            .url(httpUrl)
            .build()

        client.newCall(request).enqueue(callback)
    }

    fun urlGetNaver(httpUrl: String, naverID: String, naverKey: String, callback: Callback) {
        val request = Request.Builder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", naverID)
            .addHeader("X-NCP-APIGW-API-KEY", naverKey)
            .url(httpUrl)
            .build()
        client.newCall(request).enqueue(callback)

    }

    /**
     * Http Post (JSON)
     * @return Json
     */
    fun callerUrlInfoPostWebServer(map: Map<String, String>, progressBar: AlertDialog?,  httpUrl: String, callback: Callback) {

        try {
            val jsonObject = JSONObject()

            if(map.isNotEmpty()) {
                map.forEach { data ->
                    jsonObject.put(data.key, data.value)
                }

                if(progressBar?.isShowing == false) progressBar.show()

                request
                    .url(httpUrl)
                    .post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                    .build()

                client.newCall(request.build()).enqueue(callback)
            }
        } catch (e: Exception) {
            throw IllegalAccessException (e.toString())
        }

    }

    fun callUrlJsonWebServer(jsonObject: JSONObject, progressBar: AlertDialog?, httpUrl: String, callback: Callback) {

        if(progressBar?.isShowing == false) progressBar.show()

        request
            .url(httpUrl)
            .post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .build()

        client.newCall(request.build()).enqueue(callback)

    }

    fun callUrlJsonCodeWebServer(httpUrl: String, map: Map<String, String>, callback: Callback){
            val jsonObject = JSONObject()

            map.forEach { data ->
                jsonObject.put(data.key, data.value)
            }

            request
                .url(httpUrl)
                .post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()

            client.newCall(request.build()).enqueue(callback)


    }

    fun callerUrlInfoPostFileUpload(obj: Map<String, String>, progressBar: AlertDialog?,
                                    httpUrl: String, filePath: File, callback: Callback) {
        if(progressBar?.isShowing == false) progressBar.show()

        val requestBody: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("imageFile", obj.getValue("atflNm"), RequestBody.create(IMAGE, filePath))
            .addFormDataPart("ladWtnCode",obj.getValue("ladWtnCode"))
            .addFormDataPart("thingWtnCode", obj.getValue("thingWtnCode"))
            .addFormDataPart("saupCode",obj.getValue("saupCode"))
            .addFormDataPart("rm",obj.getValue("rm"))
            .addFormDataPart("atflNm",obj.getValue("atflNm"))
            .addFormDataPart("fileseInfo",obj.getValue("fileseInfo"))
            .addFormDataPart("fileCodeNm",obj.getValue("fileCodeNm"))
            .addFormDataPart("register",obj.getValue("register"))
            .addFormDataPart("atflSize",obj.getValue("atflSize"))
            .addFormDataPart("atflExtsn",obj.getValue("atflExtsn"))
            .addFormDataPart("lon",obj.getValue("lon"))
            .addFormDataPart("lat",obj.getValue("lat"))
            .addFormDataPart("azimuth", obj.getValue("azimuth"))
            .addFormDataPart("mulit", obj.getValue("mulit"))

        val request: Request = Request.Builder()
            .url(httpUrl)
            .post(requestBody.build())
            .build()
        client.newCall(request).enqueue(callback)


    }

    fun callerUrlInfoPostDelvyChange(obj: Map<String, String>, progressBar: AlertDialog?,
                                    httpUrl: String, signFilePath: File, documentFilePath: File,
                                    callback: Callback) {
        if(progressBar?.isShowing == false) progressBar.show()

        val requestBody: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("saupCode", obj.getValue("saupCode"))
            .addFormDataPart("indvdlGrpCode", obj.getValue("indvdlGrpCode"))
            .addFormDataPart("indvdlGrpTy", obj.getValue("indvdlGrpTy"))
            .addFormDataPart("delvyZip", obj.getValue("delvyZip"))
            .addFormDataPart("delvyAddr",obj.getValue("delvyAddr"))
            .addFormDataPart("delvyAddrDetail", obj.getValue("delvyAddrDetail"))
            .addFormDataPart("signFile", obj.getValue("signFileNm"), RequestBody.create(IMAGE, signFilePath))
            .addFormDataPart("signFileAtfl", obj.getValue("signFileNm"))
            .addFormDataPart("signFileseInfo", obj.getValue("signFileseInfo"))
            .addFormDataPart("signAtflExtsn", obj.getValue("signAtflExtsn"))
            .addFormDataPart("documentFile", obj.getValue("documentFileNm"), RequestBody.create(IMAGE, documentFilePath))
            .addFormDataPart("documentFileAtfl", obj.getValue("documentFileNm"))
            .addFormDataPart("documentFileseInfo", obj.getValue("documentFileseInfo"))
            .addFormDataPart("documentAtflExtsn", obj.getValue("documentAtflExtsn"))
            .addFormDataPart("register", obj.getValue("register"))

        val request: Request = Request.Builder()
            .url(httpUrl)
            .post(requestBody.build())
            .build()
        client.newCall(request).enqueue(callback)

    }


    /**
     * Http Post
     * @return Map
     */

    fun callerUrlInfoPostGeoServer(obj: Map<String, String>, progressBar: AlertDialog?, httpUrl: String, callback: Callback){

        try{
            val builder = FormBody.Builder()
            obj.forEach{ data ->
                builder.add(data.key, data.value)
            }

            //if(progressBar?.isShowing == false) progressBar.show()

            val formBody: RequestBody = builder.build()

            request
                .url(httpUrl)
                .post(formBody)
                .build()

            client.newCall(request.build()).enqueue(callback)

        }catch (e:Exception){
            logUtil.d(e.toString())
        }

    }



    /**
     * 좌표계 변환
     * @x x
     * @y y
     * @sourceCRS  이전 CRS
     * @targetCRS  변경 CRS
     */
   /* fun projTransfrom(x:String?, y:String?, sourceCRS: String?, targetCRS: String?): String? {
        return convertCRS
    }*/


    companion object {

        @Volatile private var instance: HttpUtil? = null

        @JvmStatic fun getInstance(context: Context): HttpUtil =
            instance ?: synchronized(this) {
                instance ?: HttpUtil().also {
                    instance = it
                }
            }

        private val TAG: String? = HttpUtil::class.simpleName;
    }

}