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
import com.vr.audiolibscan.databinding.FragmentAddBinding
import com.vr.audiolibscan.tools.ImageUtils
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.AdminActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddFragment : Fragment() {
    lateinit var binding: FragmentAddBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater)
        return binding.root
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
             namaBarang = etNamaBarang.text.toString()
             penjelasan = etPenjelasan.text.toString()
            //generate kode barang
             kodeBarang = UUID.randomUUID().toString()
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

            // Periksa apakah semua field yang diperlukan terisi
            if (namaBarang.isNotEmpty() && kodeBarang.isNotEmpty() && penjelasan.isNotEmpty()
                && title.isNotEmpty() && creator.isNotEmpty() && subject.isNotEmpty() && description.isNotEmpty()
                && publisher.isNotEmpty() && contributor.isNotEmpty() && date.isNotEmpty() && type.isNotEmpty()
                && format.isNotEmpty() && identifier.isNotEmpty() && source.isNotEmpty() && language.isNotEmpty()
                && relation.isNotEmpty() && coverage.isNotEmpty() && rights.isNotEmpty()
                ) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(context)
                progressDialog.setMessage("Mengunggah barang...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    coverImageUrl = uploadImage(imagesList[0])
                    // Tambahkan detail produk ke Firestore
                    addBarangToFirestore()
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
    private fun addBarangToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val barangData = hashMapOf(
            "barangId" to UUID.randomUUID().toString(),
            "uid" to currentUser!!.uid,
            "nama" to namaBarang,
            "kodeBarang" to kodeBarang,
            "penjelasan" to penjelasan,
            "fotoBarang" to coverImageUrl,
            //meta
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