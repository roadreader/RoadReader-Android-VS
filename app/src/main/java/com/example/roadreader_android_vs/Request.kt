package com.example.roadreader_android_vs

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Request {
    internal lateinit var tripId: String
    internal lateinit var path: String
    internal lateinit var display: DisplayActivity

    constructor(d: DisplayActivity) {
        display = d
    }

    constructor() {

    }

    @Throws(IOException::class)
    fun sendGET(s: String): String {
        val url = URL(s)
        val con = url.openConnection() as HttpURLConnection

        con.setRequestMethod("GET")
        con.setRequestProperty("User-Agent", USER_AGENT)
        val responseCode = con.getResponseCode()

        Log.d("GET request", "GET Response Code :: $responseCode")
        System.out.println("GET Response Code :: $responseCode")

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            val `in` = BufferedReader(InputStreamReader(con.getInputStream()))
            val response = StringBuffer()

            while (`in`.readLine() != null) {
                response.append(`in`.readLine())
            }
            `in`.close()

            // log result
            Log.d("GET request", response.toString())
            System.out.println(response.toString())
            con.disconnect()
            return response.toString()

        } else {
            Log.d("GET request", "GET request did not work")
            System.out.println("GET request did not work")
            con.disconnect()
            return ""
        }
    }

    @Throws(IOException::class)
    fun sendPOST(): String {

        return ""
    }

    @Throws(FileNotFoundException::class)
    fun sendTrip(file: File, filePath: String): String {
        path = filePath //get filepath of the trip's corresponding video

        //read trip.json file and convert it to trip class
        val br = BufferedReader(FileReader(file))
        val trip = Gson().fromJson(br, Trip::class.java)

        Log.d("database", "User ID: " + trip.userId)

        val db = FirebaseFirestore.getInstance()
        db.collection("trips").add(trip)
                .addOnSuccessListener(object : OnSuccessListener<DocumentReference> {
                    override fun onSuccess(documentReference: DocumentReference) {
                        Log.d("database", "DocumentSnapshot written with ID: " + documentReference.getId())
                        //trip.setTripId(documentReference.getId());
                        tripId = documentReference.getId()
                        sendVideo(path, trip.userId + "/" + tripId) //send video if trip uploaded
                    }
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        Log.w("database", "Error adding document", e)
                    }
                })

        Log.d("database", "tripId: $tripId")
        return trip.userId + "/" + tripId
    }

    fun sendVideo(filePath: String, ref: String) {

        val file = Uri.fromFile(File(filePath))

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val vidRef = storageRef.child(ref)
        val metadata = StorageMetadata.Builder().build()

        val uploadTask = vidRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(object : OnFailureListener {
            override fun onFailure(exception: Exception) {
                // Handle unsuccessful uploads
                Log.d("database", "Failed to upload video")
            }
        }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.d("database", "Successfully uploaded video")
                display.delete(true)

            }
        })

    }

    companion object {

        private val USER_AGENT = "Mozilla/5.0"
    }
}
