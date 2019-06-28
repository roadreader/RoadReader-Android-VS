package com.example.roadreader_android_vs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth? = null
    private val signup: Button? = null
    private val email: EditText? = null
    private val name: EditText? = null
    private val password: EditText? = null
    private val r_password: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
    }


}
