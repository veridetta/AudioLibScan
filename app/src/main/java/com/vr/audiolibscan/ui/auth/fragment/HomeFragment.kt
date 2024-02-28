package com.vr.audiolibscan.ui.auth.fragment

import android.R.attr.bitmap
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText

import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.vr.audiolibscan.R
import com.vr.audiolibscan.adapter.BarangAdapter
import com.vr.audiolibscan.model.BarangModel
import com.vr.audiolibscan.tools.saveBarang
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.AdminActivity
import com.vr.audiolibscan.ui.auth.EditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var barangAdapter: BarangAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val barangList: MutableList<BarangModel> = mutableListOf()
//    lateinit var qrEncoder: QRGEncoder
    lateinit var gambar:Bitmap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        recyclerView = itemView.findViewById(R.id.rcBarang)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            barangAdapter = BarangAdapter(
                barangList,
                requireContext(),
                { barang -> editBarang(barang) },
                { barang -> hapusBarang(barang) },
                { barang -> shareQr(barang) }
            )
        }
        val shimmerContainer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

        recyclerView.adapter = barangAdapter

        val searchEditText = itemView.findViewById<EditText>(R.id.btnCari)
        barangAdapter.filter("")
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                barangAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("barang").whereEqualTo("uid", uid).get().await()
                val barangs = mutableListOf<BarangModel>()
                for (document in result) {
                    val barang = document.toObject(BarangModel::class.java)
                    val docId = document.id
                    barang.documentId = docId
                    barangs.add(barang)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    barangList.addAll(barangs)
                    barangAdapter.filteredBarangList.addAll(barangs)
                    barangAdapter.notifyDataSetChanged()
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                shimmerContainer.stopShimmer() // Stop shimmer effect
                shimmerContainer.visibility = View.GONE // Hide shimmer container
            }
        }
    }


    private fun editBarang(barang: BarangModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(requireContext(), EditActivity::class.java)
        intent.putExtra("documentId", barang.documentId)
        intent.putExtra("nama", barang.nama)
        intent.putExtra("fotoBarang", barang.fotoBarang)
        intent.putExtra("kodeBarang", barang.kodeBarang)
        intent.putExtra("penjelasan", barang.penjelasan)
        intent.putExtra("barangId", barang.barangId)
        intent.putExtra("uid", barang.uid)
        saveBarang(barang, requireContext())
        startActivity(intent)
    }
    private fun hapusBarang(barang: BarangModel) {
        //hapus barang dari firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("barang").document(barang.documentId.toString())
            .delete()
            .addOnSuccessListener {
                showSnack(requireActivity(),"Berhasil menghapus barang")
                // Redirect to SellerActivity fragment home
                val intent = Intent(requireContext(), AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                Log.w(TAG, "Error getting documents : $e")
            }
    }
    fun shareQr(barang: BarangModel) {
//        val qrgEncoder = QRGEncoder(barang.kodeBarang, null, QRGContents.Type.TEXT, 500)
//        qrgEncoder.colorBlack = Color.BLACK
//        qrgEncoder.colorWhite = Color.WHITE
//        try {
//            val gambar = qrgEncoder.bitmap
//            val qrgSaver = QRGSaver()
//            val dir = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/")
//            if (!dir.exists()) {
//                dir.mkdirs()
//            }
//
//            qrgSaver.save(
//                requireContext().getExternalFilesDir(null).toString() + "/QRCode/" ,
//                barang.kodeBarang,
//                gambar,
//                QRGContents.ImageType.IMAGE_JPEG
//            )
//            if (gambar != null) {
//                Log.d(TAG, "shareQr: $gambar")
//            }
//            val file = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/" + barang.kodeBarang + ".jpg")
//            shareQRFile(file,barang.kodeBarang.toString())
//        } catch (e: WriterException) {
//            Log.e(TAG, "Error creating QR Code: ${e.message}")
//        }
//        val roundedSquares = QRCode.ofSquares()
//            .withColor(Colors.DEEP_SKY_BLUE) // Default is Colors.BLACK
//            .withSize(25) // Default is 25
//            .build(barang.kodeBarang.toString())
//        val roundedSquarePngData = roundedSquares.renderToBytes()
//
//        val dir = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/")
//            if (!dir.exists()) {
//                dir.mkdirs()
//            }
//        FileOutputStream(File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/" + barang.kodeBarang + ".png")).use { os ->
//            os.write(roundedSquarePngData)
//        }
//        val file = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/" + barang.kodeBarang + ".png")
//        shareQRFile(file,barang.kodeBarang.toString())

        val bitmap = generateQRCode(barang.kodeBarang.toString())
        val dir = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        try {
            val file = File(requireContext().getExternalFilesDir(null).toString() + "/QRCode/" + barang.kodeBarang + ".png")
            val fOut = FileOutputStream(file)
            //quality yang bagus
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            shareQRFile(file,barang.kodeBarang.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareQRFile(file: File,title:String) {
        val uri = FileProvider.getUriForFile(requireContext(), "com.vr.audiolibscan.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Qr Code $title"))
    }
    private fun generateQRCode(text: String): Bitmap {
        val width = 150
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix =
                codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    bitmap.setPixel(x, y, color)
                }
            }
        } catch (e: WriterException) {

            Log.d(TAG, "generateQRCode: ${e.message}")

        }
        return bitmap
    }

}