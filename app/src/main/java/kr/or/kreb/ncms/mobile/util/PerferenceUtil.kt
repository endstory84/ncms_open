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
    private var mPref: SharedPreferences? = null

    private fun getPref(context: Context): SharedPreferences {
        mPref = mPref ?: context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return mPref!!
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

}