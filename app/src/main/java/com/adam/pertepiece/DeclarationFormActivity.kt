package com.adam.pertepiece

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Locale

class DeclarationFormActivity : AppCompatActivity() {

    private var declarationIdToEdit: String? = null

    // Variables pour l'image
    private var imageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    // Le sélecteur d'image (Galerie)
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivPreview.setImageURI(uri) // Affiche l'aperçu
            ivPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declaration_form)

        // 1. Liaison des éléments visuels
        val spinner = findViewById<Spinner>(R.id.spinnerDocType)
        val inputDate = findViewById<EditText>(R.id.inputDate)
        val inputLocation = findViewById<EditText>(R.id.inputLocation)
        val inputDescription = findViewById<EditText>(R.id.inputDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val headerTitle = findViewById<TextView>(R.id.tvHeaderTitle)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        ivPreview = findViewById(R.id.ivPreview)

        // --- CORRECTION : CALENDRIER INTELLIGENT (Une seule fois !) ---
        inputDate.setOnClickListener {
            // A. On récupère la date actuelle
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // B. On ouvre la fenêtre de calendrier
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // C. Quand l'utilisateur choisit, on formate : JJ/MM/AAAA
                    val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)

                    // D. On écrit le résultat dans le champ
                    inputDate.setText(formattedDate)
                },
                year, month, day
            )

            // (Optionnel) Empêcher de choisir une date dans le futur
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }

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

        // Action bouton retour
        btnBack.setOnClickListener {
            finish()
        }

        // 3. CHECK EDIT MODE (Mode Modification)
        val editJson = intent.getStringExtra("EDIT_DECLARATION_JSON")
        if (editJson != null) {
            val declaration = Json.decodeFromString<Declaration>(editJson)
            declarationIdToEdit = declaration.id

            // Remplir les champs
            inputDate.setText(declaration.incidentDate)
            inputLocation.setText(declaration.incidentLocation)
            inputDescription.setText(declaration.description)

            // Sélectionner le spinner
            val index = docTypes.indexOfFirst { it.id == declaration.documentTypeId }
            if (index >= 0) spinner.setSelection(index)

            // Changement des textes
            btnSubmit.text = "Mettre à jour"
            headerTitle.text = "Modifier la Déclaration"
        }

        // 4. Clic sur "Ajouter une photo"
        btnSelectImage.setOnClickListener {
            pickImage.launch("image/*") // Ouvre la galerie
        }

        // 5. Gestion du clic sur "Soumettre" (Upload + Save)
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

                // On lance le processus (Upload -> Database)
                lifecycleScope.launch {
                    try {
                        val user = SupabaseClient.client.auth.currentUserOrNull()
                        if (user != null) {

                            // A. UPLOAD DE L'IMAGE (Si une image est choisie)
                            var imageUrl: String? = null

                            if (imageUri != null) {
                                // Lire les données de l'image
                                val byteArray = contentResolver.openInputStream(imageUri!!)?.readBytes()
                                if (byteArray != null) {
                                    // Nom unique : img-TIMESTAMP.jpg
                                    val fileName = "img-${System.currentTimeMillis()}.jpg"
                                    val bucket = SupabaseClient.client.storage.from("declarations")

                                    // Upload
                                    bucket.upload(fileName, byteArray)

                                    // Récupérer l'URL publique
                                    imageUrl = bucket.publicUrl(fileName)
                                }
                            }

                            // B. SAUVEGARDE EN BASE DE DONNÉES
                            if (declarationIdToEdit != null) {
                                // MODE UPDATE
                                SupabaseClient.client.from("declarations").update(
                                    {
                                        set("document_type_id", selectedDoc.id)
                                        set("incident_date", formattedDate)
                                        set("incident_location", locationStr)
                                        set("description", descriptionStr)
                                        // On met à jour l'image SEULEMENT si une nouvelle a été envoyée
                                        if (imageUrl != null) {
                                            set("image_url", imageUrl)
                                        }
                                    }
                                ) {
                                    filter {
                                        eq("id", declarationIdToEdit!!)
                                    }
                                }
                                Toast.makeText(this@DeclarationFormActivity, "Déclaration mise à jour !", Toast.LENGTH_LONG).show()
                            } else {
                                // MODE INSERT (Nouvelle déclaration)
                                val newDeclaration = Declaration(
                                    userId = user.id,
                                    documentTypeId = selectedDoc.id,
                                    incidentDate = formattedDate,
                                    incidentLocation = locationStr,
                                    description = descriptionStr,
                                    status = "EN_ATTENTE",
                                    imageUrl = imageUrl // On ajoute l'URL ici
                                )
                                SupabaseClient.client.from("declarations").insert(newDeclaration)
                                Toast.makeText(this@DeclarationFormActivity, "Déclaration enregistrée !", Toast.LENGTH_LONG).show()
                            }

                            finish() // Ferme l'écran
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

    private fun formatDateForSupabase(dateInput: String): String? {
        val regex = Regex("""^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$""")
        val match = regex.find(dateInput)

        if (match != null) {
            val (day, month, year) = match.destructured
            val dayPad = day.padStart(2, '0')
            val monthPad = month.padStart(2, '0')
            return "$year-$monthPad-$dayPad"
        }
        return null
    }
}