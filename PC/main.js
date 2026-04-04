const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { spawn } = require('child_process');
const net = require('net');
const ModelDownloader = require('./downloader');
const { getModelById, GEMMA4_MODELS } = require('./model/ModelConfig');

let mainWindow;
let apiServer;
let modelPath = '';
let isServerRunning = false;
let currentPort = -1;
const START_PORT = 3000;
const MAX_PORT = 3999;
const downloader = new ModelDownloader();

function findAvailablePort(startPort, maxPort) {
    return new Promise((resolve, reject) => {
        const checkPort = (port) => {
            if (port > maxPort) {
                reject(new Error('No available port found'));
                return;
            }

            const server = net.createServer();
            server.listen(port, () => {
                server.close();
                resolve(port);
            });
            server.on('error', () => {
                checkPort(port + 1);
            });
        };
        checkPort(startPort);
    });
}

function getModelDir() {
    const userDataPath = app.getPath('userData');
    const modelDir = path.join(userDataPath, 'models');
    if (!fs.existsSync(modelDir)) {
        fs.mkdirSync(modelDir, { recursive: true });
    }
    return modelDir;
}

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 900,
        title: 'TokenEdge - 边缘设备部署大模型Token服务',
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false
        }
    });

    mainWindow.loadFile('index.html');
}

function getTotalRAM() {
    return Math.round(os.totalmem() / (1024 * 1024 * 1024));
}

app.whenReady().then(() => {
    createWindow();
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
        createWindow();
    }
});

app.on('before-quit', () => {
    if (apiServer) {
        apiServer.kill();
    }
});

ipcMain.handle('get-total-ram', async () => {
    return getTotalRAM();
});

ipcMain.handle('download-model', async (event, downloadUrls, modelId) => {
    const modelDir = getModelDir();
    const model = getModelById(modelId);
    if (!model) {
        return { success: false, error: 'Model not found' };
    }

    const fileName = `${modelId}.litertlm`;
    const destPath = path.join(modelDir, fileName);

    downloader.on('progress', (data) => {
        mainWindow.webContents.send('download-progress', { ...data, modelId });
    });

    downloader.on('source-change', (data) => {
        mainWindow.webContents.send('download-source-change', { ...data, modelId });
    });

    downloader.on('error', (data) => {
        mainWindow.webContents.send('download-error', { ...data, modelId });
    });

    downloader.on('waiting', (data) => {
        mainWindow.webContents.send('download-waiting', { ...data, modelId });
    });

    try {
        const result = await downloader.downloadWithRetry(downloadUrls, destPath);
        return result;
    } catch (error) {
        return { success: false, error: error.message };
    }
});

ipcMain.handle('start-service', async (event, config) => {
    if (isServerRunning) {
        return { success: false, message: '服务器已在运行中' };
    }

    const model = getModelById(config.modelId);
    if (!model) {
        return { success: false, message: 'Model not found' };
    }

    const modelDir = getModelDir();
    const modelPath = path.join(modelDir, `${config.modelId}.litertlm`);
    
    if (!fs.existsSync(modelPath)) {
        return { success: false, message: 'Model file not found, please download first' };
    }

    try {
        const port = await findAvailablePort(START_PORT, MAX_PORT);
        currentPort = port;
        config.port = port;
        config.modelPath = modelPath;
        
        const serverScript = path.join(__dirname, 'server.js');
        apiServer = spawn('node', [serverScript, JSON.stringify(config)]);
        
        apiServer.stdout.on('data', (data) => {
            mainWindow.webContents.send('server-log', data.toString());
        });

        apiServer.stderr.on('data', (data) => {
            mainWindow.webContents.send('server-log', `[ERROR] ${data.toString()}`);
        });

        apiServer.on('close', (code) => {
            isServerRunning = false;
            currentPort = -1;
            mainWindow.webContents.send('server-stopped', code);
        });

        isServerRunning = true;
        return { success: true, message: '服务器启动成功', port: port };
    } catch (error) {
        return { success: false, message: error.message };
    }
});

ipcMain.handle('stop-service', async () => {
    if (apiServer) {
        apiServer.kill();
        apiServer = null;
        isServerRunning = false;
        currentPort = -1;
        return { success: true, message: '服务器已停止' };
    }
    return { success: false, message: '没有运行中的服务器' };
});

ipcMain.handle('get-server-status', async () => {
    return { running: isServerRunning, port: currentPort };
});

ipcMain.handle('open-chat', async (event, config) => {
    const chatPath = path.join(__dirname, 'test-chat.html');
    mainWindow.loadFile(chatPath);
    return { success: true };
});

ipcMain.handle('save-config', async (event, config) => {
    const configPath = path.join(app.getPath('userData'), 'config.json');
    fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
    return { success: true };
});

ipcMain.handle('load-config', async () => {
    const configPath = path.join(app.getPath('userData'), 'config.json');
    if (fs.existsSync(configPath)) {
        return JSON.parse(fs.readFileSync(configPath, 'utf8'));
    }
    return { totalRAMGB: getTotalRAM() };
});
