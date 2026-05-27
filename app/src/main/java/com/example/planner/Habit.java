package com.example.planner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public String description;

    public int color;

    public String schedule;

    public long time;

    public boolean hasReminder;

    public long reminderTime;

    public int targetDays;

    public int completedDays;

    public int streak;

    public int bestStreak;

    public boolean completedToday;
}