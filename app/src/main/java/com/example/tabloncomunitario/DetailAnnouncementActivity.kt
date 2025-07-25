package com.example.tabloncomunitario // Asegúrate de que tu paquete sea correcto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Importar lifecycleScope
import androidx.compose.runtime.getValue // Importar para 'by mutableStateOf'
import androidx.compose.runtime.mutableStateOf // Importar para 'by mutableStateOf'
import androidx.compose.runtime.setValue // Importar para 'by mutableStateOf'
import androidx.compose.material3.MaterialTheme // Importar MaterialTheme
import androidx.activity.compose.setContent // Importar setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.CommentRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.tabloncomunitario.ui.auth.DetailAnnouncementScreen // <--- Importa tu Composable
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModel // <--- Importa tu ViewModel
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModelFactory // <--- Importa tu Factory
import kotlinx.coroutines.flow.collectLatest // Importar collectLatest
import kotlinx.coroutines.launch // Importar launch

class DetailAnnouncementActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var detailAnnouncementViewModel: DetailAnnouncementViewModel // Instancia del ViewModel

    // No necesitamos más estados aquí, el ViewModel los gestiona
    // private var currentAnnouncementState by mutableStateOf<Announcement?>(null)
    // private var commentsState by mutableStateOf<List<Comment>>(emptyList())
    // private var commentInputState by mutableStateOf("")
    // private var isLoadingState by mutableStateOf(false)
    // private var statusMessageState by mutableStateOf<String?>(null)
    // private var isAuthorState by mutableStateOf(false)
    // private var canEditDeleteState by mutableStateOf(true)

    companion object {
        private const val TAG = "DetailAnnouncementAct"
    }

    private val editAnnouncementLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // El ViewModel ya tiene la lógica de refresco y chequeo de acciones
            // Aquí solo se notifica que la edición terminó, el ViewModel se encargará.
            Toast.makeText(this, "Anuncio editado. Refrescando...", Toast.LENGTH_SHORT).show()
            val announcementId = intent.getStringExtra("announcement") ?: "" // Re-obtener el ID
            if (announcementId.isNotEmpty()) {
                detailAnnouncementViewModel.loadAnnouncementDetails(announcementId) // Forzar recarga si hubo edición
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DetailAnnouncementActivity iniciada.")

        // Configurar el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())
        commentRepository = CommentRepository(database.commentDao())

        // --- NUEVO: Inicializar DetailAnnouncementViewModel con un Factory ---
        val factory = DetailAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository, commentRepository)
        detailAnnouncementViewModel = ViewModelProvider(this, factory)[DetailAnnouncementViewModel::class.java]
        // --- FIN NUEVO ---

        // Obtener el ID del anuncio del Intent y pasárselo al ViewModel para que inicie la carga
        val announcementId = intent.getStringExtra("announcement")
        if (announcementId == null) {
            Toast.makeText(this, "Error: ID de Anuncio no proporcionado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        detailAnnouncementViewModel.loadAnnouncementDetails(announcementId) // Iniciar carga del anuncio en el ViewModel

        // --- Observar el uiState del ViewModel ---
        lifecycleScope.launch {
            detailAnnouncementViewModel.uiState.collectLatest { uiState ->
                // Mostrar Toasts para mensajes de estado (el ViewModel no tiene contexto)
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@DetailAnnouncementActivity, message, Toast.LENGTH_SHORT).show()
                    // Si es un mensaje de éxito/error no persistente, lo podemos limpiar en el ViewModel
                    // detailAnnouncementViewModel.setStatusMessage(null) // Si el ViewModel tiene este método
                }

                // Manejar la eliminación del anuncio (navegar hacia atrás)
                if (uiState.announcementDeleted) {
                    Toast.makeText(this@DetailAnnouncementActivity, "Anuncio eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    finish() // Cierra esta Activity y vuelve a la anterior
                    detailAnnouncementViewModel.announcementDeletionCompleted() // Resetea la bandera
                }
            }
        }
        // --- FIN Observación ---

        // --- Configurar la UI con Jetpack Compose ---
        setContent {
            MaterialTheme {
                val uiState by detailAnnouncementViewModel.uiState.collectAsState()

                DetailAnnouncementScreen(
                    uiState = uiState,
                    onCommentInputChange = { detailAnnouncementViewModel.onCommentInputChange(it) },
                    onSendCommentClick = { detailAnnouncementViewModel.addComment() },
                    onEditClick = {
                        // Aquí, el ViewModel ya tiene el anuncio cargado
                        detailAnnouncementViewModel.uiState.value.announcement?.let { announcement ->
                            val intent = Intent(this@DetailAnnouncementActivity, AddAnnouncementActivity::class.java)
                            intent.putExtra("EDIT_ANNOUNCEMENT", announcement.id) // Pasar el ID del anuncio
                            editAnnouncementLauncher.launch(intent)
                        }
                    },
                    onDeleteClick = {
                        // El ViewModel expone un evento o muestra un diálogo de confirmación.
                        // Aquí, la Activity aún maneja el AlertDialog por ser componente de Android.
                        detailAnnouncementViewModel.uiState.value.announcement?.let { announcement ->
                            AlertDialog.Builder(this@DetailAnnouncementActivity)
                                .setTitle("Eliminar Anuncio")
                                .setMessage("¿Estás seguro de que quieres eliminar este anuncio: '${announcement.title}'?")
                                .setPositiveButton("Sí") { dialog, which ->
                                    detailAnnouncementViewModel.deleteAnnouncement() // Llama al ViewModel para eliminar
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                    },
                    onCommentAuthorClick = { userId -> navigateToUserProfilePreview(userId) },
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

    private fun navigateToUserProfilePreview(userId: String) {
        val intent = Intent(this, UserProfilePreviewActivity::class.java)
        intent.putExtra(UserProfilePreviewActivity.EXTRA_USER_ID, userId)
        startActivity(intent)
    }

    // ELIMINADO: Todas las funciones de lógica (checkAuthorActions, deleteAnnouncement, loadCurrentUserProfile, addComment, loadComments)
    // Se han movido al DetailAnnouncementViewModel.
}