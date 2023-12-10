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
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
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
    lateinit var lyBahasa : RelativeLayout
    lateinit var btnIndo : Button
    lateinit var btnInggris : Button
    lateinit var translator: Translator
    private var audioLanguage = Locale.US // Bahasa Inggris (default)
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
        initTermjemahan()
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
        btnIndo = findViewById(R.id.btnIndo)
        btnInggris = findViewById(R.id.btnInggris)
        lyBahasa = findViewById(R.id.lyBahasa)
        textToSpeech = TextToSpeech(this, this)
        lyBahasa.visibility = View.VISIBLE
        initIntent()
    }
    fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnReplay.setOnClickListener {
            speakText(tPenjelasan)
        }
        btnDownload.setOnClickListener {
            saveTextToAudioFile(tPenjelasan,tnama)
        }
        btnIndo.setOnClickListener {
            lyBahasa.visibility = View.GONE
            audioLanguage = Locale("id", "ID")

            // Jika TTS sudah diinisialisasi, ganti bahasa
            if (textToSpeech.isLanguageAvailable(audioLanguage) != TextToSpeech.LANG_MISSING_DATA) {
                textToSpeech.language = audioLanguage
            } else {
                // Tangani kesalahan jika bahasa tidak tersedia
                showSnack(this@ResultHistroyActivity, "Terjadi kesalahan")
            }
            stopSpeaking()
            speakText(tPenjelasan)
        }
        btnInggris.setOnClickListener {
            lyBahasa.visibility = View.GONE
            stopSpeaking()

            audioLanguage = Locale.US

            // Jika TTS sudah diinisialisasi, ganti bahasa
            if (textToSpeech.isLanguageAvailable(audioLanguage) != TextToSpeech.LANG_MISSING_DATA) {
                textToSpeech.language = audioLanguage
            } else {
                // Tangani kesalahan jika bahasa tidak tersedia
                showSnack(this@ResultHistroyActivity
                    , "Terjadi kesalahan")
            }
            translator.translate(tPenjelasan)
                .addOnSuccessListener { translatedText ->
                    tvPenjelasan.text = translatedText
                    speakText(translatedText)
                }
                .addOnFailureListener { exception ->
                    // Terjemahan gagal
                    // ...
                    showSnack(this@ResultHistroyActivity, "Terjadi kesalahan")
                    val intent = Intent(this@ResultHistroyActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }
    }
    fun initTermjemahan(){
        // Inisialisasi model terjemahan
        var bing = btnInggris.text.toString()
        btnInggris.text = bing + " (Loading...)"
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.INDONESIAN) // Bahasa asal (dalam contoh ini bahasa Indonesia)
            .setTargetLanguage(TranslateLanguage.ENGLISH)    // Bahasa yang dituju (bahasa Inggris)
            .build()
        translator = Translation.getClient(options)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                // Model terjemahan sudah diunduh atau telah diunduh saat ini
                // Anda dapat mengaktifkan tombol Inggris atau melakukan terjemahan di sini
                btnInggris.isEnabled = true
                btnInggris.text = bing
            }
            .addOnFailureListener { exception ->
                // Model belum diunduh dan ada kesalahan unduhan model
                // Anda dapat menangani kesalahan atau memberikan pesan kesalahan kepada pengguna
                showSnack(this@ResultHistroyActivity, "Terjadi kesalahan")
            }
    }
    fun initIntent(){
        //ambil intent
        tnama = intent.getStringExtra("nama").toString()
        tPenjelasan = intent.getStringExtra("penjelasan").toString()
        tImage = intent.getStringExtra("fotoBarang").toString()
        kodeBarang = intent.getStringExtra("kodeBarang").toString()
        //set data
        tvNamaBarang.text = tnama
        tvKodeBarang.text = kodeBarang
        tvPenjelasan.text = tPenjelasan
        Glide.with(this).load(tImage).into(fotoBarang)
        tPenjelasan = kodeBarang + " "+ tnama + " "+ tPenjelasan
    }
    private fun speakText(text: String) {
        val volume = Bundle()
        volume.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, volume, null)
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
        // Set bahasa TTS saat inisialisasi
        if (AudioSiap) {
            textToSpeech.language = audioLanguage
        }
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