package com.adam.pertepiece

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class DeclarationFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declaration_form)

        // 1. Liaison des éléments visuels
        val spinner = findViewById<Spinner>(R.id.spinnerDocType)
        val inputDate = findViewById<EditText>(R.id.inputDate)
        val inputLocation = findViewById<EditText>(R.id.inputLocation)
        val inputDescription = findViewById<EditText>(R.id.inputDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // 2. Remplir le menu déroulant
        val documents = listOf("Carte Nationale d'Identité", "Passeport Biométrique", "Permis de Conduire", "Extrait de Naissance")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, documents)
        spinner.adapter = adapter

        // 3. Action du bouton Envoyer
        btnSubmit.setOnClickListener {
            val rawDate = inputDate.text.toString() // ex: 14/12/2025
            val location = inputLocation.text.toString()
            val description = inputDescription.text.toString()
            val docTypeId = (spinner.selectedItemPosition + 1).toLong()

            // --- CORRECTION DATE ICI ---
            // On transforme "14/12/2025" en "2025-12-14"
            val formattedDate = formatDateForSupabase(rawDate)

            if (formattedDate == null) {
                Toast.makeText(this, "Format de date invalide. Utilisez JJ/MM/AAAA", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // ---------------------------

            if (location.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val user = SupabaseClient.client.auth.currentUserOrNull()
                        if (user != null) {

                            val declaration = Declaration(
                                userId = user.id,
                                documentTypeId = docTypeId,
                                incidentDate = formattedDate, // On envoie la date corrigée
                                incidentLocation = location,
                                description = description,
                                status = "EN_ATTENTE"
                            )

                            SupabaseClient.client.from("declarations").insert(declaration)

                            Toast.makeText(this@DeclarationFormActivity, "Déclaration envoyée !", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@DeclarationFormActivity, "Erreur : Non connecté", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DeclarationFormActivity, "Erreur envoi: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            } else {
                Toast.makeText(this, "Le lieu est obligatoire", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fonction utilitaire pour corriger la date
    private fun formatDateForSupabase(dateInput: String): String? {
        // Regex pour forcer le format JJ/MM/AAAA
        val regex = Regex("""^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$""")
        val match = regex.find(dateInput)

        if (match != null) {
            val (day, month, year) = match.destructured
            // On s'assure que jour et mois ont 2 chiffres (ex: 5 -> 05)
            val dayPad = day.padStart(2, '0')
            val monthPad = month.padStart(2, '0')
            return "$year-$monthPad-$dayPad"
        }
        return null // Format invalide
    }
}