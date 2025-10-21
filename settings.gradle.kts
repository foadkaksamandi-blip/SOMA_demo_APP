// settings.gradle.kts  — ریشه پروژه
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
rootProject.name = "SOMA_demo_APP"

// اگر دو ماژول داری:
include(":consumer-app")
include(":merchant-app")
