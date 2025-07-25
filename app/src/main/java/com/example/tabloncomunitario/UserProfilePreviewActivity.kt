package com.example.tabloncomunitario // Asegúrate de que tu paquete sea correcto

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Importar lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme // Importar MaterialTheme
import androidx.activity.compose.setContent // Importar setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.UserProfilePreviewScreen // <--- Importa tu Composable
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModel // <--- Importa tu ViewModel
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModelFactory // <--- Importa tu Factory
import com.google.firebase.auth.FirebaseAuth // Todavía necesario para auth.currentUser?.uid (aunque no lo uses directamente, es para userId)
import kotlinx.coroutines.launch

class UserProfilePreviewActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var userProfilePreviewViewModel: UserProfilePreviewViewModel // Instancia del ViewModel

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        private const val TAG = "UserProfilePreviewAct" // Cambiar TAG para evitar conflictos
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: UserProfilePreviewActivity iniciada.")

        // Configuración del Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Limpia el título del Toolbar

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        // --- NUEVO: Inicializar UserProfilePreviewViewModel con un Factory ---
        val factory = UserProfilePreviewViewModelFactory(userRepository)
        userProfilePreviewViewModel = ViewModelProvider(this, factory)[UserProfilePreviewViewModel::class.java]
        // --- FIN NUEVO ---

        // Obtener el userId del Intent y pasárselo al ViewModel para que inicie la carga
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        Log.d(TAG, "onCreate: User ID recibido del Intent: $userId")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "ID de usuario no proporcionado.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onCreate: User ID es nulo o vacío. Finalizando actividad.")
            finish()
            return
        }

        userProfilePreviewViewModel.loadUserProfile(userId) // Iniciar carga del perfil en el ViewModel

        // --- Configurar la UI con Jetpack Compose ---
        setContent {
            MaterialTheme {
                val uiState by userProfilePreviewViewModel.uiState.collectAsState()

                UserProfilePreviewScreen(
                    uiState = uiState,
                    onNavigateBack = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
        // --- FIN Configuración UI con Compose ---
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // ELIMINADO: loadUserProfile y displayUserProfile ya no están aquí, se movieron al ViewModel
}