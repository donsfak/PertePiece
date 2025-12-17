package com.adam.pertepiece

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth // <-- C'est ici que ça change !
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://nvwigxaexrxwolezvmsl.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im52d2lneGFleHJ4d29sZXp2bXNsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDY0MzIsImV4cCI6MjA4MTMyMjQzMn0.0H2fW2CN9C-AxWSMNmZy87BKv7WvSuDk2yfM5M61WC4"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)    // <-- On utilise Auth ici, pas GoTrue
        install(Storage)
    }
}