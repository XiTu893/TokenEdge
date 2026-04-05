import express from 'express';
import cors from 'cors';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import { getLlama, LlamaChatSession } from 'node-llama-cpp';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const config = JSON.parse(process.argv[2] || '{}');
const app = express();
const PORT = config.port || 8080;

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
        const llama = await getLlama();
        const model = await llama.loadModel({ modelPath: config.modelPath });
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
        modelLoaded: !!session
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
            responseText = `收到您的输入：${prompt}\n\n(这是一个模拟响应)`;
        }

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
    res.sendFile(path.join(__dirname, 'index.html'));
});

// 初始化模型并启动服务器
initializeModel().then(() => {
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`Server running on http://0.0.0.0:${PORT}`);
        console.log(`Health check: http://0.0.0.0:${PORT}/health`);
        console.log(`API docs: http://0.0.0.0:${PORT}/v1/models`);
        console.log(`Model status: ${session ? 'Loaded' : 'Not loaded'}`);
    });
});
