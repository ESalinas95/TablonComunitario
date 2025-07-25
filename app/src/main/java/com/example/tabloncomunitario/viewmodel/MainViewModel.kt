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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MainUiState(
    val welcomeMessage: String = "Cargando...",
    val allAnnouncements: List<Announcement> = emptyList(),
    val filteredAnnouncements: List<Announcement> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentUserProfile: User? = null

    private var allAnnouncementsCache: List<Announcement> = emptyList()

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun initialize() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadCurrentUserProfileAndWelcome(currentUser.uid)
            loadAnnouncements()
        } else {
            _uiState.value = _uiState.value.copy(
                welcomeMessage = "Tablón Comunitario",
                statusMessage = "Por favor, inicia sesión."
            )
            Log.w(TAG, "initialize: No hay usuario autenticado.")
        }
    }

    private fun loadCurrentUserProfileAndWelcome(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                currentUserProfile = user
                user?.let { userProfile ->
                    _uiState.value = _uiState.value.copy(
                        welcomeMessage = "Bienvenido, ${userProfile.displayName.ifEmpty { auth.currentUser?.email }}!"
                    )
                    Log.d(TAG, "Perfil de bienvenida cargado desde Room: ${userProfile.displayName}")
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        welcomeMessage = "Bienvenido, ${auth.currentUser?.email}!"
                    )
                    Log.w(TAG, "Documento de perfil no encontrado en Room para UID: $userId.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar perfil de bienvenida desde Room: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    welcomeMessage = "Bienvenido, ${auth.currentUser?.email}!"
                )
            }
        }
    }

    private fun loadAnnouncements() {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        Log.d(TAG, "loadAnnouncements: Iniciando carga de anuncios desde Room.")
        viewModelScope.launch {
            try {
                announcementRepository.getAllAnnouncements().collectLatest { loadedAnnouncements ->
                    allAnnouncementsCache = loadedAnnouncements // Actualiza la lista completa en caché
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allAnnouncements = loadedAnnouncements // También actualiza allAnnouncements en UiState
                    )
                    filterAnnouncements(_uiState.value.searchQuery) // Aplica el filtro actual

                    if (loadedAnnouncements.isEmpty()) {
                        Log.d(TAG, "loadAnnouncements: No se encontraron documentos en la tabla 'announcements' de Room.")
                        _uiState.value = _uiState.value.copy(statusMessage = "Aún no hay anuncios publicados.")
                    } else {
                        Log.d(TAG, "loadAnnouncements: Anuncios recibidos desde Room: ${loadedAnnouncements.size}")
                        _uiState.value = _uiState.value.copy(statusMessage = null)
                    }
                    Log.d(TAG, "loadAnnouncements: Total de anuncios cargados y mapeados desde Room: ${allAnnouncementsCache.size}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Error al cargar anuncios: ${e.message}"
                )
                Log.e(TAG, "Error al cargar anuncios desde Room: ${e.message}", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterAnnouncements(query)
    }

    private fun filterAnnouncements(query: String) {
        val filteredList = if (query.isEmpty()) {
            allAnnouncementsCache // Usar la caché completa si no hay filtro
        } else {
            allAnnouncementsCache.filter { announcement ->
                announcement.title.contains(query, ignoreCase = true) ||
                        announcement.description.contains(query, ignoreCase = true) ||
                        (announcement.authorEmail?.contains(query, ignoreCase = true) ?: false) ||
                        (announcement.authorDisplayName.contains(query, ignoreCase = true))
            }
        }
        _uiState.value = _uiState.value.copy(filteredAnnouncements = filteredList) // Actualiza la lista filtrada

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "No se encontraron resultados para '$query'.")
        } else if (filteredList.isNotEmpty() && query.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = null) // Limpiar mensaje si hay resultados para la búsqueda
        } else if (allAnnouncementsCache.isEmpty() && query.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "Aún no hay anuncios publicados.") // Si no hay nada y no se busca
        } else {
            _uiState.value = _uiState.value.copy(statusMessage = null) // Limpiar mensaje en otros casos
        }
    }

    fun logoutUser() {
        auth.signOut()
        Log.d(TAG, "Usuario deslogueado.")
    }
}