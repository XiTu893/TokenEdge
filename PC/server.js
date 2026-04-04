const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const { LlamaChatSession, LlamaModel } = require('node-llama-cpp');

const config = JSON.parse(process.argv[2] || '{}');
const app = express();
const PORT = config.port || 8080;

let model = null;
let session = null;

app.use(cors());
app.use(express.json());

// 初始化模型
async function initializeModel() {
    if (!config.modelPath || !fs.existsSync(config.modelPath)) {
        console.log('Model not found:', config.modelPath);
        return;
    }

    try {
        console.log('Loading model:', config.modelPath);
        model = new LlamaModel({ modelPath: config.modelPath });
        session = new LlamaChatSession({ model });
        console.log('Model loaded successfully');
    } catch (error) {
        console.error('Failed to load model:', error.message);
    }
}

app.get('/health', (req, res) => {
    res.json({ 
        status: 'ok', 
        model: config.modelPath || 'Not set', 
        port: PORT,
        modelLoaded: !!model
    });
});

app.post('/v1/chat/completions', async (req, res) => {
    const { model: modelName, messages, max_tokens = 512, temperature = 0.7 } = req.body;

    try {
        const lastMessage = messages && messages.length > 0
            ? messages[messages.length - 1].content
            : '';

        let responseText = '';

        // 如果有模型，使用真实推理
        if (session) {
            responseText = await session.prompt(lastMessage, {
                maxTokens: max_tokens,
                temperature: temperature
            });
        } else {
            // 模拟响应
            responseText = `收到您的问题：${lastMessage}\n\n(这是一个模拟响应，模型未加载)\n\n配置信息：\n- 模型：${config.modelId || 'gemma-4'}\n- 上下文：${config.contextSize || 8192}\n- 端口：${PORT}`;
        }

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
    } catch (error) {
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
    const { model: modelName, prompt, max_tokens = 512, temperature = 0.7 } = req.body;

    try {
        let responseText = '';

        if (session) {
            responseText = await session.prompt(prompt, {
                maxTokens: max_tokens,
                temperature: temperature
            });
        } else {
            responseText = `完成文本：${prompt}...\n\n(这是一个模拟响应)`;
        }

        const id = 'cmpl-' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        const created = Math.floor(Date.now() / 1000);

        res.json({
            id: id,
            object: 'text_completion',
            created: created,
            model: modelName || config.modelId || 'gemma-4',
            choices: [{
                text: responseText,
                index: 0,
                logprobs: null,
                finish_reason: 'stop'
            }],
            usage: {
                prompt_tokens: 10,
                completion_tokens: responseText.length / 4,
                total_tokens: 10 + Math.round(responseText.length / 4)
            }
        });
    } catch (error) {
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
        object: 'list',
        data: [
            { id: 'gemma-4-e2b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-e4b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-26b-a4b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'gemma-4-31b-dense', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'llama-4-scout', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'qwen3.5-27b', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
            { id: 'phi-4-mini', object: 'model', created: Math.floor(Date.now() / 1000), owned_by: 'xi-tu-studio' },
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
                .status { display: inline-block; padding: 4px 12px; border-radius: 16px; font-weight: 600; }
                .status.ok { background: #e6f4ea; color: #137333; }
                .status.error { background: #fce8e6; color: #d93025; }
            </style>
        </head>
        <body>
            <h1>🚀 TokenEdge API</h1>
            <div class="info">
                <strong>服务状态:</strong> <span class="status ${model ? 'ok' : 'error'}">${model ? '✅ 运行中' : '❌ 模型未加载'}</span><br>
                <strong>端口:</strong> ${PORT}<br>
                <strong>模型:</strong> ${config.modelId || 'Not set'}<br>
                <strong>上下文:</strong> ${config.contextSize || 8192}<br>
                <strong>模型路径:</strong> ${config.modelPath || 'Not set'}<br>
                <strong>推理引擎:</strong> llama.cpp (node-llama-cpp)
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

// 启动服务器并加载模型
app.listen(PORT, async () => {
    console.log(`TokenEdge API Server running on http://localhost:${PORT}`);
    console.log(`Model path: ${config.modelPath || 'Not configured'}`);
    console.log(`Model: ${config.modelId || 'Not set'}`);
    console.log(`Context size: ${config.contextSize || 8192}`);
    console.log(`Inference engine: llama.cpp (node-llama-cpp)`);
    
    // 异步加载模型
    await initializeModel();
});
