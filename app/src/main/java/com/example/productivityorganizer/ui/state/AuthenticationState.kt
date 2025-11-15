package com.example.productivityorganizer.ui.state

import io.github.jan.supabase.auth.user.UserInfo

sealed class AuthenticationState {
    object Idle : AuthenticationState()
    object Loading : AuthenticationState()
    data class Success(val user: UserInfo?) : AuthenticationState() // <-- ИЗМЕНЕНО
    data class Error(val message: String) : AuthenticationState()
}