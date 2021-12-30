/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.graphics.*
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_document_crop.*
import kotlinx.android.synthetic.main.fragment_document_crop.view.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.scan.DocumentImageEdgePoint
import kr.or.kreb.ncms.mobile.util.ImageUtil
import kr.or.kreb.ncms.mobile.util.visibleView
import kr.or.kreb.ncms.mobile.view.PolygonView
import org.opencv.core.MatOfPoint2f
import java.util.*
import kotlin.collections.ArrayList

private const val IMAGE_FILE_BITMAP ="imageFileByteArray"
private const val LON = "lon"
private const val LAT = "lat"
private const val AZIMUTH = "azimuth"
private const val SAUP_CODE = "saupCode"
private const val BIZ_CODE = "bizCode"
private const val FILE_CODE = "fileCode"
private const val FILE_CODE_NM = "fileCodeNm"


class DocumentCropViewFragment : Fragment() {

    private var imageFile: String? = null
    private var lon: String? = null
    private var lat: String? = null
    private var azimuth: String? = null
    private var saupCode: String? = null
    private var bizCode: String? = null
    private var fileCode: String? = null
    private var fileCodeNm: String? = null

    private lateinit var resultBitmap: Bitmap

    private lateinit var documentImageEdgePoint: DocumentImageEdgePoint

    lateinit var mHolderImageCrop : FrameLayout
    lateinit var mPolygonView : PolygonView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageFile = it.getString(IMAGE_FILE_BITMAP)
            lon = it.getString(LON)
            lat = it.getString(LAT)
            azimuth = it.getString(AZIMUTH)
            saupCode = it.getString(SAUP_CODE)
            bizCode = it.getString(BIZ_CODE)
            fileCode = it.getString(FILE_CODE)
            fileCodeNm = it.getString(FILE_CODE_NM)
        }

        val getByteArr = arguments?.getByteArray(IMAGE_FILE_BITMAP)
        resultBitmap = BitmapFactory.decodeByteArray(getByteArr, 0 , getByteArr!!.size)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_crop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewCropView.setImageBitmap(resultBitmap)

        mHolderImageCrop = view.findViewById(R.id.holderImageCrop)
        mPolygonView = view.findViewById(R.id.polygonView)

        initializeElement(view)

        view.btnImageEnhance.setOnClickListener {

            println("go to document edit")

            val cropDocumentBitmap = getCroppedImage()
            val convertByteArray = ImageUtil.bitmapToByteArray(cropDocumentBitmap)

            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.layoutCamera, DocumentEditViewFragment.newInstance(
                    convertByteArray,
                    lon,
                    lat,
                    azimuth,
                    saupCode,
                    bizCode,
                    fileCode,
                    fileCodeNm
                ))
                .commit()
        }
    }

    private fun initializeElement(view: View) {
        documentImageEdgePoint = DocumentImageEdgePoint()
        view.layoutCameraCropFrame.post { initializeCropping() }
    }

    private fun initializeCropping() {

        val resizeBitmap: Bitmap = scaledBitmap(resultBitmap, mHolderImageCrop.width, mHolderImageCrop.height)
        imageViewCropView.setImageBitmap(resizeBitmap)

        val pointFs: Map<Int?, PointF?>? = getEdgePoints(resizeBitmap)
        mPolygonView.setPoints(pointFs!!)
        mPolygonView.visibleView()
        val padding = 16
        val layoutParams = FrameLayout.LayoutParams(resizeBitmap.width + 2 * padding, resizeBitmap.height + 2 * padding)
        layoutParams.gravity = Gravity.CENTER
        mPolygonView.layoutParams = layoutParams
    }

    fun getCroppedImage(): Bitmap {
        val points = polygonView.getPoints()
        val xRatio = resultBitmap.width.toFloat() / imageViewCropView!!.width
        val yRatio = resultBitmap.height.toFloat() / imageViewCropView!!.height
        val x1 = points[0]!!.x * xRatio
        val x2 = points[1]!!.x * xRatio
        val x3 = points[2]!!.x * xRatio
        val x4 = points[3]!!.x * xRatio
        val y1 = points[0]!!.y * yRatio
        val y2 = points[1]!!.y * yRatio
        val y3 = points[2]!!.y * yRatio
        val y4 = points[3]!!.y * yRatio
        return documentImageEdgePoint.getScannedBitmap(resultBitmap, x1, y1, x2, y2, x3, y3, x4, y4)
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(
            RectF(0.0F, 0.0F, bitmap.width.toFloat(), bitmap.height.toFloat()),
            RectF(0.0F, 0.0F, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getEdgePoints(bitmap: Bitmap): Map<Int?, PointF?>? {
        val pointFs = getContourEdgePoints(bitmap)
        return orderedValidEdgePoints(bitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        val point2f: MatOfPoint2f? = documentImageEdgePoint.getPoint(tempBitmap)
        val result: MutableList<PointF>  = ArrayList()

        if(point2f !== null){
            val points = listOf(*point2f!!.toArray())
            for (i in points.indices) {
                result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
            }
            return result
        }
        return result
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int?, PointF?>? {
        val outlinePoints: MutableMap<Int?, PointF?> = HashMap()
        outlinePoints[0] = PointF(0.0F, 0.0F)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0.0F)
        outlinePoints[2] = PointF(0.0F, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat())
        return outlinePoints
    }

    private fun orderedValidEdgePoints(tempBitmap: Bitmap, pointFs: List<PointF>): Map<Int?, PointF?>? {
        var orderedPoints = mPolygonView.getOrderedPoints(pointFs)
        if (!mPolygonView.isValidShape(orderedPoints!!)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    companion object {

        // cpp data load
        init { System.loadLibrary("opencv_java4") }

        @JvmStatic
        fun newInstance(imageFile: ByteArray, lon: String?, lat: String?, azimuth: String?, saupCode: String?, bizCode: String?, fileCode: String?, fileCodeNm: String?) =
            DocumentCropViewFragment().apply {
                arguments = Bundle().apply {
                    putByteArray(IMAGE_FILE_BITMAP, imageFile)
                    putString(LON, lon)
                    putString(LAT, lat)
                    putString(AZIMUTH, azimuth)
                    putString(SAUP_CODE, saupCode)
                    putString(BIZ_CODE, bizCode)
                    putString(FILE_CODE, fileCode)
                    putString(FILE_CODE_NM, fileCodeNm)
                }
            }
    }
}