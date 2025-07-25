// viewmodel/profile_preview/UserProfilePreviewViewModelFactory.kt
package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.UserRepository

class UserProfilePreviewViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfilePreviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfilePreviewViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}