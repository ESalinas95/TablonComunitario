package com.example.tabloncomunitario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class AddAnnouncementViewModelFactory(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddAnnouncementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddAnnouncementViewModel(auth, userRepository, announcementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}