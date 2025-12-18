package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUpLink = findViewById<TextView>(R.id.tvSignUpLink)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val cbRemember = findViewById<android.widget.CheckBox>(R.id.cbRemember)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedEmail = prefs.getString("saved_email", null)
        if (savedEmail != null) {
            inputEmail.setText(savedEmail)
            cbRemember.isChecked = true
        }

        // 1. Connexion
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val pwd = inputPassword.text.toString()

            if (email.isNotBlank() && pwd.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        SupabaseClient.client.auth.signInWith(Email) {
                            this.email = email
                            this.password = pwd
                        }

                        if (cbRemember.isChecked) {
                            prefs.edit().putString("saved_email", email).apply()
                        } else {
                            prefs.edit().remove("saved_email").apply()
                        }

                        Toast.makeText(this@LoginActivity, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Inscription
        tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 3. Mot de passe oublié
        tvForgotPassword.setOnClickListener {
            showResetPasswordDialog()
        }
    }

    private fun showResetPasswordDialog() {
        val inputResetEmail = EditText(this)
        inputResetEmail.hint = "Entrez votre email"
        val padding = 50
        inputResetEmail.setPadding(padding, padding, padding, padding)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mot de passe oublié")
            .setMessage("Nous allons vous envoyer un lien pour réinitialiser votre mot de passe.")
            .setView(inputResetEmail)
            .setPositiveButton("Envoyer") { _, _ ->
                val email = inputResetEmail.text.toString()
                if (email.isNotBlank()) {
                    sendResetEmail(email)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun sendResetEmail(email: String) {
        lifecycleScope.launch {
            try {
                // CORRECTION : On passe redirectUrl directement en paramètre, pas entre accolades
                SupabaseClient.client.auth.resetPasswordForEmail(
                    email = email,
                    redirectUrl = "pertepiece://login-callback"
                )

                Toast.makeText(this@LoginActivity, "Email envoyé ! Vérifiez vos spams.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

