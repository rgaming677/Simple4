package com.example.simple4.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.simple4.database.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);

    @Query("SELECT * FROM note_table ORDER BY datetime DESC")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM note_table WHERE id = :noteId")
    LiveData<NoteEntity> getNoteById(int noteId);
}
