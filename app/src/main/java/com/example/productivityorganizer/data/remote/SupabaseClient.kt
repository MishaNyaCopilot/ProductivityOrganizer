package com.example.productivityorganizer.data.remote // Adjust package if needed

import android.content.Context
import com.example.productivityorganizer.MainApplication
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager { // Changed to object for singleton instance

    private val context: Context get() = MainApplication.instance

    private val supabaseUrl: String by lazy {
        context.resources.getString(
            context.resources.getIdentifier("supabase_url", "string", context.packageName)
        )
    }

    private val supabaseAnonKey: String by lazy {
        context.resources.getString(
            context.resources.getIdentifier("supabase_anon_key", "string", context.packageName)
        )
    }

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
