package com.example.sahamatik_lig.view

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sahamatik_lig.databinding.ActivityLigDetailBinding
import androidx.fragment.app.Fragment

class LigDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLigDetailBinding
    private lateinit var siralamaFragment: SiralamaFragment
    private lateinit var maclarFragment: MaclarFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLigDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent ile gelen verileri karşılıyoruz
        val ligAdi = intent.getStringExtra("LIG_ADI") ?: "LİG DETAY"
        val formatTipi = intent.getStringExtra("FORMAT_TIPI") ?: "KLASIK"
        val takimlar = intent.getStringArrayListExtra("TAKIMLAR") ?: arrayListOf()

        @Suppress("UNCHECKED_CAST")
        val gruplarMap = intent.getSerializableExtra("GRUPLAR_MAP") as? HashMap<String, ArrayList<String>>

        binding.tvLigTitle.text = ligAdi

        // Geri Butonu
        binding.tvBack.setOnClickListener { finish() }

        // Format tipine göre Fragment instance'larını ayağa kaldırıyoruz
        if (formatTipi == "GRUP" && gruplarMap != null) {
            siralamaFragment = SiralamaFragment.newInstanceGrup(gruplarMap)
            maclarFragment = MaclarFragment.newInstanceGrup(gruplarMap) // 🚀 BURAYI newInstanceGrup YAPTIK
        } else {
            siralamaFragment = SiralamaFragment.newInstance(takimlar)
            maclarFragment = MaclarFragment.newInstance(takimlar)
        }

        // İlk açılışta Sıralama Fragment'ını ekrana basıyoruz
        replaceFragment(siralamaFragment)
        setButtonSelected(isSiralamaSelected = true)

        // Buton Tıklamaları
        binding.btnSiralama.setOnClickListener {
            replaceFragment(siralamaFragment)
            setButtonSelected(isSiralamaSelected = true)
        }

        binding.btnMaclar.setOnClickListener {
            replaceFragment(maclarFragment)
            setButtonSelected(isSiralamaSelected = false)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun setButtonSelected(isSiralamaSelected: Boolean) {
        if (isSiralamaSelected) {
            binding.btnSiralama.setBackgroundColor(Color.parseColor("#00C853")) // Yeşil
            binding.btnSiralama.setTextColor(Color.WHITE)

            binding.btnMaclar.setBackgroundColor(Color.parseColor("#D9D9D9")) // Gri
            binding.btnMaclar.setTextColor(Color.BLACK)
        } else {
            binding.btnMaclar.setBackgroundColor(Color.parseColor("#00C853")) // Yeşil
            binding.btnMaclar.setTextColor(Color.WHITE)

            binding.btnSiralama.setBackgroundColor(Color.parseColor("#D9D9D9")) // Gri
            binding.btnSiralama.setTextColor(Color.BLACK)
        }
    }
}