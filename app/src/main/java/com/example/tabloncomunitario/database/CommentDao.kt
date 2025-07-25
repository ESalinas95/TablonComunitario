package com.example.tabloncomunitario.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.tabloncomunitario.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllComments(comments: List<Comment>)

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: Long)

    @Query("SELECT * FROM comments WHERE announcementId = :announcementId ORDER BY timestamp ASC")
    fun getCommentsForAnnouncement(announcementId: String): Flow<List<Comment>>

    @Query("DELETE FROM comments WHERE announcementId = :announcementId")
    suspend fun deleteCommentsForAnnouncement(announcementId: String)

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}