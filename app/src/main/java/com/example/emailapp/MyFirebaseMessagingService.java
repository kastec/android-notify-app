package com.example.emailapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "push_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        LogHelper.d(TAG, "From: " + remoteMessage.getFrom());
        LogHelper.d(TAG, "Message received! Data size: " + remoteMessage.getData().size());
        LogHelper.d(TAG, "Has notification: " + (remoteMessage.getNotification() != null));

        String messageText = null;
        String title = null;

        // Проверяем, есть ли данные в сообщении
        if (remoteMessage.getData().size() > 0) {
            LogHelper.d(TAG, "Message data payload: " + remoteMessage.getData());
            // Пытаемся получить текст из data payload
            messageText = remoteMessage.getData().get("message");
            if (messageText == null) {
                messageText = remoteMessage.getData().get("body");
            }
            // Пытаемся получить заголовок из data payload
            title = remoteMessage.getData().get("title");
        }

        // Проверяем, есть ли уведомление в сообщении
        if (remoteMessage.getNotification() != null) {
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();
            LogHelper.d(TAG, "Message Notification Title: " + notificationTitle);
            LogHelper.d(TAG, "Message Notification Body: " + notificationBody);
            if (messageText == null) {
                messageText = notificationBody;
            }
            if (title == null) {
                title = notificationTitle;
            }
        }

        // ВСЕГДА показываем уведомление, даже если есть только data payload
        if (messageText != null && !messageText.isEmpty()) {
            sendNotification(title, messageText);
            LogHelper.d(TAG, "Уведомление отправлено: " + messageText);
        } else {
            LogHelper.w(TAG, "Не удалось получить текст сообщения для уведомления");
        }

        // Отправляем сообщение в MainActivity для отображения в textView
        if (messageText != null) {
            sendMessageToActivity(messageText);
        }
    }

    @Override
    public void onNewToken(String token) {
        LogHelper.d(TAG, "Refreshed token: " + token);
        // Отправьте токен на ваш сервер, если необходимо
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // URL для отправки токена
        // Примечание: для эмулятора Android используйте "http://10.0.2.2:5003" вместо "localhost"
        //String url = "http://localhost:5003/api/key/" + token;
        String url = "http://10.0.2.2:5003/api/key/" + token;
       
        LogHelper.d(TAG, "Отправка токена на сервер: " + url);
        
        OkHttpClient client = new OkHttpClient();
        
        // Создаем POST запрос с пустым телом (или можно добавить JSON, если требуется)
        RequestBody requestBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogHelper.e(TAG, "Ошибка при отправке токена на сервер", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    LogHelper.d(TAG, "Токен успешно отправлен на сервер. Код ответа: " + response.code());
                } else {
                    LogHelper.e(TAG, "Ошибка ответа сервера. Код: " + response.code() + ", Сообщение: " + response.message());
                }
                response.close();
            }
        });
    }

    private void sendNotification(String title, String messageBody) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("message", messageBody);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title != null && !title.isEmpty() ? title : "Уведомление")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                // Используем уникальный ID для каждого уведомления
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());
                LogHelper.d(TAG, "Уведомление отображено с ID: " + notificationId);
            } else {
                LogHelper.e(TAG, "NotificationManager is null!");
            }
        } catch (Exception e) {
            LogHelper.e(TAG, "Ошибка при создании уведомления", e);
        }
    }

    private void sendMessageToActivity(String message) {
        // Отправляем Intent напрямую в Activity с сообщением
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.putExtra("message", message);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activityIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Push Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Канал для push уведомлений");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                LogHelper.d(TAG, "Канал уведомлений создан: " + CHANNEL_ID);
            } else {
                LogHelper.e(TAG, "Не удалось создать канал уведомлений - NotificationManager null");
            }
        }
    }
}

