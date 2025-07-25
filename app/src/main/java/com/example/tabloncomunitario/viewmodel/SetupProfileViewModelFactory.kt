package com.example.tabloncomunitario.viewmodel // O tu paquete de ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class SetupProfileViewModelFactory(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetupProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SetupProfileViewModel(auth, storage, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}