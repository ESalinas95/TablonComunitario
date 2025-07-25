package com.example.tabloncomunitario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.ui.auth.ProfileScreen
import com.example.tabloncomunitario.viewmodel.ProfileViewModel
import com.example.tabloncomunitario.viewmodel.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ProfileActivity iniciada.")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Perfil" // El título del Toolbar

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())

        val factory = ProfileViewModelFactory(auth, userRepository, announcementRepository)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(Intent(this, AuthActivity::class.java))
            return
        }

        // Manejo del botón de retroceso (sin cambios)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Simplemente cierra la actividad
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            MaterialTheme {
                val uiState = profileViewModel.uiState.collectAsState().value

                ProfileScreen(
                    uiState = uiState,
                    onEditProfileClick = { navigateToEditProfile() },
                    onAnnouncementClick = { announcement -> navigateToAnnouncementDetails(announcement) },
                    onNavigateBack = { onBackPressedDispatcher.onBackPressed() }
                )

                LaunchedEffect(currentUser.uid) {
                    profileViewModel.loadProfileAndAnnouncements(currentUser.uid)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun navigateToEditProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAnnouncementDetails(announcement: Announcement) {
        val intent = Intent(this, DetailAnnouncementActivity::class.java)
        intent.putExtra("announcement", announcement)
        startActivity(intent)
    }
}