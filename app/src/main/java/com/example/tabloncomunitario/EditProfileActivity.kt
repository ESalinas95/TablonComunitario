package com.example.tabloncomunitario // ¡IMPORTANTE! Asegúrate de que este sea tu nombre de paquete correcto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.tabloncomunitario.databinding.ActivityEditProfileBinding // Mantener si aún usas XML para esta Activity
// ELIMINADO: import com.example.tabloncomunitario.ui.profile_edit.EditProfileScreen // Si ya estás migrando a Compose
// ELIMINADO: import com.example.tabloncomunitario.ui.profile_edit.EditProfileUiState // Si ya estás migrando a Compose
// ELIMINADO: import androidx.compose.runtime.getValue
// ELIMINADO: import androidx.compose.runtime.mutableStateOf
// ELIMINADO: import androidx.compose.runtime.setValue
// ELIMINADO: import androidx.compose.material3.MaterialTheme
// ELIMINADO: import androidx.activity.compose.setContent // Si ya estás migrando a Compose

import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    // Si estás migrando a Compose, estas variables de estado se moverían al ViewModel y la Activity usaria setContent
    // private var selectedImageUriState: Uri? by mutableStateOf(null)
    // private var isLoadingState: Boolean by mutableStateOf(false)
    // private var statusMessageState: String? by mutableStateOf(null)
    // private var displayNameInputState: String by mutableStateOf("")
    // private var contactNumberInputState: String by mutableStateOf("")
    // private var dniNumberInputState: String by mutableStateOf("")
    // private var apartmentNumberInputState: String by mutableStateOf("")
    // private var aboutMeInputState: String by mutableStateOf("")

    // Si sigues en Views (XML binding)
    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository

    private var selectedImageUri: Uri? = null // Para la Activity basada en Views
    private var currentUserProfile: User? = null

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data // Actualiza la variable de la Activity
            selectedImageUri?.let {
                binding.imageViewProfile.visibility = View.VISIBLE // Asegura que el ImageView esté visible
                Glide.with(this).load(it).into(binding.imageViewProfile)
                Log.d(TAG, "Imagen seleccionada: $it")
            } ?: Log.w(TAG, "No se recibió URI de imagen al seleccionar.")
        } else {
            Log.d(TAG, "Selección de imagen cancelada.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Si sigues en Views (XML binding)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del Toolbar
        setSupportActionBar(binding.toolbarEditProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        loadUserProfile()

        binding.buttonSelectProfileImage.setOnClickListener {
            openImageChooser()
        }

        binding.imageViewProfile.setOnClickListener {
            openImageChooser()
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveUserProfile()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Carga el perfil del usuario actual desde Room.
     */
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    currentUserProfile = userRepository.getUserById(userId)
                    currentUserProfile?.let { user ->
                        // Campos de solo lectura
                        binding.editTextDisplayName.setText(user.displayName)
                        binding.editTextSetupDocumentNumber.setText(user.documentNumber)

                        // Campos editables
                        binding.editTextSetupContactNumber.setText(user.contactNumber)
                        binding.editTextSetupApartmentNumber.setText(user.apartmentNumber)
                        binding.editTextSetupAboutMe.setText(user.aboutMe)

                        // Cargar imagen de perfil
                        if (!user.profileImageUrl.isNullOrEmpty()) {
                            // Cargar la imagen remota
                            Glide.with(this@EditProfileActivity).load(user.profileImageUrl).into(binding.imageViewProfile)
                            // Actualizar selectedImageUri si quieres que el "Guardar" la mantenga sin resubir
                            selectedImageUri = Uri.parse(user.profileImageUrl)
                        }
                        Log.d(TAG, "Perfil de usuario cargado desde Room para edición: ${user.displayName}")
                    } ?: run {
                        Log.w(TAG, "Perfil de usuario no encontrado en Room para edición: $userId. Campos vacíos.")
                        Toast.makeText(this@EditProfileActivity, "Perfil no encontrado en la base de datos local.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar perfil desde Room para edición: ${e.message}", e)
                    Toast.makeText(this@EditProfileActivity, "Error al cargar perfil.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.w(TAG, "No hay usuario autenticado al intentar cargar el perfil para edición.")
        }
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

    /**
     * Guarda (actualiza) el perfil del usuario en Room.
     */
    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener los campos desde la UI
        val displayName = binding.editTextDisplayName.text.toString().trim()
        val contactNumber = binding.editTextSetupContactNumber.text.toString().trim()
        val documentNumber = binding.editTextSetupDocumentNumber.text.toString().trim()
        val apartmentNumber = binding.editTextSetupApartmentNumber.text.toString().trim()
        val aboutMe = binding.editTextSetupAboutMe.text.toString().trim()

        if (displayName.isEmpty()) {
            Toast.makeText(this, "El nombre a mostrar es obligatorio.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.textViewStatus.text = "Guardando perfil..."
        binding.buttonSaveProfile.isEnabled = false
        Log.d(TAG, "Intentando guardar perfil de usuario.")

        val userToUpdate = currentUserProfile?.copy(
            displayName = displayName,
            contactNumber = if (contactNumber.isEmpty()) null else contactNumber,
            documentNumber = documentNumber,
            apartmentNumber = if (apartmentNumber.isEmpty()) null else apartmentNumber,
            aboutMe = if (aboutMe.isEmpty()) null else aboutMe,
        ) ?: run {
            Log.e(TAG, "currentUserProfile es nulo al intentar guardar. No se puede crear un objeto User completo para guardar.")
            Toast.makeText(this, "Error interno: perfil no cargado. No se pueden guardar los cambios.", Toast.LENGTH_SHORT).show()
            binding.buttonSaveProfile.isEnabled = true
            return
        }

        if (selectedImageUri != null) {
            Log.d(TAG, "Imagen de perfil seleccionada. Iniciando subida.")
            // --- CAMBIO CLAVE AQUÍ: Pasando selectedImageUri a la función ---
            uploadProfileImage(userId, userToUpdate, selectedImageUri!!)
            // --- FIN CAMBIO CLAVE ---
        } else {
            Log.d(TAG, "No hay nueva imagen de perfil. Guardando solo datos.")
            userToUpdate.profileImageUrl = currentUserProfile?.profileImageUrl // Mantener la URL existente
            saveUserToRoom(userToUpdate)
        }
    }

    /**
     * Sube la imagen de perfil a Firebase Storage y luego guarda el perfil en Room.
     * @param userId UID del usuario.
     * @param user El objeto User con los datos actualizados (sin la nueva URL de imagen aún).
     * @param imageUri La URI local de la imagen seleccionada.
     */
    private fun uploadProfileImage(userId: String, user: User, imageUri: Uri) { // <--- CAMBIO CLAVE AQUÍ: Añadido imageUri
        val storageRef = storage.reference
        val profileImageRef = storageRef.child("profile_images/$userId.jpg")

        profileImageRef.putFile(imageUri) // Usar el imageUri pasado como parámetro
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Imagen de perfil subida a Storage: ${taskSnapshot.metadata?.path}")
                profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d(TAG, "URL de descarga obtenida: $downloadUri")
                    user.profileImageUrl = downloadUri.toString()
                    saveUserToRoom(user)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener URL de descarga de imagen de perfil: ${e.message}", e)
                    binding.textViewStatus.text = "Error al obtener URL de la imagen."
                    binding.buttonSaveProfile.isEnabled = true
                    Toast.makeText(this, "Error al obtener URL: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al subir la imagen de perfil: ${e.message}", e)
                binding.textViewStatus.text = "Error al subir la imagen."
                binding.buttonSaveProfile.isEnabled = true
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                binding.textViewStatus.text = "Subiendo imagen: $progress%"
                Log.d(TAG, "Progreso de subida de imagen de perfil: $progress%")
            }
    }

    /**
     * Guarda el objeto User actualizado en Room.
     */
    private fun saveUserToRoom(user: User) {
        lifecycleScope.launch {
            try {
                userRepository.updateUser(user)
                Log.d(TAG, "Perfil de usuario actualizado en Room para UID: ${user.uid}")
                Toast.makeText(this@EditProfileActivity, "Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                binding.textViewStatus.text = ""
                binding.buttonSaveProfile.isEnabled = true
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar perfil en Room: ${e.message}", e)
                Toast.makeText(this@EditProfileActivity, "Error al guardar el perfil: ${e.message}", Toast.LENGTH_LONG).show()
                binding.textViewStatus.text = "Error al guardar."
                binding.buttonSaveProfile.isEnabled = true
            }
        }
    }
}