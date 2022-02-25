/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.content.Context
import android.content.SharedPreferences
import kr.or.kreb.ncms.mobile.enums.BizEnum

object PreferenceUtil {

    private const val SHARED_PREFERENCES_NAME = "NCMS_PREFERENCES"
    private const val SHARED_PREFERENCES_LOGIN_NAME = "appLogin"
    private const val SHARED_PREFERENCES_USER_INFO = "UserInfo"

    public const val KEY_NM_LOGIN_ID = "ID"
    public const val KEY_NM_LOGIN_CHK = "loginCheck"

    public const val KEY_NM_USER_EMP_CD = "EMP_CD"
    public const val KEY_NM_USER_EMP_NM = "EMP_NM"
    public const val KEY_NM_USER_DEPT_CD = "DEPT_CD"
    public const val KEY_NM_USER_DEPT_NM = "DEPT_NM"
    public const val KEY_NM_USER_OFCPS = "OFCPS"
    public const val KEY_NM_USER_CLSF = "CLSF"


    private var mPref: SharedPreferences? = null
    private var mLoginPref: SharedPreferences? = null
    private var mUserInfoPref: SharedPreferences? = null

    private fun getPref(context: Context): SharedPreferences {
        mPref = mPref ?: context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return mPref!!
    }

    private fun getLoginPref(context: Context): SharedPreferences {
        mLoginPref = mLoginPref ?: context.getSharedPreferences(SHARED_PREFERENCES_LOGIN_NAME, Context.MODE_PRIVATE)
        return mLoginPref!!
    }

    private fun getUserInfoPref(context: Context): SharedPreferences {
        mUserInfoPref = mUserInfoPref ?: context.getSharedPreferences(SHARED_PREFERENCES_USER_INFO, Context.MODE_PRIVATE)
        return mUserInfoPref!!
    }

    fun getString(context: Context, key: String, defValue: String): String {
        return getPref(context).getString(key, defValue).toString()
    }

    fun getBiz(context: Context, key: String?): BizEnum {
        return BizEnum.valueOf(getPref(context).getString(key,"defValue").toString())
    }

    fun getPageNameString(context: Context, key: String, pageName: String): String {
        return getPref(context).getString(key, pageName).toString()
    }

    fun setString(context: Context, key: String, str: String) {
        getPref(context).edit().putString(key, str).apply()
    }

    fun setBiz(context: Context, key: String, bizEnum: BizEnum) {
        getPref(context).edit().putString(key, bizEnum.name).apply()
    }

    fun setPageNameString(context: Context, key: String, str: String) {
        getPref(context).edit().putString(key, str).apply()
    }

    /**
     * 로그인 정보
     */
    fun setLoginInfo(context: Context, saveId: Boolean, id: String) {

        var edit: SharedPreferences.Editor = getLoginPref(context).edit()

        edit.putBoolean(KEY_NM_LOGIN_CHK, saveId)
        edit.putString(KEY_NM_LOGIN_ID, id)

        edit.commit()

    }

    fun getLoginIsSaveId(context: Context) : Boolean {

        return getLoginPref(context).getBoolean(KEY_NM_LOGIN_CHK, false)!!

    }

    fun getLoginId(context: Context) : String {

        return getUserInfo(context, KEY_NM_LOGIN_ID, "")

    }

    fun removeLoginInfo(context: Context) {

        var edit: SharedPreferences.Editor = getLoginPref(context).edit()
        edit.remove(KEY_NM_LOGIN_CHK)
        edit.remove(KEY_NM_LOGIN_ID)
        edit.clear()
        edit.commit()

    }

    /**
     * 사용자 정보
     */
    fun setUserInfo(context: Context, empCd: String, empNm: String, deptCd: String, deptNm: String, ofcps: String, clsf: String) {

        var edit: SharedPreferences.Editor = getUserInfoPref(context).edit()

        edit.putString(KEY_NM_USER_EMP_CD, empCd)
        edit.putString(KEY_NM_USER_EMP_NM, empNm)
        edit.putString(KEY_NM_USER_DEPT_CD, deptCd)
        edit.putString(KEY_NM_USER_DEPT_NM, deptNm)
        edit.putString(KEY_NM_USER_OFCPS, ofcps)
        edit.putString(KEY_NM_USER_CLSF, clsf)

        edit.commit()
    }

    fun getUserInfo(context: Context, keyNm: String) : String {

        return getUserInfoPref(context).getString(keyNm, "")!!

    }

    fun getUserInfo(context: Context, keyNm: String, defaultValue: String) : String {

        return getUserInfoPref(context).getString(keyNm, defaultValue)!!

    }

    fun removeUserInfo(context: Context, keyNm: String) {

        var edit: SharedPreferences.Editor = getUserInfoPref(context).edit()
        edit.remove(keyNm)
        edit.commit()

    }

    fun removeAllUserInfo(context: Context) {

        var edit: SharedPreferences.Editor = getUserInfoPref(context).edit()

        edit.clear()
        edit.commit()

    }

}