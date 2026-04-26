package com.example.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {

    private TextView tvTitle, tvSubtitle, tvDescription, btnSkip;
    private ImageView ivIllustration;
    private LinearLayout llIndicators;
    private Button btnBack, btnNext;

    private int currentPage = 0;
    private static final int TOTAL_PAGES = 3;
    private boolean isAnimating = false;

    private final int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };

    private final int[] illustrations = {
            R.drawable.ic_tasks_illustration,
            R.drawable.ic_calendar_illustration,
            R.drawable.ic_habits_illustration
    };

    private final int[] subtitles = {
            R.string.onboarding_subtitle_1,
            R.string.onboarding_subtitle_2,
            R.string.onboarding_subtitle_3
    };

    private final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivIllustration = view.findViewById(R.id.ivIllustration);
        llIndicators = view.findViewById(R.id.llIndicators);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);
        btnSkip = view.findViewById(R.id.btnSkip);

        createIndicators();
        updateContent(false); // без анимации при первом показе

        btnNext.setOnClickListener(v -> {
            if (!isAnimating) goToNextPage();
        });

        btnBack.setOnClickListener(v -> {
            if (!isAnimating) goToPreviousPage();
        });

        btnSkip.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToCalendar();
            }
        });
    }

    private void createIndicators() {
        llIndicators.removeAllViews();
        for (int i = 0; i < TOTAL_PAGES; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == 0 ? 24 : 16,
                    i == 0 ? 24 : 16
            );
            params.setMargins(0, 0, i < TOTAL_PAGES - 1 ? 12 : 0, 0);
            dot.setLayoutParams(params);

            if (i == 0) {
                dot.setBackgroundResource(R.drawable.indicator_active);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_inactive);
            }

            llIndicators.addView(dot);
        }
    }

    private void updateIndicators() {
        for (int i = 0; i < llIndicators.getChildCount(); i++) {
            View dot = llIndicators.getChildAt(i);
            if (i == currentPage) {
                dot.setBackgroundResource(R.drawable.indicator_active);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = 24;
                params.height = 24;
                dot.setLayoutParams(params);

                // Анимация для активной точки
                Animation pulseAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                pulseAnim.setDuration(300);
                dot.startAnimation(pulseAnim);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_inactive);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = 16;
                params.height = 16;
                dot.setLayoutParams(params);
            }
        }
    }

    private void updateContent(boolean animate) {
        if (animate) {
            animateContentExit(() -> {
                updateContentData();
                animateContentEnter();
            });
        } else {
            updateContentData();
        }
    }

    private void updateContentData() {
        tvTitle.setText(titles[currentPage]);
        ivIllustration.setImageResource(illustrations[currentPage]);
        tvSubtitle.setText(subtitles[currentPage]);
        tvDescription.setText(descriptions[currentPage]);

        btnBack.setVisibility(currentPage == 0 ? View.GONE : View.VISIBLE);

        if (currentPage == TOTAL_PAGES - 1) {
            btnNext.setText("Начать");
        } else {
            btnNext.setText("Дальше");
        }

        updateIndicators();
    }

    private void animateContentExit(Runnable onComplete) {
        // Анимация ухода текущего контента
        AnimationSet exitSet = new AnimationSet(true);

        // Затухание и сдвиг вверх
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        fadeOut.setDuration(250);

        TranslateAnimation slideUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -0.2f
        );
        slideUp.setDuration(250);
        slideUp.setInterpolator(new DecelerateInterpolator());

        exitSet.addAnimation(fadeOut);
        exitSet.addAnimation(slideUp);
        exitSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onComplete.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        tvTitle.startAnimation(exitSet);
        ivIllustration.startAnimation(exitSet);
        tvSubtitle.startAnimation(exitSet);
        tvDescription.startAnimation(exitSet);

        // Анимация для кнопок
        Animation btnFadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        btnFadeOut.setDuration(200);
        btnBack.startAnimation(btnFadeOut);
        btnNext.startAnimation(btnFadeOut);
        btnSkip.startAnimation(btnFadeOut);
    }

    private void animateContentEnter() {
        // Анимация появления нового контента
        AnimationSet enterSet = new AnimationSet(true);

        // Появление с небольшим сдвигом снизу
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        fadeIn.setDuration(350);
        fadeIn.setStartOffset(100);

        TranslateAnimation slideUpFromBottom = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0.2f,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideUpFromBottom.setDuration(350);
        slideUpFromBottom.setStartOffset(100);
        slideUpFromBottom.setInterpolator(new DecelerateInterpolator());

        enterSet.addAnimation(fadeIn);
        enterSet.addAnimation(slideUpFromBottom);
        enterSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        tvTitle.startAnimation(enterSet);
        ivIllustration.startAnimation(enterSet);
        tvSubtitle.startAnimation(enterSet);
        tvDescription.startAnimation(enterSet);

        // Анимация для кнопок
        Animation btnFadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        btnFadeIn.setDuration(350);
        btnFadeIn.setStartOffset(150);
        btnBack.startAnimation(btnFadeIn);
        btnNext.startAnimation(btnFadeIn);
        btnSkip.startAnimation(btnFadeIn);

        // Анимация для иконки (легкое масштабирование)
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.8f, 1.0f,
                0.8f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(400);
        scaleAnim.setStartOffset(100);
        scaleAnim.setInterpolator(new DecelerateInterpolator());
        ivIllustration.startAnimation(scaleAnim);
    }

    private void goToNextPage() {
        if (currentPage < TOTAL_PAGES - 1) {
            currentPage++;
            updateContent(true);
        } else {
            if (getActivity() instanceof MainActivity) {
                // Анимация при завершении онбординга
                animateExitToCalendar();
            }
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateContent(true);
        }
    }

    private void animateExitToCalendar() {
        // Анимация ухода всего онбординга
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        fadeOut.setDuration(400);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToCalendar();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        if (getView() != null) {
            getView().startAnimation(fadeOut);
        }
    }
}