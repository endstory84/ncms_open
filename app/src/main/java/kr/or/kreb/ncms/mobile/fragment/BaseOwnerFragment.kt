/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_add_choice_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_new_modify_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.fragment_add_owner_dialog.view.selectInputBtn
import kotlinx.android.synthetic.main.fragment_add_owner_relate.*
import kotlinx.android.synthetic.main.fragment_add_owner_relate.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_owner_dialog.view.addOwnerRgistAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerCrpNoText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDelvyAddrText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerDivisionText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerNameText
import kotlinx.android.synthetic.main.fragment_add_select_relate_dialog.view.ownerSameNameText
import kr.or.kreb.ncms.mobile.MapActivity
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.*
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.enums.BizEnum
import kr.or.kreb.ncms.mobile.enums.ToastType
import kr.or.kreb.ncms.mobile.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

abstract class BaseOwnerFragment() : Fragment()
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

}