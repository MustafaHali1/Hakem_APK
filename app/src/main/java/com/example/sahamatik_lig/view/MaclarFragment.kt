package com.example.sahamatik_lig.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sahamatik_lig.adapter.MacAdapter
import com.example.sahamatik_lig.databinding.DialogSkorBinding
import com.example.sahamatik_lig.databinding.FragmentMaclarBinding
import com.example.sahamatik_lig.model.Mac
import java.util.ArrayList

class MaclarFragment : Fragment() {

    private var _binding: FragmentMaclarBinding? = null
    private val binding get() = _binding!!

    private var formatTipi: String = "KLASIK"
    private var takimIsimleri: ArrayList<String>? = null
    private var gruplarMap: HashMap<String, ArrayList<String>>? = null
    // MaclarFragment.kt içinde en üste:
    private val viewModel: LigViewModel by activityViewModels()

    private lateinit var macAdapter: MacAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formatTipi = it.getString(ARG_FORMAT, "KLASIK")
            takimIsimleri = it.getStringArrayList(ARG_TAKIMLAR)
            @Suppress("UNCHECKED_CAST")
            gruplarMap = it.getSerializable(ARG_GRUPLAR) as? HashMap<String, ArrayList<String>>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaclarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[LigViewModel::class.java]

        if (formatTipi == "GRUP" && gruplarMap != null) {
            viewModel.grupTurnuvasiBaslat(gruplarMap!!)
        } else {
            takimIsimleri?.let { takimlar ->
                viewModel.ligiBaslat(takimlar)
            }
        }

        macAdapter = MacAdapter(viewModel.macListesi) { secilenMac ->
            showScoreDialog(secilenMac)
        }

        binding.rvMaclar.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaclar.adapter = macAdapter
    }

    private fun showScoreDialog(mac: Mac) {
        val dialogInflater = LayoutInflater.from(requireContext())
        val dialogBinding = DialogSkorBinding.inflate(dialogInflater)

        dialogBinding.tvTakim1.text = mac.takim1
        dialogBinding.tvTakim2.text = mac.takim2

        mac.skor1?.let { dialogBinding.Skor1.setText(it.toString()) }
        mac.skor2?.let { dialogBinding.Skor2.setText(it.toString()) }

        AlertDialog.Builder(requireContext())
            .setTitle("Maç Skoru Gir")
            .setView(dialogBinding.root)
            .setPositiveButton("Kaydet") { _, _ ->
                val s1 = dialogBinding.Skor1.text.toString().toIntOrNull()
                val s2 = dialogBinding.Skor2.text.toString().toIntOrNull()

                if (s1 != null && s2 != null) {
                    viewModel.skorGuncelle(mac, s1, s2)
                    macAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Skor kaydedildi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Lütfen tüm skorları girin!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FORMAT = "format_tipi"
        private const val ARG_TAKIMLAR = "takimlar"
        private const val ARG_GRUPLAR = "gruplar_map"

        @JvmStatic
        fun newInstance(takimlar: ArrayList<String>) =
            MaclarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FORMAT, "KLASIK")
                    putStringArrayList(ARG_TAKIMLAR, takimlar)
                }
            }

        @JvmStatic
        fun newInstanceGrup(gruplar: HashMap<String, ArrayList<String>>) =
            MaclarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FORMAT, "GRUP")
                    putSerializable(ARG_GRUPLAR, gruplar)
                }
            }
    }
}