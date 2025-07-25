package com.example.tabloncomunitario.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.tabloncomunitario.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnnouncements(announcements: List<Announcement>)

    @Update
    suspend fun updateAnnouncement(announcement: Announcement)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcements WHERE id = :announcementId")
    suspend fun deleteAnnouncementById(announcementId: String)

    @Query("SELECT * FROM announcements WHERE id = :announcementId")
    suspend fun getAnnouncementById(announcementId: String): Announcement?

    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements WHERE authorId = :authorId ORDER BY timestamp DESC")
    fun getAnnouncementsByAuthor(authorId: String): Flow<List<Announcement>>

    @Query("DELETE FROM announcements")
    suspend fun deleteAllAnnouncements()
}