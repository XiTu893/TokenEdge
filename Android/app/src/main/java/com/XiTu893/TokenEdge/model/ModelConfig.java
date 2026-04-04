package com.XiTu893.TokenEdge.model;

public class ModelConfig {
    public String id;
    public String name;
    public String displayName;
    public String sizeGB;
    public long sizeBytes;
    public int minRAMGB;
    public int recommendedRAMGB;
    public int[] contextOptions;
    public int defaultContextIndex;
    public String[] downloadUrls;
    public String description;

    public enum ModelStatus {
        NOT_DOWNLOADED,
        DOWNLOADING,
        DOWNLOADED,
        STARTING,
        RUNNING
    }

    public ModelConfig(String id, String name, String displayName, String sizeGB, 
                       long sizeBytes, int minRAMGB, int recommendedRAMGB, 
                       int[] contextOptions, int defaultContextIndex,
                       String[] downloadUrls, String description) {
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

    public static final ModelConfig[] GEMMA4_MODELS = {
        new ModelConfig(
            "gemma-4-e2b",
            "Gemma 4 E2B",
            "Gemma 4 E2B",
            "2.5",
            2684354560L,
            4,
            6,
            new int[]{8192, 32768, 65536, 131072},
            1,
            new String[]{
                "https://modelscope.cn/models/litert-community/gemma-4-E2B-it-litert-lm/resolve/master/gemma-4-E2B-it.litertlm",
                "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
                "https://hf-mirror.com/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
                "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
            },
            "端侧版 · 2.3B 有效参数 · 极致轻量 · 适合手机和 IoT 设备"
        ),
        new ModelConfig(
            "gemma-4-e4b",
            "Gemma 4 E4B",
            "Gemma 4 E4B",
            "5",
            5368709120L,
            6,
            8,
            new int[]{8192, 32768, 65536, 131072},
            2,
            new String[]{
                "https://modelscope.cn/models/litert-community/gemma-4-E4B-it-litert-lm/resolve/master/gemma-4-E4B-it.litertlm",
                "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
                "https://hf-mirror.com/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
                "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm"
            },
            "端侧版 · 4.5B 有效参数 · 更强性能 · 适合高端手机"
        ),
        new ModelConfig(
            "gemma-4-26b-a4b",
            "Gemma 4 26B-A4B",
            "Gemma 4 26B-A4B",
            "18",
            19327352832L,
            12,
            16,
            new int[]{32768, 65536, 131072, 262144},
            2,
            new String[]{
                "https://modelscope.cn/models/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/master/gemma-4-26B-A4B-it.litertlm",
                "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm",
                "https://hf-mirror.com/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm",
                "https://huggingface.co/litert-community/gemma-4-26B-A4B-it-litert-lm/resolve/main/gemma-4-26B-A4B-it.litertlm"
            },
            "工作站版 · 混合专家模型 · 260亿总参数/38亿有效参数 · 适合 PC/工作站"
        ),
        new ModelConfig(
            "gemma-4-31b-dense",
            "Gemma 4 31B Dense",
            "Gemma 4 31B Dense",
            "20",
            21474836480L,
            16,
            24,
            new int[]{32768, 65536, 131072, 262144},
            2,
            new String[]{
                "https://modelscope.cn/models/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/master/gemma-4-31B-Dense-it.litertlm",
                "https://ghproxy.com/https://huggingface.co/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm",
                "https://hf-mirror.com/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm",
                "https://huggingface.co/litert-community/gemma-4-31B-Dense-it-litert-lm/resolve/main/gemma-4-31B-Dense-it.litertlm"
            },
            "旗舰版 · 纯稠密模型 · 310亿参数 · 全球前三性能 · 适合高端工作站"
        )
    };

    public static ModelConfig getRecommendedModel(int totalRAMGB) {
        for (int i = GEMMA4_MODELS.length - 1; i >= 0; i--) {
            if (totalRAMGB >= GEMMA4_MODELS[i].recommendedRAMGB) {
                return GEMMA4_MODELS[i];
            }
        }
        return GEMMA4_MODELS[0];
    }

    public static ModelConfig getMinimalModel(int totalRAMGB) {
        for (ModelConfig model : GEMMA4_MODELS) {
            if (totalRAMGB >= model.minRAMGB) {
                return model;
            }
        }
        return GEMMA4_MODELS[0];
    }
}
