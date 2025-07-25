package com.example.tabloncomunitario.database // Asegúrate de que el paquete sea correcto

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.Comment

@Database(entities = [User::class, Announcement::class, Comment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Define los DAOs que Room debe proporcionar
    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile // Hace que la instancia sea inmediatamente visible para otros hilos
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia es nula, crea la base de datos
            return INSTANCE ?: synchronized(this) { // Bloquea para asegurar que solo un hilo cree la instancia
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Usa applicationContext para evitar fugas de memoria
                    AppDatabase::class.java,
                    "tablon_comunitario_db" // Nombre del archivo de la base de datos
                )
                    // .fallbackToDestructiveMigration() // Opcional: Para desarrollo, destruye y recrea la DB en migraciones
                    // En producción, necesitarías una estrategia de migración más robusta
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}