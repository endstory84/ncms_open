/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.content.Context
import es.dmoral.toasty.Toasty

class ToastUtil(private var context: Context?) {

    init { Toasty.Config.getInstance().tintIcon(true).setTextSize(14).allowQueue(true).apply() }

    /**
     * @param msg (메세지)
     * @param duration (초)
     * @param colorId (색상)
     */

    // TODO: 2021-04-29 종류별로 메소드 지정하여 사용 가능 (success, error, warning, normal, info, custom)

    //fun msg(msg:String, duration:Int, colorId:Int) = Toasty.custom(context, msg, R.mipmap.ic_launcher, colorId, duration, true, true).show()
    fun msg(msg:String, duration:Int) = context?.let { Toasty.normal(it, msg, duration).show() }
    fun msg(msg:Int, duration:Int) = context?.let { Toasty.normal(it, msg, duration).show() }
    fun msg_success(msg:String, duration:Int) = context?.let { Toasty.success(it, msg, duration).show() }
    fun msg_success(msg:Int, duration:Int) = context?.let { Toasty.success(it, msg, duration).show() }
    fun msg_error(msg:String, duration:Int) = context?.let { Toasty.error(it, msg, duration).show() }
    fun msg_error(msg:Int, duration:Int) = context?.let { Toasty.error(it, msg, duration).show() }
    fun msg_warning(msg: String, duration:Int) = context?.let { Toasty.warning(it, msg, duration).show() }
    fun msg_warning(msg: Int, duration:Int) = context?.let { Toasty.warning(it, msg, duration).show() }
    fun msg_info(msg:String, duration:Int) = context?.let { Toasty.info(it, msg, duration).show() }
    fun msg_info(msg:Int, duration:Int) = context?.let { Toasty.info(it, msg, duration).show() }

}