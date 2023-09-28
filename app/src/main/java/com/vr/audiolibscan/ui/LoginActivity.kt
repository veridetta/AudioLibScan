package com.vr.audiolibscan.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.audiolibscan.R
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.AdminActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    // Declare UI elements
    private lateinit var buttonLogin: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        clickView()
    }

    fun initView(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Initialize UI elements
        buttonLogin = findViewById(R.id.buttonLogin)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        progressBar = findViewById(R.id.progressBar)

    }
    fun clickView(){
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            progressBar.visibility = View.VISIBLE

            // Authenticate using Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // Check if user's email is verified
                            Log.d("verified: ","email is verified");
                            firestore.collection("users").document(user!!.uid)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val userRole = documentSnapshot.getString("role")

                                    // Save user role to SharedPreferences
                                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isLogin", true)
                                    editor.putString("userRole", userRole)
                                    editor.putString("userUid", documentSnapshot.getString("uid"))
                                    editor.putString("userName", documentSnapshot.getString("name"))
                                    editor.putString("userPhone", documentSnapshot.getString("noHP"))
                                    editor.putString("userEmail", documentSnapshot.getString("email"))
                                    editor.apply()
                                    Log.d("Login","Role $userRole")
                                    // Redirect to appropriate activity based on user role
                                    when (userRole) {
                                        "admin" ->
                                            startActivity(Intent(this, AdminActivity::class.java)
                                        )
                                    }
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    showSnack(this, "Login failed. Please check your credentials.")
                                }
                    } else {
                        progressBar.visibility = View.GONE
                        showSnack(this,"Login failed. Please check your credentials.")
                    }
                }
        }
    }
}