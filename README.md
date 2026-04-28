# Compose Slidable

[中文文档](./README.zh_CN.md)

A Compose Multiplatform project centered on the `compose-slidable` library, with a demo as a companion example. It includes:

- `compose-slidable/`: Reusable CMP library
- `demo/androidApp/`: Android sample app
- `demo/shared/`: Shared demo code for Android / iOS
- `demo/iosApp/`: iOS sample host

## Demo

<img src="./docs/assets/demo.gif" width=270></img>

## Run the Demo Locally

```bash
./gradlew :androidApp:assembleDebug
```

## Platform Support

The library module uses Kotlin Multiplatform with source in `commonMain`, configured for:

- Android
- iOS (`iosX64` / `iosArm64` / `iosSimulatorArm64`)

## Installation

Add the Maven repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://the-eric-kwok.github.io/compose-slidable")
    }
}
```

Add the dependency in your module's `build.gradle.kts`:

```kotlin
// Android project
implementation("com.erickwok:compose-slidable:<version>")

// KMP project
commonMain.dependencies {
    implementation("com.erickwok:compose-slidable:<version>")
}
```

Replace `<version>` with a release version (e.g. `0.1.0`).

### Other Options

1. Local project dependency

```kotlin
implementation(project(":compose-slidable"))
```

2. Publish to local Maven then reference it

```bash
./gradlew :compose-slidable:publishToMavenLocal
```

```kotlin
implementation("com.erickwok:compose-slidable:0.1.0")
```

## Core API

```kotlin
SwipeActionCard(
    startActions = listOf(...),
    endActions = listOf(...)
) {
    // Your card content
}
```
