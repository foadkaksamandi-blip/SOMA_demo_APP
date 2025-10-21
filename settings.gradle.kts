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

// ماژول‌ها را در مرحله‌های بعد اضافه می‌کنیم
// فاز 2 و 3:
// include(":consumer-app", ":merchant-app")
