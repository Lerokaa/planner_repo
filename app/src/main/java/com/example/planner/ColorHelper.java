package com.example.planner;

import android.graphics.Color;

public class ColorHelper {

    private static final String[] COLOR_ARRAY = {
            "#FF6B6B", // Красный
            "#4ECDC4", // Бирюзовый
            "#45B7D1", // Голубой
            "#96CEB4", // Зеленый
            "#FFEAA7", // Желтый
            "#DDA0DD", // Фиолетовый
            "#FF8C42", // Оранжевый
            "#A8E6CF"  // Мятный
    };

    public static int getColorForTitle(String title) {
        int hash = title.hashCode();
        int colorIndex = Math.abs(hash) % COLOR_ARRAY.length;
        return Color.parseColor(COLOR_ARRAY[colorIndex]);
    }

    public static String getColorHexForTitle(String title) {
        int hash = title.hashCode();
        int colorIndex = Math.abs(hash) % COLOR_ARRAY.length;
        return COLOR_ARRAY[colorIndex];
    }
}