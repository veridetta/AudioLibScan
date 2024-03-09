package com.vr.audiolibscan.ui.home

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.ImageButton
import android.widget.TextView
import com.vr.audiolibscan.R

class PetunjukActivity : AppCompatActivity() {
    private lateinit var tvPetunjuk:TextView
    private lateinit var btnBack:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petunjuk)
        initView()
        initClick()
    }
    fun initView(){
        tvPetunjuk = findViewById(R.id.tvPetunjuk)
        btnBack = findViewById(R.id.btnBack)
        val petunjuk="<div style=\"text-align: justify;\">\n" +
                "<p>Aplikasi audiolibscan merupakan aplikasi alih media berbasis audio digital sebagai upaya pelestarian koleksi memorabilia dan sebagai sarana dalam mengakses informasi koleksi memorabilia.</p>\n" +
                "\n" +
                "<br>"+
                "<p>Cara penggunaan aplikasi audiolibscan</p>\n" +
                "<ol>\n" +
                "  <li>Klik button masuk yang tersedia di halaman utama</li>\n" +
                "  <li>Arahkan kamera ke barcode yang terdapat pada setiap koleksi memorabilia</li>\n" +
                "  <li>Pilih bahasa</li>\n" +
                "  <li>Selanjutnya pengguna dapat mengakses dan mendapatkan informasi koleksi memorabilia sesuai dengan barcode yang telah di-scan</li>\n" +
                "</ol>\n"+
                "</div>"
        tvPetunjuk.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(petunjuk, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(petunjuk)
        }
    }

    fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
    }
}