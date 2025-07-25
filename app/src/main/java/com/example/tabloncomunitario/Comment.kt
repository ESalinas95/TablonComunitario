package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity // Importar Entity
import androidx.room.PrimaryKey // Importar PrimaryKey
import androidx.room.ForeignKey // Importar ForeignKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Announcement::class,
            parentColumns = ["id"],
            childColumns = ["announcementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ])
@Parcelize
data class Comment(
    @PrimaryKey(autoGenerate = true) // <--- Define 'id' como clave primaria auto-generada
    var id: Long = 0, // Id ahora es Long y auto-generado por Room
    var announcementId: String = "", // FK al anuncio
    var authorId: String = "",       // FK al usuario
    var authorDisplayName: String = "",
    var authorProfileImageUrl: String? = null,
    var text: String = "",
    var timestamp: Long? = null // <--- Cambiado de Date a Long
) : Parcelable {
    constructor() : this(0, "", "", "", null, "", null) // Ajusta el constructor
}