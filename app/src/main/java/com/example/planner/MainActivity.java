package com.example.planner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Всегда показываем сплэш и онбординг при каждом запуске
        showSplashScreen();
    }

    private void showSplashScreen() {
        setContentView(R.layout.activity_splash);

        View splashView = findViewById(R.id.splash_root);
        if (splashView != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            splashView.startAnimation(fadeIn);
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::showOnboarding, SPLASH_DURATION);
    }

    private void showOnboarding() {
        setContentView(R.layout.activity_onboarding);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.onboarding_container, new OnboardingFragment())
                .commit();
    }

    /**
     * Переход в календарь после завершения онбординга
     */
    public void navigateToCalendar() {
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Переход в задачи (если нужно)
     */
    public void navigateToTasks() {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}