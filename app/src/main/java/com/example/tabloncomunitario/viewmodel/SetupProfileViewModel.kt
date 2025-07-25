package com.example.tabloncomunitario.viewmodel

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
import kotlinx.coroutines.launch


// Define el estado de la UI para la pantalla de configuración de perfil
data class SetupProfileUiState(
    val displayNameInput: String = "",
    val contactNumberInput: String = "",
    val documentNumberInput: String = "",
    val apartmentNumberInput: String = "",
    val aboutMeInput: String = "",
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

    companion object {
        private const val TAG = "SetupProfileViewModel"
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
                            aboutMeInput = user.aboutMe.orEmpty()
                        )
                        Log.d(TAG, "initialize: Perfil existente cargado: ${user.displayName}. URI de imagen cargada: ${user.profileImageUrl}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "initialize: Error al cargar perfil existente para pre-llenar: ${e.message}", e)
                }
            }
        }
    }

    // Funciones para actualizar el estado de los campos desde la UI
    fun onDisplayNameChange(name: String) { _uiState.value = _uiState.value.copy(displayNameInput = name, statusMessage = null) }
    fun onContactNumberChange(number: String) { _uiState.value = _uiState.value.copy(contactNumberInput = number, statusMessage = null) }
    fun onDocumentNumberChange(document: String) { _uiState.value = _uiState.value.copy(documentNumberInput = document, statusMessage = null) }
    fun onApartmentNumberChange(apt: String) { _uiState.value = _uiState.value.copy(apartmentNumberInput = apt, statusMessage = null) }
    fun onAboutMeChange(about: String) { _uiState.value = _uiState.value.copy(aboutMeInput = about, statusMessage = null) }
    fun requestImagePick() { /* ... */ }

    fun saveProfileInformation() {
        val userId = currentUserId
        if (userId == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: Usuario no autenticado.")
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
            profileImageUrl = null // Siempre null aquí, no se selecciona imagen
        )

        viewModelScope.launch {
            user.profileImageUrl = loadedUserProfile?.profileImageUrl // Mantiene la URL existente si había una cargada
            saveUserToRoom(user)
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