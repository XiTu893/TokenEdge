const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');
const { EventEmitter } = require('events');

class ModelDownloader extends EventEmitter {
    constructor() {
        super();
        this.downloads = new Map();
    }

    async downloadWithRetry(downloadUrls, destination, options = {}) {
        const downloadId = Date.now().toString();
        const destPath = path.resolve(destination);
        const tempPath = destPath + '.download';
        const sourceNames = ['ModelScope', 'ghproxy.com', 'hf-mirror.com', 'Hugging Face'];
        
        let urlIndex = 0;
        let attemptCount = 0;

        this.downloads.set(downloadId, {
            downloadUrls,
            destPath,
            tempPath,
            urlIndex,
            attemptCount,
            progress: 0,
            status: 'downloading',
            startTime: Date.now(),
            currentSource: sourceNames[0]
        });

        while (true) {
            const currentUrl = downloadUrls[urlIndex];
            const currentSourceName = sourceNames[urlIndex];
            attemptCount++;

            this.downloads.get(downloadId).currentSource = currentSourceName;
            this.downloads.get(downloadId).urlIndex = urlIndex;
            this.downloads.get(downloadId).attemptCount = attemptCount;

            this.emit('source-change', { 
                downloadId, 
                source: currentSourceName, 
                attempt: attemptCount 
            });

            try {
                await this.ensureDirectory(path.dirname(destPath));
                await this.downloadFile(currentUrl, tempPath, downloadId);
                
                if (fs.existsSync(destPath)) {
                    fs.unlinkSync(destPath);
                }
                fs.renameSync(tempPath, destPath);

                const download = this.downloads.get(downloadId);
                download.status = 'completed';
                download.progress = 100;
                
                this.emit('progress', { downloadId, ...download });
                this.emit('completed', { downloadId, path: destPath });
                
                this.downloads.delete(downloadId);
                return { success: true, path: destPath, source: currentSourceName };
            } catch (error) {
                this.emit('error', { 
                    downloadId, 
                    error: error.message,
                    source: currentSourceName 
                });

                urlIndex = (urlIndex + 1) % downloadUrls.length;
                
                if (urlIndex === 0) {
                    this.emit('waiting', { downloadId, message: '所有源尝试完成，等待2秒后重试...' });
                    await this.sleep(2000);
                }
            }
        }
    }

    async download(url, destination, options = {}) {
        const downloadId = Date.now().toString();
        const destPath = path.resolve(destination);
        const tempPath = destPath + '.download';

        this.downloads.set(downloadId, {
            url,
            destPath,
            tempPath,
            progress: 0,
            status: 'downloading',
            startTime: Date.now()
        });

        try {
            await this.ensureDirectory(path.dirname(destPath));
            await this.downloadFile(url, tempPath, downloadId);
            
            if (fs.existsSync(destPath)) {
                fs.unlinkSync(destPath);
            }
            fs.renameSync(tempPath, destPath);

            const download = this.downloads.get(downloadId);
            download.status = 'completed';
            download.progress = 100;
            
            this.emit('progress', { downloadId, ...download });
            this.emit('completed', { downloadId, path: destPath });
            
            this.downloads.delete(downloadId);
            return { success: true, path: destPath };
        } catch (error) {
            if (fs.existsSync(tempPath)) {
                fs.unlinkSync(tempPath);
            }
            
            const download = this.downloads.get(downloadId);
            if (download) {
                download.status = 'failed';
                download.error = error.message;
                this.emit('error', { downloadId, error: error.message });
            }
            
            return { success: false, error: error.message };
        }
    }

    downloadFile(url, tempPath, downloadId) {
        return new Promise((resolve, reject) => {
            const protocol = url.startsWith('https') ? https : http;
            
            const request = protocol.get(url, (response) => {
                if (response.statusCode >= 300 && response.statusCode < 400 && response.headers.location) {
                    return this.downloadFile(response.headers.location, tempPath, downloadId)
                        .then(resolve)
                        .catch(reject);
                }

                if (response.statusCode !== 200) {
                    reject(new Error(`HTTP ${response.statusCode}`));
                    return;
                }

                const totalSize = parseInt(response.headers['content-length'], 10);
                let downloaded = 0;
                const writeStream = fs.createWriteStream(tempPath);

                response.on('data', (chunk) => {
                    downloaded += chunk.length;
                    writeStream.write(chunk);
                    
                    if (totalSize) {
                        const progress = Math.round((downloaded / totalSize) * 100);
                        const download = this.downloads.get(downloadId);
                        if (download) {
                            download.progress = progress;
                            download.downloaded = downloaded;
                            download.total = totalSize;
                            this.emit('progress', { downloadId, ...download });
                        }
                    }
                });

                response.on('end', () => {
                    writeStream.end();
                    resolve();
                });

                response.on('error', (error) => {
                    writeStream.close();
                    reject(error);
                });

                writeStream.on('finish', () => {
                    resolve();
                });

                writeStream.on('error', (error) => {
                    reject(error);
                });
            });

            request.on('error', (error) => {
                reject(error);
            });

            request.setTimeout(30000, () => {
                request.destroy();
                reject(new Error('下载超时'));
            });
        });
    }

    ensureDirectory(dirPath) {
        return new Promise((resolve, reject) => {
            fs.mkdir(dirPath, { recursive: true }, (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    getDownloads() {
        return Array.from(this.downloads.values());
    }
}

module.exports = ModelDownloader;
