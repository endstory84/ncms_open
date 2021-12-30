/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.*
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.fragment_indoor_sketch.view.*
import kotlinx.android.synthetic.main.view_indoorsketch_editor_text.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.util.LogUtil
import kr.or.kreb.ncms.mobile.util.ToastUtil
import kr.or.kreb.ncms.mobile.util.goneView
import kr.or.kreb.ncms.mobile.util.visibleView
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * https://github.com/Korilakkuma/CanvasView
 */


class IndoorCanvasView :
    View,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private val logutil: LogUtil = LogUtil(IndoorCanvasView::class.simpleName.toString())

    // 캔버스 모드 (그리기, 텍스트, 지우개)
    enum class Mode { DRAW, TEXT, ERASER }

    // 그리기모드 상세메뉴
    enum class Drawer { PEN, LINE, RECTANGLE, CIRCLE, OVAL, QUADRATIC_BEZIER, QUBIC_BEZIER }

    private var canvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private var gridPaperBitmap: Bitmap? = null

    private val pathLists: MutableList<Path> = ArrayList()
    private val paintLists: MutableList<Paint> = ArrayList()

    private var baseColor = Color.TRANSPARENT

    // 이전 다음
    private var historyPointer = 0

    // enum 상태값
    private var mode = Mode.DRAW
    private var drawer = Drawer.LINE
    private var isDown = false

    // 페인트
    private var paintStyle = Paint.Style.STROKE
    private var paintStrokeColor = Color.RED
    private var paintFillColor = Color.RED
    private var paintStrokeWidth = 5f
    private var opacity = 255
    private var blur = 0f
    private var lineCap = Paint.Cap.ROUND

    // 텍스트
    private var text = ""
    private var fontFamily = context?.resources?.getFont(R.font.notosansbold)
    private var fontSize = 42f
    private val textAlign = Paint.Align.RIGHT // fixed

    private var textPaint = Paint()
    private var textX = 0f
    private var textY = 0f

    // 그리기 모드
    private var startX = 0f
    private var startY = 0f
    private var controlX = 0f
    private var controlY = 0f

    var indoorSketchView: View? = null
    var _isTextViewMove = false

    var tempLayout: FrameLayout.LayoutParams? = null
    private var detectorCompat: GestureDetectorCompat

    val canvasEditTextLayout = mutableListOf<ConstraintLayout>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setup()
        detectorCompat = GestureDetectorCompat(getContext(), this)
        detectorCompat.setOnDoubleTapListener(this)
    }

    /**
     * Common initialization.
     *
     * @param context
     */
    private fun setup() {
        pathLists.add(Path())
        paintLists.add(createPaint())
        historyPointer++
        textPaint.setARGB(0, 255, 255, 255)
    }

    /**
     * This method creates the instance of Paint.
     * In addition, this method sets styles for Paint.
     *
     * @return paint This is returned as the instance of Paint
     */
    private fun createPaint(): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = paintStyle
        paint.strokeWidth = paintStrokeWidth
        paint.strokeCap = lineCap
        paint.strokeJoin = Paint.Join.MITER // fixed

        // for Text
        if (mode == Mode.TEXT) {
            paint.typeface = fontFamily
            paint.textSize = fontSize
            paint.textAlign = textAlign
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
        }
        if (mode == Mode.ERASER) {
            // Eraser
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.setARGB(0, 0, 0, 0)

            // paint.setColor(this.baseColor);
            // paint.setShadowLayer(this.blur, 0F, 0F, this.baseColor);
        } else {
            // Otherwise
            paint.color = paintStrokeColor
            paint.setShadowLayer(blur, 0f, 0f, paintStrokeColor)
            paint.alpha = opacity
        }
        return paint
    }

    /**
     * This method initialize Path.
     * Namely, this method creates the instance of Path,
     * and moves current position.
     *
     * @param event This is argument of onTouchEvent method
     * @return path This is returned as the instance of Path
     */
    private fun createPath(event: MotionEvent): Path {
        val path = Path()

        // Save for ACTION_MOVE
        startX = event.x
        startY = event.y
        path.moveTo(startX, startY)
        return path
    }

    /**
     * This method updates the lists for the instance of Path and Paint.
     * "Undo" and "Redo" are enabled by this method.
     *
     * @param path the instance of Path
     * @param paint the instance of Paint
     */
    private fun updateHistory(path: Path) {
        if (historyPointer == pathLists.size) {
            pathLists.add(path)
            paintLists.add(createPaint())
            historyPointer++
        } else {
            // On the way of Undo or Redo
            pathLists[historyPointer] = path
            paintLists[historyPointer] = createPaint()
            historyPointer++
            var i = historyPointer
            val size = paintLists.size
            while (i < size) {
                pathLists.removeAt(historyPointer)
                paintLists.removeAt(historyPointer)
                i++
            }
        }
    }

    /**
     * This method gets the instance of Path that pointer indicates.
     *
     * @return the instance of Path
     */
    private fun getCurrentPath(): Path {
        return pathLists[historyPointer - 1]
    }

    /**
     * This method draws text.
     *
     * @param canvas the instance of Canvas
     */
    private fun drawText(canvas: Canvas) {
        if (text.isEmpty()) {
            return
        }
        if (mode == Mode.TEXT) {
            textX = startX
            textY = startY
            textPaint = createPaint()
        }
        val textX = textX
        val textY = textY
        val paintForMeasureText = Paint()

        // Line break automatically
        val textLength = paintForMeasureText.measureText(text)
        val lengthOfChar = textLength / text.length.toFloat()
        val restWidth = this.canvas!!.width - textX // text-align : right
        val numChars = if (lengthOfChar <= 0) 1 else floor((restWidth / lengthOfChar).toDouble()).toInt() // The number of characters at 1 line
        val modNumChars = if (numChars < 1) 1 else numChars
        var y = textY
        var i = 0
        val len = text.length
        while (i < len) {
            val substring: String = if (i + modNumChars < len) {
                text.substring(i, i + modNumChars)
            } else {
                text.substring(i, len)
            }
            y += fontSize
            canvas.drawText(substring, textX, y, textPaint)
            i += modNumChars
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     *
     * @param event This is argument of onTouchEvent method
     */
    private fun onActionDown(event: MotionEvent) {
        when (mode) {
            Mode.DRAW, Mode.ERASER -> if (drawer != Drawer.QUADRATIC_BEZIER && drawer != Drawer.QUBIC_BEZIER) {
                // Oherwise
                updateHistory(createPath(event))
                isDown = true
            } else {
                // Bezier
                if (startX == 0f && startY == 0f) {
                    // The 1st tap
                    updateHistory(createPath(event))
                } else {
                    // The 2nd tap
                    controlX = event.x
                    controlY = event.y
                    isDown = true
                }
            }
            Mode.TEXT -> {
                startX = event.x
                startY = event.y

            }
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_MOVE
     *
     * @param event This is argument of onTouchEvent method
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onActionMove(event: MotionEvent) {
        val x = event.x
        val y = event.y
        when (mode) {
            Mode.DRAW, Mode.ERASER -> if (drawer != Drawer.QUADRATIC_BEZIER && drawer != Drawer.QUBIC_BEZIER) {
                if (!isDown) {
                    return
                }
                val path = getCurrentPath()
                when (drawer) {
                    Drawer.PEN -> path.lineTo(x, y)
                    Drawer.LINE -> {
                        path.reset()
                        path.moveTo(startX, startY)
                        path.lineTo(x, y)
                    }
                    Drawer.RECTANGLE -> {
                        path.reset()
                        path.addRect(startX, startY, x, y, Path.Direction.CCW)
                    }
                    Drawer.CIRCLE -> {
                        val distanceX = abs((startX - x).toDouble())
                        val distanceY = abs((startX - y).toDouble())
                        val radius = sqrt(distanceX.pow(1.5) + distanceY.pow(1.5))
                        path.reset()
                        path.addCircle(startX, startY, radius.toFloat(), Path.Direction.CCW)
                    }
                    Drawer.OVAL -> {
                        val rect = RectF(startX, startY, x, y)
                        path.reset()
                        path.addOval(rect, Path.Direction.CCW)
                    }
                    else -> {
                    }
                }
            } else {
                if (!isDown) {
                    return
                }
                val path = getCurrentPath()
                path.reset()
                path.moveTo(startX, startY)
                path.quadTo(controlX, controlY, x, y)
            }
            Mode.TEXT -> {
                startX = x
                startY = y

                // TODO: 2021-10-18 커스텀 텍스트뷰 컨트롤 기능 구현중 (최초 사용자가 찍은 화면 좌표값에 텍스트 표출)
                if(tempLayout == null){
                    tempLayout = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    indoorSketchView = layoutInflater.inflate(R.layout.view_indoorsketch_editor_text, null)

                    indoorSketchView?.layoutIndoorSketchTextviewLayout?.let {
                        it.tvIndoorSktetchEditorText.text = getText()
                        it.layoutParams = tempLayout

                        if (it.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        (this.parent as ViewGroup).addView(it)

                        canvasEditTextLayout.add(it)

                        it.x = startX
                        it.y = startY

                        indoorSketchView?.imgindoorSketchEditorClose?.setOnClickListener {
                            logutil.d("닫기")
                            (this.parent as ViewGroup).removeView(indoorSketchView!!.layoutIndoorSketchTextviewLayout)
                        }

                        indoorSketchView?.imgindoorSketchEditorMove?.setOnClickListener {
                            logutil.d("click")
                            _isTextViewMove = !_isTextViewMove

                            //if(_isTextViewMove) (this.parent as ViewGroup).tvIndoorStatus.text = "이동가능" else (this.parent as ViewGroup).tvIndoorStatus.text = "이동 불가"

                            logutil.d("텍스트뷰 무브 상태값: $_isTextViewMove")

                            if(!_isTextViewMove){
                                indoorSketchView?.frmBorder?.background = null
                                indoorSketchView?.indoorButtonGroup?.goneView()
                            } else {
                                indoorSketchView?.frmBorder?.background = context.resources.getDrawable(R.drawable.layout_round_textview_border, null)
                                indoorSketchView?.indoorButtonGroup?.visibleView()
                            }

                            ToastUtil(context).msg("텍스트뷰 무브 상태값: $_isTextViewMove", 500)

                        }

                        it.setOnTouchListener { view, motionEvent ->
                            detectorCompat.onTouchEvent(motionEvent)
                            when (motionEvent.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    view.performClick()
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    if (_isTextViewMove) {
                                        indoorSketchView?.x = event.x
                                        indoorSketchView?.y = event.y
                                        view.performClick()
                                    }
                                }
                            }
                            true
                        }

                    }
                }

            }
        }
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        logutil.d("onFling: $event1 $event2")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        logutil.d("onLongPress: $event")

        //if(_isTextViewMove) (this.parent as ViewGroup).tvIndoorStatus.text = "이동가능" else (this.parent as ViewGroup).tvIndoorStatus.text = "이동 불가"

        logutil.d(canvasEditTextLayout.size.toString())

        canvasEditTextLayout.forEach { obj -> logutil.d(obj.accessibilityClassName.toString())}

        if(indoorSketchView?.indoorButtonGroup?.visibility == GONE) {
            _isTextViewMove = true
            indoorSketchView?.indoorButtonGroup?.visibleView()
            indoorSketchView?.frmBorder?.background = context.resources.getDrawable(R.drawable.layout_round_textview_border, null)
        }
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        logutil.d("onScroll: $event1 $event2")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        logutil.d("onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        logutil.d("onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        logutil.d("onDoubleTap: $event")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        logutil.d("onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        logutil.d("onSingleTapConfirmed: $event")
        return true
    }


    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     *
     * @param event This is argument of onTouchEvent method
     */
    private fun onActionUp(event: MotionEvent) {
        if (isDown) {
            startX = 0f
            startY = 0f
            isDown = false
        }
    }

    /**
     * This method updates the instance of Canvas (View)
     *
     * @param canvas the new instance of Canvas
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Before "drawPath"
        val drawPaint = Paint()

        //bitmap = createBitmap((context as MapActivity).thingIndoorCanvasView.width, (context as MapActivity).thingIndoorCanvasView.height, Config.ARGB_8888)
        bitmap = createBitmap(width, height, Config.ARGB_8888)

        this.canvas = Canvas(bitmap!!)
        this.canvas?.drawColor(baseColor)
        this.canvas?.drawBitmap(bitmap as Bitmap, 0f, 0f, drawPaint)

        for (i in 0 until historyPointer) {
            val path = pathLists[i]
            val paint = paintLists[i]
            canvas.drawPath(path, paint)
        }

        //drawText(canvas)
        this.canvas = canvas

    }


    /**
     * This method set event listener for drawing.
     *
     * @param event the instance of MotionEvent
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> onActionUp(event)
            else -> {
            }
        }

        // Re draw
        this.invalidate()
        return true
    }

    /**
     * This method is getter for mode.
     *
     * @return
     */
    fun getMode(): Mode = mode

    /**
     * This method is setter for mode.
     *
     * @param mode
     */
    fun setMode(mode: Mode) { this.mode = mode }

    /**
     * This method is getter for drawer.
     *
     * @return
     */
    fun getDrawer(): Drawer = drawer

    /**
     * This method is setter for drawer.
     *
     * @param drawer
     */
    fun setDrawer(drawer: Drawer) { this.drawer = drawer }

    /**
     * This method draws canvas again for Undo.
     *
     * @return If Undo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    fun undo(): Boolean {
        return if (historyPointer > 1) {
            historyPointer--
            this.invalidate()
            true
        } else {
            false
        }
    }

    /**
     * This method draws canvas again for Redo.
     *
     * @return If Redo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    fun redo(): Boolean {
        return if (historyPointer < pathLists.size) {
            historyPointer++
            this.invalidate()
            true
        } else {
            false
        }
    }

    /**
     * This method initializes canvas.
     *
     * @return
     */
    fun clear() {
        val path = Path()
        path.moveTo(0f, 0f)
        path.addRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Path.Direction.CCW
        )
        path.close()
        val paint = Paint()
        paint.color = Color.TRANSPARENT
        paint.style = Paint.Style.FILL

        historyPointer = 0
        this.invalidate()

//        this.canvas?.drawBitmap(bitmap as Bitmap, 0f, 0f, Paint())

        this.canvas?.drawColor(this.baseColor)

//        bitmap = createBitmap(
//            (context as MapActivity).thingIndoorCanvasView.width,
//            (context as MapActivity).thingIndoorCanvasView.height,
//            Config.ARGB_8888
//        )
//
//        this.canvas?.drawBitmap(FileUtil(context!!).convertToPNG(bitmap as Bitmap)!!, 0f, 0f, Paint())

        if (historyPointer == pathLists.size) {
            pathLists.add(path)
            paintLists.add(paint)
            historyPointer++
        } else {
            // On the way of Undo or Redo
            pathLists[historyPointer] = path
            paintLists[historyPointer] = paint
            historyPointer++
            var i = historyPointer
            val size = paintLists.size
            while (i < size) {
                pathLists.removeAt(historyPointer)
                paintLists.removeAt(historyPointer)
                i++
            }
        }
        text = ""

        val viewCnt = (this.parent as ViewGroup).childCount

        try{
            for(i in 11 until viewCnt){
                if(i <= viewCnt){
                    logutil.d(i.toString())
                    if(i == 11) {
                        (this.parent as ViewGroup).removeViewAt(i)
                    } else {
                        (this.parent as ViewGroup).removeViewAt(i-1)
                    }
                }
            }

        }catch (e:Exception){
            logutil.d(e.toString())
        }

        // Clear
        this.invalidate()
    }

    /**
     * This method is getter for canvas background color
     *
     * @return
     */
    fun getBaseColor(): Int = baseColor

    /**
     * This method is setter for canvas background color
     *
     * @param color
     */
    fun setBaseColor(color: Int) { baseColor = color }

    /**
     * This method is getter for drawn text.
     *
     * @return
     */
    fun getText(): String = text

    /**
     * This method is setter for drawn text.
     *
     * @param text
     */
    fun setText(text: String) { this.text = "${ (this.parent as ViewGroup).childCount} $text" }

    /**
     * This method is getter for stroke or fill.
     *
     * @return
     */
    fun getPaintStyle(): Paint.Style = paintStyle

    /**
     * This method is setter for stroke or fill.
     *
     * @param style
     */
    fun setPaintStyle(style: Paint.Style) { paintStyle = style }

    /**
     * This method is getter for stroke color.
     *
     * @return
     */
    fun getPaintStrokeColor(): Int = paintStrokeColor

    /**
     * This method is setter for stroke color.
     *
     * @param color
     */
    fun setPaintStrokeColor(color: Int) { paintStrokeColor = color }

    /**
     * This method is getter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @return
     */
    fun getPaintFillColor(): Int = paintFillColor

    /**
     * This method is setter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @param color
     */
    fun setPaintFillColor(color: Int) { paintFillColor = color }

    /**
     * This method is getter for stroke width.
     *
     * @return
     */
    fun getPaintStrokeWidth(): Float = paintStrokeWidth

    /**
     * This method is setter for stroke width.
     *
     * @param width
     */
    fun setPaintStrokeWidth(width: Float) {
        if (width >= 0) {
            paintStrokeWidth = width
        } else {
            paintStrokeWidth = 3f
        }
    }

    /**
     * This method is getter for alpha.
     *
     * @return
     */
    fun getOpacity(): Int = opacity

    /**
     * This method is setter for alpha.
     * The 1st argument must be between 0 and 255.
     *
     * @param opacity
     */
    fun setOpacity(opacity: Int) = if (opacity in 0..255) this.opacity = opacity else this.opacity = 255

    /**
     * This method is getter for amount of blur.
     *
     * @return
     */
    fun getBlur(): Float = blur

    /**
     * This method is setter for amount of blur.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param blur
     */
    fun setBlur(blur: Float) {
        if (blur >= 0) {
            this.blur = blur
        } else {
            this.blur = 0f
        }
    }

    /**
     * This method is getter for line cap.
     *
     * @return
     */
    fun getLineCap(): Paint.Cap = lineCap

    /**
     * This method is setter for line cap.
     *
     * @param cap
     */
    fun setLineCap(cap: Paint.Cap) { lineCap = cap }

    /**
     * This method is getter for font size,
     *
     * @return
     */
    fun getFontSize(): Float = fontSize

    /**
     * This method is setter for font size.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param size
     */
    fun setFontSize(size: Float) = if (size >= 0f) fontSize = size else fontSize = 32f

    /**
     * This method is getter for font-family.
     *
     * @return
     */
    fun getFontFamily(): Typeface? = fontFamily

    /**
     * This method is setter for font-family.
     *
     * @param face
     */
    fun setFontFamily(face: Typeface) { fontFamily = face }

    /**
     * This method gets current canvas as bitmap.
     *
     * @return This is returned as bitmap.
     */
    fun getBitmap(): Bitmap {
        this.isDrawingCacheEnabled = false
        this.isDrawingCacheEnabled = true
        return createBitmap(this.drawingCache)
    }

    /**
     * This method gets current canvas as scaled bitmap.
     *
     * @return This is returned as scaled bitmap.
     */
    fun getScaleBitmap(w: Int, h: Int): Bitmap? {
        this.isDrawingCacheEnabled = false
        this.isDrawingCacheEnabled = true
        return Bitmap.createScaledBitmap(this.drawingCache, w, h, true)
    }

    /**
     * This method draws the designated bitmap to canvas.
     *
     * @param bitmap
     */
    fun drawBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        this.invalidate()
    }

    /**
     * This method draws the designated byte array of bitmap to canvas.
     *
     * @param byteArray This is returned as byte array of bitmap.
     */
    fun drawBitmap(byteArray: ByteArray) {
        this.drawBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
    }

    /**
     * This static method gets the designated bitmap as byte array.
     *
     * @param bitmap
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    fun getBitmapAsByteArray(bitmap: Bitmap, format: CompressFormat?, quality: Int): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * This method gets the bitmap as byte array.
     *
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    fun getBitmapAsByteArray(format: CompressFormat?, quality: Int): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        getBitmap().compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * This method gets the bitmap as byte array.
     * Bitmap format is PNG, and quality is 100.
     *
     * @return This is returned as byte array of bitmap.
     */
    fun getBitmapAsByteArray(): ByteArray? {
        return this.getBitmapAsByteArray(CompressFormat.PNG, 100)
    }

    override fun onDown(event: MotionEvent): Boolean {
        println("onDown: $event")
        return true
    }

}