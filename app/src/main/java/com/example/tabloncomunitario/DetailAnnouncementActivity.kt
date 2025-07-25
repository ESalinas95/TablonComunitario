package com.example.tabloncomunitario

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.CommentRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.tabloncomunitario.ui.auth.DetailAnnouncementScreen
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModel
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailAnnouncementActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var detailAnnouncementViewModel: DetailAnnouncementViewModel

    companion object {
        private const val TAG = "DetailAnnouncementAct"
    }

    private val editAnnouncementLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Anuncio editado. Refrescando...", Toast.LENGTH_SHORT).show()
            val announcementId = intent.getStringExtra("announcement") ?: ""
            if (announcementId.isNotEmpty()) {
                detailAnnouncementViewModel.loadAnnouncementDetails(announcementId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DetailAnnouncementActivity iniciada.")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())
        commentRepository = CommentRepository(database.commentDao())

        val factory = DetailAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository, commentRepository)
        detailAnnouncementViewModel = ViewModelProvider(this, factory)[DetailAnnouncementViewModel::class.java]

        val announcementId = intent.getStringExtra("announcement")
        if (announcementId == null) {
            Toast.makeText(this, "Error: ID de Anuncio no proporcionado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        detailAnnouncementViewModel.loadAnnouncementDetails(announcementId)

        lifecycleScope.launch {
            detailAnnouncementViewModel.uiState.collectLatest { uiState ->
                uiState.statusMessage?.let { message ->
                    Toast.makeText(this@DetailAnnouncementActivity, message, Toast.LENGTH_SHORT).show()
                }

                if (uiState.announcementDeleted) {
                    Toast.makeText(this@DetailAnnouncementActivity, "Anuncio eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    finish()
                    detailAnnouncementViewModel.announcementDeletionCompleted()
                }
            }
        }
        setContent {
            MaterialTheme {
                val uiState by detailAnnouncementViewModel.uiState.collectAsState()

                DetailAnnouncementScreen(
                    uiState = uiState,
                    onCommentInputChange = { detailAnnouncementViewModel.onCommentInputChange(it) },
                    onSendCommentClick = { detailAnnouncementViewModel.addComment() },
                    onEditClick = {
                        detailAnnouncementViewModel.uiState.value.announcement?.let { announcement ->
                            val intent = Intent(this@DetailAnnouncementActivity, AddAnnouncementActivity::class.java)
                            intent.putExtra("EDIT_ANNOUNCEMENT", announcement.id)
                            editAnnouncementLauncher.launch(intent)
                        }
                    },
                    onDeleteClick = {
                        detailAnnouncementViewModel.uiState.value.announcement?.let { announcement ->
                            AlertDialog.Builder(this@DetailAnnouncementActivity)
                                .setTitle("Eliminar Anuncio")
                                .setMessage("¿Estás seguro de que quieres eliminar este anuncio: '${announcement.title}'?")
                                .setPositiveButton("Sí") { dialog, which ->
                                    detailAnnouncementViewModel.deleteAnnouncement()
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
}