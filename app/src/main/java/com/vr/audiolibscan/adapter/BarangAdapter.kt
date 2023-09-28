package com.vr.audiolibscan.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vr.audiolibscan.R
import com.vr.audiolibscan.model.BarangModel
import java.util.Locale


class BarangAdapter(
    private var barangList: MutableList<BarangModel>,
    val context: Context,
    private val onEditClickListener: (BarangModel) -> Unit,
    private val onHapusClickListener: (BarangModel) -> Unit,
) : RecyclerView.Adapter<BarangAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<BarangModel> = mutableListOf()
    init {
        filteredBarangList.addAll(barangList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredBarangList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredBarangList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in barangList) {
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredBarangList.add(product)
                    Log.d("Ada ", product.nama.toString())
                }
            }
        } else {
            filteredBarangList.addAll(barangList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredBarangList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = currentBarang.nama
        holder.tvKode.text = currentBarang.kodeBarang
        holder.tvPenjelasan.text = currentBarang.penjelasan

        Glide.with(context)
            .load(currentBarang.fotoBarang)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(holder.imgBarang)
        holder.btnUbah.setOnClickListener { onEditClickListener(currentBarang) }
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaBarang)
        val tvKode: TextView = itemView.findViewById(R.id.tvKodeBarang)
        val tvPenjelasan: TextView = itemView.findViewById(R.id.tvPenjelasanBarang)
        val imgBarang: ImageView = itemView.findViewById(R.id.fotoBarang)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
