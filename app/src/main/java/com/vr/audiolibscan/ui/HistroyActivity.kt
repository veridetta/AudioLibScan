package com.vr.audiolibscan.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.audiolibscan.R
import com.vr.audiolibscan.adapter.BarangAdapter
import com.vr.audiolibscan.adapter.HistoryAdapter
import com.vr.audiolibscan.model.BarangModel
import com.vr.audiolibscan.tools.showSnack
import com.vr.audiolibscan.ui.auth.EditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HistroyActivity : AppCompatActivity() {
    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var barangAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val barangList: MutableList<BarangModel> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_histroy)
        recyclerView = findViewById(R.id.rcBarang)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@HistroyActivity, 1)
            // set the custom adapter to the RecyclerView
            barangAdapter = HistoryAdapter(
                barangList,
                this@HistroyActivity
            ){ barang -> openBarang(barang) }
        }
        val shimmerContainer = findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(shimmerContainer) }

        recyclerView.adapter = barangAdapter

        val searchEditText = findViewById<EditText>(R.id.btnCari)
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
    private fun readData(shimmerContainer: ShimmerFrameLayout) {
        //get uid dari shared preference
        val sharedPref = getSharedPreferences("user", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "")
        Log.d(TAG, "UID : $uid")
        if (uid != "") {
            shimmerContainer.startShimmer() // Start shimmer effect
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val result = mFirestore.collection("history").get().await()
                    val plants = mutableListOf<BarangModel>()
                    var jum = 0
                    for (document in result) {
                        val plant = document.toObject(BarangModel::class.java)
                        val docId = document.id
                        plant.documentId = docId
                        plants.add(plant)
                        Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                        jum++
                    }

                    withContext(Dispatchers.Main) {
                        barangList.addAll(plants)
                        barangAdapter.filteredBarangList.addAll(plants)
                        barangAdapter.notifyDataSetChanged()
                        shimmerContainer.stopShimmer() // Stop shimmer effect
                        shimmerContainer.visibility = View.GONE // Hide shimmer container
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.w(TAG, "Error getting documents : $e")
                        shimmerContainer.stopShimmer() // Stop shimmer effect
                        shimmerContainer.visibility = View.GONE // Hide shimmer container
                    }
                }
            }
        } else {
            showSnack(this@HistroyActivity, "Silahkan scan terlebih dahulu")
        }
    }
    private fun openBarang(barang: BarangModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, ResultHistroyActivity::class.java)
        intent.putExtra("documentId", barang.documentId)
        intent.putExtra("nama", barang.nama)
        intent.putExtra("fotoBarang", barang.fotoBarang)
        intent.putExtra("kodeBarang", barang.kodeBarang)
        intent.putExtra("penjelasan", barang.penjelasan)
        intent.putExtra("barangId", barang.barangId)
        intent.putExtra("uid", barang.uid)
        startActivity(intent)
    }
}