// viewmodel/profile_edit/EditProfileViewModelFactory.kt
package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class EditProfileViewModelFactory(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(auth, storage, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}