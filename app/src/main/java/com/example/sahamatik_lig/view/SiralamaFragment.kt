package com.example.sahamatik_lig.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sahamatik_lig.adapter.PuanDurumuAdapter
import com.example.sahamatik_lig.databinding.FragmentSiralamaBinding
import com.example.sahamatik_lig.model.TakimPuan
import androidx.fragment.app.activityViewModels

class SiralamaFragment : Fragment() {
    private var _binding: FragmentSiralamaBinding? = null
    private val binding get() = _binding!!
    private var takimIsimleri: ArrayList<String>? = null

    private val viewModel: LigViewModel by activityViewModels()

    private lateinit var puanAdapter: PuanDurumuAdapter
    private val puanListesi = ArrayList<TakimPuan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            takimIsimleri = it.getStringArrayList(ARG_TAKIMLAR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiralamaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter Kurulumu
        puanAdapter = PuanDurumuAdapter(puanListesi)
        binding.rvPuanDurumu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPuanDurumu.adapter = puanAdapter

        // Takımları ViewModel'a ilet
        takimIsimleri?.let {
            viewModel.ligiBaslat(it)
        }

        // Skor girildikçe hesaplanan yeni puan durumunu tabloya bas
        viewModel.puanDurumu.observe(viewLifecycleOwner) { siraliListe ->
            if (siraliListe != null && siraliListe.isNotEmpty()) {
                puanListesi.clear()
                puanListesi.addAll(siraliListe)
                puanAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAKIMLAR = "takimlar"

        @JvmStatic
        fun newInstance(takimlar: ArrayList<String>) =
            SiralamaFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_TAKIMLAR, takimlar)
                }
            }
    }
}