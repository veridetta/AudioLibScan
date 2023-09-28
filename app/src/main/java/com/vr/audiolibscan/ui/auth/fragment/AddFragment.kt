package com.vr.audiolibscan.ui.auth.fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vr.audiolibscan.R
import com.vr.audiolibscan.tools.ImageUtils
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.AdminActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()

    private lateinit var btnUploadCover: LinearLayout
    private lateinit var coverReplace: ImageView
    private lateinit var btnAdd: Button

    private lateinit var etNamaBarang: EditText
    private lateinit var etKodeBarang: EditText
    private lateinit var etPenjelasan: EditText
    private val REQUEST_CODE_COVER = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        initView(itemView)
        initClick()
    }
    private fun initView(itemView: View){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imagesList = mutableListOf(null, null, null, null, null, null,null)
        coverReplace = itemView.findViewById(R.id.coverReplace)
        btnUploadCover = itemView.findViewById(R.id.btnUploadCover)

        btnAdd = itemView.findViewById(R.id.btnAdd)
        etNamaBarang = itemView.findViewById(R.id.etNamaBarang)
        etKodeBarang = itemView.findViewById(R.id.etKodeBarang)
        etPenjelasan = itemView.findViewById(R.id.etPenjelasan)
    }
    private fun initClick(){
        btnUploadCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }
        btnAdd.setOnClickListener {
            val namaBarang = etNamaBarang.text.toString()
            val kodeBarang = etKodeBarang.text.toString()
            val penjelasan = etPenjelasan.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (namaBarang.isNotEmpty() && kodeBarang.isNotEmpty() && penjelasan.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(context)
                progressDialog.setMessage("Mengunggah barang...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    var coverImageUrl = ""
                    coverImageUrl = uploadImage(imagesList[0])
                    // Tambahkan detail produk ke Firestore
                    addBarangToFirestore(
                        namaBarang,
                        kodeBarang,
                        penjelasan,
                        coverImageUrl
                    )
                    progressDialog.dismiss()
                }
            } else {
                showSnack(requireActivity(),"Mohon isi semua field yang diperlukan")
            }
        }
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(requireContext(),imageUri)
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
    private fun addBarangToFirestore(
        namaBarang: String,
        kodeBarang: String,
        penjelasan: String,
        coverImageUrl: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val barangData = hashMapOf(
            "barangId" to UUID.randomUUID().toString(),
            "uid" to currentUser!!.uid,
            "nama" to namaBarang,
            "kodeBarang" to kodeBarang,
            "penjelasan" to penjelasan,
            "fotoBarang" to coverImageUrl,
        )

        val db = FirebaseFirestore.getInstance()

        // Add the product data to Firestore
        db.collection("barang")
            .add(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(requireActivity(),"Berhasil menyimpan barang")
                // Redirect to SellerActivity fragment home
                val intent = Intent(requireActivity(), AdminActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                showSnack(requireActivity(),"Gagal menyimpan barang ${e.message}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_COVER -> {
                    // Ambil URI gambar yang dipilih dari galeri
                    val selectedImageUri = data?.data
                    // Tampilkan gambar yang dipilih ke imageView coverReplace
                    coverReplace.setImageURI(selectedImageUri)
                    // Simpan URI gambar ke dalam list untuk penggunaan nanti
                    imagesList[0] = selectedImageUri
                }
            }
        }
    }
}