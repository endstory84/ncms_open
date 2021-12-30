/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseDialogFragment<VB: ViewBinding>(private val inflate: Inflate<VB>, private val fragmentName: String) : DialogFragment() {

    val logUtil: LogUtil = LogUtil(fragmentName::class.java.simpleName)
    val toastUtil: ToastUtil = ToastUtil(context)

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.dialog?.setCanceledOnTouchOutside(false)
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        logUtil.d("[$fragmentName] in onDestroyView done!")
    }
}