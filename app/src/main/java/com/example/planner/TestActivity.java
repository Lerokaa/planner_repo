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

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.Calendar;

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
        findViewById(R.id.btn_update_note).setOnClickListener(v -> showUpdateNoteDialog());
        findViewById(R.id.btn_delete_note).setOnClickListener(v -> showDeleteNoteDialog());
        findViewById(R.id.btn_get_all_notes).setOnClickListener(v -> getAllNotes());
        findViewById(R.id.btn_get_note_by_id).setOnClickListener(v -> showGetNoteByIdDialog());

        findViewById(R.id.btn_create_task).setOnClickListener(v -> showCreateTaskDialog());
        findViewById(R.id.btn_complete_task).setOnClickListener(v -> showCompleteTaskDialog());
        findViewById(R.id.btn_update_task).setOnClickListener(v -> showUpdateTaskDialog());
        findViewById(R.id.btn_delete_task).setOnClickListener(v -> showDeleteTaskDialog());
        findViewById(R.id.btn_get_all_tasks).setOnClickListener(v -> getAllTasks());

        findViewById(R.id.btn_create_reminder).setOnClickListener(v -> showCreateReminderDialog());
        findViewById(R.id.btn_get_reminders_for_task).setOnClickListener(v -> showGetRemindersForTaskDialog());
        findViewById(R.id.btn_update_reminder).setOnClickListener(v -> showUpdateReminderDialog());
        findViewById(R.id.btn_delete_reminder).setOnClickListener(v -> showDeleteReminderDialog());
        findViewById(R.id.btn_get_active_time_reminders).setOnClickListener(v -> getActiveTimeReminders());
        findViewById(R.id.btn_get_active_location_reminders).setOnClickListener(v -> getActiveLocationReminders());
        findViewById(R.id.btn_delete_all_reminders_for_task).setOnClickListener(v -> showDeleteAllRemindersForTaskDialog());

        findViewById(R.id.btn_clear_output).setOnClickListener(v -> clearOutput());
    }

    private void showCreateNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Создание заметки");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        TextView titleLabel = new TextView(this);
        titleLabel.setText("Заголовок:");
        titleLabel.setTextSize(14);
        layout.addView(titleLabel);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Введите заголовок заметки");
        titleInput.setPadding(0, 10, 0, 20);
        layout.addView(titleInput);

        TextView contentLabel = new TextView(this);
        contentLabel.setText("Содержание:");
        contentLabel.setTextSize(14);
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
                appendOutput("СОЗДАНА ЗАМЕТКА");
                appendOutput("   Заголовок: " + note.title);
                appendOutput("   Содержание: " + note.content);
                appendOutput("   Дата создания: " + sdf.format(new Date(note.lastModified)));
                appendOutput("");
                Toast.makeText(this, "Заметка создана", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showUpdateNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Обновление заметки");
        builder.setMessage("Введите ID заметки для обновления:");

        final EditText input = new EditText(this);
        input.setHint("ID заметки");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Далее", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID заметки", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long noteId = Long.parseLong(idStr);
                showUpdateNoteFormDialog(noteId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showUpdateNoteFormDialog(long noteId) {
        new Thread(() -> {
            Note existingNote = noteDao.getNoteById((int) noteId);
            if (existingNote == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАМЕТКА С ID " + noteId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Заметка не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Обновление заметки ID: " + noteId);

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 40);

                TextView titleLabel = new TextView(this);
                titleLabel.setText("Новый заголовок:");
                titleLabel.setTextSize(14);
                layout.addView(titleLabel);

                final EditText titleInput = new EditText(this);
                titleInput.setText(existingNote.title);
                titleInput.setPadding(0, 10, 0, 20);
                layout.addView(titleInput);

                TextView contentLabel = new TextView(this);
                contentLabel.setText("Новое содержание:");
                contentLabel.setTextSize(14);
                contentLabel.setPadding(0, 20, 0, 0);
                layout.addView(contentLabel);

                final EditText contentInput = new EditText(this);
                contentInput.setText(existingNote.content);
                contentInput.setPadding(0, 10, 0, 20);
                contentInput.setMinLines(3);
                layout.addView(contentInput);

                builder.setView(layout);

                builder.setPositiveButton("Обновить", (dialog, which) -> {
                    String newTitle = titleInput.getText().toString().trim();
                    String newContent = contentInput.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "Введите заголовок заметки", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newContent.isEmpty()) {
                        Toast.makeText(this, "Введите содержание заметки", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateNoteById(noteId, newTitle, newContent);
                });

                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        }).start();
    }

    private void updateNoteById(long noteId, String newTitle, String newContent) {
        new Thread(() -> {
            Note note = noteDao.getNoteById((int) noteId);
            if (note != null) {
                String oldTitle = note.title;
                note.title = newTitle;
                note.content = newContent;
                note.lastModified = System.currentTimeMillis();
                noteDao.update(note);

                runOnUiThread(() -> {
                    appendOutput("ЗАМЕТКА ОБНОВЛЕНА");
                    appendOutput("   ID: " + note.id);
                    appendOutput("   Было: " + oldTitle);
                    appendOutput("   Стало: " + note.title);
                    appendOutput("   Дата изменения: " + sdf.format(new Date(note.lastModified)));
                    appendOutput("");
                    Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDeleteNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление заметки");
        builder.setMessage("Введите ID заметки для удаления:");

        final EditText input = new EditText(this);
        input.setHint("ID заметки");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID заметки", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long noteId = Long.parseLong(idStr);
                deleteNoteById(noteId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteNoteById(long noteId) {
        new Thread(() -> {
            Note note = noteDao.getNoteById((int) noteId);
            if (note == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАМЕТКА С ID " + noteId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Заметка не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            noteDao.delete(note);

            runOnUiThread(() -> {
                appendOutput("ЗАМЕТКА УДАЛЕНА");
                appendOutput("   ID: " + note.id);
                appendOutput("   Заголовок: " + note.title);
                appendOutput("");
                Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void getAllNotes() {
        new Thread(() -> {
            List<Note> notes = noteDao.getAllNotes();
            runOnUiThread(() -> {
                appendOutput("ВСЕ ЗАМЕТКИ (" + notes.size() + " шт.)");
                if (notes.isEmpty()) {
                    appendOutput("   Нет заметок");
                } else {
                    for (Note note : notes) {
                        appendOutput("   ID:" + note.id + " | " + note.title);
                        appendOutput("     Содержание: " + (note.content.length() > 50 ? note.content.substring(0, 50) + "..." : note.content));
                        appendOutput("     Изменено: " + sdf.format(new Date(note.lastModified)));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void showGetNoteByIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поиск заметки");
        builder.setMessage("Введите ID заметки:");

        final EditText input = new EditText(this);
        input.setHint("ID заметки");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Найти", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID заметки", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long noteId = Long.parseLong(idStr);
                getNoteById(noteId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getNoteById(long noteId) {
        new Thread(() -> {
            Note found = noteDao.getNoteById((int) noteId);
            runOnUiThread(() -> {
                appendOutput("ПОИСК ЗАМЕТКИ ПО ID: " + noteId);
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

    private void showCreateTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Создание задачи");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        TextView titleLabel = new TextView(this);
        titleLabel.setText("Название задачи:");
        titleLabel.setTextSize(14);
        layout.addView(titleLabel);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Введите название задачи");
        titleInput.setPadding(0, 10, 0, 20);
        layout.addView(titleInput);

        TextView contentLabel = new TextView(this);
        contentLabel.setText("Описание:");
        contentLabel.setTextSize(14);
        contentLabel.setPadding(0, 20, 0, 0);
        layout.addView(contentLabel);

        final EditText contentInput = new EditText(this);
        contentInput.setHint("Введите описание задачи");
        contentInput.setPadding(0, 10, 0, 20);
        contentInput.setMinLines(3);
        layout.addView(contentInput);

        TextView dateTimeLabel = new TextView(this);
        dateTimeLabel.setText("Дата и время (необязательно):");
        dateTimeLabel.setTextSize(14);
        dateTimeLabel.setPadding(0, 20, 0, 0);
        layout.addView(dateTimeLabel);

        LinearLayout dateTimeLayout = new LinearLayout(this);
        dateTimeLayout.setOrientation(LinearLayout.HORIZONTAL);
        dateTimeLayout.setPadding(0, 10, 0, 0);

        final Calendar calendar = Calendar.getInstance();

        LinearLayout dateLayout = new LinearLayout(this);
        dateLayout.setOrientation(LinearLayout.VERTICAL);
        dateLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        final TextView dateLabel = new TextView(this);
        dateLabel.setText("Дата:");
        dateLabel.setTextSize(12);
        dateLayout.addView(dateLabel);

        final Button dateButton = new Button(this);
        dateButton.setText("Не выбрана");
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        dateButton.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
        dateLayout.addView(dateButton);

        LinearLayout timeLayout = new LinearLayout(this);
        timeLayout.setOrientation(LinearLayout.VERTICAL);
        timeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        final TextView timeLabel = new TextView(this);
        timeLabel.setText("Время:");
        timeLabel.setTextSize(12);
        timeLayout.addView(timeLabel);

        final Button timeButton = new Button(this);
        timeButton.setText("Не выбрано");
        timeButton.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        timeButton.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePicker.show();
        });
        timeLayout.addView(timeButton);

        dateTimeLayout.addView(dateLayout);
        dateTimeLayout.addView(timeLayout);
        layout.addView(dateTimeLayout);

        builder.setView(layout);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean hasDate = !dateButton.getText().toString().equals("Не выбрана");
            boolean hasTime = !timeButton.getText().toString().equals("Не выбрано");

            Long selectedDateTime = null;
            if (hasDate && hasTime) {
                selectedDateTime = calendar.getTimeInMillis();
            } else if (hasDate && !hasTime) {
                // Только дата - ставим время в 00:00
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTimeInMillis(calendar.getTimeInMillis());
                tempCal.set(Calendar.HOUR_OF_DAY, 0);
                tempCal.set(Calendar.MINUTE, 0);
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                selectedDateTime = tempCal.getTimeInMillis();
            } else if (!hasDate && hasTime) {
                // Только время - ставим дату на сегодня
                Calendar tempCal = Calendar.getInstance();
                tempCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                tempCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                selectedDateTime = tempCal.getTimeInMillis();
            }
            // Если hasDate == false && hasTime == false, то selectedDateTime остается null

            createTask(title, content, selectedDateTime);
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createTask(String title, String content, Long dateTime) {
        new Thread(() -> {
            Task task = new Task();
            task.title = title;
            task.content = content;
            task.isCompleted = false;

            // Только если пользователь явно выбрал дату и время
            if (dateTime != null) {
                // Разделяем dateTime на date и time
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(dateTime);

                // Для date - устанавливаем начало дня
                java.util.Calendar dateCal = java.util.Calendar.getInstance();
                dateCal.set(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0);
                dateCal.set(java.util.Calendar.MILLISECOND, 0);
                task.date = dateCal.getTimeInMillis();

                // Для time - сохраняем время от начала дня
                java.util.Calendar timeCal = java.util.Calendar.getInstance();
                timeCal.set(1970, 0, 1, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), 0);
                timeCal.set(java.util.Calendar.MILLISECOND, 0);
                task.time = timeCal.getTimeInMillis();
            } else {
                task.date = null;
                task.time = null;
            }

            taskDao.insert(task);

            runOnUiThread(() -> {
                appendOutput("СОЗДАНА ЗАДАЧА");
                appendOutput("   Название: " + task.title);
                appendOutput("   Описание: " + task.content);
                if (dateTime != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
                    appendOutput("   Дата и время: " + sdf.format(new java.util.Date(dateTime)));
                } else {
                    appendOutput("   Дата и время: не установлены");
                }
                appendOutput("   Статус: Не выполнена");
                appendOutput("");
                Toast.makeText(this, "Задача создана", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showCompleteTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отметить выполнение задачи");
        builder.setMessage("Введите ID задачи для отметки как выполненной:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Отметить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                markTaskAsCompleted(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void markTaskAsCompleted(long taskId) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);

            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            if (task.isCompleted) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА УЖЕ ВЫПОЛНЕНА");
                    appendOutput("   ID: " + task.id);
                    appendOutput("   Название: " + task.title);
                    appendOutput("");
                    Toast.makeText(this, "Задача уже выполнена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            taskDao.updateTaskCompletion(task.id, true);

            runOnUiThread(() -> {
                appendOutput("ЗАДАЧА ОТМЕЧЕНА КАК ВЫПОЛНЕННАЯ");
                appendOutput("   ID: " + task.id);
                appendOutput("   Название: " + task.title);
                appendOutput("");
                Toast.makeText(this, "Задача " + task.id + " отмечена как выполненная", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showUpdateTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Обновление задачи");
        builder.setMessage("Введите ID задачи для обновления:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Далее", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                showUpdateTaskFormDialog(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showUpdateTaskFormDialog(long taskId) {
        new Thread(() -> {
            Task existingTask = taskDao.getTask(taskId);
            if (existingTask == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Обновление задачи ID: " + taskId);

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 40);

                TextView titleLabel = new TextView(this);
                titleLabel.setText("Новое название:");
                titleLabel.setTextSize(14);
                layout.addView(titleLabel);

                final EditText titleInput = new EditText(this);
                titleInput.setText(existingTask.title);
                titleInput.setPadding(0, 10, 0, 20);
                layout.addView(titleInput);

                TextView contentLabel = new TextView(this);
                contentLabel.setText("Новое описание:");
                contentLabel.setTextSize(14);
                contentLabel.setPadding(0, 20, 0, 0);
                layout.addView(contentLabel);

                final EditText contentInput = new EditText(this);
                contentInput.setText(existingTask.content);
                contentInput.setPadding(0, 10, 0, 20);
                contentInput.setMinLines(3);
                layout.addView(contentInput);

                builder.setView(layout);

                builder.setPositiveButton("Обновить", (dialog, which) -> {
                    String newTitle = titleInput.getText().toString().trim();
                    String newContent = contentInput.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateTaskById(taskId, newTitle, newContent);
                });

                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        }).start();
    }

    private void updateTaskById(long taskId, String newTitle, String newContent) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task != null) {
                task.title = newTitle;
                task.content = newContent;
                taskDao.update(task);

                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА ОБНОВЛЕНА");
                    appendOutput("   ID: " + task.id);
                    appendOutput("   Новое название: " + task.title);
                    appendOutput("   Новое описание: " + task.content);
                    appendOutput("");
                    Toast.makeText(this, "Задача обновлена", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDeleteTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление задачи");
        builder.setMessage("Введите ID задачи для удаления:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                deleteTaskById(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteTaskById(long taskId) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            reminderDao.deleteByTaskId(task.id);
            taskDao.delete(task);

            runOnUiThread(() -> {
                appendOutput("ЗАДАЧА И ЕЁ НАПОМИНАНИЯ УДАЛЕНЫ");
                appendOutput("   ID: " + task.id);
                appendOutput("   Название: " + task.title);
                appendOutput("");
                Toast.makeText(this, "Задача удалена", Toast.LENGTH_SHORT).show();
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
                        appendOutput("   ID:" + task.id + " | " + task.title);
                        appendOutput("     Статус: " + (task.isCompleted ? "Выполнена" : "Не выполнена"));

                        // Показываем дату и время
                        if (task.date != null && task.date != 0 && task.time != null && task.time != 0) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
                            // Для отображения нужно объединить дату и время
                            java.util.Calendar dateCal = java.util.Calendar.getInstance();
                            dateCal.setTimeInMillis(task.date);
                            java.util.Calendar timeCal = java.util.Calendar.getInstance();
                            timeCal.setTimeInMillis(task.time);
                            dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
                            dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
                            appendOutput("     Дата/время: " + sdf.format(new java.util.Date(dateCal.getTimeInMillis())));
                        } else if (task.date != null && task.date != 0) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
                            appendOutput("     Дата: " + sdf.format(new java.util.Date(task.date)));
                        } else if (task.time != null && task.time != 0) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                            appendOutput("     Время: " + sdf.format(new java.util.Date(task.time)));
                        } else {
                            appendOutput("     Дата/время: не указано");
                        }

                        String description = task.content != null ? task.content : "";
                        appendOutput("     Описание: " + (description.length() > 50 ? description.substring(0, 50) + "..." : description));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void showCreateReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Создание напоминания");
        builder.setMessage("Введите ID задачи для создания напоминания:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Далее", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                showReminderTypeDialog(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showReminderTypeDialog(long taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тип напоминания");
        builder.setItems(new String[]{"TIME (Временное)", "LOCATION (Гео)"}, (dialog, which) -> {
            if (which == 0) {
                showTimeReminderDialog(taskId);
            } else {
                showLocationReminderDialog(taskId);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showTimeReminderDialog(long taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Временное напоминание");
        builder.setMessage("Введите за сколько минут до задачи сработает напоминание:");

        final EditText input = new EditText(this);
        input.setHint("Минуты до задачи (например, 15)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String minutesStr = input.getText().toString().trim();
            if (minutesStr.isEmpty()) {
                Toast.makeText(this, "Введите задержку", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minutes = Integer.parseInt(minutesStr);
                createTimeReminder(taskId, minutes);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createTimeReminder(long taskId, int minutesBefore) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Проверяем, есть ли у задачи дата и время
            if (task.date == null || task.date == 0) {
                runOnUiThread(() -> {
                    appendOutput("ОШИБКА! У задачи нет даты выполнения");
                    Toast.makeText(this, "У задачи нет даты", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            if (task.time == null || task.time == 0) {
                runOnUiThread(() -> {
                    appendOutput("ОШИБКА! У задачи нет времени выполнения");
                    Toast.makeText(this, "У задачи нет времени", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            int count = reminderDao.getCountForTask(taskId);
            if (count >= 5) {
                runOnUiThread(() -> {
                    appendOutput("ЛИМИТ! У задачи уже есть " + count + " напоминаний (макс. 5)");
                    appendOutput("");
                });
                return;
            }

            // Объединяем дату и время в одну метку времени
            long dueDateTime = combineDateAndTime(task.date, task.time);

            // Время срабатывания = время выполнения задачи - задержка (в миллисекундах)
            long triggerTime = dueDateTime - (minutesBefore * 60 * 1000);

            // Проверяем, не прошедшее ли время
            if (triggerTime <= System.currentTimeMillis()) {
                runOnUiThread(() -> {
                    appendOutput("ПРЕДУПРЕЖДЕНИЕ! Время напоминания уже прошло");
                    appendOutput("   Задача на: " + formatDateTime(dueDateTime));
                    appendOutput("   Напоминание за " + minutesBefore + " мин. должно было сработать в: " + formatDateTime(triggerTime));
                    appendOutput("");
                    Toast.makeText(this, "Время напоминания уже прошло", Toast.LENGTH_LONG).show();
                });
                return;
            }

            Reminder reminder = new Reminder();
            reminder.taskId = taskId;
            reminder.type = "TIME";
            reminder.triggerTime = triggerTime;
            reminder.isTriggered = false;

            long id = reminderDao.insert(reminder);

            runOnUiThread(() -> {
                appendOutput("СОЗДАНО НАПОМИНАНИЕ");
                appendOutput("   Для задачи ID: " + taskId + " (" + task.title + ")");
                appendOutput("   Тип: TIME");
                appendOutput("   Задача назначена на: " + formatDateTime(dueDateTime));
                appendOutput("   Напоминание за " + minutesBefore + " мин. сработает в: " + formatDateTime(triggerTime));
                appendOutput("   Напоминаний у задачи: " + (count + 1) + "/5");
                appendOutput("");
                Toast.makeText(this, "Напоминание создано", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // Вспомогательный метод для объединения даты и времени
    private long combineDateAndTime(long dateMillis, long timeMillis) {
        // Получаем календарь из даты
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);

        // Получаем часы и минуты из времени
        java.util.Calendar timeCalendar = java.util.Calendar.getInstance();
        timeCalendar.setTimeInMillis(timeMillis);

        // Устанавливаем часы и минуты в основную дату
        calendar.set(java.util.Calendar.HOUR_OF_DAY, timeCalendar.get(java.util.Calendar.HOUR_OF_DAY));
        calendar.set(java.util.Calendar.MINUTE, timeCalendar.get(java.util.Calendar.MINUTE));
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    // Вспомогательный метод для форматирования даты+времени (для вывода в лог)
    private String formatDateTime(long millis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(millis));
    }

    private void showLocationReminderDialog(long taskId) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Гео-напоминание");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 40);

            TextView latLabel = new TextView(this);
            latLabel.setText("Широта:");
            latLabel.setTextSize(14);
            layout.addView(latLabel);

            final EditText latInput = new EditText(this);
            latInput.setHint("55.7558");
            latInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(latInput);

            TextView lngLabel = new TextView(this);
            lngLabel.setText("Долгота:");
            lngLabel.setTextSize(14);
            lngLabel.setPadding(0, 20, 0, 0);
            layout.addView(lngLabel);

            final EditText lngInput = new EditText(this);
            lngInput.setHint("37.6176");
            lngInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(lngInput);

            TextView radiusLabel = new TextView(this);
            radiusLabel.setText("Радиус (метры):");
            radiusLabel.setTextSize(14);
            radiusLabel.setPadding(0, 20, 0, 0);
            layout.addView(radiusLabel);

            final EditText radiusInput = new EditText(this);
            radiusInput.setHint("500");
            radiusInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(radiusInput);

            builder.setView(layout);

            builder.setPositiveButton("Создать", (dialog, which) -> {
                String latStr = latInput.getText().toString().trim();
                String lngStr = lngInput.getText().toString().trim();
                String radiusStr = radiusInput.getText().toString().trim();

                if (latStr.isEmpty() || lngStr.isEmpty() || radiusStr.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    float radius = Float.parseFloat(radiusStr);
                    createLocationReminder(taskId, lat, lng, radius);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Введите корректные значения", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void createLocationReminder(long taskId, double lat, double lng, float radius) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            int count = reminderDao.getCountForTask(taskId);
            if (count >= 5) {
                runOnUiThread(() -> {
                    appendOutput("ЛИМИТ! У задачи уже есть " + count + " напоминаний (макс. 5)");
                    appendOutput("");
                });
                return;
            }

            Reminder reminder = new Reminder();
            reminder.taskId = taskId;
            reminder.type = "LOCATION";
            reminder.lat = lat;
            reminder.lng = lng;
            reminder.radius = radius;
            reminder.isTriggered = false;

            long id = reminderDao.insert(reminder);

            runOnUiThread(() -> {
                appendOutput("СОЗДАНО НАПОМИНАНИЕ");
                appendOutput("   ID: " + id);
                appendOutput("   Для задачи ID: " + taskId + " (" + task.title + ")");
                appendOutput("   Тип: LOCATION");
                appendOutput("   Координаты: (" + lat + ", " + lng + ")");
                appendOutput("   Радиус: " + radius + " м");
                appendOutput("   Напоминаний у задачи: " + (count + 1) + "/5");
                appendOutput("");
                Toast.makeText(this, "Напоминание создано", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showGetRemindersForTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Напоминания задачи");
        builder.setMessage("Введите ID задачи:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Показать", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                getRemindersForTask(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getRemindersForTask(long taskId) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            List<Reminder> reminders = reminderDao.getRemindersForTask(taskId);

            runOnUiThread(() -> {
                appendOutput("НАПОМИНАНИЯ ЗАДАЧИ: " + task.title + " (ID: " + taskId + ")");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   ID:" + r.id + " | Тип: " + r.type);
                        if ("TIME".equals(r.type)) {
                            appendOutput("     Время: " + sdf.format(new Date(r.triggerTime)));
                        } else if ("LOCATION".equals(r.type)) {
                            appendOutput("     Локация: (" + r.lat + ", " + r.lng + ") радиус: " + r.radius + "м");
                        }
                        appendOutput("     Выполнено: " + (r.isTriggered ? "Да" : "Нет"));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void showUpdateReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отметить напоминание");
        builder.setMessage("Введите ID напоминания для отметки как выполненного:");

        final EditText input = new EditText(this);
        input.setHint("ID напоминания");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Отметить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID напоминания", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long reminderId = Long.parseLong(idStr);
                updateReminderById(reminderId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateReminderById(long reminderId) {
        new Thread(() -> {
            List<Reminder> allReminders = reminderDao.getActiveTimeReminders();
            Reminder reminderToUpdate = null;
            for (Reminder r : allReminders) {
                if (r.id == reminderId) {
                    reminderToUpdate = r;
                    break;
                }
            }

            if (reminderToUpdate == null) {
                runOnUiThread(() -> {
                    appendOutput("НАПОМИНАНИЕ С ID " + reminderId + " НЕ НАЙДЕНО ИЛИ УЖЕ ВЫПОЛНЕНО\n");
                    Toast.makeText(this, "Напоминание не найдено", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            final Reminder finalReminderToUpdate = reminderToUpdate;
            reminderToUpdate.isTriggered = true;
            reminderDao.update(reminderToUpdate);

            runOnUiThread(() -> {
                appendOutput("НАПОМИНАНИЕ ОТМЕЧЕНО КАК ВЫПОЛНЕННОЕ");
                appendOutput(" ID: " + finalReminderToUpdate.id);
                appendOutput(" Тип: " + finalReminderToUpdate.type);
                appendOutput("");
                Toast.makeText(this, "Напоминание отмечено", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showDeleteReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление напоминания");
        builder.setMessage("Введите ID напоминания для удаления:");

        final EditText input = new EditText(this);
        input.setHint("ID напоминания");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID напоминания", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long reminderId = Long.parseLong(idStr);
                deleteReminderById(reminderId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteReminderById(long reminderId) {
        new Thread(() -> {
            List<Reminder> allReminders = reminderDao.getActiveTimeReminders();
            allReminders.addAll(reminderDao.getActiveLocationReminders());

            Reminder reminderToDelete = null;
            for (Reminder r : allReminders) {
                if (r.id == reminderId) {
                    reminderToDelete = r;
                    break;
                }
            }

            if (reminderToDelete == null) {
                runOnUiThread(() -> {
                    appendOutput("НАПОМИНАНИЕ С ID " + reminderId + " НЕ НАЙДЕНО\n");
                    Toast.makeText(this, "Напоминание не найдено", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            final Reminder finalReminderToDelete = reminderToDelete;
            reminderDao.delete(reminderToDelete);

            runOnUiThread(() -> {
                appendOutput("НАПОМИНАНИЕ УДАЛЕНО");
                appendOutput(" ID: " + finalReminderToDelete.id);
                appendOutput("");
                Toast.makeText(this, "Напоминание удалено", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void getActiveTimeReminders() {
        new Thread(() -> {
            List<Reminder> reminders = reminderDao.getActiveTimeReminders();
            runOnUiThread(() -> {
                appendOutput("АКТИВНЫЕ TIME-НАПОМИНАНИЯ (" + reminders.size() + " шт.)");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет активных напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   ID:" + r.id + " | Задача ID:" + r.taskId);
                        appendOutput("     Сработает: " + sdf.format(new Date(r.triggerTime)));
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void getActiveLocationReminders() {
        new Thread(() -> {
            List<Reminder> reminders = reminderDao.getActiveLocationReminders();
            runOnUiThread(() -> {
                appendOutput("АКТИВНЫЕ LOCATION-НАПОМИНАНИЯ (" + reminders.size() + " шт.)");
                if (reminders.isEmpty()) {
                    appendOutput("   Нет активных location-напоминаний");
                } else {
                    for (Reminder r : reminders) {
                        appendOutput("   ID:" + r.id + " | Задача ID:" + r.taskId);
                        appendOutput("     Координаты: (" + r.lat + ", " + r.lng + ")");
                        appendOutput("     Радиус: " + r.radius + " метров");
                    }
                }
                appendOutput("");
            });
        }).start();
    }

    private void showDeleteAllRemindersForTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление напоминаний задачи");
        builder.setMessage("Введите ID задачи для удаления всех напоминаний:");

        final EditText input = new EditText(this);
        input.setHint("ID задачи");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            String idStr = input.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Введите ID задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long taskId = Long.parseLong(idStr);
                deleteAllRemindersForTask(taskId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректный ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteAllRemindersForTask(long taskId) {
        new Thread(() -> {
            Task task = taskDao.getTask(taskId);
            if (task == null) {
                runOnUiThread(() -> {
                    appendOutput("ЗАДАЧА С ID " + taskId + " НЕ НАЙДЕНА\n");
                    Toast.makeText(this, "Задача не найдена", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            int count = reminderDao.getCountForTask(taskId);
            reminderDao.deleteByTaskId(taskId);

            runOnUiThread(() -> {
                appendOutput("УДАЛЕНЫ ВСЕ НАПОМИНАНИЯ ЗАДАЧИ");
                appendOutput("   Задача: " + task.title + " (ID: " + task.id + ")");
                appendOutput("   Удалено напоминаний: " + count);
                appendOutput("");
                Toast.makeText(this, "Напоминания удалены", Toast.LENGTH_SHORT).show();
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
        tvOutput.setText("...\n\n");
        Toast.makeText(this, "Вывод очищен", Toast.LENGTH_SHORT).show();
    }
}