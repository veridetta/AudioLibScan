package com.vr.audiolibscan.ui

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.adapter.PagerAdapter
import com.vr.audiolibscan.databinding.ActivityResultHistroyBinding
import com.vr.audiolibscan.tools.showSnack
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultHistroyActivity : AppCompatActivity(){
    lateinit var binding : ActivityResultHistroyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initViewPager()
        initListener()
    }
    fun initView(){
        binding = ActivityResultHistroyBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun initViewPager(){
        val sectionsPagerAdapter = PagerAdapter(this)
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Detail Barang"
                }
                1 -> {
                    tab.text = "Meta Tag"
                }
            }
        }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPager.requestLayout()
            }
        })
    }
    fun initListener(){
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}