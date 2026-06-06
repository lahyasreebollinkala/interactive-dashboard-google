package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.EducationMetric

@Database(entities = [EducationMetric::class], version = 1, exportSchema = false)
abstract class EducationDatabase : RoomDatabase() {
    abstract fun educationDao(): EducationDao

    companion object {
        @Volatile
        private var INSTANCE: EducationDatabase? = null

        fun getDatabase(context: Context): EducationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EducationDatabase::class.java,
                    "education_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
