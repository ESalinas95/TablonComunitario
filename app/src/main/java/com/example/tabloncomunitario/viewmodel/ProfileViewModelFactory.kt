// viewmodel/profile/ProfileViewModelFactory.kt
package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModelFactory(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(auth, userRepository, announcementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}