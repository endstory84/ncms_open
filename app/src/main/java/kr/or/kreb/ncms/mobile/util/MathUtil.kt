/*
 * Create by sgablc team.eco-chain on 2021.
 * Copyright (c) 2021. sgablc. All rights reserved.
 */

package kr.or.kreb.ncms.mobile.util

import com.carto.core.MapPos
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import kotlin.math.*

object MathUtil {

    @JvmStatic
    fun toMatOfPointInt(mat: MatOfPoint2f): MatOfPoint {
        val matInt = MatOfPoint()
        mat.convertTo(matInt, CvType.CV_32S)
        return matInt
    }

    @JvmStatic
    fun toMatOfPointFloat(mat: MatOfPoint): MatOfPoint2f {
        val matFloat = MatOfPoint2f()
        mat.convertTo(matFloat, CvType.CV_32FC2)
        return matFloat
    }

    @JvmStatic
    fun angle(p1: Point, p2: Point, p0: Point): Double {
        val dx1 = p1.x - p0.x
        val dy1 = p1.y - p0.y
        val dx2 = p2.x - p0.x
        val dy2 = p2.y - p0.y
        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }

    @JvmStatic
    fun scaleRectangle(original: MatOfPoint2f, scale: Double): MatOfPoint2f {
        val originalPoints = original.toList()
        val resultPoints: MutableList<Point> = ArrayList()
        for (point in originalPoints) {
            resultPoints.add(Point(point.x * scale, point.y * scale))
        }
        val result = MatOfPoint2f()
        result.fromList(resultPoints)
        return result
    }

    fun layersForArea(data: MutableList<MapPos>, radius: Double): Double {

        val dataSize = data.size

        if(dataSize < 3) {
            return 0.0
        } else {
            var total = 0.0
            var firstTanLat: Double
            var firstLng: Double
            val lastPos = data[dataSize-1]

            firstTanLat = tan((1.5707963267948966 - Math.toRadians(lastPos.y)) / 2.0)
            firstLng = Math.toRadians(lastPos.x)

            for(pos in data) {
                val tanLat = tan((1.5707963267948966 - Math.toRadians(pos.y)) / 2.0)
                val lng = Math.toRadians(pos.x)
                total += polaygonArea(tanLat, lng, firstTanLat, firstLng)
                firstTanLat = tanLat
                firstLng = lng

            }
//            Math.abs(computeSignedArea(path))
            return abs(total * radius * radius)


        }

    }

    /**
     * 면적 및 거리 계산 수식
     */

    private fun polaygonArea(tan1: Double, lng1: Double, tan2: Double, lng2: Double): Double {

        val areaLng = lng1 - lng2
        val t = tan1 * tan2

        return 2.0 * atan2(t * sin(areaLng), 1.0 + t * cos(areaLng))
    }

    fun getLineDistance(pos1: MapPos, pos2: MapPos, radius: Double): Double {
        val fromLongRadians = Math.toRadians(pos1.x)
        val fromLatRadians = Math.toRadians(pos1.y)
        val toLongRadians = Math.toRadians(pos2.x)
        val toLatRaidians = Math.toRadians(pos2.y)

        val distanceRadians = getDistanceRadians(fromLatRadians, toLatRaidians, fromLongRadians - toLongRadians)

        return arcHav(distanceRadians) * radius
    }

    private fun getDistanceRadians(lat1: Double, lat2: Double, dLng: Double): Double = hav(lat1-lat2) + hav(dLng) * cos(lat1) * cos(lat2)

    private fun hav(x: Double): Double { val sinHalf = sin(x * 0.5); return sinHalf * sinHalf }

    private fun arcHav(x: Double): Double = 2.0 * asin(sqrt(x))
}