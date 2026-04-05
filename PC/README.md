# TokenEdge PC 端

TokenEdge PC 端 - 边缘设备部署大模型 Token API 服务

## 功能特性

- ✅ 支持 14 个领先大模型（Gemma 4、Llama 4、Qwen3.5、Phi-4、Mistral、DeepSeek R1）
- ✅ 4 个国内下载源自动重试（ModelScope、ghproxy.com、hf-mirror.com、Hugging Face）
- ✅ 美观的 Google 风格 UI
- ✅ 内存检测和模型推荐
- ✅ OpenAI 兼容 API 服务
- ✅ 完整的状态管理
- ✅ 配置持久化

## 支持的模型

### 📊 完整模型列表

| 系列 | 模型 | 大小 | 内存要求 | 特点 |
|------|------|------|----------|------|
| **Gemma 4** | E2B | 2.5GB | 4-6GB | 端侧版 · 2.3B 有效参数 · 极致轻量 |
| **Gemma 4** | E4B | 5GB | 6-8GB | 端侧版 · 4.5B 有效参数 · 更强性能 |
| **Gemma 4** | 26B-A4B | 18GB | 12-16GB | 工作站版 · 混合专家模型 |
| **Gemma 4** | 31B Dense | 20GB | 16-24GB | 旗舰版 · 纯稠密模型 · 全球前三性能 |
| **Llama 4** | Scout | 8GB | 8-12GB | Meta 出品 · 17B 总参数/16E MoE · 多模态原生支持 |
| **Llama 4** | Maverick | 28GB | 24-32GB | Meta 出品 · 17B 总参数/128E MoE · 顶级性能 |
| **Qwen3.5** | 27B, 35B-A3B, 122B-A10B | 16-70GB | 12-64GB | 阿里 2026 新作 · 中文最强 |
| **Phi-4** | Mini | 4GB | 4-6GB | 微软出品 · 3.8B 参数 · 小身材大智慧 |
| **Phi-4** | 14B | 11GB | 12-16GB | 微软出品 · 14B 参数 · 思维链推理 |
| **Mistral** | Nemo | 9GB | 8-12GB | Mistral AI 出品 · 12B 参数 · 高效快速 |
| **Mistral** | Large | 16GB | 16-24GB | Mistral AI 出品 · 24B 参数 · 欧洲最强 |
| **DeepSeek R1** | 7B | 5GB | 6-8GB | 深度求索 · 7B 蒸馏版 · 推理能力突出 |
| **DeepSeek R1** | 32B | 20GB | 16-24GB | 深度求索 · 32B 蒸馏版 · 推理能力顶级 |

### 🎯 按内存推荐

| 内存大小 | 推荐模型 |
|----------|----------|
| **4-6GB** | Gemma 4 E2B, Phi-4 Mini |
| **6-8GB** | Gemma 4 E4B, Qwen2.5 7B, DeepSeek R1 7B |
| **8-12GB** | Llama 4 Scout, Mistral Nemo |
| **12-16GB** | Gemma 4 26B-A4B, Qwen2.5 14B, Phi-4 14B |
| **16-24GB** | Gemma 4 31B Dense, Qwen2.5 32B, Mistral Large, DeepSeek R1 32B |
| **24-32GB** | Llama 4 Maverick |

### 🏆 模型特点总结

**综合性能最强**：
- 🥇 Llama 4 Scout/Maverick - Meta 出品，生态最强
- 🥈 Gemma 4 31B Dense - Google 旗舰，全球前三
- 🥉 Qwen2.5 32B - 阿里出品，中文最强

**推理能力最强**：
- 🥇 DeepSeek R1 32B - 媲美 o1 水平
- 🥈 Phi-4 14B - 思维链推理
- 🥉 DeepSeek R1 7B - 小身材大智慧

