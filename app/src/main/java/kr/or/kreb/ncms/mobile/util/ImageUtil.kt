/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream


object ImageUtil {

    /**
     * bitmap -> ByteArray
     * @param bitmap
     */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, stream)
        return stream.toByteArray()
    }

    /**
     * byteArray -> bitmap
     * @param byteArray
     */
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    /**
     * 이미지 회전
     * @param original
     * @param angle
     */
    @JvmStatic
    fun rotateBitmap(original: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    }

    /**
     * 이미지 압축
     * @param bitmap
     */
    fun compressBitmap(bitmap: Bitmap): Bitmap? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * bitmap -> Mat(OpenCV 참조)
     * @param bitmap
     */
    @JvmStatic
    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(4.0))
        val bitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bitmap32, mat)
        return mat
    }

    /**
     * Mat(OpenCV 참조) -> bitmap
     * @param mat
     */
    @JvmStatic
    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

//    fun processPicture(picture: Mat) {
//        val img = Imgcodecs.imdecode(picture, Imgcodecs.IMREAD_UNCHANGED)
//        picture.release()
//        println("processPicture - imported image" + img.size().width +"x" + img.size().height)
//        val doc = detectDocument(img)
//        //mMainActivity.saveDocument(doc)
//        doc!!.release()
//        picture.release()
//        //mMainActivity.setImageProcessorBusy(false)
//        //mMainActivity.waitSpinnerInvisible()
//    }
//
//    private fun findContours(src: Mat): ArrayList<MatOfPoint> {
//        var grayImage: Mat?
//        var cannedImage: Mat?
//        var resizedImage: Mat?
//        val ratio = src.size().height / 500
//        val height = java.lang.Double.valueOf(src.size().height / ratio).toInt()
//        val width = java.lang.Double.valueOf(src.size().width / ratio).toInt()
//        val size = Size(width.toDouble(), height.toDouble())
//        resizedImage = Mat(size, CvType.CV_8UC4)
//        grayImage = Mat(size, CvType.CV_8UC4)
//        cannedImage = Mat(size, CvType.CV_8UC1)
//        Imgproc.resize(src, resizedImage, size)
//        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4)
//        Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
//        Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
//        val contours = ArrayList<MatOfPoint>()
//        val hierarchy = Mat()
//        findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
//        hierarchy.release()
//        contours.sortWith { lhs, rhs ->
//            java.lang.Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs))
//        }
//        resizedImage.release()
//        grayImage.release()
//        cannedImage.release()
//        return contours
//    }
//
//    private fun enhanceDocument(src: Mat) {
//        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY)
//        Imgproc.adaptiveThreshold(
//            src,
//            src,
//            255.0,
//            Imgproc.ADAPTIVE_THRESH_MEAN_C,
//            Imgproc.THRESH_BINARY,
//            15,
//            15.0
//        )
//    }
//
//    private fun detectDocument(inputRgba: Mat): ScannedDocument? {
//        val contours: ArrayList<MatOfPoint> = findContours(inputRgba)
//        val sd = ScannedDocument(inputRgba)
//        val doc: Mat = Mat(inputRgba.size(), CvType.CV_8UC4)
//        inputRgba.copyTo(doc)
//        enhanceDocument(doc)
//        return sd.setProcessed(doc)
//    }
//
//    private fun detectPreviewDocument(inputRgba: Mat): Boolean {
//        val contours = findContours(inputRgba)
//        val quad: Quadrilateral = getQuadrilateral(contours, inputRgba.size())
//        mPreviewPoints = null
//        mPreviewSize = inputRgba.size()
//        if (quad != null) {
//            val rescaledPoints = arrayOfNulls<Point>(4)
//            val ratio = inputRgba.size().height / 500
//            for (i in 0..3) {
//                val x: Int = java.lang.Double.valueOf(quad.points.get(i).x * ratio).toInt()
//                val y: Int = java.lang.Double.valueOf(quad.points.get(i).y * ratio).toInt()
//                if (mBugRotate) {
//                    rescaledPoints[(i + 2) % 4] =
//                        Point(Math.abs(x - mPreviewSize.width), Math.abs(y - mPreviewSize.height))
//                } else {
//                    rescaledPoints[i] = Point(x.toDouble(), y.toDouble())
//                }
//            }
//            mPreviewPoints = rescaledPoints
//            drawDocumentBox(mPreviewPoints, mPreviewSize)
//            Log.d(com.myapps.documentscanner.ImageProcessor.TAG,
//                quad.points.get(0).toString().toString() +" ," + quad.points.get(1)
//                    .toString() +" ," + quad.points.get(2).toString() +" ," + quad.points.get(3).toString()
//            )
//            return true
//        }
//        mMainActivity.getHUD().clear()
//        mMainActivity.invalidateHUD()
//        return false
//    }

    /**
     * @description 이미지 프로세싱 (매직컬러, 블랙&화이트, 그레이스케일)
     */

    fun setMagicColorBitmap(bitmap: Bitmap): Mat {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4, Scalar(4.0))
        val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(tempBitmap, mat)

        val dst = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(1.0))
        Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGBA2mRGBA, 1)

        val alpha = 1.9
        val beta = -80.0
        dst.convertTo(dst, -1, alpha, beta)
        return dst
    }

    fun setBWBitmap(bitmap: Bitmap): Mat {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(4.0))
        val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(tempBitmap, mat)

        val grayMat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(1.0))
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY, 1)

        val theresholdMat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(1.0))
        Imgproc.threshold(grayMat, theresholdMat, 128.0, 255.0, Imgproc.THRESH_BINARY)
        return theresholdMat
    }

    fun setGrayScaleBitmap(bitmap: Bitmap): Mat {
        val mat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        return mat
    }

}