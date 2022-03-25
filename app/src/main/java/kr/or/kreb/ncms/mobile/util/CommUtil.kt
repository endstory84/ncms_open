/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jhlabs.map.Point2D
import com.jhlabs.map.proj.ProjectionFactory
import com.naver.maps.map.overlay.PolygonOverlay
import kotlinx.android.synthetic.main.activity_map_with_drawerlayout.*
import kotlinx.android.synthetic.main.fragment_bsn_search.*
import kotlinx.android.synthetic.main.include_drawer_header.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kr.or.kreb.ncms.mobile.*
import kr.or.kreb.ncms.mobile.data.BizJibunListInfo
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.CameraEnum
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * 현재 액티비티 Orientation 체크
 * @param activity
 * @return 1: 세로, 2: 가로
 */
fun getWindowOrientation(activity: Activity): Int {
    return activity.resources.configuration.orientation
}

/**
 * 팝업 디스플레이 width, hegiht 지정
 * @param dialog - 프래그먼트 다이얼로그 및 알럿 다이얼로그
 * @param activity
 * @param width
 * @param height
 */
fun getDisplayDistance(dialog: Dialog?, activity: Activity?, width: Float, height: Float) {
    val outMetrics = DisplayMetrics()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val display: Display? = activity?.display
        display?.getRealMetrics(outMetrics)

    } else {
        @Suppress("DEPRECATION")
        val display = activity?.windowManager?.defaultDisplay
        @Suppress("DEPRECATION")
        display?.getRealMetrics(outMetrics)
    }

    val deviceHeight: Int = outMetrics.heightPixels
    val deviceWidth: Int = outMetrics.widthPixels

    val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
    params?.width = (deviceWidth * width).toInt()
    params?.height = (deviceHeight * height).toInt()
    dialog?.window?.attributes = params as WindowManager.LayoutParams
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

/**
 * 액티비티 이동
 * @param context
 * @param actNo : 액티비티 이동
 * @param saupCode : 사업코드
 * @param cameraCode: 카메라 코드(현장, 서류)
 */
fun nextView(context: Context, actNo: Int, saupCode: BizEnum?, cameraCode: CameraEnum?, coord: String?, restData: JSONObject?) {
    val i = Intent()
    when (actNo) {
        Constants.LOGIN_ACT -> {
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            i.setClass(context, LoginActivity::class.java)
            context.startActivity(i)
        }

        Constants.BIZ_LIST_ACT -> {
            i.setClass(context, BizListActivity::class.java)
            context.startActivity(i)
        }

        Constants.BIZ_CNFIRM_ACT -> {
            i.setClass(context, BizCnfirmActivity::class.java)
            context.startActivity(i)
        }

        Constants.MAP_ACT -> {
            i.setClass(context, MapActivity::class.java)
            i.putExtra("saupCode", saupCode)
            i.putExtra("coord", coord)
            i.putExtra("restData", restData.toString())
            context.startActivity(i)
        }

        Constants.CAMERA_ACT -> {
            i.setClass(context, CameraActivity::class.java)
            i.putExtra("cameraCode", cameraCode)
            context.startActivity(i)
        }
    }
}

fun nextViewBizList(context: Context, actNo: Int, id: String?) {
    val i = Intent()
    when (actNo) {
        Constants.BIZ_LIST_ACT -> {
            i.setClass(context, BizListActivity::class.java)
            i.putExtra("id", id)
            context.startActivity(i)
        }
    }
}

fun nextViewCamera(context: Context, actNo: Int, saupCode: String?, bizCode: BizEnum?, fileCode: String, fileCodeNm: String, cameraCode: CameraEnum?) {
    val i = Intent()

    when (actNo) {
        Constants.CAMERA_ACT -> {
            i.setClass(context, CameraActivity::class.java)
            i.putExtra("cameraCode", cameraCode)
            i.putExtra("saupCode", saupCode)
            i.putExtra("bizCode", bizCode)
            i.putExtra("fileCode", fileCode)
            i.putExtra("fileCodeNm", fileCodeNm)
            context.startActivity(i)
        }
    }


}

fun nextViewMultiCamera(context: Context, actNo: Int, saupCode: String?, bizCode: BizEnum?, fileCode: String, fileCodeNm: String, cameraCode: CameraEnum?, wtnCodeArr: ArrayList<String>) {
    val i = Intent()

    when (actNo) {
        Constants.CAMERA_ACT -> {
            i.setClass(context, CameraActivity::class.java)
            i.putExtra("cameraCode", cameraCode)
            i.putExtra("saupCode", saupCode)
            i.putExtra("bizCode", bizCode)
            i.putExtra("fileCode", fileCode)
            i.putExtra("fileCodeNm", fileCodeNm)
            i.putExtra("wtnCodeArr", wtnCodeArr)
            context.startActivity(i)
        }
    }
}

