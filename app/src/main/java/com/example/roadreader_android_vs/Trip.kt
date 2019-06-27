package com.example.roadreader_android_vs


import java.util.ArrayList
import java.util.HashMap

class Trip (val userId: String) : Cloneable {

    internal var gpsPoints: MutableList<GPSPoint>

    init {
        gpsPoints = ArrayList()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        return super.clone()
    }

    /**
     * Creates new GPS instance and adds it to the trip.
     * @param sensor_data Data from accelerometer and gyroscope since last recorded GPS location
     * @param latitude
     * @param longitude
     */
    fun addGPSPoint(sensor_data: HashMap<String, ArrayList<Float>>,
                    latitude: Double, longitude: Double, time: Long) {

        gpsPoints.add(GPSPoint(sensor_data, latitude, longitude, time))

    }

}
