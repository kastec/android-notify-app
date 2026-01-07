package com.example.emailapp;

import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogHelper {
    private static TextView logTextView;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void setLogTextView(TextView textView) {
        logTextView = textView;
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
        appendToLogView("D", tag, message, null);
    }

    public static void d(String tag, String message, Throwable throwable) {
        Log.d(tag, message, throwable);
        appendToLogView("D", tag, message, throwable);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        appendToLogView("E", tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
        appendToLogView("E", tag, message, throwable);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        appendToLogView("W", tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        Log.w(tag, message, throwable);
        appendToLogView("W", tag, message, throwable);
    }

    private static void appendToLogView(String level, String tag, String message, Throwable throwable) {
        if (logTextView != null) {
            String time = dateFormat.format(new Date());
            String logEntry = String.format("[%s] %s/%s: %s", time, level, tag, message);
            
            if (throwable != null) {
                logEntry += "\n" + Log.getStackTraceString(throwable);
            }
            
            // Создаем final копию для использования в лямбде
            final String finalLogEntry = logEntry;
            
            logTextView.post(() -> {
                String currentText = logTextView.getText().toString();
                logTextView.setText(currentText + finalLogEntry + "\n");
                
                // Прокручиваем вниз
                if (logTextView.getLayout() != null) {
                    int scrollAmount = logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight();
                    if (scrollAmount > 0) {
                        logTextView.scrollTo(0, scrollAmount);
                    }
                } else {
                    // Альтернативный способ прокрутки через ScrollView
                    android.view.ViewParent parent = logTextView.getParent();
                    if (parent instanceof android.widget.ScrollView) {
                        android.widget.ScrollView scrollView = (android.widget.ScrollView) parent;
                        scrollView.post(() -> scrollView.fullScroll(android.view.View.FOCUS_DOWN));
                    }
                }
            });
        }
    }
}

