package com.example.roadreader_android_vs

import java.util.*

internal class GPSPoint {
    val API_KEY = "YK9ZNUS6"

    lateinit var sensor_data: HashMap<String, ArrayList<Float>>
    var time: Long = 0
    var lat: Double = 0.toDouble()
    var lng: Double = 0.toDouble()

    constructor(sensorData: HashMap<String, ArrayList<Float>>, latitude: Double, longitude: Double, timestamp: Long) {

        sensor_data = HashMap(sensorData)
        lat = latitude
        lng = longitude
        time = timestamp
    }

    /**
     * Constructor for testing
     * @param latitude
     * @param longitude
     */
    constructor(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

}
