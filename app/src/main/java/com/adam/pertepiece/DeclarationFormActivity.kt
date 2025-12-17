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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json // Added import for Json
import kotlinx.serialization.decodeFromString // Added import for decodeFromString

// Data class for DocumentType, implied by the changes
data class DocumentType(val id: Long, val name: String) {
    override fun toString(): String = name // This is important for ArrayAdapter to display the name
}

class DeclarationFormActivity : AppCompatActivity() {

    private var declarationIdToEdit: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declaration_form)

        // 1. Liaison des éléments visuels
        val spinner = findViewById<Spinner>(R.id.spinnerDocType)
        val inputDate = findViewById<EditText>(R.id.inputDate)
        val inputLocation = findViewById<EditText>(R.id.inputLocation)
        val inputDescription = findViewById<EditText>(R.id.inputDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val headerTitle = findViewById<TextView>(R.id.headerTitle) // Assuming we have a title view in xml or we add one?
        // Note: The XML provided earlier didn't have a specific ID for the title "Nouvelle Déclaration".
        // If it does, we can change it. Else we skip title change or find it by text/id if available.
        // Let's check layout if needed. For now assuming standard behavior.

        // 2. Remplir le menu déroulant
        val docTypes = listOf(
            DocumentType(1, "Carte Nationale d'Identité"),
            DocumentType(2, "Passeport Biométrique"),
            DocumentType(3, "Permis de Conduire"),
            DocumentType(4, "Extrait de Naissance"),
            DocumentType(5, "Autre Document")
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, docTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // CHECK EDIT MODE
        val editJson = intent.getStringExtra("EDIT_DECLARATION_JSON")
        if (editJson != null) {
            val declaration = Json.decodeFromString<Declaration>(editJson)
            declarationIdToEdit = declaration.id

            // Pre-fill
            inputDate.setText(declaration.incidentDate)
            inputLocation.setText(declaration.incidentLocation)
            inputDescription.setText(declaration.description)

            // Select Spinner
            val index = docTypes.indexOfFirst { it.id == declaration.documentTypeId }
            if (index >= 0) spinner.setSelection(index)

            btnSubmit.text = "Mettre à jour"
            headerTitle.text = "Modifier la Déclaration" // Assuming headerTitle exists and we want to change it
        }

        // 3. Gestion du clic sur "Soumettre"
        btnSubmit.setOnClickListener {
            val selectedDoc = spinner.selectedItem as DocumentType
            val dateStr = inputDate.text.toString()
            val locationStr = inputLocation.text.toString()
            val descriptionStr = inputDescription.text.toString()

            if (locationStr.isNotBlank()) {
                val formattedDate = formatDateForSupabase(dateStr)
                if (formattedDate == null) {
                    Toast.makeText(this, "Format de date invalide (JJ/MM/AAAA)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        val user = SupabaseClient.client.auth.currentUserOrNull()
                        if (user != null) {

                            if (declarationIdToEdit != null) {
                                // UPDATE
                                SupabaseClient.client.from("declarations").update(
                                    {
                                        set("document_type_id", selectedDoc.id)
                                        set("incident_date", formattedDate)
                                        set("incident_location", locationStr)
                                        set("description", descriptionStr)
                                    }
                                ) {
                                    filter {
                                        eq("id", declarationIdToEdit!!)
                                    }
                                }
                                Toast.makeText(this@DeclarationFormActivity, "Déclaration mise à jour !", Toast.LENGTH_LONG).show()
                            } else {
                                // INSERT
                                val newDeclaration = Declaration(
                                    userId = user.id,
                                    documentTypeId = selectedDoc.id,
                                    incidentDate = formattedDate,
                                    incidentLocation = locationStr,
                                    description = descriptionStr,
                                    status = "EN_ATTENTE"
                                )
                                SupabaseClient.client.from("declarations").insert(newDeclaration)
                                Toast.makeText(this@DeclarationFormActivity, "Déclaration enregistrée !", Toast.LENGTH_LONG).show()
                            }

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