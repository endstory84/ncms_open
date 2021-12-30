/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {

    val logUtil:LogUtil = LogUtil("PermissionUtil")

    const val PERMISSION_REQUEST_CODE = 2000

    // 요청 권한 리스트
    private val REQUIRED_PERMISSION_ARR = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private val REJECTED_PERMISSION_ARR = ArrayList<String>() // 권한거부 된 리스트

    /**
     * 권한 확인
     */

    fun hasPermission(context:Context, activity: Activity) {
        for(permission in REQUIRED_PERMISSION_ARR) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                REJECTED_PERMISSION_ARR.add(permission)
            }
        }
        requestPermission(activity)
    }

    /**
     * 권한 요청
     */

    private fun requestPermission(activity:Activity) {
        if(REJECTED_PERMISSION_ARR.isNotEmpty()){
            val array = arrayOfNulls<String>(REJECTED_PERMISSION_ARR.size)
            ActivityCompat.requestPermissions(activity, REJECTED_PERMISSION_ARR.toArray(array), PERMISSION_REQUEST_CODE)
        }
    }

    /**
     *  요청 재 확인
     * true : 사용자가 전에 해당 요청을 거부
     * false : 거부했으며 다시묻지않음 또는 기기 정책에서 권한을 금지한 경우
     */

    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        for (permission in REQUIRED_PERMISSION_ARR) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                return true
            }
        }
        return false
    }

    /**
     * 안드로이드 권한 설정창 이동
     */

    fun launchPermissionSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
}