package com.example.tabloncomunitario.ui.auth

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tabloncomunitario.R
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.viewmodel.SetupProfileUiState // Importar SetupProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    uiState: SetupProfileUiState,
    onDisplayNameChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onApartmentNumberChange: (String) -> Unit,
    onAboutMeChange: (String) -> Unit,
    onSelectImageClick: () -> Unit,
    onSaveProfileClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la pantalla (puedes añadir un TopAppBar si quieres la flecha de retroceso aquí)
        Text(
            text = "Completa tu Perfil",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- ELIMINADA: Sección de Foto de Perfil ---
        // Image(
        //     painter = if (uiState.profileImageUri != null) { /* ... */ } else { /* ... */ },
        //     contentDescription = "Foto de Perfil",
        //     modifier = Modifier.size(120.dp).clip(CircleShape).clickable(enabled = !uiState.isLoading) { onSelectImageClick() },
        //     contentScale = ContentScale.Crop
        // )
        // Spacer(Modifier.height(8.dp))
        // Button(
        //     onClick = onSelectImageClick,
        //     enabled = !uiState.isLoading,
        //     modifier = Modifier.fillMaxWidth().height(48.dp)
        // ) { Text("Seleccionar Foto") }
        // Spacer(Modifier.height(24.dp))
        // --- FIN ELIMINADA ---

        // Campos de texto (mantener paddingTop si se eliminan elementos de arriba)
        OutlinedTextField(
            value = uiState.displayNameInput,
            onValueChange = onDisplayNameChange,
            label = { Text("Tu Nombre a Mostrar") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        // ... (resto de campos de texto y botón de guardar - sin cambios) ...
        OutlinedTextField(
            value = uiState.contactNumberInput,
            onValueChange = onContactNumberChange,
            label = { Text("Número de Contacto (Ej. Celular)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.documentNumberInput,
            onValueChange = onDocumentNumberChange,
            label = { Text("Número de DNI/Identificación") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.apartmentNumberInput,
            onValueChange = onApartmentNumberChange,
            label = { Text("Número de Departamento/Casa") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.aboutMeInput,
            onValueChange = onAboutMeChange,
            label = { Text("Cuéntanos un poco sobre ti...") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5,
            minLines = 3
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSaveProfileClick,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Guardar y Continuar")
        }

        // Mensaje de estado o progreso
        if (uiState.statusMessage != null && uiState.statusMessage.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(uiState.statusMessage, color = MaterialTheme.colorScheme.error)
        }

        if (uiState.isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    }
}

// Preview para Android Studio (actualiza para que no necesite profileImageUri)
@Preview(showBackground = true)
@Composable
fun SetupProfileScreenPreview() {
    val sampleUser = User(
        displayName = "Pedro Gomez",
        contactNumber = "0981123456",
        documentNumber = "1234567",
        apartmentNumber = "5B",
        aboutMe = "Me gusta programar en Compose"
        // profileImageUrl ya no es parte de la preview UiState aquí
    )
    SetupProfileScreen(
        uiState = SetupProfileUiState(
            displayNameInput = sampleUser.displayName,
            contactNumberInput = sampleUser.contactNumber ?: "",
            documentNumberInput = sampleUser.documentNumber ?: "",
            apartmentNumberInput = sampleUser.apartmentNumber ?: "",
            aboutMeInput = sampleUser.aboutMe ?: ""
        ),
        onDisplayNameChange = {},
        onContactNumberChange = {},
        onDocumentNumberChange = {},
        onApartmentNumberChange = {},
        onAboutMeChange = {},
        onSelectImageClick = {},
        onSaveProfileClick = {}
    )
}