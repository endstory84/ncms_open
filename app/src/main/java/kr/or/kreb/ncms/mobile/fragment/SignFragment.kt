/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_sign.*
import kotlinx.android.synthetic.main.fragment_sign.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.databinding.FragmentSignBinding
import kr.or.kreb.ncms.mobile.util.FileUtil
import kr.or.kreb.ncms.mobile.util.getDisplayDistance
import org.json.JSONObject


class SignFragment(var delvyFragment: DelvyAddrChangeFragment?, val ownerInfoJson: JSONObject?) :
    BaseDialogFragment<FragmentSignBinding>(FragmentSignBinding::inflate, SignFragment::class.java.simpleName),
    View.OnClickListener {

    var listener: PreviewListener? = null

    interface PreviewListener { fun onPreview(bitmap: Bitmap) }
    fun setPreviewListener(listener: PreviewListener) { this.listener = listener }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignReset.setOnClickListener(this)
        btnSignCancel.setOnClickListener(this)
        btnSignSave.setOnClickListener(this)

        gestureView.isDrawingCacheEnabled = true
        gestureView.isAlwaysDrawnWithCacheEnabled = true
        gestureView.isHapticFeedbackEnabled = false
        gestureView.cancelLongPress()
        gestureView.cancelClearAnimation()

        if(ownerInfoJson != null) {
            view.tvSignUser.text = checkStringNull(ownerInfoJson.getString("ownerNm"))
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSignReset -> {
                gestureView?.apply {
                    invalidate()
                    clear(true)
                    clearAnimation()
                    cancelClearAnimation()
                }
            }
            R.id.btnSignCancel -> dialog?.dismiss()

            R.id.btnSignSave -> {

                val getSignBitmap = Bitmap.createBitmap(gestureView.drawingCache)
                FileUtil.convertToPNG(getSignBitmap)

                when (delvyFragment) {
                    null -> {
                        logUtil.d("현장조사 카메라 서명")
                        listener?.onPreview(getSignBitmap)
                    }
                    else -> {
                        logUtil.d("송달주소변경 서명")
                        listener?.onPreview(getSignBitmap)
                        delvyFragment?.onPreview(getSignBitmap)
                    }
                }

                dialog?.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.5F, 0.5F)
    }

    fun checkStringNull(nullString: String): String {
        if (nullString == "null") {
            return ""
        } else {
            return nullString
        }
    }

}