import express from 'express';
import cors from 'cors';
import path from 'path';
import fs from 'fs';

const config = JSON.parse(process.argv[2] || '{}');
const app = express();
const PORT = config.port || 8080;

app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    model: config.modelPath || 'Not set',
    port: PORT,
    modelLoaded: false
  });
});

interface ChatMessage {
  role: string;
  content: string;
}

interface ChatRequest {
  model?: string;
  messages: ChatMessage[];
  max_tokens?: number;
  temperature?: number;
}

interface CompletionRequest {
  model?: string;
  prompt: string;
  max_tokens?: number;
  temperature?: number;
}

app.post('/v1/chat/completions', async (req, res) => {
  const { model: modelName, messages, max_tokens = 512, temperature = 0.7 }: ChatRequest = req.body;

  try {
    const lastMessage = messages && messages.length > 0
      ? messages[messages.length - 1].content
      : '';

    const responseText = `收到您的问题：${lastMessage}\n\n这是一个测试响应，用于验证 API 服务是否正常运行。\n\n配置信息：\n- 模型：${config.modelId || 'gemma-4'}\n- 上下文：${config.contextSize || 8192}\n- 端口：${PORT}`;

    const id = 'chatcmpl-' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    const created = Math.floor(Date.now() / 1000);

    res.json({
      id: id,
      object: 'chat.completion',
      created: created,
      model: modelName || config.modelId || 'gemma-4',
      choices: [{
        index: 0,
        message: {
          role: 'assistant',
          content: responseText
        },
        finish_reason: 'stop'
      }],
      usage: {
        prompt_tokens: 10,
        completion_tokens: responseText.length / 4,
        total_tokens: 10 + Math.round(responseText.length / 4)
      }
    });
  } catch (error: any) {
    console.error('Chat error:', error.message);
    res.status(500).json({
      error: {
        message: error.message,
        type: 'internal_error'
      }
    });
  }
});

app.post('/v1/completions', async (req, res) => {
  const { model: modelName, prompt, max_tokens = 512, temperature = 0.7 }: CompletionRequest = req.body;

  try {
    const responseText = `收到您的输入：${prompt}\n\n这是一个测试响应。`;

    const id = 'cmpl-' + Math.random().toString(36).substring(2, 15);
    const created = Math.floor(Date.now() / 1000);

    res.json({
      id: id,
      object: 'text_completion',
      created: created,
      model: modelName || config.modelId || 'gemma-4',
      choices: [{
        index: 0,
        text: responseText,
        finish_reason: 'stop'
      }],
      usage: {
        prompt_tokens: 10,
        completion_tokens: responseText.length / 4,
        total_tokens: 10 + Math.round(responseText.length / 4)
      }
    });
  } catch (error: any) {
    console.error('Completion error:', error.message);
    res.status(500).json({
      error: {
        message: error.message,
        type: 'internal_error'
      }
    });
  }
});

app.get('/v1/models', (req, res) => {
  res.json({
    data: [
      {
        id: config.modelId || 'gemma-4',
        object: 'model',
        created: Math.floor(Date.now() / 1000),
        owned_by: 'token-edge'
      }
    ]
  });
});

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on http://0.0.0.0:${PORT}`);
  console.log(`Health check: http://0.0.0.0:${PORT}/health`);
  console.log(`API docs: http://0.0.0.0:${PORT}/v1/models`);
});
