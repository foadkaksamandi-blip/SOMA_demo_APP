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

// ⚠️ نام پوشه‌ها رو دقیق بنویس (حروف دقیق مثل پوشه‌ها در گیت‌هاب)
include(":consumer-app")
include(":merchant-app")
