package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity // Importar Entity
import androidx.room.PrimaryKey // Importar PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users") // <--- Anotación Entity, define el nombre de la tabla
@Parcelize
data class User(
    @PrimaryKey // <--- Define 'uid' como clave primaria
    var uid: String = "",
    var displayName: String = "",
    var email: String = "",
    var contactNumber: String? = null,
    var documentNumber: String? = null,
    var apartmentNumber: String? = null,
    var aboutMe: String? = null,
    var profileImageUrl: String? = null
) : Parcelable {
    constructor() : this("", "", "", null, null, null, null, null)
}