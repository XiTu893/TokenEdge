const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const config = JSON.parse(process.argv[2] || '{}');
const app = express();
const PORT = config.port || 8080;

app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => {
    res.json({ status: 'ok', model: config.modelPath || 'Not set', port: PORT });
});

app.post('/v1/chat/completions', async (req, res) => {
    const { model, messages, max_tokens = 512, temperature = 0.7 } = req.body;

    try {
        const lastMessage = messages && messages.length > 0
            ? messages[messages.length - 1].content
            : '';

        const id = 'chatcmpl-' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        const created = Math.floor(Date.now() / 1000);

        res.json({
            id: id,
            object: 'chat.completion',
            created: created,
            model: model || 'gemma-4',
            choices: [{
                index: 0,
                message: {
                    role: 'assistant',
                    content: `收到您的问题: ${lastMessage}\n\n(这是一个模拟响应，实际需要集成模型推理)\n\n配置信息：\n- 模型: ${config.modelId || 'gemma-4'}\n- 上下文: ${config.contextSize || 8192}\n- 端口: ${PORT}`
                },
                finish_reason: 'stop'
            }],
            usage: {
                prompt_tokens: 10,
                completion_tokens: 50,
                total_tokens: 60
            }
        });
    } catch (error) {
        res.status(500).json({
            error: {
                message: error.message,
                type: 'internal_error'
            }
        });
    }
});

app.post('/v1/completions', async (req, res) => {
    const { model, prompt, max_tokens = 512, temperature = 0.7 } = req.body;

    try {
        const id = 'cmpl-' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        const created = Math.floor(Date.now() / 1000);

        res.json({
            id: id,
            object: 'text_completion',
            created: created,
            model: model || 'gemma-4',
            choices: [{
                text: `完成文本: ${prompt}...\n\n(这是一个模拟响应)`,
                index: 0,
                logprobs: null,
                finish_reason: 'stop'
            }],
            usage: {
                prompt_tokens: 10,
                completion_tokens: 50,
                total_tokens: 60
            }
        });
    } catch (error) {
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
        object: 'list',
        data: [
            { id: 'gemma-4-e2b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-e4b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-26b-a4b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-31b-dense', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gpt-3.5-turbo', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' }
        ]
    });
});

app.get('/', (req, res) => {
    res.send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>TokenEdge API</title>
            <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
                h1 { color: #1a73e8; }
                .info { background: #e8f0fe; padding: 20px; border-radius: 8px; margin: 20px 0; }
                .api-endpoint { background: #f1f3f4; padding: 15px; border-radius: 8px; margin: 10px 0; font-family: 'Courier New', monospace; }
                code { background: #f1f3f4; padding: 2px 6px; border-radius: 4px; }
            </style>
        </head>
        <body>
            <h1>🚀 TokenEdge API</h1>
            <div class="info">
                <strong>服务状态:</strong> 运行中<br>
                <strong>端口:</strong> ${PORT}<br>
                <strong>模型:</strong> ${config.modelId || 'Not set'}<br>
                <strong>上下文:</strong> ${config.contextSize || 8192}<br>
                <strong>模型路径:</strong> ${config.modelPath || 'Not set'}
            </div>
            <h2>API 端点</h2>
            <div class="api-endpoint">POST /v1/chat/completions</div>
            <div class="api-endpoint">POST /v1/completions</div>
            <div class="api-endpoint">GET /v1/models</div>
            <div class="api-endpoint">GET /health</div>
            <h2>使用说明</h2>
            <p>使用 <code>http://localhost:${PORT}</code> 作为 API 基础地址</p>
            <p>API Key 可以是任意字符串</p>
        </body>
        </html>
    `);
});

app.listen(PORT, () => {
    console.log(`TokenEdge API Server running on http://localhost:${PORT}`);
    console.log(`Model path: ${config.modelPath || 'Not configured'}`);
    console.log(`Model: ${config.modelId || 'Not set'}`);
    console.log(`Context size: ${config.contextSize || 8192}`);
});
