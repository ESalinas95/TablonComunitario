package com.example.tabloncomunitario.repository // Asegúrate de que el paquete sea correcto

import com.example.tabloncomunitario.User // Importa tu clase User
import com.example.tabloncomunitario.database.UserDao // Importa tu UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
}