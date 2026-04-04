# Gradle Wrapper 生成说明

由于 gradlew 和 gradlew.bat 文件较大，需要在本地生成。

## 生成步骤

### 前置要求
- 已安装 Gradle 8.0 或更高版本

### 生成方法

在 `Android/` 目录下运行：

```bash
cd Android
gradle wrapper --gradle-version 8.0
```

这将生成以下文件：
- `gradlew` (Linux/Mac)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`

## 或者

如果你没有安装 Gradle，可以：
1. 打开 Android Studio
2. 打开项目
3. Android Studio 会自动下载并配置 Gradle
4. Gradle Wrapper 文件会自动生成

## 注意

生成 gradlew 文件后，请确保：
- `gradlew` 文件有执行权限（Linux/Mac）：`chmod +x gradlew`
- 将这些文件提交到 Git
