package com.vr.audiolibscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.HistroyActivity
import com.vr.audiolibscan.ui.LoginActivity
import com.vr.audiolibscan.ui.ScanActivity
import com.vr.audiolibscan.ui.auth.BarangActivity

class MainActivity : AppCompatActivity() {
    lateinit var btnScan: LinearLayout
    lateinit var btnHistory: LinearLayout
    lateinit var btnLogin: ImageView
    private val CAMERA_PERMISSION_REQUEST_CODE = 101 // Atur dengan kode permintaan izin yang Anda inginkan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        setContentView(R.layout.activity_main)
        initView()
        initClick()
    }

    private fun initView() {
        btnScan = findViewById(R.id.btnScan)
        btnLogin = findViewById(R.id.btnLogin)
        btnHistory = findViewById(R.id.btnHistory)
    }

    private fun initClick() {
        btnScan.setOnClickListener {
            // Memeriksa apakah izin kamera telah diberikan
            if (checkCameraPermission()) {
                startCameraIntent()
            } else {
                requestCameraPermission()
            }
        }
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistroyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan, memulai intent kamera
                startCameraIntent()
            } else {
                // Izin kamera tidak diberikan, Anda dapat memberikan pesan atau tindakan lainnya di sini
                showSnack(this,"Izin kamera tidak diberikan")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCameraIntent() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }

}

