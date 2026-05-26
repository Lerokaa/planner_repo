package com.example.planner;

import androidx.lifecycle.LiveData;  // ← Добавлен
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    LiveData<List<Note>> getAllNotes();  // Теперь работает ✅

    @Query("SELECT * FROM notes WHERE id = :id")
    LiveData<Note> getNoteByIdLiveData(long id);
}