/**
 * 스케치 툴바 Animate
 */
fun fabAnimateFunc(v: View, rotate: Boolean): Boolean {
    v.animate().setDuration(200)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }
        })
        .rotation(if (rotate) 135f else 0f)
    return rotate
}

/**
 * 이미지 3D Swipe Rotate
 * @param view
 */
fun setSwipe3DRotate(view: com.google.android.material.appbar.MaterialToolbar) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post(object : Runnable {
        override fun run() {
            //logUtil.d("반복")

            val downTime = SystemClock.uptimeMillis()
            val eventTime = SystemClock.uptimeMillis()
            val x = 0.0f
            val y = 250.0f
            val metaState = 0

//                val motionEvent1 = MotionEvent.obtain( downTime, eventTime+1000, MotionEvent.ACTION_DOWN, x, y, metaState )
//                val motionEvent2 = MotionEvent.obtain( downTime+1000, eventTime+2000, MotionEvent.ACTION_MOVE, x, 0f, metaState )
//                val motionEvent3 = MotionEvent.obtain( downTime+2000, eventTime+2000, MotionEvent.ACTION_UP, x, 0f, metaState )

            val motionEvent1 = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, 0f, metaState)
            val motionEvent2 = MotionEvent.obtain(downTime, eventTime + 5, MotionEvent.ACTION_MOVE, 0f, 200f, metaState)
            val motionEvent3 = MotionEvent.obtain(downTime + 10, eventTime + 10, MotionEvent.ACTION_UP, 0f, 100f, metaState)

            view.dispatchTouchEvent(motionEvent1)
            view.dispatchTouchEvent(motionEvent2)
            view.dispatchTouchEvent(motionEvent3)

            mainHandler.postDelayed(this, 1000)
        }
    })

}


/**
 * 액티비티 이동간 애니메이션
 * @param startAnimate : 시작 애니메이션
 * @param endAnimate : 종료 애니메이션
 */
fun setActivityChangeAnimate(activity: Activity, startAnimate: Int, endAnimate: Int) {
    activity.overridePendingTransition(startAnimate, endAnimate)
}

/**
 * 상태바 색 지정
 * @param activity
 */

@RequiresApi(Build.VERSION_CODES.R)
fun AppCompatActivity.setStatusBarColor() {
    window.apply {
        insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        )
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        statusBarColor = Color.TRANSPARENT
        setDecorFitsSystemWindows(false)
    }
}

/**
 * 색 지정 (color, alpha)
 * @param context
 * @param color : R.color.resource
 * @param alpha : 0 ~ 255 (투명 -> 불투명)
 */
fun setObjectColor(context: Context, color: Int, alpha: Int): Int {
    return ColorUtils.setAlphaComponent(context.getColor(color), alpha)
}


/**
 *  선택 폴리곤 색 변경
 *  @param poly 폴리곤 객체
 *  @param colorId 색상
 */

fun toggleNaverPolygonColor(context: Context, poly: PolygonOverlay, colorId: Int): PolygonOverlay {
    poly.apply {
        color = setObjectColor(context, colorId, 20)
        outlineWidth = 5
        outlineColor = setObjectColor(context, colorId, 255)

    }
    return poly
}


/**
 * response body string -> Json Parse
 * @param resultStr : Http Response Value
 */

fun httpResultToJsonObject(resultStr: String): JsonObject? {
    return if(!resultStr.contains("<xml")){
        JsonParser.parseString(resultStr).asJsonObject
    } else {
        null
    }
}


/**
 * json파일 파싱
 */
fun loadJSONFromAsset(context: Context, fileName: String): String {
    val json: String?
    try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        val charset: Charset = Charsets.UTF_8
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, charset)
    } catch (ex: IOException) {
        ex.printStackTrace()
        return ""
    }
    return json
}

fun isStringEmpty(obj: Any?): Boolean {
    return obj?.toString()?.trim()?.isEmpty() ?: true
}

/**
 * 좌표계 변환 (EPSG:3857 -> EPSG:4326)
 * @param lat 위도
 * @param lon 경도
 * @return Point2D.Double
 */
