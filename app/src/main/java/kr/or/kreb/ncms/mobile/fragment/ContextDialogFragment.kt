/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_dialog_top.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.util.goneView
import kr.or.kreb.ncms.mobile.util.visibleView


class ContextDialogFragment(
    val icon: Int,
    val title: String,
    var array: MutableList<String>,
    var address: String?,
    var jibun: String?,
    var legaldongCode: String?,
    var dataString: String?,
    var popupType: String?) :
    DialogFragment()
{

    private var contextDialogListener: ContextDialogListener? = null
    lateinit var materialDialog: Dialog
    var _isShow = false

    interface ContextDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, popupType: String?, legaldongCode: String?)
        fun onDialogNegativeClick(dialog: DialogFragment)
        fun onSingleChoiceItems(dialog: DialogFragment, position: Int)
    }

    override fun onAttach(context: Context) {

        super.onAttach(context)
        try {
            contextDialogListener = context as ContextDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context" +"must implement ContextDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (!_isShow) {
            val checkedItem = 0

            val inflater = activity!!.layoutInflater

            val fragmentCustomTopLayout = inflater.inflate(R.layout.fragment_dialog_top, null)
            //val fragmentCustomContentLayout = inflater.inflate(R.layout.biz_spinner_item, null)

            fragmentCustomTopLayout.apply {
                textViewFragmentDialogTitle.text = title
                textViewFragmentDialogTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)

                if(address != null){
                    textViewFragmentDialogAddr.visibleView()
                    textViewFragmentDialogAddr.text ="$address $jibun"
                } else {
                    textViewFragmentDialogAddr.goneView()
                }
            }

            //val customAdapter = SimpleAdapter(requireActivity(), null, R.layout.custom_simple_single_choice_item_1line, null, null)
//            val adapter = CustomSingleChoiceAdapter(context!!, array.toTypedArray())

            materialDialog = MaterialAlertDialogBuilder(requireContext())
                .setIcon(icon)
                .setTitle(title)
                //.setView(content)
                //.setView(fragmentCustomContentLayout)
                .setCustomTitle(fragmentCustomTopLayout)
                //.setAdapter(adapter, null)
                .setPositiveButton(R.string.msg_alert_y) { _, _ ->
                    contextDialogListener?.onDialogPositiveClick(this, popupType, legaldongCode)
                }
                .setNegativeButton(R.string.msg_alert_n) { _, _ ->
                    contextDialogListener?.onDialogNegativeClick(this)
                }
                .setSingleChoiceItems(array.toTypedArray(), checkedItem) { _, which ->
                    contextDialogListener?.onSingleChoiceItems(this, which)
                }
                .setCancelable(false)
                .create()

            _isShow = true
        }

        return materialDialog
    }

    override fun onDestroy() {
        super.onDestroy()
        _isShow = false
    }

}