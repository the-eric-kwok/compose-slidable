# Compose Slidable

[English](./README.md)

一个以 `compose-slidable` 依赖库为主、demo 作为附属样例的 Compose Multiplatform 工程，包含：

- `compose-slidable/`：可复用的 CMP 依赖库
- `demo/androidApp/`：Android 示例应用
- `demo/shared/`：Android / iOS 共用的 demo 代码
- `demo/iosApp/`：iOS 示例宿主

## Demo

<video src="./docs/assets/demo.mp4" width="320" height="240" controls>Demo video</video>

## 本地运行 demo

```bash
./gradlew :androidApp:assembleDebug
```

## 平台支持

当前库模块已切到 Kotlin Multiplatform，源码位于 `commonMain`，默认配置了：

- Android
- iOS (`iosX64` / `iosArm64` / `iosSimulatorArm64`)

## 作为依赖使用

在 `settings.gradle.kts` 中添加 Maven 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://the-eric-kwok.github.io/compose-slidable")
    }
}
```

在模块的 `build.gradle.kts` 中添加依赖：

```kotlin
// Android 项目
implementation("com.erickwok:compose-slidable:<version>")

// KMP 项目
commonMain.dependencies {
    implementation("com.erickwok:compose-slidable:<version>")
}
```

将 `<version>` 替换为发布版本号（如 `0.1.0`）。

### 其他方式

1. 直接项目依赖

```kotlin
implementation(project(":compose-slidable"))
```

2. 发布到本地 Maven 后再引用

```bash
./gradlew :compose-slidable:publishToMavenLocal
```

```kotlin
implementation("com.erickwok:compose-slidable:0.1.0")
```

## 核心 API

```kotlin
SwipeActionCard(
    startActions = listOf(...),
    endActions = listOf(...)
) {
    // 你的卡片内容
}
```
