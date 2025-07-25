package com.example.tabloncomunitario.repository // Asegúrate de que el paquete sea correcto

import com.example.tabloncomunitario.Comment // Importa tu clase Comment
import com.example.tabloncomunitario.database.CommentDao // Importa tu CommentDao
import kotlinx.coroutines.flow.Flow // Importa Flow

class CommentRepository(private val commentDao: CommentDao) {

    suspend fun insertComment(comment: Comment) {
        commentDao.insertComment(comment)
    }

    suspend fun insertAllComments(comments: List<Comment>) {
        commentDao.insertAllComments(comments)
    }

    suspend fun updateComment(comment: Comment) {
        commentDao.updateComment(comment)
    }

    suspend fun deleteComment(comment: Comment) {
        commentDao.deleteComment(comment)
    }

    suspend fun deleteCommentById(commentId: Long) {
        commentDao.deleteCommentById(commentId)
    }

    fun getCommentsForAnnouncement(announcementId: String): Flow<List<Comment>> {
        return commentDao.getCommentsForAnnouncement(announcementId)
    }

    suspend fun deleteCommentsForAnnouncement(announcementId: String) {
        commentDao.deleteCommentsForAnnouncement(announcementId)
    }

    suspend fun deleteAllComments() {
        commentDao.deleteAllComments()
    }
}