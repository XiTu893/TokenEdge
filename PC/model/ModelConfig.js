class ModelConfig {
    constructor(id, name, displayName, sizeGB, sizeBytes, minRAMGB, recommendedRAMGB, contextOptions, defaultContextIndex, downloadUrls, description) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.sizeGB = sizeGB;
        this.sizeBytes = sizeBytes;
        this.minRAMGB = minRAMGB;
        this.recommendedRAMGB = recommendedRAMGB;
        this.contextOptions = contextOptions;
        this.defaultContextIndex = defaultContextIndex;
        this.downloadUrls = downloadUrls;
        this.description = description;
    }
}

const PC_MODELS = [
    // ========== Gemma 4 系列 ==========
    new ModelConfig(
        "gemma-4-e2b",
        "Gemma 4 E2B",
        "Gemma 4 E2B",
        "2.5",
        2684354560,
        4,
        6,
        [8192, 32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/gemma-4-E2B-it/resolve/master/gemma-4-E2B-it.gguf",
            "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-E2B-it/resolve/main/gemma-4-E2B-it.gguf",
            "https://hf-mirror.com/litert-community/gemma-4-E2B-it/resolve/main/gemma-4-E2B-it.gguf",
            "https://huggingface.co/litert-community/gemma-4-E2B-it/resolve/main/gemma-4-E2B-it.gguf"
        ],
        "端侧版 · 2.3B 有效参数 · 极致轻量 · 适合轻薄本"
    ),
    new ModelConfig(
        "gemma-4-e4b",
        "Gemma 4 E4B",
        "Gemma 4 E4B",
        "5",
        5368709120,
        6,
        8,
        [8192, 32768, 65536, 131072],
        2,
        [
            "https://modelscope.cn/models/litert-community/gemma-4-E4B-it-litert-lm/resolve/master/gemma-4-E4B-it.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
            "https://hf-mirror.com/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
            "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm"
        ],
        "端侧版 · 4.5B 有效参数 · 更强性能 · 适合主流 PC"
    ),
    new ModelConfig(
        "gemma-4-26b-a4b",
        "Gemma 4 26B-A4B",
        "Gemma 4 26B-A4B",
        "18",
        19327352832,
        12,
        16,
        [32768, 65536, 131072, 262144],
        2,
        [
            "https://modelscope.cn/models/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/master/gemma-4-26B-A4B-it.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm",
            "https://hf-mirror.com/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm",
            "https://huggingface.co/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm"
        ],
        "工作站版 · 混合专家模型 · 260 亿总参数/38 亿有效参数 · 适合游戏本/工作站"
    ),
    new ModelConfig(
        "gemma-4-31b-dense",
        "Gemma 4 31B Dense",
        "Gemma 4 31B Dense",
        "20",
        21474836480,
        16,
        24,
        [32768, 65536, 131072, 262144],
        2,
        [
            "https://modelscope.cn/models/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/master/gemma-4-31B-Dense-it.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm",
            "https://hf-mirror.com/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm",
            "https://huggingface.co/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm"
        ],
        "旗舰版 · 纯稠密模型 · 310 亿参数 · 全球前三性能 · 适合高端工作站"
    ),
    
    // ========== Llama 4 系列 ==========
    new ModelConfig(
        "llama-4-scout",
        "Llama 4 Scout",
        "Llama 4 Scout",
        "8",
        8589934592,
        8,
        12,
        [32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/Llama-4-Scout-17B-16E-litert-lm/resolve/master/Llama-4-Scout.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Llama-4-Scout-17B-16E-litert-lm/resolve/main/Llama-4-Scout.litertlm",
            "https://hf-mirror.com/litert-community/Llama-4-Scout-17B-16E-litert-lm/resolve/main/Llama-4-Scout.litertlm",
            "https://huggingface.co/litert-community/Llama-4-Scout-17B-16E-litert-lm/resolve/main/Llama-4-Scout.litertlm"
        ],
        "Meta 出品 · 17B 总参数/16E MoE · 多模态原生支持 · 200+ 语言 · 综合性能最强"
    ),
    new ModelConfig(
        "llama-4-maverick",
        "Llama 4 Maverick",
        "Llama 4 Maverick",
        "28",
        30064771072,
        24,
        32,
        [65536, 131072, 262144],
        1,
        [
            "https://modelscope.cn/models/litert-community/Llama-4-Maverick-17B-128E-litert-lm/resolve/master/Llama-4-Maverick.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Llama-4-Maverick-17B-128E-litert-lm/resolve/main/Llama-4-Maverick.litertlm",
            "https://hf-mirror.com/litert-community/Llama-4-Maverick-17B-128E-litert-lm/resolve/main/Llama-4-Maverick.litertlm",
            "https://huggingface.co/litert-community/Llama-4-Maverick-17B-128E-litert-lm/resolve/main/Llama-4-Maverick.litertlm"
        ],
        "Meta 出品 · 17B 总参数/128E MoE · 顶级性能 · 适合专业场景"
    ),
    
    // ========== Qwen3.5 系列 ==========
    new ModelConfig(
        "qwen3.5-27b",
        "Qwen 3.5 27B",
        "Qwen 3.5 27B",
        "16",
        17179869184,
        12,
        16,
        [32768, 65536, 131072, 262144],
        1,
        [
            "https://modelscope.cn/models/litert-community/Qwen3.5-27B-litert-lm/resolve/master/Qwen3.5-27B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Qwen3.5-27B-litert-lm/resolve/main/Qwen3.5-27B.litertlm",
            "https://hf-mirror.com/litert-community/Qwen3.5-27B-litert-lm/resolve/main/Qwen3.5-27B.litertlm",
            "https://huggingface.co/litert-community/Qwen3.5-27B-litert-lm/resolve/main/Qwen3.5-27B.litertlm"
        ],
        "阿里 2026 新作 · 27B 稠密 · 中文最强 · 代码/数学/推理全面升级"
    ),
    new ModelConfig(
        "qwen3.5-35b-a3b",
        "Qwen 3.5 35B-A3B",
        "Qwen 3.5 35B-A3B",
        "20",
        21474836480,
        16,
        24,
        [65536, 131072, 262144],
        1,
        [
            "https://modelscope.cn/models/litert-community/Qwen3.5-35B-A3B-litert-lm/resolve/master/Qwen3.5-35B-A3B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Qwen3.5-35B-A3B-litert-lm/resolve/main/Qwen3.5-35B-A3B.litertlm",
            "https://hf-mirror.com/litert-community/Qwen3.5-35B-A3B-litert-lm/resolve/main/Qwen3.5-35B-A3B.litertlm",
            "https://huggingface.co/litert-community/Qwen3.5-35B-A3B-litert-lm/resolve/main/Qwen3.5-35B-A3B.litertlm"
        ],
        "阿里 2026 新作 · MoE 架构 · 35B 总参数/3B 激活 · 高效推理"
    ),
    new ModelConfig(
        "qwen3.5-122b-a10b",
        "Qwen 3.5 122B-A10B",
        "Qwen 3.5 122B-A10B",
        "70",
        75161927680,
        48,
        64,
        [65536, 131072, 262144],
        1,
        [
            "https://modelscope.cn/models/litert-community/Qwen3.5-122B-A10B-litert-lm/resolve/master/Qwen3.5-122B-A10B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Qwen3.5-122B-A10B-litert-lm/resolve/main/Qwen3.5-122B-A10B.litertlm",
            "https://hf-mirror.com/litert-community/Qwen3.5-122B-A10B-litert-lm/resolve/main/Qwen3.5-122B-A10B.litertlm",
            "https://huggingface.co/litert-community/Qwen3.5-122B-A10B-litert-lm/resolve/main/Qwen3.5-122B-A10B.litertlm"
        ],
        "阿里 2026 新作 · MoE 架构 · 122B 总参数/10B 激活 · 旗舰级性能"
    ),
    
    // ========== Phi-4 系列 ==========
    new ModelConfig(
        "phi-4-mini",
        "Phi-4 Mini",
        "Phi-4 Mini",
        "4",
        4294967296,
        4,
        6,
        [16384, 32768, 65536],
        1,
        [
            "https://modelscope.cn/models/litert-community/Phi-4-mini-instruct-litert-lm/resolve/master/Phi-4-mini-instruct.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Phi-4-mini-instruct-litert-lm/resolve/main/Phi-4-mini-instruct.litertlm",
            "https://hf-mirror.com/litert-community/Phi-4-mini-instruct-litert-lm/resolve/main/Phi-4-mini-instruct.litertlm",
            "https://huggingface.co/litert-community/Phi-4-mini-instruct-litert-lm/resolve/main/Phi-4-mini-instruct.litertlm"
        ],
        "微软出品 · 3.8B 参数 · 小身材大智慧 · 代码/推理优秀"
    ),
    new ModelConfig(
        "phi-4",
        "Phi-4",
        "Phi-4",
        "11",
        11811160064,
        12,
        16,
        [16384, 32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/Phi-4-litert-lm/resolve/master/Phi-4.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Phi-4-litert-lm/resolve/main/Phi-4.litertlm",
            "https://hf-mirror.com/litert-community/Phi-4-litert-lm/resolve/main/Phi-4.litertlm",
            "https://huggingface.co/litert-community/Phi-4-litert-lm/resolve/main/Phi-4.litertlm"
        ],
        "微软出品 · 14B 参数 · 思维链推理 · 代码/数学/逻辑全能"
    ),
    
    // ========== Mistral 系列 ==========
    new ModelConfig(
        "mistral-nemo",
        "Mistral Nemo",
        "Mistral Nemo",
        "9",
        9663676416,
        8,
        12,
        [32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/Mistral-Nemo-12B-litert-lm/resolve/master/Mistral-Nemo-12B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Mistral-Nemo-12B-litert-lm/resolve/main/Mistral-Nemo-12B.litertlm",
            "https://hf-mirror.com/litert-community/Mistral-Nemo-12B-litert-lm/resolve/main/Mistral-Nemo-12B.litertlm",
            "https://huggingface.co/litert-community/Mistral-Nemo-12B-litert-lm/resolve/main/Mistral-Nemo-12B.litertlm"
        ],
        "Mistral AI 出品 · 12B 参数 · 高效快速 · 多语言支持优秀"
    ),
    new ModelConfig(
        "mistral-large",
        "Mistral Large",
        "Mistral Large",
        "16",
        17179869184,
        16,
        24,
        [32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/Mistral-Large-24B-litert-lm/resolve/master/Mistral-Large-24B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/Mistral-Large-24B-litert-lm/resolve/main/Mistral-Large-24B.litertlm",
            "https://hf-mirror.com/litert-community/Mistral-Large-24B-litert-lm/resolve/main/Mistral-Large-24B.litertlm",
            "https://huggingface.co/litert-community/Mistral-Large-24B-litert-lm/resolve/main/Mistral-Large-24B.litertlm"
        ],
        "Mistral AI 出品 · 24B 参数 · 旗舰性能 · 欧洲最强开源模型"
    ),
    
    // ========== DeepSeek R1 系列 ==========
    new ModelConfig(
        "deepseek-r1-7b",
        "DeepSeek R1 7B",
        "DeepSeek R1 7B",
        "5",
        5368709120,
        6,
        8,
        [16384, 32768, 65536],
        1,
        [
            "https://modelscope.cn/models/litert-community/DeepSeek-R1-Distill-Qwen-7B-litert-lm/resolve/master/DeepSeek-R1-Distill-Qwen-7B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-7B-litert-lm/resolve/main/DeepSeek-R1-Distill-Qwen-7B.litertlm",
            "https://hf-mirror.com/litert-community/DeepSeek-R1-Distill-Qwen-7B-litert-lm/resolve/main/DeepSeek-R1-Distill-Qwen-7B.litertlm",
            "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-7B-litert-lm/resolve/main/DeepSeek-R1-Distill-Qwen-7B.litertlm"
        ],
        "深度求索 · 7B 蒸馏版 · 推理能力突出 · 数学/代码/逻辑强"
    ),
    new ModelConfig(
        "deepseek-r1-32b",
        "DeepSeek R1 32B",
        "DeepSeek R1 32B",
        "20",
        21474836480,
        16,
        24,
        [32768, 65536, 131072],
        1,
        [
            "https://modelscope.cn/models/litert-community/DeepSeek-R1-Distill-Llama-32B-litert-lm/resolve/master/DeepSeek-R1-Distill-Llama-32B.litertlm",
            "https://ghproxy.com/https://huggingface.co/litert-community/DeepSeek-R1-Distill-Llama-32B-litert-lm/resolve/main/DeepSeek-R1-Distill-Llama-32B.litertlm",
            "https://hf-mirror.com/litert-community/DeepSeek-R1-Distill-Llama-32B-litert-lm/resolve/main/DeepSeek-R1-Distill-Llama-32B.litertlm",
            "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Llama-32B-litert-lm/resolve/main/DeepSeek-R1-Distill-Llama-32B.litertlm"
        ],
        "深度求索 · 32B 蒸馏版 · 推理能力顶级 · 媲美 o1 水平"
    )
];

function getRecommendedModel(totalRAMGB) {
    for (let i = PC_MODELS.length - 1; i >= 0; i--) {
        if (totalRAMGB >= PC_MODELS[i].recommendedRAMGB) {
            return PC_MODELS[i];
        }
    }
    return PC_MODELS[0];
}

function getMinimalModel(totalRAMGB) {
    for (let model of PC_MODELS) {
        if (totalRAMGB >= model.minRAMGB) {
            return model;
        }
    }
    return PC_MODELS[0];
}

function getModelById(id) {
    return PC_MODELS.find(m => m.id === id);
}

module.exports = {
    ModelConfig,
    PC_MODELS,
    getRecommendedModel,
    getMinimalModel,
    getModelById
};
