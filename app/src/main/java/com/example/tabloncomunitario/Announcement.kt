package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "announcements",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ])
@Parcelize
data class Announcement(
    @PrimaryKey
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var authorId: String = "",
    var authorEmail: String = "",
    var authorDisplayName: String = "",
    var authorProfileImageUrl: String? = null,
    var imageUrl: String? = null,
    var timestamp: Long? = null
) : Parcelable {
    constructor() : this("", "", "", "", "", "", null, null, null)
}