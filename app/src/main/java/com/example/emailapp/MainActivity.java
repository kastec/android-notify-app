package com.example.emailapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация launcher для запроса разрешения на уведомления
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Запрос разрешения на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        Button buttonReset = findViewById(R.id.buttonReset);
        textView = findViewById(R.id.textView);
        TextView logTextView = findViewById(R.id.logTextView);

        // Инициализируем LogHelper для перехвата логов
        LogHelper.setLogTextView(logTextView);

        buttonReset.setOnClickListener(v -> {
            // Сброс FCM токена и получение нового
            resetFCMToken();
        });

        // Проверяем, есть ли сообщение в Intent (если Activity была запущена из сервиса)
        handleIncomingMessage(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingMessage(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Проверяем сообщение при возврате на экран
        handleIncomingMessage(getIntent());
    }

    private void handleIncomingMessage(Intent intent) {
        if (intent != null && intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            if (message != null && textView != null) {
                textView.setText(message);
            }
        }
    }

    private void resetFCMToken() {
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            LogHelper.d("MainActivity", "Токен успешно удален");
                            // Получаем новый токен
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                LogHelper.w("MainActivity", "Ошибка при получении нового токена", task.getException());
                                                Toast.makeText(MainActivity.this, "Ошибка при получении нового токена", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Новый токен получен
                                            String newToken = task.getResult();
                                            LogHelper.d("MainActivity", "Новый токен получен: " + newToken);
                                            Toast.makeText(MainActivity.this, "Ключ сброшен. Новый токен получен", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            LogHelper.e("MainActivity", "Ошибка при удалении токена", task.getException());
                            Toast.makeText(MainActivity.this, "Ошибка при сбросе ключа", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}