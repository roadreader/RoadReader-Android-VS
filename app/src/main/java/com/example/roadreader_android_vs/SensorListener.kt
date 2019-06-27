package com.example.roadreader_android_vs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import java.io.File
import java.io.FileWriter
import java.util.ArrayList
import java.util.HashMap


class SensorListener
/**
 * Constructor for SensorListener
 * @param context Context for CameraActivity
 */
(context: Context) : SensorEventListener {

    protected var sensorManager: SensorManager
    protected var accelerometer: Sensor
    protected var gyroscope: Sensor
    protected var ax: ArrayList<Float>
    protected var ay: ArrayList<Float>
    protected var az: ArrayList<Float>
    protected var gx: ArrayList<Float>
    protected var gy: ArrayList<Float>
    protected var gz: ArrayList<Float>
    internal var accel_writer: FileWriter? = null
    internal var gyro_writer: FileWriter? = null
    internal var sensor_data: HashMap<String, ArrayList<Float>>


    init {

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensor_data = HashMap()
        ax = ArrayList()
        ay = ArrayList()
        az = ArrayList()
        gx = ArrayList()
        gy = ArrayList()
        gz = ArrayList()

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() === Sensor.TYPE_ACCELEROMETER) {
            ax.add(event.values[0]  )
            ay.add(event.values[1])
            az.add(event.values[2])
            Log.d("accelerometer", event.values[0] as String + " " + event.values[1] + " " + event.values[2] + "\n")

        } else if (event.sensor.getType() === Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0])
            gy.add(event.values[1])
            gz.add(event.values[2])
            Log.d("gyroscope", event.values[0] as String + " " + event.values[1] + " " +
                    event.values[2] + "\n")

        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //Safe not to implement
    }

    fun resume() {
        try {
            //File accel = new File()
            accel_writer = FileWriter("accel.txt", true)
            gyro_writer = FileWriter("gyro.txt", true)
        } catch (e: Exception) {
        }

    }

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, gyroscope)
        sensorManager.unregisterListener(this)
    }

    fun pause() {
        if (accel_writer != null) {
            try {
                accel_writer!!.close()
            } catch (e: Exception) {
            }

        }

        if (gyro_writer != null) {
            try {
                gyro_writer!!.close()
            } catch (e: Exception) {
            }

        }
    }

    fun get_sensor_data(): HashMap<String, ArrayList<Float>> {
        sensor_data.put("ax", ArrayList<Float>(ax))
        sensor_data.put("ay", ArrayList<Float>(ay))
        sensor_data.put("az", ArrayList<Float>(az))
        sensor_data.put("gx", ArrayList<Float>(gx))
        sensor_data.put("gy", ArrayList<Float>(gy))
        sensor_data.put("gz", ArrayList<Float>(gz))
        Log.d("trip", "ax: $ax")
        return HashMap(sensor_data)
    }

    fun reset_sensor_data() {
        sensor_data = HashMap()
        ax = ArrayList()
        ay = ArrayList()
        az = ArrayList()
        gx = ArrayList()
        gy = ArrayList()
        gz = ArrayList()
    }


}
