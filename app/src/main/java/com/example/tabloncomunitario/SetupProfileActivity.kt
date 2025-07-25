package com.example.tabloncomunitario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.SetupProfileScreen
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModel
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var setupProfileViewModel: SetupProfileViewModel

    companion object {
        private const val TAG = "SetupProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: SetupProfileActivity iniciada.")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Completa tu Perfil"

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        // Inicializar SetupProfileViewModel
        val factory = SetupProfileViewModelFactory(auth, storage, userRepository)
        setupProfileViewModel = ViewModelProvider(this, factory)[SetupProfileViewModel::class.java]

        // Inicializar el ViewModel con el UID y email del usuario
        val userId = intent.getStringExtra("USER_UID")
        val userEmail = auth.currentUser?.email
        setupProfileViewModel.initialize(userId, userEmail)

        // Manejo del botón de retroceso
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@SetupProfileActivity, "Debes completar tu perfil para continuar.", Toast.LENGTH_SHORT).show()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        lifecycleScope.launch {
            setupProfileViewModel.uiState.collectLatest { uiState ->
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@SetupProfileActivity, message, Toast.LENGTH_SHORT).show()
                }

                if (uiState.navigateToMain) {
                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    setupProfileViewModel.navigationCompleted() // Resetear la bandera en el ViewModel
                }
            }
        }

        setContent {
            MaterialTheme {
                val uiState by setupProfileViewModel.uiState.collectAsState()

                SetupProfileScreen(
                    uiState = uiState,
                    onDisplayNameChange = { setupProfileViewModel.onDisplayNameChange(it) },
                    onContactNumberChange = { setupProfileViewModel.onContactNumberChange(it) },
                    onDocumentNumberChange = { setupProfileViewModel.onDocumentNumberChange(it) },
                    onApartmentNumberChange = { setupProfileViewModel.onApartmentNumberChange(it) },
                    onAboutMeChange = { setupProfileViewModel.onAboutMeChange(it) },
                    onSelectImageClick = { setupProfileViewModel.requestImagePick() },
                    onSaveProfileClick = { setupProfileViewModel.saveProfileInformation() }
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Delega al dispatcher, que usa el callback que definimos
        return true
    }
}