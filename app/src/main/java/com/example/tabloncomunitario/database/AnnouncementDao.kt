package com.example.tabloncomunitario.database // Asegúrate de que el paquete sea correcto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.tabloncomunitario.Announcement // Importa tu clase Announcement
import kotlinx.coroutines.flow.Flow // Importa Flow para observar cambios

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

    // Para obtener todos los anuncios, ordenados por timestamp descendente
    // Usamos Flow para que Room emita actualizaciones cada vez que los datos cambien
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    // Para obtener los anuncios de un autor específico
    @Query("SELECT * FROM announcements WHERE authorId = :authorId ORDER BY timestamp DESC")
    fun getAnnouncementsByAuthor(authorId: String): Flow<List<Announcement>>

    @Query("DELETE FROM announcements")
    suspend fun deleteAllAnnouncements()
}