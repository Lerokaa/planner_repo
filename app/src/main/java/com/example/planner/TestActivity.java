package com.example.planner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private AppDatabase db;
    private NoteDao noteDao;
    private TaskDao taskDao;
    private ReminderDao reminderDao;
    private TextView tvOutput;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        tvOutput = findViewById(R.id.tv_output);

        db = AppDatabase.getInstance(this);
        noteDao = db.noteDao();
        taskDao = db.taskDao();
        reminderDao = db.reminderDao();

        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.btn_create_note).setOnClickListener(v -> showCreateNoteDialog());
        findViewById(R.id.btn_update_note).setOnClickListener(v -> updateNote());
        findViewById(R.id.btn_delete_note).setOnClickListener(v -> deleteNote());
        findViewById(R.id.btn_get_all_notes).setOnClickListener(v -> getAllNotes());
        findViewById(R.id.btn_get_note_by_id).setOnClickListener(v -> getNoteById());

        // TASK FUNCTIONS
        findViewById(R.id.btn_create_task).setOnClickListener(v -> createTask());
        findViewById(R.id.btn_update_task).setOnClickListener(v -> updateTask());
        findViewById(R.id.btn_delete_task).setOnClickListener(v -> deleteTask());
        findViewById(R.id.btn_get_all_tasks).setOnClickListener(v -> getAllTasks());

        // REMINDER FUNCTIONS
        findViewById(R.id.btn_create_reminder).setOnClickListener(v -> createReminder());
        findViewById(R.id.btn_get_reminders_for_task).setOnClickListener(v -> getRemindersForTask());
        findViewById(R.id.btn_update_reminder).setOnClickListener(v -> updateReminder());
        findViewById(R.id.btn_delete_reminder).setOnClickListener(v -> deleteReminder());
        findViewById(R.id.btn_get_active_time_reminders).setOnClickListener(v -> getActiveTimeReminders());
        findViewById(R.id.btn_get_active_location_reminders).setOnClickListener(v -> getActiveLocationReminders());
        findViewById(R.id.btn_delete_all_reminders_for_task).setOnClickListener(v -> deleteAllRemindersForTask());

        // UTILITY
        findViewById(R.id.btn_clear_output).setOnClickListener(v -> clearOutput());
    }

    private void showCreateNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📝 Создание заметки");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        TextView titleLabel = new TextView(this);
        titleLabel.setText("Заголовок:");
        titleLabel.setTextSize(14);
        titleLabel.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(titleLabel);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Введите заголовок заметки");
        titleInput.setPadding(0, 10, 0, 20);
        layout.addView(titleInput);

        TextView contentLabel = new TextView(this);
        contentLabel.setText("Содержание:");
        contentLabel.setTextSize(14);
        contentLabel.setTextColor(getResources().getColor(android.R.color.black));
        contentLabel.setPadding(0, 20, 0, 0);
        layout.addView(contentLabel);

        final EditText contentInput = new EditText(this);
        contentInput.setHint("Введите содержание заметки");
        contentInput.setPadding(0, 10, 0, 20);
        contentInput.setMinLines(3);
        layout.addView(contentInput);

        builder.setView(layout);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите заголовок заметки", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(this, "Введите содержание заметки", Toast.LENGTH_SHORT).show();
                return;
            }

            createNote(title, content);
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void createNote(String title, String content) {
        new Thread(() -> {
            Note note = new Note();
            note.title = title;
            note.content = content;
            note.lastModified = System.currentTimeMillis();

            noteDao.insert(note);

            runOnUiThread(() -> {
                appendOutput(" СОЗДАНА ЗАМЕТКА");
                appendOutput("   ID: " + note.id);
                appendOutput("   Заголовок: " + note.title);
                appendOutput("   Содержание: " + note.content);
                appendOutput("   Дата создания: " + sdf.format(new Date(note.lastModified)));
                appendOutput("");
                Toast.makeText(this, "Заметка создана", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void updateNote() {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            if (notes.isEmpty()) {
                runOnUiThread(() -> appendOutput("НЕТ ЗАМЕТОК ДЛЯ ОБНОВЛЕНИЯ\n"));
                return;
            }

            Note note = notes.get(0);
            String oldTitle = note.title;
            note.title = "ОБНОВЛЕНО: " + oldTitle;
            note.lastModified = System.currentTimeMillis();
            noteDao.update(note);

            runOnUiThread(() -> {
                appendOutput("ЗАМЕТКА ОБНОВЛЕНА");
                appendOutput("   ID: " + note.id);
                appendOutput("   Было: " + oldTitle);
                appendOutput("   Стало: " + note.title);
                appendOutput("   Дата изменения: " + sdf.format(new Date(note.lastModified)));
                appendOutput("");
            });
        }).start();
    }

    private void deleteNote() {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            if (notes.isEmpty()) {
                runOnUiThread(() -> appendOutput("НЕТ ЗАМЕТОК ДЛЯ УДАЛЕНИЯ\n"));
                return;
            }

            Note note = notes.get(0);
            noteDao.delete(note);

            runOnUiThread(() -> {
                appendOutput("ЗАМЕТКА УДАЛЕНА");
                appendOutput("   ID: " + note.id);
                appendOutput("   Заголовок: " + note.title);
                appendOutput("");
            });
        }).start();
    }

    private void getAllNotes() {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            runOnUiThread(() -> {
                appendOutput("📝 ВСЕ ЗАМЕТКИ (" + notes.size() + " шт.)");
                if (notes.isEmpty()) {
                    appendOutput("   Нет заметок");
                } else {
                    for (Note note : notes) {
                        appendOutput("   • ID:" + note.id + " | " + note.title);
                        appendOutput("     Содержание: " + (note.content.length() > 50 ? note.content.substring(0, 50) + "..." : note.content));
                        appendOutput("     Изменено: " + sdf.format(new Date(note.lastModified)));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void getNoteById() {
        new Thread(() -> {
            Note newNote = new Note();
            newNote.title = "Тестовая заметка для поиска";
            newNote.content = "Содержимое для поиска по ID";
            newNote.lastModified = System.currentTimeMillis();
            noteDao.insert(newNote);

            Note found = noteDao.getNoteById(newNote.id);

            runOnUiThread(() -> {
                appendOutput(" ПОИСК ЗАМЕТКИ ПО ID: " + newNote.id);
                if (found != null) {
                    appendOutput("НАЙДЕНО:");
                    appendOutput("   ID: " + found.id);
                    appendOutput("   Заголовок: " + found.title);
                    appendOutput("   Содержание: " + found.content);
                    appendOutput("   Дата создания: " + sdf.format(new Date(found.lastModified)));
                } else {
                    appendOutput("ЗАМЕТКА НЕ НАЙДЕНА");
                }
                appendOutput("");
            });
        }).start();
    }


    private void createTask() {
        new Thread(() -> {
            Task task = new Task();
            task.title = "Задача " + sdf.format(new Date());
            task.content = "Описание задачи";
            task.date = System.currentTimeMillis();
            task.time = System.currentTimeMillis();
            task.isCompleted = false;

            taskDao.insert(task);

            runOnUiThread(() -> {
                appendOutput(" СОЗДАНА ЗАДАЧА");
                appendOutput("   ID: " + task.id);
                appendOutput("   Название: " + task.title);
                appendOutput("   Выполнена: " + (task.isCompleted ? "Да" : "Нет"));
                appendOutput("");
                Toast.makeText(this, "Задача создана", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void updateTask() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput("НЕТ ЗАДАЧ ДЛЯ ОБНОВЛЕНИЯ\n"));
                return;
            }

            Task task = tasks.get(0);
            task.title = "ОБНОВЛЕНО: " + task.title;
            task.isCompleted = !task.isCompleted;
            taskDao.update(task);

            runOnUiThread(() -> {
                appendOutput(" ЗАДАЧА ОБНОВЛЕНА");
                appendOutput("   ID: " + task.id);
                appendOutput("   Новое название: " + task.title);
                appendOutput("   Статус выполнения: " + (task.isCompleted ? "Выполнена " : "Не выполнена "));
                appendOutput("");
            });
        }).start();
    }

    private void deleteTask() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput(" НЕТ ЗАДАЧ ДЛЯ УДАЛЕНИЯ\n"));
                return;
            }

            Task task = tasks.get(0);
            reminderDao.deleteByTaskId(task.id);
            taskDao.delete(task);

            runOnUiThread(() -> {
                appendOutput("ЗАДАЧА И ЕЁ НАПОМИНАНИЯ УДАЛЕНЫ");
                appendOutput("   ID: " + task.id);
                appendOutput("   Название: " + task.title);
                appendOutput("");
            });
        }).start();
    }

    private void getAllTasks() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            runOnUiThread(() -> {
                appendOutput("ВСЕ ЗАДАЧИ (" + tasks.size() + " шт.)");
                if (tasks.isEmpty()) {
                    appendOutput("   Нет задач");
                } else {
                    for (Task task : tasks) {
                        appendOutput("   • ID:" + task.id + " | " + task.title);
                        appendOutput("     Статус: " + (task.isCompleted ? "Выполнена" : "Не выполнена"));
                        appendOutput("     Дата: " + (task.date != null ? sdf.format(new Date(task.date)) : "не указана"));
                    }
                }
                appendOutput("");
            });
        }).start();
    }


    private void createReminder() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput(" СНАЧАЛА СОЗДАЙТЕ ЗАДАЧУ\n"));
                return;
            }

            Task task = tasks.get(0);
            int count = reminderDao.getCountForTask(task.id);

            if (count >= 5) {
                runOnUiThread(() -> {
                    appendOutput(" ЛИМИТ! У задачи уже есть " + count + " напоминаний (макс. 5)");
                    appendOutput("");
                });
                return;
            }

            Reminder reminder = new Reminder();
            reminder.taskId = task.id;
            reminder.type = "TIME";
            reminder.triggerTime = System.currentTimeMillis() + 60000;
            reminder.isTriggered = false;

            long id = reminderDao.insert(reminder);

            runOnUiThread(() -> {
                appendOutput(" СОЗДАНО НАПОМИНАНИЕ");
                appendOutput("   ID: " + id);
                appendOutput("   Для задачи ID: " + task.id + " (" + task.title + ")");
                appendOutput("   Тип: TIME");
                appendOutput("   Сработает: " + sdf.format(new Date(reminder.triggerTime)));
                appendOutput("   Напоминаний у задачи: " + (count + 1) + "/5");
                appendOutput("");
            });
        }).start();
    }

    private void getRemindersForTask() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput("НЕТ ЗАДАЧ\n"));
                return;
            }

            Task task = tasks.get(0);
            List<Reminder> reminders = reminderDao.getRemindersForTask(task.id);

            runOnUiThread(() -> {
                appendOutput("НАПОМИНАНИЯ ЗАДАЧИ: " + task.title + " (ID: " + task.id + ")");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   • ID:" + r.id + " | Тип: " + r.type);
                        if ("TIME".equals(r.type)) {
                            appendOutput("     Время: " + sdf.format(new Date(r.triggerTime)));
                        }
                        appendOutput("     Выполнено: " + (r.isTriggered ? " Да" : " Нет"));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void updateReminder() {
        new Thread(() -> {
            List<Reminder> reminders = reminderDao.getActiveTimeReminders();
            if (reminders.isEmpty()) {
                runOnUiThread(() -> appendOutput(" НЕТ АКТИВНЫХ TIME-НАПОМИНАНИЙ\n"));
                return;
            }

            Reminder reminder = reminders.get(0);
            reminder.isTriggered = true;
            reminderDao.update(reminder);

            runOnUiThread(() -> {
                appendOutput("НАПОМИНАНИЕ ОТМЕЧЕНО КАК ВЫПОЛНЕННОЕ");
                appendOutput("   ID: " + reminder.id);
                appendOutput("   Тип: " + reminder.type);
                appendOutput("   isTriggered = " + reminder.isTriggered);
                appendOutput("");
            });
        }).start();
    }

    private void deleteReminder() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput(" НЕТ ЗАДАЧ\n"));
                return;
            }

            List<Reminder> reminders = reminderDao.getRemindersForTask(tasks.get(0).id);
            if (reminders.isEmpty()) {
                runOnUiThread(() -> appendOutput(" НЕТ НАПОМИНАНИЙ ДЛЯ УДАЛЕНИЯ\n"));
                return;
            }

            Reminder reminder = reminders.get(0);
            reminderDao.delete(reminder);

            runOnUiThread(() -> {
                appendOutput(" НАПОМИНАНИЕ УДАЛЕНО");
                appendOutput("   ID: " + reminder.id);
                appendOutput("");
            });
        }).start();
    }

    private void getActiveTimeReminders() {
        new Thread(() -> {
            List<Reminder> reminders = reminderDao.getActiveTimeReminders();
            runOnUiThread(() -> {
                appendOutput(" АКТИВНЫЕ TIME-НАПОМИНАНИЯ (" + reminders.size() + " шт.)");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет активных напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   • ID:" + r.id + " | Задача ID:" + r.taskId);
                        appendOutput("     Сработает: " + sdf.format(new Date(r.triggerTime)));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void getActiveLocationReminders() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (!tasks.isEmpty()) {
                Reminder locationReminder = new Reminder();
                locationReminder.taskId = tasks.get(0).id;
                locationReminder.type = "LOCATION";
                locationReminder.lat = 55.7558;
                locationReminder.lng = 37.6176;
                locationReminder.radius = 500.0f;
                locationReminder.isTriggered = false;
                reminderDao.insert(locationReminder);
            }

            List<Reminder> reminders = reminderDao.getActiveLocationReminders();
            runOnUiThread(() -> {
                appendOutput(" АКТИВНЫЕ LOCATION-НАПОМИНАНИЯ (" + reminders.size() + " шт.)");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет активных location-напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   • ID:" + r.id + " | Задача ID:" + r.taskId);
                        appendOutput("     Координаты: (" + r.lat + ", " + r.lng + ")");
                        appendOutput("     Радиус: " + r.radius + " метров");
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void deleteAllRemindersForTask() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            if (tasks.isEmpty()) {
                runOnUiThread(() -> appendOutput(" НЕТ ЗАДАЧ\n"));
                return;
            }

            Task task = tasks.get(0);
            int count = reminderDao.getCountForTask(task.id);
            reminderDao.deleteByTaskId(task.id);

            runOnUiThread(() -> {
                appendOutput(" УДАЛЕНЫ ВСЕ НАПОМИНАНИЯ ЗАДАЧИ");
                appendOutput("   Задача: " + task.title + " (ID: " + task.id + ")");
                appendOutput("   Удалено напоминаний: " + count);
                appendOutput("");
            });
        }).start();
    }

    private void appendOutput(String text) {
        tvOutput.append(text + "\n");
        if (tvOutput.getLineCount() > 200) {
            String currentText = tvOutput.getText().toString();
            int newlineIndex = currentText.indexOf("\n", currentText.length() / 2);
            if (newlineIndex != -1) {
                tvOutput.setText(currentText.substring(newlineIndex + 1));
            }
        }
    }

    private void clearOutput() {
        tvOutput.setText("=== НАЧАЛО НОВОГО ТЕСТИРОВАНИЯ ===\n\n");
        Toast.makeText(this, "Вывод очищен", Toast.LENGTH_SHORT).show();
    }
}