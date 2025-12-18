package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity to handle password reset after user clicks the email link.
 * This activity is opened via deep link: pertepiece://login-callback
 */
class ResetPasswordActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ResetPassword"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val inputNewPassword = findViewById<EditText>(R.id.inputNewPassword)
        val inputConfirmPassword = findViewById<EditText>(R.id.inputConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)

        // Handle the deep link to extract session
        handleDeepLink(intent)

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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data
        Log.d(TAG, "handleDeepLink called, uri: $uri")
        Log.d(TAG, "Intent data: ${intent?.data}")
        Log.d(TAG, "Intent extras: ${intent?.extras}")
        
        lifecycleScope.launch {
            try {
                // Try to let Supabase SDK handle the deep link
                if (intent != null) {
                    try {
                        SupabaseClient.client.handleDeeplinks(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "handleDeeplinks error: ${e.message}")
                    }
                }
                
                // Wait a bit for the session to be established
                delay(500)
                
                // Check if we got a valid session
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d(TAG, "Session obtained successfully")
                    Toast.makeText(this@ResetPasswordActivity, 
                        "Entrez votre nouveau mot de passe", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "No session - but staying on screen for user to try later")
                    // Don't redirect - let user stay on this screen
                    // They might have clicked the link and session will sync
                    Toast.makeText(this@ResetPasswordActivity, 
                        "Entrez votre nouveau mot de passe", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleDeepLink", e)
                Toast.makeText(this@ResetPasswordActivity, 
                    "Entrez votre nouveau mot de passe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                // Check if user is authenticated
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session == null) {
                    Toast.makeText(this@ResetPasswordActivity, 
                        "Vous devez d'abord cliquer sur le lien dans votre email.\nSi vous l'avez déjà fait, veuillez réessayer.", 
                        Toast.LENGTH_LONG).show()
                    return@launch
                }

                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                
                Toast.makeText(this@ResetPasswordActivity, 
                    "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show()
                
                // Go to main activity
                startActivity(Intent(this@ResetPasswordActivity, MainActivity::class.java))
                finishAffinity() // Clear the back stack
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating password", e)
                Toast.makeText(this@ResetPasswordActivity, 
                    "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
