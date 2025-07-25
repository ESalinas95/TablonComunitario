package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // Importar collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Importar await para operaciones Firebase
import java.util.UUID // Para generar IDs de anuncios si es necesario

// Data class para el estado de la UI
data class AddAnnouncementUiState(
    val headerText: String = "Crear Nuevo Anuncio",
    val titleInput: String = "",
    val descriptionInput: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val navigateToDetailsOrMain: Boolean = false // Bandera para navegación de vuelta
)

class AddAnnouncementViewModel(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAnnouncementUiState())
    val uiState: StateFlow<AddAnnouncementUiState> = _uiState.asStateFlow()

    private var currentUserProfile: User? = null // Cache del perfil del usuario logueado
    private var announcementToEdit: Announcement? = null // Anuncio si estamos en modo edición

    private val IMAGE_PICK_REQUEST_ID = "AddAnnouncementImageRequest" // ID de la petición de imagen para este ViewModel

    companion object {
        private const val TAG = "AddAnnouncementVM"
    }

    // Llama a esto desde el Composable (ej. LaunchedEffect) para inicializar
    fun initialize(announcementId: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        viewModelScope.launch {
            loadCurrentUserProfile() // Cargar perfil al inicio
            if (announcementId != null) {
                // Modo edición
                val announcement = announcementRepository.getAnnouncementById(announcementId)
                announcement?.let {
                    announcementToEdit = it
                    _uiState.value = _uiState.value.copy(
                        headerText = "Editar Anuncio",
                        titleInput = it.title,
                        descriptionInput = it.description,
                        selectedImageUri = if (!it.imageUrl.isNullOrEmpty()) Uri.parse(it.imageUrl) else null,
                        isLoading = false
                    )
                    Log.d(TAG, "Modo Edición: Anuncio cargado con ID: ${it.id}")
                } ?: run {
                    Log.w(TAG, "Anuncio no encontrado para edición: $announcementId")
                    _uiState.value = _uiState.value.copy(
                        headerText = "Crear Nuevo Anuncio", // Volver a modo crear
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

    // Funciones para actualizar el estado de los campos de entrada
    fun onTitleChange(title: String) { _uiState.value = _uiState.value.copy(titleInput = title, statusMessage = null) }
    fun onDescriptionChange(description: String) { _uiState.value = _uiState.value.copy(descriptionInput = description, statusMessage = null) }

    // --- ESTA ES LA FUNCIÓN onSelectImageClick que actualiza el UiState con la URI ---
    // También es llamada desde init{} cuando llega el resultado de la Activity
    fun onSelectImageClick(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, statusMessage = null)
        Log.d(TAG, "onSelectImageClick: ViewModel recibió URI del picker: $uri. Actualizando UiState.")
    }
    // --- FIN onSelectImageClick ---

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
            if (_uiState.value.selectedImageUri != null) {
                Log.d(TAG, "Imagen de perfil seleccionada. Iniciando subida.")
                uploadImageAndSaveAnnouncement(announcement, _uiState.value.selectedImageUri!!)
            } else {
                Log.d(TAG, "No hay imagen seleccionada. Guardando solo datos.")
                announcement.imageUrl = announcementToEdit?.imageUrl
                saveAnnouncementToRoom(announcement)
            }
        }
    }

    private suspend fun uploadImageAndSaveAnnouncement(announcement: Announcement, imageUri: Uri) {
        val imageFileName = "${System.currentTimeMillis()}_${announcement.authorId}.jpg"
        val imageRef = storage.reference.child("announcement_images/$imageFileName")

        try {
            _uiState.value = _uiState.value.copy(statusMessage = "Subiendo imagen: 0%")
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                _uiState.value = _uiState.value.copy(statusMessage = "Subiendo imagen: $progress%")
            }.await()

            val downloadUri = imageRef.downloadUrl.await()
            Log.d(TAG, "URL de descarga obtenida: $downloadUri")
            announcement.imageUrl = downloadUri.toString()

            announcementToEdit?.imageUrl?.let { oldImageUrl ->
                if (oldImageUrl != downloadUri.toString()) {
                    try {
                        storage.getReferenceFromUrl(oldImageUrl).delete().await()
                        Log.d(TAG, "Imagen antigua borrada de Storage.")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al borrar imagen antigua de Storage: ${e.message}")
                    }
                }
            }
            saveAnnouncementToRoom(announcement)

        } catch (e: Exception) {
            Log.e(TAG, "Error al subir la imagen: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Error al subir imagen: ${e.message}"
            )
        }
    }

    private suspend fun saveAnnouncementToRoom(announcement: Announcement) {
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