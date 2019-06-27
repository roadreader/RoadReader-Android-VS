package com.example.roadreader_android_vs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var signUp: Button? = null
    private var login: Button? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private val GSO_ID_TOKEN = "87118424386-qnbbtp8ad2hj41rco3ci1osa06mp31ub.apps.googleusercontent.com"
    private val GSO_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GSO_ID_TOKEN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        val signInButton = findViewById(R.id.sign_in_button) as SignInButton
        signInButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                signUpGoogle()
            }
        })


        email = findViewById(R.id.emailText) as EditText
        password = findViewById(R.id.passwordText) as EditText

        signUp = findViewById(R.id.signup) as Button
        signUp!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                signupUser(email!!.getText().toString(), password!!.getText().toString())
            }
        })

        login = findViewById(R.id.login) as Button
        login!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                if (mAuth!!.getCurrentUser() != null) {
                    startActivity(Intent(this@LoginActivity, ListActivity::class.java))
                }


                /*
                File media = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + File.separator + "RoadReader");
                File [] files = media.listFiles();
                for(int i = 0; i < files.length; i++) {
                    Toast.makeText(LoginActivity.this, files[i].getName(), Toast.LENGTH_SHORT).show();


                }


                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(files[0].getAbsolutePath());
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                Toast.makeText(LoginActivity.this, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), Toast.LENGTH_SHORT).show();
                */


            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.getCurrentUser()

        if (currentUser != null) {
            Toast.makeText(this, "hello " + currentUser!!.getEmail(), Toast.LENGTH_SHORT).show()
        }

        //Intent signup = new Intent(LoginActivity.this, SignupActivity.class);
        //startActivity(signup);

    }

    private fun signUpGoogle() {
        val signInIntent = mGoogleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, GSO_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GSO_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }

            } catch (e: ApiException) {
                Log.w("google sign in", "Google Sign in failed", e)
            }

            //handleSignInResult(task);
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("google sign in", "firebaseAuthWithGoogle:" + acct.getId())

        val credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful()) {
                            Log.d("google sign in", "signInWithCredential:success")
                            val user = mAuth!!.getCurrentUser()
                            Toast.makeText(this@LoginActivity, user!!.getEmail() + " logged in", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, "Google sign in failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            Toast.makeText(this@LoginActivity, account!!.getEmail() + " logged in!", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Sign In", "signInResult:failed code=" + e.getStatusCode())
            Toast.makeText(this@LoginActivity, "Google Authencation failed", Toast.LENGTH_SHORT).show()

        }

    }

    private fun signupUser(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful()) {
                            Log.d("firebase", "signInWithEmail:success")
                            val user = mAuth!!.getCurrentUser()
                            Toast.makeText(this@LoginActivity, user!!.getEmail() + " logged in!", Toast.LENGTH_SHORT).show()

                        } else {
                            Log.w("firebase", task.getException())
                            Toast.makeText(this@LoginActivity, "Authencation failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }
}