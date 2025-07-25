package com.example.tabloncomunitario.repository // Asegúrate de que el paquete sea correcto

import com.example.tabloncomunitario.Announcement // Importa tu clase Announcement
import com.example.tabloncomunitario.database.AnnouncementDao // Importa tu AnnouncementDao
import kotlinx.coroutines.flow.Flow // Importa Flow

class AnnouncementRepository(private val announcementDao: AnnouncementDao) {

    suspend fun insertAnnouncement(announcement: Announcement) {
        announcementDao.insertAnnouncement(announcement)
    }

    suspend fun insertAllAnnouncements(announcements: List<Announcement>) {
        announcementDao.insertAllAnnouncements(announcements)
    }

    suspend fun updateAnnouncement(announcement: Announcement) {
        announcementDao.updateAnnouncement(announcement)
    }

    suspend fun deleteAnnouncement(announcement: Announcement) {
        announcementDao.deleteAnnouncement(announcement)
    }

    suspend fun deleteAnnouncementById(announcementId: String) {
        announcementDao.deleteAnnouncementById(announcementId)
    }

    suspend fun getAnnouncementById(announcementId: String): Announcement? {
        return announcementDao.getAnnouncementById(announcementId)
    }

    fun getAllAnnouncements(): Flow<List<Announcement>> {
        return announcementDao.getAllAnnouncements()
    }

    fun getAnnouncementsByAuthor(authorId: String): Flow<List<Announcement>> {
        return announcementDao.getAnnouncementsByAuthor(authorId)
    }

    suspend fun deleteAllAnnouncements() {
        announcementDao.deleteAllAnnouncements()
    }
}