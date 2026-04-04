# TokenEdge 大模型推理集成指南

## 概述

本项目目前使用**模拟推理引擎**（MockInferenceEngine）进行演示。要获得真实的模型推理能力，需要集成以下其中一种推理后端：

1. **Google AI Edge MediaPipe LLM Inference API**（推荐）
2. **llama.cpp**（最流行的开源方案）

---

## 方案一：Google AI Edge MediaPipe LLM Inference API（推荐）

### 特点
- Google 官方方案
- 参考 Google AI Edge Gallery
- 支持 CPU/GPU/NPU 加速
- 开箱即用，无需复杂编译

### 集成步骤

#### 1. 添加依赖

在 `Android/app/build.gradle` 中添加：

```gradle
dependencies {
    // MediaPipe LLM Inference - 使用最新版本
    implementation 'com.google.mediapipe:tasks-genai:0.10.27'
}
```

注意：请查看 [MediaPipe 官方文档](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android) 获取最新版本号。

#### 2. 创建 MediaPipeInferenceEngine

创建新文件 `Android/app/src/main/java/com/XiTu893/TokenEdge/service/MediaPipeInferenceEngine.java`：

```java
package com.XiTu893.TokenEdge.service;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceOptions;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

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
        // 将 OpenAI 格式消息转换为 MediaPipe 格式
        String prompt = convertMessagesToPrompt(messages);
        return generate(prompt, maxTokens, temperature);
    }

    private String convertMessagesToPrompt(String messages) {
        // 简化实现：提取最后一条用户消息
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
```

#### 3. 更新 ModelInferenceEngine

修改 `ModelInferenceEngine.java` 中的 `createEngine()` 方法：

```java
static ModelInferenceEngine createEngine() {
    // 使用 MediaPipe 推理引擎
    Log.i("ModelInferenceEngine", "Creating MediaPipe inference engine");
    return new MediaPipeInferenceEngine();
}
```

---

## 方案二：llama.cpp

### 特点
- 最流行的开源大模型推理库
- 支持 GGUF 格式
- 活跃的社区支持
- 需要编译原生代码

### 集成步骤

#### 1. 克隆 llama.cpp

```bash
cd Android/app/src/main
git clone https://github.com/ggml-org/llama.cpp
```

#### 2. 添加 CMakeLists.txt

创建 `Android/app/src/main/cpp/CMakeLists.txt`：

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("tokenedge")

add_subdirectory(../llama.cpp)

add_library(tokenedge SHARED
    tokenedge_jni.cpp
)

target_link_libraries(tokenedge
    llama
    android
    log
)
```

#### 3. 创建 JNI 绑定

创建 `Android/app/src/main/cpp/tokenedge_jni.cpp`：

```cpp
#include <jni.h>
#include <string>
#include <android/log.h>
#include "llama.h"

