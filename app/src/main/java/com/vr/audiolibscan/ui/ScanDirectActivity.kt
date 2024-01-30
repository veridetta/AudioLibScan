package com.vr.audiolibscan.ui

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.model.BarangModel
import com.vr.audiolibscan.tools.saveBarang
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.BarangActivity
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

class ScanDirectActivity : AppCompatActivity() {

    lateinit var lyScan : RelativeLayout
    lateinit var imgScan : ImageView
    var kodeBarang=""
    private val mFirestore = FirebaseFirestore.getInstance()
    val TAG = "BarangActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_direct)
        initView()
    }

    fun initView(){
        lyScan = findViewById(R.id.lyScan)
        imgScan = findViewById(R.id.imgScan)
        initIntent()
        getData(kodeBarang)
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
                saveBarang(barangs[0], this@ScanDirectActivity)
                withContext(Dispatchers.Main) {
                    saveToHistory(barangs[0])
                    runOnUiThread {
                        lyScan.visibility = View.GONE
                        val intent = Intent(this@ScanDirectActivity, ResultHistroyActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                showSnack(this@ScanDirectActivity, "Terjadi kesalahan")
                val intent = Intent(this@ScanDirectActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun saveToHistory(barang : BarangModel){
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
            "title" to barang.title,
            "creator" to barang.creator,
            "subject" to barang.subject,
            "description" to barang.description,
            "publisher" to barang.publisher,
            "contributor" to barang.contributor,
            "date" to barang.date,
            "type" to barang.type,
            "format" to barang.format,
            "identifier" to barang.identifier,
            "source" to barang.source,
            "language" to barang.language,
            "relation" to barang.relation,
            "coverage" to barang.coverage,
            "rights" to barang.rights
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