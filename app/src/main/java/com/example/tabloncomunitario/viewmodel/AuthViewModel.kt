package com.example.tabloncomunitario.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val isAuthenticated: Boolean = false, // Mantener para saber si está logueado en general
)

// Define los eventos de navegación que el ViewModel puede emitir
sealed class AuthNavigationEvent {
    data class NavigateToSetupProfile(val userId: String) : AuthNavigationEvent()
    object NavigateToMain : AuthNavigationEvent()
    object NavigateToAuth : AuthNavigationEvent() // Para el logout
}

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvents: SharedFlow<AuthNavigationEvent> = _navigationEvents.asSharedFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email, statusMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(passwordInput = password, statusMessage = null)
    }

    fun registerUser() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput.trim()

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "Por favor, ingresa correo y contraseña.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        Log.d(TAG, "Intentando registrar usuario: $email")

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.email ?: "Usuario",
                        email = firebaseUser.email ?: "",
                        profileImageUrl = null
                    )
                    userRepository.insertUser(newUser)
                    Log.d(TAG, "Perfil básico de usuario insertado en Room para UID: ${firebaseUser.uid}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true // Sigue siendo true hasta que se haga signOut
                    )
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToSetupProfile(firebaseUser.uid))
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = "Error inesperado al registrar usuario."
                    )
                    Log.e(TAG, "Registro exitoso, pero firebaseUser es nulo.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
                    is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido."
                    is FirebaseAuthUserCollisionException -> "El correo electrónico ya está registrado."
                    else -> "Error de registro: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(statusMessage = errorMessage)
                Log.e(TAG, "Fallo en el registro de Firebase Auth: $errorMessage", e)
            }
        }
    }

    fun loginUser() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput.trim()

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = "Por favor, ingresa correo y contraseña.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
        Log.d(TAG, "Intentando iniciar sesión para: $email")

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                _navigationEvents.emit(AuthNavigationEvent.NavigateToMain) // <-- EMITIR EVENTO
                Log.d(TAG, "Inicio de sesión exitoso.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                val errorMessage = e.message ?: "Error desconocido al iniciar sesión."
                _uiState.value = _uiState.value.copy(statusMessage = "Fallo al iniciar sesión: $errorMessage")
                Log.e(TAG, "Fallo en el inicio de sesión de Firebase Auth: $errorMessage", e)
            }
        }
    }
}