package com.XiTu893.TokenEdge;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.XiTu893.TokenEdge.adapter.MessageAdapter;
import com.XiTu893.TokenEdge.model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_API_ADDRESS = "api_address";
    public static final String EXTRA_MODEL_NAME = "model_name";

    private String apiAddress;
    private String modelName;
    private MessageAdapter messageAdapter;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private TextView btnSend;
    private TextView tvApiAddress;
    private TextView tvModelInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        apiAddress = getIntent().getStringExtra(EXTRA_API_ADDRESS);
        modelName = getIntent().getStringExtra(EXTRA_MODEL_NAME);

        initViews();
        setupRecyclerView();
        setupSendButton();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvApiAddress = findViewById(R.id.tvApiAddress);
        tvModelInfo = findViewById(R.id.tvModelInfo);

        tvApiAddress.setText(apiAddress);
        tvModelInfo.setText("模型: " + modelName);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
        
        messageAdapter.addMessage(new ChatMessage(
            ChatMessage.Role.ASSISTANT,
            "你好！我是 " + modelName + "，有什么可以帮助你的吗？"
        ));
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        messageAdapter.addMessage(new ChatMessage(ChatMessage.Role.USER, message));
        etMessage.setText("");
        scrollToBottom();
        
        btnSend.setEnabled(false);
        btnSend.setText("发送中...");
        
        new Thread(() -> sendToApi(message)).start();
    }

    private void sendToApi(String userMessage) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiAddress + "/v1/chat/completions");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer tokenedge");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            requestBody.put("messages", messages);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8")
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONObject choice = choices.getJSONObject(0);
                JSONObject messageObj = choice.getJSONObject("message");
                String assistantMessage = messageObj.getString("content");

                runOnUiThread(() -> {
                    messageAdapter.addMessage(
                        new ChatMessage(ChatMessage.Role.ASSISTANT, assistantMessage)
                    );
                    scrollToBottom();
                    resetSendButton();
                });
            } else {
                throw new Exception("HTTP Error: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                messageAdapter.addMessage(new ChatMessage(
                    ChatMessage.Role.ASSISTANT,
                    "抱歉，发生错误：" + e.getMessage()
                ));
                scrollToBottom();
                resetSendButton();
            });
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void resetSendButton() {
        btnSend.setEnabled(true);
        btnSend.setText("发送");
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}
