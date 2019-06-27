package com.example.roadreader_android_vs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log

class GPS(context: Context, id: String) : LocationListener {

    internal var locationManager: LocationManager
    internal var locationListener: LocationListener
    internal var sensor: SensorListener
    internal var trip: Trip
    internal var gpsTask: GPSTask

    private val isLocationEnabled: Boolean
        get() =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

    init {

        trip = Trip(id)
        sensor = SensorListener(context)

        val start_time = System.currentTimeMillis()

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val lng = location.longitude
                val lat = location.latitude
                val time = System.currentTimeMillis() - start_time
                Log.d(
                    "trip", "lat: " + lat + ", lng: " + lng + ", " +
                            "time: " + time + "\n"
                )
                try {
                    Log.d("trip", "Adding GPS Point...")
                    trip.addGPSPoint(sensor.get_sensor_data(), lat, lng, time)
                    Log.d("trip", "Added.")
                } catch (e: Exception) {
                    Log.d("trip", "Could not add GPS Point")
                    Log.d("trip", e.toString())
                    e.printStackTrace()
                    System.exit(-1)
                }

                sensor.reset_sensor_data()

            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

            override fun onProviderEnabled(s: String) {}

            override fun onProviderDisabled(s: String) {}
        }

        val locationProvider = LocationManager.NETWORK_PROVIDER
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("trip", "Location disabled\n")

        }
        gpsTask = GPSTask()
        sensor.start()
        gpsTask.execute()
        //locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        //locationManager.requestSingleUpdate(locationProvider, locationListener);

    }

    fun track_on(context: Context) {

    }

    fun track_off(context: Context) {

    }


    internal inner class GPSTask : AsyncTask<Void, Void, Boolean>() {

        @Throws(SecurityException::class)
        override fun onPreExecute() {
        }

        override fun doInBackground(vararg voids: Void): Boolean? {

            val locationProvider = LocationManager.GPS_PROVIDER

            Looper.prepare()


            try {
                Log.d("trip", "Updating Location...")
                locationManager.requestLocationUpdates(locationProvider, 1000, 0f, locationListener)
                Log.d("trip", "Location Updated")
            } catch (s: SecurityException) {
                Log.d("trip", "Failed to get GPS permission")
                return false
            }


            Looper.loop()

            return true
        }
    }


    fun resume() {
        sensor.resume()
    }

    fun start() {
        sensor.start()
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
        gpsTask.cancel(true)
        sensor.stop()
    }

    protected fun pause() {
        sensor.pause()
    }

    override fun onLocationChanged(location: Location) {
        //locationListener.onLocationChanged(location);
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

    }

    override fun onProviderEnabled(s: String) {

    }

    override fun onProviderDisabled(s: String) {

    }
}
