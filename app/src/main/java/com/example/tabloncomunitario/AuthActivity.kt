package com.example.tabloncomunitario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.AuthScreen
import com.example.tabloncomunitario.viewmodel.AuthViewModel
import com.example.tabloncomunitario.viewmodel.AuthViewModelFactory
// ELIMINADO: import com.example.tabloncomunitario.viewmodel.auth.AuthNavigationEvent // Ya no se observa aquí directamente
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest // Importar collectLatest
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var authViewModel: AuthViewModel

    companion object {
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: AuthActivity iniciada.")

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        val factory = AuthViewModelFactory(auth, userRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // --- CAMBIO CLAVE AQUÍ: Observar los eventos de navegación del ViewModel ---
        // La AuthActivity, al ser el punto de entrada, necesita cerrarse cuando el ViewModel indica navegación.
        lifecycleScope.launch {
            authViewModel.navigationEvents.collectLatest { event ->
                Log.d(TAG, "AuthActivity: Evento de navegación recibido: $event. Finalizando Activity.")
                // Independientemente del evento, si el ViewModel lo emite, esta Activity debe terminar.
                // La navegación real la orquesta el NavHost en MainActivity.
                finish()
            }
        }
        // --- FIN CAMBIO CLAVE ---

        // Configurar la UI con Jetpack Compose
        setContent {
            MaterialTheme {
                val uiState = authViewModel.uiState.collectAsState().value

                AuthScreen(
                    onRegisterClick = { email, password ->
                        authViewModel.onEmailChange(email)
                        authViewModel.onPasswordChange(password)
                        authViewModel.registerUser()
                    },
                    onLoginClick = { email, password ->
                        authViewModel.onEmailChange(email)
                        authViewModel.onPasswordChange(password)
                        authViewModel.loginUser()
                    },
                    statusMessage = uiState.statusMessage,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}