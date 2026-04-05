import * as fs from 'fs';
import * as path from 'path';
import * as https from 'https';
import * as http from 'http';
import { EventEmitter } from 'events';

interface DownloadState {
  downloadUrls: string[];
  destPath: string;
  tempPath: string;
  urlIndex: number;
  attemptCount: number;
  progress: number;
  status: 'downloading' | 'completed' | 'failed';
  startTime: number;
  currentSource: string;
  downloaded?: number;
  total?: number;
  error?: string;
}

export class ModelDownloader extends EventEmitter {
  private downloads: Map<string, DownloadState> = new Map();

  constructor() {
    super();
  }

  async downloadWithRetry(
    downloadUrls: string[],
    destination: string,
    options: any = {}
  ): Promise<{ success: boolean; path?: string; source?: string; error?: string }> {
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

      const currentState = this.downloads.get(downloadId)!;
      currentState.currentSource = currentSourceName;
      currentState.urlIndex = urlIndex;
      currentState.attemptCount = attemptCount;

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

        const download = this.downloads.get(downloadId)!;
        download.status = 'completed';
        download.progress = 100;
        
        this.emit('progress', { downloadId, ...download });
        this.emit('completed', { downloadId, path: destPath });
        
        this.downloads.delete(downloadId);
        return { success: true, path: destPath, source: currentSourceName };
      } catch (error: any) {
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

  async download(
    url: string,
    destination: string,
    options: any = {}
  ): Promise<{ success: boolean; path?: string; error?: string }> {
    const downloadId = Date.now().toString();
    const destPath = path.resolve(destination);
    const tempPath = destPath + '.download';

    this.downloads.set(downloadId, {
      downloadUrls: [url],
      destPath,
      tempPath,
      urlIndex: 0,
      attemptCount: 0,
      progress: 0,
      status: 'downloading',
      startTime: Date.now(),
      currentSource: 'direct'
    });

    try {
      await this.ensureDirectory(path.dirname(destPath));
      await this.downloadFile(url, tempPath, downloadId);
      
      if (fs.existsSync(destPath)) {
        fs.unlinkSync(destPath);
      }
      fs.renameSync(tempPath, destPath);

      const download = this.downloads.get(downloadId)!;
      download.status = 'completed';
      download.progress = 100;
      
      this.emit('progress', { downloadId, ...download });
      this.emit('completed', { downloadId, path: destPath });
      
      this.downloads.delete(downloadId);
      return { success: true, path: destPath };
    } catch (error: any) {
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

  private downloadFile(url: string, tempPath: string, downloadId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const protocol = url.startsWith('https') ? https : http;
      
      const request = protocol.get(url, (response) => {
        if (response.statusCode! >= 300 && response.statusCode! < 400 && response.headers.location) {
          return this.downloadFile(response.headers.location, tempPath, downloadId)
            .then(resolve)
            .catch(reject);
        }

        if (response.statusCode !== 200) {
          reject(new Error(`HTTP ${response.statusCode}`));
          return;
        }

        const totalSize = parseInt(response.headers['content-length'] || '0', 10);
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

  private ensureDirectory(dirPath: string): Promise<void> {
    return new Promise((resolve, reject) => {
      fs.mkdir(dirPath, { recursive: true }, (err) => {
        if (err) reject(err);
        else resolve();
      });
    });
  }

  private sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  getDownloads(): DownloadState[] {
    return Array.from(this.downloads.values());
  }
}

export default ModelDownloader;
