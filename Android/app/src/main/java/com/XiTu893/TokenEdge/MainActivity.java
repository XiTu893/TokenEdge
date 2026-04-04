package com.XiTu893.TokenEdge;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.XiTu893.TokenEdge.adapter.ModelAdapter;
import com.XiTu893.TokenEdge.model.ModelConfig;
import com.XiTu893.TokenEdge.service.DownloadService;
import com.XiTu893.TokenEdge.service.ServiceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ModelAdapter.OnModelActionListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_MODEL_REQUEST = 101;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int SERVICE_CHECK_DELAY = 500;

    private TextView tvTotalRAM;
    private TextView tvAvailableRAM;
    private TextView tvStatus;
    private TextView tvApiAddress;
    private TextView tvTestResponse;
    private View statusDot;
    private View apiAddressLayout;
    private MaterialButton btnCopyApi;
    private MaterialButton btnSendTest;
    private TextView btnOpenChat;
    private TextInputEditText etTestInput;
    private RecyclerView rvModels;

    private ModelAdapter modelAdapter;
    private OkHttpClient client;
    private int totalRAMGB = 0;
    private int availableRAMGB = 0;
    private Handler handler;
    private Runnable serviceCheckRunnable;
    private BroadcastReceiver downloadReceiver;
    private String currentRunningModelId = null;
    private Map<String, String> modelPathMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        modelPathMap = new HashMap<>();
        initViews();
        detectMemory();
        setupModelList();
        checkPermissions();
        client = new OkHttpClient();
        setupDownloadReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startServiceMonitor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopServiceMonitor();
    }

    private void startServiceMonitor() {
        if (serviceCheckRunnable == null) {
            serviceCheckRunnable = new Runnable() {
                @Override
                public void run() {
                    updateServiceUI();
                    handler.postDelayed(this, SERVICE_CHECK_DELAY);
                }
            };
            handler.post(serviceCheckRunnable);
        }
    }

    private void stopServiceMonitor() {
        if (serviceCheckRunnable != null) {
            handler.removeCallbacks(serviceCheckRunnable);
            serviceCheckRunnable = null;
        }
    }

    private void initViews() {
        tvTotalRAM = findViewById(R.id.tvTotalRAM);
        tvAvailableRAM = findViewById(R.id.tvAvailableRAM);
        tvStatus = findViewById(R.id.tvStatus);
        tvApiAddress = findViewById(R.id.tvApiAddress);
        tvTestResponse = findViewById(R.id.tvTestResponse);
        statusDot = findViewById(R.id.statusDot);
        apiAddressLayout = findViewById(R.id.apiAddressLayout);
        btnCopyApi = findViewById(R.id.btnCopyApi);
        btnSendTest = findViewById(R.id.btnSendTest);
        btnOpenChat = findViewById(R.id.btnOpenChat);
        etTestInput = findViewById(R.id.etTestInput);
        rvModels = findViewById(R.id.rvModels);

        btnCopyApi.setOnClickListener(v -> copyApiAddress());
        btnSendTest.setOnClickListener(v -> sendTestRequest());
        btnOpenChat.setOnClickListener(v -> openChatActivity());
    }

    private void detectMemory() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        totalRAMGB = (int) (memoryInfo.totalMem / (1024 * 1024 * 1024));
        availableRAMGB = (int) (memoryInfo.availMem / (1024 * 1024 * 1024));

        tvTotalRAM.setText(totalRAMGB + " GB");
        tvAvailableRAM.setText(availableRAMGB + " GB");

        if (modelAdapter != null) {
            modelAdapter.setTotalRAM(totalRAMGB);
        }
    }

    private void setupModelList() {
        List<ModelConfig> models = Arrays.asList(ModelConfig.GEMMA4_MODELS);
        modelAdapter = new ModelAdapter(models, totalRAMGB, this);
        
        rvModels.setLayoutManager(new LinearLayoutManager(this));
        rvModels.setAdapter(modelAdapter);
        rvModels.setItemAnimator(null);
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
                break;
            }
        }

        if (!hasAllPermissions) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "需要权限才能正常使用", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDownload(ModelConfig model) {
        modelAdapter.setModelStatus(model.id, ModelConfig.ModelStatus.DOWNLOADING);
        
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("downloadUrls", model.downloadUrls);
        intent.putExtra("modelId", model.id);
        intent.putExtra("autoStart", false);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        Toast.makeText(this, "正在下载 " + model.displayName + "...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart(ModelConfig model, int contextSize) {
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        if (serviceManager.isServiceRunning()) {
            Toast.makeText(this, "请先停止当前运行的服务", Toast.LENGTH_SHORT).show();
            return;
        }

        String modelPath = modelPathMap.get(model.id);
        if (modelPath == null || modelPath.isEmpty()) {
            Toast.makeText(this, "模型文件路径未找到", Toast.LENGTH_SHORT).show();
            return;
        }
        
        modelAdapter.setModelStatus(model.id, ModelConfig.ModelStatus.STARTING);
        currentRunningModelId = model.id;
        
        Intent intent = new Intent(this, com.XiTu893.TokenEdge.service.ApiService.class);
        intent.putExtra("modelPath", modelPath);
        intent.putExtra("modelId", model.id);
        intent.putExtra("contextSize", contextSize);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        Toast.makeText(this, "正在启动 " + model.displayName + "...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop(ModelConfig model) {
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        if (!serviceManager.isServiceRunning()) {
            Toast.makeText(this, "服务未在运行", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, com.XiTu893.TokenEdge.service.ApiService.class);
        stopService(intent);
        
        Toast.makeText(this, "正在停止服务...", Toast.LENGTH_SHORT).show();
    }

    private void setupDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String modelPath = intent.getStringExtra(DownloadService.EXTRA_MODEL_PATH);
                String modelId = intent.getStringExtra("modelId");
                
                if (modelId != null && modelPath != null && !modelPath.isEmpty()) {
                    modelPathMap.put(modelId, modelPath);
                    modelAdapter.setModelDownloaded(modelId, modelPath);
                    Toast.makeText(MainActivity.this, "模型下载完成！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    private void updateServiceUI() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        boolean isRunning = serviceManager.isServiceRunning();
        int port = serviceManager.getCurrentPort();

        if (isRunning && port != -1) {
            statusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.status_dot_active));
            tvStatus.setText(R.string.service_running);
            apiAddressLayout.setVisibility(View.VISIBLE);
            
            if (currentRunningModelId != null) {
                modelAdapter.setModelStatus(currentRunningModelId, ModelConfig.ModelStatus.RUNNING);
            }
            
            String ip = getLocalIPAddress();
            tvApiAddress.setText("http://" + ip + ":" + port + "/v1/chat/completions");
        } else {
            statusDot.setBackgroundColor(0xFF666666);
            tvStatus.setText(R.string.service_stopped);
            apiAddressLayout.setVisibility(View.GONE);
            
            if (currentRunningModelId != null) {
                modelAdapter.setModelStatus(currentRunningModelId, ModelConfig.ModelStatus.DOWNLOADED);
                currentRunningModelId = null;
            }
        }
    }

    private String getLocalIPAddress() {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4) {
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "127.0.0.1";
    }

    private void copyApiAddress() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("API Address", tvApiAddress.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    private String getCurrentModelName() {
        if (currentRunningModelId != null) {
            for (ModelConfig model : ModelConfig.GEMMA4_MODELS) {
                if (model.id.equals(currentRunningModelId)) {
                    return model.displayName;
                }
            }
        }
        return "Unknown Model";
    }

    private void openChatActivity() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        if (!serviceManager.isServiceRunning()) {
            Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
            return;
        }

        int port = serviceManager.getCurrentPort();
        if (port == -1) {
            Toast.makeText(this, "服务端口未就绪", Toast.LENGTH_SHORT).show();
            return;
        }

        String ip = getLocalIPAddress();
        String apiBaseUrl = "http://" + ip + ":" + port;
        String modelName = getCurrentModelName();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_API_ADDRESS, apiBaseUrl);
        intent.putExtra(ChatActivity.EXTRA_MODEL_NAME, modelName);
        startActivity(intent);
    }

    private void sendTestRequest() {
        String prompt = etTestInput.getText().toString();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "请输入测试消息", Toast.LENGTH_SHORT).show();
            return;
        }

        ServiceManager serviceManager = ServiceManager.getInstance();
        if (!serviceManager.isServiceRunning()) {
            Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
            return;
        }

        int port = serviceManager.getCurrentPort();
        if (port == -1) {
            Toast.makeText(this, "服务端口未就绪", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("model", currentRunningModelId != null ? currentRunningModelId : "gemma-4");
            
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);
            json.put("messages", messages);
            
            json.put("max_tokens", 512);
            json.put("temperature", 0.7);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url("http://localhost:" + port + "/v1/chat/completions")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        tvTestResponse.setText("请求失败: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONArray choices = jsonResponse.optJSONArray("choices");
                            if (choices != null && choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject message = choice.optJSONObject("message");
                                if (message != null) {
                                    tvTestResponse.setText(message.optString("content", "无响应"));
                                } else {
                                    tvTestResponse.setText(choice.optString("text", "无响应"));
                                }
                            } else {
                                tvTestResponse.setText(responseData);
                            }
                        } catch (Exception e) {
                            tvTestResponse.setText(responseData);
                        }
                    });
                }
            });
        } catch (Exception e) {
            tvTestResponse.setText("错误: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServiceMonitor();
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }
    }
}
