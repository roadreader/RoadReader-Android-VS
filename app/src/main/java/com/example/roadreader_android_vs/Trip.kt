package com.example.roadreader_android_vs


import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Trip (val userId: String) : Cloneable {

    internal var gpsPoints: MutableList<GPSPoint>

    init {
        gpsPoints = ArrayList()
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    private fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val netDate = Date((s.toLong() * 1000))
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
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
