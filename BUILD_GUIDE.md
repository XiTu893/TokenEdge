# AIEdge 项目构建指南

## 项目概述

AIEdge 是一个参考 Google Edge Gallery 的项目，提供 Android 和 PC 端应用，用于直接部署 Gemma 4 大模型并提供 API 服务。

**作者**: 溪土红薯
**GitHub**: [https://github.com/XiTu893](https://github.com/XiTu893)

## 项目结构

```
AIEdge/
├── README.md                    # 项目说明
├── BUILD_GUIDE.md               # 本构建指南
├── PC/                          # PC 端应用
│   ├── package.json             # 项目配置
│   ├── bunfig.toml              # Bun 配置
│   ├── main.js                  # Electron 主进程
│   ├── server.js                # API 服务器
│   ├── downloader.js            # 模型下载器
│   ├── index.html               # 前端界面
│   ├── run.bat                  # Windows 一键启动脚本
│   ├── run.sh                   # Mac/Linux 一键启动脚本
│   └── .gitignore               # Git 忽略文件
└── Android/                     # Android 端应用
    ├── build.gradle
    ├── settings.gradle
    ├── gradle.properties
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/aiedge/app/
            └── res/
```

## PC 端构建指南

### 前置要求

- Node.js (v16 或更高版本)
- npm 或 yarn

### 安装依赖

```bash
cd PC
npm install
```

### 运行开发版本

```bash
npm start
```

### 打包应用

```bash
npm run build
```

打包后的应用位于 `PC/dist/` 目录。

## Android 端构建指南

### 前置要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 34
- Gradle 8.0

### 使用 Android Studio 构建

1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择 `Android/` 目录
4. 等待 Gradle 同步完成
5. 连接 Android 设备或启动模拟器
6. 点击 "Run" 按钮或按 Shift+F10

### 命令行构建

#### Debug 版本

```bash
cd Android
./gradlew assembleDebug
```

APK 位置: `Android/app/build/outputs/apk/debug/app-debug.apk`

#### Release 版本

1. 配置签名密钥 (在 `app/build.gradle` 中)
2. 运行:

```bash
cd Android
./gradlew assembleRelease
```

APK 位置: `Android/app/build/outputs/apk/release/app-release.apk`

## 功能特性

### PC 端功能

- ✅ 模型管理 (下载、本地选择)
- ✅ 自定义下载地址 (支持国内镜像)
- ✅ API 服务启动/停止
- ✅ 实时日志查看
- ✅ API 测试功能

### Android 端功能

- ✅ 模型下载 (前台服务)
- ✅ 本地模型选择
- ✅ API 服务 (前台服务)
- ✅ API 测试界面
- ✅ 通知栏状态显示

## API 接口文档

### 基础 URL

```
http://localhost:8080
```

### 健康检查

```http
GET /health
```

响应示例:
```json
{
  "status": "ok",
  "model": "/path/to/model"
}
```

### 聊天接口

```http
POST /api/chat
Content-Type: application/json

{
  "prompt": "你好",
  "maxTokens": 512,
  "temperature": 0.7
}
```

响应示例:
```json
{
  "success": true,
  "response": "收到您的问题: 你好\n\n(响应内容)",
  "model": "gemma-4"
}
```

### 文本补全

```http
POST /api/completions
Content-Type: application/json

{
  "prompt": "从前有座山",
  "maxTokens": 512,
  "temperature": 0.7
}
```

### 模型列表

```http
GET /api/models
```

## 注意事项

1. **模型推理**: 当前版本包含模拟响应，实际推理需要集成 llama.cpp 或其他推理引擎
2. **国内下载**: 支持配置国内镜像源解决下载慢的问题
3. **Android 权限**: 需要授予存储和通知权限

## 下载地址

- [完整项目 ZIP 下载](.)
- [Android APK 下载](Android/app/build/outputs/apk/)

## 许可证

MIT License
