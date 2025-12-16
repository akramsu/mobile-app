package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.User
import com.freshly.app.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel : ViewModel() {
    
    private val repository = UserRepository()
    
    val user: StateFlow<User> = repository.user
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = User(name = "", xp = 0, level = 1, streak = 0)
        )
    
    suspend fun updateProfile(name: String, avatarUrl: String) {
        repository.updateUserProfile(name, avatarUrl)
    }
}
