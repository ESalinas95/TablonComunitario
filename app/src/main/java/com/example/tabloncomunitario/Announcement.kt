package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity // Importar Entity
import androidx.room.PrimaryKey // Importar PrimaryKey
import androidx.room.ForeignKey // Importar ForeignKey
import androidx.room.ColumnInfo // Opcional, para nombres de columna personalizados
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "announcements",
    foreignKeys = [
        ForeignKey(
            entity = User::class, // La entidad a la que hace referencia
            parentColumns = ["uid"], // La columna de la entidad padre
            childColumns = ["authorId"], // La columna en esta entidad que es la FK
            onDelete = ForeignKey.CASCADE // Qué hacer si el User padre es borrado
        )
    ])
@Parcelize
data class Announcement(
    @PrimaryKey // <--- Define 'id' como clave primaria
    var id: String = "",
    var title: String = "",
    var description: String = "",
    // Columnas para el autor (serán FK a la tabla 'users')
    var authorId: String = "",
    var authorEmail: String = "",
    var authorDisplayName: String = "",
    var authorProfileImageUrl: String? = null,
    var imageUrl: String? = null,
    // Room no soporta directamente Date, necesitaremos Type Converters (lo haremos luego)
    // Por ahora, para simplificar, podemos guardar timestamp como Long o String
    // Guardémoslo como Long (milisegundos desde la época)
    var timestamp: Long? = null // <--- Cambiado de Date a Long
) : Parcelable {
    constructor() : this("", "", "", "", "", "", null, null, null)
}