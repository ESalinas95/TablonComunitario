package com.example.tabloncomunitario.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AddAnnouncementUiState(
    val headerText: String = "Crear Nuevo Anuncio",
    val titleInput: String = "",
    val descriptionInput: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val navigateToDetailsOrMain: Boolean = false
)

class AddAnnouncementViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAnnouncementUiState())
    val uiState: StateFlow<AddAnnouncementUiState> = _uiState.asStateFlow()

    private var currentUserProfile: User? = null
    private var announcementToEdit: Announcement? = null

    companion object {
        private const val TAG = "AddAnnouncementVM"
    }

    fun initialize(announcementId: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        viewModelScope.launch {
            loadCurrentUserProfile()
            if (announcementId != null) {
                // Modo edición
                val announcement = announcementRepository.getAnnouncementById(announcementId)
                announcement?.let {
                    announcementToEdit = it
                    _uiState.value = _uiState.value.copy(
                        headerText = "Editar Anuncio",
                        titleInput = it.title,
                        descriptionInput = it.description,
                        isLoading = false
                    )
                    Log.d(TAG, "Modo Edición: Anuncio cargado con ID: ${it.id}")
                } ?: run {
                    Log.w(TAG, "Anuncio no encontrado para edición: $announcementId")
                    _uiState.value = _uiState.value.copy(
                        headerText = "Crear Nuevo Anuncio",
                        isLoading = false,
                        statusMessage = "Anuncio no encontrado para edición."
                    )
                }
            } else {
                // Modo añadir
                _uiState.value = _uiState.value.copy(
                    headerText = "Crear Nuevo Anuncio",
                    isLoading = false
                )
                Log.d(TAG, "Modo Añadir: Creando nuevo anuncio.")
            }
        }
    }

    private suspend fun loadCurrentUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                currentUserProfile = userRepository.getUserById(userId)
                if (currentUserProfile == null) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Perfil no encontrado en BD local. No podrás publicar."
                    )
                    Log.w(TAG, "Perfil de usuario nulo en Room para UID: $userId.")
                } else {
                    _uiState.value = _uiState.value.copy(statusMessage = null)
                    Log.d(TAG, "Perfil de usuario cargado desde Room: ${currentUserProfile?.displayName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar perfil desde Room: ${e.message}", e)
                _uiState.value = _uiState.value.copy(statusMessage = "Error al cargar perfil. No podrás publicar.")
            }
        } else {
            Log.w(TAG, "No hay usuario autenticado al intentar cargar el perfil.")
            _uiState.value = _uiState.value.copy(statusMessage = "No autenticado. No podrás publicar.")
        }
    }

    fun onTitleChange(title: String) { _uiState.value = _uiState.value.copy(titleInput = title, statusMessage = null) }
    fun onDescriptionChange(description: String) { _uiState.value = _uiState.value.copy(descriptionInput = description, statusMessage = null) }

    fun saveAnnouncementInformation() {
        val title = _uiState.value.titleInput.trim()
        val description = _uiState.value.descriptionInput.trim()
        val currentUser = auth.currentUser

        if (title.isEmpty() || description.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "Por favor, completa título y descripción.")
            return
        }

        if (currentUser == null || currentUserProfile == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: Datos de usuario no disponibles. Asegúrate de tener perfil.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = if (announcementToEdit != null) "Actualizando anuncio..." else "Publicando anuncio...")
        Log.d(TAG, "Intentando guardar/publicar anuncio.")

        val authorDisplayName = currentUserProfile?.displayName ?: currentUser.email ?: "Anónimo"
        val authorProfileImageUrl = currentUserProfile?.profileImageUrl
        val currentTimestamp = System.currentTimeMillis()

        val announcement = Announcement(
            id = announcementToEdit?.id ?: "",
            title = title,
            description = description,
            authorId = currentUser.uid,
            authorEmail = currentUser.email ?: "Anónimo",
            authorDisplayName = authorDisplayName,
            authorProfileImageUrl = authorProfileImageUrl,
            imageUrl = null,
            timestamp = announcementToEdit?.timestamp ?: currentTimestamp
        )

        viewModelScope.launch {
            announcement.imageUrl = announcementToEdit?.imageUrl
            saveToRoom(announcement)
        }
    }

    private suspend fun saveToRoom(announcement: Announcement) {
        try {
            if (announcementToEdit != null) {
                announcementRepository.updateAnnouncement(announcement)
                Log.d(TAG, "Anuncio actualizado en Room: ${announcement.id}")
            } else {
                announcement.id = UUID.randomUUID().toString()
                announcementRepository.insertAnnouncement(announcement)
                Log.d(TAG, "Nuevo anuncio insertado en Room con ID: ${announcement.id}")
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = if (announcementToEdit != null) "Anuncio actualizado con éxito!" else "Anuncio publicado con éxito!",
                navigateToDetailsOrMain = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar/actualizar anuncio en Room: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Error al guardar/publicar anuncio: ${e.message}"
            )
        }
    }

    fun navigationCompleted() {
        _uiState.value = _uiState.value.copy(navigateToDetailsOrMain = false)
    }
}