#define LOG_TAG "TokenEdgeJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_context* ctx = nullptr;
static llama_model* model = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_XiTu893_TokenEdge_service_LlamaCppInferenceEngine_initializeModel(
        JNIEnv* env,
        jobject thiz,
        jstring model_path,
        jint context_size) {
    
    const char* path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing model: %s", path);
    
    llama_params params = llama_params_default();
    params.n_ctx = context_size;
    params.n_threads = 4;
    
    model = llama_load_model_from_file(path, params);
    if (!model) {
        LOGE("Failed to load model");
        env->ReleaseStringUTFChars(model_path, path);
        return JNI_FALSE;
    }
    
    ctx = llama_new_context_with_model(model, params);
    if (!ctx) {
        LOGE("Failed to create context");
        llama_free_model(model);
        model = nullptr;
        env->ReleaseStringUTFChars(model_path, path);
        return JNI_FALSE;
    }
    
    LOGI("Model initialized successfully");
    env->ReleaseStringUTFChars(model_path, path);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_XiTu893_TokenEdge_service_LlamaCppInferenceEngine_generateText(
        JNIEnv* env,
        jobject thiz,
        jstring prompt,
        jint max_tokens,
        jfloat temperature) {
    
    if (!model || !ctx) {
        return env->NewStringUTF("Model not loaded");
    }
    
    const char* text = env->GetStringUTFChars(prompt, nullptr);
    
    // 简化的推理逻辑
    std::string result = "Response from llama.cpp: ";
    result += text;
    
    env->ReleaseStringUTFChars(prompt, text);
    return env->NewStringUTF(result.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_XiTu893_TokenEdge_service_LlamaCppInferenceEngine_releaseModel(
        JNIEnv* env,
        jobject thiz) {
    
    LOGI("Releasing model");
    
    if (ctx) {
        llama_free(ctx);
        ctx = nullptr;
    }
    
    if (model) {
        llama_free_model(model);
        model = nullptr;
    }
}
```

#### 4. 创建 LlamaCppInferenceEngine

```java
package com.XiTu893.TokenEdge.service;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class LlamaCppInferenceEngine implements ModelInferenceEngine {
    private static final String TAG = "LlamaCppEngine";
    
    static {
        System.loadLibrary("tokenedge");
    }
    
    private native boolean initializeModel(String modelPath, int contextSize);
    private native String generateText(String prompt, int maxTokens, float temperature);
    private native void releaseModel();
    
    private String modelPath;
    private String modelId;
    private int contextSize;
    private boolean isLoaded = false;

    @Override
    public boolean initialize(Context context, String modelPath, String modelId, int contextSize) {
        Log.i(TAG, "Initializing llama.cpp engine");
        this.modelPath = modelPath;
        this.modelId = modelId;
        this.contextSize = contextSize;
        
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            Log.e(TAG, "Model file not found");
            return false;
        }
        
        isLoaded = initializeModel(modelPath, contextSize);
        return isLoaded;
    }

    @Override
    public String generate(String prompt, int maxTokens, float temperature) {
        if (!isLoaded) return "Model not loaded";
        return generateText(prompt, maxTokens, temperature);
    }

    @Override
    public String generateChat(String messages, int maxTokens, float temperature) {
        String prompt = extractLastUserMessage(messages);
        return generate(prompt, maxTokens, temperature);
    }
    
    private String extractLastUserMessage(String messages) {
        try {
            int lastContentIndex = messages.lastIndexOf("\"content\"");
            if (lastContentIndex == -1) return "";
            int quoteStart = messages.indexOf("\"", lastContentIndex + 10);
            int quoteEnd = messages.indexOf("\"", quoteStart + 1);
            return messages.substring(quoteStart + 1, quoteEnd);
        } catch (Exception e) {
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
        Log.i(TAG, "Releasing llama.cpp engine");
        if (isLoaded) {
            releaseModel();
            isLoaded = false;
        }
    }
}
```

#### 5. 更新 build.gradle

在 `Android/app/build.gradle` 中添加：

```gradle
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags ""
            }
        }
    }
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.22.1"
        }
    }
}
```

---

## MediaPipe 支持的模型格式

MediaPipe LLM Inference API 支持 **LiteRT 优化格式**的模型。这些模型可以从 [LiteRT 社区模型库](https://ai.google.dev/edge/models)获取。

### 可用的模型
- Gemma 2 (2B, 9B)
- Gemma 3n (2B-E2B)
- Phi 3 (3.8B)
- Mistral 7B
- 以及更多...

### 模型转换
如果你有 GGUF 格式的模型，可能需要先转换为 LiteRT 格式。请参考：
- https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference

---

## 模型下载

### 官方源（Hugging Face）
- Gemma 4 E2B: https://huggingface.co/google/gemma-4-e2b-it
- Gemma 4 E4B: https://huggingface.co/google/gemma-4-e4b-it
- Gemma 4 26B A4B: https://huggingface.co/google/gemma-4-26b-a4b-it
- Gemma 4 31B Dense: https://huggingface.co/google/gemma-4-31b-dense-it

### 国内镜像（ModelScope）
- Gemma 4 E2B: https://modelscope.cn/models/google/gemma-4-e2b-it
- Gemma 4 E4B: https://modelscope.cn/models/google/gemma-4-e4b-it
- Gemma 4 26B A4B: https://modelscope.cn/models/google/gemma-4-26b-a4b-it
- Gemma 4 31B Dense: https://modelscope.cn/models/google/gemma-4-31b-dense-it

---

## 测试

集成完成后，可以使用以下方式测试：

### 1. API 测试（OpenAI 格式）

```bash
curl http://localhost:3000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gemma-4-e2b",
    "messages": [{"role": "user", "content": "你好"}],
    "max_tokens": 512
  }'
```

### 2. 健康检查

```bash
curl http://localhost:3000/health
```

---

## 故障排除

### 问题：模型加载失败
- 检查模型文件是否完整下载
- 验证模型文件格式（需要 GGUF 格式）
- 检查设备内存是否足够

### 问题：推理速度慢
- 使用 Q4 或 Q8 量化模型
- 降低上下文窗口大小
- 使用 GPU/NPU 加速（MediaPipe）

---

## 参考资源

- Google AI Edge LLM Inference API: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference
- llama.cpp GitHub: https://github.com/ggml-org/llama.cpp
- Google AI Edge Gallery: https://github.com/google-ai-edge/gallery
