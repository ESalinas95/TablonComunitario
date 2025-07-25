package com.example.tabloncomunitario.viewmodel // Asegúrate de que el paquete sea correcto

import android.util.Log
import androidx.lifecycle.ViewModel // Importar ViewModel
import androidx.lifecycle.ViewModelProvider // Importar ViewModelProvider
import androidx.lifecycle.viewModelScope // Importar viewModelScope
import com.example.tabloncomunitario.Announcement // Importar tu data class Announcement
import com.example.tabloncomunitario.User // Importar tu data class User
import com.example.tabloncomunitario.repository.AnnouncementRepository // Importar Repositorio de Anuncios
import com.example.tabloncomunitario.repository.UserRepository // Importar Repositorio de Usuarios
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow // Importar MutableStateFlow
import kotlinx.coroutines.flow.StateFlow // Importar StateFlow
import kotlinx.coroutines.flow.asStateFlow // Importar asStateFlow
import kotlinx.coroutines.flow.collectLatest // Importar collectLatest
import kotlinx.coroutines.launch // Importar launch

// Mueve esta data class (MainUiState) de MainScreen.kt a este archivo
data class MainUiState(
    val welcomeMessage: String = "Cargando...",
    val allAnnouncements: List<Announcement> = emptyList(), // Lista completa sin filtrar
    val filteredAnnouncements: List<Announcement> = emptyList(), // Lista visible después de aplicar filtro
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null // Para errores o mensajes de "no resultados"
)

class MainViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentUserProfile: User? = null // Cache del perfil del usuario logueado

    private var allAnnouncementsCache: List<Announcement> = emptyList() // Cache para la lista completa sin filtrar

    companion object {
        private const val TAG = "MainViewModel"
    }

    // --- Lógica migrada desde MainActivity.kt ---

    // Llama a esto cuando el ViewModel se inicialice (ej. en LaunchedEffect del Composable)
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

    /**
     * Carga el perfil del usuario actual desde el Repositorio (Room) y actualiza el mensaje de bienvenida.
     */
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

    /**
     * Carga todos los anuncios desde el Repositorio (Room) y actualiza el estado de Compose.
     * Utiliza Flow para obtener actualizaciones en tiempo real de la base de datos local.
     */
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

    /**
     * Filtra la lista de anuncios basándose en el query de búsqueda.
     * Esta función es llamada cuando el query cambia o cuando los anuncios se recargan.
     */
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

    // Lógica para cerrar sesión (llamada desde el Composable)
    fun logoutUser() {
        auth.signOut()
        // Aquí no se navega, solo se desloguea. La navegación la gestiona el NavHost en MainActivity.
        // Después de logout, la appHost debería volver a AUTH_ROUTE
        Log.d(TAG, "Usuario deslogueado.")
    }
}