// viewmodel/profile_preview/UserProfilePreviewViewModel.kt
package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.User // Importar tu data class User
import com.example.tabloncomunitario.repository.UserRepository // Importar UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Mueve esta data class (UserProfilePreviewUiState) de ui.profile_preview.UserProfilePreviewScreen.kt a este archivo
data class UserProfilePreviewUiState(
    val userProfile: User? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null
)

class UserProfilePreviewViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfilePreviewUiState())
    val uiState: StateFlow<UserProfilePreviewUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "UserProfilePreviewVM"
    }

    // Llama a esta función desde el Composable (ej. LaunchedEffect) para cargar el perfil
    fun loadUserProfile(userId: String) {
        // Solo cargar si no está ya cargado o en proceso de carga para este userId
        if (_uiState.value.userProfile?.uid != userId || (_uiState.value.userProfile == null && !_uiState.value.isLoading)) {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Cargando perfil...")
            Log.d(TAG, "loadUserProfile: Iniciando carga de perfil para ID: $userId (desde Room).")
            viewModelScope.launch {
                try {
                    val user = userRepository.getUserById(userId)
                    _uiState.value = _uiState.value.copy(
                        userProfile = user,
                        isLoading = false,
                        statusMessage = null // Limpiar mensaje de estado si hay éxito
                    )
                    if (user == null) {
                        Log.w(TAG, "loadUserProfile: Documento de perfil nulo en Room para ID: $userId.")
                        _uiState.value = _uiState.value.copy(statusMessage = "Perfil no encontrado en BD local.")
                    } else {
                        Log.d(TAG, "loadUserProfile: Perfil de usuario cargado desde Room: ${user.displayName}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar perfil desde Room para ID $userId: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Error al cargar perfil: ${e.message}"
                    )
                }
            }
        } else {
            Log.d(TAG, "loadUserProfile: Perfil ya cargado o en proceso para UID: $userId")
        }
    }

    // Puedes añadir este método si necesitas establecer mensajes de error desde afuera
    fun setStatusMessage(message: String?) {
        _uiState.value = _uiState.value.copy(statusMessage = message)
    }
}