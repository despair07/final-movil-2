package com.example.notasapp.Database

import androidx.lifecycle.LiveData
import com.example.notasapp.Models.Note

class NotesRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun update(note: Note) {
        // Verificamos si el id es nulo, y si es así, no hacemos la actualización.
        val noteId = note.id ?: return
        noteDao.update(noteId, note.title, note.note)
    }
}
