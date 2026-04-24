package com.example.planner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long taskId;

    public String type;

    public Long triggerTime;

    public Double lat;
    public Double lng;
    public Float radius;

    public boolean isTriggered = false;
}