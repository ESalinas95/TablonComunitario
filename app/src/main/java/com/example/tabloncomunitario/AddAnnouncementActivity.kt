package com.example.tabloncomunitario // Asegúrate de que tu paquete sea correcto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.tabloncomunitario.ui.auth.AddAnnouncementScreen // <--- Importa tu Composable
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModel // <--- Importa tu ViewModel
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModelFactory // <--- Importa tu Factory
import kotlinx.coroutines.flow.collectLatest // Importar collectLatest
import kotlinx.coroutines.launch // Importar launch

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var addAnnouncementViewModel: AddAnnouncementViewModel // Instancia del ViewModel

    companion object {
        private const val TAG = "AddAnnouncementActivity"
    }

    // Launcher para seleccionar una imagen de la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            addAnnouncementViewModel.onSelectImageClick(result.data?.data) // Pasa la URI al ViewModel
            Log.d(TAG, "Imagen seleccionada desde Activity: ${result.data?.data}")
        } else {
            Log.d(TAG, "Selección de imagen cancelada desde Activity.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: AddAnnouncementActivity iniciada.")

        // Configuración del Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())

        // --- NUEVO: Inicializar AddAnnouncementViewModel con un Factory ---
        val factory = AddAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository)
        addAnnouncementViewModel = ViewModelProvider(this, factory)[AddAnnouncementViewModel::class.java]
        // --- FIN NUEVO ---

        // Obtener el announcementId del Intent si estamos en modo edición
        val announcementId = intent.getStringExtra("EDIT_ANNOUNCEMENT") // O intent.getParcelableExtra("EDIT_ANNOUNCEMENT")?.id
        addAnnouncementViewModel.initialize(announcementId)

        // --- Observar el uiState del ViewModel ---
        lifecycleScope.launch {
            addAnnouncementViewModel.uiState.collectLatest { uiState ->
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@AddAnnouncementActivity, message, Toast.LENGTH_SHORT).show()
                }

                if (uiState.navigateToDetailsOrMain) {
                    setResult(Activity.RESULT_OK) // Indica a la actividad anterior que hubo cambios
                    finish() // Cierra la actividad
                    addAnnouncementViewModel.navigationCompleted()
                }
            }
        }
        // --- FIN Observación ---

        // --- Configurar la UI con Jetpack Compose ---
        setContent {
            MaterialTheme {
                val uiState by addAnnouncementViewModel.uiState.collectAsState()

                AddAnnouncementScreen(
                    uiState = uiState,
                    onTitleChange = { addAnnouncementViewModel.onTitleChange(it) },
                    onDescriptionChange = { addAnnouncementViewModel.onDescriptionChange(it) },
                    onSelectImageClick = { openImageChooser() }, // Dispara el launcher de la Activity
                    onPublishClick = { addAnnouncementViewModel.saveAnnouncementInformation() },
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

    /**
     * Abre el selector de imágenes de la galería del dispositivo.
     * Esta función debe vivir en la Activity porque usa ActivityResultLauncher.
     */
    private fun openImageChooser() {
        Log.d(TAG, "openImageChooser: Iniciando selector de imágenes.")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            pickImageLauncher.launch(intent)
            Log.d(TAG, "openImageChooser: Intent lanzado para seleccionar imagen.")
        } else {
            Toast.makeText(this, "No se encontró aplicación para seleccionar imágenes.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "openImageChooser: No se encontró aplicación para manejar el Intent ACTION_GET_CONTENT.")
        }
    }
}