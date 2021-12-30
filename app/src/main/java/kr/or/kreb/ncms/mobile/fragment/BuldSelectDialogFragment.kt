/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import kr.or.kreb.ncms.mobile.util.getDisplayDistance

class BuldSelectDialogFragment (context: Context, activity: Activity, private val v: View) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        builder.setView(v)

        val dlg = builder.create()

        return dlg
    }

    override fun onResume() {
        super.onResume()

//        val outMetrics = DisplayMetrics()
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            val display: Display? = activity?.display
//            display?.getRealMetrics(outMetrics)
//
//        } else {
//            @Suppress("DEPRECATION")
//            val display = activity?.windowManager?.defaultDisplay
//            @Suppress("DEPRECATION")
//            display?.getRealMetrics(outMetrics)
//        }
//
//        val deviceHeight: Int = outMetrics.heightPixels
//        val deviceWidth: Int = outMetrics.widthPixels
//
//        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
//        params?.width = (deviceWidth * 0.95).toInt()
//        params?.height = (deviceHeight * 0.75).toInt()
//        dialog?.window?.attributes = params as WindowManager.LayoutParams
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        getDisplayDistance(dialog, activity, 0.95F, 0.75F)
    }
}