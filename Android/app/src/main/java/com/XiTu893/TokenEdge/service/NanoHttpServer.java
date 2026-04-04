package com.XiTu893.TokenEdge.service;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class NanoHttpServer {
    private static final String TAG = "NanoHttpServer";
    
    private com.sun.net.httpserver.HttpServer server;
    private int port;
    private String modelPath;
    private String modelId;
    private int contextSize;
    private Context context;
    private ModelInferenceEngine inferenceEngine;

    public NanoHttpServer(int port, String modelPath, String modelId, int contextSize, Context context) throws IOException {
        this.port = port;
        this.modelPath = modelPath;
        this.modelId = modelId;
        this.contextSize = contextSize;
        this.context = context;
        
        Log.i(TAG, "Initializing server with model: " + modelId);
        
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
        setupHandlers();
        
        initializeInferenceEngine();
    }

    private void initializeInferenceEngine() {
        Log.i(TAG, "Initializing inference engine");
        inferenceEngine = ModelInferenceEngine.createEngine();
        
        boolean success = inferenceEngine.initialize(context, modelPath, modelId, contextSize);
        if (success) {
            Log.i(TAG, "Inference engine initialized successfully");
        } else {
            Log.e(TAG, "Failed to initialize inference engine");
        }
    }

    private void setupHandlers() {
        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"ok\",\"model\":\"" + modelId + "\",\"engine\":\"" + 
                (inferenceEngine != null ? inferenceEngine.getModelName() : "none") + "\"}";
            sendResponse(exchange, 200, response);
        });

        server.createContext("/v1/chat/completions", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = readRequestBody(exchange);
                String response = handleOpenAIChatCompletions(requestBody);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "{\"error\":{\"message\":\"Method not allowed\",\"type\":\"invalid_request_error\"}}");
            }
        });

        server.createContext("/v1/completions", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = readRequestBody(exchange);
                String response = handleOpenAICompletions(requestBody);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "{\"error\":{\"message\":\"Method not allowed\",\"type\":\"invalid_request_error\"}}");
            }
        });

        server.createContext("/v1/models", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            String response = getOpenAIModelsList();
            sendResponse(exchange, 200, response);
        });

        server.createContext("/api/chat", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = readRequestBody(exchange);
                String response = handleChatRequest(requestBody);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });

        server.createContext("/api/completions", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = readRequestBody(exchange);
                String response = handleCompletionsRequest(requestBody);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });

        server.createContext("/api/models", exchange -> {
            String response = getModelsList();
            sendResponse(exchange, 200, response);
        });
    }

    private void handleOptions(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
    }

    private String readRequestBody(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String handleOpenAIChatCompletions(String requestBody) {
        try {
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            
            String id = "chatcmpl-" + UUID.randomUUID().toString().substring(0, 24);
            long created = System.currentTimeMillis() / 1000;
            
            int maxTokens = extractIntValue(requestBody, "max_tokens", 512);
            float temperature = extractFloatValue(requestBody, "temperature", 0.7f);
            
            String content;
            if (inferenceEngine != null && inferenceEngine.isModelLoaded()) {
                content = inferenceEngine.generateChat(requestBody, maxTokens, temperature);
            } else {
                String lastMessage = extractLastMessage(requestBody);
                content = "收到您的问题: " + lastMessage + "\n\n(这是一个模拟响应，实际需要集成模型推理)";
            }
            
            jsonWriter.beginObject();
            jsonWriter.name("id").value(id);
            jsonWriter.name("object").value("chat.completion");
            jsonWriter.name("created").value(created);
            jsonWriter.name("model").value(modelId);
            jsonWriter.name("choices").beginArray();
            jsonWriter.beginObject();
            jsonWriter.name("index").value(0);
            jsonWriter.name("message").beginObject();
            jsonWriter.name("role").value("assistant");
            jsonWriter.name("content").value(content);
            jsonWriter.endObject();
            jsonWriter.name("finish_reason").value("stop");
            jsonWriter.endObject();
            jsonWriter.endArray();
            jsonWriter.name("usage").beginObject();
            jsonWriter.name("prompt_tokens").value(10);
            jsonWriter.name("completion_tokens").value(content.length() / 4);
            jsonWriter.name("total_tokens").value(10 + content.length() / 4);
            jsonWriter.endObject();
            jsonWriter.endObject();
            jsonWriter.close();
            return writer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error handling chat completions", e);
            return "{\"error\":{\"message\":\"" + e.getMessage() + "\",\"type\":\"internal_error\"}}";
        }
    }

    private String handleOpenAICompletions(String requestBody) {
        try {
            String prompt = extractValue(requestBody, "prompt");
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            
            String id = "cmpl-" + UUID.randomUUID().toString().substring(0, 24);
            long created = System.currentTimeMillis() / 1000;
            
            int maxTokens = extractIntValue(requestBody, "max_tokens", 512);
            float temperature = extractFloatValue(requestBody, "temperature", 0.7f);
            
            String content;
            if (inferenceEngine != null && inferenceEngine.isModelLoaded()) {
                content = inferenceEngine.generate(prompt, maxTokens, temperature);
            } else {
                content = "完成文本: " + prompt + "...\n\n(这是一个模拟响应)";
            }
            
            jsonWriter.beginObject();
            jsonWriter.name("id").value(id);
            jsonWriter.name("object").value("text_completion");
            jsonWriter.name("created").value(created);
            jsonWriter.name("model").value(modelId);
            jsonWriter.name("choices").beginArray();
            jsonWriter.beginObject();
            jsonWriter.name("text").value(content);
            jsonWriter.name("index").value(0);
            jsonWriter.name("logprobs").nullValue();
            jsonWriter.name("finish_reason").value("stop");
            jsonWriter.endObject();
            jsonWriter.endArray();
            jsonWriter.name("usage").beginObject();
            jsonWriter.name("prompt_tokens").value(10);
            jsonWriter.name("completion_tokens").value(content.length() / 4);
            jsonWriter.name("total_tokens").value(10 + content.length() / 4);
            jsonWriter.endObject();
            jsonWriter.endObject();
            jsonWriter.close();
            return writer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error handling completions", e);
            return "{\"error\":{\"message\":\"" + e.getMessage() + "\",\"type\":\"internal_error\"}}";
        }
    }

    private String getOpenAIModelsList() {
        StringWriter writer = new StringWriter();
        try {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();
            jsonWriter.name("object").value("list");
            jsonWriter.name("data").beginArray();
            
            String[] modelIds = {"gemma-4-e2b", "gemma-4-e4b", "gemma-4-26b-a4b", "gemma-4-31b-dense"};
            for (String mid : modelIds) {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(mid);
                jsonWriter.name("object").value("model");
                jsonWriter.name("created").value(System.currentTimeMillis() / 1000);
                jsonWriter.name("owned_by").value("xi-tu-studio");
                jsonWriter.endObject();
            }
            
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting models list", e);
            return "{\"error\":{\"message\":\"" + e.getMessage() + "\"}}";
        }
        return writer.toString();
    }

    private String extractLastMessage(String json) {
        try {
            int messagesIndex = json.indexOf("\"messages\"");
            if (messagesIndex == -1) {
                return extractValue(json, "prompt");
            }
            int lastContentIndex = json.lastIndexOf("\"content\"");
            if (lastContentIndex == -1) return "";
            int quoteStart = json.indexOf("\"", lastContentIndex + 9);
            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            return json.substring(quoteStart + 1, quoteEnd);
        } catch (Exception e) {
            return "";
        }
    }

    private String handleChatRequest(String requestBody) {
        try {
            String prompt = extractValue(requestBody, "prompt");
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            
            String content;
            if (inferenceEngine != null && inferenceEngine.isModelLoaded()) {
                int maxTokens = extractIntValue(requestBody, "max_tokens", 512);
                float temperature = extractFloatValue(requestBody, "temperature", 0.7f);
                content = inferenceEngine.generate(prompt, maxTokens, temperature);
            } else {
                content = "收到您的问题: " + prompt + "\n\n(这是一个模拟响应)";
            }
            
            jsonWriter.beginObject();
            jsonWriter.name("success").value(true);
            jsonWriter.name("response").value(content);
            jsonWriter.name("model").value(modelId);
            jsonWriter.endObject();
            jsonWriter.close();
            return writer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error handling chat request", e);
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String handleCompletionsRequest(String requestBody) {
        try {
            String prompt = extractValue(requestBody, "prompt");
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            
            String content;
            if (inferenceEngine != null && inferenceEngine.isModelLoaded()) {
                int maxTokens = extractIntValue(requestBody, "max_tokens", 512);
                float temperature = extractFloatValue(requestBody, "temperature", 0.7f);
                content = inferenceEngine.generate(prompt, maxTokens, temperature);
            } else {
                content = "完成文本: " + prompt + "...\n\n(这是一个模拟响应)";
            }
            
            jsonWriter.beginObject();
            jsonWriter.name("success").value(true);
            jsonWriter.name("choices").beginArray();
            jsonWriter.beginObject();
            jsonWriter.name("text").value(content);
            jsonWriter.name("index").value(0);
            jsonWriter.endObject();
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
            return writer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error handling completions request", e);
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getModelsList() {
        return "{\n" +
                "  \"models\": [\n" +
                "    { \"id\": \"gemma-4-e2b\", \"name\": \"Gemma 4 E2B\", \"size\": \"6GB\" },\n" +
                "    { \"id\": \"gemma-4-e4b\", \"name\": \"Gemma 4 E4B\", \"size\": \"10GB\" },\n" +
                "    { \"id\": \"gemma-4-26b-a4b\", \"name\": \"Gemma 4 26B A4B\", \"size\": \"34GB\" },\n" +
                "    { \"id\": \"gemma-4-31b-dense\", \"name\": \"Gemma 4 31B Dense\", \"size\": \"41GB\" }\n" +
                "  ]\n" +
                "}";
    }

    private String extractValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return "";
        int colonIndex = json.indexOf(":", keyIndex);
        int quoteStart = json.indexOf("\"", colonIndex + 1);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private int extractIntValue(String json, String key, int defaultValue) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return defaultValue;
            int colonIndex = json.indexOf(":", keyIndex);
            int startIndex = colonIndex + 1;
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            int endIndex = startIndex;
            while (endIndex < json.length() && Character.isDigit(json.charAt(endIndex))) {
                endIndex++;
            }
            if (startIndex < endIndex) {
                return Integer.parseInt(json.substring(startIndex, endIndex));
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to extract int value for " + key, e);
        }
        return defaultValue;
    }

    private float extractFloatValue(String json, String key, float defaultValue) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return defaultValue;
            int colonIndex = json.indexOf(":", keyIndex);
            int startIndex = colonIndex + 1;
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            int endIndex = startIndex;
            while (endIndex < json.length() && (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.')) {
                endIndex++;
            }
            if (startIndex < endIndex) {
                return Float.parseFloat(json.substring(startIndex, endIndex));
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to extract float value for " + key, e);
        }
        return defaultValue;
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }

    public void start() {
        Log.i(TAG, "Starting server on port " + port);
        server.start();
    }

    public void stop() {
        Log.i(TAG, "Stopping server");
        if (inferenceEngine != null) {
            inferenceEngine.release();
        }
        server.stop(0);
    }
}
