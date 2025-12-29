package com.adam.pertepiece

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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

class DeclarationFormActivity : AppCompatActivity() {

    private var declarationIdToEdit: String? = null

    // NOUVEAU : Variables pour l'image
    private var imageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    // NOUVEAU : Le sélecteur d'image (Galerie)
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
        val headerTitle = findViewById<TextView>(R.id.headerTitle)

        // NOUVEAU : Liaison des éléments image
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        ivPreview = findViewById(R.id.ivPreview)

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

            // Note: Pour afficher l'image existante ici, il faudrait utiliser Coil.
            // Pour l'instant, on laisse l'utilisateur en choisir une nouvelle s'il veut changer.
        }

        // 4. NOUVEAU : Clic sur "Ajouter une photo"
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