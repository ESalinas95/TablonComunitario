package com.example.tabloncomunitario.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch // Mantener si usas viewModelScope.launch

data class EditProfileUiState(
    val userProfile: User? = null,
    val displayNameInput: String = "",
    val contactNumberInput: String = "",
    val documentNumberInput: String = "",
    val apartmentNumberInput: String = "",
    val aboutMeInput: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val navigateToProfile: Boolean = false
)

class EditProfileViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var loadedUserProfile: User? = null

    companion object {
        private const val TAG = "EditProfileViewModel"
    }

    fun loadUserProfile(userId: String) {
        currentUserId = userId
        if (loadedUserProfile?.uid != userId || (loadedUserProfile == null && !_uiState.value.isLoading)) {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Cargando perfil...")
            viewModelScope.launch {
                try {
                    val user = userRepository.getUserById(userId)
                    loadedUserProfile = user

                    _uiState.value = _uiState.value.copy(
                        userProfile = user,
                        displayNameInput = user?.displayName.orEmpty(),
                        contactNumberInput = user?.contactNumber.orEmpty(),
                        documentNumberInput = user?.documentNumber.orEmpty(),
                        apartmentNumberInput = user?.apartmentNumber.orEmpty(),
                        aboutMeInput = user?.aboutMe.orEmpty()
                    )

                    user?.let {
                        Log.d(TAG, "Perfil de usuario cargado desde Room para edición: ${it.displayName}. URI de imagen cargada: ${it.profileImageUrl}")
                    } ?: run {
                        Log.w(TAG, "Perfil de usuario no encontrado en Room para edición: $userId.")
                        _uiState.value = _uiState.value.copy(statusMessage = "Perfil no encontrado en la base de datos local.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar perfil desde Room para edición: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(statusMessage = "Error al cargar perfil: ${e.message}")
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        } else {
            Log.d(TAG, "loadUserProfile: Perfil ya cargado o en proceso para UID: $userId")
        }
    }

    fun onContactNumberChange(number: String) { _uiState.value = _uiState.value.copy(contactNumberInput = number, statusMessage = null) }
    fun onApartmentNumberChange(apt: String) { _uiState.value = _uiState.value.copy(apartmentNumberInput = apt, statusMessage = null) }
    fun onAboutMeChange(about: String) { _uiState.value = _uiState.value.copy(aboutMeInput = about, statusMessage = null) }

    fun saveUserProfile() {
        val userId = currentUserId
        val currentProfile = loadedUserProfile
        if (userId == null || currentProfile == null) {
            _uiState.value = _uiState.value.copy(statusMessage = "Error: Usuario no autenticado o perfil no cargado.")
            return
        }

        val displayName = _uiState.value.displayNameInput.trim()
        val contactNumber = _uiState.value.contactNumberInput.trim()
        val documentNumber = _uiState.value.documentNumberInput.trim()
        val apartmentNumber = _uiState.value.apartmentNumberInput.trim()
        val aboutMe = _uiState.value.aboutMeInput.trim()

        if (displayName.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "El nombre a mostrar es obligatorio.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Guardando perfil...")
        Log.d(TAG, "Intentando guardar perfil de usuario para UID: $userId")

        val userToUpdate = currentProfile.copy(
            displayName = displayName,
            contactNumber = if (contactNumber.isEmpty()) null else contactNumber,
            documentNumber = documentNumber,
            apartmentNumber = if (apartmentNumber.isEmpty()) null else apartmentNumber,
            aboutMe = if (aboutMe.isEmpty()) null else aboutMe,
        )

        viewModelScope.launch {
            userToUpdate.profileImageUrl = loadedUserProfile?.profileImageUrl
            saveUserToRoom(userToUpdate)
        }
    }

    private suspend fun saveUserToRoom(user: User) {
        try {
            userRepository.updateUser(user)
            Log.d(TAG, "Perfil de usuario actualizado en Room para UID: ${user.uid}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Perfil actualizado con éxito!",
                navigateToProfile = true
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
        _uiState.value = _uiState.value.copy(navigateToProfile = false)
    }
}