package com.vr.audiolibscan.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vr.audiolibscan.R
import com.vr.audiolibscan.databinding.FragmentMetaBinding
import com.vr.audiolibscan.databinding.FragmentResultBinding
import com.vr.audiolibscan.tools.getBarang

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MetaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MetaFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentMetaBinding

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
        return inflater.inflate(R.layout.fragment_meta, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMetaBinding.bind(view)
        initView()
    }
    fun initView() {
        val barang = getBarang(requireContext())
        binding.tvMetaTitleValue.text = barang.title
        binding.tvMetaCreatorValue.text = barang.creator
        binding.tvMetaSubjectValue.text = barang.subject
        binding.tvMetaDescriptionValue.text = barang.description
        binding.tvMetaPublisherValue.text = barang.publisher
        binding.tvMetaContributorValue.text = barang.contributor
        binding.tvMetaDateValue.text = barang.date
        binding.tvMetaTypeValue.text = barang.type
        binding.tvMetaFormatValue.text = barang.format
        binding.tvMetaIdentifierValue.text = barang.identifier
        binding.tvMetaSourceValue.text = barang.source
        binding.tvMetaLanguageValue.text = barang.language
        binding.tvMetaRelationValue.text = barang.relation
        binding.tvMetaCoverageValue.text = barang.coverage
        binding.tvMetaRightsValue.text = barang.rights
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MetaFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MetaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}