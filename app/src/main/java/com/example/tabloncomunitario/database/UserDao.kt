package com.example.tabloncomunitario.database // Asegúrate de que el paquete sea correcto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tabloncomunitario.User // Importa tu clase User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Si el usuario ya existe, lo reemplaza
    suspend fun insertUser(user: User) // 'suspend' para Coroutines

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users LIMIT 1") // Útil si solo esperas un usuario o el usuario actual
    suspend fun getCurrentUser(): User?

    @Query("DELETE FROM users WHERE uid = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers() // Para limpiar la tabla
}