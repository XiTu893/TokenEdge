package com.XiTu893.TokenEdge.service;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * 模型推理引擎接口
 * 用于集成不同的大模型推理后端（如 llama.cpp、Google AI Edge 等）
 */
public interface ModelInferenceEngine {

    /**
     * 初始化模型
     * 
     * @param context Android 上下文
     * @param modelPath 模型文件路径
     * @param modelId 模型 ID
     * @param contextSize 上下文窗口大小
     * @return 初始化是否成功
     */
    boolean initialize(Context context, String modelPath, String modelId, int contextSize);

    /**
     * 生成文本响应
     * 
     * @param prompt 用户输入提示
     * @param maxTokens 最大生成 token 数
     * @param temperature 温度参数 (0-1)
     * @return 生成的文本
     */
    String generate(String prompt, int maxTokens, float temperature);

    /**
     * 生成聊天响应（OpenAI 格式）
     * 
     * @param messages 消息历史
     * @param maxTokens 最大生成 token 数
     * @param temperature 温度参数 (0-1)
     * @return 生成的文本
     */
    String generateChat(String messages, int maxTokens, float temperature);

    /**
     * 获取当前加载的模型名称
     */
    String getModelName();

    /**
     * 检查模型是否已加载
     */
    boolean isModelLoaded();

    /**
     * 释放模型资源
     */
    void release();

    /**
     * 创建推理引擎实例
     * 使用 MediaPipe LLM Inference API
     */
    static ModelInferenceEngine createEngine() {
        Log.i("ModelInferenceEngine", "Creating MediaPipe inference engine");
        return new MediaPipeInferenceEngine();
    }
}
