package com.vr.audiolibscan.ui

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.tools.showSnack
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultHistroyActivity : AppCompatActivity(), TextToSpeech.OnInitListener  {
    lateinit var btnBack : LinearLayout
    lateinit var btnReplay : LinearLayout
    lateinit var btnDownload : LinearLayout
    lateinit var tvNamaBarang : TextView
    lateinit var tvKodeBarang : TextView
    lateinit var tvPenjelasan : TextView
    lateinit var fotoBarang : ImageView
    var tPenjelasan=""
    var tnama = ""
    var tImage=""
    var kodeBarang=""
    private lateinit var textToSpeech: TextToSpeech
    var AudioSiap = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_histroy)
        initView()
        initClick()
    }
    fun initView(){
        btnBack = findViewById(R.id.btnBack)
        btnReplay = findViewById(R.id.btnReplay)
        btnDownload = findViewById(R.id.btnDownload)
        tvNamaBarang = findViewById(R.id.tvNamaBarang)
        tvKodeBarang = findViewById(R.id.tvKodeBarang)
        tvPenjelasan = findViewById(R.id.tvPenjelasan)
        fotoBarang = findViewById(R.id.fotoBarang)
        textToSpeech = TextToSpeech(this, this)
        initIntent()
    }
    fun initClick(){
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        btnReplay.setOnClickListener {
            speakText(tPenjelasan)
        }
        btnDownload.setOnClickListener {
            saveTextToAudioFile(tPenjelasan,tnama)
        }
    }

    fun initIntent(){
        //ambil intent
        tnama = intent.getStringExtra("nama").toString()
        tPenjelasan = intent.getStringExtra("penjelasan").toString()
        tImage = intent.getStringExtra("image").toString()
        kodeBarang = intent.getStringExtra("kode").toString()
        //set data
        tvNamaBarang.text = tnama
        tvKodeBarang.text = kodeBarang
        tvPenjelasan.text = tPenjelasan
        Glide.with(this).load(tImage).into(fotoBarang)
        tPenjelasan = kodeBarang + " "+ tnama + " "+ tPenjelasan
        speakText(tPenjelasan)
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
    fun saveTextToAudioFile(text: String, nama :String) {
        if (AudioSiap) {
            val dir = File(Environment.getExternalStorageDirectory(), "AudioFiles-AudioLibScan")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())

            val audioFile = File(dir, "$nama-$timeStamp.mp3")
            val textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech.synthesizeToFile(
                        text, null, audioFile, "audio"
                    )
                    if (result == TextToSpeech.SUCCESS) {
                        // Audio file saved successfully
                        showSnack(this@ResultHistroyActivity, "Audio berhasil disimpan")
                        //bersiap untuk intent ke aplikasi lain
                        shareAudioFile(audioFile)
                    } else {
                        // Error saving audio
                        showSnack(this@ResultHistroyActivity, "Gagal menyimpan audio")
                    }
                }
            })
        }
    }

    fun playAudioFile(audioFile: File) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFile.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun shareAudioFile(audioFile: File) {
        val uri = FileProvider.getUriForFile(this, "com.vr.audiolibscan.fileprovider", audioFile)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Audio"))
    }
}