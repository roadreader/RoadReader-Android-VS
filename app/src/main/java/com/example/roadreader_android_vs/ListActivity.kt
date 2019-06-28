package com.example.roadreader_android_vs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
//import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button

class ListActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var recordBtn: Button? = null

    internal fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
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
