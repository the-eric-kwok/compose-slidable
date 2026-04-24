import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.erickwok.composeslidable.demoshared"
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "SwipeActionCard iOS demo shared module"
        homepage = "https://github.com/madou-dev/swipe-action-card-compose"
        ios.deploymentTarget = "14.0"

        framework {
            baseName = "DemoShared"
            isStatic = true
        }

        podfile = project.file("../iosApp/Podfile")
        xcodeConfigurationToNativeBuildType["Debug"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["Release"] = NativeBuildType.RELEASE
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":compose-slidable"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui.tooling.preview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
