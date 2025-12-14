package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userName: String? = null
)

class AuthViewModel : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val userRepository = UserRepository()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        val currentUser = FirebaseManager.auth.currentUser
        _authState.value = AuthState(
            isAuthenticated = currentUser != null,
            userEmail = currentUser?.email,
            userName = currentUser?.displayName
        )
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val result = FirebaseManager.signInWithEmail(email, password)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _authState.value = AuthState(
                        isLoading = false,
                        isAuthenticated = true,
                        userEmail = user?.email,
                        userName = user?.displayName
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Sign in failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val result = FirebaseManager.signUpWithEmail(name, email, password)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    
                    // Create user document in Firestore
                    userRepository.createNewUser(name, email)
                    
                    _authState.value = AuthState(
                        isLoading = false,
                        isAuthenticated = true,
                        userEmail = user?.email,
                        userName = user?.displayName
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Sign up failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val result = FirebaseManager.resetPassword(email)
                
                if (result.isSuccess) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Password reset failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun signOut() {
        FirebaseManager.signOut()
        _authState.value = AuthState(isAuthenticated = false)
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email address"
            else -> null
        }
    }
    
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }
    
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}
