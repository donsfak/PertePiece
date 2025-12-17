package com.adam.pertepiece

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
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
             // Should not happen if fetched correctly
             finish()
             return
        }

        // 2. Init Views
        val tvDocType = findViewById<TextView>(R.id.tvDocType)
        tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        btnMarkFound = findViewById<Button>(R.id.btnMarkFound)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // 3. Bind Data
        tvDocType.text = getDocName(declaration.documentTypeId)
        tvDate.text = declaration.incidentDate
        tvLocation.text = declaration.incidentLocation
        tvDescription.text = declaration.description
        updateStatusView(declaration.status)

        // 4. Action: Back
        btnBack.setOnClickListener { finish() }

        // 5. Action: Edit
        val btnEdit = findViewById<ImageButton>(R.id.btnEdit)
        btnEdit.setOnClickListener {
            val intent = Intent(this, DeclarationFormActivity::class.java)
            val json = Json.encodeToString(declaration)
            intent.putExtra("EDIT_DECLARATION_JSON", json)
            startActivity(intent)
            finish() // Close details so we return to list or we can reload details on return? For now, finish.
        }

        // 6. Action: Mark Found
        btnMarkFound.setOnClickListener {
            updateDeclarationStatus("RETROUVE")
        }

        // 6. Action: Delete
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cette déclaration ?")
                .setPositiveButton("Oui") { _, _ ->
                    deleteDeclaration()
                }
                .setNegativeButton("Non", null)
                .show()
        }
    }

    private fun updateStatusView(status: String) {
        tvStatus.text = status
        when (status) {
            "EN_ATTENTE" -> tvStatus.setTextColor(Color.parseColor("#FF9800"))
            "RETROUVE", "TROUVE", "VALIDE" -> {
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                btnMarkFound.visibility = View.GONE // Hide button if already found
            }
            "REJETE" -> tvStatus.setTextColor(Color.parseColor("#F44336"))
            else -> tvStatus.setTextColor(Color.GRAY)
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
        lifecycleScope.launch {
            try {
                SupabaseClient.client.from("declarations").delete {
                    filter {
                        eq("id", declarationId)
                    }
                }
                Toast.makeText(this@DeclarationDetailsActivity, "Déclaration supprimée", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@DeclarationDetailsActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
