// viewmodel/profile/ProfileViewModel.kt
package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Mueve esta data class (ProfileUiState) de ui.profile.ProfileScreen.kt a este archivo
data class ProfileUiState(
    val userProfile: User? = null,
    val myAnnouncements: List<Announcement> = emptyList(),
    val isLoading: Boolean = false,
    val statusMessage: String? = null
)

class ProfileViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    // Llama a esta función desde el Composable (ej. en LaunchedEffect) para iniciar la carga
    fun loadProfileAndAnnouncements(userId: String) {
        if (_uiState.value.userProfile == null || _uiState.value.myAnnouncements.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
            Log.d(TAG, "loadProfileAndAnnouncements: Iniciando carga para UID: $userId")
            loadAndDisplayUserProfile(userId)
            loadMyAnnouncements(userId)
        } else {
            Log.d(TAG, "loadProfileAndAnnouncements: Datos ya cargados para UID: $userId. No recargando.")
        }
    }

    /**
     * Carga el perfil del usuario actual desde Room y actualiza el estado.
     */
    private fun loadAndDisplayUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                _uiState.value = _uiState.value.copy(userProfile = user) // Actualiza el estado
                if (user == null) {
                    _uiState.value = _uiState.value.copy(statusMessage = "Perfil incompleto. Por favor, edita tu perfil.")
                    Log.w(TAG, "Documento de perfil no encontrado en Room para UID: $userId.")
                } else {
                    _uiState.value = _uiState.value.copy(statusMessage = null)
                    Log.d(TAG, "Perfil de usuario cargado desde Room: ${user.displayName}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(statusMessage = "Error al cargar perfil: ${e.message}")
                Log.e(TAG, "Error al cargar perfil desde Room: ${e.message}", e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false) // Desactiva la carga al finalizar
            }
        }
    }

    /**
     * Carga los anuncios del autor actual desde Room y actualiza el estado.
     */
    private fun loadMyAnnouncements(userId: String) {
        viewModelScope.launch {
            announcementRepository.getAnnouncementsByAuthor(userId).collectLatest { newAnnouncements ->
                _uiState.value = _uiState.value.copy(myAnnouncements = newAnnouncements) // Actualiza el estado
                if (newAnnouncements.isEmpty()) {
                    Log.d(TAG, "No se encontraron anuncios para el autor $userId en Room.")
                }
                Log.d(TAG, "Mis anuncios actualizados desde Room: ${newAnnouncements.size} anuncios.")
                _uiState.value = _uiState.value.copy(isLoading = false) // Desactiva la carga al finalizar
            }
        }
    }
}