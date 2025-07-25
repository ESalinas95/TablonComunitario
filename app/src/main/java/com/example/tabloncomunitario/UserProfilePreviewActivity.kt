package com.example.tabloncomunitario

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.UserProfilePreviewScreen
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModel
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModelFactory

class UserProfilePreviewActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var userProfilePreviewViewModel: UserProfilePreviewViewModel

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        private const val TAG = "UserProfilePreviewAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: UserProfilePreviewActivity iniciada.")

        // Configuración del Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Limpia el título del Toolbar

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        val factory = UserProfilePreviewViewModelFactory(userRepository)
        userProfilePreviewViewModel = ViewModelProvider(this, factory)[UserProfilePreviewViewModel::class.java]

        val userId = intent.getStringExtra(EXTRA_USER_ID)
        Log.d(TAG, "onCreate: User ID recibido del Intent: $userId")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "ID de usuario no proporcionado.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onCreate: User ID es nulo o vacío. Finalizando actividad.")
            finish()
            return
        }

        userProfilePreviewViewModel.loadUserProfile(userId)

        setContent {
            MaterialTheme {
                val uiState by userProfilePreviewViewModel.uiState.collectAsState()

                UserProfilePreviewScreen(
                    uiState = uiState,
                    onNavigateBack = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}