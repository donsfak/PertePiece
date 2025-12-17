package com.adam.pertepiece

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val inputName = findViewById<EditText>(R.id.inputName) // Nouveau
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val cbPolicy = findViewById<CheckBox>(R.id.cbPolicy)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnSignUp.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val pwd = inputPassword.text.toString()

            // Vérifications basiques
            if (!cbPolicy.isChecked) {
                Toast.makeText(this, "Veuillez accepter les conditions.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isBlank() || email.isBlank() || pwd.isBlank()) {
                Toast.makeText(this, "Tous les champs sont obligatoires.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // ÉTAPE 1 : Créer le compte Auth (Email/Mdp)
                    SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = pwd
                    }

                    // ÉTAPE 2 : Récupérer l'ID du nouvel utilisateur
                    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        // ÉTAPE 3 : Sauvegarder le NOM dans la table 'profiles'
                        val newProfile = Profile(id = userId, fullName = name)
                        SupabaseClient.client.from("profiles").insert(newProfile)

                        Toast.makeText(this@SignUpActivity, "Compte créé avec succès !", Toast.LENGTH_LONG).show()
                        finish() // Retour au login
                    } else {
                        throw Exception("Erreur lors de la récupération de l'ID utilisateur.")
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }
}