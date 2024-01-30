package com.vr.audiolibscan.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vr.audiolibscan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.util.UUID
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.vr.audiolibscan.MainActivity
import com.vr.audiolibscan.databinding.ActivityEditBinding
import com.vr.audiolibscan.databinding.FragmentAddBinding
import com.vr.audiolibscan.tools.ImageUtils
import com.vr.audiolibscan.tools.getBarang
import com.vr.audiolibscan.tools.showSnack
import kotlinx.coroutines.launch

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()

    private lateinit var btnUploadCover: LinearLayout
    private lateinit var coverReplace: ImageView
    private lateinit var btnAdd: Button
    private lateinit var btnBack: ImageButton

    private lateinit var etNamaBarang: EditText
    private lateinit var etKodeBarang: EditText
    private lateinit var etPenjelasan: EditText
    private val REQUEST_CODE_COVER = 1
    private var readyUpload = false

    var namaBarang = ""
    var penjelasan = ""
    var coverImageUrl = ""
    //generate kode barang
    var kodeBarang = ""
    //meta

    var title = ""
    var creator = ""
    var subject = ""
    var description = ""
    var publisher = ""
    var contributor = ""
    var date = ""
    var type = ""
    var format = ""
    var identifier = ""
    var source = ""
    var language = ""
    var relation = ""
    var coverage = ""
    var rights = ""
    var docId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initClick()
        initIntent()
    }

    private fun initView(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imagesList = mutableListOf(null, null, null, null, null, null,null)
        coverReplace = findViewById(R.id.coverReplace)
        btnUploadCover = findViewById(R.id.btnUploadCover)

        btnBack = findViewById(R.id.btnBack)
        btnAdd = findViewById(R.id.btnAdd)
        etNamaBarang = findViewById(R.id.etNamaBarang)
        etPenjelasan = findViewById(R.id.etPenjelasan)
    }
    private fun initClick(){
        btnUploadCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }
        btnAdd.setOnClickListener {
             namaBarang = etNamaBarang.text.toString()
//             kodeBarang = etKodeBarang.text.toString()
             penjelasan = etPenjelasan.text.toString()
            //meta
            title = binding.etMetaTitle.text.toString()
            creator = binding.etMetaCreator.text.toString()
            subject = binding.etMetaSubject.text.toString()
            description = binding.etMetaDescription.text.toString()
            publisher = binding.etMetaPublisher.text.toString()
            contributor = binding.etMetaContributor.text.toString()
            date = binding.etMetaDate.text.toString()
            type = binding.etMetaType.text.toString()
            format = binding.etMetaFormat.text.toString()
            identifier = binding.etMetaIdentifier.text.toString()
            source = binding.etMetaSource.text.toString()
            language = binding.etMetaLanguage.text.toString()
            relation = binding.etMetaRelation.text.toString()
            coverage = binding.etMetaCoverage.text.toString()
            rights = binding.etMetaRights.text.toString()
            docId = intent.getStringExtra("documentId").toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (namaBarang.isNotEmpty()  && penjelasan.isNotEmpty()
                && title.isNotEmpty() && creator.isNotEmpty() && subject.isNotEmpty() && description.isNotEmpty()
                && publisher.isNotEmpty() && contributor.isNotEmpty() && date.isNotEmpty() && type.isNotEmpty()
                && format.isNotEmpty() && identifier.isNotEmpty() && source.isNotEmpty() && language.isNotEmpty()
                && relation.isNotEmpty() && coverage.isNotEmpty() && rights.isNotEmpty()
                ) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this@EditActivity)
                progressDialog.setMessage("Mengunggah barang...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    if (readyUpload){
                        coverImageUrl = uploadImage(imagesList[0])
                    }else{
                        coverImageUrl = intent.getStringExtra("fotoBarang").toString()
                    }
                    // Tambahkan detail produk ke Firestore
                    addBarangToFirestore()
                    progressDialog.dismiss()
                }
            } else {
                showSnack(this,"Mohon isi semua field yang diperlukan")
            }
        }
        btnBack.setOnClickListener {
            val intent = Intent(this@EditActivity, AdminActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun initIntent(){
        val namaBarang = intent.getStringExtra("nama")
        val kodeBarang = intent.getStringExtra("kodeBarang")
        val penjelasan = intent.getStringExtra("penjelasan")
        val fotoBarang = intent.getStringExtra("fotoBarang")

        etNamaBarang.setText(namaBarang)
        etPenjelasan.setText(penjelasan)
        coverImageUrl= fotoBarang.toString()
        Glide.with(this)
            .load(fotoBarang)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(coverReplace)

        val barang = getBarang(this)
        //meta
        binding.etMetaTitle.setText(barang.title)
        binding.etMetaCreator.setText(barang.creator)
        binding.etMetaSubject.setText(barang.subject)
        binding.etMetaDescription.setText(barang.description)
        binding.etMetaPublisher.setText(barang.publisher)
        binding.etMetaContributor.setText(barang.contributor)
        binding.etMetaDate.setText(barang.date)
        binding.etMetaType.setText(barang.type)
        binding.etMetaFormat.setText(barang.format)
        binding.etMetaIdentifier.setText(barang.identifier)
        binding.etMetaSource.setText(barang.source)
        binding.etMetaLanguage.setText(barang.language)
        binding.etMetaRelation.setText(barang.relation)
        binding.etMetaCoverage.setText(barang.coverage)
        binding.etMetaRights.setText(barang.rights)
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(this,imageUri)
        val storageReference = FirebaseStorage.getInstance().getReference("foto_barang")
        val imageFileName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("$imageFileName.jpg")
        return try {
            val uploadTask = imageRef.putFile(compressedImageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }
    private suspend fun compressImage(context: Context, imageUri: Uri?): Uri {
        val originalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val compressedBitmap = ImageUtils.compressBitmap(originalBitmap)

        val compressedImageUri = ImageUtils.createTempImageFile(context)
        val outputStream = context.contentResolver.openOutputStream(compressedImageUri)
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream?.close()

        return compressedImageUri
    }
    private fun addBarangToFirestore() {
        val barangData = hashMapOf(
            "nama" to namaBarang,
            "penjelasan" to penjelasan,
            "fotoBarang" to coverImageUrl,
            "title" to title,
            "creator" to creator,
            "subject" to subject,
            "description" to description,
            "publisher" to publisher,
            "contributor" to contributor,
            "date" to date,
            "type" to type,
            "format" to format,
            "identifier" to identifier,
            "source" to source,
            "language" to language,
            "relation" to relation,
            "coverage" to coverage,
            "rights" to rights
        )

        val db = FirebaseFirestore.getInstance()
        // Add the product data to Firestore
        db.collection("barang")
            .document(docId)
            .update(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                showSnack(this,"Gagal menyimpan barang ${e.message}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_COVER -> {
                    // Ambil URI gambar yang dipilih dari galeri
                    val selectedImageUri = data?.data
                    // Tampilkan gambar yang dipilih ke imageView coverReplace
                    //glide
                    Glide.with(this)
                        .load(selectedImageUri)
                        .override(270,270).centerCrop()
                        .placeholder(R.drawable.no_image)
                        .into(coverReplace)
                    // Simpan URI gambar ke dalam list untuk penggunaan nanti
                    imagesList[0] = selectedImageUri
                    readyUpload = true
                }
            }
        }
    }
}