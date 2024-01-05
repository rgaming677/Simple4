package com.example.simple4.livedata;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.simple4.database.NoteDatabase;
import com.example.simple4.database.dao.NoteDao;
import com.example.simple4.database.entity.NoteEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NoteRepository {

    private NoteDao noteDao;
    private LiveData<List<NoteEntity>> allNotes;
    private Executor executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(NoteEntity note) {
        executor.execute(() -> noteDao.insert(note));
    }

    public void update(NoteEntity note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void delete(NoteEntity note) {
        executor.execute(() -> {
            noteDao.delete(note);
        });
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public LiveData<NoteEntity> getNoteById(int noteId) {
        return noteDao.getNoteById(noteId);
    }
}

