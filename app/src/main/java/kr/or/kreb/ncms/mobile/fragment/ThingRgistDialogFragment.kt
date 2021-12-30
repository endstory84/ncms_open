/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ThingRgistDialogFragment(
    context: Context,
    activity: Activity,
    v: View,
) : DialogFragment() {

    private val mActivity: Activity = activity
    private val mContext: Context = context
    private val v: View = v

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(mContext)

        builder.setView(v)

        val dlg = builder.create()

        return dlg
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
        params?.width = (deviceWidth * 0.95).toInt()
        params?.height = (deviceHeight * 0.75).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

    }
}