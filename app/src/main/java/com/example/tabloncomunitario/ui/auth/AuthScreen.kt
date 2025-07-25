// ui/auth/AuthScreen.kt
package com.example.tabloncomunitario.ui.auth

import androidx.compose.foundation.Image // Importa Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Importa painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tabloncomunitario.R // Importa tu R para los drawables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onRegisterClick: (String, String) -> Unit,
    onLoginClick: (String, String) -> Unit,
    statusMessage: String? = null,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- NUEVO: Logo encima del correo ---
        Image(
            painter = painterResource(id = R.drawable.ic_logo), // Usa el ID de tu nuevo logo
            contentDescription = "Logo de la Aplicación",
            modifier = Modifier.size(120.dp) // Ajusta el tamaño según necesites
        )

        Spacer(Modifier.height(32.dp)) // Espacio entre el logo y el campo de email
        // --- FIN NUEVO ---

        // Campo de correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Botón de Registrarse
        Button(
            onClick = { onRegisterClick(email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Registrarse")
        }

        Spacer(Modifier.height(16.dp))

        // Botón de Iniciar Sesión
        OutlinedButton(
            onClick = { onLoginClick(email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Iniciar Sesión")
        }

        // Mensaje de estado o progreso
        if (statusMessage != null && statusMessage.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(statusMessage, color = MaterialTheme.colorScheme.error)
        }

        if (isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onRegisterClick = { _, _ -> },
        onLoginClick = { _, _ -> },
        statusMessage = "Mensaje de ejemplo de error"
    )
}