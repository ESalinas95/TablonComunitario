package com.example.tabloncomunitario.ui.auth

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.Comment
import com.example.tabloncomunitario.viewmodel.DetailAnnouncementUiState
import com.example.tabloncomunitario.R
import com.example.tabloncomunitario.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAnnouncementScreen(
    uiState: DetailAnnouncementUiState,
    onCommentInputChange: (String) -> Unit,
    onSendCommentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCommentAuthorClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Anuncio") },
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
                .padding(16.dp)
        ) {
            uiState.announcement?.let { announcement ->
                // Título del Anuncio
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Autor y Fecha
                Text(
                    text = "Publicado por: ${announcement.authorDisplayName.ifEmpty { announcement.authorEmail }} el ${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                            Date(announcement.timestamp ?: 0L)
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Imagen del Anuncio
                if (!announcement.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(announcement.imageUrl),
                        contentDescription = "Imagen del anuncio",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Descripción Completa
                Text(
                    text = announcement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Acciones del Autor (Editar/Eliminar)
                if (uiState.isAuthor) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onEditClick,
                            enabled = uiState.canEditDelete, // Habilitar/deshabilitar basado en si tiene comentarios
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("EDITAR")
                        }
                        Button(
                            onClick = onDeleteClick,
                            enabled = uiState.canEditDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text("ELIMINAR")
                        }
                    }
                    if (!uiState.canEditDelete) {
                        Text(
                            text = "Este anuncio tiene comentarios y no puede ser editado/eliminado.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Sección de Comentarios
                Text(
                    text = "Comentarios",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Lista de Comentarios
                if (uiState.comments.isEmpty()) {
                    Text(
                        text = "Sé el primero en comentar.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.comments.forEach { comment ->
                            CommentItem(comment = comment, onCommentAuthorClick = onCommentAuthorClick)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Campo para añadir comentario
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.commentInput,
                        onValueChange = onCommentInputChange,
                        label = { Text("Escribe un comentario...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(1f),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onSendCommentClick, enabled = uiState.commentInput.isNotEmpty()) {
                        Icon(painterResource(id = R.drawable.ic_menu_send), contentDescription = "Enviar comentario")
                    }
                }
            } ?: run {
                Text("Anuncio no encontrado.", style = MaterialTheme.typography.headlineSmall)
            }


            // Indicador de carga y mensaje de estado
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
            }
            uiState.statusMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, onCommentAuthorClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCommentAuthorClick(comment.authorId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (comment.authorProfileImageUrl != null) {
                    rememberAsyncImagePainter(comment.authorProfileImageUrl)
                } else {
                    painterResource(id = R.drawable.ic_default_profile)
                },
                contentDescription = "Foto de perfil del autor del comentario",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorDisplayName.ifEmpty { comment.authorId },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(comment.timestamp ?: 0L)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(comment.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DetailAnnouncementScreenPreview() {
    val sampleAnnouncement = Announcement(
        id = "a1",
        title = "Asado Comunitario",
        description = "Hola vecinos, mañana asado adasdsadsadsdasadsdadasdadadasdad...",
        authorDisplayName = "Eduardo Salinas",
        authorEmail = "eduardo@gmail.com",
        timestamp = System.currentTimeMillis()
    )
    val sampleComments = listOf(
        Comment(text = "Genial! Allí estaremos.", authorDisplayName = "Juanita", timestamp = System.currentTimeMillis() - 100000),
        Comment(text = "No me lo pierdo.", authorDisplayName = "Pedro", timestamp = System.currentTimeMillis() - 50000)
    )

    DetailAnnouncementScreen(
        uiState = DetailAnnouncementUiState(
            announcement = sampleAnnouncement,
            comments = sampleComments,
            commentInput = "Un nuevo comentario",
            isLoading = false,
            isAuthor = true,
            canEditDelete = true
        ),
        onCommentInputChange = {},
        onSendCommentClick = {},
        onEditClick = {},
        onDeleteClick = {},
        onCommentAuthorClick = {},
        onNavigateBack = {}
    )
}