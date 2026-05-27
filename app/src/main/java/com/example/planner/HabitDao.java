package com.example.planner;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {

    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits")
    List<Habit> getAll();

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    Habit getById(long id);

    @Query("SELECT * FROM habits ORDER BY id DESC")
    List<Habit> getAllHabits();

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    Habit getHabitById(long id);
}