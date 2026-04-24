package com.example.planner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private TaskDao taskDao;
    private ReminderDao reminderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        taskDao = db.taskDao();
        reminderDao = db.reminderDao();

        // 🧪 тестовые данные
        createTaskWithId1();
        createReminderForTask1();

        loadTasks();
        loadReminders();
    }

    // --------------------
    // TASK
    // --------------------

    private void createTaskWithId1() {
        new Thread(() -> {

            Task task = new Task();

            task.title = "Подготовить отчет";
            task.content = "Написать первую главу";

            task.date = System.currentTimeMillis();
            task.time = System.currentTimeMillis();

            task.isCompleted = false;

            taskDao.insert(task);

            System.out.println("TASK CREATED id=" + task.id);

        }).start();
    }

    private void loadTasks() {
        new Thread(() -> {

            List<Task> tasks = taskDao.getAllTasks();

            runOnUiThread(() -> {
                for (Task t : tasks) {
                    System.out.println(
                            "TASK: " + t.id +
                                    " | " + t.title +
                                    " | " + t.content
                    );
                }
            });

        }).start();
    }

    // --------------------
    // REMINDER
    // --------------------

    private void createReminderForTask1() {
        new Thread(() -> {

            long taskId = 1;

            int count = reminderDao.getCountForTask(taskId);

            if (count >= 5) {
                System.out.println("LIMIT: max 5 reminders per task");
                return;
            }

            Reminder reminder = new Reminder();
            reminder.taskId = taskId;
            reminder.type = "TIME";

            // ⏰ через 10 минут
            reminder.triggerTime = System.currentTimeMillis() + 600000;

            reminder.lat = null;
            reminder.lng = null;
            reminder.radius = null;

            reminder.isTriggered = false;

            long id = reminderDao.insert(reminder);

            System.out.println("REMINDER CREATED id=" + id + " for taskId=1");

        }).start();
    }

    private void loadReminders() {
        new Thread(() -> {

            List<Reminder> reminders =
                    reminderDao.getActiveTimeReminders();

            runOnUiThread(() -> {
                for (Reminder r : reminders) {
                    System.out.println(
                            "REMINDER: id=" + r.id +
                                    " taskId=" + r.taskId +
                                    " | time=" + r.triggerTime +
                                    " | triggered=" + r.isTriggered
                    );
                }
            });

        }).start();
    }
}