/*
* Create by sgablc team.eco-chain on 2021.
* Copyright (c) 2021. sgablc. All rights reserved.
*/

package kr.or.kreb.ncms.mobile.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ConfirmDialogFragment(
    val icon: Int,
    val title: String,
    val msg: String,
    val y: String,
    val n: String) : DialogFragment() {

    private var confirmDialogListener: ConfirmDialogListener? = null

    interface ConfirmDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            confirmDialogListener = context as ConfirmDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context" +"must implement ConfirmDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(icon)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(y) { _, _ -> confirmDialogListener?.onDialogPositiveClick(this) }
            .setNegativeButton(n) {_, _ -> confirmDialogListener?.onDialogNegativeClick(this) }
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialog.dismiss()
    }

    fun show(){
        this.show()
    }

    fun close(){
        this.dismiss()
    }
}