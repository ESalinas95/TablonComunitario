package com.example.tabloncomunitario.repository

import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.database.UserDao

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