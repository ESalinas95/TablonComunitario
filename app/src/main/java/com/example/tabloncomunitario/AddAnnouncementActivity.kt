package com.example.tabloncomunitario

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.tabloncomunitario.ui.auth.AddAnnouncementScreen
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModel
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var addAnnouncementViewModel: AddAnnouncementViewModel

    companion object {
        private const val TAG = "AddAnnouncementActivity"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            addAnnouncementViewModel.onSelectImageClick(result.data?.data)
            Log.d(TAG, "Imagen seleccionada desde Activity: ${result.data?.data}")
        } else {
            Log.d(TAG, "Selección de imagen cancelada desde Activity.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: AddAnnouncementActivity iniciada.")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())

        val factory = AddAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository)
        addAnnouncementViewModel = ViewModelProvider(this, factory)[AddAnnouncementViewModel::class.java]

        val announcementId = intent.getStringExtra("EDIT_ANNOUNCEMENT")
        addAnnouncementViewModel.initialize(announcementId)


        lifecycleScope.launch {
            addAnnouncementViewModel.uiState.collectLatest { uiState ->
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@AddAnnouncementActivity, message, Toast.LENGTH_SHORT).show()
                }

                if (uiState.navigateToDetailsOrMain) {
                    setResult(Activity.RESULT_OK)
                    finish()
                    addAnnouncementViewModel.navigationCompleted()
                }
            }
        }

        setContent {
            MaterialTheme {
                val uiState by addAnnouncementViewModel.uiState.collectAsState()

                AddAnnouncementScreen(
                    uiState = uiState,
                    onTitleChange = { addAnnouncementViewModel.onTitleChange(it) },
                    onDescriptionChange = { addAnnouncementViewModel.onDescriptionChange(it) },
                    onSelectImageClick = { openImageChooser() },
                    onPublishClick = { addAnnouncementViewModel.saveAnnouncementInformation() },
                    onNavigateBack = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

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