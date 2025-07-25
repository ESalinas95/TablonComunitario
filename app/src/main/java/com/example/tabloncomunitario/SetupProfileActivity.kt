package com.example.tabloncomunitario // ¡IMPORTANTE! Asegúrate de que este sea tu nombre de paquete correcto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.SetupProfileScreen // Importa el Composable de la UI
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModel // Importa tu ViewModel
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModelFactory // Importa tu Factory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class SetupProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var setupProfileViewModel: SetupProfileViewModel // Instancia del ViewModel

    companion object {
        private const val TAG = "SetupProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: SetupProfileActivity iniciada.")

        // Configuración del Toolbar (debe estar aquí, en la Activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Completa tu Perfil"

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        // Inicializar SetupProfileViewModel con un Factory
        val factory = SetupProfileViewModelFactory(auth, storage, userRepository)
        setupProfileViewModel = ViewModelProvider(this, factory)[SetupProfileViewModel::class.java]

        // Inicializar el ViewModel con el UID y email del usuario
        val userId = intent.getStringExtra("USER_UID")
        val userEmail = auth.currentUser?.email
        setupProfileViewModel.initialize(userId, userEmail)

        // Manejo del botón de retroceso (debe estar en la Activity)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@SetupProfileActivity, "Debes completar tu perfil para continuar.", Toast.LENGTH_SHORT).show()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // --- Observar el uiState del ViewModel para mensajes y navegación ---
        lifecycleScope.launch {
            setupProfileViewModel.uiState.collectLatest { uiState ->
                // Mostrar mensajes de estado como Toast (el ViewModel no tiene contexto para Toasts)
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@SetupProfileActivity, message, Toast.LENGTH_SHORT).show()
                    // Si el mensaje no es persistente, resetéalo en el ViewModel después de mostrarlo
                    // Para simplificar, asumimos que el ViewModel lo limpia.
                }

                // Manejar la navegación disparada por el ViewModel
                if (uiState.navigateToMain) {
                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    setupProfileViewModel.navigationCompleted() // Resetear la bandera en el ViewModel
                }
            }
        }

        // --- Configurar la UI con Jetpack Compose ---
        setContent {
            MaterialTheme {
                // Observar el uiState del ViewModel como un estado de Compose
                val uiState by setupProfileViewModel.uiState.collectAsState()

                SetupProfileScreen(
                    uiState = uiState,
                    onDisplayNameChange = { setupProfileViewModel.onDisplayNameChange(it) },
                    onContactNumberChange = { setupProfileViewModel.onContactNumberChange(it) },
                    onDocumentNumberChange = { setupProfileViewModel.onDocumentNumberChange(it) },
                    onApartmentNumberChange = { setupProfileViewModel.onApartmentNumberChange(it) },
                    onAboutMeChange = { setupProfileViewModel.onAboutMeChange(it) },
                    // El Composable llama a requestImagePick del ViewModel
                    onSelectImageClick = { setupProfileViewModel.requestImagePick() },
                    onSaveProfileClick = { setupProfileViewModel.saveProfileInformation() }
                )
            }
        }
    }

    // onSupportNavigateUp es llamado por el sistema cuando se pulsa la flecha de retroceso del Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Delega al dispatcher, que usa el callback que definimos
        return true
    }
}