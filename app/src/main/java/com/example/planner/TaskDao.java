package com.example.planner;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY date ASC, startTime ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(long id);

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    void updateCompletion(long taskId, boolean completed);

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY startTime ASC")
    LiveData<List<Task>> getTasksByDate(long date);
}