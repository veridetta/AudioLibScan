package com.vr.audiolibscan.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R

class HomeActivity : AppCompatActivity() {
    private lateinit var btnMasuk:Button;
    private lateinit var btnProfil:Button;
    private lateinit var btnPetunjuk:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initView()
        initClick()
    }
    fun initView(){
        btnMasuk = findViewById(R.id.btnMasuk);
        btnProfil = findViewById(R.id.btnProfil);
        btnPetunjuk = findViewById(R.id.btnPetujuk);
    }

    fun initClick(){
        btnMasuk.setOnClickListener {
            //intent ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
        btnProfil.setOnClickListener {
            //intent ke ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        btnPetunjuk.setOnClickListener {
            //intent ke PetunjukActivity
            val intent = Intent(this, PetunjukActivity::class.java)
            startActivity(intent)
        }
    }

}