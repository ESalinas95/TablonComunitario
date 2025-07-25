package com.example.tabloncomunitario

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import kotlinx.parcelize.Parcelize

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
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var announcementId: String = "",
    var authorId: String = "",
    var authorDisplayName: String = "",
    var authorProfileImageUrl: String? = null,
    var text: String = "",
    var timestamp: Long? = null
) : Parcelable {
    constructor() : this(0, "", "", "", null, "", null) // Ajusta el constructor
}