package com.example.tabloncomunitario.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.R
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.ui.components.AnnouncementCard
import com.example.tabloncomunitario.viewmodel.ProfileUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onAnnouncementClick: (Announcement) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") }, // Título de la pantalla
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de Header/Información del Usuario
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Foto de Perfil
                    Image(
                        painter = if (uiState.userProfile?.profileImageUrl != null) {
                            rememberAsyncImagePainter(uiState.userProfile.profileImageUrl)
                        } else {
                            painterResource(id = R.drawable.ic_default_profile)
                        },
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    // Nombre a Mostrar
                    Text(
                        text = uiState.userProfile?.displayName.takeIf { it?.isNotEmpty() == true } ?: uiState.userProfile?.email ?: "Usuario Desconocido",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(Modifier.height(4.dp))

                    // Información Adicional (Email, Contacto, Dpto, Acerca de)
                    Text(
                        text = buildString {
                            if (uiState.userProfile?.displayName != uiState.userProfile?.email) {
                                append("Email: ${uiState.userProfile?.email ?: "N/A"}\n")
                            }
                            uiState.userProfile?.contactNumber?.let { append("Contacto: $it\n") }
                            uiState.userProfile?.documentNumber?.let { append("Documento: $it\n") }
                            uiState.userProfile?.apartmentNumber?.let { append("Depto: $it\n") }
                            uiState.userProfile?.aboutMe?.let { append("Acerca de mí: $it") }
                        }.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Botón Editar Perfil
                    Button(
                        onClick = onEditProfileClick,
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("EDITAR PERFIL", color = Color.White)
                    }
                }
            }

            // Sección de Mis Anuncios
            Text(
                text = "Mis Anuncios",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            // Lista de Mis Anuncios
            if (uiState.myAnnouncements.isEmpty()) {
                Text(
                    text = "Aún no has publicado ningún anuncio.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.myAnnouncements) { announcement ->
                        AnnouncementCard(announcement = announcement, onAnnouncementClick = onAnnouncementClick)
                    }
                }
            }

            // Mensaje de estado o progreso
            if (uiState.statusMessage != null && uiState.statusMessage.isNotEmpty()) {
                Text(uiState.statusMessage, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val sampleUser = User(
        displayName = "Eduardo Salinas",
        email = "eduardo@gmail.com",
        contactNumber = "0981512442",
        documentNumber = "23156159",
        apartmentNumber = "512",
        aboutMe = "Persona agresiva sin ganas de socializar",
        profileImageUrl = null
    )
    val sampleAnnouncements = listOf(
        Announcement(id = "1", title = "Bienvenida", description = "Hola vecinos...", timestamp = System.currentTimeMillis()),
        Announcement(id = "2", title = "Asado", description = "Asadito mañana...", timestamp = System.currentTimeMillis())
    )

    ProfileScreen(
        uiState = ProfileUiState(
            userProfile = sampleUser,
            myAnnouncements = sampleAnnouncements,
            isLoading = false
        ),
        onEditProfileClick = {},
        onAnnouncementClick = {},
        onNavigateBack = {}
    )
}

@Composable
fun AnnouncementCard(announcement: Announcement, onAnnouncementClick: (Announcement) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onAnnouncementClick(announcement) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = if (announcement.authorProfileImageUrl != null) {
                        rememberAsyncImagePainter(announcement.authorProfileImageUrl)
                    } else {
                        painterResource(id = R.drawable.ic_default_profile)
                    },
                    contentDescription = "Foto de perfil del autor",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(announcement.authorDisplayName.ifEmpty { announcement.authorEmail }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(announcement.timestamp ?: 0L)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Text(announcement.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(announcement.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}