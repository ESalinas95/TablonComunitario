package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.CommentRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class DetailAnnouncementViewModelFactory(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailAnnouncementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailAnnouncementViewModel(auth, storage, userRepository, announcementRepository, commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}