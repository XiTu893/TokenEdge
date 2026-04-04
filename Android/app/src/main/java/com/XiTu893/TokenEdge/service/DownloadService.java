package com.XiTu893.TokenEdge.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.XiTu893.TokenEdge.MainActivity;
import com.XiTu893.TokenEdge.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 2;
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.XiTu893.TokenEdge.DOWNLOAD_COMPLETE";
    public static final String EXTRA_MODEL_PATH = "model_path";
    private boolean isDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isDownloading) {
            String[] downloadUrls = intent.getStringArrayExtra("downloadUrls");
            String modelId = intent.getStringExtra("modelId");
            boolean autoStart = intent.getBooleanExtra("autoStart", false);
            startForeground(NOTIFICATION_ID, createNotification("准备下载..."));
            startDownloadWithRetry(downloadUrls, modelId, autoStart);
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "模型下载",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TokenEdge 模型下载")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(int progress, String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TokenEdge 模型下载")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build();

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void startDownloadWithRetry(String[] downloadUrls, String modelId, boolean autoStart) {
        isDownloading = true;
        new Thread(() -> {
            int retryCount = 0;
            while (true) {
                for (int i = 0; i < downloadUrls.length; i++) {
                    String urlString = downloadUrls[i];
                    String sourceName = getSourceName(urlString);
                    updateNotification(0, "尝试从 " + sourceName + " 下载... (尝试 " + (retryCount * downloadUrls.length + i + 1) + ")");
                    
                    boolean success = tryDownload(urlString, modelId, autoStart, sourceName);
                    if (success) {
                        return;
                    }
                }
                retryCount++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }

    private String getSourceName(String url) {
        if (url.contains("modelscope.cn")) return "ModelScope";
        if (url.contains("ghproxy.com")) return "ghproxy.com";
        if (url.contains("hf-mirror.com")) return "hf-mirror.com";
        if (url.contains("huggingface.co")) return "Hugging Face";
        return "未知源";
    }

    private boolean tryDownload(String urlString, String modelId, boolean autoStart, String sourceName) {
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            int fileLength = connection.getContentLength();
            input = connection.getInputStream();

            File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            String fileName = modelId != null ? modelId + ".gguf" : "model.gguf";
            File outputFile = new File(downloadDir, fileName);
            output = new FileOutputStream(outputFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (fileLength > 0) {
                    int progress = (int) (total * 100 / fileLength);
                    updateNotification(progress, "从 " + sourceName + " 下载中... " + progress + "%");
                }
                output.write(data, 0, count);
            }

            updateNotification(100, "下载完成");
            
            Intent broadcastIntent = new Intent(ACTION_DOWNLOAD_COMPLETE);
            broadcastIntent.putExtra(EXTRA_MODEL_PATH, outputFile.getAbsolutePath());
            broadcastIntent.putExtra("modelId", modelId);
            sendBroadcast(broadcastIntent);
            
            if (autoStart) {
                Intent serviceIntent = new Intent(this, ApiService.class);
                serviceIntent.putExtra("modelPath", outputFile.getAbsolutePath());
                serviceIntent.putExtra("modelId", modelId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }

            stopSelf();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (connection != null) connection.disconnect();
            isDownloading = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
