package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

/**
 * Activity for password reset with manual link confirmation.
 * 
 * Flow:
 * 1. User enters email, clicks "Envoyer le lien"
 * 2. User receives email, clicks link (opens in browser, creates session)
 * 3. User comes back to app, clicks "J'ai cliqué sur le lien"
 * 4. App checks for session, shows password fields if authenticated
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private var userEmail: String = ""

    // UI elements
    private lateinit var step1Layout: LinearLayout
    private lateinit var step2Layout: LinearLayout
    private lateinit var step3Layout: LinearLayout
    private lateinit var inputEmail: EditText
    private lateinit var btnSendLink: Button
    private lateinit var tvEmailSent: TextView
    private lateinit var btnConfirmClicked: Button
    private lateinit var btnResendLink: TextView
    private lateinit var inputNewPassword: EditText
    private lateinit var inputConfirmPassword: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Step 1: Enter email
        step1Layout = findViewById(R.id.step1Layout)
        inputEmail = findViewById(R.id.inputEmail)
        btnSendLink = findViewById(R.id.btnSendCode)

        // Step 2: Confirm link clicked
        step2Layout = findViewById(R.id.step2Layout)
        tvEmailSent = findViewById(R.id.tvEmailSent)
        btnConfirmClicked = findViewById(R.id.btnConfirmClicked)
        btnResendLink = findViewById(R.id.btnResendCode)

        // Step 3: Enter new password (reusing step2Layout views)
        step3Layout = findViewById(R.id.step3Layout)
        inputNewPassword = findViewById(R.id.inputNewPassword)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        // Back link
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
    }

    private fun setupListeners() {
        // Step 1: Send reset link
        btnSendLink.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Veuillez entrer un email valide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userEmail = email
            sendResetLink(email)
        }

        // Step 2: User confirms they clicked the link
        btnConfirmClicked.setOnClickListener {
            checkSessionAndShowPasswordForm()
        }

        // Resend link
        btnResendLink.setOnClickListener {
            if (userEmail.isNotBlank()) {
                sendResetLink(userEmail)
            }
        }

        // Step 3: Reset password
        btnResetPassword.setOnClickListener {
            val newPassword = inputNewPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()

            when {
                newPassword.isBlank() -> {
                    Toast.makeText(this, "Veuillez entrer un nouveau mot de passe", Toast.LENGTH_SHORT).show()
                }
                newPassword.length < 6 -> {
                    Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
                }
                newPassword != confirmPassword -> {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    updatePassword(newPassword)
                }
            }
        }

        // Back to login
        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendResetLink(email: String) {
        lifecycleScope.launch {
            try {
                btnSendLink.isEnabled = false
                btnSendLink.text = "Envoi..."

                SupabaseClient.client.auth.resetPasswordForEmail(email)

                Toast.makeText(this@ForgotPasswordActivity, "Lien envoyé !", Toast.LENGTH_SHORT).show()

                // Show step 2
                step1Layout.visibility = View.GONE
                step2Layout.visibility = View.VISIBLE
                tvEmailSent.text = "Un lien a été envoyé à\n$email\n\nCliquez sur le lien dans l'email, puis revenez ici."

            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSendLink.isEnabled = true
                btnSendLink.text = "Envoyer le lien"
            }
        }
    }

    private fun checkSessionAndShowPasswordForm() {
        lifecycleScope.launch {
            try {
                btnConfirmClicked.isEnabled = false
                btnConfirmClicked.text = "Vérification..."

                // Refresh the session
                try {
                    SupabaseClient.client.auth.refreshCurrentSession()
                } catch (e: Exception) {
                    // Ignore refresh errors
                }

                val session = SupabaseClient.client.auth.currentSessionOrNull()

                if (session != null) {
                    // User is authenticated, show password form
                    step2Layout.visibility = View.GONE
                    step3Layout.visibility = View.VISIBLE
                    Toast.makeText(this@ForgotPasswordActivity, "Parfait ! Définissez votre nouveau mot de passe.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, 
                        "Session non détectée.\n\nAssurez-vous d'avoir cliqué sur le lien dans l'email, puis réessayez.", 
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnConfirmClicked.isEnabled = true
                btnConfirmClicked.text = "J'ai cliqué sur le lien"
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                btnResetPassword.isEnabled = false
                btnResetPassword.text = "Modification..."

                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }

                Toast.makeText(this@ForgotPasswordActivity, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()

                // Go to main activity
                startActivity(Intent(this@ForgotPasswordActivity, MainActivity::class.java))
                finishAffinity()

            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                btnResetPassword.isEnabled = true
                btnResetPassword.text = "Réinitialiser le mot de passe"
            }
        }
    }
}
