# TokenEdge - 边缘设备部署大模型 Token API 服务

## 项目介绍

TokenEdge 是一个参考 Google Edge Gallery 设计风格的项目，提供 Android 和 PC 端应用，用于纯本地离线运行 **Gemma 4** 大模型，对外提供标准 API 服务。

**核心特点：**

- 📱 支持 Android 和 PC 双端
- 🚀 纯本地离线运行，无需联网
- 📦 支持在线下载模型 / 本地文件选择
- 🌐 完美解决国内下载慢问题（4个国内源自动重试）
- 🔌 一键部署、开箱即用
- 🎯 兼容 OpenAI API 格式，可直接对接 ChatBox、NextChat 等客户端
- 💬 内置聊天界面，自动配置 API，无需手动操作

## 作者信息

**溪土红薯（XiTu893）**

- GitHub: <https://github.com/XiTu893>
- 邮箱: <28491599@qq.com>
- 所在地: 上海
- 工作室: 溪土工作室

## 下载

- [完整项目 ZIP 下载](#)
- [Android APK 下载](#)

## 支持的模型

本项目**仅支持 Gemma 4 系列模型**（LiteRT 格式）：

| 模型 | 大小 | 内存要求 | 上下文 | 特点 |
|------|------|----------|--------|------|
| **Gemma 4 E2B** | 2.5GB | 4-6GB | 8K/32K/64K/128K | 端侧版 · 2.3B 有效参数 · 极致轻量 · 适合手机和 IoT 设备 |
| **Gemma 4 E4B** | 5GB | 6-8GB | 8K/32K/64K/128K | 端侧版 · 4.5B 有效参数 · 更强性能 · 适合高端手机 |
| **Gemma 4 26B-A4B** | 18GB | 12-16GB | 32K/64K/128K/256K | 工作站版 · 混合专家模型 · 260亿总参数/38亿有效参数 · 适合 PC/工作站 |
| **Gemma 4 31B Dense** | 20GB | 16-24GB | 32K/64K/128K/256K | 旗舰版 · 纯稠密模型 · 310亿参数 · 全球前三性能 · 适合高端工作站 |

**关于 Gemma 4**

Gemma 4 是 Google 于 2026 年 4 月 3 日发布的最新一代开源大模型系列，采用 Apache 2.0 许可证：

- ✅ 256K 上下文窗口（工作站型号）
- ✅ 128K 上下文窗口（端侧型号）
- ✅ 原生多模态支持（文本 + 图像 + 语音）
- ✅ 140+ 语言支持
- ✅ 原生函数调用（Function Calling）
- ✅ 思考模式（Thinking Mode）
- ✅ 极致性能提升（数学推理 +68%，代码能力 +20x）

**完整 Gemma 4 模型规格：**

| 型号 | 总参数量 | 有效/激活参数 | 上下文 | 目标设备 |
|------|----------|--------------|--------|----------|
| E2B | 51亿 | 23亿 | 128K | 手机、树莓派 |
| E4B | 80亿 | 45亿 | 128K | 手机、Jetson |
| 26B MoE | 260亿 | 38亿 | 256K | 工作站、Agent |
| 31B Dense | 310亿 | 310亿 | 256K | 单卡 H100 |

## 国内下载加速

每个模型都配置了 **4 个下载源**，自动重试直到成功：

1. 🚀 **ModelScope**（国内首选）
2. 🔗 **ghproxy.com**（GitHub 代理）
3. 📦 **hf-mirror.com**（Hugging Face 镜像）
4. 🌐 **Hugging Face 官方**（备用）

## 核心功能

### 🎨 极简 UI

- Google 原生 Material Design 风格
- 简洁直观的操作界面

### 📦 模型管理

- **在线下载**: 4 个国内源自动重试
- **本地导入**: 支持选择本地模型文件
- **内存检测**: 自动检测手机内存，推荐合适的模型
- **上下文选择**: 支持 8K/32K/64K/128K 上下文窗口

### 🔌 本地 API 服务

- 无需联网，本机提供大模型接口
- 标准 OpenAI 兼容 RESTful API
- 前台服务运行，通知栏状态显示
- 端口自动分配（从 3000 开始）
- 同一时间仅一个服务运行

### 💬 内置聊天界面

- 自动获取本机 IP 和端口
- 自动配置 API，无需手动操作
- 支持用户和助手消息气泡
- 支持发送和回车发送
- 自动滚动到底部

### 🇨🇳 国内优化

- 彻底解决官方模型下载慢 / 无法下载问题
- 4 个下载源自动重试
- 支持本地模型导入

## API 接口说明

### 服务地址

```
http://手机IP:端口/v1/chat/completions
```

### 请求方式

```http
POST /v1/chat/completions
Content-Type: application/json
Authorization: Bearer tokenedge
```

### 请求示例（兼容 OpenAI 格式）

```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "user",
      "content": "你好"
    }
  ],
  "max_tokens": 512,
  "temperature": 0.7
}
```

### 响应示例

```json
{
  "id": "chatcmpl-xxx",
  "object": "chat.completion",
  "created": 1234567890,
  "model": "gpt-3.5-turbo",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你好！有什么我可以帮助你的吗？"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 50,
    "total_tokens": 60
  }
}
```

### 其他接口

- `GET /v1/models` - 获取模型列表
- `POST /v1/completions` - 文本补全接口
- `GET /health` - 健康检查

## 使用教程

### Android 端

1. 安装 APK 或导入源码编译
2. 打开 APP → 选择模型 → 点击下载
   - 4 个下载源自动重试，无需手动选择
3. 下载完成后，点击"启动"
4. 服务启动成功后：
   - **方式 1**：点击"💬 打开聊天"，直接在应用内对话
   - **方式 2**：复制 API 地址，在其他工具（如 NextChat、ChatBox）中使用

### PC 端

1. 进入 PC 目录
2. 使用 Bun 一键运行
   ```bash
   cd PC
   # Windows
   run.bat
   # Mac/Linux
   ./run.sh
   ```
3. 选择或下载模型
4. 启动服务，使用 API

## GitHub Actions 自动编译

项目已配置 GitHub Actions 工作流，每次 push 到 main/master 分支都会自动编译 APK：

### 使用步骤

1. 生成 Gradle Wrapper（在 Android 目录下）：
   ```bash
   cd Android
   gradle wrapper --gradle-version 8.0
   ```
2. 提交代码到 GitHub
3. 打开 GitHub 仓库 → Actions 标签
4. 查看 "Build Android APK" 工作流
5. 下载编译好的 APK Artifacts

### APK 下载位置

| 版本 | Artifacts 名称 |
|------|----------------|
| Debug APK | `app-debug` |
| Release APK | `app-release` |

## 项目结构

```
AIEdge/
├── README.md                  # 本说明文件
├── BUILD_GUIDE.md             # 详细构建指南
├── NEXTCHAT_INTEGRATION.md   # NextChat 集成指南
├── .github/
│   └── workflows/
│       └── build-android.yml  # GitHub Actions 自动编译工作流
├── PC/                        # PC 端应用
│   ├── package.json
│   ├── bunfig.toml
│   ├── main.js
│   ├── server.js
│   ├── downloader.js
│   ├── index.html
│   ├── run.bat               # Windows 一键启动
│   ├── run.sh                # Mac/Linux 一键启动
│   └── README.md
└── Android/                   # Android 端应用
    ├── build.gradle
    ├── settings.gradle
    ├── gradle.properties
    ├── GENERATE_GRADLE_WRAPPER.md
    ├── MODEL_INTEGRATION_GUIDE.md
    ├── gradle/
    │   └── wrapper/
    │       └── gradle-wrapper.properties
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── assets/
            ├── java/com/XiTu893/TokenEdge/
            │   ├── MainActivity.java
            │   ├── ChatActivity.java
            │   ├── adapter/
            │   │   ├── ModelAdapter.java
            │   │   └── MessageAdapter.java
            │   ├── model/
            │   │   ├── ModelConfig.java
            │   │   └── ChatMessage.java
            │   └── service/
            │       ├── ApiService.java
            │       ├── NanoHttpServer.java
            │       ├── DownloadService.java
            │       ├── ServiceManager.java
            │       ├── ModelInferenceEngine.java
            │       └── MediaPipeInferenceEngine.java
            └── res/
```

## 特性总结

- ✅ **仅支持 Gemma 4 系列**: Google 最新开源大模型
- ✅ 完整源码 ZIP: 可二次开发、商用、自定义
- ✅ GitHub Actions 自动编译: 每次 push 自动生成 APK
- ✅ 解决国内下载难题: 4个国内源自动重试
- ✅ 内置作者信息: 溪土红薯（XiTu893）
- ✅ 标准大模型 API: 兼容 OpenAI 生态，开箱即用
- ✅ 内置聊天界面: 自动配置 API，无需手动操作

## 技术栈

### Android 端

- **语言**: Java
- **最低 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 34 (Android 14)
- **推理引擎**: MediaPipe LLM Inference API
- **Web 服务器**: NanoHTTPD
- **UI 框架**: Material Design Components

### PC 端

- **运行时**: Bun
- **UI 框架**: 原生 HTML/CSS/JavaScript
- **Web 服务器**: Node.js HTTP

## 许可证

MIT License
