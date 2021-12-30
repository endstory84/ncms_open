/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


@SuppressLint("ServiceCast")
class GPSUtil(var context: Context?) :
    LocationListener,
    SensorEventListener {

    var logUtil:LogUtil = LogUtil(GPSUtil::class.java.simpleName)

    // 위도, 경도
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    var azimuth: Float = 0f

    var flag: Boolean = false

    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null

    private lateinit var sensorManager: SensorManager
    private var accelerationSensor: Sensor? = null
    private var magneticSensor: Sensor? = null

    private var accelerationData = FloatArray(3)
    private var magneticData = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)

    private val alpha = 0.97f
    private var azimuthinDegreesStr:String? = null


    override fun onLocationChanged(location: Location) {
        location.let {
            lat = it.latitude
            lon = it.longitude
        }
        flag = true
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event.let {
            when(event?.sensor?.type){
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerationData = event.values
                    accelerationData[0] = alpha * accelerationData[0] + (1 - alpha) * event.values[0]
                    accelerationData[1] = alpha * accelerationData[1] + (1 - alpha) * event.values[1]
                    accelerationData[2] = alpha * accelerationData[2] + (1 - alpha) * event.values[2]
                }
                else -> {
                    magneticData = event!!.values
                    magneticData[0] = alpha * magneticData[0] + (1 - alpha) * event.values[0]
                    magneticData[1] = alpha * magneticData[1] + (1 - alpha) * event.values[1]
                    magneticData[2] = alpha * magneticData[2] + (1 - alpha) * event.values[2]
                }
            }
        }

        val sensorFlag = SensorManager.getRotationMatrix(rotationMatrix, orientationAngles, accelerationData, magneticData)
        if(sensorFlag){
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360
        }
        //logUtil.d(azimuth.toString())

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }

    /**
     * 방위각 센서 init
     */
    fun initSensor() {
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.apply {
            accelerationSensor = getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magneticSensor = getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
        startSensor()
    }

    /**
     * 방위각 센서 실행
     */
    fun startSensor(){
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    /**
     * 방위각 센서 종료
     */
    fun closeSensor(){
        sensorManager.unregisterListener(this)
    }

    /**
     * 방위각 센서 수치 String Convert
     * @param degrees 방위각 센서 value
     */
    fun directionConvertStr(degrees: Float):String {
        azimuthinDegreesStr = when(degrees.toInt()){
            in 0..10 -> "북"
            in 10..35 ->"북북동"
            in 35..57 ->"북동"
            in 57..78 ->"북북동"
            in 78..101 ->"동"
            in 101..123 ->"동남동"
            in 123..146 ->"남동"
            in 147..170 ->"남남동"
            in 171..191 ->"남"
            in 192..213 ->"남남서"
            in 214..236 ->"남서"
            in 237..258 ->"서남서"
            in 259..280 ->"서"
            in 280..303 ->"서북서"
            in 304..326 ->"북서"
            in 327..348 ->"북북서"
            in 349..360 ->"북"
            else -> null
        }
        //logUtil.d(azimuthinDegreesStr.toString())
        return azimuthinDegreesStr!!
    }

    /** ============================================================================================================= */

    fun getLocation() {

        try {
            mLocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED && context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mLocation = mLocationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if ((ContextCompat.checkSelfPermission(
                    context!!, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_CODE
                )
            }
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5f, this)

        } catch (e: Exception) {
            TAG?.let { LogUtil(it).e(e.toString()) };
        }
    }

    // 위도
    fun getLatitude(): Double {
        mLocation?.let { lat = it!!.latitude }
        return lat
    }

    // 경도
    fun getLongitude(): Double {
        mLocation?.let { lon = it!!.longitude }
        return lon
    }

    companion object {

        @Volatile
        private var instance: GPSUtil? = null

        @JvmStatic
        fun getInstance(context: Context): GPSUtil =
            instance ?: synchronized(this) {
                instance ?: GPSUtil(context).also {
                    instance = it
                }
            }

        private val TAG: String? = GPSUtil::class.simpleName
        private const val LOCATION_PERMISSION_CODE = 2000
    }

}