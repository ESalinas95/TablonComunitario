package com.example.tabloncomunitario.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.Comment
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.CommentRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DetailAnnouncementUiState(
    val announcement: Announcement? = null,
    val comments: List<Comment> = emptyList(),
    val commentInput: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val isAuthor: Boolean = false,
    val canEditDelete: Boolean = true,
    val announcementDeleted: Boolean = false
)

class DetailAnnouncementViewModel(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailAnnouncementUiState())
    val uiState: StateFlow<DetailAnnouncementUiState> = _uiState.asStateFlow()

    private var currentUserProfile: User? = null // Perfil del usuario que comenta
    private var currentAnnouncementId: String? = null // ID del anuncio actual

    companion object {
        private const val TAG = "DetailAnnouncementVM"
    }

    fun loadAnnouncementDetails(announcementId: String) {
        currentAnnouncementId = announcementId
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        viewModelScope.launch {
            try {
                val announcement = announcementRepository.getAnnouncementById(announcementId)
                _uiState.value = _uiState.value.copy(announcement = announcement)

                if (announcement == null) {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Anuncio no encontrado en BD local.",
                        isLoading = false
                    )
                    Log.w(TAG, "Anuncio nulo al cargar detalles para ID: $announcementId")
                    return@launch
                }

                checkAuthorActions(announcement) // Verificar acciones del autor
                loadCurrentUserProfile() // Cargar perfil de usuario para comentar
                loadComments(announcementId) // Cargar comentarios

                _uiState.value = _uiState.value.copy(isLoading = false, statusMessage = null)
                Log.d(TAG, "Detalles de anuncio cargados para ID: $announcementId")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar detalles del anuncio desde Room: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Error al cargar detalles: ${e.message}"
                )
            }
        }
    }

    // Funciones para actualizar el estado de entrada de comentarios
    fun onCommentInputChange(input: String) {
        _uiState.value = _uiState.value.copy(commentInput = input)
    }

    // Llama a esto si hay un error que se debe mostrar en la UI
    fun setStatusMessage(message: String?) {
        _uiState.value = _uiState.value.copy(statusMessage = message)
    }

    private fun checkAuthorActions(announcement: Announcement) {
        val currentUser = auth.currentUser
        val isAuthor = (currentUser != null && announcement.authorId == currentUser.uid)
        _uiState.value = _uiState.value.copy(isAuthor = isAuthor)

        if (isAuthor) {
            viewModelScope.launch {
                try {
                    val comments = commentRepository.getCommentsForAnnouncement(announcement.id)
                        .take(1) // Solo necesitamos el primer valor para el chequeo
                        .collect { currentComments ->
                            _uiState.value = _uiState.value.copy(canEditDelete = currentComments.isEmpty())
                            Log.d(TAG, "CheckAuthorActions: Anuncio tiene comentarios? ${!currentComments.isEmpty()}")
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al verificar comentarios para acciones del autor desde Room: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(statusMessage = "Error al verificar acciones del anuncio.", canEditDelete = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(canEditDelete = false)
        }
    }

    fun deleteAnnouncement() {
        val announcement = _uiState.value.announcement
        val announcementId = currentAnnouncementId
        if (announcement == null || announcementId == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: No hay anuncio para eliminar.")
            return
        }

        viewModelScope.launch {
            try {
                commentRepository.getCommentsForAnnouncement(announcementId)
                    .take(1)
                    .collect { currentComments ->
                        if (currentComments.isEmpty()) {
                            announcementRepository.deleteAnnouncement(announcement)
                            Log.d(TAG, "Anuncio eliminado de Room: ${announcement.id}")

                            commentRepository.deleteCommentsForAnnouncement(announcement.id)
                            Log.d(TAG, "Comentarios del anuncio eliminados de Room para ${announcement.id}")

                            announcement.imageUrl?.let { imageUrl ->
                                try {
                                    storage.getReferenceFromUrl(imageUrl).delete().await()
                                    Log.d(TAG, "Imagen eliminada de Storage.")
                                } catch (e: Exception) { // Capturar cualquier error de Storage
                                    Log.w(TAG, "Error al eliminar imagen de Storage: ${e.message}")
                                }
                            }

                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Anuncio eliminado correctamente.",
                                announcementDeleted = true // Activar bandera para navegación
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(statusMessage = "Este anuncio tiene comentarios y no puede ser eliminado.")
                            Log.w(TAG, "Intento de eliminar anuncio con comentarios detectado (Room).")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar anuncio o verificar comentarios en Room: ${e.message}", e)
                _uiState.value = _uiState.value.copy(statusMessage = "Error al eliminar anuncio: ${e.message}")
            }
        }
    }

    private fun loadCurrentUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                try {
                    currentUserProfile = userRepository.getUserById(userId)
                    Log.d(TAG, "Perfil de usuario cargado desde Room para comentar: ${currentUserProfile?.displayName}")
                    if (currentUserProfile == null) {
                        Log.w(TAG, "Perfil de usuario nulo en Room al preparar comentario para UID: $userId.")
                        _uiState.value = _uiState.value.copy(statusMessage = "Perfil no encontrado. No podrás comentar.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar perfil desde Room para comentar: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(statusMessage = "Error al cargar perfil para comentar.")
                }
            }
        } else {
            Log.w(TAG, "No hay usuario autenticado al intentar cargar el perfil para comentar.")
            _uiState.value = _uiState.value.copy(statusMessage = "No autenticado. No podrás comentar.")
        }
    }

    fun addComment() {
        val commentText = _uiState.value.commentInput.trim()
        val currentUser = auth.currentUser
        val announcement = _uiState.value.announcement

        if (commentText.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "El comentario no puede estar vacío.")
            return
        }

        if (currentUser == null || currentUserProfile == null || announcement == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: Datos de usuario o anuncio no disponibles para comentar.")
            return
        }

        val authorDisplayName = currentUserProfile?.displayName ?: currentUser.email ?: "Anónimo"
        val authorProfileImageUrl = currentUserProfile?.profileImageUrl

        val comment = Comment(
            announcementId = announcement.id,
            authorId = currentUser.uid,
            authorDisplayName = authorDisplayName,
            authorProfileImageUrl = authorProfileImageUrl,
            text = commentText,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                commentRepository.insertComment(comment)
                Log.d(TAG, "Comentario añadido a Room.")
                _uiState.value = _uiState.value.copy(commentInput = "", statusMessage = "Comentario enviado.")
            } catch (e: Exception) {
                Log.e(TAG, "Error al añadir comentario a Room: ${e.message}", e)
                _uiState.value = _uiState.value.copy(statusMessage = "Error al enviar comentario: ${e.message}")
            }
        }
    }

    private fun loadComments(announcementId: String) {
        Log.d(TAG, "loadComments: Iniciando carga de comentarios para anuncio ID: $announcementId (desde Room).")
        viewModelScope.launch {
            commentRepository.getCommentsForAnnouncement(announcementId).collectLatest { newComments ->
                Log.d(TAG, "loadComments: Flow de comentarios emitido. Comentarios recibidos: ${newComments.size}")
                if (newComments.isEmpty()) {
                    Log.d(TAG, "loadComments: No hay comentarios para el anuncio $announcementId en Room (lista vacía).")
                } else {
                    Log.d(TAG, "loadComments: Primer comentario recibido: ${newComments.firstOrNull()?.text} por ${newComments.firstOrNull()?.authorDisplayName}")
                }
                _uiState.value = _uiState.value.copy(comments = newComments)
                Log.d(TAG, "loadComments: UI de comentarios actualizada. Total en estado: ${newComments.size}")
            }
        }
    }

    // Método para resetear la bandera de eliminación de anuncio
    fun announcementDeletionCompleted() {
        _uiState.value = _uiState.value.copy(announcementDeleted = false)
    }
}