**中文支持最好**：
- 🥇 Qwen3.5 系列 - 阿里 2026 新作，最强中文模型
- 🥈 Qwen2.5 系列 - 阿里出品，中文场景首选
- 🥉 DeepSeek R1 系列 - 深度求索，中文优化

**代码能力最强**：
- 🥇 Phi-4 系列 - 微软出品，代码/数学/逻辑全能
- 🥈 Qwen2.5 系列 - 代码生成优秀
- 🥉 Llama 4 系列 - 综合能力强

**速度最快**：
- 🥇 Gemma 4 E2B - 2.3B 有效参数，极速推理
- 🥈 Phi-4 Mini - 3.8B 参数，轻量快速
- 🥉 Mistral Nemo - 12B 参数，高效快速

## 快速开始

### 方式一：使用编译好的安装包（推荐）

1. 访问 [GitHub Actions](https://github.com/XiTu893/TokenEdge/actions)
2. 选择最新的 "Build PC Application" 工作流
3. 下载对应系统的安装包：
   - **Windows**: 下载 `TokenEdge-Windows.zip`，解压后运行 `.exe` 安装包
   - **Linux**: 下载 `TokenEdge-Linux.zip`，解压后运行 `.AppImage` 文件
   - **macOS**: 下载 `TokenEdge-macOS.zip`，解压后运行 `.dmg` 安装包
4. 安装完成后直接运行

### 方式二：本地源码运行

#### 前置要求

- Bun 1.0+（推荐使用 Bun，速度更快）
- 或者 Node.js 18+

#### 安装依赖

```bash
# 使用 Bun（推荐）
bun install

# 或使用 npm
npm install
```

#### 运行

```bash
# 使用 Bun
bun run start

# 或使用 npm
npm start
```

#### 开发模式

```bash
# 使用 Bun
bun run dev

# 或使用 npm
npm run dev
```

## 项目结构

```
PC/
├── main.js              # Electron 主进程
├── server.js            # API 服务器（集成 llama.cpp 推理）
├── downloader.js        # 模型下载器（支持多源重试）
├── index.html           # 主界面
├── test-chat.html       # 测试聊天界面
├── package.json         # 项目配置
├── bunfig.toml          # Bun 配置
├── model/
│   └── ModelConfig.js   # 模型配置（14 个领先模型）
├── run.bat              # Windows 一键启动
├── run.sh               # Mac/Linux 一键启动
└── README.md            # 本说明文件
```

## API 接口

### 服务地址

```
http://localhost:3000
```

### 接口说明

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 服务状态页面 |
| `/health` | GET | 健康检查 |
| `/v1/chat/completions` | POST | 聊天补全（OpenAI 兼容） |
| `/v1/completions` | POST | 文本补全（OpenAI 兼容） |
| `/v1/models` | GET | 模型列表 |

## 使用流程

1. 启动应用
2. 选择模型（会根据内存自动推荐）
3. 点击"📥 下载"按钮
   - 4 个下载源自动重试，无需手动选择
4. 下载完成后，点击"▶️ 启动"
5. 服务启动成功后，点击"💬 打开聊天"
6. 开始对话！

## 下载源

每个模型都配置了 4 个下载源，自动重试直到成功：

1. 🚀 **ModelScope**（国内首选）
2. 🔗 **ghproxy.com**（GitHub 代理）
3. 📦 **hf-mirror.com**（Hugging Face 镜像）
4. 🌐 **Hugging Face 官方**（备用）

## 模型格式说明

PC 端使用 **GGUF 格式**，这是 llama.cpp 原生支持的格式。GGUF（GGML Unified Format）是 llama.cpp 社区标准的模型格式，具有优秀的跨平台兼容性和加载性能。

所有模型会自动下载 GGUF 版本，无需手动转换。

## 作者信息

**溪土红薯（XiTu893）**

- GitHub: <https://github.com/XiTu893>
- 邮箱: <28491599@qq.com>
- 所在地: 上海
- 工作室: 溪土工作室

## 许可证

MIT License
