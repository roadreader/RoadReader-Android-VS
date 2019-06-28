@file:Suppress("DEPRECATION")

package com.example.roadreader_android_vs

import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.example.roadreader_android_vs.media.CameraHelper
import com.example.roadreader_android_vs.media.CameraPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CameraActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mCamera: Camera? = null
    private var mCameraInfo: Camera.CameraInfo? = null
    private var profile: CamcorderProfile? = null
    private var layout: FrameLayout? = null
    private var cameraPreview: CameraPreview? = null
    private var mPreview: TextureView? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private var outputStream: OutputStream? = null

    private var isRecording = false
    private var captureButton: Button? = null
    private val canRecord = false
    internal lateinit var user: FirebaseUser

    internal lateinit var timeStamp: String

    private var gps: GPS? = null

    private val requiredPermissions = arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mPreview = findViewById(R.id.textureView) as TextureView
        captureButton = findViewById(R.id.button_capture) as Button
        layout = findViewById(R.id.camera_preview) as FrameLayout

        if (areCameraPermissionGranted()) {
            initCamera()
        } else {
            requestCameraPermissions()
        }

    }

    private fun initCamera() {

        mCamera = CameraHelper.getDefaultBackFacingCameraInstance()
        mCameraInfo = CameraHelper.getDefaultCameraInfo()

        val defaultRotation = this.getWindowManager().getDefaultDisplay().getRotation()
        val correctRotation = CameraHelper.getCorrectCameraOrientation(defaultRotation, mCamera, mCameraInfo)

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        val parameters = mCamera!!.getParameters()

        parameters.setRotation(correctRotation)
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)

        mCamera!!.setParameters(parameters)
        mCamera = CameraHelper.getDefaultBackFacingCameraInstance()
        cameraPreview = CameraPreview(this, mCamera)
        layout!!.addView(cameraPreview)
    }


    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases [android.media.MediaRecorder] and [android.hardware.Camera]. When not recording,
     * it prepares the [android.media.MediaRecorder] and starts recording.
     *
     * @param view the view generating the event.
     */
    fun onCaptureClick(view: View) {

        if (areCameraPermissionGranted()) {
            Log.d("camera", "record button pressed")
            startCapture()
        } else {
            requestCameraPermissions()
        }
    }

    private fun startCapture() {

        if (isRecording)
            Log.d("camera", "isrecording")
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder!!.stop()  // stop the recording
            } catch (e: RuntimeException) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()")

                mOutputFile!!.delete()
            }

            var trip: Trip
            try {
                trip = gps!!.trip.clone() as Trip
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
                trip = gps!!.trip
                Log.d("RoadReader", "failed to clone trip")
            }

            gps!!.stop()

            val gson = Gson()

            val tripInternalDir = File(getFilesDir(), "Trips")
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!tripInternalDir.exists()) {
                if (!tripInternalDir.mkdirs()) {
                    Log.d("RoadReader", "failed to create directory")
                }
            }

            try {
                val tripFile = File(tripInternalDir, "$timeStamp.json")
                //gson.toJson(trip, new FileWriter(tripFile));
                Log.d("trip", "Writing trip to file")
                outputStream = FileOutputStream(getFilesDir() as String + "/" + "Trips/" + tripFile.getName())
                //outputStream = openFileOutputtripFile.getAbsolutePath(), Context.MODE_PRIVATE);
                outputStream!!.write(gson.toJson(trip).toByteArray())
                outputStream!!.close()
                Log.d("trip", "Trip to String:")
                Log.d("trip", gson.toJson(trip))
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("trip", "failed to write trip to file")
            }


            releaseMediaRecorder() // release the MediaRecorder object
            mCamera!!.lock()         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture")
            isRecording = false
            releaseCamera()
            releaseMediaRecorder()
            //transition to listView
            startActivity(Intent(this@CameraActivity, ListActivity::class.java))
        } else {

            timeStamp = (System.currentTimeMillis() / 1000L) as String

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            MediaPrepareTask().execute(null, null, null)
            gps = GPS(this, user.getUid())
            // END_INCLUDE(prepare_start_media_recorder)

        }
    }

    private fun setCaptureButtonText(title: String) {
        captureButton!!.setText(title)
    }

    protected override fun onResume() {
        super.onResume()
        if (gps != null)
            gps!!.resume()
    }

    protected override fun onStart() {
        super.onStart()
        user = FirebaseAuth.getInstance().getCurrentUser()!!
        if (gps != null)
            gps!!.start()
    }

    protected override fun onPause() {
        super.onPause()
        layout!!.removeAllViews()
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder()
        // release the camera immediately on pause event
        releaseCamera()
    }

    protected override fun onStop() {
        super.onStop()
        if (gps != null)
            gps!!.stop()
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder!!.reset()
            // release the recorder object
            mMediaRecorder!!.release()
            mMediaRecorder = null
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera!!.lock()
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera!!.release()
            mCamera = null
        }
    }

    private fun prepareVideoRecorder(): Boolean {

        mCamera = CameraHelper.getDefaultBackFacingCameraInstance()
        mCameraInfo = CameraHelper.getDefaultCameraInfo()

        val defaultRotation = this.getWindowManager().getDefaultDisplay().getRotation()
        val correctRotation = CameraHelper.getCorrectCameraOrientation(defaultRotation, mCamera, mCameraInfo)

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        val parameters = mCamera!!.getParameters()
        val mSupportedPreviewSizes = parameters.getSupportedPreviewSizes()
        val mSupportedVideoSizes = parameters.getSupportedVideoSizes()
        val optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview!!.getWidth(), mPreview!!.getHeight())

        parameters.setRotation(correctRotation)
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)


        // Use the same size for recording profile.
        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)
        profile!!.videoFrameWidth = optimalSize.width
        profile!!.videoFrameHeight = optimalSize.height

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)
        mCamera!!.setParameters(parameters)

        // BEGIN_INCLUDE (configure_preview)
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera!!.setPreviewTexture(mPreview!!.getSurfaceTexture())
        } catch (e: IOException) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.message)
            return false
        }

        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = MediaRecorder()

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera!!.unlock()
        mMediaRecorder!!.setCamera(mCamera)

        // Step 2: Set sources
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder!!.setProfile(profile)
        mMediaRecorder!!.setCaptureRate(5.0)

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO, CameraHelper.EXTERNAL_SAVE, timeStamp)

        if (mOutputFile == null) {
            return false
        }
        mMediaRecorder!!.setOutputFile(mOutputFile!!.getPath())
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        }

        return true
    }

    private fun areCameraPermissionGranted(): Boolean {

        for (permission in requiredPermissions) {
            if (!(ActivityCompat.checkSelfPermission(this, permission) === PackageManager.PERMISSION_GRANTED)) {
                return false
            }
        }
        return true
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                MEDIA_RECORDER_REQUEST)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {

        if (MEDIA_RECORDER_REQUEST != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        var areAllPermissionsGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                areAllPermissionsGranted = false
                break
            }
        }

        if (areAllPermissionsGranted) {
            initCamera()
        } else {
            // User denied one or more of the permissions, without these we cannot record
            // Show a toast to inform the user.
            Toast.makeText(getApplicationContext(),
                    getString(R.string.need_camera_permissions),
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Asynchronous task for preparing the [android.media.MediaRecorder] since it's a long blocking
     * operation.
     */
    internal inner class MediaPrepareTask : AsyncTask<Void, Void, Boolean>() {

        @Override
        protected override fun doInBackground(vararg voids: Void): Boolean {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder!!.start()

                isRecording = true
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder()
                return false
            }
            return true
        }

        override fun onPostExecute(result: Boolean) {
            if (!result) {
                this@CameraActivity.finish()
            }
            // inform the user that recording has started
            setCaptureButtonText("Stop")

        }
    }

    companion object {

        private val MEDIA_RECORDER_REQUEST = 0
        private val TAG = "Recorder"
    }

}