fun convertWGS84(lat: Double, lon: Double): Point2D.Double {
    var convertProj: Point2D.Double
    val proj = ProjectionFactory.fromPROJ4Specification(Constants.PROJ4_3857)
    convertProj = proj.inverseTransform(Point2D.Double(lat, lon), Point2D.Double())
    return convertProj
}

/**
 * 좌표계 변환 (EPSG:4326 -> EPSG:3857)
 * @param lat 위도
 * @param lon 경도
 * @return Point2D.Double
 */
fun convertEPSG3857(lat: Double, lon: Double): Point2D.Double {
    val proj = ProjectionFactory.fromPROJ4Specification(Constants.PROJ4_3857)
    return proj.transform(Point2D.Double(lon, lat), Point2D.Double())
}

/**
 * 사업별로 배너 색지정
 * @param context
 * @param mainTv
 * @param subTv
 */
fun setColorBizCategory(context: Context?, mainTv: TextView, subTv: TextView) {
    val tvCategoryColor: GradientDrawable = mainTv.background as GradientDrawable
    val tvSubCategoryColor: GradientDrawable = subTv.background as GradientDrawable

    // TODO: 2021-05-24 사업카테고리 및 현장조사 카테고리 별로 색 지정
    //tvCategoryColor.color = context?.getColorStateList(R.color.root_color_1)
    tvSubCategoryColor.color = context?.getColorStateList(R.color.green)
}

/**
 * 필수입력항목 지정 (*)
 * @param arr
 */
fun setRequireContent(arr: MutableList<TextView>){

    arr.forEach {
        val content: String = it.text.toString()
        val spannableString = SpannableString(content)

        val word = "*"
        val start = content.indexOf(word)
        val end = start + word.length

        spannableString.apply {
            setSpan(ForegroundColorSpan(Color.parseColor("#FF6702")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(1.3f), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            it.text = spannableString
        }
    }

}

/**
 * 단일 스트링 강조 색 지정
 * @param allStr
 * @param valueStr
 */
fun setColorChar(allStr:TextView, valueStr:String){

    val content: String = allStr.text.toString()
    val spannableString = SpannableString(content)

    val start = content.indexOf(valueStr)
    val end = start + valueStr.length

    spannableString.apply {
        setSpan(ForegroundColorSpan(Color.parseColor("#FF6702")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(RelativeSizeSpan(1f), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        allStr.text = spannableString
    }


}

/**
 * StringArray 내 콤마 삽입
 * @param str
 */
fun setJoinToStringComma(strArr: MutableList<String>): String = strArr.joinToString(separator = ",")

/**
 * 마지막 콤마 제거
 * @param str
 */
fun lastCommaRemove(str: String?): String = str?.substring(0, str.length - 1)!!

/**
 * 주민등록번호, 사업자번호 with 애스터(*)
 *  15자 미만은 기존 표출 15자 이상은 subrstring 진행
 * @param ihidNum
 */
fun withIhidNumAsterRisk(ihidNum: String?): String {
    return withIhidNumAsterRisk(true, ihidNum)
}

fun withIhidNumAsterRisk(indvdl: Boolean, ihidNum: String?): String {

    if (null != ihidNum && ihidNum.isNotEmpty()) {
        if (ihidNum!!.indexOf("-") > -1 && ihidNum.length == 14) {
            return when(indvdl) {
                true -> "${ihidNum.substring(0, 8)}******"
                else -> "${ihidNum.substring(0, 7)}*******"
            }
        }
        else if (ihidNum.length == 13) {
            return when(indvdl) {
                true -> "${ihidNum.substring(0, 6)}-${ihidNum.substring(6,7)}******"
                else -> "${ihidNum.substring(0, 6)}-*******"
            }
        }
    }

    return if (ihidNum?.length!! < 14) {
        ihidNum
    } else {
        "${ihidNum.substring(0, 8)} ******"
    }
}

fun clearCameraValue(){
    Constants.CAMERA_ADAPTER = null
    Constants.CAMERA_IMGAE_INDEX = 0
    Constants.CAMERA_IMAGE_ARR.clear()
}

/**
 * AES 256 암호화
 */
fun encryptCBC(text: String) : String {

    val keySpec = SecretKeySpec(Constants.SECRET_KEY.toByteArray(), "AES")
    val iv = IvParameterSpec(Constants.IV.toByteArray())
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
    val crypted = cipher.doFinal(text.toByteArray())
    val encodeByte = Base64.getEncoder().encode(crypted)

    return String(encodeByte)

}