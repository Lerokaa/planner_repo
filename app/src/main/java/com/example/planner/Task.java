package com.example.planner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String description;

    // Дата задачи (timestamp начала дня)
    public long date;

    // Время в минутах от полуночи (например, 9:30 = 570)
    public int startTime;
    public int endTime; // -1 если не указано

    public boolean isCompleted;
    public int color; // цвет задачи

    // Вспомогательные методы
    public String getStartTimeString() {
        if (startTime < 0) return "";
        return String.format("%02d:%02d", startTime / 60, startTime % 60);
    }

    public String getEndTimeString() {
        if (endTime < 0) return "";
        return String.format("%02d:%02d", endTime / 60, endTime % 60);
    }
}