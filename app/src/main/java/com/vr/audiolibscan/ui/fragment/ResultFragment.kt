package com.vr.audiolibscan.ui.fragment

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.R
import com.vr.audiolibscan.databinding.FragmentResultBinding
import com.vr.audiolibscan.tools.getBarang
import com.vr.audiolibscan.tools.showSnack
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment(), TextToSpeech.OnInitListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentResultBinding
    lateinit var btnBack : LinearLayout
    lateinit var btnReplay : LinearLayout
    lateinit var btnDownload : LinearLayout
    lateinit var tvNamaBarang : TextView
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
    private lateinit var textToSpeech: TextToSpeech
    var AudioSiap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResultBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        initView()
        initTermjemahan()
        initClick()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun initView(){
        btnReplay = binding.btnReplay
        btnDownload = binding.btnDownload
        tvNamaBarang = binding.tvNamaBarang
        tvPenjelasan = binding.tvPenjelasan
        fotoBarang = binding.fotoBarang
        btnIndo = binding.lyBahasa.btnIndo
        btnInggris = binding.lyBahasa.btnInggris
        lyBahasa = binding.lyBahasa.lyBahasa
        textToSpeech = TextToSpeech(requireContext(), this)
        lyBahasa.visibility = View.VISIBLE
        initIntent()
    }
    fun initClick(){
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
                showSnack(requireActivity(), "Terjadi kesalahan")
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
                showSnack(requireActivity()
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
                    showSnack(requireActivity(), "Terjadi kesalahan")
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
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
                showSnack(requireActivity(), "Terjadi kesalahan")
            }
    }
    fun initIntent(){
        val barang = getBarang(requireContext())
        tnama = barang.nama.toString()
        tPenjelasan = barang.penjelasan.toString()
        tImage = barang.fotoBarang.toString()
        //set data
        tvNamaBarang.text = tnama
        tvPenjelasan.text = tPenjelasan
        Glide.with(this).load(tImage).into(fotoBarang)
        tPenjelasan =  tnama + " "+ tPenjelasan
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
            val textToSpeech = TextToSpeech(requireContext(), TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech.synthesizeToFile(
                        text, null, audioFile, "audio"
                    )
                    if (result == TextToSpeech.SUCCESS) {
                        // Audio file saved successfully
                        showSnack(requireActivity(), "Audio berhasil disimpan")
                        //bersiap untuk intent ke aplikasi lain
                        shareAudioFile(audioFile)
                    } else {
                        // Error saving audio
                        showSnack(requireActivity(), "Gagal menyimpan audio")
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
        val uri = FileProvider.getUriForFile(requireContext(), "com.vr.audiolibscan.fileprovider", audioFile)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Audio"))
    }
}