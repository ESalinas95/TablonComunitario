package com.example.tabloncomunitario.ui.auth

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.tabloncomunitario.viewmodel.AddAnnouncementUiState



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementScreen(
    uiState: AddAnnouncementUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSelectImageClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.headerText) },
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
            // Campo: Título del Anuncio
            OutlinedTextField(
                value = uiState.titleInput,
                onValueChange = onTitleChange,
                label = { Text("Título del Anuncio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            // Campo: Descripción del Anuncio
            OutlinedTextField(
                value = uiState.descriptionInput,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción del Anuncio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 16.dp), // Altura fija
                maxLines = 5,
                minLines = 3
            )

            // Vista previa de la imagen seleccionada
            if (uiState.selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uiState.selectedImageUri),
                    contentDescription = "Vista previa de la imagen del anuncio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.LightGray)
                        .clickable(enabled = !uiState.isLoading) { onSelectImageClick() }, // Hacer clicable para cambiar
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
            }

            // Botón para seleccionar imagen
            Button(
                onClick = onSelectImageClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp) // Altura estándar, sin padding aquí
            ) {
                Text("Seleccionar Imagen")
            }

            Spacer(Modifier.height(24.dp)) // Espacio después del botón de selección de imagen

            // Botón para publicar/guardar cambios
            Button(
                onClick = onPublishClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (uiState.headerText == "Editar Anuncio") "Guardar Cambios" else "Publicar Anuncio")
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
}

@Preview(showBackground = true)
@Composable
fun AddAnnouncementScreenPreview() {
    val sampleUiState = AddAnnouncementUiState(
        headerText = "Crear Nuevo Anuncio",
        titleInput = "Título de prueba",
        descriptionInput = "Descripción de prueba para el anuncio."
    )
    AddAnnouncementScreen(
        uiState = sampleUiState,
        onTitleChange = {}, onDescriptionChange = {}, onSelectImageClick = {},
        onPublishClick = {}, onNavigateBack = {}
    )
}