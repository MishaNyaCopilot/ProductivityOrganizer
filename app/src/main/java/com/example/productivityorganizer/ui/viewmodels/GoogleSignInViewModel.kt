package com.example.productivityorganizer.ui.viewmodels

// ВАЖНО: Используйте этот импорт для UserInfo
import android.app.Activity // <--- ДОБАВЛЕН ИМПОРТ Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivityorganizer.MainActivity
import com.example.productivityorganizer.data.remote.SupabaseManager
import com.example.productivityorganizer.ui.state.AuthenticationState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class GoogleSignInViewModel(application: Application) : AndroidViewModel(application) {

    private val supabase = SupabaseManager.supabaseClient // Или SupabaseClientInstance.client

    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Idle)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState.asStateFlow()

    private val applicationContext: Context = application.applicationContext

    init {
        checkCurrentUserSession()
    }

    fun checkCurrentUserSession() {
        viewModelScope.launch {
            _authenticationState.value = AuthenticationState.Loading
            try {
                // Прямое приведение к UserInfo? (Важно, если currentUserOrNull() возвращает базовый User)
                val user = supabase.auth.currentUserOrNull() as? UserInfo // <-- ИЗМЕНЕНО: приводим к UserInfo
                if (user != null) {
                    Log.d("GoogleSignInViewModel", "User session found: ${user.id}")
                    _authenticationState.value = AuthenticationState.Success(user) // <-- Передаем UserInfo
                } else {
                    Log.d("GoogleSignInViewModel", "No active user session found.")
                    _authenticationState.value = AuthenticationState.Idle
                }
            } catch (e: Exception) {
                Log.e("GoogleSignInViewModel", "Error checking user session", e)
                _authenticationState.value = AuthenticationState.Error("Failed to check current session: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun signInWithGoogleCredentialManager(activity: Activity) {
        _authenticationState.value = AuthenticationState.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)

                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("YOUR_GOOGLE_SERVER_CLIENT_ID")
                    .setNonce(hashedNonce)
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity,
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }

                // ВАЖНО: Приводим к UserInfo?
                val user = supabase.auth.currentUserOrNull() as? UserInfo // <-- ИЗМЕНЕНО
                if (user != null) {
                    Log.d("GoogleSignInViewModel", "Credential Manager Sign-In Success: User ${user.id}")
                    _authenticationState.value = AuthenticationState.Success(user) // <-- Передаем UserInfo
                } else {
                    Log.e("GoogleSignInViewModel", "Credential Manager Sign-In Success, but user is null after sign-in.")
                    _authenticationState.value = AuthenticationState.Error("Sign-in succeeded but user data not retrieved.")
                }

            } catch (e: GetCredentialException) {
                Log.e("GoogleSignInViewModel", "Credential Manager GetCredentialException: ${e.message}", e)
                _authenticationState.value = AuthenticationState.Error("Google Sign-In failed: ${e.message ?: "Unknown error"}")
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleSignInViewModel", "GoogleIdTokenParsingException: ${e.message}", e)
                _authenticationState.value = AuthenticationState.Error("Failed to parse Google ID Token: ${e.message ?: "Unknown error"}")
            } catch (e: RestException) {
                Log.e("GoogleSignInViewModel", "Supabase RestException during sign-in: ${e.message}", e)
                _authenticationState.value = AuthenticationState.Error("Supabase Sign-In error: ${e.message ?: "Unknown API error"}")
            } catch (e: Exception) {
                Log.e("GoogleSignInViewModel", "Unhandled exception during Google Sign-In: ${e.message}", e)
                _authenticationState.value = AuthenticationState.Error("An unexpected error occurred: ${e.message ?: "Unknown error"}")
            }
        }
    }


    fun processGoogleSignInSuccess() {
        _authenticationState.value = AuthenticationState.Loading
        viewModelScope.launch {
            try {
                // ВАЖНО: Приводим к UserInfo?
                val user = supabase.auth.currentUserOrNull() as? UserInfo // <-- ИЗМЕНЕНО
                if (user != null) {
                    Log.d("GoogleSignInViewModel", "processGoogleSignInSuccess: User retrieved ${user.id}")
                    _authenticationState.value = AuthenticationState.Success(user) // <-- Передаем UserInfo
                } else {
                    Log.e("GoogleSignInViewModel", "processGoogleSignInSuccess: Sign-in successful but failed to retrieve user, or user object is null.")
                    _authenticationState.value = AuthenticationState.Error("Sign-in successful but failed to retrieve user.")
                }
            } catch (e: Exception) {
                Log.e("GoogleSignInViewModel", "processGoogleSignInSuccess: Error fetching user after sign-in", e)
                _authenticationState.value = AuthenticationState.Error("Error fetching user after sign-in: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                Log.d("GoogleSignInViewModel", "Supabase sign out completed.")
                _authenticationState.value = AuthenticationState.Idle

                // ОПЦИОНАЛЬНО: Принудительное пересоздание активности для отладки
                // Это может быть полезно, чтобы увидеть, как приложение ведет себя
                // при полном "холодном старте" после выхода.
                (getApplication() as Application).let { app ->
                    val intent = Intent(app, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    app.startActivity(intent)
                }

            } catch (e: Exception) {
                Log.e("GoogleSignInViewModel", "Error during sign out", e)
                _authenticationState.value = AuthenticationState.Error("Sign out failed: ${e.message}")
            }
        }
    }

    fun processGoogleSignInCancelled() {
        Log.d("GoogleSignInViewModel", "processGoogleSignInCancelled: User cancelled Google Sign-In.")
        _authenticationState.value = AuthenticationState.Idle
    }
}