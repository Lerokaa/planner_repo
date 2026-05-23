package com.example.planner;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {

    private TextView tvTitle, tvSubtitle, tvDescription, btnSkip;
    private ImageView ivIllustration;
    private LinearLayout llIndicators;
    private Button btnBack, btnNext;

    private int currentPage = 0;
    private static final int TOTAL_PAGES = 3;
    private boolean isAnimating = false;
    private GestureDetectorCompat gestureDetector;

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
        updateContent(false);

        // Настройка свайпов
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isAnimating) return false;

                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        // Свайп вправо - предыдущая страница
                        if (currentPage > 0) {
                            goToPreviousPageWithSwipe();
                        }
                    } else {
                        // Свайп влево - следующая страница
                        if (currentPage < TOTAL_PAGES - 1) {
                            goToNextPageWithSwipe();
                        } else {
                            animateExitToCalendar();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

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
            // Для кнопок используем обычную анимацию без свайпа
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
        AnimationSet exitSet = new AnimationSet(true);

        TranslateAnimation slideUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -0.2f
        );
        slideUp.setDuration(250);
        slideUp.setInterpolator(new DecelerateInterpolator());

        Animation fadeOut = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeOut.setDuration(250);

        exitSet.addAnimation(slideUp);
        exitSet.addAnimation(fadeOut);
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

        Animation btnFadeOut = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        btnFadeOut.setDuration(200);
        btnBack.startAnimation(btnFadeOut);
        btnNext.startAnimation(btnFadeOut);
        btnSkip.startAnimation(btnFadeOut);
    }

    private void animateContentEnter() {
        AnimationSet enterSet = new AnimationSet(true);

        TranslateAnimation slideUpFromBottom = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0.2f,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideUpFromBottom.setDuration(350);
        slideUpFromBottom.setStartOffset(100);
        slideUpFromBottom.setInterpolator(new DecelerateInterpolator());

        Animation fadeIn = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeIn.setDuration(350);
        fadeIn.setStartOffset(100);

        enterSet.addAnimation(slideUpFromBottom);
        enterSet.addAnimation(fadeIn);
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

        Animation btnFadeIn = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        btnFadeIn.setDuration(350);
        btnFadeIn.setStartOffset(150);
        btnBack.startAnimation(btnFadeIn);
        btnNext.startAnimation(btnFadeIn);
        btnSkip.startAnimation(btnFadeIn);
    }

    private void goToNextPage() {
        if (currentPage < TOTAL_PAGES - 1) {
            goToNextPageWithSwipe();
        } else {
            animateExitToCalendar();
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 0) {
            goToPreviousPageWithSwipe();
        }
    }

    private void goToNextPageWithSwipe() {
        if (isAnimating) return;
        currentPage++;
        animateSwipeLeft();
    }

    private void goToPreviousPageWithSwipe() {
        if (isAnimating) return;
        currentPage--;
        animateSwipeRight();
    }

    private void animateSwipeLeft() {
        // Анимация выезда текущего контента влево
        AnimationSet exitSet = new AnimationSet(true);
        TranslateAnimation slideOutLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideOutLeft.setDuration(300);
        slideOutLeft.setInterpolator(new DecelerateInterpolator());

        Animation fadeOut = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeOut.setDuration(200);

        exitSet.addAnimation(slideOutLeft);
        exitSet.addAnimation(fadeOut);

        exitSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateContentData();
                animateSwipeLeftEnter();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        tvTitle.startAnimation(exitSet);
        ivIllustration.startAnimation(exitSet);
        tvSubtitle.startAnimation(exitSet);
        tvDescription.startAnimation(exitSet);
    }

    private void animateSwipeLeftEnter() {
        // Анимация въезда нового контента справа
        AnimationSet enterSet = new AnimationSet(true);
        TranslateAnimation slideInRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideInRight.setDuration(300);
        slideInRight.setInterpolator(new DecelerateInterpolator());

        Animation fadeIn = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeIn.setDuration(300);

        enterSet.addAnimation(slideInRight);
        enterSet.addAnimation(fadeIn);

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
    }

    private void animateSwipeRight() {
        // Анимация выезда текущего контента вправо
        AnimationSet exitSet = new AnimationSet(true);
        TranslateAnimation slideOutRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideOutRight.setDuration(300);
        slideOutRight.setInterpolator(new DecelerateInterpolator());

        Animation fadeOut = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeOut.setDuration(200);

        exitSet.addAnimation(slideOutRight);
        exitSet.addAnimation(fadeOut);

        exitSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateContentData();
                animateSwipeRightEnter();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        tvTitle.startAnimation(exitSet);
        ivIllustration.startAnimation(exitSet);
        tvSubtitle.startAnimation(exitSet);
        tvDescription.startAnimation(exitSet);
    }

    private void animateSwipeRightEnter() {
        // Анимация въезда нового контента слева
        AnimationSet enterSet = new AnimationSet(true);
        TranslateAnimation slideInLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0
        );
        slideInLeft.setDuration(300);
        slideInLeft.setInterpolator(new DecelerateInterpolator());

        Animation fadeIn = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
        fadeIn.setDuration(300);

        enterSet.addAnimation(slideInLeft);
        enterSet.addAnimation(fadeIn);

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
    }

    private void animateExitToCalendar() {
        Animation fadeOut = new Animation() {
            @Override
            public boolean willChangeBounds() { return false; }
        };
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