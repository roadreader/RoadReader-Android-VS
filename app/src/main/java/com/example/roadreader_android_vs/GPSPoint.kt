package com.example.roadreader_android_vs

import android.util.Log

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.io.IOException
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.HashMap

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

    /**
     * Uses lat and lng to get the gps coordinate's corresponding square
     * through a get request to what3words
     * @return The name of the Square that the gps coordinates fall under
     */
    /*
    protected String getSquare() throws IOException {
        String req = "https://api.what3words.com/v3/convert-to-3wa?coordinates=" +
                lat +
                "%2C" +
                lng +
                "&key=" +
                API_KEY;
        Request request = new Request();
        String response_string = request.sendGET(req);

        if(response_string.isEmpty())
            return "failed";

        Map<String, Object> jsonMap;
        Gson gson = new Gson();
        Type outputType = new TypeToken<Map<String, Object>>(){}.getType();
        jsonMap = gson.fromJson(response_string, outputType);

        Log.d("GPS", jsonMap.toString());
        System.out.println(jsonMap.toString());

        String square_name = jsonMap.get("words").toString();

        return square_name;
    }
    */
}
