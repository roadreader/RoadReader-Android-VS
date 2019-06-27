package com.example.roadreader_android_vs

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import roadreader.roadreader_android.ListAdapter.VideoViewHolder

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by chadlohrli on 5/13/19.
 */

class ListAdapter(private val context: Context) : RecyclerView.Adapter<ListAdapter.VideoViewHolder>() {

    private val videos: Array<File>?

    val itemCount: Int
        @Override
        get() = videos?.size ?: 0

    class VideoViewHolder internal constructor(item: View) : RecyclerView.ViewHolder(item) {

        internal var videoName: TextView
        internal var videoTimestamp: TextView
        internal var videoLength: TextView
        internal var videoSnapshot: ImageView

        init {
            videoName = item.findViewById(R.id.videoName)
            videoTimestamp = item.findViewById(R.id.videoTimeStamp)
            videoLength = item.findViewById(R.id.videoLength)
            videoSnapshot = item.findViewById(R.id.videoImageView)
        }
    }


    init {

        //grab all videos in folder
        val media = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + File.separator + "RoadReader")
        videos = media.listFiles()

    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): VideoViewHolder {

        val v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_list_item, viewGroup, false)
        return VideoViewHolder(v)

    }


    override fun onBindViewHolder(videoViewHolder: VideoViewHolder, i: Int) {

        //get snapshot
        val bMap = ThumbnailUtils.createVideoThumbnail(videos!![i].getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND)
        videoViewHolder.videoSnapshot.setImageBitmap(bMap)

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(videos[i].getAbsolutePath())
        var str_duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = Integer.valueOf(str_duration) / 1000
        str_duration = String.valueOf(duration) + "s"
        videoViewHolder.videoLength.setText(str_duration)

        var videoName = videos[i].getName()
        videoViewHolder.videoName.setText(videoName)

        //parse out VID_
        val startIndex = 4
        val endIndex = videoName.indexOf(".mp4")
        videoName = videoName.substring(startIndex, endIndex)
        val unixTime = Integer.valueOf(videoName)
        val sdf = SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss a", Locale.US)
        val readableDate = sdf.format(unixTime * 1000L)
        videoViewHolder.videoTimestamp.setText(readableDate)

        //transition click
        videoViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, DisplayActivity::class.java)
                intent.putExtra("file", videos[i])
                context.startActivity(intent)
            }
        })


    }

}
