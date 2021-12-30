/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.or.kreb.ncms.mobile.R


class DialogUtil(context: Context?, var activity: Activity?) : Dialog(context!!) {

    private var clickListener: ClickListener? = null
    lateinit var dialog: AlertDialog

    interface ClickListener {
        fun onPositiveClickListener(dialog: DialogInterface, type: String)
        fun onNegativeClickListener(dialog: DialogInterface, type: String)
    }

    fun setClickListener(clickListener: ClickListener) { this.clickListener = clickListener }

    /** 로딩 창 */
    fun progressDialog(builder: MaterialAlertDialogBuilder): AlertDialog {
        builder.run {
            setView(R.layout.include_loadingbar)
            setCancelable(false)
        }
        dialog = builder.create()
        return dialog
    }

    /** 확인 창 */
    fun alertDialog(
        title: String?,
        msg: String?,
        builder: MaterialAlertDialogBuilder,
        type: String
    ): AlertDialog {

        builder.run {
            setIcon(R.drawable.ic_notice)
            setTitle(title)
            setMessage(msg)
            setPositiveButton(R.string.msg_alert_y) { dialog, _ -> clickListener?.onPositiveClickListener(dialog, type) }
            setNegativeButton(R.string.msg_alert_n) { dialog, _ -> clickListener?.onNegativeClickListener(dialog, type) }
            setCancelable(false)
        }

        dialog = builder.create()
        return dialog
    }

    fun alertDialogYesNo(
        title: String?,
        msg: String?,
        builder: MaterialAlertDialogBuilder,
        type: String
    ): AlertDialog {

        builder.run {
            setIcon(R.drawable.ic_notice)
            setTitle(title)
            setMessage(msg)
            setPositiveButton(R.string.msg_alert_yy) { dialog, _ -> clickListener?.onPositiveClickListener(dialog, type) }
            setNegativeButton(R.string.msg_alert_nn) { dialog, _ -> clickListener?.onNegativeClickListener(dialog, type) }
            setCancelable(false)
        }

        dialog = builder.create()
        return dialog
    }

    fun confirmDialog(msg: String?, builder: MaterialAlertDialogBuilder, type: String): AlertDialog {
        builder.run {
            setIcon(R.drawable.ic_notice)
            setTitle("확인")
            setMessage(msg)
            setPositiveButton(R.string.msg_alert_y) { dialog, _ -> clickListener?.onPositiveClickListener(dialog, type) }
            setCancelable(false)
        }

        dialog = builder.create()
        return dialog
    }

    /** 조서 저장 확인 창 */
    fun wtnncCnfirmDialog(
        builder: MaterialAlertDialogBuilder,
        type: String
    ): AlertDialog {

        builder.run {
            setIcon(R.drawable.ic_notice)
            setTitle("조서입력")
            setMessage("입력한 조서를 저장하시겠습니까?")
            setPositiveButton(R.string.msg_alert_y) { dialog, _ -> clickListener?.onPositiveClickListener(dialog, type) }
            setNegativeButton(R.string.msg_alert_n) { dialog, _ -> clickListener?.onNegativeClickListener(dialog, type) }
            setCancelable(false)
        }

        dialog = builder.create()
        return dialog
    }

    /** 조서 내 필수입력 항목 알럿창 */
    fun wtnccAlertDialog(
        msg: String?,
        builder: MaterialAlertDialogBuilder,
        type:String
    ): AlertDialog {

        builder.run {
            setIcon(R.drawable.ic_notice)
            setTitle("필수항목")
            setMessage(msg)
            setPositiveButton(activity?.resources?.getString(R.string.msg_alert_y)) { dialog, _ -> clickListener?.onPositiveClickListener(dialog, type) }
            setCancelable(false)
        }

        dialog = builder.create()
        return dialog
    }


}