package com.example.avito_testing_2026_autum.app.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.avito_testing_2026_autum.notes.data.local.NoteDao
import com.example.avito_testing_2026_autum.notes.data.local.NoteEntity
import com.example.avito_testing_2026_autum.tasks.data.local.TaskDao
import com.example.avito_testing_2026_autum.tasks.data.local.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}