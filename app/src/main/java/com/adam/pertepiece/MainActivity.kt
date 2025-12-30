package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.text.Editable // Nécessaire pour la recherche
import android.text.TextWatcher // Nécessaire pour la recherche
import android.view.View
import android.widget.EditText // Nécessaire pour le nouveau champ recherche
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeclarationAdapter

    // Store all declarations for filtering
    private var allDeclarations: List<Declaration> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Vérification de sécurité
        val session = SupabaseClient.client.auth.currentSessionOrNull()
        if (session == null) {
            goToLogin()
            return
        }

        // 2. Initialisation des vues
        recyclerView = findViewById(R.id.recyclerView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        val btnProfile = findViewById<View>(R.id.btnProfile)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        // 3. Configuration de base du RecyclerView
        adapter = DeclarationAdapter(emptyList()) { declaration ->
            val intent = Intent(this, DeclarationDetailsActivity::class.java)
            val json = Json.encodeToString(declaration)
            intent.putExtra("DECLARATION_JSON", json)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 4. Action : Nouvelle déclaration
        fabAdd.setOnClickListener {
            val intent = Intent(this, DeclarationFormActivity::class.java)
            startActivity(intent)
        }

        // 5. Action : Profile
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // 6. Search Logic (CORRIGÉ : Ajout des accolades manquantes)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    } // <--- C'était cette accolade qui manquait !

    private fun filterList(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.updateList(allDeclarations)
            recyclerView.visibility = if (allDeclarations.isNotEmpty()) View.VISIBLE else View.GONE
            return
        }

        val lowerQuery = query.lowercase().trim()

        val filteredList = allDeclarations.filter { declaration ->
            val docId = declaration.documentTypeId ?: 0L
            val docName = getDocName(docId).lowercase()
            val location = (declaration.incidentLocation ?: "").lowercase()

            docName.contains(lowerQuery) || location.contains(lowerQuery)
        }

        adapter.updateList(filteredList)

        // Gestion de l'affichage (Sans emptyStateView pour l'instant)
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
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

    override fun onResume() {
        super.onResume()
        if (SupabaseClient.client.auth.currentSessionOrNull() != null) {
            fetchDeclarations()
        }
    }

    private fun fetchDeclarations() {
        lifecycleScope.launch {
            try {
                // VISUAL FEEDBACK
                adapter.updateList(emptyList())

                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                val list = SupabaseClient.client.from("declarations")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }.decodeList<Declaration>()

                allDeclarations = list
                adapter.updateList(list)

                // Mise à jour simple de la visibilité
                if (list.isEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erreur chargement: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}