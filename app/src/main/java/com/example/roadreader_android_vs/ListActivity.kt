package com.example.roadreader_android_vs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import android.support.v7.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button

class ListActivity : AppCompatActivity() {
    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var mAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<ListAdapter.VideoViewHolder>? = null
    private var layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    private var recordBtn: Button? = null

    internal fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recyclerView) as androidx.recyclerview.widget.RecyclerView
        recyclerView!!.setHasFixedSize(true)

        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView!!.setLayoutManager(layoutManager)

        mAdapter = ListAdapter(this)
        recyclerView!!.setAdapter(mAdapter)

        //recyclerView!!.addItemDecoration(DividerItemDecoration(recyclerView!!.getContext(), DividerItemDecoration.VERTICAL))

        recordBtn = findViewById(R.id.recordBtn)
        recordBtn!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                startActivity(Intent(this@ListActivity, CameraActivity::class.java))
            }
        })
    }
}
