package com.example.tabloncomunitario.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.tabloncomunitario.imagePickResultFlow
import com.example.tabloncomunitario.imagePickRequestChannel

data class SetupProfileUiState(
    val displayNameInput: String = "",
    val contactNumberInput: String = "",
    val documentNumberInput: String = "",
    val apartmentNumberInput: String = "",
    val aboutMeInput: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val navigateToMain: Boolean = false
)

class SetupProfileViewModel(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupProfileUiState())
    val uiState: StateFlow<SetupProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var currentUserEmail: String? = null
    private var loadedUserProfile: User? = null

    private val IMAGE_PICK_REQUEST_ID = "SetupProfileImagePick"

    companion object {
        private const val TAG = "SetupProfileViewModel"
    }

    init {
        Log.d(TAG, "ViewModel inicializado. Observando resultados de selección de imagen.")
        viewModelScope.launch {
            imagePickResultFlow.collectLatest { (requestId, uri) ->
                if (requestId == IMAGE_PICK_REQUEST_ID) {
                    onProfileImageResult(uri)
                    Log.d(TAG, "Resultado de imagen recibido para SetupProfileScreen: $uri")
                }
            }
        }
    }

    fun initialize(userId: String?, userEmail: String?) {
        currentUserId = userId
        currentUserEmail = userEmail
        if (userId == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: ID de usuario no proporcionado.")
            Log.e(TAG, "initialize: User ID es nulo.")
        } else {
            viewModelScope.launch {
                try {
                    loadedUserProfile = userRepository.getUserById(userId)
                    loadedUserProfile?.let { user ->
                        _uiState.value = _uiState.value.copy(
                            displayNameInput = user.displayName.ifEmpty { "" },
                            contactNumberInput = user.contactNumber.orEmpty(),
                            documentNumberInput = user.documentNumber.orEmpty(),
                            apartmentNumberInput = user.apartmentNumber.orEmpty(),
                            aboutMeInput = user.aboutMe.orEmpty(),
                            profileImageUri = user.profileImageUrl?.let { Uri.parse(it) }
                        )
                        Log.d(TAG, "initialize: Perfil existente cargado: ${user.displayName}. URI de imagen cargada: ${user.profileImageUrl}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "initialize: Error al cargar perfil existente para pre-llenar: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(statusMessage = "Error al cargar perfil: ${e.message}")
                }
            }
        }
    }

    fun onDisplayNameChange(name: String) { _uiState.value = _uiState.value.copy(displayNameInput = name, statusMessage = null) }
    fun onContactNumberChange(number: String) { _uiState.value = _uiState.value.copy(contactNumberInput = number, statusMessage = null) }
    fun onDocumentNumberChange(document: String) { _uiState.value = _uiState.value.copy(documentNumberInput = document, statusMessage = null) }
    fun onApartmentNumberChange(apt: String) { _uiState.value = _uiState.value.copy(apartmentNumberInput = apt, statusMessage = null) }
    fun onAboutMeChange(about: String) { _uiState.value = _uiState.value.copy(aboutMeInput = about, statusMessage = null) }

    fun onProfileImageResult(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri, statusMessage = null)
        Log.d(TAG, "onProfileImageResult: ViewModel recibió URI del picker: $uri. Actualizando UiState.")
    }

    fun requestImagePick() {
        viewModelScope.launch {
            imagePickRequestChannel.send(IMAGE_PICK_REQUEST_ID)
            Log.d(TAG, "requestImagePick: Petición de selección de imagen enviada a MainActivity con ID: $IMAGE_PICK_REQUEST_ID")
        }
    }


    fun saveProfileInformation() {
        val userId = currentUserId
        val currentProfile = loadedUserProfile
        if (userId == null || currentProfile == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: Usuario no autenticado o perfil no cargado.")
            return
        }

        if (_uiState.value.displayNameInput.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "El nombre a mostrar es obligatorio.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Guardando información...")
        Log.d(TAG, "Intentando guardar información de perfil para UID: $userId")

        val user = User(
            uid = userId,
            displayName = _uiState.value.displayNameInput,
            email = currentUserEmail ?: "",
            contactNumber = _uiState.value.contactNumberInput.takeIf { it.isNotEmpty() },
            documentNumber = _uiState.value.documentNumberInput.takeIf { it.isNotEmpty() },
            apartmentNumber = _uiState.value.apartmentNumberInput.takeIf { it.isNotEmpty() },
            aboutMe = _uiState.value.aboutMeInput.takeIf { it.isNotEmpty() },
            profileImageUrl = null
        )

        viewModelScope.launch {
            val selectedUriFromUiState = _uiState.value.profileImageUri
            val existingProfileImageUrl = loadedUserProfile?.profileImageUrl?.let { Uri.parse(it) }

            if (selectedUriFromUiState != null && selectedUriFromUiState != existingProfileImageUrl) {
                Log.d(TAG, "Imagen de perfil seleccionada/cambiada. Iniciando subida.")
                uploadProfileImageAndSave(userId, user, selectedUriFromUiState)
            } else {
                Log.d(TAG, "No hay nueva imagen de perfil. Guardando solo datos.")
                user.profileImageUrl = existingProfileImageUrl?.toString()
                saveUserToRoom(user)
            }
        }
    }

    private fun getExistingProfileImageUrlAsUri(): Uri? {
        return loadedUserProfile?.profileImageUrl?.let { Uri.parse(it) }
    }

    private suspend fun uploadProfileImageAndSave(userId: String, user: User, imageUri: Uri) {
        val profileImageRef = storage.reference.child("profile_images/$userId.jpg")

        try {
            _uiState.value = _uiState.value.copy(statusMessage = "Subiendo imagen: 0%")
            val uploadTask = profileImageRef.putFile(imageUri)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                _uiState.value = _uiState.value.copy(statusMessage = "Subiendo imagen: $progress%")
            }.await()

            val downloadUri = profileImageRef.downloadUrl.await()
            Log.d(TAG, "Imagen de perfil subida a Storage. Download URL: $downloadUri")

            user.profileImageUrl = downloadUri.toString()
            saveUserToRoom(user)

        } catch (e: Exception) {
            Log.e(TAG, "Error al subir la imagen de perfil: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Error al subir imagen: ${e.message}"
            )
        }
    }

    private suspend fun saveUserToRoom(user: User) {
        try {
            userRepository.insertUser(user)
            Log.d(TAG, "Perfil de usuario guardado/actualizado en Room para UID: ${user.uid}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Perfil configurado con éxito!",
                navigateToMain = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar perfil en Room: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Error al guardar el perfil: ${e.message}"
            )
        }
    }

    fun navigationCompleted() {
        _uiState.value = _uiState.value.copy(navigateToMain = false)
    }
}