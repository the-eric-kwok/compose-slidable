pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-slidable"

include(":compose-slidable")
project(":compose-slidable").projectDir = file("compose-slidable")
include(":demoShared")
project(":demoShared").projectDir = file("demo/shared")
include(":androidApp")
project(":androidApp").projectDir = file("demo/androidApp")
