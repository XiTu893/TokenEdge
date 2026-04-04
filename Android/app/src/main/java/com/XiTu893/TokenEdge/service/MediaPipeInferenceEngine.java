package com.XiTu893.TokenEdge.service;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceOptions;

import java.io.File;

public class MediaPipeInferenceEngine implements ModelInferenceEngine {
    private static final String TAG = "MediaPipeEngine";
    private LlmInference llmInference;
    private String modelPath;
    private String modelId;
    private int contextSize;
    private boolean isLoaded = false;

    @Override
    public boolean initialize(Context context, String modelPath, String modelId, int contextSize) {
        Log.i(TAG, "Initializing MediaPipe LLM Inference");
        this.modelPath = modelPath;
        this.modelId = modelId;
        this.contextSize = contextSize;

        try {
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file not found: " + modelPath);
                return false;
            }

            Log.i(TAG, "Loading model from: " + modelPath);
            
            LlmInferenceOptions options = LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .build();

            llmInference = LlmInference.createFromOptions(context, options);
            isLoaded = true;
            Log.i(TAG, "MediaPipe LLM Inference initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize MediaPipe LLM Inference", e);
            return false;
        }
    }

    @Override
    public String generate(String prompt, int maxTokens, float temperature) {
        Log.i(TAG, "Generating response with MediaPipe");
        if (!isLoaded || llmInference == null) {
            return "模型未加载，请先加载模型";
        }

        try {
            Log.i(TAG, "Generating response for prompt: " + prompt.substring(0, Math.min(100, prompt.length())));
            
            // MediaPipe LLM Inference 同步生成
            String result = llmInference.generate(prompt);
            
            Log.i(TAG, "Generated response length: " + result.length());
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error generating response", e);
            return "生成失败: " + e.getMessage();
        }
    }

    @Override
    public String generateChat(String messages, int maxTokens, float temperature) {
        Log.i(TAG, "Generating chat response with MediaPipe");
        String prompt = convertMessagesToPrompt(messages);
        return generate(prompt, maxTokens, temperature);
    }

    private String convertMessagesToPrompt(String messages) {
        try {
            int lastContentIndex = messages.lastIndexOf("\"content\"");
            if (lastContentIndex == -1) return "";
            int quoteStart = messages.indexOf("\"", lastContentIndex + 10);
            int quoteEnd = messages.indexOf("\"", quoteStart + 1);
            return messages.substring(quoteStart + 1, quoteEnd);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing messages", e);
            return "";
        }
    }

    @Override
    public String getModelName() {
        return modelId;
    }

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }

    @Override
    public void release() {
        Log.i(TAG, "Releasing MediaPipe LLM Inference");
        if (llmInference != null) {
            llmInference.close();
            llmInference = null;
        }
        isLoaded = false;
    }
}
