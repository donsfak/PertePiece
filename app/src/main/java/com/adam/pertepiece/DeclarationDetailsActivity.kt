package com.adam.pertepiece

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View // Indispensable pour .visibility = View.GONE
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView // Indispensable pour ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load // Indispensable pour charger l'image
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeclarationDetailsActivity : AppCompatActivity() {

    private lateinit var declarationId: String
    private lateinit var btnMarkFound: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declaration_details)

        // 1. Receive Data
        val json = intent.getStringExtra("DECLARATION_JSON") ?: run {
            finish()
            return
        }
        val declaration = Json.decodeFromString<Declaration>(json)
        declarationId = declaration.id ?: run {
            finish()
            return
        }

        // 2. Init Views
        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvDocType = findViewById<TextView>(R.id.tvDocType)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)

        val btnMarkFound = findViewById<Button>(R.id.btnMarkFound)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEdit = findViewById<ImageButton>(R.id.btnEdit) // Assure-toi d'avoir cet ID dans le XML

        // 3. Bind Data
        tvDocType.text = getDocName(declaration.documentTypeId ?: 0L)
        tvDate.text = declaration.incidentDate
        tvLocation.text = declaration.incidentLocation
        tvDescription.text = declaration.description

        updateStatusView(declaration.status ?: "EN_ATTENTE")

        // --- GESTION IMAGE ---
        if (!declaration.imageUrl.isNullOrEmpty()) {
            ivDetailImage.visibility = View.VISIBLE // On force l'affichage
            ivDetailImage.load(declaration.imageUrl) {
                crossfade(true)
                error(android.R.drawable.ic_menu_gallery)
            }
            ivDetailImage.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            // AMÉLIORATION ICI : On cache la vue si pas d'image
            ivDetailImage.visibility = View.GONE
        }

        // 4. Actions
        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val intent = Intent(this, DeclarationFormActivity::class.java)
            val jsonToSend = Json.encodeToString(declaration)
            intent.putExtra("EDIT_DECLARATION_JSON", jsonToSend)
            startActivity(intent)
            finish()
        }

        btnMarkFound.setOnClickListener {
            updateDeclarationStatus("RETROUVE")
        }

        btnDelete.setOnClickListener {
            Toast.makeText(this, "Confirmation suppression...", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(this)
                .setTitle("Suppression")
                .setMessage("Confirmez-vous la suppression ?")
                .setPositiveButton("OUI") { _, _ ->
                    deleteDeclaration()
                }
                .setNegativeButton("NON", null)
                .show()
        }
    } // <--- N'oublie pas cette accolade fermante pour finir la méthode !

    private fun updateStatusView(status: String) {
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        // On récupère la carte qu'on vient de nommer dans le XML
        val cvStatusBadge = findViewById<androidx.cardview.widget.CardView>(R.id.cvStatusBadge)

        tvStatus.text = status

        when (status) {
            "EN_ATTENTE" -> {
                // Orange Foncé sur Orange très clair
                tvStatus.setTextColor(Color.parseColor("#E65100"))
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            }
            "RETROUVE", "TROUVE", "VALIDE" -> {
                // Vert Foncé sur Vert très clair
                tvStatus.setTextColor(Color.parseColor("#1B5E20"))
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#E8F5E9"))

                // On cache le bouton "Marquer comme retrouvé" car c'est déjà fait
                findViewById<Button>(R.id.btnMarkFound).visibility = View.GONE
            }
            "REJETE" -> {
                // Rouge Foncé sur Rouge très clair
                tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
            }
            else -> {
                // Gris par défaut
                tvStatus.setTextColor(Color.DKGRAY)
                cvStatusBadge.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }
        }
    }

    private fun getDocName(id: Long): String {
        return when (id) {
            1L -> "Carte Nationale d'Identité"
            2L -> "Passeport Biométrique"
            3L -> "Permis de Conduire"
            4L -> "Extrait de Naissance"
            else -> "Autre Document"
        }
    }

    private fun updateDeclarationStatus(newStatus: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.from("declarations").update(
                    {
                        set("status", newStatus)
                    }
                ) {
                    filter {
                        eq("id", declarationId)
                    }
                }

                Toast.makeText(this@DeclarationDetailsActivity, "Statut mis à jour !", Toast.LENGTH_SHORT).show()
                updateStatusView(newStatus)
                // Optionally finish() if we want to return to list immediately
            } catch (e: Exception) {
                Toast.makeText(this@DeclarationDetailsActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun deleteDeclaration() {
        if (declarationId.isEmpty()) {
             Toast.makeText(this, "Erreur: ID manquant", Toast.LENGTH_SHORT).show()
             return
        }
        lifecycleScope.launch {
            try {
                // 1. Attempt Delete
                SupabaseClient.client.from("declarations").delete {
                    filter {
                        eq("id", declarationId)
                    }
                }

                // 2. Verify if it's actually gone
                val check = SupabaseClient.client.from("declarations").select {
                    filter { eq("id", declarationId) }
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }.decodeList<Declaration>()

                if (check.isNotEmpty()) {
                    // It's still there!
                    Toast.makeText(this@DeclarationDetailsActivity, "Erreur: Impossible de supprimer. Vérifiez les politiques RLS sur Supabase (DELETE policy).", Toast.LENGTH_LONG).show()
                } else {
                    // Success
                    Toast.makeText(this@DeclarationDetailsActivity, "Déclaration supprimée avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DeclarationDetailsActivity, "Echec suppression: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}
