/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kr.or.kreb.ncms.mobile.R
import java.util.*

class YearPickerDialogFragment(activity: Activity, v: View) : DialogFragment() {

    private var listener: DatePickerDialog.OnDateSetListener? = null
    private val MAX_YEAR = 2099
    private val MIN_YEAR = 1980
    private val mActivity = activity
    val v = v as TextView
    var cal = Calendar.getInstance()

    fun setListener(listener: DatePickerDialog.OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater
        val dialog: View = inflater.inflate(R.layout.wtnnc_year_picker, null)
        val yearPicker = dialog.findViewById<View>(R.id.yearPicker) as NumberPicker
        builder.setPositiveButton("확인") { dialogInterface, i ->
            v.text = yearPicker.value.toString()
        }
        builder.setNegativeButton("취소", null)

        val year = cal[Calendar.YEAR]
        yearPicker.minValue = MIN_YEAR
        yearPicker.maxValue = MAX_YEAR
        yearPicker.value = year
        builder.setView(dialog)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val outMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display: Display? = mActivity.display
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
        params?.width = (deviceWidth * 0.4).toInt()
        params?.height = (deviceHeight * 0.15).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE));

    }

}