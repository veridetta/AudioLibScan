package com.vr.audiolibscan.ui.auth.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.audiolibscan.R
import com.vr.audiolibscan.adapter.BarangAdapter
import com.vr.audiolibscan.model.BarangModel
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.AdminActivity
import com.vr.audiolibscan.ui.auth.EditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
                { barang -> hapusBarang(barang) }
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
        startActivity(intent)
        requireActivity().finish()
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

}