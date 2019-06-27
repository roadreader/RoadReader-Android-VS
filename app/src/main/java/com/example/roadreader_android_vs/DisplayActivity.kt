package com.example.roadreader_android_vs

import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView

import com.google.firebase.storage.FirebaseStorage

import java.io.File
import java.io.FileNotFoundException

class DisplayActivity : AppCompatActivity() {

    private var videoView: VideoView? = null
    private var uploadBtn: Button? = null
    private var deleteBtn: Button? = null
    private var videoTitle: TextView? = null
    private var playBtn: ImageButton? = null
    private var video: File? = null

    private val videoInfo: Boolean
        get() {
            val intent = getIntent()
            val bundle = intent.getExtras()
            if (bundle != null) {
                video = bundle!!.get("file") as File
                return true
            }
            return false
        }


    @Override
    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        videoView = findViewById(R.id.videoView)
        videoTitle = findViewById(R.id.videoTitle)
        playBtn = findViewById(R.id.playBtn)

        uploadBtn = findViewById(R.id.uploadBtn)
        uploadBtn!!.setOnClickListener(object : View.OnClickListener() {
            override fun onClick(v: View) {
                try {
                    upload()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        })

        deleteBtn = findViewById(R.id.deleteBtn)
        deleteBtn!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                delete(false)
            }
        })

        initPlayer()

    }

    private fun initPlayer() {
        if (videoInfo) {
            videoTitle!!.setText(video!!.getName())
            videoView!!.setVideoPath(video!!.getAbsolutePath())
            videoView!!.start()
            playBtn!!.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    @Throws(FileNotFoundException::class)
    private fun upload() {
        val tripName = getTimestamp(video!!.getName())
        val tripFile = File(getFilesDir(), "Trips/$tripName.json")
        val request = Request(this)
        request.sendTrip(tripFile, video!!.getAbsolutePath())
        //request.sendVideo(video.getAbsolutePath(), ref);
    }

    fun delete(isSent: Boolean) {
        videoView!!.stopPlayback()
        if (isSent)
            Toast.makeText(this, "Video Sent!", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, "Video Deleted!", Toast.LENGTH_SHORT).show()
        video!!.delete()
        startActivity(Intent(this@DisplayActivity, ListActivity::class.java))
    }

    private fun getTimestamp(videoName: String): String {
        var videoName = videoName
        val startIndex = 4
        val endIndex = videoName.indexOf(".mp4")
        videoName = videoName.substring(startIndex, endIndex)
        return videoName
    }
}
