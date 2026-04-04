package com.XiTu893.TokenEdge.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.XiTu893.TokenEdge.MainActivity;
import com.XiTu893.TokenEdge.R;

import java.io.IOException;

public class ApiService extends Service {
    private static final String CHANNEL_ID = "api_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private NanoHttpServer server;
    private int currentPort = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        if (serviceManager.isServiceRunning()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String modelPath = intent.getStringExtra("modelPath");
        String modelId = intent.getStringExtra("modelId");
        int contextSize = intent.getIntExtra("contextSize", 4096);
        int port = serviceManager.findAvailablePort();
        
        if (port == -1) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (modelId == null) {
            modelId = "gemma-4";
        }

        currentPort = port;
        serviceManager.setServiceRunning(true);
        serviceManager.setCurrentPort(port);

        startForeground(NOTIFICATION_ID, createNotification(port));
        startApiServer(port, modelPath, modelId, contextSize);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "API 服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(int port) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TokenEdge API 服务")
                .setContentText("服务运行中 - http://localhost:" + port)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void startApiServer(int port, String modelPath, String modelId, int contextSize) {
        new Thread(() -> {
            try {
                server = new NanoHttpServer(port, modelPath, modelId, contextSize, ApiService.this);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
        ServiceManager.getInstance().setServiceRunning(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
