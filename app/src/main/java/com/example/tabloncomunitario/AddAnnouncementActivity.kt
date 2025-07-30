package com.example.tabloncomunitario

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    private lateinit var storage: FirebaseStorage // Se mantiene si se usa para eliminar imágenes
    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var addAnnouncementViewModel: AddAnnouncementViewModel // Instancia del ViewModel

    companion object {
        private const val TAG = "AddAnnouncementActivity"
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

        val factory = AddAnnouncementViewModelFactory(auth, userRepository, announcementRepository)
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
}