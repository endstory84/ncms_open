/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile

import android.graphics.Bitmap
import kr.or.kreb.ncms.mobile.base.BaseActivity
import kr.or.kreb.ncms.mobile.data.WtnncImage
import kr.or.kreb.ncms.mobile.databinding.ActivityCameraBinding
import kr.or.kreb.ncms.mobile.fragment.CameraFragment
import kr.or.kreb.ncms.mobile.fragment.CameraViewFragment
import kr.or.kreb.ncms.mobile.util.Constants
import kr.or.kreb.ncms.mobile.util.GPSUtil

class CameraActivity :
    BaseActivity<ActivityCameraBinding>(R.layout.activity_camera, CameraActivity::class.java.simpleName),
    CameraViewFragment.CameraImageCallback {

    // common
    private var mGPSUtil = GPSUtil(this)

    var getCameraCode: String? = null

    override fun initViewStart() {

        getCameraCode = intent!!.extras!!.get("cameraCode").toString()
        val saupCode = intent!!.extras!!.get("saupCode").toString()
        val bizCode = intent!!.extras!!.get("bizCode").toString()
        val fileCode = intent!!.extras!!.get("fileCode").toString()
        val fileCodeNm = intent!!.extras!!.get("fileCodeNm").toString()
        val wtnCodeArr = intent!!.extras!!.getStringArrayList("wtnCodeArr")

        log.d("getCameraCode -> $getCameraCode")

        mGPSUtil.getLocation()
        mGPSUtil.initSensor()

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.layoutCamera
                , CameraFragment.newInstance(
                    getCameraCode!!,
                    mGPSUtil.getLongitude().toString(),
                    mGPSUtil.getLatitude().toString(),
                    saupCode,
                    bizCode,
                    fileCode,
                    fileCodeNm,
                    wtnCodeArr
                )
                ,"camera"
            )
            .commit()

    }

    override fun initDataBinding() {}

    override fun initViewFinal() {}

    override fun onResume() {
        super.onResume()
        setPageCode(Constants.CAMERA_ACT)
    }

    override fun onSetImage(
        bitmap: Bitmap?,
        saupCode: String,
        bizCode: String?,
        rmTxt: String,
        fileNameString: String,
        fileCode:String,
        fileCodeNm:String,
        lon: String,
        lat: String,
        azimuth: String
    ) {
        log.d("onSetImage")
        log.d("camera image arr size -> ${Constants.CAMERA_IMAGE_ARR.size}")

        val wtnncImage = WtnncImage(Constants.CAMERA_IMGAE_INDEX, bitmap, saupCode, bizCode!!, rmTxt, fileNameString, fileCode, fileCodeNm, lon, lat, azimuth)

        Constants.CAMERA_IMGAE_INDEX++

        log.d("camera index -> ${Constants.CAMERA_IMGAE_INDEX}")

        if(Constants.CAMERA_IMGAE_INDEX > 5){
            Constants.CAMERA_ADAPTER?.addItem(wtnncImage, bizCode)
        } else {
            Constants.CAMERA_ADAPTER?.updateItem(wtnncImage, Constants.CAMERA_IMGAE_INDEX, bizCode)
        }

        finish()
    }

    override fun onClosed() {
        finish()
    }

}
