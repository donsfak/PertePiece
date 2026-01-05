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
// import io.github.jan.supabase.postgrest.from // Plus besoin de ça ici
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val inputName = findViewById<EditText>(R.id.inputName)
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val cbPolicy = findViewById<CheckBox>(R.id.cbPolicy)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnSignUp.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val pwd = inputPassword.text.toString()

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
                    // ÉTAPE UNIQUE : Créer le compte et envoyer le nom en "metadata"
                    SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = pwd

                        // C'est ICI la magie : on envoie le nom dans les données
                        // Le trigger SQL va lire ce "full_name" pour créer le profil automatiquement
                        this.data = buildJsonObject {
                            put("full_name", name)
                        }
                    }

                    // Plus besoin de l'étape 2 et 3 !
                    // Le trigger SQL a déjà créé le profil grâce aux data ci-dessus.

                    Toast.makeText(this@SignUpActivity, "Compte créé avec succès !", Toast.LENGTH_LONG).show()
                    finish() // Retour au login

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