
/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_indoor_sketch.*
import kotlinx.android.synthetic.main.include_indoorsketch_toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.adapter.SketchThingBuldAdpater
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.data.SketchInfo
import kr.or.kreb.ncms.mobile.data.ThingWtnObject
import kr.or.kreb.ncms.mobile.databinding.FragmentIndoorSketchBinding
import kr.or.kreb.ncms.mobile.util.*
import kr.or.kreb.ncms.mobile.view.IndoorCanvasView


class IndoorSketchFragment :
    BaseDialogFragment<FragmentIndoorSketchBinding>(FragmentIndoorSketchBinding::inflate, IndoorSketchFragment::class.java.simpleName),
    View.OnClickListener {

    private var fab_open: Animation? = null
    private var fab_close: Animation? = null

    private var fabIndoorVisableArr = mutableListOf<FloatingActionButton>()
    private var fabIndoorArr = mutableListOf<FloatingActionButton>()
    private var fabIndoorTranslationXArr = mutableListOf<Float>()

    private lateinit var dialogUtil: DialogUtil
    lateinit var dialogBuilder: MaterialAlertDialogBuilder

    lateinit var adapter: SketchThingBuldAdpater

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogUtil = DialogUtil(context, activity)
        dialogBuilder = MaterialAlertDialogBuilder(context!!)

        var isFabOpen = false

        fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close)

        fabIndoorVisableArr = mutableListOf(
            floatingActionButtonIndoorToolbar,
            floatingActionButtonIndoorUndo,
            floatingActionButtonIndoorRedo,
            floatingActionButtonLine,
            floatingActionButtonRectangle,
            floatingActionButtonOval,
            floatingActionButtonIndoorClear,
            floatingActionButtonIndoorText,
            floatingActionButtonSave,
        )

        fabIndoorArr = mutableListOf(
            floatingActionButtonSave,
            floatingActionButtonIndoorText,
            floatingActionButtonIndoorClear,
            floatingActionButtonOval,
            floatingActionButtonRectangle,
            floatingActionButtonLine,
            floatingActionButtonIndoorRedo,
            floatingActionButtonIndoorUndo
        )

        fabIndoorTranslationXArr = mutableListOf(-1200f, -1050f, -900f, -750f, -600f, -450f, -300f, -150f)

        toggleIndoorFab(false)

        floatingActionButtonIndoorToolbar.setOnClickListener {
            isFabOpen = fabAnimateFunc(floatingActionButtonIndoorToolbar, !isFabOpen)
            toggleIndoorFab(isFabOpen)
        }

        fabIndoorArr.forEach { obj -> obj.setOnClickListener(this) }


        // TODO: 2022-01-04 물건 스케치 리사이클러 뷰
        val sketchInfoArr = mutableListOf<SketchInfo>()

        for(i in 0 until ThingWtnObject.buldContainsArr!!.size){
            sketchInfoArr.add(SketchInfo(ThingWtnObject.buldContainsArr!![i], ThingWtnObject.buldContainsWtnccCodeArr?.get(i)!!))
        }

        adapter = SketchThingBuldAdpater(sketchInfoArr)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvSketchThing.layoutManager = layoutManager
        rvSketchThing.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        getDisplayDistance(dialog, activity, 0.8F, 0.8F)
    }

    /**
     * 실내 스케치 툴바 ON/OFF
     */
    private fun toggleIndoorFab(flag: Boolean) {
        if (!flag) {
            fabIndoorVisableArr.forEachIndexed { idx, obj ->
                obj.visibleView()
                if (idx > 0) {
                    ObjectAnimator.ofFloat(
                        fabIndoorArr[idx - 1],
                        "translationX",
                        fabIndoorTranslationXArr[idx - 1]
                    ).run { start() }
                }
            }
        } else {
            fabIndoorArr.forEach { obj ->
                ObjectAnimator.ofFloat(obj, "translationX", 0f).run { start() }
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            // 실내 스케치
            R.id.floatingActionButtonIndoorUndo -> binding.indoorCanvasView.undo()
            R.id.floatingActionButtonIndoorRedo -> binding.indoorCanvasView.redo()
            R.id.floatingActionButtonIndoorClear -> binding.indoorCanvasView.clear()
            R.id.floatingActionButtonLine -> {
                binding.indoorCanvasView.setMode(IndoorCanvasView.Mode.DRAW)
                binding.indoorCanvasView.setDrawer(IndoorCanvasView.Drawer.LINE)
            }
            R.id.floatingActionButtonRectangle -> {
                binding.indoorCanvasView.setMode(IndoorCanvasView.Mode.DRAW)
                binding.indoorCanvasView.setDrawer(IndoorCanvasView.Drawer.RECTANGLE)
            }
            R.id.floatingActionButtonOval -> {
                binding.indoorCanvasView.setMode(IndoorCanvasView.Mode.DRAW)
                binding.indoorCanvasView.setDrawer(IndoorCanvasView.Drawer.OVAL)
            }
            R.id.floatingActionButtonIndoorText -> {
                binding.indoorCanvasView.setMode(IndoorCanvasView.Mode.TEXT)

                val editText = EditText(context)

                dialogUtil.run {

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(100, 100, 100, 100)

                    editText.apply {
                        gravity = Gravity.TOP or Gravity.START
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        setLines(1)
                        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))
                        hint = "10자 미만으로 입력해주세요."
                        layoutParams = params
                    }

                    dialogBuilder.run {
                        setTitle("텍스트 입력")
                        setView(editText)
                        setPositiveButton(R.string.msg_alert_y) { dialog, _ ->

                            binding.indoorCanvasView.tempLayout = null
                            binding.indoorCanvasView.setText(editText.text.toString())

                        }
                        setNegativeButton(R.string.msg_alert_n) { dialog, _ ->
                            dialog.dismiss()

                        }
                        setCancelable(false)
                    }
                    dialog = dialogBuilder.create()
                    dialog.show()


                }

            }
            R.id.floatingActionButtonSave -> {
                logUtil.d("indoor save")
                // TODO: 2021-10-20 현재 뷰 이미지 파일 추출 -> (파일 서버저장은 추후에 다시 재정립해야함.)
                setVisibleToolbar(true)

                GlobalScope.launch {
                    delay(500L)
                    view?.let { FileUtil.getBitmapFromView(it, Color.TRANSPARENT) }
                    dialog?.dismiss()
                }

                setVisibleToolbar(false)
            }
        }
    }

    private fun setVisibleToolbar(flag:Boolean){

        if(flag){
            fabIndoorVisableArr.forEach { obj -> obj.visibleView() }
        } else {
            fabIndoorVisableArr.forEach { obj -> obj.goneView() }
        }
    }

}