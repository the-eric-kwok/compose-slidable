plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.erickwok.composeslidable.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.erickwok.composeslidable.demo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":demoShared"))
    implementation(libs.androidx.activity.compose)

    debugImplementation(libs.compose.ui.tooling)
}
