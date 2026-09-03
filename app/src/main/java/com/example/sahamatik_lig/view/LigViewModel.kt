package com.example.sahamatik_lig.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sahamatik_lig.model.Mac
import com.example.sahamatik_lig.model.TakimPuan
import com.example.sahamatik_lig.util.FiksturHelper

class LigViewModel : ViewModel() {

    val macListesi = ArrayList<Mac>()

    private val _puanDurumu = MutableLiveData<List<TakimPuan>>()
    val puanDurumu: LiveData<List<TakimPuan>> get() = _puanDurumu

    private var takimlar = ArrayList<String>()

    fun ligiBaslat(gelenTakimlar: List<String>) {
        if (takimlar.isEmpty()) {
            takimlar.addAll(gelenTakimlar)
        }
        if (macListesi.isEmpty()) {
            macListesi.addAll(FiksturHelper.fiksturOlustur(takimlar))
        }
        puanDurumunuHesapla()
    }

    fun skorGuncelle(mac: Mac, s1: Int, s2: Int) {
        mac.skor1 = s1
        mac.skor2 = s2
        mac.isOynandi = true
        puanDurumunuHesapla()
    }

    fun puanDurumunuHesapla() {
        val tablo = LinkedHashMap<String, TakimPuan>()

        // 1. Önce takimlar listesindeki takımları ekle
        for (takim in takimlar) {
            tablo[takim] = TakimPuan(takimAdi = takim)
        }

        // 2. Maçlardaki takımları tabloya ekle (garanti yöntem)
        for (mac in macListesi) {
            if (!tablo.containsKey(mac.takim1)) tablo[mac.takim1] = TakimPuan(takimAdi = mac.takim1)
            if (!tablo.containsKey(mac.takim2)) tablo[mac.takim2] = TakimPuan(takimAdi = mac.takim2)
        }

        // 3. Oynanan maçları işle
        for (mac in macListesi) {
            if (mac.isOynandi) {
                val s1 = mac.skor1
                val s2 = mac.skor2
                val t1 = tablo[mac.takim1]
                val t2 = tablo[mac.takim2]

                if (s1 != null && s2 != null && t1 != null && t2 != null) {
                    t1.oynanan++
                    t2.oynanan++
                    t1.atilanGol += s1
                    t1.yenilenGol += s2
                    t2.atilanGol += s2
                    t2.yenilenGol += s1

                    when {
                        s1 > s2 -> {
                            t1.galibiyet++
                            t1.puan += 3
                            t2.maglubiyet++
                        }
                        s2 > s1 -> {
                            t2.galibiyet++
                            t2.puan += 3
                            t1.maglubiyet++
                        }
                        else -> {
                            t1.beraberlik++
                            t2.beraberlik++
                            t1.puan += 1
                            t2.puan += 1
                        }
                    }

                    t1.averaj = t1.atilanGol - t1.yenilenGol
                    t2.averaj = t2.atilanGol - t2.yenilenGol
                }
            }
        }

        val siraliListe = tablo.values.sortedWith(
            compareByDescending<TakimPuan> { it.puan }
                .thenByDescending { it.averaj }
                .thenByDescending { it.atilanGol }
        )

        _puanDurumu.value = siraliListe
    }
}