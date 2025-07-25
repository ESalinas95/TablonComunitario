package com.example.tabloncomunitario

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tabloncomunitario.database.AppDatabase
import com.example.tabloncomunitario.repository.AnnouncementRepository
import com.example.tabloncomunitario.repository.UserRepository
import com.example.tabloncomunitario.repository.CommentRepository
import com.example.tabloncomunitario.navigation.AppDestinations
import com.example.tabloncomunitario.ui.auth.AuthScreen
import com.example.tabloncomunitario.ui.auth.MainScreen
import com.example.tabloncomunitario.ui.auth.SetupProfileScreen
import com.example.tabloncomunitario.ui.auth.DetailAnnouncementScreen
import com.example.tabloncomunitario.ui.auth.AddAnnouncementScreen
import com.example.tabloncomunitario.ui.auth.ProfileScreen
import com.example.tabloncomunitario.ui.auth.EditProfileScreen
import com.example.tabloncomunitario.ui.auth.UserProfilePreviewScreen
import com.example.tabloncomunitario.viewmodel.AuthViewModel
import com.example.tabloncomunitario.viewmodel.AuthViewModelFactory
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModel
import com.example.tabloncomunitario.viewmodel.SetupProfileViewModelFactory
import com.example.tabloncomunitario.viewmodel.MainViewModel
import com.example.tabloncomunitario.viewmodel.MainViewModelFactory
import com.example.tabloncomunitario.viewmodel.ProfileViewModel
import com.example.tabloncomunitario.viewmodel.ProfileViewModelFactory
import com.example.tabloncomunitario.viewmodel.EditProfileViewModel
import com.example.tabloncomunitario.viewmodel.EditProfileViewModelFactory
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModel
import com.example.tabloncomunitario.viewmodel.AddAnnouncementViewModelFactory
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModel
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementViewModelFactory
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModel
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewViewModelFactory
import com.example.tabloncomunitario.viewmodel.AuthNavigationEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var announcementRepository: AnnouncementRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val TAG = "MainActivityHost"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity iniciada como host de navegación.")

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        announcementRepository = AnnouncementRepository(database.announcementDao())
        commentRepository = CommentRepository(database.commentDao())

        // Configurar la UI con Jetpack Compose y el NavHost
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                val startDestination = remember {
                    if (auth.currentUser != null) {
                        Log.d(TAG, "Usuario logueado al inicio. Ruta de inicio: ${AppDestinations.MAIN_ROUTE}")
                        AppDestinations.MAIN_ROUTE
                    } else {
                        Log.d(TAG, "Usuario no logueado al inicio. Ruta de inicio: ${AppDestinations.AUTH_ROUTE}")
                        AppDestinations.AUTH_ROUTE
                    }
                }

                AppNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    auth = auth,
                    userRepository = userRepository,
                    announcementRepository = announcementRepository,
                    commentRepository = commentRepository,
                    storage = storage
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    auth: FirebaseAuth,
    userRepository: UserRepository,
    announcementRepository: AnnouncementRepository,
    commentRepository: CommentRepository,
    storage: FirebaseStorage
) {

    NavHost(navController = navController, startDestination = startDestination) {

        // --- Ruta de Autenticación (AUTH_ROUTE) ---
        composable(AppDestinations.AUTH_ROUTE) {
            val authViewModelFactory = remember { AuthViewModelFactory(auth, userRepository) }
            val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
            val uiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                authViewModel.navigationEvents.collectLatest { event ->
                    when (event) {
                        is AuthNavigationEvent.NavigateToSetupProfile -> {
                            Log.d(TAG, "Navegando a SETUP_PROFILE_ROUTE para UID: ${event.userId}")
                            navController.navigate(AppDestinations.SETUP_PROFILE_ROUTE + "?userId=${event.userId}") {
                                popUpTo(AppDestinations.AUTH_ROUTE) { inclusive = true }
                            }
                        }
                        AuthNavigationEvent.NavigateToMain -> {
                            Log.d(TAG, "Navegando a MAIN_ROUTE (login exitoso).")
                            navController.navigate(AppDestinations.MAIN_ROUTE) {
                                popUpTo(AppDestinations.AUTH_ROUTE) { inclusive = true }
                            }
                        }
                        AuthNavigationEvent.NavigateToAuth -> {
                            Log.d(TAG, "Navegando a AUTH_ROUTE (logout).")
                            navController.navigate(AppDestinations.AUTH_ROUTE) {
                                popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                            }
                        }
                    }
                }
            }

            AuthScreen(
                onRegisterClick = { email, password ->
                    authViewModel.onEmailChange(email)
                    authViewModel.onPasswordChange(password)
                    authViewModel.registerUser()
                },
                onLoginClick = { email, password ->
                    authViewModel.onEmailChange(email)
                    authViewModel.onPasswordChange(password)
                    authViewModel.loginUser()
                },
                statusMessage = uiState.statusMessage,
                isLoading = uiState.isLoading
            )
        }

        // --- Ruta de Configuración de Perfil (SETUP_PROFILE_ROUTE) ---
        composable(
            route = AppDestinations.SETUP_PROFILE_ROUTE + "?userId={userId}",
            arguments = listOf(navArgument("userId") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            val setupProfileViewModelFactory = remember { SetupProfileViewModelFactory(auth, storage, userRepository) }
            val setupProfileViewModel: SetupProfileViewModel = viewModel(factory = setupProfileViewModelFactory)

            val uiState by setupProfileViewModel.uiState.collectAsState()

            LaunchedEffect(userId) {
                if (userId != null && uiState.displayNameInput.isEmpty() && !uiState.isLoading) {
                    setupProfileViewModel.initialize(userId, auth.currentUser?.email)
                } else if (userId == null) {
                    Log.w(TAG, "SETUP_PROFILE_ROUTE: userId es nulo al entrar. No se puede inicializar el perfil.")
                    setupProfileViewModel.initialize(null, auth.currentUser?.email)
                }
            }
            LaunchedEffect(uiState.navigateToMain) {
                if (uiState.navigateToMain) {
                    Log.d(TAG, "Navegando de SETUP_PROFILE_ROUTE a MAIN_ROUTE.")
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.SETUP_PROFILE_ROUTE + (userId?.let { "?userId=$it" } ?: "")) { inclusive = true }
                    }
                    setupProfileViewModel.navigationCompleted()
                }
            }

            SetupProfileScreen(
                uiState = uiState,
                onDisplayNameChange = { setupProfileViewModel.onDisplayNameChange(it) },
                onContactNumberChange = { setupProfileViewModel.onContactNumberChange(it) },
                onDocumentNumberChange = { setupProfileViewModel.onDocumentNumberChange(it) },
                onApartmentNumberChange = { setupProfileViewModel.onApartmentNumberChange(it) },
                onAboutMeChange = { setupProfileViewModel.onAboutMeChange(it) },
                onSelectImageClick = {
                    Toast.makeText(navController.context, "Esta funcionalidad aún no se encuentra implementada.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Seleccionar imagen de anuncio - revertido a mensaje.")
                },
                onSaveProfileClick = { setupProfileViewModel.saveProfileInformation() }
            )
        }

        // --- Ruta Principal (MAIN_ROUTE) ---
        composable(AppDestinations.MAIN_ROUTE) {
            val mainViewModelFactory = remember { MainViewModelFactory(auth, userRepository, announcementRepository) }
            val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)

            val uiState by mainViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                Log.d(TAG, "LaunchedEffect(MAIN_ROUTE): Inicializando MainViewModel.")
                mainViewModel.initialize()
            }

            MainScreen(
                uiState = uiState,
                onSearchQueryChange = { query -> mainViewModel.onSearchQueryChange(query) },
                onFabClick = { navController.navigate(AppDestinations.ADD_ANNOUNCEMENT_ROUTE) },
                onAnnouncementClick = { announcement ->
                    navController.navigate("${AppDestinations.DETAIL_ANNOUNCEMENT_ROUTE}/${announcement.id}")
                },
                onProfileClick = { navController.navigate(AppDestinations.PROFILE_ROUTE) },
                onLogoutClick = {
                    mainViewModel.logoutUser()
                    navController.navigate(AppDestinations.AUTH_ROUTE) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // --- Ruta de Mi Perfil (PROFILE_ROUTE) ---
        composable(AppDestinations.PROFILE_ROUTE) {
            val profileViewModelFactory = remember { ProfileViewModelFactory(auth, userRepository, announcementRepository) }
            val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)

            val uiState by profileViewModel.uiState.collectAsState()

            LaunchedEffect(auth.currentUser?.uid) {
                Log.d(TAG, "LaunchedEffect(PROFILE_ROUTE): Cargando perfil y anuncios para UID: ${auth.currentUser?.uid}")
                auth.currentUser?.uid?.let { profileViewModel.loadProfileAndAnnouncements(it) }
            }

            ProfileScreen(
                uiState = uiState,
                onEditProfileClick = { navController.navigate(AppDestinations.EDIT_PROFILE_ROUTE) },
                onAnnouncementClick = { announcement ->
                    navController.navigate("${AppDestinations.DETAIL_ANNOUNCEMENT_ROUTE}/${announcement.id}")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Ruta de Editar Perfil (EDIT_PROFILE_ROUTE) ---
        composable(AppDestinations.EDIT_PROFILE_ROUTE) {
            val editProfileViewModelFactory = remember { EditProfileViewModelFactory(auth, storage, userRepository) }
            val editProfileViewModel: EditProfileViewModel = viewModel(factory = editProfileViewModelFactory)

            val uiState by editProfileViewModel.uiState.collectAsState()

            LaunchedEffect(auth.currentUser?.uid) {
                Log.d(TAG, "LaunchedEffect(EDIT_PROFILE_ROUTE): Cargando perfil para UID: ${auth.currentUser?.uid}")
                auth.currentUser?.uid?.let { editProfileViewModel.loadUserProfile(it) }
            }
            LaunchedEffect(uiState.navigateToProfile) {
                if (uiState.navigateToProfile) {
                    Log.d(TAG, "Navegando de vuelta a PROFILE_ROUTE.")
                    navController.popBackStack()
                    editProfileViewModel.navigationCompleted()
                }
            }

            EditProfileScreen(
                uiState = uiState,
                onSelectImageClick = {
                    // --- CAMBIO CLAVE AQUÍ: Revertido a mensaje ---
                    Toast.makeText(navController.context, "Esta funcionalidad aún no se encuentra implementada.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Seleccionar imagen de perfil (edición) - revertido a mensaje.")
                    // --- FIN CAMBIO CLAVE ---
                },
                onContactNumberChange = { editProfileViewModel.onContactNumberChange(it) },
                onApartmentNumberChange = { editProfileViewModel.onApartmentNumberChange(it) },
                onAboutMeChange = { editProfileViewModel.onAboutMeChange(it) },
                onSaveProfileClick = { editProfileViewModel.saveUserProfile() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Ruta de Añadir/Editar Anuncio (ADD_ANNOUNCEMENT_ROUTE) ---
        composable(
            route = AppDestinations.ADD_ANNOUNCEMENT_ROUTE + "?announcementId={announcementId}",
            arguments = listOf(navArgument("announcementId") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val announcementId = backStackEntry.arguments?.getString("announcementId")

            val addAnnouncementViewModelFactory = remember { AddAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository) }
            val addAnnouncementViewModel: AddAnnouncementViewModel = viewModel(factory = addAnnouncementViewModelFactory)

            val uiState by addAnnouncementViewModel.uiState.collectAsState()

            LaunchedEffect(announcementId) {
                Log.d(TAG, "LaunchedEffect(ADD_ANNOUNCEMENT_ROUTE): Inicializando ViewModel con announcementId: $announcementId")
                addAnnouncementViewModel.initialize(announcementId)
            }
            LaunchedEffect(uiState.navigateToDetailsOrMain) {
                if (uiState.navigateToDetailsOrMain) {
                    navController.popBackStack()
                    addAnnouncementViewModel.navigationCompleted()
                }
            }

            AddAnnouncementScreen(
                uiState = uiState,
                onTitleChange = { addAnnouncementViewModel.onTitleChange(it) },
                onDescriptionChange = { addAnnouncementViewModel.onDescriptionChange(it) },
                onSelectImageClick = {
                    Toast.makeText(navController.context, "Esta funcionalidad aún no se encuentra implementada.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Seleccionar imagen de anuncio - revertido a mensaje.")
                },
                onPublishClick = { addAnnouncementViewModel.saveAnnouncementInformation() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Ruta de Detalle de Anuncio (DETAIL_ANNOUNCEMENT_ROUTE) ---
        composable(
            route = AppDestinations.DETAIL_ANNOUNCEMENT_ROUTE + "/{announcementId}",
            arguments = listOf(navArgument("announcementId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val announcementId = backStackEntry.arguments?.getString("announcementId")

            val detailAnnouncementViewModelFactory = remember { DetailAnnouncementViewModelFactory(auth, storage, userRepository, announcementRepository, commentRepository) }
            val detailAnnouncementViewModel: DetailAnnouncementViewModel = viewModel(factory = detailAnnouncementViewModelFactory)

            val uiState by detailAnnouncementViewModel.uiState.collectAsState()

            LaunchedEffect(announcementId) {
                Log.d(TAG, "LaunchedEffect(DETAIL_ANNOUNCEMENT_ROUTE): Cargando detalles para announcementId: $announcementId")
                if (announcementId != null) {
                    detailAnnouncementViewModel.loadAnnouncementDetails(announcementId)
                } else {
                    detailAnnouncementViewModel.setStatusMessage("Error: ID de anuncio no proporcionado.")
                }
            }
            LaunchedEffect(uiState.announcementDeleted) {
                if (uiState.announcementDeleted) {
                    Log.d(TAG, "Anuncio eliminado. Volviendo atrás.")
                    navController.popBackStack()
                    detailAnnouncementViewModel.announcementDeletionCompleted()
                }
            }

            DetailAnnouncementScreen(
                uiState = uiState,
                onCommentInputChange = { detailAnnouncementViewModel.onCommentInputChange(it) },
                onSendCommentClick = { detailAnnouncementViewModel.addComment() },
                onEditClick = {
                    navController.navigate("${AppDestinations.ADD_ANNOUNCEMENT_ROUTE}?announcementId=$announcementId")
                },
                onDeleteClick = {
                    detailAnnouncementViewModel.deleteAnnouncement()
                },
                onCommentAuthorClick = { userId ->
                    navController.navigate("${AppDestinations.USER_PROFILE_PREVIEW_ROUTE}/$userId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Ruta de Previsualización de Perfil de Usuario (USER_PROFILE_PREVIEW_ROUTE) ---
        composable(
            route = AppDestinations.USER_PROFILE_PREVIEW_ROUTE + "/{userId}",
            arguments = listOf(navArgument("userId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            val userProfilePreviewViewModelFactory = remember { UserProfilePreviewViewModelFactory(userRepository) }
            val userProfilePreviewViewModel: UserProfilePreviewViewModel = viewModel(factory = userProfilePreviewViewModelFactory)

            val uiState by userProfilePreviewViewModel.uiState.collectAsState()

            LaunchedEffect(userId) {
                Log.d(TAG, "LaunchedEffect(USER_PROFILE_PREVIEW_ROUTE): Cargando perfil para userId: $userId")
                if (userId != null) {
                    userProfilePreviewViewModel.loadUserProfile(userId)
                } else {
                    userProfilePreviewViewModel.setStatusMessage("Error: ID de usuario no proporcionado.")
                }
            }

            UserProfilePreviewScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}