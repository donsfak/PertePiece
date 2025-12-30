package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Initialisation des vues (Avec les nouveaux IDs du design Pro)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)

        // (Optionnel) Si tu veux gérer l'avatar plus tard
        // val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)

        // 2. Action : Retour
        btnBack.setOnClickListener {
            finish()
        }

        // 3. Charger les infos de l'utilisateur connecté
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            tvUserEmail.text = user.email ?: "Email inconnu"

            // On essaie de récupérer le nom depuis les métadonnées, sinon on met "Utilisateur"
            // Note: Cela dépend de comment tu as géré l'inscription.
            // Si tu n'as pas de métadonnées, on peut afficher la partie gauche de l'email.
            val name = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")

            if (!name.isNullOrBlank()) {
                tvUserName.text = name
            } else {
                // Astuce : Utiliser le début de l'email comme nom par défaut
                tvUserName.text = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Utilisateur"
            }
        }

        // 4. Action : Déconnexion
        btnLogout.setOnClickListener {
            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signOut()
                    // Retour à l'écran de connexion
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    // On vide la pile d'activités pour empêcher le retour arrière
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}