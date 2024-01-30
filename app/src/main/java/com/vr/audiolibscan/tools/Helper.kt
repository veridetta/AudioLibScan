package com.vr.audiolibscan.tools

import android.content.Context
import com.vr.audiolibscan.model.BarangModel

fun saveBarang(barangModel: BarangModel, context: Context) {
    val sharedPreferences = context.getSharedPreferences("BARANG", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("nama", barangModel.nama)
    editor.putString("documentId", barangModel.documentId)
    editor.putString("fotoBarang", barangModel.fotoBarang)
    editor.putString("kodeBarang", barangModel.kodeBarang)
    editor.putString("penjelasan", barangModel.penjelasan)
    editor.putString("uid", barangModel.uid)
    editor.putString("barangId", barangModel.barangId)
    editor.putString("title", barangModel.title)
    editor.putString("creator", barangModel.creator)
    editor.putString("subject", barangModel.subject)
    editor.putString("description", barangModel.description)
    editor.putString("publisher", barangModel.publisher)
    editor.putString("contributor", barangModel.contributor)
    editor.putString("date", barangModel.date)
    editor.putString("type", barangModel.type)
    editor.putString("format", barangModel.format)
    editor.putString("identifier", barangModel.identifier)
    editor.putString("source", barangModel.source)
    editor.putString("language", barangModel.language)
    editor.putString("relation", barangModel.relation)
    editor.putString("coverage", barangModel.coverage)
    editor.putString("rights", barangModel.rights)
    editor.apply()

}
fun getBarang(context: Context): BarangModel {
    val sharedPreferences = context.getSharedPreferences("BARANG", Context.MODE_PRIVATE)
    val barangModel = BarangModel()
    barangModel.nama = sharedPreferences.getString("nama", "")
    barangModel.documentId = sharedPreferences.getString("documentId", "")
    barangModel.fotoBarang = sharedPreferences.getString("fotoBarang", "")
    barangModel.kodeBarang = sharedPreferences.getString("kodeBarang", "")
    barangModel.penjelasan = sharedPreferences.getString("penjelasan", "")
    barangModel.uid = sharedPreferences.getString("uid", "")
    barangModel.barangId = sharedPreferences.getString("barangId", "")
    barangModel.title = sharedPreferences.getString("title", "")
    barangModel.creator = sharedPreferences.getString("creator", "")
    barangModel.subject = sharedPreferences.getString("subject", "")
    barangModel.description = sharedPreferences.getString("description", "")
    barangModel.publisher = sharedPreferences.getString("publisher", "")
    barangModel.contributor = sharedPreferences.getString("contributor", "")
    barangModel.date = sharedPreferences.getString("date", "")
    barangModel.type = sharedPreferences.getString("type", "")
    barangModel.format = sharedPreferences.getString("format", "")
    barangModel.identifier = sharedPreferences.getString("identifier", "")
    barangModel.source = sharedPreferences.getString("source", "")
    barangModel.language = sharedPreferences.getString("language", "")
    barangModel.relation = sharedPreferences.getString("relation", "")
    barangModel.coverage = sharedPreferences.getString("coverage", "")
    barangModel.rights = sharedPreferences.getString("rights", "")
    return barangModel
}
fun clearBarang(context: Context) {
    val sharedPreferences = context.getSharedPreferences("BARANG", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()
}