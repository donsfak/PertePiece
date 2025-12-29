package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast // Added import
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
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
        recyclerView = findViewById(R.id.recyclerViewDeclarations)
        emptyStateView = findViewById(R.id.emptyStateView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        val avatarIcon = findViewById<android.widget.ImageView>(R.id.avatarIcon)
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

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
        
        // 5. Action : Profile/Logout via Avatar
        avatarIcon.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        
        // 6. Search Logic
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        // Si la recherche est vide, on réaffiche tout
        if (query.isNullOrBlank()) {
            adapter.updateList(allDeclarations)
            recyclerView.visibility = if (allDeclarations.isNotEmpty()) View.VISIBLE else View.GONE
            return
        }

        val lowerQuery = query.lowercase().trim()

        val filteredList = allDeclarations.filter { declaration ->
            // On récupère les valeurs en mode sécurisé (si null, on met "")
            // Note: Assure-toi que getDocName accepte un Long? ou gère le cas ici
            val docId = declaration.documentTypeId ?: 0L
            val docName = getDocName(docId).lowercase()

            val location = (declaration.incidentLocation ?: "").lowercase()

            // On vérifie si ça matche
            docName.contains(lowerQuery) || location.contains(lowerQuery)
        }

        adapter.updateList(filteredList)

        // Gestion de l'affichage vide/plein
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            // Tu peux afficher une vue "Aucun résultat" ici si tu en as une
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
                // VISUAL FEEDBACK: Clear list so we know it's trying to load
                adapter.updateList(emptyList()) 
                
                // On récupère uniquement les déclarations de l'utilisateur connecté
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
                showEmptyState(list.isEmpty())
                
                // DEBUG TOAST
                // Toast.makeText(this@MainActivity, "Refresh: ${list.size} items", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erreur chargement: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    // Fonction utilitaire pour basculer entre la liste et l'état vide
    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}