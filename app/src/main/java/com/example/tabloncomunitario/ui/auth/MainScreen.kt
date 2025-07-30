package com.example.tabloncomunitario.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.ui.components.AnnouncementCard
import com.example.tabloncomunitario.viewmodel.MainUiState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.tabloncomunitario.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onSearchQueryChange: (String) -> Unit,
    onFabClick: () -> Unit,
    onAnnouncementClick: (Announcement) -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.welcomeMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onProfileClick,
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp)
                        ) {
                            Text("Mi Perfil", color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                        TextButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp)
                        ) {
                            Text("Cerrar Sesión", color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Filled.Add, "Añadir nuevo anuncio")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Buscar anuncios...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (uiState.statusMessage != null && uiState.statusMessage.isNotEmpty()) {
                Text(
                    text = uiState.statusMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.filteredAnnouncements.isEmpty() && uiState.searchQuery.isEmpty()) { // <--- CAMBIO CLAVE AQUÍ
                Text(
                    text = "Aún no hay anuncios publicados.",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.filteredAnnouncements.isEmpty() && uiState.searchQuery.isNotEmpty()) { // <--- CAMBIO CLAVE AQUÍ
                Text(
                    text = "No se encontraron anuncios para '${uiState.searchQuery}'.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredAnnouncements) { announcement -> // <--- CAMBIO CLAVE: Muestra filteredAnnouncements
                        AnnouncementCard(announcement = announcement, onAnnouncementClick = onAnnouncementClick)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val sampleAnnouncements = listOf(
        Announcement(id = "a1", title = "Bienvenida al Barrio", description = "Hola vecinos, les doy la bienvenida a esta Hermosa vecindad...", authorDisplayName = "Juan Pérez", authorEmail = "juan@example.com", timestamp = System.currentTimeMillis()),
        Announcement(id = "a2", title = "Venta de Muebles", description = "Se vende sofá y mesa en buen estado...", authorDisplayName = "María García", authorEmail = "maria@example.com", timestamp = System.currentTimeMillis() - 86400000)
    )
    MainScreen(
        uiState = MainUiState(
            welcomeMessage = "Bienvenido, Juan!",
            allAnnouncements = sampleAnnouncements,
            filteredAnnouncements = sampleAnnouncements,
            searchQuery = "",
            isLoading = false
        ),
        onSearchQueryChange = {},
        onFabClick = {},
        onAnnouncementClick = {},
        onProfileClick = {},
        onLogoutClick = {}
    )
}