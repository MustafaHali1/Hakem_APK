package com.example.sahamatik_lig.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sahamatik_lig.adapter.LigAdapter
import com.example.sahamatik_lig.databinding.ActivityMainBinding
import com.example.sahamatik_lig.databinding.DialogAddLeagueBinding
import com.example.sahamatik_lig.model.Lig
import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.sahamatik_lig.databinding.BottomSheetLigSecimBinding
import com.example.sahamatik_lig.databinding.DialogGrupTurnuvaEkleBinding
import com.example.sahamatik_lig.model.TurnuvaTipi



// Activity'nin olduğu pakete göre değişebilir

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ligList: ArrayList<Lig>
    private lateinit var adapter: LigAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Güncellenmiş modele uygun varsayılan ligler
        ligList = arrayListOf(
            Lig(1, "Sultanbeyli Ligi", 4, listOf("Takım A", "Takım B", "Takım C", "Takım D")),
            Lig(2, "Pendik Ligi", 2, listOf("Pendik Spor", "Sahil FC")),
            Lig(3, "Kartal Ligi", 2, listOf("Kartal SK", "Atalar FC"))
        )

        // RecyclerView ve Adapter kurulumu
        adapter = LigAdapter(ligList) { secilenLig ->
            val intent = Intent(this@MainActivity, LigDetailActivity::class.java).apply {
                putExtra("LIG_ADI", secilenLig.name)
                putExtra("FORMAT_TIPI", secilenLig.formatTipi)
                putStringArrayListExtra("TAKIMLAR", ArrayList(secilenLig.takimlar))

                // Grup turnuvası ise grupları da Intent'e ekle
                if (secilenLig.formatTipi == "GRUP" && secilenLig.gruplarMap != null) {
                    val gruplarHashMap = HashMap<String, ArrayList<String>>()
                    for ((grup, takimlar) in secilenLig.gruplarMap) {
                        gruplarHashMap[grup] = ArrayList(takimlar)
                    }
                    putExtra("GRUPLAR_MAP", gruplarHashMap)
                }
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        // Artı butonuna basınca pop-up açılır
        binding.fabAddLeague.setOnClickListener {
            showLigTuruSecimDialog()
        }
    }

    private fun showLigTuruSecimDialog() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetLigSecimBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        // 1. Klasik Lig seçildiğinde
        sheetBinding.cardKlasikLig.setOnClickListener {
            dialog.dismiss()
            showAddLeagueDialog()
        }

        // 2. Gruplu Turnuva seçildiğinde
        sheetBinding.cardGrupTurnuva.setOnClickListener {
            dialog.dismiss()
            showAddGroupTournamentDialog()
        }

        dialog.show()
    }

  //Grup ligi ekleme
    private fun showAddGroupTournamentDialog() {
        val dialogBinding = DialogGrupTurnuvaEkleBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("⚡ Gruplu Turnuva Oluştur")
            .setView(dialogBinding.root)
            .setPositiveButton("Oluştur") { _, _ ->
                val turnuvaAdi = dialogBinding.etTurnuvaAdi.text.toString().trim()
                val grupSayisiStr = dialogBinding.etGrupSayisi.text.toString().trim()
                val takimlarGirdi = dialogBinding.etTakimlar.text.toString().trim()

                val grupSayisi = grupSayisiStr.toIntOrNull() ?: 0

                val takimListesi = takimlarGirdi.split("\n", ",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                // Doğrulamalar
                if (turnuvaAdi.isEmpty()) {
                    Toast.makeText(this, "Lütfen turnuva adını girin!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (grupSayisi < 2) {
                    Toast.makeText(this, "Grup sayısı en az 2 olmalıdır!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val gerekenMinimumTakim = grupSayisi * 2
                if (takimListesi.size < gerekenMinimumTakim) {
                    Toast.makeText(
                        this,
                        "$grupSayisi grup için en az $gerekenMinimumTakim takım girmelisiniz!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                // Kura ile dağıtım
                val gruplar = com.example.sahamatik_lig.util.GrupTurnuvaHelper.gruplaraDagit(takimListesi, grupSayisi)

                // 1. Ana ekrandaki Lig modeline uygun nesneyi oluştur
                val yeniLig = Lig(
                    id = ligList.size + 1,
                    name = turnuvaAdi,
                    takimsayisi = takimListesi.size,
                    takimlar = takimListesi,
                    formatTipi = "GRUP",
                    gruplarMap = gruplar
                )

                // 2. Listeye ekle
                ligList.add(yeniLig)
                adapter.notifyItemInserted(ligList.size - 1)

                Toast.makeText(
                    this,
                    "$turnuvaAdi oluşturuldu: $grupSayisi grup, ${takimListesi.size} takım",
                    Toast.LENGTH_SHORT
                ).show()

                // 3. Güvenli Map dönüşümü ile Detay ekranına geçiş
                val gruplarHashMap = HashMap<String, ArrayList<String>>()
                for ((grup, takimlar) in gruplar) {
                    gruplarHashMap[grup] = ArrayList(takimlar)
                }

                val intent = Intent(this@MainActivity, LigDetailActivity::class.java).apply {
                    putExtra("LIG_ADI", turnuvaAdi)
                    putExtra("FORMAT_TIPI", "GRUP")
                    putExtra("GRUPLAR_MAP", gruplarHashMap)
                }
                startActivity(intent)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun ligBilgileriAl(secilenTip: TurnuvaTipi) {
        if (secilenTip == TurnuvaTipi.KLASIK_LIG) {
            // Eski çalışan klasik lig ekleme diyaloğunu aç
            showAddLeagueDialog()
        } else {
            // Gruplu turnuva için grup sayısını ve takımları alacak formu açacağız
            android.widget.Toast.makeText(this, "Gruplu Turnuva Modu Seçildi", android.widget.Toast.LENGTH_SHORT).show()
        }
    }


      // tablo ligi ekleme
      private fun showAddLeagueDialog() {

          val dialogBinding = DialogAddLeagueBinding.inflate(layoutInflater)

          val teamEditTextList = mutableListOf<EditText>()
          var currentStep = 1
          var leagueName = ""

          val builder = AlertDialog.Builder(this)
              .setView(dialogBinding.root)
              .setPositiveButton("Devam Et", null)
              .setNegativeButton("İptal") { dialog, _ -> dialog.dismiss() }

          val alertDialog = builder.create()
          alertDialog.show()

          alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
              if (currentStep == 1) {
                  // ADIM 1: LİG ADI VE TAKIM SAYISI ALMA
                  leagueName = dialogBinding.etLeagueName.text.toString().trim()
                  val teamCountStr = dialogBinding.etTeamCount.text.toString().trim()

                  if (leagueName.isNotEmpty() && teamCountStr.isNotEmpty()) {
                      val teamCount = teamCountStr.toIntOrNull()

                      if (teamCount == null || teamCount < 2) {
                          Toast.makeText(this, "En az 2 takım olmalı!", Toast.LENGTH_SHORT).show()
                          return@setOnClickListener
                      }

                      dialogBinding.containerTeams.removeAllViews()
                      teamEditTextList.clear()

                      for (i in 1..teamCount) {
                          val editText = EditText(this)
                          editText.hint = "$i. Takım Adı"
                          editText.setSingleLine()

                          dialogBinding.containerTeams.addView(editText)
                          teamEditTextList.add(editText)
                      }

                      dialogBinding.layoutStep1.visibility = View.GONE
                      dialogBinding.layoutStep2.visibility = View.VISIBLE
                      dialogBinding.tvDialogTitle.text = "Takım isimlerini Gir (2/2)"

                      alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Kura Cek & Ligi Baslat"
                      currentStep = 2

                  } else {
                      Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                  }

              } else if (currentStep == 2) {
                  // ADIM 2: TAKIM İSİMLERİNİ TOPLAMA VE LİSTEYE EKLEME
                  val teamNames = mutableListOf<String>()
                  var allFilled = true

                  for (et in teamEditTextList) {
                      val name = et.text.toString().trim()
                      if (name.isEmpty()) {
                          allFilled = false
                          break
                      }
                      teamNames.add(name)
                  }

                  if (allFilled) {
                      // YENİ LİGİ MODELİMİZE UYGUN OLUŞTURUYORUZ
                      val yeniLig = Lig(
                          id = ligList.size + 1,
                          name = leagueName,
                          takimsayisi = teamNames.size,
                          takimlar = teamNames
                      )

                      // LİSTEYE EKLE VE ADAPTER'A HABER VER
                      ligList.add(yeniLig)
                      adapter.notifyItemInserted(ligList.size - 1)

                      Toast.makeText(this, "$leagueName başarıyla kuruldu!", Toast.LENGTH_SHORT).show()
                      alertDialog.dismiss()

                      // DETAY EKRANINA GEÇİŞ (FORMAT_TIPI EKLENDİ)
                      val intent = Intent(this@MainActivity, LigDetailActivity::class.java).apply {
                          putExtra("LIG_ADI", leagueName)
                          putExtra("FORMAT_TIPI", "KLASIK")
                          putStringArrayListExtra("TAKIMLAR", ArrayList(teamNames))
                      }
                      startActivity(intent)

                  } else {
                      Toast.makeText(this, "Lütfen tüm takım isimlerini girin!", Toast.LENGTH_SHORT).show()
                  }
              }
          }
      }
}