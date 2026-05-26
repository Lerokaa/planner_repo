package com.example.planner;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<Note> getNoteById(long id) {
        return noteDao.getNoteByIdLiveData(id);
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> noteDao.delete(note));
    }
}