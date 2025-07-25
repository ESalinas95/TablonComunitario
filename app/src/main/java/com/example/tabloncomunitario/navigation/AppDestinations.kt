// navigation/AppDestinations.kt
package com.example.tabloncomunitario.navigation // O tu paquete utils

// Objeto para definir las rutas principales de la aplicación
object AppDestinations {
    const val AUTH_ROUTE = "auth_route" // Ruta para la pantalla de Login/Registro
    const val SETUP_PROFILE_ROUTE = "setup_profile_route" // Ruta para la configuración inicial de perfil
    const val MAIN_ROUTE = "main_route" // Ruta para la pantalla principal de anuncios
    const val PROFILE_ROUTE = "profile_route" // Ruta para la pantalla de Mi Perfil
    const val EDIT_PROFILE_ROUTE = "edit_profile_route" // Ruta para la pantalla de Editar Perfil
    const val ADD_ANNOUNCEMENT_ROUTE = "add_announcement_route" // Ruta para crear/editar anuncio
    const val DETAIL_ANNOUNCEMENT_ROUTE = "detail_announcement_route" // Ruta para detalles de anuncio
    const val USER_PROFILE_PREVIEW_ROUTE = "user_profile_preview_route" // Ruta para previsualización de perfil de usuario
}

// Para rutas con argumentos, también se pueden definir aquí (ej. "detail/{announcementId}")
// y luego construir la ruta con los argumentos.