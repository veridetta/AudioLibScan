package com.vr.audiolibscan.ui.auth

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BarangActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    lateinit var btnBack : LinearLayout
    lateinit var btnReplay : LinearLayout
    lateinit var btnDownload : LinearLayout
    lateinit var tvNamaBarang : TextView
    lateinit var tvKodeBarang : TextView
    lateinit var tvPenjelasan : TextView
    lateinit var fotoBarang : ImageView
    lateinit var lyScan : RelativeLayout
    lateinit var lyBahasa : RelativeLayout
    lateinit var imgScan : ImageView
    lateinit var btnIndo : Button
    lateinit var btnInggris : Button
    lateinit var translator:Translator
    private var audioLanguage = Locale.US // Bahasa Inggris (default)

    var ing = false
    var tPenjelasan=""
    var tnama = ""
    var kodeBarang=""
    private val mFirestore = FirebaseFirestore.getInstance()
    val TAG = "BarangActivity"
    private lateinit var textToSpeech: TextToSpeech
    var AudioSiap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barang)
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
        lyScan = findViewById(R.id.lyScan)
        lyBahasa.visibility = View.VISIBLE
        imgScan = findViewById(R.id.imgScan)
        textToSpeech = TextToSpeech(this, this)
        initIntent()
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
                showSnack(this@BarangActivity, "Terjadi kesalahan")
            }
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
        btnIndo.setOnClickListener {
            lyBahasa.visibility = View.GONE
            lyScan.visibility = View.VISIBLE
            Glide.with(this).load(R.drawable.qr_scan).into(imgScan)
            // Atur bahasa ke bahasa Indonesia
            audioLanguage = Locale("id", "ID")

            // Jika TTS sudah diinisialisasi, ganti bahasa
            if (textToSpeech.isLanguageAvailable(audioLanguage) != TextToSpeech.LANG_MISSING_DATA) {
                textToSpeech.language = audioLanguage
            } else {
                // Tangani kesalahan jika bahasa tidak tersedia
                showSnack(this@BarangActivity, "Terjadi kesalahan")
            }
            getData(kodeBarang)
        }
        btnInggris.setOnClickListener {
            lyBahasa.visibility = View.GONE
            lyScan.visibility = View.VISIBLE
            Glide.with(this).load(R.drawable.qr_scan).into(imgScan)
            ing = true
            // Atur bahasa ke bahasa Inggris
            audioLanguage = Locale.US

            // Jika TTS sudah diinisialisasi, ganti bahasa
            if (textToSpeech.isLanguageAvailable(audioLanguage) != TextToSpeech.LANG_MISSING_DATA) {
                textToSpeech.language = audioLanguage
            } else {
                // Tangani kesalahan jika bahasa tidak tersedia
                showSnack(this@BarangActivity, "Terjadi kesalahan")
            }
            getData(kodeBarang)
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
                    saveToHistory(barangs[0])
                   tvNamaBarang.text = barangs[0].nama
                   tvKodeBarang.text = barangs[0].kodeBarang
                   tvPenjelasan.text = barangs[0].penjelasan
                    Glide.with(this@BarangActivity)
                        .load(barangs[0].fotoBarang)
                        .override(270,270).centerCrop()
                        .placeholder(R.drawable.no_image)
                        .into(fotoBarang)
                    tnama = barangs[0].nama.toString()
                    var tNama = "dengan nama barang adalah "+barangs[0].nama +" ."
                    var tKode = "Kode Barang :"+ barangs[0].kodeBarang +" , berhasil di scan."
                     tPenjelasan = tKode + " "+ tNama + " "+ barangs[0].penjelasan
                    if(ing){
                        translator.translate(tPenjelasan)
                            .addOnSuccessListener { translatedText ->
                                tvPenjelasan.text = translatedText
                                speakText(translatedText)
                            }
                            .addOnFailureListener { exception ->
                                // Terjemahan gagal
                                // ...
                                showSnack(this@BarangActivity, "Terjadi kesalahan")
                                val intent = Intent(this@BarangActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }else{
                        speakText(tPenjelasan)
                    }
                    runOnUiThread {
                        lyScan.visibility = View.GONE
                    }
                }
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
        val volume = Bundle()
        volume.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
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
                        showSnack(this@BarangActivity, "Audio berhasil disimpan")
                        //bersiap untuk intent ke aplikasi lain
                        shareAudioFile(audioFile)
                    } else {
                        // Error saving audio
                        showSnack(this@BarangActivity, "Gagal menyimpan audio")
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
    private fun saveToHistory(barang :BarangModel){
        //check shared prefrences apakah uid sudah ada
        val sharedPref = getSharedPreferences("user", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", UUID.randomUUID().toString())
        //save uid ke shared prefrences
        val editor = sharedPref.edit()
        editor.putString("uid", uid)
        editor.apply()
        val data = hashMapOf(
            "barangId" to barang.barangId,
            "uid" to uid,
            "nama" to barang.nama,
            "kodeBarang" to kodeBarang,
            "penjelasan" to barang.penjelasan,
            "fotoBarang" to barang.fotoBarang,
        )
        mFirestore.collection("history").document().set(data)
            .addOnSuccessListener {
                println("Sukses")
            }
            .addOnFailureListener { e ->
                println("Gagal")
            }
    }
}