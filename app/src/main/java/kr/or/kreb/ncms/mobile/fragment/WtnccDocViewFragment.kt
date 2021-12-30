
/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import kr.or.kreb.ncms.mobile.databinding.FragmentWtnncDocViewBinding
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.util.FileUtil
import kr.or.kreb.ncms.mobile.util.getDisplayDistance
import kr.or.kreb.ncms.mobile.util.goneView
import kr.or.kreb.ncms.mobile.util.visibleView
import java.io.File

class WtnccDocViewFragment(private val file: File?) :
    BaseDialogFragment<FragmentWtnncDocViewBinding>(FragmentWtnncDocViewBinding::inflate, WtnccDocViewFragment::class.java.simpleName) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         *  https://github.com/barteksc/AndroidPdfViewer
         *  fromUri(Uri)
         *  fromFile(File)
         *  fromBytes(byte[])
         *  fromStream(InputStream)
         */

        FileUtil.run {
            val mimeTYpe: String = getMimeType(getExtension(file!!.name))
            logUtil.d(mimeTYpe)

            when(mimeTYpe){
                "application/pdf" -> {
                    with(binding){
                        imageView.goneView()
                        pdfView.fromFile(file).load()
                        pdfView.visibleView()

                    }
                }
                "image/png" -> {
                    with(binding){
                        pdfView.goneView()

                        val imageFileBitmap = BitmapFactory.decodeFile(file.absolutePath)
                        imageView.setImageBitmap(imageFileBitmap)

                        imageView.visibleView()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.85F, 0.85F)
    }

}