package com.example.tabloncomunitario.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tabloncomunitario.R
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.viewmodel.UserProfilePreviewUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePreviewScreen(
    uiState: UserProfilePreviewUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Usuario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else if (uiState.statusMessage != null && uiState.statusMessage.isNotEmpty()) {
                Text(
                    text = uiState.statusMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.userProfile == null) {
                Text(
                    text = "Perfil no encontrado.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Contenido del perfil
                val user = uiState.userProfile

                Image(
                    painter = if (user.profileImageUrl != null) {
                        rememberAsyncImagePainter(user.profileImageUrl)
                    } else {
                        painterResource(id = R.drawable.ic_default_profile)
                    },
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = user.displayName.ifEmpty { user.email.ifEmpty { "Usuario Anónimo" } },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = buildString {
                        if (user.displayName != user.email) {
                            append("Email: ${user.email.ifEmpty { "N/A" }}\n")
                        }
                        user.apartmentNumber?.let { append("Departamento: $it\n") }
                        user.contactNumber?.let { append("Contacto: $it\n") }
                        user.aboutMe?.let { append("Acerca de: $it") }
                    }.trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfilePreviewScreenPreview() {
    val sampleUser = User(
        displayName = "María Pérez",
        email = "maria@example.com",
        apartmentNumber = "101",
        contactNumber = "0991234567",
        aboutMe = "Me gusta el arte y la lectura.",
        profileImageUrl = null
    )
    UserProfilePreviewScreen(
        uiState = UserProfilePreviewUiState(userProfile = sampleUser, isLoading = false),
        onNavigateBack = {}
    )
}