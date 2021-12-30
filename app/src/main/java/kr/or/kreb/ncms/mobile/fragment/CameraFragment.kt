/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_camera.*
import kr.or.kreb.ncms.mobile.R
import kr.or.kreb.ncms.mobile.base.BaseDialogFragment
import kr.or.kreb.ncms.mobile.databinding.FragmentCameraBinding
import kr.or.kreb.ncms.mobile.util.*
import kr.or.kreb.ncms.mobile.view.CameraView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Long
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.timer


private const val CAMERA_CODE ="cameraCode"
private const val LON ="lon"
private const val LAT ="lat"
private const val SAUP_CODE ="saupCode"
private const val BIZ_CODE ="bizCode"
private const val FILE_CODE ="fileCode"
private const val FILE_CODE_NM ="fileCodeNm"
private const val FILE_WTNCODE_ARR = "wtnCodeArr"

/**
 * 커스텀 카메라 (TextureView 인용)
 * @author hyobin im
 */

class CameraFragment :
    BaseDialogFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate, CameraFragment::class.java.simpleName),
    View.OnClickListener {

    /**
     * Activity -> get Arguments
     */
    private var cameraCode: String? = null // 카메라 코드 (DEFAULT, DOCUMENT)
    private var lonString: String? = null
    private var latString: String? = null
    private var saupCode: String? = null
    private var bizCode: String? = null
    private var fileCode: String? = null
    private var fileCodeNm: String? = null
    private var wtnCodeArr: ArrayList<String>? = null

    /**
     * 커스텀 카메라 변수
     */

    private lateinit var cameraId: String

    private lateinit var cameraView: CameraView
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size

    /**
     * 공통 변수
     */

    var captureImageByteArray:ByteArray? = null
    var resultBitmap:Bitmap? = null

    var gpsUtil:GPSUtil? = null
    var getAzimuth:Float = 0f
    var azimuthTimer:Timer? = null

    /////////////////////////////////////////////// [리스너] ////////////////////////////////////////////////////////////////////

    /**
     * @see surfaceTextureListener
     */

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) = openCamera(width, height)

        @RequiresApi(Build.VERSION_CODES.R)
        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = configureTransform(width, height)

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    /**
     * @see stateCallback 카메라 상태값 콜백 리스너
     */

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release()
            this@CameraFragment.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraFragment.cameraDevice = null
            stopBackgroundThread()
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraFragment.cameraDevice = null
            val activity: Activity? = activity
            activity?.finish()
        }

    }

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var imageReader: ImageReader? = null
    private lateinit var file: File

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file, lonString!!, latString!!))
    }

    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private var state = STATE_PREVIEW

    /**
     * A [Semaphore] 카메라를 닫기 전에 앱이 종료되는 것을 방지합니다.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    /**
     * 현재 카메라 장치가 Flash를 지원하는지 여부.
     */
    private var flashSupported = false

    /**
     * 카메라 센서의 방향
     */
    private var sensorOrientation = 0

    /**
     * A [CameraCaptureSession.CaptureCallback] JPEG 캡처와 관련된 이벤트를 처리합니다.
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit // 카메라 미리보기가 정상적으로 작동하면 아무 작업도 하지마세요
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE는 일부 장치에서 null 일 수 있습니다.
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE는 일부 장치에서 null 일 수 있습니다.
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                // CONTROL_AE_STATE는 일부 장치에서 null 일 수 있습니다.
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    /**
     * 카메라와 관련된 멤버 변수를 설정합니다
     *
     * @param width  카메라 미리보기에 사용할 수있는 크기의 너비
     * @param height 카메라 미리보기에 사용 가능한 크기의 높이
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // TODO: 2021-06-30 후면 카메라 사용  (현장조사)
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // 스틸 이미지 캡처의 경우 사용 가능한 가장 큰 크기를 사용합니다.
                val largest = Collections.max(
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height,
                    ImageFormat.JPEG, /*maxImages*/ 1
                ).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                // 센서와 관련되어 미리보기 회전 진행
                val displayRotation = activity?.windowManager?.defaultDisplay?.rotation

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                val swappedDimensions = areDimensionsSwapped(displayRotation!!)

                val displaySize = Point()
                activity!!.windowManager.defaultDisplay.getSize(displaySize)

                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                // 너무 큰 미리보기 크기를 사용하려고하면 카메라를 초과 할 수 있습니다 (이미지 사이즈 리사이즈가 필요함)
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight,
                    maxPreviewWidth, maxPreviewHeight,
                    largest
                )

                // TextureView(CameraView)의 종횡비를 선택한 미리보기 크기에 맞춥니다.
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    cameraView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    cameraView.setAspectRatio(previewSize.height, previewSize.width)
                }

                // Check if the flash is supported.
                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                return
            }
        } catch (e: CameraAccessException) {
            println(e.toString())
        } catch (e: NullPointerException) {
            // Camera2API가 지원되는 기기에서만 발생한다.
        }

    }

    /**
     * 디바이스 회전에 따라 가로, 세로 변환 여부
     *
     * @param displayRotation 디스플레이의 현재 회전
     * @return 치수가 바뀌면 true, 그렇지 않으면 false입니다.
     */
    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                println("Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    /**
     * 지정된 카메라를 엽니다 [CameraFragment.cameraId].
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun openCamera(width: Int, height: Int) {
        val permission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // 카메라가 열릴 때까지 기다립니다. (예시 2.5초)
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            println(e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }

    }

    /**
     * 카메라 닫기 [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * 백그라운드 스레드 시작 [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = backgroundThread?.looper?.let { Handler(it) }
    }

    /**
     * 백그라운드 스레드 종료 [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 카메라 미리보기를위한 [CameraCaptureSession]을 만듭니다.
     */
    private fun createCameraPreviewSession() {
        try {
            val camera = cameraView.surfaceTexture

            // 기본 버퍼의 크기를 원하는 카메라 미리보기 크기로 구성합니다..
            camera?.setDefaultBufferSize(previewSize.width, previewSize.height)

            //미리보기를 시작하는 데 필요한 출력 표면입니다.
            val surface = Surface(camera)

            // CaptureRequest.Builder 설정
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            // 여기에서 카메라 미리보기를위한 CameraCaptureSession을 만듭니다..
            cameraDevice?.createCaptureSession(listOf(surface, imageReader?.surface), object : CameraCaptureSession.StateCallback() {

                override fun onClosed(session: CameraCaptureSession) {
                    super.onClosed(session)
                    stopBackgroundThread()
                }

                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // 카메라가 닫혀있을 경우
                        if (cameraDevice == null) return

                        // 세션이 준비되면 미리보기를 표시하기 시작합니다.
                        captureSession = cameraCaptureSession
                        try {

                            gpsUtil = GPSUtil(requireContext())

                            // 카메라 열리면서 방위각 센서 실행
                            if(cameraCode =="DEFAULT"){

                                gpsUtil?.getLocation()
                                gpsUtil?.initSensor()

                                azimuthTimer = timer(period = 500, initialDelay = 500) {
                                    getAzimuth = gpsUtil?.azimuth!!
                                    val azimuthStr = gpsUtil?.directionConvertStr(getAzimuth)
                                    activity?.runOnUiThread {
                                        activity?.apply {

//                                            if(textViewCameraLocale != null){
//                                                textViewCameraLocale?.let {
//                                                    it.visibleView()
//                                                    it.text ="대한민국"
//                                                }
//                                            }
//
//                                            if(textViewCamreaLonLat != null){
//                                                textViewCamreaLonLat.let {
//                                                    it.visibleView()
//                                                    it.text ="+${gpsUtil?.getLatitude()} , +${gpsUtil?.getLongitude()}"
//                                                }
//                                            }
//
                                            if(textViewCamreaAzimuth != null){
                                                textViewCamreaAzimuth.let {
                                                    it.visibleView()
                                                    //it.text =" ${getAzimuth.toInt()}° , $azimuthStr"
                                                    it.text =" 방위각: $azimuthStr"
                                                }
                                            }

                                            if(imageViewCamreaAzimuth != null){
                                                imageViewCamreaAzimuth.let {
                                                    it.visibleView()
                                                    it.animate().rotation(-getAzimuth).setDuration(200).start()
                                                }
                                            }


                                        }
                                    }
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    requireActivity().apply {
                                        textViewCamreaAzimuth.visibility = View.GONE
                                        imageViewCamreaAzimuth.visibility = View.GONE
                                    }
                                }
                            }

                            //카메라 미리보기를 위해 자동 초점이 계속되어야합니다.
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE,
                            )

                            // 필요한 경우 플래시가 자동으로 활성화됩니다.
                            setAutoFlash(previewRequestBuilder)

                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(
                                previewRequest,
                                captureCallback, backgroundHandler
                            )

                        } catch (e: CameraAccessException) {
                            println(e.toString())
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        ToastUtil(activity!!).msg("failed", 100)
                    }

                }, null
            )
        } catch (e: CameraAccessException) {
            println(e.toString())
        }

    }

    /**
     * 스틸 사진을 캡처합니다. 이 메서드는 응답을받을 때 호출되어야합니다.
     * [captureCallback] from both [lockFocus]
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun captureStillPicture() {
        try {
            if (activity == null || cameraDevice == null) return
            val rotation = activity?.display?.rotation

            //사진을 찍는 데 사용하는 CaptureRequest.Builder입니다.
            val captureBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            )?.apply {
                addTarget(imageReader?.surface!!)

                // 센서 방향은 대부분의 기기에서 90, 일부 기기에서 270입니다.
                // 이를 고려하여 JPEG를 올바르게 회전해야합니다.
                // 방향이 90 인 장치의 경우 ORIENTATIONS에서 매핑을 반환합니다.
                // 방향이 270 인 장치의 경우 JPEG를 180도 회전해야합니다.

                set(
                    CaptureRequest.JPEG_ORIENTATION,
                    (ORIENTATIONS.get(rotation!!) + sensorOrientation + 270) % 360
                )

                //미리보기와 동일한 AE 및 AF 모드 사용
                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }?.also { setAutoFlash(it) }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    ToastUtil(activity!!).msg("Saved: $file", 100)
                    println("camera saved file -> $file")

                    // 이미지 파일 Meta-Data Get
                    var exif : ExifInterface? = null
                    try{
                        exif = ExifInterface(file.absolutePath)
                    }catch (e : IOException){
                        e.printStackTrace()
                    }

                    val filename = file.name
                    val manufacturer = exif?.getAttribute(ExifInterface.TAG_MAKE)
                    val cameraModel = exif?.getAttribute(ExifInterface.TAG_MODEL)
                    val orientation = when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }

                    val dateTime = exif?.getAttribute(ExifInterface.TAG_DATETIME)
                    val length = exif?.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
                    val width = exif?.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)

                    logUtil.d("File Name : $filename")
                    logUtil.d("model : $manufacturer")
                    logUtil.d("model2 : $cameraModel")
                    logUtil.d("Orientation : $orientation")
                    logUtil.d("dateTime : $dateTime")
                    logUtil.d("Resolution(x*y) : $width x $length")

                    gpsUtil?.getLocation()

                    val lon = gpsUtil?.getLongitude()
                    val lat = gpsUtil?.getLatitude()

                    // 1. 사진 이미지 버퍼 전송
                    val captureBitmap = BitmapFactory.decodeFile(file.path)
                    val matrix = Matrix()
                    matrix.postRotate(orientation.toFloat())
                    resultBitmap = Bitmap.createBitmap(captureBitmap, 0, 0, captureBitmap.width, captureBitmap.height, matrix, true)
                    captureImageByteArray = FileUtil.bitmapToByteArray(resultBitmap!!)
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()

                    when(cameraCode){
                       "DEFAULT" -> {
                           try {
                            transaction
                                .replace(R.id.layoutCamera,
                                    CameraViewFragment.newInstance(
                                        captureImageByteArray!!,
                                        lon.toString(),
                                        lat.toString(),
                                        getAzimuth.toString(),
                                        saupCode,
                                        bizCode,
                                        fileCode,
                                        fileCodeNm,
                                        wtnCodeArr
                                ))
                                //.addToBackStack(null)
                                .commit()
                           } catch (e: Exception) {
                               logUtil.d(e.toString())
                           }

                       }

                       "DOCUMENT" -> {
                           try {
                               transaction
                                   .replace(R.id.layoutCamera, DocumentCropViewFragment.newInstance(
                                       captureImageByteArray!!,
                                       lon.toString(),
                                       lat.toString(),
                                       getAzimuth.toString(),
                                       saupCode,
                                       bizCode,
                                       fileCode,
                                       fileCodeNm
                                   ))
                                   //.addToBackStack(null)
                                   .commit()
                           } catch (e: Exception) {
                               logUtil.d(e.toString())
                           }
                       }
                    }

                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                captureBuilder?.build()?.let { capture(it, captureCallback, null) }
            }
        } catch (e: CameraAccessException) {
            logUtil.d(e.toString())
        }

    }

    /**
     * 필요한 [android.graphics.Matrix] 변환을 `textureView`로 구성합니다.
     * 이 메서드는 카메라 미리보기 크기가
     * setUpCameraOutputs 및`textureView`의 크기도 고정됩니다.
     *
     * @param viewWidth textureView의 너비
     * @param viewHeight textureView의 높이
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        activity ?: return
        val rotation = activity?.display?.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale =
                (viewHeight.toFloat() / previewSize.height).coerceAtLeast(viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        cameraView.setTransform(matrix)
    }

    /**
     * 스틸 이미지 캡처의 1단계 초점을 고정합니다.
     */
    private fun lockFocus() {
        try {
            //초점을 고정하도록 카메라에 지시하는 방법입니다..
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            //잠금을 기다리도록 #captureCallback에 지시하십시오.
            state = STATE_WAITING_LOCK

            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            println(e.toString())
        }

    }

    /**
     * 스틸 이미지 캡처를 위해 사전 캡처 시퀀스를 실행합니다 이 메서드는 다음과 같은 경우에 호출되어야 한다.
     * [lockFocus]에서 [captureCallback]으로 응답을 받습니다.
     */
    private fun runPrecaptureSequence() {
        try {
            // 카메라에 트리거를 지시하는 방법입니다.
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // [captureCallback] 에 사전 캡처 시퀀스가 설정 될 때까지 기다린다.
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            println(e.toString())
        }

    }


    /**
     * 카메라 초점 해제
     */
    private fun unlockFocus() {
        try {
            //자동 초점 트리거 재설정
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
            //그 후 카메라는 미리보기의 정상 상태로 돌아갑니다.
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(
                previewRequest, captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            println(e.toString())
        }

    }

    /**
     * 플래시 자동
     */
    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cameraCode = it.getString(CAMERA_CODE)
            lonString = it.getString(LON)
            latString = it.getString(LAT)
            saupCode = it.getString(SAUP_CODE)
            bizCode = it.getString(BIZ_CODE)
            fileCode = it.getString(FILE_CODE)
            fileCodeNm = it.getString(FILE_CODE_NM)
            wtnCodeArr = it.getStringArrayList(FILE_WTNCODE_ARR)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.imageButtonCamreaLense).setOnClickListener(this)
        //view.findViewById<View>(R.id.info).setOnClickListener(this)

        cameraView = view.findViewById(R.id.customKrebCameraView)
        cameraView.isOpaque = false

        var cameraTextViewTitle: TextView? = null
        cameraTextViewTitle = view.findViewById(R.id.textViewCameraTitle)

        when (cameraCode) {
           "DEFAULT" -> cameraTextViewTitle.text ="현장지원모바일 현장조사 사진 촬영모드입니다."
           "DOCUMENT" -> cameraTextViewTitle.text ="도큐멘트 촬영모드입니다."
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        file = File(activity?.getExternalFilesDir(null),"temp.jpg")
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        /*
         * 화면이 꺼졌다가 다시 켜지면 SurfaceTexture는 이미 사용 가능하며"onSurfaceTextureAvailable"이 호출되지 않습니다. 이 경우 열 수 있습니다.
         * 여기에서 미리보기를 시작합니다. 그렇지 않으면 텍스쳐뷰가 준비 될 때까지 기다립니다.
         */

        if (cameraView.isAvailable) {
            openCamera(cameraView.width, cameraView.height)
        } else {
            cameraView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        stopBackgroundThread()
        closeCamera()
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()

        stopBackgroundThread()
        closeCamera()

        if(activity != null){
            azimuthTimer?.cancel()
            if(cameraCode == "DEFAULT"){
                gpsUtil?.closeSensor()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imageButtonCamreaLense -> lockFocus()
        }
    }

    companion object {

        /**
         * 화면 회전에서 JPEG 방향으로 변환.
         */
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private const val TAG ="CameraFragment"

        /**
         * 카메라 상태 : 카메라 미리보기를 표시합니다.
         */
        private const val STATE_PREVIEW = 0

        /**
         * 카메라 상태 : 초점이 고정 될 때까지 대기 중입니다.
         */
        private const val STATE_WAITING_LOCK = 1

        /**
         * 카메라 상태 : 노출이 사전 캡처 상태가되기를 기다리고 있습니다.
         */
        private const val STATE_WAITING_PRECAPTURE = 2

        /**
         * 카메라 상태 : 노출 상태가 사전 캡처 이외의 상태가되기를 기다리고 있습니다.
         */
        private const val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * 카메라 상태 : 사진이 촬영되었습니다.
         */
        private const val STATE_PICTURE_TAKEN = 4

        /**
         * Camera2 API에서 보장하는 최대 미리보기 너비
         */
        private const val MAX_PREVIEW_WIDTH = 1920

        /**
         * Camera2 API에서 보장하는 최대 미리보기 높이
         */
        private const val MAX_PREVIEW_HEIGHT = 1080

        /**
         * 카메라가 지원하는`크기`의`선택`을 감안할 때 가장 작은 것을 선택하십시오.
         * 적어도 각 텍스처 뷰 크기만큼 크며, 최대 크기는
         * 각각의 최대 크기 및 가로 세로 비율이 지정된 값과 일치합니다. 그렇다면
         * 크기가 존재하지 않습니다. 각 최대 크기만큼 큰 것을 선택하십시오.
         * 크기와 종횡비가 지정된 값과 일치합니다.
         *
         * @param choices           카메라가 의도 한대로 지원하는 크기 목록
         *                          output class
         * @param textureViewWidth  센서 좌표를 기준으로 한 텍스처 뷰의 너비
         * @param textureViewHeight 센서 좌표에 대한 텍스처 뷰의 높이
         * @param maxWidth          선택할 수있는 최대 너비
         * @param maxHeight         선택할 수있는 최대 높이
         * @param aspectRatio       종횡비
         * @return  최적의 '크기'또는 충분히 크지 않은 경우 임의의 크기
         */
        @JvmStatic
        private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ): Size {

            // 최소한 미리보기 Surface만큼 큰 지원되는 해상도 수집
            val bigEnough = ArrayList<Size>()
            // 미리보기 Surface보다 작은 지원되는 해상도 수집
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w
                ) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // 충분히 큰 것 중에서 가장 작은 것을 선택하십시오. 충분히 큰 사람이 없으면
            // 충분히 크지 않은 것 중 가장 큰 것.
            return when {
                bigEnough.size > 0 -> {
                    Collections.min(bigEnough, CompareSizesByArea())
                }
                notBigEnough.size > 0 -> {
                    Collections.max(notBigEnough, CompareSizesByArea())
                }
                else -> {
                    Log.e(TAG,"Couldn't find any suitable preview size")
                    choices[0]
                }
            }
        }

        @JvmStatic
        fun newInstance(cameraCode: String, lon: String?, lat: String?, saupCode: String?, bizCode: String?, fileCode: String?, fileCodeNm: String?, wtnCodeArr: ArrayList<String>?) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    putString(CAMERA_CODE, cameraCode)
                    putString(LON, lon)
                    putString(LAT, lat)
                    putString(SAUP_CODE, saupCode)
                    putString(BIZ_CODE, bizCode)
                    putString(FILE_CODE, fileCode)
                    putString(FILE_CODE_NM, fileCodeNm)
                    putStringArrayList(FILE_WTNCODE_ARR, wtnCodeArr)
                }
            }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////

    internal class CompareSizesByArea : Comparator<Size> {

        // We cast here to ensure the multiplications won't overflow
        override fun compare(lhs: Size, rhs: Size) =
            Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

    }

    open class ImageSaver(
        /**
         * The JPEG image
         */
        private val image: Image,

        /**
         * The file we save the image into.
         */
        private val file: File,
        private val lon:String,
        private val lat:String
    ) : Runnable {

        override fun run() {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(file).apply {
                    write(bytes)
                }
            } catch (e: IOException) {
                println(e.toString())
            } finally {
                image.close()
                output?.let {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        println(e.toString())
                    }
                }
            }
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////
}