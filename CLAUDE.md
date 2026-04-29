# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ShellTerminal 是一个支持中文的 Android Shell 终端应用，支持 SSH 连接和保存主机/用户认证信息。

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: API 26 (Android 8.0)
- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **SSH**: JSch (com.github.mwiede:jsch:0.2.17)
- **加密存储**: EncryptedSharedPreferences

## 项目结构

```
app/src/main/java/com/shellterminal/
├── data/
│   ├── local/SecureStorage.kt      # EncryptedSharedPreferences 加密存储
│   └── repository/                 # Repository 实现
├── domain/
│   ├── model/SSHHost.kt           # SSHHost 数据模型
│   ├── repository/                # Repository 接口
│   └── usecase/                  # 业务逻辑 (HostUseCases, SSHConnectionManager)
├── presentation/
│   ├── home/                      # 主页面 (终端 + 主机列表)
│   ├── hosteditor/                # 主机编辑页面
│   ├── terminal/TerminalView.kt   # 终端视图组件
│   └── theme/Theme.kt             # Material 3 主题配置
├── di/                            # Hilt 依赖注入模块
├── MainActivity.kt
└── ShellTerminalApp.kt            # Application 类
```

## 常用命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk

# 清理并重新构建
./gradlew clean assembleDebug

# 运行 lint 检查
./gradlew lint

# 启动带调试的构建
./gradlew assembleDebug --stacktrace
```

## 数据模型

### SSHHost
```kotlin
data class SSHHost(
    val id: String,
    val name: String,           // 显示名称
    val host: String,            // IP 或域名
    val port: Int = 22,
    val username: String,
    val authType: AuthType,     // PASSWORD | PRIVATE_KEY
    val password: String? = null,
    val privateKey: String? = null,
    val passphrase: String? = null
)
```

## 关键实现

- **SSH连接**: `domain/usecase/SSHConnectionManager.kt` - 使用 JSch 实现 SSH 连接，支持 PTY
- **加密存储**: `data/local/SecureStorage.kt` - 使用 EncryptedSharedPreferences 安全存储主机信息
- **终端视图**: `presentation/terminal/TerminalView.kt` - 基础终端显示组件

## 注意事项

- SSH 认证信息使用 EncryptedSharedPreferences 加密存储
- 私钥支持两种方式：文件选择或文本粘贴
- 连接状态通过 StateFlow 实时更新 UI

## CI/CD

使用 GitHub Actions 自动构建：

```bash
# 查看工作流
ls -la .github/workflows/

# 主要工作流: .github/workflows/ci.yml
# - Push/PR 时自动构建 Debug APK
# - 运行 Lint 检查
# - 自动上传 APK 到 Actions artifacts
```

### GitHub Actions 配置

1. 提交代码到 GitHub 后，Actions 会自动运行
2. Debug APK 构建完成后可在 Actions 页面的 Artifacts 下载
3. 如需 Release APK，需要配置签名 keystore secrets

### Secrets 配置（Release 构建需要）

在 GitHub repo Settings > Secrets 中配置：
- `KEYSTORE_BASE64`: Base64 编码的 keystore 文件
- `KEYSTORE_PASSWORD`: keystore 密码
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码