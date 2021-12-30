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

class AddOwnerDialogFragment(context: Context, activity: Activity, private val v: View): DialogFragment()  {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(v)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.95F, 0.75F)

    }
}