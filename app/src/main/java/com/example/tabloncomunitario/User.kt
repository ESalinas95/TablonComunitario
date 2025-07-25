package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users")
@Parcelize
data class User(
    @PrimaryKey
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