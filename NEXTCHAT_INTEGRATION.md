# NextChat 集成指南

本指南说明如何将 NextChat (ChatGPT Next Web) 与 TokenEdge 提供的大模型 API 服务集成。

## 前置条件

1. TokenEdge 应用已安装并运行
2. 模型已下载并成功启动
3. 手机和电脑在同一 Wi-Fi 网络

## 步骤 1：获取 API 地址

在 TokenEdge 应用中：
1. 启动模型服务
2. 查看显示的服务地址（例如：`http://192.168.1.100:3000`）

## 步骤 2：下载并启动 NextChat

### 方式 A：使用 Web 版本（推荐）

1. 访问 NextChat 官方网站：https://app.nextchat.dev/
2. 或访问国内镜像：https://chat.next-ai.top/

### 方式 B：使用桌面版

1. 从 GitHub Releases 下载：https://github.com/ChatGPTNextWeb/NextChat/releases
2. 选择适合你系统的版本安装

### 方式 C：本地运行（开发者）

```bash
# 克隆项目
git clone https://github.com/ChatGPTNextWeb/NextChat.git
cd NextChat

# 安装依赖并运行
npm install
npm run dev
```

## 步骤 3：配置 NextChat

### 方法 1：通过设置界面配置

1. 打开 NextChat
2. 点击左下角的「设置」图标
3. 找到「自定义接口」或「API 设置」部分
4. 填写以下信息：

| 配置项 | 值 |
|--------|-----|
| **接口地址** | `http://[你的手机IP]:3000` |
| **API Key** | 任意字符串（如 `tokenedge`） |
| **模型** | `gpt-3.5-turbo`（或任意模型名） |

### 方法 2：使用环境变量（本地运行）

创建 `.env.local` 文件：

```env
OPENAI_API_KEY=tokenedge
BASE_URL=http://192.168.1.100:3000
```

## 步骤 4：验证连接

1. 在 NextChat 中新建对话
2. 发送测试消息，例如：`你好`
3. 如果收到回复，说明连接成功！

## API 端点说明

TokenEdge 提供以下 OpenAI 兼容的 API 端点：

| 端点 | 说明 |
|------|------|
| `POST /v1/chat/completions` | 聊天补全（推荐使用） |
| `POST /v1/completions` | 文本补全 |
| `GET /v1/models` | 获取模型列表 |

### 请求示例

#### 聊天补全

```bash
curl -X POST http://192.168.1.100:3000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer tokenedge" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [
      {"role": "user", "content": "你好"}
    ]
  }'
```

#### 文本补全

```bash
curl -X POST http://192.168.1.100:3000/v1/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer tokenedge" \
  -d '{
    "model": "gpt-3.5-turbo",
    "prompt": "你好，",
    "max_tokens": 100
  }'
```

## 常见问题

### Q: 连接失败怎么办？

1. 确认手机和电脑在同一 Wi-Fi
2. 检查 TokenEdge 服务是否正在运行
3. 确认 IP 地址和端口号正确
4. 尝试关闭手机防火墙

### Q: 响应速度慢？

- 使用更小的模型（如 Gemma 4 E2B Q4）
- 减小上下文长度
- 确保手机性能足够

### Q: 支持哪些客户端？

除了 NextChat，还支持：
- ChatBox
- Lobe Chat
- OpenCat
- 任何 OpenAI API 兼容的客户端

## 其他兼容客户端

### ChatBox

1. 下载：https://chatboxai.app/
2. 设置 → 提供商 → OpenAI
3. 接口地址：`http://[IP]:3000`
4. API Key：任意

### Lobe Chat

1. 下载：https://lobehub.com/
2. 设置 → 语言模型 → OpenAI
3. 代理地址：`http://[IP]:3000`
4. API Key：任意

## 技术说明

TokenEdge 的 API 服务基于 NanoHTTPD 实现，完全兼容 OpenAI API v1 规范。

### 响应格式

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
        "content": "回复内容"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 20,
    "total_tokens": 30
  }
}
```

## 许可证

本集成指南遵循 TokenEdge 项目的许可证。
