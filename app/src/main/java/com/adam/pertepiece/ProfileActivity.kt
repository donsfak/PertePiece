package com.adam.pertepiece

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)

        // 1. Back Navigation
        btnBack.setOnClickListener {
            finish()
        }

        // 2. Fetch User Data
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            tvUserEmail.text = user.email
            
            lifecycleScope.launch {
                try {
                    val profile = SupabaseClient.client.from("profiles")
                        .select {
                            filter {
                                eq("id", user.id)
                            }
                        }.decodeSingleOrNull<Profile>()
                    
                    if (profile != null) {
                        tvUserName.text = profile.fullName
                    } else {
                        tvUserName.text = "Utilisateur"
                    }
                } catch (e: Exception) {
                    tvUserName.text = "Utilisateur"
                    e.printStackTrace()
                }
            }
        }

        // 3. Logout Logic
        btnLogout.setOnClickListener {
            lifecycleScope.launch {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                // Clear back stack so user can't go back
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
