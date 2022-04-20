/*
 * Create by sgablc team.eco-chain on 2022.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.listener

import android.content.Context
import kr.or.kreb.ncms.mobile.util.LogUtil
import net.nshc.droidx3.manager.DroidXCallbackListenerV2
import net.nshc.droidx3.manager.library.DroidXLibraryManager

class DroidXServiceListener(val context: Context) : DroidXCallbackListenerV2 {

    val logUtil: LogUtil = LogUtil("DroidXServiceListener")


    override fun callbackRoot(resultCode: Int) {
        logUtil.i("NSHC_Listener, CallbackRoot -----------> $resultCode")
        if(resultCode < 0) {
            // 에러
        } else {
            if(resultCode == 0) {
                // 정상
            } else {
                //루팅

            }
        }
        DroidXLibraryManager.getInstance().runFastMalwareScan()
    }

    override fun callbackMalware(resultCode: Int) {
        logUtil.i("NSHC_Listener, callbackMalware ------------> $resultCode")
    }

    override fun callbackInit(resultCode: Int) {
        logUtil.i("NSHC_LISTENER, CallbackInit --------> $resultCode")
        DroidXLibraryManager.getInstance().runUpdate()
    }

    override fun callbackRealTimeMalware(resultCode: Int) {
        logUtil.i("NSHC_Listener, CallbackRealTimeMalware ---------> $resultCode")
    }

    override fun callbackUpdate(resultCode: Int) {
        logUtil.i("NSHC_Listener, CallbackUpdate ------------> $resultCode")

        DroidXLibraryManager.getInstance().runRootingCheck()
    }

    override fun callbackMalwareResult(type: Int, iResult: Int, mapResult: MutableMap<Any?, Any?>?) {
        logUtil.i("NSHC_Listener, CallbackMalwareResult --------> $type | $iResult | ${mapResult!!.size}")
    }

    override fun measureUpdateEngine(resultCode: String?) {
    }

    override fun updateProgress(percent: Int, p1: String?) {
        logUtil.i("NSHC_Listener, UpdateProgress ----------> $percent")
    }

    override fun callbackDetailMalware(resultCode: Int) {
    }

    override fun callbackEngineVersion(p0: Array<out String>?, p1: Array<out String>?) {
    }
}