package com.example.planner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public String content;

    public Long date;

    public Long time;

    public boolean isCompleted;
}