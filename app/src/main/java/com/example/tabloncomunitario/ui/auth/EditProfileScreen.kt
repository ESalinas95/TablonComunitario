package com.example.tabloncomunitario.ui.auth

import androidx.compose.foundation.Image
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
import com.example.tabloncomunitario.viewmodel.EditProfileUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    uiState: EditProfileUiState,
    onContactNumberChange: (String) -> Unit,
    onApartmentNumberChange: (String) -> Unit,
    onAboutMeChange: (String) -> Unit,
    onSaveProfileClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = rememberAsyncImagePainter(uiState.userProfile?.profileImageUrl)

            Image(
                painter = painter,
                contentDescription = "Foto de Perfil",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                ,contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.displayNameInput,
                onValueChange = {}, // No editable
                label = { Text("Tu Nombre a Mostrar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.contactNumberInput,
                onValueChange = onContactNumberChange,
                label = { Text("Número de Contacto (Ej. Celular)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.documentNumberInput,
                onValueChange = {}, // No editable
                label = { Text("Número de Documento/Identificación") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.apartmentNumberInput,
                onValueChange = onApartmentNumberChange,
                label = { Text("Número de Departamento/Casa") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.aboutMeInput,
                onValueChange = onAboutMeChange,
                label = { Text("Cuéntanos un poco sobre ti...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                enabled = !uiState.isLoading,
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
                Text("Guardar Cambios")
            }

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
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    val sampleUser = User(
        displayName = "Pedro Gomez",
        contactNumber = "0981512442",
        documentNumber = "23156159",
        apartmentNumber = "512",
        aboutMe = "Nada",
        profileImageUrl = null
    )
    EditProfileScreen(
        uiState = EditProfileUiState(
            userProfile = sampleUser,
            displayNameInput = sampleUser.displayName,
            contactNumberInput = sampleUser.contactNumber ?: "",
            documentNumberInput = sampleUser.documentNumber ?: "",
            apartmentNumberInput = sampleUser.apartmentNumber ?: "",
            aboutMeInput = sampleUser.aboutMe ?: ""
        ),
        onContactNumberChange = {}, onApartmentNumberChange = {}, onAboutMeChange = {},
        onSaveProfileClick = {}, onNavigateBack = {}
    )
}