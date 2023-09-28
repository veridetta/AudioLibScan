package com.vr.audiolibscan.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.model.BarangModel
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.ScanActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BarangActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var btnBack : LinearLayout
    lateinit var tvNamaBarang : TextView
    lateinit var tvKodeBarang : TextView
    lateinit var tvPenjelasan : TextView
    lateinit var fotoBarang : ImageView
    lateinit var lyScan : RelativeLayout
    lateinit var imgScan : ImageView
    var kodeBarang=""
    private val mFirestore = FirebaseFirestore.getInstance()
    val TAG = "BarangActivity"
    private lateinit var textToSpeech: TextToSpeech
    var AudioSiap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barang)
        initView()
        initClick()
    }

    fun initView(){
        btnBack = findViewById(R.id.btnBack)
        tvNamaBarang = findViewById(R.id.tvNamaBarang)
        tvKodeBarang = findViewById(R.id.tvKodeBarang)
        tvPenjelasan = findViewById(R.id.tvPenjelasan)
        fotoBarang = findViewById(R.id.fotoBarang)
        lyScan = findViewById(R.id.lyScan)
        imgScan = findViewById(R.id.imgScan)
        Glide.with(this).load(R.drawable.qr_scan).into(imgScan)
        textToSpeech = TextToSpeech(this, this)
        initIntent()
        getData(kodeBarang)
    }
    fun initClick(){
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun initIntent(){
        kodeBarang = intent.getStringExtra("contents").toString()
    }
    private fun getData( kode: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("barang")
                    .whereEqualTo("kodeBarang", kode).get().await()
                val barangs = mutableListOf<BarangModel>()
                for (document in result) {
                    val barang = document.toObject(BarangModel::class.java)
                    barangs.add(barang)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                   tvNamaBarang.text = barangs[0].nama
                   tvKodeBarang.text = barangs[0].kodeBarang
                   tvPenjelasan.text = barangs[0].penjelasan
                    Glide.with(this@BarangActivity)
                        .load(barangs[0].fotoBarang)
                        .override(270,270).centerCrop()
                        .placeholder(R.drawable.no_image)
                        .into(fotoBarang)
                    var tNama = "dengan nama barang adalah "+barangs[0].nama +" ."
                    var tKode = "Kode Barang :"+ barangs[0].kodeBarang +" , berhasil di scan."
                    var tPenjelasan = tKode + " "+ tNama + " "+ barangs[0].penjelasan
                    speakText(tPenjelasan)
                }
                lyScan.visibility = View.GONE
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                showSnack(this@BarangActivity, "Terjadi kesalahan")
                val intent = Intent(this@BarangActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    private fun stopSpeaking() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }
    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
    override fun onInit(status: Int) {
        AudioSiap = status == TextToSpeech.SUCCESS
    }
}