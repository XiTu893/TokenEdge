# TokenEdge PC 端一键打包部署指南

## 🚀 快速开始

### 方式一：使用 pkg 打包（推荐）

**打包成独立可执行文件**（无需安装 Node.js）：

```bash
# Windows
npm install
npm run pkg:win

# Linux
npm install
npm run pkg:linux

# Mac
npm install
npm run pkg:mac
```

**打包产物位置**：
- Windows: `dist/token-edge/token-edge.exe`
- Linux: `dist/token-edge/token-edge`
- Mac: `dist/token-edge/token-edge-macos`

---

### 方式二：使用 Electron 打包

**打包成安装包**：

```bash
# Windows 安装包
npm install
npm run build:win

# Mac 安装包
npm install
npm run build:mac

# Linux 安装包
npm install
npm run build:linux
```

**打包产物位置**：
- Windows: `dist/TokenEdge Setup.exe`
- Mac: `dist/TokenEdge.dmg`
- Linux: `dist/TokenEdge.AppImage`

---

## 📦 打包说明

### pkg 打包优势

- ✅ **单文件**：只有一个 .exe 文件
- ✅ **无需安装**：双击即可运行
- ✅ **包含所有依赖**：Node.js、npm 包、llama.cpp 都打包进去
- ✅ **便携**：可以放在 U 盘随身携带

### Electron 打包优势

- ✅ **完整应用**：包含 GUI 界面
- ✅ **自动更新**：支持在线更新
- ✅ **系统集成**：创建桌面快捷方式、开始菜单

---

## 🔧 本地测试

### 开发模式

```bash
npm install
npm run dev
```

### 运行 Electron

```bash
npm install
npm start
```

---

## 📋 部署步骤

### pkg 打包版本部署

1. **下载打包文件**
   - 将 `dist/token-edge/token-edge.exe` 复制到目标机器

2. **运行**
   ```bash
   token-edge.exe
   ```

3. **访问 API**
   - 打开浏览器访问：`http://localhost:3000`

---

### Electron 打包版本部署

1. **运行安装程序**
   - Windows: 双击 `TokenEdge Setup.exe`
   - Mac: 打开 `TokenEdge.dmg` 并拖拽到 Applications
   - Linux: 运行 `TokenEdge.AppImage`

2. **启动应用**
   - 从桌面快捷方式或开始菜单启动

3. **使用应用**
   - 选择模型 → 下载 → 启动 → 聊天

---

## 🎯 模型文件格式

PC 端使用 **GGUF 格式**（llama.cpp 原生支持），需要在 ModelConfig 中配置 GGUF 格式的下载地址。

**示例**：
```javascript
downloadUrls: [
    "https://modelscope.cn/models/xxx/gemma-4-e2b.gguf",
    "https://ghproxy.com/https://huggingface.co/xxx/gemma-4-e2b.gguf",
    "https://hf-mirror.com/xxx/gemma-4-e2b.gguf",
    "https://huggingface.co/xxx/gemma-4-e2b.gguf"
]
```

---

## 💡 常见问题

### Q: 打包后文件很大？
A: 是的，因为包含了 Node.js 运行时和 llama.cpp 二进制文件。通常 100-200MB。

### Q: 可以在没有网络的电脑上使用吗？
A: 可以！打包后的可执行文件完全独立，但模型文件需要单独下载。

### Q: 支持 GPU 加速吗？
A: node-llama-cpp 会自动检测并使用可用的 GPU（CUDA/Metal/Vulkan）。

### Q: 如何更新模型？
A: 模型文件存储在用户数据目录，可以直接删除重新下载。

---

## 📊 打包产物对比

| 打包方式 | 文件大小 | 是否需要安装 | 包含 GUI | 推荐场景 |
|----------|----------|--------------|----------|----------|
| **pkg** | 100-200MB | ❌ 否 | ❌ 否 | 服务器部署、命令行使用 |
| **Electron** | 200-400MB | ✅ 是 | ✅ 是 | 桌面应用、普通用户 |

---

## 作者信息

**溪土红薯（XiTu893）**

- GitHub: <https://github.com/XiTu893>
- 邮箱: <28491599@qq.com>
- 所在地: 上海
- 工作室: 溪土工作室
