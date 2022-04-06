/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.base

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.NewOwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.adapter.OwnerRecyclerViewAdapter
import kr.or.kreb.ncms.mobile.enums.ToastType
import kr.or.kreb.ncms.mobile.util.DialogUtil
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil

abstract class BaseFragment() : Fragment()
{

    protected lateinit var recyclerViewAdapter: OwnerRecyclerViewAdapter
    protected lateinit var newOwnerRecyclerViewAdapter: NewOwnerRecyclerViewAdapter
    protected var logUtil: LogUtil = LogUtil("BaseOwnerFragment")
    protected var progressDialog: AlertDialog? = null
    protected var builder: MaterialAlertDialogBuilder? = null
    protected var dialogUtil: DialogUtil? = null
    val toast   : ToastUtil by lazy { ToastUtil(activity) }

    abstract fun showOwnerPopup()

    protected fun dismissProgress() {

        activity!!.runOnUiThread {
            progressDialog?.dismiss()
        }

    }

    protected fun showToastError() {
        showToast(ToastType.ERROR, R.string.msg_server_connected_fail, 100)
    }

    protected fun showToast(type: ToastType, resId: Int, duration: Int) {
        showToast(type, getString(resId), duration)
    }

    protected fun showToast(type: ToastType, text: String, duration: Int) {

        activity!!.runOnUiThread {

            when(type) {
                ToastType.NORMAL -> toast.msg(text, duration)
                ToastType.SUCCESS -> toast.msg_success(text, duration)
                ToastType.ERROR -> toast.msg_error(text, duration)
                ToastType.WARNING -> toast.msg_warning(text, duration)
                ToastType.INFO -> toast.msg_info(text, duration)
            }

        }
    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

}