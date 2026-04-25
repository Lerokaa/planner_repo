package com.example.planner;

import androidx.room.*;

import java.util.List;

@Dao
public interface ReminderDao {

    // получить все reminder задачи
    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    List<Reminder> getRemindersForTask(long taskId);

    // посчитать сколько reminder у задачи (для твоего лимита 5)
    @Query("SELECT COUNT(*) FROM reminders WHERE taskId = :taskId")
    int getCountForTask(long taskId);

    // получить все активные TIME reminders (для AlarmManager)
    @Query("SELECT * FROM reminders WHERE type = 'TIME' AND isTriggered = 0")
    List<Reminder> getActiveTimeReminders();

    // получить все LOCATION reminders
    @Query("SELECT * FROM reminders WHERE type = 'LOCATION' AND isTriggered = 0")
    List<Reminder> getActiveLocationReminders();

    // вставка
    @Insert
    long insert(Reminder reminder);

    // обновление (например, isTriggered = true)
    @Update
    void update(Reminder reminder);

    // удаление
    @Delete
    void delete(Reminder reminder);

    // удалить все reminders задачи (например при удалении task)
    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    void deleteByTaskId(long taskId);
}