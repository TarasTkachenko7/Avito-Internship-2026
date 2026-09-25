package com.example.avito_testing_2026_autum.notes.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC
    """)
    fun getNotesDesc(query: String = ""): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY createdAt ASC
    """)
    fun getNotesAsc(query: String = ""): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE id = :id
    """)
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("""
        DELETE FROM notes 
        WHERE id = :noteId
    """)
    suspend fun deleteNoteById(noteId: Long)

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

}