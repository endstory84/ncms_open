/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.webkit.MimeTypeMap
import java.io.*

object FileUtil {

    /**
     * 디렉토리 생성
     */
    fun createDir(path: String?): String? {
        if (isStringEmpty(path)) return null
        val file = File(path)
        if (!file.exists()) {
            if (!file.mkdirs()) return null
        }
        return path
    }

    private fun createTempFile(): File? {
        var tempFile: File? = null
        try {
            tempFile = File.createTempFile("temp_file",".png")
        } catch (e: java.lang.Exception) {
        }
        return tempFile
    }

    /**
     * @description  파일생성
     */
    fun createFile(path: String?, fileName: String?): File {
        var file: File? = null
        try {
            createDir(path)
            file = File(path, fileName)
        } catch (e: Exception) {
            throw IllegalAccessException(e.toString())
        } finally {
            assert(file != null)
            file!!.deleteOnExit()
        }
        return file
    }

    fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun getBitmapFromView(view: View, defaultColor: Int): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(defaultColor)
        view.draw(canvas)
        return bitmap
    }


    /**
     * @description Bimtmap -> PNG
     * @param image
     */
    fun convertToPNG(image: Bitmap): Bitmap? {
        val imageFile = createTempFile()
        val outStream: FileOutputStream?
        val byteArrayOutputStream = ByteArrayOutputStream()
        val byteArr = byteArrayOutputStream.toByteArray()

        try {
            outStream = FileOutputStream(imageFile)
            image.compress(CompressFormat.PNG, 50, outStream)
            outStream.write(byteArr)
            outStream.flush()
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeFile(imageFile?.absolutePath)
    }

    /**
     * Bitmap 다운로드
     */

    fun saveBitmapToFileCache(bitmap: Bitmap, strFilePath: String) {
        val fileCacheItem = File(strFilePath)
        var out: OutputStream? = null
        try {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)
            bitmap.compress(CompressFormat.PNG, 35, out)
            out.flush()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * PDF 다운로드
     */

    fun savePdfToFileCache(inputStream: InputStream, strFilePath: String) {
        val fileOutputStream = FileOutputStream(strFilePath)
        var data: Int
        while (inputStream.read().also { data = it } >= 0) {
            fileOutputStream.write(data)
        }
        inputStream.close()
    }

    /**
     * @param bitmap -> ByteArray
     */
    fun bitmapToByteArray(bitmap: Bitmap):ByteArray{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 25, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     *  Bitmap 픽셀단위로 백그라운드 투명화 처리
     *  @param src
     */

    fun replaceColor(src: Bitmap?): Bitmap? {
        if (src == null) return null
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, 1 * width, 0, 0, width, height)
        for (x in pixels.indices) {
            //    pixels[x] = ~(pixels[x] << 8 & 0xFF000000) & Color.BLACK;
            if (pixels[x] == Color.WHITE) pixels[x] = 0
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    /**
     *  파일확장자 구하기
     */
    fun getExtension(fileStr: String?): String?  = fileStr?.substring(fileStr.lastIndexOf(".") + 1, fileStr.length)

    /**
     *  파일확장자 체크하기 (파일)
     */
    fun getMimeType(fileName: String?): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = getExtension(fileName)?.toLowerCase()
        val mimeType = mimeTypeMap.getMimeTypeFromExtension(extension)
        return mimeType.toString()
    }

    /**
     *  파일확장자 체크하기 (URl)
     */
    fun getUrlMimeType(urlString: String?): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = MimeTypeMap.getFileExtensionFromUrl(urlString)
        val mimeType = mimeTypeMap.getMimeTypeFromExtension(extension)
        return mimeType.toString()
    }
}